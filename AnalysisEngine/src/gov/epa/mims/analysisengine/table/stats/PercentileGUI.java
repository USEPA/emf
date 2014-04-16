package gov.epa.mims.analysisengine.table.stats;

import gov.epa.mims.analysisengine.gui.DefaultUserInteractor;
import gov.epa.mims.analysisengine.gui.DoubleEditableTablePanel;
import gov.epa.mims.analysisengine.gui.DoubleValuePanel;
import gov.epa.mims.analysisengine.gui.StringValuePanel;
import gov.epa.mims.analysisengine.gui.UserInteractor;
import gov.epa.mims.analysisengine.table.format.FormattedCellRenderer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

public class PercentileGUI extends JPanel {

	/** a data model for the gui */
	private PercentileModel percentileModel;

	/** a panel to specify percentile values */
	private DoubleEditableTablePanel percentilePanel;

	/** a rb for standard */
	private JRadioButton standardRB;

	/** a rb for quintiles */

	private JRadioButton quintilesRB;

	/** a rb for quartiles */

	private JRadioButton quartilesRB;

	/** a rb for tentiles */

	private JRadioButton decilesRB;

	/* a double value panel to specify the lower bound percentiles */
	private DoubleValuePanel minValuePanel;

	/* a double value panel to specify the upper bound percentiles */
	private DoubleValuePanel maxValuePanel;

	/* an integer value panel to specify the upper bound percentiles */
	private DoubleValuePanel stepSizeValuePanel;

	/** a button for add a specified percentile to the percentile panel */
	private JButton addPercentileButton;

	/** a string value panel to specify the tab name */
	private StringValuePanel tabNamePanel;

	/** a line border */
	private Border lineBorder = BorderFactory.createLineBorder(Color.black);

	/** a deimal format for the renderer */
	private DecimalFormat format = new DecimalFormat("0.0000");

	/** renderer for the table */
	private FormattedCellRenderer renderer;

	private static final double TOLERANCE_DIVIDER = 10000.0;

