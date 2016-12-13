package antworld.client;

import antworld.common.*;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This class contains all the methods to connect to the server and to
 * control the ants in the application.
 */
public class ClientRandomWalk
{
  private static final boolean DEBUG = false;
  private final TeamNameEnum myTeam;
  private static final long password = 962740848319L;//Each team has been assigned a random password.
  private ObjectInputStream inputStream = null;
  private ObjectOutputStream outputStream = null;
  private boolean isConnected = false;
  private NestNameEnum myNestName = null;
  private int centerX, centerY;
  
  public static int mapWidth, mapHeight;
  public int scoreToAntRatio = 1;
  
  static ClientCell[][] world; //contains all the land types of the map being used
  
  private Socket clientSocket;
  
  private int numThreads = 16;
  private int antListSize = Constants.INITIAL_ANT_SPAWN_COUNT;
  private int swarmAssignNum = 0;
  private ExecutorService executor = Executors.newFixedThreadPool(numThreads);
  private ArrayList<ArrayList<AntData>> antDataListsForThreads = new ArrayList<>();
  private ArrayList<WorkerThread> workerThreads = new ArrayList<>();
  private ArrayList<Swarm> swarmList = new ArrayList<>();
  ArrayList<ClientCell> nestCenterCells = new ArrayList<>();
  volatile boolean mapIsRead = false;
  
  /**
   * Contains the code to connect to the server and intialize game values.
   *
   * @param host
   * @param portNumber
   * @param team
   */
  public ClientRandomWalk(String host, int portNumber, TeamNameEnum team)
  {
    myTeam = team;
    System.out.println("Starting " + team + " on " + host + ":" + portNumber + " at "
            + System.currentTimeMillis());
    
    isConnected = openConnection(host, portNumber);
    if (!isConnected) System.exit(0);
    CommData data = obtainNest();
    (new WorkerThread(this)).start(); // will build the map
    initializeAntDataLists();
    assignAntsToWorkerThreads(data);
    initializeSwarms(data);
    assignAntsToSwarm(data);
    mainGameLoop(data);
    closeAll();
  }
  
  private void assignAntsToSwarm(CommData commData)
  {
    int swarmNum;
    for (AntData antData : commData.myAntList)
    {
      swarmNum = antData.id % 4;
      swarmList.get(swarmNum).addAntToIDSet(antData);
    }
  }
  
  private void initializeSwarms(CommData commData)
  {
    for (int i = 0; i < 4; i++)
    {
      SwarmAI swarmAI = new SwarmAI(i, commData, null);
      Swarm swarm = new Swarm(i, centerX, centerY, 50, swarmAI, commData);
      swarmAI.setMySwarm(swarm);
      swarm.setNestCenterCells(nestCenterCells);
      swarmList.add(i, swarm);
    }
  }
  
  private void assignAntsToWorkerThreads(CommData commData)
  {
    int count = 0;
    int index = 0;
    int step = commData.myAntList.size() / 4;
    
    for (AntData antData : commData.myAntList)
    {
      if (count == step)
      {
        index++;
        count = 0;
      }
      antDataListsForThreads.get(index).add(antData);
      count++;
    }
  }
  
  
  private void initializeAntDataLists()
  {
    for (int i = 0; i < numThreads; i++)
    {
      ArrayList<AntData> antDataList = new ArrayList<>();
      antDataListsForThreads.add(i, antDataList);
    }
  }
  
  /**
   * Reads the map to be used in the application to help with the decision tree.
   *
   * @param map - A .png file to be used as map for the application
   */
  public void readMap(BufferedImage map)
  {
    int mapWidth = map.getWidth();
    int mapHeight = map.getHeight();
    this.mapHeight = mapHeight;
    this.mapWidth = mapWidth;
    world = new ClientCell[mapWidth][mapHeight];
    
    for (int y = 0; y < mapHeight; y++)
    {
      for (int x = 0; x < mapWidth; x++)
      {
        int rgb = (map.getRGB(x, y) & 0x00FFFFFF);
        LandType landType = LandType.GRASS;
        int height = 0;
        boolean isNestCenter = false;
        if (rgb == 0xF0E68C)
        {
          landType = LandType.NEST;
        }
        else if (rgb == 0x1E90FF)
        {
          landType = LandType.WATER;
        }
        else if (rgb == 0x000000)
        {
          //treat black dots as grass
          landType = LandType.GRASS;
          isNestCenter = true;
        }
        else
        {
          int g = (rgb & 0x0000FF00) >> 8;
          height = g - 55;
        }
        world[x][y] = new ClientCell(landType, height, x, y);
        if (isNestCenter && x != centerX && y != centerY)
        {
          nestCenterCells.add(world[x][y]);
        }
      }
    }
  }
  
