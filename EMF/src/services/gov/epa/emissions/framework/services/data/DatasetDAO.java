package gov.epa.emissions.framework.services.data;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.ExternalSource;
import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.data.KeyVal;
import gov.epa.emissions.commons.db.DataQuery;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.TableCreator;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.db.version.Versions;
import gov.epa.emissions.commons.io.VersionedQuery;
import gov.epa.emissions.commons.io.importer.DataTable;
import gov.epa.emissions.commons.io.importer.ImporterException;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.commons.util.EmfArrays;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfDbServer;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.AccessLog;
import gov.epa.emissions.framework.services.basic.Status;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.casemanagement.CaseDAO;
import gov.epa.emissions.framework.services.casemanagement.CaseInput;
import gov.epa.emissions.framework.services.casemanagement.outputs.CaseOutput;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.services.cost.ControlStrategyInputDataset;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;
import gov.epa.emissions.framework.services.editor.Revision;
import gov.epa.emissions.framework.services.persistence.HibernateFacade;
import gov.epa.emissions.framework.services.persistence.LockingScheme;
import gov.epa.emissions.framework.tasks.DebugLevels;
import gov.epa.emissions.framework.utils.Utils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

public class DatasetDAO {

    private static Log LOG = LogFactory.getLog(DatasetDAO.class);

    private LockingScheme lockingScheme;

    private HibernateFacade hibernateFacade;

    private DbServerFactory dbServerFactory;
    
    private List strategyList = null;

    private List controlProgList = null;

    public DatasetDAO() {
        lockingScheme = new LockingScheme();
        hibernateFacade = new HibernateFacade();
    }

    public DatasetDAO(DbServerFactory dbServerFactory) {
        this();
        this.dbServerFactory = dbServerFactory;
    }

    public boolean exists(int id, Class clazz, Session session) {
        return hibernateFacade.exists(id, clazz, session);
    }

    /*
     * Return true if the name is already used
     */
    public boolean nameUsed(String name, Class clazz, Session session) {
        return hibernateFacade.nameUsed(name, clazz, session); // case insensitive comparison
    }

    public Object current(int id, Class clazz, Session session) {
        return hibernateFacade.current(id, clazz, session);
    }

    public boolean canUpdate(EmfDataset dataset, Session session) throws Exception {
        return canUpdate(dataset.getId(), dataset.getName(), session);
    }

    private boolean canUpdate(int id, String newName, Session session) throws Exception {
        if (!exists(id, EmfDataset.class, session)) {
            throw new EmfException("Dataset with id=" + id + " does not exist.");
        }

        EmfDataset current = (EmfDataset) current(id, EmfDataset.class, session);
        session.clear();// clear to flush current
        if (current.getName().equalsIgnoreCase(newName))
            return true;

        return !datasetNameUsed(newName, session);
    }

    public boolean exists(String name, Session session) {
        return hibernateFacade.exists(name, EmfDataset.class, session);
    }

    public List all(Session session) {
        return hibernateFacade.getAll(EmfDataset.class, session);
    }

    public int getNumOfDatasets(int userId, Session session) {
        String dsts = "( select DT from DatasetType as DT " 
            + "where "
            + " DT.id not in (select EDT.id from User as U "
            + " inner join U.excludedDatasetTypes as EDT where U.id = "
            + userId + ")" 
            + " order by DT.name )";
        
        List<?> num = session.createQuery("SELECT COUNT(ds.id) from EmfDataset as ds " 
                + " where ds.status <> 'Deleted' and ds.datasetType.id in " + dsts )
                .list();
        return Integer.parseInt(num.get(0).toString());
    }

    public int getNumOfDatasets(Session session, String name, int userId) {
        String ns = Utils.getPattern(name.toLowerCase().trim());
        String dsts = "( select DT from DatasetType as DT " 
            + "where "
            + " DT.id not in (select EDT.id from User as U "
            + " inner join U.excludedDatasetTypes as EDT where U.id = "
            + userId + ")" 
            + " order by DT.name )";
      
        List<?> num = session.createQuery(
                "SELECT COUNT(ds.id) from EmfDataset as ds where ds.status <> 'Deleted' " + " AND lower(ds.name) like "
                        + ns + " and ds.datasetType.id in " + dsts ).list();
        return Integer.parseInt(num.get(0).toString());
    }

    public int getNumOfDatasets(Session session, int dsTypeId) {
        List<?> num = session.createQuery(
                "SELECT COUNT(ds.id) from EmfDataset as ds where ds.status <> 'Deleted' " + " AND ds.datasetType.id = "
                        + dsTypeId).list();

        return Integer.parseInt(num.get(0).toString());
    }

    public int getNumOfDatasets(Session session, int dsTypeId, String name) {
        String ns = Utils.getPattern(name.toLowerCase().trim());
        List<?> num = session.createQuery(
                "SELECT COUNT(ds.id) from EmfDataset as ds where ds.status <> 'Deleted' " + " AND ds.datasetType.id = "
                        + dsTypeId + " AND lower(ds.name) like " + ns).list();

        return Integer.parseInt(num.get(0).toString());
    }

    // FIXME: to be deleted after dataset removed from db
    public List allNonDeleted(Session session, int userId) {
        String dsts = "( select DT from DatasetType as DT " 
            + "where "
            + " DT.id not in (select EDT.id from User as U "
            + " inner join U.excludedDatasetTypes as EDT where U.id = "
            + userId + ")" 
            + " order by DT.name )";
        return session
                .createQuery(
                        "select new EmfDataset(DS.id, DS.name, DS.defaultVersion, DS.modifiedDateTime, DS.datasetType.id, DS.datasetType.name, DS.status, DS.creator, DS.creatorFullName, IU.name, P.name, R.name, DS.startDateTime, DS.stopDateTime, DS.temporalResolution) "
                                + " from EmfDataset as DS left join DS.intendedUse as IU left join DS.project as P left join DS.region as R "
                                + " where DS.status <> 'Deleted' and DS.datasetType.id in " + dsts + "order by DS.name").list();
    }

    public List allNonDeleted(Session session, String nameContains, int userId) {
        String ns = Utils.getPattern(nameContains.toLowerCase().trim());
        String dsts = " (select DT from DatasetType as DT " 
            + "where "
            + " DT.id not in (select EDT.id from User as U "
            + " inner join U.excludedDatasetTypes as EDT where U.id = "
            + userId + ")" 
            + " order by DT.name ) ";
        return session
                .createQuery(
                        "select new EmfDataset(DS.id, DS.name, DS.defaultVersion, DS.modifiedDateTime, DS.datasetType.id, DS.datasetType.name, DS.status, DS.creator, DS.creatorFullName, IU.name, P.name, R.name, DS.startDateTime, DS.stopDateTime, DS.temporalResolution) "
                                + " from EmfDataset as DS left join DS.intendedUse as IU left join DS.project as P left join DS.region as R "
                                + " where lower(DS.name) like "
                                + ns
                                + " and DS.status <> 'Deleted' and DS.datasetType.id in " + dsts 
                                + " order by DS.name").list();
    }

    public void add(EmfDataset dataset, Session session) throws EmfException {
        //NOTE: to trim the leading and trailing spaces???
        String name = dataset.getName();
        
        String newName = name;
        if ( newName != null) {
            newName = newName.trim();
        } else {
            throw new EmfException("Dataset name is null");
        }
        dataset.setName(newName);
        
        hibernateFacade.add(dataset, session);
    }

    public void add(Version version, Session session) {
        hibernateFacade.add(version, session);
    }

    // NOTE: make sure dataset has no changes in name, emission table name(s)
    // when call this update method. Not for updating status to be 'Deleted'.
    public void updateDSPropNoLocking(EmfDataset dataset, Session session) throws Exception {
        hibernateFacade.updateOnly(dataset, session);
    }

    public void updateWithoutLocking(EmfDataset dataset, Session session) throws Exception {
        try {
            renameEmissionTable(dataset, getDataset(session, dataset.getId()), session);
        } catch (Exception e) {
            LOG.info("Can not rename emission table: " + dataset.getInternalSources()[0].getTable());
        } finally {
            session.clear();
            hibernateFacade.updateOnly(dataset, session);
        }
    }

    public void remove(EmfDataset dataset, Session session) {
        if (DebugLevels.DEBUG_12())
            System.out.println("dataset dao remove(dataset, session) called: " + dataset.getId() + " "
                    + dataset.getName());

        ExternalSource[] extSrcs = null;

        if (dataset.isExternal())
            extSrcs = getExternalSrcs(dataset.getId(), -1, null, session);

        if (extSrcs != null && extSrcs.length > 0)
            hibernateFacade.removeObjects(extSrcs, session);

        hibernateFacade.remove(dataset, session);
    }

    // FIXME: change this method name to indicate mark deleted
    public void remove(User user, EmfDataset dataset, Session session) throws EmfException {
        if (DebugLevels.DEBUG_14())
            System.out.println("DatasetDAO starts removing dataset " + dataset.getName() + " " + new Date());

        // NOTE: method to be modified to really remove dataset. It is only rename it for now.
        if (dataset.getStatus().equalsIgnoreCase("Deleted"))
            return;

        String datasetName = dataset.getName();

        if (dataset.isLocked())
            throw new EmfException("Could not remove dataset " + datasetName + ". It is locked by "
                    + dataset.getLockOwner());

        if (DebugLevels.DEBUG_12()) {
            System.out.println("dataset dao remove(user, dataset, session) called: " + dataset.getId() + " "
                    + datasetName);
            System.out.println("Dataset status: " + dataset.getStatus() + " dataset retrieved null? "
                    + (getDataset(session, dataset.getId()) == null));
        }

        checkIfUsedByCases(new int[] { dataset.getId() }, session);

        // Disabled temporarily according to Alison's request 1/15/2008
        // if (isUsedByControlStrategies(session, dataset))
        // throw new EmfException("Cannot delete \"" + dataset.getName() + "\" - it is use by a control strategy.");

        String prefix = "DELETED_" + new Date().getTime() + "_";

        try {
            String newName = prefix + datasetName;

            if (!canUpdate(dataset.getId(), newName, session)) // Check to see if the new name is available
                throw new EmfException("The Dataset name is already in use: " + dataset.getName());

            DatasetType type = dataset.getDatasetType();

            if (type != null && type.getTablePerDataset() > 1)
                LOG.info("Renaming emission tables for dataset " + dataset.getName() + " is not allowed.");

            EmfDataset locked = obtainLocked(user, dataset, session);

            if (locked == null) {
                LOG.info("Could not get lock on dataset " + dataset.getName() + " to remove it.");
                return;
            }
            
            String name1 = newName;
            if ( newName != null) {
                name1 = name1.trim();
            } else {
                throw new EmfException("Dataset name is null");
            }
            locked.setName(name1);
            
            locked.setStatus("Deleted");

            updateToRemove(locked, dataset, session);
        } catch (Exception e) {
            LOG.error("Could not remove dataset " + datasetName + ".", e);
            throw new EmfException("Could not remove dataset " + datasetName + ". Reason: " + e.getMessage());
        }

        if (DebugLevels.DEBUG_14())
            System.out.println("DatasetDAO has finished removing dataset " + dataset.getName() + " " + new Date());
    }

