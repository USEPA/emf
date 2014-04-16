package gov.epa.emissions.commons.io.orl;

import java.io.File;

public class ImportOneMBFileTest extends ImportPerformanceTestCase {

    public ImportOneMBFileTest(String name) {
        super(name);
    }

    public void testImport() throws Exception {
        File importFile = new File("test/data/orl/nc/performance", "onroad-1MB.txt");
        
        long before = time();
        super.doImport(importFile, "onroad_one_mb");
        System.out.println("Import of 1 MB complete in " + (time() - before)/(1000));
    }

}
