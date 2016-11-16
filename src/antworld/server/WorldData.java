package antworld.server;

import java.io.Serializable;
import java.util.ArrayList;
import antworld.common.Constants;

public class WorldData implements Serializable
{
  private static final long serialVersionUID = Constants.VERSION;

  public ArrayList<Nest> nestList = new ArrayList<Nest>();
  public ArrayList<FoodSpawnSite> foodSpawnList;
  public int gameTick = 0;
}
