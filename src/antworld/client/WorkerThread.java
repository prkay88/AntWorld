package antworld.client;

import antworld.common.CommData;

/**
 * Created by Arthur on 12/5/2016.
 */
public class WorkerThread extends Thread
{
  CommData commData;
  RandomWalkAI testAI;
  
  public WorkerThread(RandomWalkAI testAI)
  {
    this.testAI = testAI;
  }
  
  
  @Override
  public void run()
  {
      
  }
  
  public void setCommData(CommData commData)
  {
    this.commData = commData;
  }
  
}
