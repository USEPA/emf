package gov.epa.emissions.commons.db;

import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.commons.io.FileFormatWithOptionalCols;
import gov.epa.emissions.commons.io.TableFormat;
import gov.epa.emissions.commons.io.importer.PersistenceTestCase;
import gov.epa.emissions.commons.io.temporal.VersionedTableFormat;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class TableModifierTest extends PersistenceTestCase {

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

    public void testShouldInsertRowUsingSpecifiedCols() throws Exception {
        TableModifier modifier = new TableModifier(datasource,table);

        String[] data = { null, "102", "0", "", "p1", "p2", "" };
        modifier.insertOneRow(data);
        DataQuery query = datasource.query();
        ResultSet rs = query.selectAll(table);
        assertTrue("Should have inserted row", rs.next());
        assertEquals("102", rs.getString(2));
        assertEquals("0", rs.getString(3));
        assertEquals("", rs.getString(4));
        assertEquals("p1", rs.getString(5));
        assertEquals("p2", rs.getString(6));
        assertEquals("", rs.getString(7)); // in line comments

        rs.close();
    }

    public void testShouldInsertRow() throws Exception {
        TableModifier modifier = new TableModifier(datasource,table);

        String[] data = { null, "102", "0", "", "p1", "p2", "" };
        modifier.insertOneRow(data);

        DataQuery query = datasource.query();
        ResultSet rs = query.selectAll(table);
        assertTrue("Should have inserted row", rs.next());
        assertEquals("102", rs.getString(2));
        assertEquals("0", rs.getString(3));
        assertEquals("", rs.getString(4));
        assertEquals("p1", rs.getString(5));
        assertEquals("p2", rs.getString(6));
        assertEquals("", rs.getString(7)); // in line comments

        rs.close();
        
        //gov.epa.emissions.framework.utils.Utils.addVersionEntryToVersionsTable(this.sessionFactory, this.user,dataset.getId(), 0, "Initial Version", "", true, "");
    }
}
