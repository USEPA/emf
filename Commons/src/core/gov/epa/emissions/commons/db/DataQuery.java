package gov.epa.emissions.commons.db;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface DataQuery {

    ResultSet executeQuery(String query) throws SQLException;
    
    ResultSet executeUpdateQuery(String query) throws SQLException;

    ResultSet select(String[] columnNames, String table) throws SQLException;

    ResultSet selectAll(String table) throws SQLException;

    // FIXME: move the clients to use TableDefinition
    void execute(String update) throws SQLException;

}
