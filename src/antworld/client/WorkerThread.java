package antworld.client;

import antworld.common.CommData;

/**
 * Created by Arthur on 12/5/2016.
 * Processes the map in its own thread
 */
public class WorkerThread extends Thread
{
  ClientRandomWalk clientRandomWalk;
  
  /**
   * Constructor takes in a ClientRandomWalk and then reads its map
   * @param client
   */
  public WorkerThread(ClientRandomWalk client)
  {
    clientRandomWalk = client;
  }
  
  /**
   * reads the map and creates the world array for the ClientRandomWalk
   */
  @Override
  public void run()
  {
    clientRandomWalk.createMap();
    
    clientRandomWalk.mapIsRead = true;
  }
}
