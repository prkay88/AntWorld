package antworld.client;

import antworld.common.CommData;

/**
 * Extra thread for reading the map to be used.
 */
public class WorkerThread extends Thread
{
  ClientRandomWalk clientRandomWalk;
  
  public WorkerThread(ClientRandomWalk client)
  {
    clientRandomWalk = client;
  }
  
  @Override
  public void run()
  {
    clientRandomWalk.createMap();
    clientRandomWalk.mapIsRead = true;
  }
}
