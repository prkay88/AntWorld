package antworld.client;

import com.sun.deploy.util.SessionState;

import java.util.LinkedList;

/**
 * FoodStatus is needed to store food coordinates and the path from the nest to the food.
 * Will be used to form the assembly line.
 *
 * This is needed because CommData's information about food locations gets lost when
 * it's not on one of the ants' visions. We want to store this information in some
 * kind of food bank in the AI class.
 */
public class FoodStatus
{
  ClientCell foodCell;
  ClientCell currentPathHead;
  int currentPathHeadIndex;
  LinkedList<ClientCell> pathFromNestToFood;
  int pathLength;
  //maybe add a field that says whether the food's assembly line is in full saturation or not.
  
  public FoodStatus(ClientCell foodCell,
                    LinkedList<ClientCell> pathFromNestToFood)
  {
    this.foodCell = foodCell;
    this.pathFromNestToFood = pathFromNestToFood;
    pathLength = pathFromNestToFood.size();
    //pathLength-2 is the INDEX of the cell that is one step before the food (end of the path).
    currentPathHeadIndex = pathLength - 2;
    currentPathHead = pathFromNestToFood.get(pathLength-2);
    //    currentPathHead = pathFromNestToFood.get();
    
  }
  //call this for when ants are trying to go to the assembly line
  public void antSecureSpotInAssemblyLine()
  {
    //doesn't get subtracted because in randomWalkAi goToFood(), the object gets recreated.
//    System.out.print("ant secured a spot in assembly line, before subtraction: currenPathHeadIndex is="+currentPathHeadIndex+", ");
    currentPathHeadIndex-=2;
//    System.out.println("after subtraction currentPathHeadIndex="+currentPathHeadIndex);
    currentPathHead = pathFromNestToFood.get(currentPathHeadIndex);
  }
}
