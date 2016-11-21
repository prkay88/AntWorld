package antworld.client;

import antworld.common.*;

/**
 * Created by Phillip on 11/18/2016.
 */
public class RandomWalkAI extends AI
{
  public RandomWalkAI(CommData data, AntData antData)
  {
        /*this.commData = data;
        centerX = commData.nestData[commData.myNest.ordinal()].centerX;
        centerY = commData.nestData[commData.myNest.ordinal()].centerY;
        antAction.type = AntAction.AntActionType.STASIS;
        this.antData = antData;*/
    super(data, antData);
  }
  
  private AntAction chooseDirection(int startX, int startY, int goalX, int goalY)
  {
//        AntAction antAction = new AntAction(AntAction.AntActionType.MOVE);
    //ask commData if there is an ant at the position i'm looking to go to.
    System.out.println("In RWAI choosDirection()");
    if (startX > goalX && startY > goalY && !positionTaken(startX-1, startY-1))
    {
      antAction.direction = Direction.NORTHWEST;
    }
    else if (startX < goalX && startY > goalY && !positionTaken(startX+1, startY-1))
    {
      antAction.direction = Direction.NORTHEAST;
    }
    else if (startX > goalX && startY < goalY && !positionTaken(startX-1, startY+1))
    {
      antAction.direction = Direction.SOUTHWEST;
    }
    else if (startX < goalX && startY < goalY && !positionTaken(startX+1, startY+1))
    {
      antAction.direction = Direction.SOUTHEAST;
    }
    else if (startX == goalX && startY > goalY && !positionTaken(startX, startY-1))
    {
      antAction.direction = Direction.NORTH;
    }
    else if (startX == goalX && startY < goalY && !positionTaken(startX, startY+1))
    {
      antAction.direction = Direction.SOUTH;
    }
    else if (startX > goalX && startY == goalY && !positionTaken(startX+1, startY))
    {
      antAction.direction = Direction.EAST;
    }
    else if (startX < goalX && startY == goalY && !positionTaken(startX-1, startY))
    {
      antAction.direction = Direction.WEST;
    }
    else
    {
      System.out.println("FINDING ALTERNATIVE PATH.");
      //find an alternative path when an ant is occupying a coordinate already
      for(int i=-1; i<=1; i++)
      {
        for(int j=-1; j<=1; j++)
        {
          if(!positionTaken(startX+i, startY+j))
          {
            //no case for 0,0
            if (i==-1 && j==-1) antAction.direction = Direction.NORTHWEST;
            else if (i==-1 && j==0) antAction.direction = Direction.WEST;
            else if (i==-1 && j==1) antAction.direction = Direction.SOUTHWEST;
            else if (i==0 && j==-1) antAction.direction = Direction.NORTH;
            else if (i==0 && j==1) antAction.direction = Direction.SOUTH;
            else if (i==1 && j==-1) antAction.direction = Direction.NORTHEAST;
            else if (i==1 && j==0) antAction.direction = Direction.EAST;
            else if (i==1 && j==1) antAction.direction = Direction.SOUTHEAST;
          }
        }
      }
    }
//    if (startX > goalX && startY > goalY)
//    {
//      antAction.direction = Direction.NORTHWEST;
//    }
//    else if (startX < goalX && startY > goalY)
//    {
//      antAction.direction = Direction.NORTHEAST;
//    }
//    else if (startX > goalX && startY < goalY)
//    {
//      antAction.direction = Direction.SOUTHWEST;
//    }
//    else if (startX < goalX && startY < goalY)
//    {
//      antAction.direction = Direction.SOUTHEAST;
//    }
//    else if (startX == goalX && startY > goalY)
//    {
//      antAction.direction = Direction.NORTH;
//    }
//    else if (startX == goalX && startY < goalY)
//    {
//      antAction.direction = Direction.SOUTH;
//    }
//    else if (startX > goalX && startY == goalY)
//    {
//      antAction.direction = Direction.EAST;
//    }
//    else if (startX < goalX && startY == goalY)
//    {
//      antAction.direction = Direction.WEST;
//    }
//    else
//    {
//      antAction.direction = Direction.getRandomDir();
//    }
    return antAction;
  }
  
