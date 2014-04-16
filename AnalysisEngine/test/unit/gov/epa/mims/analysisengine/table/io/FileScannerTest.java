/*
 * Created on Jun 24, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package gov.epa.mims.analysisengine.table.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringBufferInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import java_cup.runtime.Symbol;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author kthanga
 * 
 * To change the template for this generated type comment go to Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code
 * and Comments
 */
public class FileScannerTest extends TestCase {
	String fileLocation = null;

	public FileScannerTest(String arg0) {
		super(arg0);
		fileLocation = "test/data/example";
	}

	private String[] readLines(String filename) throws IOException {

		ArrayList lines = new ArrayList();
		String line = null;
		FileReader reader = new FileReader(filename);
		BufferedReader r = new BufferedReader(reader);
		while ((line = r.readLine()) != null)
			lines.add(line);
		r.close();
		reader.close();
		return (String[]) lines.toArray(new String[0]);
	}

	public void FIXME_testGetLine() {
		String filename = new File(fileLocation, "NormalFile").getAbsolutePath();
		try {
			readLines(filename);
		} catch (IOException e) {
			System.out.println("File " + filename + " missing");
			return;
		}
		// Three conditions to be checked
		// before any data is read by tokenizer, getLine() is invoked
		FileScanner scanner = null;
		try {
			scanner = new FileScanner(new java.io.FileReader(fileLocation + File.separator + "NormalFile"));
		} catch (IOException ie) {
			assertEquals(true, false);
		}
		String line = scanner.getLine();
		assertEquals(line, "");

		try {
			while (scanner.getTokensPerLine(',', false) != null) {
				line = scanner.getLine();
				/*
				 * System.out.println(line.substring(0, line.length()-1)); System.out.println(lines[i]); boolean
				 * equality=false; if(lines[i++].compareTo(line.substring(0, line.length()-1))==0) equality = true;
				 * System.out.println("equality= "+ equality); assertEquals(equality, true);
				 */}
			// when end of file is reached, getLine() should return null and not throw up
			line = scanner.getLine();
			assertEquals(line, "");
		} catch (IOException ie) {
			assertEquals(true, false);
		}
	}

	public void FIXME_testWithEmptyLine() {
		String emptyLine = "";
		FileScanner scanner = new FileScanner(new StringBufferInputStream(emptyLine));
		try {

			Symbol[] tokens = scanner.getTokensPerLine(',', false);

			if (tokens != null) {
				assertEquals(true, false);
			}

			String line = scanner.getLine();
			if (line.length() != 0) {
				assertEquals(true, false);
			}
		} catch (IOException ie) {
			assertEquals(true, false);
		}

	}

	public void FIXME_testWithNewLineCharacterOnlyLine() {
		String line = "\n";
		FileScanner scanner = null;
		try {
			scanner = new FileScanner(new StringBufferInputStream(line));
		} catch (Exception e) {
			System.out.println("testWithNewLineCharacterOnlyLine");
			e.printStackTrace();
			assertEquals(true, false);
		}

		Symbol[] expected_value = { scanner.symbol(TokenConstants.NULL_LITERAL, null) };
		try {
			Symbol[] tokens = scanner.getTokensPerLine(',', false);
			if (tokens == null)
				assertEquals(true, false);
			assertEquals(1, tokens.length);
			assertEquals(compareSymbol(expected_value[0], tokens[0]), true);
			assertEquals(line, scanner.getLine());
		} catch (IOException ie) {
			assertEquals(true, false);
		}
	}

	private boolean compareSymbol(Symbol sym1, Symbol sym2) {
		if (sym1 == null) {
			if (sym2 == null)
				return true;

			return false;
		}

		if (sym2 == null)
			return false;

		if (sym1.sym == TokenConstants.NULL_LITERAL) {
			if (sym2.sym == TokenConstants.NULL_LITERAL)
				return true;
			return false;
		}

		if (sym1.value == null || sym2.value == null) {
			if (sym2.value == null && sym1.value == null)
				return true;

			return false;
		}

		if (sym1.sym != sym2.sym)
			return false;

		if (!((sym1.value).equals(sym2.value)))
			return false;

		return true;
	}

	private boolean compareSymbols(Symbol[] sym1, Symbol[] sym2) {
		if (sym1 == null || sym2 == null) {
			if (sym1 == null && sym2 == null)
				return true;
			return false;
		}

		if (sym1.length != sym2.length) {
			return false;
		}

		for (int i = 0; i < sym1.length; i++) {
			if (compareSymbol(sym1[i], sym2[i])) {
				continue;
			}
			return false;
		}
		return true;
	}

