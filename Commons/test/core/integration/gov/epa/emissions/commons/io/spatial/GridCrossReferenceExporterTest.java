package gov.epa.emissions.commons.io.spatial;

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

public class GridCrossReferenceExporterTest extends PersistenceTestCase {

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

    public void testExportGridCrossRefData() throws Exception {
        File folder = new File("test/data/spatial");
        GridCrossReferenceImporter importer = new GridCrossReferenceImporter(folder, new String[]{"amgref.txt"},
                dataset, dbServer, sqlDataTypes);
        importer.run();

        GridCrossReferenceExporter exporter = new GridCrossReferenceExporter(dataset, "", dbServer, optimizedBatchSize);
        File file = File.createTempFile("GridCrossRefExported", ".txt");
        exporter.export(file);
        // FIXME: compare the original file and the exported file.
        assertEquals(22, countRecords());
        assertEquals(22, exporter.getExportedLinesCount());
    }
    
    public void testExportVersionedGridCrossRefData() throws Exception {
        DbServer localDbServer = dbSetup.getNewPostgresDbServerInstance();
        Version version = new Version();
        version.setVersion(0);

        File folder = new File("test/data/spatial");
        GridCrossReferenceImporter importer = new GridCrossReferenceImporter(folder, new String[]{"amgref.txt"},
                dataset, localDbServer, sqlDataTypes, new VersionedDataFormatFactory(version, dataset));
        VersionedImporter importerv = new VersionedImporter(importer, dataset, localDbServer, lastModifiedDate(folder,"amgref.txt"));
        importerv.run();
        
        GridCrossReferenceExporter exporter = new GridCrossReferenceExporter(dataset, "", dbServer, 
                new VersionedDataFormatFactory(version, dataset),optimizedBatchSize, null, null, null);
        File file = File.createTempFile("GridCrossRefExported", ".txt");
        exporter.export(file);
        // FIXME: compare the original file and the exported file.
        assertEquals(22, countRecords());
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
