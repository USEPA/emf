package gov.epa.emissions.framework.client.data.editor;

import gov.epa.emissions.framework.ui.MessagePanel;

import java.awt.Component;

import javax.swing.DefaultCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;

public class NumericTableCellEditor extends DefaultCellEditor {

    private MessagePanel messagePanel;

    private Class editedClass;

    private Object value;

    private Object oldValue;

    public NumericTableCellEditor(MessagePanel messagePanel) {
        super(new JTextField());
        this.messagePanel = messagePanel;
    }

    public Object getCellEditorValue() {
        return value;
    }

    public boolean stopCellEditing() {
        Object value = super.getCellEditorValue();
        this.value = validate(value);
        return super.stopCellEditing();

    }

    private Object validate(Object value) {
        Object updatedValue = null;
        if (editedClass.equals(Integer.class)) {
            updatedValue = validateInteger(value);
        }
        if (editedClass.equals(Double.class) || editedClass.equals(Float.class)) {
            updatedValue = validateRealValue(value);
        }
        return updatedValue;
    }

    private Object validateRealValue(Object value) {
        try {
            Double.valueOf(value.toString());
            return value;
        } catch (NumberFormatException e) {
            messagePanel.setError("Please enter a real number");
            return oldValue;
        }

    }

    private Object validateInteger(Object value) {
        try {
            Integer.valueOf(value.toString());
            return value;
        } catch (NumberFormatException e) {
            messagePanel.setError("Please enter a integer number");
            return oldValue;
        }

    }

    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        messagePanel.clear();
        this.editedClass = table.getColumnClass(column);
        this.oldValue = value;
        return super.getTableCellEditorComponent(table, value, isSelected, row, column);
    }

}