    public EmfDataset obtainLocked(User user, EmfDataset dataset, Session session) {
        return (EmfDataset) lockingScheme.getLocked(user, current(dataset, session), session);
    }

    public EmfDataset releaseLocked(User user, EmfDataset locked, Session session) {
        return (EmfDataset) lockingScheme.releaseLock(user, current(locked, session), session);
    }

    public Revision update(Revision revision, Session session) throws EmfException {
        return (Revision) lockingScheme.releaseLockOnUpdate(revision, current(revision, session), session);
    }

    public Revision obtainLocked(User user, Revision revision, Session session) {
        return (Revision) lockingScheme.getLocked(user, current(revision, session), session);
    }

    public Revision releaseLocked(User user, Revision revision, Session session) {
        return (Revision) lockingScheme.releaseLock(user, current(revision, session), session);
    }

    public EmfDataset update(EmfDataset locked, Session session) throws Exception {
        EmfDataset toReturn = null;

        try {
            renameEmissionTable(locked, getDataset(session, locked.getId()), session);
        } catch (Exception e) {
            LOG.error("Can not rename emission table: " + locked.getInternalSources()[0].getTable(), e);
        } finally { // to ignore if the rename fails
            if (DebugLevels.DEBUG_12())
                System.out.println("Update dataset " + locked.getName() + " with id: " + locked.getId());

            toReturn = (EmfDataset) lockingScheme.releaseLockOnUpdate(locked, current(locked, session), session);
        }

        return toReturn;
    }

    private void updateToRemove(EmfDataset locked, EmfDataset oldDataset, Session session) throws Exception {
        try {
            if (DebugLevels.DEBUG_14())
                System.out.println("DatasetDAO starts renaming emission table for dataset: " + oldDataset.getName());
            renameEmissionTable(locked, oldDataset, session);
            if (DebugLevels.DEBUG_14())
                System.out.println("DatasetDAO has finished renaming emission table for dataset: "
                        + oldDataset.getName());
        } catch (Exception e) {
            LOG.error("Can not rename emission table: " + locked.getInternalSources()[0].getTable(), e);
        } finally { // to ignore if the rename fails
            if (DebugLevels.DEBUG_12())
                System.out.println("Update to remove " + locked.getName() + " with id: " + locked.getId());

            lockingScheme.releaseLockOnUpdate(locked, current(locked, session), session);
        }
    }

    private EmfDataset current(EmfDataset dataset, Session session) {
        return (EmfDataset) current(dataset.getId(), EmfDataset.class, session);
    }

    private Revision current(Revision revision, Session session) {
        return (Revision) current(revision.getId(), Revision.class, session);
    }

    public List getDatasets(Session session, DatasetType datasetType) {
        Criterion statusCrit = Restrictions.ne("status", "Deleted"); // FIXME: to be deleted after dataset removed
        // from db
        Criterion typeCrit = Restrictions.eq("datasetType", datasetType);
        Criterion criterion = Restrictions.and(statusCrit, typeCrit);
        Order order = Order.asc("name");
        return hibernateFacade.get(EmfDataset.class, criterion, order, session);
    }

    public List getDatasets(Session session, int datasetTypeId) {
        return session
                .createQuery(
                        "select new EmfDataset(DS.id, DS.name, DS.defaultVersion, DS.modifiedDateTime, DS.datasetType.id, DS.datasetType.name, DS.status, DS.creator, DS.creatorFullName, IU.name, P.name, R.name, DS.startDateTime,DS.stopDateTime, DS.temporalResolution) "
                                + " from EmfDataset as DS left join DS.intendedUse as IU left join DS.project as P left join DS.region as R "
                                + " where DS.datasetType.id = "
                                + datasetTypeId
                                + " and DS.status <> 'Deleted' order by DS.name").list();
    }

    public List getDatasetsWithFilter(Session session, int datasetTypeId, String nameContains) {
        String ns = Utils.getPattern(nameContains.toLowerCase().trim());
        return session
                .createQuery(
                        "select new EmfDataset(DS.id, DS.name, DS.defaultVersion, DS.modifiedDateTime, DS.datasetType.id, DS.datasetType.name, DS.status, DS.creator, DS.creatorFullName, IU.name, P.name, R.name, DS.startDateTime, DS.stopDateTime, DS.temporalResolution) "
                                + " from EmfDataset as DS left join DS.intendedUse as IU left join DS.project as P left join DS.region as R "
                                + " where DS.datasetType.id = "
                                + datasetTypeId
                                + " and lower(DS.name) like "
                                + ns
                                + " and DS.status <> 'Deleted' " + " order by DS.name").list();
    }

    public List getDatasets(Session session, int datasetTypeId, String nameContains) {
        String ns = Utils.getPattern(nameContains.toLowerCase().trim());
        return session
                .createQuery(
                        "select new EmfDataset( DS.id, DS.name, DS.defaultVersion, DS.datasetType.id, DS.datasetType.name) from EmfDataset as DS where DS.datasetType.id = "
                                + datasetTypeId
                                + " and lower(DS.name) like "
                                + ns
                                + " and DS.status <> 'Deleted' "
                                + " order by DS.name").list();
    }

    public void addExternalSources(ExternalSource[] srcs, Session session) {
        hibernateFacade.add(srcs, session);
    }

    // NOTE: limit < 0 will return all external sources
    public ExternalSource[] getExternalSrcs(int datasetId, int limit, String filter, Session session) {
        String query = " FROM " + ExternalSource.class.getSimpleName() + " as ext WHERE ext.datasetId=" + datasetId;

        if (filter != null && !filter.trim().isEmpty() && !filter.trim().equals("*"))
            query += " AND lower(ext.datasource) LIKE '%%" + filter + "%%'";

        List<ExternalSource> srcsList = new ArrayList<ExternalSource>();

        if (limit < 0)
            srcsList = session.createQuery(query).list();

        if (limit > 0)
            srcsList = session.createQuery(query).setMaxResults(limit).list();

        return srcsList.toArray(new ExternalSource[0]);
    }

    public boolean isExternal(int datasetId, Session session) {
        String query = "SELECT COUNT(ext) FROM " + ExternalSource.class.getSimpleName()
                + " as ext WHERE ext.datasetId=" + datasetId;
        List count = session.createSQLQuery(query).list();

        return count != null && count.size() > 0;
    }

    @SuppressWarnings("unchecked")
    public EmfDataset getDataset(Session session, String name) {
        Criterion statusCrit = Restrictions.ne("status", "Deleted"); // FIXME: to be deleted after dataset removed
        // from db
        Criterion nameCrit = Restrictions.eq("name", name);
        Criterion criterion = Restrictions.and(statusCrit, nameCrit);
        Order order = Order.asc("name");
        List<EmfDataset> list = hibernateFacade.get(EmfDataset.class, criterion, order, session);

        if (list == null || list.size() == 0)
            return null;

        return list.get(0);
    }

    public EmfDataset getDataset(Session session, int id) {
        session.clear(); // to clear the cached objects in session if any
        Criterion statusCrit = Restrictions.ne("status", "Deleted"); // FIXME: to be deleted after dataset removed
        // from db
        Criterion idCrit = Restrictions.eq("id", new Integer(id));
        Criterion criterion = Restrictions.and(statusCrit, idCrit);
        return (EmfDataset) hibernateFacade.load(EmfDataset.class, criterion, session);
    }

    public Version getVersion(Session session, int datasetId, int version) {
        Criterion crit1 = Restrictions.eq("datasetId", new Integer(datasetId));
        Criterion crit2 = Restrictions.eq("version", new Integer(version));
        Criterion criterion = Restrictions.and(crit1, crit2);

        return (Version) hibernateFacade.load(Version.class, criterion, session);
    }

    public boolean isUsedByControlStrategies(Session session, EmfDataset dataset) {
        List strategies = hibernateFacade.getAll(ControlStrategy.class, session);

        if (strategies == null || strategies.isEmpty())
            return false;

        for (Iterator iter = strategies.iterator(); iter.hasNext();) {
            ControlStrategy cs = (ControlStrategy) iter.next();
            if (datasetUsed(cs, dataset))
                return true;
        }

        return false;
    }

    public boolean isUsedByCases(Session session, EmfDataset dataset) {
        CaseDAO caseDao = new CaseDAO();

        List caseInputs = caseDao.getAllCaseInputs(session);

        if (caseInputs == null || caseInputs.isEmpty())
            return false;

        if (datasetUsed((CaseInput[]) caseInputs.toArray(new CaseInput[0]), dataset))
            return true;

        return false;
    }

    private boolean datasetUsed(CaseInput[] inputs, EmfDataset dataset) {
        for (int i = 0; i < inputs.length; i++) {
            EmfDataset caseInputDataset = inputs[i].getDataset();
            if (caseInputDataset != null && caseInputDataset.equals(dataset))
                return true;
        }
        return false;
    }

    private boolean datasetUsed(ControlStrategy cs, EmfDataset dataset) {
        ControlStrategyInputDataset[] controlStrategyInputDatasets = cs.getControlStrategyInputDatasets();
        for (int i = 0; i < controlStrategyInputDatasets.length; i++)
            if (controlStrategyInputDatasets[i].getInputDataset().equals(dataset))
                return true;

        return false;
    }

