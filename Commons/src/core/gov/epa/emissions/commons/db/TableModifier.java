package gov.epa.emissions.commons.db;

import gov.epa.emissions.commons.io.Column;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class TableModifier {

    protected Connection connection;

    protected String schema;

    protected Column[] columns;

    protected Statement statement;

    protected String tableName;
    
    protected TableDefinition tableDef;
    
    protected boolean stripPMPollutantPrimarySuffix = true; //default to yes

    public TableModifier(Datasource datasource, String tableName) throws SQLException {
        
        if ("emissions".equalsIgnoreCase(datasource.getName()) && "versions".equalsIgnoreCase(tableName)) {
            throw new SQLException("Table versions moved to schema emf."); // VERSIONS TABLE
        }
        
        this.connection = datasource.getConnection();
        this.schema = datasource.getName(); 
        // VERSIONS TABLE - completed: schema only used locally
        this.tableDef = datasource.tableDefinition();
        this.tableName = tableName;
        this.columns = new TableMetaData(datasource).getColumns(tableName);
        this.statement = connection.createStatement();
    }

    public TableModifier(Datasource datasource, String tableName, boolean stripPMPollutantPrimarySuffix) throws SQLException {
        this(datasource, tableName);
        this.stripPMPollutantPrimarySuffix = stripPMPollutantPrimarySuffix;
    }
    public void insert(String[] data) throws Exception {
        if (data.length > columns.length) {
            throw new Exception("Invalid number of data tokens - " + data.length + ". Number of columns in the table: "
                    + columns.length);
        }
        insertRow(tableName, data, columns);
    }

    public void close() throws SQLException {
        statement.close();
    }

    public void insertOneRow(String[] data) throws Exception {
        if (data.length > columns.length) {
            throw new Exception("Invalid number of data tokens - " + data.length + ". Number of columns in the table: "
                    + columns.length);
        }

        try {
            insertRow(tableName, data, columns);
        } finally {
            close();
        }
    }

    private String qualified(String table) throws SQLException {
        if ("emissions".equalsIgnoreCase(schema) && "versions".equalsIgnoreCase(tableName)) {
            throw new SQLException("Table versions moved to schema emf."); // VERSIONS TABLE
        }
        return schema + "." + table;
    }

    private void insertRow(String table, String[] data, Column[] cols) throws SQLException {
        StringBuffer insert = createInsertStatement(table, data, cols);
        execute(insert.toString());
    }

    protected StringBuffer createInsertStatement(String table, String[] data, Column[] cols) throws SQLException {
        if (data.length > cols.length)
            throw new SQLException("Invalid number of data tokens - " + data.length + ". Max: " + cols.length);

        StringBuffer insert = new StringBuffer();
        insert.append("INSERT INTO " + qualified(table) + " VALUES(");

        for (int i = 0; i < data.length; i++) {
            String colName = cols[i].name();

            if (isTypeString(cols[i])) {
                
                
                //strip off PM pollutant primary designation if so desired
//                if (colName.equalsIgnoreCase("POLL") && this.stripPMPollutantPrimarySuffix) {
//                    data[i] = data[i].toUpperCase();
//                    if (data[i].equals("PM10-PRI"))
//                        data[i] = "PM10";
//                    else if (data[i].equals("PM25-PRI"))
//                        data[i] = "PM2_5";             
//                    
                    
                if (colName.equalsIgnoreCase("FIPS")) {
                    // add a leading zero if it is missing
                    if (data[i].trim().length() == 4)
                        data[i] = "0" + data[i];
                } else if (!colName.equalsIgnoreCase("Lines")) {
                    data[i] = (data[i] != null ? data[i].trim() : "");
                }

                // make sure the value is not to big....
                if (data[i] != null && cols[i].sqlType().toUpperCase().startsWith("VARCHAR")) {
                    if (colName.equalsIgnoreCase("Lines") && data[i].length() > cols[i].width() && cols[i].width() > 0) {
                        data[i] = data[i].substring(0, cols[i].width());
                    } else if (data[i].trim().length() > cols[i].width() && cols[i].width() > 0) {
                        data[i] = data[i].trim().substring(0, cols[i].width());
                    }
                }

                data[i] = escapeString(data[i]);
            } else {
                if ((data[i] == null || (data[i].trim().length() == 0)))
                    data[i] = "DEFAULT";
                else if (isTypeTimeStamp(cols[i])) {
                    data[i] = (data[i] != null ? data[i].trim() : data[i]);
                    data[i] = escapeString(data[i]) + "::" + cols[i].sqlType();
                }
            }
            

            insert.append(data[i]);
            if (i < (data.length - 1))
                insert.append(',');
        }

        insert.append(')');// close parentheses around the query
        return insert;
    }

    private boolean isTypeTimeStamp(DbColumn column) {
        String sqlType = column.sqlType().toUpperCase();
        return sqlType.startsWith("TIMESTAMP");
    }

    private boolean isTypeString(DbColumn column) {
        String sqlType = column.sqlType().toUpperCase();
        return sqlType.startsWith("VARCHAR") || sqlType.equalsIgnoreCase("TEXT");
    }

    private String escapeString(String val) {
        if (val == null)
            return "''";

        String cleaned = val.replaceAll("\'", "''");
        return "'" + cleaned + "'";
    }

    private void execute(String query) throws SQLException {
        statement.execute(query);
    }

    public void dropData(String key, long value) throws SQLException {
        execute("DELETE FROM " + qualified(tableName) + " WHERE " + key + " = " + value);       
        cleanIfTableIsEmpty();
    }

    public void dropAllData() throws SQLException {
        execute("DELETE FROM " + qualified(tableName));
        
        cleanIfTableIsEmpty();
    }
    
    private void cleanIfTableIsEmpty() throws SQLException {
        if ( this.tableDef.totalRows(tableName)<1) {
            try {
                this.tableDef.dropTable(tableName);
                // clean table_consolidation
                execute("DELETE FROM emf.table_consolidations WHERE output_table = \'" + tableName + "\'");
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                throw new SQLException( e.getMessage());
            }
        }        
    }

}
