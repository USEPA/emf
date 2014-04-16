package gov.epa.mims.analysisengine.gui;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.datatransfer.*;
import java.util.Arrays;
import java.util.Comparator;
import java.text.DecimalFormat;
import javax.swing.*;
import javax.swing.table.*;

/**
 * An EditableTablePanel that only accepts Doubles for editing.
 *
 * @author Daniel Gatti, CEP, UNC
 * @see gov.epa.mims.analysisengine.gui.EditableTablePanel.javas
 * @version $Id: DoubleEditableTablePanel.java,v 1.3 2007/05/31 14:29:33 qunhe Exp $
 */
public class DoubleEditableTablePanel extends EditableTablePanel
{
	static final long serialVersionUID = 1;
	
   /** The upper bound for validating integer entries. */
   protected double upBound = 0.0;

   /** The lower bound for validating integer entries. */
   protected double lowBound = 0.0;

   /** The list of valid choices for the user to choose from. This will be null if the user
    * can enter any value. */
   protected Double[] validChoices = null;

   /** The default value to place in newly added rows. */
   protected Double defaultValue = new Double(1.0d);

   /**
    * Consructor.
    *
    * @author Daniel Gatti
    * @param columnNames String[] that is the column names for thistable.
    */
   public DoubleEditableTablePanel(String columnName)
   {
      super(columnName);
      tableModel.setColumnClass(Double.class);
      dataFlavor = DoubleSelection.doubleFlavor;
      setUpTextFieldEditor();
      setUpTextFieldRenderer();
   } // DoubleEditableTablePanel()


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
      Double d = (Double)obj;
      DoubleSelection contents = new DoubleSelection(d);
      cb.setContents(contents, null);
   }


   /**
    * Return the values in this table as an array of doubles.
    * Return a double[] with 0 values in it if the table is empty.
    *
    * @author Daniel Gatti
    * @returns double[] that is an array of the values in this table.
    */
   public double[] getValueAsPrimitive()
   {
      double[] retval = new double[tableModel.getRowCount()];
      Object obj = null;
      for (int r = tableModel.getRowCount() - 1; r >= 0; --r)
      {
         obj = tableModel.getValueAt(r, 1);
         if (obj instanceof Double)
            retval[r] = ((Double)obj).doubleValue();
         else
         {
            DefaultUserInteractor.get().notify(this,"Unexpected object type",
               "Expected an Double value in DoubleEditableTablePanel.getValue()",
               UserInteractor.ERROR);
         }
      }

      return retval;
   }

   /**
    * Insert a Double into the table using a default value.
    *
    * @author Daniel Gatti
    * @see gov.epa.mims.analysisengine.gui.EditableTablePanel#insertRow(boolean)
    */
   protected void insertRow(boolean above)
   {
      insertRowInternal(above, defaultValue);
   }

   
   /** Insert a double array at the top/bottom of an existing table 
    *@param values double []
    *@param above boolean insert above/below
    */
   public void insertRow(double [] values)
   {
      for(int i=0 ; i < values.length; i++)
      {
         tableModel.insertRow(i,new Double(values[i]));
      }//for(i)
   }
   
   /** Insert a double array at the bottom of an existing table, but if a value in 
    * the array is already existing in the table then ignore that duplicate value
    * In comparing the duplicate values a tolerance will be calculated based on the 
    * input values min and max
    *
    *@param values double []
    *@param above boolean insert above/below
  */
   public void insertRowUniqueValues(double [] values, double tolerance)
   {
      boolean duplicate = false;
      for(int i=0 ; i < values.length; i++)
      {
         Double aValue = new Double(values[i]);
         int rowCount = table.getRowCount();
         for(int j=0; j< rowCount; j++)
         {
            Double aTableValue = (Double)table.getValueAt(j,1);
            double doubleTableValue = aTableValue.doubleValue();
            if(isEqual(aValue,aTableValue,tolerance))
            {
               duplicate = true;
               break;
            }//if
         }//for(i)
         if(!duplicate)
         {
            tableModel.insertRow(rowCount,aValue);
         }//if(duplicate)
         duplicate = false;
      }//for(i)
   }
   
   private boolean isEqual(Double one, Double two,double tolerance)
   {
      double diff = one.doubleValue() - two.doubleValue();
      if(Math.abs(diff) <= tolerance)
      {
         return true;
      }
      return false;
   }
   
   /** remove duplicate values
    */
   public void removeDuplicateValues(double tolerance)
   {
      int rowCount = tableModel.getRowCount();
      boolean [] duplicates  = new boolean[rowCount];
      //identify the rows which have duplicate values
      for(int i=0; i<rowCount-1; i++)
      {
         Double value1 = (Double)tableModel.getValueAt(i,1);
         for(int j=i+1; j < rowCount; j++)
         {
            Double value2 = (Double)tableModel.getValueAt(j,1);
            if(isEqual(value1, value2,tolerance))
            {
               duplicates[j] = true;
            }//if
         }//for(j)
      }//for(i)
      
      //removing the rows
      for(int i=0; i< rowCount; i++)
      {
         if(duplicates[i])
         {
            tableModel.removeRow(i);
         }
      }//for(i)
   }//removeDuplicateValues()
   

   /**check for duplicate values 
    *@throws Exception if duplicate value exist
    */
   public void checkDuplicateValues(double tolerance) throws Exception
   {
      int rowCount = tableModel.getRowCount();
      //identify the rows which have duplicate values
      for(int i=0; i<rowCount-1; i++)
      {
         Double value1 = (Double)tableModel.getValueAt(i,1);
         for(int j=i+1; j < rowCount; j++)
         {
            Double value2 = (Double)tableModel.getValueAt(j,1);
            if(isEqual(value1, value2, tolerance))
            {
               throw new Exception("The values at rows " + (i+1) + ", " + (j+1) + 
               " are same. \nPlease change the value in one of the rows");
            }//if
         }//for(j)
      }//for(i)
   }//checkDuplicateValues()
   
   /**
    * Set the bounds on data entered into the table.
    *
    * @author Daniel Gatti
    * @param lowerBound int that is the lower bound for the data.
    * @param upperBound int that is the upper bound for the data.
    * @see gov.epa.mims.analysisengine.gui.EditableTablePanel#setBounds(int, int)
    */
   public void setBounds(double lowerBound, double upperBound)
   {
      // If the upper bound is less than the lower bound, then swap them.
      if (upperBound < lowerBound)
      {
         upBound = lowerBound;
         lowBound = upperBound;
      }
      else
      {
         upBound = upperBound;
         lowBound = lowerBound;
      }

      shouldCheckBounds = true;
   }


   /**
    * Set a default value that will be used when a new row is added.
    *
    * @author Daniel Gatti
    * @param value Object that must be a Double and is the default value to place in a new row.
    */
   public void setDefaultValue(Object value)
   {
      if (defaultValue instanceof Double)
         defaultValue = (Double)value;
      else
      {
         DefaultUserInteractor.get().notify(this,"Unexpected object type",
         "Expected a Double value in DoubleEditableTablePanel.setDefaultValue()",
         UserInteractor.ERROR);
      }
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

      if (!(choices[0] instanceof Double))
         return;

      validChoices = new Double[choices.length];
      System.arraycopy(choices, 0, validChoices, 0, choices.length);

      setUpComboBoxEditor();
   }


   /**
    * Set the values in this table as an array of primitive doubles.
    *
    * @author Daniel Gatti
    * @param newValues double[] with the new values.
    */
   public void setValue(double[] newValues)
   {
      if (newValues == null)
         return;

      Double[] dblObjs = new Double[newValues.length];
      for (int i = newValues.length - 1; i >= 0; --i)
         dblObjs[i] = new Double(newValues[i]);
      setValue(dblObjs);
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


   /**
    * Create a validating text field to act as the editor and tell the table to use it for Integers.
    *
    * @author Daniel Gatti
    */
   protected void setUpTextFieldEditor()
   {
      // Set up the cell editor.
       final JTextField txtField = new JTextField();

       DefaultCellEditor doubleEditor = new DefaultCellEditor(txtField)
          {
            // Save the original value.
            Object originalObjectValue = null;

            // Override getTableCellEditorComponent() to save the original value
            // in case the user enters an invalid value.
            public Component getTableCellEditorComponent(JTable table,
                  Object value, boolean isSelected, int row, int column)
            {
               originalObjectValue = table.getValueAt(row, column);

               if (value instanceof Double)
               {
                  Double d = ((Double)value);
                  txtField.setText(d.toString());
                  txtField.selectAll();
               }
               else
                  DefaultUserInteractor.get().notify(DoubleEditableTablePanel.this,"Unexpected object type",
                     "Expected an Double value in DoubleEditableTablePanel.getTableCellEditorComponent()",
                     UserInteractor.ERROR);

               return txtField;
            }

             // Override DefaultCellEditor's getCellEditorValue method
             // to return an Double, not a String:
             public Object getCellEditorValue()
             {
               double enteredValue;

                try
               {
                  enteredValue = Double.parseDouble(txtField.getText());
               }
               catch(Exception e)
               {
                  DefaultUserInteractor.get().notify(DoubleEditableTablePanel.this,"Error verifying input value",
                     "Please enter a decimal number.", UserInteractor.ERROR);
                  return originalObjectValue;
               }

               if (shouldCheckBounds)
               {
                  if (enteredValue < lowBound || enteredValue > upBound)
                  {
                     String message = null;
                        if (upBound == Double.MAX_VALUE)
                        message = "Please enter a decimal > " + lowBound;
                        else
                         message = "Please enter a decimal between " + lowBound
                           + " and " + upBound;

                        DefaultUserInteractor.get().notify(DoubleEditableTablePanel.this,"Error verifying input value",
                           message, UserInteractor.ERROR);
                        return originalObjectValue;
                   }
               }

                  return new Double(enteredValue);
             }
          };

      table.setDefaultEditor(Double.class, doubleEditor);
      TableColumn tableColumn = table.getColumnModel().getColumn(1);
      doubleEditor.setClickCountToStart(2);
      tableColumn.setCellEditor(doubleEditor);
   } // setUpTextFieldEditor()


   /**
    * Set up a Renderer that correctly displays floating point nubmers.
    * @author Daniel Gatti
    */
   public void setUpTextFieldRenderer()
   {
      DefaultTableCellRenderer doubleRenderer = new DefaultTableCellRenderer();
      table.setDefaultRenderer(Double.class, doubleRenderer);
      TableColumn tableColumn = table.getColumnModel().getColumn(1);
      tableColumn.setCellRenderer(doubleRenderer);
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
               return -(((Double)a).compareTo((Double)b)); 
            }
         });
      }
      setValue(values);         
   }//sort()
   
   
//   use setCellRenderer(renderer) INSTEAD
//   /** Format the table with the specified pattern in the Decimal Format
//    *@param format DecimalFormat
//    */
//   public void setFormat(DecimalFormat format)
//   {
//      int rowCount = tableModel.getRowCount();
//      for(int i=0; i< rowCount; i++)
//      {
//         String aValue = format.format(table.getValueAt(i, 1));
//         table.setValueAt(Double.valueOf(aValue),i,1);
//      }
//   }

} // class DoubleEditableTablePanel

