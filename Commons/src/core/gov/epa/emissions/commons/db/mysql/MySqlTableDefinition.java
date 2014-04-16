package gov.epa.emissions.commons.db.mysql;

import gov.epa.emissions.commons.db.DbColumn;
import gov.epa.emissions.commons.db.TableDefinition;
import gov.epa.emissions.commons.db.TableDefinitionDelegate;
import gov.epa.emissions.commons.io.TableMetadata;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class MySqlTableDefinition implements TableDefinition {

    private String schema;

    private TableDefinitionDelegate delegate;

    public MySqlTableDefinition(String schema, Connection connection) {
        this.schema = schema;
        this.delegate = new TableDefinitionDelegate(connection);
    }

    public List getTableNames() throws SQLException {
        return delegate.getTableNames();
    }

    public void deleteTableQuietly(String table) {
        try {
            execute("DROP TABLE IF EXISTS " + qualified(table));
        } catch (SQLException e) {
            System.err.println("Could not delete table - " + table + ". Ignoring..");
        }
    }

    public void dropTable(String table) throws SQLException {
        execute("DROP TABLE " + qualified(table));
    }

    public boolean tableExists(String table) throws SQLException {
        return delegate.tableExist(table);
    }

    public void addIndex(String table, String indexName, String[] indexColumnNames) throws SQLException {
        // instantiate a new string buffer in which the query would be created
        StringBuffer sb = new StringBuffer("ALTER TABLE " + qualified(table) + " ADD ");
        final String INDEX = "INDEX ";

        sb.append(INDEX + indexName + "(" + indexColumnNames[0]);
        for (int i = 1; i < indexColumnNames.length; i++) {
            sb.append(", " + indexColumnNames[i]);
        }
        sb.append(")");

        execute(sb.toString());
    }

    public void addColumn(String table, String columnName, String columnType, String afterColumnName) throws Exception {
        // instantiate a new string buffer in which the query would be created
        StringBuffer sb = new StringBuffer("ALTER TABLE " + qualified(table) + " ADD ");
        final String AFTER = " AFTER ";

        sb.append(columnName + " " + columnType);
        if (afterColumnName != null) {
            sb.append(AFTER + afterColumnName);
        }// if

        execute(sb.toString());
    }

    private String clean(String dirtyStr) {
        return dirtyStr.replace('-', '_');
    }

    public void execute(final String query) throws SQLException {
        delegate.execute(query);
    }

    public void createTable(String table, DbColumn[] cols) throws SQLException {
        String queryString = "CREATE TABLE " + qualified(table) + " (";

        for (int i = 0; i < cols.length - 1; i++) {
            queryString += clean(cols[i].name()) + " " + cols[i].sqlType() + ", ";
        }
        queryString += clean(cols[cols.length - 1].name()) + " " + cols[cols.length - 1].sqlType();

        queryString = queryString + ")";
        execute(queryString);
    }

    private String qualified(String table) {
        if ("versions".equalsIgnoreCase(table.toLowerCase()) && "emissions".equalsIgnoreCase(schema.toLowerCase())) {
            System.err.println("Versions table moved to EMF- 001.");
        }
        return schema + "." + table;
    }

    public TableMetadata getTableMetaData(String tableName) {
        // TODO Auto-generated method stub
        return null;
    }

    public int totalRows(String tableName) throws SQLException {
        return delegate.totalRows(qualified(tableName));
    }

    public void renameTable(String table, String newName) throws SQLException {
        String renameQuery = "ALTER TABLE " + table + " RENAME TO " + newName;
        execute(renameQuery);
    }

    public void createTable(String table, DbColumn[] columns, int datasetId) throws SQLException {
        // TODO Auto-generated method stub
        throw new SQLException("EMF method is not implemented.");
    }

    public void deleteRecords(String table, String columnName, String columnType, String value) throws Exception {
        // TODO Auto-generated method stub
        
    }

    public String checkTableConsolidations(int dsTypeId, String colNames, String colTypes, float sizeLimit) {
        // TODO Auto-generated method stub
        return null;
    }

    public void addConsolidationItem(int id, String table, int numOfCols, String colNames, String colTypes, int sizeLimit) {
        // TODO Auto-generated method stub
        
    }

    public void updateConsolidationTable(int dsTypeId, String table) {
        // TODO Auto-generated method stub
        
    }

    public void addIndex(String table, String colList, boolean clustered) {
        // TODO Auto-generated method stub
        
    }

    public void analyzeTable(String table) {
        // TODO Auto-generated method stub
        
    }

}
