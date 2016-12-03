package antworld.client;

import antworld.common.*;
import com.sun.deploy.util.SessionState;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Phillip on 11/18/2016.
 */
public class RandomWalkAI extends AI
{
  private int aggroRadius = 10;
  private int healthThreshold = 5;
  AStar aStarObject; //initialized to null beginning and end
  LinkedList<ClientCell> AStarPath = null; //look at how AI is given to the ants
  ConcurrentHashMap<Integer, AntStatus> antStatusHashMap = new ConcurrentHashMap<>(); //contains all the ant IDs and their AntStatus
  ConcurrentHashMap<ClientCell, FoodStatus> foodBank = new ConcurrentHashMap<>();
//  ArrayList<FoodStatus> foodBank = new ArrayList<>();
  
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
//    antAction.direction = Direction.SOUTH;
    return true;
  }
  
  @Override
  public boolean exitNest()
  {
    System.out.println("Inside RWAI exitNest");
    if (antData.underground && antData.health >= 20)
    {
      antAction.type = AntAction.AntActionType.EXIT_NEST;
      //when food is EAST of the nest
//      antAction.x = centerX - 9;
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
    else if (antData.carryUnits == 0 && antData.health <= healthThreshold)
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
        //put in foodBank when food is found, it should be there until its count drops to 0
        ClientCell foodCell = ClientRandomWalk.world[goToX][goToY];
        aStarObject.setBeginAndEnd(ClientRandomWalk.world[centerX][centerY], foodCell);
//        System.out.println("In goToFood(), findingPath from nest to food, HARVESTER nest center coords= ("+centerX+", "+centerY+")");
        FoodStatus food = null;
        if (!foodBank.containsKey(foodCell))
        {
          food = new FoodStatus(foodCell, aStarObject.findPath());
          foodBank.put(foodCell, food); //add the new food
        }
        else
        {
          food = foodBank.get(foodCell);
        }
        AntStatus antStatus = antStatusHashMap.get(antData.id);
        if (antStatus.path.isEmpty())
        {
          ClientCell antCell = ClientRandomWalk.world[antData.gridX][antData.gridY];
          aStarObject.setBeginAndEnd(antCell, food.currentPathHead);
          System.out.println("food.currentPathHead coordinates = ("+food.currentPathHead.x +", "+food.currentPathHead.y+")");
          food.antSecureSpotInAssemblyLine(); //move the assembly line's path head
          //TODO: one of the neighbors are null? OR is it because our ant is in the nest?
          antStatus.path = aStarObject.findPath(); //set this path as the path for the ant
          ClientCell next = antStatus.path.peek();
          ClientCell nextInPath = null;
          ClientCell last = antStatus.path.getLast();
          //TODO: Start working here
          antStatus.path.poll(); //because the first position is the ants position
          if (!positionTaken(next.x, next.y))
          {
            nextInPath = antStatus.path.poll();
            findDirectionForActionThatRequiresIt(antData.gridX, antData.gridY, nextInPath.x, nextInPath.y);
          }
          else
          {
//            antAction.type = AntAction.AntActionType.STASIS;
            antAction.type = AntAction.AntActionType.MOVE;
            antAction = chooseDirection(antData.gridX, antData.gridY, last.x, last.y);
            int altDeltaX = antAction.direction.deltaX();
            int altDeltaY = antAction.direction.deltaY();
            if(Util.manhattanDistance(antData.gridX+altDeltaX, antData.gridY+altDeltaY, last.x, last.y) <
                    Util.manhattanDistance(next.x, next.y, last.x, last.y))
            {
              antStatus.path.poll(); //remove the next in the path if we skipped over it
            }
          }
          antStatus.type = AntStatus.StatusType.GOING_TO_ASSEMBLY; //so far it's the only type we have;
        }
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
    //because hashMap overwrites existing values!
    if (!antStatusHashMap.containsKey(antData.id))
    {
      //if null StatusType in AntStatus, ants has normal StatusType
      antStatusHashMap.put(antData.id, new AntStatus());
    }
    
    antAction = new AntAction(AntAction.AntActionType.STASIS);
    if (antData.ticksUntilNextAction > 0) return this.antAction;
    
    //follow the A* path if going to Assembly Line
    AntStatus antStatus = antStatusHashMap.get(antData.id);
    
    if (antStatus.type == AntStatus.StatusType.IN_ASSEMBLY)
    {
      System.out.println("did the ant get to assembly? Is the path empty" + antStatus.path.isEmpty());
      return antAction;
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
    
//    if (AStarPath != null && !AStarPath.isEmpty())
//    {
//      System.out.println("following the path produced by AStar");
//      antAction.type = AntAction.AntActionType.MOVE;
//      ClientCell nextToDestination = AStarPath.poll();
//      antAction = chooseDirection(antData.gridX, antData.gridY, nextToDestination.x, nextToDestination.y);
//      return this.antAction;
//    }
    
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
