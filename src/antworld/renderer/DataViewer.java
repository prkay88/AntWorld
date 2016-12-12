package antworld.renderer;


import antworld.common.FoodType;
import antworld.common.TeamNameEnum;
import antworld.server.AntWorld;
import antworld.server.Nest;
import antworld.server.Nest.NetworkStatus;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;

//import java.awt.Font;

@SuppressWarnings("serial")
public class DataViewer extends JFrame
{
  
  //private Font myFont = new Font ("SansSerif", Font.PLAIN , 16);
  public static JTable table_nestList, table_FoodList;
  
  public DataViewer(ArrayList<Nest> nestList)
  {
    this.setTitle(AntWorld.title);

    this.setBounds(0, 0, 768, 500);
    this.setVisible(true);
    this.setResizable(true);
    this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
    Container contentPane = this.getContentPane();
    // contentPane.setLayout(null);
    JTabbedPane tabbedPane = new JTabbedPane();
    contentPane.add(tabbedPane);

    JPanel panel_NestList = new JPanel();
    
    tabbedPane.addTab("Nest List", panel_NestList);
    int outsideWidth = tabbedPane.getWidth();
    int outsideHeight = tabbedPane.getHeight();
    Insets insets = tabbedPane.getInsets();
    int panelWidth = outsideWidth - insets.left - insets.right;
    int panelHeight = outsideHeight - insets.top - insets.bottom;
    
    
    String[] columnNames =  new String[7+ FoodType.SIZE];
    columnNames[0] = "Nest";
    columnNames[1] = "Team";
    columnNames[2] = "Status";
    columnNames[3] = "Center X";
    columnNames[4] = "Center Y";
    columnNames[5] = "Ant Count";
    columnNames[6] = "Score";
    int i = 7;
    for (FoodType type : FoodType.values())
    { columnNames[i] = type.name();
      i++;
    }
   
    DefaultTableModel model_table_nestList = new DefaultTableModel(null,columnNames);  
    
    
    table_nestList = new JTable(model_table_nestList);

 
    table_nestList.setPreferredScrollableViewportSize(new Dimension(panelWidth, panelHeight));
    
    JScrollPane scrollPane = new JScrollPane(table_nestList);
    
    table_nestList.setFillsViewportHeight(true);
    panel_NestList.setLayout(new BorderLayout());
    panel_NestList.add(scrollPane);
    
   
    model_table_nestList.setRowCount(nestList.size());
    table_nestList.selectAll();
    //tabbedPane.setSelectedIndex(0);
    

    
    
    //////////////=============== Ant List Table ============================
    
    JPanel panel_Details = new JPanel();
    tabbedPane.addTab("Ant List", panel_Details);
    
//    String[] columnAntList =  new String[5+FoodType.SIZE-1];
//    columnAntList[0] = "Nest";
//    columnAntList[1] = "Team";
//    columnAntList[2] = "Center X";
//    columnAntList[3] = "Center Y";
//    columnAntList[4] = "Ant Count";
//    int i = 5;
//    for (FoodType type : FoodType.values())
//    { if (type != FoodType.UNKNOWN)
//      { columnNames[i] = type.name();
//        i++;
//      }
//    }
//   
//    DefaultTableModel model_table_nestList = new DefaultTableModel(null,columnNames);  
//    
//    
//    table_nestList = new JTable(model_table_nestList);
//
// 
//    table_nestList.setPreferredScrollableViewportSize(new Dimension(panelWidth, panelHeight));
//    
//    JScrollPane scrollPane = new JScrollPane(table_nestList);
//    
//    table_nestList.setFillsViewportHeight(true);
//    panel_NestList.setLayout(new BorderLayout());
//    panel_NestList.add(scrollPane);
//    
//
//    JPanel panel_Details = new JPanel();
//    tabbedPane.addTab("Ant List", panel_Details);
//    
//   
//    model_table_nestList.setRowCount(nestList.size());
//    table_nestList.selectAll();
    
    //////////////=============== Food In World ============================
    
    JPanel panel_Food = new JPanel();
    tabbedPane.addTab("Food List", panel_Food);
  
  }

  public void update(ArrayList<Nest> nestList)
  {

    int avgBrainlessScore = 0;
    int brainlessCount = 0;
    for (int row = 0; row < nestList.size(); row++)
    {
      Nest nest = nestList.get(row);

      table_nestList.setValueAt(nest.nestName, row, 0);
      table_nestList.setValueAt(nest.team, row, 1);

      String status = "OK";
      if (nest.getNetworkStatus() == NetworkStatus.DISCONNECTED) status = "???";
      else if (nest.getNetworkStatus() == NetworkStatus.UNDERGROUND) status = "---";

      int score = nest.calculateScore();
      table_nestList.setValueAt(status, row, 2);
      table_nestList.setValueAt(nest.centerX, row, 3);
      table_nestList.setValueAt(nest.centerY, row, 4);
      table_nestList.setValueAt(nest.getAntList().size(), row, 5);
      table_nestList.setValueAt(score, row, 6);

      int i = 7;
      for (FoodType type : FoodType.values())
      {
          table_nestList.setValueAt(nest.getFoodStockPile(type), row, i);
          i++;
      }

      // for (int x=0; x<columnNames.length; x++)
      // {
      // table_nestList.setValueAt(nestData[x][y], y, x);
      // table_nestList.getCellRenderer(y,
      // x).getTableCellRendererComponent(table_nestList, nestData[x][y], true,
      // true, y, x).setFont(myFont);
      // }

      if (nest.team == TeamNameEnum.NEARLY_BRAINLESS_BOTS)
      {
        avgBrainlessScore += score;
        brainlessCount++;
      }

    }
    avgBrainlessScore =  avgBrainlessScore/brainlessCount;
    this.setTitle(AntWorld.title + " Avg Brainless Score = " + avgBrainlessScore);
  }

}
