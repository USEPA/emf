package gov.epa.emissions.commons.gui;

import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

public class EditableTable extends JTable implements Editor, Changeable {

    private Changeables listOfChangeables;

    private boolean changed = false;

    private EditableTableModel tableModel;

    public EditableTable(EditableTableModel tableModel) {
        super(tableModel);
        this.tableModel = tableModel;
        setRowHeight(25);
        getModel().addTableModelListener(tableModelListener());
    }

    private TableModelListener tableModelListener() {
        return new TableModelListener() {
            public void tableChanged(TableModelEvent e) {
                if (tableModel.shouldTrackChange(e.getColumn()))
                    notifyChanges();
            }

        };
    }

    public void setValueAt(Object value, int row, int column) {
        Object original = super.getValueAt(row, column);
        if (original != null && original.equals(value))// ignore, if value is unchanged
            return;

        super.setValueAt(value, row, column);
        if (tableModel.shouldTrackChange(column))
            notifyChanges();
    }

    public void commit() {
        if (isEditing()) {
            getCellEditor().stopCellEditing();
        }
    }

    public void clear() {
        this.changed = false;
    }

    void notifyChanges() {
        this.changed = true;
        this.listOfChangeables.onChanges();
    }

    public boolean hasChanges() {
        return this.changed;
    }

    public void observe(Changeables list) {
        this.listOfChangeables = list;
    }

}
