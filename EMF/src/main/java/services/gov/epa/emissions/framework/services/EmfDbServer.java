package gov.epa.emissions.framework.services;

import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.db.postgres.PostgresDbServer;
import gov.epa.emissions.framework.services.persistence.DataSourceFactory;

import java.sql.Connection;

import javax.sql.DataSource;

public class EmfDbServer implements DbServer {

    public static final String EMF_EMISSIONS_SCHEMA = "emissions";

    public static final String EMF_REFERENCE_SCHEMA = "reference";
    
    public static final String EMF_EMF_SCHEMA = "emf";

    public static final String EMF_CASE_SCHEMA = "cases";
    
    private DbServer dbServer;

    public EmfDbServer() throws Exception {
        this(null);
    }

    //NOTE: this constructor is setup only for junit test purpose
    public EmfDbServer(DbServerFactory dbServerFactory) throws Exception {
        if (dbServerFactory != null)
            dbServer = dbServerFactory.getDbServer();
        else {
            DataSource datasource = new DataSourceFactory().get();
            dbServer = new PostgresDbServer(datasource.getConnection(), EmfDbServer.EMF_REFERENCE_SCHEMA,
                    EmfDbServer.EMF_EMISSIONS_SCHEMA,EmfDbServer.EMF_EMF_SCHEMA);
        }
    }

    public Datasource getEmissionsDatasource() {
        return dbServer.getEmissionsDatasource();
    }

    public Datasource getReferenceDatasource() {
        return dbServer.getReferenceDatasource();
    }
    
    public Datasource getEmfDatasource() {
        return dbServer.getEmfDatasource();
    }

    public SqlDataTypes getSqlDataTypes() {
        return dbServer.getSqlDataTypes();
    }

    public String asciiToNumber(String asciiColumn, int precision) {
        return dbServer.asciiToNumber(asciiColumn,precision);
    }
    
    public void disconnect() throws Exception {
        dbServer.disconnect();
    }

    public boolean isConnected() throws Exception{
        // Check underlying db server connection
        return dbServer.isConnected();
    }

    public Connection getConnection() {
        return dbServer.getConnection();
    }

    public Datasource getSmsDatasource() {
        // NOTE Auto-generated method stub
        return dbServer.getSmsDatasource();
    }

    public Datasource getFastDatasource() {
        // NOTE Auto-generated method stub
        return dbServer.getFastDatasource();
    }
}