package gov.epa.emissions.framework.services.qa;

import gov.epa.emissions.commons.data.PivotConfiguration;
import gov.epa.emissions.commons.data.ProjectionShapeFile;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.postgres.PostgresSQLToShapeFile;
import gov.epa.emissions.commons.db.version.Versions;
import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.commons.io.ExporterException;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.EmfProperty;
import gov.epa.emissions.framework.services.basic.FileDownloadDAO;
import gov.epa.emissions.framework.services.basic.Status;
import gov.epa.emissions.framework.services.basic.StatusDAO;
import gov.epa.emissions.framework.services.data.DatasetDAO;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.data.QAStepResult;
import gov.epa.emissions.framework.services.persistence.EmfPropertiesDAO;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.io.File;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;

public class ExportShapeFileQAStepTask implements Runnable {

    private QAStep qastep;

    private User user;

    private StatusDAO statusDao;

    private Log log = LogFactory.getLog(ExportShapeFileQAStepTask.class);

    private File file;

    private HibernateSessionFactory sessionFactory;

    private QAStepResult result;

    private String dirName;
    
    private String fileName;
    
    private boolean overide;

    private boolean verboseStatusLogging = true;

    private DbServerFactory dbServerFactory;

    private ProjectionShapeFile projectionShapeFile;

    private PivotConfiguration pivotConfiguration;

    private Column[] columns;

    private String rowFilter;
    
    private String[] validCountyFields = new String[] { "fips", "region_cd" };
    private String[] validStateFields = new String[] { "fipsst" };
    private String[] validLatitudeFields = new String[] { "fipsst" };
    private String[] validLongitudeFields = new String[] { "fipsst" };

    private boolean download;

    private FileDownloadDAO fileDownloadDao;

    
    public ExportShapeFileQAStepTask(String dirName, String fileName, 
            boolean overide, QAStep qaStep,
            User user, HibernateSessionFactory sessionFactory, 
            DbServerFactory dbServerFactory, ProjectionShapeFile projectionShapeFile, 
            boolean verboseStatusLogging, String rowFilter, 
            PivotConfiguration pivotConfiguration) throws EmfException {
        this.dirName = dirName;
        this.fileName = fileName;
        this.overide = overide;
        this.qastep = qaStep;
        this.user = user;
        this.sessionFactory = sessionFactory;
        this.statusDao = new StatusDAO(sessionFactory);
        this.fileDownloadDao = new FileDownloadDAO(sessionFactory);
        this.dbServerFactory = dbServerFactory;
        this.projectionShapeFile = projectionShapeFile;
        this.verboseStatusLogging = verboseStatusLogging;
        this.rowFilter = rowFilter;
        this.pivotConfiguration = pivotConfiguration;
        this.validCountyFields = getProperty(EmfProperty.POSTGIS_COUNTY_FIELDS).split(",");
        this.validStateFields = getProperty(EmfProperty.POSTGIS_STATE_FIELDS).split(",");
        this.validLatitudeFields = getProperty(EmfProperty.POSTGIS_LATITUDE_FIELDS).split(",");
        this.validLongitudeFields = getProperty(EmfProperty.POSTGIS_LONGITUDE_FIELDS).split(",");
    }

    public ExportShapeFileQAStepTask(String dirName, String fileName, 
            boolean overide, QAStep qaStep,
            User user, HibernateSessionFactory sessionFactory, 
            DbServerFactory dbServerFactory, ProjectionShapeFile projectionShapeFile, 
            boolean verboseStatusLogging, String rowFilter, 
            PivotConfiguration pivotConfiguration, boolean download) throws EmfException {
        this(dirName, fileName, 
            overide, qaStep,
            user, sessionFactory, 
            dbServerFactory, projectionShapeFile, 
            verboseStatusLogging, rowFilter, 
            pivotConfiguration);
        this.download = download;
    }
    
