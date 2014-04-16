package gov.epa.emissions.commons.io.other;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.SimpleDataset;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.DbUpdate;
import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.io.importer.PersistenceTestCase;
import gov.epa.emissions.commons.io.importer.VersionedDataFormatFactory;
import gov.epa.emissions.commons.io.importer.VersionedImporter;

import java.io.File;
import java.util.Date;
import java.util.List;

public class ProjectionPacketExImporterTest extends PersistenceTestCase {

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

    public void testExportImportProjectionPacketData() throws Exception {
        File folder = new File("test/data/other");
        ProjectionPacketImporter importer = new ProjectionPacketImporter(folder, new String[]{"alm_projections_2.txt"}, 
                dataset, dbServer, sqlDataTypes);
        importer.run();
        
        System.out.println(dataset.getDescription());

        File exportfile = File.createTempFile("ProjectPacketExportFile", ".txt");
        ProjectionPacketExporter exporter = new ProjectionPacketExporter(dataset, "", dbServer, optimizedBatchSize);
        exporter.export(exportfile);

        List data = readData(exportfile);
        assertEquals(66, data.size());
        assertEquals("/PROJECTION 2000 2009/", (String) data.get(0));
        assertEquals("/END/", (String) data.get(65));
        exportfile.delete();
        assertEquals(64, exporter.getExportedLinesCount());
    }
    
    public void testExportImportVersionedProjectionPacketData() throws Exception {
        DbServer localDbServer = dbSetup.getNewPostgresDbServerInstance();
        Version version = new Version();
        version.setVersion(0);
        version.setDatasetId(dataset.getId());
        
        File folder = new File("test/data/other");
        ProjectionPacketImporter importer = new ProjectionPacketImporter(folder, new String[]{"alm_projections_2.txt"}, 
                dataset, localDbServer, sqlDataTypes, new VersionedDataFormatFactory(version, dataset));
        VersionedImporter importerv = new VersionedImporter(importer, dataset, localDbServer, lastModifiedDate(folder,"SRGDESC.txt"));
        importerv.run();

        System.out.println(dataset.getDescription());
        
        File exportfile = File.createTempFile("SRGDescExported", ".txt");
        ProjectionPacketExporter exporter = new ProjectionPacketExporter(dataset, "", dbServer, 
                new VersionedDataFormatFactory(version, dataset),optimizedBatchSize, null, null, null);
        exporter.export(exportfile);

        List data = readData(exportfile);
        assertEquals(66, data.size());
        assertEquals("/PROJECTION 2000 2009/", (String) data.get(0));
        assertEquals("/END/", (String) data.get(65));
        assertEquals(64, exporter.getExportedLinesCount());
        exportfile.delete();
    }

    private Date lastModifiedDate(File folder, String fileName) {
        return new Date(new File(folder, fileName).lastModified());
    }
}
