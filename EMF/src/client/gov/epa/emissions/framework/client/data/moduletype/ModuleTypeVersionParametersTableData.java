package gov.epa.emissions.framework.client.data.moduletype;

import gov.epa.emissions.commons.data.ModuleTypeVersionParameter;
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
        return new String[] { "Mode", "Name", "SQL Type", "Description"};
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
                            element.getParameterName(),
                            element.getSqlParameterType(),
                            getShortDescription(element) };

        Row row = new ViewableRow(element, values);
        this.rows.add(row);
    }

    private List createRows(Map<String, ModuleTypeVersionParameter> moduleTypeVersionParameters) {
        List rows = new ArrayList();

        for (ModuleTypeVersionParameter element : moduleTypeVersionParameters.values()) {
            Object[] values = { element.getMode(),
                                element.getParameterName(),
                                element.getSqlParameterType(),
                                getShortDescription(element) };

            Row row = new ViewableRow(element, values);
            rows.add(row);
        }

        return rows;
    }

    private String getShortDescription(ModuleTypeVersionParameter parameter) {
        String description = parameter.getDescription();

        if (description != null && description.length() > 100)
            return description.substring(0, 96) + " ...";

        return description;
    }
}
