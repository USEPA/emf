package gov.epa.emissions.framework.client.cost.controlstrategy.groups;

import java.util.ArrayList;
import java.util.List;

import gov.epa.emissions.framework.services.cost.controlStrategy.StrategyGroup;
import gov.epa.emissions.framework.ui.AbstractTableData;
import gov.epa.emissions.framework.ui.Row;
import gov.epa.emissions.framework.ui.ViewableRow;

public class StrategyGroupTableData extends AbstractTableData {

    private List rows;
    
    public StrategyGroupTableData(StrategyGroup[] strategyGroups) {
        this.rows = createRows(strategyGroups);
    }

    public String[] columns() {
        return new String[] { "Name", "# Strategies" };
    }

    public Class getColumnClass(int col) {
        if (col == 1)
            return Integer.class;

        return String.class;
    }

    public List rows() {
        return rows;
    }

    public boolean isEditable(int col) {
        return false;
    }
    
    private List createRows(StrategyGroup[] strategyGroups) {
        List<Row> rows = new ArrayList<Row>();
        for (int i = 0; i < strategyGroups.length; i++) {
            StrategyGroup element = strategyGroups[i];
            Object[] values = { element.getName(), element.getControlStrategies().length };
            Row row = new ViewableRow(element, values);
            rows.add(row);
        }

        return rows;
    }
}
