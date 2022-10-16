package gov.epa.mims.analysisengine.gui;



import java.util.Vector;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.HashMap;
import java.util.Map;
import java.util.EventObject;
import javax.swing.event.*;
import javax.swing.table.*;
import gov.epa.mims.analysisengine.gui.DefaultUserInteractor;
import gov.epa.mims.analysisengine.gui.GUIUserInteractor;
import gov.epa.mims.analysisengine.gui.UserInteractor;
import gov.epa.mims.analysisengine.gui.ChildHasChangedListener;
import gov.epa.mims.analysisengine.gui.HasChangedListener;



/**
 * A panel that contains a JTable in which a row can be added, deleted and
 * edited. All of this is done in place. Any number of columns can be added
 * to the tables. The colums types can be ImageTableColumn, ColorImageColumn,
 * StringImageColumn, DoubleTableColumn and IntegerTableColumn.
 * @see SpecialTableColumn, ColorTableColumn, IntegerTableColumn,
 * DoubleTableColumn, StringTableColumn, ImageTableColumn
 * @author Daniel Gatti, Alyson Eyth, Parthee Partheepan CEP UNC
 * @version $Id: MultipleEditableTablePanel.java,v 1.2 2005/09/19 14:50:03 rhavaldar Exp $
 */


public class MultipleEditableTablePanel extends JPanel implements ChildHasChangedListener
{
   public static final String DOUBLE_CLICK_TO_SELECT = "Double click to select";
   public static final String DOUBLE_CLICK_TO_ENTER = "Double click to enter";

   /** The table where in which the values will be displayed. **/
   protected JTable table = null;
  

   /** An array of columns that the table will hold */
   private TableColumn [] tableColumns ;
   
   /** The table model which is used to populate the table. */
   protected MultipleIndexedTableModel tableModel = null;
   
   /** The JToolBar in which the buttons will be placed. */
   protected JToolBar toolBar = null;
  
   /** The minimum size for this component. */
   private Dimension minSize ;

 /** The size in pixels of the icons in the talbe. NOTE: either 16 or 24. */
   protected static final int ICON_SIZE = 16;
   
   /** insert above button */
  private JButton insertAboveButton;
   

   /** insert below button */
   private JButton insertBelowButton;
 
   /** copy button */
   private JButton copyButton;

   /** paste button */
   private JButton pasteButton;

   /** delete button */
   private JButton deleteButton;

   /** move up button */
   private JButton moveUpButton;

    /** move up button */
   private JButton moveDownButton;
   
   private Component parentComponent = null;
   
   private boolean hasChanged = false;

   /**
    * Constructor for the panel. Place the toolbar and table on the panel, creates
    * the table model and assign it to the table and create the actions for the
    * buttons.
    * @author Daniel Gatti, Parthee Partheepan
    * @param tableColumns An array of type TableColumn which consist of table columns
    * that the table holds.
    *
    */
   
   public MultipleEditableTablePanel(TableColumn [] tableColumns)
   {
      DefaultUserInteractor.set(new GUIUserInteractor());
      this.tableColumns = tableColumns;
      table = new JTable();
      Vector columnNames = new Vector();
      Vector classTypes = new Vector();
      for(int i=0; i<tableColumns.length; i++)
      {
         columnNames.add((String)tableColumns[i].getHeaderValue());
         classTypes.add((Class)((SpecialTableColumn)tableColumns[i]).getType());
      }
      tableModel = new MultipleIndexedTableModel(columnNames,classTypes);
      table.setModel(tableModel);
      initialize();

   } // EditableTablePanel()

