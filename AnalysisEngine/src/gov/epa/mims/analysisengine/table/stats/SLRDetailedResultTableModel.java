/*
 * SLRDetailedResultTableModel.java
 *
 * Created on September 30, 2004, 5:28 PM
 */

package gov.epa.mims.analysisengine.table.stats;

import gov.epa.mims.analysisengine.table.SpecialTableModel;

import java.util.ArrayList;
import java.util.Vector;

/**
 * 
 * @author kthanga
 */
public class SLRDetailedResultTableModel extends SpecialTableModel {

	/** Creates a new instance of SLRDetailedResultTableModel */
	public SLRDetailedResultTableModel(Vector depVars, Vector indepVars, int numInstances, String fileName) {
		super();
		StringBuffer dataHdr = new StringBuffer();
		dataHdr.append("1-to-1 Detailed Correlation Analysis using Simple Linear Regression\n");
		dataHdr.append("Data from file " + fileName);
		dataHdr.append("\nNumber of Instances: " + numInstances);
		dataHdr.append("\nDependent Variables: " + RegressionGUI.getStringFromVector(depVars));
		dataHdr.append("\n Independent Variables: " + RegressionGUI.getStringFromVector(indepVars));
		tableDataHeader = dataHdr.toString();

		columnHeaders = new String[12][2];
		int k = 0;
		columnHeaders[k][0] = new String("Dependent");
		columnHeaders[k++][1] = new String("Variable");
		columnHeaders[k][0] = new String("Independent");
		columnHeaders[k++][1] = new String("Variable");
		columnHeaders[k][0] = new String("Correlation");
		columnHeaders[k++][1] = new String("Coefficient");
		columnHeaders[k][0] = new String("Slope");
		columnHeaders[k++][1] = new String("");
		columnHeaders[k][0] = new String("Intercept");
		columnHeaders[k++][1] = new String("");
		columnHeaders[k][0] = new String("Equation");
		columnHeaders[k++][1] = new String("");
		columnHeaders[k][0] = "Mean absolute";
		columnHeaders[k++][1] = "Error";
		columnHeaders[k][0] = "Mean Prior Absolute";
		columnHeaders[k++][1] = "Error";
		columnHeaders[k][0] = "Relative Absolute";
		columnHeaders[k++][1] = "Error";
		columnHeaders[k][0] = "Root Mean Square";
		columnHeaders[k++][1] = "Error";
		columnHeaders[k][0] = "Root Mean Prior Squared";
		columnHeaders[k++][1] = "Error";
		columnHeaders[k][0] = "Root Relative Squared";
		columnHeaders[k++][1] = "Error";

		tableData = new ArrayList();

	}

	public void addRowData(Vector rowData) {
		ArrayList rowValues = new ArrayList();
		for (int i = 0; i < rowData.size(); i++)
			rowValues.add(rowData.get(i));
		tableData.add(rowValues);
	}

	public int getColumnCount() {
		return columnHeaders.length;
	}

	public Class getColumnClass(int col) {
		if (col == 0 || col == 1 || col == 5)
			return String.class;
		return Double.class;
	}

}