  @Override
  public boolean goExplore()
  {
    //make them move North East all the time
    Direction dir = Direction.getRandomDir();
    System.out.println("Inside RWAI goExplore");
//        Direction dir = Direction.EAST; //go east
//        Direction dir = Direction.NORTH; //go north
    antAction.type = AntAction.AntActionType.MOVE;
    antAction.direction = dir;
    return true;
  }
  
  @Override
  public boolean exitNest()
  {
    System.out.println("Inside RWAI exitNest");
    if (antData.underground)
    {
      antAction.type = AntAction.AntActionType.EXIT_NEST;
      //when food is EAST of the nest
//            antAction.x = centerX;
//            antAction.y = centerY - 9;
      antAction.x = centerX - (Constants.NEST_RADIUS - 1) + random.nextInt(2 * (Constants.NEST_RADIUS - 1));
      antAction.y = centerY - (Constants.NEST_RADIUS - 1) + random.nextInt(2 * (Constants.NEST_RADIUS - 1));
      return true;
    }
    return false;
  }
  
  @Override
  public boolean pickUpFoodAdjacent()
  {
    if (commData.foodSet.size() == 0) return false;
    int antX = antData.gridX;
    int antY = antData.gridY;
    
    FoodData food = null;
    for (FoodData f : commData.foodSet)
    {
      food = f;
    }
    int foodX = food.gridX;
    int foodY = food.gridY;
    
    antAction.type = AntAction.AntActionType.PICKUP;
    antAction.quantity = 2;
    if (foodX == antX && foodY == antY-1)
    {
      antAction.direction = Direction.NORTH;
    }
    else if (foodX == antX && foodY == antY+1)
    {
      antAction.direction = Direction.SOUTH;
    }
    else if (foodX == antX+1 && foodY == antY-1)
    {
      antAction.direction = Direction.NORTHEAST;
    }
    else if (foodX == antX-1 && foodY == antY-1)
    {
      antAction.direction = Direction.NORTHWEST;
    }
    else if (foodX == antX+1 && foodY == antY+1)
    {
      antAction.direction = Direction.SOUTHEAST;
    }
    else if (foodX == antX-1 && foodY == antY+1)
    {
      antAction.direction = Direction.SOUTHWEST;
    }
    else if (foodX == antX+1 && foodY == antY)
    {
      antAction.direction = Direction.EAST;
    }
    else if (foodX == antX-1 && foodY == antY)
    {
      antAction.direction = Direction.WEST;
    }
    else
    {
      //return false when there is no adjacent food
      return false;
    }
    return true;
  }
  
  @Override
  public boolean goHomeIfCarryingOrHurt()
  {
    if (antData.carryUnits > 0)
    {
      antAction = chooseDirection(antData.gridX, antData.gridY, centerX, centerY);
      antAction.type = AntAction.AntActionType.MOVE;
      if (Util.manhattanDistance(antData.gridX, antData.gridY, centerX, centerY) <= Constants.NEST_RADIUS)
      {
        antAction = chooseDirection(antData.gridX, antData.gridY, centerX, centerY); //drop when food is in NORTH
        antAction.type = AntAction.AntActionType.DROP;
        antAction.quantity = antData.carryUnits; //just drop all
      }
      return true;
    }
    return false;
    
  }
  
  @Override
  public boolean goToFood()
  {
    int goToX = 0;
    int goToY = 0;
    int closestFood = 1000000;
    antAction.type = AntAction.AntActionType.MOVE;
    if (!commData.foodSet.isEmpty() || commData.foodSet != null)
    {
      for (FoodData food : commData.foodSet)
      {
        int distance = Util.manhattanDistance(food.gridX, food.gridY, antData.gridX, antData.gridY);
        if (distance < closestFood)
        {
          goToX = food.gridX;
          goToY = food.gridY;
          closestFood = distance;
        }
      }
      if (goToX != 0 && goToY != 0)
      {
        antAction = chooseDirection(antData.gridX, antData.gridY, goToX, goToY);
        return true;
      }
    }
    return false;
  }
  
  //This is where everything is not working correctly
  //used to check if an ant is already in this coordinate of the map
  private boolean positionTaken(int gridX, int gridY)
  {
    for (AntData antData : commData.myAntList)
    {
      if (antData.gridX == gridX && antData.gridY == gridY)
      {
        System.out.println("Ant in the position: " + antData);
        return true;
      }
    }
    return false;
  }
}
