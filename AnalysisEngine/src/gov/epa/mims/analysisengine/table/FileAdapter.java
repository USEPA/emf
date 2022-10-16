package gov.epa.mims.analysisengine.table;

import gov.epa.mims.analysisengine.table.io.DAVEFileReader;
import gov.epa.mims.analysisengine.table.io.FileImportGUI;
import gov.epa.mims.analysisengine.table.io.MonteCarloFileReader;
import gov.epa.mims.analysisengine.table.io.SMKReportFileReader;
import gov.epa.mims.analysisengine.table.io.TRIMResultFileReader;
import gov.epa.mims.analysisengine.table.io.TRIMSensitivityFileReader;

import java.io.File;
import java.util.HashMap;
import java.util.Vector;

/**
 * <p>
 * Title:FileAdapter.java
 * </p>
 * <p>
 * Description: A class to create adapter which will have information about the file name, file type, delimiter, no of
 * column header rows for the file
 * </p>
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * <p>
 * Company: CEP, UNC-Chapel Hill
 * </p>
 * 
 * @author Parthee Partheepan
 * @version $Id: FileAdapter.java,v 1.6 2007/05/17 16:01:50 qunhe Exp $
 */
public class FileAdapter {
	/**
	 * file names to be imported
	 */
	String[] fileNames;

	/** fileType to be imported */
	String fileType;

	/** delimiter for the corresponding fileType */
	String delimiter;

	/** no of column header for the corresponding fileType */
	int noOfColumnHeader;

	/**
	 * use to specify the start position to create the tab name from the file name
	 */
	int startPos = 1;

	/**
	 * use to specify the end position to create the tab name from the file name
	 */
	int endPos = 40;

	/**
	 * configuration file
	 */
	String configFile = null;

	/**
	 * should the table configuration be ignored
	 */
	boolean ignoreTableConfig = false;

	/**
	 * should the plots be displayed
	 */
	boolean showPlots = false;

	/**
	 * output directory to which plots should be saved
	 */
	String outputDir = null;

	/**
	 * default output format of the plots
	 */
	int plotFmt = SaveToDialog.PDF;

	/** hashmap to store delimiter information */
	private static HashMap fileDelimiter;

	/** hashmap to store no of column header information */
	private static HashMap fileColumnHeaderInfo;

	static {
		fileDelimiter = new HashMap();
		fileDelimiter.put(FileImportGUI.SMOKE_REPORT_FILE, SMKReportFileReader.DELIMITER);
		fileDelimiter.put(FileImportGUI.TRIM_RESULTS_FILE, TRIMResultFileReader.DELIMITER);
		fileDelimiter.put(FileImportGUI.TRIM_SENSITIVITY_FILE, TRIMSensitivityFileReader.DELIMITER);
		fileDelimiter.put(FileImportGUI.MONTE_CARLO_FILE, MonteCarloFileReader.DELIMITER);
		fileDelimiter.put(FileImportGUI.COSU_FILE, COSUAdapter.DELIMITER);
		fileDelimiter.put(FileImportGUI.ARFF_FILE, ",");
		fileDelimiter.put(FileImportGUI.COMMA_DELIMITED_FILE, ",");
		fileDelimiter.put(FileImportGUI.TAB_DELIMITED_FILE, "\t");
		fileDelimiter.put(FileImportGUI.DAVE_OUTPUT_FILE, null);
		fileDelimiter.put(FileImportGUI.GENERIC_FILE, null);
		fileDelimiter.put(FileImportGUI.FIXED_WIDTH_FILE, null);

		fileColumnHeaderInfo = new HashMap();
		fileColumnHeaderInfo.put(FileImportGUI.SMOKE_REPORT_FILE, Integer.valueOf(
				SMKReportFileReader.NO_OF_COLUMN_HEADER_ROWS));
		fileColumnHeaderInfo.put(FileImportGUI.TRIM_RESULTS_FILE, Integer.valueOf(
				TRIMResultFileReader.NO_OF_COLUMN_HEADER_ROWS));
		fileColumnHeaderInfo.put(FileImportGUI.TRIM_SENSITIVITY_FILE, Integer.valueOf(
				TRIMSensitivityFileReader.NO_OF_COLUMN_HEADER_ROWS));
		fileColumnHeaderInfo.put(FileImportGUI.MONTE_CARLO_FILE, Integer.valueOf(
				MonteCarloFileReader.NO_OF_COLUMN_HEADER_ROWS));
		fileColumnHeaderInfo.put(FileImportGUI.COSU_FILE, Integer.valueOf(COSUAdapter.NO_OF_COLUMN_HEADER_ROWS));
		fileColumnHeaderInfo.put(FileImportGUI.DAVE_OUTPUT_FILE, Integer.valueOf(DAVEFileReader.NO_OF_COLUMN_HEADER_ROWS));
		fileColumnHeaderInfo.put(FileImportGUI.ARFF_FILE, null);
		fileColumnHeaderInfo.put(FileImportGUI.GENERIC_FILE, null);
		fileColumnHeaderInfo.put(FileImportGUI.FIXED_WIDTH_FILE, null);
		fileColumnHeaderInfo.put(FileImportGUI.COMMA_DELIMITED_FILE, null);
		fileColumnHeaderInfo.put(FileImportGUI.TAB_DELIMITED_FILE, null);
	}// static

