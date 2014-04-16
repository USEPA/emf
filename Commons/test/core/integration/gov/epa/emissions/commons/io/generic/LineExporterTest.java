package gov.epa.emissions.commons.io.generic;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.SimpleDataset;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.DbUpdate;
import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.db.TableReader;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.io.CustomCharSetInputStreamReader;
import gov.epa.emissions.commons.io.importer.PersistenceTestCase;
import gov.epa.emissions.commons.io.importer.VersionedDataFormatFactory;
import gov.epa.emissions.commons.io.importer.VersionedImporter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.util.Date;
import java.util.List;

public class LineExporterTest extends PersistenceTestCase {

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

    protected void doTearDown() throws Exception {
        Datasource datasource = dbServer.getEmissionsDatasource();
        DbUpdate dbUpdate = dbSetup.dbUpdate(datasource);
        dbUpdate.dropTable(datasource.getName(), dataset.getInternalSources()[0].getTable());
        dbUpdate.deleteAll("emf", "table_consolidations");
    }

    private Dataset dataset(String name) {
        SimpleDataset dataset = new SimpleDataset();
        dataset.setName(name);
        DatasetType datasetType = new DatasetType("dsType");
        datasetType.setId(1); //fake id
        datasetType.setImporterClassName("gov.epa.emissions.commons.io.generic.LineImporter");
        dataset.setDatasetType(datasetType);
        dataset.setInlineCommentSetting(false);

        return dataset;
    }
    
