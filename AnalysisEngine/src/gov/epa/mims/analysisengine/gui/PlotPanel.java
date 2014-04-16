package gov.epa.mims.analysisengine.gui;

import gov.epa.mims.analysisengine.tree.AnalysisOptions;
import gov.epa.mims.analysisengine.tree.DataSetsAdapter;
import gov.epa.mims.analysisengine.tree.Plot;
import gov.epa.mims.analysisengine.tree.PlotInfo;
import java.awt.*;
import java.util.Arrays;
import java.util.Vector;

import javax.swing.*;
import javax.swing.border.TitledBorder;

/**
 * A Panel for setting up a plot. This will display the plot type and contains a table for editing page options.
 * 
 * @author Alison Eyth
 * @version $Id: PlotPanel.java,v 1.4 2007/05/31 14:29:32 qunhe Exp $
 * 
 */

public class PlotPanel extends JPanel {
	/** the panel to display / edit the plot's options */
	OptionsPanel plotOptionsPanel;

	/** The AnalysisOptions node for the currently active plot */
	private AnalysisOptions plotOptions = null;

	/** The plot this is showing info for */
	private Plot plot = null;

	/** PlotInfo for this plot (includes type, option keywords) */
	private PlotInfo plotInfo = null;

	/** plot type */
	private String plotType = null;

	/** field to show plot name */
	private JTextField plotNameField;

	/** GUI component to set plot type */
	private JComboBox typeComboBox;

	/** label for name field */
	private JLabel plotNameLabel;

	/** panel for plot name field and label */
	private JPanel plotNamePanel;

	/** data set adapter to choose data sets from */
	private DataSetsAdapter dataSetsAdapter;

	private DataSetsTable dataSetsTable;

	private JPanel dataSetsPanel;

	/**
	 * The border around the options panel. We need to update the plot type it the plot type changes. (ie if a new
	 * serialized tree comes in)
	 */
	protected TitledBorder optionsBorder = null;

	/**
	 * True if we need to reset the dataSetsPanel when a new tree is read in froma file. This will occur if the user
	 * starts with a BarPlot and then reads in a ScatterPlot tree.
	 */
	protected boolean resetDataSets = false;

	/**
	 * constructor with parameters for preexisting plot options
	 * 
	 * @param plotOptions
	 *            AnalysisOptions for the plot (could be null)
	 * @param dataSetsAdapter
	 *            DataSetsAdapter to select data from
	 */
	public PlotPanel(AnalysisOptions plotOptions, Plot passedPlot, DataSetsAdapter dataSetsAdapter) {
		this.plotOptions = plotOptions;
		this.dataSetsAdapter = dataSetsAdapter;
		if (passedPlot != null) {
			this.plot = passedPlot;
			plotInfo = TreeDialog.getPlotInfoFor(plot);

			if (plotInfo == null) {
				throw new IllegalArgumentException("No PlotInfo available for plot of type "
						+ passedPlot.getClass().getName());
			}
		} else {
			throw new IllegalArgumentException("Passed Plot " + passedPlot.getClass().getName() + " value is null.");
		}

		plotType = plotInfo.getPlotTypeName();

		// add a new OptionsPanel for the plot options
		plotOptionsPanel = new OptionsPanel(plotOptions, plotInfo, passedPlot.getName());
		initialize();
		plotOptions = plotOptionsPanel.getOptions();
	}// PlotPanel(plotOptions)

	/**
	 * constructor to create a particular type of plot and no preexisting options
	 * 
	 * @param plotType
	 *            String type of plot to create
	 * @param dataSetsAdapter
	 *            DataSetsAdapter to select data from
	 * @exception Exception
	 *                if the plot cannot be created
	 */
	public PlotPanel(String plotType, DataSetsAdapter dataSetsAdapter) {
		if (plotType == null) {
			throw new IllegalArgumentException("No plot type provided to PlotPanel");
		}
		this.plotInfo = TreeDialog.getPlotInfoFor(plotType);

		if (plotInfo == null) {
			throw new IllegalArgumentException("No PlotInfo available for plot type " + plotType);
		}
		this.plotType = plotInfo.getPlotTypeName();
		this.dataSetsAdapter = dataSetsAdapter;
		try {
			plot = TreeDialog.createNewPlot(plotType);
		} catch (Exception exc) {
			exc.printStackTrace();
			throw new IllegalArgumentException(exc.getMessage());
		}
		plotOptions = new AnalysisOptions();
		plotOptionsPanel = new OptionsPanel(plotOptions, plotInfo, plotInfo.getPlotTypeName());
		initialize();
		plotOptions = plotOptionsPanel.getOptions();
	}// PlotPanel(pageOptions, plotOptions)

