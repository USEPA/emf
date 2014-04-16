package gov.epa.emissions.commons.db;

import gov.epa.emissions.commons.io.Column;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DataModifier {

    protected Connection connection = null;

    private String schema;

    private JdbcToCommonsSqlTypeMap typeMap;

    private Statement statement;

    public DataModifier(String schema, Connection connection, SqlDataTypes types) {
        this.schema = schema;
        this.connection = connection;
        typeMap = new JdbcToCommonsSqlTypeMap(types);
    }

    public void execute(String sql) throws SQLException {
        Statement statement = connection.createStatement();
        try {
            statement.execute(sql);
        } catch (SQLException e) {
            throw new SQLException("Error executing query-" + sql + "\n" + e.getMessage());
        } finally {
            statement.close();
        }
    }

    public ResultSetMetaData getMetaData(String sql) throws SQLException {
        Statement statement = connection.createStatement();
        
        try {
            ResultSet rs = statement.executeQuery(sql);
            return rs.getMetaData();
        } catch (SQLException e) {
            throw new SQLException("Error executing query-" + sql + "\n" + e.getMessage());
        } finally {
            statement.close();
        }
    }
  
    //NOTE: specifically created for line-based dataset tables
    public double getLastRowLineNumber(String table) throws SQLException {
        Statement statement = connection.createStatement();
        String query = "SELECT line_number FROM " + table + " WHERE line_number=(SELECT MAX(line_number) FROM " + table + ")";
        
        try {
            ResultSet rs = statement.executeQuery(query);
            rs.next();
            
            return rs.getDouble(1);
        } catch (SQLException e) {
            throw new SQLException("Error executing query-" + query + "\n" + e.getMessage());
        } finally {
            statement.close();
        }
    }
    
    //NOTE: specifically created for line-based dataset tables
    public double getNextBiggerLineNumber(String table, double lineNumber) throws SQLException {
        Statement statement = connection.createStatement();
        String query = "SELECT line_number FROM " + table + " WHERE line_number > " + lineNumber + " ORDER BY line_number ASC LIMIT 1";
        
        try {
            ResultSet rs = statement.executeQuery(query);
            rs.next();
            
            if (rs.getRow() == 0)
                return -1.0;
            
            return rs.getDouble(1);
        } catch (SQLException e) {
            throw new SQLException("Error executing query-" + query + "\n" + e.getMessage());
        } finally {
            statement.close();
        }
    }
    
    public long getRowCount(String selectCountQuery) throws SQLException {
        Statement statement = connection.createStatement();
        
        try {
            ResultSet rs = statement.executeQuery(selectCountQuery);
            rs.next();
            
            return rs.getLong(1);
        } catch (SQLException e) {
            throw new SQLException("Error executing query-" + selectCountQuery + "\n" + e.getMessage());
        } finally {
            statement.close();
        }
    }
    
    public boolean resultExists(String query) throws SQLException {
        Statement statement = connection.createStatement();
        
        try {
            ResultSet rs = statement.executeQuery(query + " LIMIT 1");

            return rs.next();
        } catch (SQLException e) {
            throw new SQLException("Error executing query-" + query + "\n" + e.getMessage());
        } finally {
            statement.close();
        }
    }

    /**
     * UPDATE databaseName.tableName SET columnName = setExpr WHERE whereColumns[i] LIKE 'likeClauses[i]'
     * 
     * @param columnName -
     *            the column to update
     * @param setExpr -
     *            the expression used to update the column value
     * @param whereColumns -
     *            left hand sides of LIKE expressions for WHERE
     * @param likeClauses -
     *            right hand sides of LIKE expressions for WHERE
     * @throws Exception
     *             if encounter error updating table
     */
    public void updateWhereLike(String table, String columnName, String setExpr, String[] whereColumns,
            String[] likeClauses) throws Exception {
        if (whereColumns.length != likeClauses.length) {
            throw new Exception("There are different numbers of WHERE column names and LIKE clauses");
        }

        // instantiate a new string buffer in which the query would be created
        StringBuffer sb = new StringBuffer("UPDATE " + qualified(table) + " SET " + columnName + " = " + setExpr
                + " WHERE ");

        // add the first LIKE expression
        sb.append(whereColumns[0] + " LIKE '" + likeClauses[0] + "'");

        // if there is more than one LIKE expression, add
        // "AND" before each of the remaining expressions
        for (int i = 1; i < whereColumns.length; i++) {
            sb.append(" AND " + whereColumns[i] + " LIKE '" + likeClauses[i] + "'");
        }

        execute(sb.toString());
    }

    /**
     * UPDATE databaseName.tableName SET columnName = setExpr WHERE whereColumns[i] = equalsClauses[i]
     * 
     * @param columnName -
     *            the column to update
     * @param setExpr -
     *            the expression used to update the column value
     * @param whereColumns -
     *            left hand sides of = expressions for WHERE
     * @param equalsClauses -
     *            right hand sides of = expressions for WHERE
     * @throws Exception
     *             if encounter error updating table
     */
    public void updateWhereEquals(String table, String columnName, String setExpr, String[] whereColumns,
            String[] equalsClauses) throws Exception {
        if (whereColumns.length != equalsClauses.length) {
            throw new Exception("There are different numbers of WHERE column names and = clauses");
        }

        // instantiate a new string buffer in which the query would be created
        StringBuffer sb = new StringBuffer("UPDATE " + qualified(table) + " SET " + columnName + " = " + setExpr
                + " WHERE ");

        // add the first LIKE expression
        sb.append(whereColumns[0] + " = " + equalsClauses[0]);

        // if there is more than one LIKE expression, add
        // "AND" before each of the remaining expressions
        for (int i = 1; i < whereColumns.length; i++) {
            sb.append(" AND " + whereColumns[i] + " = " + equalsClauses[i]);
        }

        execute(sb.toString());
    }// updateWhereEquals(String, String, String[], String[])

    /**
     * Generate a concat expression for usage in SQL statements. If the value to be concatenated is a literal
     * (constant), it should be enclosed in ''.
     * 
     * @param exprs -
     *            Array of data ('literals' and column names)
     * @return the SQL concat expression
     */
    // FIXME: does this work with Postgres ?
    public String generateConcatExpr(String[] exprs) {
        StringBuffer concat = new StringBuffer("concat(");
        // add the first string
        concat.append(exprs[0]);
        for (int i = 1; i < exprs.length; i++) {
            concat.append("," + exprs[i]);
        }
        concat.append(")");

        return concat.toString();
    }

    // FIXME: remove, once Importers/Exporters are complete, used by legacy code
    public void insertRow(String table, String[] data, String[] colTypes) throws SQLException {
        StringBuffer insert = new StringBuffer();
        insert.append("INSERT INTO " + qualified(table) + " VALUES(");

        for (int i = 0; i < data.length; i++) {
            if (colTypes[i].startsWith("VARCHAR")) {
                String cleaned = data[i].replaceAll("\'", "''");
                insert.append("'" + cleaned + "'");
            } else {
                if (data[i].trim().length() == 0)
                    data[i] = "DEFAULT";
                insert.append(data[i]);
            }
            if (i < (data.length - 1))
                insert.append(',');
        }
        insert.append(')');// close parentheses around the query

        execute(insert.toString());
    }

    /**
     * Use 'insertRow(String table, String[] data) instead.
     */
    public void insertRow(String table, String[] data, DbColumn[] cols) throws SQLException {
        StringBuffer insert = createInsertStatement(table, data, cols);
        execute(insert.toString());
    }

    private StringBuffer createInsertStatement(String table, String[] data, DbColumn[] cols) throws SQLException {
        if (data.length > cols.length)
            throw new SQLException("Invalid number of data tokens - " + data.length + ". Max: " + cols.length);

        StringBuffer insert = new StringBuffer();
        insert.append("INSERT INTO " + qualified(table) + " VALUES(");

        for (int i = 0; i < data.length; i++) {
            if ( data[i] == null || (data[i].trim().length() == 0)) {
                if (cols[i].sqlType().startsWith("TIMESTAMP")) {
                    data[i]="NULL";
                }
                if (!isTypeString(cols[i])) { 
                    data[i] = "DEFAULT";
                }
            }
            
            if (!data[i].equalsIgnoreCase("NULL") && !data[i].equalsIgnoreCase("DEFAULT") && isTypeString(cols[i]) ) {
                data[i] = escapeString(data[i]);
            }

            insert.append(data[i]);

            if (i < (data.length - 1))
                insert.append(',');
        }

        insert.append(')');// close parentheses around the query
        return insert;
    }

    public void insertRow(String table, String[] data) throws SQLException {
        insertRow(table, data, getColumns(table));
    }

    public void initBatch() throws SQLException {
        statement = connection.createStatement();
    }

    public void addBatchInsert(String table, String[] data) throws SQLException {
        StringBuffer sql = createInsertStatement(table, data, getColumns(table));
        try {
            statement.addBatch(sql.toString());
        } catch (SQLException e) {
            throw new SQLException("Error executing query: " + sql + "\n" + e.getMessage());
        }
    }

    public void executeBatch() throws SQLException {
        try {
            statement.executeBatch();
        } finally {
            statement.close();
        }
    }

    public Column[] getColumns(String table) throws SQLException {
        
        // VERSIONS TABLE - Completed - throws exception if the following case is true
        if ("emissions".equalsIgnoreCase(schema) && "versions".equalsIgnoreCase(table.toLowerCase())) {
            throw new SQLException("Table versions moved to schema emf."); // VERSIONS TABLE
        }
        
        DatabaseMetaData meta = connection.getMetaData();
        // postgres driver creates table with lower case lettes and case sensitive
        ResultSet rs = meta.getColumns(null, schema, table.toLowerCase(), null);

        List cols = new ArrayList();
        try {
            while (rs.next()) {
                String name = rs.getString("COLUMN_NAME");
                int jdbcType = rs.getInt("DATA_TYPE");
                int width = rs.getInt("COLUMN_SIZE");
                String type = typeMap.get(jdbcType);
                cols.add(new Column(name, type, width));
            }
        } finally {
            rs.close();
        }
        return (Column[]) cols.toArray(new Column[0]);
    }

    private boolean isTypeString(DbColumn column) {
        String sqlType = column.sqlType();
        return sqlType.startsWith("VARCHAR") || sqlType.equalsIgnoreCase("TEXT") || sqlType.startsWith("TIMESTAMP");
    }

    private String escapeString(String val) {
        if (val == null)
            return "''";

        val = val.trim();
        String cleaned = val.replaceAll("\'", "''");
        return "'" + cleaned + "'";
    }

    public void dropData(String table, String key, long value) throws SQLException {
        execute("DELETE FROM " + qualified(table) + " WHERE " + key + " = " + value);
    }

    private String qualified(String table) throws SQLException {
        // VERSIONS TABLE - Completed - throws exception if the following case is true
        if ("emissions".equalsIgnoreCase(schema) && "versions".equalsIgnoreCase(table.toLowerCase())) {
            throw new SQLException("Table versions moved to schema emf."); // VERSIONS TABLE
        }
        return schema + "." + table;
    }

    public void dropAllData(String table) throws SQLException {
        execute("DELETE FROM " + qualified(table));
    }
    
//    private void cleanIfTableIsEmpty() throws SQLException {
//        if ( this.tableDef.totalRows(tableName)<1) {
//            try {
//                this.tableDef.dropTable(tableName);
//                // clean table_consolidation
//                execute("DELETE FROM emf.table_consolidations WHERE output_table = " + tableName);
//            } catch (Exception e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//                throw new SQLException( e.getMessage());
//            }
//        }        
//    }

}
