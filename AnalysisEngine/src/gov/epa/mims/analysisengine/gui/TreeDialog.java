package gov.epa.mims.analysisengine.gui;

import gov.epa.mims.analysisengine.AnalysisEngineConstants;
import gov.epa.mims.analysisengine.rcommunicator.RCommunicator;
import gov.epa.mims.analysisengine.rcommunicator.RGenerator;
import gov.epa.mims.analysisengine.table.persist.Data;
import gov.epa.mims.analysisengine.tree.AnalysisOption;
import gov.epa.mims.analysisengine.tree.AnalysisOptionConstantsIfc;
import gov.epa.mims.analysisengine.tree.AnalysisOptions;
import gov.epa.mims.analysisengine.tree.BarPlot;
import gov.epa.mims.analysisengine.tree.BoxPlot;
import gov.epa.mims.analysisengine.tree.Branch;
import gov.epa.mims.analysisengine.tree.CdfPlot;
import gov.epa.mims.analysisengine.tree.DataSetIfc;
import gov.epa.mims.analysisengine.tree.DataSets;
import gov.epa.mims.analysisengine.tree.DataSetsAdapter;
import gov.epa.mims.analysisengine.tree.DiscreteCategoryPlot;
import gov.epa.mims.analysisengine.tree.HistogramPlot;
import gov.epa.mims.analysisengine.tree.Node;
import gov.epa.mims.analysisengine.tree.Page;
import gov.epa.mims.analysisengine.tree.PageConstantsIfc;
import gov.epa.mims.analysisengine.tree.PageType;
import gov.epa.mims.analysisengine.tree.Plot;
import gov.epa.mims.analysisengine.tree.PlotInfo;
import gov.epa.mims.analysisengine.tree.RankOrderPlot;
import gov.epa.mims.analysisengine.tree.ScatterPlot;
import gov.epa.mims.analysisengine.tree.TextIfc;
import gov.epa.mims.analysisengine.tree.TimeSeries;
import gov.epa.mims.analysisengine.tree.TornadoPlot;
import gov.epa.mims.analysisengine.tree.TreeSummary;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * The main GUI for a MIMS analysis engine tree. Eventually this will allow the user to access multiple pages and plots.
 * Currently, this is a modal dialog that is restricted to trees of the following form:
 * 
 * data sets <- page options <- page <- plot options <- plot
 * 
 * It will contain buttons: - Select Data, which will bring up the DataSetsAdapter, - Save, which will return a tree
 * containing the info in the dialog - Create Plots, which will take in the information from the dialog and construct
 * and execute the newly created tree. - Cancel, which will either return the original tree if one was provided or null
 * if none were provided
 * 
 * The GUI will also contain a PagePanel for editing page and plot options
 * 
 * @author Alison Eyth
 * @version $Id: TreeDialog.java,v 1.8 2007/05/31 14:29:33 qunhe Exp $
 * 
 * @see gov.epa.mims.analysisengine.tree.Node
 * @see gov.epa.mims.analysisengine.tree.DataSets
 * @see gov.epa.mims.analysisengine.tree.AnalysisOptions
 * @see gov.epa.mims.analysisengine.tree.Page
 */

public class TreeDialog extends JDialog implements AnalysisOptionConstantsIfc, PageConstantsIfc {
	public static final String SOFTWARE_DATE = "May 31, 2007";

	/** the tree being edited by the dialog * */
	protected Branch tree = null;

	/** the original tree that was passed to this dialog -- could be null * */
	protected Branch originalTree = null;

	/** an optional datasetsadapter -- could be null * */
	protected DataSetsAdapter dataSetsAdapter = null;

	/** cancel button */
	private JButton cancelButton = new JButton("Cancel");

	/** save button */
	// AME: changed Save to Exit for some clarity when accessed from DAVE
	private JButton closeButton = new JButton("Close");

	/** plot button */
	private JButton createPlotsButton = new JButton("View Plot");

	/** The file dialog to use or loading and saving file. */
	private static final JFileChooser fileChooser = new JFileChooser();

	/** The data sets in the tree */
	private DataSets dataSets;

	/** The currently active page */
	private Page currentPage;

	/** The AnalysisOptions node for the currently active page */
	private AnalysisOptions pageOptions;

	/** panel with info about and controls for a page */
	private PagePanel pagePanel;

	/** The AnalysisOptions node for the currently active plot */
	private AnalysisOptions plotOptions;

	/** The currently active plot */
	private Plot currentPlot;

	/** The type of plot to create (could be empty) */
	private String plotType = "";

	/** The RGenerator to use for this dialog */
	private RGenerator rgenerator;

	/** An array of the available plot types */
	private static String[] plotTypes;

	/** a HashMap of Plot type strings to plotInfo objects */
	private static final HashMap plotInfoMap = new HashMap(10);

	/** A counter for how many times Create Plots has been pressed */
	private static int plotCounter = 0;

	/** The data sets selected for the plot */
	DataSetIfc[] selectedDataSets = null;

	/** Initial text values to use e.g. for Axis labels */
	private HashMap textValues = null;

	/**
	 * look up info to retreive the dependent variable
	 */
	private static HashMap dependentAxisInfo;

	/**
	 * look up info for whether plots require labels or not
	 */
	private static HashMap plotLabelInfo;

