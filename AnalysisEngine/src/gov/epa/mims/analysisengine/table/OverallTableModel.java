package gov.epa.mims.analysisengine.table;

import gov.epa.mims.analysisengine.table.filter.FilterCriteria;
import gov.epa.mims.analysisengine.table.filter.FilteringTableModel;
import gov.epa.mims.analysisengine.table.format.ColumnFormatInfo;
import gov.epa.mims.analysisengine.table.format.FormatAndIndexInfoIfc;
import gov.epa.mims.analysisengine.table.sort.SortCriteria;
import gov.epa.mims.analysisengine.table.sort.SortingTableModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.*;

/**
 * <p>
 * Description: A model that controls the underlying sort, filter and aggregating models.
 * </p>
 * <p>
 * Copyright: Copyright (c) 2003
 * </p>
 * <p>
 * Company:
 * </p>
 * 
 * @author Daniel Gatti
 * @version $Id: OverallTableModel.java,v 1.9 2009/03/26 14:22:23 dyang02 Exp $
 */
public class OverallTableModel extends MultiRowHeaderTableModel implements TableModelListener, java.io.Serializable,
		FormatAndIndexInfoIfc {
	/** The table model that performs the sorting. */
	protected SortingTableModel sortModel = null;

	/** The table model that performs that filtering. */
	protected FilteringTableModel filterModel = null;

	/** The filter model that performs the row and column aggregation. */
	protected AggregatingTableModel aggModel = null;

	/** The base model that contains all of the data. */
	protected MultiRowHeaderTableModel baseModel = null;

	/** The model for the row header. */
	protected RowHeaderTableModel headerModel = null;

	/** A mapping of column names and column formats */
	protected Hashtable ColumnNameToFormats = new Hashtable();

	/**
	 * Constructor.
	 * 
	 * @param model
	 */
	public OverallTableModel(MultiRowHeaderTableModel model) {
		// String together the data models that change and decorate the data.
		baseModel = model;
		filterModel = new FilteringTableModel(baseModel);
		sortModel = new SortingTableModel(filterModel);
		aggModel = new AggregatingTableModel(sortModel);
		headerModel = new RowHeaderTableModel(aggModel);
		headerModel.addTableModelListener(this);
	} // OverallTableModel()

	public void setBaseModel(MultiRowHeaderTableModel model){
		if ( headerModel != null )
			headerModel.removeTableModelListener(this);
		this.baseModel = model;
		filterModel.setModel(baseModel);
		sortModel.setModel(filterModel);
		aggModel.setModel(sortModel);
		headerModel.setModel(aggModel);
		headerModel.addTableModelListener(this);
		//fireTableDataChanged();
	}

	/**
	 * Don't need to implement this method unless your table is editable.
	 */
	public boolean isCellEditable(int row, int col) {
		return baseModel.isCellEditable(row, col - 1);
	}
	
	/**
	 * Aggregate the columns and add new columns.
	 */

	public void aggregateColumns() {
		aggModel.aggregateColumns();
	} // aggregateColumns()

	/**
	 * Aggregate rows by adding sum and average row.
	 * 
	 * @param redo
	 *            boolean that is true if we should redo the aggregation.
	 */
	public void aggregateRows(boolean redo) {
		aggModel.aggregateRows(redo);
	} // aggregateRows()

	/**
	 * Filter the columns in the table based on the criteria passed in.
	 */
	public void filterColumns(FilterCriteria filterCriteria) {
		filterModel.filterColumns(filterCriteria);
	} // filterColumns()

	/**
	 * Filter the rows in the table based on the criteria passed in.
	 */
	public void filterRows(FilterCriteria filterCriteria) {
		filterModel.filterRows(filterCriteria);
	} // filterRows()

	/**
	 * Return the number of columns in the base model.
	 * 
	 * @return int that is the number of columns in the base model.
	 */
	public int getBaseColumnCount() {
		return baseModel.getColumnCount();
	} // getBaseColumnCount()

	/**
	 * Return the column names in the base model.
	 * 
	 * @return String[] that is a list of the column names in the base model.
	 */
	public String[] getBaseColumnNames() {
		String[] retval = new String[baseModel.getColumnCount()];
		for (int c = 0; c < retval.length; c++) {
			retval[c] = baseModel.getColumnName(c);
		}
		return retval;
	} // getBaseColumnNames()

	/**
	 * Return the number of rows in the base model.
	 * 
	 * @return int that is the number of rows in the base model.
	 */
	public int getBaseRowCount() {
		return baseModel.getRowCount();
	} // getBaseRowCount()

	/**
	 * Return the column classes in the base model.
	 * 
	 * @return Class[] that is a list of the column classes in the base model.
	 */
	public Class[] getBaseColumnClasses() {
		Class[] retval = new Class[baseModel.getColumnCount()];
		for (int c = 0; c < retval.length; c++) {
			retval[c] = baseModel.getColumnClass(c);
		}
		return retval;
	} // getBaseColumnNames()

	/**
	 * Return the Class for the requested column.
	 * 
	 * @param col
	 *            int that is the column index.
	 * @return Class that is the class of the column in the base model.
	 */

	public Class getColumnClass(int col) {
		// System.out.println("OverallModel:getColumnClass() " + headerModel.getColumnClass());
		return headerModel.getColumnClass(col);
	} // getColumnCount()

	/**
	 * Return the number of columns in the top most model.
	 * 
	 * @return int that is the number of columns in the top most model.
	 */
	public int getColumnCount() {
		// System.out.println("OverallModel:getColumnCount() " + headerModel.getColumnCount());
		return headerModel.getColumnCount();
	} // getColumnCount()

	/**
	 * Return the column headers.
	 * 
	 * @param col
	 *            int that is the column.
	 * @return String[] that is the headers above the requested column.
	 */
	public String[] getColumnHeaders(int col) {
		return headerModel.getColumnHeaders(col);
	} // getColumnHeaders()

	/**
	 * Return the list of headers for the given column.
	 * 
	 * @param row
	 *            int that is the row index.
	 * @return String[] The headers across the column for the row index.
	 * 
	 */
	public String[] getColumnHeadersInARow(int row) {
		int rowCount = getRowCount();
		if (row < 0 || row > rowCount) {
			return null;
		}
		int colCount = getColumnCount() - 1; // deducting 1 for the firstcolumn
		String[] colHeadersInARow = new String[colCount];
		for (int i = 0; i < colHeadersInARow.length; i++) {
			colHeadersInARow[i] = getColumnHeaders(i)[row];
		}
		return colHeadersInARow;
	} // getColumnHeaders()

	/**
	 * Return the column index for the corresponding column name, not case sensitive
	 * 
	 * @param col0
	 *            String first value of a column header for a column
	 * @return int return the column index corresponds to the column name, if corresponding column name does not exist
	 *         then return -1
	 */
	public int getColumnHeaderIndex(String col0) {
		int count = getColumnCount();
		// System.out.println("column header0= "+ col0);
		// System.out.println("column count= "+ count);
		// ignore the first column
		for (int i = 1; i < count; i++) {
			String[] header = headerModel.getColumnHeaders(i - 1);
			// System.out.println("header["+ 0+ "]= "+ header[0]);
			// compared with the first value of the header
			if (col0.equalsIgnoreCase(header[0])) {
				return i;
			}
		}// for(i)

		return -1;
	}// getColumnIndex()

	/**
	 * Return the column index for the corresponding column name, not case sensitive
	 * 
	 * @param columnName
	 *            String
	 * @return int return the column index corresponds to the column name, if corresponding column name does not exist
	 *         then return -1
	 */
	public int getColumnNameIndex(String colName) {
		int count = getColumnCount();
		// ignore the first column
		// System.out.println("colName="+colName);
		for (int i = 1; i < count; i++) {
			// System.out.println("header model.getColumnName="+headerModel.getColumnName(i));
			if (colName.equalsIgnoreCase(headerModel.getColumnName(i))) {
				return i - 1;
			}
		}// for(i)

		return -1;
	}// getColumnIndex()

	/**
	 * Return the column name.
	 * 
	 * @param col
	 *            int that is the column.
	 * @return String[] that is the headers above the requested column.
	 */
	public String getColumnName(int col) {
		return headerModel.getColumnName(col);
	} // getColumnHeaders()

	/**
	 * Return the column names in the top model.
	 * 
	 * @return String[] that is a list of the column names in the top model.
	 */
	public String[] getColumnNames() {
		String[] retval = new String[getColumnCount() - 1]; // deduting one for the first col
		for (int c = 0; c < retval.length; c++) {
			retval[c] = getColumnName(c + 1);
		}

		return retval;
	} // getColumnNames(

	/**
	 * get format of the column given the column name.
	 * 
	 * @param columnName
	 *            String
	 * @return Format
	 */
	public java.text.Format getFormat(String columnName) {
		ColumnFormatInfo info = (ColumnFormatInfo) ColumnNameToFormats.get(columnName);
		if (info == null)
			return null;
		return info.getFormat();
	}

	/**
	 * Return the column name ordered and separated by the specified options
	 * 
	 * @param col
	 *            int that is the column.
	 * @param colOrder
	 *            int[] that is the column order
	 * @param separator
	 *            String that is the column order
	 * @return String that is the header above the requested column.
	 */
	public String getColumnName(int col, int[] colOrder, String separator) {
		return headerModel.getColumnName(col, colOrder, separator);
	} // getColumnHeaders()

	/**
	 * Return the headers in the top left corner of the table. We are calling the headers above the column headers
	 * "column headers" and the headers next to the rows "row headers", so we're calling these "column row headers".
	 * 
	 * @return String[] that is the headers above the requested column.
	 */
	public String[] getColumnRowHeaders() {
		return headerModel.getColumnRowHeaders();
	} // getColumnRowHeaders()

	/**
	 * Return the headers in the top left corner of the table except the 'units' row header. We are calling the headers
	 * above the column headers "column headers" and the headers next to the rows "row headers", so we're calling these
	 * "column row headers".
	 * 
	 * @return String[] that is the headers above the requested column.
	 */
	public String[] getColumnRowHeadersNoUnits() {
		String[] allHeaders = getColumnRowHeaders();
		ArrayList list = new ArrayList();
		for (int i = 0; i < allHeaders.length; i++) {
			if (!allHeaders[i].equalsIgnoreCase("units")) {
				list.add(allHeaders[i]);
			}
		}// for(i)
		String[] adjustedRowHeaders = new String[list.size()];
		adjustedRowHeaders = (String[]) list.toArray(adjustedRowHeaders);
		return adjustedRowHeaders;
	} // getColumnRowHeaders()

	/**
	 * return the index of the rowHeader which maches the argument 'name'; not case sensitive; return -1 if the 'name'
	 * is not in the column row header
	 * 
	 * @param name
	 * @return int index (0 - the first row at the top ...)
	 */
	public int getColumnRowHeaderIndex(String name) {
		String[] allHeaders = getColumnRowHeaders();
		int index = -1;
		if (allHeaders == null) {
			return -1;
		}
		for (int i = 0; i < allHeaders.length; i++) {
			if (name.equalsIgnoreCase(allHeaders[i])) {
				index = i;
				break;
			}
		}// for(i)
		return index;
	}// getColumnRowHeaderIndex()

	/**
	 * Return the number of columns of data minus the row header column.
	 * 
	 * @return int that is the number of data columns.
	 */
	public int getDataColumnCount() {
		return headerModel.getColumnCount() - 1;
	} // getDataColumnCount()

	/**
	 * Return the data value at the requested index. This does *NOT* include the header row and is designed for users
	 * getting data from the table.
	 * 
	 * @param rowIndex
	 *            int that is the row.
	 * @param columnIndex
	 *            int that is the column.
	 * @return Object that is the data value at the requested row and column.
	 */
	public Object getDataValueAt(int rowIndex, int columnIndex) {
		return headerModel.getValueAt(rowIndex, columnIndex + 1);
	} // getDataValueAt()

	/**
	 * Get the current FilterCriteria that is being applied to the data.
	 * 
	 * @return FilterCriteria
	 */
	public FilterCriteria getFilterCriteria() {
		return filterModel.getFilterCriteria();
	} // getFilterCriteria()

	/**
	 * Return the number of rows visible in the top most model.
	 * 
	 * @return int that is the number of rows visible in the top most model.
	 */
	public int getRowCount() {
		return headerModel.getRowCount();
	} // getRowCount()

	/**
	 * Get the current FilterCriteria that is being applied to the data.
	 * 
	 * @return FilterCriteria
	 */
	public SortCriteria getSortCriteria() {
		return sortModel.getSortCriteria();
	} // getSortCriteria()

	/**
	 * Return the value at the requested index.
	 * 
	 * @param rowIndex
	 *            int that is the row.
	 * @param columnIndex
	 *            int that is the column.
	 * @return Object that is the data value at the requested row and column.
	 */
	public Object getValueAt(int rowIndex, int columnIndex) {
		// System.out.println("overallModel.getValueAt("+rowIndex+", "+columnIndex+")");
		return headerModel.getValueAt(rowIndex, columnIndex);
	} // getValueAt()

	/**
	 * Return the index in the base model corresponds to the overall model index.
	 * 
	 * @param rowIndex
	 *            int that is the row.
	 * @param columnIndex
	 *            int that is the column.
	 * @return index
	 */
	public int getBaseModelRowIndex(int rowIndex) {
		return headerModel.getBaseModelRowIndex(rowIndex);
	} // getValueAt()

	/**
	 * Return an array of values at the requested column index.
	 * 
	 * @param columnIndex
	 *            int that is the column.
	 * @return Object[] data values at the requested column index
	 */
	public Object[] getValueAt(int columnIndex) {
		// System.out.println("overallModel.getValueAt("+rowIndex+", "+columnIndex+")");
		int rowCount = getRowCount();
		Object[] colData = new Object[rowCount];
		for (int i = 0; i < rowCount; i++) {
			colData[i] = getValueAt(i, columnIndex);
		}// for
		return colData;
	} // getValueAt(colIndex)

	/** Editing is allowed only if the column type is boolean */
	public void setValueAt(Object aValue, int row, int col) {
		if (getColumnClass(col) != Boolean.class) {
			return;
		}
		headerModel.setValueAt(aValue, row, col);
	}

	/**
	 * Return the value at the requested index.
	 * 
	 * @param rowIndex
	 *            int that is the row.
	 * @param columnIndex
	 *            int that is the column.
	 * @return String that is the data value at the requested row and column.
	 */
	public String getFormattedValueAt(int rowIndex, int columnIndex) {
		String columnName = getColumnName(columnIndex);
		ColumnFormatInfo info = (ColumnFormatInfo) ColumnNameToFormats.get(columnName);
		if (info == null) {
			return getValueAt(rowIndex, columnIndex).toString();
		}
		return info.getFormat().format(getValueAt(rowIndex, columnIndex));
	}// getFormattedValueAt

	/**
	 * Only reset the part of the model specified. This is used to reset the filter model for the "show top N rows"
	 * command.
	 * 
	 * @param resetRows
	 *            boolean that is true if the row filtering should be reset.
	 * @param resetColumns
	 *            boolean that is true if the row filtering should be reset.
	 * 
	 */

	public void filterReset(boolean resetRows, boolean resetColumns) {
		filterModel.reset(resetRows, resetColumns);
	} // partialReset()

	/**
	 * Reset the columns to the unaltered data.
	 */
	public void reset() {
		baseModel.reset();
		filterModel.reset(true, true);
		sortModel.sortTable(null, 0);
	} // reset()

	/**
	 * Sort the values in the model based on the given criteria.
	 * 
	 * @param sortCriteria
	 *            SortCriteria to use when sorting the data.
	 */
	public void sort(SortCriteria sortCriteria) {
		sortModel.sortTable(sortCriteria, aggModel.getLastRealRow() - 1);
	} // sort()

	/**
	 * Pass this on to any listeners above.
	 * 
	 * @param e
	 */
	public void tableChanged(TableModelEvent e) {
		fireTableChanged(e);
	} // tableChanged()

	/**
	 * update the column format information
	 * 
	 * @param ColumnName
	 *            String
	 * @param info
	 *            ColumnFormatInfo
	 */
	public void setColumnFormatInfo(String ColumnName, ColumnFormatInfo info) {
		ColumnNameToFormats.put(ColumnName, info);
	}

	/**
	 * getter method for Column Name - Format Information
	 */
	public Hashtable getColumnFormatInfo() {
		return ColumnNameToFormats;
	}

	/**
	 * getter method for Format Information given column name
	 * 
	 * @param columnName
	 *            String
	 * @return ColumnFormatInfo
	 */
	public ColumnFormatInfo getColumnFormatInfo(String columnName) {
		return (ColumnFormatInfo) ColumnNameToFormats.get(columnName);
	}

	/**
	 * get the columns of type Double
	 * 
	 * @return boolean[] The array will contain true value if the column is type of double
	 * @pre getColumnCount> 1 WARNING: precondition is not checked
	 */
	public boolean[] getDoubleColumnTypes() {
		boolean[] colClass = null;
		int colCount = getColumnCount();
		colClass = new boolean[colCount - 1];// deduting one for the first column
		for (int i = 0; i < colClass.length; i++) {
			// add 1 to skip the first column
			if (getColumnClass(i + 1).equals(Double.class)) {
				colClass[i] = true;
			}
		}// for(i)
		return colClass;
	}// getDoubleColumnTypes()

	/**
	 * get the first column which is not type of double or date
	 * 
	 * @return The array will contain the true for the first col which is of type string or integer
	 * @pre getColumnCount> 1 WARNING: precondition is not checked
	 */
	public boolean[] getFirstStringIntegerColType() {
		boolean[] colClass = null;
		int colCount = getColumnCount();
		colClass = new boolean[colCount - 1];// deduting one for the first column
		for (int i = 0; i < colClass.length; i++) {
			// add 1 to skip the first column
			Class classType = getColumnClass(i + 1);
			if (classType.equals(String.class) || classType.equals(Integer.class)) {
				colClass[i] = true;
				return colClass;
			}
		}// for(i)
		return colClass;
	}// getDoubleColumnTypes()

	/**
	 * Get the columns which are type date
	 * 
	 * @return The array will contain true for date columns
	 * @pre getColumnCount> 1 WARNING: precondition is not checked
	 */
	public boolean[] getDateColTypes() {
		boolean[] colClass = null;
		int colCount = getColumnCount();
		colClass = new boolean[colCount - 1];// deduting one for the first column
		for (int i = 0; i < colClass.length; i++) {
			// add 1 to skip the first column
			Class classType = getColumnClass(i + 1);
			if (classType.equals(Date.class)) {
				colClass[i] = true;
			}
		}// for(i)
		return colClass;
	}// getDateColTypes()

	/**
	 * Get the columns indices which are of type date(indices based on overallTableModel)
	 * 
	 * @return The array will contain indices for date columns
	 */
	public int[] getFirstDateColTypes() {
		int colCount = getColumnCount();
		int[] dateColType = null;
		int count = 0;
		if (colCount > 0) {
			boolean[] colClass = new boolean[colCount - 1];// deducting one for the first column
			for (int i = 0; i < colClass.length; i++) {
				// add 1 to skip the first column
				Class classType = getColumnClass(i + 1);
				if (classType.equals(Date.class)) {
					colClass[i] = true;
					count++;
				}
			}// for(i)
			dateColType = new int[count];
			int j = 0;
			for (int i = 0; i < colClass.length; i++) {
				if (colClass[i]) {
					dateColType[j] = i;
					j++;
				}// if(colClass[i])
			}// for(i)
		}// if(colCount >0)
		return dateColType;
	}// getDateColTypes()

	/**
	 * return the base model
	 * 
	 * @param TableModel
	 */
	public TableModel getBaseModel() {
		return baseModel;
	}

	/**
	 * get the column names for the filter columns
	 * 
	 * @return String [] column names
	 */
	public String[] getFilterRowColNames() {
		int numCols = getColumnCount();
		String[] filterColNames = new String[numCols - 1];
		return filterColNames;
	}

	public String filterSortInfoString() {
		StringBuffer sb = new StringBuffer();
		String filter = filterModel.filtersInString();
		if (filter.length() == 0)
			filter = "None";
		sb.append("Filter: " + filter);

		String sort = sortModel.sortInString();
		if (sort.length() == 0)
			sort = "None";

		sb.append(", Sort: " + sort);
		return sb.toString();
	}
} 
