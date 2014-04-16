package gov.epa.mims.analysisengine.table.stats;

import gov.epa.mims.analysisengine.gui.DefaultUserInteractor;
import gov.epa.mims.analysisengine.gui.OptionDialog;
import gov.epa.mims.analysisengine.gui.ScreenUtils;
import gov.epa.mims.analysisengine.gui.StringValuePanel;
import gov.epa.mims.analysisengine.gui.UserInteractor;
import gov.epa.mims.analysisengine.stats.Histogram;
import gov.epa.mims.analysisengine.stats.Percentile;
import gov.epa.mims.analysisengine.stats.SummaryStats;
import gov.epa.mims.analysisengine.table.JTabbedPaneWithCloseIcons;
import gov.epa.mims.analysisengine.table.OverallTableModel;
import gov.epa.mims.analysisengine.table.SelectColumnsGUI;
import gov.epa.mims.analysisengine.table.SpecialTableModel;
import gov.epa.mims.analysisengine.table.TableDataSeries;
import gov.epa.mims.analysisengine.table.filter.FilterCriteria;
import gov.epa.mims.analysisengine.tree.DataSets;
import gov.epa.mims.analysisengine.tree.DataSetsAdapter;
import gov.epa.mims.analysisengine.tree.LabeledDataSetIfc;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

/*
 * StatisticsGUI.java
 * <p>Description: A dialog to select the data columns and then set parameter for
 * different statistics <p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: CEP, UNC-Chapel Hill </p>
 * @author Parthee Partheepan
 * @version $Id: StatisticsGUI.java,v 1.1 2006/11/01 15:33:38 parthee Exp $
 */

public class StatisticsGUI extends JDialog {
	/** a tabbed pane to insert different tabs */
	private JTabbedPane tabbedPane;

	/** a message field to show the list of columns selected */
	private JTextField dataMessageField;

	/** a message label to indicate the no of data columns selected */
	private JLabel dataMessageLabel;

	private RegressionGUI regressionGUI;

	private RegressionGUI correlationGUI;

	/** histogram gui */
	private HistogramGUI histogramGUI;

	/* percentileGUI */
	private PercentileGUI percentileGUI;

	/** a check box to indicate whether to do the basic Stats Analysis */
	private JCheckBox basicStatsCheckBox;

	/** a check box to indicate whether to do Weka analysis */
	private JCheckBox wekaCheckBox;

	/**
	 * a text value panel for entering tab names
	 */
	private StringValuePanel tabNamePanel;

	/** a check box to indicate whether to calculate min */
	private JCheckBox minCheckBox;

	/** a check box to indicate whether to calculate max */
	private JCheckBox maxCheckBox;

	/** a check box to indicate whether to calculate sum */
	private JCheckBox sumCheckBox;

	/** a check box to indicate whether to calculate mean */
	private JCheckBox meanCheckBox;

	/** a check box to indicate whether to calculate median */
	private JCheckBox medianCheckBox;

	/** a check box to indicate whether to calculate stdDev */
	private JCheckBox stdDevCheckBox;

	/** a check box to indicate whether to calculate skew */
	private JCheckBox skewCheckBox;

	/** a check box to indicate whether to calculate kurtosis */
	private JCheckBox kurtosisCheckBox;

	/** a check box to indicate whether to do the histogram analysis */
	private JCheckBox histogramCheckBox;

	/** a check box to indicate whether to do the percentile analysis */
	private JCheckBox percentileCheckBox;

	/** a check box to indicate whether to do the correlation analysis */
	private JCheckBox correlationCheckBox;

	/** a check box to indicate whether to do the regression analysis */
	private JCheckBox regressionCheckBox;

	/**
	 * variable to denote whether user hits OK or CANCEL JOptionPane.OK_OPTION JOptionPane.CANCEL_OPTION;
	 */
	private int result = JOptionPane.CANCEL_OPTION;

	/** a data model for this gui */
	private StatisticsModel statisticsModel;

	/** index for the percentile tab */
	private final int PERCENTILE_TAB = 1;

	/** index for the histogram tab */
	private final int HISTOGRAM_TAB = 2;

	/** index for correlation tab */
	private final int CORRELATION_TAB = 3;

	/** index for Regression tab */
	private final int REGRESSION_TAB = 4;

	/** a data sets created for the selected columns */
	private DataSetsAdapter dataSets;

