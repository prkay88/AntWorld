package antworld.client;

/**
 * Created by Phillip on 12/8/2016.
 */
public class ReadyThreadCounter
{
  public volatile int numThreadsReady =0;
  public ReadyThreadCounter()
  {}
  public void incrementNumThreadsReady()
  {
      synchronized (this)
      {
          numThreadsReady++;
      }
  }
}
