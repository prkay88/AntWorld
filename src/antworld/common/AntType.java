package antworld.common;
/**
 *!!!!!!!!!! DO NOT MODIFY ANYTHING IN THIS CLASS !!!!!!!!!!<br>
 * This class is serialized across a network socket. Any modifications will
 * prevent the server from being able to read this class.<br><br>
 */

//new int[] {10,20,30,40,50,60,71,80,90,91};
public enum AntType
{
  ATTACK 
  {
    public FoodType[] getBirthFood() {return ATTACK_FOOD;}
    public int getMaxAttackDamage() {return super.getAttackDiceD4()*2;}
  },

  DEFENCE
  { public FoodType[] getBirthFood() {return DEFENCE_FOOD;}
    public int getMaxHealth() {return super.getMaxHealth()*2;}
  },

  MEDIC
  { public FoodType[] getBirthFood() {return MEDIC_FOOD;}
    public int getHealPointsPerWaterUnit() {return super.getHealPointsPerWaterUnit()*2;}
  },

  SPEED
  {
    public FoodType[] getBirthFood() {return SPEED_FOOD;}
    public int getBaseMovementTicksPerCell() {return super.getBaseMovementTicksPerCell()/2;}
  }, 
  
  VISION
  {
    public FoodType[] getBirthFood() {return VISION_FOOD;}
    public int getVisionRadius() {return super.getVisionRadius()*2;}
  },

  WORKER
  { public FoodType[] getBirthFood() {return WORKER_FOOD;}
    public int getCarryCapacity() {return super.getCarryCapacity()*2;}
  };

  
  
  //==========================================================================
  private static final FoodType[] ATTACK_FOOD = {FoodType.MEAT};
  private static final FoodType[] DEFENCE_FOOD = {FoodType.MEAT, FoodType.SEEDS};
  private static final FoodType[] MEDIC_FOOD = {FoodType.MEAT, FoodType.NECTAR};
  private static final FoodType[] SPEED_FOOD = {FoodType.NECTAR};
  private static final FoodType[] VISION_FOOD = {FoodType.NECTAR, FoodType.SEEDS};
  private static final FoodType[] WORKER_FOOD = {FoodType.SEEDS};




  public int getFoodUnitsToSpawn(FoodType type)
  {
    FoodType[] birthFood = getBirthFood();
    int units = 10/birthFood.length;
    for (FoodType food : birthFood)
    {
      if (type == food) return units;
    }
    return 0;
  }

  public abstract FoodType[] getBirthFood();
  public int getMaxHealth() {return 20;}
  public int getAttackDiceD4() {return 2;} //actual damage is the sum of n uniformly distributed 1-4.
  public double getAttritionDamageProbability() {return 0.001;}
  public int getBaseMovementTicksPerCell() {return 2;}
  public int getUpHillMultiplier() {return 5;}
  public int getHalfEncumbranceMultiplier() {return 2;}
  
  public int getVisionRadius() {return 30;}
  public int getCarryCapacity() {return 25;}
  public static int getDeadAntFoodUnits() {return 5;}
  public static FoodType getDeadAntFoodType() {return FoodType.MEAT;}
  public int getHealPointsPerWaterUnit() {return 1;}
}
