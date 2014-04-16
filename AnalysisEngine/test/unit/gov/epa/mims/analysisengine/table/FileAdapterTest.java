package gov.epa.mims.analysisengine.table;

import gov.epa.mims.analysisengine.table.io.FileImportGUI;
import gov.epa.mims.analysisengine.table.io.SMKReportFileReader;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class FileAdapterTest extends TestCase {
	static String dataFolder;

	static String fileName1;

	static String fileName2;

	static String fileName3;

	static String wrongFileName;
	static {
		dataFolder = "test/data/test/";
		fileName1 = dataFolder + "file1.rpt";
		fileName2 = dataFolder + "file2.rpt";
		fileName3 = dataFolder + "file3.rpt";
		wrongFileName = dataFolder + "file.rpt";
	}

	String _fileNames = "-fileNames";

	String _fileType = "-fileType";

	String _delimiter = "-delimiter";

	String _hRows = "-hRows";

	String _startPos = "-startPos";

	String _endPos = "-endPos";

	String delimiter = "|";

	String noOfColumnHeaderRows = "2";

	String startPos = "5";

	String endPos = "6";

	String wrongFileType = "WRONG_TYPE";

	String notAnInteger = "Not a Integer";

	String lessThanOne = "-1000232";

	/** Correct arguments a fileName & a file Type */
	String[] fNFt = { _fileNames, FileAdapterTest.fileName1, _fileType, FileImportGUI.SMOKE_REPORT_FILE };

	/** Correct arguments multiple fileNames & a file Type */
	String[] mulFnFt = { _fileNames, FileAdapterTest.fileName1, FileAdapterTest.fileName2, FileAdapterTest.fileName3,
			_fileType, FileImportGUI.SMOKE_REPORT_FILE };

	/** Correct arguments multiple fileNames, a file Type, a delimiter */
	String[] mulFnFtDel = { _fileNames, FileAdapterTest.fileName1, FileAdapterTest.fileName2,
			FileAdapterTest.fileName3, _fileType, FileImportGUI.SMOKE_REPORT_FILE, _delimiter, delimiter };

	/**
	 * Correct arguments multiple fileNames, a file Type, a delimiter, noOfColumnHeaderRows, startPosition and a
	 * endPosition
	 */
	String[] argsAll = { _fileNames, FileAdapterTest.fileName1, FileAdapterTest.fileName2, FileAdapterTest.fileName3,
			_fileType, FileImportGUI.GENERIC_FILE, _delimiter, delimiter, _hRows, noOfColumnHeaderRows, _startPos,
			startPos, _endPos, endPos };

	/**
	 * CORRECT BUT ORDER CHANGED: multiple fileNames, a file Type, a delimiter, noOfColumnHeaderRows, startPosition and
	 * a endPosition
	 */
	String[] changedOrderAllArgs = { _endPos, endPos, _startPos, startPos, _delimiter, delimiter, _fileType,
			FileImportGUI.GENERIC_FILE, _hRows, noOfColumnHeaderRows, _fileNames, FileAdapterTest.fileName1,
			FileAdapterTest.fileName2, FileAdapterTest.fileName3 };

	/**
	 * No file name is specified This can happen in following cases i) '_fileNames' is incorrectly spelled ii) There is
	 * no string next to '_fileNames' iii) completely missing both '_fileNames' and the file names
	 */
	String[] fNMissing = { _fileType, FileImportGUI.GENERIC_FILE, _delimiter, delimiter, _hRows, noOfColumnHeaderRows,
			_startPos, startPos, _endPos, endPos };

	/**
	 * Correct specification but one of the file specified not exist in the specified location
	 */
	String[] fNNotExist = { _endPos, endPos, _startPos, startPos, _delimiter, delimiter, _fileType,
			FileImportGUI.GENERIC_FILE, _hRows, noOfColumnHeaderRows, _fileNames, FileAdapterTest.fileName1,
			FileAdapterTest.wrongFileName, FileAdapterTest.fileName3 };

	/**
	 * No file type is specified This can happen in following cases i) '_fileType' is incorrectly spelled ii) There is
	 * no string next to '_fileType' iii) completely missing both '_fileType' and the file type
	 */
	String[] fTMissing = { _fileNames, FileAdapterTest.fileName1, FileAdapterTest.fileName2, FileAdapterTest.fileName3,
			_delimiter, delimiter, _hRows, noOfColumnHeaderRows, _startPos, startPos, _endPos, endPos };

	/**
	 * Correct specification but one of the file specified not exist in the specified location
	 */
	String[] fTNotExist = { _endPos, endPos, _startPos, startPos, _delimiter, delimiter, _fileType, wrongFileType,
			_hRows, noOfColumnHeaderRows, _fileNames, FileAdapterTest.fileName1, FileAdapterTest.fileName2,
			FileAdapterTest.fileName3 };

	/**
	 * Delimiter is not specified when required. This can happen in following cases i) '_delimiter' is incorrectly
	 * spelled ii) There is no string next to '_delimiter' iii) completely missing both '_delimiter' and the delimiter
	 */
	String[] delMissing = { _endPos, endPos, _startPos, startPos, _fileType, FileImportGUI.DAVE_OUTPUT_FILE, _hRows,
			noOfColumnHeaderRows, _fileNames, FileAdapterTest.fileName1, FileAdapterTest.fileName2,
			FileAdapterTest.fileName3 };

	/**
	 * The number for column header rows is not specified when required. This can happen in following cases i) '_hRows'
	 * is incorrectly spelled ii) There is no string next to '_hRows' iii) completely missing both '_hRows' and the
	 * number for header rows
	 */
	String[] hRowsMissing = { _fileNames, FileAdapterTest.fileName1, FileAdapterTest.fileName2,
			FileAdapterTest.fileName3, _fileType, FileImportGUI.GENERIC_FILE, _delimiter, delimiter, _startPos,
			startPos, _endPos, endPos };

	/**
	 * The number of header rows specified cannot be converterd to integer
	 */
	String[] hRowsIsNotInteger = { _fileNames, FileAdapterTest.fileName1, FileAdapterTest.fileName2,
			FileAdapterTest.fileName3, _fileType, FileImportGUI.GENERIC_FILE, _delimiter, delimiter, _hRows,
			notAnInteger, _startPos, startPos, _endPos, endPos };

	/**
	 * if the number of header specified is less than one
	 */
	String[] hRowsLessOne = { _fileNames, FileAdapterTest.fileName1, FileAdapterTest.fileName2,
			FileAdapterTest.fileName3, _fileType, FileImportGUI.GENERIC_FILE, _delimiter, delimiter, _hRows,
			lessThanOne, _startPos, startPos, _endPos, endPos };

	/**
	 * The number specified for start pos cannot be converterd to integer
	 */
	String[] startPosIsNotInteger = { _fileNames, FileAdapterTest.fileName1, FileAdapterTest.fileName2,
			FileAdapterTest.fileName3, _fileType, FileImportGUI.GENERIC_FILE, _delimiter, delimiter, _hRows,
			noOfColumnHeaderRows, _startPos, notAnInteger, _endPos, endPos };

	/**
	 * if the start position for tab name specified is less than one
	 */
	String[] startPosLessOne = { _fileNames, FileAdapterTest.fileName1, FileAdapterTest.fileName2,
			FileAdapterTest.fileName3, _fileType, FileImportGUI.GENERIC_FILE, _delimiter, delimiter, _hRows,
			noOfColumnHeaderRows, _startPos, lessThanOne, _endPos, endPos };

	/**
	 * The number specified for end pos cannot be converterd to integer
	 */
	String[] endPosIsNotInteger = { _fileNames, FileAdapterTest.fileName1, FileAdapterTest.fileName2,
			FileAdapterTest.fileName3, _fileType, FileImportGUI.GENERIC_FILE, _delimiter, delimiter, _hRows,
			noOfColumnHeaderRows, _startPos, startPos, _endPos, notAnInteger };

	/**
	 * if the end position for tab name specified is less than one
	 */
	String[] endPosLessOne = { _fileNames, FileAdapterTest.fileName1, FileAdapterTest.fileName2,
			FileAdapterTest.fileName3, _fileType, FileImportGUI.GENERIC_FILE, _delimiter, delimiter, _hRows,
			noOfColumnHeaderRows, _startPos, startPos, _endPos, lessThanOne };

	/**
	 * Test for when arguments are correctly specified with one file name and one file type
	 */
	public void testCorrectFileArg1() {
		try {
			FileAdapter fAdapter = new FileAdapter(fNFt);
			assertEquals(fAdapter.fileNames[0], FileAdapterTest.fileName1);
			assertEquals(fAdapter.fileType, FileImportGUI.SMOKE_REPORT_FILE);
			assertEquals(fAdapter.delimiter, SMKReportFileReader.DELIMITER);
			assertEquals(fAdapter.noOfColumnHeader, SMKReportFileReader.NO_OF_COLUMN_HEADER_ROWS);
			assertEquals(fAdapter.startPos, FileImportGUI.START_TAB_NAME_INDEX);
			assertEquals(fAdapter.endPos, FileImportGUI.END_TAB_NAME_INDEX);
		} catch (Exception e) {
			System.err.println("Unexpected error: " + e.getMessage());
			assertEquals(true, false);
		}
	}// testCorrectFileArg1()

	/**
	 * Test for when arguments are correctly specified with three file names and one file type
	 */
	public void testCorrectFileArg2() {
		try {
			FileAdapter fAdapter = new FileAdapter(mulFnFt);
			assertEquals(fAdapter.fileNames[0], FileAdapterTest.fileName1);
			assertEquals(fAdapter.fileNames[1], FileAdapterTest.fileName2);
			assertEquals(fAdapter.fileNames[2], FileAdapterTest.fileName3);
			assertEquals(fAdapter.fileType, FileImportGUI.SMOKE_REPORT_FILE);
			assertEquals(fAdapter.delimiter, SMKReportFileReader.DELIMITER);
			assertEquals(fAdapter.noOfColumnHeader, SMKReportFileReader.NO_OF_COLUMN_HEADER_ROWS);
			assertEquals(fAdapter.startPos, FileImportGUI.START_TAB_NAME_INDEX);
			assertEquals(fAdapter.endPos, FileImportGUI.END_TAB_NAME_INDEX);
		} catch (Exception e) {
			System.err.println("Unexpected error: " + e.getMessage());
			assertEquals(true, false);
		}
	}// testCorrectFileArg2()

	/**
	 * Test for when arguments are correctly specified with three file names and one file type and a delimiter
	 */
	public void testCorrectFileArg3() {
		try {
			FileAdapter fAdapter = new FileAdapter(mulFnFtDel);
			assertEquals(fAdapter.fileNames[0], FileAdapterTest.fileName1);
			assertEquals(fAdapter.fileNames[1], FileAdapterTest.fileName2);
			assertEquals(fAdapter.fileNames[2], FileAdapterTest.fileName3);
			assertEquals(fAdapter.fileType, FileImportGUI.SMOKE_REPORT_FILE);
			assertEquals(fAdapter.delimiter, delimiter);
			assertEquals(fAdapter.noOfColumnHeader, SMKReportFileReader.NO_OF_COLUMN_HEADER_ROWS);
			assertEquals(fAdapter.startPos, FileImportGUI.START_TAB_NAME_INDEX);
			assertEquals(fAdapter.endPos, FileImportGUI.END_TAB_NAME_INDEX);
		} catch (Exception e) {
			System.err.println("Unexpected error: " + e.getMessage());
			assertEquals(true, false);
		}
	}// testCorrectFileArg3()

	/**
	 * Test for when arguments are correctly specified with three file names and one file type a delimiter, no of column
	 * header rows, a start position for tab name and a end position for the tab name
	 */
	public void testCorrectFileArg4() {
		try {
			FileAdapter fAdapter = new FileAdapter(argsAll);
			assertEquals(fAdapter.fileNames[0], FileAdapterTest.fileName1);
			assertEquals(fAdapter.fileNames[1], FileAdapterTest.fileName2);
			assertEquals(fAdapter.fileNames[2], FileAdapterTest.fileName3);
			assertEquals(fAdapter.fileType, FileImportGUI.GENERIC_FILE);
			assertEquals(fAdapter.delimiter, delimiter);
			assertEquals(fAdapter.noOfColumnHeader, Integer.parseInt(noOfColumnHeaderRows));
			assertEquals(fAdapter.startPos, Integer.parseInt(startPos));
			assertEquals(fAdapter.endPos, Integer.parseInt(endPos));
		} catch (Exception e) {
			System.err.println("Unexpected error: " + e.getMessage());
			assertEquals(true, false);
		}
	}// testCorrectFileArg4()

	/*******************************************************************************************************************
	 * Test for when arguments are correctly specified ** BUT NOT IN A USUAL ORDER with three file names and one file
	 * type a delimiter, no of column header rows, a start position for tab name and a end position for the tab name
	 */
	public void testCorrectFileArg5() {
		try {
			FileAdapter fAdapter = new FileAdapter(changedOrderAllArgs);
			assertEquals(fAdapter.fileNames[0], FileAdapterTest.fileName1);
			assertEquals(fAdapter.fileNames[1], FileAdapterTest.fileName2);
			assertEquals(fAdapter.fileNames[2], FileAdapterTest.fileName3);
			assertEquals(fAdapter.fileType, FileImportGUI.GENERIC_FILE);
			assertEquals(fAdapter.delimiter, delimiter);
			assertEquals(fAdapter.noOfColumnHeader, Integer.parseInt(noOfColumnHeaderRows));
			assertEquals(fAdapter.startPos, Integer.parseInt(startPos));
			assertEquals(fAdapter.endPos, Integer.parseInt(endPos));
		} catch (Exception e) {
			System.err.println("Unexpected error: " + e.getMessage());
			assertEquals(true, false);
		}
	}// testCorrectFileArg4()

	/**
	 * Test for no file names is specified
	 */
	public void testFileNamesMissing() {
		String message = "You have to specify atleast one file name";
		try {
			new FileAdapter(fNMissing);
			assertEquals(true, false);
		} catch (Exception e) {
			assertEquals(e.getMessage(), message);
		}
	}// testFileNamesMissing()

	/**
	 * Test for when arguments are correctly specified BUT one of the file specified is not exist
	 */
	public void testFileNotExist() {
		String message = "The file " + wrongFileName + " is not exist";
		try {
			new FileAdapter(fNNotExist);
			assertEquals(true, false);
		} catch (Exception e) {
			assertEquals(e.getMessage(), message);
		}
	}// testFileNotExist()

	/**
	 * Test for no file type is specified
	 */
	public void testFileTypeMissing() {
		String message = "You have to specify the file type for the file names ";
		try {
			new FileAdapter(fTMissing);
			assertEquals(true, false);
		} catch (Exception e) {
			assertEquals(e.getMessage(), message);
		}
	}// testFileMissing()

	/**
	 * Test for when arguments are correctly specified BUT the file type specified is not available
	 */
	public void testFileTypeNotAvailable() {
		String message = "Unrecognized file type '" + wrongFileType + "'.";
		try {
			new FileAdapter(fTNotExist);
			assertEquals(true, false);
		} catch (Exception e) {
			assertEquals(e.getMessage(), message);
		}
	}// testFileTypeNotAvailables()

	/**
	 * Test for when delimiter is not specified when required
	 */
	public void testDelMissing() {
		String message = "For the '" + FileImportGUI.DAVE_OUTPUT_FILE + "' delimiter should be specified";
		try {
			new FileAdapter(delMissing);
			assertEquals(true, false);
		} catch (Exception e) {
			assertEquals(e.getMessage(), message);
		}
	}// testDelMissing()

	/**
	 * Test for when number of header rows is not specified when required
	 */
	public void testNumHeaderRowsMissing() {
		String message = "The file type '" + FileImportGUI.GENERIC_FILE + "' requires noOfRows to be specified.";
		try {
			new FileAdapter(hRowsMissing);
			assertEquals(true, false);
		} catch (Exception e) {
			assertEquals(e.getMessage(), message);
		}
	}

	/**
	 * Test for when number of header rows specified cannot be converted to integer
	 */
	public void testNumHeaderRowsConversion() {
		String message = "Could not convert no of column header " + notAnInteger + " to an integer";
		try {
			new FileAdapter(hRowsIsNotInteger);
			assertEquals(true, false);
		} catch (Exception e) {
			assertEquals(e.getMessage(), message);
		}
	}

	/**
	 * Test for when number of header rows is less than one
	 */
	public void testNumHeaderRowsLessThanOne() {
		String message = "The value specified for " + "No of header rows" + " should be >= 1";
		try {
			new FileAdapter(hRowsLessOne);
			assertEquals(true, false);
		} catch (Exception e) {
			assertEquals(e.getMessage(), message);
		}
	}

	/**
	 * Test for when start position specified cannot be converted to integer
	 */
	public void testStartPosConversion() {
		String message = "Could not convert startPos " + notAnInteger + " to an integer";
		try {
			new FileAdapter(startPosIsNotInteger);
			assertEquals(true, false);
		} catch (Exception e) {
			assertEquals(e.getMessage(), message);
		}
	}

	/**
	 * Test for when start position is less than one
	 */
	public void testStartPosLessThanOne() {
		String message = "The value specified for " + "start position" + " should be >= 1";
		try {
			new FileAdapter(startPosLessOne);
			assertEquals(true, false);
		} catch (Exception e) {
			assertEquals(e.getMessage(), message);
		}
	}

	/**
	 * Test for when end position specified cannot be converted to integer
	 */
	public void testEndPosConversion() {
		String message = "Could not convert endPos " + notAnInteger + " to an integer";
		try {
			new FileAdapter(endPosIsNotInteger);
			assertEquals(true, false);
		} catch (Exception e) {
			assertEquals(e.getMessage(), message);
		}
	}

	/**
	 * Test for when end position is less than one
	 */
	public void testEndPosLessThanOne() {
		String message = "The value specified for " + "end position" + " should be >= 1";
		try {
			new FileAdapter(endPosLessOne);
			assertEquals(true, false);
		} catch (Exception e) {
			assertEquals(e.getMessage(), message);
		}
	}

	public static Test suite() {
		return new TestSuite(FileAdapterTest.class);
	}

	public static void main(String[] args) {
		junit.swingui.TestRunner.run(FileAdapterTest.class);
	}// main()

}
