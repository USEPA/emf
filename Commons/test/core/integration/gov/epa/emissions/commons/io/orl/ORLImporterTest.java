package gov.epa.emissions.commons.io.orl;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.data.SimpleDataset;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.DbUpdate;
import gov.epa.emissions.commons.db.HibernateTestCase;
import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.db.TableReader;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.db.version.Versions;
import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.commons.io.NonVersionedTableFormat;
import gov.epa.emissions.commons.io.TableFormat;
import gov.epa.emissions.commons.io.importer.Importer;
import gov.epa.emissions.commons.io.importer.TemporalResolution;
import gov.epa.emissions.commons.io.importer.VersionedDataFormatFactory;
import gov.epa.emissions.commons.io.importer.VersionedImporter;
import gov.epa.emissions.commons.io.temporal.VersionedTableFormat;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Random;

import org.dbunit.dataset.ITable;

public class ORLImporterTest extends HibernateTestCase {

    private SqlDataTypes sqlDataTypes;

    private Dataset dataset;

    private DbServer dbServer;

    protected void setUp() throws Exception {
        super.setUp();

        dbServer = dbSetup.getDbServer();
        sqlDataTypes = dbServer.getSqlDataTypes();

        dataset = new SimpleDataset();
        dataset.setName("test");
        dataset.setId(Math.abs(new Random().nextInt()));
    }

    protected void doTearDown() throws Exception {
        Datasource datasource = dbServer.getEmissionsDatasource();
        DbUpdate dbUpdate = dbSetup.dbUpdate(datasource);
        
        try {
            dbUpdate.dropTable(datasource.getName(), dataset.getInternalSources()[0].getTable());
        } catch (Exception e) {
            System.out.println("Dataset table dose not exist. Possible reason: Importer didn't import data.");
        }

        dbUpdate.deleteAll(datasource.getName(), "versions");
    }

    public void testShouldImportASmallAndSimplePointFile() throws Exception {
        File folder = new File("test/data/orl/nc");
        ORLPointImporter importer = new ORLPointImporter(folder, new String[] { "small-point-comma.txt" }, dataset,
                dbServer, sqlDataTypes);
        importer.run();

        int rows = countRecords();
        assertEquals(10, rows);
    }

    public void testShouldImportASmallAndSimpleVersionedPointFile() throws Exception {
        DbServer localDbServer = dbSetup.getNewPostgresDbServerInstance();
        Version version = new Version();
        version.setVersion(0);

        File file = new File("test/data/orl/nc", "small-point-comma.txt");
        ORLPointImporter orlImporter = new ORLPointImporter(file.getParentFile(), new String[] { file.getName() },
                dataset, localDbServer, sqlDataTypes, new VersionedDataFormatFactory(version, dataset));
        VersionedImporter importer = new VersionedImporter(orlImporter, dataset, localDbServer, lastModifiedDate(file
                .getParentFile(), file.getName()));
        importer.run();

        int rows = countRecords();
        assertEquals(10, rows);
        assertVersionInfo(dataset.getInternalSources()[0].getTable(), rows);
    }

    public void testShouldImportASmallAndSimpleVersionedPointFileWithExclamationInDoubleQuotes() throws Exception {
        DbServer localDbServer = dbSetup.getNewPostgresDbServerInstance();
        Version version = new Version();
        version.setVersion(0);

        File file = new File("test/data/orl/nc", "point-with-variations-comma.txt");
        ORLPointImporter orlImporter = new ORLPointImporter(file.getParentFile(), new String[] { file.getName() },
                dataset, localDbServer, sqlDataTypes, new VersionedDataFormatFactory(version, dataset));
        VersionedImporter importer = new VersionedImporter(orlImporter, dataset, localDbServer, lastModifiedDate(file
                .getParentFile(), file.getName()));
        importer.run();

        int rows = countRecords();
        assertEquals(4, rows);
        assertVersionInfo(dataset.getInternalSources()[0].getTable(), rows);
    }

    private void assertVersionInfo(String name, int rows) throws Exception {
        verifyVersionCols(name, rows);
        verifyVersionZeroEntryInVersionsTable();
    }

    public void testShouldImportASmallAndSimpleExtendedPointFile() throws Exception {
        File folder = new File("test/data/orl/extended");
        ORLPointImporter importer = new ORLPointImporter(folder, new String[] { "orl-extended-point.txt" }, dataset,
                dbServer, sqlDataTypes);
        importer.run();

        assertEquals(193, countRecords());
    }

