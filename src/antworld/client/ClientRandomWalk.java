package antworld.client;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.Random;

import antworld.common.*;

public class ClientRandomWalk
{
  private static final boolean DEBUG = true;
  private static final TeamNameEnum myTeam = TeamNameEnum.RANDOM_WALKERS;
  private static final long password = 962740848319L;//Each team has been assigned a random password.
  private ObjectInputStream inputStream = null;
  private ObjectOutputStream outputStream = null;
  private boolean isConnected = false;
  private NestNameEnum myNestName = null;
//  private NestNameEnum myNestName = NestNameEnum.ARMY;
  private int centerX, centerY;
  public static int mapWidth, mapHeight;

  
  //cell class in client, not sure if we can use the one in server package
  static ClientCell[][] world; //contains all the land types of the map being used
  
  private RandomWalkAI testAI;

  private Socket clientSocket;

  private int antsMovingEast = 0;

  //A random number generator is created in Constants. Use it.
  //Do not create a new generator every time you want a random number nor
  //  even in every class were you want a generator.
  private static Random random = Constants.random;


  public ClientRandomWalk(String host, int portNumber)
  {
    System.out.println("Starting ClientRandomWalk: " + System.currentTimeMillis());
    isConnected = false;
    while (!isConnected)
    {
      isConnected = openConnection(host, portNumber);
      if (!isConnected) try { Thread.sleep(2500); } catch (InterruptedException e1) {}
    }

    CommData data = chooseNest();
    testAI = new RandomWalkAI(data, null);
    testAI.setCenterX(centerX);
    testAI.setCenterY(centerY);
    createMap();
    mainGameLoop(data);
    closeAll();
  }

  public int getCenterX()
  {
    return this.centerX;
  }
  public int getCenterY()
  {
    return this.centerY;
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

  public CommData chooseNest()
  {
    while (myNestName == null)
    {
      try { Thread.sleep(100); } catch (InterruptedException e1) {}
      //TODO: Uncomment for proper behavior
//      NestNameEnum requestedNest = NestNameEnum.values()[random.nextInt(NestNameEnum.SIZE)];
      NestNameEnum requestedNest = NestNameEnum.HARVESTER;
      CommData data = new CommData(requestedNest, myTeam);
      data.password = password;

      if( sendCommData(data) )
      {
        try
        {
          if (DEBUG) System.out.println("ClientRandomWalk: listening to socket....");
          CommData recvData = (CommData) inputStream.readObject();
          if (DEBUG) System.out.println("ClientRandomWalk: received <<<<<<<<<"+inputStream.available()+"<...\n" + recvData);

          if (recvData.errorMsg != null)
          {
            System.err.println("ClientRandomWalk***ERROR***: " + recvData.errorMsg);
            continue;
          }

          if ((myNestName == null) && (recvData.myTeam == myTeam))
          { myNestName = recvData.myNest;
            centerX = recvData.nestData[myNestName.ordinal()].centerX;
            centerY = recvData.nestData[myNestName.ordinal()].centerY;
            System.out.println("ClientRandomWalk: !!!!!Nest Request Accepted!!!! " + myNestName);
            return recvData;
          }
        }
        catch (IOException e)
        {
          System.err.println("ClientRandomWalk***ERROR***: client read failed");
          e.printStackTrace();
        }
        catch (ClassNotFoundException e)
        {
          System.err.println("ClientRandomWalk***ERROR***: client sent incorrect common format");
        }
      }
    }
    return null;
  }

  private void createMap()
  {
    BufferedImage map = Util.loadImage("AStarTest1.png", null);
    System.out.println("Is map null? map="+map);
    readMap(map);
  }

  public void mainGameLoop(CommData data)
  {
    //BufferedImage map = Util.loadImage("SmallMap3.png", null);
    //System.out.println("Is map null? map="+map);
    //readMap(map);
    //seems to work:
//    for(int y=0; y<map.getHeight(); y++)
//    {
//      for(int x=0; x<map.getWidth(); x++)
//      {
//        System.out.print(world[x][y].landType +" ");
//      }
//      System.out.println();
//    }
//    System.exit(0);
    
    while (true)
    {
      testAI.setCommData(data);
      try
      {
        if (DEBUG) System.out.println("ClientRandomWalk: chooseActions: " + myNestName);
        if(data.nestData == null) System.out.println("ClientRandomWalk: nestData is null before being sent to chooseActionOfAllAnts");

        chooseActionsOfAllAnts(data);
        CommData sendData = data.packageForSendToServer();

        System.out.println("ClientRandomWalk: Sending>>>>>>>: " + sendData);
        outputStream.writeObject(sendData);
        outputStream.flush();
        outputStream.reset();


        if (DEBUG) System.out.println("ClientRandomWalk: listening to socket....");
        //TODO: Server gives us this:
        CommData receivedData = (CommData) inputStream.readObject();
        if (DEBUG) System.out.println("ClientRandomWalk: received <<<<<<<<<"+inputStream.available()+"<...\n" + receivedData);
        data = receivedData;

        if ((myNestName == null) || (data.myTeam != myTeam))
        {
          System.err.println("ClientRandomWalk: !!!!ERROR!!!! " + myNestName);
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
      outputStream.writeObject(sendData); //Where data is sent to server?
      outputStream.flush(); //Flush so java won't coalesce objects
      outputStream.reset(); //resets it as well
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
    int count =0;
    if(commData.foodSet != null && !commData.foodSet.isEmpty())
    {
      for(FoodData food : commData.foodSet)
      {
        if(count == 0)
        {
          AntData antData = commData.myAntList.get(0);
          ClientCell start = world[antData.gridX][antData.gridY];
          ClientCell goal = world[food.gridX][food.gridY];
          System.out.println("FINDING A PATH FROM X: " + antData.gridX + " Y: " + antData.gridY + " TO X: " + food.gridX+ " Y: "+food.gridY);
          AStar test = new AStar(start, goal);
          LinkedList<ClientCell> path = test.findPath();
        }
        count++;
      }
    }
    //setting food locations on the map.
    for(FoodData food : commData.foodSet)
    {
      world[food.gridX][food.gridY].setFoodType(food.foodType);
      System.out.println("Food: (" + food.gridX + ", " + food.gridY + "), Count: " + food.count);
    }
    for (AntData ant : commData.myAntList)
    {
      testAI.setAntData(ant);
      ant.myAction = testAI.chooseAction(); //something weird here
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
        if (rgb == 0xF0E68C)
        {
          landType = LandType.NEST;
        }
        else if (rgb == 0x1E90FF)
        {
          landType = LandType.WATER;
        }
        else
        {
          int g = (rgb & 0x0000FF00) >> 8;
          height = g - 55;
        }
        // System.out.println("("+x+","+y+") rgb="+rgb +
        // ", landType="+landType
        // +" height="+height);
        world[x][y] = new ClientCell(landType, height, x, y);
      }
    }
  }
  
  public static void main(String[] args)
  {
    String serverHost = "localhost";
    if (args.length > 0) serverHost = args[0];
    System.out.println("Starting client with connection to: " + serverHost);

    new ClientRandomWalk(serverHost, Constants.PORT);
  }

}
