package antworld.client;

import antworld.common.LandType;

/**
 * Created by Arthur on 11/24/2016.
 */
public class ClientCell
{
  LandType landType;
  int height = 0;
  ClientCell(LandType landType, int height)
  {
    this.landType = landType;
    this.height = height;
  }
}