    public void testShouldImportASmallAndSimpleNonPointFile() throws Exception {
        File folder = new File("test/data/orl/nc");
        ORLNonPointImporter importer = new ORLNonPointImporter(folder, new String[] { "small-nonpoint-comma.txt" },
                dataset, dbServer, sqlDataTypes);
        importer.run();

        assertEquals(6, countRecords());

        Datasource datasource = dbServer.getEmissionsDatasource();
        // assert
        TableReader tableReader = tableReader(datasource);

        String table = dataset.getInternalSources()[0].getTable();
        assertTrue("Table '" + table + "' should have been created", tableReader.exists(datasource.getName(), table));

        int rows = tableReader.count(datasource.getName(), table);
        assertEquals(6, rows);
    }

    public void testShouldImportASmallAndSimpleVersionedNonPointFile() throws Exception {
        DbServer localDbServer = dbSetup.getNewPostgresDbServerInstance();
        Version version = new Version();
        version.setVersion(0);
        File folder = new File("test/data/orl/nc");

        ORLNonPointImporter orlImporter = new ORLNonPointImporter(folder, new String[] { "small-nonpoint-comma.txt" },
                dataset, localDbServer, sqlDataTypes, new VersionedDataFormatFactory(version, dataset));
        VersionedImporter importer = new VersionedImporter(orlImporter, dataset, localDbServer, lastModifiedDate(
                folder, "small-nonpoint.txt"));
        importer.run();

        assertEquals(6, countRecords());

        // assert
        Datasource datasource = dbServer.getEmissionsDatasource();
        TableReader tableReader = tableReader(datasource);

        String table = dataset.getInternalSources()[0].getTable();
        assertTrue("Table '" + table + "' should have been created", tableReader.exists(datasource.getName(), table));

        int rows = tableReader.count(datasource.getName(), table);
        assertEquals(6, rows);
        assertVersionInfo(table, rows);
    }

    private void verifyVersionCols(String table, int rows) throws Exception {
        Datasource datasource = dbServer.getEmissionsDatasource();
        TableReader tableReader = tableReader(datasource);

        ITable tableRef = tableReader.table(datasource.getName(), table);
        for (int i = 0; i < rows; i++) {
            Object recordId = tableRef.getValue(i, "Record_Id");
            assertEquals((i + 1) + "", recordId.toString());

            Object version = tableRef.getValue(i, "Version");
            assertEquals("0", version.toString());

            Object deleteVersions = tableRef.getValue(i, "Delete_Versions");
            assertEquals("", deleteVersions);
        }
    }

    private void verifyVersionZeroEntryInVersionsTable() throws Exception {
        Versions versions = new Versions();
        Version[] onRoadVersions = versions.get(dataset.getId(), session);
        assertEquals(1, onRoadVersions.length);

        Version versionZero = onRoadVersions[0];
        assertEquals(0, versionZero.getVersion());
        assertEquals(dataset.getId(), versionZero.getDatasetId());
        assertEquals("", versionZero.getPath());
        assertTrue("Version Zero should be zero upon import", versionZero.isFinalVersion());
    }

    public void testShouldImportASmallAndSimpleExtendedNonPointFile() throws Exception {
        File folder = new File("test/data/orl/extended");
        ORLNonPointImporter importer = new ORLNonPointImporter(folder, new String[] { "orl-extended-nonpoint.txt" },
                dataset, dbServer, sqlDataTypes);
        importer.run();
        assertEquals(194, countRecords());
    }

    public void testShouldImportNonPointFileWithVaryingCols() throws Exception {
        File folder = new File("test/data/orl/nc");
        ORLNonPointImporter importer = new ORLNonPointImporter(folder,
                new String[] { "varying-cols-nonpoint-comma.txt" }, dataset, dbServer, sqlDataTypes);
        importer.run();

        Datasource datasource = dbServer.getEmissionsDatasource();
        TableReader tableReader = tableReader(datasource);

        int rows = tableReader.count(datasource.getName(), dataset.getInternalSources()[0].getTable());
        assertEquals(6, rows);
        ITable table = tableReader.table(datasource.getName(), dataset.getInternalSources()[0].getTable());
        assertNull(table.getValue(0, "CEFF"));
        assertNull(table.getValue(0, "REFF"));
        assertNull(table.getValue(0, "RPEN"));

        assertNull(table.getValue(1, "CEFF"));
        assertNull(table.getValue(1, "REFF"));
        assertEquals(1.0, Double.valueOf("" + table.getValue(1, "RPEN")).doubleValue(), 0.000001);

        assertNull(table.getValue(2, "CEFF"));
        assertNull(table.getValue(2, "REFF"));
        assertNull(table.getValue(2, "RPEN"));
    }

