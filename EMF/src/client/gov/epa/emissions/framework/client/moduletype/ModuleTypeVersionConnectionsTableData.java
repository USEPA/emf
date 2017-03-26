package gov.epa.emissions.framework.client.moduletype;

import gov.epa.emissions.framework.services.module.ModuleTypeVersion;
import gov.epa.emissions.framework.services.module.ModuleTypeVersionDatasetConnection;
import gov.epa.emissions.framework.services.module.ModuleTypeVersionParameterConnection;
import gov.epa.emissions.framework.services.module.ModuleTypeVersionSubmodule;
import gov.epa.emissions.framework.ui.AbstractTableData;
import gov.epa.emissions.framework.ui.ViewableRow;
import gov.epa.emissions.framework.ui.Row;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ModuleTypeVersionConnectionsTableData extends AbstractTableData {
    private List rows;

    public ModuleTypeVersionConnectionsTableData(ModuleTypeVersion moduleTypeVersion) {
        this.rows = createRows(moduleTypeVersion);
    }

    public String[] columns() {
        return new String[] { "Category", "Source Type", "Source", "Target Type", "Target", "Description"};
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

    private List createRows(ModuleTypeVersion moduleTypeVersion) {
        List rows = new ArrayList();
        
        for (ModuleTypeVersionDatasetConnection datasetConnection : moduleTypeVersion.getModuleTypeVersionDatasetConnections().values()) {
            Object[] values = { "dataset",
                                datasetConnection.getSourceDatasetTypeName(), // source and target types must be the same
                                datasetConnection.getSourceName(),
                                datasetConnection.getTargetDatasetTypeName(),
                                datasetConnection.getTargetName(),
                                getShortDescription(datasetConnection.getDescription()) };
            Row row = new ViewableRow(datasetConnection, values);
            rows.add(row);
        }

        for (ModuleTypeVersionParameterConnection parameterConnection : moduleTypeVersion.getModuleTypeVersionParameterConnections().values()) {
            Object[] values = { "parameter",
                                parameterConnection.getSourceSqlType(), // source and target types can be different
                                parameterConnection.getSourceName(),
                                parameterConnection.getTargetSqlType(),
                                parameterConnection.getTargetName(),
                                getShortDescription(parameterConnection.getDescription()) };
            Row row = new ViewableRow(parameterConnection, values);
            rows.add(row);
        }

        return rows;
    }

    private String getShortDescription(String description) {
        if (description != null && description.length() > 100)
            return description.substring(0, 96) + " ...";

        return description;
    }
}
