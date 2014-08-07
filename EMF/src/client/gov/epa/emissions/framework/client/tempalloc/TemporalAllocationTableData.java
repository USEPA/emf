package gov.epa.emissions.framework.client.tempalloc;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import gov.epa.emissions.commons.util.CustomDateFormat;
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
        return new String[] { "Name", "Resolution", "Start Day", "End Day", "Last Modified", "Run Status", "Creator" };
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
                    formatDay(element.getStartDay()),
                    formatDay(element.getEndDay()),
                    format(element.getLastModifiedDate()),
                    element.getRunStatus(),
                    element.getCreator().getName() };
            Row row = new ViewableRow(element, values);
            rows.add(row);
        }
        
        return rows;
    }
    
    private String formatDay(Date date) {
        return (date == null) ? "N/A" : CustomDateFormat.format_MM_DD_YYYY(date);
    }
}
