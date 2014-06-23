package gov.epa.emissions.framework.ui;

import gov.epa.emissions.commons.util.CustomDateFormat;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public abstract class AbstractTableData implements TableData {

    public AbstractTableData() {
        //no data
    }
    
    public static Date toDate(Timestamp timestamp) {
        long milliseconds = timestamp.getTime() + (timestamp.getNanos() / 1000000);
        return new java.util.Date(milliseconds);
    } 

    final protected String format(Date date) {
        return (date == null) ? "N/A" : CustomDateFormat.format_YYYY_MM_DD_HH_MM(date);
    }
    
    final protected String format(Timestamp timestamp) {
        return (timestamp == null) ? "N/A" : format(toDate(timestamp));
    }

    /**
     * sets the new value on the row + col. Override it, if any special handling is needed.
     */
    public void setValueAt(Object value, int row, int col) {
        if (!isEditable(col))
            return;

        List rows = rows();

        Row rowObj = (Row) rows.get(row);
        rowObj.setValueAt(value, col);
    }

    public Object element(int row) {
        Row rowObj = (Row) rows().get(row);
        return rowObj.source();
    }

    public List elements(int[] rows) {
        List datasets = new ArrayList();
        for (int i = 0; i < rows.length; i++) {
            datasets.add(element(rows[i]));
        }

        return datasets;
    }
}
