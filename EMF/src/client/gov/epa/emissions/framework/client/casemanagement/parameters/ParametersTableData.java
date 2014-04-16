package gov.epa.emissions.framework.client.casemanagement.parameters;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.casemanagement.parameters.CaseParameter;
import gov.epa.emissions.framework.ui.ChangeableTableData;
import gov.epa.emissions.framework.ui.Row;
import gov.epa.emissions.framework.ui.ViewableRow;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ParametersTableData extends ChangeableTableData {

    private List rows;

    private CaseParameter[] values;
    
    private boolean changes = false;
    
    private EmfSession session;

    public ParametersTableData(CaseParameter[] values, EmfSession session) {
        this.session = session;
        this.values = values;
        //session has to be set before this
        this.rows = createRows(values);
    }

    public String[] columns() {
        return new String[] { "Parameter", "Order", "Envt. Var.", "Region", "Sector", 
                "Job", "Program", "Value", "Type",
                "Reqd?", "Local?", "Last Modified", "Notes", "Purpose" };
    }

    public Class getColumnClass(int col) {
        if (col == 1)
            return Float.class;
        
        return String.class;
    }

    public List rows() {
        return rows;
    }

    public void add(CaseParameter param) {
        this.changes = true;
        rows.add(row(param));
        refresh();
    }

    private List createRows(CaseParameter[] values) {
        List rows = new ArrayList();
        
        for (int i = 0; i < values.length; i++)
            rows.add(row(values[i]));

        return rows;
    }

    private Row row(CaseParameter param) {
        return new ViewableRow(new ParametersRowSource(param, session));
    }

    public boolean isEditable(int col) {
        return false;
    }

    public CaseParameter[] getValues() {
        return values;
    }
    
    public void remove(CaseParameter[] params) {
        if (params.length > 0)
            this.changes = true;
        
        for (int i = 0; i < params.length; i++)
            remove(params[i]);
        
        refresh();
    }
    
    private void remove(CaseParameter param) {
        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            ViewableRow row = (ViewableRow) iter.next();
            CaseParameter source = (CaseParameter) row.source();
            if (source == param) {
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

    public CaseParameter[] sources() {
        List sources = sourcesList();
        return (CaseParameter[]) sources.toArray(new CaseParameter[0]);
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