  /**
   * Helps with reading the map in readMap()
   */
  public void createMap()
  {
    String mapName = "AntWorld.png";
    BufferedImage map = Util.loadImage(mapName, null);
    readMap(map);
    
  }
  
  private boolean openConnection(String host, int portNumber)
  {
    try
    {
      clientSocket = new Socket(host, portNumber);
    } catch (UnknownHostException e)
    {
      System.err.println("ClientRandomWalk Error: Unknown Host " + host);
      e.printStackTrace();
      return false;
    } catch (IOException e)
    {
      System.err.println("ClientRandomWalk Error: Could not open connection to " + host + " on port " + portNumber);
      e.printStackTrace();
      return false;
    }
    
    try
    {
      outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
      inputStream = new ObjectInputStream(clientSocket.getInputStream());
      
    } catch (IOException e)
    {
      System.err.println("ClientRandomWalk Error: Could not open i/o streams");
      e.printStackTrace();
      return false;
    }
    
    return true;
    
  }
  
  /**
   * Close all resources not needed.
   */
  public void closeAll()
  {
    System.out.println("ClientRandomWalk.closeAll()");
    {
      try
      {
        if (outputStream != null) outputStream.close();
        if (inputStream != null) inputStream.close();
        clientSocket.close();
      } catch (IOException e)
      {
        System.err.println("ClientRandomWalk Error: Could not close");
        e.printStackTrace();
      }
    }
  }
  
  /**
   * This method is called ONCE after the socket has been opened.
   * The server assigns a nest to this client with an initial ant population.
   *
   * @return a reusable CommData structure populated by the server.
   */
  public CommData obtainNest()
  {
    CommData data = new CommData(myTeam);
    data.password = password;
    
    if (sendCommData(data))
    {
      try
      {
        if (DEBUG) System.out.println("ClientRandomWalk: listening to socket....");
        data = (CommData) inputStream.readObject();
        if (DEBUG)
          System.out.println("ClientRandomWalk: received <<<<<<<<<" + inputStream.available() + "<...\n" + data);
        
        if (data.errorMsg != null)
        {
          System.err.println("ClientRandomWalk***ERROR***: " + data.errorMsg);
          System.exit(0);
        }
      } catch (IOException e)
      {
        System.err.println("ClientRandomWalk***ERROR***: client read failed");
        e.printStackTrace();
        System.exit(0);
      } catch (ClassNotFoundException e)
      {
        System.err.println("ClientRandomWalk***ERROR***: client sent incorrect common format");
      }
    }
    if (data.myTeam != myTeam)
    {
      System.err.println("ClientRandomWalk***ERROR***: Server returned wrong team name: " + data.myTeam);
      System.exit(0);
    }
    if (data.myNest == null)
    {
      System.err.println("ClientRandomWalk***ERROR***: Server returned NULL nest");
      System.exit(0);
    }
    
    myNestName = data.myNest;
    centerX = data.nestData[myNestName.ordinal()].centerX;
    centerY = data.nestData[myNestName.ordinal()].centerY;
    System.out.println("ClientRandomWalk: ==== Nest Assigned ===>: " + myNestName);
    return data;
  }
  
  /**
   * Spawns a new based on the score
   *
   * @param commData
   */
  public void spawnNewAnt(CommData commData)
  {
    int myScore = 0;
    int antCount = commData.myAntList.size() * 10;
    System.out.println("antCount=" + antCount);
    for (int foodCount : commData.foodStockPile)
    {
      myScore += foodCount;
    }
    AntType[] antTypes = {AntType.ATTACK, AntType.DEFENCE, AntType.MEDIC,
            AntType.SPEED, AntType.VISION, AntType.WORKER};
    
    if (myScore >= antCount * scoreToAntRatio)
    {
      for (AntType antType : antTypes)
      {
        if (commData.foodStockPile[FoodType.MEAT.ordinal()] - antType.getFoodUnitsToSpawn(FoodType.MEAT) >= 0 &&
                commData.foodStockPile[FoodType.NECTAR.ordinal()] - antType.getFoodUnitsToSpawn(FoodType.NECTAR) >= 0 &&
                commData.foodStockPile[FoodType.SEEDS.ordinal()] - antType.getFoodUnitsToSpawn(FoodType.SEEDS) >= 0)
        {
          commData.myAntList.add(new AntData(Constants.UNKNOWN_ANT_ID, antType, commData.myNest, commData.myTeam));
          swarmAssignNum++;
        }
      }
    }
  }
  
