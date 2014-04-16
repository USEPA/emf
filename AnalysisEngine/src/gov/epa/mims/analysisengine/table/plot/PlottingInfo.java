package gov.epa.mims.analysisengine.table.plot;

import gov.epa.mims.analysisengine.AnalysisEngineConstants;
import gov.epa.mims.analysisengine.gui.DefaultUserInteractor;
import gov.epa.mims.analysisengine.gui.TreeDialog;
import gov.epa.mims.analysisengine.gui.UserInteractor;
import gov.epa.mims.analysisengine.table.OverallTableModel;
import gov.epa.mims.analysisengine.table.TableDataSeries;
import gov.epa.mims.analysisengine.table.TableDateTimeSeries;
import gov.epa.mims.analysisengine.tree.DataSets;
import gov.epa.mims.analysisengine.tree.Plot;

import java.awt.Component;
import java.text.SimpleDateFormat;
import java.util.Vector;

/**
 * This class will serve as a model for the PlottingInfoGUI and contain all the information other than the data to pass
 * on to the Analysis Engine plotting package. A reference to an object of this class will be passed on to each of the
 * dataseries so they can correctly report back the data elements and the labels without any duplication of data.
 * 
 * @author Prashant Pai, CEP UNC
 * @version $Id: PlottingInfo.java,v 1.5 2007/06/11 03:34:34 eyth Exp $
 */
public class PlottingInfo implements java.io.Serializable {
	/** serial version UID */
	static final long serialVersionUID = 1;

	// /** the columns from which to take the data values **/
	// protected boolean[] dataColumnSelections = null;

	/** the separator to use in creating the name * */
	protected char separator = ' ';

	/** the plot type that needs to be used * */
	protected String plotType = PlotTypeConverter.BAR_PLOT;

	/** the units for the dataset * */
	protected String units = " ";

	/** if unit specified using col row header then this will be >= 0 */
	protected int colRowHeaderIndex = -1;

	/** if user selects the units from header option, value will be false, */
	protected boolean userSpecifiedUnits = false;

	/** the top most level table model * */
	transient protected OverallTableModel tableModel = null;

	/** store the selected Data column Names */
	protected String[] selDataColNames;

	public static final String NOT_SELECTED = "Not Selected";
	
	private static long lastMessageTime = 0;

	/** store the selected label column Names */
	protected String[] selLabelColNames = { NOT_SELECTED, NOT_SELECTED, NOT_SELECTED };

	protected String[] selLabelDateColNames = { NOT_SELECTED, NOT_SELECTED };

	protected String timeFormat = "HH:mm:ss";

	protected String plotName = " ";
	
	public PlottingInfo(OverallTableModel model) {
		this.tableModel = model;
	}

	public void setPlotType(String plotType) {
		this.plotType = plotType;
	}

	public String getPlotType() {
		return this.plotType;
	}
	
	public void setUnits(String units) {
		this.units = units;
	}

	public void setPlotName(String name) {
		this.plotName = name;
	}

	public String getPlotName() {
		return this.plotName;
	}

	public String getUnits() {
		return this.units;
	}

	/**
	 * gets set of units from table model if available
	 */
	public String[] getUnitsChoices() {
		String[] rowHeaders = tableModel.getColumnRowHeaders();
		String[] colHeaders1 = tableModel.getColumnHeaders(0);

		String row = "Row";

		if (rowHeaders != null && rowHeaders.length == colHeaders1.length) {
			for (int i = 0; i < rowHeaders.length; i++) {
				if (rowHeaders[i].trim().equals("") || rowHeaders[i].trim().equalsIgnoreCase("row")) {
					rowHeaders[i] = row + (i + 1);
				}
			}// for
			return rowHeaders;
		}
		// if(rowHeaders == null || rowHeaders[0].equalsIgnorecase("row"))
		String[] newColRowHeaders = new String[colHeaders1.length];
		for (int i = 0; i < colHeaders1.length; i++) {
			newColRowHeaders[i] = row + (i + 1);
		}
		return newColRowHeaders;

		// Other possible cases for the else case
		// it's assumed that this type will not occur
		// 1. rowHeaders.length < colHeaders.length
		// 2. rowHeaders.length > colHeaders.length
	}// getUnits()

	public OverallTableModel getModel() {
		return tableModel;
	}

	public int getNumOfDataColumns() {
		if (selDataColNames != null) {
			return selDataColNames.length;
		}

		return 0;
	}

