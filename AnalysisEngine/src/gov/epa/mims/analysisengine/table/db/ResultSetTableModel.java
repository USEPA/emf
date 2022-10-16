package gov.epa.mims.analysisengine.table.db;

import gov.epa.mims.analysisengine.gui.DefaultUserInteractor;
import gov.epa.mims.analysisengine.gui.UserInteractor;
import gov.epa.mims.analysisengine.table.SpecialTableModel;
import gov.epa.mims.analysisengine.table.TableApp;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.HashMap;

/**
 * <p>
 * Title:ResultSetTableModel
 * </p>
 * <p>
 * Description: A table model with a java.sql.ResultSet as a data model
 * </p>
 * <p>
 * Copyright: Copyright (c) 2003
 * </p>
 * <p>
 * Company: CEP, UNC-Chapel Hill
 * </p>
 * 
 * @author Prashant Pai
 * @version $Id: ResultSetTableModel.java,v 1.1 2006/11/01 15:33:40 parthee Exp $
 */
public class ResultSetTableModel extends SpecialTableModel {
	private static HashMap defaultValue;

	static {
		defaultValue = new HashMap();
		defaultValue.put(String.class, "");
		defaultValue.put(Double.class, Double.valueOf(Double.NaN));
		defaultValue.put(Float.class, Float.valueOf(Float.NaN));
		defaultValue.put(Date.class, new Date(0L));
		defaultValue.put(Integer.class, Integer.valueOf(0));
	}

	/** a reference to the resultset */
	private ResultSet resultSet;

	/** a reference to the result set meta data * */
	private ResultSetMetaData rsMetaData = null;

	/** to denote the no of rows (= to no of elements in the largest dataSetIfc) */
	private int rowCount = -1;

	/**
	 * Store a reference to the DataSetIfc's passed in. I think that you'll have to store a pointer to each DataSetIfc
	 * in a DataSetIfc[] array. Set up column names based on the names of the data sets. Get and store the number or
	 * rows in the longest DataSetIfc.
	 * 
	 * @param dataSets
	 */
	public ResultSetTableModel(ResultSet resultSet) {
		super();
		this.resultSet = resultSet;
		try {
			this.rsMetaData = resultSet.getMetaData();
		} catch (java.sql.SQLException sqle) {
			sqle.printStackTrace();
			// what to do here
		}
		int colCount = getColumnCount();
		this.columnHeaders = new String[colCount][1];
		for (int i = 0; i < colCount; i++) {
			columnHeaders[i][0] = getColumnName(i);
		}
	}