    public long getDatasetRecordsNumber(DbServer dbServer, Session session, EmfDataset dataset, Version version)
            throws SQLException {
        DatasetType type = dataset.getDatasetType();

        if (type.getExporterClassName().endsWith("ExternalFilesExporter"))
            return getExternalSrcs(dataset.getId(), -1, null, session).length;

        Datasource datasource = dbServer.getEmissionsDatasource();
        InternalSource source = dataset.getInternalSources()[0];
        String qualifiedTable = datasource.getName() + "." + source.getTable();
        String countQuery = "SELECT count(*) FROM " + qualifiedTable + getWhereClause(version, session);
        long totalCount = 0;

        try {
            Connection connection = datasource.getConnection();
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(countQuery);
            resultSet.next();
            totalCount = resultSet.getInt(1);

            resultSet.close();
        } catch (SQLException e) {
            throw new SQLException("Cannot get total records number on dataset: " + dataset.getName() + " Reason: "
                    + e.getMessage());
        }

        return totalCount;
    }

    public Integer[] getDatasetRecordsNumber(DbServer dbServer, Session session, EmfDataset dataset, Version[] versions, String tableName)
    throws SQLException {
        DatasetType type = dataset.getDatasetType();

        if (type.getExporterClassName().endsWith("ExternalFilesExporter"))
            return new Integer[]{getExternalSrcs(dataset.getId(), -1, null, session).length};

        int nVersions = versions.length;
        Integer[] totalCount = new Integer[nVersions];
        
        Datasource datasource = dbServer.getEmissionsDatasource();
      
        for ( int i =0 ; i < versions.length; i++ ){
            //source = dataset.getInternalSources()[sIndex];
            String qualifiedTable = datasource.getName() + "." + tableName;
            String countQuery = "SELECT count(*) FROM " + qualifiedTable + getWhereClause(versions[i], session);
            totalCount[i] = 0;

            try {
                Connection connection = datasource.getConnection();
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(countQuery);
                resultSet.next();
                totalCount[i] = resultSet.getInt(1);

                resultSet.close();
            } catch (SQLException e) {
                throw new SQLException("Cannot get total records number on dataset: " + dataset.getName() + " Reason: "
                        + e.getMessage());
            }
        }
        return totalCount;
    }

    private String getWhereClause(Version version, Session session) {
        String versions = versionsList(version, session);
        String deleteClause = createDeleteClause(versions);

        String whereClause = " WHERE dataset_id = " + version.getDatasetId() + " AND version IN (" + versions + ")"
                + deleteClause;

        return whereClause;
    }

    private String createDeleteClause(String versions) {
        StringBuffer buffer = new StringBuffer();

        StringTokenizer tokenizer = new StringTokenizer(versions, ",");
        // e.g.: delete_version NOT SIMILAR TO '(6|6,%|%,6,%|%,6)'
        while (tokenizer.hasMoreTokens()) {
            String version = tokenizer.nextToken();
            if (!version.equals("0")) {
                String regex = "(" + version + "|" + version + ",%|%," + version + ",%|%," + version + ")";
                if (buffer.length() == 0) {
                    buffer.append(" AND ");
                }
                buffer.append(" delete_versions NOT SIMILAR TO '" + regex + "'");

                if (tokenizer.hasMoreTokens())
                    buffer.append(" AND ");
            }
        }

        return buffer.toString();
    }

    private String versionsList(Version finalVersion, Session session) {
        Versions versions = new Versions();
        Version[] path = versions.getPath(finalVersion.getDatasetId(), finalVersion.getVersion(), session);

        StringBuffer result = new StringBuffer();
        for (int i = 0; i < path.length; i++) {
            result.append(path[i].getVersion());
            if ((i + 1) < path.length)
                result.append(",");
        }
        return result.toString();
    }

    private void renameEmissionTable(EmfDataset dataset, EmfDataset oldDataset, Session session) throws Exception {
        if (DebugLevels.DEBUG_0()) {
            System.out.println("Check to rename. Dataset name: " + dataset.getName() + " Status: "
                    + dataset.getStatus() + " id: " + dataset.getId());
            System.out.println("Old dataset is null? " + (oldDataset == null));
            System.out.println("Old dataset name: " + ((oldDataset == null) ? "" : oldDataset.getName()));
            System.out.println("Old dataset status: " + ((oldDataset == null) ? "" : oldDataset.getStatus()));
            System.out.println("Old dataset exists? " + exists(dataset.getId(), EmfDataset.class, session));
        }

        if (!continueToRename(dataset, oldDataset))
            return;

        if (DebugLevels.DEBUG_12())
            System.out.println("Dataset ok to rename.");

        DbServer dbServer = getDbServer();

        try {
            if (DebugLevels.DEBUG_14())
                System.out.println("DatasetDAO starts renaming dataset table for dataset: " + dataset.getName());
            renameTable(dataset, oldDataset, dbServer);
            if (DebugLevels.DEBUG_14())
                System.out.println("DatasetDAO has finished renaming dataset table for dataset: " + dataset.getName());
        } finally {
            dbServer.disconnect();
        }
    }

    // when you remove a dataset, you need to rename emissions tables to mark as deleted
    // if the table is shared by multiple datasets, you don't wan to rename it
    private boolean continueToRename(EmfDataset dataset, EmfDataset oldDataset) {
        if (oldDataset == null) {
            return false;
        }

        DatasetType type = dataset.getDatasetType();
        InternalSource[] sources = dataset.getInternalSources();

        if (type != null) {
            String importerclass = (type == null ? "" : type.getImporterClassName());
            importerclass = (importerclass == null ? "" : importerclass.trim());

            if (importerclass.equals("gov.epa.emissions.commons.io.other.SMKReportImporter")
                    || importerclass.equals("gov.epa.emissions.commons.io.csv.CSVImporter")
                    || importerclass.equals("gov.epa.emissions.commons.io.generic.LineImporter"))
                return false;
        }

        if (dataset.getName().trim().equalsIgnoreCase(oldDataset.getName().trim()))
            return false;

        if (sources == null || sources.length == 0 || type.getTablePerDataset() != 1) {
            return false;
        }

        return true;
    }

    private void renameTable(EmfDataset dataset, EmfDataset oldDataset, DbServer dbServer) throws ImporterException {
        Datasource datasource = dbServer.getEmissionsDatasource();
        InternalSource[] sources = dataset.getInternalSources();
        DataTable table = new DataTable(oldDataset, datasource);
        String oldTableName = oldDataset.getInternalSources()[0].getTable();
        String newTableName = table.createName(dataset.getName());

        if (DebugLevels.DEBUG_12())
            System.out.println("new table name: " + newTableName + " old table name:" + oldTableName);

        table.rename(oldTableName, newTableName);
        // only set this if there wasn't an error.
        sources[0].setTable(newTableName);
    }

    public boolean datasetNameUsed(String name, Session session) throws Exception {
        Criterion crit = Restrictions.eq("name", name).ignoreCase();
        EmfDataset ds = (EmfDataset) hibernateFacade.load(EmfDataset.class, crit, session);

        if (ds == null)
            return false;

        return true;
    }

    private DbServer getDbServer() throws Exception {
        DbServer dbServer;
        if (dbServerFactory == null)
            dbServer = new EmfDbServer();
        else
            dbServer = dbServerFactory.getDbServer();

        return dbServer;
    }

    public void updateVersionNReleaseLock(Version target, Session session) throws EmfException {
        lockingScheme.releaseLockOnUpdate(target, (Version) current(target.getId(), Version.class, session), session);
    }
    
    public void updateVersion(Version target, Session session) throws EmfException {
        hibernateFacade.updateOnly(target, session);
    }

    public Version obtainLockOnVersion(User user, int id, Session session) {
        return (Version) lockingScheme.getLocked(user, (Version) current(id, Version.class, session), session);
    }

    public void deleteDatasets(EmfDataset[] datasets, DbServer dbServer, Session session) throws EmfException {

        // The following line is commented out because the necessary check has been done
        // EmfDataset[] deletableDatasets = getCaseFreeDatasets(datasets, session);
        // NOTE: wait till decided by EPA OAQPS
        // checkIfUsedByStrategies(datasetIDs, session);
        // EmfDataset[] deletableDatasets = getControlStrateyFreeDatasets(datasets, session);

        if (datasets == null || datasets.length == 0)
            return;

        Exception exception = null;
        int[] ids = getIDs(datasets);
        Datasource datasource = dbServer.getEmissionsDatasource();
        TableCreator emissionTableTool = new TableCreator(datasource);

        int len = ids.length;
        int remainder = len % 600;
        int loop = len / 600;

        if (remainder > 0)
            loop++;

        for (int i = 0; i < loop; i++) {
            int[] datasetIDs;
            int start = i * 599 + i;
            int end = start + 599;

            if (i < loop - 1)
                datasetIDs = subArray(ids, start, end);
            else
                datasetIDs = subArray(ids, start, len - 1);

            // Need to search all the items associated with datasets and remove them properly
            // before remove the underlying datasets
            try {
                deleteFromOutputsTable(datasetIDs, session);
            } catch (Exception e) {
                LOG.error(e);
                exception = e;
            }

            try {
                deleteFromEmfTables(datasetIDs, emissionTableTool, session);
            } catch (Exception e) {
                LOG.error(e);
                exception = e;
            }

            try {
                session.clear();
                session.flush();
                hibernateFacade.remove(datasets, session);
            } catch (Exception e) {
                LOG.error(e);
                exception = e;
            }

            try {
                dropDataTables(datasets, emissionTableTool, session);
            } catch (Exception e) {
                LOG.error(e);
                exception = e;
            }
        }

        if (exception != null) {
            LOG.error("Error purging datasets.", exception);
            throw new EmfException(exception.getMessage());
        }
    }

    public void checkIfUsedByCases(int[] datasetIDs, Session session) throws EmfException {
        List list = null;

        // check if dataset is an input dataset for some cases (via the cases.cases_caseinputs table)
        list = session.createQuery(
                "select CI.caseID from CaseInput as CI " + "where (CI.dataset.id = "
                        + getAndOrClause(datasetIDs, "CI.dataset.id") + ")").list();

        if (list != null && list.size() > 0) {
            Criterion criterion = Restrictions.eq("id", list.get(0));
            Case usedCase = (Case) hibernateFacade.get(Case.class, criterion, session).get(0);
            throw new EmfException("Dataset used by case " + usedCase.getName() + ".");
        }
    }
    
