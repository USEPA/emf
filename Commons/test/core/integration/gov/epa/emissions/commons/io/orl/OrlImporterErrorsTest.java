package gov.epa.emissions.commons.io.orl;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.SimpleDataset;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.db.TableReader;
import gov.epa.emissions.commons.io.importer.ImporterException;
import gov.epa.emissions.commons.io.importer.PersistenceTestCase;

import java.io.File;
import java.util.Random;

public class OrlImporterErrorsTest extends PersistenceTestCase {

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

    public void testShouldDropTableOnEncounteringMissingTokensInData() throws Exception {
        try {
            File folder = new File("test/data/orl/nc");
            ORLPointImporter importer = new ORLPointImporter(folder, new String[] { "BAD-point.txt" }, dataset,
                    dbServer, sqlDataTypes);
            importer.run();
        } catch (ImporterException e) {
            Datasource datasource = dbServer.getEmissionsDatasource();
            TableReader tableReader = tableReader(datasource);
            assertFalse("should have enctountered an error(missing cols) on record 5, and dropped the table",
                    tableReader.exists(datasource.getName(), dataset.getInternalSources()[0].getTable()));
            return;
        }

        fail("should have encountered an error(missing cols) on record 5, and dropped the table");
    }

    public void testShouldDropTableOnEncounteringMissingORLTagInHeader() throws Exception {
        try {
            File folder = new File("test/data/orl/nc");
            ORLPointImporter importer = new ORLPointImporter(folder,
                    new String[] { "MISSING-ORL-TAG-IN-HEADER-point.txt" }, dataset, dbServer, sqlDataTypes);
            importer.run();
        } catch (ImporterException e) {
            Datasource datasource = dbServer.getEmissionsDatasource();
            TableReader tableReader = tableReader(datasource);
            assertFalse("should have encountered an error due to missing 'ORL' tag, and dropped the table", tableReader
                    .exists(datasource.getName(), dataset.getInternalSources()[0].getTable()));
            return;
        }

        fail("should have encountered an error due to missing 'ORL' tag, and dropped the table");
    }

    public void testShouldDropTableOnEncounteringMissingCountryTagInHeader() throws Exception {
        try {
            File folder = new File("test/data/orl/nc");
            ORLPointImporter importer = new ORLPointImporter(folder,
                    new String[] { "MISSING-COUNTRY-TAG-IN-HEADER-point.txt" }, dataset, dbServer, sqlDataTypes);
            importer.run();
        } catch (ImporterException e) {
            Datasource datasource = dbServer.getEmissionsDatasource();
            TableReader tableReader = tableReader(datasource);
            assertFalse("should have encountered an error due to missing 'COUNTRY' tag, and dropped the table",
                    tableReader.exists(datasource.getName(), dataset.getInternalSources()[0].getTable()));
            return;
        }

        fail("should have encountered an error due to missing 'COUNTRY' tag, and dropped the table");
    }

    public void testShouldDropTableOnEncounteringEmptyCountryTagInHeader() throws Exception {
        try {
            File folder = new File("test/data/orl/nc");
            ORLPointImporter importer = new ORLPointImporter(folder,
                    new String[] { "MISSING-COUNTRY-TAG-IN-HEADER-point.txt" }, dataset, dbServer, sqlDataTypes);
            importer.run();
        } catch (ImporterException e) {
            Datasource datasource = dbServer.getEmissionsDatasource();
            TableReader tableReader = tableReader(datasource);
            assertFalse("should have encountered an error due to empty 'COUNTRY' tag, and dropped the table",
                    tableReader.exists(datasource.getName(), dataset.getInternalSources()[0].getTable()));
            return;
        }

        fail("should have encountered an error due to empty 'COUNTRY' tag, and dropped the table");
    }

    public void testShouldDropTableOnEncounteringMissingYearTagInHeader() throws Exception {
        try {
            File folder = new File("test/data/orl/nc");
            ORLPointImporter importer = new ORLPointImporter(folder,
                    new String[] { "MISSING-YEAR-TAG-IN-HEADER-point.txt" }, dataset, dbServer, sqlDataTypes);

            importer.run();
        } catch (ImporterException e) {
            Datasource datasource = dbServer.getEmissionsDatasource();
            TableReader tableReader = tableReader(datasource);
            assertFalse("should have encountered an error due to missing 'YEAR' tag, and dropped the table",
                    tableReader.exists(datasource.getName(), dataset.getInternalSources()[0].getTable()));
            return;
        }

        fail("should have encountered an error due to missing 'YEAR' tag, and dropped the table");
    }

    public void testShouldDropTableOnEncounteringEmptyYearTagInHeader() throws Exception {
        try {
            File folder = new File("test/data/orl/nc");
            ORLPointImporter importer = new ORLPointImporter(folder,
                    new String[] { "MISSING-YEAR-TAG-IN-HEADER-point.txt" }, dataset, dbServer, sqlDataTypes);
            importer.run();
        } catch (ImporterException e) {
            Datasource datasource = dbServer.getEmissionsDatasource();
            TableReader tableReader = tableReader(datasource);
            assertFalse("should have encountered an error due to empty 'YEAR' tag, and dropped the table", tableReader
                    .exists(datasource.getName(), dataset.getInternalSources()[0].getTable()));
            return;
        }

        fail("should have encountered an error due to empty 'YEAR' tag, and dropped the table");
    }

    protected void doTearDown() throws Exception {// no op
    }

}
