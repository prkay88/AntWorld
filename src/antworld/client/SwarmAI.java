package antworld.client;

import antworld.common.*;

import java.awt.*;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Phillip on 12/7/2016.
 */
public class SwarmAI extends AI
{
  private int aggroRadius = 20;
  private int healthThreshold = 15;
  
  private final int SWARMID;
//  AStar aStarObject; //initialized to null beginning and end
  LinkedList<ClientCell> AStarPath = null; //look at how AI is given to the ants
  ConcurrentHashMap<Integer, ExtraAntData> antStatusHashMap = new ConcurrentHashMap<>(); //contains all the ant IDs and their ExtraAntData
  Swarm mySwarm;
  
  /**
   * Constructor for the SwarmAI class
   * @param swarmID is the id that matches the swarm it is actin upon
   * @param data is the commData to be acted upon
   * @param antData is the ant that will have it's action choosen
   */
  public SwarmAI(int swarmID, CommData data, AntData antData)
  {
    super(data, antData);
    centerX = commData.nestData[commData.myNest.ordinal()].centerX;
    centerY = commData.nestData[commData.myNest.ordinal()].centerY;
    this.SWARMID = swarmID;
  }
  
  /**
   * sets the swarm that this AI will be acting on
   * @param swarm
   */
  public void setMySwarm(Swarm swarm)
  {
    mySwarm = swarm;
  }
  
  
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
      //TODO: trying to see if stopping ants is better instead of forcing them to move.
      
