package gov.epa.emissions.commons.db;

import gov.epa.emissions.commons.io.TableMetadata;

import java.sql.SQLException;
import java.util.List;

public interface TableDefinition {

    List getTableNames() throws SQLException;
    
    void execute(final String query) throws SQLException;

    void createTable(String table, DbColumn[] cols) throws SQLException;
    
    void createTable(String table, DbColumn[] columns, int datasetId) throws SQLException;

    void renameTable(String table, String newName) throws SQLException;

    void dropTable(String table) throws SQLException, Exception;

    boolean tableExists(String tableName) throws Exception;

    TableMetadata getTableMetaData(String tableName) throws SQLException;
    
    int totalRows(String tableName) throws SQLException;

    /**
     * ALTER TABLE ADD INDEX indexName (indexColumnNames0, indexColumnNames1, ....)
     */
    void addIndex(String table, String indexName, String[] indexColumnNames) throws SQLException;

    void addIndex(String table, String colNameList, boolean clustered) throws SQLException;

    void analyzeTable(String table) throws SQLException;

    /**
     * Alter the table by adding a new column of the specified type in the specified location.
     * 
     * ALTER TABLE databaseName.tableName ADD columnName columnType [AFTER afterColumnName]
     * 
     * @param columnName -
     *            the name of the new column to add
     * @param columnType -
     *            the type of the new column
     * @param afterColumnName -
     *            the column name to add the new column after. Use null for default function (add to end)
     */
    void addColumn(String table, String columnName, String columnType, String afterColumnName) throws Exception;
    
    void deleteRecords(String table, String columnName, String columnType, String value) throws Exception;
    
    public String checkTableConsolidations(int dsTypeId, String colNames, String colTypes, float sizeLimit) throws SQLException;

    void addConsolidationItem(int id, String table, int numOfCols, String colNames, String colTypes, int sizeLimit) throws SQLException;

    void updateConsolidationTable(int dsTypeId, String table) throws SQLException;
}
