package gov.epa.emissions.commons.db;

import gov.epa.emissions.commons.db.postgres.OptimizedPostgresQuery;
import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.commons.io.FileFormatWithOptionalCols;
import gov.epa.emissions.commons.io.NonVersionedTableFormat;
import gov.epa.emissions.commons.io.TableFormat;
import gov.epa.emissions.commons.io.importer.PersistenceTestCase;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class OptimizedPostgresQueryTest extends PersistenceTestCase {

    private String table;

    protected void setUp() throws Exception {
        super.setUp();
        table = "test_optimized_query";
        TableDefinition tableDefinition = emissions().tableDefinition();
        tableDefinition.createTable(table, tableFormat().cols());
        setupVersionZeroData(emissions(), table);
    }

    private void setupVersionZeroData(Datasource datasource, String table) throws SQLException {
        addRecord(datasource, table, new String[] { "1", "p1", "p2" });
        addRecord(datasource, table, new String[] { "2", "p21", "p22" });
        addRecord(datasource, table, new String[] { "3", "p31", "p32" });
        addRecord(datasource, table, new String[] { "4", "p41", "p42" });
        addRecord(datasource, table, new String[] { "5", "p51", "p52" });
    }

    public void testShouldRunQuery() throws Exception {
        int pageSize = 1;

        String query = "SELECT * FROM emissions." + table;
        OptimizedPostgresQuery runner = new OptimizedPostgresQuery(emissions().getConnection(), query, pageSize);

        int count = 0;
        while (runner.execute()) {
            ResultSet rs = runner.getResultSet();
            rs.close();
            ++count;
        }

        runner.close();
        assertEquals(5, count);
    }

    protected void doTearDown() throws Exception {
        TableDefinition def = emissions().tableDefinition();
        def.dropTable(table);
    }

    protected TableFormat tableFormat() {
        FileFormatWithOptionalCols fileFormat = new FileFormatWithOptionalCols() {
            public Column[] optionalCols() {
                return new Column[0];
            }

            public Column[] minCols() {
                Column p1 = new Column("p1", dataTypes().text());
                Column p2 = new Column("p2", dataTypes().text());

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
        return new NonVersionedTableFormat(fileFormat, dataTypes());
    }

    protected void addRecord(Datasource datasource, String table, String[] data) throws SQLException {
        DataModifier modifier = datasource.dataModifier();
        modifier.insertRow(table, data);
    }

}
