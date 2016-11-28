package antworld.client;

import antworld.common.FoodType;
import antworld.common.LandType;

import java.util.ArrayList;

/**
 * Created by Arthur on 11/24/2016.
 */
public class ClientCell
{
  LandType landType;
  int height = 0;
  int x;
  int y;
  int cost = 0;
  FoodType foodType;
  ArrayList<ClientCell> neighbors = new ArrayList<>();

  public ClientCell(LandType landType, int height, int x, int y)
  {
    this.landType = landType;
    this.height = height;
    this.foodType = null;
    this.x = x;
    this.y = y;
    setWaterHeight();

  }

  private void setWaterHeight()
  {
    if(this.landType.equals(LandType.WATER)) this.height = 10000000;
  }

  public void setFoodType(FoodType foodType)
  {
    this.foodType = foodType;
  }

  public FoodType getFoodType()
  {
    return this.foodType;
  }

  public int getHeight()
  {
    return this.height;
  }

  public void findNeighbors()
  {
    for(int i =-1; i<=1; i++)
    {
      if(x + i >= 0 && x + i <= ClientRandomWalk.mapWidth)
      {
        for(int j = -1; j<=1; j++)
        {
          if(y + j >= 0 && y + j <= ClientRandomWalk.mapHeight)
          {
            neighbors.add(ClientRandomWalk.world[x+i][y+j]);
            //System.out.println(x+i + " and " + y+j + " are neighbors of " + x + " and " + y );
          }
        }
      }

    }
  }

  public int getCost()
  {
    return this.cost;
  }

  public void setCost(int cost)
  {
    this.cost = cost;
  }

  @Override
  public boolean equals(Object obj)
  {
    boolean retVal = false;
    if(obj instanceof ClientCell)
    {
      ClientCell test = (ClientCell) obj;
      if(test.x == this.x && test.y == this.y) retVal = true;
    }

    return retVal;
  }

  @Override
  public int hashCode()
  {
    return this.x + this.y;
  }
}