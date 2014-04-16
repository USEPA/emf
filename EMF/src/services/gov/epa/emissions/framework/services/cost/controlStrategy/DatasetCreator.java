package gov.epa.emissions.framework.services.cost.controlStrategy;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.data.KeyVal;
import gov.epa.emissions.commons.data.Keyword;
import gov.epa.emissions.commons.db.DataModifier;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.TableCreator;
import gov.epa.emissions.commons.db.TableModifier;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.commons.io.TableFormat;
import gov.epa.emissions.commons.io.VersionedQuery;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.DateUtil;
import gov.epa.emissions.framework.services.cost.ControlMeasureClass;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.data.DatasetDAO;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.Keywords;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;
import gov.epa.emissions.framework.tasks.DebugLevels;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.hibernate.Session;

public class DatasetCreator {

//    private String tablePrefix;

    private User user;

//    private String outputDatasetName;

    private HibernateSessionFactory sessionFactory;

    private DbServerFactory dbServerFactory;

//    private String datasetNamePrefix;
    
    private ControlStrategy controlStrategy;

    private Keywords keywordMasterList;
    
    private Datasource datasource;

    public DatasetCreator() {
        //
    }
    public DatasetCreator(ControlStrategy controlStrategy, User user, 
            HibernateSessionFactory sessionFactory, DbServerFactory dbServerFactory,
            Datasource datasource, Keywords keywordMasterList) {
//        this.datasetNamePrefix = datasetNamePrefix;
//        this.tablePrefix = tablePrefix;
        this.user = user;
        this.sessionFactory = sessionFactory;
        this.dbServerFactory = dbServerFactory;
//        this.outputDatasetName = getResultDatasetName(strategy.getName());
        this.controlStrategy = controlStrategy;
        this.datasource = datasource;
        this.keywordMasterList = keywordMasterList;//new Keywords(new DataCommonsServiceImpl(sessionFactory).getKeywords());
    }

    public EmfDataset addDataset(String datasetNamePrefix, String tablePrefix, 
            EmfDataset inputDataset, DatasetType type, 
            TableFormat tableFormat, String description) throws EmfException {
        String outputDatasetName = createResultDatasetName(datasetNamePrefix, inputDataset);
        String outputTableName = createTableName(tablePrefix, inputDataset);
        
        //create dataset
        EmfDataset dataset = createDataset(outputDatasetName, description, type, inputDataset);

        setDatasetInternalSource(dataset, outputTableName, 
                tableFormat, inputDataset.getName());

        //persist dataset to db
        add(dataset);
        try {
            addVersionZeroEntryToVersionsTable(dataset);
        } catch (Exception e) {
            throw new EmfException("Cannot add version zero entry to versions table for dataset: " + dataset.getName());
        }

        createTable(outputTableName, tableFormat);

        return dataset;
    }

    public EmfDataset addDataset(String tablePrefix, 
            String datasetName, DatasetType type, 
            TableFormat tableFormat, String description) throws EmfException {
        String outputTableName = createTableName(tablePrefix, datasetName);
        
        //create dataset
        EmfDataset dataset = createDataset(datasetName, description, type);

        setDatasetInternalSource(dataset, outputTableName, 
                tableFormat, datasetName);

        //persist dataset to db
        add(dataset);
        try {
            addVersionZeroEntryToVersionsTable(dataset);
        } catch (Exception e) {
            throw new EmfException("Cannot add version zero entry to versions table for dataset: " + dataset.getName());
        }

        createTable(outputTableName, tableFormat);

        return dataset;
    }