	/** Creates a new instance of FileAdapter */
	public FileAdapter(String[] args) throws Exception {
		processArg(args);
	}

	/**
	 * a method to process the arguments
	 * 
	 * @throws Exception
	 *             if and when arg. are spefied inappropriately
	 */
	private void processArg(String[] args) throws Exception {
		Vector tempFileNames = new Vector();
		// String tempFileName = null;
		String tempFileType = null;
		String tempDelimiter = null;
		int tempNoOfColumnHeaderRows = -1;

		if (args.length == 0) {
			return; // open a java application with out any files
		}

		String currentArg = null;

		// Note: i may be changed inside of the loop.
		for (int i = 0; i < args.length; i++) {
			currentArg = args[i];
			if (currentArg.equalsIgnoreCase("-fileNames")) {
				i++;
				for (; i < args.length; i++) {
					// checking for argument with '-'. Since this indicates end of file Name specification
					if (args[i].startsWith("-")) {
						i--; // to go back one step so that the argument with '-' will
						// be caught one of other loops
						break;
					}// if(args[i].indexOf('-') > -1)
					tempFileNames.add(args[i]);
					// System.out.println(args[i]);
				}// for(i)
			}// if
			else if (currentArg.equalsIgnoreCase("-fileType")) {
				i++;
				tempFileType = args[i];
			}// else if(
			else if (currentArg.equalsIgnoreCase("-delimiter")) {
				i++;
				tempDelimiter = args[i];
				int length = tempDelimiter.trim().length();
				if (length >= 2) {
					throw new Exception("Delimiter should have one character but has " + length + " characters ");
				}// ifs
			}// else if
			else if (currentArg.equalsIgnoreCase("-hRows")) {
				i++;
				try {
					tempNoOfColumnHeaderRows = Integer.parseInt(args[i]);
					checkValue(tempNoOfColumnHeaderRows, "No of header rows");// whether value is >=1
				}// try
				catch (NumberFormatException e) {
					throw new NumberFormatException("Could not convert no of column header " + args[i]
							+ " to an integer");
				}// catch
			}// else if
			else if (currentArg.equalsIgnoreCase("-startPos")) {
				i++;
				try {
					int tempStartPos = Integer.parseInt(args[i]);
					checkValue(tempStartPos, "start position"); // whether value is >=1
					startPos = tempStartPos;
				}// try
				catch (NumberFormatException e) {
					throw new NumberFormatException("Could not convert startPos " + args[i] + " to an integer");
				}// catch
			}// else if
			else if (currentArg.equalsIgnoreCase("-endPos")) {
				i++;
				try {
					int tempEndPos = Integer.parseInt(args[i]);
					checkValue(tempEndPos, "end position"); // whether value is >=1
					endPos = tempEndPos;
				}// try
				catch (NumberFormatException e) {
					throw new NumberFormatException("Could not convert endPos " + args[i] + " to an integer");
				}// catch
			}// else if
			else if (currentArg.equalsIgnoreCase("-outputDir")) {
				i++;
				outputDir = args[i];
				if (!checkDirectory(outputDir)) {
					System.out.println("Invalid directory Name " + outputDir + " \n");
					outputDir = null;
				}
			} else if (currentArg.equalsIgnoreCase("-ConfigFile")) {
				i++;
				configFile = args[i];
				if (!checkFile(configFile)) {
					System.out.println("Configuration file " + configFile + "  does not" + " exist\n");
					configFile = null;
				}
			} else if (currentArg.equalsIgnoreCase("-ignoreTableConfig")) {
				ignoreTableConfig = true;
			} else if (currentArg.equalsIgnoreCase("-defaultPlotFmt")) {
				i++;
				String plot_fmt = args[i];
				plotFmt = getFormat(plot_fmt);
			} else if (currentArg.equalsIgnoreCase("-ShowPlots")) {
				showPlots = true;
			}
		}// for(i)

		// check whether user specified at least one file
		if (tempFileNames.size() == 0) {
			throw new Exception("You have to specify at least one file name");
		}// if
		// check whether file type is specified

		if (tempFileType == null) {
			throw new Exception("You have to specify the file type for the file names ");
		}// if

		// check & set this class variables
		String[] a = {};
		checkFileNames((String[]) tempFileNames.toArray(a)); // check whether file exist
		checkFileType(tempFileType); // check whether file type is correct
		checkDelimiter(tempDelimiter);
		checkNoOfColumnHeaderRows(tempNoOfColumnHeaderRows);

	}// processArg()

