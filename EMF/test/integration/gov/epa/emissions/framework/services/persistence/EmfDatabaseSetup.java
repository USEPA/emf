package gov.epa.emissions.framework.services.persistence;

import gov.epa.emissions.commons.db.ConnectionParams;
import gov.epa.emissions.commons.db.DatabaseSetup;
import gov.epa.emissions.commons.db.postgres.PostgresConnectionFactory;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

import javax.sql.DataSource;

public class EmfDatabaseSetup extends DatabaseSetup {

    protected DataSource emfDatasource;

    private Connection connection;

    public EmfDatabaseSetup(Properties properties) throws SQLException {
        super(properties);

        connection = connection(properties);
        this.emfDatasource = createEmfDataSource(connection);
    }

    private Connection connection(Properties properties) throws SQLException {
        ConnectionParams params = params(properties);
        PostgresConnectionFactory factory = PostgresConnectionFactory.get(params);
        Connection connection = factory.getConnection();

        return connection;
    }

    public DataSource emfDatasource() {
        return emfDatasource;
    }

    public void tearDown() throws SQLException {
        try {
            super.tearDown();
        } catch (Exception e) {
            throw new SQLException(e.getMessage());
        }
    }

    private ConnectionParams params(Properties pref) {
        String dbName = pref.getProperty("database.name");
        String host = pref.getProperty("database.host");
        String port = pref.getProperty("database.port");
        String username = pref.getProperty("database.username");
        String password = pref.getProperty("database.password");

        ConnectionParams params = new ConnectionParams(dbName, host, port, username, password);
        return params;
    }

    private DataSource createEmfDataSource(final Connection connection) {
        return new DataSource() {
            public int getLoginTimeout() {
                return 0;
            }

            public void setLoginTimeout(int arg0) {// no op
            }

            public PrintWriter getLogWriter() {
                return null;
            }

            public void setLogWriter(PrintWriter arg0) {// no op
            }

            public Connection getConnection() {
                return connection;
            }

            public Connection getConnection(String arg0, String arg1) {
                return connection;
            }

            public boolean isWrapperFor(Class<?> arg0) {
                // NOTE Auto-generated method stub
                return false;
            }

            public <T> T unwrap(Class<T> arg0) {
                // NOTE Auto-generated method stub
                return null;
            }

            public Logger getParentLogger() throws SQLFeatureNotSupportedException {
                // NOTE Auto-generated method stub
                return null;
            }
        };
    }

}
