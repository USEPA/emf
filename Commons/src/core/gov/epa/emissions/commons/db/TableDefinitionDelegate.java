package gov.epa.emissions.commons.db;

import gov.epa.emissions.commons.io.ColumnMetaData;
import gov.epa.emissions.commons.io.TableMetadata;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class TableDefinitionDelegate {

    private Connection connection;

    public TableDefinitionDelegate(Connection connection) {
        this.connection = connection;
    }

    /*
     * This method reads the table metadata and returns the column meta data values (label, size and type/class) in a
     * TableMetaData object
     */
    public TableMetadata getTableMetaData(String table) throws SQLException {
        String query = "SELECT * FROM " + table;
        TableMetadata tableMeta = new TableMetadata(table);

        Statement statement = connection.createStatement();
        statement.setMaxRows(1);// optimized query
        ResultSet rs = statement.executeQuery(query);
        try {
            ResultSetMetaData rsmd = rs.getMetaData();
            int colCount = rsmd.getColumnCount();

            for (int i = 1; i <= colCount; i++) {
                ColumnMetaData col = new ColumnMetaData(rsmd.getColumnLabel(i), rsmd.getColumnClassName(i), rsmd
                        .getColumnDisplaySize(i));
                tableMeta.addColumnMetaData(col);
            }
        } finally {
            rs.close();
            statement.close();
        }

        return tableMeta;
    }

    public List getTableNames() throws SQLException {
        List tableNames = new ArrayList();
        DatabaseMetaData metaData = connection.getMetaData();
        ResultSet tables = metaData.getTables(null, null, null, new String[] { "TABLE" });
        while (tables.next()) {
            String tableName = tables.getString("TABLE_NAME");
            tableNames.add(tableName);
        }
        return tableNames;
    }

    public boolean tableExist(String table) throws SQLException {
        List tableNames = getTableNames();
        for (int i = 0; i < tableNames.size(); i++) {
            String name = (String) tableNames.get(i);
            if (name.equalsIgnoreCase(table))
                return true;
        }
        return false;
    }

    public void execute(String query) throws SQLException {
        Statement statement = null;
        try {
            statement = connection.createStatement();
            statement.execute(query);
        } catch (SQLException e) {
            throw new SQLException("Could not execute query-" + query + "\n" + e.getMessage());
        } finally {
            if (statement != null)
                statement.close();
        }
    }
    
    public int totalRows(String qualfiedTableName) throws SQLException {
        String query = "SELECT COUNT(*) FROM " + qualfiedTableName;
        Statement statement = null;
        try {
            statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(query);
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            throw new SQLException("Could not execute query-" + query + "\n" + e.getMessage());
        } finally {
            if (statement != null)
                statement.close();
        }
    }
}
