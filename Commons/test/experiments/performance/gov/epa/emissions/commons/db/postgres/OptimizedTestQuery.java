package gov.epa.emissions.commons.db.postgres;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class OptimizedTestQuery {

    private Connection connection;

    private Statement statement;

    private int rows;

    private int count;

    private String query;

    public OptimizedTestQuery(Connection connection) {
        this.connection = connection;
    }

    public void init(String query, int rows) throws SQLException {
        this.rows = rows;
        this.query = query;
        connection.setReadOnly(true);
        statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        // Statement statement = connection.createStatement();
        statement.setFetchSize(rows);
        statement.setMaxRows(rows);
    }

    public ResultSet execute() throws SQLException {
        String currentQuery = query + " LIMIT " + rows + " OFFSET " + count;

        System.out.println("used memory(before query): "
                + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024));
        ResultSet resultSet = statement.executeQuery(currentQuery);
        System.out.println("used memory(after query): "
                + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024));

        count += rows;
        return resultSet;
    }

    public void close() throws SQLException {
        statement.close();
        connection.close();
    }

}
