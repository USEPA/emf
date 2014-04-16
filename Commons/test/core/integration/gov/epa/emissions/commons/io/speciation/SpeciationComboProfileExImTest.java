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

public class SpeciationComboProfileExImTest extends PersistenceTestCase {

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
        dataset.setDatasetType(new DatasetType("dsType"));
        return dataset;
    }

    protected void doTearDown() throws Exception {
        dropDatasetDataTable(dataset);
    }

    public void testExportChemicalSpeciationComboProfileData() throws Exception {
        Dataset repeatDataset = dataset("repeatTest");
        try {

            File folder = new File("test/data/speciation");
            importFile(folder, "gspro_combo_2002_08feb2008.txt", dataset);

            File exportfile = File.createTempFile("SpeciatiationComboProfileExported", ".txt");
            exportFile(dataset, exportfile);
            List data = readData(exportfile);

            // reimport the exported dataset
            importFile(exportfile.getParentFile(), exportfile.getName(), repeatDataset);
            File repeatExportfile = File.createTempFile("repeatSpeciatiationComboProfileExported", ".txt");
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

    public void testExportVersionedChemicalSpeciationComboProfileData() throws Exception {
        DbServer localDbServer = dbSetup.getNewPostgresDbServerInstance();
        Version version = version(dataset.getId());

        File folder = new File("test/data/speciation");
        String fileName = "gspro_combo_2002_08feb2008.txt";

        SpeciationComboProfileImporter importer = new SpeciationComboProfileImporter(folder,
                new String[] { fileName }, dataset, localDbServer, sqlDataTypes, new VersionedDataFormatFactory(version,
                        dataset));
        VersionedImporter importerv = new VersionedImporter(importer, dataset, localDbServer, lastModifiedDate(folder,
                fileName));
        importerv.run();

        File exportfile = File.createTempFile("SpeciatiationComboProfileExported", ".txt");
        SpeciationComboProfileExporter exporter = new SpeciationComboProfileExporter(dataset, "", dbServer,
                new NonVersionedDataFormatFactory(), optimizedBatchSize, null, null, null);
        exporter.setDelimiter(",");
        exporter.export(exportfile);

        List data = readData(exportfile);
        String line1 = "\"EXH_VOC\",001000,1,2,8750,1.0,8751,0";
        String line1000 = "\"EXH_VOC\",006047,9,2,8750,1.0,8751,0";
        assertEquals(data.get(0), line1);
        assertEquals(data.get(999), line1000);
        assertEquals(1000, data.size());
    }

    private void exportFile(Dataset dataset, File exportfile) throws ExporterException {
        SpeciationComboProfileExporter exporter = new SpeciationComboProfileExporter(dataset, "", dbServer,
                new NonVersionedDataFormatFactory(), optimizedBatchSize, null, null, null);

        exporter.export(exportfile);
    }

    private void importFile(File folder, String fileName, Dataset dataset) throws ImporterException {
        SpeciationComboProfileImporter importer = new SpeciationComboProfileImporter(folder,
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
