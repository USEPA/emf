package gov.epa.emissions.commons.io.orl;

import gov.epa.emissions.commons.Record;
import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.KeyVal;
import gov.epa.emissions.commons.data.Keyword;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.commons.io.CustomCharSetInputStreamReader;
import gov.epa.emissions.commons.io.CustomCharSetOutputStreamWriter;
import gov.epa.emissions.commons.io.DataFormatFactory;
import gov.epa.emissions.commons.io.NonVersionedTableFormat;
import gov.epa.emissions.commons.io.TableFormat;
import gov.epa.emissions.commons.io.XFileFormat;
import gov.epa.emissions.commons.io.importer.Comments;
import gov.epa.emissions.commons.io.importer.DataTable;
import gov.epa.emissions.commons.io.importer.DatasetLoader;
import gov.epa.emissions.commons.io.importer.DelimiterIdentifyingFileReader;
import gov.epa.emissions.commons.io.importer.FileVerifier;
import gov.epa.emissions.commons.io.importer.Importer;
import gov.epa.emissions.commons.io.importer.ImporterException;
import gov.epa.emissions.commons.io.importer.ImporterPostProcess;
import gov.epa.emissions.commons.io.importer.TemporalResolution;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Random;

public class FlexibleDBImporter implements Importer, ImporterPostProcess {

    private Dataset dataset;

    private Datasource datasource;

    private File file;

    private XFileFormat fileFormat;
    
    private TableFormat tableFormat;

    private DataTable dataTable;

    private DatasetLoader loader;

    private Record record;
    
    private Record firstDataRecord;

    private boolean windowsOS = false;
    
    private boolean withColNames = true; 
    
    public FlexibleDBImporter(File folder, String[] filenames, Dataset dataset, DbServer dbServer,
            SqlDataTypes sqlDataTypes) throws ImporterException {
        this.dataset = dataset;
        this.fileFormat = dataset.getDatasetType().getFileFormat();
        this.tableFormat = new NonVersionedTableFormat(fileFormat, sqlDataTypes);
        init(folder, filenames,dbServer);
    }

    public FlexibleDBImporter(File folder, String[] filenames, Dataset dataset, DbServer dbServer,
            SqlDataTypes sqlDataTypes, DataFormatFactory factory) throws ImporterException {
        this.dataset = dataset;
        this.fileFormat = dataset.getDatasetType().getFileFormat();
        this.tableFormat = factory.tableFormat(fileFormat, sqlDataTypes);
        init(folder, filenames, dbServer);
    }

    private void init(File folder, String[] filePatterns, DbServer dbServer) throws ImporterException {
        new FileVerifier().shouldHaveOneFile(filePatterns);
        String delimiter = fileFormat.getDelimiter();
        
        if (delimiter == null || !delimiter.trim().equals(","))
            throw new ImporterException("Dataset types derived from a flexible file format currently only support comma delimited data.");
        
        this.file = new File(folder, filePatterns[0]);
        this.datasource = dbServer.getEmissionsDatasource();
        if (System.getProperty("os.name").toUpperCase().startsWith("WINDOWS"))
            windowsOS = true;

        dataTable = new DataTable(dataset, datasource);
        loader = new DatasetLoader(dataset);
        loader.internalSource(file, dataTable.name(), tableFormat);
        this.withColNames = getColumnLabel();
    }

    public void run() throws ImporterException {
        KeyVal[] keys = keyValFound(Dataset.head_required);
        importAttributes(file, keys);
        
        dataTable.create(tableFormat, dataset.getId());
        try {
            doImport(file, dataset, dataTable.name());
            
            postRun();
        } catch (Exception e) {
            dataTable.drop();
            e.printStackTrace();
            throw new ImporterException("Filename: " + file.getAbsolutePath() + "; Exception: " + e.getMessage());
        }
    }

    private  KeyVal[] keyValFound(String keyword) {
        KeyVal[] keys = dataset.getDatasetType().getKeyVals();
        List<KeyVal> list = new ArrayList<KeyVal>();
        
        for (KeyVal key : keys)
            if (key.getName().equalsIgnoreCase(keyword)) 
                list.add(key);
        
        return list.toArray(new KeyVal[0]);
    }

    /*
     * @return String a name that is save to use as a file name
     */
    public String getNameForFile(String name) {
        String fileName = new String(name);
        for (int i = 0; i < fileName.length(); i++) {
            if (!Character.isLetterOrDigit(fileName.charAt(i))) {
                fileName = fileName.replace(fileName.charAt(i), '_');
            }
        }
        return fileName;
    }

