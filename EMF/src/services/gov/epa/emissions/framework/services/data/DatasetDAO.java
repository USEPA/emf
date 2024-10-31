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
import gov.epa.emissions.framework.services.module.ModuleDataset;
import gov.epa.emissions.framework.services.persistence.HibernateFacade;
import gov.epa.emissions.framework.services.persistence.HibernateFacade.CriteriaBuilderQueryRoot;
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

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;

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

    public <C> boolean exists(int id, Class<C> clazz, EntityManager entityManager) {
        return hibernateFacade.exists(id, clazz, entityManager);
    }

    /*
     * Return true if the name is already used
     */
    public <C> boolean nameUsed(String name, Class<C> clazz, EntityManager entityManager) {
        return hibernateFacade.nameUsed(name, clazz, entityManager); // case insensitive comparison
    }

    public <C> C current(int id, Class<C> clazz, EntityManager entityManager) {
        return hibernateFacade.current(id, clazz, entityManager);
    }

    public boolean canUpdate(EmfDataset dataset, EntityManager entityManager) throws Exception {
        return canUpdate(dataset.getId(), dataset.getName(), entityManager);
    }

    private boolean canUpdate(int id, String newName, EntityManager entityManager) throws Exception {
        if (!exists(id, EmfDataset.class, entityManager)) {
            throw new EmfException("Dataset with id=" + id + " does not exist.");
        }

        EmfDataset current = (EmfDataset) current(id, EmfDataset.class, entityManager);
        entityManager.clear();// clear to flush current
        if (current.getName().equalsIgnoreCase(newName))
            return true;

        return !datasetNameUsed(newName, entityManager);
    }

    public boolean exists(String name, EntityManager entityManager) {
        return hibernateFacade.exists(name, EmfDataset.class, entityManager);
    }

    public List all(EntityManager entityManager) {
        CriteriaBuilderQueryRoot<EmfDataset> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(EmfDataset.class, entityManager);
        return hibernateFacade.getAll(criteriaBuilderQueryRoot, entityManager);
    }

    public int getNumOfDatasets(int userId, EntityManager entityManager) {
        String dsts = "( select DT from DatasetType as DT " 
            + "where "
            + " DT.id not in (select EDT.id from User as U "
            + " inner join U.excludedDatasetTypes as EDT where U.id = "
            + userId + ")" 
            + " order by DT.name )";
        
        List<?> num = entityManager.createQuery("SELECT COUNT(ds.id) from EmfDataset as ds " 
                + " where ds.status <> 'Deleted' and ds.datasetType.id in " + dsts )
                .getResultList();
        return Integer.parseInt(num.get(0).toString());
    }

    public int getNumOfDatasets(EntityManager entityManager, String name, int userId) {
        String ns = Utils.getPattern(name.toLowerCase().trim());
        String dsts = "( select DT from DatasetType as DT " 
            + "where "
            + " DT.id not in (select EDT.id from User as U "
            + " inner join U.excludedDatasetTypes as EDT where U.id = "
            + userId + ")" 
            + " order by DT.name )";
      
        List<?> num = entityManager.createQuery(
                "SELECT COUNT(ds.id) from EmfDataset as ds where ds.status <> 'Deleted' " + " AND lower(ds.name) like "
                        + ns + " and ds.datasetType.id in " + dsts ).getResultList();
        return Integer.parseInt(num.get(0).toString());
    }

    public int getNumOfDatasets(EntityManager entityManager, int dsTypeId) {
        List<?> num = entityManager.createQuery(
                "SELECT COUNT(ds.id) from EmfDataset as ds where ds.status <> 'Deleted' " + " AND ds.datasetType.id = "
                        + dsTypeId).getResultList();

        return Integer.parseInt(num.get(0).toString());
    }

    public int getNumOfDatasets(EntityManager entityManager, int dsTypeId, String name) {
        String ns = Utils.getPattern(name.toLowerCase().trim());
        List<?> num = entityManager.createQuery(
                "SELECT COUNT(ds.id) from EmfDataset as ds where ds.status <> 'Deleted' " + " AND ds.datasetType.id = "
                        + dsTypeId + " AND lower(ds.name) like " + ns).getResultList();

        return Integer.parseInt(num.get(0).toString());
    }

    // FIXME: to be deleted after dataset removed from db
    public List allNonDeleted(EntityManager entityManager, int userId) {
        String dsts = "( select DT from DatasetType as DT " 
            + "where "
            + " DT.id not in (select EDT.id from User as U "
            + " inner join U.excludedDatasetTypes as EDT where U.id = "
            + userId + ")" 
            + " order by DT.name )";
        return entityManager
                .createQuery(
                        "select new EmfDataset(DS.id, DS.name, DS.defaultVersion, DS.modifiedDateTime, DS.datasetType.id, DS.datasetType.name, DS.status, DS.creator, DS.creatorFullName, IU.name, P.name, R.name, DS.startDateTime, DS.stopDateTime, DS.temporalResolution) "
                                + " from EmfDataset as DS left join DS.intendedUse as IU left join DS.project as P left join DS.region as R "
                                + " where DS.status <> 'Deleted' and DS.datasetType.id in " + dsts + "order by DS.name").getResultList();
    }

    public List allNonDeleted(EntityManager entityManager, String nameContains, int userId) {
        String ns = Utils.getPattern(nameContains.toLowerCase().trim());
        String dsts = " (select DT from DatasetType as DT " 
            + "where "
            + " DT.id not in (select EDT.id from User as U "
            + " inner join U.excludedDatasetTypes as EDT where U.id = "
            + userId + ")" 
            + " order by DT.name ) ";
        return entityManager
                .createQuery(
                        "select new EmfDataset(DS.id, DS.name, DS.defaultVersion, DS.modifiedDateTime, DS.datasetType.id, DS.datasetType.name, DS.status, DS.creator, DS.creatorFullName, IU.name, P.name, R.name, DS.startDateTime, DS.stopDateTime, DS.temporalResolution) "
                                + " from EmfDataset as DS left join DS.intendedUse as IU left join DS.project as P left join DS.region as R "
                                + " where lower(DS.name) like "
                                + ns
                                + " and DS.status <> 'Deleted' and DS.datasetType.id in " + dsts 
                                + " order by DS.name").getResultList();
    }

    public void add(EmfDataset dataset, EntityManager entityManager) throws EmfException {
        //NOTE: to trim the leading and trailing spaces???
        String name = dataset.getName();
        
        String newName = name;
        if ( newName != null) {
            newName = newName.trim();
        } else {
            throw new EmfException("Dataset name is null");
        }
        dataset.setName(newName);
        
        hibernateFacade.add(dataset, entityManager);
    }

    public void add(Version version, EntityManager entityManager) {
        hibernateFacade.add(version, entityManager);
    }

    // NOTE: make sure dataset has no changes in name, emission table name(s)
    // when call this update method. Not for updating status to be 'Deleted'.
    public void updateDSPropNoLocking(EmfDataset dataset, EntityManager entityManager) throws Exception {
        hibernateFacade.updateOnly(dataset, entityManager);
    }

    public void updateWithoutLocking(EmfDataset dataset, EntityManager entityManager) throws Exception {
//        try {
//            renameEmissionTable(dataset, getDataset(entityManager, dataset.getId()), entityManager);
//        } catch (Exception e) {
//            LOG.info("Can not rename emission table: " + dataset.getInternalSources()[0].getTable());
//        } finally {
            entityManager.clear();
            hibernateFacade.updateOnly(dataset, entityManager);
//        }
    }

    public void remove(EmfDataset dataset, EntityManager entityManager) {
        if (DebugLevels.DEBUG_12())
            System.out.println("dataset dao remove(dataset, entityManager) called: " + dataset.getId() + " "
                    + dataset.getName());

        ExternalSource[] extSrcs = null;

        if (dataset.isExternal())
            extSrcs = getExternalSrcs(dataset.getId(), -1, null, entityManager);

        if (extSrcs != null && extSrcs.length > 0)
            hibernateFacade.removeObjects(extSrcs, entityManager);

        hibernateFacade.remove(dataset, entityManager);
    }

    // FIXME: change this method name to indicate mark deleted
    public void remove(User user, EmfDataset dataset, EntityManager entityManager) throws EmfException {
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
            System.out.println("dataset dao remove(user, dataset, entityManager) called: " + dataset.getId() + " "
                    + datasetName);
            System.out.println("Dataset status: " + dataset.getStatus() + " dataset retrieved null? "
                    + (getDataset(entityManager, dataset.getId()) == null));
        }

        checkIfUsedByCases(new int[] { dataset.getId() }, entityManager);

        // Disabled temporarily according to Alison's request 1/15/2008
        // if (isUsedByControlStrategies(entityManager, dataset))
        // throw new EmfException("Cannot delete \"" + dataset.getName() + "\" - it is use by a control strategy.");

        String prefix = "DELETED_" + new Date().getTime() + "_";

        try {
            String newName = prefix + datasetName;

            if (!canUpdate(dataset.getId(), newName, entityManager)) // Check to see if the new name is available
                throw new EmfException("The Dataset name is already in use: " + dataset.getName());

            DatasetType type = dataset.getDatasetType();

            if (type != null && type.getTablePerDataset() > 1)
                LOG.info("Renaming emission tables for dataset " + dataset.getName() + " is not allowed.");

            EmfDataset locked = obtainLocked(user, dataset, entityManager);

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

            updateToRemove(locked, dataset, entityManager);
        } catch (Exception e) {
            LOG.error("Could not remove dataset " + datasetName + ".", e);
            throw new EmfException("Could not remove dataset " + datasetName + ". Reason: " + e.getMessage());
        }

        if (DebugLevels.DEBUG_14())
            System.out.println("DatasetDAO has finished removing dataset " + dataset.getName() + " " + new Date());
    }

    public EmfDataset obtainLocked(User user, EmfDataset dataset, EntityManager entityManager) {
        return (EmfDataset) lockingScheme.getLocked(user, current(dataset, entityManager), entityManager);
    }

    public EmfDataset releaseLocked(User user, EmfDataset locked, EntityManager entityManager) {
        return (EmfDataset) lockingScheme.releaseLock(user, current(locked, entityManager), entityManager);
    }

    public Revision update(Revision revision, EntityManager entityManager) throws EmfException {
        return (Revision) lockingScheme.releaseLockOnUpdate(revision, current(revision, entityManager), entityManager);
    }

    public Revision obtainLocked(User user, Revision revision, EntityManager entityManager) {
        return (Revision) lockingScheme.getLocked(user, current(revision, entityManager), entityManager);
    }

    public Revision releaseLocked(User user, Revision revision, EntityManager entityManager) {
        return (Revision) lockingScheme.releaseLock(user, current(revision, entityManager), entityManager);
    }

    public EmfDataset update(EmfDataset locked, EntityManager entityManager) throws Exception {
        EmfDataset toReturn = null;

//        try {
//            renameEmissionTable(locked, getDataset(entityManager, locked.getId()), entityManager);
//        } catch (Exception e) {
//            LOG.error("Can not rename emission table: " + locked.getInternalSources()[0].getTable(), e);
//        } finally { // to ignore if the rename fails
            if (DebugLevels.DEBUG_12())
                System.out.println("Update dataset " + locked.getName() + " with id: " + locked.getId());

            toReturn = (EmfDataset) lockingScheme.releaseLockOnUpdate(locked, current(locked, entityManager), entityManager);
//        }

        return toReturn;
    }

    private void updateToRemove(EmfDataset locked, EmfDataset oldDataset, EntityManager entityManager) throws Exception {
        try {
            if (DebugLevels.DEBUG_14())
                System.out.println("DatasetDAO starts renaming emission table for dataset: " + oldDataset.getName());
            renameEmissionTable(locked, oldDataset, entityManager);
            if (DebugLevels.DEBUG_14())
                System.out.println("DatasetDAO has finished renaming emission table for dataset: "
                        + oldDataset.getName());
        } catch (Exception e) {
            LOG.error("Can not rename emission table: " + locked.getInternalSources()[0].getTable(), e);
        } finally { // to ignore if the rename fails
            if (DebugLevels.DEBUG_12())
                System.out.println("Update to remove " + locked.getName() + " with id: " + locked.getId());

            lockingScheme.releaseLockOnUpdate(locked, current(locked, entityManager), entityManager);
        }
    }

    private EmfDataset current(EmfDataset dataset, EntityManager entityManager) {
        return (EmfDataset) current(dataset.getId(), EmfDataset.class, entityManager);
    }

    private Revision current(Revision revision, EntityManager entityManager) {
        return (Revision) current(revision.getId(), Revision.class, entityManager);
    }

    public List getDatasets(EntityManager entityManager, DatasetType datasetType) {
        CriteriaBuilderQueryRoot<EmfDataset> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(EmfDataset.class, entityManager);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<EmfDataset> root = criteriaBuilderQueryRoot.getRoot();

        Predicate statusCrit = builder.notEqual(root.get("status"), "Deleted"); // FIXME: to be deleted after dataset removed
        // from db
        Predicate typeCrit = builder.equal(root.get("datasetType"), datasetType);
        Predicate criterion = builder.and(statusCrit, typeCrit);
        javax.persistence.criteria.Order order = builder.asc(root.get("name"));
        return hibernateFacade.get(criteriaBuilderQueryRoot, criterion, order, entityManager);
    }

    public List getDatasets(EntityManager entityManager, int datasetTypeId) {
        return entityManager
                .createQuery(
                        "select new EmfDataset(DS.id, DS.name, DS.defaultVersion, DS.modifiedDateTime, DS.datasetType.id, DS.datasetType.name, DS.status, DS.creator, DS.creatorFullName, IU.name, P.name, R.name, DS.startDateTime,DS.stopDateTime, DS.temporalResolution) "
                                + " from EmfDataset as DS left join DS.intendedUse as IU left join DS.project as P left join DS.region as R "
                                + " where DS.datasetType.id = "
                                + datasetTypeId
                                + " and DS.status <> 'Deleted' order by DS.name").getResultList();
    }

    public List getDatasetsWithFilter(EntityManager entityManager, int datasetTypeId, String nameContains) {
        String ns = Utils.getPattern(nameContains.toLowerCase().trim());
        return entityManager
                .createQuery(
                        "select new EmfDataset(DS.id, DS.name, DS.defaultVersion, DS.modifiedDateTime, DS.datasetType.id, DS.datasetType.name, DS.status, DS.creator, DS.creatorFullName, IU.name, P.name, R.name, DS.startDateTime, DS.stopDateTime, DS.temporalResolution) "
                                + " from EmfDataset as DS left join DS.intendedUse as IU left join DS.project as P left join DS.region as R "
                                + " where DS.datasetType.id = "
                                + datasetTypeId
                                + " and lower(DS.name) like "
                                + ns
                                + " and DS.status <> 'Deleted' " + " order by DS.name").getResultList();
    }

    public List getDatasets(EntityManager entityManager, int datasetTypeId, String nameContains) {
        String ns = Utils.getPattern(nameContains.toLowerCase().trim());
        return entityManager
                .createQuery(
                        "select new EmfDataset( DS.id, DS.name, DS.defaultVersion, DS.datasetType.id, DS.datasetType.name) from EmfDataset as DS where DS.datasetType.id = "
                                + datasetTypeId
                                + " and lower(DS.name) like "
                                + ns
                                + " and DS.status <> 'Deleted' "
                                + " order by DS.name").getResultList();
    }

    public void addExternalSources(ExternalSource[] srcs, EntityManager entityManager) {
        hibernateFacade.add(srcs, entityManager);
    }

    // NOTE: limit < 0 will return all external sources
    public ExternalSource[] getExternalSrcs(int datasetId, int limit, String filter, EntityManager entityManager) {
        String query = " FROM " + ExternalSource.class.getSimpleName() + " as ext WHERE ext.datasetId=" + datasetId;

        if (filter != null && !filter.trim().isEmpty() && !filter.trim().equals("*"))
            query += " AND lower(ext.datasource) LIKE '%%" + filter + "%%'";

        List<ExternalSource> srcsList = new ArrayList<ExternalSource>();

        if (limit < 0)
            srcsList = entityManager.createQuery(query).getResultList();

        if (limit > 0)
            srcsList = entityManager.createQuery(query).setMaxResults(limit).getResultList();

        return srcsList.toArray(new ExternalSource[0]);
    }

    public boolean isExternal(int datasetId, EntityManager entityManager) {
        String query = "SELECT COUNT(ext) FROM " + ExternalSource.class.getSimpleName()
                + " as ext WHERE ext.datasetId=" + datasetId;
        List count = entityManager.createQuery(query).getResultList();

        return count != null && count.size() > 0;
    }

    public EmfDataset getDataset(EntityManager entityManager, String name) {
        CriteriaBuilderQueryRoot<EmfDataset> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(EmfDataset.class, entityManager);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<EmfDataset> root = criteriaBuilderQueryRoot.getRoot();

        Predicate statusCrit = builder.notEqual(root.get("status"), "Deleted"); // FIXME: to be deleted after dataset removed
        // from db
        Predicate nameCrit = builder.equal(root.get("name"), name);
        Predicate criterion = builder.and(statusCrit, nameCrit);

        List<EmfDataset> list = hibernateFacade.get(criteriaBuilderQueryRoot, criterion, builder.asc(root.get("name")), entityManager);

        if (list == null || list.size() == 0)
            return null;

        return list.get(0);
    }
    
    public EmfDataset getDataset(EntityManager entityManager, int id) {
        return getDataset(entityManager, id, true);
    }

    public EmfDataset getDataset(EntityManager entityManager, int id, boolean clearSession) {
        if (clearSession)
            entityManager.clear(); // to clear the cached objects in entityManager if any

        CriteriaBuilderQueryRoot<EmfDataset> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(EmfDataset.class, entityManager);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<EmfDataset> root = criteriaBuilderQueryRoot.getRoot();

        return hibernateFacade.load(
                entityManager, 
                criteriaBuilderQueryRoot, 
                builder.and(
                        builder.notEqual(root.get("status"), "Deleted"), 
                        builder.equal(root.get("id"), Integer.valueOf(id))
                    )
            );
    }

    public Version getVersion(EntityManager entityManager, int datasetId, int version) {
        CriteriaBuilderQueryRoot<Version> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(Version.class, entityManager);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<Version> root = criteriaBuilderQueryRoot.getRoot();

        return hibernateFacade.load(
                entityManager, 
                criteriaBuilderQueryRoot, 
                builder.and(
                        builder.equal(root.get("datasetId"), Integer.valueOf(datasetId)), 
                        builder.equal(root.get("version"), Integer.valueOf(version))
                    )
            );
    }

    public boolean isUsedByControlStrategies(EntityManager entityManager, EmfDataset dataset) {
        CriteriaBuilderQueryRoot<ControlStrategy> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(ControlStrategy.class, entityManager);
        List<ControlStrategy> strategies = hibernateFacade.getAll(criteriaBuilderQueryRoot, entityManager);

        if (strategies == null || strategies.isEmpty())
            return false;

        for (Iterator iter = strategies.iterator(); iter.hasNext();) {
            ControlStrategy cs = (ControlStrategy) iter.next();
            if (datasetUsed(cs, dataset))
                return true;
        }

        return false;
    }

    public boolean isUsedByCases(EntityManager entityManager, EmfDataset dataset) {
        CaseDAO caseDao = new CaseDAO();

        List caseInputs = caseDao.getAllCaseInputs(entityManager);

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

    public long getDatasetRecordsNumber(DbServer dbServer, EntityManager entityManager, EmfDataset dataset, Version version)
            throws SQLException {
        DatasetType type = dataset.getDatasetType();

        if (type.getExporterClassName().endsWith("ExternalFilesExporter"))
            return getExternalSrcs(dataset.getId(), -1, null, entityManager).length;

        Datasource datasource = dbServer.getEmissionsDatasource();
        InternalSource source = dataset.getInternalSources()[0];
        String qualifiedTable = datasource.getName() + "." + source.getTable();
        String countQuery = "SELECT count(*) FROM " + qualifiedTable + getWhereClause(version, entityManager);
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

    public Integer[] getDatasetRecordsNumber(DbServer dbServer, EntityManager entityManager, EmfDataset dataset, Version[] versions, String tableName)
    throws SQLException {
        DatasetType type = dataset.getDatasetType();

        if (type.getExporterClassName().endsWith("ExternalFilesExporter"))
            return new Integer[]{getExternalSrcs(dataset.getId(), -1, null, entityManager).length};

        int nVersions = versions.length;
        Integer[] totalCount = new Integer[nVersions];
        
        Datasource datasource = dbServer.getEmissionsDatasource();
      
        for ( int i =0 ; i < versions.length; i++ ){
            //source = dataset.getInternalSources()[sIndex];
            String qualifiedTable = datasource.getName() + "." + tableName;
            String countQuery = "SELECT count(*) FROM " + qualifiedTable + getWhereClause(versions[i], entityManager);
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

    private String getWhereClause(Version version, EntityManager entityManager) {
        String versions = versionsList(version, entityManager);
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

    private String versionsList(Version finalVersion, EntityManager entityManager) {
        Versions versions = new Versions();
        Version[] path = versions.getPath(finalVersion.getDatasetId(), finalVersion.getVersion(), entityManager);

        StringBuffer result = new StringBuffer();
        for (int i = 0; i < path.length; i++) {
            result.append(path[i].getVersion());
            if ((i + 1) < path.length)
                result.append(",");
        }
        return result.toString();
    }

    private void renameEmissionTable(EmfDataset dataset, EmfDataset oldDataset, EntityManager entityManager) throws Exception {
        if (DebugLevels.DEBUG_0()) {
            System.out.println("Check to rename. Dataset name: " + dataset.getName() + " Status: "
                    + dataset.getStatus() + " id: " + dataset.getId());
            System.out.println("Old dataset is null? " + (oldDataset == null));
            System.out.println("Old dataset name: " + ((oldDataset == null) ? "" : oldDataset.getName()));
            System.out.println("Old dataset status: " + ((oldDataset == null) ? "" : oldDataset.getStatus()));
            System.out.println("Old dataset exists? " + exists(dataset.getId(), EmfDataset.class, entityManager));
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

    public boolean datasetNameUsed(String name, EntityManager entityManager) throws Exception {
        LOG.info("hibernateFacade.load(EmfDataset.class, \"name\", \"" + name + "\", entityManager);");
        EmfDataset ds = hibernateFacade.load(EmfDataset.class, "name", name, entityManager);

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

    public void updateVersionNReleaseLock(Version target, EntityManager entityManager) throws EmfException {
        lockingScheme.releaseLockOnUpdate(target, (Version) current(target.getId(), Version.class, entityManager), entityManager);
    }
    
    public void updateVersion(Version target, EntityManager entityManager) throws EmfException {
        hibernateFacade.updateOnly(target, entityManager);
    }

    public Version obtainLockOnVersion(User user, int id, EntityManager entityManager) {
        return (Version) lockingScheme.getLocked(user, (Version) current(id, Version.class, entityManager), entityManager);
    }

    public void deleteDatasets(EmfDataset[] datasets, DbServer dbServer, EntityManager entityManager) throws EmfException {

        // The following line is commented out because the necessary check has been done
        // EmfDataset[] deletableDatasets = getCaseFreeDatasets(datasets, entityManager);
        // NOTE: wait till decided by EPA OAQPS
        // checkIfUsedByStrategies(datasetIDs, entityManager);
        // EmfDataset[] deletableDatasets = getControlStrateyFreeDatasets(datasets, entityManager);

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
                deleteFromOutputsTable(datasetIDs, entityManager);
            } catch (Exception e) {
                LOG.error(e);
                e.printStackTrace();
                exception = e;
            }

            try {
                deleteFromEmfTables(datasetIDs, emissionTableTool, entityManager);
            } catch (Exception e) {
                LOG.error(e);
                e.printStackTrace();
                exception = e;
            }

            try {
                decoupleDSFromModules(datasetIDs, entityManager);
            } catch (Exception e) {
                LOG.error(e);
                e.printStackTrace();
                exception = e;
            }
        }

        try {
            dropDataTables(datasets, emissionTableTool, entityManager);
        } catch (Exception e) {
            LOG.error(e);
            e.printStackTrace();
            exception = e;
        }

        for(EmfDataset dataset : datasets) {
            dataset.setKeyVals(new KeyVal[]{});
            dataset.setInternalSources(new InternalSource[]{});
            try {
                updateDSPropNoLocking(dataset, entityManager);
            } catch (Exception e) {
                LOG.error(e);
                e.printStackTrace();
                exception = e;
            }
        }
        
        try {
            entityManager.clear();
            hibernateFacade.remove(datasets, entityManager);
        } catch (Exception e) {
            LOG.error(e);
            e.printStackTrace();
            exception = e;
        }

        if (exception != null) {
            LOG.error("Error purging datasets.", exception);
            throw new EmfException(exception.getMessage());
        }
    }

    public void checkIfUsedByCases(int[] datasetIDs, EntityManager entityManager) throws EmfException {
        List list = null;

        // check if dataset is an input dataset for some cases (via the cases.cases_caseinputs table)
        list = entityManager.createQuery(
                "select CI.caseID from CaseInput as CI " + "where (CI.dataset.id = "
                        + getAndOrClause(datasetIDs, "CI.dataset.id") + ")").getResultList();

        if (list != null && list.size() > 0) {
            CriteriaBuilderQueryRoot<Case> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(Case.class, entityManager);
            Case usedCase = hibernateFacade.get(entityManager, criteriaBuilderQueryRoot, criteriaBuilderQueryRoot.getBuilder().equal(criteriaBuilderQueryRoot.getRoot().get("id"), list.get(0))).get(0);
            throw new EmfException("Dataset used by case " + usedCase.getName() + ".");
        }
    }
    
    public List<Integer> notUsedByCases(int[] datasetIDs, User user, EntityManager entityManager) throws Exception{
        if (datasetIDs == null || datasetIDs.length == 0)
            return new ArrayList<Integer>();
        
        // check if dataset is an input dataset for some cases (via the cases.cases_caseinputs table)
        @SuppressWarnings("unchecked")
        List<Object[]> list = entityManager.createQuery(
                "select DISTINCT CI.dataset, c.name from CaseInput as CI, Case as c " + "where CI.caseID = c.id AND (CI.dataset.id = "
                        + getAndOrClause(datasetIDs, "CI.dataset.id") + ")").getResultList();

        List<Integer> all = EmfArrays.convert(datasetIDs);
        
        if (list == null || list.size() == 0)
            return all;
        
        String usedby = "used by case";
        List<Integer> ids = getUsedDatasetIds(user, entityManager, list, usedby);
        all.removeAll(ids);
        
        return all;
    }

    public void checkIfUsedByStrategies(int[] datasetIDs, EntityManager entityManager) throws EmfException {
        // check if dataset is an input inventory for some strategy (via the StrategyInputDataset table)
        strategyList = entityManager.createQuery(
                "select cS.name from ControlStrategy as cS inner join cS.controlStrategyInputDatasets "
                        + "as iDs inner join iDs.inputDataset as iD with (iD.id = "
                        + getAndOrClause(datasetIDs, "iD.id") + ")").getResultList();

        if (strategyList != null && strategyList.size() > 0)
            throw new EmfException("Error: dataset used by control strategy " + strategyList.get(0) + ".");

        // check if dataset is an input inventory for some strategy (via the StrategyResult table, could be here for
        // historical reasons)
        strategyList = entityManager.createQuery(
                "select cS.name from ControlStrategyResult sR, ControlStrategy cS where "
                        + "sR.controlStrategyId = cS.id and (sR.inputDataset.id = "
                        + getAndOrClause(datasetIDs, "sR.inputDataset.id") + ")").getResultList();

        if (strategyList != null && strategyList.size() > 0)
            throw new EmfException("Error: dataset used by control strategy " + strategyList.get(0) + ".");

        // check if dataset is used as a region/county dataset for specific strategy measures
        strategyList = entityManager.createQuery(
                "select cS.name from ControlStrategy as cS inner join cS.controlMeasures as cM inner join "
                        + "cM.regionDataset as rD with (rD.id = " + getAndOrClause(datasetIDs, "rD.id") + ")").getResultList();

        if (strategyList != null && strategyList.size() > 0)
            throw new EmfException("Error: dataset used by control strategy " + strategyList.get(0) + ".");

        // check if dataset is used as a region/county dataset for specific strategy
        strategyList = entityManager.createQuery(
                "select cS.name from ControlStrategy cS where (cS.countyDataset.id = "
                        + getAndOrClause(datasetIDs, "cS.countyDataset.id") + ")").getResultList();

        if (strategyList != null && strategyList.size() > 0)
            throw new EmfException("Dataset used by control strategy " + strategyList.get(0) + ".");
    }
    
    @SuppressWarnings("unchecked")
    public List<Integer> notUsedByStrategies(int[] datasetIDs, User user, EntityManager entityManager) throws Exception {
        if (datasetIDs == null || datasetIDs.length == 0)
            return new ArrayList<Integer>();
        
        // check if dataset is an input inventory for some strategy (via the StrategyInputDataset table)
        List<Object[]> list = entityManager.createQuery(
                "select DISTINCT iDs.inputDataset, cS.name from ControlStrategy as cS inner join cS.controlStrategyInputDatasets "
                        + "as iDs inner join iDs.inputDataset as iD with (iD.id = "
                        + getAndOrClause(datasetIDs, "iD.id") + ")").getResultList();

        List<Integer> all = EmfArrays.convert(datasetIDs);
        String usedby = "used by control strategy";
        
        List<Integer> ids = getUsedDatasetIds(user, entityManager, list, usedby);
        all.removeAll(ids);
        
        if (all.size() == 0)
            return all;
        
        // check if dataset is an input inventory for some strategy (via the StrategyResult table, could be here for
        // historical reasons)
        list = entityManager.createQuery(
                "select DISTINCT sR.inputDataset, cS.name from ControlStrategyResult sR, ControlStrategy cS where "
                        + "sR.controlStrategyId = cS.id and (sR.inputDataset.id = "
                        + getAndOrClause(EmfArrays.convert(all), "sR.inputDataset.id") + ")").getResultList();

        ids = getUsedDatasetIds(user, entityManager, list, usedby);
        all.removeAll(ids);

        if (all.size() == 0)
            return all;
        
        // check if dataset is used as a region/county dataset for specific strategy measures
        list = entityManager.createQuery(
                "select DISTINCT cM.regionDataset, cS.name from ControlStrategy as cS inner join cS.controlMeasures as cM inner join "
                        + "cM.regionDataset as rD with (rD.id = " + getAndOrClause(EmfArrays.convert(all), "rD.id") + ")").getResultList();

        ids = getUsedDatasetIds(user, entityManager, list, usedby);
        all.removeAll(ids);
        
        if (all.size() == 0)
            return all;
        
        // check if dataset is used as a region/county dataset for specific strategy
        list = entityManager.createQuery(
                "select DISTINCT cS.countyDataset, cS.name from ControlStrategy cS where (cS.countyDataset.id = "
                        + getAndOrClause(EmfArrays.convert(all), "cS.countyDataset.id") + ")").getResultList();

        ids = getUsedDatasetIds(user, entityManager, list, usedby);
        all.removeAll(ids);
        
        return all;
    }

    private List<Integer> getUsedDatasetIds(User user, EntityManager entityManager, List<Object[]> list, String usedby) {
        List<Integer> ids = new ArrayList<Integer>();
        
        if (list != null && list.size() != 0) {
            for (int i = 0; i < list.size(); i++) {
                EmfDataset ds = (EmfDataset)list.get(i)[0];
                ids.add(ds.getId());
                setStatus(user.getUsername(), "Dataset \"" + ds.getName() + "\" " + usedby + ": " + list.get(i)[1] + ".", "Delete Dataset", entityManager);
            }
        }
        
        return ids;
    }

    public List<Integer> getModulesUsingDataset(int datasetId) throws Exception {
        DbServer dbServer = dbServerFactory.getDbServer();
        Datasource emfDatasource = dbServer.getEmfDatasource();
        DataQuery dataQuery = emfDatasource.query();
        // check if dataset is an input dataset for a module different than moduleId
        String query = "SELECT DISTINCT m.id FROM modules.modules_datasets md " +
                       "LEFT JOIN modules.modules m " +
                       "  ON m.id = md.module_id " +
                       "LEFT JOIN modules.module_types_versions mtv " +
                       "  ON mtv.id = m.module_type_version_id " +
                       "LEFT JOIN modules.module_types_versions_datasets mtvd " +
                       "  ON mtvd.module_type_version_id = mtv.id AND mtvd.placeholder_name = md.placeholder_name " +
                       "WHERE mtvd.mode IN ('IN', 'INOUT') AND md.dataset_id = " + datasetId + ";";

        List<Integer> moduleIds = new ArrayList<Integer>();
        ResultSet resultSet = null;
        try {
            resultSet = dataQuery.executeQuery(query);
            while (resultSet.next()) {
                moduleIds.add(resultSet.getInt(1));
            }
        } finally {
            if (resultSet != null)
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    LOG.error(e);
                    e.printStackTrace();
                }
        }
        return moduleIds;
    }
    
    
    public void checkIfUsedByControlPrograms(int[] datasetIDs, EntityManager entityManager) throws EmfException {
        // check if dataset is an input inventory for some control program (via the control_programs table)
        controlProgList = entityManager.createQuery(
                "select cP.name from ControlProgram as cP inner join cP.dataset as d with (d.id = "
                        + getAndOrClause(datasetIDs, "d.id") + ")").getResultList();

        if (controlProgList != null && controlProgList.size() > 0)
            throw new EmfException("Error: dataset used by control program " + controlProgList.get(0) + ".");

    }
    
    public boolean isUsedByControlPrograms(int datasetID, EntityManager entityManager) throws EmfException {
        // check if dataset is an input inventory for some control program (via the control_programs table)
        controlProgList = entityManager.createQuery(
                "select cP.name from ControlProgram as cP inner join cP.dataset as d with (d.id = " + datasetID + ")").getResultList();

        return (controlProgList != null && controlProgList.size() > 0);
    }
    
    public List<Integer> notUsedByControlPrograms(int[] datasetIDs, User user, EntityManager entityManager) throws Exception {
        // check if dataset is an input inventory for some control program (via the control_programs table)
        if (datasetIDs == null || datasetIDs.length == 0)
            return new ArrayList<Integer>();
        
        @SuppressWarnings("unchecked")
        List<Object[]> list = entityManager.createQuery(
                "select DISTINCT cP.dataset, cP.name from ControlProgram as cP inner join cP.dataset as d with (d.id = "
                        + getAndOrClause(datasetIDs, "d.id") + ")").getResultList();

        List<Integer> all = EmfArrays.convert(datasetIDs);
        
        if (list == null || list.size() == 0)
            return all;
        
        List<Integer> ids = getUsedDatasetIds(user, entityManager, list, "used by control program");
        all.removeAll(ids);
        
        return all;
    }
    
    @SuppressWarnings("unchecked")
    public List<Integer> notUsedBySectorScnarios(int[] datasetIDs, User user, EntityManager entityManager) throws Exception {
        if (datasetIDs == null || datasetIDs.length == 0)
            return new ArrayList<Integer>();
        
        // check if dataset is an eecsMapppingDataset in SectorScenarion table
        List<Object[]> list = entityManager.createQuery(
                "select DISTINCT SS.eecsMapppingDataset, SS.name from SectorScenario as SS inner join SS.eecsMapppingDataset "
                        + "as eMD with (eMD.id = "
                        + getAndOrClause(datasetIDs, "eMD.id") + ")").getResultList();

        List<Integer> all = EmfArrays.convert(datasetIDs);
        String usedby = "used by SectorScenario";
        
        List<Integer> ids = getUsedDatasetIds(user, entityManager, list, usedby);
        all.removeAll(ids);
        
        if (all.size() == 0)
            return all;
        
        // check if dataset is an sectorMapppingDataset in SectorScenario table
        list = entityManager.createQuery(
                "select DISTINCT SS.sectorMapppingDataset, SS.name from SectorScenario as SS inner join SS.sectorMapppingDataset "
                        + "as sMD with (sMD.id = "
                        + getAndOrClause(EmfArrays.convert(all), "sMD.id") + ")").getResultList();

        ids = getUsedDatasetIds(user, entityManager, list, usedby);
        all.removeAll(ids);

        if (all.size() == 0)
            return all;
        
        // check if dataset is used as inputDataset for specific SectorScenarioInventory
        list = entityManager.createQuery(
                "select DISTINCT invs.dataset, SS.name from SectorScenario as SS inner join SS.inventories "
                        + "as invs inner join invs.dataset as ds with (ds.id = "
                        + getAndOrClause(EmfArrays.convert(all), "ds.id") + ")").getResultList();

        ids = getUsedDatasetIds(user, entityManager, list, usedby);
        all.removeAll(ids);
        
        if (all.size() == 0)
            return all;
        
        // check if dataset is used as an inventory dataset for specific SectorScenarioOutput
        list = entityManager.createQuery(
                "select DISTINCT SSO.inventoryDataset, SS.name from SectorScenario SS, SectorScenarioOutput SSO where "
                + "SS.id = SSO.sectorScenarioId AND (SSO.inventoryDataset.id = "
                + getAndOrClause(EmfArrays.convert(all), "SSO.inventoryDataset.id") + ")").getResultList();

        ids = getUsedDatasetIds(user, entityManager, list, usedby);
        all.removeAll(ids);
        
        if (all.size() == 0)
            return all;
        
        // check if dataset is used as an output dataset for specific SectorScenarioOutput
        list = entityManager.createQuery(
                "select DISTINCT SSO.outputDataset, SS.name from SectorScenario SS, SectorScenarioOutput SSO where "
                + "SS.id = SSO.sectorScenarioId AND (SSO.outputDataset.id = "
                + getAndOrClause(EmfArrays.convert(all), "SSO.outputDataset.id") + ")").getResultList();

        ids = getUsedDatasetIds(user, entityManager, list, usedby);
        all.removeAll(ids);
        
        return all;
    }
    
    public List<Integer> notUsedByFast(int[] datasetIDs, User user, DbServer dbServer, EntityManager entityManager) throws Exception {
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
        ids = getRefdDatasetIds(user, EmfArrays.convert(all), dataQuery, query, "cancer_risk_dataset_id", entityManager);
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
        ids = getRefdDatasetIds(user, EmfArrays.convert(all), dataQuery, query, "species_mapping_dataset_id", entityManager);
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
        ids = getRefdDatasetIds(user, EmfArrays.convert(all), dataQuery, query, "transfer_coefficients_dataset_id", entityManager);
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
        ids = getRefdDatasetIds(user, EmfArrays.convert(all), dataQuery, query, "domain_population_dataset_id", entityManager);
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
        ids = getRefdDatasetIds(user, EmfArrays.convert(all), dataQuery, query, "invtable_dataset_id", entityManager);
        all.removeAll(ids);
        
        if (all.size() == 0)
            return all;
        
        //Check if dataset is in fast.fast_analyses table, 'cancer_risk_dataset_id' column
        query = "SELECT DISTINCT fa.cancer_risk_dataset_id, ds.name, fa.name AS analname, '' AS runname from fast.fast_analyses as fa "
        		+ "JOIN emf.datasets as ds ON fa.cancer_risk_dataset_id = ds.id " 
                + "WHERE fa.cancer_risk_dataset_id="
                + getAndOrClause(EmfArrays.convert(all), "fa.cancer_risk_dataset_id");
        ids = getRefdDatasetIds(user, EmfArrays.convert(all), dataQuery, query, "cancer_risk_dataset_id", entityManager);
        all.removeAll(ids);
        
        if (all.size() == 0)
            return all;
        
        //Check if dataset is in fast.fast_analysis_outputs table, 'output_dataset_id' column
        query = "SELECT DISTINCT fao.output_dataset_id, ds.name, fa.name AS analname, '' AS runname from fast.fast_analysis_outputs as fao "
        		+ "JOIN emf.datasets as ds ON fao.output_dataset_id = ds.id "
                + "JOIN fast.fast_analyses as fa ON fao.fast_analysis_id = fa.id "
        		+ "WHERE fao.output_dataset_id="
        		+ getAndOrClause(EmfArrays.convert(all), "fao.output_dataset_id");
        ids = getRefdDatasetIds(user, EmfArrays.convert(all), dataQuery, query, "output_dataset_id", entityManager);
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
        ids = getRefdDatasetIds(user, EmfArrays.convert(all), dataQuery, query, "dataset_id", entityManager);
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
        ids = getRefdDatasetIds(user, EmfArrays.convert(all), dataQuery, query, "gridded_smk_dataset_id", entityManager);
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
        ids = getRefdDatasetIds(user, EmfArrays.convert(all), dataQuery, query, "base_nonpoint_dataset_id", entityManager);
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
        ids = getRefdDatasetIds(user, EmfArrays.convert(all), dataQuery, query, "invtable_dataset_id", entityManager);
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
        ids = getRefdDatasetIds(user, EmfArrays.convert(all), dataQuery, query, "inventory_dataset_id", entityManager);
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
        ids = getRefdDatasetIds(user, EmfArrays.convert(all), dataQuery, query, "output_dataset_id", entityManager);
        all.removeAll(ids);
        
        return all;
    }

    public boolean isUsedByFast(int datasetId, User user, DbServer dbServer, EntityManager entityManager) throws Exception {
        int[] datasetIDs = new int[] { datasetId };
        List<Integer> list = notUsedByFast(datasetIDs, user, dbServer, entityManager);
        return (list.size() == 0);
    }
        
    @SuppressWarnings("unchecked")
    public List<Integer> notUsedByTemporalAllocations(int[] datasetIDs, User user, EntityManager entityManager) throws Exception {
        if (datasetIDs == null || datasetIDs.length == 0)
            return new ArrayList<Integer>();
        
        // check if dataset is an input inventory (via the TemporalAllocationInputDataset table)
        List<Object[]> list = entityManager.createQuery(
                "select DISTINCT iDs.inputDataset, tA.name from TemporalAllocation as tA inner join tA.temporalAllocationInputDatasets "
                        + "as iDs inner join iDs.inputDataset as iD with (iD.id = "
                        + getAndOrClause(datasetIDs, "iD.id") + ")").getResultList();

        List<Integer> all = EmfArrays.convert(datasetIDs);
        String usedby = "used by temporal allocation";
        
        List<Integer> ids = getUsedDatasetIds(user, entityManager, list, usedby);
        all.removeAll(ids);
        
        if (all.size() == 0)
            return all;
        
        // check if dataset is an output (via the TemporalAllocationOutputDataset table)
        list = entityManager.createQuery(
                "select DISTINCT oDs.outputDataset, tA.name from TemporalAllocationOutput oDs, TemporalAllocation tA where "
                        + "oDs.temporalAllocationId = tA.id and (oDs.outputDataset.id = "
                        + getAndOrClause(EmfArrays.convert(all), "oDs.outputDataset.id") + ")").getResultList();

        ids = getUsedDatasetIds(user, entityManager, list, usedby);
        all.removeAll(ids);
        
        if (all.size() == 0)
            return all;
        
        // check if dataset is a profile or xref (via the TemporalAllocation table)
        list = entityManager.createQuery(
                "select DISTINCT tA.monthlyProfileDataset, tA.name from TemporalAllocation tA where "
                        + "(tA.monthlyProfileDataset.id = "
                        + getAndOrClause(EmfArrays.convert(all), "tA.monthlyProfileDataset.id") + ")").getResultList();
        ids = getUsedDatasetIds(user, entityManager, list, usedby);
        all.removeAll(ids);
        
        if (all.size() == 0)
            return all;

        list = entityManager.createQuery(
                "select DISTINCT tA.weeklyProfileDataset, tA.name from TemporalAllocation tA where "
                        + "(tA.weeklyProfileDataset.id = "
                        + getAndOrClause(EmfArrays.convert(all), "tA.weeklyProfileDataset.id") + ")").getResultList();
        ids = getUsedDatasetIds(user, entityManager, list, usedby);
        all.removeAll(ids);
        
        if (all.size() == 0)
            return all;
        
        list = entityManager.createQuery(
                "select DISTINCT tA.dailyProfileDataset, tA.name from TemporalAllocation tA where "
                        + "(tA.dailyProfileDataset.id = "
                        + getAndOrClause(EmfArrays.convert(all), "tA.dailyProfileDataset.id") + ")").getResultList();
        ids = getUsedDatasetIds(user, entityManager, list, usedby);
        all.removeAll(ids);
        
        if (all.size() == 0)
            return all;

        list = entityManager.createQuery(
                "select DISTINCT tA.xrefDataset, tA.name from TemporalAllocation tA where "
                        + "(tA.xrefDataset.id = "
                        + getAndOrClause(EmfArrays.convert(all), "tA.xrefDataset.id") + ")").getResultList();
        ids = getUsedDatasetIds(user, entityManager, list, usedby);
        all.removeAll(ids);
        
        if (all.size() == 0)
            return all;
        
        return all;
    }

    public boolean isUsedByTemporalAllocations(int datasetId, User user, EntityManager entityManager) throws Exception {
        int[] datasetIDs = new int[] { datasetId };
        List<Integer> list = notUsedByTemporalAllocations(datasetIDs, user, entityManager);
        return (list.size() == 0);
    }
    
    @SuppressWarnings("unchecked")
    public List<Integer> notUsedByModules(int[] datasetIDs, User user, EntityManager entityManager) throws Exception {
        if (datasetIDs == null || datasetIDs.length == 0)
            return new ArrayList<Integer>();
        
        // check if dataset is in the modules.modules_datasets table
        List<Object[]> list = entityManager.createQuery(
                "SELECT DISTINCT dataset, m.name " +
                  "FROM Module as m, ModuleDataset as md, EmfDataset as dataset " +
                 "WHERE md.module.id = m.id " +
                   "AND dataset.id = md.datasetId " +
                   "AND (md.datasetId = " + getAndOrClause(datasetIDs, "md.datasetId") + ")").getResultList();
        
        List<Integer> all = EmfArrays.convert(datasetIDs);
        String usedby = "used by module";
        
        List<Integer> ids = getUsedDatasetIds(user, entityManager, list, usedby);
        all.removeAll(ids);
        
        if (all.size() == 0)
            return all;
        
        // check for OUT NEW datasets in most recent history records
        list = entityManager.createNativeQuery(
                "SELECT d.id, d.name, m.name AS module " +
                  "FROM (SELECT h.module_id, MAX(h.id) AS history_id " +
                          "FROM modules.history h " +
                      "GROUP BY h.module_id) v " +
                  "JOIN modules.modules m " +
                    "ON v.module_id = m.id " +
                  "JOIN modules.modules_datasets md " +
                    "ON v.module_id = md.module_id " +
                  "JOIN modules.history h " +
                    "ON v.history_id = h.id " +
                  "JOIN modules.history_datasets hd " +
                    "ON v.history_id = hd.history_id " +
                  "JOIN emf.datasets d " +
                    "ON d.id = hd.dataset_id " +
                 "WHERE h.result = 'SUCCESS' " +
                   "AND md.output_method = 'NEW' " +
                   "AND hd.placeholder_name = md.placeholder_name " +
                   "AND (hd.dataset_id = " + getAndOrClause(datasetIDs, "hd.dataset_id") + ")").getResultList();
        ids = new ArrayList<Integer>();
        
        if (list != null && list.size() != 0) {
            for (int i = 0; i < list.size(); i++) {
                ids.add((Integer)list.get(i)[0]);
                setStatus(user.getUsername(), "Dataset \"" + list.get(i)[1] + "\" " + usedby + ": " + list.get(i)[2] + ".", "Delete Dataset", entityManager);
            }
        }
        all.removeAll(ids);
        
        return all;
    }
        
    private List<Integer> getRefdDatasetIds(User user, int[] idArray, DataQuery dataQuery, String query, String dsId, EntityManager entityManager)
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
                        + msg + ".", "Delete Dataset", entityManager);
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

    private void deleteFromEmfTables(int[] datasetIDs, TableCreator tableTool, EntityManager entityManager) {
        deleteFromObjectTable(datasetIDs, Version.class, "datasetId", entityManager);
        deleteFromObjectTable(datasetIDs, AccessLog.class, "datasetId", entityManager);
        deleteFromObjectTable(datasetIDs, DatasetNote.class, "datasetId", entityManager);
        deleteFromObjectTable(datasetIDs, Revision.class, "datasetId", entityManager);
        deleteFromObjectTable(datasetIDs, ExternalSource.class, "datasetId", entityManager);

        try {
            dropQAStepResultTable(datasetIDs, tableTool, entityManager);
        } catch (Exception e) {
            LOG.error(e);
        }

        deleteFromObjectTable(datasetIDs, QAStepResult.class, "datasetId", entityManager);
        deleteFromObjectTable(datasetIDs, QAStep.class, "datasetId", entityManager);
        deleteFromObjectTable(datasetIDs, ControlStrategyResult.class, "detailedResultDataset.id", entityManager);
        deleteFromObjectTable(datasetIDs, ControlStrategyResult.class, "controlledInventoryDataset.id", entityManager);
    }

    int deleteFromObjectTable(int[] datasetIDs, Class<?> clazz, String attrName, EntityManager entityManager) {
        int[] deletedEntities = {0};

        try {
            String hqlDelete = "DELETE FROM " + clazz.getSimpleName() + " obj WHERE obj." + attrName + " = "
                    + getAndOrClause(datasetIDs, "obj." + attrName);

            if (DebugLevels.DEBUG_16())
                System.out.println("hql delete string: " + hqlDelete);

            hibernateFacade
                .executeInsideTransaction(em -> {
                    deletedEntities[0] = em.createQuery(hqlDelete).executeUpdate();
                    }, entityManager);

            return deletedEntities[0];
        } catch (HibernateException e) {
            LOG.error(e);
            return 0;
        } finally {
            if (DebugLevels.DEBUG_16())
                LOG.warn(deletedEntities[0] + " items deleted from " + clazz.getName() + " table.");
        }
    }

    public void deleteFromOutputsTable(int[] datasetIDs, EntityManager entityManager) throws EmfException {
        int[] updatedItems = {0};

        try {
            String firstPart = "UPDATE " + CaseOutput.class.getSimpleName() + " obj SET ";
            String secondPart = " WHERE obj.datasetId = " + getAndOrClause(datasetIDs, "obj.datasetId");
            String updateQuery = firstPart + "obj.message = :msg, obj.datasetId = :id" + secondPart;

            if (DebugLevels.DEBUG_16())
                System.out.println("hql update string: " + updateQuery);

            hibernateFacade
                .executeInsideTransaction(em -> {
                    updatedItems[0] = 
                        em
                            .createQuery(updateQuery)
                            .setParameter("msg", "Associated dataset deleted")
                            .setParameter("id", 0)
                            .executeUpdate();
                    }, entityManager);
            

            if (DebugLevels.DEBUG_16())
                System.out.println(updatedItems[0] + " items updated.");
        } catch (HibernateException e) {
            throw new EmfException(e.getMessage());
        } finally {
            if (DebugLevels.DEBUG_16())
                LOG.warn(updatedItems[0] + " items updated from " + CaseOutput.class.getName() + " table.");
        }
    }

    private void dropDataTables(EmfDataset[] datasets, TableCreator tableTool, EntityManager entityManager) {
        for (int i = 0; i < datasets.length; i++) {
            try {
                dropDataTables(tableTool, datasets[i], entityManager);
            } catch (Exception exc) {
                LOG.error(exc);
                setStatus(datasets[0].getCreator(), exc.getMessage(), "Delete Dataset", entityManager);
            }
        }
    }

    private void dropDataTables(TableCreator tableTool, EmfDataset dataset, EntityManager entityManager) {
        DatasetType type = dataset.getDatasetType();

        if (type != null && type.isExternal())
            return;

        InternalSource[] sources = dataset.getInternalSources();

        for (int i = 0; i < sources.length; i++) {
            try {
                dropIndividualTable(tableTool, sources[i], type, dataset.getId(), dataset.getCreator(), entityManager);
            } catch (Exception exc) { // if there is a problem with one table, keep going
                LOG.error(exc);
                setStatus(dataset.getCreator(), exc.getMessage(), "Delete Dataset", entityManager);
            }
        }
    }

    private void dropIndividualTable(TableCreator tableTool, InternalSource source, DatasetType type, 
            int dsID, String user, EntityManager entityManager) {
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
            setStatus(user, "Error deleting emission table: " + table + ".", "Delete Dataset", entityManager);
        }
    }

    private void dropQAStepResultTable(int[] datasetIDs, TableCreator tableTool, EntityManager entityManager) {
        List tables = entityManager.createQuery(
                "SELECT obj.table from " + QAStepResult.class.getSimpleName() + " as obj WHERE obj.datasetId = "
                        + getAndOrClause(datasetIDs, "obj.datasetId")).getResultList();

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
                setStatus("", "Error deleting emission table: " + table + ".", "Delete Dataset", entityManager);
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

    public EmfDataset[] getCaseFreeDatasets(EmfDataset[] datasets, EntityManager entityManager) {
        List<EmfDataset> list = new ArrayList<EmfDataset>();

        for (EmfDataset dataset : datasets) {
            try {
                checkIfUsedByCases(new int[] { dataset.getId() }, entityManager);
                list.add(dataset);
            } catch (Exception e) {
                LOG.warn(e.getMessage());
            }
        }

        return list.toArray(new EmfDataset[0]);
    }

    public EmfDataset[] getControlStrateyFreeDatasets(EmfDataset[] datasets, EntityManager entityManager) {
        List<EmfDataset> list = new ArrayList<EmfDataset>();

        for (EmfDataset dataset : datasets) {
            try {
                checkIfUsedByStrategies(new int[] { dataset.getId() }, entityManager);
                list.add(dataset);
            } catch (Exception e) {
                LOG.warn(e.getMessage());
            }
        }

        return list.toArray(new EmfDataset[0]);
    }

    public List<EmfDataset> deletedDatasets(User user, EntityManager entityManager) {
        CriteriaBuilderQueryRoot<EmfDataset> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(EmfDataset.class, entityManager);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<EmfDataset> root = criteriaBuilderQueryRoot.getRoot();
        Predicate statusPred = builder.equal(root.get("status"), "Deleted");
        Predicate namePred = builder.equal(root.get("creator"), user.getUsername());
        Predicate predicate = statusPred;

        if (!user.isAdmin())
            predicate = builder.and(statusPred, namePred);

        return hibernateFacade.get(entityManager, criteriaBuilderQueryRoot, predicate);
    }

    public void removeEmptyDatasets(User user, DbServer dbServer, EntityManager entityManager) throws EmfException, SQLException {
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

            deleteControlStrategies(tempIds, entityManager);
            decoupleDSFromCases(tempIds, entityManager);
            setDSAsDeleted(tempIds, entityManager);

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

    private void deleteControlStrategies(int[] dsIDsWithNoEmisData, EntityManager entityManager) throws EmfException {
        try {
            checkIfUsedByStrategies(dsIDsWithNoEmisData, entityManager);
            // checkIfUsedByControlPrograms(dsIDsWithNoEmisData, entityManager);
        } catch (Exception e) {
            String name = "";

            if (strategyList != null && strategyList.size() > 0)
                name = strategyList.get(0).toString();

            if (controlProgList != null && controlProgList.size() > 0)
                name = controlProgList.get(0).toString();

            throw new EmfException("Please delete control strategies/programs before purge: " + name);
        }
    }

    private void decoupleDSFromCases(int[] dsIDsWithNoEmisData, EntityManager entityManager) throws EmfException {
        int[] updatedItems = {0};

        try {
            String firstPart = "UPDATE " + CaseInput.class.getSimpleName()
                    + " obj SET obj.dataset = null, obj.version = null";
            String secondPart = " WHERE obj.dataset.id = " + getAndOrClause(dsIDsWithNoEmisData, "obj.dataset.id");
            String updateQuery = firstPart + secondPart;

            if (DebugLevels.DEBUG_16())
                System.out.println("hql update string: " + updateQuery);

            hibernateFacade
                .executeInsideTransaction(em -> {
                    updatedItems[0] = em.createQuery(updateQuery).executeUpdate();
                    }, entityManager);

            if (DebugLevels.DEBUG_16())
                System.out.println(updatedItems[0] + " items updated.");
        } catch (HibernateException e) {
            throw new EmfException(e.getMessage());
        } finally {
            if (DebugLevels.DEBUG_16())
                LOG.warn(updatedItems[0] + " items updated from " + CaseInput.class.getName() + " table.");
        }
    }

    private void decoupleDSFromModules(int[] dsIDs, EntityManager entityManager) throws EmfException {
        int updatedItems[] = {0};

        try {
            String firstPart = "UPDATE " + ModuleDataset.class.getSimpleName()
                    + " obj SET obj.datasetId = null, obj.version = null";
            String secondPart = " WHERE obj.datasetId = " + getAndOrClause(dsIDs, "obj.datasetId");
            String updateQuery = firstPart + secondPart;

            if (DebugLevels.DEBUG_16())
                System.out.println("hql update string: " + updateQuery);

            hibernateFacade
                .executeInsideTransaction(em -> {
                    updatedItems[0] = em.createQuery(updateQuery).executeUpdate();
                    }, entityManager);

            if (DebugLevels.DEBUG_16())
                System.out.println(updatedItems[0] + " items updated.");
        } catch (HibernateException e) {
            throw new EmfException(e.getMessage());
        } finally {
            if (DebugLevels.DEBUG_16())
                LOG.warn(updatedItems[0] + " items updated from " + ModuleDataset.class.getName() + " table.");
        }
    }

    private void setDSAsDeleted(int[] dsIDsWithNoEmisData, EntityManager entityManager) throws EmfException {
        int[] updatedItems = {0};

        try {
            String firstPart = "UPDATE " + EmfDataset.class.getSimpleName() + " obj SET ";
            String secondPart = " WHERE obj.id = " + getAndOrClause(dsIDsWithNoEmisData, "obj.id");
            String updateQuery = firstPart + "obj.status = :sts" + secondPart;

            if (DebugLevels.DEBUG_16())
                System.out.println("hql update string: " + updateQuery);
            hibernateFacade.executeInsideTransaction(em -> {updatedItems[0] = em.createQuery(updateQuery).setParameter("sts", "Deleted").executeUpdate();}, entityManager);

            if (DebugLevels.DEBUG_16())
                System.out.println(updatedItems[0] + " items updated.");
        } catch (HibernateException e) {
            throw new EmfException(e.getMessage());
        } finally {
            if (DebugLevels.DEBUG_16())
                LOG.warn(updatedItems[0] + " items updated from " + EmfDataset.class.getName() + " table.");
        }
    }

    public List<String> getDatasetNamesStartWith(String start, EntityManager entityManager) {
        String query = "SELECT DS.name FROM " + EmfDataset.class.getSimpleName() + " AS DS WHERE lower(DS.name) LIKE "
                + "'%" + start.toLowerCase().trim() + "%' ORDER BY DS.name";
        return entityManager.createQuery(query).getResultList();
    }

    public void updateExternalSrcsWithoutLocking(ExternalSource[] srcs, EntityManager entityManager) {
        // NOTE: update without locking objects
        hibernateFacade.update(srcs, entityManager);
    }

    public String[] getTableColumnDistinctValues(int datasetId, int datasetVersion, String columnName,
            String whereFilter, String sortOrder, EntityManager entityManager, DbServer dbServer) throws EmfException {
        List<String> values = new ArrayList<String>();
        ResultSet rs = null;
        try {
            EmfDataset dataset = getDataset(entityManager, datasetId);
            Version version = version(entityManager, datasetId, dataset.getDefaultVersion());
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

    private Version version(EntityManager entityManager, int datasetId, int version) {
        Versions versions = new Versions();
        return versions.get(datasetId, version, entityManager);
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
        // if first character start with a number let's delineate with quotes "table_name"
        if (NumberUtils.isNumber(table.charAt(0) + ""))
            table = "\"" + table + "\"";
        return "emissions." + table;
    }

    public void importRemoteDataset() {
        //
    }
    
    public List<EmfDataset> findSimilarDatasets(EmfDataset ds, String qaStep, String qaArgument, 
            int[] usedByCasesId, String dataValueFilter, boolean unconditional, int userId, EntityManager entityManager) throws Exception {
    
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

        List<EmfDataset> ds1 = entityManager.createQuery(dsquery).getResultList();

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

        List<EmfDataset> ds2 = entityManager.createQuery(dstypequery).getResultList();
        List<EmfDataset> all = new ArrayList<EmfDataset>();
        all.addAll(ds1);
        all.addAll(ds2);

        TreeSet<EmfDataset> set = new TreeSet<EmfDataset>(all);
        List<EmfDataset> total = new ArrayList<EmfDataset>(set);
        
        total = filter(total, dataValueFilter, entityManager );

        if (total.size() > 300 && !unconditional) {
            total.get(0).setName("Alert!!! More than 300 datasets selected.");
            return total.subList(0, 1);
        }

        return total;
    }
    
    private List<EmfDataset> filter( List<EmfDataset> datasets, String dataValueFilter, EntityManager entityManager) throws SQLException, EmfException {
        if ( datasets == null || dataValueFilter==null || dataValueFilter.trim().isEmpty()) {
            return datasets;
        }
        
        DbServer dbServer = this.dbServerFactory.getDbServer();
        Datasource datasource = dbServer.getEmissionsDatasource();
        Connection connection = datasource.getConnection();
        
        List<EmfDataset> filteredDatasets = new ArrayList<EmfDataset>();
        int count = 0;
        for ( EmfDataset dataset : datasets) {
            EmfDataset ds = this.getDataset(entityManager, dataset.getId());
            String sqlStr = "SELECT * FROM " + this.qualifiedEmissionTableName(ds) + " WHERE " + dataValueFilter + " LIMIT 1;";
            try {
                Statement statement = connection.createStatement();
                ResultSet resultSet = null;
                try {
                    resultSet = statement.executeQuery(sqlStr);
                } catch (SQLException e) {
                    //they're is possibility for old and missing tables, so if the first dataset/table doesn't have any issues, 
                    //then let's ignore the other issue tables...
                    if (count == 0)
                        throw new EmfException("Error with Data Value Filter, incorrect filter SQL syntax: " + dataValueFilter);
                    
                }
                ++count;
                
                if ( resultSet != null && resultSet.next()) {
                    filteredDatasets.add(dataset);
                }
                resultSet.close();
            } catch (SQLException e) {
                throw new SQLException("Error with Data Value Filter, incorrect filter SQL syntax: " + dataValueFilter);
            } catch (EmfException e) {
                throw e;
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
    
    public synchronized void setStatus(String username, String message, String msgType, EntityManager entityManager) {
        Status endStatus = new Status();
        endStatus.setUsername(username);
        endStatus.setType(msgType);
        endStatus.setMessage(message);
        endStatus.setTimestamp(new Date());

        hibernateFacade.add(endStatus, entityManager);
    }

    public boolean checkBizzareCharInColumn(DbServer dbServer, EntityManager entityManager, int datasetId, int version, String colName) throws SQLException, EmfException { 
        // only for dataset that has column PLANT BUG3588
        EmfDataset dataset = this.getDataset(entityManager, datasetId);
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
        String countQuery = "SELECT count(*) FROM " + qualifiedTable + getWhereClause(version(entityManager,datasetId,version), entityManager) + " and " + colName + " ~* '[[:cntrl:]]'"; //consider unicode?? |chr(127)-chr(65535)]'";
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
