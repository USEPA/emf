package gov.epa.mims.analysisengine.table.stats;

import gov.epa.mims.analysisengine.stats.SummaryStats;
import gov.epa.mims.analysisengine.table.OverallTableModel;
import gov.epa.mims.analysisengine.table.TableDataSeries;
import gov.epa.mims.analysisengine.tree.DataSets;
import gov.epa.mims.analysisengine.tree.DataSetsAdapter;

import java.util.ArrayList;
import java.util.HashMap;

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

/**
 * <p>
 * Title:StatisticsModel.java
 * </p>
 * <p>
 * Description: A data model for the statistics gui
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * <p>
 * Company: CEP, UNC-Chapel Hill
 * </p>
 * 
 * @author Parthee Partheepan
 * @version $Id: StatisticsModel.java,v 1.1 2006/11/01 15:33:38 parthee Exp $ Handles columns of types Double and
 *          Integer
 */

public class StatisticsModel {

	/** table model that contains the viewing data on the table */
	private OverallTableModel overallTableModel;

	/**
	 * An array of booleans to indicate which columns are selected for the statistical analysis.
	 */
	private boolean[] showColumns;

	/** indices for the data columns selected */
	private int[] dataColumns;

	/** a histogram model */
	private HistogramModel histogramModel;

	/** a percentile model */
	private PercentileModel percentileModel;

	/** regression model */
	private RegressionModel regressionModel;

	/**
	 * a counter to track the no of basic stats analysis created during a model run and used in creating the tab name
	 */
	public static int basicStats_counter = 1;

	/** a tab name for the basic stats analysis */
	private String basicStatsTabName = "Basic Statistics " + basicStats_counter;

	/** a boolean to indicate whether to do the basic statistics analysis */
	private boolean basicStatsAnalysis = true;

	/** add and remove the basis stats analysis selected from the gui */
	private HashMap selBasicStats;

	/** a boolean to indicate whether to do histogram Analysis */
	private boolean histogramAnalysis = false;

	/** a boolean to indicate whether to do histogram Analysis */
	private boolean percentileAnalysis = false;

	/** a boolean to indicate whether to do regression analysis */
	private boolean regressionAnalysis = false;

	/** a boolean to indicate whether to do correlation analysis */
	private boolean correlationAnalysis = false;

	/** a boolean to indicate whether to bring up Weka */
	private boolean wekaAnalysis = false;

	/** to save the selected column names */
	private String[] selectedColNames;

	/** Creates a new instance of StatisticsModel */
	public StatisticsModel(OverallTableModel overallTableModel) {
		this.overallTableModel = overallTableModel;
		selBasicStats = new HashMap();
		// assumed that initialy all analysis are selected
		for (int i = 0; i < SummaryStats.BASIC_STATISTICS.length; i++) {
			selBasicStats.put(SummaryStats.BASIC_STATISTICS[i], Boolean.TRUE);
		}// for(i)
	}// StatisticsModel

	/**
	 * Getter for property overallTableModel.
	 * 
	 * @return Value of property overallTableModel.
	 * 
	 */
	public OverallTableModel getOverallTableModel() {
		return overallTableModel;
	}

	/**
	 * getter for no of data colums
	 * 
	 * @return the number of data columns
	 */
	public int getNumOfDataColumns() {
		if (dataColumns != null) {
			return dataColumns.length;
		}
		return 0;
	}// getNumOfDataColumns()

	/**
	 * Combine the selected data columns to display in the textfield
	 * 
	 * @return a string that concatenates all the data column selections
	 */
	public String getDataColumnSelection() {
		String retn = "";
		if (dataColumns == null || dataColumns.length == 0) {
			return retn;
		}// if (dataColumns == null || dataColumns.length == 0)

		for (int i = 0; i < dataColumns.length; i++) {
			retn = retn.concat(overallTableModel.getColumnName(dataColumns[i] + 1) + ", ");
		}
		if (retn.length() > 2) {
			retn = retn.substring(0, retn.length() - 2);
		}// if (retn.length() > 1)
		return retn;
	}// getDataColumnSelection()

	/**
	 * Getter for property showColumns.
	 * 
	 * @return Value of property showColumns.
	 * 
	 */
	public boolean[] getShowColumns() {
		return this.showColumns;
	}

	/**
	 * Setter for property showColumns and then create dataColumns array and selectedColNames The size of the
	 * dataColumn& selectedColNames arrays is equal to number of true value in the showColumns columns.
	 * 
	 * @param showColumns
	 *            New value of property showColumns.
	 * 
	 */
	public void setShowColumns(boolean[] showColumns) {
		this.showColumns = showColumns;
		this.dataColumns = convertSelection(showColumns);
		int length = dataColumns.length;
		selectedColNames = new String[dataColumns.length];
		for (int i = 0; i < length; i++) {
			selectedColNames[i] = overallTableModel.getColumnName(dataColumns[i] + 1);
		}// for(i)
	}