    public void testShouldLoadInternalSourceIntoDatasetOnImport() throws Exception {
        File folder = new File("test/data/orl/nc");
        ORLNonPointImporter importer = new ORLNonPointImporter(folder, new String[] { "small-nonpoint-comma.txt" },
                dataset, dbServer, sqlDataTypes);
        importer.run();

        InternalSource[] sources = dataset.getInternalSources();
        assertEquals(1, sources.length);
        InternalSource source = sources[0];
        assertEquals(dataset.getInternalSources()[0].getTable(), source.getTable());
        assertEquals("ORL NonPoint", source.getType());

        TableFormat tableFormat = new NonVersionedTableFormat(new ORLNonPointFileFormat(sqlDataTypes), sqlDataTypes);
        String[] actualCols = source.getCols();
        String[] expectedCols = colNames(tableFormat.cols());
        assertEquals(expectedCols.length, actualCols.length);
        for (int i = 0; i < actualCols.length; i++) {
            assertEquals(expectedCols[i], actualCols[i]);
        }

        File file = new File(folder, "small-nonpoint-comma.txt");
        assertEquals(file.getAbsolutePath(), source.getSource());
        assertEquals(file.length(), source.getSourceSize());
    }

    public void testShouldLoadVersionedInternalSourceIntoDatasetOnImport() throws Exception {
        File folder = new File("test/data/orl/nc");
        Version version = new Version();
        version.setVersion(0);

        ORLNonPointImporter importer = new ORLNonPointImporter(folder,
                new String[] { "NonPoint_WithComments-comma.txt" }, dataset, dbServer, sqlDataTypes,
                new VersionedDataFormatFactory(version, dataset));

        importer.run();

        InternalSource[] sources = dataset.getInternalSources();
        assertEquals(1, sources.length);
        InternalSource source = sources[0];
        assertEquals(dataset.getInternalSources()[0].getTable(), source.getTable());
        assertEquals("ORL NonPoint", source.getType());

        TableFormat tableFormat = new VersionedTableFormat(new ORLNonPointFileFormat(sqlDataTypes), sqlDataTypes);
        String[] actualCols = source.getCols();
        String[] expectedCols = colNames(tableFormat.cols());
        assertEquals(expectedCols.length, actualCols.length);
        for (int i = 0; i < actualCols.length; i++) {
            assertEquals(expectedCols[i], actualCols[i]);
        }

        File file = new File(folder, "NonPoint_WithComments-comma.txt");
        assertEquals(file.getAbsolutePath(), source.getSource());
        assertEquals(file.length(), source.getSourceSize());
    }

    private String[] colNames(Column[] cols) {
        List names = new ArrayList();
        for (int i = 0; i < cols.length; i++)
            names.add(cols[i].name());

        return (String[]) names.toArray(new String[0]);
    }

    public void testShouldImportASmallAndSimpleNonRoadFile() throws Exception {
        File folder = new File("test/data/orl/nc");
        ORLNonRoadImporter importer = new ORLNonRoadImporter(folder, new String[] { "small-nonroad-comma.txt" },
                dataset, dbServer, sqlDataTypes);
        importer.run();

        int rows = countRecords();
        assertEquals(16, rows);
    }

    public void testShouldImportASmallAndSimpleVersionedNonRoadFile() throws Exception {
        DbServer localDbServer = dbSetup.getNewPostgresDbServerInstance();
        Version version = new Version();
        version.setVersion(0);

        File file = new File("test/data/orl/nc", "small-nonroad-comma.txt");
        ORLNonRoadImporter orlImporter = new ORLNonRoadImporter(file.getParentFile(), new String[] { file.getName() },
                dataset, localDbServer, sqlDataTypes, new VersionedDataFormatFactory(version, dataset));
        VersionedImporter importer = new VersionedImporter(orlImporter, dataset, localDbServer, lastModifiedDate(file
                .getParentFile(), "small-nonroad.txt"));
        importer.run();

        int rows = countRecords();
        assertEquals(16, rows);
        assertVersionInfo(dataset.getInternalSources()[0].getTable(), rows);
    }

    public void testShouldImportASmallAndSimpleExtendedNonRoadFile() throws Exception {
        File folder = new File("test/data/orl/extended");
        ORLNonRoadImporter importer = new ORLNonRoadImporter(folder, new String[] { "orl-extended-nonroad.txt" },
                dataset, dbServer, sqlDataTypes);
        importer.run();

        assertEquals(194, countRecords());
    }

    public void testShouldImportASmallAndSimpleOnRoadFile() throws Exception {
        File folder = new File("test/data/orl/nc");
        Importer importer = new ORLOnRoadImporter(folder, new String[] { "small-onroad-comma.txt" }, dataset, dbServer,
                sqlDataTypes);
        importer.run();

        int rows = countRecords();
        assertEquals(18, rows);
    }

