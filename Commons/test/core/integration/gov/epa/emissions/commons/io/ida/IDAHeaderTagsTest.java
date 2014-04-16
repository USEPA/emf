package gov.epa.emissions.commons.io.ida;

import gov.epa.emissions.commons.data.SimpleDataset;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.DbUpdate;
import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.io.importer.NonVersionedDataFormatFactory;
import gov.epa.emissions.commons.io.importer.PersistenceTestCase;

import java.io.File;
import java.sql.SQLException;
import java.util.Random;

public class IDAHeaderTagsTest extends PersistenceTestCase {

    private SqlDataTypes sqlDataTypes;

    private SimpleDataset dataset;

    private DbServer dbServer;

    protected void setUp() throws Exception {
        super.setUp();
        dbServer = dbSetup.getDbServer();
        sqlDataTypes = dbServer.getSqlDataTypes();

        dataset = new SimpleDataset();
        dataset.setName("test");
        dataset.setId(Math.abs(new Random().nextInt()));
    }

    protected void doTearDown() throws Exception {// no op
    }

    private void dropTable() throws Exception, SQLException {
        Datasource datasource = dbServer.getEmissionsDatasource();
        DbUpdate dbUpdate = dbSetup.dbUpdate(datasource);
        dbUpdate.dropTable(datasource.getName(), dataset.getInternalSources()[0].getTable());
    }

    public void testShouldIdentifyAllRequiredTags() throws Exception {
        File folder = new File("test/data/ida");
        String[] fileNames = {"small-area.txt"};
                
        IDAImporter importer = new IDAImporter(dataset, dbServer, sqlDataTypes);
        importer.setup(folder, fileNames, new IDANonPointNonRoadFileFormat(sqlDataTypes), new NonVersionedDataFormatFactory());
        importer.run();
        dropTable();
    }

    public void testShouldIdentifyNoIDATag() throws Exception {
        File folder = new File("test/data/ida");
        String[] fileNames = {"noIDATags.txt"};
        try {
            IDAImporter importer = new IDAImporter(dataset, dbServer, sqlDataTypes);
            importer.setup(folder, fileNames, new IDANonPointNonRoadFileFormat(sqlDataTypes), new NonVersionedDataFormatFactory());
            importer.run();
        } catch (Exception e) {
            assertTrue(e.getMessage().startsWith("The tag - 'IDA' is mandatory"));
            return;
        }

        fail("Should have failed as IDA tag is mandatory");
    }

}
