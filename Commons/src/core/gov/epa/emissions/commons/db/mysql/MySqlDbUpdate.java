package gov.epa.emissions.commons.db.mysql;

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

public class MySqlDbUpdate extends DbOperation implements DbUpdate{

    //TODO: move the common code betwen MySqlUpdate and Postgres Update to delegate
    public MySqlDbUpdate(Connection jdbcConnection) throws Exception {
        super(jdbcConnection);
    }

    public void deleteAll(String schema, String table) throws DatabaseUnitException, SQLException {
        IDataSet dataset = dataset(qualifiedTable(schema, table));
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

        // FIXME: use dbUnit to drop table
    }

    private String qualifiedTable(String schema, String table) {
        return table;
    }

}
