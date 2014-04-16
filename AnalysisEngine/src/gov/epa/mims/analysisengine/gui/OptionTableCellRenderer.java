package gov.epa.mims.analysisengine.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

/**
* ButtonTableCellEditor class
* Brings up a custom dialog when the button is pushed
*/
public class OptionTableCellRenderer implements TableCellRenderer
{
   private JButton button;
   private Object objectToEdit;

   /** The value of the renderer.  This is used to toggle the name of the button */
   private Object value;

   public OptionTableCellRenderer(Object value)
   {
      super();
      this.value = value;
   }

   public java.awt.Component getTableCellRendererComponent(
      JTable table, Object value, boolean isSelected, boolean hasFocus,
      int row, int column)
   {
      if (button == null)  /* initialize the button */
      {
         if (value != null)
         {
            button = new JButton("Edit");
         }
         else
         {
            button = new JButton("Add");
         }
      }
      /*else
      {
         setValue(value);
      }*/
      return button;

   }

   protected void setValue(Object newValue)
   {
      value = newValue;
      if (value == null)
      {
         button.setText("Add");
      }
      else
      {
         button.setText("Edit");
      }
   }

   protected void setText(String text)
   {
      button.setText(text);
   }
}


