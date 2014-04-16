package gov.epa.emissions.commons.io.orl;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.SimpleDataset;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.DbUpdate;
import gov.epa.emissions.commons.db.HibernateTestCase;
import gov.epa.emissions.commons.db.SqlDataTypes;

import java.io.File;
import java.util.Random;

public class ORLImportPerformanceTest extends HibernateTestCase {

    private SqlDataTypes sqlDataTypes;

    private Dataset dataset;

    private DbServer dbServer;

    public ORLImportPerformanceTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();

        dbServer = dbSetup.getDbServer();
        sqlDataTypes = dbServer.getSqlDataTypes();

        dataset = new SimpleDataset();
        dataset.setName("test");
        dataset.setId(Math.abs(new Random().nextInt()));
    }

    protected void doTearDown() throws Exception {
        Datasource datasource = dbServer.getEmissionsDatasource();
        DbUpdate dbUpdate = dbSetup.dbUpdate(datasource);
        dbUpdate.dropTable(datasource.getName(), dataset.getInternalSources()[0].getTable());

        dbUpdate.deleteAll(datasource.getName(), "versions");
    }

    public void testShouldImportASmallNonRoadFile() throws Exception {
        File folder = new File("test/data/orl/nc");
        ORLNonRoadImporter importer = new ORLNonRoadImporter(folder, new String[] { "small-nonroad.txt" }, dataset,
                dbServer, sqlDataTypes);
        importer.run();
    }

    public void testShouldImportA5MBNonRoadFile() throws Exception {
        File folder = new File("test/data/orl");
        ORLNonRoadImporter importer = new ORLNonRoadImporter(folder, new String[] { "nonRoad-5MB.txt" }, dataset,
                dbServer, sqlDataTypes);
        importer.run();
    }

}
