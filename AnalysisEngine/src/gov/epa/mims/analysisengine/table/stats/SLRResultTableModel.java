/*
 * SLRResultTableModel.java
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
public class SLRResultTableModel extends SpecialTableModel {

	/** Creates a new instance of SLRResultTableModel */
	public SLRResultTableModel(Vector depVars, Vector indepVars, int numInstances, String fileName) {
		super();
		StringBuffer dataHdr = new StringBuffer();
		dataHdr.append("1-to-1 Correlation Analysis\n");
		dataHdr.append("Data from file " + fileName);
		dataHdr.append("\nNumber of Instances: " + numInstances);
		dataHdr.append("\nDependent Variables: " + RegressionGUI.getStringFromVector(depVars));
		dataHdr.append("\n Independent Variables: " + RegressionGUI.getStringFromVector(indepVars));
		dataHdr.append("\n The following data shows only "
				+ "the 1-to-1 correlation coefficient between each dependent" + " and independent variable\n");
		tableDataHeader = dataHdr.toString();

		columnHeaders = new String[indepVars.size() + 1][2];
		columnHeaders[0][0] = "Dependent";
		columnHeaders[0][1] = "Variables";
		for (int i = 1; i < indepVars.size() + 1; i++) {
			columnHeaders[i][1] = "Coefficient";
			columnHeaders[i][0] = (String) indepVars.get(i - 1);
		}
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
		if (col == 0)
			return String.class;
		return Double.class;
	}

}
