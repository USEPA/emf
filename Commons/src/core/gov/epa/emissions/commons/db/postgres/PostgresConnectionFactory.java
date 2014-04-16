package gov.epa.emissions.commons.db.postgres;

import gov.epa.emissions.commons.db.ConnectionFactory;
import gov.epa.emissions.commons.db.ConnectionParams;

import java.sql.Connection;
import java.sql.SQLException;

import org.postgresql.ds.PGPoolingDataSource;

public class PostgresConnectionFactory implements ConnectionFactory {

    private static PostgresConnectionFactory instance;

    private PGPoolingDataSource source;

    private PostgresConnectionFactory(ConnectionParams params) {
        source = new PGPoolingDataSource();
        source.setServerName(params.getHost());
        source.setDatabaseName(params.getDbName());
        source.setUser(params.getUsername());
        source.setPassword(params.getPassword());
        source.setInitialConnections(2);
        source.setPortNumber(Integer.parseInt(params.getPort()));
    }

    // NOTE: the only reason why this is a Singleton - need to have
    // a pool of DB connections for the entire test suite. Could not find
    // a simple, intuitive way to do it.
    public static PostgresConnectionFactory get(ConnectionParams params) {
        if (instance == null)
            instance = new PostgresConnectionFactory(params);

        return instance;
    }

    public Connection getConnection() throws SQLException {
        Connection connection = source.getConnection();
        connection.setAutoCommit(true);
        
        return connection;
    }
}