    public EmfDataset addDataset(String datasetName, // TODO: JIZHEN_0713
            EmfDataset inputDataset, DatasetType type, 
            TableFormat tableFormat, String description
//            ,Map<String,String> keywordValues
            ) throws EmfException {
//        return addDataset(datasetName, "DS", 
//                inputDataset, type, 
//                tableFormat, description);
        
        String outputDatasetName = datasetName;
        //check and see if this name is already being used, if so add a timestamp.
        if (isDatasetNameUsed(datasetName)) 
            outputDatasetName = createDatasetName(datasetName);

        String outputTableName = createTableName(datasetName);
        
        //create dataset
        EmfDataset dataset = createDataset(outputDatasetName, description, type, inputDataset);
        
//        Iterator iterator = keywordValues.entrySet().iterator();
//
//        Map.Entry entry =  (Map.Entry)iterator.next();
//        String keyword = (String) entry.getKey();
//        String value = (String) entry.getValue();
//
//        while (iterator.hasNext()) {
//            entry =  (Map.Entry)iterator.next();
//            keyword = (String) entry.getKey();
//            value = (String) entry.getValue();
//            addKeyVal(dataset, keyword, value);
//        }

        setDatasetInternalSource(dataset, outputTableName, 
                tableFormat, inputDataset.getName());

        //persist dataset to db
        add(dataset);
        try {
            addVersionZeroEntryToVersionsTable(dataset);
        } catch (Exception e) {
            throw new EmfException("Cannot add version zero entry to versions table for dataset: " + dataset.getName());
        }

        createTable(outputTableName, tableFormat);

        return dataset;
    }

    public EmfDataset addControlledInventoryDataset(String datasetName, 
            EmfDataset inputDataset, DatasetType type, 
            TableFormat tableFormat, String description
//            ,Map<String,String> keywordValues
            ) throws EmfException {
//        return addDataset(datasetName, "DS", 
//                inputDataset, type, 
//                tableFormat, description);
        
        String outputDatasetName = datasetName;
        //check and see if this name is already being used, if so add a timestamp.
        if (isDatasetNameUsed(datasetName)) 
            outputDatasetName = createDatasetName(datasetName);

        String outputTableName = createTableName(datasetName);
        
        //create dataset
        EmfDataset dataset = createDataset(outputDatasetName, description, type, inputDataset);
        // TODO: JIZHEN_0713 ADD 2 more keywords
        
        //update dataset start and stop date time to correct year
        Calendar cal = Calendar.getInstance();
        Date dateTime = inputDataset.getStartDateTime();
        if (dateTime != null) {
            cal.setTime(dateTime);
            cal.set(Calendar.YEAR, controlStrategy.getInventoryYear());
            dataset.setStartDateTime(cal.getTime());
        }
        dateTime = inputDataset.getStopDateTime();
        if (dateTime != null) {
            cal.setTime(dateTime);
            cal.set(Calendar.YEAR, controlStrategy.getInventoryYear());
            dataset.setStopDateTime(cal.getTime());
        }
        
//        Iterator iterator = keywordValues.entrySet().iterator();
//
//        Map.Entry entry =  (Map.Entry)iterator.next();
//        String keyword = (String) entry.getKey();
//        String value = (String) entry.getValue();
//
//        while (iterator.hasNext()) {
//            entry =  (Map.Entry)iterator.next();
//            keyword = (String) entry.getKey();
//            value = (String) entry.getValue();
//            addKeyVal(dataset, keyword, value);
//        }

        setDatasetInternalSource(dataset, outputTableName, 
                tableFormat, inputDataset.getName());

        //persist dataset to db
        add(dataset);
        try {
            addVersionZeroEntryToVersionsTable(dataset);
        } catch (Exception e) {
            throw new EmfException("Cannot add version zero entry to versions table for dataset: " + dataset.getName());
        }

        createTable(outputTableName, tableFormat);

        return dataset;
    }
    