	static {
		// TBD: could use AnalysisEngineConstants.classes
		/*
		 * for (int i = 0; i = AnalysisEngineConstants.classes.length; i++) {
		 * addPlotInfo(AnalysisEngineConstants.classes[i].get }
		 */

		// We need this line to run the static initialization code in the OptionInfo class
		// to set the default values for the PlotInfos (whose super class,
		// AvaiableOptionsandDefaults holds the defaults in a HashMap)
		// DMG
		OptionInfo oi = new OptionInfo(null, null, null, null, null);

		addPlotInfo(BarPlot.getPlotInfo());
		addPlotInfo(DiscreteCategoryPlot.getPlotInfo());
		addPlotInfo(HistogramPlot.getPlotInfo());
		addPlotInfo(RankOrderPlot.getPlotInfo());
		addPlotInfo(ScatterPlot.getPlotInfo());
		addPlotInfo(TimeSeries.getPlotInfo());
		addPlotInfo(BoxPlot.getPlotInfo());
		addPlotInfo(CdfPlot.getPlotInfo());
		addPlotInfo(TornadoPlot.getPlotInfo());
		String userDir = System.getProperty("user.dir");
		if (userDir != null)
			fileChooser.setCurrentDirectory(new File(userDir));
		fileChooser.setMultiSelectionEnabled(false);

		dependentAxisInfo = new HashMap();
		dependentAxisInfo.put(AnalysisEngineConstants.BAR_PLOT, NUMERIC_AXIS);
		dependentAxisInfo.put(AnalysisEngineConstants.SCATTER_PLOT, Y_NUMERIC_AXIS);
		dependentAxisInfo.put(AnalysisEngineConstants.DISCRETE_CATEGORY_PLOT, Y_NUMERIC_AXIS);
		dependentAxisInfo.put(AnalysisEngineConstants.HISTOGRAM_PLOT, X_NUMERIC_AXIS);
		dependentAxisInfo.put(AnalysisEngineConstants.RANK_ORDER_PLOT, Y_NUMERIC_AXIS);
		dependentAxisInfo.put(AnalysisEngineConstants.TIME_SERIES_PLOT, Y_NUMERIC_AXIS);
		dependentAxisInfo.put(AnalysisEngineConstants.BOX_PLOT, NUMERIC_AXIS);
		dependentAxisInfo.put(AnalysisEngineConstants.CDF_PLOT, Y_NUMERIC_AXIS);
		dependentAxisInfo.put(AnalysisEngineConstants.TORNADO_PLOT, NUMERIC_AXIS);

		Boolean trueValue = new Boolean(true);
		Boolean falseValue = new Boolean(false);
		plotLabelInfo = new HashMap();
		plotLabelInfo.put(AnalysisEngineConstants.BAR_PLOT, trueValue);
		plotLabelInfo.put(AnalysisEngineConstants.SCATTER_PLOT, falseValue);
		plotLabelInfo.put(AnalysisEngineConstants.DISCRETE_CATEGORY_PLOT, trueValue);
		plotLabelInfo.put(AnalysisEngineConstants.HISTOGRAM_PLOT, falseValue);
		plotLabelInfo.put(AnalysisEngineConstants.RANK_ORDER_PLOT, falseValue);
		plotLabelInfo.put(AnalysisEngineConstants.TIME_SERIES_PLOT, trueValue);
		plotLabelInfo.put(AnalysisEngineConstants.BOX_PLOT, falseValue);
		plotLabelInfo.put(AnalysisEngineConstants.CDF_PLOT, falseValue);
		plotLabelInfo.put(AnalysisEngineConstants.TORNADO_PLOT, trueValue);
	} // static

	/**
	 * @return int value of plot counter
	 */
	public static int getPlotCounter() {
		return plotCounter;
	}

	/**
	 * To use as a modal dialog and an existing tree, open it with this method
	 * 
	 * @param tree
	 *            Branch the optional tree which the dialog will work off of
	 * @param dataSetsAdapter
	 *            DataSetsAdapter the optional provision to bring in datasets -- either actual or dummy
	 * @param rgenerator
	 *            RGenerator to use to execute trees (could be null)
	 * @param textValues
	 *            HashMap initial text values to use for plot; keywords should be standard analysis option keywords;
	 *            current implementation supports single page and plot tree only
	 * @return Branch the result of the dialog - null if user pressed Cancel, a new tree if the user pressed Save
	 * @throws Exception
	 */
	public static Branch showTreeDialog(JFrame parent, Branch tree, DataSetsAdapter dataSetsAdapter,
			RGenerator rgenerator, HashMap textValues) throws Exception {
		TreeDialog dialog = new TreeDialog(parent, tree, dataSetsAdapter, rgenerator, textValues);
		// TBD: reconcile using DataSets node and data sets adapter to choose
		// data sets
		dialog.setVisible(true);
		Branch retVal = dialog.getResultTree();
		dialog.dispose();
		return retVal;
	}

	/*
	 * To get a Data back with updated Plot infos 5/22/07
	 */
	public static Data showTreeDialog(JFrame parent, Data data, DataSetsAdapter dataSetsAdapter, RGenerator rgenerator,
			HashMap textValues) throws Exception {
		TreeDialog dialog = new TreeDialog(parent, data, dataSetsAdapter, rgenerator, textValues);
		// TBD: reconcile using DataSets node and data sets adapter to choose
		// data sets
		dialog.setVisible(true);
		dialog.dispose();
		data.tree = dialog.getResultTree();

		return data;
	}

	/**
	 * Private constructor for use when creating plots without the GUI.
	 */
	private TreeDialog() {
		/* Nothing */
	}

	/*
	 * To use as a modal dialog with a specific a plot type and datasetsadapter In this case, no tree is passed in, so
	 * we're building a new tree.
	 * 
	 * @param String plotType the type of plot that could be created - see PlotInfo.getAvailablePlotTypes for valid plot
	 * types @param DataSetsAdapter the optional provision to bring in datasets -- either actual or null @param
	 * textValues HashMap initial text values to use for plot; keywords should be standard analysis option keywords;
	 * current implementation supports single page and plot tree only @param rgenerator RGenerator to use to execute
	 * trees (could be null)
	 */
	public static Branch showTreeDialog(JFrame parent, String plotType, DataSetsAdapter dataSetsAdapter,
			RGenerator rgenerator, HashMap textValues) throws Exception {
		TreeDialog dialog = new TreeDialog(parent, plotType, dataSetsAdapter, rgenerator, textValues, true);
		dialog.setVisible(true);
		Branch retVal = dialog.getResultTree();

		dialog.dispose();
		return retVal;
	}

	/**
	 * constructor with options for supplying a tree and datasetsadapter
	 * 
	 * @param tree
	 *            Branch the optional tree which the dialog will work off of
	 * @param dataSetsAdapter
	 *            DataSetsAdapter the optional provision to bring in datasets -- either actual or dummy
	 * @param rgenerator
	 *            RGenerator to use to execute trees (could be null)
	 * @param textValues
	 *            HashMap initial text values to use for plot; keywords should be standard analysis option keywords;
	 *            current implementation supports single page and plot tree only
	 * @throws Exception
	 */
	public TreeDialog(JFrame parent, Branch tree, DataSetsAdapter dataSetsAdapter, RGenerator rgenerator,
			HashMap textValues) throws Exception {
		super(parent);
		setDataValues(tree, dataSetsAdapter, rgenerator, textValues);
		pagePanel = new PagePanel(pageOptions, plotOptions, currentPlot, dataSetsAdapter, this);
		initialize();
		pageOptions = pagePanel.getPageOptions();
		plotOptions = pagePanel.getPlotOptions();
		constructTree();
		setInitialPlotText(plotOptions);
		setInitialPlotText(pageOptions);
		pagePanel.setDataModel(pageOptions, plotOptions, currentPlot, dataSetsAdapter);
	} // TreeDialog(tree, DataSetsAdapter)

