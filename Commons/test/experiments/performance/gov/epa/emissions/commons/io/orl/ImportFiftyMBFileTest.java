package gov.epa.emissions.commons.io.orl;

import java.io.File;

public class ImportFiftyMBFileTest extends ImportPerformanceTestCase {

    public ImportFiftyMBFileTest(String name) {
        super(name);
    }

    public void testImport() throws Exception {
        File importFile = new File("test/data/orl/nc/performance", "nonroad-50MB.txt");
        long memoryBefore = usedMemory();
        long before = time();
        super.doImport(importFile, "onroad_fifty_mb");
        System.out.println("Import of 50 MB complete in " + (time() - before) +" using memory "+(usedMemory() - memoryBefore + "MB") );
    }

}
