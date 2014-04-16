package gov.epa.mims.analysisengine.table.stats;

import gov.epa.mims.analysisengine.gui.DefaultUserInteractor;
import gov.epa.mims.analysisengine.gui.DoubleEditableTablePanel;
import gov.epa.mims.analysisengine.gui.DoubleValuePanel;
import gov.epa.mims.analysisengine.gui.GUIUserInteractor;
import gov.epa.mims.analysisengine.gui.IntegerValuePanel;
import gov.epa.mims.analysisengine.gui.StringValuePanel;
import gov.epa.mims.analysisengine.gui.UserInteractor;
import gov.epa.mims.analysisengine.stats.Histogram;
import gov.epa.mims.analysisengine.table.DoubleFormatPanel;
import gov.epa.mims.analysisengine.table.format.FormattedCellRenderer;
import gov.epa.mims.analysisengine.table.format.SignificantDigitsFormat;
import gov.epa.mims.analysisengine.tree.DataSets;
import gov.epa.mims.analysisengine.tree.DataSetsAdapter;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.DecimalFormat;
import java.text.Format;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;

/**
 * <p>
 * Title:HistogramGUI.java
 * </p>
 * <p>
 * Description: A gui for the user to specify the bin parameters
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * <p>
 * Company: CEP, UNC-Chapel Hill
 * </p>
 * 
 * @author Parthee Partheepan
 * @version $Id: HistogramGUI.java,v 1.1 2006/11/01 15:33:38 parthee Exp $
 */

public class HistogramGUI extends JPanel {
	/** a StringValuePanel to specify the tab name */
	protected StringValuePanel tabNamePanel = null;

	/** an IntegerValuePanel to specify the no of bins */
	protected IntegerValuePanel binSizePanel = null;

	/** an IntegerValuePanel to specify the lowerBound */
	protected DoubleValuePanel lowerBoundPanel = null;

	/** an IntegerValuePanel to specify the upperBound */
	protected DoubleValuePanel upperBoundPanel = null;

	/** The table for the breaks in the Histogram. */
	private DoubleEditableTablePanel breaksPanel = null;

	/** type of histogram : frequency */
	private JRadioButton frequencyRB;

	/** type of histogram : percentage */
	private JRadioButton percentageRB;

	/** type of histogram : probability */
	private JRadioButton probabilityRB;

	/** specify whether user require equal bin size */
	private JRadioButton equalBinRB;

	/** specify whether user requires custom */
	private JRadioButton customRB;

	/** specify whether user requires powerTen */
	private JRadioButton powerTenRB;

	/**
	 * a button to recompute the bin break points if equalBinRB or powerTenRB is selected
	 */
	private JButton recomputeButton;

	/** a double data format panel */
	private DoubleFormatPanel doubleFormatPanel;

	/** a variable to indicate a string value */
	private static final String BIN_SIZE_LABEL1 = "Number of bins";

	/** a variable to indicate a string value */
	private static final String BIN_SIZE_LABEL2 = "Number of Steps";

	// /** a flag to check before closing all the dialog */
	// private boolean closeDialog = true;
	// /** to indicate whether user hit 'OK' or 'CANCEL' button
	// */
	// private int result = JOptionPane.CANCEL_OPTION;

	/** denote the second column in the breaks panel */
	private final int SECOND_COLUMN = 1;

	/* a model to save the data for this gui */
	private HistogramModel histogramModel;

	/** Creates a new instance of HistogramGUI */
	public HistogramGUI(DataSetsAdapter dataSets) {
		try {
			histogramModel = new HistogramModel(dataSets);
		} catch (Exception e) {
			DefaultUserInteractor.get().notify(this, "Error", e.getMessage(), UserInteractor.ERROR);
		}
		DefaultUserInteractor.set(new GUIUserInteractor());

		initialize();
		initGUIModel();
	}// HistogramGUI

