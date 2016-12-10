package antworld.client;

import antworld.common.Util;
import java.util.*;

/**
 * Created by Phillip on 11/26/2016.
 */
public class AStar {
  int startX;
  int startY;
  int goalX;
  int goalY;
  ClientCell start;
  ClientCell goal;

  PriorityQueue<ClientCell> frontier = new PriorityQueue<>(1000, new Comparator<ClientCell>() {
    @Override
    public int compare(ClientCell o1, ClientCell o2) {
      return o1.getCost() - o2.getCost();
    }
  });

  public AStar(ClientCell start, ClientCell goal)
  {
    if (start != null && goal != null)
    {
      this.startX = start.x;
      this.startY = start.y;
      this.goalX = goal.x;
      this.goalY = goal.y;
      this.start = start;
      this.goal = goal;
    }
  }

  //we just call this before we call findPath() so that we don't need to recreate a new aStarObject()
  //to find A* paths.
  public void setBeginAndEnd(ClientCell start, ClientCell goal)
  {
    frontier.clear();
    this.startX = start.x;
    this.startY = start.y;
    this.goalX = goal.x;
    this.goalY = goal.y;
    this.start = start;
    this.goal = goal;
  }
  
  public LinkedList<ClientCell> findPath()
  {
    System.out.println("I'm in the find path method");
    LinkedList<ClientCell> path = new LinkedList<>();
    LinkedHashMap<ClientCell, ClientCell> cameFrom = new LinkedHashMap<>();
    HashMap<ClientCell, Integer> costSoFar = new HashMap<>();
    int newCost = 0;
    ClientCell previous = null;
    start.setCost(0);
    frontier.add(start);
    cameFrom.put(start, null);
    costSoFar.put(start, 0);

    while(!frontier.isEmpty())
    {
      //System.out.println("AStar frontier is not empty");
      ClientCell current = frontier.poll();
      if (current.equals(goal))
      {
        previous = current;
        break;
      }
      //if(current.neighbors == null || current.neighbors.isEmpty())
//      current.findNeighbors();
      synchronized (current.neighbors)
      {
        for (ClientCell clientCell : current.getNeighbors())
        {
//          synchronized (clientCell)
//          {
//        System.out.println("In AStar "+ "current's type="+current.landType+ ", its cost="+(costSoFar.get(current))
//        +" its coordinates="+" ("+current.x+", "+current.y+") "+"clientCell=("+clientCell.x+", "+clientCell.y+")"); //TODO: this is where the null pointer exception is
//        System.out.println("costSoFar contains key current="+current+"?"+costSoFar.containsKey(current));
            newCost = costSoFar.get(current) + current.height;
  
            if (!costSoFar.containsKey(clientCell) || newCost < costSoFar.get(clientCell))
            {
              costSoFar.put(clientCell, newCost);
              if (clientCell.equals(null)) System.out.println("Neighbor Cell is Null");
              int priority = newCost + Util.manhattanDistance(clientCell.x, clientCell.y, goalX, goalY);
              clientCell.setCost(priority);
              frontier.add(clientCell);
              cameFrom.put(clientCell, current);
            }
//          }
        }
      }
    }
    if(previous == null) System.out.println("NO POSSIBLE PATH");
    else
    {
      if(!cameFrom.isEmpty())
      {
        path =printPath(cameFrom, goal);
      }
    }
    return path;
  }
  
  public LinkedList<ClientCell> printPath(LinkedHashMap<ClientCell, ClientCell> cameFrom, ClientCell goal)
  {
    LinkedList<ClientCell> path = new LinkedList<>();
    ClientCell clientCell = goal;
    while(clientCell != null)
    {
      path.addFirst(clientCell);
      clientCell = cameFrom.get(clientCell);
    }
    System.out.println("I'M PRINTING THE PATH");
    for(ClientCell c : path)
    {
      System.out.println("x: " + c.x + " y: " + c.y);
    }
    return path;
  }



}
