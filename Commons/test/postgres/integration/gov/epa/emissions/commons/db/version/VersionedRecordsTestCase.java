package gov.epa.emissions.commons.db.version;

import gov.epa.emissions.commons.db.DataModifier;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.HibernateTestCase;
import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.db.TableDefinition;
import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.commons.io.FileFormatWithOptionalCols;
import gov.epa.emissions.commons.io.TableFormat;
import gov.epa.emissions.commons.io.temporal.VersionedTableFormat;

import java.sql.SQLException;
import java.util.List;

public abstract class VersionedRecordsTestCase extends HibernateTestCase {

    protected Datasource datasource;

    protected SqlDataTypes types;

    protected String versionsTable;

    protected String dataTable;

    protected void setUp() throws Exception {
        super.setUp();

        DbServer dbServer = dbSetup.getDbServer();
        types = dbServer.getSqlDataTypes();

        datasource = dbServer.getEmissionsDatasource();
        versionsTable = "versions";
        dataTable = "versioned_data";

        clean();
        createTable(dataTable, datasource);
    }

    protected void doTearDown() throws Exception {
        clean();
        super.doTearDown();
    }

    private void dropDataTable() throws Exception {
        TableDefinition def = datasource.tableDefinition();
        def.dropTable(dataTable);
    }

    private void clean() throws SQLException {
        DataModifier modifier = datasource.dataModifier();
        modifier.dropAllData(versionsTable);

        try {
            dropDataTable();
        } catch (Exception e) {// Ignore, as table may not exist
        }
    }

    protected void addRecord(Datasource datasource, String table, String[] data) throws SQLException {
        DataModifier modifier = datasource.dataModifier();
        modifier.insertRow(table, data);
    }

    private void createTable(String table, Datasource datasource) throws SQLException {
        TableDefinition tableDefinition = datasource.tableDefinition();
        tableDefinition.createTable(table, tableFormat().cols());
    }

    protected TableFormat tableFormat() {
        FileFormatWithOptionalCols fileFormat = new FileFormatWithOptionalCols() {
            public Column[] optionalCols() {
                return new Column[0];
            }

            public Column[] minCols() {
                Column p1 = new Column("p1", types.text());
                Column p2 = new Column("p2", types.text());

                return new Column[] { p1, p2 };
            }

            public String identify() {
                return "Record_Id";
            }

            public Column[] cols() {
                return minCols();
            }

            public void fillDefaults(List data, long datasetId) {// ignored
            }
        };
        return new VersionedTableFormat(fileFormat, types);
    }

}
