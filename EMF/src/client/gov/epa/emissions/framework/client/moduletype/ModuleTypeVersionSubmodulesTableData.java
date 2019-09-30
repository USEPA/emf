package gov.epa.emissions.framework.client.moduletype;

import gov.epa.emissions.framework.services.module.ModuleTypeVersion;
import gov.epa.emissions.framework.services.module.ModuleTypeVersionSubmodule;
import gov.epa.emissions.framework.ui.AbstractTableData;
import gov.epa.emissions.framework.ui.ViewableRow;
import gov.epa.emissions.framework.ui.Row;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ModuleTypeVersionSubmodulesTableData extends AbstractTableData {
    private List rows;

    public ModuleTypeVersionSubmodulesTableData(Map<String, ModuleTypeVersionSubmodule> moduleTypeVersionSubmodules) {
        this.rows = createRows(moduleTypeVersionSubmodules);
    }

    public String[] columns() {
        return new String[] { "Name", "Module Type", "Version", "Final?", "Description"};
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

    private List createRows(Map<String, ModuleTypeVersionSubmodule> moduleTypeVersionSubmodules) {
        List rows = new ArrayList();

        for (ModuleTypeVersionSubmodule submodule : moduleTypeVersionSubmodules.values()) {
            ModuleTypeVersion moduleTypeVersion = submodule.getModuleTypeVersion();
            Object[] values = { submodule.getName(),
                                moduleTypeVersion.getModuleType().getName(),
                                moduleTypeVersion.versionName(),
                                moduleTypeVersion.getIsFinal() ? "Yes" : "No",
                                submodule.getDescription() };

            Row row = new ViewableRow(submodule, values);
            rows.add(row);
        }

        return rows;
    }
}
