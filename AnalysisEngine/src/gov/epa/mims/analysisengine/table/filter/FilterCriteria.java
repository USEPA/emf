package gov.epa.mims.analysisengine.table.filter;

import gov.epa.mims.analysisengine.gui.DefaultUserInteractor;
import gov.epa.mims.analysisengine.gui.UserInteractor;
import gov.epa.mims.analysisengine.table.OverallTableModel;
import gov.epa.mims.analysisengine.table.format.FormatAndIndexInfoIfc;

import java.io.Serializable;
import java.text.Format;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.awt.Component;

public class FilterCriteria implements Serializable, Cloneable {

	static final long serialVersionUID = 1;

	public static final String NO_FILTER = "No Filter";

	public static final int STARTS_WITH = 0;

	public static final int NOT_STARTS_WITH = 1;

	public static final int CONTAINS = 2;

	public static final int NOT_CONTAINS = 3;

	public static final int ENDS_WITH = 4;

	public static final int NOT_ENDS_WITH = 5;

	public static final int EQUAL = 6;

	public static final int NOT_EQUAL = 7;

	public static final int GREATER_THAN = 8;

	public static final int LESS_THAN = 9;

	public static final int GREATER_THAN_OR_EQUAL = 10;

	public static final int LESS_THAN_OR_EQUAL = 11;

	public static final String[] OPERATION_STRINGS = { "starts with", "does not start with", "contains",
			"does not contain", "ends with", "does not end with", "=", "not =", ">", "<", ">=", "<=" };

	public static final Hashtable symbolToConstantHash = new Hashtable();

	public static final Hashtable constantToSymbolHash = new Hashtable();

	static {
		for (int i = 0; i < OPERATION_STRINGS.length; i++) {
			symbolToConstantHash.put(OPERATION_STRINGS[i], Integer.valueOf(i));
			constantToSymbolHash.put(Integer.valueOf(i), OPERATION_STRINGS[i]);
		}
	} // static

	private String[] columnNames = null;

	private int[] operations = null;

	private Comparable[] values = null;

	private Class[] allColumnClasses = null;

	private String[] allColumnNames = null;

	protected boolean compareWithAnd = true;

	private boolean[] showColumns = null;

	private transient FormatAndIndexInfoIfc model = null;

	private boolean applyFilters = true;

	/**
	 * Constructor or filtering Rows.
	 * 
	 * @param columnName
	 *            String[] that is the column to filter on.
	 * @param operation
	 *            int[] that is one of the operation contants.
	 * @param value
	 *            Comparable[] that is the filter cutoff.
	 * @param formats
	 *            Format[] with one formatter for each value.
	 * @param allColumnNames
	 *            String[] that is the list of all columns in the table. ( this is to keep the filter columns operations
	 *            from seeing a null value. )
	 * @param comparison
	 *            boolean that is true if we should compare with "AND" and false if we should compare with "OR".
	 */
	public FilterCriteria(String[] columnNames, int[] operations, Comparable[] values, String[] allColumnNames,
			boolean comparison) {
		if (columnNames == null) {
			throw new IllegalArgumentException("columnName cannot be null in FilterCriteria() constructor!");
		}
		if (values == null) {
			throw new IllegalArgumentException("value cannot be null in FilterCriteria() constructor!");
		}

		if (columnNames.length != operations.length || columnNames.length != values.length) {
			throw new IllegalArgumentException("All four arrays (columnNames, operations, values and formats "
					+ "must be of equal length in FilterCriteria() constructor!");
		}
		this.columnNames = columnNames;
		this.operations = operations;
		this.values = values;
		this.allColumnNames = allColumnNames;
		this.compareWithAnd = comparison;
		this.showColumns = new boolean[allColumnNames.length];
		Arrays.fill(showColumns, true);
	}

	public void setTableModel(FormatAndIndexInfoIfc model) {
		this.model = model;
	}

	public FilterCriteria checkCompatibility(OverallTableModel newModel, Component parent) throws Exception {
		if (columnNames == null) {
			String[] tempColumnNames = newModel.getBaseColumnNames();
			return new FilterCriteria(tempColumnNames, showColumns);
		}

		boolean showError = true;
		String[] allColNames = newModel.getColumnNames();
		ArrayList allColNamesList = new ArrayList(Arrays.asList(allColNames));
		
		boolean[] available = new boolean[this.columnNames.length];
		int count = 0;
		String missingColNames = "";
		for (int i = 0; i < this.columnNames.length; i++) {
			if (allColNamesList.contains(columnNames[i])) {
				available[i] = true;
				count++;
			} else {
				missingColNames = missingColNames + columnNames[i] + ", ";
			}
		}

		if (count == 0 && showError) {
			showError = false;
			throw new Exception("The table does not contain any column names "
					+ "specified for filter criteria in the configuration file");
		} else if (count < columnNames.length) {
			missingColNames = missingColNames.substring(0, missingColNames.length() - 2);
			String errorString = 
				"The table does not contain these columns specified for filter criteria in the configuration file: \n" 
				+ missingColNames;
			if (showError)
			{
			    DefaultUserInteractor.get().notify(parent,"Problem Loading Filter Criteria",
					errorString, UserInteractor.WARNING);
			}
			else
			{
				System.out.println("WARNING: "+errorString);
			}
			String[] newColNames = new String[count];
			int[] newOperations = new int[count];
			Comparable[] newValues = new Comparable[count];
			count = 0;
			for (int i = 0; i < columnNames.length; i++) {
				if (available[i]) {
					newColNames[count] = columnNames[i];
					// System.out.println("columnNames["+i+"]="+columnNames[i]);
					newOperations[count] = operations[i];
					newValues[count] = values[i];
					count++;
				}// if(available[i])
			}// for(i)
			
			showError = false;
			return new FilterCriteria(newColNames, newOperations, newValues, allColNames, this.compareWithAnd);
		}// else if(count < columnNames.length)
		else {
			return new FilterCriteria(columnNames, operations, values, allColNames, this.compareWithAnd);
		}
	}

