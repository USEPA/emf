package gov.epa.mims.analysisengine.table;

import gov.epa.mims.analysisengine.gui.DefaultUserInteractor;
import gov.epa.mims.analysisengine.gui.GUIUserInteractor;
import gov.epa.mims.analysisengine.gui.OptionDialog;
import gov.epa.mims.analysisengine.gui.OptionInfo;
import gov.epa.mims.analysisengine.gui.TreeDialog;
import gov.epa.mims.analysisengine.gui.UserInteractor;
import gov.epa.mims.analysisengine.table.filter.FilterCriteria;
import gov.epa.mims.analysisengine.table.persist.AnalysisConfiguration;
import gov.epa.mims.analysisengine.table.persist.SaveConfigAction;
import gov.epa.mims.analysisengine.table.persist.SaveConfigModel;
import gov.epa.mims.analysisengine.table.plot.PlotTypeConverter;
import gov.epa.mims.analysisengine.table.plot.PlottingInfo;
import gov.epa.mims.analysisengine.table.plot.PlottingInfoView;
import gov.epa.mims.analysisengine.table.sort.SortCriteria;
import gov.epa.mims.analysisengine.table.stats.HistogramModel;
import gov.epa.mims.analysisengine.table.stats.PercentileModel;
import gov.epa.mims.analysisengine.table.stats.RegressionModel;
import gov.epa.mims.analysisengine.table.stats.StatisticsGUI;
import gov.epa.mims.analysisengine.table.stats.StatisticsModel;
import gov.epa.mims.analysisengine.table.stats.WekaExplorer;
import gov.epa.mims.analysisengine.tree.AvailableOptionsAndDefaults;
import gov.epa.mims.analysisengine.tree.Branch;
import gov.epa.mims.analysisengine.tree.DataSets;
import gov.epa.mims.analysisengine.tree.DataSetsAdapter;
import gov.epa.mims.analysisengine.tree.LineType;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;

public class SUSortFilterTablePanel extends SortFilterTablePanel {

	/** the plotting info object * */
	protected PlottingInfo plotInfo = null;

	/**
	 * statistics model which contains the data model for the statistics
	 */
	private StatisticsModel statisticsModel;

	/** a data model for the 'show topN Rows gui from the tool bar */
	private TopNRowsModel topNRowsModel;

	/** a data model for the 'show bottomN Rows gui from the tool bar */
	private TopNRowsModel botNRowsModel;

	private String source;

	private String tabName;

	private JTabbedPane mainTabbedPane;

	static HashMap previousConfig = new HashMap();

	public SUSortFilterTablePanel(Component parent, String source, String tabName, JTabbedPane maintTabbedPane,
			MultiRowHeaderTableModel tableModel) {
		super(parent, tableModel);
		this.source = source;
		this.tabName = tabName;
		this.mainTabbedPane = maintTabbedPane;
		aconfig = new AnalysisConfiguration(overallModel);
	}