	public PercentileGUI() {
		percentileModel = new PercentileModel();
		// tabName panel
		tabNamePanel = new StringValuePanel("Tab Name ", false);
		// customized options panel
		standardRB = new JRadioButton("Standard", true);
		standardRB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				percentilePanel.setValue(PercentileModel.STANDARD);
			}
		});
		quartilesRB = new JRadioButton("Quartiles", false);
		quartilesRB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				percentilePanel.setValue(PercentileModel.QUARTILES);
			}
		});
		quintilesRB = new JRadioButton("Quintiles", false);
		quintilesRB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				percentilePanel.setValue(PercentileModel.QUINTILES);
			}
		});
		decilesRB = new JRadioButton("Deciles", false);
		decilesRB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				percentilePanel.setValue(PercentileModel.DECILES);
			}
		});

		ButtonGroup group = new ButtonGroup();
		group.add(standardRB);
		group.add(quartilesRB);
		group.add(quintilesRB);
		group.add(decilesRB);
		JPanel customOptionPanel = new JPanel();
		// customOptionPanel.setLayout(new BoxLayout(customOptionPanel,BoxLayout.X_AXIS));
		customOptionPanel.setBorder(BorderFactory.createTitledBorder(lineBorder, "Frequent Options"));
		customOptionPanel.add(standardRB);
		customOptionPanel.add(quartilesRB);
		customOptionPanel.add(quintilesRB);
		customOptionPanel.add(decilesRB);
		// adding percentiles
		minValuePanel = new DoubleValuePanel("Minimum Percentile", false, 0.0, 1.0);
		maxValuePanel = new DoubleValuePanel("Maximum Percentile", false, 0.0, 1.0);
		stepSizeValuePanel = new DoubleValuePanel("Step Size", false, 0.0001, 1.0);
		addPercentileButton = new JButton("Add Percentiles");
		addPercentileButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addPercentiles();
			}
		});
		JPanel addButtonPanel = new JPanel();
		addButtonPanel.add(addPercentileButton);
		JPanel addPercentilePanel = new JPanel();
		addPercentilePanel.setBorder(BorderFactory.createTitledBorder(lineBorder, "Add Percentiles"));
		addPercentilePanel.setLayout(new BoxLayout(addPercentilePanel, BoxLayout.Y_AXIS));
		addPercentilePanel.add(minValuePanel);
		addPercentilePanel.add(maxValuePanel);
		addPercentilePanel.add(stepSizeValuePanel);
		addPercentilePanel.add(Box.createVerticalGlue());
		addPercentilePanel.add(addButtonPanel);
		percentilePanel = new DoubleEditableTablePanel("Percentile") {
			public Dimension getPreferredSize() {
				return new Dimension(300, 225);
			}
		};

		renderer = new FormattedCellRenderer(format, SwingConstants.CENTER);
		percentilePanel.setCellRenderer(renderer, 1);
		percentilePanel.setBounds(0.0, 1.0);
		JPanel contForPercentPanel = new JPanel(new BorderLayout());
		contForPercentPanel.add(percentilePanel);
		contForPercentPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black),
				"Percentiles"));

		JPanel addAndTablePanel = new JPanel();
		addAndTablePanel.setLayout(new BorderLayout());
		addAndTablePanel.add(addPercentilePanel);
		addAndTablePanel.add(contForPercentPanel, BorderLayout.EAST);
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4),
				BorderFactory.createCompoundBorder(BorderFactory.createLoweredBevelBorder(), BorderFactory
						.createEmptyBorder(4, 4, 4, 4))));
		mainPanel.add(Box.createVerticalStrut(10));
		mainPanel.add(tabNamePanel);
		mainPanel.add(Box.createVerticalStrut(20));
		mainPanel.add(customOptionPanel);
		mainPanel.add(addAndTablePanel);
		this.setLayout(new BorderLayout());
		this.add(mainPanel);
		initGUIModel();
	}// PercentileGUI()

	private void addPercentiles() {
		double minPercentile = minValuePanel.getValue();
		double maxPercentile = maxValuePanel.getValue();
		double stepSize = stepSizeValuePanel.getValue();
		if (Double.isNaN(minPercentile)) {
			DefaultUserInteractor.get().notify(this, "Input Error", "Please input a " + "value for lower percentile",
					UserInteractor.ERROR);
			return;
		} else if (Double.isNaN(maxPercentile)) {
			DefaultUserInteractor.get().notify(this, "Input Error", "Please input a " + "value for upper percentile",
					UserInteractor.ERROR);
			return;
		} else if (minPercentile > maxPercentile) {
			DefaultUserInteractor.get().notify(
					this,
					"Input Error",
					"Lower percentile value " + minPercentile + " is greater than upper percentile value "
							+ maxPercentile, UserInteractor.ERROR);
			return;
		} else if (Double.isNaN(stepSize)) {
			DefaultUserInteractor.get().notify(this, "Input Error", "Please input a " + "value for step size",
					UserInteractor.ERROR);
			return;
		} else if (stepSize > (maxPercentile - minPercentile)) {
			DefaultUserInteractor.get().notify(
					this,
					"Input Error",
					"Please input a " + "step size smaller than difference between upper percentile value "
							+ "and lower percentile value", UserInteractor.ERROR);
			return;
		}// else if(stepSize > (maxPercentile - minPercentile))
		int noOfPercentiles = (int) Math.round(((maxPercentile - minPercentile) / stepSize + 1));
		double[] newValues = new double[noOfPercentiles];
		for (int i = 0; i < newValues.length; i++) {
			newValues[i] = minPercentile + i * stepSize;
			// System.out.println("newValues[i]="+newValues[i]);
		}
		percentilePanel.insertRowUniqueValues(newValues, stepSize / TOLERANCE_DIVIDER);
		percentilePanel.sort(true);
	}// addPercentiles()

	/** initialize gui with the values from the data model * */
	public void initGUIModel() {
		tabNamePanel.setValue(percentileModel.getTabName());
		double[] percentiles = percentileModel.getPercentiles();
		if (percentiles != null) {
			percentilePanel.setValue(percentiles);
		}// if
	}// initGUIModel()

	/**
	 * initialize gui with the values from the data model
	 * 
	 * @param PercentileModel
	 */
	public void initGUIModel(PercentileModel pModel) {
		this.percentileModel = pModel;
		initGUIModel();
	}

	/**
	 * a method to save the gui data to the model
	 */
	public PercentileModel saveToModel() {
		String tabName = tabNamePanel.getValue();
		if (tabName == null || tabName.trim().length() == 0) {
			DefaultUserInteractor.get().notify(this, "Tab Name", "Please specify the tab name", UserInteractor.ERROR);
			return null;
		}// if
		percentileModel.setTabName(tabName);
		try {
			percentilePanel.checkDuplicateValues(1.0 / TOLERANCE_DIVIDER);
		} catch (Exception e) {
			DefaultUserInteractor.get().notify(this, "Duplicate values", e.getMessage(), UserInteractor.ERROR);
			return null;
		}
		double[] percentiles = percentilePanel.getValueAsPrimitive();
		if (percentiles == null || percentiles.length == 0) {
			DefaultUserInteractor.get().notify(this, "No Percentiles", "Specify the percentile values",
					UserInteractor.ERROR);
			return null;
		}// if(bins == null || bins.length == 0)
		// check whether it's sorted
		try {
			percentilePanel.verifyListSorted(true);
			percentileModel.setPercentiles(percentiles);
		} catch (Exception e) {
			DefaultUserInteractor.get().notify(this, "Not Sorted", e.getMessage(), UserInteractor.ERROR);
			return null;
		}// catch
		return this.percentileModel;
	}// saveToModel()

	public double[] getValues() {
		return percentilePanel.getValueAsPrimitive();
	}

	public String getTabName() {
		return percentileModel.getTabName();
	}

	public static void main(String[] args) {
		JFrame f = new JFrame();
		PercentileGUI percentileGUI = new PercentileGUI();
		f.getContentPane().add(percentileGUI);
		f.pack();
		f.setVisible(true);
	}
}
