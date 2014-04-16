package gov.epa.mims.analysisengine.table.format;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Hashtable;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * <p>
 * Description: An abstract class that allows the user to select from a variety of pre-defined formats or to type in
 * their own format. DecimalFormat and SimpleDateFormat are likely to be used in sub-classes.
 * </p>
 * <p>
 * Copyright: Copyright (c) 2003
 * </p>
 * <p>
 * Company: UNC - CEP
 * </p>
 * 
 * @author Daniel Gatti
 * @version $Id: FormatChooserPanel.java,v 1.1 2006/11/01 15:33:38 parthee Exp $
 */
public abstract class FormatChooserPanel extends JPanel {
	/** The editable combo box where format 1 Strings are displayed. */
	protected JComboBox formatBox = null;

	/** The editable combo box where format 2 Strings are displayed. */
	protected JComboBox format2Box = null;

	/** The label where we will display a sample formatted value. */
	protected JLabel sampleLabel = new JLabel();

	/** The text that goes in front of the sample formatting label. */
	public static final String SAMPLE_PREFIX = "Sample: ";

	/**
	 * A sample object to format for the sample label. (Date for date formatters, Double for decimal formatters, etc. )
	 */
	protected Object sampleObject = null;

	/** A hash table with formatters that have already been requested. */
	protected static final Hashtable formatters = new Hashtable();

	/** A list of stock formats. Populate this in the constructor. */
	public String[] predefinedFormats = null;

	/** The format that should be selected. */
	protected Format format = null;

	/**
	 * Constructor.
	 * 
	 * @param format
	 *            Format that is the currently selected format. (Could be null. If it is, we select the first format.)
	 * @param formatsLabel
	 *            a label for the availableFormats
	 * @param availableFormats
	 *            String[] with pre-defined formats.
	 * @param allowUserValue
	 *            boolean that is true if we want the user to be able to type in a custom format that is not in the
	 *            list.
	 */
	public FormatChooserPanel(Format format, String formatsLabel, String[] availableFormats, boolean allowUserValue) {
		this(format, formatsLabel, availableFormats, null, null, allowUserValue);
	} // FormatChooserPanel()

	/**
	 * Constructor customized for date/time split formats.. Use the other constructor for standard setup
	 * 
	 * @param format
	 *            Format that is the currently selected format. (Could be null. If it is, we select the first format.)
	 * @param availableFormats
	 *            String[] with pre-defined formats.
	 * @param formatsLabel
	 *            a label for the availableFormats
	 * @param availableFormats2
	 *            String[] with second set of formats
	 * @param formatsLabel2
	 *            a label for the second set of formats
	 * @param allowUserValue
	 *            boolean that is true if we want the user to be able to type in a custom format that is not in the
	 *            list.
	 */
	public FormatChooserPanel(Format format, String formatsLabel, String[] availableFormats, String formatsLabel2,
			String[] availableFormats2, boolean allowUserValue) {
		if (availableFormats == null) {
			throw new IllegalArgumentException("No available formats were specified");
		}
		formatBox = new JComboBox(availableFormats);
		formatBox.setEditable(allowUserValue);
		ActionListener formatListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setSampleLabel();
			}
		};
		formatBox.addActionListener(formatListener);

		// create a format panel and add the first format box to it
		JPanel formatPanel = new JPanel();
		// if a label is provided add it
		if (formatsLabel != null) {
			formatPanel.add(new JLabel(formatsLabel));
		}
		formatPanel.add(formatBox);

		// if the second format is not specified do nothing
		if (availableFormats2 != null) {
			format2Box = new JComboBox(availableFormats2);
			format2Box.setEditable(allowUserValue);
			format2Box.addActionListener(formatListener);
			formatPanel.add(new JLabel(formatsLabel2));
			formatPanel.add(format2Box);
		}// if (availableFormats2 != null)

		sampleLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		sampleLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		// We're building our own GUI, so loose the one that the super class built.
		removeAll();
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(formatPanel);
		add(sampleLabel);

		setFormat(format);
	} // FormatChooserPanel()

	/**
	 * Null conctructor for sub-classes.
	 */
	protected FormatChooserPanel() {
		/* Nothing */
	}

	/**
	 * Create a new formatter with the given format String and add it to the hashtable of existing formatters. Then
	 * return it.
	 * 
	 * @return Format that has just been created.
	 */
	protected abstract Format createNewFormat(String formatString);

	/**
	 * If the requested formatter has been created before, then return it. Otherwise make a new one, add it to the
	 * hashtable and return it. This has the effect of only creating each formatter once. Although it means that we keep
	 * unused ones around once we're done with them, too.
	 */
	public Format getSelectedFormat() {
		Format retval = null;
		String selection = (String) formatBox.getSelectedItem();

		// if there exists the second formatting box then append that selection to
		// this one
		if (format2Box != null) {
			selection = selection + " " + format2Box.getSelectedItem().toString();
		}
		if (selection != null) {
			retval = (Format) formatters.get(selection);
			if (retval == null) {
				retval = createNewFormat(selection);
			}
		}

		return retval;
	} // getSelectedFormat()

	/**
	 * Set the lit of available formats.
	 * 
	 * @param newFormats
	 *            String[] that is a list of new formats to place in the combo box.
	 */
	public void setAvailableFormats(String[] newFormats) {
		formatBox.removeAllItems();
		for (int i = 0; i < newFormats.length; i++) {
			formatBox.addItem(newFormats[i]);
		}
	} // setAvailableFormats()

	/**
	 * Set the current format.
	 */
	public void setFormat(Format newFormat) {
		this.format = newFormat;

		// Don't know how else to get the pattern String since the Format class
		// doesn't have a concept of a format string.
		String formatString = null;
		if (format instanceof SimpleDateFormat) {
			formatString = ((SimpleDateFormat) format).toPattern();
		} else if (format instanceof SignificantDigitsFormat) {
			formatString = ((SignificantDigitsFormat) format).toPattern();
		}

		if (format2Box == null) {
			formatBox.setSelectedItem(formatString);
		}
		// if this is the date/time split formatting support
		else {
			StringBuffer firstStringBuffer = new StringBuffer(formatString);
			String secondString = splitToGetSecondString(firstStringBuffer);
			String firstString = firstStringBuffer.toString();
			formatBox.setSelectedItem(firstString);
			format2Box.setSelectedItem(secondString);
		}
	} // setFormat()

	/**
	 * this placeholder method is to allow each implementing class to define how to split a format string into 2..
	 * currently required for implementing the date/time format panel
	 * 
	 * @param formatString
	 * @return
	 */
	public abstract String splitToGetSecondString(StringBuffer formatString);

	/**
	 * Set the sample label based on the current date format.
	 */
	protected void setSampleLabel() {
		format = getSelectedFormat();
		if (format == null) {
			return;
		}

		if (sampleObject != null) {
			sampleLabel.setText(SAMPLE_PREFIX + format.format(sampleObject));
		}
		validate();
	} // setSampleLabel()

} // class FormatChooserPanel