    public List<Integer> notUsedByCases(int[] datasetIDs, User user, Session session) throws Exception{
        if (datasetIDs == null || datasetIDs.length == 0)
            return new ArrayList<Integer>();
        
        // check if dataset is an input dataset for some cases (via the cases.cases_caseinputs table)
        @SuppressWarnings("unchecked")
        List<Object[]> list = session.createQuery(
                "select DISTINCT CI.dataset, c.name from CaseInput as CI, Case as c " + "where CI.caseID = c.id AND (CI.dataset.id = "
                        + getAndOrClause(datasetIDs, "CI.dataset.id") + ")").list();

        List<Integer> all = EmfArrays.convert(datasetIDs);
        
        if (list == null || list.size() == 0)
            return all;
        
        String usedby = "used by case";
        List<Integer> ids = getUsedDatasetIds(user, session, list, usedby);
        all.removeAll(ids);
        
        return all;
    }

    public void checkIfUsedByStrategies(int[] datasetIDs, Session session) throws EmfException {
        // check if dataset is an input inventory for some strategy (via the StrategyInputDataset table)
        strategyList = session.createQuery(
                "select cS.name from ControlStrategy as cS inner join cS.controlStrategyInputDatasets "
                        + "as iDs inner join iDs.inputDataset as iD with (iD.id = "
                        + getAndOrClause(datasetIDs, "iD.id") + ")").list();

        if (strategyList != null && strategyList.size() > 0)
            throw new EmfException("Error: dataset used by control strategy " + strategyList.get(0) + ".");

        // check if dataset is an input inventory for some strategy (via the StrategyResult table, could be here for
        // historical reasons)
        strategyList = session.createQuery(
                "select cS.name from ControlStrategyResult sR, ControlStrategy cS where "
                        + "sR.controlStrategyId = cS.id and (sR.inputDataset.id = "
                        + getAndOrClause(datasetIDs, "sR.inputDataset.id") + ")").list();

        if (strategyList != null && strategyList.size() > 0)
            throw new EmfException("Error: dataset used by control strategy " + strategyList.get(0) + ".");

        // check if dataset is used as a region/county dataset for specific strategy measures
        strategyList = session.createQuery(
                "select cS.name from ControlStrategy as cS inner join cS.controlMeasures as cM inner join "
                        + "cM.regionDataset as rD with (rD.id = " + getAndOrClause(datasetIDs, "rD.id") + ")").list();

        if (strategyList != null && strategyList.size() > 0)
            throw new EmfException("Error: dataset used by control strategy " + strategyList.get(0) + ".");

        // check if dataset is used as a region/county dataset for specific strategy
        strategyList = session.createQuery(
                "select cS.name from ControlStrategy cS where (cS.countyDataset.id = "
                        + getAndOrClause(datasetIDs, "cS.countyDataset.id") + ")").list();

        if (strategyList != null && strategyList.size() > 0)
            throw new EmfException("Dataset used by control strategy " + strategyList.get(0) + ".");
    }
    
    @SuppressWarnings("unchecked")
    public List<Integer> notUsedByStrategies(int[] datasetIDs, User user, Session session) throws Exception {
        if (datasetIDs == null || datasetIDs.length == 0)
            return new ArrayList<Integer>();
        
        // check if dataset is an input inventory for some strategy (via the StrategyInputDataset table)
        List<Object[]> list = session.createQuery(
                "select DISTINCT iDs.inputDataset, cS.name from ControlStrategy as cS inner join cS.controlStrategyInputDatasets "
                        + "as iDs inner join iDs.inputDataset as iD with (iD.id = "
                        + getAndOrClause(datasetIDs, "iD.id") + ")").list();

        List<Integer> all = EmfArrays.convert(datasetIDs);
        String usedby = "used by control strategy";
        
        List<Integer> ids = getUsedDatasetIds(user, session, list, usedby);
        all.removeAll(ids);
        
        if (all.size() == 0)
            return all;
        
        // check if dataset is an input inventory for some strategy (via the StrategyResult table, could be here for
        // historical reasons)
        list = session.createQuery(
                "select DISTINCT sR.inputDataset, cS.name from ControlStrategyResult sR, ControlStrategy cS where "
                        + "sR.controlStrategyId = cS.id and (sR.inputDataset.id = "
                        + getAndOrClause(EmfArrays.convert(all), "sR.inputDataset.id") + ")").list();

        ids = getUsedDatasetIds(user, session, list, usedby);
        all.removeAll(ids);

        if (all.size() == 0)
            return all;
        
        // check if dataset is used as a region/county dataset for specific strategy measures
        list = session.createQuery(
                "select DISTINCT cM.regionDataset, cS.name from ControlStrategy as cS inner join cS.controlMeasures as cM inner join "
                        + "cM.regionDataset as rD with (rD.id = " + getAndOrClause(EmfArrays.convert(all), "rD.id") + ")").list();

        ids = getUsedDatasetIds(user, session, list, usedby);
        all.removeAll(ids);
        
        if (all.size() == 0)
            return all;
        
        // check if dataset is used as a region/county dataset for specific strategy
        list = session.createQuery(
                "select DISTINCT cS.countyDataset, cS.name from ControlStrategy cS where (cS.countyDataset.id = "
                        + getAndOrClause(EmfArrays.convert(all), "cS.countyDataset.id") + ")").list();

        ids = getUsedDatasetIds(user, session, list, usedby);
        all.removeAll(ids);
        
        return all;
    }

    private List<Integer> getUsedDatasetIds(User user, Session session, List<Object[]> list, String usedby) {
        List<Integer> ids = new ArrayList<Integer>();
        
        if (list != null && list.size() != 0) {
            for (int i = 0; i < list.size(); i++) {
                EmfDataset ds = (EmfDataset)list.get(i)[0];
                ids.add(ds.getId());
                setStatus(user.getUsername(), "Dataset \"" + ds.getName() + "\" " + usedby + ": " + list.get(i)[1] + ".", "Delete Dataset", session);
            }
        }
        
        return ids;
    }

    public void checkIfUsedByControlPrograms(int[] datasetIDs, Session session) throws EmfException {
        // check if dataset is an input inventory for some control program (via the control_programs table)
        controlProgList = session.createQuery(
                "select cP.name from ControlProgram as cP inner join cP.dataset as d with (d.id = "
                        + getAndOrClause(datasetIDs, "d.id") + ")").list();

        if (controlProgList != null && controlProgList.size() > 0)
            throw new EmfException("Error: dataset used by control program " + controlProgList.get(0) + ".");

    }
    
    public List<Integer> notUsedByControlPrograms(int[] datasetIDs, User user, Session session) throws Exception {
        // check if dataset is an input inventory for some control program (via the control_programs table)
        if (datasetIDs == null || datasetIDs.length == 0)
            return new ArrayList<Integer>();
        
        @SuppressWarnings("unchecked")
        List<Object[]> list = session.createQuery(
                "select DISTINCT cP.dataset, cP.name from ControlProgram as cP inner join cP.dataset as d with (d.id = "
                        + getAndOrClause(datasetIDs, "d.id") + ")").list();

        List<Integer> all = EmfArrays.convert(datasetIDs);
        
        if (list == null || list.size() == 0)
            return all;
        
        List<Integer> ids = getUsedDatasetIds(user, session, list, "used by control program");
        all.removeAll(ids);
        
        return all;
    }
    
    @SuppressWarnings("unchecked")
    public List<Integer> notUsedBySectorScnarios(int[] datasetIDs, User user, Session session) throws Exception {
        if (datasetIDs == null || datasetIDs.length == 0)
            return new ArrayList<Integer>();
        
        // check if dataset is an eecsMapppingDataset in SectorScenarion table
        List<Object[]> list = session.createQuery(
                "select DISTINCT SS.eecsMapppingDataset, SS.name from SectorScenario as SS inner join SS.eecsMapppingDataset "
                        + "as eMD with (eMD.id = "
                        + getAndOrClause(datasetIDs, "eMD.id") + ")").list();

        List<Integer> all = EmfArrays.convert(datasetIDs);
        String usedby = "used by SectorScenario";
        
        List<Integer> ids = getUsedDatasetIds(user, session, list, usedby);
        all.removeAll(ids);
        
        if (all.size() == 0)
            return all;
        
        // check if dataset is an sectorMapppingDataset in SectorScenario table
        list = session.createQuery(
                "select DISTINCT SS.sectorMapppingDataset, SS.name from SectorScenario as SS inner join SS.sectorMapppingDataset "
                        + "as sMD with (sMD.id = "
                        + getAndOrClause(EmfArrays.convert(all), "sMD.id") + ")").list();

        ids = getUsedDatasetIds(user, session, list, usedby);
        all.removeAll(ids);

        if (all.size() == 0)
            return all;
        
        // check if dataset is used as inputDataset for specific SectorScenarioInventory
        list = session.createQuery(
                "select DISTINCT invs.dataset, SS.name from SectorScenario as SS inner join SS.inventories "
                        + "as invs inner join invs.dataset as ds with (ds.id = "
                        + getAndOrClause(EmfArrays.convert(all), "ds.id") + ")").list();

        ids = getUsedDatasetIds(user, session, list, usedby);
        all.removeAll(ids);
        
        if (all.size() == 0)
            return all;
        
        // check if dataset is used as an inventory dataset for specific SectorScenarioOutput
        list = session.createQuery(
                "select DISTINCT SSO.inventoryDataset, SS.name from SectorScenario SS, SectorScenarioOutput SSO where "
                + "SS.id = SSO.sectorScenarioId AND (SSO.inventoryDataset.id = "
                + getAndOrClause(EmfArrays.convert(all), "SSO.inventoryDataset.id") + ")").list();

        ids = getUsedDatasetIds(user, session, list, usedby);
        all.removeAll(ids);
        
        if (all.size() == 0)
            return all;
        
        // check if dataset is used as an output dataset for specific SectorScenarioOutput
        list = session.createQuery(
                "select DISTINCT SSO.outputDataset, SS.name from SectorScenario SS, SectorScenarioOutput SSO where "
                + "SS.id = SSO.sectorScenarioId AND (SSO.outputDataset.id = "
                + getAndOrClause(EmfArrays.convert(all), "SSO.outputDataset.id") + ")").list();

        ids = getUsedDatasetIds(user, session, list, usedby);
        all.removeAll(ids);
        
        return all;
    }
    
