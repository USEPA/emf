package gov.epa.emissions.commons.db.postgres;

import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.SqlDataTypes;

import java.sql.Connection;

//Note: Emissions & Reference are two schemas in a single database i.e. share a connection
public class PostgresDbServer implements DbServer {

    private SqlDataTypes types;

    private Datasource emissionsDatasource;

    private Datasource referenceDatasource;

    private Datasource emfDatasource;
    
    private Datasource smsDatasource;
    
    private Datasource fastDatasource;

    private Connection connection;
    
    private final String EMF_FAST_RUN_SCHEMA = "fast";
    
    private final String EMF_SECTOR_SCENARIO_SCHEMA = "sms";

    public PostgresDbServer(Connection connection, String referenceDatasourceName, String emissionsDatasourceName,
            String emfDatasourceName) {
        this.types = new PostgresSqlDataTypes();
        this.connection = connection;

        referenceDatasource = createDatasource(referenceDatasourceName, connection);
        emissionsDatasource = createDatasource(emissionsDatasourceName, connection);
        emfDatasource = createDatasource(emfDatasourceName, connection);
        smsDatasource = createDatasource(EMF_SECTOR_SCENARIO_SCHEMA, connection);
        fastDatasource = createDatasource(EMF_FAST_RUN_SCHEMA, connection);
    }

    public Datasource getEmissionsDatasource() { 
        
        return emissionsDatasource; // VERSIONS TABLE its name is emissions
    }

    public Datasource getReferenceDatasource() {
        return referenceDatasource;
    }

    public Datasource getEmfDatasource() { 
        
        return emfDatasource;
    }
    
    public Datasource getSmsDatasource() {
        return smsDatasource;
    }
    
    public Datasource getFastDatasource() {
        return fastDatasource;
    }

    private Datasource createDatasource(String datasourceName, Connection connection) {
        return new PostgresDatasource(datasourceName, connection, types);
    }

    public SqlDataTypes getSqlDataTypes() {
        return types;
    }

    public String asciiToNumber(String asciiColumn, int precision) {
        StringBuffer precisionBuf = new StringBuffer();
        for (int i = 0; i < precision; i++) {
            precisionBuf.append('9');
        }

        return "to_number(" + asciiColumn + ", '" + precisionBuf.toString() + "')";
    }

    public void disconnect() throws Exception {
        if (isConnected()) {
            connection.close();
            connection = null;
        }
    }

    public boolean isConnected() throws Exception {
        // Check connection
        return connection != null && !connection.isClosed();
    }

    public Connection getConnection() {
        return connection;
    }

}