	/**
	 * Create the popup menu for the table. Also add any items that are not column specific to the toolbar.
	 */
	protected void createPopupMenuAndToolBar() {
		Action action = null;

		popupMenu = new JPopupMenu("Table Operations");

		action = new SortMultipleAction(this);
		popupMenu.add(action);
		JButton sortButton = toolBar.add(action);

		sortButton.setToolTipText("Sort(Ascending/Descending)");

		popupMenu.addSeparator();

		action = new ShowTopOrBottomValuesAction(this, true, true);
		JButton topNRowsButton = toolBar.add(action);

		topNRowsButton.setToolTipText("Top N Rows");

		action = new ShowTopOrBottomValuesAction(this, true, false);
		popupMenu.add(action);

		action = new ShowTopOrBottomValuesAction(this, false, true);
		JButton botNRowsButton = toolBar.add(action);

		botNRowsButton.setToolTipText("Bottom N Rows");

		action = new ShowTopOrBottomValuesAction(this, false, false);
		popupMenu.add(action);

		action = new FilterAction(this);
		popupMenu.add(action);
		JButton filterButton = toolBar.add(action);

		filterButton.setToolTipText("Filter Rows");

		action = new ShowHideColumnsAction(this);
		popupMenu.add(action);
		JButton showHideButton = toolBar.add(action);
		showHideButton.setToolTipText("Show/Hide Columns");
		popupMenu.addSeparator();

		action = new SingleFormatAction(this);
		popupMenu.add(action);

		action = new MultipleFormatAction(this);
		popupMenu.add(action);
		JButton formatButton = toolBar.add(action);

		formatButton.setToolTipText("Format columns");
		// popupMenu.add(new AggregateRowsAction(this));
		// popupMenu.add(new AggregateColumnsAction(this));

		popupMenu.addSeparator();

		action = new PlotAction(this);
		popupMenu.add(action);
		JButton plotButton = toolBar.add(action);

		plotButton.setToolTipText("Plot");

		action = new StatsticsAction(this);
		popupMenu.add(action);
		JButton statsButton = toolBar.add(action);
		statsButton.setToolTipText("Statistics");

		popupMenu.addSeparator();

		action = new SaveConfigAction(this, configIcon);
		popupMenu.add(action);
		JButton saveConfigButton = toolBar.add(action);

		saveConfigButton.setToolTipText("Analysis Configuration");
		/*
		 * action = new LoadConfigAction(this); popupMenu.add(action); JButton LoadConfigButton = toolBar.add(action);
		 * 
		 * LoadConfigButton.setToolTipText("Load Configuration");
		 */
		action = new ResetAction(this);
		popupMenu.add(action);
		JButton resetButton = toolBar.add(action);

		resetButton.setToolTipText("Reset");

		TableMouseAdapter tableMouseAdapter = new TableMouseAdapter();
		PopupMouseAdapter popupMouseAdapter = new PopupMouseAdapter(scrollPane);

		table.getTableHeader().addMouseListener(tableMouseAdapter);
		table.addMouseListener(popupMouseAdapter);
		// table.getColumnModel().getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
	} // createPopupMenu()

	public void loadConfigFile(File file, boolean loadTableConfig, boolean binaryFormat) {
		try {
			aconfig.loadConfiguration(file, loadTableConfig, binaryFormat, parent);
			updateFormat();
		} catch (Exception e) {
			new GUIUserInteractor().notify(this, "Error Loading Configuration", e.getMessage(), UserInteractor.ERROR);
		}
	}

