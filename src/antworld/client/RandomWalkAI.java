package antworld.client;

import antworld.common.*;

import java.util.Random;

/**
 * Created by Phillip on 11/18/2016.
 */
public class RandomWalkAI extends AI {


    public RandomWalkAI(CommData data, AntData antData)
    {
        /*this.commData = data;
        centerX = commData.nestData[commData.myNest.ordinal()].centerX;
        centerY = commData.nestData[commData.myNest.ordinal()].centerY;
        ant.type = AntAction.AntActionType.STASIS;
        this.antData = antData;*/
        super(data, antData);
    }


    @Override
    public boolean goExplore()
    {
        //make them move North East all the time
//    Direction dir = Direction.getRandomDir();
        System.out.println("Inside RWAI goExplore");
        Direction dir = Direction.NORTHEAST;
        ant.type = AntAction.AntActionType.MOVE;
        ant.direction = dir;
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
            ant.type = AntAction.AntActionType.EXIT_NEST;
            //action.x = centerX+9;
            //action.y = centerY+9;

      ant.x = centerX - (Constants.NEST_RADIUS-1) + random.nextInt(2 * (Constants.NEST_RADIUS-1));
      ant.y = centerY - (Constants.NEST_RADIUS-1) + random.nextInt(2 * (Constants.NEST_RADIUS-1));
            return true;
        }
//    System.out.println("Exiting:");
        return false;
    }

}