	public void testWithNormalLineWithWrongDelimiter() {

		String line = "abcd;234.5;1234;09/12/2004 02:30;qwert;\n";
		FileScanner scanner = null;
		try {
			scanner = new FileScanner(new StringBufferInputStream(line));
		} catch (Exception e) {
			assertEquals(true, false);
		}

		Symbol[] expected_value = { scanner.symbol(TokenConstants.STRING_LITERAL, line.substring(0, line.length() - 1)) };
		try {
			Symbol[] values = scanner.getTokensPerLine(',', false);
			assertEquals(compareSymbols(expected_value, values), true);
			// assertEquals(line,scanner.getLine());
		} catch (IOException ie) {
			assertEquals(true, false);
		}

	}

	public void FIXME_testWithMixedCharacterData() {
		String line = "abcd;234.5;1234;09/12/2004 02:30;qwert;\n";
		FileScanner scanner = null;
		try {
			scanner = new FileScanner(new StringBufferInputStream(line));
		} catch (Exception e) {
			System.out.println("testWithMixedCharacterData");
			e.printStackTrace();
			assertEquals(true, false);
		}

		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm");

		try {

			Symbol[] expected_value = { scanner.symbol(TokenConstants.STRING_LITERAL, "abcd"),
					scanner.symbol(TokenConstants.DOUBLE_LITERAL, new Double("234.5")),
					scanner.symbol(TokenConstants.INTEGER_LITERAL, new Integer("1234")),
					scanner.symbol(TokenConstants.DATE_TIME_LITERAL, sdf.parse("09/12/2004 02:30")),
					scanner.symbol(TokenConstants.STRING_LITERAL, "qwert"),
					scanner.symbol(TokenConstants.NULL_LITERAL, "") };

			assertEquals(compareSymbols(expected_value, scanner.getTokensPerLine(';', false)), true);
			assertEquals(line, scanner.getLine());

		} catch (Exception ie) {
			assertEquals(true, false);
		}
	}

	public void testWithDoubleData() {
		String line = "1493.676563463864,6.56150776093055E-8,10.669118310456177,0.0608139743696002,NaN,2.2232386397631296E-4,-1.1584864434794742E8,\"1.9330E-01\"\n";
		FileScanner scanner = null;
		try {
			scanner = new FileScanner(new StringBufferInputStream(line));
		} catch (Exception e) {
			System.out.println("testWithDoubleData");
			e.printStackTrace();
			assertEquals(true, false);
		}

		Symbol[] expected_value = { scanner.symbol(TokenConstants.DOUBLE_LITERAL, new Double("1493.676563463864")),
				scanner.symbol(TokenConstants.DOUBLE_LITERAL, new Double("6.56150776093055E-8")),
				scanner.symbol(TokenConstants.DOUBLE_LITERAL, new Double("10.669118310456177")),
				scanner.symbol(TokenConstants.DOUBLE_LITERAL, new Double("0.0608139743696002")),
				scanner.symbol(TokenConstants.DOUBLE_LITERAL, new Double("NaN")),
				scanner.symbol(TokenConstants.DOUBLE_LITERAL, new Double("2.2232386397631296E-4")),
				scanner.symbol(TokenConstants.DOUBLE_LITERAL, new Double("-1.1584864434794742E8")),
				scanner.symbol(TokenConstants.DOUBLE_LITERAL, new Double("1.9330E-01")) };
		try {
			Symbol[] values = scanner.getTokensPerLine(',', false);
			assertEquals(compareSymbols(expected_value, values), true);
			assertEquals(line, scanner.getLine());
		} catch (IOException ie) {
			assertEquals(true, false);
		}
	}

	public void testWithIntegerData() {
		String line = "0000:123:023456:-1223\n";
		FileScanner scanner = null;
		try {
			scanner = new FileScanner(new StringBufferInputStream(line));
		} catch (Exception e) {
			System.out.println("testWithIntegerData");
			e.printStackTrace();
			assertEquals(true, false);
		}

		Symbol[] expected_value = { scanner.symbol(TokenConstants.INTEGER_LITERAL, new Integer("0000")),
				scanner.symbol(TokenConstants.INTEGER_LITERAL, new Integer("123")),
				scanner.symbol(TokenConstants.INTEGER_LITERAL, new Integer("023456")),
				scanner.symbol(TokenConstants.INTEGER_LITERAL, new Integer("-1223")) };
		try {
			assertEquals(compareSymbols(expected_value, scanner.getTokensPerLine(':', false)), true);
			assertEquals(line, scanner.getLine());
		} catch (IOException ie) {
			assertEquals(true, false);
		}
	}

