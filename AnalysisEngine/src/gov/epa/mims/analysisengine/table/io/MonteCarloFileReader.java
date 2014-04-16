package gov.epa.mims.analysisengine.table.io;


import java.util.ArrayList;

/*
 * MonteCarloFileReader.java
 * This class is customized to read Monte Carlo inputfile
 * Created on April 1, 2004, 12:31 PM
 * @author  Parthee Partheepan, CEP, UNC-CHAPEL HILL
 * @version $Id: MonteCarloFileReader.java,v 1.1 2006/11/01 15:13:33 parthee Exp $
 */
public class MonteCarloFileReader extends FileParser {
	public static final int NO_OF_COLUMN_HEADER_ROWS = 4;

	public static final String DELIMITER = ",";

	/** Creates a new instance of MonteCarloFileReader */
	public MonteCarloFileReader(String fileName, boolean ignoreMultDelims) throws Exception {
		super(fileName, DELIMITER, NO_OF_COLUMN_HEADER_ROWS, ignoreMultDelims);
		customizeHeaderData();
	}

	protected void customizeHeaderData() {

		rowHeaderData = new ArrayList();
		String[] columnNameToBeSplit;
		int i;
		for (i = 0; i < 2; i++) {
			columnNameToBeSplit = (String[]) columnHeaderData.get(i);
			rowHeaderData.add(columnNameToBeSplit[0]);
			columnNameToBeSplit[0] = " ";
			columnHeaderData.set(i, columnNameToBeSplit);
		}

		if (i == 2) {
			String[] aNameLine1 = new String[valuesPerLine];
			String[] aNameLine2 = new String[valuesPerLine];
			rowHeaderData.add("Volume Elements");
			aNameLine1[0] = " ";
			aNameLine2[0] = " ";
			columnNameToBeSplit = (String[]) columnHeaderData.get(2);

			for (int j = 1; j < valuesPerLine; ++j) {
				String[] tempArray = breakString(columnNameToBeSplit[j]);
				aNameLine1[j] = tempArray[0];
				aNameLine2[j] = tempArray[1];
			}// for(j)

			columnHeaderData.set(i, aNameLine1);
			columnNameToBeSplit = (String[]) columnHeaderData.remove(3);
			columnHeaderData.add(aNameLine2);
			rowHeaderData.add(columnNameToBeSplit[0]);
			columnNameToBeSplit[0] = "Run";
			columnHeaderData.add(columnNameToBeSplit);
		}
	}

	/**
	 * will break a word into two words if that word contain "in". If "aWord" does not contain "in" then it will return
	 * String array with "aWord" with that empty string.
	 * 
	 * @param aWord
	 *            return String [] length is two
	 */
	protected String[] breakString(String aWord) {
		String IN = "in";
		int index = aWord.indexOf(IN);

		if (index != -1) {
			String s1 = aWord.substring(0, index - 1);
			String s2 = aWord.substring(index + IN.length(), aWord.length());
			return new String[] { s1, s2 };
		}
		return new String[] { aWord, "" };
	}// breakString(String aWord)

}