    public void testShouldImportASmallAndSimpleVersionedOnRoadFile() throws Exception {
        DbServer localDbServer = dbSetup.getNewPostgresDbServerInstance();
        Version version = new Version();
        version.setVersion(0);

        File file = new File("test/data/orl/nc", "small-onroad-comma.txt");
        Importer orlImporter = new ORLOnRoadImporter(file.getParentFile(), new String[] { file.getName() }, dataset,
                localDbServer, sqlDataTypes, new VersionedDataFormatFactory(version, dataset));
        VersionedImporter importer = new VersionedImporter(orlImporter, dataset, localDbServer, lastModifiedDate(file
                .getParentFile(), file.getName()));
        importer.run();

        int rows = countRecords();
        assertEquals(18, rows);
        assertVersionInfo(dataset.getInternalSources()[0].getTable(), rows);
    }

    // BUG: Fix the Country lookup bug
    public void FIXME_testShouldLoadCountryRegionYearIntoDatasetOnImport() throws Exception {
        File folder = new File("test/data/orl/nc");
        Importer importer = new ORLOnRoadImporter(folder, new String[] { "small-onroad-comma.txt" }, dataset, dbServer,
                sqlDataTypes);
        importer.run();

        // assert
        assertEquals("PERU", dataset.getCountry().getName());
        assertEquals(1995, dataset.getYear());
    }

    public void testShouldLoadStartStopDateTimeIntoDatasetOnImport() throws Exception {
        File folder = new File("test/data/orl/nc");
        Importer importer = new ORLOnRoadImporter(folder, new String[] { "small-onroad-comma.txt" }, dataset, dbServer,
                sqlDataTypes);
        importer.run();

        // assert
        Date start = dataset.getStartDateTime();
        Date expectedStart = new GregorianCalendar(1995, Calendar.JANUARY, 1).getTime();
        assertEquals(expectedStart, start);

        Date end = dataset.getStopDateTime();
        GregorianCalendar endCal = new GregorianCalendar(1995, Calendar.DECEMBER, 31, 23, 59, 59);
        endCal.set(Calendar.MILLISECOND, 999);
        assertEquals(endCal.getTime(), end);
    }

    public void testShouldImportORLOnRoad() throws Exception {
        File folder = new File("test/data/orl/nc");
        Importer importer = new ORLOnRoadImporter(folder, new String[] { "small-onroad-comma.txt" }, dataset, dbServer,
                sqlDataTypes);
        importer.run();

        // assert
        assertEquals(TemporalResolution.ANNUAL.getName(), dataset.getTemporalResolution());
        assertEquals("short tons/year", dataset.getUnits());
    }

    public void testShouldFailOnWrongYearInHeader() throws Exception {
        try {
            File folder = new File("test/data/orl/nc");
            Importer importer = new ORLOnRoadImporter(folder, new String[] { "small-onroad-with-incorrect-year.txt" },
                    dataset, dbServer, sqlDataTypes);
            importer.run();
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(e.getMessage().contains("Invalid ORL Year: 2300 ( >= 2200 )."));
        }
    }
    
    public void testShouldFailOnWrongORLTagFormatInHeader() throws Exception {
        try {
            File folder = new File("test/data/orl/nc");
            Importer importer = new ORLOnRoadImporter(folder, new String[] { "small-onroad-with-incorrect-tag-format.txt" },
                    dataset, dbServer, sqlDataTypes);
            importer.run();
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("The tag - 'ORL' in right format (ORL || ORL POINT, etc.) is mandatory."));
        }
    }

    public void testShouldSetFullLineCommentsAndDescCommentsAsDatasetDescriptionOnImport() throws Exception {
        File folder = new File("test/data/orl/nc");
        Importer importer = new ORLOnRoadImporter(folder, new String[] { "small-onroad-comma.txt" }, dataset, dbServer,
                sqlDataTypes);
        importer.run();

        // assert
        String expected = "#ORL\n#TYPE    Mobile source toxics inventory, on-road mobile source only\n"
                + "#COUNTRY PERU\n#YEAR    1995\n#DESC Created from file 99OR-MOD.TXT provided by M. Strum in "
                + "November 2002.\n"
                + "#DESC North Carolina data extracted from original file using UNIX grep command.\n"
                + "#DESC    paste commands.\n" + "#comment 1\n#comment 2\n#comment 3\n#comment 4\n";
        assertEquals(expected, dataset.getDescription());
    }

    public void testShouldImportExtendedOnRoad() throws Exception {
        File folder = new File("test/data/orl/extended");
        try {
            Importer importer = new ORLOnRoadImporter(folder, new String[] { "orl-extended-onroad.txt" }, dataset,
                    dbServer, sqlDataTypes);
            importer.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
        int rows = countRecords();
        assertEquals(12, rows);
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
