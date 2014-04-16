package gov.epa.mims.analysisengine.gui;

import gov.epa.mims.analysisengine.gui.*;
import gov.epa.mims.analysisengine.tree.*;
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
public class OptionTableCellEditor extends JButton implements TableCellEditor
{
   protected Vector listeners;
   protected OptionsTable optionsTable;
   protected int row, column;
   /** dialog to show when edit button is pressed */

   private Class customDialogClass;
   private AnalysisOption objectToEdit;
   private OptionDialog customDialog = null;
   private OptionInfo optionInfo;

   /** whether the option currently exists in the tree */
   private boolean isInTree = false;

   public OptionTableCellEditor()
   {
      super("Add");
      initialize();
   }

   public OptionTableCellEditor(Class customDialogClass)
   {
      super("Add");
      this.customDialogClass = customDialogClass;
      initialize();
   }

   /**
    * Initialize this GUI from the given arguemnts.
    * @param opInfo OptionInfo
    * @param value AnalysisOption
    * @param plotTypeName String that is the the plot type name from
    *    AnalysisEngineConstants.
    * @throws Exception
    */
   public void initFromOptionInfo(OptionInfo opInfo, AnalysisOption value,
                                  String plotTypeName)
       throws Exception
   {
      this.optionInfo = opInfo;
      if (value == null)  /* the item isn't currently in the tree */
      {
         objectToEdit = opInfo.getDefaultValue();
         isInTree = false;
         setText("Add");
      }
      else  /* it is in the tree */
      {
         objectToEdit = value;
         isInTree = true;
         setText("Edit");
      }
      Class dialogClass = (Class)opInfo.getValueEditorType();
      customDialog = (OptionDialog)dialogClass.newInstance();
      customDialog.setDataSource(objectToEdit, opInfo.getName());
      customDialog.setPlotTypeName(plotTypeName);
   }

   /**
    * reset to use a default version of the option
    * @return Object the newly created object
    */
   protected Object resetOption(OptionInfo opInfo) throws Exception
   {
      isInTree = false;
      objectToEdit = opInfo.getDefaultValue();
      customDialog.setDataSource(objectToEdit, opInfo.getName());
      setText("Add");
      return objectToEdit;
   }

   /**
    * Override this to specify the info shown in the table
    * @return String info to show in the options table
    */
   public String toString()
   {
      if (customDialog != null)
         return ((OptionDialog)customDialog).getInfoString();
      else
         return "custom dialog for "+customDialogClass.getName();
   }

   private void initialize()
   {
     addActionListener(new ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent A) {
           edit();
        }});
     listeners = new Vector();

     // add focus listener to stop editing when focus is lost
     this.addFocusListener( new FocusListener(){
        public void focusGained( FocusEvent evt ){}
        public void focusLost( FocusEvent evt ){ fireEditingStopped(); }
        });
   }

  /* public void setObjectToEdit(Object objectToEdit)
   {
      this.objectToEdit = objectToEdit;
   }

   public void setCustomDialog(OptionDialog customDialog)
   {
      this.customDialog = customDialog;
   }*/

   public Component getTableCellEditorComponent(JTable table, Object value,
                                                boolean isSelected,
                                                int row, int column)
   {
      this.row = row;
      this.column = column;

      //originalValue = (Value) this.table.getParameter(row);
      //newValue = originalValue;
      optionsTable = (OptionsTable)table;
      return this;
   }

   // CellEditor methods
   public void cancelCellEditing(){fireEditingCanceled();}
   public void acceptCellEditing(){fireEditingStopped();}
   public Object getCellEditorValue()
   {
      return objectToEdit;
   }
   public boolean isCellEditable(EventObject eo){return true;}
   public boolean shouldSelectCell(EventObject eo){return true;}
   public boolean stopCellEditing(){fireEditingStopped(); return true;}
   public void addCellEditorListener(CellEditorListener cel){listeners.addElement(cel);}
   public void removeCellEditorListener(CellEditorListener cel){listeners.removeElement(cel);}


   protected void fireEditingCanceled()
   {
      ChangeEvent ce = new ChangeEvent(this);
      for(int i=listeners.size()-1; i>=0; i--)
      {
         ((CellEditorListener)listeners.elementAt(i)).editingCanceled(ce);
      }
   }

   protected void fireEditingStopped()
   {
      ChangeEvent ce = new ChangeEvent(this);
      for(int i=listeners.size()-1; i>=0; i--)
      {
         ((CellEditorListener)listeners.elementAt(i)).editingStopped(ce);
      }
   }

   void edit()
   {
      if (customDialog != null)
      {
         customDialog.setTable(optionsTable);
         customDialog.setVisible(true);
         int result = customDialog.getResult();
         if (result == OptionDialog.OK_RESULT)
         {
            setText("Edit");
            optionsTable.updateOption(row, column, optionInfo, objectToEdit);
            isInTree = true;
         }
      }
      else if (objectToEdit == null)
      {
         System.err.println("No object set for ButtonTableCellEditor for class "+
             customDialogClass.getName().toString());
      }
      else // create a new custom dialog for the object to edit
      {
      }
   }

}

