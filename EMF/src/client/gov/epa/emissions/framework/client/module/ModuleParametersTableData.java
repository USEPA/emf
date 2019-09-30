package gov.epa.emissions.framework.client.module;

import gov.epa.emissions.framework.services.module.History;
import gov.epa.emissions.framework.services.module.HistoryParameter;
import gov.epa.emissions.framework.services.module.ModuleParameter;
import gov.epa.emissions.framework.services.module.ModuleTypeVersionParameter;
import gov.epa.emissions.framework.ui.AbstractTableData;
import gov.epa.emissions.framework.ui.ViewableRow;
import gov.epa.emissions.framework.ui.Row;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ModuleParametersTableData extends AbstractTableData {
    private List rows;
    
    public ModuleParametersTableData(Map<String, ModuleParameter> moduleParameters) {
        this.rows = createRows(moduleParameters);
    }

    public String[] columns() {
        return new String[] { "Mode", "Optional?", "Name", "SQL Type", "Input Value", "Output Value", "Description"};
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

    private List createRows(Map<String, ModuleParameter> moduleParameters) {
        List rows = new ArrayList();

        for (ModuleParameter moduleParameter : moduleParameters.values()) {
            History lastHistory = moduleParameter.getModule().lastHistory();
            HistoryParameter historyParameter = null;
            if (lastHistory != null) {
                if (History.SUCCESS.equals(lastHistory.getResult())) {
                    historyParameter = lastHistory.getHistoryParameters().get(moduleParameter.getParameterName());
                }
            }
            ModuleTypeVersionParameter moduleTypeVersionParameter = moduleParameter.getModuleTypeVersionParameter();
            String mode = moduleTypeVersionParameter.getMode();
            String inValue = mode.equals(ModuleTypeVersionParameter.OUT) ? "N/A" : moduleParameter.getValue();
            String outValue = mode.equals(ModuleTypeVersionParameter.IN) ? "N/A" : ((historyParameter == null) ? "N/A" : historyParameter.getValue());
            Object[] values = { mode,
                                moduleTypeVersionParameter.getIsOptional() ? "Yes": "No",
                                moduleParameter.getParameterName(),
                                moduleTypeVersionParameter.getSqlParameterType(),
                                inValue,
                                outValue,
                                moduleTypeVersionParameter.getDescription() };

            Row row = new ViewableRow(moduleParameter, values);
            rows.add(row);
        }

        return rows;
    }
}
