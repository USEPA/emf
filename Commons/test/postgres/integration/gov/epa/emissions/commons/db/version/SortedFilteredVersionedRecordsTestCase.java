package gov.epa.emissions.commons.db.version;

import gov.epa.emissions.commons.db.DataModifier;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.HibernateTestCase;
import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.db.TableDefinition;
import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.commons.io.FileFormatWithOptionalCols;
import gov.epa.emissions.commons.io.StringFormatter;
import gov.epa.emissions.commons.io.TableFormat;
import gov.epa.emissions.commons.io.temporal.VersionedTableFormat;

import java.sql.SQLException;
import java.util.List;

public abstract class SortedFilteredVersionedRecordsTestCase extends HibernateTestCase {

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
        dataTable = "sortfilter";
        createTable(dataTable, datasource);

    }

    protected void doTearDown() throws Exception {
        DataModifier modifier = datasource.dataModifier();
        modifier.dropAllData(versionsTable);

        TableDefinition def = datasource.tableDefinition();
        def.dropTable(dataTable);

        super.doTearDown();
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
                Column col_one = new Column("col_one", types.text(), new StringFormatter());
                Column col_two = new Column("col_two", types.text(), new StringFormatter());
                Column col_three = new Column("col_three", types.text(), new StringFormatter());
                Column col_four = new Column("col_four", types.text(), new StringFormatter());
                Column col_five = new Column("col_five", types.text(), new StringFormatter());
                

                return new Column[] { col_one, col_two, col_three, col_four, col_five };
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
