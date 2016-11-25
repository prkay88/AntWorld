package antworld.client;

import antworld.common.*;

/**
 * Created by Phillip on 11/18/2016.
 */
public class RandomWalkAI extends AI
{
  public RandomWalkAI(CommData data, AntData antData)
  {
    super(data, antData);
//    this.commData = data;
    centerX = commData.nestData[commData.myNest.ordinal()].centerX;
    centerY = commData.nestData[commData.myNest.ordinal()].centerY;
//    antAction.type = AntAction.AntActionType.STASIS;
//    this.antData = antData;
  }
  
  private AntAction chooseDirection(int startX, int startY, int goalX, int goalY)
  {
    //Problem: Need to not tell the ants to move to a cell with food in it!
//        AntAction antAction = new AntAction(AntAction.AntActionType.MOVE);
    //ask commData if there is an ant at the position i'm looking to go to.
    System.out.println("In RWAI choosDirection()");
    if (startX > goalX && startY > goalY && !positionTaken(startX - 1, startY - 1))
    {
      antAction.direction = Direction.NORTHWEST;
    }
    else if (startX < goalX && startY > goalY && !positionTaken(startX + 1, startY - 1))
    {
      antAction.direction = Direction.NORTHEAST;
    }
    else if (startX > goalX && startY < goalY && !positionTaken(startX - 1, startY + 1))
    {
      antAction.direction = Direction.SOUTHWEST;
    }
    else if (startX < goalX && startY < goalY && !positionTaken(startX + 1, startY + 1))
    {
      antAction.direction = Direction.SOUTHEAST;
    }
    else if (startX == goalX && startY > goalY && !positionTaken(startX, startY - 1))
    {
      antAction.direction = Direction.NORTH;
    }
    else if (startX == goalX && startY < goalY && !positionTaken(startX, startY + 1))
    {
      antAction.direction = Direction.SOUTH;
    }
    else if (startX < goalX && startY == goalY && !positionTaken(startX + 1, startY))
    {
      antAction.direction = Direction.EAST;
    }
    else if (startX > goalX && startY == goalY && !positionTaken(startX - 1, startY))
    {
      antAction.direction = Direction.WEST;
    }
    else
    {
      System.out.println("FINDING ALTERNATIVE PATH.");
      //problem is breaking ties with two paths that have the same manhattan distance
      antAction.direction = Direction.getRandomDir();
      
//      int xClosest = centerX;
//      int yClosest = centerY;
////      int xClosest = commData.nestData[commData.myNest.ordinal()].centerX;
////      int yClosest = commData.nestData[commData.myNest.ordinal()].centerY;
//      //find an alternative path when an ant is occupying a coordinate already
//      for (int i = -1; i <= 1; i++)
//      {
//        for (int j = -1; j <= 1; j++)
//        {
//          if (!positionTaken(startX + i, startY + j))
//          {
//            int distanceToFood = Util.manhattanDistance(startX + i, startY + j, goalX, goalY);
//            int currentClosestDistance = Util.manhattanDistance(xClosest, yClosest, goalX, goalY);
//
//            //only bother if it's closer, by observation, startX and startY will never be closest
//            if (distanceToFood < currentClosestDistance || (xClosest == startX && yClosest == startY))
//            {
//              //this is the new closest coordinates
//              xClosest = startX + i;
//              yClosest = startY + j;
//              //no case for 0,0
//              if (i == -1 && j == -1)
//              {
//                antAction.direction = Direction.NORTHWEST;
//              }
//              else if (i == -1 && j == 0)
//              {
//                antAction.direction = Direction.WEST;
//                System.out.println("Chose WEST, distance="+distanceToFood);
//              }
//              else if (i == -1 && j == 1)
//              {
//                antAction.direction = Direction.SOUTHWEST;
//                System.out.println("Chose SOUTHWEST, distance="+distanceToFood);
//              }
//              else if (i == 0 && j == -1)
//              {
//                antAction.direction = Direction.NORTH;
//              }
//              else if (i == 0 && j == 1)
//              {
//                antAction.direction = Direction.SOUTH;
//              }
//              else if (i == 1 && j == -1)
//              {
//                antAction.direction = Direction.NORTHEAST;
//                System.out.println("Chose NORTHEAST, distance="+distanceToFood);
//              }
//              else if (i == 1 && j == 0)
//              {
//                antAction.direction = Direction.EAST;
//              }
//              else if (i == 1 && j == 1)
//              {
//                antAction.direction = Direction.SOUTHEAST;
//              }
//            }
//          }
//        }
//      }
    }
    return antAction;
  }
  
