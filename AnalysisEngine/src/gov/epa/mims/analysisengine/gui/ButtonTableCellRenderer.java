package gov.epa.mims.analysisengine.gui;

import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
* ButtonTableCellEditor class
* Brings up a custom dialog when the button is pushed
*/
public class ButtonTableCellRenderer implements TableCellRenderer
{
   private JButton button;
   private Object objectToEdit;
   String labelText;

   public ButtonTableCellRenderer(String text)
   {
      super();
      labelText = text;
   }

   public java.awt.Component getTableCellRendererComponent(
      JTable table, Object value, boolean isSelected, boolean hasFocus,
      int row, int column)
   {
      if (button == null)
      {
         button = new JButton(labelText);
      }
      return button;

   }
}

