package gov.epa.mims.analysisengine.table.io;

/*
 * TRIMResultFileReader.java
 * This class is customized to read TRIM Results file
 * Created on March 31, 2004, 1:38 PM
 * @author  Parthee Partheepan, CEP, UNC-CHAPEL HILL
 * @version $Id: TRIMResultFileReader.java,v 1.2 2006/10/30 21:43:51 parthee Exp $
 */

import java.util.ArrayList;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

public class TRIMResultFileReader extends FileParser {
	public static final int NO_OF_COLUMN_HEADER_ROWS = 2;

	public static final String DELIMITER = ";";

	/** Creates a new instance of TRIMResultFileReader */
	public TRIMResultFileReader(String fileName, boolean ignoreMultDelims) throws Exception {
		super(fileName, DELIMITER, NO_OF_COLUMN_HEADER_ROWS, ignoreMultDelims);
		customizeHeaderData();
		customizeFileData();
	}

	private String[] remove2ndAnd3rdElements(String[] headerData) {

		String[] result = new String[headerData.length - 2];
		result[0] = headerData[0];
		for (int i = 3; i < headerData.length; i++)
			result[i - 2] = headerData[i];
		return result;
	}

	protected void customizeHeaderData() {

		rowHeaderData = new ArrayList();
		rowHeaderData.add("Compartments");
		rowHeaderData.add("Volume Elements");
		rowHeaderData.add("Units");

		String[] columnNameToBeSplit;

		for (int i = 0; i < 2; i++) {
			columnNameToBeSplit = remove2ndAnd3rdElements((String[]) columnHeaderData.get(i));
			columnHeaderData.set(i, columnNameToBeSplit);
		}

		columnNameToBeSplit = (String[]) columnHeaderData.get(0);
		String[] aNameLine1 = new String[valuesPerLine - 2];
		String[] aNameLine2 = new String[valuesPerLine - 2];
		aNameLine1[0] = columnNameToBeSplit[0];
		aNameLine2[0] = " ";

		for (int j = 1; j < valuesPerLine - 2; ++j) {
			String[] tempArray = breakString(columnNameToBeSplit[j]);
			aNameLine1[j] = tempArray[0];
			aNameLine2[j] = tempArray[1];
		}

		columnHeaderData.set(0, aNameLine1);
		columnNameToBeSplit = (String[]) columnHeaderData.remove(1);
		columnHeaderData.add(aNameLine2);
		columnHeaderData.add(columnNameToBeSplit);
	}

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

	protected void customizeFileData() throws ParseException {

		SimpleDateFormat sdf1 = new SimpleDateFormat("MM/dd/yyyy");
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss z");

		Iterator iter = fileData.iterator();
		int i = 0;
		while (iter.hasNext()) {

			ArrayList rowData = (ArrayList) iter.next();
			Date d = (Date) rowData.remove(0);
			StringBuffer date = new StringBuffer(sdf1.format(d));
			date.append(" ");
			date.append((String) rowData.remove(0));
			date.append(" ");
			date.append((String) rowData.get(0));

			try {
				rowData.set(0, sdf.parse(date.toString()));
			} catch (ParseException pe) {
				throw new ParseException("Parsing string: " + date + " is not a date"
						+ "\nIn TRIM Results file first three columns data have information "
						+ "about the date.\nPlease check the file.\n" + fileName, 0);
			}

			fileData.set(i++, rowData);
		}

		Class[] tempColumnTypes = new Class[getColumnCount() - 2];
		tempColumnTypes[0] = Date.class;
		for (i = 3; i < getColumnCount(); i++)
			tempColumnTypes[i - 2] = columnTypes[i];
		columnTypes = tempColumnTypes;
	} // customizefileData

}
