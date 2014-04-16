package gov.epa.emissions.commons.db;

import java.sql.SQLException;

import org.dbunit.DatabaseUnitException;

public interface DbUpdate {

    void deleteAll(String schema, String table) throws DatabaseUnitException, SQLException;

    // DELETE from table where name=value
    void delete(String table, String name, String value) throws SQLException, DatabaseUnitException;

    void delete(String table, String name, int value) throws SQLException, DatabaseUnitException;

    void dropTable(String schema, String table) throws SQLException;

}