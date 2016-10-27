package gov.epa.emissions.framework.client.moduletype;

import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.services.module.ModuleTypeVersion;
import gov.epa.emissions.framework.ui.AbstractTableData;
import gov.epa.emissions.framework.ui.ViewableRow;
import gov.epa.emissions.framework.ui.Row;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ModuleTypeVersionsTableData extends AbstractTableData {
    private List rows;

    public ModuleTypeVersionsTableData(Map<Integer, ModuleTypeVersion> moduleTypeVersions) {
        this.rows = createRows(moduleTypeVersions);
    }

    public String[] columns() {
        return new String[] { "Version", "Name", "Description", "Creator", "Creation Date", "Last Mod Date", "Base Version", "Final?"};
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

    private List createRows(Map<Integer, ModuleTypeVersion> moduleTypeVersions) {
        List rows = new ArrayList();

        for (ModuleTypeVersion moduleTypeVersion : moduleTypeVersions.values()) {
            Object[] values = { moduleTypeVersion.getVersion(),
                                moduleTypeVersion.getName(),
                                getShortDescription(moduleTypeVersion.getDescription()),
                                moduleTypeVersion.getCreator().getName(),
                                CustomDateFormat.format_YYYY_MM_DD_HH_MM(moduleTypeVersion.getCreationDate()),
                                CustomDateFormat.format_YYYY_MM_DD_HH_MM(moduleTypeVersion.getLastModifiedDate()),
                                moduleTypeVersion.getBaseVersion(),
                                moduleTypeVersion.getIsFinal() ? "Yes" : "No" };

            Row row = new ViewableRow(moduleTypeVersion, values);
            rows.add(row);
        }

        return rows;
    }

    private String getShortDescription(String description) {
        if (description != null && description.length() > 100)
            return description.substring(0, 96) + " ...";

        return description;
    }
}
