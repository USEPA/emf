package gov.epa.emissions.framework.client.module;

import gov.epa.emissions.framework.services.module.HistoryParameter;
import gov.epa.emissions.framework.services.module.ModuleParameter;
import gov.epa.emissions.framework.services.module.ModuleTypeVersionParameter;
import gov.epa.emissions.framework.ui.AbstractTableData;
import gov.epa.emissions.framework.ui.ViewableRow;
import gov.epa.emissions.framework.ui.Row;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HistoryParametersTableData extends AbstractTableData {
    private List rows;

    public HistoryParametersTableData(Map<String, HistoryParameter> historyParameters) {
        this.rows = createRows(historyParameters);
    }

    public String[] columns() {
        return new String[] { "Mode", "Name", "SQL Type", "Value", "Description"};
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

    private List createRows(Map<String, HistoryParameter> historyParameters) {
        List rows = new ArrayList();

        for (HistoryParameter element : historyParameters.values()) {
            ModuleTypeVersionParameter moduleTypeVersionParameter = element.getModuleTypeVersionParameter();
            Object[] values = { moduleTypeVersionParameter.getMode(),
                                element.getParameterName(),
                                moduleTypeVersionParameter.getSqlParameterType(),
                                element.getValue(),
                                moduleTypeVersionParameter.getDescription() };

            Row row = new ViewableRow(element, values);
            rows.add(row);
        }

        return rows;
    }
}
