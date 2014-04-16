package gov.epa.emissions.googleearth.kml.gui.table;

import gov.epa.emissions.googleearth.kml.PropertiesManager;
import gov.epa.emissions.googleearth.kml.PropertiesManager.PropertyKey;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.table.DefaultTableModel;

@SuppressWarnings("serial")
public class PropertiesTableModel extends DefaultTableModel {

	private List<String> keys;
	private List<Object> values;
	private PropertiesManager propertiesManager;

	private static final String[] COLUMN_NAMES = { "Properties", "Values" };

	public PropertiesTableModel(PropertiesManager propertiesManager) {

		this.propertiesManager = propertiesManager;
		this.keys = new ArrayList<String>();
		this.values = new ArrayList<Object>();
		for (String key : propertiesManager.getKeys()) {

			this.keys.add(key);

			if (File.class.equals(PropertyKey.getType(key))) {
				this.values.add(new File(propertiesManager.getValue(key)));
			} else if (Boolean.class.equals(PropertyKey.getType(key))) {
				this.values.add(new Boolean(propertiesManager.getValue(key)));
			} else {
				this.values.add(propertiesManager.getValue(key).toString());
			}
		}
	}

	@Override
	public int getColumnCount() {
		return PropertiesTableModel.COLUMN_NAMES.length;
	}

	@Override
	public String getColumnName(int column) {
		return PropertiesTableModel.COLUMN_NAMES[column];
	}

	@Override
	public int getRowCount() {

		int count = 0;

		if (this.keys != null && this.values != null) {

			assert this.keys.size() == this.values.size();
			count = this.keys.size();
		}

		return count;
	}

	@Override
	public Object getValueAt(int row, int column) {

		assert column < PropertiesTableModel.COLUMN_NAMES.length;

		Object value = "";
		if (column == 0) {
			value = this.keys.get(row);
		} else {
			value = this.values.get(row);
		}

		return value;
	}

	@Override
	public boolean isCellEditable(int row, int column) {

		assert column < PropertiesTableModel.COLUMN_NAMES.length;

		return column == 1;
	}

	@Override
	public void setValueAt(Object value, int row, int column) {

		assert column < PropertiesTableModel.COLUMN_NAMES.length;

		if (column == 0) {
			this.keys.set(row, value.toString());
		} else {

			this.values.set(row, value);

			if (value instanceof File) {
				this.propertiesManager.setValue(this.keys.get(row),
						((File) value).getAbsolutePath());
			} else {
				this.propertiesManager.setValue(this.keys.get(row), value
						.toString());
			}
		}
	}

	public PropertiesManager getPropertiesManager() {
		return propertiesManager;
	}
}
