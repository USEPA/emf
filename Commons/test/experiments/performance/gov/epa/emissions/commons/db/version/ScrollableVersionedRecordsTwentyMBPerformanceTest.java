package gov.epa.emissions.commons.db.version;

public class ScrollableVersionedRecordsTwentyMBPerformanceTest extends ScrollableVersionedRecordsPerformanceTest {

    public ScrollableVersionedRecordsTwentyMBPerformanceTest(String name) {
        super(name);
    }

    protected String table() {
        return "emissions.test_onroad_twenty_mb";
    }

    protected int datasetId() {
        return 643082749;
    }

    public void testSimpleQuery() throws Exception {
        startTracking();

        SimpleScrollableVersionedRecords results = executeSimpleQuery();
        assertEquals(143385, results.total());

        dumpStats();
        results.close();
    }

}
