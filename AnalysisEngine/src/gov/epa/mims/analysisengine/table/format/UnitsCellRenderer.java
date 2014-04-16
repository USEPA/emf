package gov.epa.mims.analysisengine.table.format;


import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.text.Format;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 * A table cell renderer that formats the units row look like the column header. 'this' is returnedfor most cells.
 * 'topLabel' is returned for cells in the column header and units area. It can also accept a Renderer to use for the
 * rest of the cells in the table.
 * 
 * @author Daniel Gatti
 * 
 */

public class UnitsCellRenderer extends FormattedCellRenderer implements TableCellRenderer, HasFormatter {
	/** The renderer component for the units row. */
	protected JLabel topLabel = new JLabel();

	/**
	 * renderer that is used to render the cells other than the units cell. Could be null in which case we will use our
	 * own renderer.
	 * 
	 */

	public UnitsCellRenderer(Format format) {
		super(format, JLabel.CENTER);
		topLabel.setOpaque(true);
		setFont(new Font(getFont().getName(), Font.PLAIN, getFont().getSize()));
		// Set up the bold font for the headers as unchanging.
		topLabel.setFont(new Font(getFont().getName(), Font.BOLD, getFont().getSize()));
		topLabel.setBorder(BorderFactory.createRaisedBevelBorder());
		topLabel.setBackground(Color.lightGray);

	} // UnitsCellRenderer()

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		// Units renderer.
		if (row <= 0) {
			if (value != null) {
				topLabel.setText(value.toString());
			}
			return topLabel;
		}
		return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
	} // getTableCellRendererComponent()
} // class UnitsCellRenderer

