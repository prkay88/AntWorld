package antworld.client;

import antworld.common.AntData;
import antworld.common.CommData;
import antworld.common.FoodData;
import antworld.common.Util;

import java.util.ArrayList;
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
  }
  
  public void printAntIDs()
  {
    for (Integer integer : antIdSet)
    {
      System.out.println("Swarm ID is: " + SWARMID + " antId is: " + integer);
    }
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
    if (distanceToCenter > middleRadius) return false;
    else return true;
  }
  
  public boolean insideOuterRadius(int x, int y)
  {
    int distanceToCenter = Util.manhattanDistance(x, y, centerX, centerY);
    if (distanceToCenter > outerRadius) return false;
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
      centerX = centerX + random.nextInt(20);
      centerY = centerY - random.nextInt(20);
    }
    //move Swarm NW
    else if (SWARMID % 4 == 1)
    {
      centerX = centerX - random.nextInt(20);
      centerY = centerY - random.nextInt(20);
    }
    //move Swarm SW
    else if (SWARMID % 4 == 2)
    {
      centerX = centerX - random.nextInt(20);
      centerY = centerY + random.nextInt(20);
    }
    //move Swarm SE
    else if (SWARMID % 4 == 3)
    {
      centerX = centerX + random.nextInt(20);
      centerY = centerY + random.nextInt(20);
    }
  }
  
  public void moveSwarmCenterTowardsNest()
  {
    int nestX = commData.nestData[commData.myNest.ordinal()].centerX;
    int nestY = commData.nestData[commData.myNest.ordinal()].centerY;
    if (nestX > centerX)
    {
      centerX = centerX + 20;
      
    }
    else if (nestX < centerX)
    {
      centerX = centerX - 20;
    }
    if (nestY > centerY)
    {
      centerY = centerY + 20;
    }
    else if (nestY < centerY)
    {
      centerY = centerY - 20;
    }
  }
  
  public void addAntToIDSet(AntData antData)
  {
    antIdSet.add(antData.id);
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
  
  public void executeNextUpdate(CommData commData)
  {
    this.computeNextMove = true;
    this.commData = commData;
  }
  
  
  //Still need to figure out how to get multi thread working
  @Override
  public void run()
  {
    System.out.println("Starting Swarm Number: " + SWARMID);
    
    
    //System.out.println("Swarm Number: "+ SWARMID+ " computeNextUpdate is: "+computeNextMove);
    
    //System.out.println("Swarm Number: "+ SWARMID+ " is executing next update");
    if (!turnFinished)
    {
      intellegence.setCommData(this.commData);
      for (AntData antData : commData.myAntList)
      {
        if (antIdSet.contains(antData.id))
        {
          intellegence.setAntData(antData);
          antData.myAction = intellegence.chooseAction();
          System.out.println("antID: " + antData.id + " action is: " + antData.myAction);
        }
    
      }
      ClientRandomWalk.readyThreadCounter.incrementNumThreadsReady();
      //System.out.println(" Swarm Number: " + SWARMID+ " finshed choosing action");
      turnFinished = true;
    }
    
  }
}
