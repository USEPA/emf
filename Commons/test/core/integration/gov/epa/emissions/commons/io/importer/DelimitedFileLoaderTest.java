package gov.epa.emissions.commons.io.importer;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.SimpleDataset;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.DbUpdate;
import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.db.TableReader;
import gov.epa.emissions.commons.io.NonVersionedTableFormat;

import java.io.File;

public class DelimitedFileLoaderTest extends PersistenceTestCase {

    private Reader reader;

    private Datasource datasource;

    private SqlDataTypes dataType;

    private NonVersionedTableFormat tableFormat;

    protected void setUp() throws Exception {
        super.setUp();

        DbServer dbServer = dbSetup.getDbServer();
        dataType = dbServer.getSqlDataTypes();
        datasource = dbServer.getEmissionsDatasource();

        File file = new File("test/data/orl/SimpleDelimited.txt");
        reader = new DelimitedFileReader(file, new WhitespaceDelimitedTokenizer());

        tableFormat = new NonVersionedTableFormat(new DepricatedDelimitedFileFormat("test", 7, dataType), dataType);
        createTable("SimpleDelimited", datasource, tableFormat);
    }

    protected void doTearDown() throws Exception {
        DbUpdate dbUpdate = dbSetup.dbUpdate(datasource);
        dbUpdate.dropTable(datasource.getName(), "SimpleDelimited");
    }

    public void testShouldLoadRecordsIntoTable() throws Exception {
        DataLoader loader = new FixedColumnsDataLoader(datasource, tableFormat);

        Dataset dataset = new SimpleDataset();
        dataset.setName("test");
        String tableName = "simpledelimited";

        loader.load(reader, dataset, tableName);

        // assert
        TableReader tableReader = tableReader(datasource);

        assertTrue("Table '" + tableName + "' should have been created", tableReader.exists(datasource.getName(), tableName));
        assertEquals(10, tableReader.count(datasource.getName(), tableName));
    }
}
