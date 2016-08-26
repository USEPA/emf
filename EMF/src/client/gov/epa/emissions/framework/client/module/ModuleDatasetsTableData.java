package gov.epa.emissions.framework.client.module;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
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
        return new String[] { "Mode", "Name/Placeholder", "Dataset Name", "Version", "Description"};
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

        for (ModuleDataset element : moduleDatasets.values()) {
            String mode = element.getModuleTypeVersionDataset().getMode();
            String outputMethod = element.getOutputMethod();
            String datasetName = "";
            Integer datasetId = element.getDatasetId();
            if (datasetId != null) {
                try {
                    EmfDataset dataset = session.dataService().getDataset(datasetId);
                    datasetName = dataset.getName();
                } catch (EmfException ex) {
                    // ignore exception
                }
            }
            if (mode.equals("IN") || mode.equals("INOUT")) {
                Object[] values = { mode,
                                    element.getPlaceholderName(),
                                    datasetName,
                                    element.getVersion(),
                                    getShortDescription(element.getModuleTypeVersionDataset()) };
    
                Row row = new ViewableRow(element, values);
                rows.add(row);
            }
            else if (outputMethod.equals(ModuleDataset.NEW)) {
                Object[] values = { "OUT NEW",
                                    element.getPlaceholderName(),
                                    element.getDatasetNamePattern(),
                                    0,
                                    getShortDescription(element.getModuleTypeVersionDataset()) };

                Row row = new ViewableRow(element, values);
                rows.add(row);
            }
            else if (outputMethod.equals(ModuleDataset.REPLACE)) {
                Object[] values = { "OUT REPLACE",
                                    element.getPlaceholderName(),
                                    datasetName,
                                    0,
                                    getShortDescription(element.getModuleTypeVersionDataset()) };
    
                Row row = new ViewableRow(element, values);
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
