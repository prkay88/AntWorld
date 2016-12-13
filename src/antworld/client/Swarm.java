package antworld.client;

import antworld.common.AntData;
import antworld.common.CommData;
import antworld.common.LandType;
import antworld.common.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

/**
 * Created by Phillip on 12/6/2016.
 */
public class Swarm extends Thread
{
  private int centerX;
  private int centerY;
  private double innerRadius;
  private double middleRadius;
  private double outerRadius;
  private final int SWARMID;
  private boolean computeNextMove = false;
  private ArrayList<AntData> antDataList = new ArrayList<>();
  private AI intellegence;
  private CommData commData;
  private HashSet<Integer> antIdSet = new HashSet<>();
  private Random random = new Random();
  public boolean turnFinished = false;
  public boolean executing = false;
  public HashMap<Integer, Integer> swarmLocationMap = new HashMap<>();
  
  private int ticksUntilMoveSwarm = 20;
  private int ticksUntilExpandSwarm = 30;
  ArrayList<ClientCell> nestCenterCells = new ArrayList<>();
  private int enemyNestX = 999999999;
  private int enemyNestY = 99999999;
  private int distanceToEnemy = 99999999;
  private int myNestCenterX;
  private int myNestCenterY;
  private boolean goingTowardsEnemyNest;
  private boolean foundInitialEnemyNest = false;
  private boolean havePickedEnemyNest = false;
  private boolean foundFood;
  private boolean foundEnemyAnt;
  private boolean foundWater;
  private int foodUnitsToReturn = 20;
  int minHealthOfAnt = 9;
  private int goToWaterX = 999999;
  private int goToWaterY = 999999;
  private int waterRange = 100;
  private final int MAXEXPLOREDISTANCE = 1200;
  private long count =0;
  private int timeCenterOverWater =0;

  
  //Could have an AI/CommData here and then in the Client we just iterate through Swarms
  //Also could have a worker thread here too.
  
  
  public Swarm(int id, int centerX, int centerY, double innerRadius, AI intellegence, CommData commData)
  {
    this.SWARMID = id;
    this.centerX = centerX;
    this.centerY = centerY;
    this.innerRadius = innerRadius;
    this.middleRadius = this.innerRadius * 3;
    this.outerRadius = this.innerRadius * 6;
    this.intellegence = intellegence;
    this.commData = commData;
    myNestCenterX = commData.nestData[commData.myNest.ordinal()].centerX;
    myNestCenterY = commData.nestData[commData.myNest.ordinal()].centerY;
  }
  
  public void printAntIDs()
  {
    for (Integer integer : antIdSet)
    {
      System.out.println("Swarm ID is: " + SWARMID + " antId is: " + integer);
    }
  }
  
  public void setNestCenterCells(ArrayList<ClientCell> nestCenterCells)
  {
    System.out.println("In Swarm's setNestCenterCells, nestCenterCells.size()="+nestCenterCells.size());
    this.nestCenterCells = nestCenterCells;
//
//    for(ClientCell clientCell : nestCenterCells)
//    {
//      ClientCell cc = new ClientCell(clientCell.landType, clientCell.height, clientCell.x, clientCell.y);
//      nestCenterCells.add(cc);
//    }
  }
  
  public void setCenterX(int centerX)
  {
    this.centerX = centerX;
  }
  
  public int getCenterX()
  {
    return this.centerX;
  }
  
  public void setCenterY(int centerY)
  {
    this.centerY = centerY;
  }
  
  public int getCenterY()
  {
    return this.centerY;
  }
  
  public void setInnerRadius(double innerRadius)
  {
    this.innerRadius = innerRadius;
  }
  
  public double getInnerRadius()
  {
    return this.innerRadius;
  }
  
  public void setMiddleRadius(double middleRadius)
  {
    this.middleRadius = middleRadius;
  }
  
  public double getMiddleRadius()
  {
    return this.middleRadius;
  }
  
  public void setOuterRadius(double outerRadius)
  {
    this.outerRadius = outerRadius;
  }
  
  public double getOuterRadius()
  {
    return this.outerRadius;
  }
  
  public void setIntellegence(AI intellegence)
  {
    this.intellegence = intellegence;
  }
  