    private void doImport(File file, Dataset dataset, String table)
            throws Exception {
        File headerFile = null;
        File dataFile = null;
        Connection connection = null;
        try {
            String tempDir = System.getProperty("IMPORT_EXPORT_TEMP_DIR");

            if (tempDir == null || tempDir.isEmpty())
                tempDir = System.getProperty("java.io.tmpdir");

            File tempDirFile = new File(tempDir);

            if (!(tempDirFile.exists() && tempDirFile.isDirectory() && tempDirFile.canWrite() && tempDirFile.canRead()))
                throw new Exception("Import-export temporary folder does not exist or lacks write permissions: "
                        + tempDir);

            Random rando = new Random();
            long id = Math.abs(rando.nextInt());

            headerFile = new File(tempDir, getNameForFile(dataset.getName()) + id + ".header");
            dataFile = new File(tempDir, getNameForFile(dataset.getName()) + id + ".data");

            splitFile(file, headerFile, dataFile);
            loadDataset(getComments(headerFile), dataset);
            checkDataLine();
            String copyString = "COPY " + getFullTableName(table) + " (" + getColNames() + ") FROM '"
            + putEscape(dataFile.getAbsolutePath()) + "' WITH CSV QUOTE AS '\"'";
            if (withColNames){
                copyString = "COPY " + getFullTableName(table) + " (" + getColNames() + ") FROM '"
                + putEscape(dataFile.getAbsolutePath()) + "' WITH CSV HEADER QUOTE AS '\"'";    
            } 
            connection = datasource.getConnection();
            Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            statement.execute(copyString);
            statement.close();
        } catch (Exception exc) {
            // NOTE: this closes the db server for other importers
            // try
            // {
            // if ((connection != null) && !connection.isClosed()) connection.close();
            // }
            // catch (Exception ex)
            // {
            // throw ex;
            // }
            // throw exc;
            throw new Exception(exc.getMessage());
        } finally {
            if ((headerFile != null) && headerFile.exists())
                headerFile.delete();
            if ((dataFile != null) && dataFile.exists())
                dataFile.delete();
        }
    }

    private void splitFile(File file, File headerFile, File dataFile) throws Exception {
        if (withColNames){
            compareCols();   //compare number of columns and column names 
         }
        
        if (windowsOS) {
            splitOnWindows(file, headerFile, dataFile);
            return;
        }

        String headerCmd = "grep \"^#\" " + file.getAbsolutePath() + " | grep -v \"^#EXPORT_\" > "
                + headerFile.getAbsolutePath();
        String dataCmd = "grep -v \"^#\" " + file.getAbsolutePath() + " | grep -v \"^[[:space:]]*$\" > "
                + dataFile.getAbsolutePath();
        
        String[] cmd = new String[] { "sh", "-c", headerCmd + ";" + dataCmd };

        Process p = Runtime.getRuntime().exec(cmd);
        int errorLevel = p.waitFor();

        if (errorLevel > 0)
            throw new Exception("Saving data/header files to " + headerFile.getParent()
                    + " directory failed (check permissions).");
    }

    private void splitOnWindows(File file, File headerFile, File dataFile) throws IOException {
        BufferedReader fileReader = null;
        PrintWriter headWriter = null;
        PrintWriter dataWriter = null;
        String line = null;
        boolean firstHeadLine = true;
        boolean firstDataLine = true;
        
        headerFile.createNewFile();
        dataFile.createNewFile();

        try {
            fileReader = new BufferedReader(new CustomCharSetInputStreamReader(new FileInputStream(file)));
            headWriter = new PrintWriter(new CustomCharSetOutputStreamWriter(new FileOutputStream(headerFile)));
            dataWriter = new PrintWriter(new CustomCharSetOutputStreamWriter(new FileOutputStream(dataFile)));
        } catch (UnsupportedEncodingException e) {
            throw new FileNotFoundException("Encoding char set not supported.");
        }

        while ((line = fileReader.readLine()) != null) {
            line = line.trim();
            if (line.startsWith("#") && !line.startsWith("#EXPORT_")) {
                if (!firstHeadLine)
                    headWriter.println();
                headWriter.write(line);
                firstHeadLine = false;
            } else if (!line.startsWith("#") && !line.isEmpty()) {
                if (!firstDataLine)
                    dataWriter.println();
                dataWriter.write(line);
                firstDataLine = false;

            }
        }
        fileReader.close();
        headWriter.close();
        dataWriter.close();

    }

