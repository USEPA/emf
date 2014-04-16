package gov.epa.emissions.commons.io.speciation;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.SimpleDataset;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.io.ExporterException;
import gov.epa.emissions.commons.io.importer.PersistenceTestCase;
import gov.epa.emissions.commons.io.importer.VersionedDataFormatFactory;
import gov.epa.emissions.commons.io.importer.VersionedImporter;

import java.io.File;
import java.util.Date;
import java.util.List;

public class SpeciationProfileExporterTest extends PersistenceTestCase {

    private SqlDataTypes sqlDataTypes;

    private Dataset dataset;

    public DbServer dbServer;

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
            SpeciationProfileImporter importer = new SpeciationProfileImporter(folder,
                    new String[] { "gspro-speciation.txt" }, dataset, dbServer, sqlDataTypes);
            importer.run();

            SpeciationProfileExporter exporter = new SpeciationProfileExporter(dataset, "", dbServer, 
                    optimizedBatchSize);
            File file = File.createTempFile("speciatiationprofileexported", ".txt");
            exporter.export(file);
            List data = readData(file);

            // reimport the exported file
            SpeciationProfileImporter repeatImporter = new SpeciationProfileImporter(file.getParentFile(),
                    new String[] { file.getName() }, repeatDataset, dbServer, sqlDataTypes);
            repeatImporter.run();

            File repeatFile = File.createTempFile("repeatSpeciatiationprofileexported", ".txt");
            SpeciationProfileExporter repeatExporter = new SpeciationProfileExporter(repeatDataset, "", 
                    dbServer,optimizedBatchSize);
            repeatExporter.export(repeatFile);
            List repeatData = readData(repeatFile);

            assertEquals(data.size(), repeatData.size());
            for (int i = 0; i < data.size(); i++) {
                assertEquals(data.get(i), repeatData.get(i));
            }

            assertEquals(88, exporter.getExportedLinesCount());
        } finally {
            dropDatasetDataTable(repeatDataset);
        }
    }

    public void testExportVersionedChemicalSpeciationData() throws Exception {
        Version version = new Version();
        version.setVersion(0);

        File folder = new File("test/data/speciation");
        importFile(folder, "gspro-speciation.txt", dataset, version);

        File file = File.createTempFile("speciatiationprofileexported", ".txt");
        exportFile(dataset, version, file);

    }

    private void exportFile(Dataset dataset, Version version, File file) throws ExporterException {
        SpeciationProfileExporter exporter = new SpeciationProfileExporter(dataset, "", dbServer, 
                new VersionedDataFormatFactory(version, dataset), optimizedBatchSize, null, null, null);
        exporter.export(file);
    }

    private void importFile(File folder, String fileName, Dataset dataset, Version version) throws Exception {
        DbServer localDbServer = dbSetup.getNewPostgresDbServerInstance();
        SpeciationProfileImporter importer = new SpeciationProfileImporter(folder,
                new String[] { "gspro-speciation.txt" }, dataset, localDbServer, sqlDataTypes,
                new VersionedDataFormatFactory(version, dataset));
        VersionedImporter importerv = new VersionedImporter(importer, dataset, localDbServer, lastModifiedDate(folder,
                fileName));
        importerv.run();
    }

    private Date lastModifiedDate(File folder, String fileName) {
        return new Date(new File(folder, fileName).lastModified());
    }
}