   /**
    * Called by the constructor to assemble the GUI.
    *
    * @author Daniel Gatti
    */
   private void initialize()
   {
      setLayout(new BorderLayout());
      JScrollPane scrPane = new JScrollPane(table,
         JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
         JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

      // Set the column widths

      TableColumn column = null;
      // Set a samller width for the row index column.
      column = table.getColumnModel().getColumn(0);
      //column.setPreferredWidth(75);
      //column.setMaxWidth(50);
      int totalWidth = 0;
      for(int i=1; i< tableColumns.length+1; i++)
      {
         column = table.getColumnModel().getColumn(i);
         column.setCellEditor(((SpecialTableColumn)tableColumns[i-1]).getCellEditor());
         column.setCellRenderer(((SpecialTableColumn)tableColumns[i-1]).getCellRenderer());
         int width = ((SpecialTableColumn)tableColumns[i-1]).getPreferredWidth();
         column.setPreferredWidth(width);
         totalWidth = totalWidth+width;
      }
      //adding tooltip
      setupHeaderToolTip();
      minSize= new Dimension(totalWidth, 200);

      // Set a special renderer for column 0 with a raised border.

      TableColumn tableColumn = table.getColumnModel().getColumn(0);
      tableColumn.setMaxWidth(50);
      tableColumn.setCellRenderer(new TableCellRenderer()
      {
         JButton rowHeader = new JButton("");
         Color selectionColor = UIManager.getDefaults().getColor(
            "TextField.selectionBackground");
         Color headerBackgroundColor = UIManager.getDefaults().getColor(
         "TableHeader.background");

         public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column)
         {
            if (isSelected)
               rowHeader.setBackground(selectionColor);
            else
               rowHeader.setBackground(headerBackgroundColor);
            rowHeader.setText(value.toString());
            return rowHeader;
         }
      });
      toolBar = new JToolBar();

      // Insert Above button
      Action insertAboveAction = new AbstractAction("Insert Above",
         createImageIcon("/toolbarButtonGraphics/table/RowInsertBefore" +
         ICON_SIZE +".gif"))
      {
         public void actionPerformed(ActionEvent e)
         {
            if(table.isEnabled())
            {
               insertRow(true);
            }
         }
      };

      insertAboveAction.putValue(AbstractAction.SHORT_DESCRIPTION,
      "Insert a row above the current row");
      insertAboveButton = toolBar.add(insertAboveAction);
      
     // Insert Below button
      Action insertBelowAction = new AbstractAction("Insert Below",
     createImageIcon("/toolbarButtonGraphics/table/RowInsertAfter" +
      ICON_SIZE + ".gif"))
      {
         public void actionPerformed(ActionEvent e)
         {
            if(table.isEnabled())
            {
               insertRow(false);
            }
        }
     };
     insertBelowAction.putValue(AbstractAction.SHORT_DESCRIPTION,
      "Insert a row below the current row");
      insertBelowButton = toolBar.add(insertBelowAction);
      
     // Delete button
      Action deleteAction = new AbstractAction("Delete",
      createImageIcon("/toolbarButtonGraphics/table/RowDelete" +
      ICON_SIZE + ".gif"))
      {
         public void actionPerformed(ActionEvent e)
         {
            if(table.isEnabled())
            {
               deleteSelectedRows();
            }
         }
      };
      deleteAction.putValue(AbstractAction.SHORT_DESCRIPTION,
      "Delete the selected row(s)");
      deleteButton = toolBar.add(deleteAction);
      
      // Copy button
      Action copyAction = new AbstractAction("Copy",
      createImageIcon("/toolbarButtonGraphics/general/Copy" +
      ICON_SIZE + ".gif"))
      {
         public void actionPerformed(ActionEvent e)
         {
            if(table.isEnabled())
            {
               copySelectedRow();
            }
         }
      };
      copyAction.putValue(AbstractAction.SHORT_DESCRIPTION,
      "Copy the selected row");
      copyButton = toolBar.add(copyAction);
      
      // Paste button
      Action pasteAction = new AbstractAction("Paste",
      createImageIcon("/toolbarButtonGraphics/general/Paste" +
      ICON_SIZE + ".gif"))
      {
         public void actionPerformed(ActionEvent e)
         {
            if(table.isEnabled())
            {
               pasteSelectedRows();
            }
         }
      };
      pasteAction.putValue(AbstractAction.SHORT_DESCRIPTION,
      "Paste into the selected row");
      pasteButton = toolBar.add(pasteAction);
      
      //   Move Up button
      Action moveUpAction = new AbstractAction("Up",
      createImageIcon("/toolbarButtonGraphics/navigation/Up" +
      ICON_SIZE + ".gif"))
      {
         public void actionPerformed(ActionEvent e)
         {
            if(table.isEnabled())
            {
               moveSelectedRow(true);
            }
         }
      };
      moveUpAction.putValue(AbstractAction.SHORT_DESCRIPTION,
      "Move the selected row up one place");
      moveUpButton = toolBar.add(moveUpAction);
      
      //   Move Down button
      Action moveDownAction = new AbstractAction("Down",
      createImageIcon("/toolbarButtonGraphics/navigation/Down" +
      ICON_SIZE + ".gif"))

     {
         public void actionPerformed(ActionEvent e)
         {
            if(table.isEnabled())
            {
               moveSelectedRow(false);
            }
         }
      };
      moveDownAction.putValue(AbstractAction.SHORT_DESCRIPTION,
      "Move the selected row up one place");
      moveDownButton = toolBar.add(moveDownAction);

     
      add(toolBar, BorderLayout.NORTH);
      add(scrPane, BorderLayout.CENTER);
      //enable/disable all but one insert button if no rows are in the table
      TableModel model = table.getModel();
      model.addTableModelListener(new TableModelListener()
     {
        public void tableChanged(TableModelEvent e)
        {
          if(table.getRowCount() ==0)
          {
              enableAllButInsertButton(false);
            }
            else
            {
               enableAllButInsertButton(true);
            }
            update();
         }
      });//
      if(table.getRowCount() ==0)
      {

         enableAllButInsertButton(false);
      }
    
   } // initialize()

