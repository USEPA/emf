package gov.epa.emissions.framework.client.module;

import gov.epa.emissions.framework.services.module.HistoryInternalParameter;
import gov.epa.emissions.framework.services.module.Module;
import gov.epa.emissions.framework.services.module.ModuleInternalParameter;
import gov.epa.emissions.framework.ui.AbstractTableData;
import gov.epa.emissions.framework.ui.ViewableRow;
import gov.epa.emissions.framework.ui.Row;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HistoryInternalParametersTableData extends AbstractTableData {
    private List rows;
    
    public HistoryInternalParametersTableData(Map<String, HistoryInternalParameter> historyInternalParameters) {
        this.rows = createRows(historyInternalParameters);
    }

    public String[] columns() {
        return new String[] { "Submodules & Parameter Name", "SQL Type", "Output Value" };
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

    private List createRows(Map<String, HistoryInternalParameter> historyInternalParameters) {
        List rows = new ArrayList();

        for (HistoryInternalParameter historyInternalParameter : historyInternalParameters.values()) {
            Module module = historyInternalParameter.getHistory().getModule();
            ModuleInternalParameter moduleInternalParameter = null;
            if (module.getModuleInternalParameters().containsKey(historyInternalParameter.getParameterPath())) {
                moduleInternalParameter = module.getModuleInternalParameters().get(historyInternalParameter.getParameterPath());
            }
            Object[] values = { historyInternalParameter.getParameterPathNames(),
                                (moduleInternalParameter == null) ? "" : moduleInternalParameter.getModuleTypeVersionParameter().getSqlParameterType(),
                                historyInternalParameter.getValue()
                              };

            Row row = new ViewableRow(moduleInternalParameter, values);
            rows.add(row);
        }

        return rows;
    }
}
