package gov.epa.emissions.commons.io.temporal;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.SimpleDataset;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.DbUpdate;
import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.db.TableReader;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.io.ExporterException;
import gov.epa.emissions.commons.io.importer.ImporterException;
import gov.epa.emissions.commons.io.importer.PersistenceTestCase;
import gov.epa.emissions.commons.io.importer.VersionedDataFormatFactory;
import gov.epa.emissions.commons.io.importer.VersionedImporter;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class TemporalProfileTest extends PersistenceTestCase {

    private SqlDataTypes typeMapper;

    private TableReader tableReader;

    private Dataset dataset;

    private DbServer dbServer;

    private Integer optimizedBatchSize;

    protected void setUp() throws Exception {
        super.setUp();

        dbServer = dbSetup.getDbServer();
        typeMapper = dbServer.getSqlDataTypes();

        optimizedBatchSize = new Integer(10000);

        tableReader = tableReader(dbServer.getEmissionsDatasource());
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
        Datasource datasource = dbServer.getEmissionsDatasource();
        DbUpdate dbUpdate = dbSetup.dbUpdate(datasource);
        String schema = datasource.getName();

        if (tableReader.exists(schema, "Monthly"))
            dbUpdate.dropTable(schema, "Monthly");
        if (tableReader.exists(schema, "Weekly"))
            dbUpdate.dropTable(schema, "Weekly");
        if (tableReader.exists(schema, "Diurnal_Weekday"))
            dbUpdate.dropTable(schema, "Diurnal_Weekday");
        if (tableReader.exists(schema, "Diurnal_Weekend"))
            dbUpdate.dropTable(schema, "Diurnal_Weekend");

        dbUpdate.deleteAll(schema, "versions");
    }

    public void testShouldImportTwoProfileFilesSuccessively() throws ImporterException {
        runProfileImporter("small.txt");
        assertEquals(10, countRecords("Monthly"));
        assertEquals(13, countRecords("WEEKLY"));
        assertEquals(20, countRecords("DIURNAL_WEEKEND"));
        assertEquals(20, countRecords("DIURNAL_WEEKDAY"));

        runProfileImporter("small.txt");
        assertEquals(20, countRecords("Monthly"));
        assertEquals(26, countRecords("WEEKLY"));
        assertEquals(40, countRecords("DIURNAL_WEEKEND"));
        assertEquals(40, countRecords("DIURNAL_WEEKDAY"));
    }

    public void testShouldReadFromFileAndLoadSamllPacketIntoTable() throws Exception {
        runProfileImporter("small.txt");
        // assert
        assertEquals(10, countRecords("Monthly"));
        assertEquals(13, countRecords("WEEKLY"));

        File exportfile = runProfileExporter();
        // assert records
        List records = readData(exportfile);
        assertEquals(63, records.size());

        String expectedPattern1 = "    1   0   0   0   0   0 110 110 110 223 223 223   0  999";
        String expectedPattern2 = "    8 147 147 147 147 147 135 129  1000";
        String expectedPattern3 = " 2006  88  49  33  24  36 119 332 854 588 493 485 520 535 "
                + "557 648 710 789 867 660 456 387 338 257 176 1000";
        String expectedPattern4 = " 2001 166 122 103  87  92 120 182 263 367 501 623 697 721 "
                + "738 750 752 751 697 584 480 400 331 272 201 1000";

        assertEquals((String) records.get(0), expectedPattern1);
        assertEquals((String) records.get(17), expectedPattern2);
        assertEquals((String) records.get(38), expectedPattern3);
        assertEquals((String) records.get(53), expectedPattern4);
    }

    public void testShouldImportExportVersionedSamllPacketData() throws Exception {
        Version version = version(dataset.getId());
        runVersionProfileImporter("small.txt", dataset, version);

        // assert
        assertEquals(10, countRecords("Monthly"));
        assertEquals(13, countRecords("WEEKLY"));

        File exportfile = runVersionProfileExporter(version);

        // assert records
        List records = readData(exportfile);
        assertEquals(63, records.size());

        String expectedPattern1 = "    1   0   0   0   0   0 110 110 110 223 223 223   0  999";
        String expectedPattern2 = "    8 147 147 147 147 147 135 129  1000";
        String expectedPattern3 = " 2006  88  49  33  24  36 119 332 854 588 493 485 520 535 "
                + "557 648 710 789 867 660 456 387 338 257 176 1000";
        String expectedPattern4 = " 2001 166 122 103  87  92 120 182 263 367 501 623 697 721 "
                + "738 750 752 751 697 584 480 400 331 272 201 1000";
        assertEquals((String) records.get(0), expectedPattern1);
        assertEquals((String) records.get(17), expectedPattern2);
        assertEquals((String) records.get(38), expectedPattern3);
        assertEquals((String) records.get(53), expectedPattern4);
    }

    public void testShouldImportExportVersionedSamllPacketDataForAParticularDataset() throws Exception {
        Version version = version(dataset.getId());
        // first import
        runVersionProfileImporter("small.txt", dataset, version);
        assertEquals(10, countRecords("Monthly"));
        assertEquals(13, countRecords("WEEKLY"));

        // second import
        Dataset dataset2 = dataset("test2");
        Version version2 = version(dataset2.getId());
        runVersionProfileImporter("small.txt", dataset2, version2);
        assertEquals(20, countRecords("Monthly"));
        assertEquals(26, countRecords("WEEKLY"));

        File exportfile = runVersionProfileExporter(version);

        // assert records
        List records = readData(exportfile);
        assertEquals(63, records.size());

        String expectedPattern1 = "    1   0   0   0   0   0 110 110 110 223 223 223   0  999";
        String expectedPattern2 = "    8 147 147 147 147 147 135 129  1000";
        String expectedPattern3 = " 2006  88  49  33  24  36 119 332 854 588 493 485 520 535 "
                + "557 648 710 789 867 660 456 387 338 257 176 1000";
        String expectedPattern4 = " 2001 166 122 103  87  92 120 182 263 367 501 623 697 721 "
                + "738 750 752 751 697 584 480 400 331 272 201 1000";
        assertEquals((String) records.get(0), expectedPattern1);
        assertEquals((String) records.get(17), expectedPattern2);
        assertEquals((String) records.get(38), expectedPattern3);
        assertEquals((String) records.get(53), expectedPattern4);
    }

    public void testShouldReadFromFileAndLoadDiurnalWeekdayPacketIntoTable() throws Exception {
        runProfileImporter("diurnal-weekday.txt");
        // assert
        assertEquals(20, countRecords("DIURNAL_WEEKDAY"));

        File exportfile = runProfileExporter();
        // assert records
        List records = readData(exportfile);
        assertEquals(20, records.size());

        String expectedPattern = "    1   0   0   0   0   0   0   0   0 125 125 125 125 125 125 "
                + "125 125   0   0   0   0   0   0   0   0 1000";

        String actual = (String) records.get(0);
        assertTrue(actual.matches(expectedPattern));
    }

    public void testShouldImportExportVersionedDiurnalWeekdayPacketData() throws Exception {
        Version version = version(dataset.getId());
        runVersionProfileImporter("diurnal-weekday.txt", dataset, version);

        // assert
        assertEquals(20, countRecords("DIURNAL_WEEKDAY"));

        File exportfile = runVersionProfileExporter(version);

        // assert records
        List records = readData(exportfile);
        assertEquals(20, records.size());

        String expectedPattern = "    1   0   0   0   0   0   0   0   0 125 125 125 125 "
                + "125 125 125 125   0   0   0   0   0   0   0   0 1000";

        String actual = (String) records.get(0);
        assertTrue(actual.matches(expectedPattern));
    }

    public void testShouldReadFromFileAndLoadDiurnalWeekendPacketIntoTable() throws Exception {
        runProfileImporter("diurnal-weekend.txt");

        // assert
        assertEquals(20, countRecords("DIURNAL_WEEKEND"));

        File exportfile = runProfileExporter();

        // assert records
        List records = readData(exportfile);
        assertEquals(20, records.size());

        String expectedPattern = "   10   0   0   0   0   0   0   0 100 100 100 100 100 100 100 "
                + "100 100 100   0   0   0   0   0   0   0 1000";
        String actual = (String) records.get(9);
        assertTrue(actual.matches(expectedPattern));
    }

    public void testShouldImportExportVersionedDiurnalWeekendPacketData() throws Exception {
        Version version = version(dataset.getId());
        runVersionProfileImporter("diurnal-weekend.txt", dataset, version);
        // assert
        assertEquals(20, countRecords("DIURNAL_WEEKEND"));

        File exportfile = runVersionProfileExporter(version);
        // assert records
        List records = readData(exportfile);
        assertEquals(20, records.size());

        String expectedPattern = "   10   0   0   0   0   0   0   0 100 100 100 100 100 "
                + "100 100 100 100 100   0   0   0   0   0   0   0 1000";
        String actual = (String) records.get(9);
        assertTrue(actual.matches(expectedPattern));
    }

    public void testShouldReadFromFileAndLoadMonthlyPacketIntoTable() throws Exception {
        runProfileImporter("monthly.txt");
        // assert
        assertEquals(10, countRecords("MONTHLY"));

        File exportfile = runProfileExporter();
        // assert records
        List records = readData(exportfile);
        assertEquals(10, records.size());

        String expectedPattern = "   10  32  32  12  12  12  31  31  31 258 258 258  32  999";
        String actual = (String) records.get(9);
        assertTrue(actual.matches(expectedPattern));
    }

    public void testShouldImportExportVersionedMonthlyPacketData() throws Exception {
        Version version = version(dataset.getId());

        runVersionProfileImporter("monthly.txt", dataset, version);
        assertEquals(10, countRecords("MONTHLY"));

        File exportfile = runVersionProfileExporter(version);
        List records = readData(exportfile);
        assertEquals(10, records.size());

        String expectedPattern = "   10  32  32  12  12  12  31  31  31 258 258 258  32  999";

        String actual = (String) records.get(9);
        assertTrue(actual.matches(expectedPattern));
    }

    public void testShouldReadFromFileAndLoadWeeklyPacketIntoTable() throws Exception {
        runProfileImporter("weekly.txt");
        assertEquals(13, countRecords("WEEKLY"));

        File exportfile = runProfileExporter();
        List records = readData(exportfile);
        assertEquals(13, records.size());

        String expectedPattern = "    6 167 167 167 167 167 167   0  1000";

        String actual = (String) records.get(5);
        assertTrue(actual.matches(expectedPattern));
    }

    public void testShouldImportExportVersionedWeeklyPacket() throws Exception {
        Version version = version(dataset.getId());
        runVersionProfileImporter("weekly.txt", dataset, version);
        assertEquals(13, countRecords("WEEKLY"));

        File exportfile = runVersionProfileExporter(version);
        List records = readData(exportfile);
        assertEquals(13, records.size());

        String expectedPattern = "    6 167 167 167 167 167 167   0  1000";
        String actual = (String) records.get(5);
        assertTrue(actual.matches(expectedPattern));
    }

    public void testShouldGiveCorrectExportedLinesNumber() throws Exception {
        Version version = version(dataset.getId());
        runVersionProfileImporter("weekly.txt", dataset, version);
        assertEquals(13, countRecords("WEEKLY"));

        TemporalProfileExporter exporter = new TemporalProfileExporter(dataset, "", dbServer,
                new VersionedDataFormatFactory(version, dataset), optimizedBatchSize, null, null, "");
        File exportfile = File.createTempFile("VersionedTemporalProfileExported", ".txt");
        exporter.export(exportfile);
        assertEquals(13, exporter.getExportedLinesCount());

        TemporalProfileExporter exporter2 = new TemporalProfileExporter(dataset, "", dbServer,
                optimizedBatchSize);
        File exportfile2 = File.createTempFile("VersionedTemporalProfileExported", ".txt");
        exporter2.export(exportfile2);
        assertEquals(13, exporter2.getExportedLinesCount());
    }

    private int countRecords(String table) {
        Datasource datasource = dbServer.getEmissionsDatasource();
        return tableReader.count(datasource.getName(), table);
    }

    private void runProfileImporter(String fileName) throws ImporterException {
        String folder = "test/data/temporal-profiles";
        File file = new File(folder, fileName);
        TemporalProfileImporter importer = new TemporalProfileImporter(file.getParentFile(), new String[] { file
                .getName() }, dataset, dbServer, typeMapper);
        importer.run();
    }

    private File runProfileExporter() throws IOException, ExporterException {
        TemporalProfileExporter exporter = new TemporalProfileExporter(dataset, "", dbServer,
                optimizedBatchSize);
        File exportfile = File.createTempFile("VersionedTemporalProfileExported", ".txt");
        exporter.export(exportfile);
        return exportfile;
    }

    private void runVersionProfileImporter(String fileName, Dataset dataset, Version version) throws Exception {
        DbServer localDbServer = dbSetup.getNewPostgresDbServerInstance();
        File file = new File("test/data/temporal-profiles", fileName);

        TemporalProfileImporter tempProImporter = new TemporalProfileImporter(file.getParentFile(), new String[] { file
                .getName() }, dataset, localDbServer, typeMapper, new VersionedDataFormatFactory(version, dataset));
        VersionedImporter importer = new VersionedImporter(tempProImporter, dataset, localDbServer, lastModifiedDate(file
                .getParentFile(), fileName));
        importer.run();
    }

    private File runVersionProfileExporter(Version version) throws IOException, ExporterException {
        TemporalProfileExporter exporter = new TemporalProfileExporter(dataset, "", dbServer,
                new VersionedDataFormatFactory(version, dataset), optimizedBatchSize, null, null, "");
        File exportfile = File.createTempFile("VersionedTemporalProfileExported", ".txt");
        exporter.export(exportfile);
        return exportfile;
    }

    private Version version(int datasetId) {
        Version version = new Version();
        version.setVersion(0);
        version.setDatasetId(datasetId);
        return version;
    }

    private Date lastModifiedDate(File folder, String fileName) {
        return new Date(new File(folder, fileName).lastModified());
    }

    protected boolean isComment(String line) {
        return line.startsWith("/") ||line.startsWith("#");
    }

}
