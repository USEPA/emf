package gov.epa.mims.analysisengine.table.filter;

import gov.epa.mims.analysisengine.table.MultiRowHeaderTableModel;
import gov.epa.mims.analysisengine.table.ComparableBoolean;

import java.util.Hashtable;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

/**
 * This model filters data based on FilterCriteria. It contains an underlying model that contains all of the data. Based
 * on the filter criteria, it creates an array of indices that map to values in the underlying model. For example, if
 * your underlying data contains integers and your filter says to filter out any value less than 5, the map in this
 * model will only contain the data in the underlying model with values < 5.
 * 
 * @version $Id: FilteringTableModel.java,v 1.7 2009/03/26 14:22:23 dyang02 Exp $
 * @author Daniel Gatti
 */
public class FilteringTableModel extends MultiRowHeaderTableModel implements TableModelListener {
	/** The underlying data model that contains the unsorted data. */
	private MultiRowHeaderTableModel underlyingModel = null;

	/** The array that maps the filtered row data to the unsorted data. */
	private int[] rowMap = null;

	/** The array that maps the filtered column data to the unsorted data. */
	private int[] colMap = null;

	/** The FilterCriteria that is currently being applied. */
	protected FilterCriteria filterCriteria = null;

	/**
	 * A hashtable that maps column names to thier index in *this* table. (not in the underlying table. This is used to
	 * lookup column indices by the SortingModel.
	 */
	public Hashtable nameToIndexHash = new Hashtable();

	/** A constant that indicates that a column filter has been applied. */
	public static final int COLUMN_FILTER = -555;

	/**
	 * Constructor
	 */
	public FilteringTableModel(MultiRowHeaderTableModel model) {
		super(model);
		if (model == null)
			throw new IllegalArgumentException("The underlying data model cannot be null in SortingTableModel().");

		underlyingModel = model;
		underlyingModel.addTableModelListener(this);
		reset(true, true);
	} // FilteringTableModel()

	/**
	 * Filter the table columns based on the criteria passed in.
	 * 
	 * @param criteria
	 *            FilterCriteria with column filtering data.
	 */
	public void setModel(MultiRowHeaderTableModel model) {
		if (model == null)
			throw new IllegalArgumentException("The underlying data model cannot be null in SortingTableModel().");
		if ( underlyingModel != null )
			underlyingModel.removeTableModelListener(this);
		underlyingModel = model;
		underlyingModel.addTableModelListener(this);
		filterRows(filterCriteria);
		if (filterCriteria != null)
			filterColumns(filterCriteria);
	} 
	public void filterColumns(FilterCriteria criteria) {
		filterCriteria = criteria;
		String[] allColumns = criteria.getAllColumnNames();
		boolean[] showColumns = criteria.getColumntoShow();
		// Clear the column map if there is no column filtering critria.
		if (allColumns == null || showColumns == null) {
			reset(false, true);
			return;
		}
		// Find the number of trues in the boolean array.
		int numTrues = 0;
		for (int c = 0; c < showColumns.length; c++) {
			if (showColumns[c]) {
				numTrues++;
			}
		} // for(c)
		// Set up the column map with integers that point to the columns in
		// the underlying model to display.
		colMap = new int[numTrues];
		int mapColumn = 0;
		for (int c = 0; c < showColumns.length; c++) {
			if (showColumns[c]) {
				colMap[mapColumn] = c;
				mapColumn++;
			}
		} // for(c)
		populateNameToIndexHashtable();
		fireTableStructureChanged();
	} // filterColumns()

	/**
	 * Filter the table rows based on the criteria passed in.
	 */
	public void filterRows(FilterCriteria criteria) {
		this.filterCriteria = criteria;
		// If the criteria is null, then clear out the maps and reset this model.
		if (criteria == null) {
			reset(true, false);
		} else if (!filterCriteria.isApplyFilters()) {
			resetRows();
		}// else if
		else {
			int rowCount = underlyingModel.getRowCount();
			int colCount = getColumnCount();
			int rowsToShow = 0;
			int[] tmp = new int[rowCount];
			Comparable[] rowValues = new Comparable[colCount];
			// Go through each row in the underlying model and see if it should be
			// hidden or shown.
			for (int row = 0; row < rowCount; row++) {
				// System.out.println("row " + row);
				fillupRowValues(row, rowValues);

				if (criteria.accept(rowValues)) {
					// System.out.println(" Accepting row " + row);
					tmp[rowsToShow++] = row;
				}
			} // for(i)

			rowMap = new int[rowsToShow];
			System.arraycopy(tmp, 0, rowMap, 0, rowsToShow);
		}// else

		fireTableDataChanged();
	} // filterRows()

	private void fillupRowValues(int row, Comparable[] rowValues) {
		int length = rowValues.length;
		for (int col = 0; col < length; col++) {
			if (underlyingModel.getColumnClass(colMap[col]).equals(Boolean.class)) {
				Boolean myBoolean = (Boolean) underlyingModel.getValueAt(row, colMap[col]);
				rowValues[col] = new ComparableBoolean(myBoolean);
			} else {
				rowValues[col] = (Comparable) underlyingModel.getValueAt(row, colMap[col]);
			}
		}
	}

