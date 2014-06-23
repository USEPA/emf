package gov.epa.emissions.framework.ui;

import java.util.HashMap;
import java.util.Map;

public class ViewableRow<T> implements Row<T> {
    private Map<Integer, Column> columns;

    private T source;

    private RowSource<T> rowSource;

    public ViewableRow(T source, Object[] values) {
        this.source = source;

        columns = new HashMap<Integer, Column>();
        for (int i = 0; i < values.length; i++) {
            columns.put(new Integer(i), new Column(values[i]));
        }
    }

    public ViewableRow(RowSource<T> rowSource) {
        this(rowSource.source(), rowSource.values());
        this.rowSource = rowSource;
    }

    public Object getValueAt(int column) {
        Column columnHolder = columns.get(new Integer(column));
        return columnHolder.value;
    }

    private class Column {

        private Object value;

        public Column(Object value) {
            this.value = value;
        }

    }

    public T source() {
        return source;
    }

    public RowSource<T> rowSource() {
        return rowSource;
    }

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof ViewableRow))
            return false;
        ViewableRow<T> other = (ViewableRow<T>) obj;
        return source.equals(other.source);

    }

    public int hashCode() {
        return source.hashCode();
    }

    public void setValueAt(Object value, int column) {
        // No Op
    }

}
