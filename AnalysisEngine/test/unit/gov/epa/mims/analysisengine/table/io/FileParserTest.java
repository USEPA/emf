/*
 * Created on Jun 24, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package gov.epa.mims.analysisengine.table.io;

import gov.epa.mims.analysisengine.table.io.DAVEFileReader;
import gov.epa.mims.analysisengine.table.io.FileParser;
import gov.epa.mims.analysisengine.table.io.MonteCarloFileReader;
import gov.epa.mims.analysisengine.table.io.SMKReportFileReader;
import gov.epa.mims.analysisengine.table.io.TRIMResultFileReader;
import gov.epa.mims.analysisengine.table.io.TRIMSensitivityFileReader;

import java.io.File;
import java.util.Date;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author kthanga
 * 
 * To change the template for this generated type comment go to Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code
 * and Comments
 */
public class FileParserTest extends TestCase {
	private String files_location = null;

	private boolean FileHeaderIsNull = true;

	private boolean ColumnHeaderIsNull = true;

	private boolean FileDataIsNull = true;

	private boolean FooterIsNull = true;

	private boolean RowHeaderIsNull = true;

	private boolean LoggerIsEmpty = true;

	private int columnCount = 0;

	private String[] filenam = { "Test_SMOKE.txt", // 0
			"Test_DAVE.txt", // 1
			"Test_TRIM_Sensitivity.txt",// 2
			"Test_TRIM_Results.txt", // 3
			"Test_Monte_Carlo_Input.txt", // 4
			"Test_COSU.txt", // 5
			"Test_With_One_line_data_only.txt", // 6
			"Test_With_Two_Lines_Data.txt", // 7
			"Test_With_One_line_header_one_line_data.txt", // 8
			"Test_With_MissingData.txt", // 9
			"Test_Mixed_Integer_Double.txt", // 10
			"Test_ColHdr_Junk_Data.txt", // 11
			"Test_With_Multiple_Single_Line_Truncation.txt", // 12
			"Test_With_Multiple_Line_Truncation.txt", // 13
			"Test_With_Space_Between_Header_Data.txt", // 14
			"Test_Wrong_Delimiter.txt", // 15
			"Test_With_Wrong_Header_Info_Input.txt", // 16
			"Test_Data_With_NaNs.txt", // 17
			"Test_Data_In_Quotes.txt", // 18
			"Test_Data_Only.txt", // 19
			"Test_Long_Footer.txt", // 20
			"Test_Few_Rows_Many_Columns.txt" // 21
	};

	public FileParserTest(String arg0) {
		super(arg0);
	}

	protected void setUp() throws Exception {
		super.setUp();
		files_location = "test/data/example" + File.separator;
	}

	public void FIXME_testSMOKEFile() {

		FileParser parser = null;
		String delimiter = ";";
		int numColHdrRows = 2;
		FileHeaderIsNull = false;
		ColumnHeaderIsNull = false;
		FileDataIsNull = false;
		FooterIsNull = false;
		RowHeaderIsNull = true;
		LoggerIsEmpty = false;
		columnCount = 11;
		Class[] columnTypes = { Date.class, Integer.class, String.class, Double.class, Double.class, Double.class,
				Double.class, Double.class, Double.class, Double.class, Double.class };

		try {
			parser = new FileParser(files_location + filenam[0], delimiter, numColHdrRows, false);
			checkMethods(parser, columnTypes);
		} catch (Exception e) {
			System.out.println("testSMOKE " + e.getMessage());
			assertEquals(true, false);
		}
	}

	public void FIXME_testDAVE() {
		FileParser parser = null;
		String delimiter = ",";
		int numColHdrRows = 1;
		FileHeaderIsNull = false;
		ColumnHeaderIsNull = false;
		FileDataIsNull = false;
		FooterIsNull = true;
		RowHeaderIsNull = true;
		LoggerIsEmpty = true;
		columnCount = 5;
		Class[] columnTypes = { Integer.class, Double.class, Double.class, Double.class, Double.class };

		try {
			parser = new FileParser(files_location + filenam[1], delimiter, numColHdrRows, false);
			checkMethods(parser, columnTypes);
		} catch (Exception e) {
			System.out.println("testDAVE " + e.getMessage());
			assertEquals(true, false);
		}
	}