	/**
	 * Goto the underlying model and get the class for the requested column.
	 * 
	 * @param col
	 *            int that it the column.
	 * @return Class that is the class for teh requested column.
	 */
	public Class getColumnClass(int col) {
		// System.out.println("Filter: getColumnClass("+colMap[col]+") = " + underlyingModel.getValueAt(0,
		// colMap[col]).getClass());
		Class retval = String.class;
		try {
			retval = underlyingModel.getColumnClass(colMap[col]);
		} catch (Exception e) { // in case of absence of getColumnClass()
			Object value = underlyingModel.getValueAt(0, colMap[col]);
			if (value != null)
				retval = value.getClass();
		}
		return retval;
	} // getColumnClass()

	/**
	 * Return the number of columns in this table.
	 * 
	 * @return int that is the number of columns in this table.
	 */
	public int getColumnCount() {
		return colMap.length;
	} // getColumnCount()

	/**
	 * Return the list of headers for the given column.
	 * 
	 * @param col
	 *            int that is the column index.
	 * @return String[] with the headers for one column. Could be "" if there are no column headers.
	 */
	public String[] getColumnHeaders(int col) {
		String[] retval = { "" };
		if (columnHeaders != null) {
			// System.out.println("colMap["+ col+"] = " + colMap[col]);
			retval = columnHeaders[colMap[col]];
		}
		return retval;
	} // getColumnHeaders()

	/**
	 * Return the column name for the requested column.
	 * 
	 * @param col
	 *            int
	 * @return String that is the name of the requested column
	 */
	public String getColumnName(int col) {
		return underlyingModel.getColumnName(colMap[col]);
	} // getColumnName()

	/**
	 * Return the FilterCriteria that is currently being used, if any.
	 * 
	 * @return FilterCriteria that is being applied to the data. (could be null)
	 */
	public FilterCriteria getFilterCriteria() {
		return filterCriteria;
	} // getFilterCriteria()

	public int getRowCount() {
		// System.out.println("Filter : getRowCount() " + rowMap.length);
		return rowMap.length;
	} // getRowCount()

	/**
	 * Return the value at the given row and column for this model. This mean that we take the index from the filtrMap,
	 * which maps values from the GUI row and column to the underlyingModel row and column.
	 * 
	 * @param rowIndex
	 * @param columnIndex
	 * @return
	 */

	public Object getValueAt(int rowIndex, int columnIndex) {
		return underlyingModel.getValueAt(rowMap[rowIndex], colMap[columnIndex]);
	}

	public int getBaseModelRowIndex(int rowIndex) {
		return underlyingModel.getBaseModelRowIndex(rowMap[rowIndex]);
	}

	/** Editing is allowed only if the column type is boolean */
	public void setValueAt(Object aValue, int row, int col) {
		underlyingModel.setValueAt(aValue, rowMap[row], colMap[col]);
	}

	/**
	 * Populate the column name to column index hashtable.
	 */
	protected void populateNameToIndexHashtable() {
		nameToIndexHash.clear();
		int numCols = getColumnCount();
		for (int c = 0; c < numCols; c++) {
			nameToIndexHash.put(getColumnName(c), Integer.valueOf(c));
		} // for(c)
	} // populateNameToIndexHashtable()

	/**
	 * Clear and reset the row and column maps to just pass data through unchanged.
	 * 
	 * @param resetRows
	 *            boolean that is true if the row filtering should be reset.
	 * @param resetColumns
	 *            boolean that is true if the row filtering should be reset.
	 */
	public void reset(boolean resetRows, boolean resetColumns) {
		if (resetRows) {
			rowMap = new int[underlyingModel.getRowCount()];
			for (int i = 0; i < rowMap.length; i++) {
				rowMap[i] = i;
			}
		}

		if (resetColumns) {
			colMap = new int[underlyingModel.getColumnCount()];
			for (int i = 0; i < colMap.length; i++) {
				colMap[i] = i;
			}
		}

		filterCriteria = null;
		populateNameToIndexHashtable();

		// This notifies the table that both the rows and columns may have changed.
		if (resetColumns) {
			fireTableStructureChanged();
		} else {
			fireTableDataChanged();
		}
	} // reset()

	/**
	 * Clear and reset the row and column maps to just pass data through unchanged.
	 */
	public void resetRows() {
		rowMap = new int[underlyingModel.getRowCount()];
		for (int i = 0; i < rowMap.length; i++) {
			rowMap[i] = i;
		}
		populateNameToIndexHashtable();
		fireTableDataChanged();
	} // resetRows()

	public void tableChanged(TableModelEvent e) {
		fireTableChanged(e);
	}

	public String filtersInString() {
		return (filterCriteria == null) ? "" : filterCriteria.toString();
	}

}
