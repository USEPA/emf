package gov.epa.emissions.commons.db.postgres;

import gov.epa.emissions.commons.db.DbColumn;
import gov.epa.emissions.commons.db.TableDefinition;
import gov.epa.emissions.commons.db.TableDefinitionDelegate;
import gov.epa.emissions.commons.io.TableMetadata;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.UUID;

public class PostgresTableDefinition implements TableDefinition {

    private String schema;

    private TableDefinitionDelegate delegate;

    private Connection connection;

    protected PostgresTableDefinition(String schema, Connection connection) {
        this.schema = schema;
        this.delegate = new TableDefinitionDelegate(connection);
        this.connection = connection;
    }

    public List getTableNames() throws SQLException {
        return delegate.getTableNames();
    }

    public void dropTable(String table) throws Exception {
        try {
            execute("DROP TABLE IF EXISTS " + qualified(table));
        } catch (SQLException e) {
            throw new Exception("Table " + qualified(table) + " could not be dropped" + "\n" + e.getMessage());
        }
    }

    private String qualified(String table) {
        if ("versions".equalsIgnoreCase(table.toLowerCase()) && "emissions".equalsIgnoreCase(schema.toLowerCase())) {
            System.err.println("Versions table moved to EMF- 001.");
        }
        return schema + "." + table;
    }

    public boolean tableExists(String table) throws Exception {
        return delegate.tableExist(table);
    }

    public void addIndex(String table, String indexName, String[] indexColumnNames) throws SQLException {
        StringBuffer query = new StringBuffer();
        // postgres indexes must be unique across tables/database
        String syntheticIndexName = table.replace('.', '_') + "_" + indexName;
        query.append("CREATE INDEX " + syntheticIndexName + " ON " + qualified(table) + " (" + indexColumnNames[0]);
        for (int i = 1; i < indexColumnNames.length; i++) {
            query.append(", " + indexColumnNames[i]);
        }
        query.append(")");

        execute(query.toString());
    }

    public void addIndex(String table, String colNameList, boolean clustered) {
        String guid = (UUID.randomUUID()).toString().replaceAll("-", "");
        String indexName = ("idx_" + guid);

        //create_table_index(table_name character varying, table_col_list character varying, index_name_prefix character varying, clustered boolean);
        String query = "SELECT public.create_table_index('" + table + "','" + colNameList + "','" + indexName + "'," + clustered + "::boolean);";
        try {
            execute(query);
        } catch (SQLException e) {
            //e.printStackTrace();
            //supress all errors, the indexes might already be on the table...
        } finally {
            //
        }
    }

    public void analyzeTable(String table) {
        
        String query = "analyze " + qualified(table) + ";";
        try {
            execute(query);
        } catch (SQLException e) {
            //e.printStackTrace();
            //supress all errors...
        } finally {
            //
        }
    }
    
    public void addColumn(String table, String columnName, String columnType, String afterColumnName) throws Exception {
        String statement = "ALTER TABLE " + qualified(table) + " ADD " + columnName + " " + columnType;
        execute(statement);
    }

    public void execute(final String query) throws SQLException {
        delegate.execute(query);
    }

    private String clean(String data) {
        return (data).replace('-', '_');
    }

    public void createTable(String table, DbColumn[] cols) throws SQLException {
        createTable(table, cols, -1);
    }

    public void createTable(String table, DbColumn[] cols, int datasetId) throws SQLException {
        String queryString = "CREATE TABLE " + qualified(table) + " (";

        queryString = addColumnsSpec(cols, queryString, datasetId);
        queryString += clean(cols[cols.length - 1].name()) + " " + cols[cols.length - 1].sqlType();

        queryString = queryString + ")";
        execute(queryString);
    }

    private String addColumnsSpec(DbColumn[] cols, String queryString, int datasetId) {
        for (int i = 0; i < cols.length - 1; i++) {
            queryString += clean(cols[i].name()) + " " + cols[i].sqlType();

            if (cols[i].hasConstraints()) {
                if (datasetId != -1 && cols[i].name().equalsIgnoreCase("Dataset_Id"))
                    queryString += " NOT NULL DEFAULT " + datasetId;
                else
                    queryString += " " + cols[i].constraints();
            }

            queryString += ", ";
        }// for i
        return queryString;
    }

    public TableMetadata getTableMetaData(String tableName) throws SQLException {
        return delegate.getTableMetaData(qualified(tableName));
    }

    public int totalRows(String tableName) throws SQLException {
        return delegate.totalRows(qualified(tableName));
    }

    public void renameTable(String table, String newName) throws SQLException {
        String renameQuery = "ALTER TABLE " + qualified(table) + " RENAME TO " + newName;
        execute(renameQuery);
    }

    public void deleteRecords(String table, String columnName, String columnType, String value) throws Exception {
        if (!columnType.toUpperCase().contains("INT"))
            value = "'" + value + "'";

        String deleteQuery = "DELETE FROM " + qualified(table) + " WHERE " + columnName + "=" + value;
        execute(deleteQuery);
    }

    public String checkTableConsolidations(int dsTypeId, String colNames, String colTypes, float sizeLimit)
            throws SQLException {
        String query = "SELECT output_table FROM emf.table_consolidations WHERE dataset_type_id=" + dsTypeId
                + " AND lower(col_names)='" + colNames.toLowerCase() 
                + "' AND lower(col_types)='" + colTypes.toLowerCase() + "'";

        Statement statement = null;
        ResultSet data = null;

        try {
            statement = connection.createStatement();
            data = statement.executeQuery(query);

            if (data == null)
                return null;

            if (data.next())
                return data.getString(1);
            
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Could not execute query-" + query + "\n" + e.getMessage());
        } finally {
            if (data != null)
                data.close();

            if (statement != null)
                statement.close();
        }
    }

    public ResultSet executeQuery(String query, Statement statement) throws SQLException {
        try {
            statement = connection.createStatement();
            return statement.executeQuery(query);
        } catch (SQLException e) {
            throw new SQLException("Could not execute query-" + query + "\n" + e.getMessage());
        }
    }

    public void addConsolidationItem(int dsTypeId, String table, int numCols, String colNames, String colTypes,
            int sizeLimit) throws SQLException {
        String query = "INSERT INTO emf.table_consolidations VALUES (DEFAULT, " + dsTypeId + ", '" + table + "', " + numCols + ", '" + colNames + "', '"
                + colTypes + "', " + sizeLimit + ")";
        execute(query);
    }
    
    public void updateConsolidationTable(int dsTypeId, String table) throws SQLException {
        String query = "UPDATE emf.table_consolidations SET number_records = (SELECT COUNT(*) FROM emissions." + table + 
            ") WHERE dataset_type_id = " + dsTypeId + " AND output_table = '" + table + "'";
        execute(query);
    }


}
