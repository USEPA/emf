package gov.epa.mims.analysisengine.table;

import java.util.Vector;

/*
 * MultiLineColumnNameData.java
 * To holde multiple lines of column names
 * Created on March 19, 2004, 12:11 PM
 * @author  Parthee Partheepan
 * @version $Id: MultiLineColumnNameData.java,v 1.3 2006/10/30 21:43:50 parthee Exp $
 */
public class MultiLineColumnNameData {
	/** a place holder for the columns */
	private Vector columnNames;

	private String[] units;

	/** Creates a new instance of ColumnHeaderString */
	public MultiLineColumnNameData(int noOfColumns) {
		columnNames = new Vector();
		for (int i = 0; i < noOfColumns; i++) {
			Vector aColumn = new Vector();
			columnNames.add(aColumn);
		}// for i
		units = new String[noOfColumns];

	}

	/**
	 * A temporary method to get the string contained in the specfic column
	 * 
	 * 
	 */
	public String getMultiLineColumnName(int columnNo) {
		Vector aColumnName = (Vector) columnNames.get(columnNo);
		String name = "";
		int size = aColumnName.size();
		for (int i = 0; i < size - 1; i++) {
			name += (String) aColumnName.get(i) + " || ";
		}// for i
		// last one
		name += (String) aColumnName.get(size - 1);
		return name;
	}// getColumnName(int columnNo)

	public void addUnitName(int columnNo, String unit) {
		units[columnNo] = unit;
	}

	public String getUnitName(int columnNo) {
		return units[columnNo];
	}// getUnitName(int columnNo)

	public void addStringToColumnName(int columnNo, String name) {
		if (columnNo >= columnNames.size()) {
			System.err.println("The column name object size is " + columnNames.size() + " > " + columnNo);
		}// if(columnNo >= columnNames.size())

		Vector aColumnName = (Vector) columnNames.get(columnNo);
		aColumnName.add(name);

	}// addStringColumnName(int columnNo, String name)

}
