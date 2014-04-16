package gov.epa.mims.analysisengine.table.io;

import java.io.FileReader;
import java.util.ArrayList;

import java_cup.runtime.Symbol;

/*
 * FileParser.java
 * This class can be used to read any generic file in the following format with
 * help of FileScanner.java
 * -----------------------------------------------
 * | TABLE HEADER                                 |
 * | ------------------------                     |
 * | COLUMNNAME HEADER                            |
 * | ------------------------                     |
 * | BLANK SPACES                                 |
 * | ------------------------                     |
 * |                                              |
 * | DATA OF THE TABLE                            |
 * |                                              |
 * |                                              |
 * |                                              |
 * | -------------------------                    |
 * | TABLE FOOTER                                 |
 * |                                              |
 * ------------------------------------------------
 * Column name and data should be seperated by the same delimiter
 * No of columns in data should be equal to no columns in names
 * Two consequtive lines should have same no of tokens
 * So if the file has one data line and one column name line then there should
 * not be a space between a column name line and a data line
 * @see FileScanner.java
 * @see FileAttributes.java
 * Created on March 31, 2004, 10:11 AM
 *
 * @author  Krithiga Thangavelu, CEP, UNC CHAPEL HILL.
 * @version $Id: FileParser.java,v 1.5 2008/05/02 19:50:00 eyth Exp $
 */

public class FileParser {

	/** file name to be read */
	protected String fileName;

	/** delimiter for the file */
	protected String delimiter;

	// --- to store the data regarding the file --- //
	/** A String to store the file header information */
	protected String fileHeader = null;

	/* type of each columns */
	protected Class[] columnTypes = null; // not necessary as objects are

	// stored in their formats

	/** store the column headers */
	protected ArrayList columnHeaderData = null;

	/** store the row header */
	protected ArrayList rowHeaderData = null;

	/** store the data of the file */
	protected ArrayList fileData = null;

	/** A String to store the file footer information */
	protected String fileFooter = null;

	/** An int to store number of columns in the file */
	protected int valuesPerLine = 0;

	/** A string to store warnings encountered during file parsing */
	protected String logger = null;

	/** An integer to store number of rows of column header data */
	private int noOfColumnHeaderRows = 0;

	/** ignore multipleOccurences of delimiter or not */
	boolean multipleOccurences;

	/**
	 * Creates a new instance of FileParser
	 * 
	 * @param fileName
	 *            String
	 * @param delimiter
	 *            String
	 * @param noOfColumnNameRows
	 */
	public FileParser(String fileName, String delimiter, int noOfColumnHeaderRows, boolean multipleOccurences)
			throws Exception {
		this.fileName = fileName;
		if (delimiter.length() > 1) {
			throw new Exception("Only single character delimiters are supported!");
		}
		this.delimiter = delimiter;
		// this.noOfColumnHeaderRows = noOfColumnHeaderRows;
		this.multipleOccurences = multipleOccurences;
		readAndStoreData(new FileReader(fileName));
	}

	/**
	 * creates a new instance of FileParser For use of creating readers as an extension of FileParser without using the
	 * parsing functionality of FileParser
	 */
	public FileParser() {
		// Empty
	}

	/**
	 * create an instance of FileParser which can read data from a stream handed out instead from a FileName
	 */
	public FileParser(java.io.Reader reader, String delimiter, int noOfColumnHeaderRows, boolean multipleOccurences)
			throws Exception {
		if (delimiter.length() > 1) {
			throw new Exception("Only single character delimiters are supported!");
		}
		this.delimiter = delimiter;
		// this.noOfColumnHeaderRows = noOfColumnHeaderRows;
		this.multipleOccurences = multipleOccurences;
		readAndStoreData(reader);
	}

	/**
	 * this method will open the file, read and store the data
	 * 
	 * To fetch the data, user the getX() methods.
	 * 
	 * @param None
	 * 
	 */

