package gov.epa.emissions.framework.client.status;

import gov.epa.emissions.commons.gui.TableHeader;
import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.services.basic.Status;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

public class StatusTableModel extends AbstractTableModel {

    private List statusList;

    private TableHeader header;

    private List rows;

    public StatusTableModel() {
        this.header = new TableHeader(new String[] { "Message Type", "Message", "Timestamp" });
        this.statusList = new ArrayList();

        this.rows = new ArrayList();
     }

    public int getRowCount() {
        return rows.size();
    }

    public int getColumnCount() {
        return header.columnsSize();
    }

    public String getColumnName(int i) {
        return header.columnName(i);
    }

    public Object getValueAt(int row, int column) {
        if ((rows.size() - 1 < row) || row < 0 || (column > header.columnsSize() - 1) || column < 0)
            return null;

        return ((Row) rows.get(row)).getValueAt(column);
    }

    public void clear() {
        this.statusList.clear();
        this.rows.clear();

        notifyTableUpdated();
    }

    private void notifyTableUpdated() {
        super.fireTableDataChanged();
    }

    public void refresh(Status[] statuses) {
        this.statusList.addAll(Arrays.asList(statuses));
        sortDescendingByTimestamp(statusList);

        rows.clear();
        for (Iterator iter = statusList.iterator(); iter.hasNext();) {
            Row row = new Row(((Status) iter.next()));
            rows.add(row);
        }

        notifyTableUpdated();
    }

    private void sortDescendingByTimestamp(List statusList) {
        Collections.sort(statusList, new Comparator() {
            public int compare(Object item1, Object item2) {
                Status status1 = (Status) item1;
                Status status2 = (Status) item2;
                long time1 = status1.getTimestamp().getTime();
                long time2 = status2.getTimestamp().getTime();

                return time1 < time2 ? 1 : -1;// sort descending
            }
        });
    }

    private class Column {

        private Object value;

        public Column(Object value) {
            this.value = value;
        }

    }

    private class Row {
        private Map columns;

        public Row(Status status) {
            Column messageType = new Column(status.getType());
            Column message = new Column(status.getMessage());
            Column timestamp = new Column(CustomDateFormat.format_YYYY_MM_DD_HH_MM(status.getTimestamp()));

            columns = new HashMap();
            columns.put(new Integer(0), messageType);
            columns.put(new Integer(1), message);
            columns.put(new Integer(2), timestamp);
        }

        public Object getValueAt(int column) {
            Column columnHolder = (Column) columns.get(new Integer(column));
            return columnHolder.value;
        }
    }

}