	public void testTRIMSensitivity() {
		String delimiter = ";";
		int numColHdrRows = 1;
		FileHeaderIsNull = false;
		ColumnHeaderIsNull = false;
		FileDataIsNull = false;
		FooterIsNull = true;
		RowHeaderIsNull = true;
		LoggerIsEmpty = true;
		columnCount = 18;
		try {
			new FileParser(files_location + filenam[2], delimiter, numColHdrRows, false);

		} catch (Exception e) {
			System.out.println("testTRIMSensitivity " + e.getMessage());
			assertEquals(true, false);
		}
	}

	public void testTRIMResults() {
		FileParser parser = null;
		String delimiter = ";";
		int numColHdrRows = 2;
		FileHeaderIsNull = false;
		ColumnHeaderIsNull = false;
		FileDataIsNull = false;
		FooterIsNull = true;
		RowHeaderIsNull = true;
		LoggerIsEmpty = true;
		columnCount = 273;
		Class[] columnTypes = null;

		try {
			parser = new FileParser(files_location + filenam[3], delimiter, numColHdrRows, false);
			checkMethods(parser, columnTypes);
		} catch (Exception e) {
			System.out.println("testTRIMResults " + e.getMessage());
			assertEquals(true, false);
		}

	}

	public void testMonteCarloInput() {
		FileParser parser = null;
		String delimiter = ",";
		int numColHdrRows = 4;
		FileHeaderIsNull = false;
		ColumnHeaderIsNull = false;
		FileDataIsNull = false;
		FooterIsNull = true;
		RowHeaderIsNull = true;
		LoggerIsEmpty = true;
		columnCount = 7;
		Class[] columnTypes = { Integer.class, Double.class, Double.class, Double.class, Double.class, Double.class,
				Double.class };

		try {
			parser = new FileParser(files_location + filenam[4], delimiter, numColHdrRows, false);
			checkMethods(parser, columnTypes);
		} catch (Exception e) {
			System.out.println("testMonteCarloInput " + e.getMessage());
			assertEquals(true, false);
		}

	}

	public void FIXME_testCOSU() {
		FileParser parser = null;
		String delimiter = ",";
		int numColHdrRows = 1;
		FileHeaderIsNull = true;
		ColumnHeaderIsNull = false;
		FileDataIsNull = false;
		FooterIsNull = true;
		RowHeaderIsNull = true;
		LoggerIsEmpty = true;
		columnCount = 5;
		Class[] columnTypes = { Integer.class, Double.class, Double.class, Double.class, Double.class };

		try {
			parser = new FileParser(files_location + filenam[5], delimiter, numColHdrRows, false);
			checkMethods(parser, columnTypes);
		} catch (Exception e) {
			System.out.println("testCOSU " + e.getMessage());
			assertEquals(true, false);
		}

	}

	public void testMonteCarloFileReader() {
		try {
			new MonteCarloFileReader(files_location + filenam[4], false);
		} catch (Exception e) {
			System.out.println("testMonteCarloFileReader " + e.getMessage());
			assertEquals(true, false);
		}

	}

	public void testSMKReportFileReader() {
		try {
			new SMKReportFileReader(files_location + filenam[0], ";", false);
		} catch (Exception e) {
			System.out.println("testSMKReportFileReader " + e.getMessage());
			assertEquals(true, false);
		}
	}

	public void testTRIMResultFileReader() {
		try {
			new TRIMResultFileReader(files_location + filenam[3], false);
		} catch (Exception e) {
			System.out.println("testTRIMResultFileReader " + e.getMessage());
			assertEquals(true, false);
		}
	}

	public void testDAVEFileReader() {
		try {
			new DAVEFileReader(files_location + filenam[1], ",", false);
		} catch (Exception e) {
			System.out.println("testDAVEFileReader " + e.getMessage());
			assertEquals(true, false);
		}

	}

	public void testTRIMSensitivityFileReader() {
		try {
			new TRIMSensitivityFileReader(files_location + filenam[2], false);
		} catch (Exception e) {
			System.out.println("testTRIMSensitivityFileReader " + e.getMessage());
			assertEquals(true, false);
		}

	}

	public void testTRIMResultWithTrimSensitivityFile() {
		try {
			new TRIMResultFileReader(files_location + filenam[2], false);
			System.out.println("testTRIMResultWithTRIMSensitivityFile failed ");
			assertEquals(true, false);
		} catch (Exception e) {
			assertEquals(true, true);
		}
	}

	public void testSingleLine() {
		String delimiter = ";";

		try {
			new FileParser(files_location + filenam[6], delimiter, 0, false);
			System.out.println("testSingleLine failed ");
			assertFalse(true);
		} catch (Exception e) {
			assertTrue("Can't read a file with single  line",true);
		}
	}