	/**
	 * method to initialize the dialog
	 */
	private void initialize() {
		this.setLayout(new BorderLayout());
		// eventually use a layout to make this look nice and put the plot
		// type on top,and the plot options panel below.
		if (plotType == null) {
			typeComboBox = new JComboBox(TreeDialog.getAvailablePlotTypes());
			this.add(typeComboBox, BorderLayout.NORTH);
			// specify the actionPerformed method for the combobox. It should set
			// the plot type when the user picks one and create a new plot.

			// TBD: If the data sets adapater is a DummyDataSetsAdapter,
			// when the plot type is chosen, add the dummy data sets
			// for that plot type to the DummyDataSetsAdapter by calling
			// getDummyDataSets for the Plot
		} else {
			JLabel plotTypeLabel = new JLabel(plotType);
			// this.add(plotTypeLabel,BorderLayout.NORTH);
			// setBorder(AxisNumericEditor.getCustomBorder(""));
		}

		optionsBorder = BorderFactory.createTitledBorder(BorderFactory.createLoweredBevelBorder(), plotType
				+ " Options", TitledBorder.LEFT, TitledBorder.TOP);
		plotOptionsPanel.setBorder(optionsBorder);
		add(plotOptionsPanel, BorderLayout.CENTER);
		dataSetsPanel = new JPanel(new BorderLayout());
		dataSetsPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLoweredBevelBorder(), "Data sets",
				TitledBorder.LEFT, TitledBorder.TOP));
		dataSetsTable = new DataSetsTable(dataSetsAdapter, plotInfo, plot);
		dataSetsPanel.add(dataSetsTable, BorderLayout.CENTER);
		add(dataSetsPanel, BorderLayout.NORTH);
	}// initialize()

	/**
	 * @return the current plot options
	 */
	protected AnalysisOptions getPlotOptions() {
		plotOptions = plotOptionsPanel.getOptions();
		return plotOptions;
	}

	/**
	 * @return the plot
	 */
	protected Plot getPlot() {
		return plot;
	}

	/**
	 * take any actions needed when the user hits create plots or save
	 * 
	 * @throws Exception
	 */
	protected void storeGUIValues(boolean onScreen) throws Exception {
		try {
			setDataSetsForPlot();
		} catch (Exception exc) {
			if (onScreen) {
				throw exc;
			}
			// otherwise, if the datasets aren't set properly, don't worry about it
			// this may be the case if the user selects "Close" from the TreeDialog
			// without creating the plot
		}
	}

	/**
	 * store the selected data sets in the plot
	 */
	protected void setDataSetsForPlot() throws Exception {
		dataSetsTable.setDataSetsForPlot(plot);
	}

	/**
	 * Set new PlotOptions and update the GUI accordingly.
	 * 
	 * @param newPlotOptions
	 *            AnalysisOptions to use as the new data model.
	 * @param newPlot
	 *            Plot to use as the new data model.
	 * @param newDataSetsAdapter
	 *            DataSetsAdapter to use as the new data model.
	 */
	protected void setDataModel(AnalysisOptions newPlotOptions, Plot newPlot, DataSetsAdapter newDataSetsAdapter) {
		this.plotOptions = newPlotOptions;
		this.dataSetsAdapter = newDataSetsAdapter;
		// Reset the datasets panel if we have a new type of plot.
		resetDataSets = (!plot.getName().equals(newPlot.getName()));
		this.plot = newPlot;
		this.plotInfo = TreeDialog.getPlotInfoFor(newPlot);
		updateGUIFromModel();
	}

	/**
	 * Update the GUI based on the state of the model.
	 */
	protected void updateGUIFromModel() {
		plotOptionsPanel.resetOptions(plotOptions, plotInfo, plot.getName());
		plotType = plotInfo.getPlotTypeName();
		optionsBorder.setTitle(plotType + " Options");
		if (resetDataSets) {
			dataSetsPanel.remove(dataSetsTable);
			dataSetsTable = new DataSetsTable(dataSetsAdapter, plotInfo, plot);
			dataSetsPanel.add(dataSetsTable, BorderLayout.CENTER);
		}
	}

}
