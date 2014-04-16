package gov.epa.emissions.commons.io.external;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.ExternalSource;
import gov.epa.emissions.commons.data.SimpleDataset;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.io.importer.PersistenceTestCase;
import gov.epa.emissions.commons.io.importer.VersionedDataFormatFactory;
import gov.epa.emissions.commons.io.importer.VersionedImporter;

import java.io.File;
import java.util.Date;
import java.util.Random;

public class ExternalFilesExImporterTest extends PersistenceTestCase {

    private SqlDataTypes sqlDataTypes;

    private Dataset dataset;

    private DbServer dbServer;

    private Integer optimizedBatchSize;
    
    
    protected void setUp() throws Exception {
        super.setUp();
        
        optimizedBatchSize = new Integer(10000);

        dbServer = dbSetup.getDbServer();
        sqlDataTypes = dbServer.getSqlDataTypes();

        dataset = new SimpleDataset();
        dataset.setName("test");
        dataset.setId(Math.abs(new Random().nextInt()));
        dataset.setDatasetType(new DatasetType("externalDSType"));
    }

    protected void doTearDown() throws Exception {
        //Nothing to tear down
    }

    public void testExportExternalFilesShouldSucceed() throws Exception {
        File folder = new File("test/data/other");
        ExternalFilesImporter importer = new ExternalFilesImporter(folder, new String[]{"pstk.m3.txt", "costcy.txt", "costcy2.txt"},
                dataset, dbServer, sqlDataTypes);
        importer.run();
        ExternalSource[] srcs = importer.getExternalSources();

        ExternalFilesExporter exporter = new ExternalFilesExporter(dataset, "", dbServer, optimizedBatchSize);
        File exportfile = File.createTempFile("ExternalExported", ".txt");
        exporter.setExternalSources(srcs);
        exporter.export(exportfile);
        assertEquals(3, exporter.getExportedLinesCount());
        assertEquals(3, srcs.length);
        assertTrue(srcs[0].getDatasource().endsWith("pstk.m3.txt"));
        assertTrue(srcs[1].getDatasource().endsWith("costcy.txt"));
        assertTrue(srcs[2].getDatasource().endsWith("costcy2.txt"));
    }

    public void testExportVersionedExportExternalFilesShouldSucceed() throws Exception {
        DbServer localDbServer = dbSetup.getNewPostgresDbServerInstance();
        Version version = new Version();
        version.setVersion(0);
        
        File folder = new File("test/data/other");
        ExternalFilesImporter importer = new ExternalFilesImporter(folder, new String[]{"pstk.m3.txt", "costcy.txt", "costcy2.txt"},
                dataset, localDbServer, sqlDataTypes, new VersionedDataFormatFactory(version, dataset));
        VersionedImporter importerv = new VersionedImporter(importer, dataset, localDbServer, new Date());
        importerv.run();
        ExternalSource[] srcs = importer.getExternalSources();

        ExternalFilesExporter exporter = new ExternalFilesExporter(dataset, "", localDbServer, new VersionedDataFormatFactory(version, dataset), optimizedBatchSize, null, null, "");
        File exportfile = File.createTempFile("ExternalExported", ".txt");
        exporter.setExternalSources(srcs);
        exporter.export(exportfile);
        assertEquals(3, exporter.getExportedLinesCount());
        assertEquals(3, srcs.length);
        assertTrue(srcs[0].getDatasource().endsWith("pstk.m3.txt"));
        assertTrue(srcs[1].getDatasource().endsWith("costcy.txt"));
        assertTrue(srcs[2].getDatasource().endsWith("costcy2.txt"));
    }
    
    public void testExportExternalFilesShouldFail() throws Exception {
        File folder = new File("test/data/other");
        ExternalFilesImporter importer = new ExternalFilesImporter(folder, new String[]{"pstk.m3.txt", "costcy.txt", "costcy2.txt", "does not exist"},
                dataset, dbServer, sqlDataTypes);
        importer.run();
        ExternalSource[] srcs = importer.getExternalSources();
        
        assertEquals(4, srcs.length);
        assertTrue(srcs[0].getDatasource().endsWith("pstk.m3.txt"));
        assertTrue(srcs[1].getDatasource().endsWith("costcy.txt"));
        assertTrue(srcs[2].getDatasource().endsWith("costcy2.txt"));
        assertTrue(srcs[3].getDatasource().endsWith("does not exist"));
        
        ExternalFilesExporter exporter = new ExternalFilesExporter(dataset, "", dbServer, optimizedBatchSize);
        File exportfile = File.createTempFile("ExternalExported", ".txt");
        
        try {
            exporter.setExternalSources(srcs);
            exporter.export(exportfile);
        } catch (Exception e) {
            assertEquals(3, exporter.getExportedLinesCount());
            assertTrue(e.getMessage().contains("The file " + folder.getAbsolutePath() + File.separator + "does not exist doesn't exist."));
        }
    }
    
    public void testExportVersionedExportExternalFilesShouldFail() throws Exception {
        DbServer localDbServer = dbSetup.getNewPostgresDbServerInstance();
        Version version = new Version();
        version.setVersion(0);
        
        File folder = new File("test/data/other");
        ExternalFilesImporter importer = new ExternalFilesImporter(folder, new String[]{"pstk.m3.txt", "costcy.txt", "costcy2.txt", "does not exist"},
                dataset, localDbServer, sqlDataTypes, new VersionedDataFormatFactory(version, dataset));
        VersionedImporter importerv = new VersionedImporter(importer, dataset, localDbServer, new Date());
        importerv.run();
        ExternalSource[] srcs = importer.getExternalSources();
        
        assertEquals(4, srcs.length);
        assertTrue(srcs[0].getDatasource().endsWith("pstk.m3.txt"));
        assertTrue(srcs[1].getDatasource().endsWith("costcy.txt"));
        assertTrue(srcs[2].getDatasource().endsWith("costcy2.txt"));
        assertTrue(srcs[3].getDatasource().endsWith("does not exist"));
        
        ExternalFilesExporter exporter = new ExternalFilesExporter(dataset, "", localDbServer, new VersionedDataFormatFactory(version, dataset), optimizedBatchSize, null, null, "");
        File exportfile = File.createTempFile("ExternalExported", ".txt");
        
        try {
            exporter.setExternalSources(srcs);
            exporter.export(exportfile);
        } catch (Exception e) {
            assertEquals(3, exporter.getExportedLinesCount());
            assertTrue(e.getMessage().contains("The file " + folder.getAbsolutePath() + File.separator + "does not exist doesn't exist."));
        }
    }
    
}
