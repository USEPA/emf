package gov.epa.emissions.commons.db;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.io.TableFormat;
import gov.epa.emissions.commons.io.importer.ImporterException;

import java.sql.SQLException;

public class TableCreator {

    private Datasource datasource;

    public TableCreator(Datasource datasource) {
        this.datasource = datasource;
    }

    public void create(String table, TableFormat tableFormat) throws Exception {
        create(table, tableFormat, -1);
    }

    public void create(String table, TableFormat tableFormat, int datasetId) throws Exception {
        TableDefinition tableDefinition = datasource.tableDefinition();
        checkTableExist(tableDefinition, table);
        try {
            if (datasetId != -1)
                tableDefinition.createTable(table, tableFormat.cols(), datasetId);
            else
                tableDefinition.createTable(table, tableFormat.cols());
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
    }

    public void rename(String table, String newName) throws Exception {
        TableDefinition tableDefinition = datasource.tableDefinition();
        checkTableExist(tableDefinition, newName);
        try {
            tableDefinition.renameTable(table, newName);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new Exception("could not rename table - " + table + " to " + newName + "\n" + e.getMessage(), e);
        }

    }

    private void checkTableExist(TableDefinition tableDefinition, String table) throws Exception {
        try {
            if (tableDefinition.tableExists(table)) {
                throw new ImporterException("Table '" + table
                        + "' already exists in the database, and cannot be created");
            }
        } catch (Exception e) {
            throw e;
        }

    }

    /* table name(without the schema name) */
    public void drop(String table) throws Exception {
        TableDefinition def = datasource.tableDefinition();
        def.dropTable(table);
    }

    public boolean exists(String table) throws Exception {
        return datasource.tableDefinition().tableExists(table);
    }
    
    public void deleteRecords(String table, String columnName, String columnType, String value) throws Exception {
        TableDefinition def = datasource.tableDefinition();
        def.deleteRecords(table, columnName, columnType, value);
    }

    public String checkTableConsolidation(String colNames, String colTypes, Dataset dataset) throws Exception {
        return datasource.tableDefinition().checkTableConsolidations(dataset.getDatasetType().getId(), colNames, colTypes, 0);
    }
    
    public void addConsolidationItem(int numOfCols, String table, String colNames, String colTypes, Dataset dataset) throws Exception {
        datasource.tableDefinition().addConsolidationItem(dataset.getDatasetType().getId(), table, numOfCols, colNames, colTypes, 0);
    }
    
    public void updateConsolidationTable(int datasetTypeId, String table) throws SQLException {
        datasource.tableDefinition().updateConsolidationTable(datasetTypeId, table);
    }

    public void addIndex(String table, String colNameList, boolean clustered)  {
        try {
            datasource.tableDefinition().addIndex(table, colNameList, clustered);
        } catch (Exception e) {
            //suppress exceptions
//            throw new Exception("could not add table index, table = " + table + ", column list = " + colNameList + ", " + e.getMessage());
        }
    }

    public void analyzeTable(String table) throws Exception {
        try {
            datasource.tableDefinition().analyzeTable(table);
        } catch (Exception e) {
            throw new Exception("could not analyze table, table = " + table + ", " + e.getMessage());
        }
    }

}
