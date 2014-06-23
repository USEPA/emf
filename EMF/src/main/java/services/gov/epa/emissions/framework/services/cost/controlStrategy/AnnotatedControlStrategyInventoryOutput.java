package gov.epa.emissions.framework.services.cost.controlStrategy;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.commons.io.VersionedQuery;
import gov.epa.emissions.commons.io.importer.DataTable;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.ControlStrategyInputDataset;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;
import gov.epa.emissions.framework.tasks.DebugLevels;

import java.sql.SQLException;

public class AnnotatedControlStrategyInventoryOutput extends AbstractControlStrategyInventoryOutput {

    public AnnotatedControlStrategyInventoryOutput(User user, ControlStrategy controlStrategy,
            ControlStrategyResult controlStrategyResult, String namePrefix, 
            HibernateSessionFactory sessionFactory, DbServerFactory dbServerFactory) throws Exception {
        super(user, controlStrategy,
                controlStrategyResult, namePrefix,
                sessionFactory, dbServerFactory);
    }

    public void create() throws Exception {
        doCreateInventory(inputDataset, getDatasetTableName(inputDataset));
    }

    protected void doCreateInventory(EmfDataset inputDataset, String inputTable) throws EmfException, Exception, SQLException {
        startStatus(statusServices);
        try {
            EmfDataset dataset = creator.addControlledInventoryDataset(creator.createControlledInventoryDatasetName(namePrefix, inputDataset), 
                    inputDataset, inputDataset.getDatasetType(), 
                    tableFormat, description(inputDataset));
            
            String outputInventoryTableName = getDatasetTableName(dataset);
            
            ControlStrategyResult result = getControlStrategyResult(controlStrategyResult.getId());
            //make sure annotated inventory has been indexed...
            createIndexes(result.getDetailedResultDataset());
            try {
                createControlledInventory(dataset.getId(), inputTable, detailDatasetTable(result),
                        outputInventoryTableName, version(inputDataset, controlStrategyResult.getInputDatasetVersion()),
                        inputDataset, datasource);
            } catch (Exception e) {
                createControlledInventory(dataset.getId(), inputTable, detailDatasetTable(result),
                        outputInventoryTableName, version(inputDataset, controlStrategyResult.getInputDatasetVersion()),
                        inputDataset, datasource, true);
            }        

            setControlStrategyResultContolledInventory(result, dataset);
            updateVersion(dataset, dbServer, sessionFactory.getSession(), user);
        } catch (Exception e) {
            failStatus(statusServices, e.getMessage());
            e.printStackTrace();
            throw e;
        } finally {
//            setandRunQASteps();
        }
        dbServer.disconnect();
    }

    private void createIndexes(Dataset dataset) throws EmfException {
        DataTable dataTable = new DataTable(dataset, datasource);
        String table = getDatasetTableName(dataset);

        //ALWAYS create indexes for these core columns...
        dataTable.addIndex(table, "record_id", true);
        dataTable.addIndex(table, "dataset_id", false);
        dataTable.addIndex(table, "version", false);
        dataTable.addIndex(table, "delete_versions", false);

        dataTable.addIndex(table, "fips", false);
        dataTable.addIndex(table, "poll", false);
        dataTable.addIndex(table, "scc", false);
        dataTable.addIndex(table, "plantid", false);
        dataTable.addIndex(table, "pointid", false);
        dataTable.addIndex(table, "stackid", false);
        dataTable.addIndex(table, "segment", false);
        dataTable.addIndex(table, "mact", false);
        dataTable.addIndex(table, "sic", false);

        //finally analyze the table, so the indexes take affect immediately, 
        //NOT when the SQL engine gets around to analyzing eventually
        dataTable.analyzeTable(table);
    }

    private void createControlledInventory(int datasetId, String inputTable, String detailResultTable, String outputTable, Version version,
            Dataset dataset, Datasource datasource) throws EmfException {
        createControlledInventory(datasetId, inputTable, 
                detailResultTable, outputTable, 
                version, dataset, 
                datasource, false);
    }

    private void createControlledInventory(int datasetId, String inputTable, String detailResultTable, String outputTable, Version version,
            Dataset dataset, Datasource datasource, boolean missingColumns) throws EmfException {
        String query = populateInventory(datasetId, inputTable, 
                detailResultTable, outputTable,
                version(inputDataset, controlStrategyResult.getInputDatasetVersion()), inputDataset, 
                datasource, missingColumns);
        try {
            datasource.query().execute(query);
        } catch (SQLException e) {
            throw new EmfException("Error occured when copying data from " + inputTable + " to "
                    + outputTable + "\n" + e.getMessage());
        }
    }

    private String populateInventory(int datasetId, String inputTable, String detailResultTable, String outputTable, Version version,
            Dataset dataset, Datasource datasource, boolean missingColumns) throws EmfException {
        VersionedQuery versionedQuery = new VersionedQuery(version);
        String sql = "select ";
        String columnList = "";
        Column[] columns = tableFormat.cols();
        //right before abbreviation, is an empty now...
        for (int i = 0; i < columns.length; i++) {
            String columnName = columns[i].name();
            if (columnName.equalsIgnoreCase("record_id")) {
                sql += "inv.record_id";
                columnList += "record_id";
            } else if (columnName.equalsIgnoreCase("dataset_id")) {
                sql += "," + datasetId + " as dataset_id";
                columnList += ",dataset_id";
            } else if (columnName.equalsIgnoreCase("delete_versions")) {
                sql += ", '' as delete_versions";
                columnList += "," + columnName;
            } else if (columnName.equalsIgnoreCase("version")) {
                sql += ", 0 as version";
                columnList += "," + columnName;
            } else if (columnName.equalsIgnoreCase("CONTROL_MEASURES")) {
                sql += ", case when " + (!missingColumns ? "control_measures" : "null") + " is null or length(" + (!missingColumns ? "control_measures" : "null") + ") = 0 then cm_abbrev_list else " + (!missingColumns ? "control_measures" : "null") + " || '&' || cm_abbrev_list end as CONTROL_MEASURES";
                columnList += "," + columnName;
            } else if (columnName.equalsIgnoreCase("PCT_REDUCTION")) {
                sql += ", case when " + (!missingColumns ? "pct_reduction" : "null") + " is null or length(" + (!missingColumns ? "pct_reduction" : "null") + ") = 0 then percent_reduction_list else " + (!missingColumns ? "pct_reduction" : "null") + " || '&' || percent_reduction_list end as PCT_REDUCTION";
                columnList += "," + columnName;
            } else {
                sql += ", " + columnName;
                columnList += "," + columnName;
            }
        }
        sql += " FROM " + qualifiedTable(inputTable, datasource) + " as inv ";
        sql += " left outer join ( "
        + "SELECT record_id, "
        + "control_measures as cm_abbrev_list, "
        + "pct_reduction as percent_reduction_list "
        + "FROM " + qualifiedTable(detailResultTable, datasource)
        + ") as b "
        + "on inv.record_id = b.record_id"
        + " WHERE " + versionedQuery.query();
        sql = "INSERT INTO " + qualifiedTable(outputTable, datasource) + " (" + columnList + ") " + sql;
        if (DebugLevels.DEBUG_25()) 
            System.out.println(sql);
        return sql;
    }
}
