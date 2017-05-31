package gov.epa.emissions.framework.client.moduletype;

import gov.epa.emissions.framework.services.EmfException;
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
        return new String[] { "Category", "Source Type", "Source", "Optional?", "Target Type", "Target", "Description"};
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
            String sourceDatasetTypeName = "ERROR";
            try {
                sourceDatasetTypeName = datasetConnection.getSourceDatasetTypeName();
            } catch (EmfException e) {
                e.printStackTrace();
            }
            String targetDatasetTypeName = "ERROR";
            try {
                targetDatasetTypeName = datasetConnection.getTargetDatasetTypeName();
            } catch (EmfException e) {
                e.printStackTrace();
            }
            Object[] values = { "dataset",
                                sourceDatasetTypeName, // source and target types must be the same
                                datasetConnection.getSourceName(),
                                datasetConnection.isOptional() ? "Yes" : "No",
                                targetDatasetTypeName,
                                datasetConnection.getTargetName(),
                                datasetConnection.getDescription() };
            Row row = new ViewableRow(datasetConnection, values);
            rows.add(row);
        }

        for (ModuleTypeVersionParameterConnection parameterConnection : moduleTypeVersion.getModuleTypeVersionParameterConnections().values()) {
            String sourceSqlType = "ERROR";
            try {
                sourceSqlType = parameterConnection.getSourceSqlType();
            } catch (EmfException e) {
                e.printStackTrace();
            }
            String targetSqlType = "ERROR";
            try {
                targetSqlType = parameterConnection.getTargetSqlType();
            } catch (EmfException e) {
                e.printStackTrace();
            }
            Object[] values = { "parameter",
                                sourceSqlType, // source and target types can be different
                                parameterConnection.getSourceName(),
                                parameterConnection.isOptional() ? "Yes" : "No",
                                targetSqlType,
                                parameterConnection.getTargetName(),
                                parameterConnection.getDescription() };
            Row row = new ViewableRow(parameterConnection, values);
            rows.add(row);
        }

        return rows;
    }
}
