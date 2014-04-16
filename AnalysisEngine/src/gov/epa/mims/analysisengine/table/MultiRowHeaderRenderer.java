package gov.epa.mims.analysisengine.table;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.table.TableCellRenderer;

/**
 * 
 * This renderer provides a header with multiples rows. At construction time,
 * 
 * set the header values. The class will figure out the number of rows to use
 * 
 * based on the size of the String[] array passed in.
 * 
 * @author Daniel Gatti
 * 
 * @version $Id: MultiRowHeaderRenderer.java,v 1.3 2006/10/30 21:43:50 parthee Exp $
 * 
 */

public class MultiRowHeaderRenderer extends JPanel implements TableCellRenderer

{

	/** The number of rows to display. */

	protected int numRows = 1;

	/** The labels that render the values. */

	protected JLabel[] labels = null;

	/** The color to use for the background when the cell is selected. */

	protected Color headerBackgroundColor = UIManager.getDefaults().getColor(

	"TableHeader.background");

	/**
	 * 
	 * Constructor.
	 * 
	 * @param values
	 *            String[] that is the list of values to display in the header.
	 * 
	 */

	public MultiRowHeaderRenderer(String[] values) {
		if (values == null) {
			values = new String[] { "Row" };
		}
		this.numRows = values.length;
		labels = new JLabel[numRows];
		// Use a GridLayout to place the cells in a column, one below the next.
		setLayout(new GridLayout(numRows, 1));
		Font font = null;
		for (int i = 0; i < numRows; i++) {
			labels[i] = new JLabel();
			labels[i].setBorder(BorderFactory.createRaisedBevelBorder());
			if (font == null) {
				font = labels[i].getFont();
				font = new Font(font.getName(), Font.BOLD, font.getSize());
			}
			setFont(font);
			labels[i].setHorizontalAlignment(SwingConstants.CENTER);
			labels[i].setText(values[i]);
			labels[i].setOpaque(true);
			add(labels[i]);
		} // for(i)
	} // MultiRowHeaderRenderer()

	/**
	 * 
	 * Since all of the setup was done in the constructor, just return the panel.
	 * 
	 */

	public Component getTableCellRendererComponent(JTable table, Object value,

	boolean isSelected, boolean hasFocus, int row, int column)
	{
		Color background = ((isSelected) ? headerBackgroundColor : Color.lightGray);
		for (int i = 0; i < labels.length; i++)
		{
			labels[i].setBackground(background);
		}
		return this;
	}
}

