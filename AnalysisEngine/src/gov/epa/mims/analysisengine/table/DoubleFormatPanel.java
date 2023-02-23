package gov.epa.mims.analysisengine.table;

import gov.epa.mims.analysisengine.gui.NumericSpinner;
import gov.epa.mims.analysisengine.table.format.FormatChooserPanel;
import gov.epa.mims.analysisengine.table.format.SignificantDigitsFormat;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.DecimalFormat;
import java.text.Format;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

/**
 * <p>
 * Description: A panel that shows formatting doubles. It includes scientific notation, percent, dollars and a sample
 * label to show the user what their format will look like.
 * </p>
 * <p>
 * Copyright: Copyright (c) 2003
 * </p>
 * <p>
 * Company: UNC - CEP
 * </p>
 * 
 * @author Daniel Gatti, Prashant Pai (08/24)
 * @version $Id: DoubleFormatPanel.java,v 1.4 2006/11/01 15:33:37 parthee Exp $
 */
public class DoubleFormatPanel extends FormatChooserPanel implements ActionListener {
	/** The default time/date format. */
	public static final String DEFAULT_NUMBER_FORMAT = "0.00";

	/** A spinner for the number of decimal place. */
	protected NumericSpinner decimalSpinner = null;

	/** A spinner for the number of significant digits */
	protected NumericSpinner sigDigitsSpinner = null;

	/** A radio button that is the default standard format. */
	protected JRadioButton standardButton = new JRadioButton(SignificantDigitsFormat.STANDARD_FORMAT, false);

	/** A radio button that allows for scientific format. */
	protected JRadioButton scientificButton = new JRadioButton(SignificantDigitsFormat.SCIENTIFIC_FORMAT, true);

	/** Radio button for percentage format */
	protected JRadioButton percentButton = new JRadioButton(SignificantDigitsFormat.PERCENTAGE_FORMAT, false);

	/** radio button for dollars i.e. currency */
	protected JRadioButton dollarsButton = new JRadioButton(SignificantDigitsFormat.CURRENCY_FORMAT, false);

	/** checkbox for enabling the significant digits feature * */
	protected JCheckBox sigDigitsCheckBox = new JCheckBox();

	/** A tex field for expert users. */
	protected JTextField formatField = new JTextField(7);

	/** a hashmap that will contain all the buttons on the left hand panel * */
	private transient HashMap buttonMap = new HashMap();

	/** a button group for the formatting option radio buttons * */
	private ButtonGroup group = new ButtonGroup();