  /**
   * Contains the main game loop which chooses an action for each ant
   * for every tick of the server.
   *
   * @param data - the current CommData object to be processed
   */
  public void mainGameLoop(CommData data)
  {
    boolean chooseActionOfAllAntsCompleted = false;
    while (true)
    {
      try
      {
        if (!mapIsRead)
        {
          CommData sendData = data.packageForSendToServer();
          outputStream.writeObject(sendData);
          outputStream.flush();
          outputStream.reset();
          if (DEBUG) System.out.println("ClientRandomWalk: listening to socket....");
          CommData receivedData = (CommData) inputStream.readObject();
          if (DEBUG)
            System.out.println("ClientRandomWalk: received <<<<<<<<<" + inputStream.available() + "<...\n" + receivedData);
          data = receivedData;
          
          if ((myNestName == null) || (data.myTeam != myTeam))
          {
            System.err.println("ClientRandomWalk: !!!!ERROR!!!! " + myNestName);
          }
          continue;
        }
        
        if (antListSize != data.myAntList.size())
        {
          for (AntData antData : data.myAntList)
          {
            for (Swarm swarm : swarmList)
            {
              if (!swarm.contains(antData.id))
              {
                swarm.addAntToIDSet(antData);
              }
            }
          }
          
        }
        boolean allSwarmsReady = true;
        for (Swarm swarm : swarmList)
        {
          if (swarm.turnFinished == false)
          {
            allSwarmsReady = false;
            break;
          }
        }
        
        if (!chooseActionOfAllAntsCompleted)
        {
          chooseActionsOfAllAnts(data);
          chooseActionOfAllAntsCompleted = true;
        }
        
        if (allSwarmsReady)
        {
          chooseActionsOfAllAnts(data);
          spawnNewAnt(data); //try to spawn ants when possible
          
          CommData sendData = data.packageForSendToServer();
          chooseActionOfAllAntsCompleted = false;
          if (DEBUG)
          {
            System.out.println("ClientRandomWalk: Sending>>>>>>>: " + sendData);
          }
          outputStream.writeObject(sendData);
          outputStream.flush();
          outputStream.reset();
          
          //reset swarm's turn status
          for (Swarm swarm : swarmList)
          {
            swarm.turnFinished = false;
          }
          
          if (DEBUG) System.out.println("ClientRandomWalk: listening to socket....");
          CommData receivedData = (CommData) inputStream.readObject();
          if (DEBUG)
            System.out.println("ClientRandomWalk: received <<<<<<<<<" + inputStream.available() + "<...\n" + receivedData);
          data = receivedData;
          
          if ((myNestName == null) || (data.myTeam != myTeam))
          {
            System.err.println("ClientRandomWalk: !!!!ERROR!!!! " + myNestName);
          }
        }
      } catch (IOException e)
      {
        System.err.println("ClientRandomWalk***ERROR***: client read failed");
        e.printStackTrace();
        System.exit(0);
        
      } catch (ClassNotFoundException e)
      {
        System.err.println("ServerToClientConnection***ERROR***: client sent incorrect common format");
        e.printStackTrace();
        System.exit(0);
      }
    }
  }
  
  private boolean sendCommData(CommData data)
  {
    CommData sendData = data.packageForSendToServer();
    try
    {
      if (DEBUG) System.out.println("ClientRandomWalk.sendCommData(" + sendData + ")");
      outputStream.writeObject(sendData);
      outputStream.flush();
      outputStream.reset();
    } catch (IOException e)
    {
      System.err.println("ClientRandomWalk***ERROR***: client read failed");
      e.printStackTrace();
      System.exit(0);
    }
    
    return true;
    
  }
  
  private void chooseActionsOfAllAnts(CommData commData)
  {
    //sets the actions effectively editing the CommData before being sent to the server for each ants
    for (FoodData food : commData.foodSet)
    {
      world[food.gridX][food.gridY].setFoodType(food.foodType);
    }
    for (Swarm swarm : swarmList)
    {
      swarm.setCommData(commData);
      executor.execute(swarm);
    }
  }
  
  
  /**
   * The last argument is taken as the host name.
   * The default host is localhost.
   * Also supports an optional option for the teamname.
   * The default teamname is TeamNameEnum.RANDOM_WALKERS.
   *
   * @param args Array of command-line arguments.
   */
  public static void main(String[] args)
  {
    String serverHost = "foodgame.cs.unm.edu";
    if (args.length > 0) serverHost = args[args.length - 1];
    
    TeamNameEnum team = TeamNameEnum.Arthur_Phil;
    if (args.length > 1)
    {
      team = TeamNameEnum.getTeamByString(args[0]);
    }
    new ClientRandomWalk(serverHost, Constants.PORT, team);
  }
  
}
