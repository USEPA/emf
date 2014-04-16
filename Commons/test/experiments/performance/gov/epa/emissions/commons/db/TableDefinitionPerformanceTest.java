package gov.epa.emissions.commons.db;

import gov.epa.emissions.commons.PerformanceTestCase;
import gov.epa.emissions.commons.io.TableMetadata;

public class TableDefinitionPerformanceTest extends PerformanceTestCase {

    protected Datasource datasource;

    public TableDefinitionPerformanceTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void doTearDown() throws Exception {
        super.doTearDown();
        System.out.println("---------------------------------");
    }

    public void testFetchMetadataOfFiveMbTable() throws Exception {
        System.out.println("testFetchMetadataOfFiveMbTable");
        startTracking();

        TableDefinitionDelegate delegate = new TableDefinitionDelegate(super.emissions().getConnection());
        TableMetadata data = delegate.getTableMetaData("emissions.test_onroad_five_mb");
        assertEquals(14, data.getCols().length);

        dumpStats();
    }

    public void testFetchMetadataOfFiftyMbTabletadata() throws Exception {
        System.out.println("testFetchMetadataOfFiftyMbTabletadata");
        startTracking();

        TableDefinitionDelegate delegate = new TableDefinitionDelegate(super.emissions().getConnection());
        TableMetadata data = delegate.getTableMetaData("emissions.test_onroad_fifty_mb");
        assertEquals(14, data.getCols().length);

        dumpStats();
    }
}
