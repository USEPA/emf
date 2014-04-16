package gov.epa.emissions.commons.io.nif;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.data.SimpleDataset;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.DbUpdate;
import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.db.TableReader;
import gov.epa.emissions.commons.io.importer.PersistenceTestCase;
import gov.epa.emissions.commons.io.importer.SummaryTable;
import gov.epa.emissions.commons.io.nif.nonpointNonroad.NIFNonPointImporter;
import gov.epa.emissions.commons.io.nif.nonpointNonroad.NIFNonpointNonRoadSummary;

import java.io.File;
import java.util.Random;

public class NIFNonPointSummaryTest extends PersistenceTestCase {

    private Datasource emissionDatasource;

    private SqlDataTypes sqlDataTypes;

    private Dataset dataset;

    private String tableCE;

    private String tableEM;

    private String tableEP;

    private String tablePE;
    
    private Datasource referenceDatasource;

    protected void setUp() throws Exception {
        super.setUp();

        DbServer dbServer = dbSetup.getDbServer();
        sqlDataTypes = dbServer.getSqlDataTypes();
        emissionDatasource = dbServer.getEmissionsDatasource();
        referenceDatasource = dbServer.getReferenceDatasource();

        dataset = new SimpleDataset();
        dataset.setName("test");
        dataset.setId(Math.abs(new Random().nextInt()));
    }

    public void testShouldImportAAllNonPointFilesAndCreateSummary() throws Exception {
        try {
            File folder = new File("test/data/nif/nonpoint");
            String[] files = {"ky_ce.txt", "ky_em.txt", "ky_ep.txt", "ky_pe.txt"};
            NIFNonPointImporter importer = new NIFNonPointImporter(folder, files, 
                    dataset, dbServer(), sqlDataTypes);
            SummaryTable summary = new NIFNonpointNonRoadSummary(emissionDatasource, referenceDatasource, dataset);
            importer.run();
            summary.createSummary();
            
            InternalSource[] sources = dataset.getInternalSources();
            String[] tables = new String[sources.length];
            
            for(int i = 0; i < tables.length; i++) {
                tables[i] = sources[i].getTable();
                
                if (tables[i].contains("_ce"))
                tableCE = tables[i];
                
                if (tables[i].contains("_em"))
                tableEM = tables[i];
                
                if (tables[i].contains("_ep"))
                tableEP = tables[i];
                
                if (tables[i].contains("_pe"))
                tablePE = tables[i];
            }

            assertEquals(1, countRecords(tableCE));
            assertEquals(21, countRecords(tableEM));
            assertEquals(4, countRecords(tableEP));
            assertEquals(4, countRecords(tablePE));
            assertEquals(3, countRecords("test_summary"));
        } finally {
            dropTables();
        }
    }

    private int countRecords(String tableName) {
        TableReader tableReader = tableReader(emissionDatasource);
        return tableReader.count(emissionDatasource.getName(), tableName);
    }

    protected void dropTables() throws Exception {
        DbUpdate dbUpdate = dbSetup.dbUpdate(emissionDatasource);
        dbUpdate.dropTable(emissionDatasource.getName(), tableCE);
        dbUpdate.dropTable(emissionDatasource.getName(), tableEM);
        dbUpdate.dropTable(emissionDatasource.getName(), tableEP);
        dbUpdate.dropTable(emissionDatasource.getName(), tablePE);
        dbUpdate.dropTable(emissionDatasource.getName(), "test_summary");
    }
    
    protected void doTearDown() throws Exception {
        //
    }
}
