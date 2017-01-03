package gov.epa.emissions.framework.client.module;

import gov.epa.emissions.commons.data.KeyVal;
import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.module.History;
import gov.epa.emissions.framework.services.module.Module;
import gov.epa.emissions.framework.services.module.ModuleDataset;
import gov.epa.emissions.framework.services.module.ModuleType;
import gov.epa.emissions.framework.services.module.ModuleTypeVersion;
import gov.epa.emissions.framework.ui.AbstractTableData;
import gov.epa.emissions.framework.ui.ViewableRow;
import gov.epa.emissions.framework.ui.Row;

import java.util.ArrayList;
import java.util.List;

public class RelatedModulesTableData extends AbstractTableData {
    private List rows;

    public RelatedModulesTableData(EmfDataset dataset, Module[] modules, EmfSession session) {
        this.rows = createRows(dataset, modules, session);
    }

    public String[] columns() {
        return new String[] { "Module Name", "Placeholder", "Mode", "Dataset Version", "Last Run", "Last Result" };
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

    private List createRows(EmfDataset dataset, Module[] modules, EmfSession session) {
        List rows = new ArrayList();

        for (int i = 0; i < modules.length; i++) {
            Module module = modules[i];
            List<History> history = module.getModuleHistory();
            KeyVal[] keyVals = dataset.getKeyVals();
            History lastHistory = (history.size() > 0) ? history.get(history.size() - 1) : null;
            for (ModuleDataset moduleDataset : module.getModuleDatasets().values()) {
                EmfDataset emfDataset = moduleDataset.getEmfDataset(session.dataService());
                // TODO check the dataset keywords to see if the dataset was created by this module in the past
                if (emfDataset == null || emfDataset.getId() != dataset.getId())
                    continue;
                String mode = moduleDataset.getModuleTypeVersionDataset().getMode();
                String outputMethod = moduleDataset.getOutputMethod();
                if (mode.equals("IN") || mode.equals("INOUT")) {
                    Object[] values = { module.getName(),
                                        moduleDataset.getPlaceholderName(),
                                        moduleDataset.getModuleTypeVersionDataset().getMode(),
                                        moduleDataset.getVersion(),
                                        (lastHistory == null) ? "N/A" : CustomDateFormat.format_YYYY_MM_DD_HH_MM(lastHistory.getCreationDate()),
                                        (lastHistory == null) ? "N/A" : lastHistory.getResult() };
                    Row row = new ViewableRow(module, values);
                    rows.add(row);
                } else {
                    Object[] values = { module.getName(),
                                        moduleDataset.getPlaceholderName(),
                                        moduleDataset.getModuleTypeVersionDataset().getMode(),
                                        0,
                                        (lastHistory == null) ? "N/A" : CustomDateFormat.format_YYYY_MM_DD_HH_MM(lastHistory.getCreationDate()),
                                        (lastHistory == null) ? "N/A" : lastHistory.getResult() };
                    Row row = new ViewableRow(module, values);
                    rows.add(row);
                }
            }
        }
        
        return rows;
    }

    private String getShortDescription(Module module) {
        String description = module.getDescription();

        if (description != null && description.length() > 100)
            return description.substring(0, 96) + " ...";

        return description;
    }
}
