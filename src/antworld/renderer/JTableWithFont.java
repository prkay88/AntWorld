package antworld.renderer;



public class JTableWithFont
{

}
//public class MyTable extends JTable {
//
//  private static final Color TOTALS_ROW_FOREGROUND = Color.BLUE;
//  
//  private Font customFont;
//  private int customRowHeight;
//
//  public MyTable(int customFontSize) {
//    // Create font based on desired font size.
//    customFont = getFont().deriveFont((float) customFontSize);
//
//    // Get the actual height of the custom font.
//    FontMetrics metrics = getFontMetrics(customFont);
//    customRowHeight = metrics.getHeight();
//
//    // Set table row height to match font height.
//    setRowHeight(customRowHeight);
//  }
//
//  /**
//   * Override <tt>getRowHeight()</tt> so that look-and-feels which do not use
//   * the default table cell renderer (for example Substance) use the desired
//   * row height.
//   */
//  @Override
//  public int getRowHeight() {
//    if (customRowHeight > 0) {
//      return customRowHeight;
//    }
//    return super.getRowHeight();
//  }
//
//  /**
//   * Override <tt>prepareRenderer()</tt> to set the font size in the instances
//   * of the cell renderers returned by the look-and-feel's renderer.
//   * <p>
//   * Extending <tt>DefaultTableCellRenderer</tt>, setting the font in method
//   * <tt>getTableCellRendererComponent()</tt>, and setting the columns to use
//   * the custom renderer does work with the built-in look-and-feels. It also
//   * works sufficiently with other look-and-feels, but some of these normally
//   * supply their own table cell renderers, and setting a custom renderer
//   * means that some of the functionality of the look-and-feel's renderer is
//   * lost. For example, with Substance, one loses the different backgrounds
//   * for alternate rows.
//   * <p>
//   * By overriding the table's prepareRenderer() method instead, the
//   * functionality of the look-and-feel's renderer is retained.
//   */
//  public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
//    Component c = super.prepareRenderer(renderer, row, column);
//    if (customFont != null) {
//      c.setFont(customFont);
//      fontSet = true;
//      if (row == getRowCount() - 1) {
//        // Set different foreground color for Totals row.
//        c.setForeground(TOTALS_ROW_FOREGROUND);
//      }
//      else {
//        // Reset foreground color so that default is used.
//        c.setForeground(null);
//      }
//    }
//    return c;
//  }