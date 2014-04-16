package gov.epa.mims.analysisengine.table;

import gov.epa.mims.analysisengine.table.io.FileMethods;

import java.io.BufferedReader;
import java.io.CharArrayReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import org.iscmem.cosu.DoubleTable;
import org.iscmem.cosu.TableException;

/**
 * A DoubleTable that stores it's data in a file
 * 
 * this class implements the DoubleTable interface of the cosu package
 * 
 * @author Steve Howard
 * @version $Id: DoubleTableFromFile.java,v 1.4 2006/10/30 21:43:50 parthee Exp $
 * 
 */

public class DoubleTableFromFile implements DoubleTable {
	static final long serialVersionUID = 1;

	// file to store data
	protected File tableFile;

	protected boolean isOpen;

	protected boolean hasChanged;

	// Double data values
	protected ArrayList tableRows;

	protected ArrayList columnHeaders;

	protected ArrayList rowHeaders;

	/**
	 * constructor
	 * 
	 * @param filename
	 *            String
	 * @throws Exception
	 *             if the filename is invalid
	 */
	public DoubleTableFromFile(String filename) throws Exception {
		// check if no filename defined
		if (filename == null || filename.trim().length() == 0) {
			throw new Exception("No filename defined.");
		}

		// check if file is a directory
		tableFile = new File(filename);
		if (tableFile.exists() && tableFile.isDirectory()) {
			throw new Exception("table file [" + tableFile.getAbsolutePath() + "] is a directory");
		}

		// check if file can be generated
		if (!tableFile.exists()) {
			tableFile.createNewFile(); // try to ensure we can write the file
		}

		isOpen = false;
	}

	/**
	 * constructor
	 * 
	 * @param filename
	 *            String
	 * @param headers
	 *            String[] containing the column headers
	 * 
	 * @throws Exception
	 *             if the filename is invalid
	 */
	public DoubleTableFromFile(String filename, String[] headers) throws Exception {
		// check if no filename defined
		if (filename == null || filename.trim().length() == 0) {
			throw new Exception("No filename defined.");
		}

		// check if file is a directory
		tableFile = new File(filename);
		if (tableFile.exists() && tableFile.isDirectory()) {
			throw new Exception("Table file [" + tableFile.getAbsolutePath() + "] is a directory");
		}

		// check if file can be generated
		if (!tableFile.exists()) {
			tableFile.createNewFile(); // try to ensure we can write the file
		}

		// create empty table
		tableRows = new ArrayList();
		columnHeaders = new ArrayList();
		rowHeaders = new ArrayList();

		// define columnHeaders
		for (int i = 0; i < headers.length; i++) {
			columnHeaders.add(headers[i]);
		}

		isOpen = true;
		hasChanged = true;
	}

	/**
	 * Method returns the name of the table file or null if not defined
	 */
	public String getFilename() {
		if (tableFile != null) {
			return tableFile.getAbsolutePath();
		}
		return null;
	}

	/**
	 * Indicate that the contents of the table will be accessed. This must be called before any operation that access
	 * the contents. Each call to 'open' must be matched to a call to 'close'. It is permissible to call 'open' while
	 * the table is already open.
	 * 
	 * @throws Exception
	 *             if there are problems accessing the table's contents
	 */
	public void open() throws Exception {
		// if file is already open and has size, then skip
		if (isOpen && columnHeaders.size() > 0) {
			return;
		}

		if (!tableFile.exists() || !tableFile.canRead() || tableFile.length() == 0) {
			throw new Exception("Table file [" + tableFile.getAbsolutePath() + "] cannot be opened or is empty.");
		}

		// open file and read table values
		BufferedReader reader = new BufferedReader(new FileReader(tableFile));
		String record = "";
		columnHeaders = new ArrayList();
		rowHeaders = new ArrayList();
		tableRows = new ArrayList();

		// read first line to find the number of items in lists
		record = reader.readLine();
		CharArrayReader charReader = new CharArrayReader(record.toCharArray());

		// parse charReader to get column header names
		try {
			while (true) {
				String headerText = FileMethods.readNextField(charReader);
				headerText = FileMethods.removeQuotationMarks(headerText);
				headerText = FileMethods.restoreSpecialChars(headerText);
				columnHeaders.add(headerText);
			}
		} catch (Exception ex) {
			ex.printStackTrace(); //FIXME: RP
		}

		// read each line and parse values
		while ((record = reader.readLine()) != null) {
			charReader = new CharArrayReader(record.toCharArray());

			try {
				// read rowHeader from first row field
				String headerText = FileMethods.readNextField(charReader);
				headerText = FileMethods.removeQuotationMarks(headerText);
				headerText = FileMethods.restoreSpecialChars(headerText);
				rowHeaders.add(headerText);

				// read double values from row
				double[] rowValues = new double[columnHeaders.size()];
				for (int i = 0; i < rowValues.length; i++) {
					String valueText = FileMethods.readNextField(charReader);
					try {
						rowValues[i] = Double.parseDouble(valueText);
					} catch (Exception ex) {
						rowValues[i] = Double.NaN;
					}
				}
				// add rowValues to tableRows List
				tableRows.add(rowValues);
			} catch (Exception ex) {
				System.out.println("invalid fields in record:" + record);
			}
		}

		// close reader and set table to open
		isOpen = true;
		hasChanged = false;
		reader.close();
	}

