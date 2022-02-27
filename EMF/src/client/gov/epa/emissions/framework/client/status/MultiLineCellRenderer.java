package gov.epa.emissions.framework.client.status;

import javax.swing.JLabel;
import javax.swing.table.DefaultTableCellRenderer;

public class MultiLineCellRenderer extends DefaultTableCellRenderer {

	public MultiLineCellRenderer() {
		super();
		setVerticalAlignment(JLabel.TOP);
	}
}