	public FilterCriteria(String[] allColumnNames, boolean[] show) {
		this.allColumnNames = allColumnNames;
		this.showColumns = show;
	}

	public boolean accept(Comparable[] rowData) {
		// Set the initial value to True if we are cojmparing with AND and
		// false if we are comparing with OR.
		boolean retval = compareWithAnd;
		boolean tmp = true;
		if (columnNames != null)
			for (int i = 0; i < columnNames.length; i++) {
				int index = model.getColumnNameIndex(columnNames[i]);
				Class colClass = model.getColumnClass(index + 1);

				boolean doubleValue = isDoubleValue(values[i]);

				Object data = rowData[index];
				if (colClass.equals(Double.class) && doubleValue) {
					double d1 = Double.valueOf(values[i].toString()).doubleValue();
					double d2 = ((Double) data).doubleValue();

					if (!Double.isNaN(d1) && Double.isNaN(d2)) {
						return false;
					}
				}
				Format format = model.getFormat(columnNames[i]);
				String formatData = format.format(data);

				switch (operations[i]) {
				case GREATER_THAN:
					tmp = (values[i].compareTo(data) < 0);
					break;
				case LESS_THAN:
					tmp = (values[i].compareTo(data) > 0);
					break;
				case GREATER_THAN_OR_EQUAL:
					tmp = (values[i].compareTo(data) <= 0);
					break;
				case LESS_THAN_OR_EQUAL:
					tmp = (values[i].compareTo(data) >= 0);
					break;
				case EQUAL:
					tmp = (values[i].equals(data));
					break;
				case NOT_EQUAL:
					tmp = !(values[i].equals(data));
					break;
				case STARTS_WITH:
					tmp = (formatData.startsWith(values[i].toString()));
					break;
				case NOT_STARTS_WITH:
					tmp = (!formatData.startsWith(values[i].toString()));
					break;
				case CONTAINS:
					tmp = (formatData.indexOf(values[i].toString()) > -1);
					break;
				case NOT_CONTAINS:
					tmp = (!(formatData.indexOf(values[i].toString()) > -1));
					break;
				case ENDS_WITH:
					tmp = (formatData.endsWith(values[i].toString()));
					break;
				case NOT_ENDS_WITH:
					tmp = (!formatData.endsWith(values[i].toString()));
					break;
				default:
					throw new IllegalArgumentException("Bad operation " + operations[i] + " in accept()!");
				}

				if (compareWithAnd) {
					retval = retval && tmp;
				} else {
					retval = retval || tmp;
				}
			}
		return retval;
	}

	private boolean isDoubleValue(Comparable comparable) {
		try {
			Double.valueOf(comparable.toString());
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	public String[] getAllColumnNames() {
		return allColumnNames;
	}

	public boolean[] getColumntoShow() {
		return showColumns;
	}

	public String getColumnName(int index) {
		return columnNames[index];
	}

	public int getCriteriaCount() {
		if (columnNames != null) {
			return columnNames.length;
		}
		return 0;
	}

	public Format getFormat(String columnName) {
		if (model == null) {
			return null;
		}
		return model.getFormat(columnName);
	}

	public String getOperationString(int index) {
		return (String) constantToSymbolHash.get(Integer.valueOf(operations[index]));
	}

	public int getOperation(int index) {
		return operations[index];
	}

	public Comparable getValue(int index) {
		return values[index];
	}

	public boolean isCompareWithAnd() {
		return compareWithAnd;
	}

	public void setColumnsToShow(String[] columnNames, boolean[] show) {
		this.allColumnNames = columnNames;
		this.showColumns = show;
	}

	public void setRowCriteria(String[] newNames, int[] newOperations, Comparable[] newValues, /* Format[] newFormats, */
	boolean comparison) {
		columnNames = newNames;
		operations = newOperations;
		values = newValues;
		compareWithAnd = comparison;
	}

	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	public boolean isApplyFilters() {
		return applyFilters;
	}

	public void setApplyFilters(boolean applyFilters) {
		this.applyFilters = applyFilters;
	}

	public String[] getColumnNames() {
		return columnNames;
	}

	public String toString() {
		if (columnNames == null || columnNames.length == 0) {
			return "";// NO_FILTER;
		}
		StringBuffer sb = new StringBuffer();
		int length = columnNames.length;
		for (int i = 0; i < length - 1; i++) {
			sb.append(columnNames[i]).append(" ").append(getOperationString(i)).append(" ").append(values[i]).append(
					", ");
		}
		sb.append(columnNames[length - 1]).append(" ").append(getOperationString(length - 1)).append(" ").append(
				values[length - 1]);
		return sb.toString();
	}

	public Class[] getAllColumnClasses() {
		return allColumnClasses;
	}

	public void setAllColumnClasses(Class[] columnClasses) {
		if (columnClasses.length != allColumnNames.length) {
			throw new IllegalArgumentException("The column classes and "
					+ "allColumnNames arrays should be of same length");
		}
		this.allColumnClasses = columnClasses;
	}
}
