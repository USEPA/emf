package gov.epa.emissions.framework.client.data.module;

import gov.epa.emissions.commons.data.Module;
import gov.epa.emissions.commons.data.ModuleTypeVersion;
import gov.epa.emissions.commons.util.CustomDateFormat;
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
        return new String[] { "Module Name", "Module Type", "Version", "Creator", "Date", "Description"};
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
            Object[] values = { element.getName(),
                                moduleTypeVersion.getName(),
                                moduleTypeVersion.getVersion(),
                                element.getCreator().getName(),
                                CustomDateFormat.format_YYYY_MM_DD_HH_MM(element.getCreationDate()),
                                getShortDescription(element)};

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
