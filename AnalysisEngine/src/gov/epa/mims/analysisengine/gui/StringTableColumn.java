
package gov.epa.mims.analysisengine.gui;

import java.awt.Component;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import javax.swing.*;
import javax.swing.table.TableCellEditor;
/*
 * StringTableColumn.java
 *
 * Created on March 11, 2005, 10:31 AM
 * @author  parthee
 */

public class StringTableColumn extends SpecialTableColumn  implements ClipboardOwner, Transferable 
{
   
  /** The list of valid choices for the user to choose from. This will be null if the user
    * can enter any value. */

   protected String[] validChoices = null;

  /** The default value to place in newly added rows. */
   protected String defaultValue = "";
   
   
   /** Creates a new instance of StringTableColumn */
   public StringTableColumn(int modelIndex,String name)
   {
      super(modelIndex,name);
      dataFlavor = DataFlavor.stringFlavor;
      type = String.class;
   }


   /**
    * Create a combo box with the valid choices passed in by the user and tell the
    * table to use it for Strings
    * NOTE: I assume that you have checked that the validChoices array is not null.
    *@author Daniel Gatti
    */
    protected void setUpComboBoxEditor()

   {
      JComboBox comboBox = new JComboBox();
      for (int i = 0; i < validChoices.length; i++)
         comboBox.addItem(validChoices[i]);
      DefaultCellEditor editor = new DefaultCellEditor(comboBox);
      editor.setClickCountToStart(2);
      cellEditor = editor;
   }

   public TableCellEditor getCellEditor()
   {
      return this.cellEditor;
   }

   /**
    * Set a list of valid choices that will be the only ones that the user can enter into the table.
    *
    * @author Daniel Gatti
    * @param choices Object[] that is a list of valid choices to be displayed in a combo box
    *    when the user is editing data in the table.
    * @see gov.epa.mims.analysisengine.gui.EditableTablePanel#setValidChoices(java.lang.Object[])
    */
   public void setValidChoices(Object[] choices)
   {
      if (choices == null || choices.length == 0)
         return;
      if (!(choices[0] instanceof String))
         return;
      validChoices = new String[choices.length];
      System.arraycopy(choices, 0, validChoices, 0, choices.length);
      setUpComboBoxEditor();
   }

   /**
    * Copy the currently selected row.
    * Note: This works only if one row is selected.
    *
    * @author Daniel Gatti
    */
   protected void copySelectedCell(Object obj)
   {
      String str = (String)obj;
      contents = new StringSelection(str);
   }

   /**
    * Set a default value that will be used when a new row is added.
    *
    * @author Daniel Gatti
    * @param value Object that must be a Double and is the default value to place in a new row.
    */
   public void setDefaultValue(Object value)
   {
      if (value == null)
      {
         defaultValue = null;
         return;
      }
      if (value instanceof String)
      {
         defaultValue = (String)value;
      }
      else
      {
         throw new IllegalArgumentException("Expected a String value in"+
            " StringTableColumn.setDefaultValue()");
      }
   }

   /**
    * Create a validating text field to act as the editor and tell
    * the table to use it for Integers.
    *
    * @author Daniel Gatti
    */
   protected void setupCellEditor()
   {
      // Set up the cell editor.
      final JTextField txtField = new JTextField();
      DefaultCellEditor strEditor = new DefaultCellEditor(txtField)
      {
         // Save the original value.
         Object originalObjectValue = null;
        // Override getTableCellEditorComponent() to save the original value
         // in case the user enters an invalid value.
        public Component getTableCellEditorComponent(JTable table,
           Object value, boolean isSelected, int row, int column)
         {
            originalObjectValue = table.getValueAt(row, column);
            if( value == null)
            {
               txtField.setText("");
               return txtField;
            }
            if (value instanceof String)
            {
               String str = (String)value;
               txtField.setText(str);
               txtField.selectAll();
            }
            else
               DefaultUserInteractor.get().notify(table,"Unexpected object type",
               "Expected an String value in the table column "+ (column+1),
               UserInteractor.ERROR);
            return txtField;
         }
      };
      cellEditor = strEditor;
   }
    /** getter for the default value
    * @return Object defaultValue of type Double
    */
   public Object getDefaultValue()
   {
      return defaultValue;
   }

   /**
    * return the next object in the valid choices
    */
   public Object nextChoice()
   {
      if(validChoices != null)
      {
         if(count >= validChoices.length)
         {
            count = 0;
         }
         return validChoices[this.count++];
      }
      return this.defaultValue;
   }


   public void lostOwnership(Clipboard clipboard, Transferable contents)
   {
      ((StringSelection)contents).lostOwnership(clipboard, contents);
   }   

   public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException
   {      
      return ((StringSelection)contents).getTransferData(flavor);
   }   

   public DataFlavor[] getTransferDataFlavors()
   {
      return ((StringSelection)contents).getTransferDataFlavors();
   }
   
   public boolean isDataFlavorSupported(DataFlavor queryFlavor)
   {
      return ((StringSelection)contents).isDataFlavorSupported(queryFlavor);
   }
   
}

