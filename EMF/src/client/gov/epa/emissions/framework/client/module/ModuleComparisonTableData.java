package gov.epa.emissions.framework.client.module;

import gov.epa.emissions.framework.services.module.Module;
import gov.epa.emissions.framework.ui.AbstractTableData;
import gov.epa.emissions.framework.ui.ViewableRow;
import gov.epa.emissions.framework.ui.Row;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class ModuleComparisonTableData extends AbstractTableData {
    private Module firstModule;
    private Module secondModule;
    private List rows;

    public ModuleComparisonTableData(Module firstModule, Module secondModule, TreeMap<String, String[]> comp) {
        this.firstModule = firstModule;
        this.secondModule = secondModule;
        this.rows = createRows(comp);
    }

    public String[] columns() {
        return new String[] {"Attribute", "Comparison", "[ 1 ] " + firstModule.getName(), "[ 2 ] " + secondModule.getName()};
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

    private List createRows(TreeMap<String, String[]> comp) {
        List rows = new ArrayList();

        for (String attribute : comp.keySet()) {
            String[] values = comp.get(attribute);
            Object[] record = { attribute, values[0], values[1], values[2] };

            Row row = new ViewableRow(attribute, record);
            rows.add(row);
        }

        return rows;
    }
}