	/**
	 * Indicate that access to a table is no longer required. Each call to
	 * <code>close</close> balances a call to <code>open</code>.
	 * If the table is not open then a <code>TableException</code> will be thrown.
	 * @throws Exception if there are problems accessing the table's contents
	 */
	public void close() throws Exception {
		if (!isOpen) {
			throw new TableException("Table file [" + tableFile.getAbsolutePath() + "] is not open.");
		}

		// save table data to persistent storage
		save();

		// set table to close
		isOpen = false;
	}

	/**
	 * write table data to persistent storage if hasChanged flay is true
	 * 
	 * @throws Exception
	 *             if there are problems writing the table's contents
	 */
	public void save() throws Exception {
		if (!isOpen) {
			throw new TableException("Table file [" + tableFile.getAbsolutePath() + "] is not open.");
		}

		// if data has changed, write table to file
		if (hasChanged) {
			PrintWriter writer = null;

			// create writer
			try {
				writer = new PrintWriter(new FileWriter(tableFile));
			} catch (IOException ex) {
				throw new Exception("Error writing to table file [" + tableFile.getAbsolutePath() + "].");
			}

			// write table to file
			try {
				// write column headers to file
				for (int i = 0; i < columnHeaders.size(); i++) {
					String headerText = (String) (columnHeaders.get(i));
					headerText = FileMethods.replaceSpecialChars(headerText);
					headerText = ((i > 0) ? "," : "") + "\"" + headerText + "\"";
					writer.print(headerText);
				}
				writer.println();

				// write each table row to file
				for (int i = 0; i < tableRows.size(); i++) {
					// write row header
					String headerText = (String) (rowHeaders.get(i));
					headerText = FileMethods.replaceSpecialChars(headerText);
					headerText = "\"" + headerText + "\"";
					writer.print(headerText);

					// write array of double values
					double[] rowValues = (double[]) (tableRows.get(i));
					for (int j = 0; j < rowValues.length; j++) {
						writer.print("," + Double.toString(rowValues[j]));
					}
					writer.println();
				}

				writer.close();
				hasChanged = false;
			} catch (Exception ex) {
				throw new Exception("Error writing to table file [" + tableFile.getAbsolutePath() + "].");
			}
		}
	}

	/**
	 * return the number of columns in the table
	 * 
	 * @throws TableException
	 *             if the table is not open
	 * @throws Exception
	 *             if there are problems accessing the table's contents
	 * @return the number of columns
	 */
	public int getColumnCount() throws Exception {
		if (!isOpen) {
			throw new TableException("Table file [" + tableFile.getAbsolutePath() + "] is not open.");
		}
		return columnHeaders.size();
	}

	/**
	 * Return the name of a column
	 * 
	 * @return the name of a column
	 * @param columnIndex
	 *            0-based column index
	 * @throws TableException
	 *             if the table is not open
	 * @throws Exception
	 *             if there are problems accessing the table's contents
	 * 
	 * @throws java.lang.IllegalArgumentException
	 *             if the index is out of range
	 */
	public String getColumnName(int columnIndex) throws Exception {
		if (!isOpen) {
			throw new TableException("Table file [" + tableFile.getAbsolutePath() + "] is not open.");
		}

		if (columnIndex < 0 || columnIndex >= columnHeaders.size()) {
			throw new Exception("Invalid column index for table file [" + tableFile.getAbsolutePath() + "].");
		}

		return (String) (columnHeaders.get(columnIndex));
	}

