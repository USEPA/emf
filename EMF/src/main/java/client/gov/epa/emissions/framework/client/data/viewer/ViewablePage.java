package gov.epa.emissions.framework.client.data.viewer;

import gov.epa.emissions.commons.CommonDebugLevel;
import gov.epa.emissions.commons.db.Page;
import gov.epa.emissions.commons.db.version.VersionedRecord;
import gov.epa.emissions.commons.io.ColumnMetaData;
import gov.epa.emissions.commons.io.TableMetadata;
import gov.epa.emissions.framework.ui.AbstractTableData;
import gov.epa.emissions.framework.ui.Row;
import gov.epa.emissions.framework.ui.ViewableRow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ViewablePage extends AbstractTableData {

    private List rows;

    private TableMetadata tableMetadata;

    private String[] columnNames;
    
    private Class[] columnClasses;

    public ViewablePage(TableMetadata tableMetadata, Page page) {
        this.tableMetadata = tableMetadata;
        this.columnNames = createColumns();
        this.columnClasses = columnClasses();
        this.rows = createRows(page);
        
    }
    
    public Class getColumnClass(int col) {
        return columnClasses[col];
    }

    private Class[] columnClasses() {
        Class[] classes = new Class[columnNames.length];
        for (int i = 0; i < columnNames.length; i++) {
            ColumnMetaData data = tableMetadata.columnMetadata(columnNames[i]);
            classes[i]= classType(data.getType());
        }
        return classes;
    }

    private Class classType(String type) {
        try {
            return Class.forName(type);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e.getMessage());// TODO: exception what we should do
        }
    }

    public String[] columns() {
        return columnNames;
    }

    private String[] createColumns() {
        List result = new ArrayList();
        ColumnMetaData[] cols = tableMetadata.getCols();
        for (int i = 4; i < cols.length; i++){
            result.add(cols[i].getName());
        }
        result.add(cols[0].getName());//record_id
        result.add(cols[2].getName());//version
        result.add(cols[3].getName());//deleted versions
        
        return (String[]) result.toArray(new String[0]);
    }

    public List rows() {
        return rows;
    }

    public boolean isEditable(int col) {
        return false;
    }

    private List createRows(Page page) {
        List rows = new ArrayList();
        VersionedRecord[] records = page.getRecords();

        for (int i = 0; i < records.length; i++) {
            Object[] values = values(records[i]);
            
            if ( CommonDebugLevel.DEBUG_PAGE) {
            for ( int j=0; j<values.length; j++){
                if ( values[j] != null)
                    System.out.println( "" + j + " - col name: " + this.columnNames[j] + ", col class: " + this.columnClasses[j] + ", class: " + values[j].getClass() + ", value: " + values[j]);
                else
                    System.out.println( "" + j + " - null");
            }
            }
            
            Row row = new ViewableRow(records[i], values);
            rows.add(row);
        }

        return rows;
    }

    private Object[] values(VersionedRecord record) {
        List allTokens = new ArrayList();
        Object[] tokens = record.getTokens();
        allTokens.addAll(Arrays.asList(tokens));
        
        allTokens.add(new Integer(record.getRecordId()));
        allTokens.add(new Long(record.getVersion()));
        allTokens.add(record.getDeleteVersions());
        
        return allTokens.toArray();
    }
}
