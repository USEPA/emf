package gov.epa.emissions.framework.client.module;

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

    private void add(ModuleParameter element) {
        ModuleTypeVersionParameter moduleTypeVersionParameter = element.getModuleTypeVersionParameter();
        Object[] values = { moduleTypeVersionParameter.getMode(),
                            element.getParameterName(),
                            moduleTypeVersionParameter.getSqlParameterType(),
                            element.getValue(),
                            getShortDescription(moduleTypeVersionParameter)};

        Row row = new ViewableRow(element, values);
        this.rows.add(row);
    }

    private List createRows(Map<String, ModuleParameter> moduleParameters) {
        List rows = new ArrayList();

        for (ModuleParameter element : moduleParameters.values()) {
            ModuleTypeVersionParameter moduleTypeVersionParameter = element.getModuleTypeVersionParameter();
            Object[] values = { moduleTypeVersionParameter.getMode(),
                                element.getParameterName(),
                                moduleTypeVersionParameter.getSqlParameterType(),
                                element.getValue(),
                                getShortDescription(moduleTypeVersionParameter)};

            Row row = new ViewableRow(element, values);
            rows.add(row);
        }

        return rows;
    }

    private String getShortDescription(ModuleTypeVersionParameter moduleTypeVersionParameter) {
        String description = moduleTypeVersionParameter.getDescription();

        if (description != null && description.length() > 100)
            return description.substring(0, 96) + " ...";

        return description;
    }
}
