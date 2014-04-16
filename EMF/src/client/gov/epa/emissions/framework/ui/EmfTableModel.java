package gov.epa.emissions.framework.ui;

import gov.epa.emissions.commons.gui.RefreshableTableModel;
import gov.epa.emissions.commons.gui.TableHeader;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

public class EmfTableModel extends AbstractTableModel implements RefreshableTableModel {

    private TableHeader header;

    private List rows;

    protected TableData tableData;

    public EmfTableModel(TableData tableData) {
        refresh(tableData);
    }

    public int getRowCount() {
        if ( rows != null) {
            return rows.size();
        }
        return 0;
    }

    public String getColumnName(int index) {
        if ( header != null) {
            return header.columnName(index);
        }
        return "";
    }

    public int getColumnCount() {
        if ( header != null) {
            return header.columnsSize();
        }
        return 0;
    }

    public Object getValueAt(int row, int column) {
        if ( rows != null && rows.get(row) != null) {
            return ((Row) rows.get(row)).getValueAt(column);
        }
        return null;
    }

    public void refresh() {
        //if ( tableData != null) {
            this.rows = tableData.rows();
            this.header = new TableHeader(tableData.columns());
        //} 
        super.fireTableDataChanged();
    }

    public void refresh(TableData tableData) {
        //if ( tableData != null) {
            this.tableData = tableData;
        //}
        refresh();
    }

    public boolean isCellEditable(int row, int col) {
        if ( tableData != null) {
            return tableData.isEditable(col);
        }
        return false;
    }

    public Object element(int row) {
        if ( tableData != null) {
            return tableData.element(row);
        }
        return false;
    }

    public List elements(int[] selected) {
        if ( tableData != null) {
            return tableData.elements(selected);
        }
        return null;
    }

    public Class getColumnClass(int col) {
        if ( tableData != null) {
            return tableData.getColumnClass(col);
        }
        return null;
    }

    public void setValueAt(Object value, int row, int col) {
        if ( tableData != null) { 
            if (isCellEditable(row, col))            
                tableData.setValueAt(value, row, col);
        }
    }
}
