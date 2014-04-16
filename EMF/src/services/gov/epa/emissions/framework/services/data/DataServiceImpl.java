package gov.epa.emissions.framework.services.data;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.ExternalSource;
import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.db.DataModifier;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.TableCreator;
import gov.epa.emissions.commons.db.TableMetaData;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.commons.io.DeepCopy;
import gov.epa.emissions.commons.io.ExporterException;
import gov.epa.emissions.commons.io.VersionedDatasetQuery;
import gov.epa.emissions.commons.io.VersionedQuery;
import gov.epa.emissions.commons.io.importer.DataTable;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.commons.util.EmfArrays;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.controlStrategy.DoubleValue;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;
import gov.epa.emissions.framework.services.qa.TableToString;
import gov.epa.emissions.framework.tasks.DebugLevels;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.axis.utils.XMLChar;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Session;

public class DataServiceImpl implements DataService {
    private static Log LOG = LogFactory.getLog(DataServiceImpl.class);

    private HibernateSessionFactory sessionFactory;

    private DbServerFactory dbServerFactory;

    private DatasetDAO dao;
    
    public enum DeleteType {
        GENERAL, FAST, SECTOR_SCENARIO, CONTROL_STRATEGY, CONTROL_PROGRAM
    }
    
    public DataServiceImpl() {
        this(DbServerFactory.get(), HibernateSessionFactory.get());
    }

    public DataServiceImpl(HibernateSessionFactory sessionFactory) {
        this(DbServerFactory.get(), sessionFactory);
    }

    public DataServiceImpl(DbServerFactory dbServerFactory, HibernateSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
        this.dbServerFactory = dbServerFactory;
        dao = new DatasetDAO(dbServerFactory);
    }

    public synchronized EmfDataset[] getDatasets(String nameContains, int userId) throws EmfException {
        Session session = sessionFactory.getSession();
        List datasets;
        try {
            if (nameContains == null || nameContains.trim().length() == 0 || nameContains.trim().equals("*"))
                datasets = dao.allNonDeleted(session, userId);
            else
                datasets = dao.allNonDeleted(session, nameContains, userId);
            return (EmfDataset[]) datasets.toArray(new EmfDataset[datasets.size()]);
        } catch (RuntimeException e) {
            LOG.error("Could not get all Datasets", e);
            throw new EmfException("Could not get all Datasets");
        } finally {
            session.close();
        }
    }

