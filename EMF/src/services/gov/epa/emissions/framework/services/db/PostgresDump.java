package gov.epa.emissions.framework.services.db;

import gov.epa.emissions.framework.services.EmfException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class PostgresDump {

    Log log = LogFactory.getLog(PostgresDump.class);

    private boolean windowsOS = false;
    private String postgresBinDir;
    private String postgresDB;
    private String postgresUser;
    private String postgresPassword;
    private String filePath;
    private String schema;
    private String[] tableNames;
    private String errorMsg = "";

    public PostgresDump() {
        if (System.getProperty("os.name").toUpperCase().startsWith("WINDOWS"))
            this.windowsOS = true;
    }

    public PostgresDump(String postgresBinDir,
                     String postgresDB,
                     String postgresUser,
                     String postgresPassword,
                     String filePath,
                     String schema,
                     String[] tableNames) {
        this();
        this.postgresBinDir = postgresBinDir;
        this.postgresDB = postgresDB;
        this.postgresUser = postgresUser;
        this.postgresPassword = postgresPassword;
        this.filePath = filePath;
        this.schema = schema;
        this.tableNames = tableNames;
    }

    public void dump() throws EmfException {
        Process process = null;
        try {
            //Validation
            //1st see if there is data for the shape file, if not throw an exception
            //dbServer
            //2nd make sure there is the_geom column so the shape file can be created
            if (schema == null || schema.isEmpty())
                throw new EmfException("Missing schema.");
            if (tableNames == null || tableNames.length == 0)
                throw new EmfException("Missing table name.");

//            createNewFile(filePath,
//                    projectionShapeFile, overide);
            String[] cmds;
//        cmds[0] = "csh";
//        cmds[1] = "-c";
//        cmds[2] = "";

//        System.out.println("csh -c \"" + postgresBinDir + "pgsql2shp -f " + putEscape(filePath) + " -P " + postgresPassword + " -u " + postgresUser + " " + postgresDB + " \"" + selectQuery + "\"\"");

            cmds = new String[1];
//            if (windowsOS) {
//                cmds[0] = "\"" + postgresBinDir + "pg_dump\" -U " + postgresUser + " -F c -N " + schema + "  -f \"" + putEscape(filePath) + "\"";
//                for (String tableName : tableNames) {
//                    cmds[0] += " -t " + schema + "." + tableName;
//                }
//                cmds[0] += " " + postgresDB + "";
//            } else {
                cmds = new String[10 + 2 * tableNames.length];
                cmds[0] = postgresBinDir + "/pg_dump";
                cmds[1] = "-f";
                cmds[2] = putEscape(filePath);
                cmds[3] = "-U";
                cmds[4] = postgresUser;
                cmds[5] = "-F";
                cmds[6] = "c";
                cmds[7] = "-N";
                cmds[8] = schema;
                int i;
                for (i = 0; i < tableNames.length; i++) {
                    cmds[8 + 2 * i + 1] = "-t";
                    cmds[8 + 2 * i + 2] = schema + "." + tableNames[i];
                }
                //0 11 and 12
                //1 13 and 14
                //2 15 and 16
                cmds[8 + 2 * (i - 1) + 3] = postgresDB;
//            } `

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
            if (process != null)
                try {
                    logStdout("process.getErrorStream", process.getErrorStream());
                } catch (Exception e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            e.printStackTrace();
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