	public TreeDialog(JFrame parent, Data dat, DataSetsAdapter dataSetsAdapter, RGenerator rgenerator,
			HashMap textValues) throws Exception {
		super(parent);
		
		setDataValues(dat.tree, dataSetsAdapter, rgenerator, textValues);
		pagePanel = new PagePanel(pageOptions, plotOptions, currentPlot, dataSetsAdapter, this);
		initialize();
		pageOptions = pagePanel.getPageOptions();
		plotOptions = pagePanel.getPlotOptions();
		constructTree();
		setInitialPlotText(plotOptions);
		setInitialPlotText(pageOptions);
		pagePanel.setDataModel(pageOptions, plotOptions, currentPlot, dataSetsAdapter);
	} // TreeDialog(tree, DataSetsAdapter)

	/**
	 * constructor with options for supplying a plot type and datasetsadapter In this case, no tree is passed in, so
	 * we're building a new tree.
	 * 
	 * @param plotType
	 *            String the type of plot that could be created - see PlotInfo.getAvailablePlotTypes for valid plot
	 *            types
	 * @param dataSetsAdapter
	 *            DataSetsAdapter the optional provision to bring in datasets -- either actual or null
	 * @param rgenerator
	 *            RGenerator to use to execute trees (could be null)
	 * @param textValues
	 *            HashMap initial text values to use for plot; keywords should be standard analysis option keywords;
	 *            current implementation supports single page and plot tree only
	 * @param plotDefaults
	 *            TODO
	 * 
	 * @throws Exception
	 */
	public TreeDialog(JFrame parent, String plotType, DataSetsAdapter dataSetsAdapter, RGenerator rgenerator,
			HashMap textValues, boolean plotDefaults) throws Exception {
		super(parent);
		this.dataSetsAdapter = dataSetsAdapter;
		this.originalTree = null;
		this.plotType = plotType;
		this.textValues = textValues;
		PlotInfo plotInfo = TreeDialog.getPlotInfoFor(plotType);

		if (plotDefaults)
			OptionInfo.setPlotTypeDefaults(this.plotType);

		if (plotInfo == null) {
			throw new IllegalArgumentException(plotType + " is not an available plot type");
		}
		this.rgenerator = rgenerator;
		this.dataSetsAdapter = dataSetsAdapter;
		// use the data sets node as the adapter
		if (dataSetsAdapter == null) {
			this.dataSetsAdapter = this.dataSets;
		}

		// Create page and plot options
		createPageOptions();
		createPlotOptions();

		setInitialPlotText(plotOptions);
		setInitialPlotText(pageOptions);

		try {
			currentPlot = createNewPlot(plotType);
			pagePanel = new PagePanel(pageOptions, plotOptions, currentPlot, dataSetsAdapter, this);

		} catch (Exception e) {
			DefaultUserInteractor.get().notifyOfException(this, "Error Creating Plot", e, UserInteractor.ERROR);
		}
		initialize();
	}// TreeDialog(tree, DataSetsAdapter)

	/**
	 * Create a plot based on the current state of the GUI.
	 * 
	 * @param onScreen
	 *            boolean that is true if the plot should be drawn on screen.
	 */
	public void createPlotFromGUI(boolean onScreen) {
		plotCounter++;
		// Used if the user cancels from the "overwrite file?" dialog.
		boolean shouldDrawPlot = true;

		try {
			// TBD: do something with exception?

			pagePanel.storeGUIValues(onScreen);

			// If we are previewing the plot on screen, then override what the
			// GUI says.
			PageType pt = (PageType) pagePanel.getPageOptions().getOption(PAGE_TYPE);
			if (onScreen) {
				pt.setForm(SCREEN);
			}
			// Otherwise, check to see if we're overwriting a file.
			else {
				String filename = pt.getFilename();
				File prospectiveFile = new File(filename);
				if (prospectiveFile.exists()) {
					int retval = JOptionPane.showConfirmDialog(this, "The file " + prospectiveFile
							+ " already exists. Overwrite?", "Overwrite File?", JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE);
					shouldDrawPlot = (retval == JOptionPane.YES_OPTION);
				}
			} // else

			if (shouldDrawPlot) {
				constructTree();
				executeTree();
			}
		} catch (Exception exc) {
			DefaultUserInteractor.get().notifyOfException(this, "Error Creating Plot", exc, UserInteractor.ERROR);
			exc.printStackTrace(System.err);
		}
	} // createPlot()

	/**
	 * Return the intial text to use for the plot
	 * 
	 * @param option
	 *            String specifying the option to look up
	 * @param plot
	 *            Plot to get text for
	 * @return String the initial text to use for the plot
	 */
	public String getInitialText(String option, AnalysisOptions plotOptions) {
		String returnValue = (String) textValues.get(option);
		if (returnValue == null)
		// the option was not found
		{
			returnValue = "";
		}
		return returnValue;
	}

	/*
	 * Set the text for the specified analysis options node from the default @param options
	 */
	protected void setInitialPlotText(AnalysisOptions options) {
		if (textValues == null)
			return;
		Set keys = textValues.keySet();
		Iterator keyIter = keys.iterator();
		Object currKey = keyIter.next();
		while (currKey != null) {
			AnalysisOption analysisOption = (AnalysisOption) options.getOption(currKey);
			if (analysisOption instanceof TextIfc) {
				TextIfc textOption = (TextIfc) analysisOption;
				String textValue = (String) textValues.get(currKey);
				// this will need to be updated when we have multiple plots per page
				textOption.setTextString(textValue);
			}

			if (keyIter.hasNext()) {
				currKey = keyIter.next();
			} else {
				currKey = null;
			}
		}
	}

	/**
	 * @return the result of the dialog - null if cancelled, or new tree if saved
	 */
	public Branch getResultTree() {
		return tree;
	}