    public List<Integer> notUsedByFast(int[] datasetIDs, User user, DbServer dbServer, Session session) throws Exception {
        Datasource fast = dbServer.getFastDatasource();
        DataQuery dataQuery = fast.query();
        List<Integer> all = EmfArrays.convert(datasetIDs);
        List<Integer> ids = new ArrayList<Integer>();
        
        if (datasetIDs.length == 0) return all;
        
        //Check if dataset is in fast.fast_runs table, 'cancer_risk_dataset_id' column
        String query = "SELECT DISTINCT fr.cancer_risk_dataset_id, ds.name, fa.name AS analname, fr.name AS runname from fast.fast_runs as fr "
                + "JOIN emf.datasets as ds ON fr.cancer_risk_dataset_id = ds.id " 
                + "LEFT JOIN fast.fast_analysis_runs as far ON fr.id = far.fast_run_id " 
                + "LEFT JOIN fast.fast_analyses as fa ON far.fast_analysis_id = fa.id "
                + "WHERE fr.cancer_risk_dataset_id="
                + getAndOrClause(EmfArrays.convert(all), "fr.cancer_risk_dataset_id");
        ids = getRefdDatasetIds(user, EmfArrays.convert(all), dataQuery, query, "cancer_risk_dataset_id", session);
        all.removeAll(ids);
        
        if (all.size() == 0)
            return all;
        
        //Check if dataset is in fast.fast_runs table, 'species_mapping_dataset_id' column
        query = "SELECT DISTINCT fr.species_mapping_dataset_id, ds.name, fa.name AS analname, fr.name AS runname from fast.fast_runs as fr "
        		+ "JOIN emf.datasets as ds ON fr.species_mapping_dataset_id = ds.id "
                + "LEFT JOIN fast.fast_analysis_runs as far ON fr.id = far.fast_run_id " 
                + "LEFT JOIN fast.fast_analyses as fa ON far.fast_analysis_id = fa.id "
                + "WHERE fr.species_mapping_dataset_id="
                + getAndOrClause(EmfArrays.convert(all), "fr.species_mapping_dataset_id");
        ids = getRefdDatasetIds(user, EmfArrays.convert(all), dataQuery, query, "species_mapping_dataset_id", session);
        all.removeAll(ids);
        
        if (all.size() == 0)
            return all;
        
        //Check if dataset is in fast.fast_runs table, 'transfer_coefficients_dataset_id' column
        query = "SELECT DISTINCT fr.transfer_coefficients_dataset_id, ds.name, fa.name AS analname, fr.name AS runname from fast.fast_runs as fr "
        		+ "JOIN emf.datasets as ds ON fr.transfer_coefficients_dataset_id = ds.id "
                + "LEFT JOIN fast.fast_analysis_runs as far ON fr.id = far.fast_run_id " 
                + "LEFT JOIN fast.fast_analyses as fa ON far.fast_analysis_id = fa.id "
                + "WHERE fr.transfer_coefficients_dataset_id="
                + getAndOrClause(EmfArrays.convert(all), "fr.transfer_coefficients_dataset_id");
        ids = getRefdDatasetIds(user, EmfArrays.convert(all), dataQuery, query, "transfer_coefficients_dataset_id", session);
        all.removeAll(ids);
        
        if (all.size() == 0)
            return all;
        
        //Check if dataset is in fast.fast_runs table, 'domain_population_dataset_id' column
        query = "SELECT DISTINCT fr.domain_population_dataset_id, ds.name, fa.name AS analname, fr.name AS runname from fast.fast_runs as fr "
        		+ "JOIN emf.datasets as ds ON fr.domain_population_dataset_id = ds.id " 
                + "LEFT JOIN fast.fast_analysis_runs as far ON fr.id = far.fast_run_id " 
                + "LEFT JOIN fast.fast_analyses as fa ON far.fast_analysis_id = fa.id "
                + "WHERE fr.domain_population_dataset_id="
                + getAndOrClause(EmfArrays.convert(all), "fr.domain_population_dataset_id");
        ids = getRefdDatasetIds(user, EmfArrays.convert(all), dataQuery, query, "domain_population_dataset_id", session);
        all.removeAll(ids);
        
        if (all.size() == 0)
            return all;
        
        //Check if dataset is in fast.fast_runs table, 'invtable_dataset_id' column
        query = "SELECT DISTINCT fr.invtable_dataset_id, ds.name, fa.name AS analname, fr.name AS runname from fast.fast_runs as fr "
        		+ "JOIN emf.datasets as ds ON fr.invtable_dataset_id = ds.id " 
                + "LEFT JOIN fast.fast_analysis_runs as far ON fr.id = far.fast_run_id " 
                + "LEFT JOIN fast.fast_analyses as fa ON far.fast_analysis_id = fa.id "
        		+ "WHERE fr.invtable_dataset_id="
                + getAndOrClause(EmfArrays.convert(all), "fr.invtable_dataset_id");
        ids = getRefdDatasetIds(user, EmfArrays.convert(all), dataQuery, query, "invtable_dataset_id", session);
        all.removeAll(ids);
        
        if (all.size() == 0)
            return all;
        
        //Check if dataset is in fast.fast_analyses table, 'cancer_risk_dataset_id' column
        query = "SELECT DISTINCT fa.cancer_risk_dataset_id, ds.name, fa.name AS analname, '' AS runname from fast.fast_analyses as fa "
        		+ "JOIN emf.datasets as ds ON fa.cancer_risk_dataset_id = ds.id " 
                + "WHERE fa.cancer_risk_dataset_id="
                + getAndOrClause(EmfArrays.convert(all), "fa.cancer_risk_dataset_id");
        ids = getRefdDatasetIds(user, EmfArrays.convert(all), dataQuery, query, "cancer_risk_dataset_id", session);
        all.removeAll(ids);
        
        if (all.size() == 0)
            return all;
        
        //Check if dataset is in fast.fast_analysis_outputs table, 'output_dataset_id' column
        query = "SELECT DISTINCT fao.output_dataset_id, ds.name, fa.name AS analname, '' AS runname from fast.fast_analysis_outputs as fao "
        		+ "JOIN emf.datasets as ds ON fao.output_dataset_id = ds.id "
                + "JOIN fast.fast_analyses as fa ON fao.fast_analysis_id = fa.id "
        		+ "WHERE fao.output_dataset_id="
        		+ getAndOrClause(EmfArrays.convert(all), "fao.output_dataset_id");
        ids = getRefdDatasetIds(user, EmfArrays.convert(all), dataQuery, query, "output_dataset_id", session);
        all.removeAll(ids);
        
        if (all.size() == 0)
            return all;
        
        //Check if dataset is in fast.fast_datasets table, 'dataset_id' column
        query = "SELECT DISTINCT fd.dataset_id, ds.name, fa.name AS analname, fr.name AS runname from fast.fast_datasets as fd "
        		+ "JOIN emf.datasets as ds ON fd.dataset_id = ds.id " 
        		+ "LEFT JOIN fast.fast_run_inventories as fri ON fri.inventory_dataset_id = fd.dataset_id "
        		+ "LEFT JOIN fast.fast_runs as fr ON fr.id = fri.fast_run_id "
                + "LEFT JOIN fast.fast_analysis_runs as far ON fr.id = far.fast_run_id "
                + "LEFT JOIN fast.fast_analyses as fa ON far.fast_analysis_id = fa.id "
        		+ "WHERE fd.dataset_id="
        		+ getAndOrClause(EmfArrays.convert(all), "fd.dataset_id");
        ids = getRefdDatasetIds(user, EmfArrays.convert(all), dataQuery, query, "dataset_id", session);
        all.removeAll(ids);
        
        if (all.size() == 0)
            return all;
        
        //Check if dataset is in fast.fast_nonpoint_datasets table, 
        //'gridded_smk_dataset_id', 'base_nonpoint_dataset_id', 'invtable_dataset_id' column
        query = "SELECT DISTINCT fnpd.gridded_smk_dataset_id, ds.name, fa.name AS analname, fr.name AS runname from fast.fast_nonpoint_datasets as fnpd "
        		+ "JOIN emf.datasets as ds ON fnpd.gridded_smk_dataset_id = ds.id "
        		+ "LEFT JOIN fast.fast_run_inventories as fri ON fri.inventory_dataset_id = fnpd.quasi_point_dataset_id "
                + "LEFT JOIN fast.fast_runs as fr ON fr.id = fri.fast_run_id "
                + "LEFT JOIN fast.fast_analysis_runs as far ON fr.id = far.fast_run_id "
                + "LEFT JOIN fast.fast_analyses as fa ON far.fast_analysis_id = fa.id "
        		+ "WHERE fnpd.gridded_smk_dataset_id="
        		+ getAndOrClause(EmfArrays.convert(all), "fnpd.gridded_smk_dataset_id");
        ids = getRefdDatasetIds(user, EmfArrays.convert(all), dataQuery, query, "gridded_smk_dataset_id", session);
        all.removeAll(ids);
        
        if (all.size() == 0)
            return all;
        
        query = "SELECT DISTINCT fnpd.base_nonpoint_dataset_id, ds.name, fa.name AS analname, fr.name AS runname from fast.fast_nonpoint_datasets as fnpd "
        		+ "JOIN emf.datasets as ds ON fnpd.base_nonpoint_dataset_id = ds.id "
                + "LEFT JOIN fast.fast_run_inventories as fri ON fri.inventory_dataset_id = fnpd.quasi_point_dataset_id "
                + "LEFT JOIN fast.fast_runs as fr ON fr.id = fri.fast_run_id "
                + "LEFT JOIN fast.fast_analysis_runs as far ON fr.id = far.fast_run_id "
                + "LEFT JOIN fast.fast_analyses as fa ON far.fast_analysis_id = fa.id "
        		+ "WHERE fnpd.base_nonpoint_dataset_id="
        		+ getAndOrClause(EmfArrays.convert(all), "fnpd.base_nonpoint_dataset_id");
        ids = getRefdDatasetIds(user, EmfArrays.convert(all), dataQuery, query, "base_nonpoint_dataset_id", session);
        all.removeAll(ids);
        
        if (all.size() == 0)
            return all;
        
        query = "SELECT DISTINCT fnpd.invtable_dataset_id, ds.name, fa.name AS analname, fr.name AS runname from fast.fast_nonpoint_datasets as fnpd " 
        		+ "JOIN emf.datasets as ds ON fnpd.invtable_dataset_id = ds.id "
                + "LEFT JOIN fast.fast_run_inventories as fri ON fri.inventory_dataset_id = fnpd.quasi_point_dataset_id "
                + "LEFT JOIN fast.fast_runs as fr ON fr.id = fri.fast_run_id "
                + "LEFT JOIN fast.fast_analysis_runs as far ON fr.id = far.fast_run_id "
                + "LEFT JOIN fast.fast_analyses as fa ON far.fast_analysis_id = fa.id "
        		+ "WHERE fnpd.invtable_dataset_id="
            + getAndOrClause(EmfArrays.convert(all), "fnpd.invtable_dataset_id");
        ids = getRefdDatasetIds(user, EmfArrays.convert(all), dataQuery, query, "invtable_dataset_id", session);
        all.removeAll(ids);
        
        if (all.size() == 0)
            return all;
        
        //Check if dataset is in fast.fast_run_inventories table, 'inventory_dataset_id' column
        query = "SELECT DISTINCT fri.inventory_dataset_id, ds.name, fa.name AS analname, fr.name AS runname from fast.fast_run_inventories as fri "
        		+ "JOIN emf.datasets as ds ON fri.inventory_dataset_id = ds.id "
        		+ "LEFT JOIN fast.fast_runs as fr ON fr.id = fri.fast_run_id "
                + "LEFT JOIN fast.fast_analysis_runs as far ON fri.fast_run_id = far.fast_run_id "
                + "LEFT JOIN fast.fast_analyses as fa ON far.fast_analysis_id = fa.id "
        		+ "WHERE fri.inventory_dataset_id="
        		+ getAndOrClause(EmfArrays.convert(all), "fri.inventory_dataset_id");
        ids = getRefdDatasetIds(user, EmfArrays.convert(all), dataQuery, query, "inventory_dataset_id", session);
        all.removeAll(ids);
        
        if (all.size() == 0)
            return all;
        
        //Check if dataset is in fast.fast_run_outputs table, 'output_dataset_id' column
        query = "SELECT DISTINCT fro.output_dataset_id, ds.name, fa.name AS analname, fr.name AS runname from fast.fast_run_outputs as fro "
        		+ "JOIN emf.datasets as ds ON fro.output_dataset_id = ds.id "
        		+ "LEFT JOIN fast.fast_runs as fr ON fr.id = fro.fast_run_id "
                + "LEFT JOIN fast.fast_analysis_runs as far ON fro.fast_run_id = far.fast_run_id "
                + "LEFT JOIN fast.fast_analyses as fa ON far.fast_analysis_id = fa.id "
        		+ "WHERE fro.output_dataset_id="
        		+ getAndOrClause(EmfArrays.convert(all), "fro.output_dataset_id");
        ids = getRefdDatasetIds(user, EmfArrays.convert(all), dataQuery, query, "output_dataset_id", session);
        all.removeAll(ids);
        
        return all;
    }