    public EmfDataset addControlledInventoryDataset(String datasetName, 
            EmfDataset inputDataset, DatasetType type, 
            TableFormat tableFormat, String description, String detailedDatasetName
//            ,Map<String,String> keywordValues
            ) throws EmfException {
//        return addDataset(datasetName, "DS", 
//                inputDataset, type, 
//                tableFormat, description);
        
        String outputDatasetName = datasetName;
        //check and see if this name is already being used, if so add a timestamp.
        if (isDatasetNameUsed(datasetName)) 
            outputDatasetName = createDatasetName(datasetName);

        String outputTableName = createTableName(datasetName);
        
        //create dataset
        EmfDataset dataset = createDataset(outputDatasetName, description, type, inputDataset);
        addKeyVal(dataset, "CONTROL_STRATEGY_DETAILED_RESULT_NAME", detailedDatasetName);
        
        //update dataset start and stop date time to correct year
        Calendar cal = Calendar.getInstance();
        Date dateTime = inputDataset.getStartDateTime();
        if (dateTime != null) {
            cal.setTime(dateTime);
            cal.set(Calendar.YEAR, controlStrategy.getInventoryYear());
            dataset.setStartDateTime(cal.getTime());
        }
        dateTime = inputDataset.getStopDateTime();
        if (dateTime != null) {
            cal.setTime(dateTime);
            cal.set(Calendar.YEAR, controlStrategy.getInventoryYear());
            dataset.setStopDateTime(cal.getTime());
        }
        
//        Iterator iterator = keywordValues.entrySet().iterator();
//
//        Map.Entry entry =  (Map.Entry)iterator.next();
//        String keyword = (String) entry.getKey();
//        String value = (String) entry.getValue();
//
//        while (iterator.hasNext()) {
//            entry =  (Map.Entry)iterator.next();
//            keyword = (String) entry.getKey();
//            value = (String) entry.getValue();
//            addKeyVal(dataset, keyword, value);
//        }

        setDatasetInternalSource(dataset, outputTableName, 
                tableFormat, inputDataset.getName());

        //persist dataset to db
        add(dataset);
        try {
            addVersionZeroEntryToVersionsTable(dataset);
        } catch (Exception e) {
            throw new EmfException("Cannot add version zero entry to versions table for dataset: " + dataset.getName());
        }

        createTable(outputTableName, tableFormat);

        return dataset;
    }

    public EmfDataset addDataset(String datasetNamePrefix, String tablePrefix, 
            EmfDataset inputDataset, DatasetType type, 
            TableFormat tableFormat) throws EmfException {
        return addDataset(datasetNamePrefix, tablePrefix, 
                inputDataset, type, 
                tableFormat, detailedResultDescription(inputDataset));
    }

    private EmfDataset createDataset(String name, 
            String description,
            DatasetType type,
            EmfDataset inputDataset) throws EmfException {
        EmfDataset dataset = createDataset(name, 
                description,
                type);

        //Add properties from input dataset...
        dataset.setStartDateTime(inputDataset.getStartDateTime());
        dataset.setStopDateTime(inputDataset.getStopDateTime());
        dataset.setTemporalResolution(inputDataset.getTemporalResolution());
        dataset.setSectors(inputDataset.getSectors());
        dataset.setRegion(inputDataset.getRegion());
        dataset.setCountry(inputDataset.getCountry());
        
        //Add keywords to the dataset specific to the input dataset
        addKeyVals(dataset, inputDataset);
        
        return dataset;
    }
    
    private EmfDataset createDataset(String name, 
            String description,
            DatasetType type) throws EmfException {
        EmfDataset dataset = new EmfDataset();
        Date start = new Date();

        String newName = name;
        if ( newName != null) {
            newName = newName.trim();
        } else {
            throw new EmfException("Dataset name is null");
        }
        dataset.setName(newName);
        
        dataset.setCreator(user.getUsername());
        dataset.setCreatorFullName(user.getName());
        dataset.setDatasetType(type);
        dataset.setDescription(description);
        dataset.setCreatedDateTime(start);
        dataset.setModifiedDateTime(start);
        dataset.setAccessedDateTime(start);
        dataset.setStatus("Created by control strategy");
        
        dataset.setRegion(controlStrategy.getRegion());

        //Add keywords to the dataset
        addKeyVals(dataset);
        
        return dataset;
    }
    
    protected void addKeyVals(EmfDataset dataset, EmfDataset inputDataset) {
        addKeyVal(dataset, "STRATEGY_INVENTORY_NAME", inputDataset.getName());
        addKeyVal(dataset, "STRATEGY_INVENTORY_VERSION", inputDataset.getDefaultVersion()+"");
    }
        
