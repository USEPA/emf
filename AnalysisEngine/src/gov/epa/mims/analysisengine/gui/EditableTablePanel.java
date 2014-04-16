package gov.epa.mims.analysisengine.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;



/**
 * A panel that contains a JTable in which values can be added, deleted an
 * edited. All of this is done in place. The  concrete implementations of this
 * class will contain the appropiate editors for each data type that they
 * support. The panel can be used either for continuous data or discrete
 * values. When it is invoked for discrete values, the editor that appears
 * will be a JComboBox.
 *
 * @author Daniel Gatti, CEP UNC
 * @version $Id: EditableTablePanel.java,v 1.3 2007/05/31 14:29:32 qunhe Exp $
 */
public  abstract class EditableTablePanel
   extends JPanel implements ChildHasChangedListener
{
   
	static final long serialVersionUID = 123;
	
   public static final String DOUBLE_CLICK_TO_SELECT = "Double click to select";
   
   public static final String DOUBLE_CLICK_TO_ENTER = "Double click to enter";
   
   /** The table where in which the values will be displayed. **/
   protected JTable table = null;
   
   
   /** The table model which is used to populate the table. */
   protected IndexedTableModel tableModel = null;
   
   /** The JToolBar in which the buttons will be placed. */
   protected JToolBar toolBar = null;
   
   /** True if we should check the bounds on the values. */
   protected boolean shouldCheckBounds = false;
   
   /** A list of the buttons in the toolbar. */
   protected JButton[] buttons = null;
   
   /** The number of buttons on the tooolbar. */
   protected static final int NUM_BUTTONS_ON_TOOLBAR = 7;
   
   /** The DataFlavor that this table supports. DataFlavor.stringFlavor for int, double
    * and String and ColorSelection.colorFlavor for Colors. */
   protected DataFlavor dataFlavor = DataFlavor.stringFlavor;
   
   /** The minimum size for this component. */
   private Dimension minSize = new Dimension(225, 100);
   
   /** The size in pixels of the icons in the talbe. NOTE: either 16 or 24. */
   protected static final int ICON_SIZE = 16;
   
   protected boolean hasChanged = false;
   
   protected Component parentComponent;
   
   /**
    *
    * Constructor for the panel. Place the toolbar and table on the panel and
    * create the actions for the buttons.
    * NOTE: Concrete subclasses must call setColumnClass() on the IndexedTableModel
    * in their constructor for the editing and rendering to work correctly.
    *@author Daniel Gatti
    *@param columnName String that is the name of the single data column in this table.
    */
   public EditableTablePanel(String columnName)
   {
      tableModel = new IndexedTableModel(columnName);
      initialize();
   } // EditableTablePanel()
   
   /**
    * Copy the currently selected row.
    * Note: This works only if one row is selected.
    * @author Daniel Gatti
    */
   protected void copySelectedRow()
   {
      Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
      Object obj = table.getValueAt(table.getSelectedRow(), 1);
      String s = obj.toString();
      StringSelection contents = new StringSelection(s);
      cb.setContents(contents, null);
   }
   
   /**
    * Delete the currently selected row(s) from the table. If none are selected, do nothing.
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
      //update();
   } // deleeSelectedRows()
   
   
   /** enable/disable all but one insert button
    *@param enable boolean
    *
    */
   private void enableAllButInsertButton(boolean enable)
   {
      //starting from 1 to avoid disable/enable the first button
      for(int i=1; i< NUM_BUTTONS_ON_TOOLBAR; i++)
      {
         buttons[i].setEnabled(enable);
      }//for(i)
   }//enableAllButInserButton
   
   //   /**
   //    * Return the minimum size for this component.
   //    * @return minSize Dimension that is the minimum size for this panel.
   //    */
   //   public Dimension getMinimumSize()
   //   {
   //      return minSize;
   //   }
   //
   //   /**
   //    * Return the minimum size for this component.
   //    * @return minSize Dimension that is the minimum size for this panel.
   //    */
   //   public Dimension getPreferredSize()
   //   {
   //      return minSize;
   //   }
   /**
    *
    * to get the number of rows in the table
    *
    * @return int no of rows in the table
    *
    */
   public int getRowCount()
   {
      return tableModel.getRowCount();
   }
   
   /**
    *
    * Return the values in this table as an array of Objects.
    * @author Daniel Gatti
    * @returns Object[] that is an array of the values in this table.
    */
   public Object[] getValue()
   {
      Object[] retval = new Object[tableModel.getRowCount()];
      for (int r = tableModel.getRowCount() - 1; r >= 0; --r)
         retval[r] = tableModel.getValueAt(r, 1);
      return retval;
   }
   
   /**
    * Called by the constructor to assemble the GUI.
    * @author Daniel Gatti
    */
   private void initialize()
   {
      setLayout(new BorderLayout());
      table = new JTable();
      table.setModel(tableModel);
      table.setPreferredScrollableViewportSize(new Dimension(100, 100));
      JScrollPane scrPane = new JScrollPane(table,
      JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
      JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
      // Set the column widths
      TableColumn column = null;
      // Set a samller width for the row index column.
      column = table.getColumnModel().getColumn(0);
      column.setPreferredWidth(75);
      column.setMaxWidth(75);
      column = table.getColumnModel().getColumn(1);
      column.setPreferredWidth(150);
      // Set a special renderer for column 0 with a raised border.
      TableColumn tableColumn = table.getColumnModel().getColumn(0);
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
            {
               rowHeader.setBackground(Color.black);
               rowHeader.setForeground(Color.white);
            }
            else
            {
               rowHeader.setBackground(headerBackgroundColor);
               rowHeader.setForeground(Color.black);
            }
            rowHeader.setText(value.toString());
            return rowHeader;
         }
      }
      );
      
      toolBar = new JToolBar();
      buttons = new JButton[NUM_BUTTONS_ON_TOOLBAR];
      int buttonNumber = 0;
      // Insert Above button
      Action insertAboveAction = new AbstractAction("Insert Above",
      createImageIcon("/toolbarButtonGraphics/table/RowInsertBefore" +
      ICON_SIZE +".gif"))
      {
         public void actionPerformed(ActionEvent e)
         {
            insertRow(true);
         }
      };
      
      insertAboveAction.putValue(AbstractAction.SHORT_DESCRIPTION,
      "Insert a row above the current row");
      buttons[buttonNumber] = toolBar.add(insertAboveAction);
      buttonNumber++;
      
      // Insert Below button
      Action insertBelowAction = new AbstractAction("Insert Below",
      createImageIcon("/toolbarButtonGraphics/table/RowInsertAfter" +
      ICON_SIZE + ".gif"))
      {
         public void actionPerformed(ActionEvent e)
         {
            insertRow(false);
         }
      };
      insertBelowAction.putValue(AbstractAction.SHORT_DESCRIPTION,
      "Insert a row below the current row");
      buttons[buttonNumber] = toolBar.add(insertBelowAction);
      buttonNumber++;
      //   Delete button
      Action deleteAction = new AbstractAction("Delete",
      createImageIcon("/toolbarButtonGraphics/table/RowDelete" +
      ICON_SIZE + ".gif"))
      {
         public void actionPerformed(ActionEvent e)
         {
            deleteSelectedRows();
         }
      };
      deleteAction.putValue(AbstractAction.SHORT_DESCRIPTION,
      "Delete the selected row(s)");
      buttons[buttonNumber] = toolBar.add(deleteAction);
      buttonNumber++;
      
      // Copy button
      Action copyAction = new AbstractAction("Copy",
      createImageIcon("/toolbarButtonGraphics/general/Copy" +
      ICON_SIZE + ".gif"))
      {
         public void actionPerformed(ActionEvent e)
         {
            copySelectedRow();
         }
      };
      copyAction.putValue(AbstractAction.SHORT_DESCRIPTION,
      "Copy the selected row");
      buttons[buttonNumber] = toolBar.add(copyAction);
      buttonNumber++;
      
      //   Paste button
      Action pasteAction = new AbstractAction("Paste",
      createImageIcon("/toolbarButtonGraphics/general/Paste" +
      ICON_SIZE + ".gif"))
      {
         public void actionPerformed(ActionEvent e)
         {
            pasteSelectedRows();
         }
      };
      pasteAction.putValue(AbstractAction.SHORT_DESCRIPTION,
      "Paste into the selected row");
      buttons[buttonNumber] =  toolBar.add(pasteAction);
      buttonNumber++;
      
      //   Move Up button
      Action moveUpAction = new AbstractAction("Up",
      createImageIcon("/toolbarButtonGraphics/navigation/Up" +
      ICON_SIZE + ".gif"))
      {
         public void actionPerformed(ActionEvent e)
         {
            moveSelectedRow(true);
         }
      };
      
      moveUpAction.putValue(AbstractAction.SHORT_DESCRIPTION,
      "Move the selected row up one place");
      buttons[buttonNumber] = toolBar.add(moveUpAction);
      buttonNumber++;
      
      //   Move Down button
      Action moveDownAction = new AbstractAction("Down",
      createImageIcon("/toolbarButtonGraphics/navigation/Down" +
      ICON_SIZE + ".gif"))
      {
         public void actionPerformed(ActionEvent e)
         {
            moveSelectedRow(false);
         }
      };
      
      moveDownAction.putValue(AbstractAction.SHORT_DESCRIPTION,
      "Move the selected row up one place");
      buttons[buttonNumber] = toolBar.add(moveDownAction);
      buttonNumber++;
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
      
      add(toolBar, BorderLayout.NORTH);
      add(scrPane, BorderLayout.CENTER);
      //      this.setMinimumSize(minSize);
      this.setPreferredSize(new Dimension(225,200));
   } // initialize()
   
   
   /**
    *
    * Call this method from the concrete sub-classes once the correct type of
    * object has been created.
    *
    * @author Daniel Gatti
    * @param above boolean that is true if the new row should be inserted above the currently
    * selected row.
    * @param Object that is the new object to insert.
    */
   protected void insertRowInternal(boolean above, Object newValue)
   {
      // If the table is empty, insert a row.
      if (table.getRowCount() == 0)
      {
         tableModel.insertRow(0, newValue);
      }
      else
      {
         int[] insertRows =  table.getSelectedRows();
         int insertRow = -1;
         if (insertRows.length > 0)
         {
            insertRow = (above)? insertRows[0]: insertRows[insertRows.length-1];
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
      //update();
   } // insertRowInternal()
   
   
   
   
   
   /**
    * Insert a new row into the table.  Concrete classes should create an object of the
    * appropriate type and call insertRowInternal() to perform the actual insertion into the table.
    * @author Daniel Gatti
    * @param above boolean true if the insertion should occur above the currently selected row.
    *
    */
   protected  abstract void insertRow(boolean above);
   
   /**
    * Return true if the table is empty and false if it contains 1 or more rows.
    * @author Daniel Gatti
    * @retrurns boolean that is true if the table is empty.
    */
   public boolean isEmpty()
   {
      return (table.getRowCount() == 0);
   }
   
   /**
    *
    * Move the selected row one row up or down. Do nothing if this will move a
    * row beyond the bounds of the table. Do nothing if there is no row selected.
    * Do nothing if multiple rows are selected.
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
      Object obj = table.getValueAt(newIndex, 1);
      table.setValueAt(table.getValueAt(selectedIndex, 1), newIndex, 1);
      table.setValueAt(obj, selectedIndex, 1);
      table.getSelectionModel().setSelectionInterval(newIndex, newIndex);
      //update();
   }
   
   /**
    *  Paste what is on the clipboard into the selected row(s).
    * @author Daniel Gatti
    */
   protected void pasteSelectedRows()
   {
      int[] selectedRows = table.getSelectedRows();
      // Do nothing if there are not rows selected into which to paste.
      if (selectedRows == null | selectedRows.length == 0)
         return;
      
      Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
      Transferable content = cb.getContents(this);
      
      if (content.isDataFlavorSupported(dataFlavor))
      {
         try
         {
            Object o = content.getTransferData(dataFlavor);
            for (int i = selectedRows.length - 1; i >= 0; --i)
               table.setValueAt(o, selectedRows[i], 1);
         }
         catch (Exception e)
         {
            DefaultUserInteractor.get().notify(this,"Pasting Error",
            "The item that you are trying to paste cannot be pasted into this table.",
            UserInteractor.ERROR);
         }
      }
      //update();
   }
   
   
   /**
    * Set the bounds on the data entered into the table.
    * Numeric data types are the only implementations that should use this method.
    * @author Daniel Gatti
    * @param lowerBound double that is the lower bound on the data.
    * @param upperBound double that is the upper bound on the data.
    *
    */
   
   public void setBounds(double lowerBound, double upperBound)
   {
      DefaultUserInteractor.get().notify(this,"Bad Method Call",
      "Do not call EditableTablePanel.setBounds() on the super class.",
      UserInteractor.ERROR);
   }
   
   /**
    *
    * Set a default value that will be used when a new row is added.
    * Each concrete class should verify that the correct object type (Integer,
    * Double, String or Color) has been passed in. Each concrete class should
    * set up a defaultValue variable and use it to add a new row in insert row.
    * @author Daniel Gatti
    * @param value Object that is the default value to place in a new row.
    */
   public abstract void setDefaultValue(Object value);
   
   /**
    * Enable or disable the entire panel.
    * @author Daniel Gatti
    * @param enable boolean that is true if the panel should be enabled.
    */
   
   public void setEnabled(boolean enable)
   {
      for (int i = NUM_BUTTONS_ON_TOOLBAR - 1; i >= 0; --i)
         buttons[i].setEnabled(enable);
      table.setEnabled(enable);
   }
   
   /**
    * Set a list of valid choices that will be the only ones that the user can enter into the table.
    * @author Daniel Gatti
    * @param choices Object[] that is a list of valid choices to be displayed in a combo box
    * when the user is editing data in the table.
    */
   public abstract void setValidChoices(Object[] choices);
   
   
   /**
    * Set the values in the table from the data passed in.
    * Note: this method can be overridden in numeric subclasses to accept an array
    * of primitives.
    * Note: Passing in null clears the table.
    * @author dmgatti
    * @param newValues Object[] that is an array of objects of the same type with which to populate the table.
    *
    */
   
   public void setValue(Object[] newValues)
   {
      // First clear the table.
      int size = tableModel.getRowCount();
      for (int i = size - 1; i >= 0; --i)
         tableModel.removeRow(0);
      // Only fill the table if we have any values.
      if (newValues != null)
      {
         // Then populate the table with new values.
         for (int i = newValues.length - 1; i >= 0; --i )
            tableModel.insertRow(0, newValues[i]);
      }
      //update();
   } // setValue()
   
   /**
    * to remove rows
    *@param rowNo row number to be removed
    */
   public void removeRow(int rowNo)
   {
      tableModel.removeRow(rowNo);
   }
   
   
   /**
    * Verify that the items in the list are sorted in the specified order.
    * The method throws an Exception if the list is not sorted and returns
    * without error if the list is sorted.
    *
    * @param boolean ascending that is true if the list should be in ascending
    * order.
    * @throws Exception if the list is not sorted.
    */
   public void verifyListSorted(boolean ascending)
   throws Exception
   {
      int size = tableModel.getRowCount();
      
      // No need to check anything if the list has one or fewer items.
      if (size <= 1)
         return;
      
      // Ascending order
      if (ascending)
      {
         for (int r = 1; r < size; r++)
         {
            Comparable value1 = (Comparable)tableModel.getValueAt(r-1, 1);
            Comparable value2 = (Comparable)tableModel.getValueAt(r, 1);
            if (value1.compareTo(value2) >= 0)
            {
               throw new Exception("The values in this list must be in ascending order.\n" +
               "The two items at rows " + r + " and " + (r+1) +
               " are not in ascending order.");
            }
         }
      }
      // Descending order
      else
      {
         for (int r = 1; r < size; r++)
         {
            Comparable value1 = (Comparable)tableModel.getValueAt(r-1, 1);
            Comparable value2 = (Comparable)tableModel.getValueAt(r, 1);
            if (value1.compareTo(value2) <= 0)
            {
               throw new Exception("The values in this list must be in descending order.\n" +
               "The two items at rows " + r + " and " + (r+1) +
               " are not in dscending order.");
            }
         }
      }
      
   } // verifyListSorted()
   
   /**
    * Copied from the Sun website.
    * @param path
    * @param description
    * @return
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
   
   
   /** get the cell renderer of the table of the specified column
    * @param int colNo
    * @return TableCellRenderer renderer
    */
   public TableCellRenderer getCellRenderer(int colNo)
   {
      if(colNo < table.getColumnCount())
      {
         TableColumnModel model = table.getColumnModel();
         TableColumn column =  model.getColumn(colNo);
         return column.getCellRenderer();
      }//if
      return null;
   }
   
   /** set the cell renderer of the table of the specified column
    * @param TableCellRenderer renderer
    * @param int colNo
    */
   public void setCellRenderer(TableCellRenderer renderer, int colNo)
   {
      if(colNo < table.getColumnCount())
      {
         TableColumnModel model = table.getColumnModel();
         TableColumn column =  model.getColumn(colNo);
         column.setCellRenderer(renderer);
      }//if
   }
   
   /** get the no of columns *
    *@return int no of columns
    */
   public int getColumnCount()
   {
      return table.getColumnCount();
   }
   
   protected static void printUsage()
   {
      System.out.println("usage: java " + EditableTablePanel.class +
      " -type <int>|<dbl>|<str>|<clr> -vc -lb <#> -ub <#>");
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
   
   /**
    * main() for testing.
    * @author dmgatti
    * @param args String[] that is either int, dbl, str or clr to indicate an
    * Integer, Double, String or Color editor.
    */
   public static void main(String[] args)
   {
      if (args.length == 0)
      {
         printUsage();
         System.exit(1);
      }
      String currentArg = null;
      DefaultUserInteractor.set(new GUIUserInteractor());
      final int INT = 0;
      final int DBL = 1;
      final int STR = 2;
      final int CLR = 3;
      int type = INT;
      boolean useValidChoices = false;
      boolean useBounds = false;
      double dblLowerBound = Double.MIN_VALUE;
      double dblUpperBound = Double.MAX_VALUE;
      int intLowerBound = Integer.MIN_VALUE;
      int intUpperBound = Integer.MAX_VALUE;
      // Note: i may be changed inside of the loop.
      for (int i = 0; i < args.length; i++)
      {
         currentArg = args[i];
         if (currentArg.equalsIgnoreCase("-type"))
         {
            i++;
            currentArg = args[i];
            if (currentArg.equalsIgnoreCase("int"))
               type = INT;
            else if (currentArg.equalsIgnoreCase("dbl"))
               type = DBL;
            else if (currentArg.equalsIgnoreCase("str"))
               type = STR;
            else if (currentArg.equalsIgnoreCase("clr"))
               type = CLR;
            else
            {
               System.err.println("Unrecognized type '" + currentArg + "'.");
               printUsage();
               System.exit(2);
            }
         }
         else if (currentArg.equalsIgnoreCase("-vc"))
         {
            useValidChoices = true;
         }
         else if (currentArg.equalsIgnoreCase("-lb"))
         {
            i++;
            useBounds = true;
            currentArg = args[i];
            switch (type)
            {
               case INT:
               {
                  intLowerBound = Integer.parseInt(currentArg);
                  break;
               }
               case DBL:
               {
                  dblLowerBound = Double.parseDouble(currentArg);
                  break;
               }
            }
         }
         else if (currentArg.equalsIgnoreCase("-ub"))
         {
            i++;
            useBounds = true;
            currentArg = args[i];
            switch (type)
            {
               case INT:
               {
                  intUpperBound = Integer.parseInt(currentArg);
                  break;
               }
               case DBL:
               {
                  dblUpperBound = Double.parseDouble(currentArg);
                  break;
               }
            }
         }
         else
         {
            System.err.println("Unrecognized option '" + currentArg + "'.");
            printUsage();
            System.exit(4);
         }
      } // for (i)
      EditableTablePanel panel = null;
      switch (type)
      {
         case INT:
         {
            panel = new IntegerEditableTablePanel("Integer");
            int arraySize = 5;
            Integer[] inputValues = new Integer[arraySize];
            for (int i = 0; i < arraySize; i++)
               inputValues[i] = new Integer(i * 3);
            panel.setValue(inputValues);
            if (useBounds)
            {
               panel.setBounds(intLowerBound, intUpperBound);
            }
            if (useValidChoices)
            {
               Integer[] validChoices = new Integer[10];
               for (int i = 1; i <= 10; i++)
                  validChoices[i-1] = new Integer(i);
               panel.setValidChoices(validChoices);
            }
            panel.setDefaultValue(new Integer(52));
            break;
         }
         case DBL:
         {
            panel = new DoubleEditableTablePanel("Double");
            int arraySize = 5;
            if (useBounds)
               panel.setBounds(dblLowerBound, dblUpperBound);
            if (useValidChoices)
            {
               Double[] validChoices = new Double[10];
               for (int i = 1; i <= 10; i++)
                  validChoices[i-1] = new Double(i*10.0);
               panel.setValidChoices(validChoices);
            }
            panel.setDefaultValue(new Double(1.2345));
            break;
         }
         case STR:
         {
            panel = new StringEditableTablePanel("String");
            int arraySize = 5;
            String[] inputValues = new String[arraySize];
            inputValues[0] = "one";
            inputValues[1] = "two";
            inputValues[2] = "three";
            inputValues[3] = "four";
            inputValues[4] = "five";
            panel.setValue(inputValues);
            if (useValidChoices)
            {
               String[] validChoices = new String[]
               {
                  "one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten"
               };
               panel.setValidChoices(validChoices);
            }
            panel.setDefaultValue("Test suceeded!");
            break;
         }
         case CLR:
         {
            panel = new ColorEditableTablePanel("Color");
            int arraySize = 5;
            Color[] inputValues = new Color[arraySize];
            inputValues[0] = Color.black;
            inputValues[1] = Color.blue;
            inputValues[2] = Color.cyan;
            inputValues[3] = Color.green;
            inputValues[4] = Color.red;
            panel.setValue(inputValues);
            
            if (useValidChoices)
            {
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
               panel.setValidChoices(validChoices);
            }
            break;
         }
      } // switch(type)
      
      if (panel == null)
      {
         System.out.println("Error: valid argument not found.");
         System.out.println("usage: java " + EditableTablePanel.class + " <int>|<dbl>|<str>|<clr>");
         System.exit(1);
      }
      final JFrame frame = new JFrame();
      final int tmpType = type;
      frame.getContentPane().add(panel);
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      final EditableTablePanel tmpPanel = panel;
      frame.addWindowListener(new WindowAdapter()
      {
         public void windowClosing(WindowEvent e)
         {
            switch (tmpType)
            {
               case INT:
               {
                  IntegerEditableTablePanel intPnl = (IntegerEditableTablePanel)tmpPanel;
                  int[] values = intPnl.getValueAsPrimitive();
                  for (int i = 0; i < values.length; i++)
                     System.out.println(i + ",\t" + values[i]);
                  break;
               }
               case DBL:
               {
                  DoubleEditableTablePanel dblPnl = (DoubleEditableTablePanel)tmpPanel;
                  double[] values = dblPnl.getValueAsPrimitive();
                  for (int i = 0; i < values.length; i++)
                     System.out.println(i + ",\t" + values[i]);
                  break;
               }
               case STR:
               {
                  Object[] values = tmpPanel.getValue();
                  for (int i = 0; i < values.length; i++)
                     System.out.println(i + ",\t" + values[i]);
                  break;
               }
               case CLR:
               {
                  Object[] values = tmpPanel.getValue();
                  for (int i = 0; i < values.length; i++)
                     System.out.println(i + ",\t" + values[i]);
                  break;
               }
            } // switch (type)
            
            frame.dispose();
            System.exit(0);
         }
      }
      );
      frame.pack();
      frame.setVisible(true);
   } // main()
} // class EditableTablePanel