	public OverallTableModel getOverallTableModel() {
		return tableModel;
	}

	public void setSelDataColumns(String[] dataColumns) {
		selDataColNames = dataColumns;
	}

	public void setOverallTableModel(OverallTableModel tableModel, Component parent) throws Exception {
		String[] arrayColumnNames = tableModel.getColumnNames();
		
		Vector columnNames = new Vector();
		for (int i = 0; i < arrayColumnNames.length; i++) {
			columnNames.add(arrayColumnNames[i]);
		}
		String[] dataCols = getSelDataColumnNames();
		Vector newDataColNames = new Vector();
		Vector newLabelColNames = new Vector();
		Vector newDateColNames = new Vector();
		Vector missDataColNames = new Vector();
		Vector missLabelColNames = new Vector();
		Vector missDateColNames = new Vector();
		if (dataCols != null) {
			for (int i = 0; i < dataCols.length; i++) {
				if (columnNames.contains(dataCols[i])) {
					newDataColNames.add(dataCols[i]);
				} else {
					missDataColNames.add(dataCols[i]);
				}
			}// for(i)
		}// if (dataCols != null)

		String[] labelCols = getSelLabelColumnNames();
		if (labelCols != null) {
			for (int i = 0; i < labelCols.length; i++) {
				if (columnNames.contains(labelCols[i])) {
					newLabelColNames.add(labelCols[i]);
				} else if (labelCols[i].equalsIgnoreCase(NOT_SELECTED)) {
					newLabelColNames.add(labelCols[i]);
				} else {
					missLabelColNames.add(labelCols[i]);
				}
			}// for
		}// if(labelCols != null)

		String[] dateCols = getSelLabelDateColNames();
		if (dateCols != null) {
			for (int i = 0; i < dateCols.length; i++) {
				if (columnNames.contains(dateCols[i])) {
					newDateColNames.add(dateCols[i]);
				} else if (dateCols[i].equalsIgnoreCase(NOT_SELECTED)) {
					newDateColNames.add(dateCols[i]);
				} else {
					missDateColNames.add(dateCols[i]);
				}
			}// for
		}// if(dateCols != null)

		if (newDataColNames.size() == 0) {
			throw new Exception("The current table does not have any column names that"
					+ " are used as data column names in the configuration file.");
		}// if(newLabelColNames.size() == 0)
		else if (missDataColNames.size() > 0) {
			String colString = "";
			for (int i=0; (i < missDataColNames.size()) && (i < 8); i++)
			{
			   colString += missDataColNames.elementAt(i);
			   colString += ", ";
			}
			String errorString = "The current table does not have all the data column names "+
			   " specified in the configuration file for plot "+this.getPlotName()+", such as: "+ colString + "...";
			showPlotError(errorString, parent);
		}// else if (missDataColNames.size()>0)

		String aePlotType = PlotTypeConverter.getAEPlotType(plotType);
		if (TreeDialog.isLabelRequired(null, aePlotType)) {
			if (aePlotType.equals(AnalysisEngineConstants.TIME_SERIES_PLOT)) {
				if (newDateColNames.size() == 0) {
					throw new Exception("This plot '" + getPlotName() + "'requires a date label."
							+ "\nThe current table does not have the column that"
							+ " is listed as the date label column in the configuration file.");
				} else if (missDateColNames.size() > 0) {
					String dateString = "The current table does "
						+ "not have the column specified for the date label in the "
						+ "configuration file for plot "+getPlotName()+": "+missDateColNames.elementAt(0);
					showPlotError(dateString, parent);
				}
			} else if (newLabelColNames.size() == 0) {
				String colString = expandVectorIntoString(newLabelColNames);
				String labelString = "The plot " + getPlotName()+"[type="+plotType + "], requires a label."
				+ "\nThe current table does not have the columns that"
				+ " are used as label columns in the specified configuration file: "+colString;
				throw new Exception(labelString);
			}// if(newLabelColNames.size() == 0)
			else if (missLabelColNames.size() > 0) {
				String colString = expandVectorIntoString(missLabelColNames);
				String labelString = "The current table does "
			  	   + "not have the columns used as label columns in the "
				   + "configuration file: "+colString; 
				
				showPlotError(labelString, parent);
			}// else if
		}// if(isLabelRequired)

		String[] newData = new String[newDataColNames.size()];
		String[] newLabel = new String[newLabelColNames.size()];
		String[] newDate = new String[newDateColNames.size()];
		this.selDataColNames = (String[]) newDataColNames.toArray(newData);
		this.selLabelColNames = (String[]) newLabelColNames.toArray(newLabel);
		// System.out.println(selLabelColNames);
		// for(int i=0; i< selLabelColNames.length; i++)
		// {
		// System.out.println("selLabelColNames[i]="+selLabelColNames[i]);
		// }
		this.selLabelDateColNames = (String[]) newDateColNames.toArray(newDate);
		this.tableModel = tableModel;
	}// setTableModel

