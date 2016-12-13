package antworld.client;

import antworld.common.*;

import java.awt.*;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SwarmAI contains the logic for ants implementing a swarm behavior.
 * Ants following the decision tree of SwarmAI will tend to stay in small
 * packs which is further divided into three groups for better spread
 */
public class SwarmAI extends AI
{
  private int aggroRadius = 20;
  private int healthThreshold = 15;
  
  private final int SWARMID;
  AStar aStarObject; //initialized to null beginning and end
  LinkedList<ClientCell> AStarPath = null; //look at how AI is given to the ants
  ConcurrentHashMap<Integer, ExtraAntData> antStatusHashMap = new ConcurrentHashMap<>(); //contains all the ant IDs and their ExtraAntData
  ConcurrentHashMap<ClientCell, FoodStatus> foodBank = new ConcurrentHashMap<>();
  Swarm mySwarm;
  
  /**
   * Creates a SwarmAI instance
   *
   * @param swarmID
   * @param data
   * @param antData
   */
  public SwarmAI(int swarmID, CommData data, AntData antData)
  {
    super(data, antData);
    centerX = commData.nestData[commData.myNest.ordinal()].centerX;
    centerY = commData.nestData[commData.myNest.ordinal()].centerY;
    this.SWARMID = swarmID;
    aStarObject = new AStar(null, null);
  }
  
  /**
   * Called to set the Swarm object to be enclosed in this AI
   *
   * @param swarm
   */
  public void setMySwarm(Swarm swarm)
  {
    mySwarm = swarm;
  }
  
  //Sets antAction to a direction for a certain action. Contains
  //the checking of locations that are not allowed.
  private AntAction chooseDirection(int startX, int startY, int goalX, int goalY)
  {
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
      antAction.direction = Direction.getRandomDir();
      ExtraAntData extraAntData = antStatusHashMap.get(antData.id);
    }
    
