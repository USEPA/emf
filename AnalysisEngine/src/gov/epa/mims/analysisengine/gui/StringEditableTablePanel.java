package gov.epa.mims.analysisengine.gui;

import java.awt.event.*;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import javax.swing.*;
import javax.swing.table.*;


/**
 * An EditableTablePanel that only accepts Strings for editing.
 *
 * @author Daniel Gatti, CEP, UNC
 * @see gov.epa.mims.analysisengine.gui.EditableTablePanel.java
 * @version $Id: StringEditableTablePanel.java,v 1.3 2007/05/31 14:29:33 qunhe Exp $
 */
public class StringEditableTablePanel extends EditableTablePanel
{
	static final long serialVersionUID = 1;
   
   /** The list of valid choices for the user to choose from. This will be null if the user
    * can enter any value. */
   protected String[] validChoices = null;
   
   /** The default value that will be place in newly added rows. */
   protected String defaultValue = EditableTablePanel.DOUBLE_CLICK_TO_ENTER;
   
   
   /**
    * Constructor.
    *
    * @author Daniel Gatti
    * @param columnName
    */
   public StringEditableTablePanel(String columnName)
   {
      super(columnName);
      tableModel.setColumnClass(String.class);
      insertRow(true);
   }
   
   /**
    * Return the values in this table as an array of Strings.
    *
    * @author Daniel Gatti
    * @returns String[] that is an array of the values in this table.
    */
   public Object[] getValue()
   {
      List list = new ArrayList();
      for (int r = tableModel.getRowCount() - 1; r >= 0; --r)
      {
         String value = (String)tableModel.getValueAt(r, 1);
         if(!value.equalsIgnoreCase(DOUBLE_CLICK_TO_SELECT) &&
            !value.equalsIgnoreCase(DOUBLE_CLICK_TO_ENTER))
         {
            list.add(value);
         }
      }
      return list.toArray();
   }
   
   /**
    * Insert a row in the table with a default value.
    *
    * @author Daniel Gatti
    * @see gov.epa.mims.analysisengine.gui.EditableTablePanel#insertRow(boolean)
    */
   protected void insertRow(boolean above)
   {
      insertRowInternal(above, defaultValue);
   }
   
   
   /**
    * Set a default value that will be used when a new row is added.
    *
    * @author Daniel Gatti
    * @param value Object that is the default value to place in a new row.
    */
   
   public void setDefaultValue(Object value)
   {
      if (defaultValue instanceof String)
         defaultValue = (String)value;
      else
         DefaultUserInteractor.get().notify(this, "Unexpected object type",
         "Expected an String value in StringEditableTablePanel.setDefaultValue()",
         UserInteractor.ERROR);
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
            
            public final int compare( Object a, Object b)
            {
               return -(((String)a).compareTo((String)b));
            }
         });
      }
      setValue(values);
   }//sort()
} // class StringEditableTablePanel
