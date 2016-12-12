package antworld.common;

import java.util.Random;

public class Constants
{
  public static final long VERSION = 20161122L;
  public static final int NEST_RADIUS = 20;
  
  public static final int INITIAL_ANT_SPAWN_COUNT = 100;
  public static final int INITIAL_NEST_WATER_UNITS = 100;
  public static final int TIME_STEP_MSEC = 100;
  
  public static final int UNKNOWN_ANT_ID = -1;//use by client for ants being birthed
  
  public static Random random = new Random();
  
  public static final int PORT = 12321;
}

