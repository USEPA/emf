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
        return new String[] { "Mode", "Optional?", "Name/Placeholder", "Dataset Name Pattern", "Dataset Name", "Version", "Exists?", "Description"};
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
            ModuleTypeVersionDataset moduleTypeVersionDataset = moduleDataset.getModuleTypeVersionDataset(); 
            String mode = moduleTypeVersionDataset.getMode();
            String outputMethod = moduleDataset.getOutputMethod();
            EmfDataset emfDataset = null;
            try {
                emfDataset = session.moduleService().getEmfDatasetForModuleDataset(moduleDataset.getId(), moduleDataset.getDatasetId(), moduleDataset.getDatasetNamePattern());
            } catch (EmfException e) {
                e.printStackTrace();
            }
            String datasetName = (emfDataset == null) ? "" : emfDataset.getName();
            String datasetExists = (emfDataset == null) ? "No" : "Yes"; // TODO check version also
            if (mode.equals(ModuleTypeVersionDataset.IN) || mode.equals(ModuleTypeVersionDataset.INOUT)) {
                Object[] values = { mode,
                                    moduleTypeVersionDataset.getIsOptional() ? "Yes" : "No",
                                    moduleDataset.getPlaceholderName(),
                                    "N/A",
                                    datasetName,
                                    moduleDataset.getVersion(),
                                    datasetExists,
                                    moduleTypeVersionDataset.getDescription() };
    
                Row row = new ViewableRow(moduleDataset, values);
                rows.add(row);
            }
            else if (outputMethod.equals(ModuleDataset.NEW)) {
                Object[] values = { "OUT NEW",
                                    moduleTypeVersionDataset.getIsOptional() ? "Yes" : "No", // should be No
                                    moduleDataset.getPlaceholderName(),
                                    moduleDataset.getDatasetNamePattern(),
                                    datasetName,
                                    0,
                                    datasetExists,
                                    moduleTypeVersionDataset.getDescription() };

                Row row = new ViewableRow(moduleDataset, values);
                rows.add(row);
            }
            else if (outputMethod.equals(ModuleDataset.REPLACE)) {
                Object[] values = { "OUT REPLACE",
                                    moduleTypeVersionDataset.getIsOptional() ? "Yes" : "No", // should be No
                                    moduleDataset.getPlaceholderName(),
                                    "N/A",
                                    datasetName,
                                    0,
                                    datasetExists,
                                    moduleTypeVersionDataset.getDescription() };
    
                Row row = new ViewableRow(moduleDataset, values);
                rows.add(row);
            }
        }
        return rows;
    }
}
