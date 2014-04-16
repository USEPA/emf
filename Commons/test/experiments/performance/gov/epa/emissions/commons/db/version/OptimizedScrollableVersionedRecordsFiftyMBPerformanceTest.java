package gov.epa.emissions.commons.db.version;

public class OptimizedScrollableVersionedRecordsFiftyMBPerformanceTest extends
        ScrollableVersionedRecordsPerformanceTest {

    public OptimizedScrollableVersionedRecordsFiftyMBPerformanceTest(String name) {
        super(name);
    }

    protected String table() {
        return "emissions.test_onroad_fifty_mb";
    }

    protected int datasetId() {
        return 643082749;
    }

    public void testTotal() throws Exception {
        System.out.println("testTotal");
        startTracking();

        OptimizedScrollableVersionedRecords results = executeOptimizedQuery();
        assertEquals(341374, results.total());

        dumpStats();
        results.close();
    }

    public void testShouldGetRangeFromFirstQueryResults() throws Exception {
        System.out.println("testShouldGetRangeFromFirstQueryResults");
        startTracking();

        OptimizedScrollableVersionedRecords results = executeOptimizedQuery();
        VersionedRecord[] records = results.range(0, 99);
        assertEquals(100, records.length);
        assertEquals(1, records[0].getRecordId());
        assertEquals(100, records[99].getRecordId());

        dumpStats();
        results.close();
    }

    public void testShouldGetArbitraryRangeFromFirstQueryResults() throws Exception {
        System.out.println("testShouldGetArbitraryRangeFromFirstQueryResults");
        startTracking();

        OptimizedScrollableVersionedRecords results = executeOptimizedQuery();
        VersionedRecord[] records = results.range(10, 100);
        assertEquals(91, records.length);
        assertEquals(11, records[0].getRecordId());
        assertEquals(101, records[90].getRecordId());

        dumpStats();
        results.close();
    }

    public void testShouldGetRangeFromSecondQueryResults() throws Exception {
        System.out.println("testShouldGetRangeFromSecondQueryResults");
        startTracking();

        OptimizedScrollableVersionedRecords results = executeOptimizedQuery();
        VersionedRecord[] records = results.range(50000, 50099);
        assertEquals(100, records.length);
        assertEquals(50001, records[0].getRecordId());
        assertEquals(50100, records[99].getRecordId());

        dumpStats();
        results.close();
    }

    public void testShouldGetRangeFromThirdQueryResults() throws Exception {
        System.out.println("testShouldGetRangeFromThirdQueryResults");
        startTracking();

        OptimizedScrollableVersionedRecords results = executeOptimizedQuery();
        VersionedRecord[] records = results.range(100000, 100050);
        assertEquals(51, records.length);
        assertEquals(100001, records[0].getRecordId());
        assertEquals(100051, records[50].getRecordId());

        dumpStats();
        results.close();
    }

    public void testShouldGetAbitraryRangeFromThirdQueryResults() throws Exception {
        System.out.println("testShouldGetAbitraryRangeFromThirdQueryResults");
        startTracking();

        OptimizedScrollableVersionedRecords results = executeOptimizedQuery();
        VersionedRecord[] records = results.range(100200, 100225);
        assertEquals(26, records.length);
        assertEquals(100201, records[0].getRecordId());
        assertEquals(100226, records[25].getRecordId());

        dumpStats();
        results.close();
    }

    public void testShouldGetCompleteRangeFromSecondQueryResults() throws Exception {
        System.out.println("testShouldGetCompleteRangeFromSecondQueryResults");
        startTracking();

        OptimizedScrollableVersionedRecords results = executeOptimizedQuery();
        VersionedRecord[] records = results.range(50000, 59999);
        assertEquals(10000, records.length);
        assertEquals(50001, records[0].getRecordId());
        assertEquals(60000, records[9999].getRecordId());

        dumpStats();
        results.close();
    }

    public void testShouldGetCompleteRangeFromLastQueryResults() throws Exception {
        System.out.println("testShouldGetCompleteRangeFromLastQueryResults");
        startTracking();

        OptimizedScrollableVersionedRecords results = executeOptimizedQuery();
        VersionedRecord[] records = results.range(340000, 341373);
        assertEquals(1374, records.length);
        assertEquals(340001, records[0].getRecordId());
        assertEquals(341374, records[1373].getRecordId());

        dumpStats();
        results.close();
    }

    public void testShouldFetchRangeThatExceedsLastTableRecord() throws Exception {
        System.out.println("testShouldFetchRangeThatExceedsLastTableRecord");
        startTracking();

        OptimizedScrollableVersionedRecords results = executeOptimizedQuery();
        VersionedRecord[] records = results.range(340000, 350000);
        assertEquals(1374, records.length);
        assertEquals(340001, records[0].getRecordId());
        assertEquals(341374, records[1373].getRecordId());

        dumpStats();
        results.close();
    }

    public void testShouldMoveRangeFromThirdQueryToSecondQuery() throws Exception {
        System.out.println("testShouldMoveRangeFromThirdQueryToSecondQuery");
        startTracking();

        OptimizedScrollableVersionedRecords results = executeOptimizedQuery();
        results.range(100200, 100225);// third query

        VersionedRecord[] records = results.range(50000, 50099);// second query
        assertEquals(100, records.length);
        assertEquals(50001, records[0].getRecordId());
        assertEquals(50100, records[99].getRecordId());

        dumpStats();
        results.close();
    }

}
