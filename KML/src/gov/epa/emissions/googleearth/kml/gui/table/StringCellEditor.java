package gov.epa.emissions.googleearth.kml.gui.table;

import java.awt.Component;

import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableModel;

@SuppressWarnings("serial")
public class StringCellEditor extends AbstractCellEditor implements
		TableCellEditor {

	private JTextField textField;
	private TableModel model;
	private int row = -1;
	private int col = -1;

	public StringCellEditor(TableModel model) {
		this.model = model;
	}

	private void updateModel() {
		this.model.setValueAt(getCellEditorValue(), this.row, this.col);
	}

	public Object getCellEditorValue() {
		return this.textField.getText();
	}

	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean isSelected, int row, int column) {

		this.row = row;
		this.col = column;

		this.textField = new JTextField(value.toString());
		this.textField.getDocument().addDocumentListener(
				new DocumentListener() {

					@Override
					public void changedUpdate(DocumentEvent e) {
						StringCellEditor.this.updateModel();
					}

					@Override
					public void insertUpdate(DocumentEvent e) {
						StringCellEditor.this.updateModel();
					}

					@Override
					public void removeUpdate(DocumentEvent e) {
						StringCellEditor.this.updateModel();
					}
				});

		return this.textField;
	}
}