	public void FIXME_testTwoDataLinesOnly() {
		FileParser parser = null;
		String delimiter = ";";
		int numColHdrRows = 0;
		FileHeaderIsNull = true;
		ColumnHeaderIsNull = true;
		FileDataIsNull = false;
		FooterIsNull = true;
		RowHeaderIsNull = true;
		LoggerIsEmpty = true;
		columnCount = 6;
		Class[] columnTypes = { Date.class, Integer.class, String.class, Double.class, Double.class, Double.class };
		try {
			parser = new FileParser(files_location + filenam[7], delimiter, numColHdrRows, false);
			checkMethods(parser, columnTypes);
		} catch (Exception e) {
			System.out.println("testTwoDataLinesOnly failed " + e.getMessage());
			assertEquals(true, false);
		}

	}

	public void FIXME_testDataOnly() {
		FileParser parser = null;
		String delimiter = ",";
		int numColHdrRows = 0;
		FileHeaderIsNull = true;
		ColumnHeaderIsNull = true;
		FileDataIsNull = false;
		FooterIsNull = true;
		RowHeaderIsNull = true;
		LoggerIsEmpty = true;
		columnCount = 5;
		Class[] columnTypes = null;

		try {
			parser = new FileParser(files_location + filenam[19], delimiter, numColHdrRows, false);
			checkMethods(parser, columnTypes);
		} catch (Exception e) {
			System.out.println("testDataOnly failed");
			e.printStackTrace();
			assertEquals(true, false);
		}
	}

	public void FIXME_testOneHeaderLineOneDataLine() {
		FileParser parser = null;
		String delimiter = ":";
		int numColHdrRows = 1;
		FileHeaderIsNull = false;
		ColumnHeaderIsNull = false;
		FileDataIsNull = false;
		FooterIsNull = true;
		RowHeaderIsNull = true;
		LoggerIsEmpty = true;
		columnCount = 6;
		Class[] columnTypes = { Date.class, Integer.class, String.class, Double.class, Double.class, Double.class };

		try {
			parser = new FileParser(files_location + filenam[8], delimiter, numColHdrRows, false);
			checkMethods(parser, columnTypes);
		} catch (Exception e) {
			System.out.println("testOneHeaderLineOneDataLine failed: " + e.getMessage());
			assertEquals(true, false);
		}

	}

	public void FIXME_testDataWithMissingValues() {
		FileParser parser = null;
		String delimiter = ",";
		int numColHdrRows = 1;
		FileHeaderIsNull = false;
		ColumnHeaderIsNull = false;
		FileDataIsNull = false;
		FooterIsNull = true;
		RowHeaderIsNull = true;
		LoggerIsEmpty = true;
		columnCount = 18;
		Class[] columnTypes = { String.class, Double.class, Double.class, Double.class, Double.class, Double.class,
				Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class,
				Double.class, Double.class, Double.class, Double.class, Double.class };

		try {

			parser = new FileParser(files_location + filenam[9], delimiter, numColHdrRows, false);
			checkMethods(parser, columnTypes);

		} catch (Exception e) {
			System.out.println("testDataWithMissingValues failed");
			e.printStackTrace();
			assertEquals(true, false);
		}

	}

	public void FIXME_testIntegerMixedWithDouble() {
		FileParser parser = null;
		String delimiter = ",";
		int numColHdrRows = 1;
		FileHeaderIsNull = false;
		ColumnHeaderIsNull = false;
		FileDataIsNull = false;
		FooterIsNull = true;
		RowHeaderIsNull = true;
		LoggerIsEmpty = true;
		columnCount = 6;
		Class[] columnTypes = { Double.class, Double.class, Double.class, Double.class, Double.class, Double.class };

		try {
			parser = new FileParser(files_location + filenam[10], delimiter, numColHdrRows, false);
			checkMethods(parser, columnTypes);
		} catch (Exception e) {
			System.out.println("testIntegerMixedWithDouble failed");
			e.printStackTrace();
			assertEquals(true, false);
		}

	}

	public void testJunkBetweenSingleColHdrLineAndData() {
		FileParser parser = null;
		String delimiter = "#";
		int numColHdrRows = 1;
		FileHeaderIsNull = true;
		ColumnHeaderIsNull = false;
		FileDataIsNull = false;
		FooterIsNull = false;
		RowHeaderIsNull = true;
		LoggerIsEmpty = false;
		columnCount = 6;
		Class[] columnTypes = null;

		try {
			parser = new FileParser(files_location + filenam[11], delimiter, numColHdrRows, false);
			checkMethods(parser, columnTypes);
		} catch (Exception e) {
			System.out.println("testJunkBetweenColHdrAndData failed" + e.getMessage());
			assertEquals(true, false);
		}

	}

