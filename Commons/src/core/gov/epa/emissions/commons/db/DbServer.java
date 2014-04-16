package gov.epa.emissions.commons.db;

import java.sql.Connection;


public interface DbServer {

    Datasource getEmissionsDatasource();

    Datasource getReferenceDatasource();
    
    Datasource getEmfDatasource();
    
    Datasource getSmsDatasource();
    
    Datasource getFastDatasource();

    Connection getConnection();

    SqlDataTypes getSqlDataTypes();

    /**
     * @return wraps a db-specific function around ascii column to convert it to
     *         a number w/ specified precision (i.e. size)
     */
    String asciiToNumber(String asciiColumn, int precision);

    void disconnect() throws Exception;
    
    boolean isConnected() throws Exception;
}
