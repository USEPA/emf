package gov.epa.emissions.framework.client.module;

import gov.epa.emissions.framework.services.module.History;
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
//            List<History> history = moduleInternalParameter.getCompositeModule().getModuleHistory();
//            HistoryParameter historyParameter = null;
//            if (history.size() > 0) {
//                History lastHistory = history.get(history.size() - 1);
//                if (History.SUCCESS.equals(lastHistory.getResult())) {
//                    historyParameter = lastHistory.getHistoryParameters().get(moduleInternalParameter.getParameterName());
//                }
//            }
//            ModuleTypeVersionParameter moduleTypeVersionParameter = moduleInternalParameter.getModuleTypeVersionParameter();
//            String mode = moduleTypeVersionParameter.getMode();
//            String inValue = mode.equals(ModuleTypeVersionParameter.OUT) ? "N/A" : moduleInternalParameter.getValue();
//            String outValue = mode.equals(ModuleTypeVersionParameter.IN) ? "N/A" : ((historyParameter == null) ? "N/A" : historyParameter.getValue());
            Object[] values = { moduleInternalParameter.getParameterPathNames(),
                                moduleInternalParameter.getKeep() ? "Yes" : "No",
                                moduleInternalParameter.getModuleTypeVersionParameter().getSqlParameterType(),
                                ""  // TODO outValue
                              };

            Row row = new ViewableRow(moduleInternalParameter, values);
            rows.add(row);
        }

        return rows;
    }
}
