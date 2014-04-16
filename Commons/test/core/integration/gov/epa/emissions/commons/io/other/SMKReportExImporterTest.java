package gov.epa.emissions.commons.io.other;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.SimpleDataset;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.DbUpdate;
import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.db.TableReader;
import gov.epa.emissions.commons.io.importer.PersistenceTestCase;

import java.io.File;
import java.util.List;
import java.util.Random;

public class SMKReportExImporterTest extends PersistenceTestCase {

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
        dataset.setId(Math.abs(new Random().nextInt()));
        DatasetType type = new DatasetType("dsType");
        type.setId(1);
        dataset.setDatasetType(type);
    }

    protected void doTearDown() throws Exception {
        Datasource datasource = dbServer.getEmissionsDatasource();
        DbUpdate dbUpdate = dbSetup.dbUpdate(datasource);
        dbUpdate.deleteAll("emf", "table_consolidations");
        dbUpdate.deleteAll("emf", "table_consolidations");
    }

    public void testImportSMKreportDataSemicolon() throws Exception {
        File folder = new File("test/data/other");
        SMKReportImporter importer = new SMKReportImporter(folder,
                new String[] { "smkreport-semicolon-state_scc.txt" }, dataset, dbServer, sqlDataTypes);
        importer.run();
        assertEquals(34, countRecords());

        File exportfile = File.createTempFile("SMKreportSemicolonExported", ".txt");
        SMKReportExporter exporter = new SMKReportExporter(dataset, "", dbServer, optimizedBatchSize);
        exporter.export(exportfile);

        List data = readData(exportfile);
        assertEquals("Processed as Mobile sources", data.get(0));
        assertEquals("06/09/2002;001000;\"Alabama\";2201001110;0.0;0.0;0.0;0.7509;0.0;0.95146;0.0;0.0;0.0;0.0;0.0;0.0;0.0;0.0;0.0;0.0;0.0;0.0;0.0;0.0;0.0",
                data.get(12));
        assertEquals("06/09/2002;232000;\"Zacatecas\";2230070000;5.913;"
                + "0.87672;7.8905;0.1394;0.0;0.0;0.042398;0.072102;0.70752;"
                + "0.0056709;0.0040507;0.11081;0.072643;0.10221;0.1324;0.0;"
                + "0.058577;0.72796;0.067411;0.0076535;0.36858", data.get(45));
        assertEquals(34, exporter.getExportedLinesCount());
    }

    public void testImportSMKreportDataPipe() throws Exception {
        File folder = new File("test/data/other");
        SMKReportImporter importer = new SMKReportImporter(folder, new String[] { "smkreport-pipe-hour_scc.txt" },
                dataset, dbServer, sqlDataTypes);
        importer.run();
        assertEquals(44, countRecords());

        File exportfile = File.createTempFile("SMKreportPipeExported", ".txt");
        SMKReportExporter exporter = new SMKReportExporter(dataset, "", dbServer, optimizedBatchSize);
        exporter.export(exportfile);
        List data = readData(exportfile);
        assertEquals("Stationary area", data.get(0));
        assertEquals("07/09/2002;1;9900000100;\"Description unavailable\";0.0;0.0;0.0;0.01946;0.0;0.0;0.0;0.0", data.get(55));
        assertEquals(44, exporter.getExportedLinesCount());
        
        File exportfile2 = File.createTempFile("SMKreportPipeExported", ".txt");
        exporter.setDelimiter("|");
        exporter.export(exportfile2);
        List data2 = readData(exportfile2);
        assertEquals("Stationary area", data2.get(0));
        assertEquals("07/09/2002|1|9900000100|\"Description unavailable\"|0.0|0.0|0.0|0.01946|0.0|0.0|0.0|0.0", data2.get(55));
        assertEquals(88, exporter.getExportedLinesCount());
    }

    public void testExImportSMKreportDataQuotes() throws Exception {
        File folder = new File("test/data/other");
        SMKReportImporter importer = new SMKReportImporter(folder, new String[] { "smkreport-quotes.txt" }, dataset,
                dbServer, sqlDataTypes);
        importer.run();

        File exportfile = File.createTempFile("SMKreportQuotesExported", ".txt");
        SMKReportExporter exporter = new SMKReportExporter(dataset, "", dbServer, optimizedBatchSize);
        exporter.export(exportfile);
        List data = readData(exportfile);
        assertEquals("Annual total data basis in report", data.get(8));
        assertEquals("07/08/2002;2;2302003000;\"Description unavailable\";"
                + "0.0;0.0;0.0027113;0.0;0.0;0.063472;0.032899;0.030573", data.get(16));
        assertEquals(4, exporter.getExportedLinesCount());
        
        File exportfile2 = File.createTempFile("SMKreportQuotesExported", ".txt");
        exporter.setDelimiter("|");
        exporter.export(exportfile2);
        List data2 = readData(exportfile2);
        assertEquals("Annual total data basis in report", data2.get(8));
        assertEquals("07/08/2002|2|2302003000|\"Description unavailable\"|"
                + "0.0|0.0|0.0027113|0.0|0.0|0.063472|0.032899|0.030573", data2.get(16));
        assertEquals(8, exporter.getExportedLinesCount());
    }

    public void testExImportSMKreportDataComma() throws Exception {
        File folder = new File("test/data/other");
        SMKReportImporter importer = new SMKReportImporter(folder, new String[] { "smkreport-comma.txt" }, dataset,
                dbServer, sqlDataTypes);
        importer.run();
        assertEquals(67, countRecords());

        File exportfile = File.createTempFile("SMKreportCommaExported", ".txt");
        SMKReportExporter exporter = new SMKReportExporter(dataset, "", dbServer,optimizedBatchSize);
        exporter.export(exportfile);
        List data = readData(exportfile);
        assertEquals("Stationary area", data.get(0));
        assertEquals("07/08/2002;232000;\"Zacatecas\";4.0529;2.3955;85.799;100.51;25.768;21.124;6.1412;14.983", data
                .get(79));
        assertEquals(67, exporter.getExportedLinesCount());
    }

    public void testExImportSMKreportDataThatHasColumnNamesBeginningWithDigists() throws Exception {
        File folder = new File("test/data/other");
        SMKReportImporter importer = new SMKReportImporter(folder, new String[] { "smkreport-comma-digit-col-names.txt" }, dataset,
                dbServer, sqlDataTypes);
        importer.run();
        assertEquals(67, countRecords());

//        File exportfile = new File("D:\\emf_output\\smkrpt.txt");
        File exportfile = File.createTempFile("SMKreportCommaExported", ".txt");
        SMKReportExporter exporter = new SMKReportExporter(dataset, "", dbServer,optimizedBatchSize);
        exporter.export(exportfile);
        List data = readData(exportfile);
        assertEquals("Stationary area", data.get(0));
        assertEquals("date;region;state;_123co;_456nox;_7voc;nh3;so2;_1999pm10;pm2_5;pmc;comments", data.get(12));
        assertEquals("07/08/2002;232000;\"Zacatecas\";4.0529;2.3955;85.799;100.51;25.768;21.124;6.1412;14.983", data
                .get(79));
        assertEquals(67, exporter.getExportedLinesCount());
    }

    private int countRecords() {
        Datasource datasource = dbServer.getEmissionsDatasource();
        TableReader tableReader = tableReader(datasource);
        return tableReader.count(datasource.getName(), dataset.getInternalSources()[0].getTable());
    }

    protected TableReader tableReader(Datasource datasource) {
        return dbSetup.tableReader(datasource);
    }

}
