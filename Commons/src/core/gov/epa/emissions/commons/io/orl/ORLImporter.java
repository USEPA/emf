package gov.epa.emissions.commons.io.orl;

import gov.epa.emissions.commons.Record;
import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.DatasetTypeUnit;
import gov.epa.emissions.commons.data.KeyVal;
import gov.epa.emissions.commons.data.Keyword;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.commons.io.CustomCharSetInputStreamReader;
import gov.epa.emissions.commons.io.CustomCharSetOutputStreamWriter;
import gov.epa.emissions.commons.io.FileFormatWithOptionalCols;
import gov.epa.emissions.commons.io.FormatUnit;
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
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Random;

public class ORLImporter implements Importer, ImporterPostProcess  {

    private Dataset dataset;

    private Datasource datasource;

    private File file;

    private FormatUnit formatUnit;

    private DataTable dataTable;

    private DatasetLoader loader;

    private Record record;

    private boolean windowsOS = false;

    public ORLImporter(File folder, String[] filePatterns, Dataset dataset, DatasetTypeUnit formatUnit,
            Datasource datasource) throws ImporterException {
        new FileVerifier().shouldHaveOneFile(filePatterns);
        this.file = new File(folder, filePatterns[0]);
        this.dataset = dataset;
        this.formatUnit = formatUnit;
        this.datasource = datasource;
        if (System.getProperty("os.name").toUpperCase().startsWith("WINDOWS"))
            windowsOS = true;

        dataTable = new DataTable(dataset, datasource);
        loader = new DatasetLoader(dataset);
        loader.internalSource(file, dataTable.name(), formatUnit.tableFormat());
    }