	protected void readAndStoreData(java.io.Reader reader) throws Exception {
		FileScanner scanner = new FileScanner(reader);
		Symbol[] line1Tokens, line2Tokens;
		String line1, line2;
		StringBuffer tempBuffer = new StringBuffer();
		StringBuffer log = new StringBuffer();
		char delim = delimiter.charAt(0);
		// System.out.println("delimiter="+delim);
		// Get the first line
		line1Tokens = scanner.getTokensPerLine(delim, multipleOccurences);
		line1 = scanner.getLine();
		// If there is no first line then file is empty
		if (line1Tokens == null) {
			throw new Exception("Empty File\n");
		}

		// this variable to account for ONLY empty lines between first line and
		// second line
		// (line2Tokens.length == 1 => no delimiter found in this line)
		// (line2Tokens[0].sym == TokenConstants.NULL_LITERAL => Empty Line)
		int emptylines = -1;
		do {
			emptylines++;
			line2Tokens = scanner.getTokensPerLine(delim, multipleOccurences);
			line2 = scanner.getLine();
		} while (line2Tokens != null && line2Tokens.length == 1 && line2Tokens[0].sym == TokenConstants.NULL_LITERAL);

		// if end of file encountered in line 2 of the file
		// ie file contains only a single valid line
		if (line2Tokens == null) {
			// add the first line to header and return
			tempBuffer.append(line1); // fileHeader+=line1;
			// add new line charactors for number of empty lines
			while (emptylines-- > 0) {
				tempBuffer.append("\n");
			}
			fileHeader = tempBuffer.toString();
			log.append("WARNING: One line file - Header only");
			logger = log.toString();
			fileFooter = new String("");
			return;
		}

		// At this point
		// 1.we are not sure whether line1 or line2 are header lines
		// 2.we got line1Tokens and line2Tokens with tokens
		// line2Tokens != null
		// Header lines without delimiter in them will be returned as single
		// token per line
		// Find two consecutive lines with matching number(>1) of tokens
		// (Assumption: a file should consist at least two columns)
		// until then, add all encountered lines to header
		while (line1Tokens.length != line2Tokens.length || line1Tokens.length == 1) {
			// at this point noOfColumnHeaderRows = 0, => we should take out the
			// noOfColumnHeaderRows from the constructor
			if (noOfColumnHeaderRows <= 1 && line2Tokens.length == 1 && line1Tokens.length > 1) {
				while (emptylines-- > 0) {
					line2 += "\n";
				}

				// check whether there are empty lines following line2Tokens
				// line
				Symbol[] tokens = null;
				do {
					tokens = scanner.getTokensPerLine(delim, multipleOccurences);
					emptylines++;
				} while (tokens != null && tokens.length == 1 && tokens[0].sym == TokenConstants.NULL_LITERAL);

				// if no tokens returned for the delimiter
				if (tokens == null) {
					tempBuffer.append(line1);
					tempBuffer.append(line2);
					fileHeader = tempBuffer.toString();
					// Unexpected end of file encountered while
					// processing Header
					log.append("WARNING: Possibly wrong delimiter " + delim + " specified: Only file header found\n");
					log.append("\nFile Content upto 50 Lines:\n" + get50Lines(tempBuffer));
					logger = log.toString();
					fileFooter = null; // ??
					return;
				} // if (tokens == null)

				else if (tokens.length == line1Tokens.length) // tokens belong
				// to a data
				// line
				{
					log.append("Non Data Line between Header and Data.\n");
					log.append(line2);
					line2Tokens = tokens;
					line2 = scanner.getLine();
					break;
				}// else if (tokens.length == line1Tokens.length)
				else
				// (tokens != null && tokens.length != line1Tokens.length)
				// empty lines between line1 and line2 ??
				{
					tempBuffer.append(line1);
					while (emptylines-- > 0) {
						tempBuffer.append("\n");
					}// while (emptylines-- > 0)
					tempBuffer.append(line2);
					line2Tokens = tokens;
					line2 = scanner.getLine();
				}// else
			} // if (noOfColumnHeaderRows <= 1 && line2Tokens.length == 1 &&
			// line1Tokens.length > 1)
			else {
				tempBuffer.append(line1); // fileHeader+=line1;
				while (emptylines-- > 0) {
					tempBuffer.append("\n");
				}// while (emptylines-- > 0)
			}// else

			line1 = line2;
			line1Tokens = line2Tokens;
			boolean emptyLine = false;
			emptylines = 0;
			do {
				line2Tokens = scanner.getTokensPerLine(delim, multipleOccurences);
				if (line2Tokens == null) {
					fileHeader = tempBuffer.toString();

					// Unexpected end of file encountered while
					// processing Header
					log.append("WARNING: Possibly wrong delimiter " + delim + " specified: Only file header found");
					log.append("File Content up to 50 Lines:\n" + get50Lines(tempBuffer));
					logger = log.toString();
					fileFooter = null;
					return;
				}// if (line2Tokens == null)

				if (line2Tokens.length == 1 && line2Tokens[0].sym == TokenConstants.NULL_LITERAL) {
					emptyLine = true;
					emptylines++;
				}// if (line2Tokens.length == 1....
				else {
					emptyLine = false;
				}// else
			}// do
			while (emptyLine);
			line2 = scanner.getLine();
		}// while (line1Tokens.length != line2Tokens.length ||
		// line1Tokens.length == 1)
		fileHeader = tempBuffer.toString();

		// line1 and line2 are lines with matching number of tokens
		tempBuffer.setLength(0); // to log warnings
		valuesPerLine = line1Tokens.length;
		// read noOfColumnHeaderRows of column header information and
		// populate columnHeaderData
		/*
		 * if (noOfColumnHeaderRows == 0) { columnHeaderData = null; } else {
		 */
		columnHeaderData = new ArrayList();
		// }

		noOfColumnHeaderRows = 0;
		boolean stillInColumnHeader = true;
		boolean firstTimeDoubleORInteger = true;

		while (stillInColumnHeader) {
			// the first time inside the loop, you won't get inside if elseif
			// loop
			if (noOfColumnHeaderRows == 1) {
				line1Tokens = line2Tokens;
			}// if (noOfColumnHeaderRows == 1)
			else if (noOfColumnHeaderRows != 0) // ie noOfColumnHeaderRows
			// >=1???RP
			{
				do {
					line1Tokens = scanner.getTokensPerLine(delim, multipleOccurences);
				} while (line1Tokens != null && line1Tokens.length == 1
						&& line1Tokens[0].sym == TokenConstants.NULL_LITERAL);
			}// else if (noOfColumnHeaderRows != 0)

			if (line1Tokens != null && line1Tokens.length != valuesPerLine) {
				// System.out.println("line1Tokens BEOFRE = " + line1Tokens);
				while (line1Tokens != null && line1Tokens.length != valuesPerLine) {
					// System.out.println("line1Tokens AFTER = " + line1Tokens);
					tempBuffer.append(scanner.getLine());
					line1Tokens = scanner.getTokensPerLine(delim, multipleOccurences);
				}
				stillInColumnHeader = false;
				continue;
				// throw new Exception(
				// "Possibly wrong input of #columnheaderRows: Expected "
				// + +valuesPerLine + " values; Found "
				// + line1Tokens.length
				// + " values\n Unexpected end of column header data in line "
				// + (line1Tokens[0].left + 1));
				// Error --> incorrect format of column header
			}// if (line1Tokens.length != valuesPerLine)
			String[] columnNamesPerRow = new String[valuesPerLine];
			if (line1Tokens != null) {
				Symbol examinedToken;
				for (int j = 0; j < valuesPerLine; j++) {
					examinedToken = line1Tokens[j];

					if (examinedToken.sym == TokenConstants.NULL_LITERAL) {
						columnNamesPerRow[j] = " ";
					} else {
						if (examinedToken.sym != TokenConstants.STRING_LITERAL
								&& examinedToken.sym != TokenConstants.SPACE) {
							// System.out.println("non string literal " +
							// examinedToken.sym);
							if (firstTimeDoubleORInteger && noOfColumnHeaderRows == 0) {
								// System.out.println("first Time " + j);
								if (examinedToken.sym == TokenConstants.INTEGER_LITERAL) {
									columnNamesPerRow[j] = ((Integer) examinedToken.value).toString();
									// System.out.println("Integer =
									// "+columnNamesPerRow[j]);
								} else if (examinedToken.sym == TokenConstants.DOUBLE_LITERAL) {
									columnNamesPerRow[j] = ((Double) examinedToken.value).toString();
									// System.out.println("Double = "
									// +columnNamesPerRow[j]);
								}
								if (j == valuesPerLine - 1)// turn of flag once
								// u read the first
								// line
								{
									firstTimeDoubleORInteger = false;
								}
							}// if (firstTimeDoubleORInteger)
							else {
								stillInColumnHeader = false;
								break;
							}// else
						}// if (examinedToken.sym !=
						// TokenConstants.STRING_LITERAL)
						else {
							columnNamesPerRow[j] = ((String) examinedToken.value).trim();
						}// else
					}// else
				}// for(i)
				if (stillInColumnHeader) {
					columnHeaderData.add(columnNamesPerRow);
					noOfColumnHeaderRows++;
				}// if(stillInColumnHeader)
			}// if(token1Tokens != null)
			else {
				stillInColumnHeader = false;
			}
		}// while(stillInColumnHeader)

		if (noOfColumnHeaderRows > 0 && line1Tokens == null) {
			throw new Exception(
					"The file contains at least one data column based on the header, but no delimiters were found" +
					" on the first line.\n"+
					"Please verify that you selected the proper delimiter during import.\n"+
					"The first 50 lines of the file are:\n");
			
		}
		// First determine the columnTypes
		// This is done to get convert NullTokens into appropriate types
		boolean temp[] = new boolean[valuesPerLine];
		boolean classDetermined = false;
		columnTypes = new Class[valuesPerLine];
		for (int i = 0; i < valuesPerLine; i++) {
			columnTypes[i] = String.class;
			temp[i] = false;
		}

		Symbol examinedToken;
		ArrayList linetokens = new ArrayList();

		// for special case, where a column has mixed type of integer and
		// double..convert all objects to Double
		ArrayList numCols = new ArrayList(); // for tracking indexes of
		// columns with numbers
		boolean[] requiredToBeDouble = new boolean[valuesPerLine]; // requiredToBeDouble[columnNo]==true
		// for those
		// which
		// have
		// mixed
		// types

		for (int i = 0; i < valuesPerLine; i++) {
			requiredToBeDouble[i] = false;
		}

		// /Determine where the data starts
		switch (noOfColumnHeaderRows) {
		case 0: // no header - start of data from line1
			for (int j = 0; j < valuesPerLine; j++) {
				examinedToken = line1Tokens[j];
				if (examinedToken.sym != TokenConstants.NULL_LITERAL) {
					if (examinedToken.sym == 2 || examinedToken.sym == 3) {
						numCols.add(new Integer(j));
					}
					if (examinedToken.sym > 6 || examinedToken.sym < 0)
						columnTypes[j] = String.class;
					else
						columnTypes[j] = TokenConstants.TypeToClass[examinedToken.sym];
					temp[j] = true;
				}
			}
			linetokens.add(line1Tokens);
			classDetermined = areClassTypesDetermined(temp);
		case 1: // one line header ; data starts from line2
			if (!classDetermined) {
				for (int j = 0; j < valuesPerLine; j++) {
					examinedToken = line2Tokens[j];
					if (examinedToken.sym != TokenConstants.NULL_LITERAL) {
						if (temp[j] == false) {
							if (examinedToken.sym == 2 || examinedToken.sym == 3) {
								numCols.add(new Integer(j));
							}
							if (examinedToken.sym > 6 || examinedToken.sym < 0)
								columnTypes[j] = String.class;
							else
								columnTypes[j] = TokenConstants.TypeToClass[examinedToken.sym];
							temp[j] = true;
						} else {
							if (columnTypes[j].equals(Double.class) || columnTypes[j].equals(Integer.class)) {
								if (!columnTypes[j].equals(TokenConstants.TypeToClass[examinedToken.sym])) {
									requiredToBeDouble[j] = true;
								}
							}
						}
					}
				}
				classDetermined = areClassTypesDetermined(temp);
			}
			linetokens.add(line2Tokens);
			break;
		default:
			for (int j = 0; j < valuesPerLine; j++) {
				examinedToken = line1Tokens[j];
				if (examinedToken.sym != TokenConstants.NULL_LITERAL) {
					if (examinedToken.sym == 2 || examinedToken.sym == 3) {
						numCols.add(new Integer(j));
					}

					if (examinedToken.sym > 6 || examinedToken.sym < 0)
						columnTypes[j] = String.class;
					else {
						columnTypes[j] = TokenConstants.TypeToClass[examinedToken.sym];

					}
					temp[j] = true;
				}
			}
			linetokens.add(line1Tokens);
			classDetermined = areClassTypesDetermined(temp);
		}// switch (noOfColumnHeaderRows)

		while ((line1Tokens = scanner.getTokensPerLine(delim, multipleOccurences)) != null) {
			if (valuesPerLine != line1Tokens.length) {
				line1 = scanner.getLine();
				if (linetokens.size() == 0) {
					tempBuffer.append(line1);
					continue;
					// data line not yet started
				}
				if (line1Tokens.length == 1 && line1Tokens[0].sym == TokenConstants.NULL_LITERAL) {
					continue;
				} // skip empty line
				// we can continue if we want by skipping lines with
				// improper number of tokens
				line2Tokens = scanner.getTokensPerLine(delim, multipleOccurences);
				if (line2Tokens == null) {
					break;
				}
				if (line2Tokens.length == valuesPerLine) {
					log.append("Truncated line " + ((line1Tokens[0].left) + 1) + ":\n" + line1);
					line1Tokens = line2Tokens;
				} else {
					break;
				} // end of data
			}

			if (!classDetermined) {
				for (int j = 0; j < valuesPerLine; j++) {
					examinedToken = line1Tokens[j];
					if (examinedToken.sym != TokenConstants.NULL_LITERAL) {
						if (temp[j] == false) {
							columnTypes[j] = TokenConstants.TypeToClass[examinedToken.sym];
							if (examinedToken.sym == 2 || examinedToken.sym == 3) {
								numCols.add(new Integer(j));
							}
							temp[j] = true;
						} else if (!requiredToBeDouble[j] && numCols.size() > 0) {
							if (!columnTypes[j].equals(TokenConstants.TypeToClass[examinedToken.sym])) {
								requiredToBeDouble[j] = true;
							}
						}
					}
				}
				classDetermined = areClassTypesDetermined(temp);
			} else {
				// verification for Double to Integer
				for (int j = 0; j < numCols.size(); j++) {
					int k = ((Integer) numCols.get(j)).intValue();

					if (!requiredToBeDouble[k]) {
						if (!columnTypes[k].equals(TokenConstants.TypeToClass[line1Tokens[k].sym])) {
							requiredToBeDouble[k] = true;
						}
					}
				}
			}// else
			linetokens.add(line1Tokens);
		}// while ((line1Tokens = scanner.getTokensPerLine(delim,
		// multipleOccurences)) != null)

		if (numCols.size() > 0) {
			for (int l = 0; l < valuesPerLine; l++) {
				if (requiredToBeDouble[l]) {
					columnTypes[l] = Double.class;
				}
			}
		}// if (numCols.size() > 0)

		// end of data line
		// check for correct estimation of class types -- doing it for a random
		// line for performance
		if (linetokens.size() >= 1) { // if there is data
			Symbol[] linetoken = (Symbol[]) linetokens.get(linetokens.size() - 1);
			for (int idx = 0; idx < valuesPerLine; idx++) {
				if (linetoken[idx].sym != TokenConstants.NULL_LITERAL) {
					// check to see what happens if an integer is left empty
					if (!(columnTypes[idx].equals(linetoken[idx].value.getClass()))) {
//					if (!(columnTypes[idx].equals(linetoken[idx].value.getClass())) && !requiredToBeDouble[idx]) {
						if (requiredToBeDouble[idx] && linetoken[idx].value.getClass()==Integer.class)
						{
							// do nothing, this is not a problem and is handled later
						}
						else if (columnTypes[idx].toString().contains("tring"))
						{
							// if it's looking for a string but it's an integer (for example), 
							// then just convert the value to a string
							linetoken[idx].value = linetoken[idx].value.toString();
							requiredToBeDouble[idx]=false; // should already be false, but just to be sure
						}
						else
						{
							//convert the column to a String column
							System.out.println("Converting column "+idx+" to String from "+columnTypes[idx]+" due to value "+linetoken[idx].value);
							columnTypes[idx]=String.class;
							linetoken[idx].value = linetoken[idx].value.toString();
							requiredToBeDouble[idx] = false;
//						   throw new Exception(
//								"Possibly specified the wrong input format: Type mismatch in column data. Column no:"
//										+ (l + 1) + " Row: " + linetokens.size() + " \nExpected type " + columnTypes[l]
//										+ " but found " + linetoken[l].value.getClass() + "\n");
						}
					}
				}
			}
			if (!storeFileData(linetokens, requiredToBeDouble))
			{
				// a column was converted
				for (int i = 0; i < requiredToBeDouble.length; i++)
				{
					// reset which ones are still required to be double
					requiredToBeDouble[i] = (columnTypes[i]==Double.class);
				}
			}
		}// if (linetokens.size() > 1)

		// if end of file, time to return
		if (line1Tokens == null) {
			fileFooter = tempBuffer.toString();
			if (fileFooter == null) {
				log.append("No File Footer");
				fileFooter = new String("");
			}

			if (fileData == null || fileData.size() == 0) {
				log.append("Possibly wrong delimiter specified: No data was found");
			}
			logger = log.toString();
			return; // either only footer or only data
		}// if (line1Tokens == null)

		// Time to read footer and log extraneous lines encountered during file
		// processing
		if (tempBuffer.length() > 0) {
			log.append("There are lines without data line between the header and data. \n" + tempBuffer.toString());
			tempBuffer.setLength(0); // whatever stored in FileFooter so far
			// were improper data found between
			// Column Header and Data
		}// if (tempBuffer.length() > 0)

		if (line1Tokens != null) {
			tempBuffer.append(line1);
		}// if (line1Tokens != null)

		do {
			tempBuffer.append(scanner.getLine());
		} while ((line1Tokens = scanner.getTokensPerLine(delim, multipleOccurences)) != null);

		fileFooter = tempBuffer.toString();
		// end of footer
		logger = log.toString();
	}// readAndStoreData()