	/**
	 * Set the name for the given column.
	 * 
	 * @param columnIndex
	 *            0-based column index
	 * @param name
	 *            String the name of the row
	 * 
	 * @throws java.lang.IllegalArgumentException
	 *             if an index is out of range
	 * @throws TableException
	 *             if the table is not open
	 * @throws Exception
	 *             if there are problems accessing the table's contents
	 */
	public void setColumnName(int columnIndex, String name) throws Exception {
		if (columnIndex < 0 || columnIndex >= getColumnCount()) {
			throw new IllegalArgumentException("Invalid column index for table file [" + tableFile.getAbsolutePath()
					+ "].");
		}

		columnHeaders.set(columnIndex, name);
		hasChanged = true;
		return;
	}

	/**
	 * return the number of rows in the table
	 * 
	 * @return the number of rows in the table
	 * @throws TableException
	 *             if the table is not open
	 * @throws Exception
	 *             if there are problems accessing the table's contents
	 */
	public int getRowCount() throws Exception {
		if (!isOpen) {
			throw new TableException("Table file [" + tableFile.getAbsolutePath() + "] is not open.");
		}
		return tableRows.size();
	}

	/**
	 * Indicate if a value has been stored in the given cell.
	 * 
	 * @param rowIndex
	 *            0-based row index
	 * 
	 * @param columnIndex
	 *            0-based column index
	 * 
	 * @throws java.lang.IllegalArgumentException
	 *             if an index is out of range
	 * @throws TableException
	 *             if the table is not open
	 * @throws Exception
	 *             if there are problems accessing the table's contents
	 */
	public boolean isValueAvailable(int rowIndex, int columnIndex) throws Exception {
		if (rowIndex < 0 || columnIndex < 0 || rowIndex >= getRowCount() || columnIndex >= getColumnCount()) {
			throw new IllegalArgumentException("Invalid index for table file [" + tableFile.getAbsolutePath() + "].");
		}

		// get table row
		double[] rowValues = (double[]) (tableRows.get(rowIndex));

		return !Double.isNaN(rowValues[columnIndex]);
	}

	/**
	 * Wait for a value to become available in the given cell.
	 * 
	 * @param rowIndex
	 *            0-based row index
	 * 
	 * @param columnIndex
	 *            0-based column index
	 * 
	 * @throws java.lang.IllegalArgumentException
	 *             if an index is out of range
	 * @throws java.lang.InterruptedException
	 *             if the thread is interrupted while waiting
	 * @throws TableException
	 *             if the table is not open
	 * @throws Exception
	 *             if there are problems accessing the table's contents
	 */
	public void waitForDataAvailable(int rowIndex, int columnIndex) throws Exception {
		if (rowIndex < 0 || columnIndex < 0 || rowIndex >= getRowCount() || columnIndex >= getColumnCount()) {
			throw new IllegalArgumentException("Invalid index for table file [" + tableFile.getAbsolutePath() + "].");
		}
		return;
	}

	/**
	 * Return the number at the given row and column. The number might be Double.NaN.
	 * 
	 * @param rowIndex
	 *            0-based row index
	 * 
	 * @param columnIndex
	 *            0-based column index
	 * 
	 * @throws java.lang.IllegalArgumentException
	 *             if an index is out of range
	 * @throws TableException
	 *             if the table is not open or if the column does not contain a <code>double</code> (for derived table
	 *             classes)
	 * @throws Exception
	 *             if there are problems accessing the table's contents
	 */
	public double getDoubleAt(int rowIndex, int columnIndex) throws Exception {
		if (rowIndex < 0 || columnIndex < 0 || rowIndex >= getRowCount() || columnIndex >= getColumnCount()) {
			throw new IllegalArgumentException("Invalid index for table file [" + tableFile.getAbsolutePath() + "].");
		}

		// get table row
		double[] rowValues = (double[]) (tableRows.get(rowIndex));

		// return value at columnIndex
		return rowValues[columnIndex];
	}

