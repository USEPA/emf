package gov.epa.emissions.commons.io.temporal;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.SimpleDataset;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.db.TableReader;
import gov.epa.emissions.commons.io.CustomCharSetInputStreamReader;
import gov.epa.emissions.commons.io.FileFormat;
import gov.epa.emissions.commons.io.NonVersionedTableFormat;
import gov.epa.emissions.commons.io.importer.DataLoader;
import gov.epa.emissions.commons.io.importer.FixedColumnsDataLoader;
import gov.epa.emissions.commons.io.importer.FixedWidthPacketReader;
import gov.epa.emissions.commons.io.importer.ImporterException;
import gov.epa.emissions.commons.io.importer.PersistenceTestCase;
import gov.epa.emissions.commons.io.importer.Reader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;

public class WeeklyPacketLoaderTest extends PersistenceTestCase {

    private Reader reader;

    private Datasource datasource;

    private SqlDataTypes typeMapper;

    private NonVersionedTableFormat tableFormat;

    private FileFormat fileFormat;

    protected void setUp() throws Exception {
        super.setUp();

        DbServer dbServer = dbSetup.getDbServer();
        typeMapper = dbServer.getSqlDataTypes();
        datasource = dbServer.getEmissionsDatasource();

        fileFormat = new WeeklyFileFormat(typeMapper);
        tableFormat = new NonVersionedTableFormat(fileFormat, typeMapper);
        createTable("Weekly", datasource, tableFormat);
    }

    protected void doTearDown() throws Exception {
        dropTable("Weekly", datasource);
    }

    public void testShouldLoadRecordsIntoWeeklyTable() throws Exception {
        File file = new File("test/data/temporal-profiles/weekly.txt");
        BufferedReader fileReader = new BufferedReader(new CustomCharSetInputStreamReader(new FileInputStream(file)));
        int lineNumber = 0;
        reader = new FixedWidthPacketReader(fileReader, fileReader.readLine().trim(), fileFormat, lineNumber);

        try {
            DataLoader loader = new FixedColumnsDataLoader(datasource, tableFormat);

            Dataset dataset = new SimpleDataset();
            dataset.setName("test");
            String tableName = "Weekly";
            loader.load(reader, dataset, tableName);
            // assert
            assertEquals(13, countRecords(tableName));
        } finally {
            fileReader.close();
        }
    }

    private int countRecords(String tableName) {
        TableReader tableReader = tableReader(datasource);
        return tableReader.count(datasource.getName(), tableName);
    }

    public void testShouldDropDataOnEncounteringBadData() throws Exception {
        File file = new File("test/data/temporal-profiles/BAD-weekly.txt");
        BufferedReader fileReader = new BufferedReader(new FileReader(file));
        int lineNumber=0;
        reader = new FixedWidthPacketReader(fileReader, fileReader.readLine().trim(), tableFormat, lineNumber);

        DataLoader loader = new FixedColumnsDataLoader(datasource, tableFormat);

        Dataset dataset = new SimpleDataset();
        dataset.setName("test");
        String tableName = "Weekly";

        try {
            loader.load(reader, dataset, tableName);
        } catch (ImporterException e) {
            assertEquals(0, countRecords(tableName));
            return;
        } finally {
            fileReader.close();
        }

        fail("should have encountered an error(missing cols) on record 3");
    }
}