	/**
	 * Get the item in the first row and return it's class.
	 * 
	 * @return get the type of each column clas
	 */
	public Class getColumnClass(int col) {
		try {
			Class forName = Class.forName(rsMetaData.getColumnClassName(col + 1));
			// System.out.println("Column name-"+ getColumnName(col)+ " class
			// name="+rsMetaData.getColumnClassName(col+1));
			return forName;
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Return the number of data sets since each dataset is one column.
	 */
	public int getColumnCount() {
		try {
			return rsMetaData.getColumnCount();
		} catch (Exception e) {
			return 0;
		}
	}

	/**
	 * Return the name of the table column in the db
	 * 
	 * @param col
	 *            column number
	 */
	public String getColumnName(int col) {
		try {
			int length = rsMetaData.getColumnCount();
			if (col >= length) {
				DefaultUserInteractor.get().notify(null, "Error",
						col + " > maximum column in the table model:" + length, UserInteractor.ERROR);

				return "";
			}
			String name = rsMetaData.getColumnName(col + 1);
			if (name != null) {
				return name;
			}
			return "";
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Return the name and units of the requested column.
	 * 
	 */
	public String[] getColumnHeaders(int col) {
		return new String[] { getColumnName(col) };
	}

	/**
	 * Go to the DataSetIfc at the columnIndex and get the value at rowIndex from it. If the given row is greater than
	 * the number of items in the DataSetIc, then return "n/a".
	 */
	public Object getValueAt(int rowIndex, int columnIndex) {
		// System.out.println("ResultSetTableModel.getValueAt: row="+rowIndex+", column="+columnIndex);
		Object aElement = null;
		try {
			int length = getColumnCount();
			if (columnIndex >= length) {
				throw new IllegalArgumentException(columnIndex + " > maximum column in the table model: " + length);
			}

			int rowCount = getRowCount();
			if (rowIndex >= rowCount) {
				throw new IllegalArgumentException(rowIndex + " > maximum rows in the table model: " + rowCount);
			}
			if (rowIndex < rowCount) {
				aElement = null;
				resultSet.absolute(rowIndex + 1);
				aElement = resultSet.getObject(columnIndex + 1);
			}// if(rowIndex <= numOfElements)
		} catch (Exception e) {
			DefaultUserInteractor.get().notify(null, "Error",
					"A error in DataSetsTableModel.getValueAt()" + e.getMessage(), UserInteractor.ERROR);
			e.printStackTrace();
		}

		if (aElement != null) {
			return aElement;
		}
		return getDefaultNullValue(columnIndex);
	}

	private Object getDefaultNullValue(int colIndex) {
		Class classType = getColumnClass(colIndex);
		return defaultValue.get(classType);
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
		try {
			int currentRow = resultSet.getRow();
			resultSet.last();
			int maxElements = resultSet.getRow();
			if (currentRow != 0) {
				resultSet.absolute(currentRow);
			} else {
				resultSet.beforeFirst();
			}
			this.rowCount = maxElements;
		} catch (Exception e) {
			this.rowCount = 0;
		}

	}// calculateRowCount()

	public static void main(String arg[]) {
		// create a db connection
		// create a query
		// get the result set
		// Load the DB driver.
		java.sql.Connection connection = null;
		java.sql.ResultSet rs = null;
		long t1 = 0;
		long t2 = 0;
		t1 = System.currentTimeMillis();
		System.out.println("Started t1=" + t1);
		try {
			Class.forName("com.mysql.jdbc.Driver");
			String dbName = "test";
			// /String dbName = "hapem5RiskMetrics";
			// String dbName = "ecorisk_outdb";
			// String tableName = "acr_risk";
			String tableName = "states";
			connection = java.sql.DriverManager.getConnection("jdbc:mysql://localhost/" + dbName, "root", "");
			rs = connection.createStatement().executeQuery("SELECT * FROM `" + tableName + "`");
			rs.setFetchSize(1000);
			t2 = System.currentTimeMillis();
			System.out.println("Query Executed at t2=" + t2);
			System.out.println("Query Executed in (sec)=" + ((t2 - t1) / 1000));

			t1 = System.currentTimeMillis();
			System.out.println("Create RS table model t1=" + t1);
			t2 = System.currentTimeMillis();
			System.out.println("Finished RS table model t2=" + t2);
			System.out.println("RS table Model Created in (sec)=" + ((t2 - t1) / 1000));
			System.out.println("Fecth size = " + rs.getFetchSize());
		} catch (Exception e) {
			e.printStackTrace();
		}

		// System.out.println("rowCount=" +tableModel.getRowCount());
		// javax.swing.JFrame f = new javax.swing.JFrame("SortFilterTablePanel");
		// f.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
		// java.awt.Container contentPane = f.getContentPane();
		// contentPane.setLayout(new javax.swing.BoxLayout(contentPane, javax.swing.BoxLayout.X_AXIS));
		t1 = System.currentTimeMillis();
		System.out.println("Create SortFilterTablePanel model t1=" + t1);
		new TableApp(rs, "Query1");

		// SortFilterTablePanel sftp = new SortFilterTablePanel(f, tableModel);
		// contentPane.add(sftp);
		// f.pack();
		// f.setVisible(true);
		t2 = System.currentTimeMillis();
		System.out.println("Created SortFilterTablePanel model at t2=" + t2);
		System.out.println("Crated SortFilterTablePanel in (sec)  =" + ((t2 - t1) / 1000));

	}// main

}// class ResultSetTableModel