	/**
	 * get the top 50 lines in the header and return as String
	 * 
	 * @param StringBuffer
	 * 
	 * @return String
	 * 
	 */

	String get50Lines(StringBuffer buf) {
		int numLines = 0;
		int index = 0;
		while (numLines < 50 && index < buf.length()) {
			index = buf.indexOf("\n", index) + 1;
			numLines++;
		}
		return buf.substring(0, index);
	}

	/**
	 * moved storeFileData to separate method for readability purposes. from readAndStoreData.
	 * 
	 * @param ArrayList -
	 *            list of single element array of tokens per line
	 */
	boolean storeFileData(ArrayList linetokens, boolean[] doubleTypeColumns) throws Exception {
		ArrayList rowValues;
		boolean retVal = true;
		int numDataLines = linetokens.size();
		int dataLines = linetokens.size();
		if (numDataLines > 0) {
			fileData = new ArrayList();
		}
		while (numDataLines-- > 0) {
			Symbol[] tokens = (Symbol[]) linetokens.remove(0);
			rowValues = new ArrayList();
			int j = 0;
			for (j = 0; j < valuesPerLine; j++) {
				if (doubleTypeColumns[j] && tokens[j].sym != TokenConstants.DOUBLE_LITERAL) {
					if (tokens[j].sym == TokenConstants.INTEGER_LITERAL) {
						rowValues.add(new Double(((Integer) tokens[j].value).intValue()));
					} else if (tokens[j].sym == TokenConstants.NULL_LITERAL) {
						rowValues.add(new Double("NaN"));
					} else if (tokens[j].sym != TokenConstants.DOUBLE_LITERAL) {
						if (tokens[j].sym == TokenConstants.STRING_LITERAL) {
							if (columnTypes[j]==String.class) // let it be a string since it's not a mismatch
							{
								rowValues.add(tokens[j].value);
							}
							else if (tokens[j].value.toString().trim().length()==0)
							{
								rowValues.add(new Double("NaN"));
							}
							else 
							{
								try {
									rowValues.add(new Double((String) tokens[j].value));
								} catch (Exception e) {
									rowValues.add(tokens[j].value);
									retVal = false;
									System.out.println("Converting column "+j+" to String from "+columnTypes[j]+" due to value "+tokens[j].value);
									this.columnTypes[j]=String.class;
	//								throw new Exception("Type mismatch of data found in column :" + (j + 1) + " row "
	//										+ (dataLines - numDataLines + 1) + " Expected Double but found "
	//										+ TokenConstants.printType(tokens[j].sym) + " Value: " + tokens[j].value);
								}
							}
						}
					}
					continue;
				}

				if (tokens[j].sym == TokenConstants.NULL_LITERAL) {
					rowValues.add(getNullToken(columnTypes[j]));
				} else {
					if (tokens[j].sym == TokenConstants.STRING_LITERAL)
						rowValues.add(((String) tokens[j].value).trim());
					else
						rowValues.add(tokens[j].value);
				}
			}
			fileData.add(rowValues);
		}
		return retVal;
	}