	public void testWithStringsInDoubleQuotes() {
		String line = "\"Hello World\":\"Hello, World\":\"Hello:12345\":\"Dove-Tailed Deer\":\"{(a+b)*e-f*(g/h)%45}\":\" \":What the world has gone to\n";

		FileScanner scanner = null;
		try {
			scanner = new FileScanner(new StringBufferInputStream(line));
		} catch (Exception e) {
			System.out.println("testWithStringsInDoubleQuotes");
			e.printStackTrace();
			assertEquals(true, false);
		}

		Symbol[] expected_value = { scanner.symbol(TokenConstants.STRING_LITERAL, "Hello World"),
				scanner.symbol(TokenConstants.STRING_LITERAL, "Hello, World"),
				scanner.symbol(TokenConstants.STRING_LITERAL, "Hello:12345"),
				scanner.symbol(TokenConstants.STRING_LITERAL, "Dove-Tailed Deer"),
				scanner.symbol(TokenConstants.STRING_LITERAL, "{(a+b)*e-f*(g/h)%45}"),
				scanner.symbol(TokenConstants.STRING_LITERAL, ""),
				scanner.symbol(TokenConstants.STRING_LITERAL, "What the world has gone to") };
		try {
			assertEquals(compareSymbols(expected_value, scanner.getTokensPerLine(':', false)), true);
			// assertEquals(line,scanner.getLine());
		} catch (IOException ie) {
			assertEquals(true, false);
		}
	}

	public void testWithDateFormats() {
		// correct date:correct time:wrong time:correct date:wrong date:wrong format: wrong format: without
		// quotes(correct date)
		String line = "04/04/1920:\"04/04/1920 03:56\":\"04/04/1920 25:25\":1/1/1920:0/0/1234:04|04|1920:\"14-4-1920 03:04\":04/04/1920 03:56\n";
		FileScanner scanner = null;
		try {
			scanner = new FileScanner(new StringBufferInputStream(line));
		} catch (Exception e) {
			System.out.println("testWithDateFormats");
			e.printStackTrace();
			assertEquals(true, false);
		}

		try {

			java.text.SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
			Date date1 = sdf.parse("04/04/1920");
			Date date3 = sdf.parse("1/1/1920");

			sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm");
			Date date2 = sdf.parse("04/04/1920 03:56");
			Date date4 = sdf.parse("04/04/1920 03:56");

			Symbol[] expected_value = { scanner.symbol(TokenConstants.DATE_LITERAL, date1),
					scanner.symbol(TokenConstants.DATE_TIME_LITERAL, date2),
					scanner.symbol(TokenConstants.STRING_LITERAL, "04/04/1920 25:25"),
					scanner.symbol(TokenConstants.DATE_LITERAL, date3),
					scanner.symbol(TokenConstants.STRING_LITERAL, "0/0/1234"),
					scanner.symbol(TokenConstants.STRING_LITERAL, "04|04|1920"),
					scanner.symbol(TokenConstants.STRING_LITERAL, "14-4-1920 03:04"),
					scanner.symbol(TokenConstants.DATE_TIME_LITERAL, date4) };

			Symbol[] values = scanner.getTokensPerLine(':', false);

			assertEquals(compareSymbols(expected_value, values), true);
			// assertEquals(line,scanner.getLine());
		} catch (Exception ie) {
			assertEquals(true, false);
		}
	}

	public void testWithBooleanData() {
		String line = "true:false:\"true false\":true false:\"true\":\"false\":True:False:TRUE:FALSE\n";
		FileScanner scanner = null;
		try {
			scanner = new FileScanner(new StringBufferInputStream(line));
		} catch (Exception e) {
			System.out.println("testWithBooleanData");
			e.printStackTrace();
			assertEquals(true, false);
		}

		Symbol[] expected_value = { scanner.symbol(TokenConstants.BOOLEAN_LITERAL, new Boolean(true)),
				scanner.symbol(TokenConstants.BOOLEAN_LITERAL, new Boolean(false)),
				scanner.symbol(TokenConstants.STRING_LITERAL, "true false"),
				scanner.symbol(TokenConstants.STRING_LITERAL, "true false"),
				scanner.symbol(TokenConstants.BOOLEAN_LITERAL, new Boolean(true)),
				scanner.symbol(TokenConstants.BOOLEAN_LITERAL, new Boolean(false)),
				scanner.symbol(TokenConstants.STRING_LITERAL, "True"),
				scanner.symbol(TokenConstants.STRING_LITERAL, "False"),
				scanner.symbol(TokenConstants.STRING_LITERAL, "TRUE"),
				scanner.symbol(TokenConstants.STRING_LITERAL, "FALSE") };
		try {
			assertEquals(compareSymbols(expected_value, scanner.getTokensPerLine(':', false)), true);
			// assertEquals(line,scanner.getLine());
		} catch (IOException ie) {
			assertEquals(true, false);
		}

	}