      antAction.direction = Direction.getRandomDir();
      ExtraAntData extraAntData = antStatusHashMap.get(antData.id);
      
    }
    
    return antAction;
  }
  
  
  /**
   * method that chooses a direction for the ants to explore the map
   * @return
   */
  //  @Override
  public boolean goExplore()
  {
    
    antAction.type = AntAction.AntActionType.MOVE;
    ExtraAntData extraAntData = antStatusHashMap.get(antData.id);
    Direction currentDirection = extraAntData.mainDirection;
    //checking to see if an is inside swarm. If not, choosing random direction until they are.
    antAction.direction = currentDirection;
    System.out.println("currentDirection = " + antAction.direction+", deltaY(): "+ antAction.direction.deltaY());
    if (!mySwarm.insideOuterRadius(antData.gridX + antAction.direction.deltaX(), antData.gridY + antAction.direction.deltaY()))
    {
      antAction = chooseDirection(antData.gridX, antData.gridY, mySwarm.getCenterX(), mySwarm.getCenterY());
      antStatusHashMap.get(antData.id).mainDirection = antAction.direction;
      return true;
    }
    if (positionTaken(antData.gridX+currentDirection.deltaX(), antData.gridY+currentDirection.deltaY()))
    {
      antStatusHashMap.get(antData.id).updateRoamingDirection();
     
    }
    
    
    antAction.direction = antStatusHashMap.get(antData.id).mainDirection;
    //Changes the main direction randomly 5% of time, to try to spread out swarms a bit more
    if(random.nextDouble()<=.05)
    {
      antStatusHashMap.get(antData.id).updateRoamingDirection();
      antAction.direction = antStatusHashMap.get(antData.id).mainDirection;
    }
    return true;
  }
  
  /**
   * chooses to exit the nest or heal the ant
   * @return
   */
  @Override
  public boolean underGroundAction() {
    //System.out.println("In underGroundAction");
    if (antData.id != Constants.UNKNOWN_ANT_ID && antData.underground)
    {
      if (antData.health >= healthThreshold)
      {
        antAction.type = AntAction.AntActionType.EXIT_NEST;
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
   * checks to see if a food is adjacent to an ant. If so the ant picks up the food
   * @return
   */
  @Override
  public boolean pickUpFoodAdjacent()
  {
    
    ExtraAntData extraAntData = antStatusHashMap.get(antData.id);
    //right now its not picking up food unless target food cell is not null
    int antX = antData.gridX;
    int antY = antData.gridY;
    int foodX = 0;
    int foodY = 0;
    int currentClosestX = 99999999; //to find the closest food's x coord
    int currentClosestY = 99999999; //to find the closest food's y coord
    //TODO: add a check for when the food is already gone (reset the targetFoodX and targetFoodY)
    //TODO: the food set is small, so iterating to it to check if a food is close is not a big deal
//    if (extraAntData.targetfoodCell == null)
//    {
      for (FoodData foodData : commData.foodSet)
      {
        if (Util.manhattanDistance(antX, antY, foodData.gridX, foodData.gridY) <=
            Util.manhattanDistance(currentClosestX, currentClosestY, antX, antY))
        {
          foodX = foodData.gridX;
          foodY = foodData.gridY;
        }
//      }
      if (foodX == 0 && foodY == 0)
      {
        return  false;
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
      //For when the Ant's target food is gone, it roams again
      //return false when there is no adjacent food
      return false;
    }
    return true;
  }
  
  /**
   * chooses direction to move the ant closer to its nest if is below a health threshold or if it is full of a food
   * @return
   */
  @Override
  public boolean goHomeIfCarryingOrHurt()
  {
    
    if (antData.carryUnits >=  15)
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
  
  /**
   * chooses the direction to move to the closest food
   * @return
   */
  @Override
  public boolean goToFood()
  {
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
        if(distance >= 300) return false;
      }

      antAction = chooseDirection(antData.gridX, antData.gridY, goToX, goToY);
      antStatusHashMap.get(antData.id).targetFoodX = goToX;
      antStatusHashMap.get(antData.id).targetFoodY = goToY;
      return true;
 
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
  
  /**
   * sends the ant to water
   * @return
   */
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
  
  /**
   * if an ant is adjacent to water it will pick it up
   * @return
   */
  @Override
  public boolean pickUpWater()
  {
    
    int antX = antData.gridX;
    int antY = antData.gridY;
    ClientCell[][] world = ClientRandomWalk.world;
    antAction.quantity = 15;
    if (commData.foodStockPile[FoodType.WATER.ordinal()] > -10)
    {
      if (world[antX][antY - 1].landType == LandType.WATER)
      {
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
    return true;
  }
  
  /**
   * checks to see if an enemy ant is adjacent. If so, the ant will attack it.
   * @return
   */
  @Override
  public boolean attackAdjacent()
  {
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
  
  /**
   * checks to see if an enemy ant is within the attack range
   * @param myAntX
   * @param myAntY
   * @param enemyAntX
   * @param enemyAntY
   * @return
   */
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
  
  /**
   * finds closest enemy ant to target
   * @return
   */
  public boolean goToEnemyAnt()
  {
    if (commData.enemyAntSet.isEmpty())
    {

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
    
    if (!targetAntVisible)
    {
     
      antStatusHashMap.get(antData.id).targetAntId = -2; //-2 is the default since -1 is for birthed ants
      
      Point targetAntCoord = setTargetAnt(); //Now it's default, set the target ant coords
      enemyAntX = targetAntCoord.x;
      enemyAntY = targetAntCoord.y;
    }
    

    if (withinAggro(antData.gridX, antData.gridY, enemyAntX, enemyAntY))
    {
      antAction.type = AntAction.AntActionType.MOVE;
      antAction = chooseDirection(antData.gridX, antData.gridY, enemyAntX, enemyAntY);
      return true;
    }

    return false;
  }
  
  /**
   * This method is decision tree for each ant. It checks all the possible movement methods and returns
   * @return
   */
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
    
    
    antAction = new AntAction(AntAction.AntActionType.STASIS);
    if (antData.ticksUntilNextAction > 0)
    {
      return this.antAction;
    }

  
    if (goHomeIfCarryingOrHurt())
    {
      return this.antAction; //must come before goToFood() or goToWater()
    }
    
    if (underGroundAction())
    {
      return this.antAction; //always exit nest first
    }
    if (pickUpFoodAdjacent())
    {
      return this.antAction;
    }
    if (attackAdjacent())
    {
      return this.antAction;
    }
    
    if (goToEnemyAnt())
    {
      return this.antAction; //always attack when sees an ant
    }
    if (goToFood())
    {
      return this.antAction;
    }
    if (pickUpWater())
    {
      return this.antAction;
    }
    if (goToWater())
    {
      return this.antAction;
    }
    if (goExplore())
    {
      System.out.println("AntID: "+antData.id+" choose action in goExplore");
      return this.antAction;
    }
    if (goToGoodAnt())
    {
      return this.antAction;
    }
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