	/**
	 * Return the numbers in the given row, one for each column in the table. The numbers might be Double.NaN.
	 * 
	 * @param rowIndex
	 *            0-based row index
	 * 
	 * @throws java.lang.IllegalArgumentException
	 *             if an index is out of range
	 * @throws TableException
	 *             if the table is not open or if all columns do not contain a <code>double</code> (for derived table
	 *             classes)
	 * @throws Exception
	 *             if there are problems accessing the table's contents
	 */
	public double[] getDoubles(int rowIndex) throws Exception {
		if (rowIndex < 0 || rowIndex >= getRowCount()) {
			throw new IllegalArgumentException("Invalid index for table file [" + tableFile.getAbsolutePath() + "].");
		}

		// get table row
		double[] rowValues = (double[]) (tableRows.get(rowIndex));

		// return values in row
		return rowValues;
	}

	/**
	 * Indicate if the given cell in the table can be changed.
	 * 
	 * @param rowIndex
	 *            0-based row index
	 * 
	 * @param columnIndex
	 *            0-based column index
	 * 
	 * @throws java.lang.IllegalArgumentException
	 *             if an index is out of range
	 * @throws TableException
	 *             if the table is not open
	 * @throws Exception
	 *             if there are problems accessing the table's contents
	 */
	public boolean isCellEditable(int rowIndex, int columnIndex) throws Exception {
		if (rowIndex < 0 || columnIndex < 0 || rowIndex >= getRowCount() || columnIndex >= getColumnCount()) {
			throw new IllegalArgumentException("Invalid index for table file [" + tableFile.getAbsolutePath() + "].");
		}

		// return values in row
		return true;
	}

	/**
	 * Return the name for the given row, which might be null if the name has not been set.
	 * 
	 * @param rowIndex
	 *            0-based row index
	 * 
	 * @throws java.lang.IllegalArgumentException
	 *             if an index is out of range
	 * @throws Exception
	 *             if there are problems accessing the table's contents
	 */
	public String getRowName(int rowIndex) throws Exception {
		if (rowIndex < 0 || rowIndex >= this.getRowCount()) {
			throw new IllegalArgumentException("Invalid row index for table file [" + tableFile.getAbsolutePath()
					+ "].");
		}

		return (String) (rowHeaders.get(rowIndex));
	}

	/**
	 * Set the name for the given row.
	 * 
	 * @param rowIndex
	 *            0-based row index
	 * @param name
	 *            String the name of the row
	 * 
	 * @throws java.lang.IllegalArgumentException
	 *             if an index is out of range
	 * @throws TableException
	 *             if the table is not open
	 * @throws Exception
	 *             if there are problems accessing the table's contents
	 */
	public void setRowName(int rowIndex, String name) throws Exception {
		if (rowIndex < 0 || rowIndex >= this.getRowCount()) {
			throw new IllegalArgumentException("Invalid row index for table file [" + tableFile.getAbsolutePath()
					+ "].");
		}

		rowHeaders.set(rowIndex, name);
		hasChanged = true;
		return;
	}

	/**
	 * Return the 0-based index of the row with the given name or a negative number if the name was not found. Name
	 * comparisons will respect case.
	 * 
	 * @throws TableException
	 *             if the table is not open
	 * @throws Exception
	 *             if there are problems accessing the table's contents
	 */
	public int findRowByName(String rowName) throws Exception {
		if (!isOpen) {
			throw new TableException("Table file [" + tableFile.getAbsolutePath() + "] is not open.");
		}

		return rowHeaders.indexOf(rowName);
	}

	/**
	 * Return the 0-based index of the column with the given name or a negative number if the name was not found. Name
	 * comparisons will respect case.
	 * 
	 * @throws TableException
	 *             if the table is not open
	 * @throws Exception
	 *             if there are problems accessing the table's contents
	 */
	public int findColumnByName(String colName) throws Exception {
		if (!isOpen) {
			throw new TableException("Table file [" + tableFile.getAbsolutePath() + "] is not open.");
		}

		return columnHeaders.indexOf(colName);
	}