	private int getFormat(String format) {
		if (format.equalsIgnoreCase("PS"))
			return SaveToDialog.PS;
		if (format.equalsIgnoreCase("PDF"))
			return SaveToDialog.PDF;
		if (format.equalsIgnoreCase("JPEG"))
			return SaveToDialog.JPEG;
		if (format.equalsIgnoreCase("PNG"))
			return SaveToDialog.PNG;
		if (format.equalsIgnoreCase("PTX"))
			return SaveToDialog.PTX;
		return SaveToDialog.PDF;
	}

	public static void printUsage() {
		System.out.println("\n\nUsage: java " + TableApp.class + "\n\t -fileNames <name> \n" + "\t -fileType <type> \n"
				+ "\t -delimiter <delimiter> \n"
				+ "\t -hRows <noOfRows> \n\t -startPos <startidx> \n\t -endPos <endidx>\n"
				+ "\t -configFile <path> \n\t -ignoreTableConfig \n\t -defaultPlotFmt "
				+ "<PS|PDF|JPEG|PNG|PTX> \n\t -outputDir <directory> \n\t -ShowPlots\n");
		System.out.println();
		System.out.println("Detail Description:");
		System.out.println("===================");
		System.out.println("<name>                  = Name of the file");
		System.out.println("<type>                  = One of these types: " + FileImportGUI.COMMA_DELIMITED_FILE + ", "
				+ FileImportGUI.TAB_DELIMITED_FILE + ", " + FileImportGUI.SMOKE_REPORT_FILE + ", "
				+ FileImportGUI.TRIM_RESULTS_FILE + ", " + FileImportGUI.TRIM_SENSITIVITY_FILE + ", "
				+ FileImportGUI.MONTE_CARLO_FILE + ", " + FileImportGUI.COSU_FILE + ", "
				+ FileImportGUI.FIXED_WIDTH_FILE + ", " + FileImportGUI.ARFF_FILE + ", "
				+ FileImportGUI.DAVE_OUTPUT_FILE);
		System.out.println("<delimiter>             = A single character; Required only for "
				+ "DAVE Output files and Custom delimited files, Optional for SMOKE " + "Report File\n");
		System.out.println("<noOfRows>              = An integer value indicates the number of column "
				+ " header rows in the file including the units rows: Required only for "
				+ "Custom delimited files, Comma Separated Files and Tab Delimited " + " Files\n");
		System.out.println("<path>                  = complete path of the " + "Configuration File\n");
		System.out.println("-ignoreTableConfig    specify when the plots are to "
				+ "plotted with entire table data without any filter/sort/format " + "applied\n");
		System.out.println("-ShowPlots            to specify that plots to be shown when "
				+ "the Analysis Engine GUI is brought up with the Config File specified\n");
		System.out.println("<directory>             = destination directory to which the plots" + " are to be saved\n");
	}// printUsage()

	/**
	 * a helper method to check whether value is >1
	 */
	private void checkValue(int value, String message) throws Exception {
		if (value < 1) {
			throw new Exception("The value specified for " + message + " should be >= 1");
		}
	}

	/**
	 * Check the file names whether file is exist
	 */
	private void checkFileNames(String[] names) throws Exception {
		for (int i = 0; i < names.length; i++) {
			File file = new File(names[i]);
			if (!file.exists()) {
				throw new Exception("The file " + names[i] + " is not exist");
			}// if(!file.exists())
		}// for(i)
		fileNames = names;
	}// checkFileName()

