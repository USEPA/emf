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
import gov.epa.emissions.commons.io.nif.onroad.NIFOnRoadImporter;
import gov.epa.emissions.commons.io.nif.onroad.NIFOnRoadSummary;

import java.io.File;
import java.util.Random;

public class NIFOnRoadSummaryTest extends PersistenceTestCase {

    private Datasource emissionDatasource;

    private SqlDataTypes sqlDataTypes;

    private Dataset dataset;

    private String tableEM;

    private String tablePE;

    private String tableTR;

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

    public void testShouldImportASmallAndSimplePointFiles() throws Exception {
        File folder = new File("test/data/nif/onroad");
        String[] files = {"ct_em.txt", "ct_pe.txt", "ct_tr.txt"};
        NIFOnRoadImporter importer = new NIFOnRoadImporter(folder, files, dataset, dbServer(), sqlDataTypes);
        importer.run();
        SummaryTable summary = new NIFOnRoadSummary(emissionDatasource, referenceDatasource, dataset);
        summary.createSummary();
        
        InternalSource[] sources = dataset.getInternalSources();
        String[] tables = new String[sources.length];
        
        for(int i = 0; i < tables.length; i++) {
            tables[i] = sources[i].getTable();
            
            if (tables[i].contains("_em"))
            tableEM = tables[i];
            
            if (tables[i].contains("_pe"))
            tablePE = tables[i];
            
            if (tables[i].contains("_tr"))
                tableTR = tables[i];
        }
        
        assertEquals(10, countRecords(tableEM));
        assertEquals(10, countRecords(tablePE));
        assertEquals(8, countRecords(tableTR));
        assertEquals(2, countRecords("test_summary"));
    }

    private int countRecords(String tableName) {
        TableReader tableReader = tableReader(emissionDatasource);
        return tableReader.count(emissionDatasource.getName(), tableName);
    }

    protected void doTearDown() throws Exception {
        DbUpdate dbUpdate = dbSetup.dbUpdate(emissionDatasource);
        dbUpdate.dropTable(emissionDatasource.getName(), tableEM);
        dbUpdate.dropTable(emissionDatasource.getName(), tablePE);
        dbUpdate.dropTable(emissionDatasource.getName(), tableTR);
        dbUpdate.dropTable(emissionDatasource.getName(), "test_summary");
    }

}
