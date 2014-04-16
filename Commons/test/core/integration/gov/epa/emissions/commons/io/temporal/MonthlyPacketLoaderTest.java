package gov.epa.emissions.commons.io.temporal;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.SimpleDataset;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.DbUpdate;
import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.db.TableReader;
import gov.epa.emissions.commons.io.CustomCharSetInputStreamReader;
import gov.epa.emissions.commons.io.NonVersionedTableFormat;
import gov.epa.emissions.commons.io.importer.DataLoader;
import gov.epa.emissions.commons.io.importer.FixedColumnsDataLoader;
import gov.epa.emissions.commons.io.importer.FixedWidthPacketReader;
import gov.epa.emissions.commons.io.importer.PersistenceTestCase;
import gov.epa.emissions.commons.io.importer.Reader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;

public class MonthlyPacketLoaderTest extends PersistenceTestCase {

    private Reader reader;

    private Datasource datasource;

    private SqlDataTypes typeMapper;

    public NonVersionedTableFormat tableFormat;

    private BufferedReader fileReader;

    protected void setUp() throws Exception {
        super.setUp();

        DbServer dbServer = dbSetup.getDbServer();
        typeMapper = dbServer.getSqlDataTypes();
        datasource = dbServer.getEmissionsDatasource();

        File file = new File("test/data/temporal-profiles/monthly.txt");
        MonthlyFileFormat fileFormat = new MonthlyFileFormat(typeMapper);
        tableFormat = new NonVersionedTableFormat(fileFormat, typeMapper);
        createTable("Monthly", datasource, tableFormat);

        fileReader = new BufferedReader(new CustomCharSetInputStreamReader(new FileInputStream(file)));
        int lineNumber=0;
        reader = new FixedWidthPacketReader(fileReader, fileReader.readLine().trim(), fileFormat, lineNumber);
    }

    protected void doTearDown() throws Exception {
        fileReader.close();
        DbUpdate dbUpdate = dbSetup.dbUpdate(datasource);
        dbUpdate.dropTable(datasource.getName(), "monthly");
    }

    public void testShouldLoadRecordsIntoMonthlyTable() throws Exception {
        DataLoader loader = new FixedColumnsDataLoader(datasource, tableFormat);

        Dataset dataset = new SimpleDataset();
        dataset.setName("test");
        String tableName = "monthly";

        loader.load(reader, dataset, tableName);

        // assert
        TableReader tableReader = tableReader(datasource);

        assertTrue("Table '" + tableName + "' should have been created", tableReader.exists(datasource.getName(),
                tableName));
        assertEquals(10, tableReader.count(datasource.getName(), tableName));
    }
}
