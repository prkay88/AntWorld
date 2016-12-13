package antworld.client;

import antworld.common.AntAction.AntActionType;
import antworld.common.Constants;
import antworld.common.Direction;

import java.util.LinkedList;


/**
 * Created by Arthur on 11/30/2016.
 *
 * Contains information that is used to make deeper decisions
 * for an individual ant.
 */
public class ExtraAntData
{
  /**
   * This describes the current action of the ant.
   */
  enum CurrentAction
  {
    ROAMING,          //doing nothing
    FOLLOWING_FOOD,   //going to a food that is found
    GOING_HOME
  }
  
  Direction mainDirection = null;
  int ticksTillUpdate;
  int targetAntId = -2;
  int targetFoodX;
  int targetFoodY;
  int timeStuck = 0;
  
  /**
   * Creates a new ExtraAntData object with an initial direction.
   * @param mainDirection - the main direction
   */
  public ExtraAntData(Direction mainDirection)
  {
    this.mainDirection = mainDirection;
  }
  
  /**
   * Updates the direction of the ant.
   */
  public void updateRoamingDirection()
  {
    mainDirection = Direction.getRandomDir();
    ticksTillUpdate = Constants.random.nextInt(1500)+1000;
  }
}
