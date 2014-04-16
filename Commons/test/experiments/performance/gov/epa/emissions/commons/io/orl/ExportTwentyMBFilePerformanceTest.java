package gov.epa.emissions.commons.io.orl;

public class ExportTwentyMBFilePerformanceTest extends ExportPerformanceTest {

    public ExportTwentyMBFilePerformanceTest(String name) {
        super(name);
    }

    public void testTrackMemory() throws Exception {
        startTracking();

        super.doExport("test_onroad_twenty_mb");

        dumpStats();
    }

}
