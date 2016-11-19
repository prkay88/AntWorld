package antworld.client;

import antworld.common.*;

/**
 * Created by Phillip on 11/18/2016.
 */
public class RandomWalkAI extends AI {


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
        AntAction antAction = new AntAction(AntAction.AntActionType.MOVE);
        if(startX > goalX && startY > goalY) antAction.direction = Direction.NORTHWEST;
        if(startX < goalX && startY > goalY) antAction.direction = Direction.NORTHEAST;
        if(startX > goalX && startY < goalY) antAction.direction = Direction.SOUTHWEST;
        if(startX < goalX && startY < goalY) antAction.direction = Direction.SOUTHEAST;
        if(startX == goalX && startY > goalY) antAction.direction = Direction.NORTH;
        if(startX == goalX && startY < goalY) antAction.direction = Direction.SOUTH;
        if(startX > goalX && startY == goalY) antAction.direction = Direction.EAST;
        if(startX < goalX && startY == goalY) antAction.direction = Direction.WEST;

        return antAction;

    }


    @Override
    public boolean goExplore()
    {
        //make them move North East all the time
//    Direction dir = Direction.getRandomDir();
        System.out.println("Inside RWAI goExplore");
        Direction dir = Direction.EAST;
        antAction.type = AntAction.AntActionType.MOVE;
        antAction.direction = dir;
        return true;
    }

    @Override
    public  boolean exitNest()
    {
        System.out.println("Inside RWAI exitNest");
        if (antData.underground)
        {
            //can only set action.x and action.y in coordinates within the nest
//      int dir = random.nextInt(2); //random between 0 and 1
//      if(dir == 0)
//      {
//        action.x = centerX+9;
//        action.y = centerY+9;
//      }
//      else if(dir == 1)
//      {
//        action.x = centerX-9;
//        action.y = centerY-9;
//      }
            antAction.type = AntAction.AntActionType.EXIT_NEST;
            //action.x = centerX+9;
            //action.y = centerY+9;

//      antAction.x = centerX - (Constants.NEST_RADIUS-1) + random.nextInt(2 * (Constants.NEST_RADIUS-1));
//      antAction.y = centerY - (Constants.NEST_RADIUS-1) + random.nextInt(2 * (Constants.NEST_RADIUS-1));
            antAction.x = centerX+ 9;
            antAction.y = centerY;
            return true;
        }
//    System.out.println("Exiting:");
        return false;
    }

    @Override
    public boolean pickUpFoodAdjacent()
    {
        if(commData.foodSet.size() == 0) return false;
        int antX = antData.gridX;
        int antY = antData.gridY;

        FoodData food = null;
        for(FoodData f : commData.foodSet)
        {
            food = f;
        }
        int foodX = food.gridX;
        int foodY = food.gridY;

        if(foodX == antX+1)
        {
            antAction.direction = Direction.EAST;
            antAction.quantity = 2;
            antAction.type = AntAction.AntActionType.PICKUP;
            return true;
        }
        return false;
    }

    @Override
    public boolean goHomeIfCarryingOrHurt()
    {
        if(antData.carryUnits > 0)
        {
            antAction.direction = Direction.WEST;
            antAction.type = AntAction.AntActionType.MOVE;
            int diffFromCenterX = Math.abs(centerX-antData.gridX);
            int diffFromCenterY = Math.abs(centerY-antData.gridY);
            if(diffFromCenterX <= Constants.NEST_RADIUS && diffFromCenterY <= Constants.NEST_RADIUS)
            {
                antAction.direction = Direction.WEST;
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
        int gotToX=0;
        int goToY=0;
        int closestFood = 1000000;
        if (!commData.foodSet.isEmpty() || commData.foodSet != null)
        {
            for(FoodData food : commData.foodSet)
            {
                int distance = Util.manhattanDistance(food.gridX, food.gridY, antData.gridX, antData.gridY);
                if(distance < closestFood)
                {
                    gotToX = food.gridX;
                    goToY = food.gridY;
                    closestFood = distance;
                }

            }
            if(gotToX != 0 && goToY != 0)
            {
                antAction = chooseDirection(antData.gridX, antData.gridY, gotToX, goToY);
                return true;
            }
        }
        return false;
    }

}
