package gov.epa.mims.analysisengine.table;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;

/**
 * A Render for the row of numbers on the left side of a table.
 * @author  Daniel Gatti
 * @version $Id: RowHeaderRenderer.java,v 1.2 2005/09/19 14:50:03 rhavaldar Exp $
 */
public class RowHeaderRenderer extends JLabel implements TableCellRenderer
{
   /** The color to use for the background when the cell is selected. */
   protected Color headerBackgroundColor = UIManager.getDefaults().getColor(
            "TableHeader.background");

   /**
    * Constructor.
    */
   public RowHeaderRenderer()
   {
      setOpaque(true);
      setFont(new Font(getFont().getName(), Font.BOLD, getFont().getSize()));
      setBorder(BorderFactory.createRaisedBevelBorder());
      setPreferredSize(new Dimension(20, 15));
      setHorizontalAlignment(JLabel.CENTER);
      setBackground(Color.lightGray);
   } // RowHeaderRenderer()


   /**
    * Return the renderer for the first column of the table.
    */
   public Component getTableCellRendererComponent(JTable table, Object value,
      boolean isSelected, boolean hasFocus, int row, int column)
   {
//System.out.println("Getting RowHeaderRenderer for column "+column);
      if (isSelected)
      {
         setBackground(headerBackgroundColor);
      }
      else
      {
         setBackground(Color.lightGray);
      }

      if (value != null)
      {
         setText(value.toString());
      }

      return this;
   } // getTableCellRendererComponent
} // class RowHeaderRenderer

