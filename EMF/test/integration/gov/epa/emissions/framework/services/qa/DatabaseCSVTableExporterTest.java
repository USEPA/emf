package gov.epa.emissions.framework.services.qa;

import gov.epa.emissions.commons.io.ExporterException;
import gov.epa.emissions.framework.services.ServiceTestCase;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DatabaseCSVTableExporterTest extends ServiceTestCase {

    protected void doSetUp() throws Exception {
        // NOTE Auto-generated method stub

    }

    protected void doTearDown() throws Exception {
        // NOTE Auto-generated method stub

    }

    public void testShouldExportATableToCSVFile() throws IOException, ExporterException {
        DatabaseTableCSVExporter exporter = new DatabaseTableCSVExporter(dbServer()
                .getReferenceDatasource().getName() + ".Pollutants", dbServer()
                .getReferenceDatasource());
        File file = File.createTempFile("exported", ".csv");
        file.deleteOnExit();

        exporter.export(file);
        // assert data
        List data = readData(file);
        assertEquals(9, data.size());
        assertEquals("pollutant_code,pollutant_name", data.get(0));
        assertEquals("\"CO\",\"CO\"",data.get(1));
        assertEquals("\"VOC\",\"VOC\"",data.get(8));

    }

    private List readData(File file) throws IOException {
        List data = new ArrayList();

        BufferedReader r = new BufferedReader(new FileReader(file));
        for (String line = r.readLine(); line != null; line = r.readLine()) {
            if (isNotEmpty(line) && !isComment(line))
                data.add(line);
        }

        return data;
    }

    private boolean isNotEmpty(String line) {
        return line.length() != 0;
    }

    private boolean isComment(String line) {
        return line.startsWith("#");
    }

}
