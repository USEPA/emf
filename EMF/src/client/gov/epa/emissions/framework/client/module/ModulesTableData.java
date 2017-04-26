package gov.epa.emissions.framework.client.module;

import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.services.module.Module;
import gov.epa.emissions.framework.services.module.LiteModule;
import gov.epa.emissions.framework.services.module.LiteModuleType;
import gov.epa.emissions.framework.services.module.LiteModuleTypeVersion;
import gov.epa.emissions.framework.services.module.ModuleType;
import gov.epa.emissions.framework.services.module.ModuleTypeVersion;
import gov.epa.emissions.framework.ui.AbstractTableData;
import gov.epa.emissions.framework.ui.ViewableRow;
import gov.epa.emissions.framework.ui.Row;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentSkipListMap;

public class ModulesTableData extends AbstractTableData {
    private List rows;

    public ModulesTableData(ConcurrentSkipListMap<Integer, LiteModule> liteModules) {
        this.rows = createRows(liteModules);
    }

    public String[] columns() {
        return new String[] {"Module Name", "Composite?", "Final?", "Module Type", "Version", "Creator", "Date", "Lock Owner", "Lock Date", "Description" };
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

    private List createRows(ConcurrentSkipListMap<Integer, LiteModule> liteModules) {
        List rows = new ArrayList();

        for (LiteModule liteModule : liteModules.values()) {
            LiteModuleTypeVersion liteModuleTypeVersion = liteModule.getLiteModuleTypeVersion();
            LiteModuleType liteModuleType = liteModuleTypeVersion.getLiteModuleType();
            Object[] values = { liteModule.getName(),
                                liteModuleType.getIsComposite() ? "Yes" : "No",
                                liteModule.getIsFinal() ? "Yes" : "No",
                                liteModuleType.getName(),
                                liteModuleTypeVersion.getVersion(),
                                liteModule.getCreator().getName(),
                                CustomDateFormat.format_YYYY_MM_DD_HH_MM(liteModule.getCreationDate()),
                                liteModule.getLockOwner(),
                                CustomDateFormat.format_YYYY_MM_DD_HH_MM(liteModule.getLockDate()),
                                liteModule.getDescription() };

            Row row = new ViewableRow(liteModule, values);
            rows.add(row);
        }

        return rows;
    }
}
