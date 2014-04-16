package gov.epa.emissions.commons.io.orl;

public class Export100MBFilePerformanceTest extends ExportPerformanceTest {

    public Export100MBFilePerformanceTest(String name) {
        super(name);
    }

    public void testTrackMemory() throws Exception {
        startTracking();

        super.doExport("test_onroad_hundred_mb");

        dumpStats();
    }

}
