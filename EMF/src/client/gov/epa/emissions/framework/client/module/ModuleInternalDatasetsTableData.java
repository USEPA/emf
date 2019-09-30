package gov.epa.emissions.framework.client.module;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.module.History;
import gov.epa.emissions.framework.services.module.HistoryDataset;
import gov.epa.emissions.framework.services.module.HistoryParameter;
import gov.epa.emissions.framework.services.module.ModuleDataset;
import gov.epa.emissions.framework.services.module.ModuleInternalDataset;
import gov.epa.emissions.framework.services.module.ModuleTypeVersionDataset;
import gov.epa.emissions.framework.ui.AbstractTableData;
import gov.epa.emissions.framework.ui.ViewableRow;
import gov.epa.emissions.framework.ui.Row;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ModuleInternalDatasetsTableData extends AbstractTableData {
    private List rows;

    public ModuleInternalDatasetsTableData(Map<String, ModuleInternalDataset> moduleInternalDatasets, EmfSession session) {
        this.rows = createRows(moduleInternalDatasets, session);
    }

    public String[] columns() {
        return new String[] { "Submodules & Placeholder", "Keep?", "Dataset Type", "Dataset Name Pattern", "Dataset Name", "Version", "Exists?"};
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

    private List createRows(Map<String, ModuleInternalDataset> moduleInternalDatasets, EmfSession session) {
        List rows = new ArrayList();

        for (ModuleInternalDataset moduleInternalDataset : moduleInternalDatasets.values()) {
            EmfDataset emfDataset = moduleInternalDataset.getEmfDataset(session.dataService());
            String datasetName = (emfDataset == null) ? "" : emfDataset.getName();
            String datasetExists = (emfDataset == null) ? "No" : "Yes"; // TODO check version also
            Object[] values = { moduleInternalDataset.getPlaceholderPathNames(),
                                moduleInternalDataset.getKeep() ? "Yes" : "No",
                                moduleInternalDataset.getModuleTypeVersionDataset().getDatasetType().getName(),
                                moduleInternalDataset.getDatasetNamePattern(),
                                datasetName,
                                0,
                                datasetExists };

            Row row = new ViewableRow(moduleInternalDataset, values);
            rows.add(row);
        }
        return rows;
    }
}
