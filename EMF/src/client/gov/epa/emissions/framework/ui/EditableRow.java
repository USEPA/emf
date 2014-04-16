package gov.epa.emissions.framework.ui;

import gov.epa.emissions.framework.services.EmfException;

import java.util.HashMap;
import java.util.Map;

public class EditableRow implements Row {
    private Map columns;

    private RowSource rowSource;

    public EditableRow(RowSource rowSource) {
        this.rowSource = rowSource;
        columns(rowSource);
    }

    private void columns(RowSource source) {
        columns = new HashMap();

        Object[] values = source.values();
        for (int i = 0; i < values.length; i++) {
            columns.put(new Integer(i), new Column(values[i]));
        }
    }

    public Object getValueAt(int column) {
        Column columnHolder = (Column) columns.get(new Integer(column));
        return columnHolder.value;
    }

    private class Column {
        private Object value;

        public Column(Object value) {
            this.value = value;
        }
    }

    public Object source() {
        return rowSource.source();
    }

    public RowSource rowSource() {
        return rowSource;
    }

    public void setValueAt(Object val, int column) {
        rowSource.setValueAt(column, val);
        columns(rowSource);
    }
    
    public void validate(int rowNumber) throws EmfException{
        rowSource.validate(rowNumber);
    }

}
