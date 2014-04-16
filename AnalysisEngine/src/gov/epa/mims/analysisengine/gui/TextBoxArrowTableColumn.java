
package gov.epa.mims.analysisengine.gui;

import gov.epa.mims.analysisengine.tree.TextBoxArrow;

import javax.swing.event.ChangeEvent;
import java.awt.Component;
import java.awt.datatransfer.*;
import java.io.IOException;
import java.util.EventObject;
import javax.swing.*;
import javax.swing.table.*;

/*
 * TextBoxArrowTableColumn.java
 * A Table column which will be contain objects of type TextBoxArrow and when some one
 * clicks on one of the cell in the column TextBoxEditor will pop up
 * @see TextBox.java
 * @see TextBoxArrow.java
 * @author Parthee Partheepan UNC
 * @version $Id: TextBoxArrowTableColumn.java,v 1.2 2005/09/19 14:50:03 rhavaldar Exp $
 */
public class TextBoxArrowTableColumn extends SpecialTableColumn
{

   private TextBoxArrow defaultValue = new TextBoxArrow();

   private TextBoxArrow [] validChoices =  {  new TextBoxArrow()};

   /** The data flavor for a gov.epa.mims.analysisengine.tree.TextBoxArrow */
   public static DataFlavor arrowFlavor = null;

   static
   {
      try
      {
         arrowFlavor = new DataFlavor(Class.forName(
            "gov.epa.mims.analysisengine.tree.TextBoxArrow"),
            "Tree TextBoxArrow");
      }
      catch (ClassNotFoundException e)
      {
         // The user can't do anyting about this, so just print out an error for
         // the developer.
         e.printStackTrace();
      }
   }
   /** Creates a new instance of TextBoxArrowTableColumn */
   public TextBoxArrowTableColumn(int modelIndex, String name)
   {
      super(modelIndex, name);
      dataFlavor = TextBoxArrowTableColumn.arrowFlavor;
      type = TextBoxArrow.class;
      this.setupRenderer();
   }


   protected void copySelectedCell(Object obj)
   {
      TextBoxArrow a = (TextBoxArrow)obj;
      contents = new TextBoxArrowSelection(a);
   }

   /**
    * Paste what is on the contents to a particular cell defined by row and column
    * @table JTable on which paste operation is to be performed
    * @row int row no on the table
    * @column int column no on the table
    *
    */
   protected void pasteSelectedCells(JTable table, int row, int column)
   {
      // if contents is null paste nothing
      if(contents ==null)
      {
         return;
      }

      if (contents.isDataFlavorSupported(dataFlavor))
      {
         try
         {
            Object o = contents.getTransferData(dataFlavor);
            TextBoxArrow arrow = (TextBoxArrow)((TextBoxArrow)o).clone();
            table.setValueAt(arrow, row, column);
         }
         catch (Exception e)
         {
            DefaultUserInteractor.get().notify(table,"Pasting Error",
            "The item that you are trying to paste cannot be pasted into this table.",
            UserInteractor.ERROR);
         }
      }
   }


   public Object getDefaultValue()
   {
      return defaultValue;
   }

   public Object nextChoice()
   {
      return new TextBoxArrow();
   }

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
            if ( obj != null)
            {
               if(obj instanceof TextBoxArrow)
               {
                  TextBoxArrow arrow = (TextBoxArrow)obj;
                  label.setText("Pos( x="+ arrow.getX()+ ", y="+ arrow.getY()+ ")");
                  label.setHorizontalAlignment(SwingConstants.CENTER);
               }
               else
               {
                  DefaultUserInteractor.get().notify(table,"Unexpected object type",
                  "Expected a TextBoxArrow in TextBoxArrowTableColumn.getTableCellRendererComponent()",
                  UserInteractor.ERROR);
               }//else
            }
            else
            {
               label.setText("");
            }

