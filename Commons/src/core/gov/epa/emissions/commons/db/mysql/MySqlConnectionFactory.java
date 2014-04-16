package gov.epa.emissions.commons.db.mysql;

import gov.epa.emissions.commons.db.ConnectionParams;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class MySqlConnectionFactory {

    private static MySqlConnectionFactory instance;

    private Map connections;

    public MySqlConnectionFactory() {
        connections = new HashMap();
    }

    private Connection createConnection(String host, String port, String dbName, String user, String password)
            throws SQLException {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException cnfx) {
            throw new SQLException("Can't load JDBC driver!");
        }

        String url = "jdbc:mysql://" + host + ((port != null) ? (":" + port) : "") + "/" + dbName;

        return DriverManager.getConnection(url, user, password);
    }

    public static MySqlConnectionFactory get() {
        if (instance == null)
            instance = new MySqlConnectionFactory();

        return instance;
    }

    public Connection getConnection(ConnectionParams params) throws SQLException {
        if (!connections.containsKey(params.getDbName())) {
            Connection connection = createConnection(params.getHost(), params.getPort(), params.getDbName(), params
                    .getUsername(), params.getPassword());
            connections.put(params.getDbName(), connection);
        }

        return (Connection) connections.get(params.getDbName());
    }

    public void close() {
        // ignored
    }
}