	/**
	 * Present an input dialog to the user to get a number. Filter the table to only show the top or bottom N rows. If N
	 * is greater than the number of rows, show all rows.
	 * 
	 * @param showTop
	 *            boolean that is true if we should show the top N rows, false if we should show the bottom N rows.
	 * @param isToolBar
	 *            boolean if true means this action event started from a tool bar click and false means event started
	 *            from a popup menu.
	 */
	public void showNRows(boolean showTop, boolean isToolBar) {
		int selectedColumn = -1;
		String columnName = null;
		TopNRowsGUI dialog = null;
		int maximumRows = overallModel.getBaseRowCount();

		if (!isToolBar) {
			selectedColumn = table.getColumnModel().getColumnIndexAtX(currentX);

			// Don't try to sort on the row number column (column 0).
			if (selectedColumn == 0) {
				return;
			}// if (selectedColumn == 0)

			// Get the column name.
			columnName = table.getColumnName(selectedColumn);
			// Get the number of rows to show from the user.
			dialog = new TopNRowsGUI((JFrame) parent, table.getColumnName(selectedColumn), showTop, maximumRows);
		} // if(!isToolBar)
		else {
			if (showTop) {
				if (topNRowsModel == null) {
					topNRowsModel = new TopNRowsModel(showTop, overallModel);
				} else {
					topNRowsModel.setOverallTableModel(overallModel);
				}
				dialog = new TopNRowsGUI((JFrame) parent, topNRowsModel, isToolBar, maximumRows);
			} // if(showTop)
			else {
				if (botNRowsModel == null) {
					botNRowsModel = new TopNRowsModel(showTop, overallModel);
				} else {
					botNRowsModel.setOverallTableModel(overallModel);
				}
				dialog = new TopNRowsGUI((JFrame) parent, botNRowsModel, isToolBar, maximumRows);
			}

		}// else

		dialog.setLocationRelativeTo(this);
		dialog.setVisible(true);

		if (dialog.getResult() == OptionDialog.OK_RESULT) {
			if (isToolBar) {
				selectedColumn = dialog.getSelectedColumn();
				columnName = table.getColumnName(selectedColumn);
			}// if(isToolBar)
			boolean showRows = dialog.getShowRows();

			int numRows = dialog.getNumRows();

			overallModel.filterReset(true, false);

			// Sort the values first. (descending for top N, ascending for bottom N)
			SortCriteria sortCriteria = new SortCriteria(new String[] { columnName }, new boolean[] { !showTop },
					new boolean[] { true });

			sort(sortCriteria);

			// Then filter to show the top/bottom N number of rows.
			int operation = FilterCriteria.GREATER_THAN_OR_EQUAL;

			if (!showTop) {
				operation = FilterCriteria.LESS_THAN_OR_EQUAL;
			}

			Comparable value = null;

			// If the user entered '0', then we will show now rows. We do this
			// by filtering on anything that starts with '~|'. Hopefully, we will
			// never see date like this.
			if (numRows == 0) {
				operation = FilterCriteria.STARTS_WITH;
				value = "~|";
			} else if (showRows) {
				// The desired value will always be N-1 rows from the top.
				value = (Comparable) table.getValueAt(numRows - 1, selectedColumn);
			} else // showing percentage
			{
				// The desired value is a certain percentage from the top.
				// In this case, numRows is the percentage to show.
				int cutoffRow = (int) (overallModel.getBaseRowCount() * (numRows / 100.0)) - 1;

				value = (Comparable) table.getValueAt(cutoffRow, selectedColumn);
			}

			FilterCriteria filterCriteria = overallModel.getFilterCriteria();

			// If there is no filter criteria, then create a new one.
			if (filterCriteria == null) {
				String[] column = { table.getColumnName(selectedColumn) };
				int[] ops = { operation };
				Comparable[] values = { value };
				// The value for and/or does not matter here since we have only
				// one criteria on which to filter.
				filterCriteria = new FilterCriteria(column, ops, values,
				/* formats, */
				overallModel.getBaseColumnNames(), true);
				filterCriteria.setTableModel(overallModel);
			} // if (currentFilterCriteria == null)
			// Otherwise modify the existing one.
			else {
				String[] column = { table.getColumnName(selectedColumn) };
				int[] ops = { operation };
				Comparable[] values = { value };
				// The value for and/or does not matter here since we have only
				// one criteria on which to filter.
				filterCriteria.setRowCriteria(column, ops, values,
				/* allColumnFormats, */true);
			} // else

			overallModel.filterRows(filterCriteria);
		}
	} // showNRows()

	public void showPlots() {
		try {
			SaveConfigModel sc = new SaveConfigModel(aconfig);
			String[] configNames = sc.getConfigNames();
			for (int i = 0; i < configNames.length; i++) {
				sc.showPlot(i, this);
			}
		} catch (Exception e) {
			new GUIUserInteractor().notify(this, "Show Plots", "Error showing plots " + e.getMessage(),
					UserInteractor.ERROR);
		}
		return;
	}

