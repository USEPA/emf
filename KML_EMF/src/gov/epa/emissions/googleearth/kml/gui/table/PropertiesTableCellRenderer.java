package gov.epa.emissions.googleearth.kml.gui.table;

import java.awt.Component;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

@SuppressWarnings("serial")
public class PropertiesTableCellRenderer extends DefaultTableCellRenderer {

	private JCheckBox checkBox;

	public PropertiesTableCellRenderer() {

		this.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
		this.checkBox = new JCheckBox();
		this.checkBox.setOpaque(false);
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {

		Component retVal = null;
		if (value instanceof File) {

			this.setText(((File) value).getAbsolutePath());
			retVal = this;
		} else if (value instanceof Boolean) {

			this.checkBox.setSelected((Boolean) value);
			retVal = this.checkBox;
		} else {

			this.setText(value.toString());
			retVal = this;
		}

		return retVal;
	}
}
