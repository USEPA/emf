package gov.epa.emissions.commons.db.postgres;

import gov.epa.emissions.commons.db.OptimizedQuery;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class OptimizedPostgresQuery implements OptimizedQuery {

    public Statement statement;

    public int rows;

    public int count;

    public String query;

    public ResultSet resultSet;

    public OptimizedPostgresQuery(Connection connection, String query, int rows) throws SQLException {
        this.rows = rows;
        this.count = 0;
        this.query = query;

        statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        statement.setFetchSize(rows);
        statement.setMaxRows(rows);
    }

    public boolean execute() throws SQLException {
        try {
            String currentQuery = query + " LIMIT " + rows + " OFFSET " + count;

            resultSet = statement.executeQuery(currentQuery);

            count += rows;
            return resultSet.isBeforeFirst();
        } catch (SQLException e) {
            throw new SQLException(e.getMessage());
        }
    }

    public ResultSet getResultSet() {
        return resultSet;
    }

    public void close() throws SQLException {
        statement.close();
    }

}