    public void run() throws ImporterException {
        importAttributes(file, dataset);
        dataTable.create(formatUnit.tableFormat(), dataset.getId());
        try {
            doImport(file, dataset, dataTable.name(), (FileFormatWithOptionalCols) formatUnit.fileFormat());
            
        } catch (Exception e) {
            dataTable.drop();
            e.printStackTrace();
            throw new ImporterException("Filename: " + file.getAbsolutePath() + "; Exception: " + e.getMessage());
        }
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

    private void doImport(File file, Dataset dataset, String table, FileFormatWithOptionalCols fileFormat)
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

            String copyString = "COPY " + getFullTableName(table) + " (" + getColNames(fileFormat) + ") FROM '"
                    + putEscape(dataFile.getAbsolutePath()) + "' WITH CSV QUOTE AS '\"'";

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

    private String getColNames(FileFormatWithOptionalCols fileFormat) throws ImporterException {
        Column[] cols = fileFormat.cols();
        String colsString = "";

        try {
            for (int i = 0; i < record.size(); i++)
                colsString += cols[i].name() + ",";
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new ImporterException("Data doesn't match file format on number of columns " + "(expected:"
                    + cols.length + " but was:" + record.size() + ").");
        }

        return colsString.substring(0, colsString.length() - 1);
    }

    private boolean hasColName(String colName, FileFormatWithOptionalCols fileFormat) {
        Column[] cols = fileFormat.cols();
        boolean hasIt = false;
        for (int i = 0; i < cols.length; i++)
            if (colName.equalsIgnoreCase(cols[i].name())) hasIt = true;

        return hasIt;
    }

    private void loadDataset(List<String> comments, Dataset dataset) {
        String tempResltn = dataset.getTemporalResolution();

        if (tempResltn == null || tempResltn.trim().isEmpty())
            dataset.setTemporalResolution(TemporalResolution.ANNUAL.getName());
        
        dataset.setUnits("short tons/year");
        dataset.setDescription(new Comments(comments).all());
    }

    private void importAttributes(File file, Dataset dataset) throws ImporterException {
        DelimiterIdentifyingFileReader reader = null;
        try {
            // FIXME: move 'minCols' to FileFormat
            reader = new DelimiterIdentifyingFileReader(file, ((FileFormatWithOptionalCols) formatUnit.fileFormat())
                    .minCols().length);
            record = reader.read();

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

        addAttributes(reader.comments(), dataset);
    }

    private void addAttributes(List<String> commentsList, Dataset dataset) throws ImporterException {
        Comments comments = new Comments(commentsList);
        if (!comments.hasRightTagFormat("ORL"))
            throw new ImporterException("The first line of ORL files must start with #ORL.");

        if (!comments.hasContent("COUNTRY"))
            throw new ImporterException("The tag - 'COUNTRY' is mandatory.");
        // BUG: Country should not be created, but looked up
        // String country = comments.content("COUNTRY");
        // dataset.setCountry(new Country(country));

        if (!comments.hasContent("YEAR"))
            throw new ImporterException("The tag - 'YEAR' is mandatory.");
        
        int year = Integer.parseInt(comments.content("YEAR"));
        
        if (year >= 2200)
            throw new ImporterException("Invalid ORL Year: " + year + " ( >= 2200 ).");
            
        dataset.setYear(year);

        if (!comments.hasContent("EMF_START_DATE") && !comments.hasContent("EMF_END_DATE"))
            setStartStopDateTimes(dataset, year);
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
        //ignore the ORL Fire Type
        if (dataset.getDatasetType().getName().equals(DatasetType.ORL_FIRE_INVENTORY)) return;
        
        ResultSet rs = null;
        Connection connection = null;
        Statement statement = null;
        boolean hasRpenColumn = hasColName("rpen",(FileFormatWithOptionalCols) formatUnit.fileFormat());
        boolean hasMactColumn = hasColName("mact",(FileFormatWithOptionalCols) formatUnit.fileFormat());
        boolean hasSicColumn = hasColName("sic",(FileFormatWithOptionalCols) formatUnit.fileFormat());
        boolean hasCpriColumn = hasColName("cpri",(FileFormatWithOptionalCols) formatUnit.fileFormat());
        boolean hasPrimaryDeviceTypeCodeColumn = hasColName("primary_device_type_code",(FileFormatWithOptionalCols) formatUnit.fileFormat());
        boolean hasPointColumns = hasColName("pointid",(FileFormatWithOptionalCols) formatUnit.fileFormat());
        try {
//            first lets clean up -9 values and convert them to null values...
//            ann_emis
//            avd_emis
//            ceff
//            reff
//            rpen
//            mact
//            

            //check to see if -9 even shows for any of the columns in the inventory
            String sql = "select 1 " 
                    + " from " + getFullTableName(dataTable.name()) 
                    + " where dataset_id = " + dataset.getId()
                    + " and (" 
                    + " ann_emis = -9.0 " 
                    + " or avd_emis = -9.0" 
                    + " or ceff = -9.0" 
                    + " or reff = -9.0" 
                    + " or strpos(substr(fips, 1, 1) || substr(fips, length(fips), 1), ' ') > 0" 
                    + " or strpos(substr(scc, 1, 1) || substr(scc, length(scc), 1), ' ') > 0" 
                    + " or strpos(substr(poll, 1, 1) || substr(poll, length(poll), 1), ' ') > 0" 
                    + " or strpos(substr(control_measures, 1, 1) || substr(control_measures, length(control_measures), 1), ' ') > 0" 
                    + " or strpos(substr(pct_reduction, 1, 1) || substr(pct_reduction, length(pct_reduction), 1), ' ') > 0" 
                    + (hasPointColumns ? 
                            " or strpos(substr(plantid, 1, 1) || substr(plantid, length(plantid), 1), ' ') > 0" 
                            + " or strpos(substr(pointid, 1, 1) || substr(pointid, length(pointid), 1), ' ') > 0" 
                            + " or strpos(substr(stackid, 1, 1) || substr(stackid, length(stackid), 1), ' ') > 0" 
                            + " or strpos(substr(segment, 1, 1) || substr(segment, length(segment), 1), ' ') > 0" 
                            + " or strpos(substr(design_capacity_unit_numerator, 1, 1) || substr(design_capacity_unit_numerator, length(design_capacity_unit_numerator), 1), ' ') > 0" 
                            + " or strpos(substr(design_capacity_unit_denominator, 1, 1) || substr(design_capacity_unit_denominator, length(design_capacity_unit_denominator), 1), ' ') > 0" 
                            + " or design_capacity = -9.0" 
                            + " or stkflow = -9.0" 
                            + " or ANNUAL_AVG_HOURS_PER_YEAR = -9.0" 
                            : " ")
                    + (hasRpenColumn ? " or rpen = -9.0" : " ")
                    + (hasMactColumn ? " or trim(mact) = '-9' or strpos(substr(mact, 1, 1) || substr(mact, length(mact), 1), ' ') > 0" : " ")
                    + (hasSicColumn ? " or trim(sic) = '-9' or strpos(substr(sic, 1, 1) || substr(sic, length(sic), 1), ' ') > 0" : " ")
                    + (hasCpriColumn ? " or cpri = -9.0" : " ")
                    + (hasPrimaryDeviceTypeCodeColumn ? " or trim(primary_device_type_code) = '-9' or strpos(substr(primary_device_type_code, 1, 1) || substr(primary_device_type_code, length(primary_device_type_code), 1), ' ') > 0" : " ")
                    +  ") limit 1;";
            
            connection = datasource.getConnection();
            statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            rs = statement.executeQuery(sql);
            boolean foundNegative9 = false;
            while (rs.next()) {
                foundNegative9 = true;
            }

            if (foundNegative9) {
                sql = "update " + getFullTableName(dataTable.name()) 
                    + " set ann_emis = case when ann_emis = -9.0 then null::double precision else ann_emis end " 
                    + "     ,avd_emis = case when avd_emis = -9.0 then null::double precision else avd_emis end " 
                    + "     ,ceff = case when ceff = -9.0 then null::double precision else ceff end " 
                    + "     ,reff = case when reff = -9.0 then null::double precision else reff end " 
                    + " ,fips = case when strpos(substr(fips, 1, 1) || substr(fips, length(fips), 1), ' ') > 0 then trim(fips) else fips end " 
                    + " ,scc = case when strpos(substr(scc, 1, 1) || substr(scc, length(scc), 1), ' ') > 0 then trim(scc) else scc end " 
                    + " ,poll = case when strpos(substr(poll, 1, 1) || substr(poll, length(poll), 1), ' ') > 0 then trim(poll) else poll end " 
                    + " ,control_measures = case when strpos(substr(control_measures, 1, 1) || substr(control_measures, length(control_measures), 1), ' ') > 0 then trim(control_measures) else control_measures end " 
                    + " ,pct_reduction = case when strpos(substr(pct_reduction, 1, 1) || substr(pct_reduction, length(pct_reduction), 1), ' ') > 0 then trim(pct_reduction) else pct_reduction end " 
                    + (hasPointColumns ? 
                            " ,plantid = case when strpos(substr(plantid, 1, 1) || substr(plantid, length(plantid), 1), ' ') > 0 then trim(plantid) else plantid end " 
                            + " ,pointid = case when strpos(substr(pointid, 1, 1) || substr(pointid, length(pointid), 1), ' ') > 0 then trim(pointid) else pointid end " 
                            + " ,stackid = case when strpos(substr(stackid, 1, 1) || substr(stackid, length(stackid), 1), ' ') > 0 then trim(stackid) else stackid end " 
                            + " ,segment = case when strpos(substr(segment, 1, 1) || substr(segment, length(segment), 1), ' ') > 0 then trim(segment) else segment end " 
                            + " ,design_capacity_unit_numerator = case when strpos(substr(design_capacity_unit_numerator, 1, 1) || substr(design_capacity_unit_numerator, length(design_capacity_unit_numerator), 1), ' ') > 0 then trim(design_capacity_unit_numerator) else design_capacity_unit_numerator end " 
                            + " ,design_capacity_unit_denominator = case when strpos(substr(design_capacity_unit_denominator, 1, 1) || substr(design_capacity_unit_denominator, length(design_capacity_unit_denominator), 1), ' ') > 0 then trim(design_capacity_unit_denominator) else design_capacity_unit_denominator end " 
                            + " ,design_capacity = case when design_capacity = -9.0 then null::double precision else design_capacity end "
                            + " ,stkflow = case when stkflow = -9.0 then null::double precision else stkflow end "
                            + " ,ANNUAL_AVG_HOURS_PER_YEAR = case when stkflow = -9.0 then null::double precision else ANNUAL_AVG_HOURS_PER_YEAR end "
                            : " ")
                    + (hasRpenColumn ? "     ,rpen = case when rpen = -9.0 then null::double precision else rpen end " : " ")
                    + (hasMactColumn ? "     ,mact = case when trim(mact) = '-9' then null::character varying(4) when strpos(substr(mact, 1, 1) || substr(mact, length(mact), 1), ' ') > 0 then trim(mact) else mact end " : " ")
                    + (hasSicColumn ? "     ,sic = case when trim(sic) = '-9' then null::character varying(4) when strpos(substr(sic, 1, 1) || substr(sic, length(sic), 1), ' ') > 0 then trim(sic) else sic end " : " ") 
                    + (hasCpriColumn ? "     ,cpri = case when cpri = -9.0 then null::integer else cpri end " : " ") 
                    + (hasPrimaryDeviceTypeCodeColumn ? "     ,primary_device_type_code = case when trim(primary_device_type_code) = '-9' then null::character varying(4) when strpos(substr(primary_device_type_code, 1, 1) || substr(primary_device_type_code, length(primary_device_type_code), 1), ' ') > 0 then trim(primary_device_type_code) else primary_device_type_code end " : " ") 
                    + " where dataset_id = " + dataset.getId()
                    + " and (" 
                    + " ann_emis = -9.0 " 
                    + " or avd_emis = -9.0" 
                    + " or ceff = -9.0" 
                    + " or reff = -9.0" 
                    + " or strpos(substr(fips, 1, 1) || substr(fips, length(fips), 1), ' ') > 0" 
                    + " or strpos(substr(scc, 1, 1) || substr(scc, length(scc), 1), ' ') > 0" 
                    + " or strpos(substr(poll, 1, 1) || substr(poll, length(poll), 1), ' ') > 0" 
                    + " or strpos(substr(control_measures, 1, 1) || substr(control_measures, length(control_measures), 1), ' ') > 0" 
                    + " or strpos(substr(pct_reduction, 1, 1) || substr(pct_reduction, length(pct_reduction), 1), ' ') > 0" 
                    + (hasPointColumns ? 
                            " or strpos(substr(plantid, 1, 1) || substr(plantid, length(plantid), 1), ' ') > 0" 
                            + " or strpos(substr(pointid, 1, 1) || substr(pointid, length(pointid), 1), ' ') > 0" 
                            + " or strpos(substr(stackid, 1, 1) || substr(stackid, length(stackid), 1), ' ') > 0" 
                            + " or strpos(substr(segment, 1, 1) || substr(segment, length(segment), 1), ' ') > 0" 
                            + " or strpos(substr(design_capacity_unit_numerator, 1, 1) || substr(design_capacity_unit_numerator, length(design_capacity_unit_numerator), 1), ' ') > 0" 
                            + " or strpos(substr(design_capacity_unit_denominator, 1, 1) || substr(design_capacity_unit_denominator, length(design_capacity_unit_denominator), 1), ' ') > 0" 
                            + " or design_capacity = -9.0" 
                            + " or stkflow = -9.0" 
                            + " or ANNUAL_AVG_HOURS_PER_YEAR = -9.0" 
                            : " ")
                    + (hasRpenColumn ? " or rpen = -9.0" : " ")
                    + (hasMactColumn ? " or trim(mact) = '-9' or strpos(substr(mact, 1, 1) || substr(mact, length(mact), 1), ' ') > 0" : " ")
                    + (hasSicColumn ? " or trim(sic) = '-9' or strpos(substr(sic, 1, 1) || substr(sic, length(sic), 1), ' ') > 0" : " ")
                    + (hasCpriColumn ? " or cpri = -9.0" : " ")
                    + (hasPrimaryDeviceTypeCodeColumn ? " or trim(primary_device_type_code) = '-9' or strpos(substr(primary_device_type_code, 1, 1) || substr(primary_device_type_code, length(primary_device_type_code), 1), ' ') > 0" : " ")
                    +  ");";
                
                
//                sic = case when sic is null or trim(sic) = ''0'' or trim(sic) = ''-9'' or trim(sic) = '''' then null::character varying(4) else sic end 
                
//            update emissions.' || control_program.table_name || '
//            set plantid = case when plantid is null or trim(plantid) = ''0'' or trim(plantid) = ''-9'' or trim(plantid) = '''' then null::character varying(15) else plantid end,
//                pointid = case when pointid is null or trim(pointid) = ''0'' or trim(pointid) = ''-9'' or trim(pointid) = '''' then null::character varying(15) else pointid end,
//                stackid = case when stackid is null or trim(stackid) = ''0'' or trim(stackid) = ''-9'' or trim(stackid) = '''' then null::character varying(15) else stackid end,
//                segment = case when segment is null or trim(segment) = ''0'' or trim(segment) = ''-9'' or trim(segment) = '''' then null::character varying(15) else segment end,
//                fips = case when fips is null or trim(fips) = ''0'' or trim(fips) = ''-9'' or trim(fips) = '''' then null::character varying(6) else fips end,
//                scc = case when scc is null or trim(scc) = ''0'' or trim(scc) = ''-9'' or trim(scc) = '''' then null::character varying(10) else scc end,
//                poll = case when poll is null or trim(poll) = ''0'' or trim(poll) = ''-9'' or trim(poll) = '''' then null::character varying(16) else poll end,
//--                  mact = case when mact is null or trim(mact) = ''0'' or trim(mact) = ''-9'' or trim(mact) = '''' then null::character varying(6) else mact end,
//                sic = case when sic is null or trim(sic) = ''0'' or trim(sic) = ''-9'' or trim(sic) = '''' then null::character varying(4) else sic end 
//            where trim(plantid) in (''0'',''-9'','''')
//                or trim(pointid) in (''0'',''-9'','''')
//                or trim(stackid) in (''0'',''-9'','''')
//                or trim(segment) in (''0'',''-9'','''')
//                or trim(fips) in (''0'',''-9'','''')
//                or trim(scc) in (''0'',''-9'','''')
//                or trim(poll) in (''0'',''-9'','''')
//--                  or trim(mact) in (''0'',''-9'','''')
//                or trim(sic) in (''0'',''-9'','''');'
                statement.execute(sql);
                statement.execute("vacuum " + getFullTableName(dataTable.name()));
                statement.close();
            }
            
            //create indexes....
            createIndexes();
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
            throw new ImporterException(exc.getMessage());
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) { /**/
                }
                rs = null;
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) { /**/
                }
                statement = null;
            }
        }
    }

