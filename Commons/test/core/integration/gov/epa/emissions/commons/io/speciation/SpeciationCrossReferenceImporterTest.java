package gov.epa.emissions.commons.io.speciation;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.SimpleDataset;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.DbUpdate;
import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.db.TableReader;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.io.importer.PersistenceTestCase;
import gov.epa.emissions.commons.io.importer.VersionedDataFormatFactory;
import gov.epa.emissions.commons.io.importer.VersionedImporter;

import java.io.File;
import java.util.Date;
import java.util.Random;

public class SpeciationCrossReferenceImporterTest extends PersistenceTestCase {

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
        dbUpdate.dropTable(datasource.getName(), dataset.getInternalSources()[0].getTable());
    }

    public void testImportSpeciationCrossRefData() throws Exception {
        File folder = new File("test/data/speciation");
        SpeciationCrossReferenceImporter importer = new SpeciationCrossReferenceImporter(folder,
                new String[] { "gsref.cmaq.cb4p25.txt" }, dataset, dbServer, sqlDataTypes);
        importer.run();

        assertEquals(2000, countRecords());
    }

    public void testImportVersionedSpeciationCrossRefData() throws Exception {
        DbServer localDbServer = dbSetup.getNewPostgresDbServerInstance();
        Version version = new Version();
        version.setVersion(0);

        File folder = new File("test/data/speciation");
        SpeciationCrossReferenceImporter importer = new SpeciationCrossReferenceImporter(folder,
                new String[] { "gsref-point.txt" }, dataset, localDbServer, sqlDataTypes, new VersionedDataFormatFactory(
                        version, dataset));
        VersionedImporter importerv = new VersionedImporter(importer, dataset, localDbServer, lastModifiedDate(folder,
                "gsref-point.txt"));
        importerv.run();

        assertEquals(153, countRecords());
    }

    public void testImportSpeciationCrossRefData_WithLongInlineComments() throws Exception {
        DbServer localDbServer = dbSetup.getNewPostgresDbServerInstance();
        Version version = new Version();
        version.setVersion(0);

        File folder = new File("test/data/speciation");
        SpeciationCrossReferenceImporter importer = new SpeciationCrossReferenceImporter(folder,
                new String[] { "gsrefsample_withLongInlineComments.txt" }, dataset, localDbServer, sqlDataTypes,
                new VersionedDataFormatFactory(version, dataset));
        VersionedImporter importerv = new VersionedImporter(importer, dataset, localDbServer, lastModifiedDate(folder,
                "gsrefsample_withLongInlineComments.txt"));
        importerv.run();

        assertEquals(250, countRecords());
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
