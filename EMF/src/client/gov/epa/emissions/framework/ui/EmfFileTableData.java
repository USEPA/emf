package gov.epa.emissions.framework.ui;

import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.services.basic.EmfFileInfo;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class EmfFileTableData extends AbstractTableData {
    private List<Row> rows;

    public EmfFileTableData(EmfFileInfo[] files) {
        this.rows = createRows(files);
    }

    public String[] columns() {
        return new String[] { "Name", "Date Modified", "Size", "Type", "Can Read", "Can Write", "Can Execute"};
    }

    public Class getColumnClass(int col) {
        if (col > 3)
            return Boolean.class;
        
        return String.class;
    }

    public List<Row> rows() {
        return rows;
    }

    public boolean isEditable(int col) {
        return false;
    }
    
    public void add(EmfFileInfo file) {
        rows.add(row(file));
    }
    
    private Row row(EmfFileInfo info) {
        return new ViewableRow(info, rowValues(info));
    }


    private List<Row> createRows(EmfFileInfo[] files) {
        List<Row> rows = new ArrayList<Row>();

        for (int i = 0; i < files.length; i++) 
            rows.add(row(files[i]));

        return rows;
    }
    
    public void refresh() {
        this.rows = createRows(sources());
    }
    
    public EmfFileInfo[] sources() {
        List<EmfFileInfo> sources = sourcesList();
        return sources.toArray(new EmfFileInfo[0]);
    }

    private List<EmfFileInfo> sourcesList() {
        List<EmfFileInfo> sources = new ArrayList<EmfFileInfo>();
        
        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            ViewableRow row = (ViewableRow) iter.next();
            sources.add((EmfFileInfo)row.source());
        }

        return sources;
    }

    private Object[] rowValues(EmfFileInfo info) {
        Object[] values = { info.getName(), getDate(info), getSize(info), getType(info), info.canRead(),
                info.canWrite(), info.canExecute() };
        return values;
    }

    private Object getDate(EmfFileInfo info) {
        return CustomDateFormat.format_YYYY_MM_DD_HH_MM(new Date(info.getLastModified()));
    }

    private Object getType(EmfFileInfo info) {
        String path = info.getAbsolutePath();
        int index = path.lastIndexOf('.');
        return index < 0 ? "Unkown" : path.substring(++index);
    }

    private Object getSize(EmfFileInfo info) {
        return (info.getLength() / 1024) + " KB" ;
    }

}
