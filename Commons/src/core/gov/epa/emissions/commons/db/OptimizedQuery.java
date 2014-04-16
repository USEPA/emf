package gov.epa.emissions.commons.db;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface OptimizedQuery {

    boolean execute() throws SQLException;

    ResultSet getResultSet();

    void close() throws SQLException;

}
