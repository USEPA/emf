package gov.epa.mims.analysisengine.table;

import java.util.ArrayList;

/**
 * SpecialTableModel.java A model which have header and footer information inaddition to the parent class
 * 
 * @author Parthee Partheepan
 * @version $Id: SpecialTableModel.java,v 1.3 2006/10/30 21:43:50 parthee Exp $
 */
public class SpecialTableModel extends MultiRowHeaderTableModel {
	/** a string to store the table header information */
	protected String tableDataHeader;

	/** a string to store the table footer information */
	protected String tableDataFooter;

	/**
	 * table data: ArrayList of ArrayList, each row is an ArrayList
	 */
	protected ArrayList tableData;

	/**
	 * columnType: array of column class types
	 */
	protected Class[] columnTypes;

	/**
	 * Null Constructor.
	 */
	public SpecialTableModel() {
		super(new String[] { "" }, new String[][] { { "" } });
	} // SpecialTableModel()

	/**
	 * Constructor.
	 */
	public SpecialTableModel(String[] columnRowHeaders, String[][] columnHeaders,

	ArrayList tableData) {
		super(columnRowHeaders, columnHeaders);
		this.tableData = tableData;
	} // SpecialTableModel()

	/**
	 * Constructor.
	 */
	public SpecialTableModel(String[] columnRowHeaders, String[][] columnHeaders, ArrayList tableData,
			Class[] columnTypes) {
		super(columnRowHeaders, columnHeaders);
		this.tableData = tableData;
		this.columnTypes = columnTypes;
	} // SpecialTableModel()

	/**
	 * Constructor.
	 */
	public SpecialTableModel(String tableDataHeader, String[] columnRowHeaders, String[][] columnHeaders,
			ArrayList tableData, String tableDataFooter, Class[] columnTypes) {
		super(columnRowHeaders, columnHeaders);
		this.tableDataHeader = tableDataHeader;
		this.tableData = tableData;
		this.tableDataFooter = tableDataFooter;
		this.columnTypes = columnTypes;
	} // SpecialTableModel()

	/**
	 * Get the value at rowIndex and columnIndex
	 */
	public Object getValueAt(int rowIndex, int columnIndex) {
		ArrayList rowData = (ArrayList) tableData.get(rowIndex);
		return rowData.get(columnIndex);
	}

	public int getBaseModelRowIndex(int rowIndex) {
		return rowIndex;
	}

	/**
	 * getter for the table header
	 * 
	 * @return String tableHeader
	 */
	public String getTableDataHeader() {
		return tableDataHeader;
	}// getTableHeader()

	/**
	 * getter for the table footer
	 * 
	 * @return String tableFooter
	 */
	public String getTableDataFooter() {
		return tableDataFooter;
	}// getTableFooter()

	// /** get the units
	// * @param Stringp[] array of string
	// */
	// public String[][] getMultiColumnNames()
	// {
	// if(multiLineColumnNames == null)
	// {
	// return null;
	// }
	// String [] aRowNames = (String[])multiLineColumnNames.get(0);
	// String [][] columnHeaderNames= new String[multiLineColumnNames.size()][aRowNames.length];
	// for(int i=0; i< multiLineColumnNames.size(); i++)
	// {
	// columnHeaderNames[i] = (String[])multiLineColumnNames.get(i);
	// }
	//
	// return columnHeaderNames;
	// }//getMultiColumnNames()

	/**
	 * return the no of columns
	 * 
	 * @return int no of columns
	 */
	public int getColumnCount() {
		if (columnTypes == null)
			return 0;
		return columnTypes.length;
	}

	/**
	 * get the row count
	 * 
	 * @return int no of rows
	 */
	public int getRowCount() {
		if (tableData == null) {
			return 0;
		}
		return tableData.size();
	}

	public Class getColumnClass(int col) {
		if (columnTypes != null)
			return columnTypes[col];

		return Object.class;

	}

}