    protected void addKeyVals(EmfDataset dataset) {
        if (controlStrategy == null) return;
        //Add keywords to the dataset
        addKeyVal(dataset, "STRATEGY_NAME", controlStrategy.getName());
        addKeyVal(dataset, "STRATEGY_ID", controlStrategy.getId()+"");
        addKeyVal(dataset, "STRATEGY_TYPE", controlStrategy.getStrategyType().getName());
        addKeyVal(dataset, "TARGET_POLLUTANT", controlStrategy.getTargetPollutant() != null ? controlStrategy.getTargetPollutant().getName() : "Unspecified");
        addKeyVal(dataset, "COST_YEAR", controlStrategy.getCostYear() + "");
        addKeyVal(dataset, "REGION", controlStrategy.getRegion() != null && controlStrategy.getRegion().getName().length() > 0 ? controlStrategy.getRegion().getName() : "Unspecified");
//        addKeyVal(dataset, "STRATEGY_INVENTORY_NAME", inputDataset.getName());
//        addKeyVal(dataset, "STRATEGY_INVENTORY_VERSION", inputDataset.getDefaultVersion()+"");
        int measureCount = (controlStrategy.getControlMeasures() != null ? controlStrategy.getControlMeasures().length : 0);
        ControlMeasureClass[] controlMeasureClasses = controlStrategy.getControlMeasureClasses();
        String classList = "All";
        if (controlMeasureClasses != null) {
            if (controlMeasureClasses.length > 0) classList = "";
            for (int i = 0; i < controlMeasureClasses.length; i++) {
                if (classList.length() > 0) classList += ", ";  
                classList += controlMeasureClasses[i].getName();
            }
        }
        if (measureCount > 0) 
            addKeyVal(dataset, "MEASURES_INCLUDED", measureCount + "");
        else
            addKeyVal(dataset, "MEASURE_CLASSES", classList);
        addKeyVal(dataset, "DISCOUNT_RATE", controlStrategy.getDiscountRate()+"%");
        addKeyVal(dataset, "USE_COST_EQUATION", (controlStrategy.getUseCostEquations()==true? "true" : "false"));
        addKeyVal(dataset, "INCLUDE_UNSPECIFIED_COSTS", (controlStrategy.getIncludeUnspecifiedCosts()==true? "true" : "false"));
        
        // BUG3602
        addKeyVal(dataset, "TARGET_YEAR", "" + controlStrategy.getInventoryYear());
    }
    
    protected String getKeyValsAsHeaderString() {
        String header = "";
        header = "#STRATEGY_NAME=" + controlStrategy.getName();
        header += "\n#STRATEGY_TYPE=" + controlStrategy.getStrategyType().getName();
        header += "\n#TARGET_POLLUTANT=" + (controlStrategy.getTargetPollutant() != null ? controlStrategy.getTargetPollutant().getName() : "Unspecified");
        header += "\n#COST_YEAR=" + controlStrategy.getCostYear() + "";
        header += "\n#REGION=" + (controlStrategy.getRegion() != null && controlStrategy.getRegion().getName().length() > 0 ? controlStrategy.getRegion().getName() : "Unspecified");
        int measureCount = (controlStrategy.getControlMeasures() != null ? controlStrategy.getControlMeasures().length : 0);
        ControlMeasureClass[] controlMeasureClasses = controlStrategy.getControlMeasureClasses();
        String classList = "All";
        if (controlMeasureClasses != null) {
            if (controlMeasureClasses.length > 0) classList = "";
            for (int i = 0; i < controlMeasureClasses.length; i++) {
                if (classList.length() > 0) classList += ", ";  
                classList += controlMeasureClasses[i].getName();
            }
        }
        if (measureCount > 0) 
            header += "\n#MEASURES_INCLUDED=" + measureCount;
        else
            header += "\n#MEASURE_CLASSES=" + classList;
        header += "\n#DISCOUNT_RATE=" + controlStrategy.getDiscountRate()+"%";
        header += "\n#USE_COST_EQUATION=" + (controlStrategy.getUseCostEquations()==true? "true" : "false");
        header += "\n#INCLUDE_UNSPECIFIED_COSTS=" + (controlStrategy.getIncludeUnspecifiedCosts()==true? "true" : "false");
        return header;
    }
    