    public void run() {
        
        String suffix = "";
        DbServer dbServer = dbServerFactory.getDbServer();
        try {
            getStepResult();
            file = exportFile(dirName);
            PostgresSQLToShapeFile exporter = new PostgresSQLToShapeFile(dbServer);
            // Exporter exporter = new DatabaseTableCSVExporter(result.getTable(), dbServer.getEmissionsDatasource(),
            // batchSize(sessionFactory));
            suffix = suffix();
            prepare(suffix);
            if (projectionShapeFile == null)
                throw new ExporterException("The projection/shape file is missing.");
            exporter.create(getProperty("postgres-bin-dir"), getProperty("postgres-db"), getProperty("postgres-user"),
                    getProperty("pgsql2shp-info"), file.getAbsolutePath(), overide, prepareSQLStatement(), projectionShapeFile);

            //add to download queue if required...
            if (download) {
                for (File f : getShapefiles(file.getName())) {
                    //lets add shapefiles (remember there are multiple files to worry about prj, dbf, etc...) 
                    //for the user to download
                    fileDownloadDao.add(user, new Date(), f.getName(), "QA Step - Shapefile", overide);
                }
            }
            complete(suffix);
        } catch (Exception e) {
            logError("Failed to export QA step : " + qastep.getName() + suffix, e);
            setStatus("Failed to export QA step " + qastep.getName() + suffix + ". Reason: " + e.getMessage());
        } finally {
            if (dbServer != null)
                try {
                    dbServer.disconnect();
                } catch (Exception e) {
                    // NOTE Auto-generated catch block
                    e.printStackTrace();
                }
        }
    }

    private String getProperty(String propertyName) {
        Session session = sessionFactory.getSession();
        try {
            EmfProperty property = new EmfPropertiesDAO().getProperty(propertyName, session);
            return property.getValue();
        } finally {
            session.close();
        }
    }

