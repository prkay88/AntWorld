package antworld.client;

import java.util.LinkedList;

/**
 * Created by Arthur on 11/30/2016.
 */
public class AntStatus
{
  enum StatusType
  {
    IN_ASSEMBLY,       //already in assembly line
    GOING_TO_ASSEMBLY, //going to assembly
  }
  
  StatusType type = null; //normal, not involved in assembly lines
  LinkedList<ClientCell> path = new LinkedList<>();
  
}
