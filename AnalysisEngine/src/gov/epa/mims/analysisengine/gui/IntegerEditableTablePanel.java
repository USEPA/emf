package gov.epa.mims.analysisengine.gui;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.datatransfer.*;
import java.util.Arrays;
import java.util.Comparator;
import javax.swing.*;
import javax.swing.table.*;

/**
 * An EditableTablePanel that only accepts Integers for editing.
 *
 * @author Daniel Gatti, CEP, UNC
 * @see gov.epa.mims.analysisengine.gui.EditableTablePanel.java
 * @version $Id: IntegerEditableTablePanel.java,v 1.3 2007/05/31 14:29:33 qunhe Exp $
 */
public class IntegerEditableTablePanel
   extends EditableTablePanel
{
	static final long serialVersionUID = 1;
	
   /** The upper bound for validating integer entries. */
   protected int upBound = 0;

   /** The lower bound for validating integer entries. */
   protected int lowBound = 0;

   /** The list of valid choices for the user to choose from. This will be null if the user
    * can enter any value. */
   protected Integer[] validChoices = null;

   /** The default value to use in newly added rows.*/
   protected Integer defaultValue = Integer.valueOf(1);

   /**
    * Consructor.
    *
    * @author Daniel Gatti
    * @param columnNames String[] that is the column names for thistable.
    */
   public IntegerEditableTablePanel(String columnName)
   {
      super(columnName);
      tableModel.setColumnClass(Integer.class);
      dataFlavor = IntegerSelection.integerFlavor;
      setUpTextFieldEditor();

   } // IntegerEditableTablePanel()


   /**
    * Copy the currently selected row.
    * Note: This works only if one row is selected.
    *
    * @author Daniel Gatti
    */
   protected void copySelectedRow()
   {
      Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
      Object obj = table.getValueAt(table.getSelectedRow(), 1);
      Integer i = (Integer)obj;
      IntegerSelection contents = new IntegerSelection(i);
      cb.setContents(contents, null);
   }


   /**
    * Return the values in this table as a primitive array of ints.
    * Return an int[] with 0 values if the table is empty.
    *
    * @author Daniel Gatti
    * @returns int[] that is the values in this table.
    */
   public int[] getValueAsPrimitive()
   {
      int[] retval = new int[tableModel.getRowCount()];
      Object obj = null;
      for (int r = tableModel.getRowCount() - 1; r >= 0; --r)
      {
         obj = tableModel.getValueAt(r, 1);
         if (obj instanceof Integer)
            retval[r] = ((Integer)obj).intValue();
         else
         {
            DefaultUserInteractor.get().notify(this, "Unexpected object type",
               "Expected an Integer value in IntegerEditableTablePanel.getValue()",
               UserInteractor.ERROR);
         }
      }

      return retval;
   }


   /**
    * Create an Integer and place it in the table.
    *
    * @author Daniel Gatti
    * @param above boolean that is true if the value should be added above the
    * currently selected row and false if below.
    * @see gov.epa.mims.analysisengine.gui.EditableTablePanel#insertRow(boolean)
    */
   protected void insertRow(boolean above)
   {
      insertRowInternal(above, defaultValue);
   }


   /**
    * Set the bounds on data entered into the table.
    * NOTE: This truncates the double value, it does not round.
    *
    * @author Daniel Gatti
    * @param lowerBound int that is the lower bound for the data.
    * @param upperBound int that is the upper bound for the data.
    */
   public void setBounds(double lowerBound, double upperBound)
   {
      // If the upper bound is less than the lower bound, then swap them.
      if (upperBound < lowerBound)
      {
         upBound = (int)lowerBound;
         lowBound = (int)upperBound;
      }
      else
      {
         upBound = (int)upperBound;
         lowBound = (int)lowerBound;
      }

      shouldCheckBounds = true;
   }


   /**
    * Set a default value that will be used when a new row is added.
    *
    * @author Daniel Gatti
    * @param value Object that must be an Integer and is the default value to place in a new row.
    */
   public void setDefaultValue(Object value)
   {
      if (defaultValue instanceof Integer)
         defaultValue = (Integer)value;
      else
      DefaultUserInteractor.get().notify(this,"Unexpected object type",
         "Expected an Integer value in IntegerEditableTablePanel.setDefaultValue()",
         UserInteractor.ERROR);
   }


   /**
    * Set a list of valid choices that will be the only ones that the user can enter into the table.
    *
    * @author Daniel Gatti
    * @param choices Object[] that is a list of valid choices to be displayed in a combo box
    * when the user is editing data in the table.
    */
   public void setValidChoices(Object[] choices)
   {
      if (choices == null || choices.length == 0)
         return;

      if (!(choices[0] instanceof Integer))
         return;

      validChoices = new Integer[choices.length];
      System.arraycopy(choices, 0, validChoices, 0, choices.length);

      setUpComboBoxEditor();
   }


   /**
    * Set the values in this table as an array of primitive ints.
    *
    * @author Daniel Gatti
    * @param newValues int[] with the new values.
    */
   public void setValue(int[] newValues)
   {
      if (newValues == null)
         return;

      Integer[] intObjs = new Integer[newValues.length];
      for (int i = newValues.length - 1; i >= 0; --i)
         intObjs[i] = Integer.valueOf(newValues[i]);
      setValue(intObjs);
   }


   /**
    * Create a combo box with the valid choices passed in by the user and tell the
    * table to use it for Integers.
    * NOTE: I assume that you have checked that the validChoices array is not null.
    *
    *@author Daniel Gatti
    */
   protected void setUpComboBoxEditor()
   {
      JComboBox comboBox = new JComboBox();
      for (int i = 0; i < validChoices.length; i++)
         comboBox.addItem(validChoices[i]);

      TableColumn tableColumn = table.getColumnModel().getColumn(1);
      DefaultCellEditor editor = new DefaultCellEditor(comboBox);
      editor.setClickCountToStart(2);
         tableColumn.setCellEditor(editor);
   }
   
   /** sort the table in an ascending or in an desending order
    * @param ascending true means sort in an ascending order and vice versa
    */
   public void sort(boolean ascending)
   {
      Object [] values = getValue();
      if(ascending)
      {
         Arrays.sort(values);
      }
      else
      {
         Arrays.sort(values,new Comparator()
         {
            public final int compare ( Object a, Object b) 
            {
               return -(((Integer)a).compareTo((Integer)b)); 
            }
         });
      }
      setValue(values);         
   }//sort()

   /**
    * Create a validating text field to act as the editor and tell the table to use it for Integers.
    *
    * @author Daniel Gatti
    */
   protected void setUpTextFieldEditor()
   {
       //      Set up the cell editor.
       final WholeNumberField integerField = new WholeNumberField(1, 10);

       DefaultCellEditor integerEditor = new DefaultCellEditor(integerField)
          {
             // Save the original value.
            Object originalObjectValue;

            // Override getTableCellEditorComponent() to save the original value
            // in case the user enters an invalid value.
            public Component getTableCellEditorComponent(JTable table,
                  Object value, boolean isSelected, int row, int column)
            {
               originalObjectValue = table.getValueAt(row, column);
               return super.getTableCellEditorComponent(table,  value,
                  isSelected, row, column);
            }

            // Override DefaultCellEditor's getCellEditorValue method
            // to return an Integer, not a String:
            public Object getCellEditorValue()
            {
               int enteredValue;
               try
               {
                  enteredValue = Integer.parseInt(integerField.getText());
               }
               catch(Exception e)
               {
                   DefaultUserInteractor.get().notify(IntegerEditableTablePanel.this,"Error verifying input value",
                     "Please enter an integer", UserInteractor.ERROR);
                     return originalObjectValue;
               }

               // Check bounds
               if (shouldCheckBounds)
               {
                  if (enteredValue < lowBound || enteredValue > upBound)
                  {
                     String message = null;
                      if (upBound == Integer.MAX_VALUE)
                          message = "Please enter an integer > " + lowBound;
                      else
                          message = "Please enter an integer between " + lowBound
                            + " and " + upBound;
                      DefaultUserInteractor.get().notify(IntegerEditableTablePanel.this,"Error verifying input value",
                            message, UserInteractor.ERROR);
                     return originalObjectValue;
                  }
               }

               return Integer.valueOf(enteredValue);
             }
          };

       table.setDefaultEditor(Integer.class, integerEditor);
   } // setUpTextFieldEditor()
   
} // class IntegerEditableTablePanel

