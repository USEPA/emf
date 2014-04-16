package gov.epa.mims.analysisengine.table.plot;

import gov.epa.mims.analysisengine.gui.DefaultUserInteractor;
import gov.epa.mims.analysisengine.gui.OptionDialog;
import gov.epa.mims.analysisengine.gui.ScreenUtils;
import gov.epa.mims.analysisengine.gui.StringChooserPanel;
import gov.epa.mims.analysisengine.gui.StringValuePanel;
import gov.epa.mims.analysisengine.gui.TreeDialog;
import gov.epa.mims.analysisengine.gui.UserInteractor;
import gov.epa.mims.analysisengine.table.OverallTableModel;
import gov.epa.mims.analysisengine.table.SelectColumnsGUI;
import gov.epa.mims.analysisengine.table.SpecialTableModel;
import gov.epa.mims.analysisengine.table.filter.FilterCriteria;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

/**
 * This Dialog comes up when the user clicks the "Plot" button
 * 
 * @author Prashant Pai, CEP UNC
 * @version $Id: PlottingInfoView.java,v 1.3 2007/06/11 03:36:48 eyth Exp $
 */

public class PlottingInfoView extends OptionDialog {

	/** the plotting info object for this GUI * */
	protected PlottingInfo plottingInfo = null;

	/** the string chooser panel for the plot type * */
	protected StringChooserPanel plotTypePanel = null;

	/** the string combo box for the units * */
	protected JComboBox unitsHeaderComboBox = null;

	/** user specified unit */
	protected JTextField unitsUserTextField = null;

	/** radio button for to get the units from the table header */
	protected JRadioButton headerRB;

	/** radio button for to get the units as user specified */
	protected JRadioButton userSpecRB;

	/** the panel for the separator * */
	protected StringValuePanel separatorPanel = null;

	/** the editable string to give the plot a name * */
	protected JTextField plotNameField = null;

	/** label that displays the number of columns selected * */
	protected JLabel dataMessageLabel = null;

	/** textfield that displays the columns selected * */
	protected JTextField dataMessageField = null;

	/** to indicate whether columns are selected for data */
	private boolean[] showDataColumns;

	/** */
	private StringChooserPanel firstLabelPanel;

	private StringChooserPanel secondLabelPanel;

	private StringChooserPanel thirdLabelPanel;

	private JPanel labelPanel = null;

	private StringChooserPanel datePlusTimePanel;

	private StringChooserPanel timePanel;

	private StringChooserPanel formatPanel;

	private JPanel labelOrDatePanel;

	private JPanel datePanel = null;

	/** whether to show the header panel * */
	protected boolean showHeaders = true;

	protected JPanel plotNamePanel = null;

	protected JPanel typeNamePanel = null;

	private final String LABEL_BORDER = "Columns for Labels";

	private final String TIME_LABEL_BORDER = "Date and Time Columns";

	private String labelBorderTitle = LABEL_BORDER;

	private TitledBorder labelTitleBorder = BorderFactory.createTitledBorder(labelBorderTitle);

	public static final String[] timeFormats = { "HH:mm:ss", "HH:mm:ss zzz", "HH:mm", "HH", "hh a" };

	private final Border labelBorder = BorderFactory.createCompoundBorder(labelTitleBorder, BorderFactory
			.createEmptyBorder(5, 5, 5, 5));

	private JFrame parent;

	public PlottingInfoView(JFrame parent, PlottingInfo plottingInfo, boolean showPlotSelection) {
		super(parent);
		this.parent = parent;
		this.plottingInfo = plottingInfo;
		setTitle("Select Plotting Options");
		setModal(true);
		initialize(showPlotSelection);
		initGUIFromModel();
		pack();
		setLocation(ScreenUtils.getPointToCenter(this));
	}

