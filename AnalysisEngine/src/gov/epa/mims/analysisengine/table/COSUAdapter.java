package gov.epa.mims.analysisengine.table;

import java.util.ArrayList;

public class COSUAdapter {

	/** to have pointer to the DoubleTableFromFile */
	private DoubleTableFromFile doubleTable;

	/** store the column headers */
	protected String[][] columnHeader;

	/* store the data of the file */
	protected ArrayList fileData;

	/** A String to store the file footer information */
	protected String fileFooter;

	// ----------------------------------------- //

	protected Class[] columnTypes;

	public static final int NO_OF_COLUMN_HEADER_ROWS = 1;

	public static final String DELIMITER = ",";

	/** Creates a new instance of COSUAdapter */
	public COSUAdapter(String fileName) throws Exception {
		doubleTable = new DoubleTableFromFile(fileName);
		storeDoubleTable();
	}

	/**
	 * a helper method to store the doubleTableFile infor to the memory
	 */
	private void storeDoubleTable() throws Exception {
		try {
			doubleTable.open();
			int noOfColumns = doubleTable.getColumnCount();
			// 1 is added to have a row header column
			String[] columnNames = new String[noOfColumns + 1];
			columnNames[0] = "Rows";
			for (int i = 1; i < columnNames.length; i++) {
				// if name is null or empty string
				columnNames[i] = doubleTable.getColumnName(i - 1);
			}// for i
			// setting column name headers
			columnHeader = new String[1][columnNames.length];
			columnHeader[0] = columnNames;

			// getting each row of data and the row header
			int rowCount = doubleTable.getRowCount();
			fileData = new ArrayList();

			int j = 0;
			for (int i = 0; i < rowCount; i++) {
				double[] arrayOfRowData = doubleTable.getDoubles(i);
				ArrayList rowData = new ArrayList();
				rowData.add(doubleTable.getRowName(i));
				for (j = 0; j < arrayOfRowData.length; j++) {
					rowData.add(Double.valueOf(arrayOfRowData[j]));
				}// for(j)
				fileData.add(rowData);

			}// for i

			columnTypes = new Class[j];
			while (j-- > 0) {
				columnTypes[j - 1] = Double.class;
			}

			doubleTable.close();

		} catch (Exception e) {
			throw e;
		}
	}// storeDoubleTable()

	/**
	 * getter for column header data
	 * 
	 * @return String[][]
	 */
	public String[][] getColumnHeader() {
		return this.columnHeader;
	}

	public String getLogMessages() {
		return null;
	}

	/**
	 * getter for column class type
	 * 
	 * @return Class[]
	 */
	public Class[] getColumnClass() {
		return this.columnTypes;
	}

	/**
	 * getter for file header information
	 * 
	 * @return ArrayList
	 */
	public ArrayList getFileData() {
		return this.fileData;
	}

}// COSU ADAPTER

