package gov.epa.emissions.commons.io.other;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.data.SimpleDataset;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.DbUpdate;
import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.io.Exporter;
import gov.epa.emissions.commons.io.importer.Importer;
import gov.epa.emissions.commons.io.importer.PersistenceTestCase;
import gov.epa.emissions.commons.io.importer.VersionedImporter;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class GSCNVImporterExporterTest extends PersistenceTestCase {

    private SqlDataTypes sqlDataTypes;

    private Dataset dataset;

    private DbServer dbServer;

    private Integer optimizedBatchSize;

    protected void setUp() throws Exception {
        super.setUp();

        dbServer = dbSetup.getDbServer();
        sqlDataTypes = dbServer.getSqlDataTypes();
        optimizedBatchSize = new Integer(1000);

        dataset = new SimpleDataset();
        dataset.setName("test");
        dataset.setId(Math.abs(new Random().nextInt()));
        dataset.setDatasetType(new DatasetType("dsType"));
    }

    protected void doTearDown() throws Exception {
        Datasource datasource = dbServer.getEmissionsDatasource();
        DbUpdate dbUpdate = dbSetup.dbUpdate(datasource);
        InternalSource[] src = dataset.getInternalSources();

        if (src != null && src.length > 0)
            dbUpdate.dropTable(datasource.getName(), src[0].getTable());
    }

    public void testImportVersionedGSCNVFile() throws Exception {
        DbServer localDbServer = dbSetup.getNewPostgresDbServerInstance();
        File folder = new File("test/data/other");
        Importer importer = new GSCNVImporter(folder, new String[] { "gscnv_cb05_notoxics_cmaq_29aug2006.out.txt" },
                dataset, localDbServer, sqlDataTypes);
        VersionedImporter importerv = new VersionedImporter(importer, dataset, localDbServer, lastModifiedDate(folder,
                "gscnv_cb05_notoxics_cmaq_29aug2006.out.txt"));
        importerv.run();

        InternalSource[] src = dataset.getInternalSources();

        if (src != null && src.length > 0)
            assertEquals(1231, countRecords(dbServer, src[0].getTable()));
    }

    public void testExportVersionedGSCNVFile() throws Exception {
        DbServer localDbServer = dbSetup.getNewPostgresDbServerInstance();
        File folder = new File("test/data/other");
        Importer importer = new GSCNVImporter(folder, new String[] { "gscnv_cb05_notoxics_cmaq_29aug2006.out.txt" },
                dataset, localDbServer, sqlDataTypes);
        VersionedImporter importerv = new VersionedImporter(importer, dataset, localDbServer, lastModifiedDate(folder,
                "gscnv_cb05_notoxics_cmaq_29aug2006.out.txt"));
        importerv.run();

        InternalSource[] src = dataset.getInternalSources();

        if (src != null && src.length > 0)
            assertEquals(1231, countRecords(dbServer, src[0].getTable()));

        Exporter exporter = new GSCNVExporter(dataset, "", dbServer, optimizedBatchSize);
        File file = doExport(exporter);
        List records = readData(file);
        assertEquals(1231, records.size());
        assertEquals(1231, exporter.getExportedLinesCount());
    }

    private File doExport(Exporter exporter) throws Exception {
        File file = File.createTempFile("exported", ".orl");
        file.deleteOnExit();

        exporter.export(file);

        return file;
    }

    private Date lastModifiedDate(File folder, String fileName) {
        return new Date(new File(folder, fileName).lastModified());
    }

}
