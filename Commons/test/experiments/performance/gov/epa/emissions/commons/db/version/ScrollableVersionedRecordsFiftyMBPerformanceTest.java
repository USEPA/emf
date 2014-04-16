package gov.epa.emissions.commons.db.version;

public class ScrollableVersionedRecordsFiftyMBPerformanceTest extends ScrollableVersionedRecordsPerformanceTest {

    public ScrollableVersionedRecordsFiftyMBPerformanceTest(String name) {
        super(name);
    }

    protected String table() {
        return "emissions.test_onroad_fifty_mb";
    }

    protected void doTearDown() throws Exception {
        // TODO Auto-generated method stub
    }

    protected int datasetId() {
        return 643082749;
    }

    public void testSimpleQuery() throws Exception {
        startTracking();

        SimpleScrollableVersionedRecords results = executeSimpleQuery();
        assertEquals(341374, results.total());

        dumpStats();
        results.close();
    }

}
