package gov.epa.emissions.commons.io.orl;

import java.io.File;

public class Insert10000LinesTest extends ImportPerformanceTestCase {

    public Insert10000LinesTest(String name) {
        super(name);
    }

    public void testImport() throws Exception {
        File importFile = new File("test/data/orl/nc/performance", "nonRoad-10000Insert");
        for (int i = 0; i < 3; i++) {
            try {
                long before = time();
                System.out.println("\tstart-" + usedMemory());
                super.doImport(importFile, "nonroad_10000Insert");
                System.out.println("\tend-" + usedMemory());
                System.out.println("10000 inserts completed in seconds " + (time() - before));
                assertEquals(10000, countRecords());
            } finally {
                dropTable(dataset.getInternalSources()[0].getTable(), dbServer.getEmissionsDatasource());
            }
        }
    }

}