	public void showPlotGUI() {
		String plotName = " "; // We've been using plotName field in PlottingGUI

		// which is used by the Plot object to set the string equivalent of Plot Type
		// We will use the plotName field to get user specified name but reset the value
		// in PlotInfo object for its initial usage motive
		if (plotInfo == null) {
			plotInfo = new PlottingInfo(overallModel);
			boolean[] doubleCols = overallModel.getDoubleColumnTypes();
			String[] selCols = setSelectedDataColumns(doubleCols);
			plotInfo.setSelDataColumns(selCols);
		} // if (plotInfo == null)

		// deduct 3 for the three default configs
		plotInfo.setPlotName("Plot" + (aconfig.getCount() + 1 - 3));

		PlottingInfoView plotGUI = new PlottingInfoView((JFrame) parent, plotInfo, true);

		// plotGUI.show();
		plotGUI.setVisible(true);

		if (plotGUI.getResult() == OptionDialog.OK_RESULT) {
			try {
				DataSets dataSets;
				Branch data;

				plotInfo = plotGUI.getPlottingInfo();
				plotName = plotInfo.getPlotName();
				plotInfo.setPlotName(" "); // why ?? RP
				dataSets = plotInfo.createDataSets();

				/*
				 * if(previousConfig.containsKey(plotInfo.getPlotType())) { data =
				 * (gov.epa.mims.analysisengine.tree.DataSets)previousConfig.get(plotInfo.getPlotType());
				 * dataSets.add(((Branch)(data.clone())).getChild(0)); data =
				 * gov.epa.mims.analysisengine.gui.TreeDialog.showTreeDialog(dataSets, dataSets, null, null); } else {
				 */
				String tablePlotType = plotInfo.getPlotType();
				String unit = plotInfo.getUnits();
				if (unit == null) {
					unit = "";
				}
				setDefaultOptions(tablePlotType);
				String aePlotType = PlotTypeConverter.getAEPlotType(tablePlotType);
				HashMap hashMap = new HashMap();
				String depAxis = TreeDialog.getDependentAxis(this, aePlotType);
				hashMap.put(depAxis, unit);
				String title = "Customize " + aePlotType;
				boolean setPlotDefaults = true;
				if (plotInfo.getPlotType().equals(PlotTypeConverter.XY_LINES_PLOT))
					setPlotDefaults = false;

				TreeDialog dialog = new TreeDialog((JFrame) parent, aePlotType, dataSets, null, hashMap,
						setPlotDefaults);
				dialog.setTitle(title);
				dialog.setVisible(true);
				data = dialog.getResultTree();
				if (plotName != null) {
					aconfig.storePlotConfig(plotName, data, plotInfo, false);
				}
				plotInfo = plotInfo.copy();
			} // try
			catch (Exception e) {
				DefaultUserInteractor.get().notify(this, "Error", e.getMessage(), UserInteractor.ERROR);
			}
		}
	}

	private String[] setSelectedDataColumns(boolean[] showDataColumns) {
		ArrayList selColumns = new ArrayList();
		for (int i = 0; i < showDataColumns.length; i++) {
			if (showDataColumns[i]) {
				selColumns.add(overallModel.getColumnName(i + 1));
			}
		}// for
		String[] selDataCol = new String[selColumns.size()];
		return (String[]) selColumns.toArray(selDataCol);

	}

	/*
	 * a helper method to set the default option for LineType analysis option depend on the plot type
	 */
	private void setDefaultOptions(String plotType) {
		LineType lineType = new LineType();
		if (plotType.equals(PlotTypeConverter.XY_LINES_PLOT)) {
			lineType.setPlotStyle(LineType.LINES);
		} else { // for all other type it's set to points including XY_Plot
			lineType.setPlotStyle(LineType.POINTS);
		}
		AvailableOptionsAndDefaults.addGlobalDefaultValue(OptionInfo.LINE_TYPE, lineType);
	}

