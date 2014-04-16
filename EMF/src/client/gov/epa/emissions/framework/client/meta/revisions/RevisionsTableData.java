package gov.epa.emissions.framework.client.meta.revisions;

import gov.epa.emissions.framework.services.editor.Revision;
import gov.epa.emissions.framework.ui.AbstractTableData;
import gov.epa.emissions.framework.ui.Row;
import gov.epa.emissions.framework.ui.ViewableRow;

import java.util.ArrayList;
import java.util.List;

public class RevisionsTableData extends AbstractTableData {

    private List rows;

    private Revision[] values;

    public RevisionsTableData(Revision[] values) {
        this.values = values;
        this.rows = createRows(values);
    }

    public String[] columns() {
        return new String[] { "What", "Why", "References", "Version", "Creator", "Date" };
    }

    public List rows() {
        return rows;
    }

    private List createRows(Revision[] values) {
        List rows = new ArrayList();
        for (int i = 0; i < values.length; i++)
            rows.add(row(values[i]));

        return rows;
    }

    private Row row(Revision revision) {
        return new ViewableRow(new RevisionsRowSource(revision));
    }

    public boolean isEditable(int col) {
        return (col == 0) ? true : false;
    }

    public Revision[] getValues() {
        return values;
    }

    public Class getColumnClass(int col) {
        // NOTE Auto-generated method stub
        return String.class;
    }

}
