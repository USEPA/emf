package gov.epa.mims.analysisengine.table;

import gov.epa.mims.analysisengine.gui.DefaultUserInteractor;
import gov.epa.mims.analysisengine.gui.UserInteractor;
import gov.epa.mims.analysisengine.tree.DataSetIfc;
import gov.epa.mims.analysisengine.tree.DataSetsAdapter;

import java.util.Vector;

/**
 * <p>
 * Title:DataSetsTableModel
 * </p>
 * <p>
 * Description: A table model with DataSestAdapter as a data model
 * </p>
 * <p>
 * Copyright: Copyright (c) 2003
 * </p>
 * <p>
 * Company: CEP, UNC-Chapel Hill
 * </p>
 * 
 * @author Daniel Gatti, Parthee Partheepan
 * @version $Id: DataSetsTableModel.java,v 1.4 2006/10/30 21:43:50 parthee Exp $
 */
public class DataSetsTableModel extends SpecialTableModel {

	/** a refernce to the DataSetIfc contained in the dataSets */
	private DataSetIfc[] dataSetIfcs;

	/** to denote the no of rows (= to no of elements in the largest dataSetIfc) */
	private int rowCount = -1;

	/**
	 * Store a reference to the DataSetIfc's passed in. I think that you'll have to store a pointer to each DataSetIfc
	 * in a DataSetIfc[] array. Set up column names based on the names of the data sets. Get and store the number or
	 * rows in the longest DataSetIfc.
	 * 
	 * @param dataSets
	 */
	public DataSetsTableModel(DataSetsAdapter dataSets) {
		super();
		Vector data = dataSets.getDataSets(null, null);
		dataSetIfcs = new DataSetIfc[data.size()];
		this.columnHeaders = new String[data.size()][1];
		for (int i = 0; i < data.size(); i++) {
			dataSetIfcs[i] = (DataSetIfc) data.get(i);
			columnHeaders[i][0] = dataSetIfcs[i].getName();
		}
	}

	/**
	 * Get the item in the first row and return it's class.
	 * 
	 * @return get the type of each column clas
	 */
	public Class getColumnClass(int col) {
		return getValueAt(0, col).getClass();
	}

	/**
	 * Return the number of data sets since each dataset is one column.
	 */
	public int getColumnCount() {
		return dataSetIfcs.length;
	}

	/**
	 * Return the name of the DataSetIfc at column col.
	 * 
	 * @param col
	 *            column number
	 */
	public String getColumnName(int col) {
		int length = dataSetIfcs.length;
		if (col >= length) {
			DefaultUserInteractor.get().notify(null, "Error", col + " > maximum column in the table model:" + length,
					UserInteractor.ERROR);
			return "";
		}
		String name = dataSetIfcs[col].getName();
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
		return new String[] { getColumnName(col) };
	}

	/**
	 * Go to the DataSetIfc at the columnIndex and get the value at rowIndex from it. If the given row is greater than
	 * the number of items in the DataSetIc, then return "n/a".
	 */
	public Object getValueAt(int rowIndex, int columnIndex) {
		int length = dataSetIfcs.length;
		if (columnIndex >= length) {
			DefaultUserInteractor.get().notify(null, "Error",
					columnIndex + " > maximum column in the table model:" + length, UserInteractor.ERROR);
			return "";
		}

		DataSetIfc aDataSet = dataSetIfcs[columnIndex];
		int numOfElements = 0;

		try {
			aDataSet.open();
			numOfElements = aDataSet.getNumElements();

		} catch (Exception e) {
			DefaultUserInteractor.get().notify(null, "Error",
					"A error in DataSetsTableModel.getValueAt()" + e.getMessage(), UserInteractor.ERROR);
		}

		if (rowIndex < numOfElements) {
			double aElement = 0.0;
			try {
				aElement = aDataSet.getElement(rowIndex);
				aDataSet.close();
			} catch (Exception e) {
				DefaultUserInteractor.get().notify(null, "Error",
						"A error in DataSetsTableModel.getValueAt()" + e.getMessage(), UserInteractor.ERROR);
			}
			return new Double(aElement);
		}// if(rowIndex <= numOfElements)
		return new Double(Double.NaN);
	}

	/**
	 * The DataSetIfc's might have differing numbers of rows, so return the length of the longest DataSetIfc.
	 */
	public int getRowCount() {
		if (rowCount != -1) {
			return rowCount;
		}
		calculateRowCount();
		return rowCount;
	}

	/** a helper method to calcualte the row count */
	private void calculateRowCount() {
		int maxElements = 0;
		// initializing maxElements
		try {
			dataSetIfcs[0].open();
			maxElements = dataSetIfcs[0].getNumElements();
			dataSetIfcs[0].close();
		} catch (Exception e) {
			DefaultUserInteractor.get().notify(null, "Error",
					"A error in DataSetsTableModel.calculateRowCount()" + e.getMessage(), UserInteractor.ERROR);
		}

		for (int i = 1; i < dataSetIfcs.length; i++) {
			int noOfElements = 0;
			try {
				dataSetIfcs[i].open();
				noOfElements = dataSetIfcs[i].getNumElements();
				if (noOfElements > maxElements) {
					maxElements = noOfElements;
				}// if
				dataSetIfcs[i].close();
			}// try
			catch (Exception e) {
				DefaultUserInteractor.get().notify(null, "Error",
						"A error in DataSetsTableModel.calculateRowCount()" + e.getMessage(), UserInteractor.ERROR);
			}
		}// for i
		this.rowCount = maxElements;

	}// calculateRowCount()

	public static void main(String arg[]) {
		String[] labels = { "North Carolina", "South Carolina", "Virginia", "Georgia" };

		gov.epa.mims.analysisengine.gui.DoubleSeries ds1 = new gov.epa.mims.analysisengine.gui.DoubleSeries();
		gov.epa.mims.analysisengine.gui.DoubleSeries ds2 = new gov.epa.mims.analysisengine.gui.DoubleSeries();
		gov.epa.mims.analysisengine.gui.DoubleSeries ds3 = new gov.epa.mims.analysisengine.gui.DoubleSeries();
		gov.epa.mims.analysisengine.gui.DoubleSeries ds4 = new gov.epa.mims.analysisengine.gui.DoubleSeries();
		gov.epa.mims.analysisengine.gui.DoubleSeries[] ds = { ds1, ds2, ds3, ds4 };

		int count = ds.length;

		gov.epa.mims.analysisengine.tree.DataSets dataSets = new gov.epa.mims.analysisengine.tree.DataSets();

		for (int i = 0; i < count; i++) {
			int length = (int) (Math.random() * 100);
			for (int j = 0; j < length; j++) {
				ds[i].addData(Math.random() * 200);
			}// for j
			ds[i].setName(labels[i]);
			dataSets.add(ds[i], ds[i].getName());
		}// for i
		final DataSetsTableModel tableModel = new DataSetsTableModel(dataSets);
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				javax.swing.JFrame f = new javax.swing.JFrame("SortFilterTablePanel");
				f.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
				java.awt.Container contentPane = f.getContentPane();
				contentPane.setLayout(new javax.swing.BoxLayout(contentPane, javax.swing.BoxLayout.X_AXIS));
				SUSortFilterTablePanel sftp = new SUSortFilterTablePanel(f, "Datasets Table Model", null, null,
						tableModel);
				contentPane.add(sftp);

				f.pack();
				f.setVisible(true);
			}
		});

	}// main

} // class DataSetsTableModel

