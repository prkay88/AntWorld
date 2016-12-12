package antworld.server;

import antworld.common.Constants;

import java.io.Serializable;
import java.util.ArrayList;

public class WorldData implements Serializable
{
  private static final long serialVersionUID = Constants.VERSION;

  public ArrayList<Nest> nestList = new ArrayList<Nest>();
  public ArrayList<FoodSpawnSite> foodSpawnList;
  public int gameTick = 0;
}
