package gov.epa.emissions.framework.client.casemanagement.outputs;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.casemanagement.outputs.CaseOutput;
import gov.epa.emissions.framework.ui.ChangeableTableData;
import gov.epa.emissions.framework.ui.Row;
import gov.epa.emissions.framework.ui.ViewableRow;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class OutputsTableData extends ChangeableTableData {

    private List rows;

    private CaseOutput[] values;
    
    private boolean changes = false;

    private EmfSession session;

    public OutputsTableData(CaseOutput[] values, EmfSession session) {
        this.values = values;
        this.session = session;
        this.rows = createRows(values);
    }

    public String[] columns() {
        return new String[] { "Output name", "Job", "Sector", "Dataset Name", "Dataset Type", "Import Status",
                "Creator", "Creation Date", "Exec Name", "Message" };
    }

    public Class getColumnClass(int col) {
        return String.class;
    }

    public List rows() {
        return rows;
    }

    public void add(CaseOutput output) {
        Row row = row(output);
        if (!rows.contains(row)) {
            this.changes = true; 
            rows.add(row);
            //notifyChanges();
            refresh();
        }
        return;
    }

    private List createRows(CaseOutput[] values) {
        List rows = new ArrayList();
        for (int i = 0; i < values.length; i++) {
            Row row = row(values[i]);

            if (row != null)
                rows.add(row);
        }
        return rows;
    }

    private Row row(CaseOutput output) {
        OutputsRowSource source = new OutputsRowSource(output, session);

        if (source.values() == null)
            return null;

        return new ViewableRow(source);
    }

    public boolean isEditable(int col) {
        return false;
    }

    public CaseOutput[] getValues() {
        return values;
    }

    private void removeFromList(CaseOutput output) {
        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            ViewableRow row = (ViewableRow) iter.next();
            CaseOutput source = (CaseOutput) row.source();
            if (source == output) {
                rows.remove(row);
                return;
            }
        }
    }

    public void remove(CaseOutput[] values) {
        if (values.length > 0)
            this.changes = true;
        for (int i = 0; i < values.length; i++)
            removeFromList(values[i]);
        refresh();
    }

    public void refresh() {
        CaseOutput[] outputs = sources();
        this.rows = createRows(outputs);
    }
    
    public CaseOutput[] sources() {
        List sources = sourcesList();
        return (CaseOutput[]) sources.toArray(new CaseOutput[0]);
    }

    public CaseOutput[] showSources() {
        List sources = sourcesList();
        return (CaseOutput[]) sources.toArray(new CaseOutput[0]);
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
