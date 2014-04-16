package gov.epa.mims.analysisengine.table.format;


import java.awt.Color;
import java.awt.Font;
import java.text.Format;

import javax.swing.JLabel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

/**
 * <p>
 * Description: A struct that holds all of the formatting information for a TableColumn. All fields are public.
 * </p>
 * <p>
 * Copyright: Copyright (c) 2003
 * </p>
 * <p>
 * Company: UNC - CEP
 * </p>
 * 
 * @author Daniel Gatti
 * @version $Id: ColumnFormatInfo.java,v 1.1 2006/11/01 15:33:38 parthee Exp $
 */
public class ColumnFormatInfo implements java.io.Serializable {
	/** serial version UID */
	static final long serialVersionUID = 1;

	/** The font for the column. */
	public Font font = Font.getFont("dialog");

	/**
	 * The horizontal alignment for the column. Must be one of JLabel.LEFT, JLabel.CENTER, JLabel.RIGHT.
	 */
	public int alignment = JLabel.CENTER;

	/** The width of the column. */
	public int width = 75;

	/** The text format for the column. */
	private Format format = FormattedCellRenderer.nullFormatter;

	/** The foreground color for the column. */
	public Color foreground = Color.black;

	/** The background color for the column. */
	public Color background = Color.white;

	/**
	 * Constructor.
	 */
	public ColumnFormatInfo(Font font, int alignment, int width, Format format, Color foreground, Color background) {
		this.font = font;
		this.alignment = alignment;
		this.width = width;
		this.format = format;
		this.foreground = foreground;
		this.background = background;
	} // ColumnFormatInfo()

	/**
	 * Constructor that takes a TableColumn and populates the data fields.
	 * 
	 * @param column
	 *            TableColumn to use to populate this object.
	 */
	public ColumnFormatInfo(TableColumn column) {
		TableCellRenderer renderer = column.getCellRenderer();
		width = column.getWidth();

		// If the renderer is null, then we can't do z`anything else. So we just
		// return with the fields set to their defaults.
		if (renderer == null) {
			return;
		} // if (renderer == null)

		// Get the properties that almost all renderers have. They are usually
		// JLabels.
		if (renderer instanceof JLabel) {
			JLabel label = (JLabel) renderer;
			font = label.getFont();
			alignment = label.getHorizontalAlignment();
			foreground = label.getForeground();
			background = label.getBackground();
		} // if (renderer instanceof JLabel)

		// Get properties that only our FormattedCellRenderers have.
		if (renderer instanceof HasFormatter) {
			HasFormatter hasFormatter = (HasFormatter) renderer;
			format = hasFormatter.getFormat();
		} // if (renderer instanceof HasFormatter)
	} // ColumnFormatInfo()

	/**
	 * set a new format object
	 * 
	 * @param newFormat
	 */
	public void setFormat(Format newFormat) {
		this.format = newFormat;
	}// setFormat()

	/**
	 * returns the format for this info object
	 * 
	 * @return
	 */
	public Format getFormat() {
		return format;
	}
} // class ColumnFormatInfo

