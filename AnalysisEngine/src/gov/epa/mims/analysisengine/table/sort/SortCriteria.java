package gov.epa.mims.analysisengine.table.sort;

import gov.epa.mims.analysisengine.gui.DefaultUserInteractor;
import gov.epa.mims.analysisengine.gui.UserInteractor;
import gov.epa.mims.analysisengine.table.OverallTableModel;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Arrays;

public class SortCriteria implements java.io.Serializable {

	/** serial version UID */
	static final long serialVersionUID = 1;

	/**
	 * An element is true if the matching column in the columns array should be sorted in ascending order.
	 */
	private boolean[] ascending = null;

	/** The column names of the columns we should sort. */
	private String[] columnNames = null;

	/**
	 * Boolean array where an element is true if the sorting should be case sensitive for the given column.
	 */
	private boolean[] caseSensitive = null;

	/**
	 * Constructor.
	 * 
	 * @param columns
	 *            String[] that contains the names of the columns to sort.
	 * @param ascending
	 *            boolean[] that is true if the given column should be sorted in ascending order.
	 * @param caseSensitive
	 *            boolean[] that is true if the given column should be sorted case sensitive.
	 * @throws IllegalArgumentException
	 *             if the lengths of the three arguemnt arrays are net equal or are null.
	 */
	public SortCriteria(String[] columnNames, boolean[] ascending, boolean[] caseSensitive) {
		// The column names array must not be null.
		if (columnNames == null) {
			throw new IllegalArgumentException("columnnNamescanont be null in SortCriteria().");
		}

		// If the other two arrays are null, then populate them with true
		// values.
		if (ascending == null) {
			ascending = new boolean[columnNames.length];
			Arrays.fill(ascending, true);
		} // if (ascending == null)

		if (caseSensitive == null) {
			caseSensitive = new boolean[columnNames.length];
			Arrays.fill(caseSensitive, true);
		} // if (caseSensitive == null)

		// All three of these arrays *must* be the same length. */
		if (columnNames.length != ascending.length || columnNames.length != caseSensitive.length) {
			throw new IllegalArgumentException("columnnNames, ascending and caseSensitive must all have the "
					+ "same length in SortCriteria().");
		}

		this.ascending = ascending;
		this.columnNames = columnNames;
		this.caseSensitive = caseSensitive;
	} // SortCriteria()

	/**
	 * Return the ascending array.
	 * 
	 * @return boolean[] with true values for each column that should be sorted in ascending order.
	 */
	public boolean[] getAscending() {
		return ascending;
	} // getAscending()

	/**
	 * Return the case sensitive array.
	 * 
	 * @return boolean[] that contains a true value if the column should be sorted case sensitive.
	 */
	public boolean[] getCaseSensitive() {
		return this.caseSensitive;
	} // getCaseSensitive()

	/**
	 * Return the column indices that should be sorted.
	 * 
	 * @return String[]
	 */
	public String[] getColumnNames() {
		return columnNames;
	} // getColumnNames()

	/**
	 * checks whether this criteria is applicable to the OverallTableModel
	 * 
	 * @param model
	 *            OverallTableModel
	 * @return boolean true if compatible else false
	 */
	public SortCriteria checkCompatibility(OverallTableModel model, Component parent) throws Exception {
		String[] colNames = model.getColumnNames();
		ArrayList allColNames = new ArrayList();
		for (int i = 0; i < colNames.length; i++) {
			allColNames.add(colNames[i]);
		}
		boolean[] available = new boolean[columnNames.length];
		int count = 0;
		String missingColNames = "";
		for (int i = 0; i < this.columnNames.length; i++) {
			if (allColNames.contains(columnNames[i])) {
				available[i] = true;
				count++;
			} else {
				missingColNames = missingColNames + columnNames[i] + ", ";
			}
		}// for(i)

		if (count == 0) {
			throw new Exception("The table does not contain any column names "
					+ "specified for sort criteria in the configuration file");
		} else if (count < columnNames.length) {
			missingColNames = missingColNames.substring(0, missingColNames.length() - 2);
			DefaultUserInteractor.get().notify(parent,
					"Sort Criteria",
					"The table does not " + "contain " + missingColNames + " specified for sort criteria in the"
							+ " configuration file", UserInteractor.WARNING);
			String[] newColNames = new String[count];
			boolean[] newAscending = new boolean[count];
			boolean[] newCaseSen = new boolean[count];
			count = 0;
			for (int i = 0; i < columnNames.length; i++) {
				if (available[i]) {
					newColNames[count] = columnNames[i];
					newAscending[count] = ascending[i];
					newCaseSen[count] = caseSensitive[i];
					count++;
				}// if(available[i])
			}// for(i)
			return new SortCriteria(newColNames, newAscending, newCaseSen);
		}// else if(count < columnNames.length)
		else {
			return this;
		}

	}

	public String toString() {
		if (columnNames == null || columnNames.length == 0) {
			return "";// NO SORT;
		}
		StringBuffer sb = new StringBuffer();
		int length = columnNames.length;
		for (int i = 0; i < length - 1; i++) {
			sb.append(columnNames[i]).append("(").append(asceding(ascending[i])).append(")").append(", ");
		}
		sb.append(columnNames[length - 1]).append("(").append(asceding(ascending[length - 1])).append(")");
		return sb.toString();
	}

	private String asceding(boolean b) {
		return (b==true)?"+": "-";
	}

} // class SortCriteria