    public void addKeyVal(EmfDataset dataset, String keywordName, String value) {
        Keyword keyword = keywordMasterList.get(keywordName);
        KeyVal keyval = new KeyVal(keyword, value); 
        dataset.addKeyVal(keyval);
    }
    
    private void addVersionZeroEntryToVersionsTable(Dataset dataset) throws Exception {
//        TableModifier modifier = new TableModifier(datasource, "versions");
//        String[] data = { null, dataset.getId() + "", "0", "Initial Version", "", "true", null, null, null, this.user.getId() + "" };
//        modifier.insertOneRow(data);
               
        
        gov.epa.emissions.framework.utils.Utils.addVersionEntryToVersionsTable(this.sessionFactory, this.user,dataset.getId(), 0, "Initial Version", "", true, "");

//        Version defaultZeroVersion = new Version(0);
//        defaultZeroVersion.setName("Initial Version");
//        defaultZeroVersion.setPath("");
//        defaultZeroVersion.setCreator(user);
//        defaultZeroVersion.setDatasetId(dataset.getId());
//        defaultZeroVersion.setLastModifiedDate(new Date());
////        defaultZeroVersion.setNumberRecords(version.getNumberRecords());
//        defaultZeroVersion.setFinalVersion(true);
//        defaultZeroVersion.setDescription("");
//        Session session = sessionFactory.getSession();
//
//        try {
//            new DatasetDAO().add(defaultZeroVersion, session);
//        } catch (Exception e) {
//            throw new EmfException("Could not add default zero version: " + e.getMessage());
//        } finally {
//            session.close();
//        }
    }

    public void updateVersionZeroRecordCount(EmfDataset dataset) throws EmfException {
        DbServer dbServer = dbServerFactory.getDbServer();
        Session session = sessionFactory.getSession();

        try {
            DatasetDAO dao = new DatasetDAO(dbServerFactory);
            Version version = dao.getVersion(session, dataset.getId(), 0);
            Version lockedVersion = dao.obtainLockOnVersion(user, version.getId(), session);
            
            lockedVersion.setLastModifiedDate(new Date());
            int num = getNumOfRecords(datasource.getName() + "." + dataset.getInternalSources()[0].getTable(), lockedVersion);
            lockedVersion.setNumberRecords(num);
            dao.updateVersionNReleaseLock(lockedVersion, session);
        } catch (Exception e) {
            throw new EmfException("Could not query table: " + e.getMessage());
        } finally {
                try {
                    if (dbServer != null && dbServer.isConnected())
                        dbServer.disconnect();
                } catch (Exception e) {
                    // NOTE Auto-generated catch block
                    e.printStackTrace();
                }
            session.close();
        }
    }
    