  public void setCommData(CommData commData)
  {
    this.commData = commData;
  }
  
  
  public boolean insideInnerRadius(int x, int y)
  {
    int distanceToCenter = Util.manhattanDistance(x, y, centerX, centerY);
    if (distanceToCenter > innerRadius) return false;
    else return true;
  }
  
  public boolean insideMiddleRadius(int x, int y)
  {
    int distanceToCenter = Util.manhattanDistance(x, y, centerX, centerY);

    if (distanceToCenter > innerRadius && distanceToCenter< outerRadius) return false;
    else return true;
  }
  
  public boolean insideOuterRadius(int x, int y)
  {
    int distanceToCenter = Util.manhattanDistance(x, y, centerX, centerY);
    if (distanceToCenter < outerRadius && distanceToCenter >middleRadius) return false;
    else return true;
  }
  
  public void expandSwarm(double expandFactor)
  {
    innerRadius = innerRadius * expandFactor;
    middleRadius = middleRadius * expandFactor;
    outerRadius = outerRadius * expandFactor;
  }
  
  //Basic moving the center of swarm.
  //Need to implement a check of map bounds
  public void moveSwarmCenterExplore()
  {
    //move Swarm NE
    if (SWARMID % 4 == 0)
    {
      centerX = centerX + random.nextInt(3);
      centerY = centerY - random.nextInt(3);
    }
    //move Swarm NW
    else if (SWARMID % 4 == 1)
    {
      centerX = centerX - random.nextInt(3);
      centerY = centerY - random.nextInt(3);
    }
    //move Swarm SW
    else if (SWARMID % 4 == 2)
    {
      centerX = centerX - random.nextInt(3);
      centerY = centerY + random.nextInt(3);
    }
    //move Swarm SE
    else if (SWARMID % 4 == 3)
    {
      centerX = centerX + random.nextInt(3);
      centerY = centerY + random.nextInt(3);
    }
  }

  private boolean foundSwarmTarget(int targetX, int targetY)
  {
    if(targetX == centerX && targetY == centerY) return true;
    else return false;
  }
  
  public void findEnemyNest()
  {
    int tempDistance;
    if(!foundInitialEnemyNest)
    {
      if (SWARMID % 4 == 0)
      {
        for (ClientCell clientCell : nestCenterCells)
        {
          if (clientCell.x >= myNestCenterX && clientCell.y <= myNestCenterY)
          {
            tempDistance = Util.manhattanDistance(myNestCenterX, myNestCenterY, clientCell.x, clientCell.y);
            if (tempDistance < distanceToEnemy)
            {
              distanceToEnemy = tempDistance;
              enemyNestX = clientCell.x;
              enemyNestY = clientCell.y;
            }
          }
        }
      }
      else if (SWARMID % 4 == 1)
      {
        for (ClientCell clientCell : nestCenterCells)
        {
          if (clientCell.x < myNestCenterX && clientCell.y <= myNestCenterY)
          {
            tempDistance = Util.manhattanDistance(myNestCenterX, myNestCenterY, clientCell.x, clientCell.y);
            if (tempDistance < distanceToEnemy)
            {
              distanceToEnemy = tempDistance;
              enemyNestX = clientCell.x;
              enemyNestY = clientCell.y;
            }
          }
        }
      }
      else if (SWARMID % 4 == 2)
      {
        for (ClientCell clientCell : nestCenterCells)
        {
          if (clientCell.x < myNestCenterX && clientCell.y >= myNestCenterY)
          {
            tempDistance = Util.manhattanDistance(myNestCenterX, myNestCenterY, clientCell.x, clientCell.y);
            if (tempDistance < distanceToEnemy)
            {
              distanceToEnemy = tempDistance;
              enemyNestX = clientCell.x;
              enemyNestY = clientCell.y;
            }
          }
        }
      }
      else if (SWARMID % 4 == 3)
      {
        for (ClientCell clientCell : nestCenterCells)
        {
          if (clientCell.x >= myNestCenterX && clientCell.y >= myNestCenterY)
          {
            tempDistance = Util.manhattanDistance(myNestCenterX, myNestCenterY, clientCell.x, clientCell.y);
            if (tempDistance < distanceToEnemy)
            {
              distanceToEnemy = tempDistance;
              enemyNestX = clientCell.x;
              enemyNestY = clientCell.y;
            }
          }
        }
      }
    }
    else
    {
      int distance = 999999;
      int temp;
      for(ClientCell clientCell : nestCenterCells)
      {
        temp = Util.manhattanDistance(centerX, centerY, clientCell.x, clientCell.y);
        if(temp < distance)
        {
          enemyNestX = clientCell.x;
          enemyNestY = clientCell.y;
          distance = temp;
        }
      }
    }

    if (enemyNestY < 999999999 && enemyNestX < 999999999) goingTowardsEnemyNest = true;
  }

