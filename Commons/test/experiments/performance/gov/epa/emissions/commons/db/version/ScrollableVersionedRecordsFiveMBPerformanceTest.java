package gov.epa.emissions.commons.db.version;

public class ScrollableVersionedRecordsFiveMBPerformanceTest extends ScrollableVersionedRecordsPerformanceTest {

    public ScrollableVersionedRecordsFiveMBPerformanceTest(String name) {
        super(name);
    }

    protected String table() {
        return "emissions.test_onroad_five_mb";
    }

    protected int datasetId() {
        return 773529304;
    }

    public void testSimpleQuery() throws Exception {
        System.out.println("Simple Query");
        startTracking();

        SimpleScrollableVersionedRecords results = executeSimpleQuery();
        assertEquals(102870, results.total());

        dumpStats();
        results.close();
    }

}