    private List<String> getComments(File file) throws Exception {
        BufferedReader fileReader = new BufferedReader(new CustomCharSetInputStreamReader(new FileInputStream(file)));
        List<String> commentsList = new ArrayList<String>();
        String line = null;

        while ((line = fileReader.readLine()) != null)
            commentsList.add(line);

        fileReader.close();

        return commentsList;
    }

    private String getFullTableName(String table) {
        return this.datasource.getName() + "." + table;
    }

    private String getColNames() throws ImporterException {
        Column[] cols = fileFormat.cols();
        String colsString = "";
        
        try {
            for (int i = 0; i < firstDataRecord.size(); i++)
                colsString += cols[i].name() + ",";
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new ImporterException("Number of columns in the data doesn't match the file format " + "(expected:"
                    + cols.length + " but was:" + firstDataRecord.size() + ").");
        }

        return colsString.substring(0, colsString.length() - 1);
    }

//    private boolean hasColName(String colName) {
//        Column[] cols = fileFormat.cols();
//        boolean hasIt = false;
//        for (int i = 0; i < cols.length; i++)
//            if (colName.equalsIgnoreCase(cols[i].name())) hasIt = true;
//
//        return hasIt;
//    }

    private void loadDataset(List<String> comments, Dataset dataset) {
        String tempResltn = dataset.getTemporalResolution();

        if (tempResltn == null || tempResltn.trim().isEmpty())
            dataset.setTemporalResolution(TemporalResolution.ANNUAL.getName());
        
        dataset.setUnits("short tons/year");
        dataset.setDescription(new Comments(comments).all());
    }

    private void importAttributes(File file, KeyVal[] keys) throws ImporterException {
        DelimiterIdentifyingFileReader reader = null;
        try {
            int mincols = 0;
            Column[] cols = fileFormat.cols();
            
            for (Column col : cols)
                if (col.isMandatory())
                    mincols++;
            
            reader = new DelimiterIdentifyingFileReader(file, mincols);
            record = reader.read();
             
            if (withColNames)
                this.firstDataRecord = reader.read();
            else
                firstDataRecord = record; 
            
            if (record.size()==0 || firstDataRecord.size()==0 )
                throw new ImporterException("Data file is empty.");
            if (reader.delimiter() == null || !reader.delimiter().equals(","))
                throw new ImporterException("Data file is not delimited by comma.");
        } catch (Exception e) {
            throw new ImporterException(e.getMessage());
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                throw new ImporterException(e.getMessage());
            }
        }

