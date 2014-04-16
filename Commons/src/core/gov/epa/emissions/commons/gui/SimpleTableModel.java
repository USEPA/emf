package gov.epa.emissions.commons.gui;

import gov.epa.mims.analysisengine.table.MultiRowHeaderTableModel;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * A simple table model with a header. Uses the GOF's <pattern>Decorator</pattern> pattern. Delegates all behavior
 * (except selectable) to the underlying delegate model
 * </p>
 */
public class SimpleTableModel extends MultiRowHeaderTableModel {

    private RefreshableTableModel delegate;

    public SimpleTableModel(RefreshableTableModel delegate) {
        this.delegate = delegate;

        setColumnHeaders(getDelegateColumnNames());
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return delegate.isCellEditable(rowIndex, columnIndex);
    }

    void setColumnHeaders(String[] columnNames) {
        String[][] columnHeaders = new String[1][];
        columnHeaders[0] = columnNames;

        super.columnHeaders = transposeArray(columnHeaders);
    }

    public int getColumnCount() {
        return delegate.getColumnCount();
    }

    public String getColumnName(int col) {
        return delegate.getColumnName(col);
    }

    public int getRowCount() {
        return delegate.getRowCount();
    }

    public Object getValueAt(int row, int col) {
        return delegate.getValueAt(row, col);
    }

    public int getBaseModelRowIndex(int rowIndex) {
        return rowIndex;
    }

    String[] getDelegateColumnNames() {
        List names = new ArrayList();
        for (int i = 0; i < delegate.getColumnCount(); i++) {
            names.add(delegate.getColumnName(i));
        }

        return (String[]) names.toArray(new String[0]);
    }

    public void refresh() {
        delegate.refresh();
    }

    public void setValueAt(Object value, int row, int col) {
        delegate.setValueAt(value, row, col);
    }
}