	/**
	 * Show the statistics GUI
	 */
	public void showStatsticsGUI() {
		StatisticsGUI statisticsGUI = null;

		if (statisticsModel == null) {
			statisticsModel = new StatisticsModel(overallModel);
			// select all the columns which are of double type
			boolean[] showColumns = statisticsModel.getDoubleColumnTypes();
			statisticsModel.setShowColumns(showColumns);
		} else {
			statisticsModel.setOverallTableModel(overallModel);
		}

		statisticsGUI = new StatisticsGUI((JFrame) parent, statisticsModel, tabName);
		if ((JOptionPane.CANCEL_OPTION == statisticsGUI.getResult())) {
			return;
		}

		if (statisticsModel.isBasicStatsAnalysis()) {
			try {
				Object[][] data = statisticsModel.calculateBasicStatistics();
				ArrayList tableData = new ArrayList();
				for (int i = 0; i < data.length; i++) // rows
				{
					ArrayList rowData = new ArrayList();
					for (int j = 0; j < data[0].length; j++)// columns
					{
						rowData.add(data[i][j]);
					}// for(j)
					tableData.add(rowData);
				}// for(i)

				String[] names = statisticsModel.getBasicStatsColNames();
				String[][] colHeader = { names };

				Class[] colType = new Class[data[0].length];
				colType[0] = String.class;
				for (int i = 1; i < data[0].length; i++) {
					colType[i] = Double.class;
				}// for(i)

				SpecialTableModel tableModel = new SpecialTableModel(null, colHeader, tableData, colType);
				StatisticsModel.basicStats_counter++;
				String basicStatsTabName = statisticsModel.getBasicStatsTabName();
				insertTab(tableModel, basicStatsTabName);
			} catch (Exception e) {
				DefaultUserInteractor.get().notifyOfException(this, "Error", e, UserInteractor.ERROR);
			}
		}// if(statisticsModel.isBasicStatsAnalysis())

		if (statisticsModel.isHistogramAnalysis()) {
			DataSetsAdapter histogramDataSets = statisticsGUI.getHistogramDataSets();
			String histogramTabName = statisticsGUI.getHistogramName();

			if (histogramDataSets != null) {
				HistogramModel.histogram_counter++;
				LabeledDataSetsTableModel dataSetTableModel = new LabeledDataSetsTableModel(histogramDataSets);
				dataSetTableModel.setFirstColumnName("Bins");
				insertTab(dataSetTableModel, histogramTabName);
			}
		}// if(statisticsModel.isHistogramAnalysis())

		if (statisticsModel.isPercentileAnalysis()) {
			DataSetsAdapter percentileDataSets = statisticsGUI.getPercentileDataSets();
			String percentileName = statisticsGUI.getPercentileName();

			if (percentileDataSets != null) {
				PercentileModel.percentile_counter++;
				LabeledDataSetsTableModel dataSetTableModel = new LabeledDataSetsTableModel(percentileDataSets);
				dataSetTableModel.setFirstColumnName("Percentiles");
				insertTab(dataSetTableModel, percentileName);
			}
		}

		if (statisticsModel.isCorrelationAnalysis()) {
			try {
				RegressionModel model = statisticsModel.getRegressionModel();
				SpecialTableModel sModel = model.computeLR(source, true);
				insertTab(sModel, model.getTabName(true));
			} catch (Exception ie) {
				new GUIUserInteractor().notify(this, "Error", "Unable to generate 1-to-1 "
						+ "correlation analysis data: " + ie.getMessage(), UserInteractor.ERROR);
				return;
			}
		}

		if (statisticsModel.isRegressionAnalysis()) {
			try {

				RegressionModel model = statisticsModel.getRegressionModel();
				SpecialTableModel sModel = model.computeLR(source, false);
				insertTab(sModel, model.getTabName(true));
			} catch (Exception ie) {
				new GUIUserInteractor().notify(this, "Error", "Unable to generate Linear " + "Regression data: "
						+ ie.getMessage(), UserInteractor.ERROR);
				return;
			}
		}

		if (statisticsModel.isWekaAnalysis()) {
			weka.core.Instances data = statisticsModel.createWekaInstances();
			WekaExplorer explorer = new WekaExplorer(parent, data);
			explorer.showGUI();
		}
	} // showStatisticsGUI()

	private void insertTab(SpecialTableModel tableModel, String tabName) {
		if (parent instanceof TableApp) {
			((TableApp) parent).insertIntoTabbedPane(tableModel, tabName, tabName, null);
		} else {
			TablePanel panel = new TablePanel(parent, tableModel, source, tabName, null, mainTabbedPane);
			mainTabbedPane.addTab(tabName, null, panel, source);
		}
	}

	/**
	 * An action to show the top/bottom n values in the list.
	 */
	class ShowTopOrBottomValuesAction extends AbstractAction {
		SUSortFilterTablePanel parent = null;

		// True if we should show the top N rows. False is we should show the
		// bottom N rows.
		boolean isTop = false;

		/** to indicate wheter event fired from a toolBar or from a pop-up menu */
		private boolean isToolBar = false;

		public ShowTopOrBottomValuesAction(SUSortFilterTablePanel parent, boolean isTop, boolean isToolBar) {
			super("Show " + ((isTop) ? "Largest" : "Smallest") + " N rows...", (isTop) ? topNRowsIcon : bottomNRowsIcon);
			this.isTop = isTop;
			this.isToolBar = isToolBar;
			this.parent = parent;
		} // FilterAction()

