package gov.epa.emissions.framework.client.module;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.module.History;
import gov.epa.emissions.framework.services.module.HistoryDataset;
import gov.epa.emissions.framework.services.module.HistoryInternalDataset;
import gov.epa.emissions.framework.services.module.HistoryParameter;
import gov.epa.emissions.framework.services.module.Module;
import gov.epa.emissions.framework.services.module.ModuleDataset;
import gov.epa.emissions.framework.services.module.ModuleInternalDataset;
import gov.epa.emissions.framework.services.module.ModuleTypeVersionDataset;
import gov.epa.emissions.framework.ui.AbstractTableData;
import gov.epa.emissions.framework.ui.ViewableRow;
import gov.epa.emissions.framework.ui.Row;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HistoryInternalDatasetsTableData extends AbstractTableData {
    private List rows;

    public HistoryInternalDatasetsTableData(Map<String, HistoryInternalDataset> historyInternalDatasets, EmfSession session) {
        this.rows = createRows(historyInternalDatasets, session);
    }

    public String[] columns() {
        return new String[] { "Submodules & Placeholder", "Dataset Type", "Dataset Name Pattern", "Dataset Name", "Version", "Exists?"};
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

    private List createRows(Map<String, HistoryInternalDataset> historyInternalDatasets, EmfSession session) {
        List rows = new ArrayList();

        for (HistoryInternalDataset historyInternalDataset : historyInternalDatasets.values()) {
            Module module = historyInternalDataset.getHistory().getModule();
            ModuleInternalDataset moduleInternalDataset = null;
            if (module.getModuleInternalDatasets().containsKey(historyInternalDataset.getPlaceholderPath())) {
                moduleInternalDataset = module.getModuleInternalDatasets().get(historyInternalDataset.getPlaceholderPath());
            }
            EmfDataset emfDataset = null;
            try {
                emfDataset = session.dataService().getDataset(historyInternalDataset.getDatasetId());
            } catch (EmfException e) {
                // nothing to do
            }
            Object[] values = { historyInternalDataset.getPlaceholderPathNames(),
                                (emfDataset == null) ? "" : emfDataset.getDatasetType().getName(),
                                (moduleInternalDataset == null) ? "" : moduleInternalDataset.getDatasetNamePattern(),
                                (emfDataset == null) ? "" : emfDataset.getName(),
                                0, // TODO check version also
                                (emfDataset == null) ? "No" : "Yes" };

            Row row = new ViewableRow(historyInternalDataset, values);
            rows.add(row);
        }
        return rows;
    }
}
