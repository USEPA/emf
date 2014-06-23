package gov.epa.emissions.framework.client.casemanagement.jobs;

import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;
import gov.epa.emissions.framework.ui.ChangeableTableData;
import gov.epa.emissions.framework.ui.Row;
import gov.epa.emissions.framework.ui.ViewableRow;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CaseJobsTableData extends ChangeableTableData {

    private List rows;

    private CaseJob[] values;
    
    private boolean changes = false;

    public CaseJobsTableData(CaseJob[] values) {
        this.values = values;
        this.rows = createRows(values);
    }

    public String[] columns() {
        return new String[] { "Name", "Order", "Region", "Sector", "Run Status", "Running User", "Last Message", "Start Date",  
                "Completion Date", "Executable", "Arguments", "Exec. Version", "Job No.", "Path",    
                "Queue Options", "Job Group", "Local?", "ID in Queue", "User", "Host", "Purpose", "Depends On" };
    }

    public Class getColumnClass(int col) {
        if (col == 1)
            return Float.class;
        
        if (col == 11 || col == 12)
            return Integer.class;
        
        return String.class;
    }

    public List rows() {
        return rows;
    }

    public void add(CaseJob input) {
        this.changes = true;
        rows.add(row(input));
        refresh();
    }

    private List createRows(CaseJob[] values) {
        List rows = new ArrayList();
        
        for (int i = 0; i < values.length; i++)
            rows.add(row(values[i]));

        return rows;
    }

    private Row row(CaseJob job) {
        return new ViewableRow(new CaseJobsRowSource(job));
    }

    public boolean isEditable(int col) {
        return false;
    }

    public CaseJob[] getValues() {
        return values;
    }
    
    public void remove(CaseJob[] jobs) {
        if (jobs.length > 0)
            this.changes = true;
        
        for (int i = 0; i < jobs.length; i++)
            remove(jobs[i]);
        
        refresh();
    }
    
    private void remove(CaseJob job) {
        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            ViewableRow row = (ViewableRow) iter.next();
            CaseJob source = (CaseJob) row.source();
            if (source == job) {
                rows.remove(row);
                return;
            }
        }
    }
    
    private void refresh() {
        this.rows = createRows(sources());
        
//        if (this.hasChanges())
//            notifyChanges();
    }

    public CaseJob[] sources() {
        List sources = sourcesList();
        return (CaseJob[]) sources.toArray(new CaseJob[0]);
    }

    private List sourcesList() {
        List sources = new ArrayList();
        
        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            ViewableRow row = (ViewableRow) iter.next();
            sources.add(row.source());
        }

        return sources;
    }
    
    public boolean hasChanges() {
        return this.changes;
    }
    
    public void setChanges(boolean changes) {
        this.changes = changes;
    }

}
