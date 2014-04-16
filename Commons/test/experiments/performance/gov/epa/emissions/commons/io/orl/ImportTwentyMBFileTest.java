package gov.epa.emissions.commons.io.orl;

import java.io.File;

public class ImportTwentyMBFileTest extends ImportPerformanceTestCase {

    public ImportTwentyMBFileTest(String name) {
        super(name);
    }

    public void testImport() throws Exception {
        File importFile = new File("test/data/orl/nc/performance", "onroad-20MB.txt");
        
        long before = time();
        super.doImport(importFile, "onroad_twenty_mb");
        System.out.println("Import of 20 MB complete in " + (time() - before));
    }

}
