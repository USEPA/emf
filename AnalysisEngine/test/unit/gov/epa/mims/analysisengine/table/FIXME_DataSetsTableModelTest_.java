package gov.epa.mims.analysisengine.table;

import gov.epa.mims.analysisengine.gui.DoubleSeries;
import gov.epa.mims.analysisengine.tree.DataSets;
import junit.framework.TestCase;

/**
 * TestDataSetsTableModel.java
 * 
 * @author Parthee Partheepan
 * @version $Id: FIXME_DataSetsTableModelTest_.java,v 1.1 2006/01/10 23:29:38 parthee Exp $
 */
public class FIXME_DataSetsTableModelTest_ extends TestCase {
	DoubleSeries ds0;

	DoubleSeries ds1;

	DoubleSeries ds2;

	DoubleSeries ds3;

	DoubleSeries ds4;

	DoubleSeries[] ds = { ds0, ds1, ds2, ds3, ds4 };

	double[] d0;

	double[] d1;

	double[] d2;

	double[] d3;

	double[] d4;

	double[][] d = { d0, d1, d2, d3, d4 };

	String[] names = { "name0", "name1", "name2", "name3", "name4" };

	DataSetsTableModel model;

	/** Creates a new instance of TestDataSetsTableModel */
	public FIXME_DataSetsTableModelTest_() {
		super("");
		DataSets dataSets = new DataSets();
		for (int i = 0; i < d.length; i++) {
			d[i] = createDoubleArray();
			ds[i] = new DoubleSeries();
			for (int j = 0; j < d[i].length; j++) {
				ds[i].addData(d[i][j]);
			}
			ds[i].setName(names[i]);
			dataSets.add(ds[i], ds[i].getName());
		}
		model = new DataSetsTableModel(dataSets);
	}

	/**
	 * a helper method to generate random number length of arrays max length 100;
	 */
	public double[] createDoubleArray() {
		int count = (int) (Math.random() * 100);

		double[] array = new double[count];
		for (int i = 0; i < count; i++) {
			array[i] = count;
		}
		return array;
	}

	public void FIXME_testGetValueAt() {
		double verySmalleValue = 1e-23;
		for (int i = 0; i < 200; i++) {

			int columnIndex = (int) (Math.random() * model.getColumnCount());
			int rowIndex = (int) (Math.random() * model.getRowCount());
			if (d[columnIndex].length < rowIndex) {
				rowIndex = (int) (Math.random() * d[columnIndex].length);
				System.out.println("After columnIndex=" + columnIndex + " rowIndex=" + rowIndex);
			}

			double expectedValue = d[columnIndex][rowIndex];
			double value = ((Double) model.getValueAt(rowIndex, columnIndex)).doubleValue();
			assertEquals(value, expectedValue, verySmalleValue);
		}
	}

}