   /** enable/disable all but one insert button 
    *@param enable boolean
    */
   private void enableAllButInsertButton(boolean enable)
   {
      //starting from 1 to avoid disable/enable the first button
      insertBelowButton.setEnabled(enable);
      copyButton.setEnabled(enable);
      pasteButton.setEnabled(enable);
      deleteButton.setEnabled(enable);
      moveUpButton.setEnabled(enable);
      moveDownButton.setEnabled(enable);
   }//enableAllButInserButton
   
   /**
    * Copy the currently selected row: It calls the individual columns copy method
    * to store
    * Note: This works only if one row is selected.
    * @author Daniel Gatti
    */
   protected void copySelectedRow()
   {   
      Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
      int rowNo = table.getSelectedRow();
      //calls each individual columns
      if(rowNo != -1 )
      {
         for(int i=1; i< tableColumns.length+1; i++)
         {
            Object obj = table.getValueAt(rowNo, i);
            ((SpecialTableColumn)tableColumns[i-1]).copySelectedCell(obj);
         }
     }
  }

    
   /**
    * Paste what is stored  on the content variable on each of the table columns
    * of the table to the selected row(s).
    *
    */
   protected void pasteSelectedRows()
   {
      int[] selectedRows = table.getSelectedRows();
      
      // Do nothing if there are not rows selected into which to paste.
      if (selectedRows == null | selectedRows.length == 0)
        return;
      
      //calls the each individual columns pastecell method
      for(int j=0; j< selectedRows.length; j++)
      {
         int row = selectedRows[j];
         for(int i=1; i< tableColumns.length+1; i++)
         {

//System.out.println("tableColumn="+i);            
            ((SpecialTableColumn)tableColumns[i-1]).pasteSelectedCells(this.table,row,i);
         }
      }
   }
   
   
   /**
    * Delete the currently selected row(s) from the table. If none are selected, do nothing.
    *
    * @author Daniel Gatti
    */

  protected void deleteSelectedRows()
   {
      int singleSelectedIndex = -1;
      int rowCountAfterDeletion = -1;
      int[] sel = table.getSelectedRows();
      if (sel == null || sel.length == 0)
         return;
      

     // If there is only one row selected, then save the row so that we
      // can reselect a row once the selected row is gone.
      if (sel.length == 1)
         singleSelectedIndex = sel[0];
      
      for (int i = sel.length - 1; i >= 0; --i)

        tableModel.removeRow(sel[i]);
      
      // If the table isn't empty, then select the row at the deleted row's index.
      rowCountAfterDeletion = table.getModel().getRowCount();
      if (rowCountAfterDeletion != 0)
      {
         if (singleSelectedIndex >= 0)
         {
            if (singleSelectedIndex >= rowCountAfterDeletion)
               table.getSelectionModel().setSelectionInterval(
               rowCountAfterDeletion - 1, rowCountAfterDeletion - 1);
            else
               table.getSelectionModel().setSelectionInterval(
               singleSelectedIndex, singleSelectedIndex);
         }
      }
   } // deleeSelectedRows()
   
   
   /**
    * Return the minimum size for this component.
    * @return minSize Dimension that is the minimum size for this panel.
    */
   public Dimension getMinimumSize()
   {
     return minSize;
   }
   