	/**
	 * Put a value in the table. Any values previously stored in other rows will be written to persistent storage.
	 * 
	 * @param rowIndex
	 *            0-based row index
	 * 
	 * @param columnIndex
	 *            0-based column index
	 * 
	 * @param aValue
	 *            double value for the cell
	 * 
	 * @throws java.lang.IllegalArgumentException
	 *             if an index is out of range
	 * @throws TableException
	 *             if the table is not open
	 * @throws Exception
	 *             if there are problems accessing the table's contents
	 */
	public void setDoubleAt(int rowIndex, int columnIndex, double aValue) throws Exception {
		if (rowIndex < 0 || columnIndex < 0 || rowIndex >= getRowCount() || columnIndex >= getColumnCount()) {
			throw new IllegalArgumentException("Invalid index for table file [" + tableFile.getAbsolutePath() + "].");
		}

		if (!isCellEditable(rowIndex, columnIndex)) {
			throw new TableException("Cell at [" + rowIndex + "," + columnIndex + "] is not editable.");
		}

		// get table row
		double[] rowValues = (double[]) (tableRows.get(rowIndex));

		// set value at columnIndex
		rowValues[columnIndex] = aValue;
		hasChanged = true;
	}

	/**
	 * Fill a row in the table. Any values previously stored in other rows will be written to persistent storage.
	 * 
	 * @param rowIndex
	 *            0-based row index
	 * 
	 * @param values
	 *            double[] containing a values for row
	 * 
	 * @throws java.lang.IllegalArgumentException
	 *             if an index is out of range
	 * @throws TableException
	 *             if the table is not open
	 * @throws Exception
	 *             if there are problems accessing the table's contents
	 */
	public void setDoubles(int rowIndex, double values[]) throws Exception {
		if (rowIndex < 0 || rowIndex >= getRowCount()) {
			throw new IllegalArgumentException("Invalid index for table file [" + tableFile.getAbsolutePath() + "].");
		}

		// get table row
		double[] rowValues = (double[]) (tableRows.get(rowIndex));

		int ncolumns = Math.min(rowValues.length, values.length);

		// set values
		for (int i = 0; i < ncolumns; i++) {
			if (!isCellEditable(rowIndex, i)) {
				throw new TableException("Cell at [" + rowIndex + "," + i + "] is not editable.");
			}

			rowValues[i] = values[i];
			hasChanged = true;
		}

		// set row with new values
		tableRows.set(rowIndex, rowValues);
	}

	/**
	 * insert a row in the table.
	 * 
	 * @param rowIndex
	 *            0-based row index
	 * 
	 * @param header
	 *            String header for row
	 * @param values
	 *            double[] containing a values for row
	 * 
	 * @throws java.lang.IllegalArgumentException
	 *             if an index is out of range
	 * @throws TableException
	 *             if the table is not open
	 * @throws Exception
	 *             if there are problems accessing the table's contents
	 */
	public void insertRow(int rowIndex, String header, double values[]) throws Exception {
		if (rowIndex < 0 || rowIndex >= getRowCount()) {
			throw new IllegalArgumentException("Invalid index for table file [" + tableFile.getAbsolutePath() + "].");
		}

		// insert header if rowHeaders List
		rowHeaders.add(rowIndex, header);

		// build double values for row
		double[] rowValues = new double[getColumnCount()];
		for (int i = 0; i < rowValues.length; i++) {
			if (i < values.length) {
				rowValues[i] = values[i];
			} else {
				rowValues[i] = Double.NaN;
			}
		}

		// insert row in table
		tableRows.add(rowIndex, rowValues);
	}

	/**
	 * append a row in the table.
	 * 
	 * @param header
	 *            String header for row
	 * @param values
	 *            double[] containing a values for row
	 * 
	 * @throws java.lang.IllegalArgumentException
	 *             if an index is out of range
	 * @throws TableException
	 *             if the table is not open
	 * @throws Exception
	 *             if there are problems accessing the table's contents
	 */
	public void appendRow(String header, double values[]) throws Exception {
		if (!isOpen) {
			throw new TableException("Table file [" + tableFile.getAbsolutePath() + "] is not open.");
		}

		// append header if rowHeaders List
		rowHeaders.add(header);

		// build double values for row
		double[] rowValues = new double[getColumnCount()];
		for (int i = 0; i < rowValues.length; i++) {
			if (i < values.length) {
				rowValues[i] = values[i];
			} else {
				rowValues[i] = Double.NaN;
			}
		}

		// append row in table
		tableRows.add(rowValues);
	}

}