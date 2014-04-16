package gov.epa.emissions.commons.db;

import gov.epa.emissions.commons.Config;

import java.sql.Connection;
import java.sql.DriverManager;

import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;

public class DbOperation {

    protected DatabaseConnection connection;

    public DbOperation(Connection jdbcConnection) {
        this(new DatabaseConnection(jdbcConnection));
    }

    public DbOperation(DatabaseConnection connection) {
        this.connection = connection;

        DatabaseConfig dbUnitConfig = connection.getConfig();
        dbUnitConfig.setFeature(DatabaseConfig.FEATURE_QUALIFIED_TABLE_NAMES, true);
    }

    public DbOperation(Config config) throws Exception {
        Class.forName(config.driver());
        Connection jdbcConnection = DriverManager.getConnection(config.url(), config.username(), config.password());

        DatabaseConnection dbUnitConnection = new DatabaseConnection(jdbcConnection);
        DatabaseConfig dbUnitConfig = dbUnitConnection.getConfig();
        dbUnitConfig.setFeature(DatabaseConfig.FEATURE_QUALIFIED_TABLE_NAMES, true);

        this.connection = dbUnitConnection;
    }
}