   /**
    * Return the minimum size for this component.
    * @return minSize Dimension that is the minimum size for this panel.
    */
   public Dimension getPreferredSize()
   {
      return minSize;
   }
   
   
   /**
    * Call this method from the concrete sub-classes once the correct type of
    * object has been created.
    *
    * @author Daniel Gatti
    * @param above boolean that is true if the new row should be inserted above the currently
    * selected row.
    * @param Object that is the new object to insert.
    */
   protected void insertRowInternal(boolean above, Vector newValue)
   {
      // If the table is empty, insert a row.
      if (table.getRowCount() == 0)
     {
         tableModel.insertRow(0,newValue);
     }
      else
      {
         int[] insertRows =  table.getSelectedRows();
        int insertRow = -1;
         if (insertRows.length > 0)
         {
            insertRow = (above)
            ? insertRows[0]
            : insertRows[insertRows.length-1];

        }
         
         // If the nothing is selected, then add a row at the bottom.
         if (insertRow < 0)
         {
            insertRow =  table.getRowCount();
         }
         // Otherwise we are inserting below, add one to the selected index.
         else if (!above)
         {
            if (insertRow > table.getRowCount() + 1)
               insertRow =  table.getRowCount();
            insertRow++;
        }
         

        tableModel.insertRow(insertRow, newValue);
         table.getSelectionModel().setSelectionInterval(insertRow, insertRow);
      } // else
   } // insertRowInternal()

  
   
   /**
    * Insert a new row into the table.  Concrete classes should create an object of the
    * appropriate type and call insertRowInternal() to perform the actual insertion into the table.
    *
   * @author Daniel Gatti
   * @param above boolean true if the insertion should occur above the currently selected row.
 */
  public  void insertRow(boolean above)
   {
      Vector values = new Vector();
      for(int i=0; i<this.tableColumns.length; i++)
     {      
       if(((SpecialTableColumn)tableColumns[i]).isInsertDefaultValue())
       {
           values.add(((SpecialTableColumn)tableColumns[i]).getDefaultValue());
        }
       else
         {
           values.add(((SpecialTableColumn)tableColumns[i]).nextChoice());
        }
      }
      insertRowInternal(above, values);    
   }


   /**
    * Return true if the table is empty and false if it contains 1 or more rows.
    *
    * @author Daniel Gatti
    * @retrurns boolean that is true if the table is empty.
    */

  public boolean isEmpty()
   {
    return (table.getRowCount() == 0);
  }
   
   
   /**
    * Move the selected row one row up or down. Do nothing if this will move a
    * row beyond the bounds of the table. Do nothing if there is no row selected.
    * Do nothing if multiple rows are selected.
    *

   * @author Daniel Gatti
    * @param up boolean that is true to move one row up and false to move one row down.

   */
   public void moveSelectedRow(boolean up)

  {
      int[] selectedIndices = table.getSelectedRows();
      
      // If there is only one row, do nothing.
      if (table.getRowCount() < 1)

        return;
      
      // Do nothing if there is no selection or if more than one row is selected.
      if (selectedIndices == null || selectedIndices.length == 0 || selectedIndices.length > 1)
         return;
      
      int selectedIndex = selectedIndices[0];
      int newIndex = selectedIndex;

     
      // Move up
      if (up)
      {
         // If the selected row is the first row, then do nothing.
         if (selectedIndex == 0)
            return;
       
        newIndex = selectedIndex - 1;
      }
      // Move down
      else
      {

        // If the selected row is the last row, then do nothing.
         if (selectedIndex == table.getRowCount() - 1)
            return;
         
         newIndex = selectedIndex + 1;
      }
     
      for(int i=1; i< tableColumns.length+1; i++)
     {
        Object obj = table.getValueAt(newIndex,i );
         table.setValueAt(table.getValueAt(selectedIndex, i), newIndex, i);
         table.setValueAt(obj, selectedIndex, i);
      }

     table.getSelectionModel().setSelectionInterval(newIndex, newIndex);
   }
   
   
   /**
   * Get values of a particular column in a table
  * @param column int column number
   * @pre column >0
   * @return Object [] return the objects in the specified column number
   */
   public Object[] getValueAt(int column)
   {
      //if column 0 is given return null?????
      //max column check
      Object [] obj = new Object[table.getRowCount()];
     for(int i=0; i < table.getRowCount(); i++)
      {      
        obj[i] = table.getValueAt(i, column);       
      }
      return obj;
  }
 
