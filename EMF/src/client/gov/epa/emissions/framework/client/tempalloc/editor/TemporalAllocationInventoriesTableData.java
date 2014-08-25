package gov.epa.emissions.framework.client.tempalloc.editor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import gov.epa.emissions.framework.services.tempalloc.TemporalAllocationInputDataset;
import gov.epa.emissions.framework.ui.AbstractTableData;
import gov.epa.emissions.framework.ui.Row;
import gov.epa.emissions.framework.ui.ViewableRow;

public class TemporalAllocationInventoriesTableData extends AbstractTableData {

    private List rows;
    
    public TemporalAllocationInventoriesTableData(TemporalAllocationInputDataset[] temporalAllocationInputDatasets) {
        rows = createRows(temporalAllocationInputDatasets);
    }
    
    private List createRows(TemporalAllocationInputDataset[] temporalAllocationInputDatasets) {
        List rows = new ArrayList();
        for (int i = 0; i < temporalAllocationInputDatasets.length; i++) {
            Row row = row(temporalAllocationInputDatasets[i]);
            rows.add(row);
        }
        return rows;
    }

    private Row row(TemporalAllocationInputDataset temporalAllocationInputDataset) {
        Object[] values = { temporalAllocationInputDataset.getInputDataset().getDatasetType().getName(), 
                temporalAllocationInputDataset.getInputDataset().getName(), 
                temporalAllocationInputDataset.getVersion() };
        return new ViewableRow(temporalAllocationInputDataset, values);
    }

    public String[] columns() {
        return new String[] { "Type", "Dataset", "Version" };
    }

    public Class getColumnClass(int col) {
        if (col == 2) return Integer.class;
        return String.class;
    }

    public List rows() {
        return rows;
    }

    public boolean isEditable(int col) {
        return false;
    }

    public void add(TemporalAllocationInputDataset[] temporalAllocationInputDatasets) {
        for (int i = 0; i < temporalAllocationInputDatasets.length; i++) {
            Row row = row(temporalAllocationInputDatasets[i]);
            if (!rows.contains(row))
                rows.add(row);
        }
        refresh();
    }

    public void refresh() {
        this.rows = createRows(sources());
    }

    public TemporalAllocationInputDataset[] sources() {
        List sources = sourcesList();
        return (TemporalAllocationInputDataset[]) sources.toArray(new TemporalAllocationInputDataset[0]);
    }

    private List sourcesList() {
        List sources = new ArrayList();
        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            ViewableRow row = (ViewableRow) iter.next();
            sources.add(row.source());
        }

        return sources;
    }

    private void remove(TemporalAllocationInputDataset temporalAllocationInputDataset) {
        for (Iterator iter = rows.iterator(); iter.hasNext();) {
            ViewableRow row = (ViewableRow) iter.next();
            TemporalAllocationInputDataset source = (TemporalAllocationInputDataset) row.source();
            if (source == temporalAllocationInputDataset) {
                rows.remove(row);
                return;
            }
        }
    }

    public void remove(TemporalAllocationInputDataset[] temporalAllocationInputDatasets) {
        for (int i = 0; i < temporalAllocationInputDatasets.length; i++)
            remove(temporalAllocationInputDatasets[i]);

        refresh();
    }
}
