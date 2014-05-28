package gov.epa.emissions.framework.client.tempalloc;

import java.util.ArrayList;
import java.util.List;

import gov.epa.emissions.framework.services.tempalloc.TemporalAllocation;
import gov.epa.emissions.framework.services.tempalloc.TemporalAllocationResolution;
import gov.epa.emissions.framework.ui.AbstractTableData;
import gov.epa.emissions.framework.ui.Row;
import gov.epa.emissions.framework.ui.ViewableRow;

public class TemporalAllocationTableData extends AbstractTableData {

    private List rows;
    
    public TemporalAllocationTableData(TemporalAllocation[] temporalAllocations) {
        this.rows = createRows(temporalAllocations);
    }

    public String[] columns() {
        return new String[] { "Name", "Resolution", "Last Modified" };
    }
    
    public Class getColumnClass(int col) {
        return String.class;
    }

    public List rows() {
        return rows;
    }

    public boolean isEditable(int col) {
        return false;
    }
    
    private List createRows(TemporalAllocation[] temporalAllocations) {
        List<Row> rows = new ArrayList<Row>();
        for (int i = 0; i < temporalAllocations.length; i++) {
            TemporalAllocation element = temporalAllocations[i];
            TemporalAllocationResolution resolution = element.getResolution();
            Object[] values = { element.getName(), 
                    (resolution == null ? "" : element.getResolution().getName()), 
                    format(element.getLastModifiedDate()) };
            Row row = new ViewableRow(element, values);
            rows.add(row);
        }
        
        return rows;
    }
}