	/** a histogram data sets */
	private DataSetsAdapter histogramDataSets;

	/** a percentile data sets */
	private DataSetsAdapter percentileDataSets;

	/** to indicate whether column is selected */
	private boolean[] showColumns;

	/** a boolean to check wheter gui can be close or not */
	private boolean closeDialog = true;

	/** default tabname prefix to be used for statistics tabs */
	private String tabName;

	private JFrame parent;

	/**
	 * Creates a new instance of StatisticsGUI with the existing statistisModel
	 * 
	 * @pre statModel != null
	 */
	public StatisticsGUI(JFrame parent, StatisticsModel statModel, String tabName) {
		super(parent);
		this.parent = parent;
		this.tabName = tabName;
		statisticsModel = statModel;
		statisticsModel.setBasicStatsTabName(tabName + "_stats");
		this.setTitle("Statistics");

		initialize();
		initGUIModel();
		pack();
		setLocation(ScreenUtils.getPointToCenter(this));
		setModal(true);
		setVisible(true);
	}

	/** initilize the gui */
	private void initialize() {
		JPanel basicStatsPanel = createBasicStatsPanel();
		createInsPanel(); // FIXME: remove??

		JPanel basicStatInsPanel = new JPanel();
		basicStatInsPanel.setLayout(new BoxLayout(basicStatInsPanel, BoxLayout.X_AXIS));
		basicStatInsPanel.add(basicStatsPanel);

		JPanel dataPanel = createDataPanel();
		JPanel analysisPanel = createAnalysisPanel();

		JPanel firstPanel = new JPanel();
		firstPanel.setLayout(new BoxLayout(firstPanel, BoxLayout.Y_AXIS));
		Border insideBorder = BorderFactory.createLoweredBevelBorder();
		Border outsideBorder = BorderFactory.createEmptyBorder(4, 4, 4, 4);
		firstPanel.setBorder(BorderFactory.createCompoundBorder(outsideBorder, BorderFactory.createCompoundBorder(
				insideBorder, outsideBorder)));

		firstPanel.add(Box.createVerticalStrut(10));
		firstPanel.add(analysisPanel);
		firstPanel.add(Box.createVerticalStrut(10));
		firstPanel.add(basicStatInsPanel);
		firstPanel.add(dataPanel);

		JPanel correlationPanel = new JPanel();
		correlationPanel.setLayout(new BoxLayout(correlationPanel, BoxLayout.Y_AXIS));
		// RegressionModel model = new
		// RegressionModel(statisticsModel.getOverallTableModel());
		// model.setTabName(true, model.getTabName(false));
		// correlationGUI = new RegressionGUI(model, true);
		// correlationPanel.add(correlationGUI);

		JPanel regressionPanel = new JPanel();
		regressionPanel.setLayout(new BoxLayout(regressionPanel, BoxLayout.Y_AXIS));
		// regressionGUI = new RegressionGUI(model, false);
		// model.setTabName(false, model.getTabName(false));
		// regressionPanel.add(regressionGUI);

		JPanel percentilePanel = new JPanel();
		percentilePanel.setLayout(new BoxLayout(percentilePanel, BoxLayout.Y_AXIS));
		percentileGUI = new PercentileGUI();
		percentilePanel.add(percentileGUI);

		JPanel histogramPanel = new JPanel();
		histogramPanel.setLayout(new BoxLayout(histogramPanel, BoxLayout.Y_AXIS));

		tabbedPane = new JTabbedPaneWithCloseIcons();
		tabbedPane.insertTab("Data & Analyses Selection", null, firstPanel, null, 0);
		tabbedPane.insertTab("Percentiles", null, percentilePanel, null, PERCENTILE_TAB);
		tabbedPane.insertTab("Histogram", null, histogramPanel, null, HISTOGRAM_TAB);
		tabbedPane.insertTab("Correlation", null, correlationPanel, null, CORRELATION_TAB);
		tabbedPane.insertTab("Regression", null, regressionPanel, null, REGRESSION_TAB);

		Container container = this.getContentPane();
		container.setLayout(new BorderLayout());
		container.add(tabbedPane, BorderLayout.CENTER);
		container.add(createOKCancelPanel(), BorderLayout.SOUTH);
	}

