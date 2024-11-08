package gov.epa.emissions.commons.io.orl;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.data.KeyVal;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.commons.io.CustomCharSetOutputStreamWriter;
import gov.epa.emissions.commons.io.DataFormatFactory;
import gov.epa.emissions.commons.io.ExporterException;
import gov.epa.emissions.commons.io.XFileFormat;
import gov.epa.emissions.commons.io.generic.GenericExporter;
import gov.epa.emissions.commons.io.importer.NonVersionedDataFormatFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FlexibleDBExporter extends GenericExporter {

    Log log = LogFactory.getLog(ORLExporter.class);
    
    private Dataset[] datasets;
    
    private DataFormatFactory[] dataFormatFactories;
    
    private XFileFormat fileFormat;
    
    private Datasource datasource;

    private boolean windowsOS = false;
    
    private boolean withColNames = true;   //true: first data line is column names
    
    protected Map<String, String> colsToExport;
    
    public FlexibleDBExporter(Dataset[] datasets, String rowFilters, DbServer dbServer, DataFormatFactory[] dataFormatFactories,
            Integer optimizedBatchSize, Dataset filterDataset, Version filterDatasetVersion, String filterDatasetJoinCondition,
            String colsToExport) {
        super(datasets[0], rowFilters, dbServer, datasets[0].getDatasetType().getFileFormat(), null, optimizedBatchSize, filterDataset, filterDatasetVersion, filterDatasetJoinCondition);
        this.datasets = datasets;
        this.dataFormatFactories = dataFormatFactories;
        this.fileFormat = datasets[0].getDatasetType().getFileFormat();
        this.datasource = dbServer.getEmissionsDatasource();
        this.withColNames = getColumnLabel();

        if (System.getProperty("os.name").toUpperCase().startsWith("WINDOWS"))
            windowsOS = true;
        setDelimiter(",");
        
        // colsToExport format: <dataset column name>:<output column name>,<name 1>,...
        if (colsToExport != null && !colsToExport.equals("")) {
            this.colsToExport = new HashMap<String, String>();
            for (String prefValue : colsToExport.split(",")) {
                String parts[] = prefValue.split(":");
                if (parts.length > 1) {
                    this.colsToExport.put(parts[0].toLowerCase(), parts[1]);
                } else {
                    this.colsToExport.put(parts[0].toLowerCase(), "");
                }
            }
        }
    }

    public FlexibleDBExporter(Dataset dataset, String rowFilters, DbServer dbServer, Integer optimizeBatchSize) {
        this(new Dataset[] { dataset }, rowFilters, dbServer, new DataFormatFactory[] { new NonVersionedDataFormatFactory() }, optimizeBatchSize, null, null, null, null);
    }

    public void export(File file) throws ExporterException {
        // TBD: make this use the new temp dir
        String tempDir = System.getProperty("IMPORT_EXPORT_TEMP_DIR");

        if (tempDir == null || tempDir.isEmpty())
            tempDir = System.getProperty("java.io.tmpdir");

        File tempDirFile = new File(tempDir);

        if (!(tempDirFile.exists() && tempDirFile.isDirectory() && tempDirFile.canWrite() && tempDirFile.canRead()))
            throw new ExporterException("Import-export temporary folder does not exist or lacks write permissions: "
                    + tempDir + "");
        
        if (fileFormat==null || fileFormat.cols()== null || fileFormat.cols().length == 0)
            throw new ExporterException("Flexible file format is not defined for dataset: "+datasets[0].getName());

        Random rando = new Random();
        long id = Math.abs(rando.nextInt());

        String separator = System.getProperty("file.separator");
        
        String headerFileName = tempDir + separator + file.getName() + id + ".hed";
        File headerFile = new File(headerFileName);

        String[] dataFileNames = new String[datasets.length];
        File[] dataFiles = new File[datasets.length];
        
        for (int i = 0; i < datasets.length; i++) {
            id = Math.abs(rando.nextInt());
            dataFileNames[i] = tempDir + separator + file.getName() + id + ".dat";
            dataFiles[i] = new File(dataFileNames[i]);
        }

        // use one statement and connection object for all operations, this we can easily clean them up....
        Connection connection = null;
        Statement statement = null;

        try {
            writeHeader(headerFile);

            connection = datasource.getConnection();
            
            for (int i = 0; i < datasets.length; i++) {
                createNewFile(dataFiles[i]);

                String originalQuery = getQueryString(datasets[i], dataFormatFactories[i], rowFilters, datasource, this.filterDataset, this.filterDatasetVersion, this.filterDatasetJoinCondition);
                String query = getColsSpecdQueryString(datasets[i], originalQuery);
         
                String writeQuery = getWriteQueryString(dataFileNames[i], query, (i == 0 ? withColNames : false));
                //System.out.print(writeQuery);

                // log.warn(writeQuery);

                statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                setExportedLines(originalQuery, statement);
                executeQuery(statement, writeQuery);
            }
            concatFiles(file, headerFileName, dataFileNames);
            //setExportedLines(originalQuery, statement);
        } catch (Exception e) {
            //e.printStackTrace();
            // NOTE: this closes the db server for other exporters
            // try {
            // if ((connection != null) && !connection.isClosed())
            // connection.close();
            // } catch (Exception ex) {
            // throw new ExporterException(ex.getMessage());
            // }
            throw new ExporterException(e.getMessage());
        } finally {
            try {
                if (statement != null)
                    statement.close();
                // if (connection != null)
                // connection.close();
            } catch (SQLException e) {
                //
            }
            for (File dataFile : dataFiles) {
                if (dataFile.exists())
                    dataFile.delete();
            }
            if (headerFile.exists())
                headerFile.delete();
        }
    }

    private Map<String, String> getTableCols(Dataset dataset) {
        ResultSet rs = null;
        Map<String, String> cols = new HashMap<String, String>();
        InternalSource source = dataset.getInternalSources()[0];
        if ("versions".equalsIgnoreCase(source.getTable().toLowerCase()) && "emissions".equalsIgnoreCase(datasource.getName().toLowerCase())) {
            System.err.println("Versions table moved to EMF. Error in " + this.getClass().getName());
        }
        String qualifiedTable = datasource.getName() + "." + source.getTable();
        try {
            rs = datasource.query().executeQuery("select * from " + qualifiedTable + " where 1 = 0");
            ResultSetMetaData md = rs.getMetaData();
            for (int i = 1; i <= md.getColumnCount(); i++)
                cols.put(md.getColumnName(i).toLowerCase(), md.getColumnName(i));
        } catch (SQLException e) {
            //
        } finally {
            if (rs != null)
                try {
                    rs.close();
                } catch (SQLException e) {
                    //
                }
        }

        return cols;
    }

    private void createNewFile(File file) throws Exception {
        try {
            if (windowsOS) {
                // AME: Updates for EPA's system
                file.createNewFile();
                Runtime.getRuntime().exec("CACLS " + file.getAbsolutePath() + " /E /G \"Users\":W");
                file.setWritable(true, false);
                Thread.sleep(1000); // for the system to refresh the file access permissions
            }
            // for now, do nothing from Linux
        } catch (IOException e) {
            throw new ExporterException("Could not create export file: " + file.getAbsolutePath());
        }
    }

    private String putEscape(String path) {
        if (windowsOS)
            return path.replaceAll("\\\\", "\\\\\\\\");

        return path;
    }

    protected void writeHeader(File file) throws Exception {
        PrintWriter writer = new PrintWriter(new CustomCharSetOutputStreamWriter(new FileOutputStream(file)));

        try {
            boolean headercomments = datasets[0].getHeaderCommentsSetting();

            if (headercomments)
                writeHeaders(writer, datasets[0], dataFormatFactories[0]);
        } finally {
            writer.close();
        }
    }

    private void executeQuery(Statement statement, String writeQuery) throws SQLException {
        // Statement statement = null;

        try {
            statement.execute(writeQuery);
        } catch (Exception e) {
            log.error("Error executing query: " + writeQuery + ".", e);
            throw new SQLException(e.getMessage());
        } finally {
             if (statement != null)
             statement.close();
        }
    }

    private void concatFiles(File file, String headerFile, String[] dataFiles) throws Exception {
        String[] cmd = null;

        if (windowsOS) {
            String cmdString = "copy \"" + headerFile + "\"";
            for (String dataFile : dataFiles) {
                cmdString += " + \"" + dataFile + "\"";
            }
            cmdString += " \"" + file.getAbsolutePath() + "\" /Y";
            cmd = getCommands(cmdString);
        } else {
            String cmdString = "cat " + headerFile;
            for (String dataFile : dataFiles) {
                cmdString += " " + dataFile;
            }
            cmdString += " > " + file.getAbsolutePath();
            cmd = new String[] { "sh", "-c", cmdString };
        }
        
        Process p = Runtime.getRuntime().exec(cmd);
        int errorLevel = p.waitFor();

        if (errorLevel > 0)
            throw new Exception("Concatinating header and ORL data to file " + file.getAbsolutePath() + " failed.");
    }

    private String[] getCommands(String command) {
        String[] cmd = new String[3];
        String os = System.getProperty("os.name");

        if (os.equalsIgnoreCase("Windows 98") || os.equalsIgnoreCase("Windows 95")) {
            cmd[0] = "command.com";
        } else {
            cmd[0] = "cmd.exe";
        }

        cmd[1] = "/C";
        cmd[2] = command;

        return cmd;
    }

    private String getColsSpecdQueryString(Dataset dataset, String originalQuery ) throws Exception{
        String selectColsString = "SELECT ";     
        Column[] cols = fileFormat.cols();
        Map<String, String> tableColsMap = getTableCols(dataset);
        int numCols = cols.length;
        
        // check if dataset_id should get included in export
        if (colsToExport != null && colsToExport.get("dataset_id") != null) {
            selectColsString += "\"dataset_id\"";
            String colValue = colsToExport.get("dataset_id");
            if (!colValue.equals("")) {
                selectColsString += " as \"" + colValue + "\"";
            }
            selectColsString += ",";
        }
        
        for (int i = 0; i < numCols; i++) {
            String colName = cols[i].name();
            String outputName = colName;
            if (colsToExport != null) {
                String colValue = colsToExport.get(colName.toLowerCase());
                if (colValue == null) continue; // column is not exported
                if (!colValue.equals("")) {
                    outputName = colValue;
                }
            }
            // make sure you only include columns that exist in the table, new columns could have been
            // added to the ORL file format...
            if (tableColsMap.containsKey(colName.toLowerCase())) {
                selectColsString += "\"" + tableColsMap.get(colName.toLowerCase()) + "\"";
                if (!outputName.equals(colName)) {
                    selectColsString += " as \"" + outputName + "\"";
                }
            } else {
                selectColsString += "null as \"" + outputName + "\"";
            }
            selectColsString += ",";
        }

        selectColsString = selectColsString.substring(0, selectColsString.length() - 1);

        return selectColsString + " " + getSubString(originalQuery, "FROM", false);
    }
    

    private String getWriteQueryString(String dataFile, String query, boolean withColNames) {
        String columnsThatNeedQuotes = getNeedQuotesCols();
        String withClause = " WITH NULL '' CSV" + (!columnsThatNeedQuotes.isEmpty() ? " FORCE QUOTE " + columnsThatNeedQuotes : "");
        if (withColNames)
            withClause = " WITH NULL '' CSV HEADER" + (!columnsThatNeedQuotes.isEmpty() ? " FORCE QUOTE " + columnsThatNeedQuotes : "");
        return "COPY (" + query + ") to '" + putEscape(dataFile) + "'" + withClause;
    }

    private String getNeedQuotesCols() {
        String colNames = "";
        Column[] cols = fileFormat.cols();
        int numCols = cols.length;

        for (int i = 0; i < numCols; i++) {
            String colName = cols[i].name();
            String outputName = colName;
            if (colsToExport != null) {
                String colValue = colsToExport.get(colName.toLowerCase());
                if (colValue == null) continue; // column is not exported
                if (!colValue.equals("")) {
                    outputName = colValue;
                }
            }
            String colType = cols[i].sqlType().toUpperCase();

            if (colType.startsWith("VARCHAR") || colType.startsWith("TEXT"))
                colNames += outputName + delimiter;
        }

        return (colNames.length() > 0) ? colNames.substring(0, colNames.length() - 1) : colNames;
    }

    
    private boolean  getColumnLabel(){
        KeyVal[] keys = keyValFound(Dataset.csv_header_line);
        if (keys !=null && keys.length >0){
            String value = keys[0].getValue().toLowerCase();
            if ( value !=null && (value.contains("n") || value.contains("f"))) 
                return false;              //first line of data file is data 
        }
        return true; 
    }
    
    private  KeyVal[] keyValFound(String keyword) {
        KeyVal[] keys = datasets[0].getDatasetType().getKeyVals();
        List<KeyVal> list = new ArrayList<KeyVal>();
        
        for (KeyVal key : keys)
            if (key.getName().equalsIgnoreCase(keyword)) 
                list.add(key);
        
        return list.toArray(new KeyVal[0]);
    }
    
//    private String getCols() {
//        String colNames = "";
//        Column[] cols = fileFormat.cols();
//        int numCols = cols.length;
//
//        for (int i = 0; i < numCols; i++) {
//            colNames += cols[i].name() + delimiter;
//        }
//
//        return (colNames.length() > 0) ? colNames.substring(0, colNames.length() - 1) : colNames;
//    }
    
}
