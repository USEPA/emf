package gov.epa.emissions.commons.io;

import java.io.*;
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
        String exportFile = "data\\bad-exported-non-point.txt";

        run(expectedMessage, type, importFile, exportFile);
    }

    private void run(final String expectedMessage, String type, String importFile, String exportFile) throws IOException,
            InterruptedException {
        String osName = System.getProperty("os.name");
        assertTrue("ExIm Compatibility Tests only run on Windows", osName.startsWith("Win"));

        File workingDir = new File("test/core/compatibility");
        String script = "compare_invs.pl";
        String[] args = new String[] { "C:\\Users\\DDelVecc\\Documents\\git\\bin\\perl.exe", script, type, importFile, exportFile };

        Runtime rt = Runtime.getRuntime();
        Process p = rt.exec(args, null, workingDir);

        // any error message?
        StreamGobbler errorGobbler = new
                StreamGobbler(p.getErrorStream(), "ERROR");

        // any output?
        StreamGobbler outputGobbler = new
                StreamGobbler(p.getInputStream(), "OUTPUT");

        // kick them off
        errorGobbler.start();
        outputGobbler.start();


        int errorLevel = p.waitFor();
//        errorGobbler.getStatus();
        String status = outputGobbler.getStatus();
        assertEquals(expectedMessage, status);
//        String status = captureStatus(p);

        p.destroy();
    }

    /**
     * Ignore errors from the ErrorStream. Capture the status message (the last
     * line) from the InputStream.
     */
    private String captureStatus(Process p) throws IOException {
//        p.getErrorStream().close();// ignore errors
        InputStreamReader isReader = new InputStreamReader(p.getInputStream());
        BufferedReader reader = new BufferedReader(isReader, 204800);
        reader.ready();
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

    /**
     * Capture the status message (the last
     * line) from the InputStream or ErrorStream.
     */
    private String captureStatus(BufferedReader reader) {
        String line = null;
        try {
            while ((line = reader.readLine()) != null); //
        } catch (IOException e) {
            e.printStackTrace();
        }
        return line;
    }


    class StreamGobbler extends Thread
    {
        InputStream is;
        String type;
        OutputStream os;
        private String status;

        StreamGobbler(InputStream is, String type)
        {
            this(is, type, null);
        }
        StreamGobbler(InputStream is, String type, OutputStream redirect)
        {
            this.is = is;
            this.type = type;
            this.os = redirect;
        }

        public void run()
        {
            try
            {
                PrintWriter pw = null;
                if (os != null)
                    pw = new PrintWriter(os);

                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                String line=null;
                while ( (line = br.readLine()) != null)
                {
                    if (pw != null)
                        pw.println(line);
                    System.out.println(type + ">" + line);
                    this.status = line;
                }
                if (pw != null)
                    pw.flush();
            } catch (IOException ioe)
            {
                ioe.printStackTrace();
            }
        }

        public String getStatus() {
            return status;
        }
    }
}
