package gov.epa.emissions.commons.io.orl;

import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.HibernateTestCase;
import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.db.TableReader;
import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.SimpleDataset;

import java.io.File;
import java.util.Random;

public class OrlPointPrecisionTest extends HibernateTestCase {

    private SqlDataTypes sqlDataTypes;

    private Dataset dataset;

    private DbServer dbServer;

    protected void setUp() throws Exception {
        super.setUp();

        dbServer = dbSetup.getDbServer();
        sqlDataTypes = dbServer.getSqlDataTypes();

        dataset = new SimpleDataset();
        int id = Math.abs(new Random().nextInt());
        dataset.setName("test_" + id);
        dataset.setId(id);
    }

    protected void doTearDown() throws Exception {// no op
    }

    public void testShouldImportASmallAndSimplePointFile() throws Exception {
        File folder = new File("test/data/orl/nc");
        ORLPointImporter importer = new ORLPointImporter(folder, new String[] { "point-precisions.txt" }, dataset,
                dbServer, sqlDataTypes);
        importer.run();

        int rows = countRecords();
        assertEquals(36, rows);
    }

    private int countRecords() {
        Datasource datasource = dbServer.getEmissionsDatasource();
        TableReader tableReader = tableReader(datasource);
        return tableReader.count(datasource.getName(), dataset.getInternalSources()[0].getTable());
    }

}
