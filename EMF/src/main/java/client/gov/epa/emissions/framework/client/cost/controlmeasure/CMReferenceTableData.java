package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.commons.data.Reference;
import gov.epa.emissions.framework.ui.AbstractEditableTableData;
import gov.epa.emissions.framework.ui.ViewableRow;

import java.util.ArrayList;
import java.util.List;

public class CMReferenceTableData extends AbstractEditableTableData {

    private List<ViewableRow<Reference>> rows;

    public CMReferenceTableData(Reference[] references) {
        rows = createRows(references);
    }

    private List<ViewableRow<Reference>> createRows(Reference[] references) {
        
        rows = new ArrayList<ViewableRow<Reference>>();
        for (Reference reference : references) {
            
            if (reference!=null) {
                rows.add(row(reference));
            }
        }
        
        return rows;
    }

    private ViewableRow<Reference> row(Reference reference) {

        Object[] values = { reference.getDescription() };
        return new ViewableRow<Reference>(reference, values);
    }

    public String[] columns() {
        return new String[] { "Description" };
    }

    public Class<?> getColumnClass(int col) {
        return String.class;
    }

    public List<ViewableRow<Reference>> rows() {
        return this.rows;
    }

    public boolean isEditable(int col) {
        return false;
    }

    public void refresh() {
        this.rows = createRows(sources());
    }

    public Reference[] sources() {
        return sourcesList().toArray(new Reference[0]);
    }

    private List<Reference> sourcesList() {

        List<Reference> sources = new ArrayList<Reference>();        
        for (ViewableRow<Reference> row : this.rows) {
            sources.add(row.source());
        }

        return sources;
    }

    public boolean contains(Reference reference) {
        return this.sourcesList().contains(reference);
    }
    
    public void add(Reference reference) {
        rows.add(row(reference));
    }

    private void remove(Reference reference) {

        for (ViewableRow<Reference> row : this.rows) {

            Reference source = row.source();
            if (source.equals(reference)) {

                rows.remove(row);
                return;
            }
        }
    }

    public void remove(List<Reference> references) {

        for (Reference reference : references) {
            this.remove(reference);
        }
    }
}
