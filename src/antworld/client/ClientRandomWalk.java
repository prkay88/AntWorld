package antworld.client;

import antworld.common.*;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientRandomWalk
{
  private static final boolean DEBUG = true;
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

  private RandomWalkAI testAI;


  private Socket clientSocket;


  //A random number generator is created in Constants. Use it.
  //Do not create a new generator every time you want a random number nor
  //  even in every class were you want a generator.
  private static Random random = Constants.random;

  private int numThreads = 16;
  private int antListSize = Constants.INITIAL_ANT_SPAWN_COUNT;
  private int swarmAssignNum =0;
  private ExecutorService executor = Executors.newFixedThreadPool(numThreads);
  private ArrayList<ArrayList<AntData>> antDataListsForThreads = new ArrayList<>();
  private ArrayList<WorkerThread> workerThreads = new ArrayList<>();
  private ArrayList<Swarm> swarmList = new ArrayList<>();
  static volatile int numThreadsReady = 0;
  public static ReadyThreadCounter readyThreadCounter = new ReadyThreadCounter();
  ArrayList<ClientCell> nestCenterCells = new ArrayList<>();
  volatile boolean mapIsRead = false;
  
  public ClientRandomWalk(String host, int portNumber, TeamNameEnum team)
  {
    myTeam = team;
    System.out.println("Starting " + team +" on " + host + ":" + portNumber + " at "
      + System.currentTimeMillis());

    isConnected = openConnection(host, portNumber);
    if (!isConnected) System.exit(0);
    CommData data = obtainNest();
    testAI = new RandomWalkAI(data, null, myNestName);
    testAI.setCenterX(centerX);
    testAI.setCenterY(centerY);
    (new WorkerThread(this)).start(); // will build the map
//    createMap();
    initializeAntDataLists();
    assignAntsToWorkerThreads(data);
//    initiailizeWorkerThreadList();
    initializeSwarms(data);
    assignAntsToSwarm(data);
    mainGameLoop(data);
    closeAll();
  }
  boolean debug = false;
  private void assignAntsToSwarm(CommData commData)
  {
    int swarmNum;
    int count=0;
    for(AntData antData : commData.myAntList)
    {

      swarmNum = antData.id % 4;
      swarmList.get(swarmNum).addAntToIDSet(antData);
      if(count <5) swarmList.get(swarmNum).swarmLocationMap.put(antData.id, 0);
      if(count >=5 && count <12) swarmList.get(swarmNum).swarmLocationMap.put(antData.id, 1);
      if(count >=12) swarmList.get(swarmNum).swarmLocationMap.put(antData.id, 2);
      count++;
    }
  }

  private void initializeSwarms(CommData commData)
  {
    for(int i=0; i<4; i++)
    {
      SwarmAI swarmAI = new SwarmAI(i,commData, null);
      Swarm swarm = new Swarm(i, centerX, centerY, 50, swarmAI, commData);
      swarmAI.setMySwarm(swarm);
      swarm.setNestCenterCells(nestCenterCells);
      swarmList.add(i,swarm);
    }
//    System.exit(1);
  }
//  private void initiailizeWorkerThreadList()
//  {
//    for(int i=0; i<numThreads; i++)
//    {
//      //WorkerThread workerThread = new WorkerThread(null, null);
//      WorkerThread workerThread = new WorkerThread(testAI);
//      workerThreads.add(workerThread);
//    }
//  }
  private void assignAntsToWorkerThreads(CommData commData)
  {
    int count = 0;
    int index =0;
    int step = commData.myAntList.size()/4;

    for( AntData antData : commData.myAntList)
    {
      if(count == step)
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
    for(int i=0; i<numThreads; i++)
    {
      ArrayList<AntData> antDataList = new ArrayList<>();
      antDataListsForThreads.add(i, antDataList);
    }
  }

  public void readMap(BufferedImage map)
  {
    int mapWidth = map.getWidth();
    int mapHeight = map.getHeight();
    this.mapHeight = mapHeight;
    this.mapWidth = mapWidth;
    world = new ClientCell[mapWidth][mapHeight];

    for(int y=0; y<mapHeight; y++)
    {
      for(int x=0; x<mapWidth; x++)
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

//    for (int x=0; x < this.mapWidth; x++)
//    {
//      for (int y=0; y < this.mapHeight; y++)
//      {
////        System.out.println("In ClientRandomWalk finding neighbors of: ("+x+", "+y+")");
//        world[x][y].findNeighbors();
//      }
//    }
  }
  
  
  public void createMap()
  {
    String mapName = "MediumMap1.png";
//    if (debug)
//    {
//      mapName = "MediumMap1.PNG";
////      mapName = "SmallMap1.png";
//    }
    mapName = "AntWorld.png";
    BufferedImage map = Util.loadImage(mapName, null);
//    BufferedImage map = Util.loadImage("TestReadMap.png", null);
    System.out.println("Is map null? map=" + map);
    readMap(map);
  }


  private boolean openConnection(String host, int portNumber)
  {
    try
    {
      clientSocket = new Socket(host, portNumber);
    }
    catch (UnknownHostException e)
    {
      System.err.println("ClientRandomWalk Error: Unknown Host " + host);
      e.printStackTrace();
      return false;
    }
    catch (IOException e)
    {
      System.err.println("ClientRandomWalk Error: Could not open connection to " + host + " on port " + portNumber);
      e.printStackTrace();
      return false;
    }

    try
    {
      outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
      inputStream = new ObjectInputStream(clientSocket.getInputStream());

    }
    catch (IOException e)
    {
      System.err.println("ClientRandomWalk Error: Could not open i/o streams");
      e.printStackTrace();
      return false;
    }

    return true;

  }

  public void closeAll()
  {
    System.out.println("ClientRandomWalk.closeAll()");
    {
      try
      {
        if (outputStream != null) outputStream.close();
        if (inputStream != null) inputStream.close();
        clientSocket.close();
      }
      catch (IOException e)
      {
        System.err.println("ClientRandomWalk Error: Could not close");
        e.printStackTrace();
      }
    }
  }

  /**
   * This method is called ONCE after the socket has been opened.
   * The server assigns a nest to this client with an initial ant population.
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
      System.err.println("ClientRandomWalk***ERROR***: Server returned wrong team name: "+data.myTeam);
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
  private void startAllSwarms()
  {
    for(Swarm swarm : swarmList)
    {
      swarm.start();
    }
  }
  public void spawnNewAnt(CommData commData) {
    int myScore = 0;
    int antCount = commData.myAntList.size() * 10;
    System.out.println("antCount=" + antCount);
    for (int foodCount : commData.foodStockPile) {
      myScore += foodCount;
    }
    AntType[] antTypes = {AntType.ATTACK, AntType.DEFENCE, AntType.MEDIC,
            AntType.SPEED, AntType.VISION, AntType.WORKER};

    if (myScore >= antCount * scoreToAntRatio)
    {
      //try: only create speed ants for scouting
      for (AntType antType : antTypes)
      {
//      System.out.println("getFoodUnitsToSpawn() called on " + antType + "=" + (antType.getFoodUnitsToSpawn(FoodType.MEAT)));
        //if required food type is achieved for every ant type, respawn it
        if (commData.foodStockPile[FoodType.MEAT.ordinal()] - antType.getFoodUnitsToSpawn(FoodType.MEAT) >= 0 &&
                commData.foodStockPile[FoodType.NECTAR.ordinal()] - antType.getFoodUnitsToSpawn(FoodType.NECTAR) >= 0 &&
                commData.foodStockPile[FoodType.SEEDS.ordinal()] - antType.getFoodUnitsToSpawn(FoodType.SEEDS) >= 0)
        {
//        AntData newAnt = new AntData(Constants.UNKNOWN_ANT_ID, antType, commData.myNest, commData.myTeam);
//        newAnt.myAction.type = AntAction.AntActionType.BIRTH;
          commData.myAntList.add(new AntData(Constants.UNKNOWN_ANT_ID, antType, commData.myNest, commData.myTeam));
          swarmAssignNum++;
          System.out.println("Spawned a new ant, new antList size=" + commData.myAntList.size());
        }
      }
    }
  }

  public void mainGameLoop(CommData data)
  {
//    startAllSwarms();
    boolean chooseActionOfAllAntsCompleted = false;
    while (true)
    {
      testAI.setCommData(data);
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

//        if (mapIsRead)
//        {
//          System.exit(1);
//        }
        //TODO: check to see if server always places new ant at end/beginning of list to avoid looping
        if (antListSize != data.myAntList.size())
        {
          System.out.println("Adding newly spawned ant to a list");
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
          System.out.println("CLient ready to send data");
          readyThreadCounter.numThreadsReady = 0;
          spawnNewAnt(data); //try to spawn ants when possible

          CommData sendData = data.packageForSendToServer();
          System.out.println("testAI.antStatusHashMap size=" + testAI.antStatusHashMap.size());
          chooseActionOfAllAntsCompleted = false;
          System.out.println("ClientRandomWalk: Sending>>>>>>>: " + sendData);
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
      }
      catch (IOException e)
      {
        System.err.println("ClientRandomWalk***ERROR***: client read failed");
        e.printStackTrace();
        System.exit(0);

      }
      catch (ClassNotFoundException e)
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
      if (DEBUG) System.out.println("ClientRandomWalk.sendCommData(" + sendData +")");
      outputStream.writeObject(sendData);
      outputStream.flush();
      outputStream.reset();
    }
    catch (IOException e)
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
    testAI.setCommData(commData);
    //TODO: setting food locations on the map.
    for(FoodData food : commData.foodSet)
    {
      world[food.gridX][food.gridY].setFoodType(food.foodType);
      System.out.println("Food: (" + food.gridX + ", " + food.gridY + "), Count: " + food.count);
    }
    System.out.println("swarmList.size()=" + swarmList.size());
//    int runCounter = 0;
    for(Swarm swarm : swarmList)
    {
//      System.out.println("runCounter="+runCounter);
      swarm.setCommData(commData);
      executor.execute(swarm);
//      runCounter++;
//      swarm.chooseActionForSwarm(commData);
    }

    //WorkerThread wk = new WorkerThread(commData.myAntList, commData);
    //wk.setIntelligence(testAI);
    //wk.start();

    /*for (AntData ant : commData.myAntList)
    {
      testAI.setAntData(ant);
      ant.myAction = testAI.chooseAction();
    }*/

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
    String serverHost = "localhost";
    if (args.length > 0) serverHost = args[args.length - 1];
    
    TeamNameEnum team = TeamNameEnum.Arthur_Phil;
    if (args.length > 1)
    {
      team = TeamNameEnum.getTeamByString(args[0]);
    }
    
    new ClientRandomWalk(serverHost, Constants.PORT, team);
  }
  
}
