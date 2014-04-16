package gov.epa.emissions.commons.io.other;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.DatasetType;
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
import java.util.List;

public class InventoryTableExporterTest extends PersistenceTestCase {

    private SqlDataTypes sqlDataTypes;

    private Dataset dataset;

    private DbServer dbServer;

    private Integer optimizedBatchSize;

    protected void setUp() throws Exception {
        super.setUp();

        dbServer = dbSetup.getDbServer();
        sqlDataTypes = dbServer.getSqlDataTypes();

        optimizedBatchSize = new Integer(10000);

        dataset = new SimpleDataset();
        dataset.setName("test");
        dataset.setDatasetType(new DatasetType("dsType"));
    }

    protected void doTearDown() throws Exception {
        Datasource datasource = dbServer.getEmissionsDatasource();
        DbUpdate dbUpdate = dbSetup.dbUpdate(datasource);
        dbUpdate.dropTable(datasource.getName(), dataset.getInternalSources()[0].getTable());
    }

    public void testExportChemicalSpeciationData() throws Exception {
        File folder = new File("test/data/other");
        InventoryTableImporter importer = new InventoryTableImporter(folder,
                new String[] { "CAPandHAP_INVTABLE31aug2006.txt" }, dataset, dbServer, sqlDataTypes);
        importer.run();

        InventoryTableExporter exporter = new InventoryTableExporter(dataset, "", dbServer, 
                optimizedBatchSize);
        File file = File.createTempFile("inventorytableexported", ".txt");
        exporter.export(file);
        
        List data = readData(file);

        String expectedFirst = "HGSUM           593748                   Y 0.8696      N     tons/yr          Mercury, Unspeciated                    Methyl Mercury                          ";
        String expectedLast = "DIMTHYLAMAZ     60117                    N    1.0  N   N     tons/yr          Dimethyl aminoazobenzene, 4- , fine PM  4-Dimethylaminoazobenzene               ";
        assertEquals(expectedFirst, data.get(0));
        assertEquals(expectedLast, data.get(597));
        assertEquals(598, countRecords());
        assertEquals(598, exporter.getExportedLinesCount());
    }

    public void testExportVersionedChemicalSpeciationData() throws Exception {
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

        InventoryTableExporter exporter = new InventoryTableExporter(dataset, "", dbServer, 
                new VersionedDataFormatFactory(version, dataset), optimizedBatchSize, null, null, null);
        File file = File.createTempFile("inventorytableexported", ".txt");
        exporter.export(file);
        
        //FIXME: compare with original data
        assertEquals(598, countRecords());
    }

    private int countRecords() {
        Datasource datasource = dbServer.getEmissionsDatasource();
        TableReader tableReader = tableReader(datasource);
        return tableReader.count(datasource.getName(), dataset.getInternalSources()[0].getTable());
    }
    
    private Date lastModifiedDate(File folder, String fileName) {
        return new Date(new File(folder, fileName).lastModified());
    }
}
