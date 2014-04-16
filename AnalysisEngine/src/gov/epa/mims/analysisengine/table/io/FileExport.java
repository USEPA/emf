package gov.epa.mims.analysisengine.table.io;

import gov.epa.mims.analysisengine.gui.DefaultUserInteractor;
import gov.epa.mims.analysisengine.gui.UserInteractor;
import gov.epa.mims.analysisengine.table.format.SignificantDigitsFormat;

import java.io.*;
import java.util.ArrayList;
import javax.swing.table.TableModel;

/**
 * FileExport.java Use to write a file , given a file name and delimiter and the
 * data for the file
 * 
 * Created on April 5, 2004, 11:59 AM
 * 
 * @author parthee
 * @version $Id: FileExport.java,v 1.4 2007/05/11 19:40:27 qunhe Exp $
 */
public class FileExport {

	/** export file name: absolute fileName */
	private String fileName;

	/** delimiter for the file */
	private String delimiter;

	// /** header for the file */
	// private String fileHeader;
	//   
	// /** column row headers */
	// private String [] columnRowHeaders;
	//   
	// /** column header data */
	// private String[][] columnHeaders;
	//   
	// /** data for the table */
	// private ArrayList fileData;
	//   
	// /** footer info for the file */
	// private String fileFooter;

	/** printer writer for the file */
	private PrintWriter printer;

	/** Creates a new instance of FileExport */
	public FileExport(String fileName, String delimiter) {
		this.fileName = fileName;
		this.delimiter = delimiter;
	}

	/**
	 * write the file
	 * 
	 * @param fileHeader
	 * @param columnRowHeaders
	 * @param columnHeaders
	 * @param fileData
	 * @param fileFooter
	 */
	public void writeFile(String fileHeader, String[] columnRowHeaders,
			String[][] columnHeaders, ArrayList fileData, String fileFooter)
			throws Exception {
		try {
			printer = new PrintWriter(new BufferedWriter(new FileWriter(
					fileName)));
		} catch (Exception e) {
			DefaultUserInteractor.get().notify(null,
					"Error opening output file",
					"Could not open file for export: " + fileName,
					UserInteractor.ERROR);
		}

		printHeaderOrFooter(fileHeader);
		printColumnHeaderAndColumnRowHeader(columnRowHeaders, columnHeaders);
		printData(fileData);
		printHeaderOrFooter(fileFooter);
		printer.flush();
	}

	/**
	 * write the file
	 * 
	 * @param fileHeader
	 * @param columnHeaders
	 * @param fileData
	 * @param fileFooter
	 */
	public void writeFile(String fileHeader, String[][] columnHeaders,
			ArrayList fileData, String fileFooter) {
		try {
			printer = new PrintWriter(new BufferedWriter(new FileWriter(
					fileName)));
		} catch (Exception e) {
			DefaultUserInteractor.get().notify(null,
					"Error opening output file",
					"Could not open file for export: " + fileName,
					UserInteractor.ERROR);
		}

		printHeaderOrFooter(fileHeader);
		printColumnHeader(columnHeaders);
		printData(fileData);
		printHeaderOrFooter(fileFooter);
		printer.flush();
		printer.close();
	}

	/**
	 * write the file
	 * 
	 * @param fileHeader
	 * @param columnHeaders
	 * @param fileData
	 * @param fileFooter
	 */
	public void writeFile(String[][] columnHeaders, TableModel tableModel) {
		try {
			printer = new PrintWriter(new BufferedWriter(new FileWriter(
					fileName)));
		} catch (Exception e) {
			DefaultUserInteractor.get().notify(null,
					"Error opening output file",
					"Could not open file for export: " + fileName,
					UserInteractor.ERROR);
		}
		printColumnHeader(columnHeaders);
		printData(tableModel);
		printer.flush();
		printer.close();
	}

	private void printHeaderOrFooter(String fileInfo) {
		if (fileInfo == null) {
			return;
		}
		printAWordPlusEndOfLine(fileInfo);

	}

	/**
	 * helper method to print the data column header data
	 * columnRowHeaders.length <= columnHeaders.length
	 */
	private void printColumnHeaderAndColumnRowHeader(String[] columnRowHeaders,
			String[][] columnHeaders) throws Exception {
		//TODO:
	}

	/**
	 * helper method to print the data column header data WARNING: Assumed
	 * columnHeader!= null and elements of this 2D array also not null and
	 * columnHeaders.length == fileData.size() ie column sizes are equal
	 * 
	 */
	private void printColumnHeader(String[][] columnHeaders) {
		if (columnHeaders == null)
			return;
		for (int i = 0; i < columnHeaders.length; i++) {
			for (int j = 0; j < columnHeaders[0].length - 1; j++) {
				printAWordPlusDelimiter(columnHeaders[i][j]);
			}// for(j)
			printAWordPlusEndOfLine(columnHeaders[i][columnHeaders[0].length - 1]);
		}// for(i)
	}

	/**
	 * helper method to print the data
	 */
	private void printData(ArrayList fileData) {
		for (int i = 0; i < fileData.size(); i++) {
			ArrayList rowData = (ArrayList) fileData.get(i);
			int size = rowData.size();
			for (int j = 0; j < size - 1; j++) {
				printAWordPlusDelimiter(rowData.get(j).toString());
			}// for(j)
			printAWordPlusEndOfLine(rowData.get(size - 1).toString());
		}// for(i)
	}

	/**
	 * helper method to print the data
	 */
	private void printData(TableModel tableModel) {
		int rowCount = tableModel.getRowCount();
		int colCount = tableModel.getColumnCount();
		for (int i = 0; i < rowCount; i++) {
			for (int j = 0; j < colCount - 1; j++) {
				Object obj = tableModel.getValueAt(i, j);
				boolean flag = tableModel.getColumnClass(j)
						.equals(Double.class)
						&& ((Double) obj).isNaN();
				String objString = (flag) ? SignificantDigitsFormat.NaN_FORMAT
						: obj.toString();
				printAWordPlusDelimiter(objString);
			}// for(j)
			Object obj = tableModel.getValueAt(i, colCount - 1);
			boolean flag = tableModel.getColumnClass(colCount - 1).equals(
					Double.class)
					&& ((Double) obj).isNaN();
			String objString = (flag) ? SignificantDigitsFormat.NaN_FORMAT
					: obj.toString();
			printAWordPlusEndOfLine(objString);
		}// for(i)
	}

	/**
	 * helper method to print the data with quotes and followed by delimiter
	 */
	private void printAWordPlusQuoteAndDelimiter(String aWord) {
		printer.print("\"");
		printer.print(aWord);
		printer.print("\"");
		printer.print(delimiter);

	}

	/**
	 * helper method to print the data without quotes and followed by delimiter
	 */
	private void printAWordPlusDelimiter(String aWord) {
		if (aWord.indexOf(delimiter) > -1)
			printAWordPlusQuoteAndDelimiter(aWord);
		else {
//			 TODO:Remove this code when empty strings is exported sucessfully
			if (aWord.trim().length() == 0) {
				aWord = "0.0";
			}
			printer.print(aWord);
			printer.print(delimiter);
		}
	}

	/**
	 * helper method to print the data without quotes and followed by End of
	 * Line
	 */
	private void printAWordPlusEndOfLine(String aWord) {
		if (aWord.trim().length() == 0)
			return;
		
		printer.println(aWord);
	}

}
