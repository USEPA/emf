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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

public class CountryStateCountyDataExporterTest extends PersistenceTestCase {

    private SqlDataTypes sqlDataTypes;

    private Dataset dataset;

    private DbServer dbServer;

    private Integer optimizedBatchSize;

    protected void setUp() throws Exception {
        super.setUp();

        dbServer = dbSetup.getDbServer();
        sqlDataTypes = dbServer.getSqlDataTypes();
        
        optimizedBatchSize = new Integer(10000);
        
        dataset = new SimpleDataset();
        dataset.setName("test");
        dataset.setId(Math.abs(new Random().nextInt()));
        dataset.setDatasetType(new DatasetType("dsType"));
    }

    protected void doTearDown() throws Exception {
        Datasource datasource = dbServer.getEmissionsDatasource();
        DbUpdate dbUpdate = dbSetup.dbUpdate(datasource);
        dbUpdate.dropTable(datasource.getName(), "country");
        dbUpdate.dropTable(datasource.getName(), "state");
        dbUpdate.dropTable(datasource.getName(), "county");
    }

    public void testExportCountryStateCountyData() throws Exception {
        File folder = new File("test/data/other");
        CountryStateCountyDataImporter importer = new CountryStateCountyDataImporter(folder,
                new String[] { "costcy.txt" }, dataset, dbServer, sqlDataTypes);
        importer.run();

        CountryStateCountyDataExporter exporter = new CountryStateCountyDataExporter(dataset, "", dbServer, optimizedBatchSize);
        File file = File.createTempFile("CSCexported", ".txt");
        exporter.export(file);

        // assert headers
        assertComments(file);

        // assert data
        List data = readData(file);
        assertEquals(37, data.size());
        assertEquals("/COUNTRY/", (String) data.get(0));
        assertEquals("0                   US", (String) data.get(1));
        assertEquals("/STATE/", (String) data.get(8));
        assertEquals("012FL              Florida 4  TEST!jlkjafdsjalsjfd;asdlkjf", (String) data.get(18));
        assertEquals("/COUNTY/", (String) data.get(20));
        assertEquals(34, exporter.getExportedLinesCount());
    }

    public void testExportVersionedCountryStateCountyData() throws Exception {
        DbServer localDbServer = dbSetup.getNewPostgresDbServerInstance();
        Version version = version();
        
        File folder = new File("test/data/other");
        CountryStateCountyDataImporter importer = new CountryStateCountyDataImporter(folder,
                new String[] { "costcy.txt" }, dataset, localDbServer, sqlDataTypes, new VersionedDataFormatFactory(version, dataset));
        VersionedImporter importerv = new VersionedImporter(importer, dataset, localDbServer, lastModifiedDate(folder,"costcy.txt"));
        importerv.run();

        CountryStateCountyDataExporter exporter = new CountryStateCountyDataExporter(dataset, "", dbServer,
                new VersionedDataFormatFactory(version, dataset), optimizedBatchSize, null, null, "");
        File file = File.createTempFile("CSCexported", ".txt");
        exporter.export(file);

        // assert headers
        assertComments(file);

        // assert data
        List data = readData(file);
        assertEquals(37, data.size());
        assertEquals("/COUNTRY/", (String) data.get(0));
        assertEquals("0                   US", (String) data.get(1));
        assertEquals("/STATE/", (String) data.get(8));
        assertEquals("012FL              Florida 4  TEST!jlkjafdsjalsjfd;asdlkjf", (String) data.get(18));
        assertEquals("/COUNTY/", (String) data.get(20));
        assertEquals(" TN         Claiborne Co 047 25 44 440 EST   -83.664   36.474 "
                + "456.71943606  -84.0067  -83.3646  36.3284  36.5988  30059.0", data.get(36));
    }

    private Version version() {
        Version version = new Version();
        version.setVersion(0);
        version.setDatasetId(dataset.getId());
        return version;
    }

    private void assertComments(File file) throws IOException {
        List comments = readComments(file);
        assertEquals(headers(dataset.getDescription()).size() + 3, comments.size());
    }

    private List headers(String description) {
        List headers = new ArrayList();
        Pattern p = Pattern.compile("\n");
        String[] tokens = p.split(description);
        headers.addAll(Arrays.asList(tokens));

        return headers;
    }

    private List readComments(File file) throws IOException {
        List lines = new ArrayList();

        BufferedReader r = new BufferedReader(new FileReader(file));
        for (String line = r.readLine(); line != null; line = r.readLine()) {
            if (isNotEmpty(line) && isComment(line))
                lines.add(line);
        }

        return lines;
    }

    private Date lastModifiedDate(File folder, String fileName) {
        return new Date(new File(folder,fileName).lastModified());
    }
}