    private short getDbfFieldWidth(Map<String, Short> fieldWidthMap, String sqlType) {
        sqlType = sqlType.toLowerCase();

        if (sqlType.startsWith("varchar") 
                || sqlType.startsWith("text")
                || sqlType.startsWith("char")
                )
            return fieldWidthMap.get("text");

        if (
                sqlType.toLowerCase().startsWith("real")
                || sqlType.toLowerCase().startsWith("double precision")
                || sqlType.toLowerCase().startsWith("float")
                || sqlType.toLowerCase().startsWith("numeric")
                )
            return fieldWidthMap.get("double");
        
        if (sqlType.toLowerCase().startsWith("date") 
                || sqlType.toLowerCase().startsWith("timestamp without time zone")
                || sqlType.toLowerCase().trim().equals("timestamp"))
            return fieldWidthMap.get("date");
        
        if (sqlType.startsWith("int4"))
            return fieldWidthMap.get("bigint");
        
        if (sqlType.startsWith("int2"))
            return fieldWidthMap.get("int");
        
        if (sqlType.startsWith("int"))
            return fieldWidthMap.get("bigint");
        
        return fieldWidthMap.get("text");
    }
    
    
    private String prepareSQLStatement() throws ExporterException {
        // perform some validation to make sure resultset meets standards of the shapefile dbf limitations
        // see http://webhelp.esri.com/arcgisdesktop/9.3/index.cfm?TopicName=Geoprocessing_considerations_for_shapefile_output\\
        // From above link:
        //
        // Field names cannot be longer than 10 characters.
        // The maximum record length for an attribute is 4,000 bytes. The record length is the number of bytes used to define all the fields, not the number of bytes used to store the actual values.
        // The maximum number of fields is 255. A conversion to shapefile will convert the first 255 fields if this limit is exceeded.
        // The dBASE file must contain at least one field. When you create a new shapefile or dBASE table, an integer ID field is created as a default. 
        //
        // DBF Datatype Width Definitions...
        // Geodatabase Data Type   dBASE Field Type    dBASE Field Width (number of characters)
        // Object ID   Number  9
        // Short Integer   Number  4
        // Long Integer    Number  9
        // Float   Float   13
        // Double  Float   13
        // Text    Character   254
        // Date    Date    8
        Map<String, Short> fieldWidthMap = new HashMap<String, Short>();
        fieldWidthMap.put("int", (short)4);
        fieldWidthMap.put("bigint", (short)9);
        fieldWidthMap.put("float", (short)13);
        fieldWidthMap.put("double", (short)13);
        fieldWidthMap.put("text", (short)254);
        fieldWidthMap.put("date", (short)8);
        

        
        DbServer dbServer = null;
        boolean hasFipsCol = false;
        String fipsCol = "";
        boolean hasFipsStCol = false;
        String fipsStCol = "";
//        boolean hasPlantIdCol = false;
//        boolean hasLatCol = false;
//        boolean hasLonCol = false;
        boolean hasLatitudeCol = false;
        String latitudeCol = "";
        boolean hasLongitudeCol = false;
        String longitudeCol = "";
//        boolean hasXLocCol = false;
//        boolean hasYLocCol = false;
        
        
        // will hold unique list of column names, pqsql2shp doesn't like multiple columns with the same...
        Map<String, String> cols = new HashMap<String, String>();
        String colNames = "";
        String sql = "";
        String qaStepSQL = "";
        int dbfFieldWidthCount = fieldWidthMap.get("int"); //account for integer column that is added by shapefile exporter

        try {
            dbServer = dbServerFactory.getDbServer();
            this.columns = dbServer.getEmissionsDatasource().dataModifier().getColumns(result.getTable());
            
            
            //if pivot config was specified then create pivot SQL... 
            if (pivotConfiguration != null) 
                qaStepSQL = createPivotSQL();
            
            
            ResultSet rs = dbServer.getEmissionsDatasource().query().executeQuery(
                    (pivotConfiguration != null ? qaStepSQL : "select * from emissions." + result.getTable()) + " where 1 = 0");
            ResultSetMetaData md = rs.getMetaData();
            int columnCount = md.getColumnCount();

            //see how many columns, if more than 255 throw error, the dbf won't support for more than 255 fields.
            //will do a check on the width/bytes of a record, which can't exceed 4,000 bytes
            if (columnCount + 1 > 255) 
                throw new ExporterException("There are too many columns in the resultset, " + (columnCount + 1) + " columns.  The shapefile dbf specification only allows for 255 fields.");
            

            String colName = "";
            String colType = "";
            for (int i = 1; i <= columnCount; i++) {
                colName = md.getColumnName(i);
                colType = md.getColumnTypeName(i);
                dbfFieldWidthCount += getDbfFieldWidth(fieldWidthMap, colType);
//                System.out.println("getDbfFieldWidth colName=" + colName + " sqlType=" + colType + " dbfFieldWidthCount=" + dbfFieldWidthCount); 
                
                if (!cols.containsKey(colName)) {
                    cols.put(colName, "qa." + colName);
                    colNames += (colNames.length() > 0 ? "," : "") + "qa.\"" + colName + "\"";
                }
                if (!hasFipsCol) {
                    for (String fipsField : validCountyFields) {
                        if (findColumn(colName, fipsField)) {
                            hasFipsCol = true;
                            fipsCol = colName;
                            break;
                        }
                    }
                }
                if (!hasFipsStCol) {
                    for (String fipsStField : validStateFields) {
                        if (findColumn(colName, fipsStField)) {
                            hasFipsStCol = true;
                            fipsStCol = colName;
                            break;
                        }
                    }
                }
                if (!hasLatitudeCol) {
                    for (String latitudeField : validLatitudeFields) {
                        if (findColumn(colName, latitudeField)) {
                            hasLatitudeCol = true;
                            latitudeCol = colName;
                            break;
                        }
                    }
                }
                if (!hasLongitudeCol) {
                    for (String longitudeField : validLongitudeFields) {
                        if (findColumn(colName, longitudeField)) {
                            hasLongitudeCol = true;
                            longitudeCol = colName;
                            break;
                        }
                    }
                }
            }


            if (
                (hasFipsCol && projectionShapeFile.getType().equals("county")) 
                || (hasFipsStCol && projectionShapeFile.getType().equals("state"))
            ) {
                rs = dbServer.getEmissionsDatasource().query().executeQuery(
                        "select * from " + projectionShapeFile.getTableSchema() + "."
                                + projectionShapeFile.getTableName() + " where 1 = 0");
                md = rs.getMetaData();
                columnCount = md.getColumnCount();
                for (int i = 1; i <= columnCount; i++) {
                    colName = md.getColumnName(i);
                    colType = md.getColumnTypeName(i);
                    if (!cols.containsKey(colName)) {
                        cols.put(colName, "sh." + colName);
                        colNames += (colNames.length() > 0 ? "," : "") + "sh.\"" + colName + "\"";
                        //ignore geometry column, this will be stored in a different part of shapefile
                        if (!colType.equals("geometry"))
                            dbfFieldWidthCount += getDbfFieldWidth(fieldWidthMap, colType);
//                        System.out.println("getDbfFieldWidth colName=" + colName + " sqlType=" + colType + " dbfFieldWidthCount=" + dbfFieldWidthCount); 
                    }
                }
            }
            
        } catch (SQLException e) {
            throw new ExporterException(e.getMessage());
        } finally {
            if (dbServer != null)
                try {
                    dbServer.disconnect();
                } catch (Exception e) {
                    // NOTE Auto-generated catch block
                    e.printStackTrace();
                }
        }
        // Make sure the right projection/shape file was specified
//        // Also, make sure the point query has enough info to proceed...
//         if (hasPlantIdCol) {
//             if (
//             !(
//                 (hasLongitudeCol && hasLatitudeCol)
//                 || (hasLonCol && hasLatCol)
//                 || (hasXLocCol && hasYLocCol)
//             )) 
//                 throw new ExporterException("The point query must have latitude and longitude columns.");
        // county level...
        if (hasFipsCol 
                && !(
              (hasLongitudeCol && hasLatitudeCol)
                )
          ) {
            if (!projectionShapeFile.getType().equals("county"))
                throw new ExporterException("A county-level projection/shape file must be used.");
            // state level...
        } else if (hasFipsStCol
                && !(
                        (hasLongitudeCol && hasLatitudeCol)
                          )
                ) {
            if (!projectionShapeFile.getType().equals("state"))
                throw new ExporterException("A state-level projection/shape file must be used.");
        }

        // build the sql select statement using the specified projection/shape file
        // county level...
        if (
                (hasLongitudeCol && hasLatitudeCol) 
                ) {
            sql = "select "
                    + colNames
                    + ", ST_GeomFromText('POINT(' || "
                    + ("" + longitudeCol  + " || ' ' || " + latitudeCol  + "") + "|| ')', "
                    + projectionShapeFile.getSrid() + ") as the_geom" + " from " + (pivotConfiguration != null ? "(" + qaStepSQL +")" : "(select * from emissions." + result.getTable()+ (rowFilter == null || rowFilter.isEmpty() ? "" : " where " + rowFilter + "") + ")") + " qa";
        } else if (hasFipsCol) {
            sql = "select " + colNames + " from " + (pivotConfiguration != null ? "(" + qaStepSQL +")" : "(select * from emissions." + result.getTable()+ (rowFilter == null || rowFilter.isEmpty() ? "" : " where " + rowFilter + "") + ")") + " qa" + " inner join "
                    + projectionShapeFile.getTableSchema() + "." + projectionShapeFile.getTableName() + " sh"
                    + " on sh.fips = qa." + fipsCol  + "";
            // state level...
        } else if (hasFipsStCol) {
            sql = "select " + colNames + " from " + (pivotConfiguration != null ? "(" + qaStepSQL +")" : "(select * from emissions." + result.getTable()+ (rowFilter == null || rowFilter.isEmpty() ? "" : " where " + rowFilter + "") + ")") + " qa" + " inner join "
                    + projectionShapeFile.getTableSchema() + "." + projectionShapeFile.getTableName() + " sh"
                    + " on sh.statefp = qa." + fipsStCol  + "";
            // point level...
        } else {
            throw new ExporterException(
                    "QA result does not have a fips, fips state code, or plantid/latitude/longitude columns.");
        }

        //see if if the width/bytes of the record, will exceed the 4,000 bytes per record maximum limitation
        if (dbfFieldWidthCount >= 4000 * 0.95) 
            throw new ExporterException("There are to many bytes allocated per record in the resultset.  The shapefile dbf specification only allows for 4,000 bytes per record.");

        System.out.println(sql);
        return sql;// + (pivotConfiguration != null ? "" : " where " + pollCol + " = '" + poll + "'");
    }