	private void initialize() {
		// tabName panel
		tabNamePanel = new StringValuePanel("Tab Name ", false);
		// data range panel displlaying min and max value
		JPanel minDataRangePanel = new JPanel();
		JLabel minDataLabel = new JLabel("Minimum value:");
		SignificantDigitsFormat format = histogramModel.getFormat();
		JLabel minDataValueLabel = new JLabel(format.format(histogramModel.getMinValue()));
		minDataRangePanel.add(minDataLabel);
		minDataRangePanel.add(minDataValueLabel);
		JPanel maxDataRangePanel = new JPanel();
		JLabel maxDataLabel = new JLabel("Maximum value:");
		JLabel maxDataValueLabel = new JLabel(format.format(histogramModel.getMaxValue()));
		maxDataRangePanel.add(maxDataLabel);
		maxDataRangePanel.add(maxDataValueLabel);

		JPanel dataRangePanel = new JPanel();
		dataRangePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black),
				"Data Range"));
		dataRangePanel.setLayout(new BoxLayout(dataRangePanel, BoxLayout.X_AXIS));
		dataRangePanel.add(minDataRangePanel);
		dataRangePanel.add(maxDataRangePanel);

		// histogram type panel
		JPanel hitogramTypePanel = new JPanel();
		hitogramTypePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black),
				"Type"));
		frequencyRB = new JRadioButton("Frequency", true);
		percentageRB = new JRadioButton("Percentage", false);
		probabilityRB = new JRadioButton("Probability", false);
		ButtonGroup group = new ButtonGroup();
		group.add(frequencyRB);
		group.add(percentageRB);
		group.add(probabilityRB);
		hitogramTypePanel.add(frequencyRB);
		hitogramTypePanel.add(percentageRB);
		hitogramTypePanel.add(probabilityRB);
		// binning panels
		// bin type panel
		JLabel binTypeLabel = new JLabel("Bin Type ");
		equalBinRB = new JRadioButton("Equally Spaced", true);
		powerTenRB = new JRadioButton("Factor of 10", false);
		customRB = new JRadioButton("Custom", false);
		ButtonGroup binGroup = new ButtonGroup();
		binGroup.add(equalBinRB);
		binGroup.add(customRB);
		binGroup.add(powerTenRB);

		JPanel typePanel = new JPanel();
		typePanel.setLayout(new BoxLayout(typePanel, BoxLayout.X_AXIS));
		typePanel.add(binTypeLabel);
		typePanel.add(equalBinRB);
		typePanel.add(customRB);
		typePanel.add(powerTenRB);

		equalBinRB.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent ie) {
				if (equalBinRB.isSelected()) {

					recomputeButton.setEnabled(true);
					binSizePanel.setEnabled(true);
					binSizePanel.setLabel(BIN_SIZE_LABEL1);

					lowerBoundPanel.setEnabled(true);
					upperBoundPanel.setEnabled(true);
					breaksPanel.setEnabled(false);
					// update the doubel editable table model again
				}
			}
		});
		customRB.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent ie) {
				if (customRB.isSelected()) {
					recomputeButton.setEnabled(false);
					binSizePanel.setEnabled(false);
					lowerBoundPanel.setEnabled(false);
					upperBoundPanel.setEnabled(false);
					breaksPanel.setEnabled(true);

				}
			}
		});
		powerTenRB.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent ie) {
				if (powerTenRB.isSelected())

				{
					recomputeButton.setEnabled(true);
					binSizePanel.setEnabled(true);
					binSizePanel.setLabel(BIN_SIZE_LABEL2);
					lowerBoundPanel.setEnabled(true);
					upperBoundPanel.setEnabled(false);
					breaksPanel.setEnabled(false);
				}

			}
		});

		// bound panel
		lowerBoundPanel = new DoubleValuePanel("Lower bound", false);
		upperBoundPanel = new DoubleValuePanel("Upper bound", false);

		JPanel boundPanel = new JPanel();
		boundPanel.add(lowerBoundPanel);

		boundPanel.add(upperBoundPanel);
		// bin size panel

		binSizePanel = new IntegerValuePanel(BIN_SIZE_LABEL1, false, 1, Integer.MAX_VALUE);
		JPanel binPanel = new JPanel();

		binPanel.add(binSizePanel);
		recomputeButton = new JButton("Recompute");

		// action listener is added below
		binPanel.add(recomputeButton);

		recomputeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				recompute();
			}
		});

		String[] availableFormats = { HistogramModel.DEFAULT_EFORMAT };
		doubleFormatPanel = new DoubleFormatPanel(format, availableFormats, false, false, false);
		doubleFormatPanel.updateFormatField();
		JButton formatButton = new JButton("Apply Format");
		JPanel formatButtonPanel = new JPanel();
		formatButtonPanel.add(formatButton);
		formatButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Format format = doubleFormatPanel.getSelectedFormat();
				// as this for formatting double right alignment
				int horAlignment = SwingConstants.RIGHT;
				FormattedCellRenderer renderer = new FormattedCellRenderer(format, horAlignment);
				breaksPanel.setCellRenderer(renderer, SECOND_COLUMN);
				breaksPanel.repaint();
				// breaksPanel
			}
		});

		JPanel formatPanel = new JPanel();
		formatPanel.setBorder(BorderFactory.createTitledBorder("Format Labels"));
		formatPanel.setLayout(new BoxLayout(formatPanel, BoxLayout.Y_AXIS));
		formatPanel.add(doubleFormatPanel);
		formatPanel.add(formatButtonPanel);
		JPanel leftBinningPanel = new JPanel();
		leftBinningPanel.setLayout(new BoxLayout(leftBinningPanel, BoxLayout.Y_AXIS));
		leftBinningPanel.add(typePanel);
		leftBinningPanel.add(boundPanel);
		leftBinningPanel.add(binPanel);
		leftBinningPanel.add(formatPanel);

		// right side panel of the binning panel
		// Histogram breakpoints
		breaksPanel = new DoubleEditableTablePanel("Break Points") {
			public Dimension getPreferredSize() {
				return new Dimension(175, 225);
			}

		};
		breaksPanel.setEnabled(false);
		JPanel customPanel = new JPanel();
		customPanel.setLayout(new BoxLayout(customPanel, BoxLayout.Y_AXIS));
		customPanel.setBorder(BorderFactory.createEtchedBorder());
		customPanel.add(breaksPanel);
		JPanel rightBinningPanel = new JPanel(new BorderLayout());
		rightBinningPanel.add(customPanel);

		JPanel binningPanel = new JPanel();
		binningPanel.setLayout(new BoxLayout(binningPanel, BoxLayout.X_AXIS));
		binningPanel.setBorder(BorderFactory.createTitledBorder(

		BorderFactory.createLineBorder(Color.black), "Binning"));
		binningPanel.add(leftBinningPanel);
		binningPanel.add(rightBinningPanel);

		JPanel histogramPanel = new JPanel();
		histogramPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5),
				BorderFactory.createCompoundBorder(BorderFactory.createLoweredBevelBorder(), BorderFactory
						.createEmptyBorder(5, 5, 5, 5))));
		histogramPanel.setLayout(new BoxLayout(histogramPanel, BoxLayout.Y_AXIS));
		histogramPanel.add(Box.createVerticalStrut(10));
		histogramPanel.add(tabNamePanel);
		// histogramPanel.add(Box.createVerticalStrut(40));
		histogramPanel.add(hitogramTypePanel);
		histogramPanel.add(dataRangePanel);

		histogramPanel.add(binningPanel);

		this.setLayout(new BorderLayout());
		this.add(histogramPanel);

	}

	/**
	 * Recompute the bin size base on the lower bound, upper bound and the no of bins
	 */
	private void recompute() {
		double minValue = lowerBoundPanel.getValue();
		int noOfBinsOrSteps = binSizePanel.getValue();
		double[] binBreakPoints = null;
		if (equalBinRB.isSelected()) {
			double maxValue = upperBoundPanel.getValue();
			if (minValue > maxValue) {
				DefaultUserInteractor.get().notify(this, "Error Values",

				"Lower bound value is greater than upper bound value", UserInteractor.ERROR);
				return;
			}// if
			binBreakPoints = histogramModel.createEqualSizeBins(noOfBinsOrSteps, minValue, maxValue);
		}// if(equalBinRB.isSelected())
		else if (powerTenRB.isSelected()) {
			binBreakPoints = histogramModel.createFactorTenBins(noOfBinsOrSteps, minValue);
		}// else if(powerTenRB.isSelected())
		else {
			DefaultUserInteractor.get().notify(this, "Error", "This is not a valid option", UserInteractor.ERROR);
		}// else

		breaksPanel.setValue(binBreakPoints);
	}

	/** to initialize the values from the data model to the gui */
	public void initGUIModel() {
		tabNamePanel.setValue(histogramModel.getTabName());
		double minValue = histogramModel.getMinValue();
		double maxValue = histogramModel.getMaxValue();
		lowerBoundPanel.setValue(minValue);
		upperBoundPanel.setValue(maxValue);
		byte histogramType = histogramModel.getHistogramType();
		if (histogramType == Histogram.FREQUENCY)

		{
			frequencyRB.setSelected(true);
		}// if
		else if (histogramType == Histogram.PERCENTAGE) {
			percentageRB.setSelected(true);
		}// else if
		else if (histogramType == Histogram.PROBABILITY) {
			probabilityRB.setSelected(true);
		}// else if
		else {
			DefaultUserInteractor.get().notify(this, "Error",
					"The histogram type " + histogramType + " is not in the gui yet! ", UserInteractor.ERROR);

		}// else
		int noOfBins = histogramModel.getNoOfBins();
		binSizePanel.setValue(noOfBins);
		double[] breakPoints = histogramModel.createEqualSizeBins(noOfBins, minValue, maxValue);
		if (equalBinRB.isSelected()) {

			breaksPanel.setValue(breakPoints);
		}// if(equalBinRB.isSelected())

		else if (customRB.isSelected()) {
			breakPoints = histogramModel.getBinBreakPoints();
			if (breakPoints != null) {
				breaksPanel.setValue(breakPoints);
			}
		}// else if

		DecimalFormat format = histogramModel.getFormat();
		// as this for formatting double right alignment
		int horAlignment = SwingConstants.RIGHT;
		FormattedCellRenderer renderer = new FormattedCellRenderer(format, horAlignment);
		breaksPanel.setCellRenderer(renderer, SECOND_COLUMN);

		breaksPanel.repaint();
	}// initGUIModel()

	/**
	 * to initialize the values from the data model to the gui
	 * 
	 * @parma hModel HistogramModel
	 */
	public void initGUIModel(HistogramModel hModel) {
		histogramModel = hModel;
		initGUIModel();
	}

	/**
	 * a method to save the gui data to the model
	 */
	public HistogramModel saveToModel() {
		// check whether every thing in the ascending order
		String tabName = tabNamePanel.getValue();
		if (tabName == null || tabName.trim().length() == 0) {
			DefaultUserInteractor.get().notify(this, "Tab Name", "Please specify the tab name", UserInteractor.ERROR);
			// don't close
			return null;
		}// if

		histogramModel.setTabName(tabName);
		histogramModel.setMinValue(lowerBoundPanel.getValue());
		histogramModel.setMaxValue(upperBoundPanel.getValue());
		histogramModel.setNoOfBins(binSizePanel.getValue());
		byte histogramType;
		if (frequencyRB.isSelected()) {
			histogramType = Histogram.FREQUENCY;
		}// if
		else if (percentageRB.isSelected()) {
			histogramType = Histogram.PERCENTAGE;
		}// else if
		else {
			histogramType = Histogram.PROBABILITY;
		}// else
		histogramModel.setHistogramType(histogramType);
		histogramModel.setFormat((SignificantDigitsFormat) doubleFormatPanel.getSelectedFormat());
		double[] bins = breaksPanel.getValueAsPrimitive();

		if (bins == null || bins.length == 0) {
			DefaultUserInteractor.get().notify(this, "No Bins", "Spefify bin break points", UserInteractor.ERROR);
			// don't close
			return null;
		}// if(bins == null || bins.length == 0)
		// check whether it's sorted
		try {
			breaksPanel.verifyListSorted(true);
			histogramModel.setBinBreakPoints(bins);

		} catch (Exception e) {
			DefaultUserInteractor.get().notify(this, "Not Sorted", e.getMessage(), UserInteractor.ERROR);
			return null;
		}// catch
		return this.histogramModel;
	}// saveToModel()

	/**
	 * return the array of bins with break points
	 * 
	 * @return double [] bins
	 */
	public double[] getBins() {
		return histogramModel.getBinBreakPoints();
	}

	/**
	 * return the format used
	 * 
	 * @return
	 */
	public DecimalFormat getFormat() {
		return histogramModel.getFormat();
	}

	/**
	 * return the tab name
	 * 
	 * @return String
	 */
	public String getTabName() {
		return tabNamePanel.getValue();
	}// getTabName()

	/**
	 * get the type of histogram return byte histogramType
	 */
	public byte getHistogramType() {
		return histogramModel.getHistogramType();
	}// getHistogramType()

	/**
	 * get the no of bins
	 */
	public int getNoOfBins() {
		return histogramModel.getNoOfBins();
	}

	// /** to get whether user hit a 'OK' button or 'CANCEL' button
	// */
	// public int getResult()
	// {
	// return result;
	// }

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) {

		gov.epa.mims.analysisengine.gui.LabeledDoubleSeries lds = new gov.epa.mims.analysisengine.gui.LabeledDoubleSeries();
		lds.setName("dave histogram");

		int count = 6;
		String labelPrefix = "data";
		for (int i = 0; i < count; ++i) {
			String labelName = labelPrefix + i;
			double value = Math.random() * 10.0;
			lds.addData(value, labelName);
		}
		DataSets dataSets = new DataSets();
		dataSets.add(lds, "key1");
		dataSets.add(lds, "key2");
		HistogramGUI histogram = new HistogramGUI(dataSets);
		JFrame frame = new JFrame();
		frame.getContentPane().add(histogram);
		frame.pack();
		frame.setVisible(true);
	}
}
