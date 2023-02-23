package gov.epa.mims.analysisengine.table.format;

import gov.epa.mims.analysisengine.gui.ColorValuePanel;
import gov.epa.mims.analysisengine.gui.FontPanel;
import gov.epa.mims.analysisengine.gui.IntegerValuePanel;
import gov.epa.mims.analysisengine.gui.StringChooserPanel;
import gov.epa.mims.analysisengine.table.DateFormatPanel;
import gov.epa.mims.analysisengine.table.DoubleFormatPanel;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * <p>
 * Description: A GUI to allow the user to edit the column format with item such as font, alignment and number format.
 * </p>
 * <p>
 * Copyright: Copyright (c) 2003
 * </p>
 * <p>
 * Company: UNC - CEP
 * </p>
 * 
 * @author Daniel Gatti
 * @version $Id: ColumnFormatPanel.java,v 1.1 2006/11/01 15:33:38 parthee Exp $
 */
public class ColumnFormatPanel extends JPanel {
	/** The ColumnFormatInfo that this GUI represents. */
	protected ColumnFormatInfo formatInfo = null;

	/** A hashtable for alignments. */
	protected static final Hashtable alignmentConverter = new Hashtable();

	public static final String LEFT = "Left";

	public static final String CENTER = "Center";

	public static final String RIGHT = "Right";

	/** The font panel. */
	protected FontPanel fontPanel = new FontPanel(null);

	/** The alignment panel. */
	protected StringChooserPanel alignPanel = null;

	/** The panel with a combo box for the format. */
	protected FormatChooserPanel formatPanel = null;

	/** The column width panel. */
	protected IntegerValuePanel widthPanel = null;

	/** The foreground color editor. */
	protected ColorValuePanel foregroundPanel = null;

	/** The background color editor. */
	protected ColorValuePanel backgroundPanel = null;

	static {
		alignmentConverter.put(LEFT, Integer.valueOf(SwingConstants.LEFT));
		alignmentConverter.put(CENTER, Integer.valueOf(SwingConstants.CENTER));
		alignmentConverter.put(RIGHT, Integer.valueOf(SwingConstants.RIGHT));
	}

	/**
	 * Constructor
	 * 
	 * @param columnFormatInfo
	 *            ColumnFormatInfo with the formatting information about the column.
	 */
	public ColumnFormatPanel(ColumnFormatInfo columnFormatInfo) {
		this.formatInfo = columnFormatInfo;
		initialize();
		initGUIFromModel();
	} // ColumnFormatPanel()

	/**
	 * Return the ColumnFormatInfo that this panel represents.
	 * 
	 * @return ColumnFormatInfo
	 * @throws Exception
	 *             that gets passed up from saveGUIValuesToModel().
	 */
	public ColumnFormatInfo getColumnFormatInfo() throws Exception {
		saveGUIValuesToModel();
		return formatInfo;
	} // getColumnFormatInfo()

	/**
	 * Write this method to initialize the GUI from the value of the data object
	 */
	protected void initGUIFromModel() {
		fontPanel.setFontValue(formatInfo.font);

		switch (formatInfo.alignment) {
		case SwingConstants.LEFT:
			alignPanel.setValue(LEFT);
			break;
		case SwingConstants.CENTER:
			alignPanel.setValue(CENTER);
			break;
		default:
			alignPanel.setValue(RIGHT);
		} // switch(horizontalAlignment)

		foregroundPanel.setValue(formatInfo.foreground);
		backgroundPanel.setValue(formatInfo.background);

		widthPanel.setValue(formatInfo.width);

		if (formatPanel != null) {
			formatPanel.setFormat(formatInfo.getFormat());
		}
	} // initGUIFromModel()

	/**
	 * Build the GUI.
	 */
	protected void initialize() {
		// Populate the alignment values.
		String[] aligns = new String[alignmentConverter.size()];
		int i = 0;
		for (Enumeration e = alignmentConverter.keys(); e.hasMoreElements(); i++) {
			aligns[i] = (String) e.nextElement();
		}
		alignPanel = new StringChooserPanel("Horizontal Alignment", false, aligns);
		foregroundPanel = new ColorValuePanel("Text Color", false);
		backgroundPanel = new ColorValuePanel("Background Color", false);
		widthPanel = new IntegerValuePanel("Column Width", false, 0, Integer.MAX_VALUE);
		widthPanel.setToolTipText("Set column width in pixels");

		JPanel widthColorPanel = new JPanel();
		widthColorPanel.add(foregroundPanel);
		widthColorPanel.add(backgroundPanel);
		widthColorPanel.add(widthPanel);

		JPanel middlePanel = new JPanel();
		middlePanel.setLayout(new BoxLayout(middlePanel, BoxLayout.Y_AXIS));
		middlePanel.setBorder(BorderFactory.createEtchedBorder());
		middlePanel.add(alignPanel);
		middlePanel.add(widthColorPanel);

		setFormat(formatInfo.getFormat());

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(fontPanel);
		add(middlePanel);
		if (formatPanel != null) {
			// formatPanel.setBorder(BorderFactory.createEtchedBorder());
			add(formatPanel);
		}

	} // initialize()

	/**
	 * Write this method to store the info from the GUI in the data object
	 * 
	 * @throws Exception
	 */
	protected void saveGUIValuesToModel() throws java.lang.Exception {
		formatInfo.font = fontPanel.getFontValue();

		formatInfo.alignment = ((Integer) alignmentConverter.get(alignPanel.getValue())).intValue();
		if (formatPanel != null) {
			formatInfo.setFormat(formatPanel.getSelectedFormat());
		} else {
			formatInfo.setFormat(null);
		}

		formatInfo.foreground = foregroundPanel.getValue();
		formatInfo.background = backgroundPanel.getValue();

		formatInfo.width = widthPanel.getValue();
	} // saveGUIValuesToModel()

	/**
	 * Set a new text formatter to be displayed. Also change the GUI to reflect the new format.
	 * 
	 * @param newFormat
	 *            Format to use in the panel. Pass in null to show no text formatting information.
	 */
	public void setFormat(Format newFormat) {
		formatInfo.setFormat(newFormat);

		if (formatInfo.getFormat() != null) {
			if (formatPanel != null) {
				remove(formatPanel);
				formatPanel = null;
			}
			// Not sure what else to use here except 'instanceof'.
			if (formatInfo.getFormat() instanceof SimpleDateFormat) {
				// changed this to separate out date and time formats
				formatPanel = new DateFormatPanel((SimpleDateFormat) formatInfo.getFormat(),
						DateFormatPanel.EXAMPLE_DATE_FORMATS, DateFormatPanel.EXAMPLE_TIME_FORMATS, true);
			} else if (formatInfo.getFormat() instanceof SignificantDigitsFormat) {
				formatPanel = new DoubleFormatPanel(formatInfo.getFormat(), null, true, true, true);
			}

			if (formatPanel != null) {
				add(formatPanel);
			}
		} // if (formatInfo.format != null)
		else {
			if (formatPanel != null) {
				remove(formatPanel);
				formatPanel = null;
			}
		} // else

		validate();
	} // setFormat()

} // class ColumnFormatPanel

