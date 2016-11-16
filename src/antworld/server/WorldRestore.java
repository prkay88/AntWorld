package antworld.server;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import antworld.common.AntData;
import antworld.common.TeamNameEnum;
import antworld.server.Nest.NetworkStatus;

public class WorldRestore
{
  private static final String FILE_PREFIX = "AntWorld_";
  private static final String FILE_EXTENSION = ".dat";
  private static final String PATH = "restore/";

  private static final SimpleDateFormat FILE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd.HH-mm");
  
  

  public static String getFileNameFoodSpawnSites()
  { return PATH + FILE_PREFIX + "FoodSpawnSite" + FILE_EXTENSION;
  }
  
  public static String getFileNameRestorePoint()
  { return PATH + FILE_PREFIX + "RestorePoint_" + FILE_DATE_FORMAT.format(new Date()) + FILE_EXTENSION;
  }
  

  public static void writeFoodSpawnSites(ArrayList<FoodSpawnSite> foodSpawnList )
  {
    String fileName = getFileNameFoodSpawnSites();
    
    ObjectOutputStream outStream = null;
    try
    {
      FileOutputStream fos = new FileOutputStream(fileName);
      outStream = new ObjectOutputStream(fos);
    
      for (FoodSpawnSite spawnSite : foodSpawnList)
      {
        outStream.writeObject(spawnSite);
      }
    }
    catch (Exception e)
    {
      System.err.println("WorldRestore.writeFoodSpawnSites(): " + e.getMessage());
    }
    finally 
    {
      try 
      {
        if (outStream != null) outStream.close();
      } 
      catch (Exception ex) {}
    }
  }
  
  
  public static ArrayList<FoodSpawnSite> loadFoodSpawnSites()
  {
    ArrayList<FoodSpawnSite> foodSpawnList = new ArrayList<FoodSpawnSite>();
    String fileName = getFileNameFoodSpawnSites();
    ObjectInputStream inStream = null;
    
    try
    {
      FileInputStream fis = new FileInputStream(fileName);
      inStream = new ObjectInputStream(fis);
    
      Object obj = null;
      
      while ((obj = inStream.readObject()) != null) 
      {
        if (obj instanceof FoodSpawnSite) 
        {
          FoodSpawnSite spawnSite = (FoodSpawnSite)obj;
          foodSpawnList.add(spawnSite);
          System.out.println("     spawnSite=" + spawnSite);
        }
      }
    }
    catch (Exception e)
    {
      //System.err.println("WorldRestore.loadFoodSpawnSites(): " + e.getMessage());
    }
    
    finally 
    {
      try 
      {
        if (inStream != null) inStream.close();
      } 
      catch (Exception ex) {}
  }
    
    return foodSpawnList;
  }
  
  
  