    private List<Integer> getRefdDatasetIds(User user, int[] idArray, DataQuery dataQuery, String query, String dsId, Session session)
            throws SQLException {
        ResultSet resultSet = null;
        List<Integer> ids = null;
        
        if (idArray == null || idArray.length == 0)
            return new ArrayList<Integer>();
        
        try {
            ids = EmfArrays.convert(idArray);
            List<Integer> temp = new ArrayList<Integer>();
            resultSet = dataQuery.executeQuery(query);

            while (resultSet.next()) {
                temp.add(resultSet.getInt(dsId));
                String fastRunName = resultSet.getString("runname");
                String fastAnalName = resultSet.getString("analname");
                String msg = "fast dataset queue";
                
                if (fastAnalName != null && !fastAnalName.trim().isEmpty())
                    msg = "fast analysis \"" + fastAnalName + "\"";
                else if (fastRunName != null && !fastRunName.trim().isEmpty())
                    msg = "fast run \"" + fastRunName + "\"";
                
                setStatus(user.getUsername(), "Dataset \"" + resultSet.getString("name") + "\" is used by "
                        + msg + ".", "Delete Dataset", session);
            }
            
            ids = temp;
        } catch (Exception e) {
            LOG.error(e);
        } finally {
            if (resultSet != null)
                resultSet.close();
        }
        
        return ids;
    }

    private void deleteFromEmfTables(int[] datasetIDs, TableCreator tableTool, Session session) {
        deleteFromObjectTable(datasetIDs, Version.class, "datasetId", session);
        deleteFromObjectTable(datasetIDs, AccessLog.class, "datasetId", session);
        deleteFromObjectTable(datasetIDs, DatasetNote.class, "datasetId", session);
        deleteFromObjectTable(datasetIDs, Revision.class, "datasetId", session);
        deleteFromObjectTable(datasetIDs, ExternalSource.class, "datasetId", session);

        try {
            dropQAStepResultTable(datasetIDs, tableTool, session);
        } catch (Exception e) {
            LOG.error(e);
        }

        deleteFromObjectTable(datasetIDs, QAStepResult.class, "datasetId", session);
        deleteFromObjectTable(datasetIDs, QAStep.class, "datasetId", session);
        deleteFromObjectTable(datasetIDs, ControlStrategyResult.class, "detailedResultDataset.id", session);
        deleteFromObjectTable(datasetIDs, ControlStrategyResult.class, "controlledInventoryDataset.id", session);
    }

    int deleteFromObjectTable(int[] datasetIDs, Class<?> clazz, String attrName, Session session) {
        int deletedEntities = 0;

        try {
            Transaction tx = session.beginTransaction();

            String hqlDelete = "DELETE FROM " + clazz.getSimpleName() + " obj WHERE obj." + attrName + " = "
                    + getAndOrClause(datasetIDs, "obj." + attrName);

            if (DebugLevels.DEBUG_16())
                System.out.println("hql delete string: " + hqlDelete);

            deletedEntities = session.createQuery(hqlDelete).executeUpdate();
            tx.commit();

            return deletedEntities;
        } catch (HibernateException e) {
            LOG.error(e);
            return 0;
        } finally {
            if (DebugLevels.DEBUG_16())
                LOG.warn(deletedEntities + " items deleted from " + clazz.getName() + " table.");
        }
    }

    public void deleteFromOutputsTable(int[] datasetIDs, Session session) throws EmfException {
        int updatedItems = 0;

        try {
            Transaction tx = session.beginTransaction();

            String firstPart = "UPDATE " + CaseOutput.class.getSimpleName() + " obj SET ";
            String secondPart = " WHERE obj.datasetId = " + getAndOrClause(datasetIDs, "obj.datasetId");
            String updateQuery = firstPart + "obj.message = :msg, obj.datasetId = :id" + secondPart;

            if (DebugLevels.DEBUG_16())
                System.out.println("hql update string: " + updateQuery);

            updatedItems = session.createQuery(updateQuery).setString("msg", "Associated dataset deleted").setInteger(
                    "id", 0).executeUpdate();
            tx.commit();

            if (DebugLevels.DEBUG_16())
                System.out.println(updatedItems + " items updated.");
        } catch (HibernateException e) {
            throw new EmfException(e.getMessage());
        } finally {
            if (DebugLevels.DEBUG_16())
                LOG.warn(updatedItems + " items updated from " + CaseOutput.class.getName() + " table.");
        }
    }

    private void dropDataTables(EmfDataset[] datasets, TableCreator tableTool, Session session) {
        for (int i = 0; i < datasets.length; i++) {
            try {
                dropDataTables(tableTool, datasets[i], session);
            } catch (Exception exc) {
                LOG.error(exc);
                setStatus(datasets[0].getCreator(), exc.getMessage(), "Delete Dataset", session);
            }
        }
    }

    private void dropDataTables(TableCreator tableTool, EmfDataset dataset, Session session) {
        DatasetType type = dataset.getDatasetType();

        if (type != null && type.isExternal())
            return;

        InternalSource[] sources = dataset.getInternalSources();

        for (int i = 0; i < sources.length; i++) {
            try {
                dropIndividualTable(tableTool, sources[i], type, dataset.getId(), dataset.getCreator(), session);
            } catch (Exception exc) { // if there is a problem with one table, keep going
                LOG.error(exc);
                setStatus(dataset.getCreator(), exc.getMessage(), "Delete Dataset", session);
            }
        }
    }

    private void dropIndividualTable(TableCreator tableTool, InternalSource source, DatasetType type, 
            int dsID, String user, Session session) {
        String table = source.getTable();
        String importerclass = (type == null ? "" : type.getImporterClassName());
        importerclass = (importerclass == null ? "" : importerclass.trim());

        try {
            if (importerclass.equals("gov.epa.emissions.commons.io.temporal.TemporalProfileImporter")
                    || importerclass.equals("gov.epa.emissions.commons.io.other.CountryStateCountyDataImporter")
                    || importerclass.equals("gov.epa.emissions.commons.io.other.SMKReportImporter")
                    || importerclass.equals("gov.epa.emissions.commons.io.csv.CSVImporter")
                    || importerclass.equals("gov.epa.emissions.commons.io.generic.LineImporter"))
                tableTool.deleteRecords(table, source.getCols()[1], "integer", "" + dsID); // 2nd column: dataset_id
            else {
                if (DebugLevels.DEBUG_16())
                    System.out.println("Dropping data table  " + table);

                tableTool.drop(table);

                if (DebugLevels.DEBUG_16())
                    System.out.println("Data table  " + table + " dropped.");
            }
        } catch (Exception e) {
            setStatus(user, "Error deleting emission table: " + table + ".", "Delete Dataset", session);
        }
    }