	/**
	 * helper method to create OK CANCEL button pane;
	 * 
	 * @return JPanel
	 */
	private JPanel createOKCancelPanel() {
		JButton okButton = new JButton("Ok");
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveToModel();
				if (closeDialog) {
					result = JOptionPane.OK_OPTION;
					dispose();
				}
			}
		});

		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				result = JOptionPane.CANCEL_OPTION;
				dispose();
			}
		});

		JPanel buttonPanel = new JPanel();
		buttonPanel.add(okButton);
		buttonPanel.add(cancelButton);
		return buttonPanel;

	}// createOKCancelPanel()

	/**
	 * * create a panel for selecting basic stats analysis
	 */
	private JPanel createBasicStatsPanel() {
		tabNamePanel = new StringValuePanel("Tab Name", false);
		tabNamePanel.setValue(tabName + "_stats");
		minCheckBox = new JCheckBox("Minimum");
		maxCheckBox = new JCheckBox("Maximum");
		sumCheckBox = new JCheckBox("Sum");
		meanCheckBox = new JCheckBox("Mean");
		medianCheckBox = new JCheckBox("Median");
		stdDevCheckBox = new JCheckBox("Std Deviation");
		skewCheckBox = new JCheckBox("Skew");
		kurtosisCheckBox = new JCheckBox("Kurtosis");
		JPanel statsPanel1 = new JPanel();
		statsPanel1.setLayout(new GridLayout(2, 4));
		statsPanel1.add(minCheckBox);
		statsPanel1.add(maxCheckBox);
		statsPanel1.add(sumCheckBox);
		statsPanel1.add(meanCheckBox);
		statsPanel1.add(medianCheckBox);
		statsPanel1.add(stdDevCheckBox);
		statsPanel1.add(skewCheckBox);
		statsPanel1.add(kurtosisCheckBox);

		JPanel tempStatsPanel = new JPanel();
		tempStatsPanel.setLayout(new BoxLayout(tempStatsPanel, BoxLayout.X_AXIS));
		tempStatsPanel.add(Box.createHorizontalStrut(25));
		tempStatsPanel.add(statsPanel1);

		JPanel statsPanel = new JPanel();
		statsPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black),
				"Basic Statistics"));
		statsPanel.setLayout(new BorderLayout());
		statsPanel.add(tabNamePanel, BorderLayout.NORTH);
		statsPanel.add(tempStatsPanel, BorderLayout.CENTER);
		return statsPanel;
	}// createBasicStatsPanel

	/**
	 * * create a panel to insert the data
	 */
	private JPanel createDataPanel() {

		dataMessageField = new JTextField(10);
		dataMessageField.setBackground(Color.white);
		dataMessageField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
		dataMessageField.setEditable(false);
		JPanel dataMessagePanel = new JPanel();
		dataMessagePanel.setLayout(new BoxLayout(dataMessagePanel, BoxLayout.Y_AXIS));
		dataMessagePanel.add(Box.createVerticalStrut(40));
		dataMessagePanel.add(dataMessageField);
		dataMessagePanel.add(Box.createVerticalStrut(40));

		dataMessageLabel = new JLabel("[0 cols]");
		JButton dataButton = new JButton("Select");
		dataButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showColumnSelectionGUI();
			}
		});

		JPanel dataColumnPanel = new JPanel();
		dataColumnPanel.setLayout(new BoxLayout(dataColumnPanel, BoxLayout.X_AXIS));
		dataColumnPanel.add(Box.createHorizontalStrut(10));
		dataColumnPanel.add(dataMessagePanel);
		dataColumnPanel.add(Box.createHorizontalStrut(5));
		dataColumnPanel.add(dataMessageLabel);
		dataColumnPanel.add(Box.createHorizontalStrut(5));
		dataColumnPanel.add(dataButton);
		dataColumnPanel.add(Box.createHorizontalStrut(10));

		JPanel dataPanel = new JPanel();
		dataPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black),
				"Columns To Analyze"));
		dataPanel.setLayout(new BorderLayout());
		dataPanel.add(dataColumnPanel);
		return dataPanel;

	}// createDataPanel()

	private void showColumnSelectionGUI() {
		OverallTableModel overallModel = statisticsModel.getOverallTableModel();
		SelectColumnsGUI filterGUI = null;
		FilterCriteria filterCriteria = overallModel.getFilterCriteria();
		int numCols = overallModel.getColumnCount();
		if (showColumns == null) {
			showColumns = new boolean[--numCols]; // deducting one for the first column
		}
		String[] filterColNames = new String[showColumns.length];
		for (int i = 0; i < filterColNames.length; i++) {
			filterColNames[i] = overallModel.getColumnName(i + 1);
		}// for(i)
		filterCriteria = new FilterCriteria(filterColNames, /* null, */showColumns);
		filterCriteria.setTableModel(overallModel);
		filterGUI = new SelectColumnsGUI(parent, filterCriteria, "Include/Exclude Columns", "Include", "Exclude");
		filterGUI.setLocationRelativeTo(this);
		filterGUI.setVisible(true);

		// Apply the filter if the user pressed OK.
		if (filterGUI.getResult() != OptionDialog.OK_RESULT) {
			return;
		}

		showColumns = filterGUI.getSelectedColumns(); // update the showColumns
		Class[] columnClasses = overallModel.getBaseColumnClasses();
		String[] names = overallModel.getBaseColumnNames();
		boolean atLeastOneColumnSelected = false;
		// check whether selected columns are double or integer type else throw error message
		// also checks atleast one column is selected
		for (int i = 0; i < showColumns.length; i++) {
			if (showColumns[i] == true
					&& !(columnClasses[i].equals(Integer.class) || columnClasses[i].equals(Double.class))) {
				DefaultUserInteractor.get().notify(
						this,
						"Error",
						"The class type of selected columns " + "should be either an Integer or a Double."
								+ "\nThe column " + names[i] + " is type " + columnClasses[i], UserInteractor.ERROR);
				return;
			}// if(showColumns[i] == true)
			else if (showColumns[i] == true) {
				atLeastOneColumnSelected = true;
			}
		}// for(i)
		statisticsModel.setShowColumns(showColumns);
		dataMessageLabel.setText("[" + statisticsModel.getNumOfDataColumns() + " cols]");
		dataMessageField.setText(statisticsModel.getDataColumnSelection());
		if (!atLeastOneColumnSelected) {
			DefaultUserInteractor.get().notify(this, "Warning",
					"You should select atleast " + "one integer or double column.", UserInteractor.WARNING);
			dataSets = null;
			histogramGUI = null;
			histogramCheckBox.setSelected(false);
			histogramCheckBox.setEnabled(false);
			tabbedPane.setEnabledAt(HISTOGRAM_TAB, false);
			return;
		}
		createDataSets();
		createHistogramGUI();
	}// showColumnSelectioGUI()

	/**
	 * * helper method to create a data sets
	 */
	private void createDataSets() {
		try {
			this.dataSets = statisticsModel.createDataSets();
			histogramCheckBox.setEnabled(true);
		}// try
		catch (Exception ex) {
			DefaultUserInteractor.get().notify(this, "Error", ex.getMessage(), UserInteractor.ERROR);
		}// catch
	}// createDataSets

	/**
	 * helper method to create analysis panel
	 */
	private JPanel createAnalysisPanel() {
		basicStatsCheckBox = new JCheckBox("Basic Statistics", true);
		basicStatsCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				enableBasicStatsOptions();
			}
		});

		wekaCheckBox = new JCheckBox("Analysis using Weka", false);
		wekaCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				statisticsModel.setWekaAnalysis(true);
			}
		});

		percentileCheckBox = new JCheckBox("Percentiles", false);
		percentileCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (percentileCheckBox.isSelected()) {
					tabbedPane.setEnabledAt(PERCENTILE_TAB, true);
				} else {
					tabbedPane.setEnabledAt(PERCENTILE_TAB, false);
				}
			}
		});
		percentileCheckBox.setHorizontalAlignment(SwingConstants.LEFT);

		histogramCheckBox = new JCheckBox("Histogram", false);
		histogramCheckBox.setHorizontalAlignment(SwingConstants.LEFT);
		histogramCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (histogramCheckBox.isSelected()) // dataSets != null this will be
				// ensured by enabling the histogram check box only after the
				// dataSet is created
				{
					if (histogramGUI == null) {
						createHistogramGUI();
					}// if(histogramGUI == null)
					tabbedPane.setEnabledAt(HISTOGRAM_TAB, true);
				}// if(histogramCheckBox.isSelected())
				else {
					tabbedPane.setEnabledAt(HISTOGRAM_TAB, false);
				}// else
			}// actionPerformed()
		});

		correlationCheckBox = new JCheckBox("Correlation", false);
		correlationCheckBox.setHorizontalAlignment(SwingConstants.LEFT);
		correlationCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (correlationCheckBox.isSelected()) {
					if (correlationGUI == null)
						createCorrelationGUI();
					tabbedPane.setEnabledAt(CORRELATION_TAB, true);
				} else
					tabbedPane.setEnabledAt(CORRELATION_TAB, false);
			}
		});

		regressionCheckBox = new JCheckBox("Regression", false);
		regressionCheckBox.setHorizontalAlignment(SwingConstants.LEFT);
		regressionCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (regressionCheckBox.isSelected()) {
					if (regressionGUI == null)
						createRegressionGUI();
					tabbedPane.setEnabledAt(REGRESSION_TAB, true);
				} else
					tabbedPane.setEnabledAt(REGRESSION_TAB, false);
			}
		});

		JPanel analysisPanel = new JPanel();
		analysisPanel.setLayout(new GridLayout(2, 3));
		analysisPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black),
				"Analyses to Perform"));
		// analysisPanel.add(Box.createHorizontalGlue());
		analysisPanel.add(basicStatsCheckBox);
		// analysisPanel.add(Box.createHorizontalStrut(10));
		analysisPanel.add(percentileCheckBox);
		// analysisPanel.add(Box.createHorizontalStrut(10));
		analysisPanel.add(histogramCheckBox);
		// analysisPanel.add(Box.createHorizontalStrut(10));
		analysisPanel.add(correlationCheckBox);
		// analysisPanel.add(Box.createHorizontalStrut(10));
		analysisPanel.add(regressionCheckBox);
		// analysisPanel.add(Box.createHorizontalStrut(10));
		analysisPanel.add(wekaCheckBox);
		// analysisPanel.add(Box.createHorizontalGlue());

		return analysisPanel;

	}// createAnalysisPanel()

	private void enableBasicStatsOptions() {
		boolean enable = basicStatsCheckBox.isSelected();
		minCheckBox.setEnabled(enable);
		maxCheckBox.setEnabled(enable);
		sumCheckBox.setEnabled(enable);
		medianCheckBox.setEnabled(enable);
		meanCheckBox.setEnabled(enable);
		stdDevCheckBox.setEnabled(enable);
		skewCheckBox.setEnabled(enable);
		kurtosisCheckBox.setEnabled(enable);
	}// enableBasicStatsOptions

	/**
	 * a helper method to create a histogram gui
	 * 
	 * @pre dataSets != null
	 * @pre tabbedPane != null
	 */
	private void createHistogramGUI() {
		histogramGUI = new HistogramGUI(dataSets);
		JPanel histogramPanel = (JPanel) tabbedPane.getComponentAt(HISTOGRAM_TAB);
		histogramPanel.removeAll();
		histogramPanel.add(histogramGUI);
	}

	private void createCorrelationGUI() {
		if (statisticsModel.getRegressionModel() == null) {
			statisticsModel.setRegressionModel(new RegressionModel(statisticsModel.getOverallTableModel()));
			statisticsModel.getRegressionModel().setTabName(true, tabName + "_CC");
			statisticsModel.getRegressionModel().setTabName(false, tabName + "_LR");
		}
		correlationGUI = new RegressionGUI(statisticsModel.getRegressionModel(), true);
		JPanel correlationPanel = (JPanel) tabbedPane.getComponentAt(CORRELATION_TAB);
		correlationPanel.removeAll();
		correlationPanel.add(correlationGUI);
	}

	private void createRegressionGUI() {
		if (statisticsModel.getRegressionModel() == null) {
			statisticsModel.setRegressionModel(new RegressionModel(statisticsModel.getOverallTableModel()));
			statisticsModel.getRegressionModel().setTabName(false, tabName + "_LR");
			statisticsModel.getRegressionModel().setTabName(true, tabName + "_CC");
		}
		regressionGUI = new RegressionGUI(statisticsModel.getRegressionModel(), false);
		JPanel regressionPanel = (JPanel) tabbedPane.getComponentAt(REGRESSION_TAB);
		regressionPanel.removeAll();
		regressionPanel.add(regressionGUI);
	}

	private JPanel createInsPanel() {
		JTextArea insTextArea = new JTextArea();
		insTextArea.setFont(new Font("Arial", Font.BOLD, 12));
		insTextArea.setLineWrap(true);
		insTextArea.setWrapStyleWord(true);
		insTextArea.setEditable(false);
		String direction = "Directions:\n";
		String step1 = "Step1: Select the data you want to choose by clicking "
				+ "the \"Select\" button. Please note that the data selected should be " + "an integer or a double.\n";
		String step2 = "Step2: Choose the analysis to perform by checking the check boxes.\n";
		String step3 = "Step3: Click on the analysis specific tabs to change default settings for the selected "
				+ "analysis.";
		String fullText = direction + "\n" + step1 + "\n" + step2 + "\n" + step3;
		insTextArea.setText(fullText);
		// insTextArea.setSize(100,50);
		JScrollPane scrollPane = new JScrollPane(insTextArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		// scrollPane.setSize(10, 10);
		JPanel insPanel = new JPanel(new BorderLayout());
		insPanel.setBorder(BorderFactory.createLineBorder(Color.white));
		insPanel.add(scrollPane, BorderLayout.CENTER);
		return insPanel;
	}

	/**
	 * intitialize the gui with the statistics model
	 * 
	 */
	private void initGUIModel() {
		basicStatsCheckBox.setSelected(statisticsModel.isBasicStatsAnalysis());
		enableBasicStatsOptions();
		tabNamePanel.setValue(statisticsModel.getBasicStatsTabName());

		// initialization of basic stats panel
		HashMap selAnalList = statisticsModel.getSelBasicStats();
		boolean value = ((Boolean) selAnalList.get(SummaryStats.MIN)).booleanValue();
		minCheckBox.setSelected(value);
		value = ((Boolean) selAnalList.get(SummaryStats.MAX)).booleanValue();
		maxCheckBox.setSelected(value);
		value = ((Boolean) selAnalList.get(SummaryStats.SUM)).booleanValue();
		sumCheckBox.setSelected(value);
		value = ((Boolean) selAnalList.get(SummaryStats.MEAN)).booleanValue();
		meanCheckBox.setSelected(value);
		value = ((Boolean) selAnalList.get(SummaryStats.MEDIAN)).booleanValue();
		medianCheckBox.setSelected(value);
		value = ((Boolean) selAnalList.get(SummaryStats.STD_DEVIATION)).booleanValue();
		stdDevCheckBox.setSelected(value);
		value = ((Boolean) selAnalList.get(SummaryStats.SKEW)).booleanValue();
		skewCheckBox.setSelected(value);
		value = ((Boolean) selAnalList.get(SummaryStats.KURTOSIS)).booleanValue();
		kurtosisCheckBox.setSelected(value);
		// initialization related data panel

		String[] selCols = statisticsModel.getSelectedColNames();
		int newNoOfSelCols = -1;
		if (selCols != null) {
			OverallTableModel overallModel = statisticsModel.getOverallTableModel();

			int numCol = overallModel.getColumnCount();
			showColumns = new boolean[--numCol];// deduting one for the first column
			for (int i = 0; i < selCols.length; i++) {
				int index = overallModel.getColumnNameIndex(selCols[i]);
				if (index != -1) {
					showColumns[index] = true;
				}// if
			}// for(i)
			statisticsModel.setShowColumns(showColumns);
			newNoOfSelCols = statisticsModel.getNumOfDataColumns();
			dataMessageLabel.setText("[" + newNoOfSelCols + " cols]");
			dataMessageField.setText(statisticsModel.getDataColumnSelection());
		}// if

		boolean pAnalysis = statisticsModel.isPercentileAnalysis();
		boolean hAnalysis = statisticsModel.isHistogramAnalysis();
		boolean rAnalysis = statisticsModel.isRegressionAnalysis();
		boolean wAnalysis = statisticsModel.isWekaAnalysis();
		boolean cAnalysis = statisticsModel.isCorrelationAnalysis();

		percentileCheckBox.setSelected(pAnalysis);
		histogramCheckBox.setSelected(hAnalysis);
		regressionCheckBox.setSelected(rAnalysis);
		correlationCheckBox.setSelected(cAnalysis);
		wekaCheckBox.setSelected(wAnalysis);

		tabbedPane.setEnabledAt(HISTOGRAM_TAB, hAnalysis);
		tabbedPane.setEnabledAt(PERCENTILE_TAB, pAnalysis);
		tabbedPane.setEnabledAt(REGRESSION_TAB, rAnalysis);
		tabbedPane.setEnabledAt(CORRELATION_TAB, cAnalysis);

		if (newNoOfSelCols > 0) {
			createDataSets(); // have to do it here since HistogramGUI need new DataSets
			createHistogramGUI();//
			HistogramModel hModel = statisticsModel.getHistogramModel();
			if (hModel != null) {
				histogramGUI.initGUIModel(hModel);
			}
		}// if(newNoOfSelCols >0)
		else {
			histogramCheckBox.setEnabled(false);
			tabbedPane.setEnabledAt(HISTOGRAM_TAB, false);
		}

		PercentileModel pModel = statisticsModel.getPercentileModel();
		if (pModel != null) {
			percentileGUI.initGUIModel(pModel);
		}

		RegressionModel rModel = statisticsModel.getRegressionModel();
		if (rModel != null) {
			// to get a fresh set of column names from the OverallTableModel
			rModel.clearColumnNames();
			if (rAnalysis) {
				if (regressionGUI == null)
					createRegressionGUI();
				else
					regressionGUI.initGUIModel(rModel);
			}
			if (cAnalysis) {
				if (correlationGUI == null)
					createCorrelationGUI();
				else
					correlationGUI.initGUIModel(rModel);
			}
		}

	}// initGUIModel()

	/**
	 * to save the data to the model
	 */
	private void saveToModel() {
		closeDialog = true;
		// data columns selected will immediately save to the model
		// so need to save here again
		if (statisticsModel.getNumOfDataColumns() == 0) {
			DefaultUserInteractor.get().notify(this, "Error",
					"No data is " + " selected for the analysis. Please select the data or click " + " Cancel button.",
					UserInteractor.ERROR);
			closeDialog = false;
			return;
		}
		String basicStatsTabName = tabNamePanel.getValue();
		if (basicStatsTabName == null) {
			DefaultUserInteractor.get().notify(this, "Error",
					"Please enter a tab name " + "for the basic statstics analysis", UserInteractor.ERROR);
			closeDialog = false;
			return;
		}
		statisticsModel.setBasicStatsTabName(tabNamePanel.getValue());
		boolean basicAnalysis = basicStatsCheckBox.isSelected();
		boolean hAnalysis = histogramCheckBox.isSelected();
		boolean pAnalysis = percentileCheckBox.isSelected();
		boolean cAnalysis = correlationCheckBox.isSelected();
		boolean rAnalysis = regressionCheckBox.isSelected();
		statisticsModel.setBasicStatsAnalysis(basicAnalysis);
		statisticsModel.setHistogramAnalysis(hAnalysis);
		statisticsModel.setPercentileAnalysis(pAnalysis);
		statisticsModel.setCorrelationAnalysis(cAnalysis);
		statisticsModel.setRegressionAnalysis(rAnalysis);

		HashMap basicAnal = new HashMap();

		boolean selected = minCheckBox.isSelected();
		basicAnal.put(SummaryStats.MIN, new Boolean(selected));
		selected = maxCheckBox.isSelected();
		basicAnal.put(SummaryStats.MAX, new Boolean(selected));
		selected = sumCheckBox.isSelected();
		basicAnal.put(SummaryStats.SUM, new Boolean(selected));
		selected = meanCheckBox.isSelected();
		basicAnal.put(SummaryStats.MEAN, new Boolean(selected));
		selected = medianCheckBox.isSelected();
		basicAnal.put(SummaryStats.MEDIAN, new Boolean(selected));
		selected = stdDevCheckBox.isSelected();
		basicAnal.put(SummaryStats.STD_DEVIATION, new Boolean(selected));
		selected = skewCheckBox.isSelected();
		basicAnal.put(SummaryStats.SKEW, new Boolean(selected));
		selected = kurtosisCheckBox.isSelected();
		basicAnal.put(SummaryStats.KURTOSIS, new Boolean(selected));
		statisticsModel.setSelBasicStats(basicAnal);

		HistogramModel hModel = null;
		PercentileModel pModel = null;
		RegressionModel rModel = null;
		if (histogramGUI != null) {
			hModel = histogramGUI.saveToModel();
			if (hModel == null && hAnalysis) {
				closeDialog = false;
				return;
			}// if(hModel == null)
		}// if(histogramGUI != null)

		if (percentileGUI != null) {
			pModel = percentileGUI.saveToModel();
			if (pModel == null && pAnalysis) {
				closeDialog = false;
				return;
			}// if(pModel == null)
		}// if(percentileGUI != null)

		if (correlationGUI != null && cAnalysis) {
			rModel = correlationGUI.saveToModel();
			if (rModel == null && cAnalysis) {
				closeDialog = false;
				return;
			}
		}

		if (regressionGUI != null && rAnalysis) {
			rModel = regressionGUI.saveToModel();
			if (rModel == null && rAnalysis) {
				closeDialog = false;
				return;
			}
		}
		statisticsModel.setHistogramModel(hModel);
		statisticsModel.setPercentileModel(pModel);
		statisticsModel.setRegressionModel(rModel);

		if (dataSets != null && hAnalysis) {
			DecimalFormat format = histogramGUI.getFormat();
			double[] bins = histogramGUI.getBins(); // bins!= null: already
			// checked when saving histogramGUI
			byte histogramType = histogramGUI.getHistogramType();
			histogramDataSets = new DataSets();
			LabeledDataSetIfc dataSetIfc = null;
			TableDataSeries dataSeries = null;
			Vector data = dataSets.getDataSets(null, null);
			for (int j = 0; j < data.size(); j++) {
				dataSeries = (TableDataSeries) data.get(j);
				try {
					dataSetIfc = Histogram.generate(dataSeries, bins, format, histogramType);
				} catch (Exception e) {
					DefaultUserInteractor.get().notify(this, "Error", e.getMessage(), UserInteractor.ERROR);
					closeDialog = false;
					return;
				}
				histogramDataSets.add(dataSetIfc, dataSetIfc.getName());
			}// for(i)
		}// if(

		if (dataSets != null && pAnalysis) {
			double[] percentiles = percentileGUI.getValues();
			percentileDataSets = new DataSets();
			LabeledDataSetIfc dataSetIfc = null;
			TableDataSeries dataSeries = null;
			Vector data = dataSets.getDataSets(null, null);
			for (int j = 0; j < data.size(); j++) {
				dataSeries = (TableDataSeries) data.get(j);
				try {
					dataSetIfc = Percentile.generate(dataSeries, percentiles, true);
				} catch (Exception e) {
					DefaultUserInteractor.get().notify(this, "Error", e.getMessage(), UserInteractor.ERROR);
					closeDialog = false;
					return;
				}
				percentileDataSets.add(dataSetIfc, dataSetIfc.getName());
			}// for(i)
		}// if(

	}// saveToModel()

	/**
	 * return JOptionPane.OK_OPTION or JOptionPane.CANCEL_OPTION depends on whether user clicks on OK or CANCEL Button
	 */
	public int getResult() {
		return result;
	}// getResult

	/**
	 * getter for the histogram data sets
	 * 
	 * @return DataSetsAdapter
	 */
	public DataSetsAdapter getHistogramDataSets() {
		return histogramDataSets;
	}

	/**
	 * getter for the percentile data sets
	 * 
	 * @return DataSetsAdapter
	 */
	public DataSetsAdapter getPercentileDataSets() {
		return percentileDataSets;
	}

	/**
	 * getter for the histogram name
	 * 
	 * @return String histogram name
	 */
	public String getHistogramName() {
		if (histogramGUI == null) {
			return null;
		}
		return histogramGUI.getTabName();
	}

	/**
	 * getter for the percentile name
	 * 
	 * @return String percentile name
	 */
	public String getPercentileName() {
		if (percentileGUI == null) {
			return null;
		}
		return percentileGUI.getTabName();
	}

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) {
		String[][] colHeader = { { "A", "B", "C" } };
		Class[] colType = { Double.class, Double.class, Double.class };
		int count = 3;
		ArrayList data = new ArrayList();
		for (int i = 0; i < 10; i++) {
			ArrayList rowData = new ArrayList();
			for (int j = 0; j < count; j++) {
				rowData.add(new Double(j));
			}
			data.add(rowData);
		}// for(i)
		SpecialTableModel specialModel = new SpecialTableModel(null, colHeader, data, colType);
		OverallTableModel tableModel = new OverallTableModel(specialModel);
		StatisticsModel model = new StatisticsModel(tableModel);
		model.setShowColumns(tableModel.getDoubleColumnTypes());
		JFrame frame = new JFrame();
		new StatisticsGUI(frame, model, "Test"); // just see the layout
		// won't be functional as statisticsModel =null
	}

}