	/**
	 * calculate the basic statistics based on the selected data columns (of type Double and Integer)
	 */
	public Object[][] calculateBasicStatistics() throws Exception {
		if (dataColumns != null) {
			int noOfColSelected = dataColumns.length;
			// Find out the statistics which are selected for the analysis
			String[] selBasicStatAnal = getSelBasisStatsAnal();
			int arrayLength = selBasicStatAnal.length;
			// create object arrays for each statisics equal to array length;
			Object[][] labelAndDataVal = new Object[arrayLength][noOfColSelected + 1];// add 1 for the labels
			for (int i = 0; i < noOfColSelected; i++) {
				int selColIndex = dataColumns[i] + 1;
				// Check whether columns are of type double
				Class type = overallTableModel.getColumnClass(selColIndex);
				/*
				 * if(!(type.equals(Double.class) || type.equals(Integer.class))) { throw new Exception("The selected
				 * column " + selColIndex + " type is not numeric.\nPlease only select the columns with " + "type
				 * numeric"); }//if()
				 */
				// store the col data
				int rowCount = overallTableModel.getRowCount();
				// Double [] colData = new Double[rowCount];
				ArrayList colData = new ArrayList();

				for (int r = 0; r < rowCount; r++) {
					Object obj = overallTableModel.getValueAt(r, selColIndex);
					if (type == Double.class) {
						if (!Double.isNaN(((Double) obj).doubleValue())) {
							colData.add(obj);
						}
						// don't add if it's NaN
					} else if (type == Integer.class) {
						colData.add(Double.valueOf(((Integer) obj).intValue()));
					} else {
						throw new Exception("The type '" + type + "' is not handled");
					}
					// System.out.println("colData ["+r+ "]="+colData[r]);
				}// for(r)
				HashMap sumStats = null;
				if (colData.size() != 0) {
					Double[] doubleColData = new Double[colData.size()];
					colData.toArray(doubleColData);
					SummaryStats basicStats = new SummaryStats(doubleColData);
					sumStats = basicStats.getSummaryStats();
				} else {
					sumStats = SummaryStats.getAllNaNSummary();
				}

				for (int j = 0; j < arrayLength; j++) {
					// add 1 so that data is not add in the first col
					labelAndDataVal[j][i + 1] = sumStats.get(selBasicStatAnal[j]);
				}// for(j)
			}// for(i)
			// first col labels
			for (int i = 0; i < arrayLength; i++) {
				labelAndDataVal[i][0] = selBasicStatAnal[i];
			}// for(j)
			return labelAndDataVal;
		}// if(dataColumns != null)
		return null;
	}

	// A helper method to get the names of the basic statistics analysis selected
	private String[] getSelBasisStatsAnal() {
		ArrayList list = new ArrayList();
		for (int i = 0; i < SummaryStats.BASIC_STATISTICS.length; i++) {
			String key = SummaryStats.BASIC_STATISTICS[i];
			boolean selected = ((Boolean) selBasicStats.get(key)).booleanValue();
			if (selected) {
				list.add(key);
			}// if(selected)
		}// for(i)
		String[] selAnal = new String[list.size()];
		selAnal = (String[]) list.toArray(selAnal);
		return selAnal;
	}// getBasisStatsAnal()

	/**
	 * Getter for property selBasicStats.
	 * 
	 * @return Value of property selBasicStats.
	 * 
	 */
	public HashMap getSelBasicStats() {
		return selBasicStats;
	}

	/**
	 * Setter for property selBasicStats.
	 * 
	 * @param selBasicStats
	 *            New value of property selBasicStats.
	 * 
	 */
	public void setSelBasicStats(HashMap selBasicStats) {
		this.selBasicStats = selBasicStats;
	}

	/**
	 * returns the selected data columns for the basis stats analysis
	 */
	public String[] getBasicStatsColNames() {
		String[] colNames = null;
		if (dataColumns != null) {
			colNames = new String[dataColumns.length + 1]; // add 1 for the label col
			colNames[0] = "Basic Statistics";
			for (int i = 1; i < colNames.length; i++) {
				colNames[i] = overallTableModel.getColumnName(dataColumns[i - 1] + 1);
			}// for(i)s
		}
		return colNames;
	}// getSelDataColumnNames()