    private String createPivotSQL() throws ExporterException {
        String rowName = "";
        for (String name : pivotConfiguration.getRowLabels())
            rowName += (rowName.length() > 0 ? " || '_' || " : "") + "coalesce(\"" + name + "\" || '','')";
        String extraFieldsList = "";
        //add row labels to extra fields section so they come across in resultset
        for (String name : pivotConfiguration.getRowLabels())
            extraFieldsList += (extraFieldsList.length() > 0 ? ", " : "") + "\"" + name + "\"";
        for (String name : pivotConfiguration.getExtraFields())
            extraFieldsList += (extraFieldsList.length() > 0 ? ", " : "") + "\"" + name + "\"";
        
        StringBuffer sql = new StringBuffer();
        sql.append("WITH extra_fields as ( \n" +
            "   SELECT distinct on (" + rowName + ") \n" +
            "       " + rowName + " as row_name, \n" + 
            "       " + extraFieldsList + " \n" +
            "   FROM emissions." + result.getTable() + " \n" + 
            (rowFilter == null || rowFilter.isEmpty() ? "" : " where " + rowFilter + "") + " \n" +
            ") \n"
            ); 
 
        int pivotNumber = 1;
        Map<String, String[]> categoryValuesMap = new HashMap<String, String[]>();
        for (String category : pivotConfiguration.getColumnLabels()) {
            String[] categoryValues = getCategoryValues(category);
            categoryValuesMap.put(category, categoryValues);
            for (String value : pivotConfiguration.getValues()) {
                sql.append(", pivot" + pivotNumber + " as ( \n" +
                        createCategoryValuePivotSQL(category, value, categoryValues, pivotConfiguration.getSummarizeValueBy()) + 
                        ") \n"
                        ); 
                ++pivotNumber;
            }
        }

        sql.append("select  ");
        for (String name : pivotConfiguration.getRowLabels())
            sql.append("extra_fields.\"" + name + "\", \n");
        for (String name : pivotConfiguration.getExtraFields())
            sql.append("extra_fields.\"" + name + "\", \n");

        //reset for next pass
        pivotNumber = 1;
        for (String category : pivotConfiguration.getColumnLabels()) {
            String[] categoryValues = getCategoryValues(category);
            categoryValuesMap.put(category, categoryValues);
            for (String value : pivotConfiguration.getValues()) {
                
                for (String categoryValue : categoryValuesMap.get(category)) {
                    
                    sql.append("pivot" + pivotNumber + ".\"" + categoryValue + "\", \n"); 
                }
                ++pivotNumber;
            }
        }
        //get rid of last comma
        sql.delete(sql.length() - 3, sql.length());
        
        sql.append("\n"); 
        sql.append("from extra_fields \n"); 

        pivotNumber = 1;
        for (String category : pivotConfiguration.getColumnLabels()) {
            String[] categoryValues = getCategoryValues(category);
            categoryValuesMap.put(category, categoryValues);
            for (String value : pivotConfiguration.getValues()) {
                
                sql.append("    full join pivot" + pivotNumber + " \n"); 
                sql.append("    on pivot" + pivotNumber + ".row_name = extra_fields.row_name \n"); 
                ++pivotNumber;
            }
        }
        log.warn(sql.toString());
        return sql.toString();
    }