	/**
	 * method to initialize the dialog
	 * 
	 * @throws Exception
	 */
	private void initialize() throws Exception {
		this.setTitle("MIMS Analysis Engine " + plotType);
		setModal(true);

		// Menu creation

		// File Menu
		JMenuItem loadTreeItem = new JMenuItem("Load plot Template");
		loadTreeItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				loadTreeFromFile(getFileFromUser(JFileChooser.OPEN_DIALOG));
			}
		});

		JMenuItem saveTreeItem = new JMenuItem("Save plot Template");
		saveTreeItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveTreeToFile(getFileFromUser(JFileChooser.SAVE_DIALOG));
			}
		});

		JMenuItem closeItem = new JMenuItem("Close");
		closeItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					pagePanel.storeGUIValues(false);
					if (tree == null) {
						tree = constructNewTree();
					}
				} catch (Exception exc) {
					exc.printStackTrace(System.err);
				}
				setVisible(false);
			}
		});

		JMenu fileMenu = new JMenu("File");
		fileMenu.add(loadTreeItem);
		fileMenu.add(saveTreeItem);
		fileMenu.add(closeItem);

		JMenuItem aboutItem = new JMenuItem("About");
		aboutItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showAboutDialog();
			}
		});

		JMenuItem helpItem = new JMenuItem("User Guide");
		helpItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showUserGuide();
			}
		});

		JMenu helpMenu = new JMenu("Help");
		helpMenu.add(helpItem);
		helpMenu.add(aboutItem);

		JMenuBar menuBar = new JMenuBar();
		menuBar.add(fileMenu);
		menuBar.add(Box.createHorizontalGlue());
		menuBar.add(helpMenu);
		setJMenuBar(menuBar);

		if (dataSetsAdapter == null) // TBD: may need to add data sets later
		{
			// dataSetsAdapter = new DummyDataSetsAdapter();
		}
		// actionPerformed for closeButton calls constructTree, disposes the
		// dialog and leaves the tree available via getResultTree without
		// executing it
		closeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				try {
					pagePanel.storeGUIValues(false);
					if (tree == null) {
						tree = constructNewTree();
					}
				} catch (Exception exc) {
					exc.printStackTrace(System.err);
				}
				setVisible(false);
			}
		});

		// actionPerformed for cancelButton sets tree to null and disposes the
		// dialog
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				tree = null;
				dispose();
			}
		});

		// actionPerformed for createPlotsButton calls constructTree, then
		// executeTree
		// This will only place the plot on the screen.
		createPlotsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				// true means draw the plot on the screen.
				createPlotFromGUI(true);
			}
		});

		// later, use an appropriate layout; the page panel (with plot options)
		// will be in the center, and the buttons at the bottom
		this.getContentPane().setLayout(new BorderLayout());
		this.getContentPane().add(pagePanel, BorderLayout.CENTER);
		JPanel buttonPanel = new JPanel();

		buttonPanel.add(createPlotsButton);
		buttonPanel.add(closeButton);
		// AME: don't use cancel button for now - instead cancel using window
		// manager X
		// buttonPanel.add(cancelButton);
		this.getContentPane().add(buttonPanel, BorderLayout.SOUTH);

		if (rgenerator == null) {
			rgenerator = createNewRGenerator();
		}
		this.pack();
		setLocation(20, 20); // easy for others to change the plot settings and view the plot in the same time
	}// initialize()

	/**
	 * Set the data values for this object during construction.
	 * 
	 * @param tree
	 *            Branch that is the root of the tree.
	 * @param dataSetsAdapter
	 *            DataSetsAdapter that contains the datasets that we should use.
	 * @param rgenerator
	 *            RGenerator to use to drive R.
	 * @param textValues
	 *            HashMap that contains text to replace in analysis options.
	 * @throws Exception
	 */
	private void setDataValues(Branch tree, DataSetsAdapter dataSetsAdapter, RGenerator rgenerator, HashMap textValues)
			throws Exception {
		this.tree = tree;
		if (tree == null) {
			pageOptions = null;
			plotOptions = null;
			currentPlot = null;
		} else {
			boolean treeOK = setPageAndPlotOptionsFromTree();
			if (!treeOK) {
				throw new IllegalArgumentException(
						"Tree does not have the form: data sets<-page options<-page<-plot options<-plot");
			}
		}

		if (rgenerator == null) {
			this.rgenerator = createNewRGenerator();
		} else {
			this.rgenerator = rgenerator;
		}
		this.originalTree = tree;
		this.dataSetsAdapter = dataSetsAdapter;
		this.textValues = textValues;
		setInitialPlotText(pageOptions);
		setInitialPlotText(plotOptions);

		if (dataSetsAdapter == null) // use the data sets node as the adapter
		{
			this.dataSetsAdapter = this.dataSets;
		}
	}

	/**
	 * Set the page and plot options from the tree.
	 * 
	 * @return boolean whether the tree has the expected structure
	 */
	private boolean setPageAndPlotOptionsFromTree() {
		// if the tree does not have a linear structure of
		// data sets<-page options<-page<-plot options<-plot
		// return false;

		// dataSets = the dataSets node of tree
		if (!(tree instanceof DataSets)) {
			return false;
		}
		dataSets = (DataSets) tree;
		// pageOptions = the page options node of tree
		if (dataSets.getChildCount() != 1) {
			return false;
		}
		Node node = dataSets.getChild(0);
		if (!(node instanceof AnalysisOptions)) {
			return false;
		}
		pageOptions = (AnalysisOptions) node;

		if (pageOptions.getChildCount() != 1) {
			return false;
		}
		node = pageOptions.getChild(0);
		// page = the page node of tree
		if (!(node instanceof Page)) {
			return false;
		}
		currentPage = (Page) node;
		if (currentPage.getChildCount() != 1) {
			return false;
		}
		node = currentPage.getChild(0);
		if (!(node instanceof AnalysisOptions)) {
			return false;
		}
		plotOptions = (AnalysisOptions) node;
		// plotOptions = the plot options node of tree

		if (plotOptions.getChildCount() != 1) {
			return false;
		}
		node = plotOptions.getChild(0);

		if (!(node instanceof Plot)) {
			return false;
		}
		currentPlot = (Plot) node;
		PlotInfo plotInfo = TreeDialog.getPlotInfoFor(currentPlot);
		if (plotInfo == null) {
			throw new IllegalArgumentException("No PlotInfo available for plot of type "
					+ currentPlot.getClass().getName());
		}
		plotType = plotInfo.getPlotTypeName();

		return true;
	}

	/**
	 * once the plot button was clicked, a tree will constructed/edited based on the data that was set in the dialog
	 */
	private void constructTree() {
		if (tree == null) {
			tree = constructNewTree();
		} else {
			// Replace the DataSets in the current root with the DataSets in the
			// DataSetsAdapter.
			if (tree instanceof DataSets) {
				DataSets ds = (DataSets) tree;
				// If the dataSetsAdapter is the same object as the tree node,
				// then we don't want to do any replacement because we already
				// have the right data sets.
				if (dataSetsAdapter != tree) {
					ds.clearDataSets();

					Vector dataSetKeys = dataSetsAdapter.getDataSetKeys(null, this);
					for (int d = 0; d < dataSetKeys.size(); d++) {
						Object key = dataSetKeys.get(d);
						ds.add((DataSetIfc) dataSetsAdapter.getDataSet(key), key);
					}
				}
			} else {
				DefaultUserInteractor.get().notify(
						this,
						"Unexpectd Object Type",
						"Expected a DataSets in TreeDialog.constructTree() but found a " + tree.getClass().toString()
								+ " instead.", UserInteractor.ERROR);
			}
		}
	}

	/**
	 * build a tree in the case that no tree was initially provided
	 * 
	 * @return Branch the new tree with the default structure
	 */
	private Branch constructNewTree() {
		// construct the entire tree with the appropriate structure based on
		// all the data entered
		dataSets = new DataSets();
		Vector dataSetKeys = dataSetsAdapter.getDataSetKeys(null, this);
		for (int d = 0; d < dataSetKeys.size(); d++) {
			Object key = dataSetKeys.get(d);
			dataSets.add((DataSetIfc) dataSetsAdapter.getDataSet(key), key);
		}
		Branch newTree = dataSets; // set the root of the tree to be the
		// dataSets node

		currentPage = pagePanel.getPage();
		pageOptions = pagePanel.getPageOptions();

		plotOptions = pagePanel.getPlotOptions();
		// System.out.println("Plot Title =
		// "+plotOptions.getOption(gov.epa.mims.analysisengine.tree.AnalysisOptionConstantsIfc.PLOT_TITLE));
		currentPlot = pagePanel.getPlot();

		dataSets.add(pageOptions);
		pageOptions.add(currentPage);
		currentPage.add(plotOptions);
		plotOptions.add(currentPlot);

		return newTree;
	}// constructTree()

	/**
	 * execute the tree in forest to create the plots
	 */
	private void executeTree() {
		// trigger the creation of the plots by executing the tree
		try {
			rgenerator.execute(tree);
		} catch (Exception e) {
			DefaultUserInteractor.get().notifyOfException(this, "Error", e, UserInteractor.ERROR);
			return;
		}

		// We needed to add this in to get the image files to write out properly.
		// Previously, we weren't calling RCommunicator.closePlotWindows() and
		// the files were not written out until the Analysis Engine closed.
		// This meant that if you wrote a plot to a file and wanted to look at it,
		// you couldn't do it without closing the Analysis Engine.
		PageType pt = (PageType) pageOptions.getOption(PAGE_TYPE);
		if (pt != null) {
			// If we are NOT writing out to the screen, then close the plot window.
			if (!pt.getForm().equals(SCREEN)) {
				RCommunicator.getInstance().closePlotWindows(5000);
			}
		}
	}// executeTree()

	/**
	 * creates a new instance of an RGenerator
	 * 
	 * @return RGenerator a new RGenerator
	 * @throws Exception
	 */
	private RGenerator createNewRGenerator() throws Exception {
		// TBD: replace with new method to do this
		String Rexec = System.getProperty("os.name").startsWith("Windows") ? "RTerm.exe" : "R";
		RGenerator rGen = new RGenerator(Rexec);

		try {
			rGen.setLog(new File("myLogFile.txt"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		rGen.setEcho(false);
		return rGen;
	}

	/**
	 * Create a new set of Page Options.
	 */
	protected void createPageOptions() {
		pageOptions = new AnalysisOptions();
		pageOptions.addOption(PAGE_TYPE, new PageType());
	}

	/**
	 * Create a new set of Plot Options
	 */
	protected void createPlotOptions() {
		if (plotType == null) {
			DefaultUserInteractor.get().notify(this, "Plot type null",
					"The plotType cannot be null when calling createPlotOptions()!", UserInteractor.ERROR);
			return;
		}

		// Get the defaults for the current plot type.
		PlotInfo plotInfo = TreeDialog.getPlotInfoFor(plotType);

		if (plotOptions == null) {
			plotOptions = new AnalysisOptions();
		}

		// Set up the keywords and default values for them.
		String[] allKeywords = plotInfo.getAllKeywords();
		AnalysisOption[] defaultValues = null;
		try {
			defaultValues = plotInfo.getDefaultValues(allKeywords);
			for (int i = 0; i < allKeywords.length; i++) {
				Object obj = plotOptions.getOption(allKeywords[i]);
				if (obj == null) {
					plotOptions.addOption(allKeywords[i], defaultValues[i]);
				}
			}
		} catch (CloneNotSupportedException e) {
			DefaultUserInteractor.get().notify(this, "Cloning Error",
					"Cloning is not supported for some of the objects", UserInteractor.ERROR);
		} catch (Exception e) {
			DefaultUserInteractor.get().notify(this, "Error", e.getMessage(), UserInteractor.ERROR);
		}
	} // createPlotOptions()

	/**
	 * Try to load the selected file as a serialized verion of the tree.
	 * 
	 * @param file
	 *            File that should e a serialized tree file.
	 */
	public void loadTreeFromFile(File file) {
		if (file == null)
			return;

		try {
			ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(file));
			tree = (DataSets) inputStream.readObject();
			inputStream.close();

			// Synch up the GUI with the new tree.
			if (!setPageAndPlotOptionsFromTree()) {
				throw new IllegalArgumentException(
						"Tree does not have the form: data sets<-page options<-page<-plot options<-plot");
			}

			pagePanel.setDataModel(pageOptions, plotOptions, currentPlot, dataSetsAdapter);
			setTitle("MIMS Analysis Engine " + plotType);
			pack();
		} // try
		catch (Exception e) {
			DefaultUserInteractor.get().notifyOfException(this, "Error Reading file", e, UserInteractor.ERROR);
		} // catch
	} // loadTreeFromFile()

	public void loadTree(byte[] bytes) {

		if (bytes == null || bytes.length == 0)
			return;

		try {
			ObjectInputStream inputStream = new ObjectInputStream(new ByteArrayInputStream(bytes));
			tree = (DataSets) inputStream.readObject();
			inputStream.close();

			// Synch up the GUI with the new tree.
			if (!setPageAndPlotOptionsFromTree()) {
				throw new IllegalArgumentException(
						"Tree does not have the form: data sets<-page options<-page<-plot options<-plot");
			}

			pagePanel.setDataModel(pageOptions, plotOptions, currentPlot, dataSetsAdapter);
			setTitle("MIMS Analysis Engine " + plotType);
			pack();
		} // try
		catch (Exception e) {
			DefaultUserInteractor.get().notifyOfException(this, "Error Reading the array", e, UserInteractor.ERROR);
		} // catch
	}

	/**
	 * Try to load the selected file as a serialized verion of the tree. NOTE: We are NOT saving the root DataSets node.
	 * We strip the datasets from the root before serialization.
	 * 
	 * @param file
	 *            File to open as a serialized tree.
	 */
	public void saveTreeToFile(File file) {
		if (file == null)
			return;

		// If there is no tree yet, then tell the user and return without
		// doing anything.
		if (tree == null) {
			DefaultUserInteractor.get().notify(
					this,
					"No Plot Template",
					"No plot template has been created yet. Please select some data, "
							+ "create a plot and then save the template.", UserInteractor.ERROR);
			return;
		}

		try {
			// Use this variable to detrmine if we should overwrite an
			// existing file.
			int writeFile = JOptionPane.NO_OPTION;

			while (writeFile == JOptionPane.NO_OPTION) {
				// Check if the file already exists and ask the user if they
				// want to overwrite the file.
				if (file.exists()) {
					writeFile = JOptionPane.showConfirmDialog(this, "The file " + file.getAbsolutePath()
							+ " already exists. Overwrite?", "Overwrite File?", JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE);
				} else {
					writeFile = JOptionPane.YES_OPTION;
				}

				if (writeFile == JOptionPane.YES_OPTION) {
					ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(file));
					outputStream.writeObject(tree);
					outputStream.flush();
					outputStream.close();
				}
			} // while (overwriteFile == JOptionPane.NO_OPTION)
		} // try
		catch (Exception e) {
			DefaultUserInteractor.get().notifyOfException(this, "Error Writing file", e, UserInteractor.ERROR);
			e.printStackTrace(System.err);
		} // catch
	}// saveTreeToFile()

	/**
	 * Save the GUI values to the tree.
	 */
	public boolean saveGUIValuesToTree(boolean onScreen) throws Exception {
		boolean shouldDrawPlot = true;
		pagePanel.storeGUIValues(onScreen);

		// If we are previewing the plot on screen, then override what the
		// GUI says.
		PageType pt = (PageType) pagePanel.getPageOptions().getOption(PAGE_TYPE);
		if (onScreen) {
			pt.setForm(SCREEN);
		}
		// Otherwise, check to see if we're overwriting a file.
		else {
			String filename = pt.getFilename();
			File prospectiveFile = new File(pt.getFilename());
			if (prospectiveFile.exists()) {
				int retval = JOptionPane.showConfirmDialog(this, "The file " + prospectiveFile
						+ " already exists. Overwrite?", "Overwrite File?", JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE);
				shouldDrawPlot = (retval == JOptionPane.YES_OPTION);
			}
		} // else

		return shouldDrawPlot;
	}

	/**
	 * Show a simple about box with information about the version, authors, etc. Set the version and date in the static
	 * variables at the top of the class.
	 */
	public void showAboutDialog() {
		StringBuffer sb = new StringBuffer();
		sb.append("MIMS Analysis Engine GUI\n");
		sb.append("Written by : Carolina Environmental Program\n");
		sb.append("University of North Carolina at Chapel Hill\n");
		sb.append("Date Compiled : ");
		sb.append(SOFTWARE_DATE);
		DefaultUserInteractor.get().notify(this, "About Analysis Engine", sb.toString(), UserInteractor.PLAIN);
	} // showAboutDialog()

	/**
	 * Bring up the user guide in an HTML window.
	 */
	public void showUserGuide() {
		JDialog helpDialog = new JDialog();
		JEditorPane htmlPane = new JEditorPane();
		htmlPane.setContentType("text/html");
		try {
			htmlPane.setPage(getClass().getResource("/gov/epa/mims/analysisengine/docs/index.html"));
			helpDialog.getContentPane().add(htmlPane);
			helpDialog.setVisible(true);
		} catch (IOException ioe) {
			DefaultUserInteractor.get().notifyOfException(this, "Error Loading Help File", ioe, UserInteractor.ERROR);
		}
	}

	// ////////////// STATIC METHODS //////////////////////////////

	/**
	 * Add a new plotInfo object to the global list of plot infos. Each concrete plot class should create a new PlotInfo
	 * object and add it to the catalog by calling this method.
	 * 
	 * @param newPlotInfo
	 *            PlotInfo the new one to add
	 * @reutn boolean whether the add was successful
	 */
	public static boolean addPlotInfo(PlotInfo newPlotInfo) {
		String newPlotType = newPlotInfo.getPlotTypeName();
		if (plotInfoMap.get(newPlotType) != null) {
			// this plot type already existed
			return false;
		}
		plotInfoMap.put(newPlotType, newPlotInfo);
		plotTypes = new String[plotInfoMap.size()];
		Object[] array = plotInfoMap.keySet().toArray();
		for (int i = 0; i < array.length; i++) {
			plotTypes[i] = (String) (array[i]);
		}
		return true;
	}

	/**
	 * @return String [] the names of the available plot types
	 */
	public static String[] getAvailablePlotTypes() {
		return plotTypes;
	}

	/**
	 * Get a filename from the user with either an Open or Save FileChooser.
	 * 
	 * @param fileChooserType
	 *            String that is either JFileChooser.OPEN_DIALOG or JFileChooser.SAVE_DIALOG
	 * @return file File that the user selected. Will be null if the user selected 'Cancel'.
	 */
	public static File getFileFromUser(int fileChooserType) {
		File retval = null;
		switch (fileChooserType) {
		case JFileChooser.OPEN_DIALOG:
			if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
				retval = fileChooser.getSelectedFile();
			}
			break;
		case JFileChooser.SAVE_DIALOG:
			if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
				retval = fileChooser.getSelectedFile();
			}
			break;
		default:
			if (fileChooser.showDialog(null, "Select") == JFileChooser.APPROVE_OPTION) {
				retval = fileChooser.getSelectedFile();
			}
			break;
		}
		return retval;
	}

	/**
	 * Return the plot info for a particular type of plot
	 * 
	 * @param plotType
	 *            String the type of plot - should be a constant in this class
	 * @return String [] the keywords for the requested type of plot, or null if the plot type is unknown
	 */
	public static PlotInfo getPlotInfoFor(String plotType) {
		/*
		 * if (plotType.equalsIgnoreCase(PlotConstantsIfc.BAR_PLOT)) { return
		 * (PlotInfo)plotInfoMap.get(AnalysisEngineConstants.BAR_PLOT); } else if
		 * (plotType.equalsIgnoreCase(PlotConstantsIfc.DISCRETE_CATEGORY_PLOT)) { return
		 * (PlotInfo)plotInfoMap.get(AnalysisEngineConstants.DISCRETE_CATEGORY_PLOT); } else if
		 * (plotType.equalsIgnoreCase(PlotConstantsIfc.HISTOGRAM_PLOT)) { return
		 * (PlotInfo)plotInfoMap.get(AnalysisEngineConstants.HISTOGRAM_PLOT); } else if
		 * (plotType.equalsIgnoreCase(PlotConstantsIfc.RANK_ORDER_PLOT)) { return
		 * (PlotInfo)plotInfoMap.get(AnalysisEngineConstants.RANK_ORDER_PLOT); } else if
		 * (plotType.equalsIgnoreCase(PlotConstantsIfc.SCATTER_PLOT)) { return
		 * (PlotInfo)plotInfoMap.get(AnalysisEngineConstants.SCATTER_PLOT); } else if
		 * (plotType.equalsIgnoreCase(PlotConstantsIfc.TIME_SERIES_PLOT)) { return
		 * (PlotInfo)plotInfoMap.get(AnalysisEngineConstants.TIME_SERIES_PLOT); } else {
		 */
		return (PlotInfo) plotInfoMap.get(plotType);
		// }
	}

	/**
	 * Return the plot info for a particular plot
	 * 
	 * @param plot
	 *            Plot the plot to get info for
	 * @return PlotInfo the PlotInfo structure for plot
	 */
	public static PlotInfo getPlotInfoFor(Plot plot) {
		if (plot instanceof BarPlot)
			return BarPlot.getPlotInfo();
		else if (plot instanceof DiscreteCategoryPlot)
			return DiscreteCategoryPlot.getPlotInfo();
		else if (plot instanceof HistogramPlot)
			return HistogramPlot.getPlotInfo();
		else if (plot instanceof RankOrderPlot)
			return RankOrderPlot.getPlotInfo();
		else if (plot instanceof ScatterPlot)
			return ScatterPlot.getPlotInfo();
		else if (plot instanceof TimeSeries)
			return TimeSeries.getPlotInfo();
		else if (plot instanceof BoxPlot)
			return BoxPlot.getPlotInfo();
		else if (plot instanceof CdfPlot)
			return CdfPlot.getPlotInfo();
		else if (plot instanceof TornadoPlot)
			return TornadoPlot.getPlotInfo();
		// TBD: add to this as more plots are added

		return null;
	}

	/**
	 * Method to be called from outside of the AnalysisEngine to create a plot without bringing up the GUI. The initial
	 * motivation for this is to be able to draw multiple plots and save them without brining up the GUI.
	 * 
	 * @param tree
	 *            Branch the optional tree which the dialog will work off of
	 * @param dataSetsAdapter
	 *            DataSetsAdapter the optional provision to bring in datasets -- either actual or dummy
	 * @param rgenerator
	 *            RGenerator to use to execute trees (could be null)
	 * @param textValues
	 *            HashMap initial text values to use for plot; keywords should be standard analysis option keywords;
	 *            current implementation supports single page and plot tree only
	 * @return Branch the result of the dialog - null if user pressed Cancel, a new tree if the user pressed Save
	 */
	public static Branch createPlotWithoutGUI(Branch tree, DataSetsAdapter dataSetsAdapter, RGenerator rgenerator,
			HashMap textValues) throws Exception {
		TreeDialog dialog = new TreeDialog();
		dialog.setDataValues(tree, dataSetsAdapter, rgenerator, textValues);
		dialog.constructTree();
		dialog.executeTree();

		return dialog.getResultTree();
	} // createPlotWithoutGUI()

	/**
	 * Create a new plot using the given PlotInfo
	 * 
	 * @param plotType
	 *            String type of plot to return a plot for
	 * @return Plot a new plot, or null if one can't be created
	 * @throws Exeption
	 *             if plot cannot be created
	 */
	public static Plot createNewPlot(String plotType) throws Exception {
		if (plotType.equals(AnalysisEngineConstants.BAR_PLOT)) // PlotConstantsIfc.BAR_PLOT))
		{
			BarPlot plot = new BarPlot();
			return plot;
		} else if (plotType.equals(AnalysisEngineConstants.DISCRETE_CATEGORY_PLOT)) // PlotConstantsIfc.DISCRETE_CATEGORY_PLOT))
		{
			DiscreteCategoryPlot plot = new DiscreteCategoryPlot();
			return plot;
		} else if (plotType.equals(AnalysisEngineConstants.RANK_ORDER_PLOT))// PlotConstantsIfc.RANK_ORDER_PLOT))
		{
			RankOrderPlot plot = new RankOrderPlot();
			return plot;
		} else if (plotType.equals(AnalysisEngineConstants.SCATTER_PLOT))// PlotConstantsIfc.SCATTER_PLOT))
		{
			ScatterPlot plot = new ScatterPlot();
			return plot;
		} else if (plotType.equals(AnalysisEngineConstants.TIME_SERIES_PLOT))// PlotConstantsIfc.TIME_SERIES_PLOT))
		{
			TimeSeries plot = new TimeSeries();
			return plot;
		} else if (plotType.equals(AnalysisEngineConstants.HISTOGRAM_PLOT))// PlotConstantsIfc.HISTOGRAM_PLOT))
		{
			HistogramPlot plot = new HistogramPlot();
			return plot;
		} else if (plotType.equals(AnalysisEngineConstants.BOX_PLOT)) {
			BoxPlot plot = new BoxPlot();
			return plot;
		} else if (plotType.equals(AnalysisEngineConstants.CDF_PLOT)) {
			CdfPlot plot = new CdfPlot();
			return plot;
		} else if (plotType.equals(AnalysisEngineConstants.TORNADO_PLOT)) {
			System.out.println("plotType=" + plotType);
			TornadoPlot plot = new TornadoPlot();

			return plot;
		}

		// create a new plot of the appropriate class based on the plotInfo
		// TBD: the below didn't work - may not need it anyway
		/*
		 * PlotInfo plotInfo = getPlotInfoFor(plotType); if (plotInfo != null) { try { Plot plot =
		 * (Plot)plotInfo.getPlotClass().newInstance(); return plot; } catch (Exception exc) { throw new Exception(" A
		 * new plot of type "+plotType+" could not be created"); } }
		 */
		throw new Exception("Plot type " + plotType
				+ " not found in list of available plot types in TreeDialog.createNewPlot");
	}

	/**
	 * Retrieve the key word for the dependent variable
	 * 
	 * @param plotType
	 *            String type of plot to return a plot for
	 * @return String keyword for dependent axis for the plot
	 */
	public static String getDependentAxis(Component parent, String plotType) {
		String axisName = (String) dependentAxisInfo.get(plotType);
		if (axisName == null) {
			DefaultUserInteractor.get().notify(parent, "Error",
					"This " + plotType + " is not added to " + "the dependendAxisInfo hashMap in the TreeDialog",
					UserInteractor.ERROR);
		}
		return axisName;
	}

	/**
	 * Retrieve the whether plot required the labels or not
	 * 
	 * @param plotType
	 *            String type of plot to return a plot for
	 * @return boolean
	 */
	public static boolean isLabelRequired(Component parent, String plotType) {
		Boolean req = (Boolean) plotLabelInfo.get(plotType);
		if (req == null) {
			DefaultUserInteractor.get().notify(parent, "Error",
					"This " + plotType + " is not added to " + "the plotLabelInfo hashMap in the TreeDialog",
					UserInteractor.ERROR);
		}
		return req.booleanValue();
	}

	/**
	 * Given a tree, traverse it and return an object that cn be queried about how many pages and plots th tre contains.
	 * 
	 * @param tree
	 *            Branch that is the root of the tree.
	 * @return PageAndPlotSummary that contains information about how many page and plots the tree contains.
	 */
	public static TreeSummary getTreeSummary(Branch tree) {
		return new TreeSummary(tree);
	}

	/**
	 * A method that accepts a File that is an Analysis Engine serialized tree and returns the tree if reading was
	 * successful.
	 * 
	 * @param file
	 *            File that should be read in.
	 * @returns Branch that is the root of a tree fora plot.
	 * @throws IOException
	 *             if the file reading was unsuccessful.
	 */
	public static Branch readTreeFile(File file) throws java.io.IOException, ClassNotFoundException {
		if (!file.exists())
			throw new IOException("The file '" + file.getAbsolutePath()
					+ "' does not exist.\nPlease check the name and try again.");

		ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(file));
		Branch newTree = (DataSets) inputStream.readObject();
		inputStream.close();

		return newTree;
	}

	public static void main(String[] args) {
		// Set showGUI = false when you want to test the create-plot-without-GUI
		// functionality.
		boolean showGUI = true;
		DefaultUserInteractor.set(new GUIUserInteractor());
		String plotType = "";
		if (args.length > 0) {
			// System.out.println(args[0]);
			plotType = args[0];
		}
		Branch branch = null;
		HashMap textValues = new HashMap(10);
		textValues.put(gov.epa.mims.analysisengine.tree.AnalysisOptionConstantsIfc.PLOT_TITLE, "A specified title");
		textValues.put(gov.epa.mims.analysisengine.tree.AnalysisOptionConstantsIfc.PLOT_SUBTITLE,
				"A specified subtitle");
		textValues.put(gov.epa.mims.analysisengine.tree.AnalysisOptionConstantsIfc.PLOT_FOOTER, "A footer");
		textValues.put(gov.epa.mims.analysisengine.tree.AnalysisOptionConstantsIfc.CATEGORY_AXIS, "Category Axis");
		textValues.put(gov.epa.mims.analysisengine.tree.AnalysisOptionConstantsIfc.NUMERIC_AXIS, "Numeric Axis");
		textValues.put(gov.epa.mims.analysisengine.tree.AnalysisOptionConstantsIfc.X_NUMERIC_AXIS, "X Numeric Axis");
		textValues.put(gov.epa.mims.analysisengine.tree.AnalysisOptionConstantsIfc.Y_NUMERIC_AXIS, "Y Numeric Axis");
		textValues.put(gov.epa.mims.analysisengine.tree.AnalysisOptionConstantsIfc.X_TIME_AXIS, "X Time Axis");

		textValues.put(gov.epa.mims.analysisengine.tree.AnalysisOptionConstantsIfc.PAGE_TYPE, "tmp.png");

		if (showGUI) {
			if (!plotType.equals("")) {
				/** use example data sets only - don't build the whole tree */
				ExampleTree example = new ExampleTree(false);
				DataSets dataSets = example.getTree();
				TreeDialog dialog = null;
				try {
					dialog = new TreeDialog(new JFrame(), plotType, dataSets, null, textValues, true);
				} catch (HeadlessException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
				dialog.show();
				branch = dialog.getResultTree();
			} else {
				/** use full example tree */
				ExampleTree example = new ExampleTree();
				DataSets tree = example.getTree();
				try {
					branch = TreeDialog.showTreeDialog(new JFrame(), tree, null, null, textValues);
				} catch (HeadlessException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} else // (showGUI == false)
		{
			ExampleTree example = new ExampleTree();
			DataSets tree = example.getTree();

			try {
				branch = TreeDialog.createPlotWithoutGUI(tree, null, null, textValues);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (branch == null) {
			System.out.println("returned tree is null");
		} else {
			System.out.println("returned tree is NOT null");
		}
		System.exit(0);
	}

	public Plot getPlot() {
		if (pagePanel != null)
			return pagePanel.getPlot();

		return null;
	}

}