	public Instances createWekaInstances() {
		String[] colNames = getBasicStatsColNames();
		FastVector atts = new FastVector();
		for (int i = 1; i < colNames.length; i++)
			atts.addElement(new Attribute(colNames[i]));
		Instances data = new Instances("Weka Data", atts, overallTableModel.getRowCount());
		for (int i = 0; i < overallTableModel.getRowCount(); i++) {
			double[] values = new double[atts.size()];
			for (int j = 0; j < atts.size(); j++) {
				Object obj = overallTableModel.getValueAt(i, dataColumns[j] + 1);
				if (obj instanceof Double)
					values[j] = ((Double) obj).doubleValue();
				else
					values[j] = ((Integer) obj).intValue();
			}
			data.add(new Instance(1.0, values));
		}
		return data;
	}

	/**
	 * create data sets if a data columns are selected
	 * 
	 * @return DataSetsAdapter
	 * @throws Exception
	 * @pre selected columns should be either integer or double
	 */
	public DataSetsAdapter createDataSets() throws Exception {
		DataSets tableDataSets = new DataSets();
		TableDataSeries dataSeries = null;
		// assumption is that first column
		int[] labelColumns = { 1 };
		char separator = ' ';
		for (int i = 0; i < dataColumns.length; i++) {
			dataSeries = new TableDataSeries(overallTableModel, dataColumns[i], labelColumns, separator);
			tableDataSets.add(dataSeries, dataSeries.getName());
		}// for
		return tableDataSets;
	}// createDataSets()

	/**
	 * 
	 * @param selected
	 *            boolean []
	 * @return int[] consisit of indices corresponding to the elements in the selected which have true value.
	 */
	public int[] convertSelection(boolean[] selected) {
		int[] selections = new int[selected.length];
		int count = 0;
		for (int i = 0; i < selected.length; i++) {
			if (selected[i]) {
				selections[count] = i;
				count++;
			}// if (selected[i])
		}// for int i
		int[] newArray = new int[count];
		for (int j = 0; j < count; j++) {
			// System.out.println("selections[j]="+selections[j]);
			newArray[j] = selections[j];
		}
		return newArray;
	}// convertSelection(boolean[])

	/**
	 * Getter for the data columns
	 * 
	 * @return int [] dataColumns
	 */
	public int[] getDataColumns() {
		return this.dataColumns;
	}

	/**
	 * Getter for property histogramModel.
	 * 
	 * @return Value of property histogramModel.
	 * 
	 */
	public HistogramModel getHistogramModel() {
		return histogramModel;
	}

	/**
	 * Getter for property regressionModel
	 * 
	 * @return Value of property regressionModel
	 * 
	 */
	public RegressionModel getRegressionModel() {
		return regressionModel;
	}

	/**
	 * Getter for property percentileModel. s *
	 * 
	 * @return Value of property percentileModel.
	 * 
	 */
	public PercentileModel getPercentileModel() {
		return percentileModel;
	}

	/**
	 * Getter for property selectedColNames.
	 * 
	 * @return Value of property selectedColNames.
	 */
	public String[] getSelectedColNames() {
		return this.selectedColNames;
	}

	/**
	 * Getter for property histogramAnalysis.
	 * 
	 * @return Value of property histogramAnalysis.
	 * 
	 */
	public boolean isHistogramAnalysis() {
		return histogramAnalysis;
	}

	/**
	 * Getter for property percentileAnalysis.
	 * 
	 * @return Value of property percentileAnalysis.
	 * 
	 */
	public boolean isPercentileAnalysis() {
		return percentileAnalysis;
	}

	/**
	 * Getter for property regressionAnalysis.
	 * 
	 * @return Value of property regressionAnalysis.
	 * 
	 */
	public boolean isRegressionAnalysis() {
		return regressionAnalysis;
	}

	/**
	 * Getter for property wekaAnalysis.
	 * 
	 * @return Value of property wekaAnalysis.
	 * 
	 */
	public boolean isWekaAnalysis() {
		return wekaAnalysis;
	}

	/**
	 * Getter for property correlationAnalysis.
	 * 
	 * @return Value of property correlationAnalysis.
	 * 
	 */
	public boolean isCorrelationAnalysis() {
		return correlationAnalysis;
	}

	/**
	 * Setter for property histogramModel.
	 * 
	 * @param histogramModel
	 *            New value of property histogramModel.
	 * 
	 */
	public void setHistogramModel(HistogramModel histogramModel) {
		this.histogramModel = histogramModel;
	}

	/**
	 * Setter for property overallTableModel.
	 * 
	 * @param model
	 *            overallTableModel.
	 * 
	 */
	public void setOverallTableModel(OverallTableModel model) {
		this.overallTableModel = model;
	}

	/**
	 * Setter for property percentileModel.
	 * 
	 * @param percentileModel
	 *            New value of property percentileModel.
	 * 
	 */
	public void setPercentileModel(PercentileModel percentileModel) {
		this.percentileModel = percentileModel;
	}

