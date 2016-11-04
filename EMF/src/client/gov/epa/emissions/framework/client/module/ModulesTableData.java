package gov.epa.emissions.framework.client.module;

import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.services.module.Module;
import gov.epa.emissions.framework.services.module.ModuleType;
import gov.epa.emissions.framework.services.module.ModuleTypeVersion;
import gov.epa.emissions.framework.ui.AbstractTableData;
import gov.epa.emissions.framework.ui.ViewableRow;
import gov.epa.emissions.framework.ui.Row;

import java.util.ArrayList;
import java.util.List;

public class ModulesTableData extends AbstractTableData {
    private List rows;

    public ModulesTableData(Module[] modules) {
        this.rows = createRows(modules);
    }

    public String[] columns() {
        return new String[] { "Module Name", "Final?", "Module Type", "Version", "Creator", "Date"};
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

    private List createRows(Module[] modules) {
        List rows = new ArrayList();

        for (int i = 0; i < modules.length; i++) {
            Module element = modules[i];
            ModuleTypeVersion moduleTypeVersion = element.getModuleTypeVersion();
            ModuleType moduleType = moduleTypeVersion.getModuleType();
            Object[] values = { element.getName(),
                                element.getIsFinal() ? "Yes" : "No",
                                moduleType.getName(),
                                moduleTypeVersion.getVersion() + " - " + moduleTypeVersion.getName(),
                                element.getCreator().getName(),
                                CustomDateFormat.format_YYYY_MM_DD_HH_MM(element.getCreationDate()) };

            Row row = new ViewableRow(element, values);
            rows.add(row);
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
