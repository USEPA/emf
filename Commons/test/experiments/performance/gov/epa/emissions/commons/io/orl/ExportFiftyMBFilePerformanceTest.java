package gov.epa.emissions.commons.io.orl;

public class ExportFiftyMBFilePerformanceTest extends ExportPerformanceTest {

    public ExportFiftyMBFilePerformanceTest(String name) {
        super(name);
    }

    public void testTrackMemory() throws Exception {
        startTracking();

        super.doExport("test_onroad_fifty_mb");

        dumpStats();
    }

}
