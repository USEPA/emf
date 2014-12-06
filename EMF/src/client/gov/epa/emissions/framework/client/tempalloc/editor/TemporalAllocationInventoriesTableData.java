package gov.epa.emissions.framework.client.tempalloc.editor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.tempalloc.TemporalAllocationInputDataset;
import gov.epa.emissions.framework.ui.AbstractTableData;
import gov.epa.emissions.framework.ui.Row;
import gov.epa.emissions.framework.ui.ViewableRow;

public class TemporalAllocationInventoriesTableData extends AbstractTableData {

    private List rows;
    
    private EmfSession session;
    
    public TemporalAllocationInventoriesTableData(TemporalAllocationInputDataset[] temporalAllocationInputDatasets,
            EmfSession session) {
        this.session = session;
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
        EmfDataset inputDataset = temporalAllocationInputDataset.getInputDataset();
        
        int versionNum = temporalAllocationInputDataset.getVersion();
        int numRecords = 0;

        try {
            Version version = session.dataEditorService().getVersion(inputDataset.getId(), versionNum);
            if (version != null) numRecords = version.getNumberRecords();
        } catch (EmfException e) {
            // nothing
        }

        Object[] values = { inputDataset.getDatasetType().getName(), inputDataset.getName(), 
                versionNum, numRecords };

        return new ViewableRow(temporalAllocationInputDataset, values);
    }

    public String[] columns() {
        return new String[] { "Type", "Dataset", "Version", "# of Records" };
    }

    public Class getColumnClass(int col) {
        if (col > 1) return Integer.class;
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