	private String expandVectorIntoString(Vector newLabelColNames) {
		String nameString = "";
		for (int i=0; i < newLabelColNames.size(); i++)
		{
			nameString += newLabelColNames.elementAt(i);
			if (i < newLabelColNames.size()-1)
			   nameString += ", ";
		}
		return nameString;
	}

	private void showPlotError(String messageString, Component parent) {
		// don't show any messages within 2 minutes of the last message
		if (System.currentTimeMillis() > (lastMessageTime + 60000))
		{
			DefaultUserInteractor.get().notify(
					parent,
					"Warning",
					messageString+"\n\nSee the output log for additional warnings about other plots.", 
					UserInteractor.WARNING);
			
			lastMessageTime = System.currentTimeMillis();
		}
		System.out.println("WARNING: "+messageString);
		
	}

	public int[] convertSelection(boolean[] selected) {
		int[] selections = new int[selected.length];
		int count = 0;

		for (int i = 0; i < selected.length; i++) {
			if (selected[i]) {
				selections[count] = i;
				count++;
			}
			// if (selected[i])
		}

		// for int i
		int[] newArray = new int[count];

		for (int j = 0; j < count; j++) {
			newArray[j] = selections[j];
		}
		return newArray;
	}

	// getSelectedHeaders()
	public void setSeparator(char separator) {
		this.separator = separator;
	}

	// setSeparator()
	public String getSeparator() {
		return "" + separator;
	}

	// getSeparator()

	/**
	 * combine the selected data columns to display in the textfield
	 * 
	 * @return a string that concatenates all the data column selections
	 */
	public String getDataColumnSelection() {
		String retn = "";

		if ((selDataColNames == null) || (selDataColNames.length == 0)) {
			return retn;
		}

		for (int i = 0; i < selDataColNames.length; i++) {
			retn = retn.concat(selDataColNames[i] + ", ");
		}
		// truncate the last ', '
		if (retn.length() > 2) {
			retn = retn.substring(0, retn.length() - 2);
		}
		return retn;
	}

	public DataSets createDataSets() throws Exception {
		if ((selDataColNames == null) || (selDataColNames.length == 0)) {
			throw new Exception("There are no Data Columns selected. Please select some");
		}
		DataSets tableDataSets = new DataSets();
		if (plotType.equals(PlotTypeConverter.TIMESERIES_PLOT)) {
			TableDateTimeSeries dataSeries;
			// there will be one data series for each valueColumn
			int[] dateColumns = null;
			String pattern = "";
			// assume that selLabelDateColNames size =2
			// first columat type is simple date foramat
			SimpleDateFormat dateFormat = (SimpleDateFormat) tableModel.getFormat(selLabelDateColNames[0]);
			if (!selLabelDateColNames[1].equals(PlottingInfo.NOT_SELECTED)) {
				pattern = timeFormat + ' ' + dateFormat.toPattern();
				dateColumns = new int[2];
				dateColumns[0] = tableModel.getColumnNameIndex(selLabelDateColNames[0]);
				dateColumns[1] = tableModel.getColumnNameIndex(selLabelDateColNames[1]);
			} else {
				pattern = dateFormat.toPattern();
				dateColumns = new int[1];
				dateColumns[0] = tableModel.getColumnNameIndex(selLabelDateColNames[0]);
			}
			for (int i = 0; i < selDataColNames.length; i++) {
				// Make sure for the TimeSeries Plot that only one column is selected
				// and type of the column SHOULD be of type Date
				int index = tableModel.getColumnNameIndex(selDataColNames[i]);
				dataSeries = new TableDateTimeSeries(tableModel, index, dateColumns, pattern);
				tableDataSets.add(dataSeries, dataSeries.getName());
			}// for
		}// if
		else {
			TableDataSeries dataSeries = null;
			int[] labelColumns = new int[selLabelColNames.length];
			for (int i = 0; i < selLabelColNames.length; i++) {
				labelColumns[i] = tableModel.getColumnNameIndex(selLabelColNames[i]);
			}// for(i)

			for (int i = 0; i < selDataColNames.length; i++) {
				int index = tableModel.getColumnNameIndex(selDataColNames[i]);
				dataSeries = new TableDataSeries(tableModel, index, labelColumns, separator);
				tableDataSets.add(dataSeries, dataSeries.getName());
			}// for

		}// else
		Vector dataSets = tableDataSets.getDataSets(null, null);
		if (!userSpecifiedUnits) {
			checkSameUnits();
			for (int i = 0; i < dataSets.size(); i++) {
				TableDataSeries dataSet = (TableDataSeries) dataSets.get(i);
				int index = tableModel.getColumnNameIndex(selDataColNames[i]);
				String[] colHeaders = tableModel.getColumnHeaders(index);
				if (colHeaders != null && colRowHeaderIndex != -1) {
					dataSet.setUnits(colHeaders[colRowHeaderIndex]);
				}
			}// for
		}// if(!userSpecifiedUnits)
		else {
			for (int i = 0; i < dataSets.size(); i++) {
				TableDataSeries dataSet = (TableDataSeries) dataSets.get(i);
				dataSet.setUnits(units);
			}// for(i)
		}// else
		return tableDataSets;
	}