	public void FIXME_testWithMissingValues() {
		String line = "\" \":   ::  :abcd::NaN::\n";
		FileScanner scanner = null;
		try {
			scanner = new FileScanner(new StringBufferInputStream(line));
		} catch (Exception e) {
			System.out.println("testWithMissingValues");
			e.printStackTrace();
			assertEquals(true, false);
		}

		Symbol[] expected_value = { scanner.symbol(TokenConstants.STRING_LITERAL, ""),
				scanner.symbol(TokenConstants.NULL_LITERAL, ""), scanner.symbol(TokenConstants.NULL_LITERAL, ""),
				scanner.symbol(TokenConstants.NULL_LITERAL, ""), scanner.symbol(TokenConstants.STRING_LITERAL, "abcd"),
				scanner.symbol(TokenConstants.NULL_LITERAL, ""),
				scanner.symbol(TokenConstants.DOUBLE_LITERAL, new Double("NaN")),
				scanner.symbol(TokenConstants.NULL_LITERAL, ""), scanner.symbol(TokenConstants.NULL_LITERAL, "") };
		try {
			assertEquals(compareSymbols(expected_value, scanner.getTokensPerLine(':', false)), true);
			assertEquals(line, scanner.getLine());
		} catch (IOException ie) {
			assertEquals(true, false);
		}
	}

	public void FIXME_testWithMixedDataInQuotes() {
		String line = "\"Stone,Clay,Etc.\",\"100.258\",\"101.555\", \"-250\", \"04/04/1920\", \"true\",\"04/04/1920 03:56\",\n";
		FileScanner scanner = null;
		try {
			scanner = new FileScanner(new StringBufferInputStream(line));
		} catch (Exception e) {
			System.out.println("testWithMixedDataInQuotes");
			e.printStackTrace();
			assertEquals(true, false);
		}

		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm");

		try {
			Date date2 = sdf.parse("04/04/1920 03:56");
			sdf = new SimpleDateFormat("MM/dd/yyyy");
			Date date1 = sdf.parse("04/04/1920");
			Symbol[] expected_value = { scanner.symbol(TokenConstants.STRING_LITERAL, "Stone,Clay,Etc."),
					scanner.symbol(TokenConstants.DOUBLE_LITERAL, new Double("100.258")),
					scanner.symbol(TokenConstants.DOUBLE_LITERAL, new Double("101.555")),
					scanner.symbol(TokenConstants.INTEGER_LITERAL, new Integer("-250")),
					scanner.symbol(TokenConstants.DATE_LITERAL, date1),
					scanner.symbol(TokenConstants.BOOLEAN_LITERAL, new Boolean(true)),
					scanner.symbol(TokenConstants.DATE_TIME_LITERAL, date2),
					scanner.symbol(TokenConstants.NULL_LITERAL, null) };
			assertEquals(compareSymbols(expected_value, scanner.getTokensPerLine(',', false)), true);
			assertEquals(line, scanner.getLine());
		} catch (Exception e) {
			assertEquals(true, false);
		}
	}

	/*
	 * public void testWithTabInData() { String line = "\"C:\temp\tata\nem\\\":abcd:NaN:123.4\n"; FileScanner scanner =
	 * null; try { scanner=new FileScanner(new StringBufferInputStream(line)); } catch(Exception e) {
	 * System.out.println("testWithTabInData"); e.printStackTrace(); assertEquals(true, false); }
	 * System.out.println("testWithTabInData");
	 * 
	 * Symbol[] expected_value = { scanner.symbol(TokenConstants.STRING_LITERAL, "C:\temp\tata\nem\\"),
	 * scanner.symbol(TokenConstants.STRING_LITERAL, "abcd"), scanner.symbol(TokenConstants.DOUBLE_LITERAL, new
	 * Double("NaN")), scanner.symbol(TokenConstants.DOUBLE_LITERAL, "123.4") }; try {
	 * assertEquals(compareSymbols(expected_value, scanner.getTokensPerLine(':')), true); //
	 * assertEquals(line,scanner.getLine()); } catch(IOException ie) { assertEquals(true, false); } }
	 */
	public void testWithSpaceDelimitedData() {
		//TODO:
	}

	public void testWithTabInsideQuotes() {
		//TODO:
	}

	public static Test suite() {
		return new TestSuite(FileScannerTest.class);
	}

}
