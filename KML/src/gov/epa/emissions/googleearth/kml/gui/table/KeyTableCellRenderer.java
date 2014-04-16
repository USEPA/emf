package gov.epa.emissions.googleearth.kml.gui.table;

import gov.epa.emissions.googleearth.kml.PropertiesManager.PropertyKey;

import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

@SuppressWarnings("serial")
public class KeyTableCellRenderer extends DefaultTableCellRenderer {

	public KeyTableCellRenderer() {
		this.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {

		PropertyKey propertKey = PropertyKey.getPropertKey(value.toString());

		if (propertKey != null) {
			this.setText(propertKey.getDisplayName());
		} else {
			throw new RuntimeException("Unable to find key: " + value);
		}

		return this;
	}
}
