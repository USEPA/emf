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
import gov.epa.emissions.commons.io.nif.point.NIFPointImporter;
import gov.epa.emissions.commons.io.nif.point.NIFPointTableImporter;

import java.io.File;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Random;

public class NIFPointTableImporterTest extends PersistenceTestCase {

    private Datasource datasource;

    private SqlDataTypes sqlDataTypes;

    private Dataset dataset;

    private String tableCE;

    private String tableEM;

    private String tableEP;

    private String tableER;

    private String tableEU;

    private String tablePE;

    private String tableSI;

    protected void setUp() throws Exception {
        super.setUp();

        DbServer dbServer = dbSetup.getDbServer();
        sqlDataTypes = dbServer.getSqlDataTypes();
        datasource = dbServer.getEmissionsDatasource();

        dataset = new SimpleDataset();
        dataset.setName("test");
        dataset.setId(Math.abs(new Random().nextInt()));
    }

    public void testShouldImportASmallAndSimplePointFiles() throws Exception {
        try {
            File folder = new File("test/data/nif/point");
            String[] files = {"ky_ce.txt", "ky_em.txt", "ky_ep.txt",
                    "ky_er.txt", "ky_eu.txt", "ky_pe.txt", "ky_si.txt"};
            NIFPointImporter importer = new NIFPointImporter(folder, files, dataset, dbServer(), sqlDataTypes);
            importer.run();
            
            InternalSource[] sources = dataset.getInternalSources();
            String[] tables = new String[sources.length];
            
            for(int i = 0; i < tables.length; i++) {
                tables[i] = sources[i].getTable();
                
                if (tables[i].contains("_em"))
                tableEM = tables[i];
                
                if (tables[i].contains("_ep"))
                tableEP = tables[i];
                
                if (tables[i].contains("_pe"))
                tablePE = tables[i];
                
                if (tables[i].contains("_ce"))
                tableCE = tables[i];
                
                if (tables[i].contains("_er"))
                tableER = tables[i];
                
                if (tables[i].contains("_eu"))
                tableEU = tables[i];
                
                if (tables[i].contains("_si"))
                tableSI = tables[i];
            }
            
            assertEquals(92, countRecords(tableCE));
            assertEquals(143, countRecords(tableEM));
            assertEquals(26, countRecords(tableEP));
            assertEquals(15, countRecords(tableER));
            assertEquals(15, countRecords(tableEU));
            assertEquals(26, countRecords(tablePE));
            assertEquals(1, countRecords(tableSI));
            
            Importer tableImporter = new NIFPointTableImporter(tables, dataset, dbServer(), sqlDataTypes);
            tableImporter.run();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd HHmm");
            assertEquals("20020101 0000", dateFormat.format(dataset.getStartDateTime()));
            assertEquals("20021231 0000", dateFormat.format(dataset.getStopDateTime()));
            assertEquals("TON", dataset.getUnits());
        } finally {
            dropTables();
        }
    }

    public void testShouldCheckForReuiredInternalSources() throws Exception {
        File folder = new File("test/data/nif/point");
        String[] files = {"ky_ce.txt", "ky_em.txt", "ky_ep.txt",
                "ky_er.txt", "ky_eu.txt", "ky_pe.txt", "ky_si.txt"};
        NIFPointImporter importer = new NIFPointImporter(folder, files, dataset, dbServer(), sqlDataTypes);
        importer.run();
        
        InternalSource[] sources = dataset.getInternalSources();
        String[] tables = new String[sources.length];
        
        for(int i = 0; i < tables.length; i++) {
            tables[i] = sources[i].getTable();
            
            if (tables[i].contains("_em"))
            tableEM = tables[i];
            
            if (tables[i].contains("_ep"))
            tableEP = tables[i];
            
            if (tables[i].contains("_pe"))
            tablePE = tables[i];
            
            if (tables[i].contains("_ce"))
            tableCE = tables[i];
            
            if (tables[i].contains("_er"))
            tableER = tables[i];
            
            if (tables[i].contains("_eu"))
            tableEU = tables[i];
            
            if (tables[i].contains("_si"))
            tableSI = tables[i];
        }
        
        assertEquals(92, countRecords(tableCE));
        assertEquals(143, countRecords(tableEM));
        assertEquals(26, countRecords(tableEP));
        assertEquals(15, countRecords(tableER));
        assertEquals(15, countRecords(tableEU));
        assertEquals(26, countRecords(tablePE));
        assertEquals(1, countRecords(tableSI));
        
        String[] tables2 = { tableCE,tableEP};
        
        try {
            new NIFPointTableImporter(tables2, dataset, dbServer(), sqlDataTypes);
        } catch (ImporterException e) {
            assertTrue(e.getMessage().startsWith("NIF point import requires following types"));
            return;
        }finally{
            dropTables();
        }

        fail("Should have failed as required types are unspecified");
    }

    private int countRecords(String tableName) {
        TableReader tableReader = tableReader(datasource);
        return tableReader.count(datasource.getName(), tableName);
    }

    protected void doTearDown() throws Exception {// no op
    }

    private void dropTables() throws Exception, SQLException {
        DbUpdate dbUpdate = dbSetup.dbUpdate(datasource);
        dbUpdate.dropTable(datasource.getName(), tableCE);
        dbUpdate.dropTable(datasource.getName(), tableEM);
        dbUpdate.dropTable(datasource.getName(), tableEP);
        dbUpdate.dropTable(datasource.getName(), tableER);
        dbUpdate.dropTable(datasource.getName(), tableEU);
        dbUpdate.dropTable(datasource.getName(), tablePE);
        dbUpdate.dropTable(datasource.getName(), tableSI);
    }

}
