package gov.epa.emissions.commons.io.importer;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.db.DataModifier;
import gov.epa.emissions.commons.db.DatabaseSetup;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.DbUpdate;
import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.db.TableDefinition;
import gov.epa.emissions.commons.db.TableReader;
import gov.epa.emissions.commons.io.TableFormat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import junit.framework.TestCase;

public abstract class PersistenceTestCase extends TestCase {

    protected DatabaseSetup dbSetup;

    protected File fieldDefsFile;

    protected File referenceFilesDir;

    public PersistenceTestCase(String name) {
        super(name);
    }

    public PersistenceTestCase() {
        //Nothing
    }

    protected void setUp() throws Exception {
        String folder = "test";
        File conf = new File(folder, configFilename());

        if (!conf.exists() || !conf.isFile()) {
            String error = "File: " + conf + " does not exist. Please copy either of the two TEMPLATE files "
                    + "(from " + folder + "), name it " + conf.getName() + ", configure " + "it as needed, and rerun.";
            throw new RuntimeException(error);
        }

        Properties properties = new Properties();
        properties.load(new FileInputStream(conf));

        properties.put("DATASET_NIF_FIELD_DEFS", "config/field_defs.dat");
        properties.put("REFERENCE_FILE_BASE_DIR", "config/refDbFiles");

        dbSetup = new DatabaseSetup(properties);
        fieldDefsFile = new File("config/field_defs.dat");
        referenceFilesDir = new File("config/refDbFiles");
    }

    private String configFilename() {
        String db = System.getProperty("Database");
        if (db != null && db.equalsIgnoreCase("MYSQL"))
            return "mysql.conf";

        return "postgres.conf";
    }

    protected final void tearDown() throws Exception {
        doTearDown();
        dropData("versions", dbServer().getEmissionsDatasource());
        dbSetup.tearDown();
    }

    protected abstract void doTearDown() throws Exception;// subclasses must implement this method

    protected Datasource emissions() {
        return dbServer().getEmissionsDatasource();
    }

    protected DbServer dbServer() {
        return dbSetup.getDbServer();
    }

    protected SqlDataTypes dataTypes() {
        return dbServer().getSqlDataTypes();
    }

    protected void createTable(String table, Datasource datasource, TableFormat format) throws SQLException {
        TableDefinition tableDefinition = datasource.tableDefinition();
        tableDefinition.createTable(table, format.cols());
    }

    protected void dropTable(String table, Datasource datasource) throws Exception, SQLException {
        DbUpdate dbUpdate = dbSetup.dbUpdate(datasource);
        dbUpdate.dropTable(datasource.getName(), table);
    }

    protected void dropData(String table, Datasource datasource) throws SQLException {
        DataModifier modifier = datasource.dataModifier();
        modifier.dropAllData(table);
    }

    protected TableReader tableReader(Datasource datasource) {
        return dbSetup.tableReader(datasource);
    }

    public int countRecords(DbServer dbServer, String tableName) {
        Datasource datasource = dbServer.getEmissionsDatasource();
        TableReader tableReader = tableReader(datasource);
        return tableReader.count(datasource.getName(), tableName);
    }

    protected List<String> readData(File file) throws IOException {
        List<String> data = new ArrayList<String>();
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader(file));
            for (String line = bufferedReader.readLine(); line != null; line = bufferedReader.readLine()) {
                if (isNotEmpty(line) && !isComment(line))
                    data.add(line);
            }
        } finally {
            if (bufferedReader != null)
                bufferedReader.close();
        }
        return data;
    }

    protected boolean isNotEmpty(String line) {
        return !line.trim().isEmpty();
    }

    protected boolean isComment(String line) {
        return line.startsWith("#");
    }

    protected void dropDatasetDataTable(Dataset dataset) throws Exception {
        Datasource datasource = dbServer().getEmissionsDatasource();
        DbUpdate dbUpdate = dbSetup.dbUpdate(datasource);
        dbUpdate.dropTable(datasource.getName(), dataset.getInternalSources()[0].getTable());
    }
}