  @Override
  public boolean goExplore()
  {
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
            antAction.x = centerX+9;
            antAction.y = centerY;
//      antAction.x = centerX - (Constants.NEST_RADIUS - 1) + random.nextInt(2 * (Constants.NEST_RADIUS - 1));
//      antAction.y = centerY - (Constants.NEST_RADIUS - 1) + random.nextInt(2 * (Constants.NEST_RADIUS - 1));
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
    if (foodX == antX && foodY == antY - 1)
    {
      antAction.direction = Direction.NORTH;
    }
    else if (foodX == antX && foodY == antY + 1)
    {
      antAction.direction = Direction.SOUTH;
    }
    else if (foodX == antX + 1 && foodY == antY - 1)
    {
      antAction.direction = Direction.NORTHEAST;
    }
    else if (foodX == antX - 1 && foodY == antY - 1)
    {
      antAction.direction = Direction.NORTHWEST;
    }
    else if (foodX == antX + 1 && foodY == antY + 1)
    {
      antAction.direction = Direction.SOUTHEAST;
    }
    else if (foodX == antX - 1 && foodY == antY + 1)
    {
      antAction.direction = Direction.SOUTHWEST;
    }
    else if (foodX == antX + 1 && foodY == antY)
    {
      antAction.direction = Direction.EAST;
    }
    else if (foodX == antX - 1 && foodY == antY)
    {
      antAction.direction = Direction.WEST;
    }
    else
    {
      //return false when there is no adjacent food
      return false;
    }
//    System.out.println("Picking up food.");
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
        System.out.println("Will have ActionType MOVE");
        return true;
      }
    }
    return false;
  }
  
  //This is where everything is not working correctly
  //used to check if an ant is already in this coordinate of the map
  // true if coords are taken
  // false if coords is NOT taken
  private boolean positionTaken(int gridX, int gridY)
  {
    for (AntData antData : commData.myAntList)
    {
      LandType typeAtCoordinates = ClientRandomWalk.world[gridX][gridY].landType;
      if (antData.gridX == gridX && antData.gridY == gridY || typeAtCoordinates == LandType.WATER)
      {
        System.out.println("Ant in the position: " + antData);
        return true;
      }
    }
    for (FoodData foodData : commData.foodSet)
    {
      if(foodData.gridX == gridX && foodData.gridY == gridY)
      {
        return true;
      }
    }
    return false;
  }
  
  //uses map statistics for water location
  @Override
  public boolean goToWater()
  {
    if (commData.foodStockPile[FoodType.WATER.ordinal()] < 100)
    {
      antAction.type = AntAction.AntActionType.MOVE;
      antAction = chooseDirection(antData.gridX, antData.gridY, 65, 140);
      return true;
    }
    return false;
  }
  
  @Override
  public boolean pickUpWater()
  {
    //x=65, y=140 is the coordinates of one of the water patches in SmallMap3.png
    int antX = antData.gridX;
    int antY = antData.gridY;
    int waterX = 65;
    int waterY = 140;
    antAction.type = AntAction.AntActionType.PICKUP;
    antAction.quantity = 2;
    if (waterX == antX && waterY == antY - 1)
    {
      antAction.direction = Direction.NORTH;
    }
    else if (waterX == antX && waterY == antY + 1)
    {
      antAction.direction = Direction.SOUTH;
    }
    else if (waterX == antX + 1 && waterY == antY - 1)
    {
      antAction.direction = Direction.NORTHEAST;
    }
    else if (waterX == antX - 1 && waterY == antY - 1)
    {
      antAction.direction = Direction.NORTHWEST;
    }
    else if (waterX == antX + 1 && waterY == antY + 1)
    {
      antAction.direction = Direction.SOUTHEAST;
    }
    else if (waterX == antX - 1 && waterY == antY + 1)
    {
      antAction.direction = Direction.SOUTHWEST;
    }
    else if (waterX == antX + 1 && waterY == antY)
    {
      antAction.direction = Direction.EAST;
    }
    else if (waterX == antX - 1 && waterY == antY)
    {
      antAction.direction = Direction.WEST;
    }
    else
    {
      return false;
    }
    return true;
  }
  
  @Override
  public AntAction chooseAction()
  {
    antAction = new AntAction(AntAction.AntActionType.STASIS);
//        AntAction action = new AntAction(AntAction.AntActionType.STASIS);
    if (antData.ticksUntilNextAction > 0) return this.antAction;
  
    if (exitNest()) return this.antAction; //always exit nest first
  
    if (goHomeIfCarryingOrHurt()) return this.antAction; //must come before goToFood() or goToWater()
    
    if (pickUpWater()) return this.antAction;
    
    if (goToWater()) return this.antAction;
  
    if (pickUpFoodAdjacent()) return this.antAction;
  
    if (goToFood()) return this.antAction;
  
    if (goExplore()) return this.antAction;
  
    if (attackAdjacent()) return this.antAction;
  
    if (goToEnemyAnt()) return this.antAction;
  
    if (goToGoodAnt()) return this.antAction;
  
    return this.antAction;
  }
  
  private void findDirectionForActionThatRequiresIt(int antX, int antY, int goalX, int goalY)
  {
    if (goalX == antX && goalY == antY - 1)
    {
      antAction.direction = Direction.NORTH;
    }
    else if (goalX == antX && goalY == antY + 1)
    {
      antAction.direction = Direction.SOUTH;
    }
    else if (goalX == antX + 1 && goalY == antY - 1)
    {
      antAction.direction = Direction.NORTHEAST;
    }
    else if (goalX == antX - 1 && goalY == antY - 1)
    {
      antAction.direction = Direction.NORTHWEST;
    }
    else if (goalX == antX + 1 && goalY == antY + 1)
    {
      antAction.direction = Direction.SOUTHEAST;
    }
    else if (goalX == antX - 1 && goalY == antY + 1)
    {
      antAction.direction = Direction.SOUTHWEST;
    }
    else if (goalX == antX + 1 && goalY == antY)
    {
      antAction.direction = Direction.EAST;
    }
    else if (goalX == antX - 1 && goalY == antY)
    {
      antAction.direction = Direction.WEST;
    }
  }
}
