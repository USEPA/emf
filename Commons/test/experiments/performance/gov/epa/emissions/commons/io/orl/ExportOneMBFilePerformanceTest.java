package gov.epa.emissions.commons.io.orl;


public class ExportOneMBFilePerformanceTest extends ExportPerformanceTest {

    public ExportOneMBFilePerformanceTest(String name) {
        super(name);
    }

    public void testTrackMemory() throws Exception {
        long before = usedMemory();
        super.doExport("test_onroad_one_mb");
        System.out.println("Memory used: " + (usedMemory() - before) + " MB");
    }

}
