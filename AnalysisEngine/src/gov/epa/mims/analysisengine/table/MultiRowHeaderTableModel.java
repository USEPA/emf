package gov.epa.mims.analysisengine.table;

import java.io.Serializable;

import javax.swing.table.AbstractTableModel;

/**
 * <p>
 * Description: A table model that can store multi-row header informatoin.
 * </p>
 * <p>
 * Copyright: Copyright (c) 2003
 * </p>
 * <p>
 * Company:
 * </p>
 * 
 * @author Daniel Gatti
 * @version $Id: MultiRowHeaderTableModel.java,v 1.8 2009/03/23 14:12:39 dyang02 Exp $
 */
public abstract class MultiRowHeaderTableModel extends AbstractTableModel implements Serializable {
	/**
	 * The list of column headers. This is the transpose of the array passed into the constructor. Each String[] is the
	 * header information for one column.
	 */
	protected String[][] columnHeaders = null;

	/**
	 * The headers that go in the upper right of the table and act as row headers for the column names.
	 */
	protected String[] columnRowHeaders = null;

	/**
	 * Null Constructor.
	 */
	public MultiRowHeaderTableModel() {
		/* Nothing */
	} // MultiRowHeaderTableModel()

	/**
	 * Constructor.
	 * 
	 * @param MultiRowHeaderTableModel
	 *            that is beneath this one.
	 */
	public MultiRowHeaderTableModel(MultiRowHeaderTableModel other) {
		this.columnHeaders = other.columnHeaders;
		this.columnRowHeaders = other.columnRowHeaders;
	} // MultiRowHeaderTableModel()

	/**
	 * Constructor.
	 * 
	 * @param columnRowHeaders
	 *            String[] with the row headings for each column header row.
	 * @param columnHeaders
	 *            String that is the list of column headers and each String[] is one row of headers.
	 */
	public MultiRowHeaderTableModel(String[] columnRowHeaders, String[][] columnHeaders) {
		this.columnHeaders = transposeArray(columnHeaders);
		this.columnRowHeaders = columnRowHeaders;
	} // MultiRowHeaderModel()

	public abstract int getBaseModelRowIndex(int rowIndex);

	/**
	 * Return the class of objects in this column.
	 * 
	 * @param col
	 *            int the column for which to return the Class of objects.
	 * @return Class of objects in the requested column.
	 */
	public Class getColumnClass(int col) {
		Class retval = String.class;
		if (getRowCount() > 0) {
			Object value = getValueAt(0, col);
			if (value != null ) {
				retval = value.getClass();
			}
		}
		return retval;
	} // getColumnClass()

	/**
	 * Return the name of the column. This is the concatenation of all of the column headers separated by a '|'.
	 * 
	 * @param col
	 *            int that is the column index for the column name returned.
	 */
	public String getColumnName(int col) {
		String retval = "";
		if (columnHeaders != null) {
			// Temporary hack for the demo!
			StringBuffer sb = new StringBuffer();
			if (columnHeaders[col].length == 1) {
				sb.append(columnHeaders[col][columnHeaders[col].length - 1]);
			} else {
				for (int i = 0; i < columnHeaders[col].length - 2; i++) {
					sb.append(columnHeaders[col][i]);
					sb.append(" | ");
				}
				sb.append(columnHeaders[col][columnHeaders[col].length - 2]);
			}
			retval = sb.toString();
			// End of temporary hack for the demo.

			/*
			 * Put this back after the demo StringBuffer sb = new StringBuffer(); for (int i = 0; i <
			 * columnHeaders[col].length - 1; i++) { sb.append(columnHeaders[col][i]); sb.append(" | "); }
			 * sb.append(columnHeaders[col][columnHeaders[col].length - 1]); retval = sb.toString();
			 */
		} // if (columnHeaders != null)

		return retval;

	} // getColumnName()

	/**
	 * Return the name of the column. This is the concatenation of the specified row headers in the provided order by
	 * the specified separator
	 * 
	 * @param col
	 *            int that is the column index for the column name returned.
	 * @param colOrder
	 *            the order in which the headers need to be added to the column name
	 * @param separator
	 *            between the column headers
	 */
	public String getColumnName(int col, int[] colOrder, String separator) {
		StringBuffer sb = new StringBuffer();
		if (columnHeaders != null) {
			for (int i = 0; i < colOrder.length - 1; i++) {
				sb.append(columnHeaders[col][colOrder[i]]);
				sb.append(separator);
			}
			sb.append(columnHeaders[col][colOrder[colOrder.length - 1]]);
		} // if (columnHeaders != null)
		return sb.toString();

	} // getColumnName()

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
			retval = columnHeaders[col];
		}
		return retval;
	} // getColumnHeaders()

	/**
	 * Return the column headers that go above the row numbers.
	 * 
	 * @return String[] with the column row headers. Could be null.
	 */
	public String[] getColumnRowHeaders() {
		return columnRowHeaders;
	} // getColumnRowHeaders()

	/**
	 * Transpose the given array.
	 * 
	 * @param original
	 *            String[][] that is the array to transpose.
	 * @return String[][] that isthe transpose of the array passed in or null if the original array was null
	 */
	protected String[][] transposeArray(String[][] original) {
		if (original == null || original.length == 0 || original[0] == null || original[0].length == 0) {
			return null;
		}
		String[][] retval = new String[original[0].length][original.length];
		for (int r = 0; r < original.length; r++) {
			for (int c = 0; c < original[0].length; c++) {
				retval[c][r] = original[r][c];
			} // for(c)
		} // for(r)
		return retval;
	} // transposeArray()

	/**
	 * This method will be implemeted here if the data is altered other than filter and sorting operations.
	 * 
	 * @see gov.epa.emissions.emisview.EmisViewObjectTableModel
	 */
	public void reset() {
		// Empty
	}

	/** Editing is allowed only if the column type is boolean */
	public void setValueAt(Object aValue, int row, int col) {
		// Empty
	}

	/**
	 * @return columnHeaders
	 */
	public String[][] getColumnHeaders() {
		return this.columnHeaders;
	}
}

