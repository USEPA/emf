package gov.epa.emissions.framework.services.fast;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.data.KeyVal;
import gov.epa.emissions.commons.data.Keyword;
import gov.epa.emissions.commons.db.DataModifier;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.TableCreator;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.commons.io.TableFormat;
import gov.epa.emissions.commons.io.VersionedQuery;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.DateUtil;
import gov.epa.emissions.framework.services.data.DatasetDAO;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.Keywords;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

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
    
    private FastRun fastRun;

    private Keywords keywordMasterList;
    
    private Datasource datasource;

    public DatasetCreator() {
        //
    }
    public DatasetCreator(FastRun sectorScenario, User user, 
            HibernateSessionFactory sessionFactory, DbServerFactory dbServerFactory,
            Datasource datasource, Keywords keywordMasterList) {
//        this.datasetNamePrefix = datasetNamePrefix;
//        this.tablePrefix = tablePrefix;
        this.user = user;
        this.sessionFactory = sessionFactory;
        this.dbServerFactory = dbServerFactory;
//        this.outputDatasetName = getResultDatasetName(strategy.getName());
        this.fastRun = sectorScenario;
        this.datasource = datasource;
        this.keywordMasterList = keywordMasterList;//new Keywords(new DataCommonsServiceImpl(sessionFactory).getKeywords());
    }

    public EmfDataset addDataset(String datasetNamePrefix, String tablePrefix, 
            EmfDataset inputDataset, DatasetType type, 
            TableFormat tableFormat, String description) throws EmfException {
//        String outputDatasetName = createResultDatasetName(datasetNamePrefix, inputDataset);
        String outputTableName = createTableName(tablePrefix, datasetNamePrefix);
        
        //create dataset
        EmfDataset dataset = createDataset(datasetNamePrefix, description, type, inputDataset);

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
        EmfDataset dataset = createDataset(datasetName, description, type, null);

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

    public EmfDataset addDataset(String datasetName, 
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
            EmfDataset inventory) throws EmfException {
        EmfDataset newDataset = new EmfDataset();
        Date start = new Date();

        String newName = name;
        if ( newName != null) {
            newName = newName.trim();
        } else {
            throw new EmfException("Dataset name is null");
        }
        newDataset.setName(newName);
        
        newDataset.setCreator(user.getUsername());
        newDataset.setCreatorFullName(user.getName());
        newDataset.setDatasetType(type);
        newDataset.setDescription(description);
        newDataset.setCreatedDateTime(start);
        newDataset.setModifiedDateTime(start);
        newDataset.setAccessedDateTime(start);
        newDataset.setStatus("Created by FAST Tool");

        //Add properties from input dataset...
        if (inventory != null) {
            newDataset.setStartDateTime(inventory.getStartDateTime());
            newDataset.setStopDateTime(inventory.getStopDateTime());
            newDataset.setTemporalResolution(inventory.getTemporalResolution());
            newDataset.setSectors(inventory.getSectors());
            newDataset.setRegion(inventory.getRegion());
            newDataset.setCountry(inventory.getCountry());
        }
    
        //Add keywords to the dataset
        addKeyVals(newDataset, inventory);
        
        return newDataset;
    }
    
    protected void addKeyVals(EmfDataset newDataset, EmfDataset inventory) {
        if (fastRun == null) return;
        //Add keywords to the dataset
        addKeyVal(newDataset, "SCENARIO_NAME", fastRun.getName());
        addKeyVal(newDataset, "SCENARIO_ABBREVIATION", fastRun.getAbbreviation());
        addKeyVal(newDataset, "SCENARIO_ID", fastRun.getId()+"");
        if (inventory != null) {
            addKeyVal(newDataset, "SCENARIO_INVENTORY_NAME", inventory.getName());
            addKeyVal(newDataset, "SCENARIO_INVENTORY_VERSION", inventory.getDefaultVersion()+"");
        }
//        addKeyVal(newDataset, "SCENARIO_EECS_MAPPING_NAME", fastRun.getEecsMapppingDataset().getName());
//        addKeyVal(newDataset, "SCENARIO_EECS_MAPPING_VERSION", fastRun.getEecsMapppingDatasetVersion()+"");
//        addKeyVal(newDataset, "SCENARIO_SECTOR_MAPPING_NAME", fastRun.getSectorMapppingDataset().getName());
//        addKeyVal(newDataset, "SCENARIO_SECTOR_MAPPING_VERSION", fastRun.getSectorMapppingDatasetVersion()+"");
//        String[] sectors = fastRun.getSectors();
//        String sectorList = "All";
//        if (sectors != null) {
//            if (sectors.length > 0) sectorList = "";
//            for (int i = 0; i < sectors.length; i++) {
//                if (sectorList.length() > 0) sectorList += ", ";  
//                sectorList += sectors[i];
//            }
//        }
//        addKeyVal(newDataset, "SCENARIO_SECTORS", sectorList);
//        addKeyVal(newDataset, "SCENARIO_SHOULD_DOUBLE_COUNT", (fastRun.getShouldDoubleCount() ? "true" : "false"));
//        addKeyVal(newDataset, "SCENARIO_ANNOTATE_INVENTORY_WITH_EECS", (fastRun.getAnnotateInventoryWithEECS() ? "true" : "false"));
    }
    
    protected String getKeyValsAsHeaderString(EmfDataset inventory) {
        String header = "";
        header = "#SCENARIO_NAME=" + fastRun.getName();
        header += "\n#SCENARIO_ABBREVIATION=" + fastRun.getAbbreviation();
        header += "\n#SCENARIO_ID=" + fastRun.getId() + "";
        if (inventory != null) {
            header += "\nSCENARIO_INVENTORY_NAME=" + inventory.getName();
            header += "\nSCENARIO_INVENTORY_VERSION=" + inventory.getDefaultVersion()+"";
        }
//        header += "\nSCENARIO_EECS_MAPPING_NAME=" + fastRun.getEecsMapppingDataset().getName();
//        header += "\nSCENARIO_EECS_MAPPING_VERSION=" + fastRun.getEecsMapppingDatasetVersion()+"";
//        header += "\nSCENARIO_SECTOR_MAPPING_NAME=" + fastRun.getSectorMapppingDataset().getName();
//        header += "\nSCENARIO_SECTOR_MAPPING_VERSION=" + fastRun.getSectorMapppingDatasetVersion()+"";
//        String[] sectors = fastRun.getSectors();
//        String sectorList = "All";
//        if (sectors != null) {
//            if (sectors.length > 0) sectorList = "";
//            for (int i = 0; i < sectors.length; i++) {
//                if (sectorList.length() > 0) sectorList += ", ";  
//                sectorList += sectors[i];
//            }
//        }
//        header += "\nSCENARIO_SECTORS=" + sectorList;
//        header += "\nSCENARIO_SHOULD_DOUBLE_COUNT=" + (fastRun.getShouldDoubleCount() ? "true" : "false");
//        header += "\nSCENARIO_ANNOTATE_INVENTORY_WITH_EECS=" + (fastRun.getAnnotateInventoryWithEECS() ? "true" : "false");
        return header;
    }
    
    private void addKeyVal(EmfDataset dataset, String keywordName, String value) {
        Keyword keyword = keywordMasterList.get(keywordName);
        KeyVal keyval = new KeyVal(keyword, value); 
        dataset.addKeyVal(keyval);
    }
    
    private void addVersionZeroEntryToVersionsTable(Dataset dataset) throws Exception {
//        TableModifier modifier = new TableModifier(datasource, "versions");
//        String[] data = { null, dataset.getId() + "", "0", "Initial Version", "", "true", null, null, null, this.user.getId() + "" };
//        modifier.insertOneRow(data);
//
//    
        Version defaultZeroVersion = new Version(0);
        defaultZeroVersion.setName("Initial Version");
        defaultZeroVersion.setPath("");
        defaultZeroVersion.setCreator(user);
        defaultZeroVersion.setDatasetId(dataset.getId());
        defaultZeroVersion.setLastModifiedDate(new Date());
//        defaultZeroVersion.setNumberRecords(version.getNumberRecords());
        defaultZeroVersion.setFinalVersion(true);
        defaultZeroVersion.setDescription("");
        Session session = sessionFactory.getSession();

        try {
            new DatasetDAO().add(defaultZeroVersion, session);
        } catch (Exception e) {
            throw new EmfException("Could not add default zero version: " + e.getMessage());
        } finally {
            session.close();
        }
    
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

    //for testing...
    public static void main(String[] args) {
        //
        DatasetCreator dc = new DatasetCreator();
        EmfDataset dataset = new EmfDataset();
        dataset.setName("test");
        Calendar cal = Calendar.getInstance();
        dataset.setStartDateTime(cal.getTime());
        dataset.setStopDateTime(cal.getTime());
        System.out.println("zero based month = " + dataset.applicableMonth());
        System.out.println("no days in month = " + DateUtil.daysInZeroBasedMonth(2020, dataset.applicableMonth()));
        System.out.println(dc.createControlledInventoryDatasetName("asdad", dataset));
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

    public boolean isDatasetNameUsed(String name) throws EmfException {
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
           "#Implements control strategy: " + fastRun.getName() + "\n"
                + "#Input dataset used: " + inputDataset.getName()+"\n#";
    }

}