   /**
    *Get values of a particular column in a table
    * @param row int row number
    * @param column int column number
    * @pre column > 0
    * @return Object  objects in the specified cell
    */
   public Object getValueAt(int row, int column)
   {
      //if column 0 is given return null?????
      //max column check
      Object obj = table.getValueAt(row, column);
     return obj;
  }
 
   /**
   * Set values of a particular column in a table
   * @param obj An array of Objects for the column
    * @param column int column number
   * @pre column > 0
    */
  public void setValueAt(Object[] obj, int column)
  {
      //have to throw exception messages if unsuitable types are set
      //max row, column check
      for(int i=0; i < table.getRowCount(); i++)
      {
         table.setValueAt(obj[i], i, column);
      }
   }

   /**
    * Set value of a particular cell in a table
    * @param obj An object for the cell
    * @param row int row number
    * @param column int column number
    * @pre column > 0
    */
   public void setValueAt(Object obj, int row, int column)
   {
      //have to throw exception messages if unsuitable types are set
      //max row, column check
      table.setValueAt(obj, row, column);
   }

   /**
    * Set the row height of the table
    * @param rowHeight height in pixels
    */
   public void setRowHeight(int rowHeight)
   {
      table.setRowHeight(rowHeight);
   }

   

   /**
    * Copied from the Sun website.
    * @param path
    * @param description
    * @return ImageIcon
    */
   protected static ImageIcon createImageIcon(String path)
   {
      java.net.URL imgURL = EditableTablePanel.class.getResource(path);
      if (imgURL != null)
      {
         return new ImageIcon(imgURL);
      }
      else
      {
         System.err.println("Could not find file: " + path);
         return null;
      }
   }

   

   /**
    * to get the number of rows in the table
    * @return int no of rows in the table
    */
   public int getRowCount()
   {
      return tableModel.getRowCount();
   }

   

   /** to remove rows
    *@param rowNo row number to be removed
    */

   public void removeRow(int rowNo)
   {
      this.tableModel.removeRow(rowNo);
   }

   public void setEnabledAll(boolean enable)
   {
      table.setEnabled(enable);
   }

   /**
    * setting up header tool tip
    */
   private void setupHeaderToolTip()
   {
      JTableHeader header = table.getTableHeader();
      ColumnHeaderToolTips tips = new ColumnHeaderToolTips();
      // Assign a tooltip for each of the columns
      for (int c=0; c<tableColumns.length; c++)
      {
         SpecialTableColumn col = (SpecialTableColumn)tableColumns[c];
         tips.setToolTip(table.getColumnModel().getColumn(c+1), col.getColumnHeaderTooltip());
      }
      header.addMouseMotionListener(tips);
   }

   private  class ColumnHeaderToolTips extends MouseMotionAdapter
   {
     // Current column whose tooltip is being displayed.
      // This variable is used to minimize the calls to setToolTipText().
      TableColumn curCol;
      // Maps TableColumn objects to tooltips
      Map tips = new HashMap();

      // If tooltip is null, removes any tooltip text.
      public void setToolTip(TableColumn col, String tooltip)
      {
        if (tooltip == null)
        {
           tips.remove(col);
        } else
         {
           tips.put(col, tooltip);
         }
      }

