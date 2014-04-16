package gov.epa.emissions.commons.db;

import gov.epa.emissions.commons.data.SimpleDataset;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.io.importer.PersistenceTestCase;
import gov.epa.emissions.commons.io.importer.VersionedDataFormatFactory;
import gov.epa.emissions.commons.io.orl.ORLNonPointImporter;

import java.io.File;
import java.util.Random;

public class ScrollableRecordsTest extends PersistenceTestCase {

    private ScrollableRecords results;

    private SimpleDataset dataset;

    protected void setUp() throws Exception {
        super.setUp();
        importNonPoint();

        results = new ScrollableRecords(emissions(), "SELECT * from emissions." + dataset.getInternalSources()[0].getTable());
        results.execute();
    }

    protected void doTearDown() throws Exception {
        results.close();

        dropNonPoint();
    }

    private void importNonPoint() throws Exception {
        dataset = new SimpleDataset();
        dataset.setName("test");
        dataset.setId(Math.abs(new Random().nextInt()));
        File file = new File("test/data/orl/nc", "arinv.nonpoint.nti99_NC.txt");
        
        Version version = new Version();
        version.setVersion(0);

        ORLNonPointImporter importer = new ORLNonPointImporter(file.getParentFile(), new String[] { file.getName() },
                dataset, dbServer(), dataTypes(), new VersionedDataFormatFactory(version, dataset));
        importer.run();
    }

    private void dropNonPoint() throws Exception {
        super.dropTable(dataset.getInternalSources()[0].getTable(), emissions());
    }

    public void testRowCount() throws Exception {
        assertEquals(394, results.total());
    }

    public void testScrollForward() throws Exception {
        assertEquals(0, results.position());
        results.forward(10);
        assertEquals(10, results.position());
    }

    public void testScrollBackward() throws Exception {
        assertEquals(0, results.position());
        results.forward(10);
        results.backward(3);

        assertEquals(7, results.position());
    }

    public void testMoveToSpecificPosition() throws Exception {
        results.moveTo(3);
        assertEquals(3, results.position());
    }

    public void testFetchRangeOfRecords() throws Exception {
        DbRecord[] records = results.range(3, 7);
        assertNotNull("Should be able to fetch a range of records", records);
        assertEquals(5, records.length);
        for (int i = 0; i < records.length; i++)
            assertNotNull(records[i]);
    }

    public void testFetchRecordsOutOfRangeShouldReturnOnlyValidPartialRange() throws Exception {
        assertRecords(6, results.range(388, 397));
        assertRecords(93, results.range(301, 399));
        assertRecords(394, results.range(0, 399));
        assertRecords(10, results.range(0, 9));
    }

    private void assertRecords(int expected, DbRecord[] range) {
        assertEquals(expected, range.length);
        for (int i = 0; i < range.length; i++)
            assertNotNull(range[i]);
    }

    public void testIterate() throws Exception {
        for (int i = 0; i < 394; i++) {
            assertTrue("Should have more records", results.available());
            DbRecord record = results.next();
            assertNotNull("Should be able to iterate through records", record);
            assertEquals(i + 1, record.getId());
        }
    }

    public void testFetchFirstRecord() throws Exception {
        DbRecord record = results.next();
        assertEquals(1, record.getId());

        assertEquals(42, record.size());
        assertNotNull("Should be able to fetch first record", record);

        assertEquals("1", record.token(0));// record id
        assertEquals(dataset.getId() + "", record.token(1));
        assertEquals("0", record.token(2));// version
        assertEquals("", record.token(3));// delete versions
        assertEquals("37001", record.token(4));
        assertEquals("10201302", record.token(5));
        assertEquals("0", record.token(6));
        assertEquals("0107", record.token(7));
        assertEquals("2", record.token(8));
        assertEquals("0", record.token(9));
        assertEquals("246", record.token(10));
        assertTrue(record.token(11).startsWith("0.000387296"));// because of precision
        assertEquals("-9",record.token(12));
        assertEquals("-9",record.token(13));
        assertEquals("-9",record.token(14));
        assertEquals("-9",record.token(15));
        assertNull(record.token(16));
    }

}
