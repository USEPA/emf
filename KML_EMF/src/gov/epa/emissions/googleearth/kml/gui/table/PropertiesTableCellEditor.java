package gov.epa.emissions.googleearth.kml.gui.table;

import java.awt.Component;

import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableModel;

@SuppressWarnings("serial")
public class PropertiesTableCellEditor extends AbstractCellEditor implements
		TableCellEditor {

	// private FileEditor fileEditor;
	// private DefaultCellEditor booleanEditor;
	private StringCellEditor stringEditor;
	private Object value;

	public PropertiesTableCellEditor(TableModel model) {

		// this.fileEditor = new FileEditor(model);
		this.stringEditor = new StringCellEditor(model);
		// this.booleanEditor = new DefaultCellEditor(new JCheckBox());
	}

	@Override
	public Object getCellEditorValue() {

		// if (this.value instanceof File) {
		// return this.fileEditor.getCellEditorValue();
		// } else
		// if (this.value instanceof Boolean) {
		// return this.booleanEditor.getCellEditorValue();
		// } else {
		return this.stringEditor.getCellEditorValue();
		// }
	}

	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean isSelected, int row, int column) {

		this.value = value;

		// if (value instanceof File) {
		// return this.fileEditor.getTableCellEditorComponent(table, value,
		// isSelected, row, column);
		// } else

		// if (value instanceof Boolean) {
		// return this.booleanEditor.getTableCellEditorComponent(table, value,
		// isSelected, row, column);
		// } else {
		return this.stringEditor.getTableCellEditorComponent(table, value,
				isSelected, row, column);
		// }
	}
}