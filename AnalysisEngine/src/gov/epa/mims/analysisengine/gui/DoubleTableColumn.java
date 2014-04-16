/*
 * EditableTableColumn.java
 *
 * Created on November 26, 2003, 10:36 AM
 */

package gov.epa.mims.analysisengine.gui;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.datatransfer.*;
import javax.swing.*;
import javax.swing.table.*;
import gov.epa.mims.analysisengine.gui.DefaultUserInteractor;
import gov.epa.mims.analysisengine.gui.UserInteractor;


/**
 *
 * @author  parthee
 */
public class DoubleTableColumn extends SpecialTableColumn
{


   /** The upper bound for validating integer entries. */
   protected double upBound = 0.0;

   /** The lower bound for validating integer entries. */
   protected double lowBound = 0.0;

   /** True if we should check the bounds on the values. */
   protected boolean shouldCheckBounds = false;

   /** The list of valid choices for the user to choose from. This will be null if the user
    * can enter any value. */
   protected Double[] validChoices = null;//   {new Double(1.0)};

   /** The default value to place in newly added rows. */
   protected Double defaultValue = new Double(1.0d);


   //protected  DoubleSelection contents ;

   /** Creates a new instance of EditableTableColumn */
   public DoubleTableColumn(int modelIndex,String name)
   {
      super(modelIndex,name);
      dataFlavor = DoubleSelection.doubleFlavor;
      type = Double.class;
      setupRenderer();
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

      if (!(choices[0] instanceof Double))
         return;

      validChoices = new Double[choices.length];
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
      Double d = (Double)obj;
      contents = new DoubleSelection(d);
   }

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
   public void setDefaultValue(Object value) throws Exception
   {
      if (value == null)
      {
         defaultValue = null;
         return;
      }

      if (value instanceof Double)
      {
         defaultValue = (Double)value;
      }
      else
      {
         throw new Exception("Expected a Double value in"+
            " DoubleEditableTablePanel.setDefaultValue()");
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
            if( value == null)
            {
               txtField.setText("");
               return txtField;
            }

            if (value instanceof Double || value instanceof Integer)
            {
               Double d = ((Double)value);
               txtField.setText(d.toString());
               txtField.selectAll();
            }
            else
               DefaultUserInteractor.get().notify(table,"Unexpected object type",
               "Expected an Double value in DoubleEditableTablePanel.getTableCellEditorComponent()",
               UserInteractor.ERROR);

            return txtField;
         }

         // Override DefaultCellEditor's getCellEditorValue method
         // to return an Double, not a String:
         public Object getCellEditorValue()
         {
            double enteredValue=0;

            try
            {
               //to allow a blank cell when bounds are not specified
               //see ReferenceLineEditor.java
               if(txtField.getText().trim().length() == 0 && !shouldCheckBounds)
               {
                  return null;
               }
               enteredValue = Double.parseDouble(txtField.getText());
            }
            catch(Exception e)
            {
               DefaultUserInteractor.get().notify(txtField,"Error verifying input value",
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

                  DefaultUserInteractor.get().notify(txtField,"Error verifying input value",
                  message, UserInteractor.ERROR);
                  return originalObjectValue;
               }
            }

            return new Double(enteredValue);
         }
      };
      cellEditor = doubleEditor;
   } // setUpTextFieldEditor()



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
//      else
//      {
//         DefaultUserInteractor.get().notify("Null pointer Exception",
//         "The valid choices are not yet set.",
//         UserInteractor.WARNING);
//      }
      return this.defaultValue;
   }
   
    /**
    * Set up a Renderer that correctly displays floating point nubmers.
    */
   private void setupRenderer()
   {
      this.setCellRenderer(new DefaultTableCellRenderer()
         {
            JLabel label = new JLabel();
      
            public Component getTableCellRendererComponent(JTable table,
                  Object value, boolean isSelected, boolean hasFocus, int row,
                  int column)
            {
               Object obj = table.getValueAt(row, column);
               if(obj == null)
               {
                  label.setText("");
               }
               else if (obj instanceof Double)
               {
                  label.setText( ((Double)obj).toString());
               }
               else
               {
                  DefaultUserInteractor.get().notify(table,"Unexpected object type",
                     "Expected a Double object in DoubleTableColumn.setupRenderer()",
                     UserInteractor.ERROR);
               }
               return label;
            }
         }
      );
   }

}

