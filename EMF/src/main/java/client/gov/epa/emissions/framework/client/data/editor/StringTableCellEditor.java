package gov.epa.emissions.framework.client.data.editor;

import java.awt.Component;

import gov.epa.emissions.commons.io.ColumnMetaData;
import gov.epa.emissions.commons.io.TableMetadata;
import gov.epa.emissions.framework.ui.MessagePanel;

import javax.swing.DefaultCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;

public class StringTableCellEditor extends DefaultCellEditor {

    private TableMetadata tableMetadata;
    
    private MessagePanel messagePanel;

    private String columnName;

    private Object oldValue;

    private Object value;

    public StringTableCellEditor(TableMetadata tableMetadata, MessagePanel messagePanel) {
        super(new JTextField());
        this.tableMetadata = tableMetadata;
        this.messagePanel = messagePanel;
    }
    
    public Object getCellEditorValue() {
       return value;
    }

    public boolean stopCellEditing() {
        Object value = super.getCellEditorValue();
        this.value = validate((String) value);
        return super.stopCellEditing();
    }
    
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        messagePanel.clear();
        this.columnName = table.getColumnName(column);
        this.oldValue = value;
        return super.getTableCellEditorComponent(table, value, isSelected, row, column);
    }
    
    private Object validate(String value) {
        ColumnMetaData metadata = tableMetadata.columnMetadata(columnName);
        if (metadata == null) {
            return value;
        }
        if (!sizeCheck(value, metadata)) {
            messagePanel.setError("Enter a value no longer than " + metadata.getSize()+" characters");
            return oldValue;
        }
        return value;

    }

    private boolean sizeCheck(String value, ColumnMetaData metadata) {
        int dbColumnSize = metadata.getSize();
        // -1=> no size constraints
        return (dbColumnSize == -1 || value == null || dbColumnSize >= value.length());
    }
    

}
