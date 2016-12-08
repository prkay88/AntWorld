package antworld.client;

import antworld.common.AntAction;
import antworld.common.Constants;
import antworld.common.Direction;
import antworld.common.AntAction.AntActionType;
import com.sun.deploy.util.SessionState;

import java.util.LinkedList;


/**
 * Created by Arthur on 11/30/2016.
 */
public class ExtraAntData
{
  enum CurrentAction
  {
//    IN_ASSEMBLY,       //already in assembly line
//    GOING_TO_ASSEMBLY, //going to assembly
    ROAMING,          //doing nothing
    FOLLOWING_FOOD,   //going to a food that is found
    GOING_HOME
  }

//  StatusType type = null; //normal, not involved in assembly lines
  Direction mainDirection = null;
  LinkedList<ClientCell> path = new LinkedList<>();
  CurrentAction action = CurrentAction.ROAMING;
  int nextCellIndex; //initializes to 0
  ClientCell targetfoodCell;
  int ticksTillUpdate;
  AntActionType typeFromPreviousTurn;
  Direction directionFromPreviousTurn;
//  FoodStatus targetFood;
//  int indexInAssembly;

  public ExtraAntData(Direction mainDirection)
  {
    this.mainDirection = mainDirection;
  }
  
  public void setPath(LinkedList<ClientCell> path)
  {
    this.path = path;
  }
  
  public void updateRoamingDirection()
  {
    mainDirection = Direction.getRandomDir();
    ticksTillUpdate = Constants.random.nextInt(10);
  }
}
