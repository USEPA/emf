package gov.epa.emissions.commons.db;

import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.commons.io.IntegerFormatter;
import gov.epa.emissions.commons.io.LongFormatter;
import gov.epa.emissions.commons.io.RealFormatter;
import gov.epa.emissions.commons.io.StringFormatter;
import gov.epa.emissions.commons.io.TableMetadata;
import gov.epa.emissions.commons.io.importer.PersistenceTestCase;

import java.sql.SQLException;
import java.util.List;

public class TableDefinitionDelegateTest extends PersistenceTestCase {

    private Datasource datasource;

    private SqlDataTypes sqlDataTypes;

    protected void setUp() throws Exception {
        super.setUp();
        DbServer dbServer = dbSetup.getDbServer();
        sqlDataTypes = dbServer.getSqlDataTypes();
        datasource = dbServer.getEmissionsDatasource();
    }

    public void testShouldGetTableMetaDataForTableWithOneType() throws Exception {
        String[] names = { "table1" };
        try {
            TableDefinition definition = datasource.tableDefinition();
            if (definition.tableExists(names[0]))
                dropTables(names);

            createTables(names);
            while (!definition.tableExists(names[0])) {
                // busy wait for table to be created
            }
            TableMetadata tmd = definition.getTableMetaData("table1");
            assertEquals("column name should match", tmd.getCols()[0].getName(), "col1");
            assertEquals("column type should match", tmd.getCols()[0].getType(), "java.lang.String");
            assertEquals("column size should match", tmd.getCols()[0].getSize(), 15);

        } finally {
            dropTables(names);
        }
    }

    public void FIXME_testShouldGetTableMetaDataForTableWithMultipleColumnTypes() throws Exception {
        String[] names = { "table1" };
        try {
            TableDefinition definition = datasource.tableDefinition();
            if (definition.tableExists(names[0]))
                dropTables(names);

            createTablesWithMultipleColumns(names);

            TableMetadata tmd = definition.getTableMetaData("table1");

            assertEquals("column name should match", tmd.getCols()[0].getName(), "col1");
            assertEquals("column type should match", tmd.getCols()[0].getType(), "java.lang.String");
            assertEquals("column size should match", tmd.getCols()[0].getSize(), 15);

            assertEquals("column name should match", tmd.getCols()[1].getName(), "col2");
            assertEquals("column type should match", tmd.getCols()[1].getType(), "java.lang.String");
            assertEquals("column size should match", tmd.getCols()[1].getSize(), 255);

            assertEquals("column name should match", tmd.getCols()[2].getName(), "col3");
            assertEquals("column type should match", tmd.getCols()[2].getType(), "java.lang.Boolean");
            assertEquals("column size should match", tmd.getCols()[2].getSize(), 1);

            assertEquals("column name should match", tmd.getCols()[2].getName(), "col3");
            assertEquals("column type should match", tmd.getCols()[2].getType(), "java.lang.Boolean");
            assertEquals("column size should match", tmd.getCols()[2].getSize(), 1);

            assertEquals("column name should match", tmd.getCols()[3].getName(), "col4");
            assertEquals("column type should match", tmd.getCols()[3].getType(), "java.lang.String");
            assertEquals("column size should match", tmd.getCols()[3].getSize(), 1);

            assertEquals("column name should match", tmd.getCols()[4].getName(), "col5");
            assertEquals("column type should match", tmd.getCols()[4].getType(), "java.lang.Integer");
            assertEquals("column size should match", tmd.getCols()[4].getSize(), 11);

            assertEquals("column name should match", tmd.getCols()[5].getName(), "col6");
            assertEquals("column type should match", "java.lang.Double", tmd.getCols()[5].getType());
            assertEquals("column size should match", tmd.getCols()[5].getSize(), 20);

            assertEquals("column name should match", tmd.getCols()[6].getName(), "col7");
            assertEquals("column type should match", tmd.getCols()[6].getType(), "java.lang.Long");
            assertEquals("column size should match", tmd.getCols()[6].getSize(), 20);

            assertEquals("column name should match", tmd.getCols()[7].getName(), "col8");
            assertEquals("column type should match", tmd.getCols()[7].getType(), "java.lang.String");
            assertEquals("column size should match", tmd.getCols()[7].getSize(), -5);

        } finally {
            dropTables(names);
        }
    }

    public void testTableExist() throws Exception {
        TableDefinitionDelegate definition = new TableDefinitionDelegate(datasource.getConnection());
        String[] names = { "table1" };
        createTables(names);
        assertTrue(definition.tableExist(names[0]));
        dropTables(names);
    }

    public void testTableNames() throws Exception {
        TableDefinitionDelegate definition = new TableDefinitionDelegate(datasource.getConnection());
        String[] names = { "nif", "orl", "ida" };
        createTables(names);
        List tables = definition.getTableNames();
        assertTrue(tables.contains(names[0]));
        assertTrue(tables.contains(names[1]));
        assertTrue(tables.contains(names[2]));
        dropTables(names);
    }

    public void testShoudReturnNoOfRowsInTheTable() throws Exception {
        TableDefinitionDelegate definition = new TableDefinitionDelegate(datasource.getConnection());
        String tableName = "test";
        createTables(new String[] { tableName });
        try{
        for (int i = 0; i < 100; i++)
            datasource.dataModifier().insertRow(tableName, new String[] { "" + i });

        int totalRows = definition.totalRows(datasource.getName() + "." + tableName);
        assertEquals(100, totalRows);
        }finally{
            dropTable(tableName,datasource);
        }
    }

    private void createTables(String[] names) throws SQLException {
        // create table with one column

        Column[] cols = { new Column("col1", sqlDataTypes.stringType(15), new StringFormatter(15)) };
        for (int i = 0; i < names.length; i++) {
            datasource.tableDefinition().createTable(names[i], cols);
        }
    }

    private void createTablesWithMultipleColumns(String[] names) throws SQLException {
        // create table with multiple columns
        Column col1 = new Column("col1", sqlDataTypes.stringType(15), new StringFormatter(15));
        Column col2 = new Column("col2", sqlDataTypes.stringType(255), new StringFormatter(255));
        Column col3 = new Column("col3", sqlDataTypes.booleanType(), new StringFormatter(1));
        Column col4 = new Column("col4", sqlDataTypes.charType(), new StringFormatter(1));
        Column col5 = new Column("col5", sqlDataTypes.intType(), new IntegerFormatter());
        Column col6 = new Column("col6", sqlDataTypes.realType(), new RealFormatter());
        Column col7 = new Column("col7", sqlDataTypes.longType(), new LongFormatter());
        Column col8 = new Column("col8", sqlDataTypes.stringType(), new StringFormatter());

        Column[] cols = { col1, col2, col3, col4, col5, col6, col7, col8 };
        for (int i = 0; i < names.length; i++) {
            datasource.tableDefinition().createTable(names[i], cols);
        }
    }

    private void dropTables(String[] names) throws Exception {
        DbUpdate dbUpdate = dbSetup.dbUpdate(datasource);
        for (int i = 0; i < names.length; i++) {
            dbUpdate.dropTable(datasource.getName(), names[i]);
        }
    }

    protected void doTearDown() throws Exception {// No op
    }
}