	public void FIXME_testMultipleSingleDataTruncatedLines() {
		FileParser parser = null;
		String delimiter = ",";
		int numColHdrRows = 1;
		FileHeaderIsNull = false;
		ColumnHeaderIsNull = false;
		FileDataIsNull = false;
		FooterIsNull = true;
		RowHeaderIsNull = true;
		LoggerIsEmpty = false;
		columnCount = 5;
		Class[] columnTypes = null; // ignore this check

		try {
			parser = new FileParser(files_location + filenam[12], delimiter, numColHdrRows, false);
			checkMethods(parser, columnTypes);
		} catch (Exception e) {
			System.out.println("testMultipleSingleTruncatedLines failed" + e.getMessage());
			assertEquals(true, false);
		}

	}

	public void testMoreThanTwoConsequtiveTruncatedLines() {
		FileParser parser = null;
		String delimiter = ",";
		int numColHdrRows = 1;
		FileHeaderIsNull = false;
		ColumnHeaderIsNull = false;
		FileDataIsNull = false;
		FooterIsNull = false;
		RowHeaderIsNull = true;
		LoggerIsEmpty = true;
		columnCount = 5;
		Class[] columnTypes = null; // here we're not testing this aspect; so ignore

		try {
			parser = new FileParser(files_location + filenam[13], delimiter, numColHdrRows, false);
			checkMethods(parser, columnTypes);
		} catch (Exception e) {
			System.out.println("testMoreThanTwoConsequtiveTruncatedLines failed" + e.getMessage());
			assertEquals(true, false);
		}

	}

	public void FIXME_testOneHeaderLineOneDataLineWithBlankLineInBetween() {
		FileParser parser = null;
		String delimiter = ":";
		int numColHdrRows = 1;
		FileHeaderIsNull = false;
		ColumnHeaderIsNull = false;
		FileDataIsNull = false;
		FooterIsNull = true;
		RowHeaderIsNull = true;
		LoggerIsEmpty = true;
		columnCount = 6;
		Class[] columnTypes = null;

		try {
			parser = new FileParser(files_location + filenam[14], delimiter, numColHdrRows, false);
			checkMethods(parser, columnTypes);
		} catch (Exception e) {
			System.out.println("testOneHeaderLineOneDataLineWithBlankLineInBetween" + e.getMessage());
			assertEquals(true, false);
		}

	}

	public void FIXME_testWrongDelimiterFoundInHeader() {
		FileParser parser = null; // Test_SMOKE
		String delimiter = ",";
		int numColHdrRows = 2;
		FileHeaderIsNull = false;
		ColumnHeaderIsNull = false;
		FileDataIsNull = true;
		FooterIsNull = false;
		RowHeaderIsNull = true;
		LoggerIsEmpty = false;
		columnCount = 2;
		Class[] columnTypes = null;

		try {
			parser = new FileParser(files_location + filenam[15], delimiter, numColHdrRows, false);
			checkMethods(parser, columnTypes);
		} catch (Exception e) {
			System.out.println("testWrongDelimiterFoundInHeader failed: " + e.getMessage());
			assertEquals(true, false);
		}

	}

	public void testWrongDelimiterNotFoundInHeader() {
		FileParser parser = null;
		String delimiter = "|";
		int numColHdrRows = 2;
		FileHeaderIsNull = false;
		ColumnHeaderIsNull = true;
		FileDataIsNull = true;
		FooterIsNull = true;
		RowHeaderIsNull = true;
		LoggerIsEmpty = false;
		columnCount = 0;
		Class[] columnTypes = null;

		try {
			parser = new FileParser(files_location + filenam[15], delimiter, numColHdrRows, false);
			checkMethods(parser, columnTypes);
		} catch (Exception e) {
			System.out.println("testWrongDelimiterNotFoundInHeader failed:" + e.getMessage());
			e.printStackTrace();
			printValues(parser);
			assertEquals(true, false);
		}

	}

	public void FIXME_testLesserInputOfColHdrRowCount() {
		try {
			System.out.println("testLesserInputOfColHdrRowCount failed");
			assertEquals(true, false);
		} catch (Exception e) {
			assertEquals(true, true);
		}

	}