    private void dropQAStepResultTable(int[] datasetIDs, TableCreator tableTool, Session session) {
        List tables = session.createQuery(
                "SELECT obj.table from " + QAStepResult.class.getSimpleName() + " as obj WHERE obj.datasetId = "
                        + getAndOrClause(datasetIDs, "obj.datasetId")).list();

        for (Iterator<String> iter = tables.iterator(); iter.hasNext();) {
            String table = iter.next();

            if (table == null || table.trim().isEmpty())
                continue;

            try {
                tableTool.drop(table);

                if (DebugLevels.DEBUG_16())
                    System.out.println("QA step result table " + table + " dropped.");
            } catch (Exception e) {
                LOG.error(e);
                setStatus("", "Error deleting emission table: " + table + ".", "Delete Dataset", session);
            }
        }
    }

    private String getAndOrClause(int[] datasetIDs, String attrName) {
        if (datasetIDs == null || datasetIDs.length == 0) return "";
        
        StringBuffer sb = new StringBuffer();
        int numIDs = datasetIDs.length;

        if (numIDs == 1)
            return "" + datasetIDs[0];

        for (int i = 0; i < numIDs - 1; i++)
            sb.append(datasetIDs[i] + " OR " + attrName + " = ");

        sb.append(datasetIDs[numIDs - 1]);

        return sb.toString();
    }

    private int[] getIDs(EmfDataset[] datasets) {
        int len = datasets.length;
        int[] ids = new int[len];

        for (int i = 0; i < len; i++)
            ids[i] = datasets[i].getId();

        return ids;
    }

    public EmfDataset[] getCaseFreeDatasets(EmfDataset[] datasets, Session session) {
        List<EmfDataset> list = new ArrayList<EmfDataset>();

        for (EmfDataset dataset : datasets) {
            try {
                checkIfUsedByCases(new int[] { dataset.getId() }, session);
                list.add(dataset);
            } catch (Exception e) {
                LOG.warn(e.getMessage());
            }
        }

        return list.toArray(new EmfDataset[0]);
    }

    public EmfDataset[] getControlStrateyFreeDatasets(EmfDataset[] datasets, Session session) {
        List<EmfDataset> list = new ArrayList<EmfDataset>();

        for (EmfDataset dataset : datasets) {
            try {
                checkIfUsedByStrategies(new int[] { dataset.getId() }, session);
                list.add(dataset);
            } catch (Exception e) {
                LOG.warn(e.getMessage());
            }
        }

        return list.toArray(new EmfDataset[0]);
    }

    public List<EmfDataset> deletedDatasets(User user, Session session) {
        Criterion statusCrit = Restrictions.eq("status", "Deleted");
        Criterion nameCrit = Restrictions.eq("creator", user.getUsername());
        Criterion criterion = statusCrit;

        if (!user.isAdmin())
            criterion = Restrictions.and(statusCrit, nameCrit);

        return hibernateFacade.get(EmfDataset.class, criterion, session);
    }

    public void removeEmptyDatasets(User user, DbServer dbServer, Session session) throws EmfException, SQLException {
        int[] dsIDsWithNoEmisData = getAllDatasetsWithNoEmissionData(dbServer);
        int len = dsIDsWithNoEmisData.length;
        int remainder = len % 600;
        int loop = len / 600;

        if (remainder > 0)
            loop++;

        for (int i = 0; i < loop; i++) {
            int[] tempIds;
            int start = i * 599 + i;
            int end = start + 599;

            if (i < loop - 1)
                tempIds = subArray(dsIDsWithNoEmisData, start, end);
            else
                tempIds = subArray(dsIDsWithNoEmisData, start, len - 1);

            deleteControlStrategies(tempIds, session);
            decoupleDSFromCases(tempIds, session);
            setDSAsDeleted(tempIds, session);

        }
    }

    private int[] subArray(int[] ids, int start, int end) {
        int len = end - start + 1;
        int[] tempIds = new int[len];

        for (int i = 0; i < len; i++)
            tempIds[i] = ids[i + start];

        return tempIds;
    }

    private int[] getAllDatasetsWithNoEmissionData(DbServer dbServer) throws EmfException, SQLException {
        Datasource emf = dbServer.getEmfDatasource();
        DataQuery dataQuery = emf.query();
        String query = "SELECT dataset_id from emf.internal_sources where lower(table_name) NOT IN (select tablename from pg_tables WHERE schemaname='emissions')";
        int[] ids = null;
        ResultSet resultSet = null;

        try {
            resultSet = dataQuery.executeQuery(query);
            resultSet.last();
            int size = resultSet.getRow();
            resultSet.beforeFirst();
            ids = new int[size];
            int i = 0;

            while (resultSet.next())
                ids[i++] = resultSet.getInt("dataset_id");
        } catch (SQLException e) {
            e.printStackTrace();
            LOG.error(e);
            throw new EmfException(e.getMessage());
        } finally {
            if (resultSet != null)
                resultSet.close();
        }

        return ids;
    }

    private void deleteControlStrategies(int[] dsIDsWithNoEmisData, Session session) throws EmfException {
        try {
            checkIfUsedByStrategies(dsIDsWithNoEmisData, session);
            // checkIfUsedByControlPrograms(dsIDsWithNoEmisData, session);
        } catch (Exception e) {
            String name = "";

            if (strategyList != null && strategyList.size() > 0)
                name = strategyList.get(0).toString();

            if (controlProgList != null && controlProgList.size() > 0)
                name = controlProgList.get(0).toString();

            throw new EmfException("Please delete control strategies/programs before purge: " + name);
        }
    }

    private void decoupleDSFromCases(int[] dsIDsWithNoEmisData, Session session) throws EmfException {
        int updatedItems = 0;

        try {
            Transaction tx = session.beginTransaction();

            String firstPart = "UPDATE " + CaseInput.class.getSimpleName()
                    + " obj SET obj.dataset = null, obj.version = null";
            String secondPart = " WHERE obj.dataset.id = " + getAndOrClause(dsIDsWithNoEmisData, "obj.dataset.id");
            String updateQuery = firstPart + secondPart;

            if (DebugLevels.DEBUG_16())
                System.out.println("hql update string: " + updateQuery);

            updatedItems = session.createQuery(updateQuery).executeUpdate();
            tx.commit();

            if (DebugLevels.DEBUG_16())
                System.out.println(updatedItems + " items updated.");
        } catch (HibernateException e) {
            throw new EmfException(e.getMessage());
        } finally {
            if (DebugLevels.DEBUG_16())
                LOG.warn(updatedItems + " items updated from " + CaseInput.class.getName() + " table.");
        }
    }

    private void setDSAsDeleted(int[] dsIDsWithNoEmisData, Session session) throws EmfException {
        int updatedItems = 0;

        try {
            Transaction tx = session.beginTransaction();

            String firstPart = "UPDATE " + EmfDataset.class.getSimpleName() + " obj SET ";
            String secondPart = " WHERE obj.id = " + getAndOrClause(dsIDsWithNoEmisData, "obj.id");
            String updateQuery = firstPart + "obj.status = :sts" + secondPart;

            if (DebugLevels.DEBUG_16())
                System.out.println("hql update string: " + updateQuery);

            updatedItems = session.createQuery(updateQuery).setString("sts", "Deleted").executeUpdate();
            tx.commit();

            if (DebugLevels.DEBUG_16())
                System.out.println(updatedItems + " items updated.");
        } catch (HibernateException e) {
            throw new EmfException(e.getMessage());
        } finally {
            if (DebugLevels.DEBUG_16())
                LOG.warn(updatedItems + " items updated from " + EmfDataset.class.getName() + " table.");
        }
    }

    public List<String> getDatasetNamesStartWith(String start, Session session) {
        String query = "SELECT DS.name FROM " + EmfDataset.class.getSimpleName() + " AS DS WHERE lower(DS.name) LIKE "
                + "'%" + start.toLowerCase().trim() + "%' ORDER BY DS.name";
        return session.createQuery(query).list();
    }

    public void updateExternalSrcsWithoutLocking(ExternalSource[] srcs, Session session) {
        // NOTE: update without locking objects
        hibernateFacade.update(srcs, session);
    }

