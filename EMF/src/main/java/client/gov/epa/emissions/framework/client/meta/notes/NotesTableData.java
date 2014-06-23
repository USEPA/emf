package gov.epa.emissions.framework.client.meta.notes;

import gov.epa.emissions.framework.services.data.DatasetNote;
import gov.epa.emissions.framework.ui.ChangeableTableData;
import gov.epa.emissions.framework.ui.Row;
import gov.epa.emissions.framework.ui.ViewableRow;

import java.util.ArrayList;
import java.util.List;

public class NotesTableData extends ChangeableTableData {

    private List rows;

    private DatasetNote[] values;

    private List additions;

    public NotesTableData(DatasetNote[] values) {
        this.values = values;
        this.rows = createRows(values);
        this.additions = new ArrayList();
    }

    public String[] columns() {
        return new String[] { "Id", "Summary", "Type", "Version", "Creator", "Date", "References", "Details" };
    }

    public Class getColumnClass(int col) {
        if (col == 2)
            return Long.class;
        return String.class;
    }

    public List rows() {
        return rows;
    }

    public void add(DatasetNote note) {
        additions.add(note);
        rows.add(row(note));
        notifyChanges();
    }
    
    public void add(DatasetNote[] notes) {
        for (int i=0; i< notes.length; i++){
            Row row = row (notes[i]);
            if(!rows.contains(row)){ 
                additions.add(notes[i]);
                rows.add(row(notes[i]));
                notifyChanges();
            }
        }
    }

    private List createRows(DatasetNote[] values) {
        List rows = new ArrayList();
        for (int i = 0; i < values.length; i++)
            rows.add(row(values[i]));

        return rows;
    }

    private Row row(DatasetNote note) {
        return new ViewableRow(new NotesRowSource(note));
    }

    public boolean isEditable(int col) {
        return false;
    }

    public DatasetNote[] getValues() {
        return values;
    }

    public DatasetNote[] additions() {
        return (DatasetNote[]) additions.toArray(new DatasetNote[0]);
    }

}
