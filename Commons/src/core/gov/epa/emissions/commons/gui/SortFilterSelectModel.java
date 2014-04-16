package gov.epa.emissions.commons.gui;

import gov.epa.mims.analysisengine.table.MultiRowHeaderTableModel;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.apache.commons.collections.primitives.IntList;

/**
 * <p>
 * A table that adds the 'selectable' behavior to a MultiRowHeaderTableModel. Uses the GOF's <pattern>Decorator</pattern>
 * pattern. Delegates all behavior (except selectable) to the underlying delegate model
 * </p>
 */
public class SortFilterSelectModel extends MultiRowHeaderTableModel implements SelectModel {

    private static final String SELECT_COL_NAME = "Select";

    private Boolean[] selects;

    private RefreshableTableModel delegate;

    public SortFilterSelectModel(RefreshableTableModel delegate) {
        this.delegate = delegate;

        resetSelections();

        setColumnHeaders(getDelegateColumnNames());
        //setSelectClass();
        
    }

    public Class<?> getColumnClass(int col) {
        if (col == 0)
            return Boolean.class;

        return delegate.getColumnClass(col - 1);

    }

    private void resetSelections() {
        this.selects = new Boolean[getRowCount()];
        if (getRowCount() == 1) {
            selects[0] = Boolean.TRUE;
        }else {
            for (int i = 0; i < getRowCount(); i++) {
                selects[i] = Boolean.FALSE;
            }
        }
    }

    private void resetSelections(int[] selections) {
        this.selects = new Boolean[getRowCount()];
        for (int i = 0; i < getRowCount(); i++) {
            selects[i] = Boolean.FALSE;
        }

        for (int j = 0; j < selections.length; j++) {
            selects[selections[j]] = Boolean.TRUE;
        }
    }

    void setColumnHeaders(String[] columnNames) {
        String[][] columnHeaders = new String[1][];

        String[] firstColumnHeaderRow = new String[columnNames.length + 1];
        firstColumnHeaderRow[0] = SELECT_COL_NAME;
        System.arraycopy(columnNames, 0, firstColumnHeaderRow, 1, columnNames.length);

        columnHeaders[0] = firstColumnHeaderRow;

        // contains - Select + delegate columns
        super.columnHeaders = transposeArray(columnHeaders);

        super.columnRowHeaders = new String[1];
        super.columnRowHeaders[0] = "#";
    }

    public int getColumnCount() {
        return 1 + delegate.getColumnCount();
    }

    public String getColumnName(int col) {
        if (col == 0)
            return SELECT_COL_NAME;

        return delegate.getColumnName(col - 1);// minus the Select col
    }

    public int getRowCount() {
        return delegate.getRowCount();
    }

    public Object getValueAt(int row, int col) {
        if (col == findColumn(SELECT_COL_NAME)) {
            return selects[row];
        }

        return delegate.getValueAt(row, col - 1);
    }

    public int getBaseModelRowIndex(int rowIndex) {
        return rowIndex;
    }

    public boolean isCellEditable(int row, int col) {
        if (col == findColumn(SELECT_COL_NAME))
            return true;

        return delegate.isCellEditable(row, col);
    }

    public void setValueAt(Object value, int row, int col) {
        if (col == findColumn(SELECT_COL_NAME)) {
            selects[row] = (Boolean) value;
            fireTableCellUpdated(row, col);
        }
    }

    public String getSelectableColumnName() {
        return SELECT_COL_NAME;
    }

    String[] getDelegateColumnNames() {
        List<String> names = new ArrayList<String>();
        for (int i = 0; i < delegate.getColumnCount(); i++) {
            names.add(delegate.getColumnName(i));
        }

        return names.toArray(new String[0]);
    }

    public int getSelectedCount(int[] selected) {
        return selected(selected).size();
    }

    public int[] getSelectedIndexes() {
        IntList indexes = new ArrayIntList();

        for (int i = 0; i < selects.length; i++) {
            if (selects[i].equals(Boolean.TRUE))
                indexes.add(i);
        }

        return indexes.toArray();
    }

    public void refresh() {
        delegate.refresh();
        resetSelections(getSelectedIndexes());
    }
    
    public void refresh(RefreshableTableModel delegate){
        this.delegate = delegate; 
        resetSelections();
    }

    public List<?> selected() {
        int[] selected = getSelectedIndexes();
        return delegate.elements(selected);
    }

    public List<?> selected(int[] selected) {
        return delegate.elements(selected);
    }

    //FIXME: remove this method & after propagating SelectableSortFilterWrapper to all the Managers
    public int getSelectedCount() {
        return getSelectedIndexes().length;
    }
}
