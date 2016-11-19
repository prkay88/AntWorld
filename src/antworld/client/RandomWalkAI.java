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
//        Direction dir = Direction.EAST; //go east
        Direction dir = Direction.NORTH; //go north
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
            antAction.type = AntAction.AntActionType.EXIT_NEST;
            //when food is EAST of the nest
//            antAction.x = centerX + 9;
//            antAction.y = centerY;
            antAction.x = centerX;
            antAction.y = centerY - 9;
            return true;
        }
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

        if(foodX == antX+1 && foodY == antY)
        {
            antAction.direction = Direction.EAST;
            antAction.quantity = 2;
            antAction.type = AntAction.AntActionType.PICKUP;
            return true;
        }
        else if(foodX == antX && foodY == antY-1)
        {
            System.out.println("In pickUpFood(): pick up food north.");
            antAction.direction = Direction.NORTH;
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
            int xDiff = antData.gridX - centerX;
            int yDiff = antData.gridY - centerY;
            System.out.println("in goHomeIfCarryingOrHurt: yDiff="+yDiff);

            if(xDiff >= 1 && yDiff == 0)
            {
                antAction.direction = Direction.WEST;
            }
            else if(xDiff == 0 && yDiff <= -1)
            {
                System.out.println("in goHomeIfCarryingOrHurt: go south");
                antAction.direction = Direction.SOUTH;
            }
            antAction.type = AntAction.AntActionType.MOVE;
            int diffFromCenterX = Math.abs(centerX-antData.gridX);
            int diffFromCenterY = Math.abs(centerY-antData.gridY);
            if(diffFromCenterX <= Constants.NEST_RADIUS && diffFromCenterY <= Constants.NEST_RADIUS)
            {
//                antAction.direction = Direction.WEST; //drop when food is in EAST
                antAction.direction = Direction.SOUTH; //drop when food is in NORTH
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
