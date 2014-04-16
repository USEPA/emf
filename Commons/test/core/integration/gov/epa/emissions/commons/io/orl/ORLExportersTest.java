package gov.epa.emissions.commons.io.orl;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.SimpleDataset;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.DbUpdate;
import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.io.DataFormatFactory;
import gov.epa.emissions.commons.io.Exporter;
import gov.epa.emissions.commons.io.importer.Importer;
import gov.epa.emissions.commons.io.importer.PersistenceTestCase;
import gov.epa.emissions.commons.io.importer.VersionedDataFormatFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

public class ORLExportersTest extends PersistenceTestCase {

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
         dbUpdate.dropTable(datasource.getName(), dataset.getInternalSources()[0].getTable());
    }
    
    public void testShouldExportFlexibleDBExporter() throws Exception {
        File folder = new File("test/data/orl/nc");
        Importer importer = new FlexibleDBImporter(folder, new String[] { "small-nonroad-comma.txt" }, dataset,
                dbServer, sqlDataTypes);
        importer.run();

        Exporter exporter = new FlexibleDBExporter(dataset, "", dbServer, optimizedBatchSize);
        File file = doExport(exporter);

        // assert headers
        assertComments(file);

        // assert data
        List data = readData(file);
        assertEquals(16, data.size());
        assertEquals("\"37001\",\"2260001010\",\"100414\",0.56000000000000005,-9,-9,-9,-9,,,,,,,,,,,,,,,,,,,,,,", (String) data.get(0));
        assertEquals("\"37001\",\"2260001010\",\"100425\",0.029999999999999999,-9,-9,-9,-9,,,,,,,,,,,,,,,,,,,,,,", (String) data.get(1));
        // assertEquals(16, exporter.getExportedLinesCount());
    }

    public void testShouldExportOnRoad() throws Exception {
        File folder = new File("test/data/orl/nc");
        Importer importer = new ORLOnRoadImporter(folder, new String[] { "small-onroad-comma.txt" }, dataset, dbServer,
                sqlDataTypes);
        importer.run();

        Exporter exporter = new ORLOnRoadExporter(dataset, "", dbServer, optimizedBatchSize);
        File file = doExport(exporter);

        // assert headers
        assertComments(file);

        // assert data
        List data = readData(file);
        assertEquals(18, data.size());
        assertEquals("\"37001\",\"2201001150\",\"100414\",1.0626199999999999,-9,,,,,,,1.1000000000000001,,,,", (String) data.get(0));
        assertEquals("\"37001\",\"2201001150\",\"100425\",0.20263,-9,,,,,,1.2,,,,,", (String) data.get(1));
    }

    public void testShouldExportOnRoadVersionZero() throws Exception {
        Version version = new Version();
        version.setVersion(0);
        version.setDatasetId(dataset.getId());

        File importFile = new File("test/data/orl/nc", "small-onroad.txt");
        DataFormatFactory formatFactory = new VersionedDataFormatFactory(version, dataset);
        Importer importer = new ORLOnRoadImporter(importFile.getParentFile(), new String[] { importFile.getName() },
                dataset, dbServer, sqlDataTypes, formatFactory);
        importer.run();

        Exporter exporter = new ORLOnRoadExporter(dataset, "", dbServer, formatFactory, optimizedBatchSize, null, null, null);
        File file = doExport(exporter);

        // assert headers
        assertComments(file);

        // assert data
        List data = readData(file);
        assertEquals(18, data.size());
        assertEquals("\"37001\",\"2201001150\",\"100414\",1.0626199999999999,-9,,,,,,,,,,,", (String) data.get(0));
        assertEquals("\"37001\",\"2201001150\",\"100425\",0.20263,-9,,,,,,,,,,,", (String) data.get(1));
    }

    public void testShouldExportNonRoad() throws Exception {
        File folder = new File("test/data/orl/nc");
        Importer importer = new ORLNonRoadImporter(folder, new String[] { "small-nonroad-comma.txt" }, dataset,
                dbServer, sqlDataTypes);
        importer.run();

        Exporter exporter = new ORLNonRoadExporter(dataset, "", dbServer, optimizedBatchSize);
        File file = doExport(exporter);

        // assert headers
        assertComments(file);

        // assert data
        List data = readData(file);
        assertEquals(16, data.size());
        assertEquals("\"37001\",\"2260001010\",\"100414\",0.56000000000000005,-9,-9,-9,-9,,,,,,,,,,,,,,,,,,,,,,", (String) data.get(0));
        assertEquals("\"37001\",\"2260001010\",\"100425\",0.029999999999999999,-9,-9,-9,-9,,,,,,,,,,,,,,,,,,,,,,", (String) data.get(1));
        // assertEquals(16, exporter.getExportedLinesCount());
    }

    public void testShouldExportNonPoint() throws Exception {
        File folder = new File("test/data/orl/nc");
        Importer importer = new ORLNonPointImporter(folder, new String[] { "small-nonpoint-comma.txt" }, dataset,
                dbServer, sqlDataTypes);
        importer.run();

        Exporter exporter = new ORLNonPointExporter(dataset, "", dbServer, optimizedBatchSize);
        File file = doExport(exporter);

        // assert headers
        assertComments(file);

        // assert data
        List data = readData(file);
        assertEquals(6, data.size());
        // regex is used because of precision diff between mysql and postgres
        assertEquals("\"37001\",\"10201302\",\"0\",\"0107\",\"2\",\"0\",\"246\",0.00038729630520000001,-9,-9,-9,-9,,,,,,,,,,,,,,,,,,,,,,,,,",
                ((String) data.get(0)));
        assertEquals("\"37001\",\"10201302\",\"0\",\"0107\",\"2\",\"0\",\"253\",0.00069105811319999997,-9,-9,-9,-9,,,,,,,,,,,,,,,,,,,,,,,,,",
                ((String) data.get(1)));
        // assertEquals(6, exporter.getExportedLinesCount());
    }

    public void testShouldExportPoint() throws Exception {
        File folder = new File("test/data/orl/nc");
        Importer importer = new ORLPointImporter(folder, new String[] { "small-point-comma.txt" }, dataset, dbServer,
                sqlDataTypes);
        importer.run();

        Exporter exporter = new ORLPointExporter(dataset, "", dbServer, optimizedBatchSize);
        File file = doExport(exporter);

        // assert headers
        assertComments(file);

        // assert records
        List records = readData(file);
        assertEquals(10, records.size());
        String expectedPattern = "\"37119\",\"0001\",\"0001\",\"1\",\"1\",\"REXMINC.,CUSTOMDIVISION\",\"40201301\"," +
                "\"02\",\"01\",60,7.5,375,2083.4630000000002,47.159999999999997,\"3083\",\"0714\",\"0\",\"L\",-80.708100000000002,35.119999999999997,17," +
                		"\"108883\",9.7041409999999999,-9,-9,-9,-9,-9,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,666.005,,,,,,,";
        String actual = (String) records.get(0);
        assertEquals(expectedPattern, actual);
    }

    public void testShouldExportFiresInv() throws Exception {
        File folder = new File("test/data/orl/nc");
        Importer importer = new ORLFiresInvImporter(folder, new String[] { "ptinv_fire_2002nei_26jul2006.txt" },
                dataset, dbServer, sqlDataTypes);
        importer.run();

        Exporter exporter = new ORLFiresInvExporter(dataset, "", dbServer, optimizedBatchSize);
        File file = doExport(exporter);

        // assert headers
        assertComments(file);

        // assert records
        List records = readData(file);
        assertEquals(180, records.size());

        String expectedPattern = "\"37001\",\"35273\",\"               \",\"28100010F0\",\"0\",35.909999999999997,-79.310000000000002,\"P \",936,8000";
        String actual = (String) records.get(0);
        assertEquals(expectedPattern, actual);
        //assertEquals(180, exporter.getExportedLinesCount());
    }

    public void testShouldExportDaySpecFiresInv() throws Exception {
        File folder = new File("test/data/orl/nc");
        Importer importer = new ORLDaySpecFiresImporter(folder,
                new String[] { "ptday_firedata_hap2002nei_26jul2006.txt" }, dataset, dbServer, sqlDataTypes);
        importer.run();

        Exporter exporter = new ORLDaySpecFiresExporter(dataset, "", dbServer, optimizedBatchSize);
        File file = doExport(exporter);

        // assert headers
        assertComments(file);

        // assert records
        List records = readData(file);
        assertEquals(783, records.size());

        String expectedPattern = "\"37001\",\"35273\",\"\",\"28100010F0\",\"106990\",\"02/04/02\",0.000172125,0,23";
        String actual = (String) records.get(0);
        assertEquals(expectedPattern, actual);
        assertEquals(783, exporter.getExportedLinesCount());
    }

    private void assertComments(File file) throws IOException {
        List comments = readComments(file);
        assertEquals(headers(dataset.getDescription()).size() + 3, comments.size());
    }

    private File doExport(Exporter exporter) throws Exception {
        File file = File.createTempFile("exported", ".orl");
        file.deleteOnExit();

        exporter.export(file);

        return file;
    }

    private List headers(String description) {
        List headers = new ArrayList();
        Pattern p = Pattern.compile("\n");
        String[] tokens = p.split(description);
        headers.addAll(Arrays.asList(tokens));

        return headers;
    }

    private List readComments(File file) throws IOException {
        List<String> lines = new ArrayList<String>();

        BufferedReader r = new BufferedReader(new FileReader(file));
        for (String line = r.readLine(); line != null; line = r.readLine()) {
            if (isNotEmpty(line) && isComment(line))
                lines.add(line);
        }
        
        if (r != null)
            r.close();

        return lines;
    }

}
