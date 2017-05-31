package gov.epa.emissions.framework.client.moduletype;

import gov.epa.emissions.framework.services.module.ModuleTypeVersionParameter;
import gov.epa.emissions.framework.ui.AbstractTableData;
import gov.epa.emissions.framework.ui.ViewableRow;
import gov.epa.emissions.framework.ui.Row;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class ModuleTypeVersionParametersTableData extends AbstractTableData {
    private List rows;

    public ModuleTypeVersionParametersTableData(Map<String, ModuleTypeVersionParameter> moduleTypeVersionParameters) {
        this.rows = createRows(moduleTypeVersionParameters);
    }

    public String[] columns() {
        return new String[] { "Mode", "Optional?", "Name", "SQL Type", "Description"};
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

    private void add(ModuleTypeVersionParameter element) {
        Object[] values = { element.getMode(),
                            element.getIsOptional() ? "Yes" : "No",
                            element.getParameterName(),
                            element.getSqlParameterType(),
                            element.getDescription() };

        Row row = new ViewableRow(element, values);
        this.rows.add(row);
    }

    private List createRows(Map<String, ModuleTypeVersionParameter> moduleTypeVersionParameters) {
        List rows = new ArrayList();

        for (ModuleTypeVersionParameter element : moduleTypeVersionParameters.values()) {
            Object[] values = { element.getMode(),
                                element.getIsOptional() ? "Yes" : "No",
                                element.getParameterName(),
                                element.getSqlParameterType(),
                                element.getDescription() };

            Row row = new ViewableRow(element, values);
            rows.add(row);
        }

        return rows;
    }
}
