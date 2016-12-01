package gov.epa.emissions.framework.client.module;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.module.History;
import gov.epa.emissions.framework.services.module.HistoryDataset;
import gov.epa.emissions.framework.services.module.HistoryParameter;
import gov.epa.emissions.framework.services.module.ModuleDataset;
import gov.epa.emissions.framework.services.module.ModuleTypeVersionDataset;
import gov.epa.emissions.framework.ui.AbstractTableData;
import gov.epa.emissions.framework.ui.ViewableRow;
import gov.epa.emissions.framework.ui.Row;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ModuleDatasetsTableData extends AbstractTableData {
    private List rows;

    public ModuleDatasetsTableData(Map<String, ModuleDataset> moduleDatasets, EmfSession session) {
        this.rows = createRows(moduleDatasets, session);
    }

    public String[] columns() {
        return new String[] { "Mode", "Name/Placeholder", "Dataset Name Pattern", "Dataset Name", "Version", "Exists?", "Description"};
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

    private List createRows(Map<String, ModuleDataset> moduleDatasets, EmfSession session) {
        List rows = new ArrayList();

        for (ModuleDataset moduleDataset : moduleDatasets.values()) {
            String mode = moduleDataset.getModuleTypeVersionDataset().getMode();
            String outputMethod = moduleDataset.getOutputMethod();
            EmfDataset emfDataset = moduleDataset.getEmfDataset(session.dataService());
            String datasetName = (emfDataset == null) ? "" : emfDataset.getName();
            String datasetExists = (emfDataset == null) ? "No" : "Yes"; // TODO check version also
            if (mode.equals("IN") || mode.equals("INOUT")) {
                Object[] values = { mode,
                                    moduleDataset.getPlaceholderName(),
                                    "N/A",
                                    datasetName,
                                    moduleDataset.getVersion(),
                                    datasetExists,
                                    getShortDescription(moduleDataset.getModuleTypeVersionDataset()) };
    
                Row row = new ViewableRow(moduleDataset, values);
                rows.add(row);
            }
            else if (outputMethod.equals(ModuleDataset.NEW)) {
                Object[] values = { "OUT NEW",
                                    moduleDataset.getPlaceholderName(),
                                    moduleDataset.getDatasetNamePattern(),
                                    datasetName,
                                    0,
                                    datasetExists,
                                    getShortDescription(moduleDataset.getModuleTypeVersionDataset()) };

                Row row = new ViewableRow(moduleDataset, values);
                rows.add(row);
            }
            else if (outputMethod.equals(ModuleDataset.REPLACE)) {
                Object[] values = { "OUT REPLACE",
                                    moduleDataset.getPlaceholderName(),
                                    "N/A",
                                    datasetName,
                                    0,
                                    datasetExists,
                                    getShortDescription(moduleDataset.getModuleTypeVersionDataset()) };
    
                Row row = new ViewableRow(moduleDataset, values);
                rows.add(row);
            }
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