            return label;
         }
      }
      );
   } //setUpRenderer()


   public void setDefaultValue(Object obj)
   {
      //      this.defaultValue = (Strng)obj;
   }

   protected void setupCellEditor()
   {
      cellEditor = new TextBoxArrowCellEditor();
   }

   public class TextBoxArrowCellEditor extends DefaultCellEditor
   {

      /** The original string in the cell. */
      private String originalString = null;

      /** The stirng that we will return. This will be the newly chosen value if
       * editing was successful and the original value if editing was cancelled. */
      private String returnString = null;

      /** The editing label to place in the cell while the user is editing a textBox. */
      private JLabel label = new JLabel("Editing...", SwingConstants.CENTER);

      private TextBoxArrowEditor cellTextBoxArrowEditor = null;


      public TextBoxArrowCellEditor()
      {
         super(new JTextField());
      }

      public Object getCellEditorValue()
      {
         return cellTextBoxArrowEditor.getDataSource();
      }

      public java.awt.Component getTableCellEditorComponent(JTable table,
      Object value, boolean isSelected, int row, int column)
      {

         Object obj = table.getValueAt(row, column);
         if (obj instanceof TextBoxArrow)
         {
            cellTextBoxArrowEditor = new TextBoxArrowEditor((TextBoxArrow)obj);
         }
         else
         {
            DefaultUserInteractor.get().notify(table,"Unexpected object type",
            "Expected an TextBoxArrow in TextBoxArrowTableColumn.getTableCellEditorComponent()",
            UserInteractor.ERROR);
         }
         return label;

      }

      /**
       * Bring up the editor
       * This has to be done here because this is the last method that Java
       * calls when editing. It does *NOT* work from getTableCellEditorComponent().
       *
       * @author Daniel Gatti
       * @return boolean that is what the super class would return.
       */
      public boolean shouldSelectCell(EventObject e)
      {
         JTable table = (JTable)e.getSource();

         cellTextBoxArrowEditor.setVisible(true);
         table.editingStopped(new ChangeEvent(table));
         return super.shouldSelectCell(e);
      }

      /**
       * Always let the editor stop editing.
       *
       * @return boolean that is always true.
       */
      public boolean stopCellEditing()
      {
         return true;
      }

      /**
       * Set the return TextBoxArrow back to the previous state before editing
       */
      public void cancelCellEditing()
      {
         returnString = originalString;
      }

   }//TextBoxArrowCellEditor


   //TextBoxArrowSelection
   /**
 * A class to encapsulte gov.epa.mims.analysisengine.tree.TextBoxArrow for copying
 * and pasting.
 * @see gov.epa.mims.analysisengine.gui.TextBoxArrowTableColumn
 * @see gov.epa.mims.analysisengine.gui.SpecialTableColumn
 * @see gov.epa.mims.analysisengine.gui.MultiEditableTablePanel
 */
public class TextBoxArrowSelection implements Transferable, ClipboardOwner
{

   /** All of the data flavors that this class supports. */
   protected DataFlavor[] dataFlavors = {arrowFlavor};

   /** The TextBoxArrow that is stored for transfer. */
   protected TextBoxArrow arrowValue = null;



   /**
    * Constructor
    *
    * @author Daniel Gatti
    * @param value TextBoxArrow that is stored in this TransferHandler.
    */
   public TextBoxArrowSelection(TextBoxArrow value)
   {
      this.arrowValue = value;
   }


   /**
    * Return the data flavors supported by this class.
    * (gov.epa.mims.analysisengine.tree.TextBoxArrow)
    *
    * @author Daniel Gatti
    * @return DataFlavor[] that lists all of the flavors suppoted by this object.
    * @see java.awt.datatransfer.Transferable#getTransferDataFlavors()
    */
   public DataFlavor[] getTransferDataFlavors()
   {
      return dataFlavors;
   }


   /**
    * Return true if the given flavor is for gov.epa.mims.analysisengine.tree.TextBoxArrow
    *
    * @author Daniel Gatti
    * @param queryFlavor DataFlavor to compare to this objects flavors.
    * @return boolean that is true if the passed in flavor is for a gov.epa.mims.analysisengine.tree.TextBoxArrow
    * @see java.awt.datatransfer.Transferable#isDataFlavorSupported(java.awt.datatransfer.DataFlavor)
    */
   public boolean isDataFlavorSupported(DataFlavor queryFlavor)
   {
      return (queryFlavor == arrowFlavor);
   }


   /**
    * Return the transfer data encapsulted in this object.
    *
    * @author Daniel Gatti
    * @param flavor DataFlavor to request from this object.
    * @return Object that is the data contained in this object.
    * @see java.awt.datatransfer.Transferable#getTransferData(java.awt.datatransfer.DataFlavor)
    */
   public Object getTransferData(DataFlavor flavor)
      throws UnsupportedFlavorException, IOException
   {
      if (flavor == arrowFlavor)
         return arrowValue;
      else
         throw new UnsupportedFlavorException(flavor);
   }


   /**
    * Do nothing.
    *
    * @see java.awt.datatransfer.ClipboardOwner#lostOwnership(java.awt.datatransfer.Clipboard, java.awt.datatransfer.Transferable)
    */
   public void lostOwnership(Clipboard arg0, Transferable arg1)
   {
      /* Nothing */
   }
}//TextBoxArrowSelection



}
