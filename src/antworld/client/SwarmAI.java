package antworld.client;

import antworld.common.*;

import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Phillip on 12/7/2016.
 */
public class SwarmAI extends AI
{
    private int aggroRadius = 10;
    private int healthThreshold = 15;
    private final int SWARMID;
    AStar aStarObject; //initialized to null beginning and end
    LinkedList<ClientCell> AStarPath = null; //look at how AI is given to the ants
    ConcurrentHashMap<Integer, ExtraAntData> antStatusHashMap = new ConcurrentHashMap<>(); //contains all the ant IDs and their ExtraAntData
    ConcurrentHashMap<ClientCell, FoodStatus> foodBank = new ConcurrentHashMap<>();


    public SwarmAI(int swarmID, CommData data, AntData antData)
    {
        super(data, antData);
        centerX = commData.nestData[commData.myNest.ordinal()].centerX;
        centerY = commData.nestData[commData.myNest.ordinal()].centerY;
        this.SWARMID = swarmID;
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

            antAction.direction = Direction.getRandomDir();
            ExtraAntData extraAntData = antStatusHashMap.get(antData.id);

        }

        return antAction;
    }



    //  @Override
    public boolean goExplore()
    {

        antAction.type = AntAction.AntActionType.MOVE;
        ExtraAntData extraAntData = antStatusHashMap.get(antData.id);
        //TODO: if position taken for the main direction change the antId's ExtraAntData's mainDirection field
        antAction.direction = antStatusHashMap.get(antData.id).mainDirection;
        System.out.println("In goExplore(), ticksTillNextUpdate="+ extraAntData.ticksTillUpdate);
        if (extraAntData.ticksTillUpdate == 0 ||
                positionTaken(antData.gridX + antAction.direction.deltaX(), antData.gridY + antAction.direction.deltaY()))
        {
            extraAntData.updateRoamingDirection();
        }
        if (extraAntData.ticksTillUpdate > 0)
        {
            extraAntData.ticksTillUpdate--;
        }
        antAction.direction = antStatusHashMap.get(antData.id).mainDirection;


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
                //antAction.x = centerX - (Constants.NEST_RADIUS - 1) + random.nextInt(2 * (Constants.NEST_RADIUS - 1));
                //antAction.y = centerY - (Constants.NEST_RADIUS - 1) + random.nextInt(2 * (Constants.NEST_RADIUS - 1));
                if(SWARMID % 4 == 0)
                {
                    antAction.x = random.nextInt((centerX+(Constants.NEST_RADIUS-1) - centerX) +1 ) + centerX;
                    antAction.y = random.nextInt((centerY - (centerY-Constants.NEST_RADIUS-1)) +1) + (centerY-Constants.NEST_RADIUS-1);

                }
                else if(SWARMID % 4 == 1)
                {
                    antAction.x = random.nextInt((centerX -(centerX-Constants.NEST_RADIUS-1)) +1) + (centerX-Constants.NEST_RADIUS-1);
                    antAction.y = random.nextInt((centerY - (centerY-Constants.NEST_RADIUS-1)) +1) + (centerY-Constants.NEST_RADIUS-1);
                }
                else if(SWARMID % 4 == 2)
                {
                    antAction.x = random.nextInt((centerX -(centerX-Constants.NEST_RADIUS-1)) +1) + (centerX-Constants.NEST_RADIUS-1);
                    antAction.y = random.nextInt(((centerY + Constants.NEST_RADIUS-1) - centerY) +1) + centerY;
                }
                else if(SWARMID % 4 ==3)
                {
                    antAction.x = random.nextInt((centerX+(Constants.NEST_RADIUS-1) - centerX) +1 ) + centerX;
                    antAction.y = random.nextInt(((centerY + Constants.NEST_RADIUS-1) - centerY) +1) + centerY;
                }
                System.out.println("CenterX: "+centerX+ " CenterY: "+centerY);
                System.out.println("SWARMID: "+SWARMID+ " antAction.x: "+antAction.x+ " antAction.y: " +antAction.y);




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
        ExtraAntData extraAntData = antStatusHashMap.get(antData.id);
        if (extraAntData.targetfoodCell == null) return false;
        int antX = antData.gridX;
        int antY = antData.gridY;

        int foodX = extraAntData.targetfoodCell.x;
        int foodY = extraAntData.targetfoodCell.y;

        antAction.type = AntAction.AntActionType.PICKUP;
        antAction.quantity = antData.antType.getCarryCapacity(); //TODO: better so far, uncomment for proper behavior

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
            ExtraAntData extraAntData = antStatusHashMap.get(antData.id);
            //find A* path to the food
            if (extraAntData.action == ExtraAntData.CurrentAction.ROAMING && goToX != 0 && goToY != 0)
            {
                ClientCell foodCell = ClientRandomWalk.world[goToX][goToY];
                extraAntData.targetfoodCell = foodCell;
                aStarObject.setBeginAndEnd(ClientRandomWalk.world[antData.gridX][antData.gridY], foodCell);
                extraAntData.setPath(aStarObject.findPath());
                //need to poll first and last to have proper behavior
                extraAntData.path.pollFirst();
                extraAntData.path.pollLast();
                int nextX = extraAntData.path.get(extraAntData.nextCellIndex).x;
                int nextY = extraAntData.path.get(extraAntData.nextCellIndex).y;
                antAction = chooseDirection(antData.gridX, antData.gridY, nextX, nextY);
                if (extraAntData.nextCellIndex < extraAntData.path.size())
                {
                    if (!positionTaken(nextX, nextY))
                    {
                        extraAntData.nextCellIndex++;
                    }
                }
                extraAntData.action = ExtraAntData.CurrentAction.FOLLOWING_FOOD;
//        antAction = chooseDirection(antData.gridX, antData.gridY, goToX, goToY); //uncomment for proper behavior
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
        if (commData.foodStockPile[FoodType.WATER.ordinal()] < 100)
        {
            if (world[antX][antY - 1].landType == LandType.WATER)
            {
                System.out.println("antX=" + antX + ", antY=" + antY + ", world[antX][antY-1]=" + world[antX][antY - 1].landType);
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
            //if null StatusType in ExtraAntData, ants has normal StatusType
            antStatusHashMap.put(antData.id, new ExtraAntData(Direction.getRandomDir()));
        }

        ExtraAntData extraAntData = antStatusHashMap.get(antData.id);
        //priority is to spawn ants
//    if (spawnNewAnt()) return antAction;
        if (extraAntData.targetfoodCell != null)
        {
            boolean foodStillThere = false;
            for (FoodData food : commData.foodSet)
            {
                if (food.gridX == extraAntData.targetfoodCell.x && food.gridY == extraAntData.targetfoodCell.y)
                {
                    foodStillThere = true;
                }
            }
            //if the food is not there anymore, set the ant's action to roaming
            if (!foodStillThere)
            {
                antStatusHashMap.get(antData.id).action = ExtraAntData.CurrentAction.ROAMING;
                antStatusHashMap.get(antData.id).path.clear();
                antStatusHashMap.get(antData.id).nextCellIndex = 0;
                antStatusHashMap.get(antData.id).targetfoodCell = null;
            }
        }

        antAction = new AntAction(AntAction.AntActionType.STASIS);
        if (antData.ticksUntilNextAction > 0) return this.antAction;

//    ExtraAntData extraAntData = antStatusHashMap.get(antData.id);
        System.out.println("Ant's action is currently:" + extraAntData.action);
        if (extraAntData.action == ExtraAntData.CurrentAction.FOLLOWING_FOOD)
        {
            if (extraAntData.nextCellIndex < extraAntData.path.size())
            {
                antAction.type = AntAction.AntActionType.MOVE;
                int nextX = extraAntData.path.get(extraAntData.nextCellIndex).x;
                int nextY = extraAntData.path.get(extraAntData.nextCellIndex).y;
                antAction = chooseDirection(antData.gridX, antData.gridY, nextX, nextY);
                if (!positionTaken(nextX, nextY))
                {
                    extraAntData.nextCellIndex++;
                }
                return antAction;
            }
            else
            {
                //food is reached, find an A* path to go home
                ClientCell antCell = ClientRandomWalk.world[antData.gridX][antData.gridY];
                ClientCell nestCell = ClientRandomWalk.world[centerX-Constants.NEST_RADIUS][centerY];
                aStarObject.setBeginAndEnd(antCell, nestCell);
                extraAntData.setPath(aStarObject.findPath());
                extraAntData.path.pollFirst();
                extraAntData.path.pollLast();
                extraAntData.nextCellIndex = 0; //reset to 0 when going home
                extraAntData.action = ExtraAntData.CurrentAction.GOING_HOME; //just go pass through the if statement, expected to pick up food
            }
        }
        else if (extraAntData.action == ExtraAntData.CurrentAction.GOING_HOME)
        {
            //TODO: Start working here, stuck on water when trying to get home
            int nextCellIndex = extraAntData.nextCellIndex;
            if (extraAntData.nextCellIndex < extraAntData.path.size())
            {
                int nextX = extraAntData.path.get(nextCellIndex).x;
                int nextY = extraAntData.path.get(nextCellIndex).y;
                antAction.type = AntAction.AntActionType.MOVE;
                antAction = chooseDirection(antData.gridX, antData.gridY, nextX, nextY);
                if (!positionTaken(nextX, nextY))
                {
                    extraAntData.nextCellIndex++;
                }
                return antAction;
            }
            else
            {
                //initialize back to the food cell if the food is dropped
                if (antData.carryUnits == 0)
                {
                    aStarObject.setBeginAndEnd(ClientRandomWalk.world[antData.gridX][antData.gridY], extraAntData.targetfoodCell);
                    extraAntData.setPath(aStarObject.findPath());
                    extraAntData.path.pollFirst();
                    extraAntData.path.pollLast();
                    extraAntData.nextCellIndex = 0;
                    extraAntData.action = ExtraAntData.CurrentAction.FOLLOWING_FOOD;
                }
            }
        }



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



}
