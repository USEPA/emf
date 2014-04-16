package gov.epa.emissions.commons.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

public class ExImCompatibilityTest extends TestCase {

    public void testExportedImportedFilesMatchForValidNonPointFiles() throws Exception {
        String expectedMessage = "Status: success";
        String type = "ORLNonpoint";
        String importFile = "data\\imported-non-point.txt";
        String exportFile = "data\\exported-non-point.txt";

        run(expectedMessage, type, importFile, exportFile);
    }
    
    public void testExportedImportedFilesMatchForValidOnRoadFiles() throws Exception {
        String expectedMessage = "Status: success";
        String type = "ORLOnroad";
        String importFile = "data\\imported-small-onroad.orl";
        String exportFile = "data\\exported-small-onroad.orl";
        
        run(expectedMessage, type, importFile, exportFile);
    }

    public void testExportShouldFailToMatchImportForUnmatchedNonPointFiles() throws Exception {
        String expectedMessage = "Status: failure";

        String type = "ORLNonpoint";
        String importFile = "data\\imported-non-point.txt";
        String exportFile = "data\\BAD-exported-non-point.txt";

        run(expectedMessage, type, importFile, exportFile);
    }

    private void run(String expectedMessage, String type, String importFile, String exportFile) throws IOException,
            InterruptedException {
        String osName = System.getProperty("os.name");
        assertTrue("ExIm Compatibility Tests only run on Windows", osName.startsWith("Win"));

        File workingDir = new File("test/core/compatibility");
        String script = "compare_invs.pl";
        String[] args = new String[] { "perl.exe", script, type, importFile, exportFile };

        Runtime rt = Runtime.getRuntime();
        Process p = rt.exec(args, null, workingDir);

        String status = captureStatus(p);

        p.waitFor();
        p.destroy();

        assertEquals(expectedMessage, status);
    }

    /**
     * Ignore errors from the ErrorStream. Capture the status message (the last
     * line) from the InputStream.
     */
    private String captureStatus(Process p) throws IOException {
        p.getErrorStream().close();// ignore errors
        InputStreamReader isReader = new InputStreamReader(p.getInputStream());
        BufferedReader reader = new BufferedReader(isReader, 204800);

        List lines = new ArrayList();
        String line = null;
        do {
            line = reader.readLine();
            if (line != null)
                lines.add(line);
        } while (line != null);

        reader.close();

        assertTrue("Should have atleast the status message streamed by the compatiblity script", lines.size() >= 1);

        return (String) lines.get(lines.size() - 1);
    }

}
