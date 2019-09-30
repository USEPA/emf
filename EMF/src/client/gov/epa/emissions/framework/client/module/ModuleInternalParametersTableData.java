package gov.epa.emissions.framework.client.module;

import gov.epa.emissions.framework.services.module.History;
import gov.epa.emissions.framework.services.module.HistoryInternalParameter;
import gov.epa.emissions.framework.services.module.HistoryParameter;
import gov.epa.emissions.framework.services.module.ModuleInternalParameter;
import gov.epa.emissions.framework.services.module.ModuleTypeVersionParameter;
import gov.epa.emissions.framework.ui.AbstractTableData;
import gov.epa.emissions.framework.ui.ViewableRow;
import gov.epa.emissions.framework.ui.Row;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ModuleInternalParametersTableData extends AbstractTableData {
    private List rows;
    
    public ModuleInternalParametersTableData(Map<String, ModuleInternalParameter> moduleInternalParameters) {
        this.rows = createRows(moduleInternalParameters);
    }

    public String[] columns() {
        return new String[] { "Submodules & Parameter Name", "Keep?", "SQL Type", "Output Value" };
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

    private List createRows(Map<String, ModuleInternalParameter> moduleInternalParameters) {
        List rows = new ArrayList();

        for (ModuleInternalParameter moduleInternalParameter : moduleInternalParameters.values()) {
            String parameterPath = moduleInternalParameter.getParameterPath();
            String parameterPathNames = moduleInternalParameter.getParameterPathNames();
            ModuleTypeVersionParameter moduleTypeVersionParameter = moduleInternalParameter.getModuleTypeVersionParameter();
            HistoryInternalParameter historyInternalParameter = null;
            History lastHistory = moduleInternalParameter.getCompositeModule().lastHistory();
            if (lastHistory != null) {
                Map<String, HistoryInternalParameter> historyInternalParameters = lastHistory.getHistoryInternalParameters();
                if (History.SUCCESS.equals(lastHistory.getResult()) && historyInternalParameters.containsKey(parameterPath)) {
                    historyInternalParameter = historyInternalParameters.get(parameterPath);
                }
            }
            String outValue = (historyInternalParameter == null) ? "N/A" : historyInternalParameter.getValue();
            Object[] values = { parameterPathNames,
                                moduleInternalParameter.getKeep() ? "Yes" : "No",
                                moduleTypeVersionParameter.getSqlParameterType(),
                                outValue
                              };

            Row row = new ViewableRow(moduleInternalParameter, values);
            rows.add(row);
        }

        return rows;
    }
}
