package gov.epa.emissions.commons.io.orl;

public class ExportFiveMBFilePerformanceTest extends ExportPerformanceTest {

    public ExportFiveMBFilePerformanceTest(String name) {
        super(name);
    }

    public void testTrackMemory() throws Exception {
        startTracking();
        
        super.doExport("test_onroad_five_mb");

        dumpStats();
    }

    

}
