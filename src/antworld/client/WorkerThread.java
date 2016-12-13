package antworld.client;

import antworld.common.CommData;

/**
 * Created by Arthur on 12/5/2016.
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
