package gov.epa.emissions.framework.services.module;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.data.KeyVal;
import gov.epa.emissions.commons.data.Keyword;
import gov.epa.emissions.commons.data.ModuleDataset;
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
import gov.epa.emissions.framework.services.data.DataCommonsServiceImpl;
import gov.epa.emissions.framework.services.data.DatasetDAO;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.Keywords;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.Session;

public class DatasetCreator {

    private User user;

    private HibernateSessionFactory sessionFactory;

    private DbServerFactory dbServerFactory;

    private ModuleDataset moduleDataset;

    private Keywords keywordMasterList;
    
    private Datasource datasource;

    public DatasetCreator() {
    }

    public DatasetCreator(ModuleDataset moduleDataset, User user, 
            HibernateSessionFactory sessionFactory, DbServerFactory dbServerFactory,
            Datasource datasource, Keywords keywordMasterList) {
        this.user = user;
        this.sessionFactory = sessionFactory;
        this.dbServerFactory = dbServerFactory;
        this.moduleDataset = moduleDataset;
        this.datasource = datasource;
        this.keywordMasterList = keywordMasterList;
    }

    public DatasetCreator(ModuleDataset moduleDataset, User user, 
            HibernateSessionFactory sessionFactory, DbServerFactory dbServerFactory,
            Datasource datasource) {
        this(moduleDataset, user, sessionFactory, dbServerFactory, datasource, getKeywords(sessionFactory));
    }

    private static Keywords getKeywords(HibernateSessionFactory sessionFactory) {
        Keywords keywords = null;
        try {
            keywords = new Keywords(new DataCommonsServiceImpl(sessionFactory).getKeywords());
        } catch (EmfException ex) {
            // ignore for now
        }
        return keywords;
    }
    
    public EmfDataset addDataset(String datasetNamePrefix, String tablePrefix, 
            EmfDataset inputDataset, DatasetType type, 
            TableFormat tableFormat, String description) throws EmfException {
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
        newDataset.setStatus("Created by Module Runner");

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
        addKeyVals(newDataset);
        
        return newDataset;
    }
    
    protected void addKeyVals(EmfDataset newDataset) {
        if (moduleDataset == null) return;
        //Add keywords to the dataset
        addKeyVal(newDataset, "MODULE_NAME", moduleDataset.getModule().getName());
        addKeyVal(newDataset, "MODULE_ID", moduleDataset.getModule().getId() + "");
        addKeyVal(newDataset, "MODULE_PLACEHOLDER", moduleDataset.getPlaceholderName());
    }
    
    protected String getKeyValsAsHeaderString(EmfDataset inventory) {
        String header = "";
        header = "#MODULE_NAME=" + moduleDataset.getModule().getName();
        header += "\n#MODULE_ID=" + moduleDataset.getModule().getId() + "";
        header += "\n#MODULE_PLACEHOLDER=" + moduleDataset.getPlaceholderName();
        return header;
    }
    
    private void addKeyVal(EmfDataset dataset, String keywordName, String value) {
        Keyword keyword = keywordMasterList.get(keywordName);
        KeyVal keyval = new KeyVal(keyword, value); 
        dataset.addKeyVal(keyval);
    }
    
    private void addVersionZeroEntryToVersionsTable(Dataset dataset) throws Exception {
        Version defaultZeroVersion = new Version(0);
        defaultZeroVersion.setName("Initial Version");
        defaultZeroVersion.setPath("");
        defaultZeroVersion.setCreator(user);
        defaultZeroVersion.setDatasetId(dataset.getId());
        defaultZeroVersion.setLastModifiedDate(new Date());
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

    public static String createDatasetName(String name) {
        if (name.length() > 54) {     // postgresql table name max length is 64
            name = name.substring(0, 53);
        }

        for (int i = 0; i < name.length(); i++) {
            if (!Character.isLetterOrDigit(name.charAt(i))) {
                name = name.replace(name.charAt(i), '_');
            }
        }

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
}
