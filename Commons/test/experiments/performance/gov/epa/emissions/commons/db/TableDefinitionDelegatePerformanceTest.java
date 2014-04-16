package gov.epa.emissions.commons.db;

import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.TableDefinition;
import gov.epa.emissions.commons.io.TableMetadata;
import gov.epa.emissions.commons.io.importer.PersistenceTestCase;

public class TableDefinitionDelegatePerformanceTest extends PersistenceTestCase {

    private Datasource datasource;

    protected void setUp() throws Exception {
        super.setUp();
        DbServer dbServer = dbSetup.getDbServer();
        datasource = dbServer.getEmissionsDatasource();
    }

    protected void doTearDown() throws Exception {// No op
    }

    public void testShouldGetTableMetaData() throws Exception {
        TableDefinition definition = datasource.tableDefinition();
        
        Runtime runtime = Runtime.getRuntime();
        System.out.println(runtime.freeMemory()/1024);
        
        TableMetadata data = definition.getTableMetaData("nonroad_capann_nei2002_0110_x_txt");
        assertNotNull(data);

        System.out.println(runtime.freeMemory()/1024);
    }

}