    private String createCategoryValuePivotSQL(String category, String value, String[] categoryValues, String summarizeBy) throws ExporterException {
        String rowName = "";
        for (String name : pivotConfiguration.getRowLabels())
            rowName += (rowName.length() > 0 ? " || \\'_\\' || " : "") + "coalesce(\"" + name + "\" || \\'\\',\\'\\')";
        String categortDataType = getColumnDataType(value);
        
        String sql = 
            "select *  \n" +
            "from crosstab(E' \n" +
            "SELECT " + rowName + " as row_name, \n" + 
            "   \"" + category + "\", \n" + 
            "   " + summarizeBy + "(\"" + value + "\") as \"" + value + "\" \n" + 
            "   \n" + 
            "FROM emissions." + result.getTable() + " \n" + 
            (rowFilter == null || rowFilter.isEmpty() ? "" : "where " + rowFilter.replaceAll("'", "\\\\'") + "") + " \n" +
            "group by \n" + 
            "   " + rowName + ", \n" +
            "   \"" + category + "\" \n" +
            "order by " + rowName + ", \"" + category + "\" \n" +
            " \n" +
            "',E'SELECT distinct \"" + category + "\" \n" +
            "FROM emissions." + result.getTable() + " \n" +
            (rowFilter == null || rowFilter.isEmpty() ? "" : "where " + rowFilter.replaceAll("'", "\\\\'") + "") + " \n" +
            "order by 1') as c( \n" +
            "   row_name character varying(1000) \n";
        for (String name : categoryValues) {
            sql += "   " + ",\"" + name + "\"" + " " + categortDataType + " \n";
        }
        sql += ")";
        
        return sql;
    }
    private String[] getCategoryValues(String category) throws ExporterException {

        List<String> categoryValueList = new ArrayList<String>();
        DbServer dbServer = null;
        try {
            dbServer = dbServerFactory.getDbServer();
            ResultSet rs = dbServer.getEmissionsDatasource().query().executeQuery(
                    "select distinct \"" + category + "\" from emissions."
                            + result.getTable() + 
                    (rowFilter == null || rowFilter.isEmpty() ? "" : " where " + rowFilter + "") + " \n" +
                    " order by \"" + category + "\"");
            
            while (rs.next()) {
                categoryValueList.add(rs.getString(1));
            }
            
        } catch (SQLException e) {
            throw new ExporterException(e.getMessage());
        } finally {
            if (dbServer != null)
                try {
                    dbServer.disconnect();
                } catch (Exception e) {
                    // NOTE Auto-generated catch block
                    e.printStackTrace();
                }
        }
        return categoryValueList.toArray(new String[0]);
    }

