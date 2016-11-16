package antworld.server;

import antworld.common.AntData;
import antworld.common.FoodData;
import antworld.common.NestNameEnum;

public class GameObject
{
  public enum GameObjectType {ANT, FOOD};
  
  public GameObjectType type;
  
  public AntData ant = null;
  public FoodData food = null; 
  public FoodSpawnSite foodSpawnSite = null;
  
  public GameObject(AntData ant)
  {
    type =  GameObjectType.ANT;
    this.ant = ant;
  }
  
  public GameObject(FoodSpawnSite foodSpawnSite, FoodData food)
  {
    type =  GameObjectType.FOOD;
    this.foodSpawnSite = foodSpawnSite;
    this.food = food;
  }
  
  
  public void setFoodCount(AntWorld world, NestNameEnum nestName, int count)
  {
    if (food == null) return;
    if (count <= 0)
    { 
      count = 0;
      world.removeFood(food);
    }
    food.count = count;
    
    
    
    if (foodSpawnSite != null)
    { foodSpawnSite.nestGatheredFood(nestName, count);
    }
    
  }
  
  public int getFoodCount()
  {
    if (type !=  GameObjectType.FOOD) return 0;
    if (food == null) return 0;
    return food.getCount();
  }
}
