package gov.epa.emissions.framework.services.db;

import gov.epa.emissions.framework.services.EmfException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;

public class PostgresRestore {

    Log log = LogFactory.getLog(PostgresRestore.class);

    private boolean windowsOS = false;
    private String postgresBinDir;
    private String postgresDB;
    private String postgresUser;
    private String postgresPassword;
    private String filePath;
    private String errorMsg = "";

    public PostgresRestore() {
        if (System.getProperty("os.name").toUpperCase().startsWith("WINDOWS"))
            this.windowsOS = true;
    }

    public PostgresRestore(String postgresBinDir,
                           String postgresDB,
                           String postgresUser,
                           String postgresPassword,
                           String filePath) {
        this();
        this.postgresBinDir = postgresBinDir;
        this.postgresDB = postgresDB;
        this.postgresUser = postgresUser;
        this.postgresPassword = postgresPassword;
        this.filePath = filePath;
    }

    public void restore() throws EmfException {
        Process process = null;
        try {
            //Validation
            //1st see if there is data for the shape file, if not throw an exception
            //dbServer
            //2nd make sure there is the_geom column so the shape file can be created
            if (filePath == null || filePath.isEmpty())
                throw new EmfException("Missing file path.");

            String[] cmds = new String[6];
            cmds[0] = postgresBinDir + File.separator + "pg_restore";
            cmds[1] = "-U";
            cmds[2] = postgresUser;
            cmds[3] = "-d";
            cmds[4] = postgresDB;
            cmds[5] = putEscape(filePath);

            ProcessBuilder processBuilder = new ProcessBuilder(cmds);
            processBuilder.environment().put("PGPASSWORD", postgresPassword);
            process = processBuilder.start();// Runtime.getRuntime().exec(cmds);

            final InputStream stdout = process.getInputStream();
            new Thread(new Runnable() {

                public void run() {
                    BufferedReader rdr = new BufferedReader(
                            new InputStreamReader(stdout));
                    String line;
                    try {
//                while (rdr.readLine() != null) {
                        while ((line = rdr.readLine()) != null) {
                            System.out.println("lame stdout: " + line);
                        }
                        rdr.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
            final InputStream stderr = process.getErrorStream();
            new Thread(new Runnable() {

                public void run() {
                    BufferedReader rdr = new BufferedReader(
                            new InputStreamReader(stderr));
                    String line;
                    try {
                        while ((line = rdr.readLine()) != null) {
                            errorMsg = (!errorMsg.isEmpty() ? errorMsg + "; " : "")  + line;
                        }
                        rdr.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
//            encode.waitFor();
     
            
            
            
            //lets wait for the process to end, otherwise the process will run asynchronously,
            //and we swon't know when its finished...
            process.waitFor();

            if (!errorMsg.isEmpty())
                throw new EmfException(errorMsg);

//            logStdout("process.getErrorStream", process.getErrorStream());
//            logStdout("process.getErrorStream", process.getInputStream());

        } catch (Exception e) {
//            if (process != null)
//                try {
//                    logStdout("process.getErrorStream", process.getErrorStream());
//                } catch (Exception e1) {
//                    // TODO Auto-generated catch block
//                    e1.printStackTrace();
//                }
//            e.printStackTrace();
            throw new EmfException(e.getMessage());
        } finally {
            //
            System.out.println("");
        }
    }

    public void logStdout(String title, InputStream inStream) throws Exception {
        /**
         * log the stdout from a remote command to the log
         */
        BufferedReader reader = null;

        // log the title of this series of message to the LOG
        log.warn(title);

        reader = new BufferedReader(new InputStreamReader(inStream));

        if (reader != null) {
            try {
                String message = reader.readLine();

                while (message != null) {
                    log.warn(message);

                    message = reader.readLine();
                }
                reader.close();
            } catch (Exception e) {
                e.printStackTrace();
                throw new Exception("Error logging remote command's stdout/stderr: " + e.getMessage());
            }
        }
    }
    
    private String putEscape(String path) {
        if (windowsOS)
            return path.replaceAll("\\\\", "\\\\\\\\");

        return path;
    }

}
