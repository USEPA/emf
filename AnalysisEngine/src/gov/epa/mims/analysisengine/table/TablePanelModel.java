package gov.epa.mims.analysisengine.table;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * <p>
 * Title:TablePanelModel.java
 * </p>
 * <p>
 * Description: A data model to hold the data in a table panel
 * </p>
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * <p>
 * Company: CEP, UNC-Chapel Hill
 * </p>
 * 
 * @author Parthee Partheepan
 * @version $Id: TablePanelModel.java,v 1.4 2007/05/31 14:29:30 qunhe Exp $
 */
public class TablePanelModel implements Serializable {
	static final long serialVersionUID = 1;
	
	/** A String to store the table header information */
	private String tableDataHeader;

	/** store the column headers */
	private String[][] columnHeaders;

	/* store the data of the table */
	private ArrayList tableData;

	/** A String to store the file footer information */
	private String tableDataFooter;

	// /** tab no */
	// private int tabNo = -1;

	/** name of the file opened in the tab pane (absolute file name) */
	private String fileName;

	private Class[] columnTypes;

	public TablePanelModel(String fileName, String fileType) {
		this.fileName = fileName;
	}

	public TablePanelModel(String tableDataHeader, String[] columnRowHeaders, String[][] columnHeaders,
			ArrayList tableData, String tableDataFooter, Class[] columnTypes) {
		this.tableDataHeader = tableDataHeader;
		this.columnHeaders = columnHeaders;
		this.tableData = tableData;
		this.tableDataFooter = tableDataFooter;
		this.columnTypes = columnTypes;
	}// TablePanelModel()

	/**
	 * Getter for property tableDataHeader.
	 * 
	 * @return Value of property tableDataHeader.
	 * 
	 */
	public String getTableDataHeader() {
		return tableDataHeader;
	}

	public void setColumnTypes(Class[] columnTypes) {
		this.columnTypes = columnTypes;
	}

	public Class getColumnClass(int i) {
		return columnTypes[i];
	}

	/**
	 * Setter for property tableDataHeader.
	 * 
	 * @param tableDataHeader
	 *            New value of property tableDataHeader.
	 * 
	 */
	public void setTableDataHeader(String tableDataHeader) {
		this.tableDataHeader = tableDataHeader;
	}

	/**
	 * Getter for property columnHeaders.
	 * 
	 * @return Value of property columnHeaders.
	 * 
	 */
	public String[][] getColumnHeaders() {
		return this.columnHeaders;
	}

	/**
	 * Setter for property columnHeaders.
	 * 
	 * @param columnHeaders
	 *            New value of property columnHeaders.
	 * 
	 */
	public void setColumnHeaders(String[][] columnHeaders) {
		this.columnHeaders = columnHeaders;
	}

	/**
	 * Getter for property tableData.
	 * 
	 * @return Value of property tableData.
	 * 
	 */
	public java.util.ArrayList getTableData() {
		return tableData;
	}

	/**
	 * Setter for property tableData.
	 * 
	 * @param tableData
	 *            New value of property tableData.
	 * 
	 */
	public void setTableData(java.util.ArrayList tableData) {
		this.tableData = tableData;
	}

	/**
	 * Getter for property tableDataFooter.
	 * 
	 * @return Value of property tableDataFooter.
	 * 
	 */
	public String getTableDataFooter() {
		return tableDataFooter;
	}

	/**
	 * Setter for property tableDataFooter.
	 * 
	 * @param tableDataFooter
	 *            New value of property tableDataFooter.
	 * 
	 */
	public void setTableDataFooter(String tableDataFooter) {
		this.tableDataFooter = tableDataFooter;
	}

	/**
	 * Getter for property fileName.
	 * 
	 * @return Value of property fileName.
	 * 
	 */
	public String getFileName() {
		return fileName;
	}

}// TablePanelModel