        addAttributes(reader.comments(), keys);
    }

    private void addAttributes(List<String> commentsList, KeyVal[] keys) throws ImporterException {
        Comments comments = new Comments(commentsList);
        
        if (comments.hasContent("YEAR")) {
            String yearValueStr = comments.content("YEAR");
            int year = getYear(yearValueStr);
            
            if (year == -1)
                throw new ImporterException("Invalid Year: " + yearValueStr + ".");
        
            if (year >= 2200)
                throw new ImporterException("Invalid Year: " + year + " ( >= 2200 ).");
            
            dataset.setYear(year);
            
            if (!comments.hasContent("EMF_START_DATE") && !comments.hasContent("EMF_END_DATE"))
                setStartStopDateTimes(dataset, year);
        }
        
        if (keys == null || keys.length == 0)
            return;
        
        for (KeyVal key : keys) {
            String value = key.getValue();
            
            if (value != null && !comments.hasRightTagFormat(value.trim().charAt(0)+"", value.trim().substring(1)))
                throw new ImporterException("The imported file was supposed to have - '" + value.trim() + "' in the header, but it was missing.");
        }
    }

    private int getYear(String yearStr) {
        int len = yearStr == null ? 0 : yearStr.length();
        String year = "";
        int mark = -1;
        
        for (int i = 0; i < len; i++) {
            if (!year.isEmpty() && i == mark + 1)
                break;
            
            if (Character.isDigit(yearStr.charAt(i))) {
                year += yearStr.charAt(i);
            } else
                mark = i;
        }
        
        if (year.isEmpty())
            return -1;
        
        return Integer.parseInt(year);
    }

    private void setStartStopDateTimes(Dataset dataset, int year) {
        Date start = new GregorianCalendar(year, Calendar.JANUARY, 1).getTime();
        dataset.setStartDateTime(start);

        Calendar endCal = new GregorianCalendar(year, Calendar.DECEMBER, 31, 23, 59, 59);
        endCal.set(Calendar.MILLISECOND, 999);
        dataset.setStopDateTime(endCal.getTime());
    }

    private String putEscape(String path) {
        if (windowsOS)
            return path.replaceAll("\\\\", "\\\\\\\\");

        return path;
    }

    public void postRun() throws ImporterException {

        try {

            createIndexes();

        } catch (Exception exc) {
            throw new ImporterException(exc.getMessage());
        } finally {
            //
        }
    }
    
    private boolean  getColumnLabel(){
        KeyVal[] keys = keyValFound(Dataset.csv_header_line);
        if (keys !=null && keys.length >0){
            String value = keys[0].getValue().toLowerCase();
            if ( value !=null && (value.contains("n") || value.contains("f"))) 
                return false;              //first line of data file is data 
        }
        return true;   // first line is column names
    }
    
     private void compareCols() throws ImporterException{
        String[] cols = getAllColNames().split(",");
        String[] tokens = record.getTokens();
        
        if (cols.length != tokens.length)
            throw new ImporterException("Number of columns in the data doesn't match the file format " + "(expected:"
                    + cols.length + " but was:" + tokens.length + "). Hint: correct typos or set keyword EXPORT_COLUMN_LABEL to false");
        for (int i = 0; i < cols.length; i++) {
            if (!cols[i].equalsIgnoreCase(tokens[i].trim()))
                throw new ImporterException("columns in the data doesn't match columns in the file format " + "(expected:"
                        + cols[i] + " but was:" + tokens[i] + "). Hint: correct typos or set keyword EXPORT_COLUMN_LABEL to false");
        }
    } 
     
     private void checkDataLine() throws ImporterException{
      
         Column[] cols = fileFormat.cols();
         String[] tokens = firstDataRecord.getTokens();
         //System.out.println(tokens[0]);
         
         for (int i = 0; i < cols.length; i++) {
             if (cols[i].isMandatory()){
                if ( tokens[i]!=null && !tokens[i].trim().isEmpty()){
                    String type = cols[i].sqlType();
                    if (type.toUpperCase().startsWith("VARCHAR")){
                        int end =  type.lastIndexOf(")");
                        //Here startIndex is inclusive while endIndex is exclusive.
                        int length = Integer.parseInt(type.substring(8, end));
                        if (tokens[i].length() > length)
                            throw new ImporterException("Error format for column[" + i +"], expected: " 
                                    + type +", but was: " + tokens[i]);
                    }
                    try {
                        if (type.toUpperCase().startsWith("DOUBLE") || type.toUpperCase().startsWith("FLOAT")){
                            Double.parseDouble(tokens[i]);
                            //System.out.println("value of column " +cols[i]+" is "+ value);
                        }
                        if (type.toUpperCase().startsWith("INT")){
                            Integer.parseInt(tokens[i]);
                            //System.out.println("value of column " +cols[i]+" is "+ value);
                        }
                    }catch (NumberFormatException nfe) {
                        throw new ImporterException("Error format for column[" + i +"], expected: " 
                                + type +", but was: " + tokens[i]);
                    }
                }
                else {
                    throw new ImporterException("Data in column[" + i +"], \"" 
                            + cols[i].getName()+ "\", is mandatory, but value is ''.");
                }
             }
         }
     }
     
     public String getAllColNames(){
         Column[] columns = fileFormat.cols();
         String colsString="";
         for (int i = 0; i < columns.length; i++)
             colsString += columns[i].name().trim() + ",";
         return colsString.substring(0, colsString.length() - 1);
     }

     private void createIndexes() {
         String table = dataTable.name().toLowerCase();

         //ALWAYS create indexes for these core columns...
         dataTable.addIndex(table, "record_id", true);
         dataTable.addIndex(table, "dataset_id", false);
         dataTable.addIndex(table, "version", false);
         dataTable.addIndex(table, "delete_versions", false);

         //now index the columns specified in the Keyword INDICES values
         KeyVal[] keyVal = keyValFound(Keyword.INDICES);
         if (keyVal != null && keyVal.length > 0) {
             for (String columnList : keyVal[0].getValue().split("\\|")) {
                 dataTable.addIndex(table, columnList, false);
             }
         }

         //finally analyze the table, so the indexes take affect immediately, 
         //NOT when the SQL engine gets around to analyzing eventually
         dataTable.analyzeTable(table);
     }
}

