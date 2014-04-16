package gov.epa.emissions.commons.io.csv;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.data.SimpleDataset;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.DbUpdate;
import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.db.TableReader;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.io.ExporterException;
import gov.epa.emissions.commons.io.importer.Importer;
import gov.epa.emissions.commons.io.importer.ImporterException;
import gov.epa.emissions.commons.io.importer.PersistenceTestCase;
import gov.epa.emissions.commons.io.importer.VersionedDataFormatFactory;
import gov.epa.emissions.commons.io.importer.VersionedImporter;

import java.io.File;
import java.util.Date;
import java.util.List;

public class CSVFileExImporterTest extends PersistenceTestCase {
    private Datasource datasource;

    private Dataset dataset;

    private SqlDataTypes sqlDataTypes;

    private String tableName;

    private DbServer dbServer;

    private Integer optimizedBatchSize;

    protected void setUp() throws Exception {
        super.setUp();
        tableName = "test";
        dataset = dataset(tableName);

        dbServer = dbSetup.getDbServer();
        sqlDataTypes = dbServer.getSqlDataTypes();
        datasource = dbServer.getEmissionsDatasource();
        optimizedBatchSize = new Integer(10000);
    }

    private Dataset dataset(String name) {
        SimpleDataset dataset = new SimpleDataset();
        dataset.setName(name);
        DatasetType datasetType = new DatasetType("dsType");
        datasetType.setId(1); //fake id
        datasetType.setImporterClassName("gov.epa.emissions.commons.io.csv.CSVImporter");
        dataset.setDatasetType(datasetType);
        dataset.setInlineCommentSetting(false);

        return dataset;
    }

    protected void doTearDown() throws Exception {
        DbUpdate dbUpdate = dbSetup.dbUpdate(datasource);
        InternalSource[] sources = dataset.getInternalSources();

        if (sources != null && sources.length > 0)
            dbUpdate.dropTable(datasource.getName(), sources[0].getTable());
        
        dbUpdate.deleteAll("emf", "table_consolidations");
    }

    public void testImportASmallAndSimplePointFileWithCSVImporter() throws Exception {
        File folder = new File("test/data/csv");
        Importer importer = new CSVImporter(folder, new String[] { "pollutants.txt" }, dataset, dbServer, sqlDataTypes);
        importer.run();
        int rows = countRecords();
        assertEquals(8, rows);

        File file = File.createTempFile("ExportedSmallAndSimplePointFile", ".txt");
        CSVExporter exporter = new CSVExporter(dataset, "", dbServer, optimizedBatchSize);
        exporter.export(file);

        List<String> data = readData(file);
        assertEquals(data.get(0), "pollutant_code,pollutant_name,fake_amount");
        assertEquals(data.get(8), "\"VOC\",\"VOC\",15.55");
        assertEquals(8, exporter.getExportedLinesCount());
    }