    public void testExportSmallLineFile() throws Exception {
        try {
            File folder = new File("test/data/orl/nc");
            LineImporter importer = new LineImporter(folder, new String[] { "small-point.txt" }, dataset, dbServer,
                    sqlDataTypes);
            importer.run();

            LineExporter exporter = new LineExporter(dataset, "", dbServer, optimizedBatchSize);
            File file = File.createTempFile("lineexporter", ".txt");
            exporter.export(file);
            assertEquals(22, countRecords());

            // assert records
            List records = readData(file);

            String expectedPattern1 = "#EXPORT_DATE=";
            String expectedPattern2 = "#EXPORT_VERSION_NAME=";
            String expectedPattern3 = "#EXPORT_VERSION_NUMBER=";
            String expectedPattern4 = "#ORL";
            String expectedPattern5 = "37119 0001 0001 1 1 'REXMINC.;CUSTOMDIVISION' 40201301 02 01 60 7.5 375 2083.463 47.16 3083 0714 0 L -80.7081 35.12 17 108883 9.704141 -9 -9 -9 -9 -9";
            String expectedPattern6 = "#Cutomized Division - 1998";
            String expectedPattern7 = "#End Comment";

            assertTrue(records.get(0).toString().startsWith(expectedPattern1));
            assertTrue(records.get(1).toString().startsWith(expectedPattern2));
            assertTrue(records.get(2).toString().startsWith(expectedPattern3));
            assertEquals(expectedPattern4, records.get(3));
            assertEquals(expectedPattern5, records.get(9));
            assertEquals(expectedPattern6, records.get(18));
            assertEquals(expectedPattern7, records.get(24));
            assertEquals(22, exporter.getExportedLinesCount());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void testExportVersionedSmallLineFile() throws Exception {
        DbServer localDbServer = dbSetup.getNewPostgresDbServerInstance();
        Version version = version();
        File folder = new File("test/data/orl/nc");
        LineImporter importer = new LineImporter(folder, new String[] { "small-point.txt" }, dataset, localDbServer,
                sqlDataTypes, new VersionedDataFormatFactory(version, dataset));
        VersionedImporter importer2 = new VersionedImporter(importer, dataset, localDbServer, lastModifiedDate(folder,
                "small-point.txt"));
        importer2.run();

        LineExporter exporter = new LineExporter(dataset, "", dbServer, new VersionedDataFormatFactory(
                version, dataset), optimizedBatchSize, null, null, null);
        File file = File.createTempFile("lineexporter", ".txt");
        exporter.export(file);
        assertEquals(22, countRecords());

        // assert records
        List records = readData(file);

        String expectedPattern1 = "#ORL";
        String expectedPattern2 = "37119 0001 0001 1 1 'REXMINC.;CUSTOMDIVISION' 40201301 02 01 60 7.5 375 2083.463 47.16 3083 0714 0 L -80.7081 35.12 17 108883 9.704141 -9 -9 -9 -9 -9";
        String expectedPattern3 = "#Cutomized Division - 1998";
        String expectedPattern4 = "#End Comment";

        assertEquals(expectedPattern1, records.get(3));
        assertEquals(expectedPattern2, records.get(9));
        assertEquals(expectedPattern3, records.get(18));
        assertEquals(expectedPattern4, records.get(24));
    }
 
    public void testExportSmallLineFileWithSepcialChars() throws Exception {
        DbServer localDbServer = dbSetup.getNewPostgresDbServerInstance();
        Version version = version();
        File folder = new File("test/data/ida");
        LineImporter importer = new LineImporter(folder, new String[] { "short_2000negu_canada_province_truncated_ida.txt" }, dataset, localDbServer,
                sqlDataTypes, new VersionedDataFormatFactory(version, dataset));
        VersionedImporter importer2 = new VersionedImporter(importer, dataset, localDbServer, lastModifiedDate(folder,
        "small-point.txt"));
        importer2.run();
        
        LineExporter exporter = new LineExporter(dataset, "", dbServer, new VersionedDataFormatFactory(
                version, dataset), optimizedBatchSize, null, null, null);
        File file = File.createTempFile("lineexporter", ".txt");
        exporter.export(file);
        assertEquals(29, countRecords());
        
        // assert records
        List records = readData(file);
        
        String expectedPattern1 = "HYDRO-QU�BEC;Centrale des Iles-de-la-Mad39999999";
        String expectedPattern2 = "LES PAPIERS PERKINS LT�E;Division Lachut10200501";
        
        assertTrue(records.get(14).toString().indexOf(expectedPattern1) > 0);
        assertTrue(records.get(15).toString().indexOf(expectedPattern2) > 0);
    }

    public void testGenericExporterToStringOnVersionedData() throws Exception {
        DbServer localDbServer = dbSetup.getNewPostgresDbServerInstance();
        Version version = version();
        File folder = new File("test/data/ida");
        String fileName = "short_2000negu_canada_province_truncated_ida.txt";
        LineImporter importer = new LineImporter(folder, new String[] { fileName }, dataset, localDbServer,
                sqlDataTypes, new VersionedDataFormatFactory(version, dataset));
        VersionedImporter importer2 = new VersionedImporter(importer, dataset, localDbServer, lastModifiedDate(folder,
        "small-point.txt"));
        importer2.run();
        
        GenericExporterToString exporter = new GenericExporterToString(dataset, "", dbServer, new VersionedDataFormatFactory(
                version, dataset), optimizedBatchSize);
        exporter.export(null);
        String exportedLines = exporter.getOutputString();
        
        BufferedReader fileReader = new BufferedReader(new CustomCharSetInputStreamReader(new FileInputStream(new File(folder, fileName))));
        StringBuffer buffer = new StringBuffer();
        
        String line;
        
        while((line = fileReader.readLine()) != null) {
            buffer.append(line + System.getProperty("line.separator"));
        }
        
        assertEquals(buffer.toString(), exportedLines);
        assertEquals(29, countRecords());
    }

    public void testGenericExporterToStringOnNonVersionedData() throws Exception {
        DbServer localDbServer = dbSetup.getNewPostgresDbServerInstance();
        File folder = new File("test/data/ida");
        String fileName = "short_2000negu_canada_province_truncated_ida.txt";
        LineImporter importer = new LineImporter(folder, new String[] { fileName }, dataset, localDbServer,
                sqlDataTypes);
        importer.run();
        
        GenericExporterToString exporter = new GenericExporterToString(dataset, "", dbServer, optimizedBatchSize);
        exporter.export(null);
        String exportedLines = exporter.getOutputString();
        
        BufferedReader fileReader = new BufferedReader(new CustomCharSetInputStreamReader(new FileInputStream(new File(folder, fileName))));
        StringBuffer buffer = new StringBuffer();
        
        String line;
        
        while((line = fileReader.readLine()) != null) {
            buffer.append(line + System.getProperty("line.separator"));
        }
        
        assertEquals(buffer.toString(), exportedLines);
        assertEquals(29, countRecords());
    }
    
    private Date lastModifiedDate(File folder, String fileName) {
        return new Date(new File(folder, fileName).lastModified());
    }

    private Version version() {
        Version version = new Version();
        version.setVersion(0);
        version.setDatasetId(dataset.getId());
        return version;
    }

    private int countRecords() {
        Datasource datasource = dbServer.getEmissionsDatasource();
        TableReader tableReader = tableReader(datasource);
        return tableReader.count(datasource.getName(), dataset.getInternalSources()[0].getTable());
    }

    protected boolean isComment(String line) {
        return false;
    }

    protected boolean isNotEmpty(String line) {
        return true;
    }

}