  public boolean contains(int antId)
  {
    if(antIdSet.contains(antId)) return true;
    else return false;
  }

  public void moveSwarmCenterTowardsNest()
  {
    
    if (myNestCenterX > centerX)
    {
      centerX = centerX++;
      
    }
    else if (myNestCenterX < centerX)
    {
      centerX = centerX--;
    }
    if (myNestCenterY > centerY)
    {
      centerY = centerY++;
    }
    else if (myNestCenterY < centerY)
    {
      centerY = centerY--;
    }
  }
  
  public void addAntToIDSet(AntData antData)
  {
    antIdSet.add(antData.id);
  }
  
  private void moveTowardsEnemyNest()
  {


      if (centerX > enemyNestX) centerX--;
      if (centerX < enemyNestX) centerX++;
      if (centerY > enemyNestY) centerY--;
      if (centerY < enemyNestY) centerY++;

  }
  
  public void chooseActionForSwarm(CommData commData)
  {
    this.commData = commData;
    intellegence.setCommData(this.commData);
    for (AntData antData : commData.myAntList)
    {
      if (antIdSet.contains(antData.id))
      {
        intellegence.setAntData(antData);
        antData.myAction = intellegence.chooseAction();
      }
      
    }
  }
  
  private void findWaterWithinRange(int range)
  {
    boolean foundWater = false;
    for (int i = 0; i <= range; i++)
    {
      if (foundWater) break;
      for (int j = 0; j <= range; j++)
      {
        if (ClientRandomWalk.world[centerX + i][centerY + j].landType == LandType.WATER)
        {
          goToWaterX = ClientRandomWalk.world[centerX + i][centerY].x;
          goToWaterY = ClientRandomWalk.world[centerX + i][centerY].y;
          foundWater = true;
          break;
        }
        if (ClientRandomWalk.world[centerX - i][centerY + j].landType == LandType.WATER)
        {
          goToWaterX = ClientRandomWalk.world[centerX - i][centerY + j].x;
          goToWaterY = ClientRandomWalk.world[centerX - i][centerY + j].y;
          foundWater = true;
          break;
        }
        if (ClientRandomWalk.world[centerX + i][centerY - j].landType == LandType.WATER)
        {
          goToWaterX = ClientRandomWalk.world[centerX + i][centerY - j].x;
          goToWaterY = ClientRandomWalk.world[centerX + i][centerY - j].y;
          foundWater = true;
          break;
        }
        if (ClientRandomWalk.world[centerX - i][centerY - j].landType == LandType.WATER)
        {
          goToWaterX = ClientRandomWalk.world[centerX - i][centerY - j].x;
          goToWaterY = ClientRandomWalk.world[centerX - i][centerY - j].y;
          foundWater = true;
          break;
        }
      }
      
      
    }
  }
  
  public void executeNextUpdate(CommData commData)
  {
    this.computeNextMove = true;
    this.commData = commData;
  }
  
  private void moveSwarmToWater()
  {
    if (centerX > goToWaterX) centerX--;
    if (centerX < goToWaterX) centerX++;
    if (centerY > goToWaterY) centerY--;
    if (centerY < goToWaterY) centerY++;
  }
  
