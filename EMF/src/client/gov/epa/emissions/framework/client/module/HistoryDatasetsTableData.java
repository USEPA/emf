package gov.epa.emissions.framework.client.module;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.module.HistoryDataset;
import gov.epa.emissions.framework.services.module.ModuleDataset;
import gov.epa.emissions.framework.services.module.ModuleTypeVersionDataset;
import gov.epa.emissions.framework.ui.AbstractTableData;
import gov.epa.emissions.framework.ui.ViewableRow;
import gov.epa.emissions.framework.ui.Row;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HistoryDatasetsTableData extends AbstractTableData {
    private List rows;

    public HistoryDatasetsTableData(Map<String, HistoryDataset> historyDatasets, EmfSession session) {
        this.rows = createRows(historyDatasets, session);
    }

    public String[] columns() {
        return new String[] { "Mode", "Name/Placeholder", "Dataset Name", "Version", "Exists?", "Description"};
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

    private List createRows(Map<String, HistoryDataset> historyDatasets, EmfSession session) {
        List rows = new ArrayList();

        for (HistoryDataset element : historyDatasets.values()) {
            String mode = element.getModuleTypeVersionDataset().getMode();
            EmfDataset dataset = null;
            String datasetName = "";
            Integer datasetId = element.getDatasetId();
            if (datasetId != null) {
                try {
                    dataset = session.dataService().getDataset(datasetId);
                    if (dataset != null)
                        datasetName = dataset.getName();
                } catch (EmfException ex) {
                    // ignore exception
                }
            }
            Object[] values = { mode,
                                element.getPlaceholderName(),
                                datasetName,
                                element.getVersion(),
                                (dataset == null) ? "No" : "Yes",
                                getShortDescription(element.getModuleTypeVersionDataset()) };

            Row row = new ViewableRow(element, values);
            rows.add(row);
        }
        return rows;
    }


    private String getShortDescription(ModuleTypeVersionDataset moduleTypeVersionDataset) {
        String description = moduleTypeVersionDataset.getDescription();

        if (description != null && description.length() > 100)
            return description.substring(0, 96) + " ...";

        return description;
    }
}