    public String[] getTableColumnDistinctValues(int datasetId, int datasetVersion, String columnName,
            String whereFilter, String sortOrder, Session session, DbServer dbServer) throws EmfException {
        List<String> values = new ArrayList<String>();
        ResultSet rs = null;
        try {
            EmfDataset dataset = getDataset(session, datasetId);
            Version version = version(session, datasetId, dataset.getDefaultVersion());
            String datasetVersionedQuery = new VersionedQuery(version).query();
            String query = "select distinct " + columnName + " from " + qualifiedEmissionTableName(dataset) + " where "
                    + datasetVersionedQuery
                    + (whereFilter != null && whereFilter.trim().length() > 0 ? " and (" + whereFilter + ")" : "")
                    + " order by " + (sortOrder != null && sortOrder.trim().length() > 0 ? sortOrder : columnName)
                    + ";";
            rs = dbServer.getEmissionsDatasource().query().executeQuery(query);
            while (rs.next()) {
                values.add(rs.getString(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new EmfException(e.getMessage());// new EmfException("Could not execute query -" + query + "\n" +
                                                    // e.getMessage());
        } finally {
            //
        }
        return values.toArray(new String[0]);
    }

    private Version version(Session session, int datasetId, int version) {
        Versions versions = new Versions();
        return versions.get(datasetId, version, session);
    }

    private String qualifiedEmissionTableName(Dataset dataset) throws EmfException {
        return qualifiedName(emissionTableName(dataset));
    }

    private String emissionTableName(Dataset dataset) {
        InternalSource[] internalSources = dataset.getInternalSources();
        return internalSources[0].getTable();
    }

    private String qualifiedName(String table) throws EmfException {
        // VERSIONS TABLE - Completed - throws exception if the following case is true
        if ("versions".equalsIgnoreCase(table.toLowerCase())) {
            throw new EmfException("Table versions moved to schema emf."); // VERSIONS TABLE
        }
        return "emissions." + table;
    }

    public void importRemoteDataset() {
        //
    }
    
    public List<EmfDataset> findSimilarDatasets(EmfDataset ds, String qaStep, String qaArgument, 
            int[] usedByCasesId, String dataValueFilter, boolean unconditional, int userId, Session session) throws Exception {
    
        if (ds.getDatasetType() == null && dataValueFilter != null && !dataValueFilter.trim().isEmpty()) {
            throw new Exception("Dataset Type must be set if you want to use Data Value Filter.");
        }
        
        String dsts = "( select DT from DatasetType as DT " 
            + "where "
            + " DT.id not in (select EDT.id from User as U "
            + " inner join U.excludedDatasetTypes as EDT where U.id = "
            + userId + ")" 
            + " order by DT.name )";
        
        String dsTypeStr = (ds.getDatasetType() == null ? "AND DS.datasetType.id in " + dsts : " AND DS.datasetType.id = "
            + ds.getDatasetType().getId());
        String name = ds.getName();
        String dsNameStr = (name == null || name.trim().isEmpty() ? "" : " AND lower(DS.name) LIKE "
            + Utils.getPattern(name.toLowerCase().trim()));
        String creator = ds.getCreator();
        String dsCreatorStr = (creator == null || creator.trim().isEmpty() ? "" : " AND lower(DS.creator) LIKE "
            + Utils.getPattern(creator.toLowerCase().trim()));
        String dsKeyStr = getDSKeyStr(ds.getKeyVals());
        String desc = ds.getDescription(); 
        String qaStr = "";

        if ( !( qaStep==null || qaStep.length()==0) )
            qaStr = " AND lower(QS.name) LIKE "+ Utils.getPattern(qaStep.toLowerCase().trim())+ " AND DS.id=QS.datasetId" ;
        if ( !( qaArgument==null || qaArgument.length()==0) )
            qaStr += " AND lower(QS.programArguments) LIKE "+ Utils.getPattern(qaArgument.toLowerCase().trim());
        if ( !qaStr.isEmpty())
            qaStr += " AND DS.id=QS.datasetId" ;
        String descStr = (desc == null || desc.trim().isEmpty() ? "" : " AND lower(DS.description) LIKE "
            + Utils.getPattern(desc.toLowerCase().trim()));
        String dsProjStr = (ds.getProject() == null ? "" : " AND DS.project.id = " + ds.getProject().getId());
        String caseStr = ( usedByCasesId == null || usedByCasesId.length==0) ? "" : 
            (Arrays.toString(usedByCasesId).equals("[0]") ? "" : " AND CI.caseID in " + Arrays.toString(usedByCasesId).replace('[', '(').replace(']', ')')) 
            + " AND DS.id=CI.dataset.id";
               
        String dsquery = "SELECT new EmfDataset(DS.id, DS.name, DS.defaultVersion, DS.modifiedDateTime, DS.datasetType.id, DS.datasetType.name, DS.status,"
            + " DS.creator, DS.creatorFullName, IU.name, P.name, R.name, DS.startDateTime, DS.stopDateTime, DS.temporalResolution)"
            + " FROM EmfDataset AS DS LEFT JOIN DS.intendedUse as IU LEFT JOIN DS.project as P LEFT JOIN DS.region as R "
            + (qaStr.isEmpty() ? "" : ", QAStep as QS ")
            + ( ( usedByCasesId == null || usedByCasesId.length==0 ) ? "" : ", CaseInput as CI ")
            + dsKeyStr
            + " WHERE DS.status <> 'Deleted'"
            + dsTypeStr
            + qaStr
            + dsNameStr
            + dsCreatorStr
            + checkBackSlash(descStr)
            + dsProjStr
            + caseStr
            + " ORDER BY DS.name";
        if ( DebugLevels.DEBUG_12())
            System.out.print(dsquery+ "\n");

        List<EmfDataset> ds1 = session.createQuery(dsquery).list();

        String dsTypeKeyStr = getDSTypeKeyStr(ds.getKeyVals());
        String dstypequery = "SELECT new EmfDataset(DS.id, DS.name, DS.defaultVersion, DS.modifiedDateTime, DS.datasetType.id, DS.datasetType.name, DS.status,"
            + " DS.creator, DS.creatorFullName, IU.name, P.name, R.name, DS.startDateTime, DS.stopDateTime, DS.temporalResolution)"
            + " FROM EmfDataset AS DS "
            + (dsTypeKeyStr.isEmpty() ? "" : ", DatasetType AS TYPE")
            + (qaStr.isEmpty() ? "" : ", QAStep as QS ")
            + " LEFT JOIN DS.intendedUse as IU LEFT JOIN DS.project as P LEFT JOIN DS.region as R "
            + ( ( usedByCasesId == null || usedByCasesId.length==0 ) ? "" : ", CaseInput as CI ")
            + dsTypeKeyStr
            + " WHERE DS.status <> 'Deleted'"
            + dsTypeStr
            + qaStr
            + (dsTypeKeyStr.isEmpty() ? "" : " AND TYPE.id = DS.datasetType.id")
            + dsNameStr
            + dsCreatorStr    
            + checkBackSlash(descStr) 
            + dsProjStr 
            + caseStr
            + " ORDER BY DS.name";
        if ( DebugLevels.DEBUG_12())
            System.out.print(dstypequery);

        List<EmfDataset> ds2 = session.createQuery(dstypequery).list();
        List<EmfDataset> all = new ArrayList<EmfDataset>();
        all.addAll(ds1);
        all.addAll(ds2);

        TreeSet<EmfDataset> set = new TreeSet<EmfDataset>(all);
        List<EmfDataset> total = new ArrayList<EmfDataset>(set);
        
        total = filter(total, dataValueFilter, session );

        if (total.size() > 300 && !unconditional) {
            total.get(0).setName("Alert!!! More than 300 datasets selected.");
            return total.subList(0, 1);
        }

        return total;
    }
    
    private List<EmfDataset> filter( List<EmfDataset> datasets, String dataValueFilter, Session session) throws SQLException, EmfException {
        if ( datasets == null || dataValueFilter==null || dataValueFilter.trim().isEmpty()) {
            return datasets;
        }
        
        DbServer dbServer = this.dbServerFactory.getDbServer();
        Datasource datasource = dbServer.getEmissionsDatasource();
        Connection connection = datasource.getConnection();
        
        List<EmfDataset> filteredDatasets = new ArrayList<EmfDataset>();
        for ( EmfDataset dataset : datasets) {
            EmfDataset ds = this.getDataset(session, dataset.getId());
            String sqlStr = "SELECT * FROM " + this.qualifiedEmissionTableName(ds) + " WHERE " + dataValueFilter + " LIMIT 1;";
            try {
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(sqlStr);
                if ( resultSet != null && resultSet.next()) {
                    filteredDatasets.add(dataset);
                }
                resultSet.close();
            } catch (SQLException e) {
                throw new SQLException("Error with Data Value Filter, incorrect filter SQL syntax: " + dataValueFilter);
            }
        }
       
        return filteredDatasets;
    }

    private String getDSKeyStr(KeyVal[] keyVals) {
        if (keyVals.length == 0)
            return "";

        StringBuilder withStr = new StringBuilder(" INNER JOIN DS.keyVals keyVal WITH ");

        for (KeyVal kv : keyVals) {
            String name = kv.getName();
            String value = checkBackSlash(kv.getValue());
            value = value.replaceAll("'", "''");

            if (!value.trim().isEmpty())
                value = " AND lower(keyVal.value) = '" + value.trim().toLowerCase() + "'";

            withStr.append("(keyVal.kwname = '" + name + "'" + value + ") OR ");
        }

        return withStr.toString().substring(0, withStr.length() - 3);
    }

    private String getDSTypeKeyStr(KeyVal[] keyVals) {
        if (keyVals.length == 0)
            return "";

        StringBuilder typeWithStr = new StringBuilder(" INNER JOIN TYPE.keyVals keyVal WITH ");

        for (KeyVal kv : keyVals) {
            String name = kv.getName();
            String value = checkBackSlash(kv.getValue());
            value = value.replaceAll("'", "''");

            if (!value.trim().isEmpty())
                value = " AND lower(keyVal.value) = '" + value.trim().toLowerCase() + "'";

            typeWithStr.append("(keyVal.kwname = '" + name + "'" + value + ") OR ");
        }

        return typeWithStr.toString().substring(0, typeWithStr.length() - 3);
    }

    private String checkBackSlash(String str) {
        if (str == null)
            return "";

        return str.replaceAll("\\\\", "\\\\\\\\");
    }
    
    public synchronized void setStatus(String username, String message, String msgType, Session session) {
        Status endStatus = new Status();
        endStatus.setUsername(username);
        endStatus.setType(msgType);
        endStatus.setMessage(message);
        endStatus.setTimestamp(new Date());

        hibernateFacade.add(endStatus, session);
    }

    public boolean checkBizzareCharInColumn(DbServer dbServer, Session session, int datasetId, int version, String colName) throws SQLException, EmfException { 
        // only for dataset that has column PLANT BUG3588
        EmfDataset dataset = this.getDataset(session, datasetId);
        DatasetType type = dataset.getDatasetType();

        if (type.getExporterClassName().endsWith("ExternalFilesExporter"))
            return false; // set it as false for external files for now BUG3588

        Datasource datasource = dbServer.getEmissionsDatasource();
        InternalSource source = dataset.getInternalSources()[0];
        String qualifiedTable = datasource.getName() + "." + source.getTable();
        // VERSIONS TABLE - Completed - throws exception if the following case is true
        if ("emissions.versions".equalsIgnoreCase(qualifiedTable) ) {
            throw new EmfException("Table versions moved to schema emf."); // VERSIONS TABLE
        }
        String countQuery = "SELECT count(*) FROM " + qualifiedTable + getWhereClause(version(session,datasetId,version), session) + " and " + colName + " ~* '[[:cntrl:]]'"; //consider unicode?? |chr(127)-chr(65535)]'";
        long totalCount = 0;

        try {
            Connection connection = datasource.getConnection();
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(countQuery);
            resultSet.next();
            totalCount = resultSet.getInt(1);

            resultSet.close();
        } catch (SQLException e) {
            throw new SQLException("Cannot check bizzare cahracters in column " + colName + " on dataset: " + dataset.getName() + " Reason: "
                    + e.getMessage());
        }

        return totalCount > 0;
    }

}
