package gov.epa.emissions.commons.db;

import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.commons.io.FileFormatWithOptionalCols;
import gov.epa.emissions.commons.io.TableFormat;
import gov.epa.emissions.commons.io.importer.PersistenceTestCase;
import gov.epa.emissions.commons.io.temporal.VersionedTableFormat;

import java.sql.SQLException;
import java.util.List;

public class TableMetadataTest extends PersistenceTestCase {
    protected Datasource datasource;

    protected String table;

    protected void setUp() throws Exception {
        super.setUp();

        datasource = emissions();
        table = "Modifier_Test";
        createTable(table, datasource);
    }

    private void createTable(String table, Datasource datasource) throws SQLException {
        TableDefinition tableDefinition = datasource.tableDefinition();
        tableDefinition.createTable(table, cols());
    }

    private Column[] cols() {
        return tableFormat(dataTypes()).cols();
    }

    protected TableFormat tableFormat(final SqlDataTypes types) {
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

            public void fillDefaults(List data, long datasetId) {// ignore
            }
        };
        return new VersionedTableFormat(fileFormat, types);
    }

    protected void doTearDown() throws Exception {
        TableDefinition def = datasource.tableDefinition();
        def.dropTable(table);
    }

    public void testGetColumns() throws SQLException {
        TableMetaData metadata = new TableMetaData(datasource);
        Column[] cols = metadata.getColumns(table);

        assertEquals(7, cols.length);
        assertEquals(cols[0].name(), "record_id");
        assertEquals(cols[1].name(), "dataset_id");
        assertEquals(cols[2].name(), "version");
        assertEquals(cols[3].name(), "delete_versions");
        assertEquals(cols[4].name(), "p1");
        assertEquals(cols[5].name(), "p2");
        assertEquals(cols[6].name(), "comments");
    }
}
