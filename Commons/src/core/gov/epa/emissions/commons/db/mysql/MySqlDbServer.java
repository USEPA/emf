package gov.epa.emissions.commons.db.mysql;

import gov.epa.emissions.commons.db.ConnectionParams;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.SqlDataTypes;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Note: Emissions & Reference are two schemas in a single database i.e. share a connection. A datasource is represented
 * by a schema in MySql, and Database == Schema
 */
public class MySqlDbServer implements DbServer {

    private SqlDataTypes types;

    private Datasource emissionsDatasource;

    private Datasource referenceDatasource;

    public MySqlDbServer(ConnectionParams emissionParams, ConnectionParams referenceParams) throws SQLException {
        this.types = new MySqlDataTypes();
        createEmissionsDatasource(emissionParams);
        createReferenceDatasource(referenceParams);
    }

    private void createEmissionsDatasource(ConnectionParams emissionParams) throws SQLException {
        Connection connection = connectToMySqlServer(emissionParams);
        String name = emissionParams.getDbName();
        if (!doesSchemaExist(name, connection)) {
            createSchema(name, connection);
        }
        Connection emissionConnection = MySqlConnectionFactory.get().getConnection(emissionParams);
        emissionsDatasource = new MySqlDatasource(name, emissionConnection, types);

    }

    private void createReferenceDatasource(ConnectionParams referenceParams) throws SQLException {
        Connection connection = connectToMySqlServer(referenceParams);
        String name = referenceParams.getDbName();
        if (!doesSchemaExist(name, connection)) {
            createSchema(name, connection);
            // TODO: create& import reference tables and data
        }
        Connection referenceConnection = MySqlConnectionFactory.get().getConnection(referenceParams);
        referenceDatasource = new MySqlDatasource(name, referenceConnection, types);

    }

    private Connection connectToMySqlServer(ConnectionParams params) throws SQLException {
        ConnectionParams paramsWODbName = new ConnectionParams("", params.getHost(), params.getPort(), params
                .getUsername(), params.getPassword());
        return MySqlConnectionFactory.get().getConnection(paramsWODbName);
    }

    public Datasource getEmissionsDatasource() {
        return emissionsDatasource;
    }

    public Datasource getReferenceDatasource() {
        return referenceDatasource;
    }

    private void createSchema(String datasourceName, Connection connection) throws SQLException {
        Statement stmt = null;
        try {
            stmt = connection.createStatement();
            stmt.execute("CREATE DATABASE " + datasourceName);
        } finally {
            if (stmt != null)
                stmt.close();
        }
    }

    private boolean doesSchemaExist(String datasourceName, Connection connection) throws SQLException {
        Statement stmt = null;
        try {
            stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SHOW databases");
            while (rs.next()) {
                String aDatasourceName = rs.getString(1);
                if (aDatasourceName.equalsIgnoreCase(datasourceName))
                    return true;
            }
        } finally {
            if (stmt != null)
                stmt.close();
        }

        return false;
    }

    public SqlDataTypes getSqlDataTypes() {
        return types;
    }

    public String asciiToNumber(String asciiColumn, int precision) {
        return asciiColumn;
    }

    public void disconnect() {
        MySqlConnectionFactory.get().close();
    }

    public Datasource getEmfDatasource() {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean isConnected() throws Exception {
        // TODO Auto-generated method stub
        // FIX this
        return false;
    }

    public Connection getConnection() {
        // TODO Auto-generated method stub
        return null;
    }

    public Datasource getSmsDatasource() {
        // TODO Auto-generated method stub
        return null;
    }

    public Datasource getFastDatasource() {
        // TODO Auto-generated method stub
        return null;
    }

}