		public void actionPerformed(ActionEvent e) {
			parent.showNRows(isTop, isToolBar);
		} // actionPerformed()
	} // class ShowTopOrBottomValuesAction

	/**
	 * Sort By Multiple Columns Action.
	 */
	class PlotAction extends AbstractAction {
		SUSortFilterTablePanel parent = null;

		public PlotAction(SUSortFilterTablePanel parent) {
			super("Plot Data", plotIcon);
			this.parent = parent;
		} // SortMultipleAction()

		public void actionPerformed(ActionEvent e) {
			parent.showPlotGUI();
		} // actionPerformed()
	} // class PlotAction

	class StatsticsAction extends AbstractAction {
		SUSortFilterTablePanel parent = null;

		public StatsticsAction(SUSortFilterTablePanel parent) {
			super("Statistics", statsIcon);
			this.parent = parent;
		} // StatsHistogramAction()

		public void actionPerformed(ActionEvent e) {
			parent.showStatsticsGUI();
		} // actionPerformed()
	} // class StatsticsAction

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) {
		java.util.Calendar cal = java.util.Calendar.getInstance();

		cal.set(java.util.Calendar.MILLISECOND, 0);
		cal.set(1990, 0, 1, 0, 0, 0);
		final int NUM_ROWS = 26;
		final int NUM_COLS = 5;
		Object[][] data = new Object[NUM_ROWS][NUM_COLS];
		char[] charArray1 = { 'a', 'a', 'a', 'a', 'a' };
		char[] charArray2 = { 'Z', 'Z', 'Z', 'Z', 'Z' };
		for (int r = 0; r < NUM_ROWS; r++) {
			data[r] = new Object[NUM_COLS];
			data[r][2] = Integer.valueOf(r);
			data[r][3] = Double.valueOf(r * 10.0);
			data[r][4] = cal.getTime();
			cal.add(java.util.Calendar.HOUR, 1);
		} // for(r)

		int r = 0;

		charArray1[0] = 'a';
		charArray2[0] = 'Z';
		outer: for (int i = 0; i < 26 && r < NUM_ROWS; i++) {
			charArray1[1] = 'a';
			charArray2[1] = 'Z';
			for (int j = 0; j < 26 && r < NUM_ROWS; j++) {
				charArray1[2] = 'a';
				charArray2[2] = 'Z';
				for (int k = 0; k < 26 && r < NUM_ROWS; k++) {
					charArray1[3] = 'a';
					charArray2[3] = 'Z';
					for (int l = 0; l < 26 && r < NUM_ROWS; l++) {
						charArray1[4] = 'a';
						charArray2[4] = 'Z';
						for (int m = 0; m < 26 && r < NUM_ROWS; m++) {
							data[r][0] = new String(charArray1);
							data[r][1] = new String(charArray2);
							charArray1[4]++;
							charArray2[4]--;
							r++;
							if (r >= NUM_ROWS) {
								break outer;
							}
						}
						charArray1[3]++;
						charArray2[3]--;
					}
					charArray1[2]++;
					charArray2[2]--;
				}
				charArray1[1]++;
				charArray2[1]--;
			}
			charArray1[0]++;
			charArray1[0]--;
		}

		final String[][] columnHeaders = { { "String 1", "String 2", "Integer", "Double", "Date" },
				{ "g/cm3", "lb/in^2", "m/s", "items/hectare", "years" } };

		final SimpleTestModel tableModel = new SimpleTestModel(data, null, columnHeaders);

		// final SpecialTableModel tableModel = new SpecialTableModel();

		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				JFrame f = new JFrame("SUSortFilterTablePanel");
				f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				java.awt.Container contentPane = f.getContentPane();
				contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.X_AXIS));
				SUSortFilterTablePanel sftp = new SUSortFilterTablePanel(f, "Simple Table Model", null, null,
						tableModel);

				contentPane.add(sftp);

				f.pack();
				f.setVisible(true);
			}
		});

	}

}