    private int getNumOfRecords (String table, Version version) throws EmfException {
        DbServer dbServer = dbServerFactory.getDbServer();

        try {
            Datasource datasource = dbServer.getEmissionsDatasource();
            DataModifier dataModifier = datasource.dataModifier();
            VersionedQuery versionedQuery = new VersionedQuery(version);

            String whereClause = " WHERE " + versionedQuery.query();

            String countQuery = "SELECT COUNT(*) FROM " + table + whereClause;
            
            return Integer.parseInt(dataModifier.getRowCount(countQuery)+ "");
        } catch (SQLException e) {
            throw new EmfException("Please check data table name and/or the syntax of row filter.");
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        } finally {
            try {
                if (dbServer != null && dbServer.isConnected())
                    dbServer.disconnect();
            } catch (Exception e) {
                // NOTE Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
    private void setDatasetInternalSource(EmfDataset dataset, String tableName, TableFormat tableFormat, String source) {
        InternalSource internalSource = new InternalSource();
        internalSource.setTable(tableName);
        internalSource.setType(tableFormat.identify());
        internalSource.setCols(colNames(tableFormat.cols()));
        internalSource.setSource(source);
        dataset.setInternalSources(new InternalSource[] { internalSource });
    }

    private String[] colNames(Column[] cols) {
        List names = new ArrayList();
        for (int i = 0; i < cols.length; i++)
            names.add(cols[i].name());

        return (String[]) names.toArray(new String[0]);
    }

    private String createResultDatasetName(String datasetNamePrefix, EmfDataset inputDataset) {
        return createDatasetName(datasetNamePrefix+ "_" + inputDataset.getId() 
                + "_V" + inputDataset.getDefaultVersion());
    }
        
    //for testing...
    public static void main(String[] args) {
        //
        DatasetCreator dc = new DatasetCreator();
        EmfDataset dataset = new EmfDataset();
        dataset.setName("test");
        Calendar cal = Calendar.getInstance();
        dataset.setStartDateTime(cal.getTime());
        dataset.setStopDateTime(cal.getTime());
    }
    
    public String createControlledInventoryDatasetName(String datasetNamePrefix, EmfDataset inputDataset) {
        String datasetName = "";
        //if no prefix was passed then use the existing name as a starting point.
        if (datasetNamePrefix == null || datasetNamePrefix.length() == 0)
            datasetName = "Cntld_" + inputDataset.getName();
        else {
            datasetName = datasetNamePrefix;
            //see if we need to tag with a monthly indicator
            int applicableMonth = inputDataset.applicableMonth();
            if (applicableMonth >= 0) {
                if (applicableMonth == Calendar.JANUARY) {
                    if (("_" + datasetName.toLowerCase() + "_").indexOf("_jan_") == -1) 
                        datasetName += "_jan";
                }
                else if (applicableMonth == Calendar.FEBRUARY) {
                    if (("_" + datasetName.toLowerCase() + "_").indexOf("_feb_") == -1) 
                        datasetName += "_feb";
                }
                else if (applicableMonth == Calendar.MARCH) {
                    if (("_" + datasetName.toLowerCase() + "_").indexOf("_mar_") == -1) 
                        datasetName += "_mar";
                }
                else if (applicableMonth == Calendar.APRIL) {
                    if (("_" + datasetName.toLowerCase() + "_").indexOf("_apr_") == -1) 
                        datasetName += "_apr";
                }
                else if (applicableMonth == Calendar.MAY) {
                    if (("_" + datasetName.toLowerCase() + "_").indexOf("_may_") == -1) 
                        datasetName += "_may";
                }
                else if (applicableMonth == Calendar.JUNE) {
                    if (("_" + datasetName.toLowerCase() + "_").indexOf("_jun_") == -1) 
                        datasetName += "_jun";
                }
                else if (applicableMonth == Calendar.JULY) {
                    if (("_" + datasetName.toLowerCase() + "_").indexOf("_jul_") == -1) 
                        datasetName += "_jul";
                }
                else if (applicableMonth == Calendar.AUGUST) {  
                    if (("_" + datasetName.toLowerCase() + "_").indexOf("_aug_") == -1) 
                        datasetName += "_aug";
                }
                else if (applicableMonth == Calendar.SEPTEMBER) {
                    if (("_" + datasetName.toLowerCase() + "_").indexOf("_sep_") == -1) 
                        datasetName += "_sep";
                }
                else if (applicableMonth == Calendar.OCTOBER) {
                    if (("_" + datasetName.toLowerCase() + "_").indexOf("_oct_") == -1) 
                        datasetName += "_oct";
                }
                else if (applicableMonth == Calendar.NOVEMBER) {
                    if (("_" + datasetName.toLowerCase() + "_").indexOf("_nov_") == -1) 
                        datasetName += "_nov";
                }
                else if (applicableMonth == Calendar.DECEMBER) {
                    if (("_" + datasetName.toLowerCase() + "_").indexOf("_dec_") == -1) 
                        datasetName += "_dec";
                }
            }
        }
        return datasetName;
    }
        
    public static String createDatasetName(String name) {
        //name += "_" + CustomDateFormat.format_YYYYMMDDHHMMSSSS(new Date());
//        if (name.length() > 46) {     //postgresql table name max length is 64
//            name = name.substring(0, 45);
//        }//16+1
        if (name.length() > 54) {     //postgresql table name max length is 64
            name = name.substring(0, 53);
        }//8+1

        for (int i = 0; i < name.length(); i++) {
            if (!Character.isLetterOrDigit(name.charAt(i))) {
                name = name.replace(name.charAt(i), '_');
            }
        }
//        format_HHMMSSSS
//        format_YYYYMMDDHHMMSSSS
        return name.trim().replaceAll(" ", "_") + "_" + CustomDateFormat.format_HHMMSSSS(new Date());
    }

    private String createTableName(String tablePrefix, EmfDataset inputDataset) {
        String prefix = tablePrefix + "_" + inputDataset.getId() 
            + "_V" + inputDataset.getDefaultVersion();
        String name = inputDataset.getName();
        return createTableName(prefix, name);
    }
    
    private String createTableName(String tablePrefix, String name) {
        return createTableName(tablePrefix + "_" + name);
    }

    private String createTableName(String name) {
        String table = name;
        //truncate if necessary so a unique timestamp can be added to ensure uniqueness
        if (table.length() > 46) {     //postgresql table name max length is 64
            table = table.substring(0, 45);
        }

        for (int i = 0; i < table.length(); i++) {
            if (!Character.isLetterOrDigit(table.charAt(i))) {
                table = table.replace(table.charAt(i), '_');
            }
        }

        //add unique timestamp to ensure uniqueness
        return table.trim().replaceAll(" ", "_") + "_" + CustomDateFormat.format_YYYYMMDDHHMMSSSS(new Date());
    }

    private boolean isDatasetNameUsed(String name) throws EmfException {
        boolean nameUsed = false;
        Session session = sessionFactory.getSession();
        try {
            DatasetDAO dao = new DatasetDAO();
            nameUsed = dao.datasetNameUsed(name, session);
        } catch (Exception e) {
            throw new EmfException("Could not check if name is already used in a dataset: " + name);
        } finally {
            session.close();
        }
        return nameUsed;
    }
    
    private void add(EmfDataset dataset) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            DatasetDAO dao = new DatasetDAO(dbServerFactory);
            if (dao.datasetNameUsed(dataset.getName(), session))
                throw new EmfException("The selected dataset name is already in use.");

            dao.add(dataset, session);
        } catch (Exception e) {
            throw new EmfException("Could not add dataset: " + dataset.getName());
        } finally {
            session.close();
        }
    }
    
    public void update(EmfDataset dataset) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            DatasetDAO dao = new DatasetDAO(dbServerFactory);

            dao.updateWithoutLocking(dataset, session);
        } catch (Exception e) {
            throw new EmfException("Could not add dataset: " + dataset.getName());
        } finally {
            session.close();
        }
    }
    
//    public void update(EmfDataset dataset) throws EmfException {
//        Session session = sessionFactory.getSession();
//        try {
//            DatasetDAO dao = new DatasetDAO(dbServerFactory);
//            dao.updateWithoutLocking(dataset, session);
//        } catch (Exception e) {
//            throw new EmfException("Could not update dataset: " + dataset.getName());
//        } finally {
//            session.close();
//        }
//    }
//    
    private void createTable(String tableName, TableFormat tableFormat) throws EmfException {
        TableCreator creator = new TableCreator(datasource);
        try {
            if (creator.exists(tableName))
                creator.drop(tableName);

            creator.create(tableName, tableFormat);
        } catch (Exception e) {
            throw new EmfException("Could not create table '" + tableName + "'+\n" + e.getMessage());
        }
    }
    
    public String detailedResultDescription(EmfDataset inputDataset) {
        return "#Control strategy detailed result\n" + 
           "#Implements control strategy: " + controlStrategy.getName() + "\n"
                + "#Input dataset used: " + inputDataset.getName()+"\n#";
    }

}