//    private void makeSureInventoryDatasetHasIndexes() {
//        String table = dataTable.name().toLowerCase();
//        String query = "SELECT public.create_orl_table_indexes('" + table + "');analyze " + getFullTableName(table).toLowerCase() + ";";
//        try {
//            datasource.query().execute(query);
//        } catch (SQLException e) {
//            //e.printStackTrace();
//            //supress all errors, the indexes might already be on the table...
//        } finally {
//            //
//        }
//    }
    

    private void createIndexes() {
        String table = dataTable.name().toLowerCase();

        //ALWAYS create indexes for these core columns...
        dataTable.addIndex(table, "record_id", true);
        dataTable.addIndex(table, "dataset_id", false);
        dataTable.addIndex(table, "version", false);
        dataTable.addIndex(table, "delete_versions", false);

        dataTable.addIndex(table, "fips", false);
        dataTable.addIndex(table, "poll", false);
        dataTable.addIndex(table, "scc", false);
        dataTable.addIndex(table, "plantid", false);
        dataTable.addIndex(table, "pointid", false);
        dataTable.addIndex(table, "stackid", false);
        dataTable.addIndex(table, "segment", false);
        dataTable.addIndex(table, "mact", false);
        dataTable.addIndex(table, "sic", false);

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

    private  KeyVal[] keyValFound(String keyword) {
        KeyVal[] keys = dataset.getDatasetType().getKeyVals();
        List<KeyVal> list = new ArrayList<KeyVal>();
        
        for (KeyVal key : keys)
            if (key.getName().equalsIgnoreCase(keyword)) 
                list.add(key);
        
        return list.toArray(new KeyVal[0]);
    }
}