    return antAction;
  }
  
  /**
   * Tells an ant to explore the map and follow the "rules" imposed by the Swarm
   * they belong to
   *
   * @return
   */
  public boolean goExplore()
  {
    
    antAction.type = AntAction.AntActionType.MOVE;
    ExtraAntData extraAntData = antStatusHashMap.get(antData.id);
    Direction currentDirection = extraAntData.mainDirection;
    //checking to see if an is inside swarm. If not, choosing random direction until they are.
    antAction.direction = currentDirection;
    //System.out.println("in goExplore, antAction.direction = " + antAction.direction);
    //TODO: uncomment for Phil's proper swarm behavior
    if (mySwarm.swarmLocationMap.get(antData.id) == 0)
    {
      if(!mySwarm.insideInnerRadius( antData.gridX, antData.gridY))
      {
        antAction = chooseDirection(antData.gridX, antData.gridY, mySwarm.getCenterX(), mySwarm.getCenterY());
        //System.out.println("In goExplore(): the ant is not inside the outerRadius");
        antStatusHashMap.get(antData.id).mainDirection = antAction.direction;
      }
      else
      {
        antStatusHashMap.get(antData.id).updateRoamingDirection();
        antAction.direction = antStatusHashMap.get(antData.id).mainDirection;
      }


      return true;
    }
    else if(mySwarm.swarmLocationMap.get(antData.id) == 1)
    {
      if(!mySwarm.insideMiddleRadius(antData.gridX, antData.gridY))
      {
        antAction = chooseDirection(antData.gridX, antData.gridY, mySwarm.getCenterX(), mySwarm.getCenterY());
        //System.out.println("In goExplore(): the ant is not inside the outerRadius");
        antStatusHashMap.get(antData.id).mainDirection = antAction.direction;
      }
      else
      {
        antStatusHashMap.get(antData.id).updateRoamingDirection();
        antAction.direction = antStatusHashMap.get(antData.id).mainDirection;
      }
      return true;
    }
    else if(mySwarm.swarmLocationMap.get(antData.id) == 2)
    {
      if(!mySwarm.insideOuterRadius( antData.gridX, antData.gridY))
      {
        antAction = chooseDirection(antData.gridX, antData.gridY, mySwarm.getCenterX(), mySwarm.getCenterY());
        //System.out.println("In goExplore(): the ant is not inside the outerRadius");
        antStatusHashMap.get(antData.id).mainDirection = antAction.direction;
      }
      else
      {
        antStatusHashMap.get(antData.id).updateRoamingDirection();
        antAction.direction = antStatusHashMap.get(antData.id).mainDirection;
      }
      return true;
    }
    if (positionTaken(antData.gridX + currentDirection.deltaX(), antData.gridY + currentDirection.deltaY()))
    {
      antStatusHashMap.get(antData.id).updateRoamingDirection();
      //System.out.println("in go Explore(): roaming direction updated");
    }
    
    antAction.direction = antStatusHashMap.get(antData.id).mainDirection;
    //Changes the main direction randomly 5% of time, to try to spread out swarms a bit more
    if (random.nextDouble() <= .05)
    {
      antStatusHashMap.get(antData.id).updateRoamingDirection();
      antAction.direction = antStatusHashMap.get(antData.id).mainDirection;
    }
    return true;
    //TODO: delete this:
//    antAction.direction = Direction.WEST;
  }
  
  /**
   * Ants who need to pop up from being underground or after healing themselves
   * will use this method to get out of the nest.
   *
   * @return
   */
  @Override
  public boolean underGroundAction()
  {
    //System.out.println("In underGroundAction");
    if (antData.id != Constants.UNKNOWN_ANT_ID && antData.underground)
    {
      if (antData.health >= healthThreshold)
      {
        antAction.type = AntAction.AntActionType.EXIT_NEST;
        //antAction.x = centerX - (Constants.NEST_RADIUS - 1) + random.nextInt(2 * (Constants.NEST_RADIUS - 1));
        //antAction.y = centerY - (Constants.NEST_RADIUS - 1) + random.nextInt(2 * (Constants.NEST_RADIUS - 1));
        if (SWARMID % 4 == 0)
        {
          antAction.x = random.nextInt((centerX + (Constants.NEST_RADIUS - 1) - centerX) + 1) + centerX;
          antAction.y = random.nextInt((centerY - (centerY - Constants.NEST_RADIUS - 1)) + 1) + (centerY - Constants.NEST_RADIUS - 1);
          
        }
        else if (SWARMID % 4 == 1)
        {
          antAction.x = random.nextInt((centerX - (centerX - Constants.NEST_RADIUS - 1)) + 1) + (centerX - Constants.NEST_RADIUS - 1);
          antAction.y = random.nextInt((centerY - (centerY - Constants.NEST_RADIUS - 1)) + 1) + (centerY - Constants.NEST_RADIUS - 1);
        }
        else if (SWARMID % 4 == 2)
        {
          antAction.x = random.nextInt((centerX - (centerX - Constants.NEST_RADIUS - 1)) + 1) + (centerX - Constants.NEST_RADIUS - 1);
          antAction.y = random.nextInt(((centerY + Constants.NEST_RADIUS - 1) - centerY) + 1) + centerY;
        }
        else if (SWARMID % 4 == 3)
        {
          antAction.x = random.nextInt((centerX + (Constants.NEST_RADIUS - 1) - centerX) + 1) + centerX;
          antAction.y = random.nextInt(((centerY + Constants.NEST_RADIUS - 1) - centerY) + 1) + centerY;
        }
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
  
  /**
   * Ants will pick up the nearest food source by using this method. It checks
   * to see if there is one that is adjacent (1 cell away) from the
   * ant's coordinates.
   *
   * @return
   */
  @Override
  public boolean pickUpFoodAdjacent()
  {
    //System.out.println("in pickUpFoodAdjacent");
    ExtraAntData extraAntData = antStatusHashMap.get(antData.id);
    //right now its not picking up food unless target food cell is not null
    int antX = antData.gridX;
    int antY = antData.gridY;
    int foodX = 0;
    int foodY = 0;
    int currentClosestX = 99999999; //to find the closest food's x coord
    int currentClosestY = 99999999; //to find the closest food's y coord
    for (FoodData foodData : commData.foodSet)
    {
      if (Util.manhattanDistance(antX, antY, foodData.gridX, foodData.gridY) <=
              Util.manhattanDistance(currentClosestX, currentClosestY, antX, antY))
      {
        foodX = foodData.gridX;
        foodY = foodData.gridY;
      }
      if (foodX == 0 && foodY == 0)
      {
        return false;
      }
    }
    
    antAction.type = AntAction.AntActionType.PICKUP;
    antAction.quantity = antData.antType.getCarryCapacity();
    
    //means there is no food in the food set of commData
    
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
      return false;
    }
    return true;
  }
  
  /**
   *
   * @return
   */
  @Override
  public boolean goHomeIfCarryingOrHurt()
  {
    if (antData.carryUnits >= 15)
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
        ClientRandomWalk.world[antData.gridX][antData.gridY].height -= 100000000; //because they will be gone
      }
      return true;
    }
    return false;
  }
  
  @Override
  public boolean goToFood()
  {
    //System.out.println("in goToFood()" + "foodSet size: " + commData.foodSet.size());
    int goToX = 0;
    int goToY = 0;
    int closestFood = 1000000;
    ExtraAntData extraAntData = antStatusHashMap.get(antData.id);
    antAction.type = AntAction.AntActionType.MOVE;
    //TODO: incorporate targetFoodX and targetFoodY
    if (extraAntData.targetFoodX != 0 && extraAntData.targetFoodY != 0)
    {
      int visionRadius = antData.antType.getVisionRadius();
      goToX = extraAntData.targetFoodX;
      goToY = extraAntData.targetFoodY;
      closestFood = Util.manhattanDistance(goToX, goToY, antData.gridX, antData.gridY);
      
      //should only check for food's visibility if it's within the vision radius
      if (closestFood <= visionRadius)
      {
        boolean foodVisible = false;
        for (FoodData foodData : commData.foodSet)
        {
          if (foodData.gridX == goToX && foodData.gridY == goToY)
          {
            foodVisible = true;
          }
        }
        if (!foodVisible)
        {
          goToX = 0;
          goToY = 0;
          closestFood = 1000000;
          antStatusHashMap.get(antData.id).targetFoodX = 0;
          antStatusHashMap.get(antData.id).targetFoodY = 0;
        }
      }
    }
    if (!commData.foodSet.isEmpty() && commData.foodSet != null)
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
        if (distance >= 300) return false;
      }
      
      antAction = chooseDirection(antData.gridX, antData.gridY, goToX, goToY);
      antStatusHashMap.get(antData.id).targetFoodX = goToX;
      antStatusHashMap.get(antData.id).targetFoodY = goToY;
      return true;
      //TODO: trying to remove A*
