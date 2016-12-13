package antworld.client;

import antworld.common.AntData;
import antworld.common.CommData;
import antworld.common.LandType;
import antworld.common.Util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

/**
 * Created by Phillip on 12/6/2016.
 * <p>
 * This class creates subgroups of our ants to make macro-decisions
 * on to.
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
  
  /**
   * Creates a new swarm.
   *
   * @param id           - of the Swarm
   * @param centerX      - initial center x coordinate of the Swarm
   * @param centerY      - initial center y coordinate of the Swarm
   * @param innerRadius  - size of the inner radius of the swarm
   * @param intellegence - AI object of the swarm
   * @param commData     - commData of the whole application
   */
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
  
  /**
   * Finds all the enemy ant Nest centers.
   *
   * @param nestCenterCells - nest center cells of the enemy bases
   */
  public void setNestCenterCells(ArrayList<ClientCell> nestCenterCells)
  {
    this.nestCenterCells = nestCenterCells;
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
  
  public void setCommData(CommData commData)
  {
    this.commData = commData;
  }
  
  
  public boolean insideOuterRadius(int x, int y)
  {
    int distanceToCenter = Util.manhattanDistance(x, y, centerX, centerY);
    if (distanceToCenter > outerRadius) return false;
    else return true;
  }
  
  /**
   * Basic moving of the center of a swarm.
   */
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
    if (targetX == centerX && targetY == centerY) return true;
    else return false;
  }
  
  /**
   * Sets the enemy nest coordinates to the closest enemy nest.
   */
  public void findEnemyNest()
  {
    int tempDistance;
    if (!foundInitialEnemyNest)
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
      for (ClientCell clientCell : nestCenterCells)
      {
        temp = Util.manhattanDistance(centerX, centerY, clientCell.x, clientCell.y);
        if (temp < distance)
        {
          enemyNestX = clientCell.x;
          enemyNestY = clientCell.y;
          distance = temp;
        }
      }
    }
    
    if (enemyNestY < 999999999 && enemyNestX < 999999999) goingTowardsEnemyNest = true;
  }
  
  /**
   * Checks to see if the ant's ID belongs to the swarm.
   *
   * @param antId
   * @return
   */
  public boolean contains(int antId)
  {
    if (antIdSet.contains(antId)) return true;
    else return false;
  }
  
  /**
   * Moves the center of the swarm towards the nest center.
   */
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
  
  /**
   * Adds ant to the ID set.
   *
   * @param antData - AntData object to be added
   */
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
  
  private void moveSwarmToWater()
  {
    if (centerX > goToWaterX) centerX--;
    if (centerX < goToWaterX) centerX++;
    if (centerY > goToWaterY) centerY--;
    if (centerY < goToWaterY) centerY++;
  }
  
  @Override
  public void run()
  {
    int foodCount = 0;
    int healthOfWeakestAnt = 20;
    int numOfHurtAnts = 0;
    int numOfHurtAntsThreshold = antIdSet.size() / 2;
    
    if (!havePickedEnemyNest)
    {
      findEnemyNest();
      havePickedEnemyNest = true;
    }
    if (!turnFinished)
    {
      intellegence.setCommData(this.commData);
      for (AntData antData : commData.myAntList)
      {
        if (antIdSet.contains(antData.id))
        {
          intellegence.setAntData(antData);
          antData.myAction = intellegence.chooseAction();
          foodCount += antData.carryUnits;
          if (antData.health <= minHealthOfAnt) numOfHurtAnts++;
        }
      }
      if (ClientRandomWalk.world[centerX][centerY].landType == LandType.WATER)
      {
        ClientCell newTargetNest = nestCenterCells.get(random.nextInt(nestCenterCells.size()));
        enemyNestX = newTargetNest.x;
        enemyNestY = newTargetNest.y;
      }
      if (goingTowardsEnemyNest && foodCount < foodUnitsToReturn && numOfHurtAnts < numOfHurtAntsThreshold)
      {
        if (!foundSwarmTarget(enemyNestX, enemyNestY)) moveTowardsEnemyNest();
        else goingTowardsEnemyNest = false;
      }
      else if (foodCount >= foodUnitsToReturn)
      {
        moveSwarmCenterTowardsNest();
        goingTowardsEnemyNest = false;
      }
      else if (numOfHurtAnts >= numOfHurtAntsThreshold)
      {
        findWaterWithinRange(waterRange);
        if (goToWaterX < 999999 && goToWaterY < 999999) moveSwarmToWater();
        else moveSwarmCenterTowardsNest();
        goingTowardsEnemyNest = false;
      }
      else if (Util.manhattanDistance(centerX, centerY, myNestCenterX, myNestCenterY) >= MAXEXPLOREDISTANCE)
      {
        moveSwarmCenterTowardsNest();
        goingTowardsEnemyNest = false;
      }
      else
      {
        if (random.nextBoolean()) moveSwarmCenterExplore();
        else
        {
          foundInitialEnemyNest = true;
          findEnemyNest();
        }
      }
      ticksUntilExpandSwarm--;
      if (ticksUntilExpandSwarm == 0)
      {
        if (outerRadius <= 500)
        {
          ticksUntilExpandSwarm = 200;
        }
      }
      turnFinished = true;
    }
  }
}