	/**
	 * check for whether class types for all columns are determined
	 * 
	 * based on a boolean array
	 * 
	 * @param boolean[] -
	 *            each element corresponds to one column
	 * 
	 * @return boolean true or false
	 * 
	 */

	boolean areClassTypesDetermined(boolean[] temp) {
		for (int i = 0; i < temp.length; i++) {
			if (temp[i] == false) {
				return false;
			}
		}
		return true;
	}

	/**
	 * getter for Null Token of a particular class type
	 * 
	 * @param Class
	 *            type
	 * @return java.lang.Object
	 */
	public static Object getNullToken(Class type) throws Exception {
		if (type.equals(Double.class)) {
			return new Double("NaN");
		}
		if (type.equals(String.class)) {
			return new String("");
		}
		if (type.equals(Integer.class)) {
			return new Integer(Integer.MIN_VALUE);
		}
		throw new Exception("Missing data, but there is no equivalent Null Object of " + type + " type");
	}

	/**
	 * getter for log
	 * 
	 * @return String
	 * 
	 */

	public String getLogMessages() {
		if (logger == null) {
			return null;
		}
		if (logger.trim().length() == 0) {
			return null;
		}
		return logger;
	}

	/**
	 * getter for file name
	 * 
	 * @return String
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * getter for file header information
	 * 
	 * @return String
	 */
	public String getFileHeader() {
		return this.fileHeader;
	}

