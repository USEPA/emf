package gov.epa.emissions.commons.db;

import gov.epa.emissions.commons.db.mysql.MySqlDbServer;
import gov.epa.emissions.commons.db.mysql.MySqlDbUpdate;
import gov.epa.emissions.commons.db.mysql.MySqlTableReader;
import gov.epa.emissions.commons.db.postgres.PostgresConnectionFactory;
import gov.epa.emissions.commons.db.postgres.PostgresDbServer;
import gov.epa.emissions.commons.db.postgres.PostgresDbUpdate;
import gov.epa.emissions.commons.db.postgres.PostgresTableReader;

import java.sql.SQLException;
import java.util.Properties;

public class DatabaseSetup {

    private DbServer dbServer;

    private String dbType;
    
    private ConnectionParams params;
    
    private String emissionsDatasource;
    
    private String referenceDatasource;
    
    private String emfDatasource;

    public DatabaseSetup(Properties pref) throws SQLException {
        dbType = pref.getProperty("database.type");

        emissionsDatasource = pref.getProperty("datasource.emissions.name");
        referenceDatasource = pref.getProperty("datasource.reference.name");
        emfDatasource = pref.getProperty("datasource.emf.name");

        String dbName = pref.getProperty("database.name");
        String host = pref.getProperty("database.host");
        String port = pref.getProperty("database.port");
        String username = pref.getProperty("database.username");
        String password = pref.getProperty("database.password");
        //FIXME: emfDatasource is not implemented in MySql
        if (isMySql()) {
            ConnectionParams emissionParams = new ConnectionParams(emissionsDatasource, host, port, username, password);
            ConnectionParams referenceParams = new ConnectionParams(referenceDatasource, host, port, username, password);
            createMySqlDbServer(emissionParams, referenceParams);
        } else {
            params = new ConnectionParams(dbName, host, port, username, password);
            createPostgresDbServer(emissionsDatasource, referenceDatasource,emfDatasource, params);
        }
    }

    private boolean isMySql() {
        return dbType.equals("mysql");
    }

    private void createPostgresDbServer(String emissionsDatasource, String referenceDatasource, String emfDatasource, ConnectionParams params)
            throws SQLException {
        PostgresConnectionFactory factory = PostgresConnectionFactory.get(params);
        dbServer = new PostgresDbServer(factory.getConnection(), referenceDatasource, emissionsDatasource,emfDatasource);
    }

    public DbServer getNewPostgresDbServerInstance() throws SQLException {
        PostgresConnectionFactory factory = PostgresConnectionFactory.get(params);
        return new PostgresDbServer(factory.getConnection(), referenceDatasource, emissionsDatasource,emfDatasource);
    }

    private void createMySqlDbServer(ConnectionParams emissionParams, ConnectionParams referenceparams)
            throws SQLException {
        dbServer = new MySqlDbServer(emissionParams, referenceparams);
    }

    public DbServer getDbServer() {
        return dbServer;
    }

    public void tearDown() throws Exception {
        dbServer.disconnect();
    }

    public TableReader tableReader(Datasource datasource) {
        if (isMySql())
            return new MySqlTableReader(datasource.getConnection());

        return new PostgresTableReader(datasource.getConnection());
    }

    public DbUpdate dbUpdate(Datasource datasource) throws Exception {
        if (isMySql())
            return new MySqlDbUpdate(datasource.getConnection());
        
        return new PostgresDbUpdate(datasource.getConnection());
    }
}