	/**
	 * a helper method to check whether user labels are same(not case sensitive) or not if there are not same then show
	 * a warning message
	 * 
	 */
	private void checkSameUnits() {
		String[] colHeaders = tableModel.getColumnHeadersInARow(colRowHeaderIndex);
		if (colHeaders == null) {
			return;
		}
		int index = tableModel.getColumnNameIndex(selDataColNames[0]);
		String firstLabel = colHeaders[index];
		// System.out.println("firstUnitLabel = "+ firstLabel);
		for (int i = 1; i < selDataColNames.length; i++) {
			index = tableModel.getColumnNameIndex(selDataColNames[i]);
			if (!firstLabel.equalsIgnoreCase(colHeaders[index])) {
				// System.out.println("colHeaders["+i+"]="+ colHeaders[dataColumns[i]]);
				DefaultUserInteractor.get().notify(null, "WARNING", "The selected columns" + 
						" do not have the same units",
						UserInteractor.WARNING);
				units = "";
				return;
			}
		}
	}

	public String[] getSelDataColumnNames() {
		return selDataColNames;
	}

	public String[] getSelLabelColumnNames() {
		return selLabelColNames;
	}

	public void setSelLabelColumnNames(String[] labelColNames) {
		selLabelColNames = labelColNames;
	}

	public PlottingInfo copy() {
		PlottingInfo info = new PlottingInfo(this.tableModel);
		String sep = getSeparator();
		info.setSeparator(sep.charAt(0));
		info.setPlotType(getPlotType());

		info.setUserSpecifiedUnits(userSpecifiedUnits);
		info.setColRowHeaderIndex(colRowHeaderIndex);

		if (this.selDataColNames != null) {
			info.setSelDataColumns(selDataColNames);
		}
		if (this.selLabelColNames != null) {
			info.setSelLabelColumnNames(selLabelColNames);
		}
		if (selLabelDateColNames != null) {
			info.setSelLabelDateColNames(selLabelDateColNames);
		}
		info.setTimeFormat(getTimeFormat());
		info.setUnits(getUnits());
		return info;
	}

	public int getColRowHeaderIndex() {
		return colRowHeaderIndex;
	}

	public void setColRowHeaderIndex(int colRowHeaderIndex) {
		this.colRowHeaderIndex = colRowHeaderIndex;
		String[] headers = tableModel.getColumnHeadersInARow(colRowHeaderIndex);
		if (selDataColNames != null && selDataColNames.length > 0) {
			int index = tableModel.getColumnNameIndex(selDataColNames[0]);
			if (headers != null) {
				units = headers[index];// set the first units for the label
			}
		}
	}

	public boolean isUserSpecifiedUnits() {
		return userSpecifiedUnits;
	}

	public void setUserSpecifiedUnits(boolean userSpecifiedUnits) {
		this.userSpecifiedUnits = userSpecifiedUnits;
	}

	public String[] getSelLabelDateColNames() {
		return this.selLabelDateColNames;
	}

	public void setSelLabelDateColNames(String[] selLabelDateColNames) {
		this.selLabelDateColNames = selLabelDateColNames;
	}

	public String getTimeFormat() {
		return timeFormat;
	}

	public void setTimeFormat(String timeFormat) {
		this.timeFormat = timeFormat;
	}
}