	/**
	 * getter for column count
	 * 
	 * @return int
	 */
	public int getColumnCount() {
		return this.valuesPerLine;
	}

	/**
	 * getter for column header data
	 * 
	 * @return ArrayList
	 */
	public ArrayList getColumnHeaderData() {
		return this.columnHeaderData;
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
	 * getter for row header data
	 * 
	 * @return ArrayList
	 */
	public ArrayList getRowHeaderData() {
		return this.rowHeaderData;
	}

	/**
	 * getter for file header information
	 * 
	 * @return ArrayList
	 */
	public ArrayList getFileData() {
		return this.fileData;
	}

	/**
	 * getter for file header information
	 * 
	 * @return String
	 */
	public String getFileFooter() {
		return this.fileFooter;
	}

	public static void main(String[] argv) {
		try {
			FileParser parser = new FileParser(argv[1], argv[0], Integer.parseInt(argv[2]), false);

			System.out.println("File Header\n================\n" + parser.getFileHeader());
			System.out.println("\n\nColumn Name Rows \n ============\n");
			ArrayList result = parser.getColumnHeaderData();
			StringBuffer line_construct = new StringBuffer();

			if (result != null) {
				for (int i = 0; i < result.size(); i++) {
					String[] array = (String[]) result.get(i);

					line_construct.setLength(0);
					for (int j = 0; j < parser.getColumnCount(); j++) {
						line_construct.append(array[j] + "\t");
					}
					System.out.println(line_construct.toString());
				}
			}
			System.out.println("\n\nRow Header Data \n ============\n");
			result = parser.getRowHeaderData();
			if (result != null) {
				for (int i = 0; i < result.size(); i++) {
					ArrayList array = (ArrayList) result.get(i);
					line_construct.setLength(0);
					for (int j = 0; j < parser.getColumnCount(); j++) {
						line_construct.append(array.get(j) + "\t");
					}
					System.out.println(line_construct.toString());
				}
			}
			System.out.println("\n\nFile Data \n ============\n");
			result = parser.getFileData();
			if (result != null) {
				for (int i = 0; i < result.size(); i++) {
					ArrayList array = (ArrayList) result.get(i);
					line_construct.setLength(0);
					for (int j = 0; j < parser.valuesPerLine; j++) {
						line_construct.append(array.get(j) + "\t");
					}
					System.out.println(line_construct.toString());
				}
			}
			System.out.println("Column Types\n=============\n");
			Class[] classes = parser.getColumnClass();
			for (int i = 0; i < classes.length; i++)
				System.out.println(classes[i]);
			System.out.println("File Footer\n================\n" + parser.fileFooter);
			System.out.println("Log file\n=========\n" + parser.getLogMessages());
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
}
