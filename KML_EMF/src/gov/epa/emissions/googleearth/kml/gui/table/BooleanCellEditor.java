package gov.epa.emissions.googleearth.kml.gui.table;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableModel;

@SuppressWarnings("serial")
public class BooleanCellEditor extends AbstractCellEditor implements
		TableCellEditor {

	private JCheckBox checkBox;
	private TableModel model;
	private int row = -1;
	private int col = -1;

	public BooleanCellEditor(TableModel model) {

		this.model = model;
		this.checkBox = new JCheckBox();
		this.checkBox.setOpaque(false);
		this.checkBox.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				BooleanCellEditor.this.updateModel();
			}
		});
	}

	private void updateModel() {
		this.model.setValueAt(getCellEditorValue(), this.row, this.col);
	}

	public Object getCellEditorValue() {
		return this.checkBox.isSelected();
	}

	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean isSelected, int row, int column) {

		this.row = row;
		this.col = column;

		this.checkBox.setSelected((Boolean) value);

		return this.checkBox;
	}
}