	public void testHigherInputOfColHdrRowCount() {
		FileHeaderIsNull = false;
		ColumnHeaderIsNull = false;
		FileDataIsNull = false;
		FooterIsNull = true;
		RowHeaderIsNull = true;
		LoggerIsEmpty = true;
		columnCount = 7;
		try {
			assertEquals(true, true);
		} catch (Exception e) {
			System.out.println("testHigherInputOfColHdrRowCount failed " + e.getMessage());
			assertEquals(true, false);
		}

	}

	public void FIXME_testDataWithNaNs() {
		FileParser parser = null;
		String delimiter = ",";
		int numColHdrRows = 1;
		FileHeaderIsNull = false;
		ColumnHeaderIsNull = false;
		FileDataIsNull = false;
		FooterIsNull = true;
		RowHeaderIsNull = true;
		LoggerIsEmpty = true;
		columnCount = 18;
		Class[] columnTypes = { String.class, Double.class, Double.class, Double.class, Double.class, Double.class,
				Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class,
				Double.class, Double.class, Double.class, Double.class, Double.class };

		try {
			parser = new FileParser(files_location + filenam[17], delimiter, numColHdrRows, false);
			checkMethods(parser, columnTypes);
		} catch (Exception e) {
			System.out.println("DataWithNaNs " + e.getMessage());
			assertEquals(true, false);
		}

	}

	public void testDataInQuotes() {
		FileParser parser = null;
		String delimiter = ",";
		int numColHdrRows = 1;
		FileHeaderIsNull = false;
		ColumnHeaderIsNull = false;
		FileDataIsNull = false;
		FooterIsNull = false;
		RowHeaderIsNull = true;
		LoggerIsEmpty = true;
		columnCount = 11;
		Class[] columnTypes = { String.class, Double.class, Double.class, Double.class, Double.class, Double.class,
				Double.class, Double.class, Double.class, Double.class, Double.class };

		try {
			parser = new FileParser(files_location + filenam[18], delimiter, numColHdrRows, false);
			checkMethods(parser, columnTypes);
		} catch (Exception e) {
			System.out.println("TestDataInQuotes failed: " + e.getMessage());
			assertEquals(true, false);
		}

	}

	public void testLongFooter() {
		try {
			new FileParser(files_location + filenam[20], ",", 2, false);
		} catch (Exception e) {
			System.out.println("TestLongFooter failed:" + e.getMessage());
			assertEquals(true, false);
		}
	}

	private void checkMethods(FileParser parser, Class[] columnTypes) throws Exception {

		if (FileHeaderIsNull) {
			assertEquals(0, parser.getFileHeader().length());
		} else if (parser.getFileHeader() == null)
			assertEquals(true, false);

		if (ColumnHeaderIsNull) {
			assertEquals(null, parser.getColumnHeaderData());
		} else {
			if (parser.getColumnHeaderData() == null)
				assertEquals(true, false);
		}

		if (FileDataIsNull) {
			assertEquals(null, parser.getFileData());
		} else {
			if (parser.getFileData() == null)
				assertEquals(true, false);
		}

		if (FooterIsNull) {
			if (parser.getFileFooter() != null)
				assertEquals(0, parser.getFileFooter().length());
		} else {
			if (parser.getFileFooter() == null)
				assertEquals(true, false);
		}

		if (RowHeaderIsNull) {
			assertEquals(null, parser.getRowHeaderData());
		} else {
			if (parser.getRowHeaderData() == null)
				assertEquals(true, false);
		}

		if (LoggerIsEmpty)
			assertEquals(parser.getLogMessages(), null);
		else if ((parser.getLogMessages()).length() == 0)
			assertEquals(true, false);

		assertEquals(columnCount, parser.getColumnCount());

		Class[] types = parser.getColumnClass();
		if (types != null && columnTypes != null)
			for (int i = 0; i < columnCount; i++) {
				assertEquals(columnTypes[i], types[i]);
			}

	}

	private void printValues(FileParser parser) {
		System.out.println("File Header\n ==========");
		System.out.println(parser.getFileHeader());
		System.out.println("Column Header\n ==========");
		System.out.println(parser.getColumnHeaderData());
		System.out.println("File Data\n ==========");
		System.out.println(parser.getFileData());
		System.out.println("Row Header Data\n ==========");
		System.out.println(parser.getRowHeaderData());
		System.out.println("File Footer\n ==========");
		System.out.println(parser.getFileFooter());
		System.out.println("Column Count : " + parser.getColumnCount());
	}

	/*******************************************************************************************************************
	 * retrieve this test suite
	 * 
	 * @return this test suite
	 ******************************************************************************************************************/
	public static Test suite() {
		return new TestSuite(FileParserTest.class);
	}

}
