package gov.epa.emissions.commons.io.nif;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.data.SimpleDataset;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.DbUpdate;
import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.db.TableReader;
import gov.epa.emissions.commons.io.importer.Importer;
import gov.epa.emissions.commons.io.importer.ImporterException;
import gov.epa.emissions.commons.io.importer.PersistenceTestCase;
import gov.epa.emissions.commons.io.nif.nonpointNonroad.NIFNonPointImporter;
import gov.epa.emissions.commons.io.nif.nonpointNonroad.NIFNonPointTableImporter;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Random;

public class NIFNonPointTableImporterTest extends PersistenceTestCase {

    private Datasource datasource;

    private SqlDataTypes sqlDataTypes;

    private Dataset dataset;

    private String tableCE;

    private String tableEM;

    private String tableEP;

    private String tablePE;

    protected void setUp() throws Exception {
        super.setUp();

        DbServer dbServer = dbSetup.getDbServer();
        sqlDataTypes = dbServer.getSqlDataTypes();
        datasource = dbServer.getEmissionsDatasource();

        dataset = new SimpleDataset();
        dataset.setName("test");
        dataset.setId(Math.abs(new Random().nextInt()));
    }

    public void testShouldImportAAllNonPointFiles() throws Exception {
        // first import the files
        try {
            File folder = new File("test/data/nif/nonpoint");
            String[] files = {"ky_ce.txt", "ky_em.txt", "ky_ep.txt", "ky_pe.txt"};
            Importer importer = new NIFNonPointImporter(folder, files, dataset, dbServer(), sqlDataTypes);
            importer.run();
            
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
            
            Importer tableImporter = new NIFNonPointTableImporter(tables, dataset, dbServer(), sqlDataTypes);
            tableImporter.run();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd HHmm");
            assertEquals("20020101 0000", dateFormat.format(dataset.getStartDateTime()));
            assertEquals("20021231 2359", dateFormat.format(dataset.getStopDateTime()));
            assertEquals("TON", dataset.getUnits());
        } catch (RuntimeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }  finally {
            dropTables();
        }
    }

    public void testShouldCheckForRequiredTables() throws Exception {
        try {
            File folder = new File("test/data/nif/nonpoint");
            String[] files = {"ky_ce.txt", "ky_em.txt", "ky_ep.txt", "ky_pe.txt"};
            Importer importer = new NIFNonPointImporter(folder, files, dataset, dbServer(), sqlDataTypes);
            importer.run();

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
            
            String[] tables2 = { tableCE, tableEP };
            new NIFNonPointTableImporter(tables2, dataset, dbServer(), sqlDataTypes);
        } catch (ImporterException e) {
            assertTrue(e.getMessage().startsWith("NIF nonpoint import requires following types "));
            return;
        } finally {
            dropTables();
        }

        fail("Should have failed as not all tables are specified");
    }

    private int countRecords(String tableName) {
        TableReader tableReader = tableReader(datasource);
        return tableReader.count(datasource.getName(), tableName);
    }
    
    protected void dropTables() throws Exception {
        DbUpdate dbUpdate = dbSetup.dbUpdate(datasource);
        dbUpdate.dropTable(datasource.getName(), tableCE);
        dbUpdate.dropTable(datasource.getName(), tableEM);
        dbUpdate.dropTable(datasource.getName(), tableEP);
        dbUpdate.dropTable(datasource.getName(), tablePE);
    }

    protected void doTearDown() throws Exception {
//
    }
}
