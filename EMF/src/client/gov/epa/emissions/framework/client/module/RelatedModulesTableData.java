package gov.epa.emissions.framework.client.module;

import gov.epa.emissions.framework.services.module.LiteModule;
import gov.epa.emissions.framework.ui.AbstractTableData;
import gov.epa.emissions.framework.ui.ViewableRow;
import gov.epa.emissions.framework.ui.Row;

import java.util.ArrayList;
import java.util.List;

public class RelatedModulesTableData extends AbstractTableData {
    private List rows;

    public RelatedModulesTableData(LiteModule[] liteModules) {
        this.rows = createRows(liteModules);
    }

    public String[] columns() {
        return new String[] { "Module Name" };
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

    private List createRows(LiteModule[] liteModules) {
        List rows = new ArrayList();

        for (LiteModule liteModule : liteModules) {
            Object[] values = { liteModule.getName() };
            Row row = new ViewableRow(liteModule, values);
            rows.add(row);
        }
        
        return rows;
    }
}