    private String getColumnDataType(String name) {
        String dataType = "";
        for (Column column : columns) {
            if (column.getName().equals(name)) {
                dataType = column.getSqlType();
                if (dataType.equals("VARCHAR"))
                    dataType += "(" + column.getWidth() + ")";
                return dataType;
            }
                
        }
        // NOTE Auto-generated method stub
        return null;
    }

    private void prepare(String suffixMsg) {
        if (verboseStatusLogging)
            setStatus("Started exporting QA step shapefile '" + qastep.getName() + "'" + suffixMsg);
    }

    private void complete(String suffixMsg) {
        if (verboseStatusLogging)
            setStatus("Completed exporting QA step shapefile '" + qastep.getName() + "'" + suffixMsg
                    + (download ? ".  The file will start downloading momentarily, see the Download Manager for the download status." : ""));
    }

    private void logError(String message, Exception e) {
        log.error(message, e);

    }

    private void setStatus(String message) {
        Status endStatus = new Status();
        endStatus.setUsername(user.getUsername());
        endStatus.setType("ExportQAStep");
        endStatus.setMessage(message);
        endStatus.setTimestamp(new Date());

        statusDao.add(endStatus);

    }

    private String suffix() {
        return " for Version '" + versionName() + "' of Dataset '" + datasetName() + "' to " + file.getAbsolutePath();
    }

    private String versionName() {
        Session session = sessionFactory.getSession();
        try {
            return new Versions().get(qastep.getDatasetId(), qastep.getVersion(), session).getName();
        } finally {
            session.close();
        }
    }

    private String datasetName() {
        Session session = sessionFactory.getSession();
        try {
            DatasetDAO dao = new DatasetDAO();
            return dao.getDataset(session, qastep.getDatasetId()).getName();
        } finally {
            session.close();
        }
    }

    private void getStepResult() throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            result = new QADAO().qaStepResult(qastep, session);
            if (result == null || result.getTable() == null)
                throw new EmfException("You have to first run the QA Step before export");
        } finally {
            session.close();
        }
    }

    private File exportFile(String dirName) throws EmfException {
        return new File(validateDir(dirName), fileName());
    }

    private File[] getShapefiles(String fileName) throws EmfException {
        File shapefileDirectory = validateDir(dirName);
        
        return new File[] {
            new File(shapefileDirectory, fileName + ".prj"),
            new File(shapefileDirectory, fileName + ".shp"),
            new File(shapefileDirectory, fileName + ".shx"),
            new File(shapefileDirectory, fileName + ".dbf")
        };
    }

    private String fileName() {
        if ( fileName == null || fileName.trim().length()==0)
            return result.getTable();
        return fileName;
    }

    private boolean findColumn(String actualColumnName, String columnNameToFind) {
        if (columnNameToFind.matches("(?i)(\"*)(" + actualColumnName + ")(\"*)"))
            return true;
        return false;
    }

    private File validateDir(String dirName) throws EmfException {
        File file = new File(dirName);

        //don't check if exists when downloading, just create
        if (download) {
            file.mkdir();
            file.setReadable(true, true);
            file.setWritable(true, false);
            return file;
        }

        if (!file.exists() || !file.isDirectory()) {
            log.error("Folder " + dirName + " does not exist");
            throw new EmfException("Folder does not exist: " + dirName);
        }
        return file;
    }
    
    public static void main(String[] args) {
        System.out.println("fips='37001'".replaceAll("'", "\\\\\\\\'"));
        System.out.println(        "fipss".matches("(?i)(\"*)(fips)(\"*)") );        
    }
}