  public static void saveRestorePoint(Cell[][]world, ArrayList<Nest> nestList, ArrayList<FoodSpawnSite> foodSpawnList, int gameTick )
  {
    WorldData data = new WorldData();
    data.foodSpawnList = foodSpawnList;
    data.gameTick = gameTick;
    data.nestList = nestList;
    
    
    String fileName = getFileNameRestorePoint();
    
    ObjectOutputStream outStream = null;
    try
    {
      FileOutputStream fos = new FileOutputStream(fileName);
      outStream = new ObjectOutputStream(fos);
    
      outStream.writeObject(data);
    }
    catch (Exception e)
    {
      System.err.println("WorldRestore.saveRestorePoint(): " + e.getMessage());
      e.printStackTrace();
    }
    finally 
    {
      try 
      {
        if (outStream != null) outStream.close();
      } 
      catch (Exception ex) {}
    }
  }

  
  public static WorldData loadRestorePoint(String fileName)
  {
    ObjectInputStream inStream = null;
    WorldData data = null;
    try
    {
      FileInputStream fis = new FileInputStream(fileName);
      inStream = new ObjectInputStream(fis);
    
      data = (WorldData) inStream.readObject();
    }
    catch (Exception e)
    {
      System.err.println("WorldRestore.loadRestorePoint(): " + e.getMessage());
      e.printStackTrace();
      System.exit(0);
    }
    finally 
    {
      try 
      {
        if (inStream != null) inStream.close();
      } 
      catch (Exception ex) {}
    }
    
    
    for (Nest myNest : data.nestList) 
    {
      if (myNest.team == TeamNameEnum.NEARLY_BRAINLESS_BOTS)
      { ArrayList<AntData> antList = myNest.getAntList();
        for (AntData ant : antList) 
        {
          ant.underground=true;
        }
      }
      else
      {
    	if (myNest.getNetworkStatus() == NetworkStatus.CONNECTED)
    	{  myNest.setNetworkStatus(NetworkStatus.DISCONNECTED);
    	
    	}
      
      }
    }
    
    

    
//    Nest RANDOM_WALKERS_nest = common.nestList.get(NestNameEnum.LEPTOGENYS.ordinal());
//    
//    Nest myNest = common.nestList.get(1);
//    AntData ant = myNest.getAntList().get(0);
//    ant.underground = false;
//    ant.health = 1000;
//    ant.carryType = FoodType.ATTACK;
//    ant.carryUnits = ant.antType.getCarryCapacity();
//    ant.gridX = RANDOM_WALKERS_nest.centerX + 30;
//    ant.gridY = RANDOM_WALKERS_nest.centerY;
//    
//    
//    RANDOM_WALKERS_nest.getAntList().clear();
//    ant = Ant.createAnt(AntType.WORKER, NestNameEnum.LEPTOGENYS, TeamNameEnum.RANDOM_WALKERS);
//    
//    ant.health = 2000;
//    ant.underground = false;
//    ant.gridX = RANDOM_WALKERS_nest.centerX + 29;
//    ant.gridY = RANDOM_WALKERS_nest.centerY;
//    RANDOM_WALKERS_nest.getAntList().add(ant);
    
    
    return data;
  }

  
  
//private void buildAntWorld(BufferedImage map)
//{
//for (int y=0; y<worldHeight; y++)
//{
////if ((y % 100) == 0) System.out.println("...buildAntWorld() row = "+y);
//for (int x=0; x<worldWidth; x++)
//{
//int rgb = (map.getRGB(x, y) & 0x00FFFFFF);
//LandType landType = LandType.WATER;
//int height = 0;
//if (rgb == 0)
//{ landType = LandType.NEST_CENTER;
//nestList.add(new Nest(x, y));
//}
//else if (rgb == 0x719B64) landType = LandType.GRASS;
//else if (rgb == 0x89BC79) {landType = LandType.GRASS; height = 50 +
//random.nextInt(15) - random.nextInt(15);}
//else if (rgb == 0xA1DD8E) {landType = LandType.GRASS; height = 100 +
//random.nextInt(15) - random.nextInt(15);}
//else if (rgb == 0xB6FAA1) {landType = LandType.GRASS; height = 150 +
//random.nextInt(15) - random.nextInt(15);}
//else if (rgb == 0xDCFFF0) {landType = LandType.GRASS; height = 200;}
////System.out.println("("+x+","+y+") rgb="+rgb + ", landType="+landType
//+" height="+height);
//world[x][y] = new Cell(landType, height, x, y);
//}
//}
//
//
//for (Nest nest : nestList)
//{ int x0 = nest.getLocationX();
//int y0 = nest.getLocationY();
//for (int y = y0-Nest.SAND_RADIUS; y <= y0+Nest.SAND_RADIUS; y++)
//{ for (int x = x0-Nest.SAND_RADIUS; x <= x0+Nest.SAND_RADIUS; x++)
//{
//if ((x == x0) && (y == y0)) continue;
//if (manhattanDistance(x, y, x0, y0) <= Nest.SAND_RADIUS)
//{ world[x][y].setLandType(LandType.SAND);
//}setNestID
//}
//}
//}
//
//
//System.out.println("Adjusting Heights...");
//
////Adjust Heights
//long n = worldWidth * worldHeight * 1000L;
//for (long i=0; i<n; i++)
//{
//if (i % 1000000L == 0L) System.out.println("  ... i="+i + " of " + n);
//int x = random.nextInt(worldWidth-2) + 1;
//int y = random.nextInt(worldHeight-2) + 1;
//
//if (world[x][y].getLandType() != LandType.GRASS) continue;
//Direction dir = Direction.getRandomDir();
//for (int k=0; k<Direction.SIZE; k++)
//{
//int x2 = x+dir.deltaX();
//int y2 = y+dir.deltaY();
//if (world[x2][y2].getLandType() == LandType.GRASS)
//{
//if (Math.abs(world[x][y].getHeight() - world[x2][y2].getHeight()) >=2 )
//{ int avgHeight = (world[x][y].getHeight() + world[x2][y2].getHeight())/2;
//if (world[x][y].getHeight() < 200) world[x][y].setHeight(avgHeight);
//else world[x2][y2].setHeight(avgHeight);
//break;
//}
//}
//dir = Direction.getNextDir(dir);
//}
//}
//
//
//System.out.println("Done Building AntWorld");
//
//
//}
  
  

}
