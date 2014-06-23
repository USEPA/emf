package gov.epa.emissions.framework.client.casemanagement.inputs;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.casemanagement.CaseInput;
import gov.epa.emissions.framework.ui.ChangeableTableData;
import gov.epa.emissions.framework.ui.Row;
import gov.epa.emissions.framework.ui.ViewableRow;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class InputsTableData extends ChangeableTableData {

    private List rows;

    private CaseInput[] values;
    
    private boolean changes = false;
    
    private EmfSession session;

    public InputsTableData(CaseInput[] values, EmfSession session) {
        this.session = session;
        this.values = values;
        //session has to be set before this
        this.rows = createRows(values);
    }

    public String[] columns() {
        return new String[] { "Input", "Envt. Var.", "Region", "Sector", "Job", "Program", "Dataset", "Version", "QA Status", "DS Type",
                "Reqd?", "Local?", "Sub Dir", "Last Modified" };
    }

    public Class getColumnClass(int col) {
        return String.class;
    }

    public List rows() {
        return rows;
    }

    public void add(CaseInput input) {
        this.changes = true;
        rows.add(row(input));
        refresh();
    }

    private List createRows(CaseInput[] values) {
        List rows = new ArrayList();
        
        for (int i = 0; i < values.length; i++)
            rows.add(row(values[i]));

        return rows;
    }

    private Row row(CaseInput input) {
        return new ViewableRow(new InputsRowSource(input, session));
    }

    public boolean isEditable(int col) {
        return false;
    }

    public CaseInput[] getValues() {
        return values;
    }
    
    public void remove(CaseInput[] inputs) {
        if (inputs.length > 0)
            this.changes = true;
        
        for (int i = 0; i < inputs.length; i++)
            remove(inputs[i]);
        
        refresh();
    }
    
    private void remove(CaseInput input) {
        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            ViewableRow row = (ViewableRow) iter.next();
            CaseInput source = (CaseInput) row.source();
            if (source == input) {
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

    public CaseInput[] sources() {
        List sources = sourcesList();
        return (CaseInput[]) sources.toArray(new CaseInput[0]);
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
