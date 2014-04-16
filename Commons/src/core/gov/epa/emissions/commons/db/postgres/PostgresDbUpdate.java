package gov.epa.emissions.commons.db.postgres;

import gov.epa.emissions.commons.Config;
import gov.epa.emissions.commons.db.DbOperation;
import gov.epa.emissions.commons.db.DbUpdate;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.dbunit.DatabaseUnitException;
import org.dbunit.database.QueryDataSet;
import org.dbunit.dataset.DefaultDataSet;
import org.dbunit.dataset.DefaultTable;
import org.dbunit.dataset.IDataSet;
import org.dbunit.operation.DatabaseOperation;

public class PostgresDbUpdate extends DbOperation implements DbUpdate {

    public PostgresDbUpdate(Connection jdbcConnection) throws Exception {
        super(jdbcConnection);
    }

    public PostgresDbUpdate() throws Exception {
        this(new PostgresDbConfig("test/postgres.conf"));
    }

    public PostgresDbUpdate(Config config) throws Exception {
        super(config);
    }

    public void deleteAll(String schema, String table) throws DatabaseUnitException, SQLException {
        IDataSet dataset = dataset(qualifiedTable(schema, table));
        DatabaseOperation.DELETE_ALL.execute(connection, dataset);
    }

    public void deleteAll(String table) throws DatabaseUnitException, SQLException {
        IDataSet dataset = new DefaultDataSet(new DefaultTable(table));
        DatabaseOperation.DELETE_ALL.execute(connection, dataset);
    }

    private IDataSet dataset(String table) {
        return new DefaultDataSet(new DefaultTable(table));
    }

    protected void doDelete(IDataSet dataset) throws DatabaseUnitException, SQLException {
        DatabaseOperation.DELETE.execute(connection, dataset);
    }

    // DELETE from table where name=value
    public void delete(String table, String name, String value) throws SQLException, DatabaseUnitException {
        QueryDataSet dataset = new QueryDataSet(connection);
        dataset.addTable(table, "SELECT * from " + table + " WHERE " + name + " ='" + value + "'");

        doDelete(dataset);
    }

    public void delete(String table, String name, int value) throws SQLException, DatabaseUnitException {
        delete(table, name, value + "");
    }

    public void dropTable(String schema, String table) throws SQLException {
        Connection jdbcConnection = connection.getConnection();
        Statement stmt = jdbcConnection.createStatement();
        stmt.execute("DROP TABLE " + qualifiedTable(schema, table));
    }

    private String qualifiedTable(String schema, String table) {
        if ("versions".equalsIgnoreCase(table.toLowerCase()) && "emissions".equalsIgnoreCase(schema.toLowerCase())) {
            System.err.println("Versions table moved to EMF- 001.");
        }
        String qualifiedTable = schema + "." + table;
        return qualifiedTable;
    }

}
