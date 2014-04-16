package gov.epa.emissions.commons.db.postgres;

import gov.epa.emissions.commons.data.ProjectionShapeFile;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.io.ExporterException;
import gov.epa.emissions.commons.util.CustomDateFormat;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PostgresSQLToShapeFile {

    Log log = LogFactory.getLog(PostgresSQLToShapeFile.class);

    private boolean windowsOS = false;
    
    private DbServer dbServer;
    
    public PostgresSQLToShapeFile(DbServer dbServer) {
        if (System.getProperty("os.name").toUpperCase().startsWith("WINDOWS"))
            windowsOS = true;
        this.dbServer = dbServer;
    }

    public void create(String postgresBinDir, 
            String postgresDB, 
            String postgresUser, 
            String postgresPassword, 
            String filePath, 
            boolean overide,
            String selectQuery, ProjectionShapeFile projectionShapeFile) throws ExporterException {
        Process process = null;
        try {
            //Validation
            //1st see if there is data for the shape file, if not throw an exception
            //dbServer
            //2nd make sure there is the_geom column so the shape file can be created
            validateSelectQuery(selectQuery);

//            createNewFile(filePath,
//                    projectionShapeFile, overide);

            //found bug where when query lenght exceeds 32k, then it wont work correctly
            //so i'll first create a semi-temporary table (we'll clean it up later)
            //then psql2shp on temp table
            String tempTable = createTempTable(selectQuery);
            
            String[] exportCommand = getWriteQueryString(postgresBinDir, 
                    postgresDB, 
                    postgresUser, 
                    postgresPassword,
                    filePath, 
                    tempTable);


            process = Runtime.getRuntime().exec(exportCommand);

            final InputStream stdout = process.getInputStream();
            new Thread(new Runnable() {
     
            public void run() {
                BufferedReader rdr = new BufferedReader(
                    new InputStreamReader(stdout));
//                String line;
                try {
                while (rdr.readLine() != null) {
//                while ((line = rdr.readLine()) != null) {
                    //System.out.println("lame stdout: " + line);
                }
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
                    System.err.println("lame stderr: " + line);
                }
                } catch (IOException e) {
                e.printStackTrace();
                }
            }
            }).start();
//            encode.waitFor();
     
            
            
            
            //lets wait for the process to end, otherwise the process will run asynchronously,
            //and we swon't know when its finished...
            process.waitFor();
//            logStdout("process.getErrorStream", process.getErrorStream());
//            logStdout("process.getErrorStream", process.getInputStream());

            //remove temp table
            deleteTempTable("public." + tempTable);
        } catch (Exception e) {
            if (process != null)
                try {
                    logStdout("process.getErrorStream", process.getErrorStream());
                } catch (Exception e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            e.printStackTrace();
            throw new ExporterException(e.getMessage());
        } finally {
            //
            System.out.println("");
        }
    }

    private String createTempTable(String selectQuery) throws Exception {
        String nowTimestamp = CustomDateFormat.format_YYYYMMDDHHMMSSSS(new Date());
        dbServer.getEmissionsDatasource().query().execute("create table public.tmp_shp_" + nowTimestamp + " as " + selectQuery);
        return "tmp_shp_" + nowTimestamp;
    }
    
    private void deleteTempTable(String tableName) throws Exception {
        dbServer.getEmissionsDatasource().query().execute("drop table " + tableName);
    }
    
    private void validateSelectQuery(String selectQuery) throws ExporterException {
        try {
            ResultSet rs = dbServer.getEmissionsDatasource().query().executeQuery(selectQuery + " limit 1");
            //see if there are any rows returned...
            if (!rs.next()) 
                throw new ExporterException("Query does not return any data in the resultset.");

            //see if there is a the_geom column...
            ResultSetMetaData md = rs.getMetaData();
            int columnCount = md.getColumnCount();
            boolean hasTheGeomColumn = false;
            for (int i = 1; i <= columnCount; i++) {
                if (md.getColumnName(i).equalsIgnoreCase("the_geom")) {
                    hasTheGeomColumn = true;
                    break;
                }
            }
            if (!hasTheGeomColumn) 
                throw new ExporterException("The SQL query does not have the required the_geom column.");
 
            //make sure we only get one row per geometry setting, if more than one row has the same geometry, the shapefile 
            //will not know which row has the correct information and most likely show a row randomly
            rs = dbServer.getEmissionsDatasource().query().executeQuery("select 1 from (" + selectQuery + ") tbl group by the_geom having count(the_geom) > 1 limit 1");
            if (rs.next())
                throw new ExporterException("The SQL query can''t return more than one record per geometry definition.  For example, a specific county (fips code) has more than one row returned.  Try filtering by pollutant to limit to one record per geometry or try pivoting the result (moving pollutants from rows to columns).");
            
        } catch (SQLException e) {
            throw new ExporterException(e.getMessage(), e);
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

    private String[] getWriteQueryString(String postgresBinDir, 
            String postgresDB, 
            String postgresUser, 
            String postgresPassword, 
            String filePath, 
            String selectQuery) {
        //"pgsql2shp -f test2 -P postgres -u postgres EMF "select * from us_state_shape"
        
        String[] cmds;
//        cmds[0] = "csh";
//        cmds[1] = "-c";
//        cmds[2] = "";

//        System.out.println("csh -c \"" + postgresBinDir + "pgsql2shp -f " + putEscape(filePath) + " -P " + postgresPassword + " -u " + postgresUser + " " + postgresDB + " \"" + selectQuery + "\"\"");

        if (windowsOS) {
            cmds = new String[1];
            cmds[0] = "\"" + postgresBinDir + "pgsql2shp\" -f \"" + putEscape(filePath) + "\" -P " + postgresPassword + " -u " + postgresUser + " " + postgresDB + " \"" + selectQuery.replaceAll("\n", " ").replaceAll("\"", "\\\\\"") + "\"";
        } else {
            cmds = new String[9];
            cmds[0] = postgresBinDir + "/pgsql2shp";
            cmds[1] = "-f";
            cmds[2] = putEscape(filePath);
            cmds[3] = "-P";
            cmds[4] = postgresPassword;
            cmds[5] = "-u";
            cmds[6] = postgresUser;
            cmds[7] = postgresDB;
            cmds[8] = selectQuery;
        }
        
        return cmds;//"csh -c \"" + postgresBinDir + "pgsql2shp -f " + putEscape(filePath) + " -P " + postgresPassword + " -u " + postgresUser + " " + postgresDB + " \"" + selectQuery + "\"\"";
//        return "\"" + postgresBinDir + "pgsql2shp\" -f \"" + putEscape(filePath) + "\" -P " + postgresPassword + " -u " + postgresUser + " " + postgresDB + " \"" + selectQuery + "\"";
    }

    protected void createNewFile(String filePath,
            ProjectionShapeFile projectionShapeFile, boolean overide) throws Exception {
        try {
            System.out.println(filePath);
            // AME: Updates for EPA's system
            File dbfFile = new File(filePath + ".dbf");
            if (!dbfFile.exists()) {
                if (windowsOS) {
                    dbfFile.createNewFile();
                    Runtime.getRuntime().exec("CACLS " + dbfFile.getAbsolutePath() + " /E /G \"Users\":W");
                    dbfFile.setWritable(true, false);
                    Thread.sleep(1000); // for the system to refresh the file access permissions
                }
            } else {
                if ( !overide) 
                    throw new Exception("The file " + dbfFile.getAbsolutePath() + " already exists.");
                dbfFile.delete();
            }
            File shpFile = new File(filePath + ".shp");
            if (!shpFile.exists()) {
                if (windowsOS) {
                    shpFile.createNewFile();
                    Runtime.getRuntime().exec("CACLS " + shpFile.getAbsolutePath() + " /E /G \"Users\":W");
                    shpFile.setWritable(true, false);
                    Thread.sleep(1000); // for the system to refresh the file access permissions
                }
            } else {
                if ( !overide) 
                    throw new Exception("The file " + shpFile.getAbsolutePath() + " already exists.");
                shpFile.delete();
            }
            File shxFile = new File(filePath + ".shx");
            if (!shxFile.exists()) {
                if (windowsOS) {
                    shxFile.createNewFile();
                    Runtime.getRuntime().exec("CACLS " + shxFile.getAbsolutePath() + " /E /G \"Users\":W");
                    shxFile.setWritable(true, false);
                    Thread.sleep(1000); // for the system to refresh the file access permissions
                }
            } else {
                if ( !overide) 
                    throw new Exception("The file " + shxFile.getAbsolutePath() + " already exists.");
                shxFile.delete();
            }
            File prjFile = new File(filePath + ".prj");
            if (!prjFile.exists()) {
                if (windowsOS) {
                    prjFile.createNewFile();
                    Runtime.getRuntime().exec("CACLS " + prjFile.getAbsolutePath() + " /E /G \"Users\":W");
                    prjFile.setWritable(true, false);
                    Thread.sleep(1000); // for the system to refresh the file access permissions
                }
            } else {
                if ( !overide) 
                    throw new Exception("The file " + prjFile.getAbsolutePath() + " already exists.");
                prjFile.delete();
            }
//            if (prjFile.exists()) prjFile.delete();
//            prjFile.createNewFile();
//            if (windowsOS) {
//                Runtime.getRuntime().exec("CACLS " + prjFile.getAbsolutePath() + " /E /G \"Users\":W");
//                prjFile.setWritable(true, false);
//                Thread.sleep(1000); // for the system to refresh the file access permissions
//            }
//            Writer output = new BufferedWriter(new FileWriter(prjFile));
//            try {
//                output.write( projectionShapeFile != null ? projectionShapeFile.getPrjText() : "");
//            }
//            finally {
//                output.close();
//            }
            // for now, do nothing from Linux
        } catch (IOException e) {
            e.printStackTrace();
            throw new ExporterException("Could not create shape files: " + filePath + ": " + e.getMessage());
        }
    }
}
