package gov.epa.emissions.framework.client.download;

import gov.epa.emissions.commons.gui.TableHeader;
import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.services.basic.FileDownload;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

public class FileDownloadTableModel extends AbstractTableModel {

    private List fileDownloadList;

    private TableHeader header;

    private List rows;

    public FileDownloadTableModel() {
        this.header = new TableHeader(new String[] { "Downloads" });
        this.fileDownloadList = new ArrayList();

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

    public Object getSource(int row) {
        if ((rows.size() - 1 < row) || row < 0)
            return null;

        return ((Row) rows.get(row)).getValue();
    }

    public Object getValueAt(int row, int column) {
        if ((rows.size() - 1 < row) || row < 0 || (column > header.columnsSize() - 1) || column < 0)
            return null;

        return ((Row) rows.get(row)).getValueAt(column);
    }

    public void setValueAt(Object fileDownload, int row, int column) {
        
        if ((rows.size() - 1 < row) || row < 0 || (column > header.columnsSize() - 1) || column < 0)
            return;

        ((Row) rows.get(row)).setValueAt((FileDownload)fileDownload);
    }

    public void clear() {
        this.fileDownloadList.clear();
        this.rows.clear();

        notifyTableUpdated();
    }

    private void notifyTableUpdated() {
        super.fireTableDataChanged();
    }

    public void refresh(FileDownload[] statuses) {
        this.fileDownloadList.addAll(Arrays.asList(statuses));
        sortDescendingByTimestamp(fileDownloadList);

        rows.clear();
        for (Iterator iter = fileDownloadList.iterator(); iter.hasNext();) {
            Row row = new Row(((FileDownload) iter.next()));
            rows.add(row);
        }

        notifyTableUpdated();
    }

    private void sortDescendingByTimestamp(List statusList) {
        Collections.sort(statusList, new Comparator() {
            public int compare(Object item1, Object item2) {
                FileDownload status1 = (FileDownload) item1;
                FileDownload status2 = (FileDownload) item2;
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
        private FileDownload fileDownload;
        
        public Row(FileDownload fileDownload) {
            this.fileDownload = fileDownload;
            Column progress = new Column(this.fileDownload);

            columns = new HashMap();
            columns.put(new Integer(0), progress);
        }

        public Object getValue() {
            return this.fileDownload;
        }

        public void setValueAt(FileDownload fileDownload) {
            this.fileDownload = fileDownload;
        }

        public Object getValueAt(int column) {
//            Column columnHolder = (Column) columns.get(new Integer(column));
            return this.fileDownload;
        }
    }

}
