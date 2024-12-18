package gov.epa.emissions.commons.io.other;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.SimpleDataset;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.DbUpdate;
import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.io.importer.PersistenceTestCase;
import gov.epa.emissions.commons.io.importer.VersionedDataFormatFactory;
import gov.epa.emissions.commons.io.importer.VersionedImporter;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class DaySpecPointInventoryExImporterTest extends PersistenceTestCase {

    private SqlDataTypes sqlDataTypes;

    private Dataset dataset;

    private DbServer dbServer;

    private Integer optimizedBatchSize;

    protected void setUp() throws Exception {
        super.setUp();

        dbServer = dbSetup.getDbServer();
        sqlDataTypes = dbServer.getSqlDataTypes();

        optimizedBatchSize = Integer.valueOf(10000);
        
        dataset = new SimpleDataset();
        dataset.setName("test");
        dataset.setId(Math.abs(new Random().nextInt()));
        dataset.setDatasetType(new DatasetType("dsType"));
    }

    protected void doTearDown() throws Exception {
        Datasource datasource = dbServer.getEmissionsDatasource();
        DbUpdate dbUpdate = dbSetup.dbUpdate(datasource);
        dbUpdate.dropTable(datasource.getName(), dataset.getInternalSources()[0].getTable());
    }

    public void testImportCEMpthourData() throws Exception {
        File folder = new File("test/data/other");
        DaySpecPointInventoryImporter importer = new DaySpecPointInventoryImporter(folder, new String[]{"nonCEMptday.txt"},
                dataset, dbServer, sqlDataTypes);
        importer.run();

        File exportfile = File.createTempFile("ptdayExported", ".txt");
        exportfile.deleteOnExit();
        DaySpecPointInventoryExporter exporter = new DaySpecPointInventoryExporter(dataset, "", dbServer, optimizedBatchSize);
        exporter.export(exportfile);

        List data = readData(exportfile);
        assertEquals(48, data.size());
        assertEquals(48, exporter.getExportedLinesCount());
        // assertEquals(
        // "ORISPL_CODE,UNITID,OP_DATE,OP_HOUR,OP_TIME,GLOAD,SLOAD,NOX_MASS,NOX_RATE,SO2_MASS,HEAT_INPUT,FLOW",
        // (String) data.get(0));
        // assertEquals("2161,**GT2,000113,19,0,-9,-9,-9,-9,-9,-9,-9", (String)data.get(21));
    }

    public void testImportVersionedCEMpthourData() throws Exception {
        DbServer localDbServer = dbSetup.getNewPostgresDbServerInstance();
        Version version = new Version();
        version.setVersion(0);
        version.setDatasetId(dataset.getId());
        
        File folder = new File("test/data/other");
        DaySpecPointInventoryImporter importer = new DaySpecPointInventoryImporter(folder, new String[]{"nonCEMptday.txt"},
                dataset, localDbServer, sqlDataTypes, new VersionedDataFormatFactory(version, dataset));
        VersionedImporter importerv = new VersionedImporter(importer, dataset, localDbServer, lastModifiedDate(folder,"nonCEMptday.txt"));
        importerv.run();

        File exportfile = File.createTempFile("ptdayExported", ".txt");
        exportfile.deleteOnExit();
        DaySpecPointInventoryExporter exporter = new DaySpecPointInventoryExporter(dataset, "", dbServer, 
                new VersionedDataFormatFactory(version, dataset), optimizedBatchSize, null, null, null);
        exporter.export(exportfile);

        List data = readData(exportfile);
        assertEquals(48, data.size());
        // assertEquals(
        // "ORISPL_CODE,UNITID,OP_DATE,OP_HOUR,OP_TIME,GLOAD,SLOAD,NOX_MASS,NOX_RATE,SO2_MASS,HEAT_INPUT,FLOW",
        // (String) data.get(0));
        // assertEquals("2161,**GT2,000113,19,0,-9,-9,-9,-9,-9,-9,-9", (String)data.get(21));
    }

    
    private Date lastModifiedDate(File folder, String fileName) {
        return new Date(new File(folder, fileName).lastModified());
    }
}
