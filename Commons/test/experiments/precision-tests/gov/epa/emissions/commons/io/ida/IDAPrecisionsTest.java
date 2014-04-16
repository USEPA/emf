package gov.epa.emissions.commons.io.ida;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.SimpleDataset;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.db.TableReader;
import gov.epa.emissions.commons.io.importer.PersistenceTestCase;

import java.io.File;
import java.util.Random;

public class IDAPrecisionsTest extends PersistenceTestCase {

    private SqlDataTypes sqlDataTypes;

    private Dataset dataset;

    private DbServer dbServer;

    protected void setUp() throws Exception {
        super.setUp();
        dbServer = dbSetup.getDbServer();
        sqlDataTypes = dbServer.getSqlDataTypes();

        dataset = new SimpleDataset();
        int id = Math.abs(new Random().nextInt());
        dataset.setName("test_" + id);
        dataset.setId(id);
    }

    protected void doTearDown() throws Exception {// no op
    }

    public void testShouldImportASmallAreaFile() throws Exception {
        File folder = new File("test/data/ida");
        String[] fileNames = { "area-precisions-test.txt" };
        IDANonPointNonRoadImporter importer = new IDANonPointNonRoadImporter(folder, fileNames, dataset, dbServer,
                sqlDataTypes);
        importer.run();
        // assert
        Datasource datasource = dbServer.getEmissionsDatasource();
        TableReader tableReader = tableReader(datasource);
        assertEquals(11, tableReader.count(datasource.getName(), dataset.getInternalSources()[0].getTable()));
    }

}
