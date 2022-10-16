package gov.epa.mims.analysisengine.table;

import gov.epa.mims.analysisengine.gui.DefaultUserInteractor;
import gov.epa.mims.analysisengine.gui.UserInteractor;
import gov.epa.mims.analysisengine.tree.DataSetsAdapter;
import gov.epa.mims.analysisengine.tree.LabeledDataSetIfc;

import java.util.Vector;

/**
 * <p>
 * Title:LabeledDataSetsTableModel
 * </p>
 * <p>
 * Description: A table model with DataSestAdapter which contain labeled data sets as a data model
 * </p>
 * WARNING: Labels for all the data sets should be same
 * <p>
 * Copyright: Copyright (c) 2003
 * </p>
 * <p>
 * Company: CEP, UNC-Chapel Hill
 * </p>
 * 
 * @author Parthee Partheepan
 * @version $Id: LabeledDataSetsTableModel.java,v 1.4 2006/10/30 21:43:50 parthee Exp $
 */
public class LabeledDataSetsTableModel extends DataSetsTableModel {
	/** a refernce to the DataSetIfc contained in the dataSets */
	private LabeledDataSetIfc[] dataSetIfcs;

	/** a name for the first column header */
	private String firstColumnName = "";

	/** Creates a new instance of LabeledDataSetTableModel */
	public LabeledDataSetsTableModel(DataSetsAdapter dataSets) {
		super(dataSets);
		Vector data = dataSets.getDataSets(null, null);
		dataSetIfcs = new LabeledDataSetIfc[data.size()];
		this.columnHeaders = new String[data.size() + 1][1];
		columnHeaders[0][0] = firstColumnName;
		for (int i = 0; i < data.size(); i++) {
			dataSetIfcs[i] = (LabeledDataSetIfc) data.get(i);
			columnHeaders[i + 1][0] = dataSetIfcs[i].getName();
		}
		columnRowHeaders = null;
	}

	/**
	 * Return the number of data sets since each dataset is one column.
	 */
	public int getColumnCount() {
		// add 1 for the label column
		return dataSetIfcs.length + 1;
	}

	/**
	 * Return the name of the DataSetIfc at column col.
	 * 
	 * @param col
	 *            column number
	 */
	public String getColumnName(int col) {
		if (col == 0) {
			return firstColumnName;
		}
		String name = dataSetIfcs[col - 1].getName();
		if (name != null) {
			return name;
		}
		return "";
	}

	/**
	 * Return the name and units of the requested column.
	 */
	public String[] getColumnHeaders(int col) {
		String units = dataSetIfcs[col].getUnits();
		if (units != null) {
			return new String[] { getColumnName(col), units };
		}
		System.out.println("col no = " + col);
		System.out.println("column name = " + getColumnName(col));
		return new String[] { getColumnName(col) };
	}

	/**
	 * Go to the DataSetIfc at the columnIndex and get the value at rowIndex from it. If the given row is greater than
	 * the number of items in the DataSetIc, then return "n/a".
	 */
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (columnIndex == 0) {

			LabeledDataSetIfc aDataSet = dataSetIfcs[0];
			try {
				aDataSet.open();
				// return aDataSet.getLabel(rowIndex);
				// RP: This is a temporary fix, we want to plot CDF plot using
				// scatter plot so we won't to create data sets from first column
				// so converting it to doubles
				String label = aDataSet.getLabel(rowIndex);
				try {
					return Double.valueOf(label);
				} catch (Exception e) {
					return label;
				}

			} catch (Exception e) {
				DefaultUserInteractor.get().notify(null, "Error",
						"A error in DataSetsTableModel.getValueAt()" + e.getMessage(), UserInteractor.ERROR);
			}// catch()

		} else {

			LabeledDataSetIfc aDataSet = dataSetIfcs[columnIndex - 1];
			int numOfElements = 0;

			try {
				aDataSet.open();

				numOfElements = aDataSet.getNumElements();
			} catch (Exception e) {
				DefaultUserInteractor.get().notify(null, "Error",
						"A error in DataSetsTableModel.getValueAt()" + e.getMessage(), UserInteractor.ERROR);
			}// catch()

			if (rowIndex < numOfElements) {
				double aElement = 0.0;
				try {
					aElement = aDataSet.getElement(rowIndex);
					aDataSet.close();
				} catch (Exception e) {
					DefaultUserInteractor.get().notify(null, "Error",
							"A error in DataSetsTableModel.getValueAt()" + e.getMessage(), UserInteractor.ERROR);
				}
				return Double.valueOf(aElement);
			}// if(rowIndex <= numOfElements)
			return Double.valueOf(Double.NaN);
		}// else
		return null;
	}

	/**
	 * setter for the first column name
	 * 
	 * @param colName
	 *            String for the first columnName
	 */
	public void setFirstColumnName(String colName) {
		firstColumnName = colName;
		columnHeaders[0][0] = colName;

	}

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) {
		String[] labels = { "North Carolina", "South Carolina", "Virginia", "Georgia" };

		gov.epa.mims.analysisengine.gui.LabeledDoubleSeries ds1 = new gov.epa.mims.analysisengine.gui.LabeledDoubleSeries();
		gov.epa.mims.analysisengine.gui.LabeledDoubleSeries ds2 = new gov.epa.mims.analysisengine.gui.LabeledDoubleSeries();
		gov.epa.mims.analysisengine.gui.LabeledDoubleSeries ds3 = new gov.epa.mims.analysisengine.gui.LabeledDoubleSeries();
		gov.epa.mims.analysisengine.gui.LabeledDoubleSeries ds4 = new gov.epa.mims.analysisengine.gui.LabeledDoubleSeries();
		gov.epa.mims.analysisengine.gui.LabeledDoubleSeries[] ds = { ds1, ds2, ds3, ds4 };

		int count = ds.length;
		String[] rowLabels = { "label1", "label2", "label3", "label4", "label5", "label6", "label7", "label8",
				"label9", "label10" };

		gov.epa.mims.analysisengine.tree.DataSets dataSets = new gov.epa.mims.analysisengine.tree.DataSets();

		for (int i = 0; i < count; i++) {
			int length = 10;// (int)(Math.random()*100);
			// for (int j = 0; j < length; j++)
			// {
			// ds[i].addData(Math.random()*200);
			// }//for j
			for (int j = 0; j < length; j++) {
				ds[i].addData((j + 1) * (i + 1), rowLabels[j]);

			}// for j

			ds[i].setName(labels[i]);
			dataSets.add(ds[i], ds[i].getName());
		}// for i
		final LabeledDataSetsTableModel tableModel = new LabeledDataSetsTableModel(dataSets);
		tableModel.setFirstColumnName("Labels");
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				javax.swing.JFrame f = new javax.swing.JFrame("SortFilterTablePanel");
				f.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
				java.awt.Container contentPane = f.getContentPane();
				contentPane.setLayout(new javax.swing.BoxLayout(contentPane, javax.swing.BoxLayout.X_AXIS));
				SUSortFilterTablePanel sftp = new SUSortFilterTablePanel(null, "Labeled Dataset Table Model", null,
						null, tableModel);
				contentPane.add(sftp);

				f.pack();
				f.setVisible(true);
			}
		});

	}// main()

}
