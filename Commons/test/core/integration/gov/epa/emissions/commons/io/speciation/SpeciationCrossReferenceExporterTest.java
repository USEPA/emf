package gov.epa.emissions.commons.io.speciation;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.SimpleDataset;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.io.ExporterException;
import gov.epa.emissions.commons.io.importer.ImporterException;
import gov.epa.emissions.commons.io.importer.NonVersionedDataFormatFactory;
import gov.epa.emissions.commons.io.importer.PersistenceTestCase;
import gov.epa.emissions.commons.io.importer.VersionedDataFormatFactory;
import gov.epa.emissions.commons.io.importer.VersionedImporter;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class SpeciationCrossReferenceExporterTest extends PersistenceTestCase {

    private SqlDataTypes sqlDataTypes;

    private Dataset dataset;

    private DbServer dbServer;

    private Integer optimizedBatchSize;

    protected void setUp() throws Exception {
        super.setUp();

        dbServer = dbSetup.getDbServer();
        sqlDataTypes = dbServer.getSqlDataTypes();

        optimizedBatchSize = new Integer(10000);
        dataset = dataset("test");
    }

    private Dataset dataset(String name) {
        Dataset dataset = new SimpleDataset();
        dataset.setName(name);
        dataset.setId(Math.abs(new Random().nextInt()));
        dataset.setDatasetType(new DatasetType("dsType"));
        return dataset;
    }

    protected void doTearDown() throws Exception {
        dropDatasetDataTable(dataset);
    }

    public void testExportChemicalSpeciationData() throws Exception {
        Dataset repeatDataset = dataset("repeatTest");
        try {

            File folder = new File("test/data/speciation");
            importFile(folder, "gsref-point.txt", dataset);

            File exportfile = File.createTempFile("SpeciatiationCrossRefExported", ".txt");
            exportFile(dataset, exportfile);
            List data = readData(exportfile);

            // reimport the exported dataset
            importFile(exportfile.getParentFile(), exportfile.getName(), repeatDataset);
            File repeatExportfile = File.createTempFile("repeatSpeciatiationCrossRefExported", ".txt");
            exportFile(repeatDataset, repeatExportfile);
            List repeatData = readData(repeatExportfile);
            assertEquals(data.size(), repeatData.size());
            for (int i = 0; i < data.size(); i++) {
                assertEquals(data.get(i), repeatData.get(i));
            }
        } finally {
            dropDatasetDataTable(repeatDataset);
        }
    }

    public void testExportVersionedChemicalSpeciationData() throws Exception {
        DbServer localDbServer = dbSetup.getNewPostgresDbServerInstance();
        Version version = version(dataset.getId());

        File folder = new File("test/data/speciation");
        String fileName = "gsref-point.txt";

        SpeciationCrossReferenceImporter importer = new SpeciationCrossReferenceImporter(folder,
                new String[] { fileName }, dataset, localDbServer, sqlDataTypes, new VersionedDataFormatFactory(version,
                        dataset));
        VersionedImporter importerv = new VersionedImporter(importer, dataset, localDbServer, lastModifiedDate(folder,
                fileName));
        importerv.run();

        File exportfile = File.createTempFile("SpeciatiationCrossRefExported", ".txt");
        SpeciationCrossReferenceExporter exporter = new SpeciationCrossReferenceExporter(dataset, "", dbServer,
                new NonVersionedDataFormatFactory(), optimizedBatchSize, null, null, null);

        exporter.export(exportfile);

        List data = readData(exportfile);
        assertEquals(154, data.size());

    }

    private void exportFile(Dataset dataset, File exportfile) throws ExporterException {
        SpeciationCrossReferenceExporter exporter = new SpeciationCrossReferenceExporter(dataset, "", dbServer, 
                new NonVersionedDataFormatFactory(), optimizedBatchSize, null, null, null);

        exporter.export(exportfile);
    }

    private void importFile(File folder, String fileName, Dataset dataset) throws ImporterException {
        SpeciationCrossReferenceImporter importer = new SpeciationCrossReferenceImporter(folder,
                new String[] { fileName }, dataset, dbServer, sqlDataTypes, new NonVersionedDataFormatFactory());
        importer.run();
    }

    private Version version(int id) {
        Version version = new Version();
        version.setVersion(0);
        version.setDatasetId(id);
        return version;
    }

    private Date lastModifiedDate(File folder, String fileName) {
        return new Date(new File(folder, fileName).lastModified());
    }
}
