package gov.epa.emissions.commons.io.orl;

import gov.epa.emissions.commons.PerformanceTestCase;
import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.SimpleDataset;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.db.TableReader;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.io.DataFormatFactory;
import gov.epa.emissions.commons.io.importer.Importer;
import gov.epa.emissions.commons.io.importer.ImporterException;
import gov.epa.emissions.commons.io.importer.VersionedDataFormatFactory;

import java.io.File;
import java.util.Random;

public class ImportPerformanceTestCase extends PerformanceTestCase {

    protected SqlDataTypes sqlDataTypes;

    protected Dataset dataset;

    protected DbServer dbServer;

    protected VersionedDataFormatFactory formatFactory;

    protected Version version;

    public ImportPerformanceTestCase(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();

        dbServer = dbSetup.getDbServer();
        sqlDataTypes = dbServer.getSqlDataTypes();

        version = new Version();
        version.setVersion(0);
        formatFactory = new VersionedDataFormatFactory(version, dataset);
    }

    protected void doImport(File importFile, String datasetName) throws Exception {
        dataset = new SimpleDataset();
        dataset.setName("test_" + datasetName);
        dataset.setId(Math.abs(new Random().nextInt()));

        doImport(importFile);
    }

    private void doImport(File importFile) throws ImporterException {
        DataFormatFactory formatFactory = new VersionedDataFormatFactory(version, dataset);
        Importer importer = new ORLNonRoadImporter(importFile.getParentFile(), new String[] { importFile.getName() },
                dataset, dbServer, sqlDataTypes, formatFactory);
        importer.run();
    }
    
    protected int countRecords() {
        Datasource datasource = dbServer.getEmissionsDatasource();
        TableReader tableReader = tableReader(datasource);
        return tableReader.count(datasource.getName(), dataset.getInternalSources()[0].getTable());
    }

}
