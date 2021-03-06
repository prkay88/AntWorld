package antworld.client;

import antworld.common.AntAction;
import antworld.common.AntData;
import antworld.common.CommData;

import java.util.Random;

/**
 * Created by Phillip on 11/18/2016.
 * This will be the super class that houses all the decision making methods.
 * This class will be extended by particular AI behaviors.
 */
public class AI
{
  CommData commData;
  Random random = new Random();
  int centerX;
  int centerY;
  AntAction antAction;
  AntData antData;
  
  public AI()
  {}
  
  /**
   * Default AI constructor
   *
   * @param commData
   * @param antData
   */
  public AI(CommData commData, AntData antData)
  {
    antAction = new AntAction(AntAction.AntActionType.STASIS);
    
    this.commData = commData;
    if (this.commData == null) System.out.println("commData is null");
    if (this.commData.myNest == null) System.out.println("myNest is null");
    if (this.commData.nestData == null) System.out.println("nestData is null");
    antAction.type = AntAction.AntActionType.STASIS;
    this.antData = antData;
  }
  
  public void setAntData(AntData antData)
  {
    this.antData = antData;
  }
  
  public void setCommData(CommData commData)
  {
    this.commData = commData;
  }
  
  public void setCenterX(int centerX)
  {
    this.centerX = centerX;
  }
  
  public void setCenterY(int centerY)
  {
    this.centerY = centerY;
  }
  
  
  //////////////////////////////////////////////////////////
  //See comments on SwarmAI
  //////////////////////////////////////////////////////////
  public boolean underGroundAction()
  {
    return false;
  }
  
  public boolean attackAdjacent()
  {
    return false;
  }
  
  public boolean pickUpFoodAdjacent()
  {
    return false;
  }
  
  public boolean goHomeIfCarryingOrHurt()
  {
    return false;
  }
  
  public boolean pickUpWater()
  {
    return false;
  }
  
  public boolean goToEnemyAnt()
  {
    return false;
  }
  
  public boolean goToFood()
  {
    return false;
  }
  
  public boolean goToGoodAnt()
  {
    return false;
  }
  
  public boolean goExplore()
  {
    return false;
  }
  
  public boolean goToWater()
  {
    return false;
  }
  
  /**
   * Decision tree of the ant
   *
   * @return
   */
  public AntAction chooseAction()
  {
    antAction = new AntAction(AntAction.AntActionType.STASIS);
    
    if (antData.ticksUntilNextAction > 0) return this.antAction;
    
    if (underGroundAction()) return this.antAction; //always exit nest first
    
    if (goHomeIfCarryingOrHurt()) return this.antAction; //must come before goToFood()
    
    if (pickUpFoodAdjacent()) return this.antAction;
    
    if (goToFood()) return this.antAction;
    
    if (goExplore()) return this.antAction;
    
    if (attackAdjacent()) return this.antAction;
    
    if (pickUpWater()) return this.antAction;
    
    if (goToEnemyAnt()) return this.antAction;
    
    if (goToGoodAnt()) return this.antAction;
    
    return this.antAction;
  }
}