	/**
	 * Constructor.
	 * 
	 * @param format
	 *            Format that is the currently selected format. (Could be null. If it is, we select the first format.)
	 * @param availableFormats
	 *            String[] with pre-defined formats. (Could be null. If it is, we supply formats)
	 * @param allowUserValue
	 *            boolean that is true if we want the user to be able to type in a custom format that is not in the
	 *            list.
	 * @param showPercent
	 *            boolean that is true if we should show the percent check box.
	 * @param showDollars
	 *            boolean that is true if we should show the dollars check box.
	 */
	public DoubleFormatPanel(Format format, String[] availableFormats, boolean allowUserValue, boolean showPercent,
			boolean showDollars) {
		this.format = format;

		this.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Numeric Format Options"),
				BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		decimalSpinner = new NumericSpinner("Decimal Places:", 3.0, new DecimalFormat("0"), 1.0, 0.0, 10.0);
		decimalSpinner.setPreferredSize(new Dimension(40, 20));
		decimalSpinner.setBorder(BorderFactory.createEmptyBorder(3, 22, 3, 3));
		decimalSpinner.setEnabled(false);

		sigDigitsSpinner = new NumericSpinner("Significant Digits:", 4.0, new DecimalFormat("0"), 1.0, 1.0, 10.0);
		sigDigitsSpinner.setPreferredSize(new Dimension(40, 20));
		sigDigitsSpinner.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 3));

		sigDigitsCheckBox.setSelected(true);

		sampleObject = Double.valueOf(123.456);

		// Listen to changes to update the format field.
		decimalSpinner.addActionListener(this);
		sigDigitsSpinner.addActionListener(this);
		sigDigitsCheckBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent ie) {
				// enable the spinner if the state is changed to selected
				sigDigitsSpinner.setEnabled(ie.getStateChange() == ItemEvent.SELECTED);
			}
		});

		standardButton.addActionListener(this);
		scientificButton.addActionListener(this);
		percentButton.addActionListener(this);
		dollarsButton.addActionListener(this);

		// put all the formatting options in a buttongroup so that
		// they are mutually exclusive

		group.add(standardButton);
		group.add(scientificButton);
		group.add(percentButton);
		group.add(dollarsButton);

		buttonMap.put(SignificantDigitsFormat.SCIENTIFIC_FORMAT, scientificButton);
		buttonMap.put(SignificantDigitsFormat.STANDARD_FORMAT, standardButton);
		buttonMap.put(SignificantDigitsFormat.PERCENTAGE_FORMAT, percentButton);
		buttonMap.put(SignificantDigitsFormat.CURRENCY_FORMAT, dollarsButton);

		// put all these buttons into a hashmap to get the correct one activated
		// when a new format is set
		sampleLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		sampleLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		// Add the new component for this GUI.
		JPanel optionsPanel = new JPanel();
		optionsPanel.setLayout(new GridLayout(4, 1, 3, 5));
		optionsPanel.add(scientificButton);
		optionsPanel.add(standardButton);

		if (showPercent) {
			optionsPanel.add(percentButton);
		}

		if (showDollars) {
			optionsPanel.add(dollarsButton);
		}

		JPanel sigDigitsPanel = new JPanel(new BorderLayout());
		sigDigitsPanel.add(sigDigitsCheckBox, BorderLayout.WEST);
		sigDigitsPanel.add(sigDigitsSpinner, BorderLayout.CENTER);

		JPanel rightPanel = new JPanel(new GridLayout(3, 1, 5, 8));
		// rightPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		// rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
		rightPanel.add(decimalSpinner);
		rightPanel.add(sigDigitsPanel);
		// rightPanel.add(formatField);
		// rightPanel.add(sampleLabel);
		JPanel formatPanel = new JPanel(new BorderLayout());
		formatPanel.add(new JLabel("Format: "), BorderLayout.WEST);
		formatPanel.add(formatField, BorderLayout.CENTER);
		formatPanel.setBorder(BorderFactory.createEmptyBorder(3, 22, 3, 3));
		formatField.setToolTipText("Edit this field only if you understand the formatting string.");

		JPanel testPanel = new JPanel(new BorderLayout());
		testPanel.add(formatPanel, BorderLayout.WEST);
		testPanel.add(sampleLabel, BorderLayout.CENTER);
		rightPanel.add(testPanel);
		rightPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		setLayout(new BorderLayout());
		add(optionsPanel, BorderLayout.WEST);
		add(rightPanel, BorderLayout.CENTER);
		initGUIFromModel();
	}// DoubleFormatPanel()

	/**
	 * this method initializes the panel from the set format class
	 */
	protected void initGUIFromModel() {
		String formatOption = ((SignificantDigitsFormat) format).getSelectedOption();
		JRadioButton buttonToBeSelected = (JRadioButton) buttonMap.get(formatOption);
		buttonToBeSelected.setSelected(true);

		int numDecimalPlaces = ((SignificantDigitsFormat) format).getNumberOfDecimalPlaces();
		decimalSpinner.setValue(numDecimalPlaces);

		int numSigDigits = ((SignificantDigitsFormat) format).getNumberOfSignificantDigits();
		if (numSigDigits == 0) {
			sigDigitsCheckBox.setSelected(false);
		}
		sigDigitsSpinner.setValue(numSigDigits);

		actionPerformed(null);
	}// initGUIFromModel()

	/**
	 * Create a new formatter with the given format String and add it to the hashtable of existing formatters. Then
	 * return it.
	 * 
	 * @return Format that has just been created.
	 */
	protected Format createNewFormat(String formatString) {
		// DecimalFormat newFormat = new DecimalFormat(formatString);
		SignificantDigitsFormat newFormat = new SignificantDigitsFormat(formatString);
		formatters.put(formatString, newFormat);

		return newFormat;
	}

	// createNewFormat()

	/**
	 * Respond to actions and reset the format.
	 */
	public void actionPerformed(ActionEvent e) {
		decimalSpinner.setEnabled(!scientificButton.isSelected());
		sigDigitsCheckBox.setEnabled(!scientificButton.isSelected());
		if (scientificButton.isSelected()) {
			decimalSpinner.setValue((int) sigDigitsSpinner.getValue() - 1);
			sigDigitsCheckBox.setSelected(true);
		}// if (scientificButton.isSelected())
		// update the format field after the decimalSpinner value has been set
		updateFormatField();
	}// actionPerformed()

	/**
	 * If the requested formatter has been created before, then return it. Otherwise make a new one, add it to the
	 * hashtable and return it. This has the effect of only creating each formatter once. Although it means that we keep
	 * unused ones around once we're done with them, too.
	 * 
	 * @return the format from the format string
	 */
	public Format getSelectedFormat() {
		SignificantDigitsFormat retval = new SignificantDigitsFormat();
		// String formatPattern = formatField.getText();
		int numSigDigits = (int) sigDigitsSpinner.getValue();
		// if the significant digits check is unchecked
		// set the numSigDigits to 0, this will disable the sig digits feature
		if (!sigDigitsCheckBox.isSelected()) {
			numSigDigits = 0;
		}
		int numDecimalPlaces = (int) decimalSpinner.getValue();
		String formatOption = getFormatOption();
		retval.setSelectedOption(formatOption);
		retval.setNumberOfDecimalPlaces(numDecimalPlaces);
		retval.setNumberOfSignificantDigits(numSigDigits);
		retval.applyPattern(retval.toPattern());

		return retval;
	}

	// getSelectedFormat()

	/**
	 * Set the current format.
	 */
	public void setFormat(Format newFormat) {
		this.format = newFormat;
		initGUIFromModel();
	}

	// setFormat()

	/**
	 * Set the sample label based on the current date format.
	 */
	protected void setSampleLabel() {
		Format fieldFormat = getSelectedFormat();

		if (fieldFormat == null) {
			return;
		}

		if (sampleObject != null) {
			sampleLabel.setText(SAMPLE_PREFIX + fieldFormat.format(sampleObject));
		}
	}

	// setSampleLabel()

	/**
	 * When something about the format has changed, update the format field and then change the sample label. PPP - The
	 * format pattern is later picked up from this field.
	 */
	public void updateFormatField() {
		SignificantDigitsFormat fieldFormat = (SignificantDigitsFormat) getSelectedFormat();
		formatField.setText(fieldFormat.toPattern());
		setSampleLabel();
	}// updateFormatField()

	/**
	 * returns the formatting option selected by checking the buttons
	 * 
	 * @return
	 */
	private String getFormatOption() {
		if (standardButton.isSelected()) {
			return SignificantDigitsFormat.STANDARD_FORMAT;
		} else if (scientificButton.isSelected()) {
			return SignificantDigitsFormat.SCIENTIFIC_FORMAT;
		} else if (dollarsButton.isSelected()) {
			return SignificantDigitsFormat.CURRENCY_FORMAT;
		} else if (percentButton.isSelected()) {
			return SignificantDigitsFormat.PERCENTAGE_FORMAT;
		} else {
			return null;
		}
	}// getFormatOption()

	/**
	 * Empty implmentation. This is currently only done for the DateFormatPanel
	 * 
	 * @param formatString
	 * @return
	 */
	public String splitToGetSecondString(StringBuffer formatString) {
		return null;
	}// splitToGetSecondString(StringBuffer)
}

// class DoubleFormatPanel