	/**
	 * Setter for property percentileModel.
	 * 
	 * @param regressionModel
	 *            New value of property regressionModel.
	 * 
	 */
	public void setRegressionModel(RegressionModel regressionModel) {
		this.regressionModel = regressionModel;
	}

	/**
	 * Setter for property histogramAnalysis.
	 * 
	 * @param histogramAnalysis
	 *            New value of property histogramAnalysis.
	 * 
	 */
	public void setHistogramAnalysis(boolean histogramAnalysis) {
		this.histogramAnalysis = histogramAnalysis;
	}

	/**
	 * Setter for property percentileAnalysis.
	 * 
	 * @param percentileAnalysis
	 *            New value of property percentileAnalysis.
	 * 
	 */
	public void setPercentileAnalysis(boolean percentileAnalysis) {
		this.percentileAnalysis = percentileAnalysis;
	}

	/**
	 * Setter for property regressionAnalysis.
	 * 
	 * @param regressionAnalysis
	 *            New value of property regressionAnalysis.
	 * 
	 */
	public void setRegressionAnalysis(boolean regressionAnalysis) {
		this.regressionAnalysis = regressionAnalysis;
	}

	/**
	 * Setter for property correlationAnalysis
	 * 
	 * @param correlationAnalysis
	 *            new value
	 * 
	 */
	public void setCorrelationAnalysis(boolean correlationAnalysis) {
		this.correlationAnalysis = correlationAnalysis;
	}

	/**
	 * setter for property wekaAnalysis
	 * 
	 * @param wekaAnalysis
	 *            new value
	 * 
	 */
	public void setWekaAnalysis(boolean wekaAnalysis) {
		this.wekaAnalysis = wekaAnalysis;
	}

	/**
	 * Setter for property selectedColNames.
	 * 
	 * @param selectedColNames
	 *            New value of property selectedColNames.
	 * 
	 */
	public void setSelectedColNames(String[] selectedColNames) {
		this.selectedColNames = selectedColNames;
	}

	/**
	 * Getter for property basicStatsAnalysis.
	 * 
	 * @return Value of property basicStatsAnalysis.
	 * 
	 */
	public boolean isBasicStatsAnalysis() {
		return basicStatsAnalysis;
	}

	/**
	 * Setter for property basicStatsAnalysis.
	 * 
	 * @param basicStatsAnalysis
	 *            New value of property basicStatsAnalysis.
	 * 
	 */
	public void setBasicStatsAnalysis(boolean basicStatsAnalysis) {
		this.basicStatsAnalysis = basicStatsAnalysis;
	}

	/**
	 * Getter for property basicStatsTabName.
	 * 
	 * @return Value of property basicStatsTabName.
	 * 
	 */
	public String getBasicStatsTabName() {
		return basicStatsTabName;
	}

	/**
	 * Setter for property basicStatsTabName.
	 * 
	 * @param basicStatsTabName
	 *            New value of property basicStatsTabName.
	 * 
	 */
	public void setBasicStatsTabName(String basicStatsTabName) {
		this.basicStatsTabName = basicStatsTabName;
	}

	// /** get the columns of type Double
	// * @return int [] The array will contain the col no which are of type
	// * Double
	// */
	// public int [] getDoubleColumnTypes()
	// {
	// int colCount = overallTableModel.getColumnCount();
	// boolean [] colClass = new boolean[colCount];
	// int count =0;
	// for(int i=1; i<colCount; i++)
	// {
	// if(overallTableModel.getColumnClass(i).equals(Double.class))
	// {
	// colClass[i] = true;
	// count ++;
	// }
	// }//for(i)
	// int [] doubleType = new int[count];
	// count=0;
	// for(int i=1; i < colCount; i++)
	// {
	// if(colClass[i] == true)
	// {
	// doubleType[count++] = i;
	// }
	// }//for(i)
	// return doubleType;
	// }//getDoubleColumnTypes()

	/**
	 * get the columns of type Double
	 * 
	 * @return int [] The array will contain the col no which are of type Double
	 */
	public boolean[] getDoubleColumnTypes() {
		int colCount = overallTableModel.getColumnCount();
		boolean[] colClass = new boolean[colCount - 1];// deduting one for the first column
		// System.out.println("colCount="+colCount);
		for (int i = 0; i < colClass.length; i++) {
			// System.out.println("column name = " + overallTableModel.getColumnName(i));
			if (overallTableModel.getColumnClass(i + 1).equals(Double.class)) {
				// System.out.println("Double column no="+i);
				colClass[i] = true;
			}
		}// for(i)

		return colClass;
	}// getDoubleColumnTypes()

}
