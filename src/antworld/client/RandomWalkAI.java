package antworld.client;

import antworld.common.*;

/**
 * Created by Phillip on 11/18/2016.
 */
public class RandomWalkAI extends AI
{
  private int aggroRadius = 10;
  
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
//      antAction.x = centerX + Constants.NEST_RADIUS-1;
//      antAction.y = centerY;
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
    //must see no ants have health less than 18
    else if (antData.carryUnits == 0 && antData.health <= 18)
    {
      if (Util.manhattanDistance(antData.gridX, antData.gridY, centerX, centerY) <= Constants.NEST_RADIUS)
      {
        antAction = chooseDirection(antData.gridX, antData.gridY, centerX, centerY); //drop when food is in NORTH
        antAction.type = AntAction.AntActionType.ENTER_NEST;
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
      if (foodData.gridX == gridX && foodData.gridY == gridY)
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
    if (commData.foodStockPile[FoodType.WATER.ordinal()] < 120)
    {
      int antX = antData.gridX;
      int antY = antData.gridY;
      int goToX = 0;
      int goToY = 0;
      int closestWater = 1000000;
      antAction.type = AntAction.AntActionType.MOVE;
      //search 30x30 grid around the ant, if water is not found, choose random direction.
      for (int i=-15; i<=15; i++)
      {
        if (antX+i < 0) continue; //illegal coordinates
        for (int j=-15; j<=15; j++)
        {
          if (antY+j < 0) continue; //illegal coordinates
          int distanceToWater = Util.manhattanDistance(antData.gridX, antData.gridY, i,j);
          if (ClientRandomWalk.world[antX+i][antY+j].landType == LandType.WATER &&
                  distanceToWater < closestWater)
          {
            goToX = i;
            goToY = j;
            closestWater = distanceToWater;
          }
        }
      }
      //if no water land type in the 30x30 area around ant, then just choose random dir
      if (goToX == 0 && goToY == 0 && closestWater == 1000000)
      {
        antAction.direction = Direction.getRandomDir();
        return true;
      }
      antAction = chooseDirection(antData.gridX, antData.gridY, goToX, goToY);
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
    ClientCell[][] world = ClientRandomWalk.world;
//    int waterX = 65;
//    int waterY = 140;
    antAction.quantity = 2;
    if (world[antX][antY-1].landType==LandType.WATER)
    {
      System.out.println("antX="+antX+", antY="+antY+", world[antX][antY-1]="+world[antX][antY-1].landType);
      antAction.direction = Direction.NORTH;
    }
    else if (world[antX][antY+1].landType==LandType.WATER)
    {
      antAction.direction = Direction.SOUTH;
    }
    else if (world[antX+1][antY-1].landType==LandType.WATER)
    {
      antAction.direction = Direction.NORTHEAST;
    }
    else if (world[antX-1][antY-1].landType==LandType.WATER)
    {
      antAction.direction = Direction.NORTHWEST;
    }
    else if (world[antX+1][antY+1].landType==LandType.WATER)
    {
      antAction.direction = Direction.SOUTHEAST;
    }
    else if (world[antX-1][antY+1].landType==LandType.WATER)
    {
      antAction.direction = Direction.SOUTHWEST;
    }
    else if (world[antX+1][antY].landType==LandType.WATER)
    {
      antAction.direction = Direction.EAST;
    }
    else if (world[antX-1][antY].landType==LandType.WATER)
    {
      antAction.direction = Direction.WEST;
    }
    else
    {
      return false;
    }
    antAction.type = AntAction.AntActionType.PICKUP;
    System.out.println("Ant with id="+antData.id+" found water and will pick it up.");
    return true;
  }
  
  //work on this
  @Override
  public boolean attackAdjacent()
  {
    int antX = antData.gridX;
    int antY = antData.gridY;
    int enemyAntX = 65;
    int enemyAntY = 140;
    
    if (commData.enemyAntSet.isEmpty())
    {
      return false;
    }
    
    for (AntData enemyAnt : commData.enemyAntSet)
    {
      enemyAntX = enemyAnt.gridX;
      enemyAntY = enemyAnt.gridY;
    }
    antAction.type = AntAction.AntActionType.ATTACK;
    if (enemyAntX == antX && enemyAntY == antY - 1)
    {
      antAction.direction = Direction.NORTH;
    }
    else if (enemyAntX == antX && enemyAntY == antY + 1)
    {
      antAction.direction = Direction.SOUTH;
    }
    else if (enemyAntX == antX + 1 && enemyAntY == antY - 1)
    {
      antAction.direction = Direction.NORTHEAST;
    }
    else if (enemyAntX == antX - 1 && enemyAntY == antY - 1)
    {
      antAction.direction = Direction.NORTHWEST;
    }
    else if (enemyAntX == antX + 1 && enemyAntY == antY + 1)
    {
      antAction.direction = Direction.SOUTHEAST;
    }
    else if (enemyAntX == antX - 1 && enemyAntY == antY + 1)
    {
      antAction.direction = Direction.SOUTHWEST;
    }
    else if (enemyAntX == antX + 1 && enemyAntY == antY)
    {
      antAction.direction = Direction.EAST;
    }
    else if (enemyAntX == antX - 1 && enemyAntY == antY)
    {
      antAction.direction = Direction.WEST;
    }
    else
    {
      return false;
    }
    return true;
  }
  
  public boolean withinAggro(int myAntX, int myAntY,
                             int enemyAntX, int enemyAntY)
  {
    if (Util.manhattanDistance(myAntX, myAntY, enemyAntX, enemyAntY) <= aggroRadius)
    {
      return true;
    }
    return false;
  }
  
  public boolean goToEnemyAnt()
  {
    if (commData.enemyAntSet.isEmpty())
    {
      return false;
    }
    for (AntData enemyAnt : commData.enemyAntSet)
    {
      for (AntData myAnt : commData.myAntList)
      {
        if (withinAggro(myAnt.gridX, myAnt.gridY, enemyAnt.gridX, enemyAnt.gridY))
        {
          antAction.type = AntAction.AntActionType.MOVE;
          antAction = chooseDirection(myAnt.gridX, myAnt.gridY, enemyAnt.gridX, enemyAnt.gridY);
          return true;
        }
      }
    }
    return false;
  }
  
  
  @Override
  public AntAction chooseAction()
  {
    antAction = new AntAction(AntAction.AntActionType.STASIS);
    if (antData.ticksUntilNextAction > 0) return this.antAction;
    
    if (exitNest()) return this.antAction; //always exit nest first
  
    if (attackAdjacent()) return this.antAction;
    
    if (goToEnemyAnt()) return this.antAction; //always attack when sees an ant
    
    if (goHomeIfCarryingOrHurt()) return this.antAction; //must come before goToFood() or goToWater()
    
    if (pickUpWater()) return this.antAction;
    
    if (goToWater()) return this.antAction;
  
    if (pickUpFoodAdjacent()) return this.antAction;
    
    if (goToFood()) return this.antAction;
    
    if (goExplore()) return this.antAction;
    
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
