package gov.epa.emissions.commons.io.other;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.data.SimpleDataset;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.DbUpdate;
import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.db.TableReader;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.io.importer.PersistenceTestCase;
import gov.epa.emissions.commons.io.importer.VersionedDataFormatFactory;
import gov.epa.emissions.commons.io.importer.VersionedImporter;

import java.io.File;
import java.util.Date;
import java.util.Random;

public class InventoryTableImporterTest extends PersistenceTestCase {

    private SqlDataTypes sqlDataTypes;

    private Dataset dataset;

    private DbServer dbServer;

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
        InternalSource[] src = dataset.getInternalSources();

        if (src != null && src.length > 0)
            dbUpdate.dropTable(datasource.getName(), src[0].getTable());
    }

    public void testImportInventoryTableData() throws Exception {
        File folder = new File("test/data/other");
        InventoryTableImporter importer = new InventoryTableImporter(folder,
                new String[] { "CAPandHAP_INVTABLE31aug2006.txt" }, dataset, dbServer, sqlDataTypes);
        importer.run();

        assertEquals(598, countRecords());
    }

    public void testImportVersionedInventoryTableData() throws Exception {
        DbServer localDbServer = dbSetup.getNewPostgresDbServerInstance();
        Version version = new Version();
        version.setVersion(0);

        File folder = new File("test/data/other");
        InventoryTableImporter importer = new InventoryTableImporter(folder,
                new String[] { "CAPandHAP_INVTABLE31aug2006.txt" }, dataset, localDbServer, sqlDataTypes,
                new VersionedDataFormatFactory(version, dataset));
        VersionedImporter importerv = new VersionedImporter(importer, dataset, localDbServer, lastModifiedDate(folder,
                "CAPandHAP_INVTABLE31aug2006.txt"));
        importerv.run();

        assertEquals(598, countRecords());
    }

    private int countRecords() {
        Datasource datasource = dbServer.getEmissionsDatasource();
        TableReader tableReader = tableReader(datasource);

        InternalSource[] src = dataset.getInternalSources();
        
        if (src != null && src.length > 0)
            return tableReader.count(datasource.getName(), src[0].getTable());
        
        return 0;
    }

    private Date lastModifiedDate(File folder, String fileName) {
        return new Date(new File(folder, fileName).lastModified());
    }
}
