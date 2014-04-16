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
public class ButtonTableCellEditor extends JButton implements TableCellEditor
{
   protected Vector listeners;
   protected JTable table;
   protected int row, column;
   /** dialog to show when edit button is pressed */

   private Class customDialogClass;
   private Object objectToEdit;
   private JDialog customDialog = null;

   public ButtonTableCellEditor(Class customDialogClass)
   {
      super("Edit");
      this.customDialogClass = customDialogClass;
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

   public void setObjectToEdit(Object objectToEdit)
   {
      this.objectToEdit = objectToEdit;
   }

   public Component getTableCellEditorComponent(JTable table, Object value,
                                                boolean isSelected,
                                                int row, int column)
   {
      this.table = table;
      this.row = row;
      this.column = column;

      //originalValue = (Value) this.table.getParameter(row);
      //newValue = originalValue;
      return this;
   }

   // CellEditor methods
   public void cancelCellEditing(){fireEditingCanceled();}
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
         customDialog.setVisible(true);
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

   public String toString()
   {
      return "ButtonTableCellEditor for class "+customDialogClass.getName().toString();
   }
}