	private void initialize(boolean showPlotSelection) {
		plotTypePanel = new StringChooserPanel("Plot Type: ", false, PlotTypeConverter.AVAILABLE_PLOT_TYPES);
		ActionListener listener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String plotType = plotTypePanel.getValue();
				addRemovePanel(plotType);
				enableLabelColums();
			}
		};

		plotTypePanel.addActionListener(listener);
		// if (showPlotSelection)

		FlowLayout plotNamePanelLayout = new FlowLayout();
		plotNamePanel = new JPanel();
		plotNamePanel.setLayout(plotNamePanelLayout);
		plotNamePanelLayout.setAlignment(FlowLayout.LEFT);
		plotNamePanelLayout.setHgap(5);
		plotNamePanelLayout.setVgap(5);
		JLabel lplotName = new JLabel("Plot Name:");
		lplotName.setVisible(true);
		plotNamePanel.add(lplotName);
		plotNameField = new JTextField(10);
		plotNameField.setBackground(Color.white);
		plotNameField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
		plotNameField.setEditable(true);
		plotNameField.setVisible(true);
		plotNamePanel.add(plotNameField);
		typeNamePanel = new JPanel();
		if (showPlotSelection) {
			typeNamePanel.add(plotTypePanel);
		}

		typeNamePanel.add(plotNamePanel);
		dataMessageField = new JTextField(10);
		dataMessageField.setBackground(Color.white);
		dataMessageField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
		dataMessageField.setEditable(false);
		dataMessageLabel = new JLabel("[0 cols]");
		JPanel dsPanel = new JPanel();
		dsPanel.add(dataMessageLabel);
		dsPanel.add(new JButton(new SelectAction(this)));
		headerRB = new JRadioButton("Header", false);
		headerRB.setToolTipText("The unit for each datasets is from the column " + "header of each column");
		userSpecRB = new JRadioButton("User", true);
		userSpecRB.setToolTipText("The unit for each datasets is user specified");
		ButtonGroup bg = new ButtonGroup();
		bg.add(headerRB);
		bg.add(userSpecRB);
		ActionListener rbListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				boolean enable = false;
				if (headerRB.isSelected()) {
					enable = true;
				}
				unitsHeaderComboBox.setEnabled(enable);
				unitsUserTextField.setEnabled(!enable);
			}
		};
		headerRB.addActionListener(rbListener);
		userSpecRB.addActionListener(rbListener);
		String[] units = plottingInfo.getUnitsChoices();
		unitsHeaderComboBox = new JComboBox();
		unitsHeaderComboBox.setToolTipText("Select the row column header");

		// if there is no column row header or only one row header is available
		// then user has to specify unit if he want to
		if (units == null || units.length == 1) {
			headerRB.setSelected(false);
			userSpecRB.setSelected(true);
			headerRB.setEnabled(false);
			unitsHeaderComboBox.setEnabled(false);
		} else {
			DefaultComboBoxModel model = new DefaultComboBoxModel(units);
			unitsHeaderComboBox.setModel(model);
			unitsHeaderComboBox.setSelectedIndex(units.length - 1);
		}
		unitsHeaderComboBox.setPreferredSize(new Dimension(150, 30));
		unitsUserTextField = new JTextField();
		unitsUserTextField.setToolTipText("Enter the unit for all the data sets");
		unitsUserTextField.setPreferredSize(new Dimension(100, 30));
		unitsUserTextField.setMaximumSize(new Dimension(150, 30));
		JPanel unitsPanel = new JPanel();
		unitsPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Units"),
				BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		unitsPanel.setLayout(new BoxLayout(unitsPanel, BoxLayout.X_AXIS));
		unitsPanel.add(headerRB);
		unitsPanel.add(unitsHeaderComboBox);
		unitsPanel.add(userSpecRB);
		unitsPanel.add(unitsUserTextField);
		JPanel dataColumnPanel = new JPanel();
		dataColumnPanel.setLayout(new BoxLayout(dataColumnPanel, BoxLayout.X_AXIS));
		dataColumnPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Data Columns"),
				BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		dataColumnPanel.add(dataMessageField);
		dataColumnPanel.add(dsPanel);
		createLabelPanel();
		createDateLabelPanel();
		labelOrDatePanel = new JPanel(new BorderLayout());
		labelOrDatePanel.add(labelPanel);
		// labelOrDatePanel.setPreferredSize(new Dimension(300, 500));
		// labelOrDatePanel.setMinimumSize(new Dimension(300, 300));
		labelOrDatePanel.setBorder(labelBorder);

		Container contentPane = this.getContentPane();
		contentPane.setLayout(new BorderLayout());
		// the main panel that contains everything except the buttons at the bottom
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED),
				BorderFactory.createEmptyBorder(10, 10, 10, 10)));
		mainPanel.add(typeNamePanel);
		mainPanel.add(dataColumnPanel);
		mainPanel.add(unitsPanel);
		mainPanel.add(labelOrDatePanel);
		contentPane.add(getButtonPanel(), BorderLayout.SOUTH);
		contentPane.add(mainPanel, BorderLayout.CENTER);
	}// initialize()

	private void createLabelPanel() {
		String[] colNames = plottingInfo.getOverallTableModel().getColumnNames();
		String[] colNamesWithNA = new String[colNames.length + 1];
		colNamesWithNA[0] = PlottingInfo.NOT_SELECTED;
		System.arraycopy(colNames, 0, colNamesWithNA, 1, colNames.length);
		firstLabelPanel = new StringChooserPanel("", false, colNames);
		firstLabelPanel.setToolTipText("Select the first label column");

		secondLabelPanel = new StringChooserPanel("", false, colNamesWithNA);
		secondLabelPanel.setToolTipText("Select the second label column");

		thirdLabelPanel = new StringChooserPanel("", false, colNamesWithNA);
		thirdLabelPanel.setToolTipText("Select the third label column");

		JPanel labelChooserPanel = new JPanel();
		labelChooserPanel.setLayout(new BoxLayout(labelChooserPanel, BoxLayout.X_AXIS));
		labelChooserPanel.add(firstLabelPanel);
		labelChooserPanel.add(secondLabelPanel);
		labelChooserPanel.add(thirdLabelPanel);
		separatorPanel = new StringValuePanel("Separator", false, true);
		separatorPanel.setToolTipText("A single charator for joining the labels" + "\nfrom selected label columns");
		separatorPanel.addSelectAllFocusListener();
		separatorPanel.setPreferredSize(new Dimension(90, 30));
		separatorPanel.setMinimumSize(new Dimension(90, 30));
		separatorPanel.setMaximumSize(new Dimension(90, 30));

		labelPanel = new JPanel();
		labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.X_AXIS));
		labelPanel.add(labelChooserPanel);
		labelPanel.add(separatorPanel);
		labelPanel.setToolTipText("Select one to three columns for the labels "
				+ "\nand enter a charactor for joining the labels from the columns");
	}// createLabelPanel()

	private void createDateLabelPanel() {
		String[] colNames = plottingInfo.getOverallTableModel().getColumnNames();
		datePlusTimePanel = new StringChooserPanel("Date +[Time]", false, colNames);
		datePlusTimePanel.setToolTipText("Select the colum of type date");
		String[] colNamesWithNA = new String[colNames.length + 1];
		colNamesWithNA[0] = PlottingInfo.NOT_SELECTED;
		System.arraycopy(colNames, 0, colNamesWithNA, 1, colNames.length);
		timePanel = new StringChooserPanel("Time", false, colNamesWithNA);
		timePanel.setToolTipText("Select the column for the time");
		formatPanel = new StringChooserPanel("Format", false, timeFormats);
		formatPanel.setToolTipText("Select the format that matches the selected " + "time column format");
		formatPanel.setPreferredSize(new Dimension(150, 30));
		formatPanel.setMinimumSize(new Dimension(150, 30));
		formatPanel.setMaximumSize(new Dimension(175, 30));
		JPanel datePanel1 = new JPanel();
		datePanel1.setLayout(new BoxLayout(datePanel1, BoxLayout.X_AXIS));

		datePanel1.add(datePlusTimePanel);
		datePanel1.add(timePanel);
		datePanel = new JPanel(new BorderLayout());
		datePanel.setToolTipText("Select upto two columns and time format for specifying date labels"
				+ "\n.The first column type should be date.");
		datePanel.add(datePanel1, BorderLayout.CENTER);
		datePanel.add(formatPanel, BorderLayout.EAST);
	}// createDateLablePanel

	private void enableLabelColums() {
		String plotType = plotTypePanel.getValue();// plottingInfo.getPlotType();
		String aePlotType = PlotTypeConverter.getAEPlotType(plotType);
		boolean enable = TreeDialog.isLabelRequired(this, aePlotType);
		firstLabelPanel.setEnabled(enable);
		secondLabelPanel.setEnabled(enable);
		thirdLabelPanel.setEnabled(enable);
		separatorPanel.setEnabled(enable);
	}// enableLabelColums()

	/**
	 * 
	 * save the values from the GUI to the PLottingInfo object
	 * 
	 */

	protected void saveGUIValuesToModel() {
		OverallTableModel model = plottingInfo.getOverallTableModel();
		if (model.getRowCount() <= 0) {
			DefaultUserInteractor.get().notify(this, "Error", "No rows are selected for" + " analyses",
					UserInteractor.ERROR);
			return;
		}
		String plotType = plotTypePanel.getValue();
		plottingInfo.setPlotType(plotType);
		plottingInfo.setPlotName(plotNameField.getText());
		if (plottingInfo.getNumOfDataColumns() == 0) {
			DefaultUserInteractor.get().notify(this, "Error", "Please select at least one data column",
					UserInteractor.ERROR);
			shouldContinueClosing = false;
			return;
		}
		if (plotType.equals(PlotTypeConverter.TIMESERIES_PLOT)) {
			// check whether only one column is selected
			String firstDateColName = datePlusTimePanel.getValue();
			int index = model.getColumnNameIndex(firstDateColName);
			if (index != -1) {
				Format format = model.getFormat(firstDateColName);
				Class classType = model.getColumnClass(index + 1); // add 1 to skip the first col
				if (!classType.equals(Date.class) || !(format instanceof SimpleDateFormat)) {
					DefaultUserInteractor.get().notify(this, "Error",
							"The Date + [Time] " + " column should be of type date.", UserInteractor.ERROR);
					shouldContinueClosing = false;
					return;
				}
				String pattern = ((SimpleDateFormat) format).toPattern();
				String[] newDateColumns;
				// select first row data fron each col and apply the pattern to see
				// whether you can convert it to a date
				int index1 = model.getColumnNameIndex(firstDateColName);
				String newDateString = model.getFormattedValueAt(0, index1 + 1);
				String timeFormat = "";
				String secondDateColName = timePanel.getValue();
				if (!secondDateColName.equals(PlottingInfo.NOT_SELECTED)) {
					int index2 = model.getColumnNameIndex(secondDateColName);
					String s2 = model.getFormattedValueAt(0, index2 + 1);
					newDateString = s2 + ' ' + newDateString;
					timeFormat = formatPanel.getValue();
					pattern = timeFormat + ' ' + pattern;
				}

				plottingInfo.setTimeFormat(timeFormat);
				try {
					SimpleDateFormat d = new SimpleDateFormat(pattern);
					d.parse(newDateString);
					newDateColumns = new String[2];
					newDateColumns[0] = firstDateColName;
					newDateColumns[1] = secondDateColName;
					plottingInfo.setSelLabelDateColNames(newDateColumns);
				} catch (ParseException e) {
					DefaultUserInteractor.get().notify(
							this,
							"Error",
							"Cannot combine two columns selected to "
									+ "form a date.\nPlease check the format in the table for the "
									+ "first column and selected time format in this GUI for the second" + " column",
							UserInteractor.ERROR);
					shouldContinueClosing = false;
					return;
				}// catch()
			}// if(index != -1)
		}// if(plotType.equals(PlotTypeConverter.TIMESERIES_PLOT))
		else {
			String firstLabel = firstLabelPanel.getValue();
			String secondLabel = secondLabelPanel.getValue();
			String thirdLabel = thirdLabelPanel.getValue();
			if (secondLabel.equals(PlottingInfo.NOT_SELECTED) && !thirdLabel.equals(PlottingInfo.NOT_SELECTED)) {
				DefaultUserInteractor.get().notify(this, "Error",
						"Please select the " + "second column label before selecting the third column label",
						UserInteractor.ERROR);
				shouldContinueClosing = false;
				return;
			}// if
			else if (firstLabel.equals(secondLabel) || firstLabel.equals(thirdLabel)
					|| (secondLabel.equals(thirdLabel) && !secondLabel.equals(PlottingInfo.NOT_SELECTED))) {
				DefaultUserInteractor.get().notify(this, "Error", "Please select different " + "columns for labels",
						UserInteractor.ERROR);
				shouldContinueClosing = false;
				return;
			}// else if
			else {
				String[] selColNames = new String[3];
				selColNames[0] = firstLabel;
				selColNames[1] = secondLabel;
				selColNames[2] = thirdLabel;
				plottingInfo.setSelLabelColumnNames(selColNames);
			}// else
			String stringSepar = separatorPanel.getValue();
			char charSepar;
			if (stringSepar == null) {
				charSepar = ' ';
			} else {
				charSepar = stringSepar.charAt(0);
			}
			plottingInfo.setSeparator(charSepar);
		}// else

		if (headerRB.isSelected()) {
			int colRowHeaderindex = unitsHeaderComboBox.getSelectedIndex();
			plottingInfo.setUserSpecifiedUnits(false);
			plottingInfo.setColRowHeaderIndex(colRowHeaderindex);
		} else {
			plottingInfo.setUserSpecifiedUnits(true);
			plottingInfo.setUnits(unitsUserTextField.getText());
		}// else
	}// saveGUIValuesToModel()

	/**
	 * 
	 * initialize the GUI with values from the PlottingInfo object
	 * 
	 */

	protected void initGUIFromModel() {
		String plotType = plottingInfo.getPlotType();
		plotTypePanel.setValue(plotType);
		plotNameField.setText(plottingInfo.getPlotName());
		int colRowHeaderindex = plottingInfo.getColRowHeaderIndex();
		if (headerRB.isEnabled() && colRowHeaderindex != -1) {
			unitsHeaderComboBox.setSelectedIndex(colRowHeaderindex);
		}
		unitsUserTextField.setText(plottingInfo.getUnits());
		boolean userSpec = plottingInfo.isUserSpecifiedUnits();
		headerRB.setSelected(!userSpec);
		userSpecRB.setSelected(userSpec);
		boolean enable = false;
		if (headerRB.isSelected()) {
			enable = true;
		}
		unitsHeaderComboBox.setEnabled(enable);
		unitsUserTextField.setEnabled(!enable);
		separatorPanel.setValue(plottingInfo.getSeparator());
		OverallTableModel overallModel = plottingInfo.getOverallTableModel();
		int numCol = overallModel.getColumnCount();
		// checking & initializing data columns
		String[] selDataColNames = plottingInfo.getSelDataColumnNames();
		int newNoOfDataSelCols = -1;
		if ((selDataColNames != null) && (selDataColNames.length != 0)) {
			showDataColumns = new boolean[numCol - 1]; // deduting one for the first column
			for (int i = 0; i < selDataColNames.length; i++) {
				int index = overallModel.getColumnNameIndex(selDataColNames[i]);
				if (index != -1) {
					showDataColumns[index] = true;
				}
				// if
			}// for(i)
			setSelectedDataColumns(showDataColumns);
			newNoOfDataSelCols = plottingInfo.getNumOfDataColumns();
			dataMessageLabel.setText("[" + newNoOfDataSelCols + " cols]");
			dataMessageField.setText(plottingInfo.getDataColumnSelection());
		}// if ((selDataColNames != null) && (selDataColNames.length != 0))
		String[] selLabelColNames = plottingInfo.getSelLabelColumnNames();
		String[] allColNames = overallModel.getColumnNames();
		if (selLabelColNames[0].equals(PlottingInfo.NOT_SELECTED) && allColNames.length >= 1) {
			String firstColName = allColNames[0];
			firstLabelPanel.setValue(firstColName);
		} else {
			firstLabelPanel.setValue(selLabelColNames[0]);
		}
		secondLabelPanel.setValue(selLabelColNames[1]);
		thirdLabelPanel.setValue(selLabelColNames[2]);

		String[] selLabelDateColNames = plottingInfo.getSelLabelDateColNames();
		String oneDateColName = selLabelDateColNames[0];
		if (oneDateColName.equals(PlottingInfo.NOT_SELECTED)) {
			int[] dateCol = overallModel.getFirstDateColTypes();
			if (dateCol != null && dateCol.length >= 1) {
				oneDateColName = overallModel.getColumnName(dateCol[0]);
			}// if()
		}// if

		datePlusTimePanel.setValue(oneDateColName);
		timePanel.setValue(selLabelDateColNames[1]);
		formatPanel.setValue(plottingInfo.getTimeFormat());
		addRemovePanel(plotType);
		enableLabelColums();
	}// initGUIFromModel()

	private void setSelectedDataColumns(boolean[] showDataColumns) {
		ArrayList selColumns = new ArrayList();
		OverallTableModel overallModel = plottingInfo.getOverallTableModel();
		for (int i = 0; i < showDataColumns.length; i++) {
			if (showDataColumns[i]) {
				selColumns.add(overallModel.getColumnName(i + 1));
			}
		}// for
		String[] selDataCol = new String[selColumns.size()];
		selColumns.toArray(selDataCol);
		plottingInfo.setSelDataColumns(selDataCol);
	}

	private void addRemovePanel(String plotType) {
		if (plotType.equals(PlotTypeConverter.TIMESERIES_PLOT)) {
			labelTitleBorder.setTitle(TIME_LABEL_BORDER);
			labelOrDatePanel.remove(0);
			labelOrDatePanel.add(datePanel);
			labelOrDatePanel.validate();
		} else {
			labelTitleBorder.setTitle(LABEL_BORDER);
			labelOrDatePanel.remove(0);
			labelOrDatePanel.add(labelPanel);
			labelOrDatePanel.validate();
		}
		labelOrDatePanel.repaint();
	}

	public PlottingInfo getPlottingInfo() {
		return plottingInfo;
	}

	public void setPlotName(String name) {
		plotNameField.setText(name);
	}

	/**
	 * Show the include/exclude columns GUI. Set the selected columns based on the user's selections once the dialog
	 * closes.
	 */
	public void showFilterColumnsGUI() {
		SelectColumnsGUI filterGUI = null;
		OverallTableModel overallModel = plottingInfo.getModel();
		FilterCriteria filterCriteria = overallModel.getFilterCriteria();
		boolean[] showColumns = null;
		String title = "";
		title = "Select Data Columns";
		showColumns = showDataColumns;
		int numCols = overallModel.getColumnCount();
		if (showColumns == null) {
			showColumns = new boolean[--numCols]; // deducting one for the first column
		}
		String[] filterColNames = new String[showColumns.length];// deducting one for the first column
		for (int i = 0; i < filterColNames.length; i++) {
			filterColNames[i] = overallModel.getColumnName(i + 1);// adding one to avoid the first col
		}// for(i)
		filterCriteria = new FilterCriteria(filterColNames, showColumns);
		filterGUI = new SelectColumnsGUI(this.parent, filterCriteria, title, "Include", "Exclude");
		// Show the filter column GUI.
		filterGUI.setLocationRelativeTo(this);
		filterGUI.setVisible(true);
		// Apply the filter if the user pressed OK.

		if (filterGUI.getResult() == OptionDialog.OK_RESULT) {
			boolean[] selCols = filterGUI.getSelectedColumns();
			setSelectedDataColumns(selCols);
			dataMessageLabel.setText("[" + plottingInfo.getNumOfDataColumns() + " cols]");
			dataMessageField.setText(plottingInfo.getDataColumnSelection());
		}

	}// showFilterColumnsGUI

	public static void main(String[] args) {
		String[][] colHeader = { { "A", "B", "C" }, { "a", "b", "c" } };
		String[] rowHeader = { "row1", "Units" };
		Class[] colType = { Double.class, Double.class, Double.class };
		int count = 3;
		ArrayList data = new ArrayList();
		for (int i = 0; i < 10; i++) {
			ArrayList rowData = new ArrayList();
			for (int j = 0; j < count; j++) {
				rowData.add(new Double(j));
			}
			data.add(data);
		}// for(i)

		SpecialTableModel specialModel = new SpecialTableModel(rowHeader, colHeader, data, colType);
		OverallTableModel tableModel = new OverallTableModel(specialModel);
		PlottingInfo plotInfo = new PlottingInfo(tableModel);
		PlottingInfoView gui = new PlottingInfoView(null, plotInfo, true);
		gui.setVisible(true);
		System.exit(0);
	}// main()

	class SelectAction extends AbstractAction {
		PlottingInfoView plottingInfoView = null;

		public SelectAction(PlottingInfoView plottingInfoView) {
			super("Select");
			this.plottingInfoView = plottingInfoView;
		}

		public void actionPerformed(ActionEvent e) {
			plottingInfoView.showFilterColumnsGUI();
		}
	}
}
