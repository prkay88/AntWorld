package antworld.client;

import antworld.common.*;

import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Created by Phillip on 11/18/2016.
 */
public class RandomWalkAI extends AI
{
  
  private int aggroRadius = 10;
  private int healthThreshold = 19;
  AStar aStarObject; //initialized to null beginning and end
  LinkedList<ClientCell> AStarPath = null; //look at how AI is given to the ants
  ConcurrentHashMap<Integer, AntStatus> antStatusHashMap = new ConcurrentHashMap<>(); //contains all the ant IDs and their AntStatus
  ConcurrentHashMap<ClientCell, FoodStatus> foodBank = new ConcurrentHashMap<>();
//  ArrayList<FoodStatus> foodBank = new ArrayList<>();
//private static final int DIR_BIT_N  = 1;
//  private static final int DIR_BIT_NE = 2;
//  private static final int DIR_BIT_E  = 4;
//  private static final int DIR_BIT_SE = 8;
//  private static final int DIR_BIT_S  = 16;
//  private static final int DIR_BIT_SW = 32;
//  private static final int DIR_BIT_W  = 64;
//  private static final int DIR_BIT_NW = 128;
//
//  private static final int DIR_BIT_ANY_N = DIR_BIT_N | DIR_BIT_NE | DIR_BIT_NW;
//  private static final int DIR_BIT_ANY_S = DIR_BIT_S | DIR_BIT_SE | DIR_BIT_SW;
//  private static final int DIR_BIT_ANY_E = DIR_BIT_E | DIR_BIT_NE | DIR_BIT_SE;
//  private static final int DIR_BIT_ANY_W = DIR_BIT_W | DIR_BIT_NW | DIR_BIT_SW;
//  private static final int MAX_EXPLORE_DIST = 300;
  
  public RandomWalkAI(CommData data, AntData antData)
  {
    super(data, antData);
//    this.commData = data;
    centerX = commData.nestData[commData.myNest.ordinal()].centerX;
    centerY = commData.nestData[commData.myNest.ordinal()].centerY;
//    antAction.type = AntAction.AntActionType.STASIS;
//    this.antData = antData;
    aStarObject = new AStar(null,null);
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
      //TODO: trying to see if stopping ants is better instead of forcing them to move.
//      System.out.println("FINDING ALTERNATIVE PATH.");
      //problem is breaking ties with two paths that have the same manhattan distance
      antAction.direction = Direction.getRandomDir();
//      antAction.type = AntAction.AntActionType.STASIS;
    }
//    updateMap(antAction.direction);
    return antAction;
  }
  
//  private void updateMap(Direction chosenDir)
//  {
//    ClientRandomWalk.world[antData.gridX][antData.gridY].height-=100000000;
//    ClientRandomWalk.world[antData.gridX+chosenDir.deltaX()][antData.gridY+chosenDir.deltaY()].height+=100000000;
//  }
  