	private boolean checkFile(String name) {
		File file = new File(name);
		if (!file.exists()) {
			return false;
		}// if(!file.exists())
		return true;
	}// checkFile()

	private boolean checkDirectory(String name) {
		if (checkFile(name)) {
			if (new File(name).isDirectory())
				return true;
			return false;
		}

		if (new File(name).mkdir())
			return true;
		return false;

	}

	/**
	 * check the file Type whether the file type specified is avaliable file type
	 */
	private void checkFileType(String type) throws Exception {
		if (type.equalsIgnoreCase(FileImportGUI.SMOKE_REPORT_FILE)) {
			this.fileType = FileImportGUI.SMOKE_REPORT_FILE;
		} else if (type.equalsIgnoreCase(FileImportGUI.ARFF_FILE)) {
			this.fileType = FileImportGUI.ARFF_FILE;
		} else if (type.equalsIgnoreCase(FileImportGUI.TRIM_RESULTS_FILE)) {
			this.fileType = FileImportGUI.TRIM_RESULTS_FILE;
		} else if (type.equalsIgnoreCase(FileImportGUI.TRIM_SENSITIVITY_FILE)) {
			this.fileType = FileImportGUI.TRIM_SENSITIVITY_FILE;
		} else if (type.equalsIgnoreCase(FileImportGUI.MONTE_CARLO_FILE)) {
			this.fileType = FileImportGUI.MONTE_CARLO_FILE;
		} else if (type.equalsIgnoreCase(FileImportGUI.COSU_FILE)) {
			this.fileType = FileImportGUI.COSU_FILE;
		} else if (type.equalsIgnoreCase(FileImportGUI.DAVE_OUTPUT_FILE)) {
			this.fileType = FileImportGUI.DAVE_OUTPUT_FILE;
		} else if (type.equalsIgnoreCase(FileImportGUI.GENERIC_FILE)) {
			this.fileType = FileImportGUI.GENERIC_FILE;
		} else if (type.equalsIgnoreCase(FileImportGUI.COMMA_DELIMITED_FILE)) {
			this.fileType = FileImportGUI.COMMA_DELIMITED_FILE;
		} else if (type.equalsIgnoreCase(FileImportGUI.TAB_DELIMITED_FILE)) {
			this.fileType = FileImportGUI.TAB_DELIMITED_FILE;
		} else if (type.equalsIgnoreCase(FileImportGUI.FIXED_WIDTH_FILE)) {
			this.fileType = FileImportGUI.FIXED_WIDTH_FILE;
		} else {
			throw new Exception("Unrecognized file type '" + type + "'.");
		}// if
	}// checkFileType()

	/**
	 * check whether delimiter is required and if required whether is available
	 */
	private void checkDelimiter(String delimiter) throws Exception {
		String tempDelimiter = (String) FileAdapter.fileDelimiter.get(fileType);

		if (fileType.equals(FileImportGUI.SMOKE_REPORT_FILE) && delimiter != null) {
			this.delimiter = delimiter;
			return;
		}// if

		if (fileType.equals(FileImportGUI.FIXED_WIDTH_FILE)) {
			return;
		}

		if (tempDelimiter != null) {
			this.delimiter = tempDelimiter;
		} else if (delimiter != null) {
			this.delimiter = delimiter;
		} else // (tempDelimiter == null &&,|| delimiter == null)
		{
			throw new Exception("For the '" + fileType + "' delimiter should be specified");
		}// if
	}// checkDelimiter()

	/**
	 * check whether no of column header should be specified
	 */
	private void checkNoOfColumnHeaderRows(int noOfColumnHeaderRows) throws Exception {
		Integer tempNoOfColumnHeaderRows = (Integer) FileAdapter.fileColumnHeaderInfo.get(fileType);

		if (fileType.equals(FileImportGUI.FIXED_WIDTH_FILE)) {
			return;
		}

		if (tempNoOfColumnHeaderRows != null) {
			this.noOfColumnHeader = tempNoOfColumnHeaderRows.intValue();
		} else if (noOfColumnHeaderRows >= 0) {
			this.noOfColumnHeader = noOfColumnHeaderRows;
		}// else if
		else // tempNoOfColumnHeaderRows == null &&,||noOfColumnHeaderRows != -1
		{
			throw new Exception("The file type '" + fileType + "' requires noOfRows to be specified.");
		}// if
	}// checkNoOfColumnHeaderRows()

}
