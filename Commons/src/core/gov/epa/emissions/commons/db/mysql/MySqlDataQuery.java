package gov.epa.emissions.commons.db.mysql;

import gov.epa.emissions.commons.db.DataQuery;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MySqlDataQuery implements DataQuery {

    private Connection connection;
    private String schema;

    public MySqlDataQuery(String schema, Connection connection) {
        this.schema = schema;
        this.connection = connection;
    }

    public ResultSet executeQuery(String query) throws SQLException {
        Statement statement = connection.createStatement();
        return statement.executeQuery(query);
    }

    public void execute(String query) throws SQLException {
        Statement statement = connection.createStatement();
        statement.execute(query);
    }

    public ResultSet select(String[] columnNames, String table) throws SQLException {
        final String selectPrefix = "SELECT ";
        StringBuffer sb = new StringBuffer(selectPrefix);
        sb.append(columnNames[0]);
        for (int i = 1; i < columnNames.length; i++) {
            sb.append("," + columnNames[i]);
        }
        String fromSuffix = " FROM ";
        try {
            fromSuffix += qualified(table);
        } catch (Exception e) {
            e.printStackTrace();
        }
        sb.append(fromSuffix);

        Statement statement = connection.createStatement();
        statement.execute(sb.toString());
        ResultSet results = statement.getResultSet();

        return results;
    }

    private String qualified(String table) throws Exception {
        if ("versions".equalsIgnoreCase(table.toLowerCase()) && "emissions".equalsIgnoreCase(schema.toLowerCase())) {
            throw new Exception("Versions table moved to EMF.");
        }
        return schema + "." + table;
    }

    public ResultSet selectAll(String table) throws SQLException {
        return select(new String[] { "*" }, table);
    }

    public ResultSet executeUpdateQuery(String query) throws SQLException {
        // TODO Auto-generated method stub
        throw new SQLException("executeUpdateQuery() method not implemented for MySQL");
    }
}
