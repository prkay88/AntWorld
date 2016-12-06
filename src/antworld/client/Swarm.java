package antworld.client;

import antworld.common.AntData;
import antworld.common.CommData;
import antworld.common.Util;

import java.util.ArrayList;
import java.util.HashSet;

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

  private ArrayList<AntData> antDataList = new ArrayList<>();
  private AI intellegence;
  private CommData commData;
  private HashSet<Integer> antIdSet = new HashSet<>();
  //Could have an AI/CommData here and then in the Client we just iterate through Swarms
  //Also could have a worker thread here too.


    public Swarm(int centerX, int centerY, double innerRadius, ArrayList<AntData> antDataList, AI intellegence, CommData commData)
  {
      this.centerX = centerX;
      this.centerY = centerY;
      this.innerRadius = innerRadius;
      this.middleRadius = this.innerRadius*3;
      this.outerRadius = this.innerRadius * 6;
      this.antDataList = antDataList;
      this.intellegence = intellegence;
      this.commData = commData;
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

  public void addAnt(AntData antData)
  {
      antDataList.add(antData);
  }

  public boolean insideInnerRadius(int x, int y)
  {
      int distanceToCenter = Util.manhattanDistance(x, y, centerX, centerY);
      if(distanceToCenter > innerRadius) return false;
      else return true;
  }

  public boolean insideMiddleRadius(int x, int y)
  {
      int distanceToCenter = Util.manhattanDistance(x, y, centerX, centerY);
      if(distanceToCenter > middleRadius) return false;
      else return true;
  }

  public boolean insideOuterRadius(int x, int y)
  {
      int distanceToCenter = Util.manhattanDistance(x, y, centerX, centerY);
      if(distanceToCenter > outerRadius) return false;
      else return true;
  }

  public void expandSwarm(double expandFactor)
  {
      innerRadius = innerRadius*expandFactor;
      middleRadius = middleRadius*expandFactor;
      outerRadius = outerRadius*expandFactor;
  }

  public void addAntToIDSet(AntData antData)
  {
      antIdSet.add(antData.id);
  }


    @Override
  public void run()
  {
     intellegence.setCommData(commData);
     for(AntData antData : commData.myAntList)
     {
         if(antIdSet.contains(antData.id))
         {
             intellegence.setAntData(antData);
             antData.myAction = intellegence.chooseAction();
         }

     }
  }
}