//  @Override
  public boolean goExplore()
  {
//    Direction dir = Direction.getRandomDir();
    System.out.println("Inside RWAI goExplore");
//        Direction dir = Direction.EAST; //go east
//        Direction dir = Direction.NORTH; //go north
    antAction.type = AntAction.AntActionType.MOVE;
    antAction.direction = antStatusHashMap.get(antData.id).mainDirection;
    //TODO: if position taken for the main direction change the antId's AntStatus's mainDirection field
    if (positionTaken(antData.gridX + antAction.direction.deltaX(), antData.gridY + antAction.direction.deltaY()))
    {
      antStatusHashMap.get(antData.id).mainDirection = Direction.getRandomDir();
    }
//    updateMap(antAction.direction);
//    antAction.direction = Direction.SOUTH;
    return true;
  }
  
  @Override
  public boolean underGroundAction()
  {
    System.out.println("Inside RWAI underGroundAction");
    if (antData.id != Constants.UNKNOWN_ANT_ID && antData.underground)
    {
      if (antData.health >= healthThreshold)
      {
        antAction.type = AntAction.AntActionType.EXIT_NEST;
        //when food is EAST of the nest
//      antAction.x = centerX;
//      antAction.y = centerY - Constants.NEST_RADIUS;
//      antAction.x = centerX - Constants.NEST_RADIUS;
//      antAction.y = centerY;
        antAction.x = centerX - (Constants.NEST_RADIUS - 1) + random.nextInt(2 * (Constants.NEST_RADIUS - 1));
        antAction.y = centerY - (Constants.NEST_RADIUS - 1) + random.nextInt(2 * (Constants.NEST_RADIUS - 1));
//      ClientRandomWalk.world[antAction.x][antAction.y].height+=100000000; //ants occupy cells now
      }
      else if (antData.health < healthThreshold)
      {
        //this is how ants heal
        antAction.type = AntAction.AntActionType.HEAL;
      }
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
    antAction.quantity = antData.antType.getCarryCapacity(); //TODO: better so far, uncomment for proper behavior
//    antAction.quantity = 1;
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
    else if (antData.carryUnits == 0 && antData.health <= healthThreshold)
    {
      if (Util.manhattanDistance(antData.gridX, antData.gridY, centerX, centerY) <= Constants.NEST_RADIUS)
      {
        antAction = chooseDirection(antData.gridX, antData.gridY, centerX, centerY); //drop when food is in NORTH
        antAction.type = AntAction.AntActionType.ENTER_NEST;
        ClientRandomWalk.world[antData.gridX][antData.gridY].height-=100000000; //because they will be gone
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
      AntStatus antStatus = antStatusHashMap.get(antData.id);
      //find A* path to the food
      if (antStatus.action == AntStatus.CurrentAction.ROAMING && goToX != 0 && goToY != 0)
      {
        ClientCell foodCell = ClientRandomWalk.world[goToX][goToY];
        antStatus.targetfoodCell = foodCell;
        aStarObject.setBeginAndEnd(ClientRandomWalk.world[antData.gridX][antData.gridY], foodCell);
        antStatus.setPath(aStarObject.findPath());
        //need to poll first and last to have proper behavior
        antStatus.path.pollFirst();
        antStatus.path.pollLast();
        int nextX = antStatus.path.get(antStatus.nextCellIndex).x;
        int nextY = antStatus.path.get(antStatus.nextCellIndex).y;
        antAction = chooseDirection(antData.gridX, antData.gridY, nextX, nextY);
        if (antStatus.nextCellIndex < antStatus.path.size())
        {
          if (!positionTaken(nextX, nextY))
          {
            antStatus.nextCellIndex++;
          }
        }
        antStatus.action = AntStatus.CurrentAction.FOLLOWING_FOOD;
//        antAction = chooseDirection(antData.gridX, antData.gridY, goToX, goToY); //uncomment for proper behavior
        return true;
        //put in foodBank when food is found, it should be there until its count drops to 0
//        ClientCell foodCell = ClientRandomWalk.world[goToX][goToY];
//        aStarObject.setBeginAndEnd(ClientRandomWalk.world[centerX][centerY], foodCell);
////        System.out.println("In goToFood(), findingPath from nest to food, HARVESTER nest center coords= ("+centerX+", "+centerY+")");
//        FoodStatus food = null;
//        if (!foodBank.containsKey(foodCell))
//        {
//          food = new FoodStatus(foodCell, aStarObject.findPath());
//          foodBank.put(foodCell, food); //add the new food
//        }
//        else
//        {
//          food = foodBank.get(foodCell);
//        }
//        antStatusHashMap.get(antData.id).targetFood = food; //now ant has this food as its target
//        AntStatus antStatus = antStatusHashMap.get(antData.id);
//        if (antStatus.path.isEmpty())
//        {
//          ClientCell antCell = ClientRandomWalk.world[antData.gridX][antData.gridY];
//          aStarObject.setBeginAndEnd(antCell, food.currentPathHead);
//          System.out.println("food.currentPathHead coordinates = ("+food.currentPathHead.x +", "+food.currentPathHead.y+")");
//          antStatus.indexInAssembly = food.currentPathHeadIndex; //put the index it reserved here
//          food.antSecureSpotInAssemblyLine(); //move the assembly line's path head
//          antStatus.path = aStarObject.findPath(); //set this path as the path for the ant
//          ClientCell next = antStatus.path.peek();
//          ClientCell nextInPath = null;
//          ClientCell last = antStatus.path.getLast();
//          antStatus.path.poll(); //because the first position is the ants position
//          if (!positionTaken(next.x, next.y))
//          {
//            nextInPath = antStatus.path.poll();
//            findDirectionForActionThatRequiresIt(antData.gridX, antData.gridY, nextInPath.x, nextInPath.y);
//            updateMap(antAction.direction); //set by findDirectionForAction...() method
//          }
//          else
//          {
//            antAction.type = AntAction.AntActionType.MOVE;
//            antAction = chooseDirection(antData.gridX, antData.gridY, last.x, last.y);
//            int altDeltaX = antAction.direction.deltaX();
//            int altDeltaY = antAction.direction.deltaY();
//            if(Util.manhattanDistance(antData.gridX+altDeltaX, antData.gridY+altDeltaY, last.x, last.y) <
//                    Util.manhattanDistance(next.x, next.y, last.x, last.y))
//            {
//              antStatus.path.poll(); //remove the next in the path if we skipped over it
//            }
//          }
//          antStatus.type = AntStatus.StatusType.GOING_TO_ASSEMBLY; //so far it's the only type we have;
//        }
//        return true;
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
    if (commData.foodStockPile[FoodType.WATER.ordinal()] < 100)
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
    //so that the newly spawned ants from client random walk will not have to ChooseAction
    if (antData.id == Constants.UNKNOWN_ANT_ID)
    {
      return antAction;
    }
    //because hashMap overwrites existing values!
    if (!antStatusHashMap.containsKey(antData.id))
    {
      //if null StatusType in AntStatus, ants has normal StatusType
      antStatusHashMap.put(antData.id, new AntStatus(Direction.getRandomDir()));
    }
    
    //priority is to spawn ants
//    if (spawnNewAnt()) return antAction;
    
    antAction = new AntAction(AntAction.AntActionType.STASIS);
    if (antData.ticksUntilNextAction > 0) return this.antAction;
    
    AntStatus antStatus = antStatusHashMap.get(antData.id);
    System.out.println("Ant's action is currently:" + antStatus.action);
    if (antStatus.action == AntStatus.CurrentAction.FOLLOWING_FOOD)
    {
      if (antStatus.nextCellIndex < antStatus.path.size())
      {
        antAction.type = AntAction.AntActionType.MOVE;
        int nextX = antStatus.path.get(antStatus.nextCellIndex).x;
        int nextY = antStatus.path.get(antStatus.nextCellIndex).y;
        antAction = chooseDirection(antData.gridX, antData.gridY, nextX, nextY);
        if (!positionTaken(nextX, nextY))
        {
          antStatus.nextCellIndex++;
        }
        return antAction;
      }
      else
      {
        //food is reached, find an A* path to go home
        ClientCell antCell = ClientRandomWalk.world[antData.gridX][antData.gridY];
        ClientCell nestCell = ClientRandomWalk.world[centerX-Constants.NEST_RADIUS][centerY];
        aStarObject.setBeginAndEnd(antCell, nestCell);
        antStatus.setPath(aStarObject.findPath());
        antStatus.path.pollFirst();
        antStatus.path.pollLast();
        antStatus.nextCellIndex = 0; //reset to 0 when going home
        antStatus.action = AntStatus.CurrentAction.GOING_HOME; //just go pass through the if statement, expected to pick up food
      }
    }
    else if (antStatus.action == AntStatus.CurrentAction.GOING_HOME)
    {
      //TODO: Start working here, stuck on water when trying to get home
      int nextCellIndex = antStatus.nextCellIndex;
      if (antStatus.nextCellIndex < antStatus.path.size())
      {
        int nextX = antStatus.path.get(nextCellIndex).x;
        int nextY = antStatus.path.get(nextCellIndex).y;
        antAction.type = AntAction.AntActionType.MOVE;
        antAction = chooseDirection(antData.gridX, antData.gridY, nextX, nextY);
        if (!positionTaken(nextX, nextY))
        {
          antStatus.nextCellIndex++;
        }
        return antAction;
      }
      else
      {
        //initialize back to the food cell if the food is dropped
        if (antData.carryUnits == 0)
        {
          aStarObject.setBeginAndEnd(ClientRandomWalk.world[antData.gridX][antData.gridY], antStatus.targetfoodCell);
          antStatus.setPath(aStarObject.findPath());
          antStatus.path.pollFirst();
          antStatus.path.pollLast();
          antStatus.nextCellIndex = 0;
          antStatus.action = AntStatus.CurrentAction.FOLLOWING_FOOD;
        }
      }
    }
    
//    if (antStatus.)
    /*
    //follow the A* path if going to Assembly Line
    AntStatus antStatus = antStatusHashMap.get(antData.id);
    
    if (antStatus.type == AntStatus.StatusType.IN_ASSEMBLY)
    {
      System.out.println("did the ant get to assembly? Is the path empty" + antStatus.path.isEmpty());
      FoodStatus targetFood = antStatusHashMap.get(antData.id).targetFood;
      int assemblyIndex = antStatus.indexInAssembly;
      if (antData.carryUnits > 0)
      {
        int indexToDrop = assemblyIndex-1;
        ClientCell dropOfSpace = targetFood.pathFromNestToFood.get(indexToDrop);
        int dropX = dropOfSpace.x;
        int dropY = dropOfSpace.y;
        antAction.type = AntAction.AntActionType.DROP;
        antAction.quantity = antData.carryUnits;
        findDirectionForActionThatRequiresIt(antData.gridX, antData.gridY, dropX, dropY);
//        antAction = chooseDirection(antData.gridX, antData.gridY, dropX, dropY);
        return antAction;
        //drop the food
      }
      else if (antData.carryUnits == 0)
      {
        int indexToPickUp = assemblyIndex+1;
        ClientCell foodSpace = targetFood.pathFromNestToFood.get(indexToPickUp);
        int foodX = foodSpace.x;
        int foodY = foodSpace.y;
        antAction.type = AntAction.AntActionType.PICKUP;
        antAction.quantity = antData.antType.getCarryCapacity(); //want to pick up max units
        findDirectionForActionThatRequiresIt(antData.gridX, antData.gridY, foodX, foodY);
//        antAction = chooseDirection(antData.gridX, antData.gridY, foodX, foodY);
        return antAction;
      }
      else
      {
        //just stasis
        return antAction;
      }
    }
    if (antStatus.type == AntStatus.StatusType.GOING_TO_ASSEMBLY)
    {
      System.out.println("did the ant get to assembly? Is the path empty" + antStatus.path.isEmpty());
      if (antStatus.path.isEmpty())
      {
        antStatus.type = AntStatus.StatusType.IN_ASSEMBLY;
        return antAction;
      }
      ClientCell next = antStatus.path.peek();
      if (positionTaken(next.x, next.y))
      {
        //recomputing the A* path to its spot in the assembly
        ClientCell antCell = ClientRandomWalk.world[antData.gridX][antData.gridY];
        aStarObject.setBeginAndEnd(antCell, antStatus.targetFood.pathFromNestToFood.get(antStatus.indexInAssembly));
        antStatus.path = aStarObject.findPath(); //set this path as the path for the ant
//      } //TODO: testing recomputation of A* remove this curly brace for proper behavior
        antAction.type = AntAction.AntActionType.MOVE;
        ClientCell last = antStatus.path.getLast();
        antAction = chooseDirection(antData.gridX, antData.gridY, last.x, last.y);
        int altDeltaX = antAction.direction.deltaX();
        int altDeltaY = antAction.direction.deltaY();
        //if next is the last position in the A* path from ant to its spot in assembly line, poll it
        if(Util.manhattanDistance(antData.gridX+altDeltaX, antData.gridY+altDeltaY, last.x, last.y) <
                Util.manhattanDistance(next.x, next.y, last.x, last.y))
        {
          antStatus.path.poll(); //remove the next in the path if we skipped over it
        }
        return antAction; //stasis, don't move if another ant is there
      }
      antAction.type = AntAction.AntActionType.MOVE;
      ClientCell nextInPath = antStatus.path.poll();
      System.out.println("nextInPath="+nextInPath);
      
      antAction = chooseDirection(antData.gridX, antData.gridY, nextInPath.x, nextInPath.y);
//      findDirectionForActionThatRequiresIt(antData.gridX, antData.gridY, nextInPath.x, nextInPath.y);
      return antAction;
    }
    */
    
    if (underGroundAction()) return this.antAction; //always exit nest first
  
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
  
//
//  public int getDirectionBitsOpen(AntData ant)
//  {
//    int dirBits = 255;
//    for (Direction dir : Direction.values())
//    {
//      int x = ant.gridX + dir.deltaX();
//      int y = ant.gridY + dir.deltaY();
//      int bit = 1 << dir.ordinal();
//
//      ClientCell neighborCell = ClientRandomWalk.world[x][y];
//
//      if (neighborCell == null) dirBits = dirBits & bit;
//
//      else if (neighborCell.landType == LandType.WATER) dirBits -= bit;
//
//
//      else if ((neighborCell.landType == LandType.NEST) && (!inAreaAroundCoord(centerX,centerY,neighborCell.x, neighborCell.y)))  dirBits -= bit;
//
////      else if (neighborCell.getGameObject() != null) dirBits -= bit; ////TODO: this
//
////      else if ((neighborCell.getNest() != null) && (neighborCell.getNest() != myNest))  dirBits -= bit;
////
////      else if (neighborCell.getGameObject() != null) dirBits -= bit;
//    }
//
//    System.out.println("  getDirectionBitsOpen() dirBits="+dirBits);
//    return dirBits;
//  }
//
//  private boolean inAreaAroundCoord(int xCenter, int yCenter, int xToCheck, int yToCheck)
//  {
//    //if in between the x values
//    if (xToCheck >= xCenter-Constants.NEST_RADIUS/2 && xToCheck <= xCenter+Constants.NEST_RADIUS/2 &&
//            yToCheck <= yToCheck+Constants.NEST_RADIUS/2 && yToCheck >= yToCheck-Constants.NEST_RADIUS/2)
//    {
//      return true;
//    }
//    return false;
//  }
//
//  public boolean goExplore()
//  {
//
//    if (Util.manhattanDistance(antData.gridX, antData.gridY, centerX, centerY) > MAX_EXPLORE_DIST) return false;
//
//
//    int goalX = 0;
//    int goalY = 0;
//    if (antData.gridX > centerX) goalX = 1000000;
//    if (antData.gridY > centerY) goalY = 1000000;
//
//    int dirBits = getDirectionBitsOpen(antData);
//    dirBits = getDirBitsToLocation(dirBits, antData.gridX, antData.gridY, goalX, goalY);
//
//    if (antData.myAction.type == AntAction.AntActionType.MOVE)
//    {
//      int dx = antData.myAction.direction.deltaY();
//      int dy = antData.myAction.direction.deltaY();
//      int lastGoalX = goalX;
//      int lastGoalY = goalY;
//      if (dx != 0) lastGoalX = antData.gridX + dx;
//      if (dy != 0) lastGoalY = antData.gridY + dy;
//
//      dirBits = getDirBitsToLocation(dirBits, antData.gridX, antData.gridY, lastGoalX, lastGoalY);
//    }
//
//    if (dirBits == 0) return false;
//
//    return goToward(antData, goalX, goalY);
//  }
//
//  public static int getDirBitsToLocation(int dirBits, int x, int y, int xx, int yy)
//  {
//
//    //System.out.println("  getDirBitsToLocation("+dirBits+", " + x +", " + y + ", " + xx+ ", " + yy + ")");
//    //System.out.println("0 " + dirBits  +" & DIR_BIT_ANY_E=" + DIR_BIT_ANY_E);
//    if (xx <= x) dirBits = dirBits & (~DIR_BIT_ANY_E);
//
//    //System.out.println("1 " + dirBits);
//    if (xx >= x) dirBits = dirBits & (~DIR_BIT_ANY_W);
//
//    //System.out.println("2 " + dirBits);
//    if (yy <= y) dirBits = dirBits & (~DIR_BIT_ANY_S);
//    if (yy >= y) dirBits = dirBits & (~DIR_BIT_ANY_N);
//
//    return dirBits;
//  }
//
//  public boolean goToward(AntData ant, int x, int y)
//  {
//    int dirBits = getDirectionBitsOpen(ant);
//
//    dirBits = getDirBitsToLocation(dirBits, ant.gridX, ant.gridY, x, y);
//
//    Direction dir = getRandomDirection(dirBits);
//
//    if (dir == null) return false;
//    ant.myAction.type = AntAction.AntActionType.MOVE;
//    ant.myAction.direction = dir; //TODO: uncomment for proper behavior
////    ant.myAction.direction = Direction.WEST; //TODO: delete this for proper behavior
//    return true;
//  }
//
//  public Direction getRandomDirection(int dirBits)
//  {
//    Direction dir = Direction.getRandomDir();
//    for (int i = 0; i<Direction.SIZE; i++)
//    {
//      int bit = 1 << dir.ordinal();
//      if ((bit & dirBits) != 0) return dir;
//
//      dir = Direction.getRightDir(dir);
//    }
//    return null;
//  }
}
