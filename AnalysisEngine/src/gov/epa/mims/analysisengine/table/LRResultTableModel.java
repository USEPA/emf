/*
 * SLRResultTableModel.java
 *
 * Created on September 30, 2004, 5:28 PM
 */

package gov.epa.mims.analysisengine.table;

import gov.epa.mims.analysisengine.table.stats.RegressionGUI;

import java.util.ArrayList;
import java.util.Vector;

/**
 * 
 * @author kthanga
 */
public class LRResultTableModel extends SpecialTableModel {

	/** Creates a new instance of LRResultTableModel */
	public LRResultTableModel(Vector depVars, Vector indepVars, int numInstances, String fileName) {
		super();
		StringBuffer dataHdr = new StringBuffer();
		dataHdr.append("Linear Regression\n");
		dataHdr.append("Data from file " + fileName);
		dataHdr.append("\nNumber of Instances: " + numInstances);
		dataHdr.append("\nDependent Variables: " + RegressionGUI.getStringFromVector(depVars));
		dataHdr.append("\n Independent Variables: " + RegressionGUI.getStringFromVector(indepVars));
		tableDataHeader = dataHdr.toString();

		columnHeaders = new String[indepVars.size() + 10][2];
		columnHeaders[0][0] = new String("Dependent");
		columnHeaders[0][1] = new String("Variables");
		columnHeaders[1][1] = new String(" ");
		columnHeaders[1][0] = new String("Equation");
		for (int i = 0; i < indepVars.size(); i++) {
			columnHeaders[i + 2][1] = new String("Coefficient");
			columnHeaders[i + 2][0] = new String((String) indepVars.get(i));
		}
		int k = 2 + indepVars.size();
		columnHeaders[k][0] = new String("Constant");
		columnHeaders[k++][1] = new String("Coefficient");
		columnHeaders[k][0] = "Correlation";
		columnHeaders[k++][1] = "Coefficient";
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
		if (col == 0 || col == 1)
			return String.class;
		
		return Double.class;
	}

}
