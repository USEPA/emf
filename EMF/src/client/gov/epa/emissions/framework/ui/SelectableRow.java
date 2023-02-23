package gov.epa.emissions.framework.ui;

import java.util.HashMap;
import java.util.Map;

public class SelectableRow implements Row {
    private Map columns;

    private Object source;

    private RowSource rowSource;

    private Boolean selected;

    public SelectableRow(Object source, Object[] values) {
        this.source = source;
        selected = Boolean.FALSE;

        columns = createCols(values);
    }

    public SelectableRow(RowSource rowSource) {
        this(rowSource.source(), rowSource.values());
        this.rowSource = rowSource;
    }

    private Map createCols(Object[] values) {
        Map columns = new HashMap();
        columns.put(Integer.valueOf(0), new Column(selected));// select col
        for (int i = 0; i < values.length; i++) {
            columns.put(Integer.valueOf(i + 1), new Column(values[i]));
        }

        return columns;
    }

    public Object getValueAt(int column) {
        Column columnHolder = (Column) columns.get(Integer.valueOf(column));
        return columnHolder.value;
    }

    private class Column {

        private Object value;

        public Column(Object value) {
            this.value = value;
        }

    }

    public Object source() {
        return source;
    }

    public RowSource rowSource() {
        return rowSource;
    }

    public void setValueAt(Object value, int column) {
        switch (column) {
        case 0:
            selected = (Boolean) value;
            break;
        default:
            throw new RuntimeException("cannot edit column - " + column);
        }
    }

    public boolean isSelected() {
        return selected.booleanValue();
    }

}