//      ExtraAntData extraAntData = antStatusHashMap.get(antData.id);
//      //find A* path to the food
//      if (extraAntData.action == ExtraAntData.CurrentAction.ROAMING && goToX != 0 && goToY != 0)
//      {
//        ClientCell foodCell = ClientRandomWalk.world[goToX][goToY];
//        extraAntData.targetfoodCell = foodCell;
//        aStarObject.setBeginAndEnd(ClientRandomWalk.world[antData.gridX][antData.gridY], foodCell);
//        extraAntData.setPath(aStarObject.findPath());
//        //need to poll first and last to have proper behavior
//        if (extraAntData.path.size() > 3)
//        {
//          extraAntData.path.pollFirst();
//          extraAntData.path.pollLast();
//        }
////        if (extraAntData.path.size() == 0)
////        {
////          return false;
////        }
//        int nextX = extraAntData.path.get(extraAntData.nextCellIndex).x;
//        int nextY = extraAntData.path.get(extraAntData.nextCellIndex).y;
//
//        antAction = chooseDirection(antData.gridX, antData.gridY, nextX, nextY);
//        if (extraAntData.nextCellIndex < extraAntData.path.size())
//        {
//          if (!positionTaken(nextX, nextY))
//          {
//            extraAntData.nextCellIndex++;
//          }
//        }
//        extraAntData.action = ExtraAntData.CurrentAction.FOLLOWING_FOOD;
////        antAction = chooseDirection(antData.gridX, antData.gridY, goToX, goToY); //uncomment for proper behavior
//        return true;
//
//      }
      //TODO: uncomment till here to get back to the A* implementation
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
        //System.out.println("Ant in the position: " + antData);
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
    //System.out.println("In goToWater()");
    if (commData.foodStockPile[FoodType.WATER.ordinal()] < 100)
    {
      int antX = antData.gridX;
      int antY = antData.gridY;
      int goToX = 0;
      int goToY = 0;
      int closestWater = 1000000;
      antAction.type = AntAction.AntActionType.MOVE;
      //search 30x30 grid around the ant, if water is not found, choose random direction.
      for (int i = -15; i <= 15; i++)
      {
        if (antX + i < 0) continue; //illegal coordinates
        for (int j = -15; j <= 15; j++)
        {
          if (antY + j < 0) continue; //illegal coordinates
          int distanceToWater = Util.manhattanDistance(antData.gridX, antData.gridY, i, j);
          if (ClientRandomWalk.world[antX + i][antY + j].landType == LandType.WATER &&
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
    //System.out.println("in pickUpWater()");
    //x=65, y=140 is the coordinates of one of the water patches in SmallMap3.png
    int antX = antData.gridX;
    int antY = antData.gridY;
    ClientCell[][] world = ClientRandomWalk.world;
//    int waterX = 65;
//    int waterY = 140;
    antAction.quantity = 2;
    if (commData.foodStockPile[FoodType.WATER.ordinal()] < 100)
    {
      if (world[antX][antY - 1].landType == LandType.WATER)
      {
        //System.out.println("antX=" + antX + ", antY=" + antY + ", world[antX][antY-1]=" + world[antX][antY - 1].landType);
        antAction.direction = Direction.NORTH;
      }
      else if (world[antX][antY + 1].landType == LandType.WATER)
      {
        antAction.direction = Direction.SOUTH;
      }
      else if (world[antX + 1][antY - 1].landType == LandType.WATER)
      {
        antAction.direction = Direction.NORTHEAST;
      }
      else if (world[antX - 1][antY - 1].landType == LandType.WATER)
      {
        antAction.direction = Direction.NORTHWEST;
      }
      else if (world[antX + 1][antY + 1].landType == LandType.WATER)
      {
        antAction.direction = Direction.SOUTHEAST;
      }
      else if (world[antX - 1][antY + 1].landType == LandType.WATER)
      {
        antAction.direction = Direction.SOUTHWEST;
      }
      else if (world[antX + 1][antY].landType == LandType.WATER)
      {
        antAction.direction = Direction.EAST;
      }
      else if (world[antX - 1][antY].landType == LandType.WATER)
      {
        antAction.direction = Direction.WEST;
      }
      else
      {
        return false;
      }
    }
    else
    {
      return false;
    }
    antAction.type = AntAction.AntActionType.PICKUP;
    //System.out.println("Ant with id=" + antData.id + " found water and will pick it up.");
    return true;
  }
  
  //work on this
  @Override
  public boolean attackAdjacent()
  {
    //System.out.println("in attackAdjacent()");
    int antX = antData.gridX;
    int antY = antData.gridY;
    int enemyAntX = 0;
    int enemyAntY = 0;
    
    if (commData.enemyAntSet.isEmpty())
    {
      return false;
    }
    ExtraAntData extraAntData = antStatusHashMap.get(antData.id);
    
    if (extraAntData.targetAntId == -2)
    {
      return false; //no target
    }
    else
    {
      for (AntData enemyAnt : commData.enemyAntSet)
      {
        //if the enemy ant id is found, set the x coords of the attack
        if (enemyAnt.id == extraAntData.targetAntId)
        {
          enemyAntX = enemyAnt.gridX;
          enemyAntY = enemyAnt.gridY;
        }
      }
      if (enemyAntX == 0 && enemyAntY == 0)
      {
        antStatusHashMap.get(antData.id).targetAntId = -2; //the ant is not there anymore
        return false;
      }
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
  
  //gives a score for an enemy ant to be chosen as target, bigger = more likely to be the target
  private int vulnerabilityScore(int hitPoints, int carryUnits, int distance)
  {
    return carryUnits + (20-hitPoints) - distance;
  }
  
  //used in attackAdjacent()
  private Point setTargetAnt()
  {
    int enemyAntX = 0;
    int enemyAntY = 0;
    if (antStatusHashMap.get(antData.id).targetAntId == -2)
    {
      //find the ant that is most vulnerable
      int score = -9999999;
      for (AntData enemyAnt : commData.enemyAntSet)
      {
        int distance = Util.manhattanDistance(antData.gridX, antData.gridY, enemyAnt.gridX, enemyAnt.gridY);
        int vulnerabilityScore = vulnerabilityScore(enemyAnt.health, enemyAnt.carryUnits, distance);
        if (vulnerabilityScore >= score)
        {
          score = vulnerabilityScore;
          //System.out.println("enemy antId being checked="+ enemyAnt.id);
          antStatusHashMap.get(antData.id).targetAntId = enemyAnt.id;
          enemyAntX = enemyAnt.gridX;
          enemyAntY = enemyAnt.gridY;
        }
      }
    }
    //if (0,0) is returned, then no ants
    return new Point(enemyAntX, enemyAntY);
  }
  
  //used in goToEnemyAnt()
  public boolean withinAggro(int myAntX, int myAntY,
                             int enemyAntX, int enemyAntY)
  {
    //System.out.println("in goToEnemyAnt()");
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
//      System.out.println("in goToEnemyAnt(), enemyAntSet is empty.");
      return false;
    }
    int enemyAntX = 0;
    int enemyAntY = 0;
    ExtraAntData extraAntData = antStatusHashMap.get(antData.id);
    boolean targetAntVisible = false;
    for (AntData enemyAnt : commData.enemyAntSet)
    {
      if (enemyAnt.id == extraAntData.targetAntId)
      {
        enemyAntX = enemyAnt.gridX;
        enemyAntY = enemyAnt.gridY;
        targetAntVisible = true;
        break;
      }
    }
    //this means that the targetAntId = -2
    if (!targetAntVisible)
    {
      //System.out.println("in goToEnemyAnt, targetAnt is not visible");
      antStatusHashMap.get(antData.id).targetAntId = -2; //-2 is the default since -1 is for birthed ants
      
      Point targetAntCoord = setTargetAnt(); //Now it's default, set the target ant coords
      enemyAntX = targetAntCoord.x;
      enemyAntY = targetAntCoord.y;
    }
    
//    for (AntData enemyAnt : commData.enemyAntSet)
//    {
//      for (AntData myAnt : commData.myAntList)
//      {
//
    if (withinAggro(antData.gridX, antData.gridY, enemyAntX, enemyAntY))
    {
      antAction.type = AntAction.AntActionType.MOVE;
      antAction = chooseDirection(antData.gridX, antData.gridY, enemyAntX, enemyAntY);
      return true;
    }
//      }
//    }
    return false;
  }
  
  @Override
  public AntAction chooseAction()
  {
    //so that the newly spawned ants from client random walk will not have to ChooseAction
    if (antData.id == Constants.UNKNOWN_ANT_ID)
    {
      System.out.println("AntID: "+antData.id+ " returned because ANTID equals unkown ID");
      return antAction;
    }
    //because hashMap overwrites existing values!
    if (!antStatusHashMap.containsKey(antData.id))
    {
      //if null StatusType in ExtraAntData, ants has normal StatusType
      antStatusHashMap.put(antData.id, new ExtraAntData(Direction.getRandomDir()));
    }
    ExtraAntData extraAntData = antStatusHashMap.get(antData.id);
    //System.out.println("in chooseAction's extraAntData, target food is at: ("+extraAntData.targetFoodX +", " + extraAntData.targetFoodY+")");
    //TODO: uncomment for A* behavior
//    if (extraAntData != null && extraAntData.targetfoodCell != null)
//    {
//      boolean foodStillThere = false;
//      for (FoodData food : commData.foodSet)
//      {
//        if (food.gridX == extraAntData.targetfoodCell.x && food.gridY == extraAntData.targetfoodCell.y)
//        {
//          foodStillThere = true;
//        }
//      }
//      //if the food is not there anymore, set the ant's action to roaming
//      if (!foodStillThere)
//      {
//        antStatusHashMap.get(antData.id).action = ExtraAntData.CurrentAction.ROAMING;
//        antStatusHashMap.get(antData.id).path.clear();
//        antStatusHashMap.get(antData.id).nextCellIndex = 0;
//        antStatusHashMap.get(antData.id).targetfoodCell = null;
//      }
//    }
    
    antAction = new AntAction(AntAction.AntActionType.STASIS);
    if (antData.ticksUntilNextAction > 0)
    {
      System.out.println("ANTID: "+ antData.id+ " returned because ticksTillNextAction is greater than 0");
      return this.antAction;
    }

    //TODO: uncomment for A* behavior
//    ExtraAntData extraAntData = antStatusHashMap.get(antData.id);
    //System.out.println("Ant's action is currently:" + extraAntData.action);
//    if (extraAntData.action == ExtraAntData.CurrentAction.FOLLOWING_FOOD)
//    {
//      if (extraAntData.nextCellIndex < extraAntData.path.size())
//      {
//        antAction.type = AntAction.AntActionType.MOVE;
//        int nextX = extraAntData.path.get(extraAntData.nextCellIndex).x;
//        int nextY = extraAntData.path.get(extraAntData.nextCellIndex).y;
//        antAction = chooseDirection(antData.gridX, antData.gridY, nextX, nextY);
//        if (!positionTaken(nextX, nextY))
//        {
//          extraAntData.nextCellIndex++;
//        }
//        return antAction;
//      }
//      else
//      {
//        //food is reached, find an A* path to go home
//        ClientCell antCell = ClientRandomWalk.world[antData.gridX][antData.gridY];
//        ClientCell nestCell = ClientRandomWalk.world[centerX - Constants.NEST_RADIUS][centerY];
//        aStarObject.setBeginAndEnd(antCell, nestCell);
//        extraAntData.setPath(aStarObject.findPath());
//        if (extraAntData.path.size() > 3)
//        {
//          extraAntData.path.pollFirst();
//          extraAntData.path.pollLast();
//        }
//        extraAntData.nextCellIndex = 0; //reset to 0 when going home
//        extraAntData.action = ExtraAntData.CurrentAction.GOING_HOME; //just go pass through the if statement, expected to pick up food
//      }
//    }
//    else if (extraAntData.action == ExtraAntData.CurrentAction.GOING_HOME)
//    {
//      //TODO: Start working here, stuck on water when trying to get home
//      int nextCellIndex = extraAntData.nextCellIndex;
//      if (extraAntData.nextCellIndex < extraAntData.path.size())
//      {
//        int nextX = extraAntData.path.get(nextCellIndex).x;
//        int nextY = extraAntData.path.get(nextCellIndex).y;
//        antAction.type = AntAction.AntActionType.MOVE;
//        antAction = chooseDirection(antData.gridX, antData.gridY, nextX, nextY);
//        if (!positionTaken(nextX, nextY))
//        {
//          extraAntData.nextCellIndex++;
//        }
//        return antAction;
//      }
//      else
//      {
//        //initialize back to the food cell if the food is dropped
//        if (antData.carryUnits == 0)
//        {
//          aStarObject.setBeginAndEnd(ClientRandomWalk.world[antData.gridX][antData.gridY], extraAntData.targetfoodCell);
//          extraAntData.setPath(aStarObject.findPath());
//          if (extraAntData.path.size() > 3)
//          {
//            extraAntData.path.pollFirst();
//            extraAntData.path.pollLast();
//          }
//          extraAntData.nextCellIndex = 0;
//          extraAntData.action = ExtraAntData.CurrentAction.FOLLOWING_FOOD;
//        }
//      }
//    }
  
    if (goHomeIfCarryingOrHurt())
    {
      System.out.println("AntID: "+antData.id+" choose action in goHomeIfCarryOrHurt");
      return this.antAction; //must come before goToFood() or goToWater()
    }
    
    if (underGroundAction())
    {
      System.out.println("AntID: "+antData.id+" choose action in underGroundAction");
      return this.antAction; //always exit nest first
    }
    if (pickUpFoodAdjacent())
    {
      System.out.println("AntID: "+antData.id+" choose action in pickUpFoodAdjacent");
      return this.antAction;
    }
    if (attackAdjacent())
    {
      System.out.println("AntID: "+antData.id+" choose action in attackAdjacent");
      return this.antAction;
    }
    
    if (goToEnemyAnt())
    {
      System.out.println("AntID: "+antData.id+" choose action in goToEnemyAnt");
      return this.antAction; //always attack when sees an ant
    }
    if (goToFood())
    {
      System.out.println("AntID: "+antData.id+" choose action in goToFood");
      return this.antAction;
    }
    if (pickUpWater())
    {
      System.out.println("AntID: "+antData.id+" choose action in pickUpWater");
      return this.antAction;
    }
    if (goToWater())
    {
      System.out.println("AntID: "+antData.id+" choose action in goToWater");
      return this.antAction;
    }
    if (goExplore())
    {
      System.out.println("AntID: "+antData.id+" choose action in goExplore");
      return this.antAction;
    }
    if (goToGoodAnt())
    {
      System.out.println("AntID: "+antData.id+" choose action in goToGoodAnt");
      return this.antAction;
    }
    System.out.println("AntID: "+antData.id+" choose action in none of the methods");
    return this.antAction;
  }
  
  private Direction findDirectionOfOffset(int offSetX, int offSetY)
  {
    for (Direction direction : Direction.values())
    {
      if (offSetX == direction.deltaX() && offSetY == direction.deltaY())
      {
        return direction;
      }
    }
    return null;
  }
  
  
}