    public void testImportASmallProblematicFile() throws Exception {
        try {
            File folder = new File("test/data/csv");
            Importer importer = new CSVImporter(folder, new String[] { "wont_import.txt" }, dataset, dbServer,
                    sqlDataTypes);
            importer.run();
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("Column names were not found in file"));
        }
    }

    public void testImportASmallAndSimplePointFileWithDigitsInColumnNames() throws Exception {
        File folder = new File("test/data/csv");
        Importer importer = new CSVImporter(folder, new String[] { "pollutants-with-digit-column-names.txt" }, dataset,
                dbServer, sqlDataTypes);
        importer.run();

        int rows = countRecords();
        assertEquals(8, rows);

        File file = File.createTempFile("ExportedSmallAndSimplePointFile", ".txt");
        CSVExporter exporter = new CSVExporter(dataset, "", dbServer, optimizedBatchSize);
        exporter.export(file);

        List<String> data = readData(file);
        assertEquals(data.get(0), "_123pollutant_code,_456pollutant_4name,fake_amount567");
        assertEquals(data.get(8), "\"VOC\",\"VOC\",15.55");
        assertEquals(8, exporter.getExportedLinesCount());
    }

    public void testImportASmallAndSimplePointFileWithVersionedCSVImporter() throws Exception {
        Version version = new Version();
        version.setVersion(0);
        DbServer localDBServer = dbSetup.getNewPostgresDbServerInstance();

        File folder = new File("test/data/reference");
        Importer importer = new CSVImporter(folder, new String[] { "pollutants.txt" }, dataset, localDBServer,
                sqlDataTypes, new VersionedDataFormatFactory(version, dataset));
        VersionedImporter importerv = new VersionedImporter(importer, dataset, localDBServer, lastModifiedDate(folder,
                "pollutants.txt"));
        importerv.run();

        int rows = countRecords();
        assertEquals(8, rows);

        File file = File.createTempFile("ExportedSmallAndSimplePointFile", ".txt");
        CSVExporter exporter = new CSVExporter(dataset, "", dbServer, new VersionedDataFormatFactory(version,
                dataset), optimizedBatchSize, null, null, null);
        exporter.export(file);

        List<String> data = readData(file);
        assertEquals(data.get(1), "\"CO\",\"CO\"");
        assertEquals(data.get(8), "\"VOC\",\"VOC\"");
    }

    public void testShouldExImportSimpleCommaDelimitedCSVFile() throws Exception {
        File folder = new File("test/data/csv");
        Importer importer = new CSVImporter(folder, new String[] { "generation_file.csv" }, dataset, dbServer,
                sqlDataTypes);
        importer.run();

        int rows = countRecords();
        assertEquals(14, rows);

        File file = File.createTempFile("ExportedCommaDelimitedFile", ".txt");
        CSVExporter exporter = new CSVExporter(dataset, "", dbServer, optimizedBatchSize);
        exporter.export(file);

        List<String> data = readData(file);
        assertEquals(data.get(1), "\"USA\",\"Population\",\"100\",\"NO\",\"YES\"");
        assertEquals(data.get(7), "\"USA\",\"3/4 Total Roadway Miles plus 1/4 Population\",\"255\",\"YES\",\"\"");
    }

    public void testShouldExImportSurrogateSpecFile() throws Exception {
        Dataset repeatDataset = dataset("repeatTest");

        try {
            File folder = new File("test/data/csv");
            String fileName = "surrogate_specification.csv";
            importFile(folder, fileName, dataset);

            int rows = countRecords();
            assertEquals(14, rows);

            File file = File.createTempFile("ExportedCommaDelimitedFile", ".txt");
            exportFile(file, dataset);

            importFile(file.getParentFile(), file.getName(), repeatDataset);
            File repeatFile = File.createTempFile("RepeatExportedCommaDelimitedFile", ".txt");
            exportFile(repeatFile, repeatDataset);

            List<String> data = readData(file);
            List<String> repeatData = readData(repeatFile);

            assertEquals(data.size(), repeatData.size());
            for (int i = 0; i < data.size(); i++) {
                assertEquals(data.get(i), repeatData.get(i));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dropTable(repeatDataset.getInternalSources()[0].getTable(), datasource);
        }

    }

    private void exportFile(File file, Dataset dataset) throws ExporterException {
        CSVExporter exporter = new CSVExporter(dataset, "", dbServer, optimizedBatchSize);
        exporter.export(file);
    }

    private void importFile(File folder, String fileName, Dataset dataset) throws ImporterException {
        Importer importer = new CSVImporter(folder, new String[] { fileName }, dataset, dbServer, sqlDataTypes);
        importer.run();
    }

    public void testShouldExImportShapeCatFile() throws Exception {
        Version version = new Version();
        version.setVersion(0);
        DbServer localDBServer = dbSetup.getNewPostgresDbServerInstance();

        File folder = new File("test/data/csv");

        Importer importer = new CSVImporter(folder, new String[] { "shapefile_catalog.csv" }, dataset, localDBServer,
                sqlDataTypes, new VersionedDataFormatFactory(version, dataset));
        VersionedImporter importerv = new VersionedImporter(importer, dataset, localDBServer, lastModifiedDate(folder,
                "shapefile_catalog.csv"));
        importerv.run();

        int rows = countRecords();
        assertEquals(41, rows);

        File file = File.createTempFile("ExportedCommaDelimitedFile", ".txt");
        CSVExporter exporter = new CSVExporter(dataset, "", dbServer, new VersionedDataFormatFactory(version,
                dataset), optimizedBatchSize, null, null, null);
        exporter.export(file);

        List<String> data = readData(file);
        String expect = "\"cnty_tn_lcc\",\"D:\\MIMS\\mimssp_7_2005\\data\\\",\"SPHERE\","
                + "\"proj=lcc,+lat_1=33,+lat_2=45,+lat_0=40,+lon_0=-97\","
                + "\"TN county boundaries\",\"from UNC CEP machine\",\"\"";
        assertEquals(expect, data.get(1));
        expect = "\"us_ph\",\"D:\\MIMS\\emiss_shp2003\\us\\\",\"\",\"\",\"The change in housing between 1990 and 2000\",\"US Census Bureau\",\"No Data\"";
        assertEquals(data.get(7), expect);
    }

    private int countRecords() {
        TableReader tableReader = tableReader(datasource);
        return tableReader.count(datasource.getName(), dataset.getInternalSources()[0].getTable());
    }

    private Date lastModifiedDate(File folder, String fileName) {
        return new Date(new File(folder, fileName).lastModified());
    }
}