      public void mouseMoved(MouseEvent evt)
      {
        TableColumn col = null;
         JTableHeader header = (JTableHeader)evt.getSource();
         JTable table = header.getTable();
        TableColumnModel colModel = table.getColumnModel();
        int vColIndex = colModel.getColumnIndexAtX(evt.getX());
          if (vColIndex >= 0)
         {
            col = colModel.getColumn(vColIndex);
         }        
         if (col != curCol)
         {
            header.setToolTipText((String)tips.get(col));
            curCol = col;
         }
      }
}


 /**
   * @param args the command line arguments
    */
   public static void main(String[] args)
   {
      DefaultUserInteractor.set(new GUIUserInteractor());
      DoubleTableColumn doubleColumn1 = new DoubleTableColumn(1,"Double1");
      doubleColumn1.setColumnHeaderTooltip("I am double1");
      DoubleTableColumn doubleColumn2 = new DoubleTableColumn(2,"Double2");
      ColorTableColumn colorColumn1 = new ColorTableColumn(3,"Color");
      ImageTableColumn imageColumn1 = new ImageTableColumn(4,"Image");
      doubleColumn2.setBounds(0, 10);
     Double[] validChoices1 = new Double[10];
      Double[] validChoices2 = new Double[10];
      Color[] validChoices = new Color[13];
      validChoices[0] = Color.black;
      validChoices[1] = Color.blue;
      validChoices[2] = Color.cyan;
     validChoices[3] = Color.darkGray;
     validChoices[4] = Color.gray;
      validChoices[5] = Color.green;
     validChoices[6] = Color.lightGray;
      validChoices[7] = Color.magenta;
      validChoices[8] = Color.orange;
      validChoices[9] = Color.pink;
      validChoices[10] = Color.red;
      validChoices[11] = Color.white;
      validChoices[12] = Color.yellow;

      ImageIcon[] validChoices4 = new ImageIcon[5];
     validChoices4[0] = createImageIcon("/gov/epa/mims/analysisengine/gui/icons/lineStyles/blank.jpg");
      validChoices4[1] = createImageIcon("/gov/epa/mims/analysisengine/gui/icons/lineStyles/solid.jpg");
      validChoices4[2] =  createImageIcon("/gov/epa/mims/analysisengine/gui/icons/lineStyles/dashed.jpg");
      validChoices4[3] = createImageIcon("/gov/epa/mims/analysisengine/gui/icons/lineStyles/twoDash.jpg");
      validChoices4[4] =  createImageIcon("/gov/epa/mims/analysisengine/gui/icons/lineStyles/dotDash.jpg");
      for (int i = 1; i <= 10; i++)
      {
         validChoices1[i-1] = Double.valueOf(i*10.0);
         validChoices2[i-1] = Double.valueOf(i);
      }
      doubleColumn1.setValidChoices(validChoices1);
      //doubleColumn2.setValidChoices(validChoices2);
      //colorColumn1.setValidChoices(validChoices);
      imageColumn1.setValidChoices(validChoices4);
      try
      {
         doubleColumn1.setDefaultValue(Double.valueOf(1.2345));
         doubleColumn2.setDefaultValue(Double.valueOf(1.2345));
      }
      catch(Exception e)
      {
         //nothing
         e.printStackTrace();
      }
      TableColumn [] columns = {doubleColumn1,doubleColumn2,colorColumn1,imageColumn1};
      MultipleEditableTablePanel panel = new MultipleEditableTablePanel(columns);    
      final JFrame frame = new JFrame();
      frame.getContentPane().add(panel);
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      frame.addWindowListener(new WindowAdapter()
      {
         public void windowClosing(WindowEvent e)
         {
            frame.dispose();
            System.exit(0);
         }
      });
      frame.pack();
      frame.setVisible(true);
   }

   public void setParentComponent(Component parent)
   {
      parentComponent = parent;
   }
   
   /**
    * IF hasChanged is false then update calls the update method in the parent component
    * otherwise it changes the hasChanged value to true
    */
   public void update()
   {
      if(parentComponent != null && parentComponent instanceof HasChangedListener)
      {
         if(!hasChanged)
         {
            ((HasChangedListener)parentComponent).update();
         }
         hasChanged = true;
      }
   }
   
   public void setHasChanged(boolean hasChanged)
   {
      this.hasChanged = hasChanged;
   }
}