  //Still need to figure out how to get multi thread working
  @Override
  public void run()
  {
    //System.out.println("Starting Swarm Number: " + SWARMID + "center is at: (" + centerX + ", " + centerY + ")");
    count++;
    System.out.println("count is: "+count);
    int foodCount = 0;
    int healthOfWeakestAnt = 20;
    int numOfHurtAnts = 0;
    int numOfHurtAntsThreshold = antIdSet.size() / 2;
    //System.out.println("Swarm Number: "+ SWARMID+ " computeNextUpdate is: "+computeNextMove);
    
    //System.out.println("Swarm Number: "+ SWARMID+ " is executing next update");
    if(!havePickedEnemyNest)
    {
      findEnemyNest();
      havePickedEnemyNest = true;
    }
    if (!turnFinished)
    {
      System.out.println("Starting Swarm Number: " + SWARMID + "center is at: (" + centerX + ", " + centerY + ")");
      intellegence.setCommData(this.commData);
      for (AntData antData : commData.myAntList)
      {
        if (antIdSet.contains(antData.id))
        {
          intellegence.setAntData(antData);
          antData.myAction = intellegence.chooseAction();
          foodCount += antData.carryUnits;
          if (antData.health <= minHealthOfAnt) numOfHurtAnts++;
          System.out.println("Swarm Number: " + SWARMID + " antID: " + antData.id + " action is: " + antData.myAction);
        }
        
      }
      System.out.println("Swarm: "+SWARMID+ " is going to enemyNestX: "+enemyNestX+" enemyNestY: "+enemyNestY);
      ClientRandomWalk.readyThreadCounter.incrementNumThreadsReady();
      //System.out.println(" Swarm Number: " + SWARMID+ " finshed choosing action");
      
//      System.out.println("goingTowardsEnemyNest=" + goingTowardsEnemyNest +
//              ", foodCount < foodUnitsToReturn=" + (foodCount < foodUnitsToReturn) +
//              ", healthOfWeakestAnt > minHealthOfAnt=" + (numOfHurtAnts < numOfHurtAntsThreshold));
      //TODO: Add more logic to decide what action Swarm does.
      if (goingTowardsEnemyNest && foodCount < foodUnitsToReturn && numOfHurtAnts < numOfHurtAntsThreshold)
      {
        //System.out.println("inside Swarm's run() going to move the swarm center");
        if(!foundSwarmTarget(enemyNestX, enemyNestY) && count%3 ==0) moveTowardsEnemyNest();
        else if(foundSwarmTarget(enemyNestX, enemyNestY)) goingTowardsEnemyNest = false;

      }
      
      else if (foodCount >= foodUnitsToReturn)
      {
        if(count%3 ==0)moveSwarmCenterTowardsNest();
        goingTowardsEnemyNest = false;
      }
//      else if (healthOfWeakestAnt <= minHealthOfAnt)
      else if (numOfHurtAnts >= numOfHurtAntsThreshold)
      {
        findWaterWithinRange(waterRange);
        if (goToWaterX < 999999 && goToWaterY < 999999) moveSwarmToWater();
        else if(count%3 ==0) moveSwarmCenterTowardsNest();
        goingTowardsEnemyNest = false;
        
      }
      else if(Util.manhattanDistance(centerX, centerY, myNestCenterX, myNestCenterY) >= MAXEXPLOREDISTANCE)
      {
         if(count%3==0)moveSwarmCenterTowardsNest();
        goingTowardsEnemyNest = false;
      }
      else
      {
        if(random.nextBoolean())moveSwarmCenterExplore();
        else
        {
          if(count >= 200)foundInitialEnemyNest = true;
          findEnemyNest();
        }
      }
      
      ticksUntilExpandSwarm--;
      if (ticksUntilExpandSwarm == 0)
      {
        if (outerRadius <= 500)
        {
          //expandSwarm(1.5);
          ticksUntilExpandSwarm = 200;
        }
        
      }
      if(ClientRandomWalk.world[centerX][centerY].landType == LandType.WATER) timeCenterOverWater++;
      if(timeCenterOverWater >= 5)
      {
        System.out.println("Swarm Center over water for 5+ turns");
        System.out.println("Swarm:"+SWARMID+ " centerX: "+centerX+ " centerY: "+centerY);
        nestCenterCells.get(random.nextInt(nestCenterCells.size()));
        timeCenterOverWater = 0;
        System.out.println("Swarm: "+SWARMID+ " centerX: "+centerX+ " centerY: "+centerY);
      }
      turnFinished = true;
    }
    
  }
}
