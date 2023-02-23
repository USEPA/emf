package gov.epa.mims.analysisengine.gui;

import java.util.ArrayList;
import javax.swing.table.AbstractTableModel;

/**
 * A table model that supports the leftmost column as a row number.
 * This row index column is not stored in the data model. It is only returned 
 * by the getValueAt() method. 
 * The first column cannot be edited. The data column can be edited.  
 *  
 * @author Daniel Gatti, CEP, UNC
 */
public class IndexedTableModel 
   extends AbstractTableModel 
{
   /** The column name in this table. */
   protected String columnName = null;
   
   /** The column class in this table. */
   protected Class columnClass = null;
   
   /** The data structure that contains the data for this table. */
   protected ArrayList data = new ArrayList();
   
   /**
    * Constructor
    * 
    * @author Daniel Gatti
    */
   public IndexedTableModel(String columnName) 
   {
      super();
      this.columnName = columnName;
   }


   /** 
    * Return the total number of columns in this table including the index column.
    * 
    * @author Daniel Gatti
    * @return int that is the number of columns in this table. 
    * @see javax.swing.table.TableModel#getColumnCount()
    */
   public int getColumnCount() 
   {
      // Add one because we have a "ghost" column for the row number in column 0.
      return 2;
   }
   
   /**
    * Return the class of the requested column.
    * 
    * @author Daniel Gatti
    * @param col int that is the column index requested.
    * @return Class that is the class of objects stored in the requested column. 
    */
   public Class getColumnClass(int col)
   {
      if (col == 0)
         return Integer.class;

      return columnClass;
   }
   
   
   /**
    * Return the column name for the given column index.
    * Return "#" for the first column.
    * 
    * @author Daniel Gatti
    * @param col int that is the column for which the name should be returned.
    * @return String that is the column name for the requested column. 
    */
   public String getColumnName(int col)
   {
      if (col == 0)
         return "#";

      return columnName;
   }

   /**
    * Return the total number of rows in this table.
    * 
    * @author Daniel Gatti
    * @return int that is the number of rows in the table.
    * @see javax.swing.table.TableModel#getRowCount() 
    */
   public int getRowCount() 
   {
      return data.size();      
   }

   /**
    * Return the value at the given index. 
    * First column is the row index that is generated in this method.
    * Second column is the data stored in this class.
    *
    * @author Daniel Gatti
    * @param row int that is the row where the data is requested.
    * @param col int that is the column where the data is requested.
    * @return Object that is the data from the requested row and column.
    * @see javax.swing.table.TableModel#getValueAt(int, int)
    */
   public Object getValueAt(int row, int col) 
   {
      // For the first column, return the row number.
      if (col == 0)
      {
         return Integer.valueOf(row +1);
      }
      else
      {
         Object obj = (Object)data.get(row);
         return obj;
      }
   }
   
   
   /**
    * Insert a row at the given index with the given data array.
    * If the row index is less than 0, the data is inserted at row 0;
    * If the row index is greater than the data array size, then the data is 
    * inserted at the last row.
    * 
    * @author Daniel Gatti
    * @param row in that is the row at which to insert data.
    * @param value Object that is the data to insert into the row.
    */
   public void insertRow(int row, Object value)
   {
      if (row < 0)
         row = 0;
      else if (row > data.size())
         row = data.size();

      data.add(row, value);
      fireTableDataChanged();
   }
   
   
   /**
    * Return false for the first column and true for all other columns.
    * 
    * @author Daniel Gatti
    * @param row int that is the row index.
    * @param col int that is the column index. 
    */
   public boolean isCellEditable(int row, int col)
   {
      return (col != 0); 
   }
   
   
   /**
    * Remove the row at the given index.
    * 
    * @author Daniel Gatti
    * @param row int that is the index of the rwo to remove. 
    */
   public void removeRow(int row)
   {
      if (row < 0 || row >= data.size())
         return;
      
      data.remove(row);
      fireTableRowsDeleted(row, row);
   }
   
   /**
    * Set the class that this table is displaying.
    * NOTE: You must call this for teh editing and rendering to work correctly.
    * 
    * @author Daniel Gatti
    * @param Class that is the type of class that the data column in this table will hold.
    */
   protected void setColumnClass(Class columnClass)
   {
      this.columnClass = columnClass;
   }
   
   
   /**
    * Set the value at the given row and column index to the value passed in.
    * Note that we will ignore any requests to insert into column 0 since this is
    * the row index column.
    *  
    * @author dmgatti
    * @param value Object that is the new value to insert into the table.
    * @param row int that is the row at which to insert this data.
    * @param col int that is the column at which to insert this data.
    */
   public void setValueAt(Object value, int row, int col)
   {
      if (col == 0)
         return;
      
      data.set(row, value);
      fireTableCellUpdated(row, col);
   }
} // class IndexedTableModel