    public synchronized EmfDataset getDataset(Integer datasetId) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            EmfDataset dataset = dao.getDataset(session, datasetId.intValue());
            return dataset;
        } catch (RuntimeException e) {
            LOG.error("Could not get dataset with id=" + datasetId.intValue(), e);
            throw new EmfException("Could not get dataset with id=" + datasetId.intValue());
        } finally {
            session.close();
        }
    }

    public synchronized EmfDataset getDataset(String datasetName) throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            EmfDataset dataset = dao.getDataset(session, datasetName);
            return dataset;
        } catch (RuntimeException e) {
            LOG.error("Could not get dataset " + datasetName, e);
            throw new EmfException("Could not get dataset " + datasetName);
        } finally {
            session.close();
        }
    }

    public synchronized EmfDataset obtainLockedDataset(User owner, EmfDataset dataset) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            EmfDataset locked = dao.obtainLocked(owner, dataset, session);

            return locked;
        } catch (RuntimeException e) {
            LOG.error("Could not obtain lock for Dataset: " + dataset.getName() + " by owner: " + owner.getUsername(),
                    e);
            throw new EmfException("Could not obtain lock for Dataset: " + dataset.getName() + " by owner: "
                    + owner.getUsername());
        } finally {
            session.close();
        }
    }

    public synchronized EmfDataset releaseLockedDataset(User user, EmfDataset locked) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            EmfDataset released = dao.releaseLocked(user, locked, session);

            return released;
        } catch (RuntimeException e) {
            LOG.error(
                    "Could not release lock for Dataset: " + locked.getName() + " by owner: " + locked.getLockOwner(),
                    e);
            throw new EmfException("Could not release lock for Dataset: " + locked.getName() + " by owner: "
                    + locked.getLockOwner());
        } finally {
            session.close();
        }
    }

    public synchronized EmfDataset updateDataset(EmfDataset dataset) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            DatasetType type = dataset.getDatasetType();

            if (!dao.canUpdate(dataset, session))
                throw new EmfException("The Dataset name " + dataset.getName() + " is already in use.");

            if (type != null && type.getTablePerDataset() > 1)
                LOG.info("Renaming emission tables for dataset " + dataset.getName() + " is not allowed.");

            EmfDataset released = dao.update(dataset, session);

            return released;
        } catch (Exception e) {
            LOG.error("Could not update Dataset: " + dataset.getName() + " " + e.getMessage(), e);
            throw new EmfException("Could not update Dataset: " + e.getMessage());
        } finally {
            session.close();
        }
    }

    public synchronized EmfDataset[] getDatasets(DatasetType datasetType) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            List datasets = dao.getDatasets(session, datasetType);

            return (EmfDataset[]) datasets.toArray(new EmfDataset[datasets.size()]);
        } catch (RuntimeException e) {
            LOG.error("Could not get all Datasets for dataset type " + datasetType, e);
            throw new EmfException("Could not get all Datasets for dataset type " + datasetType);
        } finally {
            session.close();
        }
    }

    public synchronized EmfDataset[] getDatasetsWithFilter(int datasetTypeId, String nameContains) throws EmfException {
        Session session = sessionFactory.getSession();
        List<EmfDataset> datasets;
        
        try {
            if (nameContains == null || nameContains.trim().length() == 0 || nameContains.trim().equals("*"))
                datasets = dao.getDatasets(session, datasetTypeId);
            else
                datasets = dao.getDatasetsWithFilter(session, datasetTypeId, nameContains);

            return datasets.toArray(new EmfDataset[0]);
        } catch (RuntimeException e) {
            LOG.error("Could not get all Datasets for dataset type " + datasetTypeId, e);
            throw new EmfException("Could not get all Datasets for dataset type " + datasetTypeId);
        } finally {
            session.close();
        }
    }

    public synchronized EmfDataset[] getDatasets(int datasetTypeId, String nameContaining) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            List datasets = dao.getDatasets(session, datasetTypeId, nameContaining);

            return (EmfDataset[]) datasets.toArray(new EmfDataset[datasets.size()]);
        } catch (RuntimeException e) {
            LOG.error("Could not get all Datasets for dataset type " + datasetTypeId, e);
            throw new EmfException("Could not get all Datasets for dataset type " + datasetTypeId);
        } finally {
            session.close();
        }
    }

    public synchronized int getNumOfDatasets(String nameContains, int userId) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            if (nameContains == null || nameContains.trim().length() == 0)
                return dao.getNumOfDatasets(userId, session);
            
            return dao.getNumOfDatasets(session, nameContains, userId);
        } catch (RuntimeException e) {
            LOG.error("Could not get all Datasets", e);
            throw new EmfException("Could not get all Datasets");
        } finally {
            session.close();
        }
    }

    public synchronized int getNumOfDatasets(int datasetTypeId, String nameContains) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            if (nameContains == null || nameContains.trim().length() == 0)
                return dao.getNumOfDatasets(session, datasetTypeId);
            
            return dao.getNumOfDatasets(session, datasetTypeId, nameContains);
        } catch (RuntimeException e) {
            LOG.error("Could not get all Datasets for dataset type " + datasetTypeId, e);
            throw new EmfException("Could not get all Datasets for dataset type " + datasetTypeId);
        } finally {
            session.close();
        }
    }

    public synchronized void deleteDatasets(User owner, EmfDataset[] datasets) throws EmfException {
        deleteDatasets(owner, datasets, DeleteType.GENERAL);
    }
    
    public synchronized void deleteDatasets(User owner, EmfDataset[] datasets, DeleteType delType) throws EmfException {
        String prefix = "DELETED_" + new Date().getTime() + "_";
        int count = 0;

        try {
            EmfDataset[] removables = getRemovableDatasets(datasets, owner, delType); //JIZHEN-SECTOR
                
            for (EmfDataset ds : removables) {
                if (ds.getStatus().equalsIgnoreCase("Deleted")) {
                    //update count, useful for strategy runs...
                    count++;
                    continue;
                }
                    
                try {
                    
                    String newName = prefix + ds.getName();
                    if ( newName != null) {
                        newName = newName.trim();
                    } else {
                        throw new EmfException("Dataset name is null");
                    }
                    ds.setName(newName);
                    
                    ds.setStatus("Deleted");
                    updateDataset(ds);
                    count++;
                } catch (Exception e) {
                    // NOTE Auto-generated catch block
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            LOG.error("Could not delete datasets: ", e);
            throw new EmfException(e.getMessage());
        }
        
        String msg = count + " dataset" + (count > 1 ? "s" : "") + " deleted.";
        
        if (count != datasets.length) msg += " Please check status window for details.";
            
        throw new EmfException(msg, EmfException.MSG_TYPE);
    }

    private EmfDataset[] getRemovableDatasets(EmfDataset[] datasets, User owner, DeleteType delType) {
        Session session = sessionFactory.getSession();
        DbServer dbServer = dbServerFactory.getDbServer();
        List<EmfDataset> removables = new ArrayList<EmfDataset>();
        String nonRemovables = "";
        int len = datasets.length;

        for (int i = 0; i < len; i++) {
            try {
                checkUser(datasets[i], owner);
                removables.add(datasets[i]);
            } catch (EmfException e) {
                nonRemovables += datasets[i] + ";";
            }
        }
        
        if (removables.size() < len) {
            String errMsg = "You are not the creator of dataset(s): " + nonRemovables;
            dao.setStatus(owner.getUsername(), errMsg, "Delete Dataset", session);
        }
        
        if (removables.size() == 0) return new EmfDataset[0];
        
        int[] dsIDs = new int[removables.size()];
        
        for (int i = 0; i < dsIDs.length; i++) {
            dsIDs[i] = removables.get(i).getId();
        }

        List<Integer> ids = new ArrayList<Integer>();
        
        try {
            ids = getNotRefdDatasetIds(owner, session, dbServer, dsIDs, delType); // JIZHEN-SECTOR
        } catch (Exception e) {
            LOG.error("Error checking dataset usage: ", e);
        } 
            
        try {
            if (session != null && session.isConnected())
                session.close();
            closeDB(dbServer);
        } catch (EmfException e) {
            LOG.error("Could not close db server: ", e);
        } catch (HibernateException e) {
            LOG.error("Could not close hibernate session: ", e);
        }
        
        if (ids == null || ids.size() == 0)
            return new EmfDataset[0];

        List<EmfDataset> list = new ArrayList<EmfDataset>();
        
        for (EmfDataset ds : removables)
            if (ids.contains(new Integer(ds.getId())))
                list.add(ds);
                
        return list.toArray(new EmfDataset[0]);
    }

    private List<Integer> getNotRefdDatasetIds(User owner, Session session, DbServer dbServer, int[] dsIDs, 
            DeleteType delType) throws Exception {  //JIZHEN-SECTOR
        List<Integer> ids;
        ids = dao.notUsedByCases(dsIDs, owner, session);
        
        if (delType != DeleteType.CONTROL_STRATEGY)
            ids = dao.notUsedByStrategies(EmfArrays.convert(ids), owner, session);
        
        if (delType != DeleteType.CONTROL_PROGRAM)
            ids = dao.notUsedByControlPrograms(EmfArrays.convert(ids), owner, session);
        
        if (delType != DeleteType.SECTOR_SCENARIO)
            ids = dao.notUsedBySectorScnarios(EmfArrays.convert(ids), owner, session);
        
        if (delType != DeleteType.FAST)
            ids = dao.notUsedByFast(EmfArrays.convert(ids), owner, dbServer, session);
        
        return ids;
    }
    
    private synchronized void checkUser(EmfDataset dataset, User owner) throws EmfException {
        if (!owner.isAdmin() && !dataset.getCreator().equalsIgnoreCase(owner.getUsername())) {
            releaseLockedDataset(owner, dataset);
            throw new EmfException("You are not the creator of " + dataset.getName() + ".");
        }
    }

    private synchronized void checkCase(int[] datasetIDs) throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            dao.checkIfUsedByCases(datasetIDs, session);
        } catch (Exception ex) {
            LOG.error("Error checking case.", ex);
            throw new EmfException(ex.getMessage());
        } finally {
            session.close();
        }
    }

    private synchronized void checkControlStrategy(int[] datasetIDs) throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            dao.checkIfUsedByStrategies(datasetIDs, session);
        } finally {
            session.close();
        }
    }

    private synchronized void checkControlProgram(int[] datasetIDs) throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            dao.checkIfUsedByControlPrograms(datasetIDs, session);
        } finally {
            session.close();
        }
    }

    public synchronized String[] getDatasetValues(Integer datasetId) throws EmfException {
        EmfDataset dataset = null;
        List<String> values = new ArrayList<String>();

        if (datasetId == null || datasetId.intValue() == 0)
            return null;

        dataset = getDataset(datasetId);

        values.add("name," + (dataset.getName() == null ? "" : dataset.getName()));
        values.add("datasetType," + (dataset.getDatasetTypeName() == null ? "" : dataset.getDatasetTypeName()));
        values.add("creator," + (dataset.getCreator() == null ? "" : dataset.getCreator()));
        values.add("createdDateTime," + (dataset.getCreatedDateTime() == null ? "" : dataset.getCreatedDateTime()));
        values.add("status," + (dataset.getStatus() == null ? "" : dataset.getStatus()));

        return values.toArray(new String[0]);
    }

    public Version obtainedLockOnVersion(User user, int id) throws EmfException {
        Session session = this.sessionFactory.getSession();

        try {
            return dao.obtainLockOnVersion(user, id, session);
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        } finally {
            session.close();
        }
    }

    public void updateVersionNReleaseLock(Version locked) throws EmfException {
        Session session = this.sessionFactory.getSession();

        try {
            dao.updateVersionNReleaseLock(locked, session);
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        } finally {
            session.close();
        }

    }

    public void checkIfDeletable(User user, int datasetID) throws EmfException {
        Session session = this.sessionFactory.getSession();

        try {
            dao.checkIfUsedByCases(new int[] { datasetID }, session);
            dao.checkIfUsedByStrategies(new int[] { datasetID }, session);
            dao.checkIfUsedByControlPrograms(new int[] { datasetID }, session);
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        } finally {
            session.close();
        }
    }

    public void purgeDeletedDatasets(User user) throws EmfException {
        purgeDeletedDatasets(user, DeleteType.GENERAL);
    }
    
    public void purgeDeletedDatasets(User user, DeleteType delType) throws EmfException {
        Session session = this.sessionFactory.getSession();
        DbServer dbServer = DbServerFactory.get().getDbServer();

        try {
            if (user.getUsername().equals("admin") && user.isAdmin())
                dao.removeEmptyDatasets(user, dbServer, session);

            List<EmfDataset> list = dao.deletedDatasets(user, session);
            EmfDataset[] toDelete = getRemovableDatasets(list.toArray(new EmfDataset[0]), user, delType);
            dao.deleteDatasets(toDelete, dbServer, session);
        } catch (Exception e) {
            LOG.error("Error purging deleted datasets.", e);
            throw new EmfException(e.getMessage());
        } catch (Throwable t) {
            LOG.error("Error purging deleted datasets.", t);
            throw new EmfException(t.getMessage());
        } finally {
            if (session != null && session.isConnected())
                session.close();
            
            closeDB(dbServer);
        }
    }

    private void closeDB(DbServer dbServer) throws EmfException {
        try {
            if (dbServer != null && dbServer.isConnected())
                dbServer.disconnect();
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        }
    }

    public int getNumOfDeletedDatasets(User user) throws EmfException {
        Session session = this.sessionFactory.getSession();

        try {
            return dao.deletedDatasets(user, session).size();
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        } finally {
            session.close();
        }
    }

    public synchronized String getTableAsString(String qualifiedTableName) throws EmfException {
        DbServer dbServer = dbServerFactory.getDbServer();
        try {
            String str =  new TableToString(dbServer, qualifiedTableName, ",").toString();
            int invalidInx = this.detectFirstInvalidChar(str);
            if ( invalidInx >= 0) {
                LOG.error("Invalid XML character detected: " + Integer.toHexString(str.charAt(invalidInx)) + " at " + invalidInx + " in the returned string.");
                throw new EmfException("Invalid XML character detected: " + Integer.toHexString(str.charAt(invalidInx)) + " at " + invalidInx + " in the returned string.");
            }
            return str;
        } catch (RuntimeException e) {
            LOG.error("Could not retrieve table: " + qualifiedTableName, e);
            throw new EmfException("Could not retrieve table: " + qualifiedTableName);
        } catch (ExporterException e) {
            LOG.error("Could not retrieve table: " + qualifiedTableName, e);
            throw new EmfException("Could not retrieve table: " + qualifiedTableName);
        } catch (Exception e) {
            LOG.error("Could not retrieve table: " + qualifiedTableName, e);
            throw new EmfException("Could not retrieve table: " + qualifiedTableName + ". " + e.getMessage());
        } finally {
            closeDB(dbServer);
        }
    }

    public synchronized String getTableAsString(String qualifiedTableName, long recordLimit, long recordOffset) throws EmfException {
        DbServer dbServer = dbServerFactory.getDbServer();
        try {
            String str = new TableToString(dbServer, qualifiedTableName, ",").toString(recordLimit, recordOffset);
            int invalidInx = this.detectFirstInvalidChar(str);
            if ( invalidInx >= 0) {
                LOG.error("Invalid XML character detected: " + Integer.toHexString(str.charAt(invalidInx)) + " at " + invalidInx + " in the returned string.");
                throw new EmfException("Invalid XML character detected: " + Integer.toHexString(str.charAt(invalidInx)) + " at " + invalidInx + " in the returned string.");
            }
            return str;
        } catch (RuntimeException e) {
            LOG.error("Could not retrieve table: " + qualifiedTableName, e);
            throw new EmfException("Could not retrieve table: " + qualifiedTableName);
        } catch (ExporterException e) {
            LOG.error("Could not retrieve table: " + qualifiedTableName, e);
            throw new EmfException("Could not retrieve table: " + qualifiedTableName);
        } catch (Exception e) {
            LOG.error("Could not retrieve table: " + qualifiedTableName, e);
            throw new EmfException("Could not retrieve table: " + qualifiedTableName  + ". " + e.getMessage());
        } finally {
            closeDB(dbServer);
        }
    }

    public synchronized long getTableRecordCount(String qualifiedTableName) throws EmfException {
        DbServer dbServer = dbServerFactory.getDbServer();
        long recordCount = 0;
        try {
            
            //need to perform a ANALYZE SQL command to make sure statistics are updated...
          
            dbServer.getEmissionsDatasource().query().execute("analyze "  + qualifiedTableName + ";");
            
            ResultSet rs = dbServer.getEmissionsDatasource().query().executeQuery(
                    "select public.count_estimate ('select * from "  + qualifiedTableName + " ') ");
            if (rs.next())
                recordCount = rs.getLong(1);
        } catch (RuntimeException e) {
            LOG.error("Could not retrieve table record count: " + qualifiedTableName, e);
            throw new EmfException("Could not retrieve table record count: " + qualifiedTableName);
        } catch (SQLException e) {
            LOG.error("Could not retrieve table record count: " + qualifiedTableName, e);
            throw new EmfException("Could not retrieve table record count: " + qualifiedTableName);
        } finally {
            closeDB(dbServer);
        }
        return recordCount;
    }

    public synchronized void appendData(User user, int srcDSid, int srcDSVersion, String filter, int targetDSid,
            int targetDSVersion, DoubleValue targetStartLineNumber) throws EmfException {
        DbServer dbServer = dbServerFactory.getDbServer();
        Session session = sessionFactory.getSession();

        try {
            Datasource datasource = dbServer.getEmissionsDatasource();
            Version srcVersion = dao.getVersion(session, srcDSid, srcDSVersion);
            EmfDataset srcDS = dao.getDataset(session, srcDSid);
            InternalSource[] srcSources = srcDS.getInternalSources();
            EmfDataset targetDS = dao.getDataset(session, targetDSid);
            InternalSource[] targetSources = targetDS.getInternalSources();
            
            EmfDataset locked = dao.obtainLocked(user, targetDS, session);
            locked.setModifiedDateTime(new Date());
            dao.update(locked, session);

            DataModifier dataModifier = datasource.dataModifier();

            if (srcSources.length != targetSources.length)
                throw new EmfException("Source dataset set has different number of tables from target dataset.");

            if (srcDS.getDatasetType().getImporterClassName().equals(
                    "gov.epa.emissions.commons.io.generic.LineImporter")) {
                String srcTable = datasource.getName() + "." + srcSources[0].getTable();
                String targetTable = datasource.getName() + "." + targetSources[0].getTable();
                
                // VERSIONS TABLE - Completed - throws exception if the following case is true
                if ("emissions.versions".equalsIgnoreCase(srcTable) && "emissions.versions".equalsIgnoreCase(targetTable)) {
                    throw new EmfException("Table versions moved to schema emf."); // VERSIONS TABLE
                }

                appendLineBasedData(filter, srcVersion, srcDS, srcTable, targetTable, targetDSid, targetDSVersion,
                        dataModifier, targetStartLineNumber.getValue());
            }
            else {
                for (int i = 0; i < targetSources.length; i++) {
                    String srcTable = datasource.getName() + "." + srcSources[i].getTable();
                    String targetTable = datasource.getName() + "." + targetSources[i].getTable();

                    // VERSIONS TABLE - Completed - throws exception if the following case is true
                    if ("emissions.versions".equalsIgnoreCase(srcTable) && "emissions.versions".equalsIgnoreCase(targetTable)) {
                        throw new EmfException("Table versions moved to schema emf."); // VERSIONS TABLE
                    }
                    
                    appendData2SingleTable(filter, srcVersion, srcDS, srcTable, targetTable, targetDSid, targetDSVersion,
                            dataModifier);
                }
            }
            //dao.releaseLocked(user, locked, session);
            Version tarVersion = dao.getVersion(session, targetDSid, targetDSVersion);
            Version lockedVersion = obtainedLockOnVersion(user, tarVersion.getId());
            
            lockedVersion.setLastModifiedDate(new Date());
            int num = getNumOfRecords(datasource.getName() + "." + targetSources[0].getTable(), lockedVersion, null);
            lockedVersion.setNumberRecords(num);
            updateVersionNReleaseLock(lockedVersion);
            
            dao.releaseLocked(user, locked, session);
            if (DebugLevels.DEBUG_17()) {
                LOG.warn("Update version : "+ lockedVersion.getName());
                LOG.warn("Table name : "+ datasource.getName() + "." + targetSources[0].getTable());
                LOG.warn("Table row count : "+ num);
            }
            
            
        } catch (Exception e) {
            LOG.error("Could not query table : ", e);
            throw new EmfException("Could not query table: " + e.getMessage());
        } finally {
            closeDB(dbServer);
            session.close();
        }
    }

    private void appendLineBasedData(String filter, Version srcVersion, EmfDataset srcDS, String srcTable,
            String targetTable, int targetDSid, int targetDSVersion, DataModifier dataModifier, double startLineNum)
            throws Exception {
        String[] srcColumns = null;
        srcColumns = getTableColumns(dataModifier, srcTable, filter);

        String[] targetColumns = null;
        targetColumns = getTableColumns(dataModifier, targetTable, "");

        VersionedDatasetQuery dsQuery = new VersionedDatasetQuery(srcVersion, srcDS);
        double nextBiggerLineNum, increatment;

        if (startLineNum < 0) {
            startLineNum = dataModifier.getLastRowLineNumber(targetTable);
            increatment = 1.0;

        } else {
            long records2Append = dataModifier.getRowCount(dsQuery.generateFilteringQueryWithoutOrderBy(" COUNT(*) ",
                    srcTable, filter));
            nextBiggerLineNum = dataModifier.getNextBiggerLineNumber(targetTable, startLineNum);

            if (nextBiggerLineNum < 0)
                increatment = 1.0;
            else
                increatment = (nextBiggerLineNum - startLineNum) / (records2Append + 1);
        }

        String query = "INSERT INTO "
                + targetTable
                + " ("
                + getTargetColString(targetColumns)
                + ") ("
                + getLineBasedSrcColString(targetDSid, targetDSVersion, srcColumns, startLineNum, increatment,
                        srcTable, filter, dsQuery) + ")";

        if (DebugLevels.DEBUG_17()) {
            LOG.warn("Append data query: " + query);
            LOG.warn("Query starts at: " + new Date());
        }

        dataModifier.execute(query);

        if (DebugLevels.DEBUG_17())
            LOG.warn("Query ends at: " + new Date());
    }

    private void appendData2SingleTable(String filter, Version srcVersion, EmfDataset srcDS, String srcTable,
            String targetTable, int targetDSid, int targetDSVersion, DataModifier dataModifier) throws Exception {
        String[] srcColumns = null;
        srcColumns = getTableColumns(dataModifier, srcTable, filter);

        String[] targetColumns = null;
        targetColumns = getTableColumns(dataModifier, targetTable, "");

        VersionedDatasetQuery dsQuery = new VersionedDatasetQuery(srcVersion, srcDS);
        String query = "INSERT INTO "
                + targetTable
                + " ("
                + getTargetColString(targetColumns)
                + ") ("
                + dsQuery.generateFilteringQuery(
                        getSrcColString(targetDSid, targetDSVersion, srcColumns, targetColumns), srcTable, filter)
                + ")";

        if (DebugLevels.DEBUG_17()) {
            LOG.warn("Append data query: " + query);
            LOG.warn("Query starts at: " + new Date());
        }

        dataModifier.execute(query);

        if (DebugLevels.DEBUG_17())
            LOG.warn("Query ends at: " + new Date());
    }

    public synchronized String[] getTableColumns(String table) throws EmfException {
        DbServer dbServer = dbServerFactory.getDbServer();
        try {
            DataModifier dataModifier = dbServer.getEmissionsDatasource().dataModifier();

            return getTableColumns(dataModifier, table, null);
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error("Could not query table : ", e);
            throw new EmfException("Could not query table: " + e.getMessage());
        } finally {
            closeDB(dbServer);
        }
    }
    
    private String[] getTableColumns(DataModifier mod, String table, String filter) throws Exception {
        ResultSetMetaData md = null;
        String query = null;

        if (filter == null || filter.isEmpty())
            query = "SELECT * FROM " + table + " LIMIT 0";
        else
            query = "SELECT * FROM " + table + " WHERE (" + filter + ") LIMIT 0";

        try {
            md = mod.getMetaData(query);
        } catch (SQLException e) {
            if (filter.isEmpty())
                throw e;

            throw new Exception("Filter format is incorrect: " + filter);
        }

        List<String> cols = new ArrayList<String>();
        int colCount = md.getColumnCount();

        for (int i = 1; i <= colCount; i++)
            cols.add(md.getColumnName(i));

        return cols.toArray(new String[0]);
    }

    private String[] getColNameTypes(DataModifier mod, String table) throws Exception {
        ResultSetMetaData md = null;
        String query = "SELECT * FROM " + table + " LIMIT 0";

        try {
            md = mod.getMetaData(query);
        } catch (SQLException e) {
            throw new Exception(e.getMessage());
        }

        List<String> cols = new ArrayList<String>();
        int colCount = md.getColumnCount();

        // Asumming firt 4 columns are record_id, dataset_id, version, and delete_versions
        // which is common to all emf datasets
        cols.add(md.getColumnName(1) + " SERIAL PRIMARY KEY");
        cols.add(md.getColumnName(2) + " int8 NOT NULL");
        cols.add(md.getColumnName(3) + " int4 DEFAULT 0");
        cols.add(md.getColumnName(4) + " text DEFAULT ''::text");

        for (int i = 5; i <= colCount; i++) {
            String type = md.getColumnTypeName(i);
            String notNull = (md.isNullable(i) == ResultSetMetaData.columnNoNulls ? "NOT NULL" : "");

            if (type.toUpperCase().startsWith("VARCHAR"))
                cols.add(md.getColumnName(i) + " " + type + "(" + md.getPrecision(i) + ")" + " " + notNull);
            else
                cols.add(md.getColumnName(i) + " " + type + " " + notNull);
        }

        return cols.toArray(new String[0]);
    }
    
    private String getColTypesString(DataModifier mod, String table) throws Exception {
        ResultSetMetaData md = null;
        String query = "SELECT * FROM " + table + " LIMIT 0";

        try {
            md = mod.getMetaData(query);
        } catch (SQLException e) {
            throw new Exception(e.getMessage());
        }

        // Asumming firt 4 columns are record_id, dataset_id, version, and delete_versions
        // which is common to all emf datasets
        String cols = "SERIAL,Primary Key(Record_Id),BIGINT,INTEGER,TEXT";
        int colCount = md.getColumnCount();

        for (int i = 5; i <= colCount; i++) {
            String type = md.getColumnTypeName(i);

            if (type.toUpperCase().startsWith("VARCHAR"))
                cols += "," + type.toUpperCase() + "(" + md.getPrecision(i) + ")";
            else if (type.toUpperCase().startsWith("FLOAT"))
                cols += ",double precision";
            else
                cols += "," + type;
        }

        return cols;
    }

    public boolean checkTableDefinitions(int srcDSid, int targetDSid) throws EmfException {
        DbServer dbServer = dbServerFactory.getDbServer();
        Session session = sessionFactory.getSession();

        try {
            Datasource datasource = dbServer.getEmissionsDatasource();
            EmfDataset srcDS = dao.getDataset(session, srcDSid);
            InternalSource[] srcSources = srcDS.getInternalSources();
            EmfDataset targetDS = dao.getDataset(session, targetDSid);
            InternalSource[] targetSources = targetDS.getInternalSources();

            DataModifier dataModifier = datasource.dataModifier();

            if (srcSources.length != targetSources.length)
                throw new EmfException("Source dataset set has different number of tables from target dataset.");

            for (int i = 0; i < targetSources.length; i++) {
                String srcTable = datasource.getName() + "." + srcSources[i].getTable();
                String targetTable = datasource.getName() + "." + targetSources[i].getTable();

                // VERSIONS TABLE - Completed - throws exception if the following case is true
                if ("emissions.versions".equalsIgnoreCase(srcTable) && "emissions.versions".equalsIgnoreCase(targetTable)) {
                    throw new EmfException("Table versions moved to schema emf."); // VERSIONS TABLE
                }
                
                String[] srcCols = getTableColumns(dataModifier, srcTable, "");
                String[] targetCols = getTableColumns(dataModifier, targetTable, "");

                if (!areColumnsMatched(srcCols, targetCols))
                    return false;
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error("Could not query table : ", e);
            throw new EmfException("Could not query table: " + e.getMessage());
        } finally {
            closeDB(dbServer);
            session.close();
        }

    }

    private boolean areColumnsMatched(String[] srcColumns, String[] targetColumns) {
        if (srcColumns.length != targetColumns.length)
            return false;

        for (int i = 0; i < srcColumns.length; i++)
            if (!srcColumns[i].equalsIgnoreCase(targetColumns[i]))
                return false;

        // NOTE: better if column types are also compared.

        return true;
    }

    private String getLineBasedSrcColString(int targetDSid, int targetDSVersion, String[] cols, double startLineNum,
            double increatment, String srcTable, String filter, VersionedDatasetQuery dsQuery) {
        // NOTE: assume first 4 columns are record_id, dataset_id, version, delete_versions
        // which is common for dataset tables. Omit the first column.
        // delete_versions is overwritten with blank string ''
        String colString = "SELECT " + targetDSid + " AS dataset_id, " + targetDSVersion
                + " AS version, '' AS delete_versions";

        int numOfCols = cols.length;
        String colsOtherThanLineNumber = "";

        for (int i = 4; i < numOfCols; i++) {
            if (cols[i].equals("line_number"))
                colString += ", row_number() over w  + " + startLineNum + " AS line_number";
//                colString += ", public.run_sum(" + startLineNum + ", incrementor, 'line_no'::text) AS line_number";
            else {
                colString += ", " + cols[i];
                colsOtherThanLineNumber += cols[i] + ", ";
            }
        }

        colString += " FROM (SELECT " + colsOtherThanLineNumber + " " + increatment + " AS incrementor " + "FROM "
                + srcTable + " WHERE " + dsQuery.getVersionQuery() + filter + " ORDER BY line_number) AS srctbl "
                + " WINDOW w AS (ORDER BY 1)";

        return colString;
    }

    private String getSrcColString(int targetDSid, int targetDSVersion, String[] srcCols, String[] targetCols) {
        // NOTE: assume first 4 columns are record_id, dataset_id, version, delete_versions
        // which is common for dataset tables. Omit the first column.
        // delete_versions is overwritten with blank string ''
        String colString = targetDSid + " AS dataset_id, " + targetDSVersion + " AS version,  '' AS delete_versions";
        int numOfTargetCols = targetCols.length;

        for (int i = 4; i < numOfTargetCols; i++)
            colString += ", " + srcCols[i];

        return colString;
    }

    private String getTargetColString(String[] cols) {
        // NOTE: assume first 4 columns are record_id, dataset_id, version, delete_versions
        // which is common for dataset tables. Omit the first column.
        String colString = cols[1];
        int numOfCols = cols.length;

        for (int i = 2; i < numOfCols; i++)
            colString += ", " + cols[i];

        return colString;
    }

    public void replaceColValues(String table, String col, String find, String replaceWith, Version version,
            String filter) throws EmfException {
        DbServer dbServer = dbServerFactory.getDbServer();

        try {
            Datasource datasource = dbServer.getEmissionsDatasource();
            DataModifier dataModifier = datasource.dataModifier();
            VersionedQuery versionedQuery = new VersionedQuery(version);
            Map<String,Column> columnMap = getColumnMap(table.replace("emissions.", ""));

            //make sure we are only finding and replacing on the following data types;
            //due to precision issues with floats, doubles, timestamp we won't allow these to be replaced... 
            String sqlType = columnMap.get(col).getSqlType();
            if (!(
                    sqlType.startsWith("VARCHAR")
                    || sqlType.startsWith("TEXT")
                    || sqlType.startsWith("INTEGER")
                    || sqlType.startsWith("INT2")
                    || sqlType.startsWith("BIGINT")
                    )
            ) throw new EmfException("Only these data types; varchar, text, smallint, integer, bigint; can be replaced.");
                
            
            String[] cols = getTableColumns(dataModifier, table, "");

            
            int vNum = version.getVersion();
            String whereClause = "";
            String selectQuery = "";
            String selectCurVerQuery = "";
            String insertQuery = "";
            String updateQuery = "";
            String updateDelVersions = "";
            
            if (find.contains("%")){
                whereClause = " WHERE " + col + " LIKE '" + find + "' AND (" + versionedQuery.query() + ")"
                + (filter == null || filter.isEmpty() ? "" : " AND (" + filter + ")") 
                + " AND dataset_id = " + version.getDatasetId() + " AND version <> " + vNum;
                
                selectQuery = " SELECT " + getSrcColString(version.getDatasetId(), vNum, cols, cols) + " FROM "
                + table + whereClause;

                selectCurVerQuery = " SELECT " + getSrcColString(version.getDatasetId(), vNum, cols, cols)
                + " FROM " + table + " WHERE " + col + " LIKE '" + find + "' AND (" + versionedQuery.query() + ")"
                + (filter == null || filter.isEmpty() ? "" : " AND (" + filter + ")")
                + " AND dataset_id = " + version.getDatasetId() + " AND version = " + vNum;
                
                insertQuery = "INSERT INTO " + table + "(" + getTargetColString(cols) + ")" + selectQuery;

                updateQuery = "UPDATE " + table + " SET " + col + "='" + replaceWith + "' WHERE " + col + " LIKE '"
                + find + "' "
                + (filter == null || filter.isEmpty() ? "" : " AND (" + filter + ")") 
                + " AND version=" + vNum + " AND dataset_id = " + version.getDatasetId() ; 
                
                updateDelVersions = "UPDATE " + table + " SET delete_versions = trim(both ',' from coalesce(delete_versions,'')||'," 
                + vNum + "') " + whereClause;
            }
            
            else {
                whereClause = " WHERE coalesce(" + col + "::text,'')='" + find + "' AND (" + versionedQuery.query() + ")"
                + (filter == null || filter.isEmpty() ? "" : " AND (" + filter + ")") 
                + " AND dataset_id = " + version.getDatasetId() + " AND version <> " + vNum;

                selectQuery = " SELECT " + getSrcColString(version.getDatasetId(), vNum, cols, cols) + " FROM "
                + table + whereClause;

                selectCurVerQuery = " SELECT " + getSrcColString(version.getDatasetId(), vNum, cols, cols)
                + " FROM " + table + " WHERE coalesce(" + col + "::text,'')='" + find + "' AND (" + versionedQuery.query() + ")"
                + (filter == null || filter.isEmpty() ? "" : " AND (" + filter + ")")
                + " AND dataset_id = " + version.getDatasetId() + " AND version = " + vNum;

                insertQuery = "INSERT INTO " + table + "(" + getTargetColString(cols) + ")" + selectQuery;

                updateQuery = "UPDATE " + table + " SET " + col + "='" + replaceWith + "' WHERE coalesce(" + col + "::text,'')='"
                + find + "' "
                + (filter == null || filter.isEmpty() ? "" : " AND (" + filter + ")") 
                + " AND version=" + vNum + " AND dataset_id = " + version.getDatasetId() ;

                updateDelVersions = "UPDATE " + table + " SET delete_versions = trim(both ',' from coalesce(delete_versions,'')||'," 
                + vNum + "') " + whereClause;

            }

            if (DebugLevels.DEBUG_16()) {
                System.out.println("Query to select records: " + selectQuery);
                System.out.println("Query to select records in current version: " + selectCurVerQuery);
                System.out.println("Query to insert records: " + insertQuery);
                System.out.println("Query to replace column values: " + updateQuery);
                System.out.println("Query to update previous delete_versions: " + updateDelVersions);
            }

            // NOTE: replace values of records in previous versions and also in current version
            if (dataModifier.resultExists(selectQuery)) {
                dataModifier.execute(insertQuery);
                dataModifier.execute(updateQuery);
                dataModifier.execute(updateDelVersions);
                return;
            }

            // NOTE: replace values of records only in current version
            if (dataModifier.resultExists(selectCurVerQuery)) {
                dataModifier.execute(updateQuery);
                return;
            }

            // NOTE: if no records found in previous version and current version, throw exception
            throw new EmfException("No record found for column = '" + col + "' and value  LIKE '" + find + "'.");
        } catch (SQLException e) {
            LOG.error("Could not query table : ", e);
            throw new EmfException("Could not query table.");
        } catch (Exception e) {
            LOG.error("Error : ", e);
            throw new EmfException(e.getMessage());
        } finally {
            closeDB(dbServer);
        }
    }
    
    public void replaceColValues(String table, String findFilter, String replaceWith, Version version, 
            String filter) throws EmfException {
        DbServer dbServer = dbServerFactory.getDbServer();

        try {
            Datasource datasource = dbServer.getEmissionsDatasource();
            DataModifier dataModifier = datasource.dataModifier();
            VersionedQuery versionedQuery = new VersionedQuery(version);
//            Map<String,Column> columnMap = getColumnMap(table.replace("emissions.", ""));
//
//            //make sure we are only finding and replacing onm the following data types;
//            //due to precision issues with floats, doubles, timestamp we won't allow these to be replaced... 
//            String sqlType = columnMap.get(col).getSqlType();
//            if (!(
//                    sqlType.startsWith("VARCHAR")
//                    || sqlType.startsWith("TEXT")
//                    || sqlType.startsWith("INTEGER")
//                    || sqlType.startsWith("INT2")
//                    || sqlType.startsWith("BIGINT")
//                    )
//            ) throw new EmfException("Only these data types; varchar, text, smallint, integer, bigint; can be replaced.");
//                
//            
            String[] cols = getTableColumns(dataModifier, table, "");

            
            int vNum = version.getVersion();
            String useWhere = findFilter.trim().toUpperCase().startsWith("WHERE")? "" : " WHERE ";
            String whereClause = "";
            String selectQuery = "";
            String selectCurVerQuery = "";
            String insertQuery = "";
            String updateQuery = "";
            String updateDelVersions = "";
            

            //include SQL based filter (findFilter), also include dataset editor defined filter(filter) (from toolbar),
            //and include all records for this version except for records with this version number 
            //(e.g., version 3, include relevant version 2,1, and 0 records but not version 3 records)
            whereClause = " " + useWhere + " (" + versionedQuery.query() + ")" 
            + (findFilter == null || findFilter.isEmpty() ? "" : " AND (" + findFilter + ")") 
            + (filter == null || filter.isEmpty() ? "" : " AND (" + filter + ")") 
            + " AND dataset_id = " + version.getDatasetId() + " AND version <> " + vNum;

            //get relevant records to use using above filter, these will be new records, and columns for the new records will be updated  
            //in a later UPDATE step
            selectQuery = " SELECT " + getSrcColString(version.getDatasetId(), vNum, cols, cols) + " FROM "
            + table + whereClause;
            
            //insert these new records...
            insertQuery = "INSERT INTO " + table + "(" + getTargetColString(cols) + ")" + selectQuery;

            //target same records as above SELECT statement, except only include the current version records...
            selectCurVerQuery = " SELECT " + getSrcColString(version.getDatasetId(), vNum, cols, cols)
            + " FROM " + table + " " + useWhere + " (" + versionedQuery.query() + ")"
            + (findFilter == null || findFilter.isEmpty() ? "" : " AND (" + findFilter + ")") 
            + (filter == null || filter.isEmpty() ? "" : " AND (" + filter + ")")
            + " AND dataset_id = " + version.getDatasetId() + " AND version = " + vNum;

            //update new records with new values...
            updateQuery = "UPDATE " + table + " SET " + replaceWith + " " + useWhere 
            + " version=" + vNum + " AND dataset_id = " + version.getDatasetId()  
            + (findFilter == null || findFilter.isEmpty() ? "" : " AND (" + findFilter + ")") 
            + (filter == null || filter.isEmpty() ? "" : " AND (" + filter + ")"); 

            updateDelVersions = "UPDATE " + table + " SET delete_versions = trim(both ',' from coalesce(delete_versions,'')||'," 
            + vNum + "') " + whereClause;            
            System.out.println("debug16: "+DebugLevels.DEBUG_16());
            if (DebugLevels.DEBUG_16()) {
                System.out.println("Query to select records: " + selectQuery);
                System.out.println("Query to select records in current version: " + selectCurVerQuery);
                System.out.println("Query to insert records: " + insertQuery);
                System.out.println("Query to replace column values: " + updateQuery);
                System.out.println("Query to update previous delete_versions: " + updateDelVersions);
            }

            // NOTE: replace values of records in previous versions and also in current version
            if (dataModifier.resultExists(selectQuery)) {
                dataModifier.execute(insertQuery);
                dataModifier.execute(updateQuery);
                dataModifier.execute(updateDelVersions);
                return;
            }

            // NOTE: replace values of records only in current version
            if (dataModifier.resultExists(selectCurVerQuery)) {
                dataModifier.execute(updateQuery);
                return;
            }

            // NOTE: if no records found in previous version and current version, throw exception
            throw new EmfException("No record found. ");
        } catch (SQLException e) {
            LOG.error("Could not query table : ", e);
            throw new EmfException("Could not query table.");
        } catch (Exception e) {
            LOG.error("Error : ", e);
            throw new EmfException(e.getMessage());
        } finally {
            closeDB(dbServer);
        }
    }

    private Map<String,Column> getColumnMap(String table) throws EmfException {
        DbServer dbServer = dbServerFactory.getDbServer();

        try {
            Datasource datasource = dbServer.getEmissionsDatasource();

            return new TableMetaData(datasource).getColumnMap(table);
        } catch (SQLException e) {
            LOG.error("Could not query table : ", e);
            throw new EmfException("Could not query table.");
        } catch (Exception e) {
            LOG.error("Error : ", e);
            throw new EmfException(e.getMessage());
        } finally {
            closeDB(dbServer);
        }
    }

    public synchronized void copyDataset(int datasetId, Version version, User user) throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            EmfDataset dataset = dao.getDataset(session, datasetId);
            Date time = new Date();
            DatasetType type = dataset.getDatasetType();

            if (type.isExternal())
                throw new Exception("Copying of a version to a new dataset is not supported for this dataset type: "
                        + type.getName() + ".");
            
            EmfDataset copied = (EmfDataset) DeepCopy.copy(dataset);
            
            String newName = dataset.getName();
            if ( newName != null) {
                newName = newName.trim();
            } else {
                throw new EmfException("Dataset name is null");
            }
            copied.setName(getUniqueNewName("Copy of " + newName + "_v" + version.getVersion()));
            
            copied.setStatus(dataset.getStatus());
            copied.setDescription("#Copied from version " + version.getVersion() + " of dataset " + dataset.getName()
                    + " on " + time + System.getProperty("line.separator") + (dataset.getDescription()==null? "":dataset.getDescription()));
            copied.setCreator(user.getUsername());
            copied.setCreatorFullName(user.getName());
            copied.setDefaultVersion(0);
            copied.setInternalSources(null);

            copied.setCreatedDateTime(time);
            copied.setAccessedDateTime(time);
            copied.setModifiedDateTime(time);

            session.clear();
            dao.add(copied, session);
            EmfDataset loaded = dao.getDataset(session, copied.getName());
            EmfDataset locked = dao.obtainLocked(user, loaded, session);

            if (locked == null)
                throw new EmfException("Errror copying dataset: can't obtain lock to update copied dataset.");

            copyDatasetTable(dataset, version, loaded, user, session);

            Version defaultVersion = new Version(0);
            defaultVersion.setName("Initial Version");
            defaultVersion.setPath("");
            defaultVersion.setCreator(user);
            defaultVersion.setDatasetId(locked.getId());
            defaultVersion.setLastModifiedDate(time);
            defaultVersion.setNumberRecords(version.getNumberRecords());
            defaultVersion.setFinalVersion(true);
            defaultVersion.setDescription("");
            session.clear();
            dao.add(defaultVersion, session);
        } catch (Exception e) {
            String error = "Error copying dataset...";
            String msg = e.getMessage();
            LOG.error(error, e);
            throw new EmfException(msg == null ? error : msg.substring(msg.length() > 150 ? msg.length() - 150 : 0));
        } finally {
            session.close();
        }

    }

    private void copyDatasetTable(EmfDataset dataset, Version version, EmfDataset copied, User user, Session session)
            throws Exception {
        InternalSource[] sources = dataset.getInternalSources();

        if (sources == null || sources.length == 0)
            return;

        DbServer dbServer = dbServerFactory.getDbServer();
        Datasource emisSrc = dbServer.getEmissionsDatasource();
        String schema = emisSrc.getName() + ".";
        InternalSource src = sources[0];
        DataTable tableData = new DataTable(copied, emisSrc);
        TableCreator tCreator = new TableCreator(emisSrc);
        
        String origTable = schema + src.getTable();
        String[] cols = getTableColumns(emisSrc.dataModifier(), origTable, "");
        String[] colNameTypes = getColNameTypes(emisSrc.dataModifier(), origTable);
        String colNameString = colString(cols, 0);
        String colTypesString = getColTypesString(emisSrc.dataModifier(), origTable);
        
        String newTable = tableData.name();
        String consldTable = getConsldTableName(dataset, tCreator,  colNameString, colTypesString, newTable, cols.length);
            
        VersionedDatasetQuery queryOrigData = new VersionedDatasetQuery(version, dataset);

        if (sources.length == 1) {
            String create = "CREATE TABLE " + schema + newTable + " (" + colString(colNameTypes, 0) + ")";

            if (consldTable == null)
                emisSrc.tableDefinition().execute(create);
            else
                newTable = consldTable;
            
            String insert = "INSERT INTO " + schema + newTable + "(" + colString(cols, 1) + ") SELECT "
                + getSrcColString(copied.getId(), 0, cols, cols) + " FROM " + origTable + " "
                + queryOrigData.versionWhereClause();
            
            emisSrc.tableDefinition().execute(insert);

            src.setSource(dataset.getName() + " version: " + version.getVersion());
            src.setTable(newTable);
            copied.setInternalSources(new InternalSource[] { src });
            dao.update(copied, session);
            dao.releaseLocked(user, copied, session);
        }
        
        if (sources.length > 1) {
            for (int i = 0; i < sources.length; i++) {
                String table = schema + sources[i].getTable();
                String[] tableCols = getTableColumns(emisSrc.dataModifier(), table, "");
                String insert = "INSERT INTO " + table + " (" + colString(tableCols, 1) + ") SELECT "
                    + getSrcColString(copied.getId(), 0, tableCols, tableCols) + " FROM " + table + " "
                    + queryOrigData.versionWhereClause();
            
                emisSrc.tableDefinition().execute(insert);
                sources[i].setSource(dataset.getName() + " version: " + version.getVersion());
            }
            
            copied.setInternalSources(sources);
            dao.update(copied, session);
            dao.releaseLocked(user, copied, session);
        }
    }

    private String getConsldTableName(EmfDataset dataset, TableCreator tCreator, String colNameString,
            String colTypesString, String newTable, int numOfCols) throws Exception {
        DatasetType type = dataset.getDatasetType();
        String imptrClass = type.getImporterClassName();
        
        if (imptrClass.equals("gov.epa.emissions.commons.io.other.SMKReportImporter") //These datasets' tables are consolidated
                || imptrClass.equals("gov.epa.emissions.commons.io.csv.CSVImporter")
                || imptrClass.equals("gov.epa.emissions.commons.io.generic.LineImporter")) {
            String existedTable = tCreator.checkTableConsolidation(colNameString, colTypesString, dataset);
            
            if (existedTable != null && !existedTable.isEmpty())
                return existedTable;
                
            tCreator.addConsolidationItem(numOfCols, newTable,  colNameString, colTypesString, dataset);
        }
        
        return null;
    }

    private String colString(String[] cols, int start) {
        int len = cols.length;
        String colString = "";

        for (int i = start; i < len - 1; i++)
            colString += cols[i] + ",";

        colString += cols[len - 1];

        return colString;
    }

    private String getUniqueNewName(String name) throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            List<String> names = dao.getDatasetNamesStartWith(name, session);

            if (names == null || names.size() == 0)
                return name;

            return name + " " + getSequence(name, names);
        } catch (Exception e) {
            LOG.error("Could not get all dataset names.\n", e);
            throw new EmfException(e.getMessage());
        } finally {
            session.close();
        }
    }

    private int getSequence(String stub, List<String> names) {
        int sequence = names.size() + 1;
        String integer = "";

        try {
            for (Iterator<String> iter = names.iterator(); iter.hasNext();) {
                integer = iter.next().substring(stub.length()).trim();

                if (!integer.isEmpty()) {
                    int temp = Integer.parseInt(integer);

                    if (temp == sequence)
                        ++sequence;
                    else if (temp > sequence)
                        sequence = temp + 1;
                }
            }

            return sequence;
        } catch (Exception e) {
            // NOTE: Assume one dataset won't be copied 10000 times.
            // This is farely safe assuming the random number do not duplicate.
            return Math.abs(new Random().nextInt()) % 10000;
        }
    }

    public void addExternalSources(String folder, String[] files, int datasetId) throws EmfException {
        Session session = sessionFactory.getSession();
        EmfDataset ds = null;

        try {
            ds = dao.getDataset(session, datasetId);
            ExternalSource[] srcs = reconstructExtSrcs(folder, files, datasetId);
            dao.addExternalSources(srcs, session);
        } catch (Exception e) {
            LOG.error("Could not add all external sources for dataset "
                    + (ds == null ? "(id=" + datasetId + ")." : ds.getName() + "."), e);
            throw new EmfException("Could not add all external sources for dataset "
                    + (ds == null ? "(id=" + datasetId + ")." : ds.getName() + "."));
        } finally {
            if (session != null && session.isConnected())
                session.close();
        }
    }

    private ExternalSource[] reconstructExtSrcs(String dir, String[] files, int datasetId) {
        int len = files.length;
        ExternalSource[] srcs = new ExternalSource[len];
        String sep = getSeparator(dir);
        dir += dir.endsWith(sep) ? "" : sep;

        for (int i = 0; i < len - 1; i++) {
            srcs[i] = new ExternalSource();
            srcs[i].setDatasetId(datasetId);
            srcs[i].setDatasource(dir + files[i]);
        }

        return srcs;
    }

    private String getSeparator(String file) {
        if (file == null)
            return "/";

        file = file.trim();

        if (file.startsWith("/") || file.startsWith("./") || file.startsWith("../"))
            return "/";

        return "\\";
    }

    public ExternalSource[] getExternalSources(int datasetId, int limit, String filter) throws EmfException {
        Session session = sessionFactory.getSession();
        EmfDataset ds = null;

        try {
            ds = dao.getDataset(session, datasetId);
            return dao.getExternalSrcs(datasetId, limit, filter, session);
        } catch (Exception e) {
            LOG.error("Could not get all external sources for dataset "
                    + (ds == null ? "(id=" + datasetId + ")." : ds.getName() + "."), e);
            throw new EmfException("Could not get all external sources for dataset "
                    + (ds == null ? "(id=" + datasetId + ")." : ds.getName() + "."));
        } finally {
            if (session != null && session.isConnected())
                session.close();
        }
    }
    
    public int getNumExternalSources(int datasetId, String filter) throws EmfException {
        return getExternalSources(datasetId, -1, filter).length;
    }

    public boolean isExternal(int datasetId) throws EmfException {
        Session session = sessionFactory.getSession();
        EmfDataset ds = null;

        try {
            ds = dao.getDataset(session, datasetId);
            return dao.isExternal(datasetId, session);
        } catch (Exception e) {
            LOG.error("Could not determine externality for dataset "
                    + (ds == null ? "(id=" + datasetId + ")." : ds.getName() + "."), e);
            throw new EmfException("Could not determine externality for dataset "
                    + (ds == null ? "(id=" + datasetId + ")." : ds.getName() + "."));
        } finally {
            if (session != null && session.isConnected())
                session.close();
        }
    }

    public synchronized void updateExternalSources(int datasetId, String newDir) throws EmfException {
        Session session = sessionFactory.getSession();
        EmfDataset ds = null;

        try {
            ds = dao.getDataset(session, datasetId);

            if (ds == null)
                throw new EmfException("Dataset (id=" + datasetId + ") doesn't exist.");

            if (!ds.getDatasetType().isExternal())
                throw new EmfException("Dataset (" + ds.getName() + ") type is not external.");

            ExternalSource[] srcs = dao.getExternalSrcs(datasetId, -1, null, session);
            
            if (srcs == null || srcs.length == 0)
                throw new EmfException("Dataset (" + ds.getName() + ") has no sources to update.");

            String firstSrc = srcs[0].getDatasource();
            String oldsep = getSeparator(firstSrc);
            String newsep = getSeparator(newDir);

            for (int i = 0; i < srcs.length; i++) {
                String src = srcs[i].getDatasource();

                if (src == null || src.trim().isEmpty())
                    continue;

                int index = src.lastIndexOf(oldsep);

                if (index < 0)
                    continue;

                if (index == src.trim().length() - 1) {
                    srcs[i].setDatasource(newDir);
                    continue;
                }

                String temp = src.substring(index + 1); // File name
                srcs[i].setDatasource(newDir + newsep + temp);
            }

            // NOTE: assume this is locked by editing the dataset; need to take care if other porcesses update
            // external sources at the same time
            dao.updateExternalSrcsWithoutLocking(srcs, session);
        } catch (Exception e) {
            LOG.error("Could not update sources for dataset "
                    + (ds == null ? "(id=" + datasetId + ")." : ds.getName() + "."), e);
            throw new EmfException("Could not update sources for dataset "
                    + (ds == null ? "(id=" + datasetId + ")." : ds.getName() + "."));
        } finally {
            if (session != null && session.isConnected())
                session.close();
        }
    }
    
    public synchronized void deleteRecords (User user, String table, Version version,
            String filter) throws EmfException {
        DbServer dbServer = dbServerFactory.getDbServer();

        try {
            Datasource datasource = dbServer.getEmissionsDatasource();
            DataModifier dataModifier = datasource.dataModifier();
            VersionedQuery versionedQuery = new VersionedQuery(version);
            int vNum = version.getVersion();

            String whereClause = " WHERE " + versionedQuery.query()
                    + (filter == null || filter.isEmpty() ? "" : " AND (" + filter + ")");

            //NOTE: delete values of records only in current version
            String deleteCurVerQuery = "DELETE FROM " + table + whereClause + " AND version = " + vNum;

            //NOTE: Update delete versions column
            String updateDelVersions = "UPDATE " + table + " SET delete_versions = coalesce(delete_versions,'')||',"
                    + vNum + "'" + whereClause + " AND version <> " + vNum;

            if (DebugLevels.DEBUG_16()) {
                System.out.println("Query to delete records: " + deleteCurVerQuery);
                System.out.println("Query to update previous delete_versions: " + updateDelVersions);
            }

            dataModifier.execute(deleteCurVerQuery);
            dataModifier.execute(updateDelVersions);
        } catch (SQLException e) {
            LOG.error("Could not query table : ", e);
            throw new EmfException("Could not query table.");
        } catch (Exception e) {
            LOG.error("Error : ", e);
            throw new EmfException(e.getMessage());
        } finally {
            closeDB(dbServer);
        }
    }
    
    public synchronized int getNumOfRecords (String table, Version version,
            String filter) throws EmfException {
        DbServer dbServer = dbServerFactory.getDbServer();

        try {
            Datasource datasource = dbServer.getEmissionsDatasource();
            DataModifier dataModifier = datasource.dataModifier();
            VersionedQuery versionedQuery = new VersionedQuery(version);

            String whereClause = " WHERE " + versionedQuery.query()
                    + (filter == null || filter.isEmpty() ? "" : " AND (" + filter + ")");

            String countQuery = "SELECT COUNT(*) FROM " + table + whereClause;
            
            return Integer.parseInt(dataModifier.getRowCount(countQuery)+ "");
        } catch (SQLException e) {
            LOG.error("Could not query table : ", e);
            throw new EmfException("Please check data table name and/or the syntax of row filter.");
        } catch (Exception e) {
            LOG.error("Error: ", e);
            throw new EmfException(e.getMessage());
        } finally {
            closeDB(dbServer);
        }
    }
    
    public synchronized Integer[] getNumOfRecords (int datasetId, Version[] versions, String tableName) throws EmfException {
        DbServer dbServer = dbServerFactory.getDbServer();
        Session session = sessionFactory.getSession();
        try {
            EmfDataset dataset = getDataset(datasetId);
            return dao.getDatasetRecordsNumber(dbServer, session, dataset, versions, tableName);
        } catch (Exception e) {
            LOG.error("Error: ", e);
            throw new EmfException(e.getMessage());
        } finally {
            if (session != null && session.isConnected())
                session.close();
            closeDB(dbServer);
        }
    }

    public String[] getTableColumnDistinctValues(int datasetId, int datasetVersion, String columnName, String whereFilter,
            String sortOrder) throws EmfException {
        DbServer dbServer = dbServerFactory.getDbServer();
        Session session = sessionFactory.getSession();
        try {
            return dao.getTableColumnDistinctValues(datasetId, datasetVersion, 
                    columnName, whereFilter, 
                    sortOrder, session, 
                    dbServer);
        } catch (Exception e) {
            LOG.error("Error: ", e);
            throw new EmfException(e.getMessage());
        } finally {
            if (session != null && session.isConnected())
                session.close();
            closeDB(dbServer);
        }
    }

    public EmfDataset[] findDatasets(EmfDataset dataset, String qaStep, String qaArgument, 
            int[] usedByCasesID, String dataValueFilter, boolean unconditional, int userId) throws EmfException {
        Session session = sessionFactory.getSession();
        
        try {
            return dao.findSimilarDatasets(dataset, qaStep, qaArgument, usedByCasesID, 
                    dataValueFilter, unconditional, userId, session).toArray(new EmfDataset[0]);
        } catch (Exception e) {
            LOG.error("Could not find similar datasets.", e);
            throw new EmfException("Could not find similar datasets: " + e.getMessage());
        } finally {
            session.close();
        }
    }

    public void updateVersion(Version locked) throws EmfException {
        Session session = this.sessionFactory.getSession();

        try {
            dao.updateVersion(locked, session);
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        } finally {
            session.close();
        }
    }
    
    public boolean checkBizzareCharInColumn(int datasetId, int version, String colName) throws EmfException {
        Session session = sessionFactory.getSession();
        DbServer dbServer = dbServerFactory.getDbServer();
        
        EmfDataset ds = null;

        try {
            ds = dao.getDataset(session, datasetId);
            return dao.checkBizzareCharInColumn(dbServer, session, datasetId, version, colName);
        } catch (Exception e) {
            LOG.error("Could not checkBizzareChar for dataset "
                    + (ds == null ? "(id=" + datasetId + ")." : ds.getName() + "."), e);
            throw new EmfException("Could not checkBizzareChar for dataset "
                    + (ds == null ? "(id=" + datasetId + ")." : ds.getName() + "."));
        } finally {
            if (session != null && session.isConnected())
                session.close();
        }
    }
    
    private int detectFirstInvalidChar( String xmlStr) {
        if ( xmlStr == null || xmlStr.equals(""))
            return -1;
        for ( int i=0; i<xmlStr.length(); i++) {
            int ch = xmlStr.charAt(i);
            if ( XMLChar.isInvalid( ch)) 
                return i;
        }
        return -2;
    }
}
