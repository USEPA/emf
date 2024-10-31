package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.BasicSearchFilter;
import gov.epa.emissions.framework.services.basic.EmfProperty;
import gov.epa.emissions.framework.services.basic.SearchDAOUtility;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyConstraint;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;
import gov.epa.emissions.framework.services.cost.controlStrategy.StrategyGroup;
import gov.epa.emissions.framework.services.cost.controlStrategy.StrategyResultType;
import gov.epa.emissions.framework.services.data.DataServiceImpl;
import gov.epa.emissions.framework.services.data.DataServiceImpl.DeleteType;
import gov.epa.emissions.framework.services.data.DatasetDAO;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.persistence.EmfPropertiesDAO;
import gov.epa.emissions.framework.services.persistence.HibernateFacade;
import gov.epa.emissions.framework.services.persistence.HibernateFacade.CriteriaBuilderQueryRoot;
import gov.epa.emissions.framework.services.persistence.LockingScheme;
import gov.epa.emissions.framework.tasks.DebugLevels;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.HibernateException;

public class ControlStrategyDAO {
    private LockingScheme lockingScheme;

    private HibernateFacade hibernateFacade;

    private EntityManagerFactory entityManagerFactory;

    private DbServerFactory dbServerFactory;
    
    private DatasetDAO datasetDao;

    public ControlStrategyDAO() {
        lockingScheme = new LockingScheme();
        hibernateFacade = new HibernateFacade();
        this.datasetDao = new DatasetDAO();
    }

    public ControlStrategyDAO(DbServerFactory dbServerFactory, EntityManagerFactory entityManagerFactory) {
        this();
        this.dbServerFactory = dbServerFactory;
        this.entityManagerFactory = entityManagerFactory;
    }

    public int add(ControlStrategy element, EntityManager entityManager) {
        hibernateFacade.add(element, entityManager);
        return element.getId();
    }

    public void add(ControlStrategyConstraint element, EntityManager entityManager) {
        hibernateFacade.add(element, entityManager);
    }

    public int add(ControlStrategyResult element, EntityManager entityManager) {
        hibernateFacade.add(element, entityManager);
        return element.getId();
    }

    public String getControlStrategyRunStatus(int controlStrategyId, EntityManager entityManager) {
        return (String)entityManager.createQuery("select cS.runStatus from ControlStrategy cS where cS.id = " + controlStrategyId).getSingleResult();
    }

    public Long getControlStrategyRunningCount(EntityManager entityManager) {
        return (Long)entityManager.createQuery("select count(*) as total from ControlStrategy cS where cS.runStatus = 'Running'").getSingleResult();
    }

    public List<ControlStrategy> getControlStrategiesByRunStatus(String runStatus, EntityManager entityManager) {
//        Criterion critRunStatus = Restrictions.eq("runStatus", runStatus);
//        return hibernateFacade.get(ControlStrategy.class, critRunStatus, Order.asc("lastModifiedDate"), entityManager);
//
        return entityManager
                .createQuery("select new ControlStrategy(cS.id, cS.name) from ControlStrategy cS where cS.runStatus = :runStatus order by cS.lastModifiedDate", ControlStrategy.class)
                .setParameter("runStatus", runStatus)
                .getResultList();
    }

    public void setControlStrategyRunStatusAndCompletionDate(int controlStrategyId, String runStatus, Date completionDate, EntityManager entityManager) {
        try {
            hibernateFacade.executeInsideTransaction(em -> {
                em
                    .createQuery("update ControlStrategy set runStatus = :status, lastModifiedDate = :date, completionDate = :completionDate where id = :id")
                    .setParameter("status", runStatus)
                    .setParameter("date", new Date())
                    .setParameter("completionDate", completionDate)
                    .setParameter("id", Integer.valueOf(controlStrategyId))
                    .executeUpdate();
                }, entityManager);
        } catch (HibernateException e) {
            throw e;
        }
    }

    // return ControlStrategies orderby name
    public List<ControlStrategy> all(EntityManager entityManager) {

//        "Name", "Last Modified", "Run Status", "Region", 
//        "Target Pollutant", "Total Cost", "Reduction", 
//        "Project", "Strategy Type", "Cost Year", 
//        "Inv. Year", "Creator"
//        element.getName(), format(element.getLastModifiedDate()), element.getRunStatus(), region(element),
//        element.getTargetPollutant(), getTotalCost(element.getId()), getReduction(element.getId()), 
//        project(element), analysisType(element), costYear(element), 
//        "" + (element.getInventoryYear() != 0 ? element.getInventoryYear() : ""), 
//        element.getCreator().getName()
        return entityManager
                .createQuery("select new ControlStrategy(cS.id, cS.name, " +
                    "cS.lastModifiedDate, cS.runStatus, " +
                    "R, TP, " +
                    "P, ST, " +
                    "cS.costYear, cS.inventoryYear, " +
    //                "cS.creator, (select sum(sR.totalCost) from ControlStrategyResult sR where sR.controlStrategyId = cS.id), (select sum(sR.totalReduction) from ControlStrategyResult sR where sR.controlStrategyId = cS.id)) " +
                    "cS.creator, cS.totalCost, cS.totalReduction, cS.isFinal) " +
                    "from ControlStrategy as cS " +
                    "left join cS.targetPollutant as TP " +
                    "left join cS.strategyType as ST " +
                    "left join cS.region as R " +
                    "left join cS.project as P " +
                    "order by cS.name", ControlStrategy.class)
                .getResultList();
        //return hibernateFacade.getAll(ControlStrategy.class, Order.asc("name"), entityManager);
    }

    public List<ControlStrategy> getControlStrategies(EntityManager entityManager, BasicSearchFilter searchFilter) throws EmfException {
        String hql = "select distinct new ControlStrategy(cs.id, cs.name, " +
                "cs.lastModifiedDate, cs.runStatus, " +
                "region, targetPollutant, " +
                "project, strategyType, " +
                "cs.costYear, cs.inventoryYear, " +
//                "cS.creator, (select sum(sR.totalCost) from ControlStrategyResult sR where sR.controlStrategyId = cS.id), (select sum(sR.totalReduction) from ControlStrategyResult sR where sR.controlStrategyId = cS.id)) " +
                "cs.creator, cs.totalCost, cs.totalReduction, cs.isFinal) " +
                "from ControlStrategy as cs " +
                "left join cs.targetPollutant as targetPollutant " +
                "left join cs.strategyType as strategyType " +
                "left join cs.region as region " +
                "left join cs.project as project " +
                "left join cs.creator as creator " +
                "left join cs.controlStrategyInputDatasets as inputDataset " +
                "left join cs.controlPrograms as controlProgram " +
                "left join cs.controlMeasures as controlMeasure " +
                "left join cs.controlMeasureClasses as controlMeasureClass ";
        //
        if (StringUtils.isNotBlank(searchFilter.getFieldName())
                && StringUtils.isNotBlank(searchFilter.getFieldValue())) {
            String whereClause = SearchDAOUtility.buildSearchCriterion(new ControlStrategyFilter(), searchFilter);
            if (StringUtils.isNotBlank(whereClause))
                hql += " where " + whereClause;
        }
        return entityManager.createQuery(hql, ControlStrategy.class).getResultList();
    }
//    // return ControlStrategies orderby name
//    public List test(EntityManager entityManager) {
//        //check if dataset is a input inventory for some strategy (via the StrategyInputDataset table)
//        List list = entityManager.createQuery("select cS.name from ControlStrategy as cS inner join cS.controlStrategyInputDatasets as iDs inner join iDs.inputDataset as iD with iD.id = 1221").list();
//        //check if dataset is a input inventory for some strategy (via the StrategyResult table, could be here for historical reasons)
//        list = entityManager.createQuery("select cS.name from ControlStrategyResult sR, ControlStrategy cS where sR.controlStrategyId = cS.id and sR.inputDataset.id = 1221").list();
//        //check if dataset is a detailed result dataset for some strategy
//        list = entityManager.createQuery("select cS.name from ControlStrategyResult sR, ControlStrategy cS where sR.controlStrategyId = cS.id and sR.detailedResultDataset.id = 1221").list();
//        //check if dataset is a controlled inventory for some strategy
//        list = entityManager.createQuery("select cS.name from ControlStrategyResult sR, ControlStrategy cS where sR.controlStrategyId = cS.id and sR.controlledInventoryDataset.id = 1221").list();
//        //check if dataset is used as a region/county dataset for specific strategy measures
//        list = entityManager.createQuery("select cS.name from ControlStrategy as cS inner join cS.controlMeasures as cM inner join cM.regionDataset as rD with rD.id = 1221").list();
//        //check if dataset is used as a region/county dataset for specific strategy
//        list = entityManager.createQuery("select cS.name from ControlStrategy cS where cS.countyDataset.id = 1221").list();
//
//        return list;
//    }

    public List<StrategyType> getAllStrategyTypes(EntityManager entityManager) {
        CriteriaBuilderQueryRoot<StrategyType> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(StrategyType.class, entityManager);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<StrategyType> root = criteriaBuilderQueryRoot.getRoot();

        return hibernateFacade.getAll(criteriaBuilderQueryRoot, builder.asc(root.get("name")), entityManager);
    }

//    // TODO: gettig all the strategies to obtain the lock--- is it a good idea?
//    public ControlStrategy obtainLocked(User owner, ControlStrategy element, EntityManager entityManager) {
//        return (ControlStrategy) lockingScheme.getLocked(owner, current(element, entityManager), entityManager);
//    }
//
    public ControlStrategy obtainLocked(User owner, int id, EntityManager entityManager) {
        return (ControlStrategy) lockingScheme.getLocked(owner, current(id, entityManager), entityManager);
    }

//    public void releaseLocked(ControlStrategy locked, EntityManager entityManager) {
//        ControlStrategy current = current(locked, entityManager);
//        String runStatus = current.getRunStatus();
//        if (runStatus == null || !runStatus.equalsIgnoreCase("Running"))
//            lockingScheme.releaseLock(current, entityManager);
//    }

    public void releaseLocked(User user, int id, EntityManager entityManager) {
        ControlStrategy current = getById(id, entityManager);
        String runStatus = current.getRunStatus();
        if (runStatus == null || !runStatus.equalsIgnoreCase("Running"))
            lockingScheme.releaseLock(user, current, entityManager);
    }

    public ControlStrategy update(ControlStrategy locked, EntityManager entityManager) throws EmfException {
        return (ControlStrategy) lockingScheme.releaseLockOnUpdate(locked, current(locked, entityManager), entityManager);
    }

    public void updateWithoutLock(ControlStrategy controlStrategy, EntityManager entityManager) throws EmfException {
        hibernateFacade.saveOrUpdate(controlStrategy, entityManager);
    }
    
    public void updateWithoutLock(ControlStrategyResult controlStrategyResult, EntityManager entityManager) throws EmfException {
        hibernateFacade.saveOrUpdate(controlStrategyResult, entityManager);
    }
    
    public ControlStrategy updateWithLock(ControlStrategy locked, EntityManager entityManager) throws EmfException {
        return (ControlStrategy) lockingScheme.renewLockOnUpdate(locked, current(locked, entityManager), entityManager);
    }

    private ControlStrategy current(ControlStrategy strategy, EntityManager entityManager) {
        return current(strategy.getId(), entityManager);
    }

    public boolean canUpdate(ControlStrategy controlStrategy, EntityManager entityManager) {
        if (!exists(controlStrategy.getId(), entityManager)) {
            return false;
        }

        ControlStrategy current = current(controlStrategy.getId(), entityManager);

        entityManager.clear();// clear to flush current

        if (current.getName().equals(controlStrategy.getName()))
            return true;

        return !nameUsed(controlStrategy.getName(), entityManager);
    }

    public boolean nameUsed(String name, EntityManager entityManager) {
        return hibernateFacade.nameUsed(name, ControlStrategy.class, entityManager);
    }

    private ControlStrategy current(int id, EntityManager entityManager) {
        return hibernateFacade.current(id, ControlStrategy.class, entityManager);
    }

    public boolean exists(int id, EntityManager entityManager) {
        return hibernateFacade.exists(id, ControlStrategy.class, entityManager);
    }

    public void remove(ControlStrategy strategy, EntityManager entityManager) {
        if (strategy.getConstraint() != null) hibernateFacade.remove(strategy.getConstraint(), entityManager);
        hibernateFacade.remove(strategy, entityManager);
    }

    public void remove(ControlStrategyResult result, EntityManager entityManager) {
        hibernateFacade.remove(result, entityManager);
    }

    public StrategyResultType getDetailedStrategyResultType(EntityManager entityManager) {
        return getStrategyResultType(StrategyResultType.detailedStrategyResult, entityManager);
    }

    public StrategyResultType getStrategyResultType(String name, EntityManager entityManager) {
        return hibernateFacade.load(StrategyResultType.class, "name", name, entityManager);
    }

    public StrategyResultType[] getOptionalStrategyResultTypes(EntityManager entityManager) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<StrategyResultType> criteriaQuery = builder.createQuery(StrategyResultType.class);
        Root<StrategyResultType> root = criteriaQuery.from(StrategyResultType.class);

        criteriaQuery.select(root);
        criteriaQuery.where(builder.equal(root.get("optional"), true));

        return entityManager.createQuery(criteriaQuery).getResultList().toArray(new StrategyResultType[0]);
    }

    public StrategyResultType getSummaryStrategyResultType(EntityManager entityManager) {
        return getStrategyResultType(StrategyResultType.strategyMeasureSummary, entityManager);
    }

    public ControlStrategyResult getControlStrategyResult(int controlStrategyId, int inputDatasetId, 
            int detailedResultDatasetId, EntityManager entityManager) {
        CriteriaBuilderQueryRoot<ControlStrategyResult> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(ControlStrategyResult.class, entityManager);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<ControlStrategyResult> root = criteriaBuilderQueryRoot.getRoot();
        Join<EmfDataset, ControlStrategyResult> drdJoin = root.join("detailedResultDataset", javax.persistence.criteria.JoinType.INNER);

        Predicate critControlStrategyId = builder.equal(root.get("controlStrategyId"), controlStrategyId);
        Predicate critInputDatasetId = builder.equal(root.get("inputDatasetId"), inputDatasetId);
        Predicate critDetailedResultDatasetId = builder.equal(drdJoin.get("id"), detailedResultDatasetId);
        return hibernateFacade.load(entityManager, criteriaBuilderQueryRoot, new Predicate[] {critControlStrategyId, critInputDatasetId, critDetailedResultDatasetId});
    }

    public ControlStrategyResult getControlStrategyResult(int id, EntityManager entityManager) {
        return hibernateFacade.load(ControlStrategyResult.class, "id", Integer.valueOf(id), entityManager);
    }

//    private void updateControlStrategyIds(ControlStrategy controlStrategy, EntityManager entityManager) {
//        Criterion c1 = Restrictions.eq("name", controlStrategy.getName());
//        List list = hibernateFacade.get(ControlStrategy.class, c1, entityManager);
//        if (!list.isEmpty()) {
//            ControlStrategy cs = (ControlStrategy) list.get(0);
//            controlStrategy.setId(cs.getId());
//        }
//    }
//
    public void updateControlStrategyResult(ControlStrategyResult result, EntityManager entityManager) {
        hibernateFacade.saveOrUpdate(result, entityManager);
    }

    public String controlStrategyRunStatus(int id, EntityManager entityManager) {
        ControlStrategy controlStrategy = hibernateFacade.current(id, ControlStrategy.class, entityManager);
        return controlStrategy.getRunStatus();
    }

//    public void removeControlStrategyResult(ControlStrategy controlStrategy, EntityManager entityManager) {
//        Criterion c = Restrictions.eq("controlStrategyId", Integer.valueOf(controlStrategy.getId()));
//        List list = hibernateFacade.get(ControlStrategyResult.class, c, entityManager);
//        for (int i = 0; i < list.size(); i++) {
//            ControlStrategyResult result = (ControlStrategyResult) list.get(i);
//            hibernateFacade.delete(result,entityManager);
//        }
//    }

    public void removeControlStrategyResults(int controlStrategyId, EntityManager entityManager) {
        String hqlDelete = "delete ControlStrategyResult sr where sr.controlStrategyId = :controlStrategyId";
        try {
            hibernateFacade.executeInsideTransaction(em -> {
                em
                    .createQuery( hqlDelete )
                    .setParameter("controlStrategyId", Integer.valueOf(controlStrategyId))
                    .executeUpdate();
                }, entityManager);
        } catch (RuntimeException e) {
            throw e;
        }
    }

    public void removeControlStrategyResult(int controlStrategyId, int resultId, EntityManager entityManager) {
        String hqlDelete = "delete ControlStrategyResult sr where sr.id = :resultId and sr.controlStrategyId = :controlStrategyId";
        try {
            hibernateFacade.executeInsideTransaction(em -> {
                em
                    .createQuery( hqlDelete )
                    .setParameter("resultId", Integer.valueOf(resultId))
                    .setParameter("controlStrategyId", Integer.valueOf(controlStrategyId))
                    .executeUpdate();
                }, entityManager);
        } catch (RuntimeException e) {
            throw e;
        }
    }

    public ControlStrategy getByName(String name, EntityManager entityManager) {
        return hibernateFacade.load(ControlStrategy.class, "name", name, entityManager);
    }

    public ControlStrategy getById(int id, EntityManager entityManager) {
        return hibernateFacade.load(ControlStrategy.class, "id", Integer.valueOf(id), entityManager);
    }

    public List<ControlStrategyResult> getControlStrategyResults(int controlStrategyId, EntityManager entityManager) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<ControlStrategyResult> criteriaQuery = builder.createQuery(ControlStrategyResult.class);
        Root<ControlStrategyResult> root = criteriaQuery.from(ControlStrategyResult.class);

        criteriaQuery.select(root);

        criteriaQuery.orderBy(builder.desc(root.get("startTime")));

        criteriaQuery.where(builder.equal(root.get("controlStrategyId"), controlStrategyId));

        return entityManager.createQuery(criteriaQuery).getResultList();
    }
    
    public void removeResultDatasets(EmfDataset[] datasets, User user, EntityManager entityManager, DbServer dbServer) throws EmfException {
        if (datasets != null) {
            try {
                deleteDatasets(datasets, user, entityManager);
                datasetDao.deleteDatasets(datasets, dbServer, entityManager);
            } catch (EmfException e) {
                if (DebugLevels.DEBUG_12())
                    System.out.println(e.getMessage());
                
                throw new EmfException(e.getMessage());
            }
        }
    }
    
    public void deleteDatasets(EmfDataset[] datasets, User user, EntityManager entityManager) throws EmfException {
        EmfDataset[] lockedDatasets = getLockedDatasets(datasets, user, entityManager);
        
        if (lockedDatasets == null)
            return;
        
        try {
            new DataServiceImpl(dbServerFactory, entityManagerFactory).deleteDatasets(user, lockedDatasets, DeleteType.CONTROL_STRATEGY);
        } catch (EmfException e) {
//            releaseLocked(lockedDatasets, user, entityManager);
//            throw new EmfException(e.getMessage());
            if (!e.getType().equals(EmfException.MSG_TYPE))
                throw new EmfException(e.getMessage());
        } finally {
            releaseLocked(lockedDatasets, user, entityManager);
        }
    }
    
    private EmfDataset[] getLockedDatasets(EmfDataset[] datasets, User user, EntityManager entityManager) {
        List<EmfDataset> lockedList = new ArrayList<EmfDataset>();
        
        for (int i = 0; i < datasets.length; i++) {
            EmfDataset locked = obtainLockedDataset(datasets[i], user, entityManager);
            if (locked == null) {
                releaseLocked(lockedList.toArray(new EmfDataset[0]), user, entityManager);
                return null;
            }
            
            lockedList.add(locked);
        }
        
        return lockedList.toArray(new EmfDataset[0]);
    }

    private EmfDataset obtainLockedDataset(EmfDataset dataset, User user, EntityManager entityManager) {
        EmfDataset locked = datasetDao.obtainLocked(user, dataset, entityManager);
        return locked;
    }
    
    private void releaseLocked(EmfDataset[] lockedDatasets, User user, EntityManager entityManager) {
        if (lockedDatasets.length == 0)
            return;
        
        for(int i = 0; i < lockedDatasets.length; i++)
            datasetDao.releaseLocked(user, lockedDatasets[i], entityManager);
    }
//    public void removeResultDatasets(Integer[] ids, User user, EntityManager entityManager, DbServer dbServer) throws EmfException {
//        DatasetDAO dsDao = new DatasetDAO();
//        for (Integer id : ids ) {
//            EmfDataset dataset = dsDao.getDataset(entityManager, id);
//
//            if (dataset != null) {
//                try {
//                    dsDao.remove(user, dataset, entityManager);
//                    purgeDeletedDatasets(dataset, entityManager, dbServer);
//                    entityManager.flush();
//                    entityManager.clear();
//                } catch (EmfException e) {
//                    if (DebugLevels.DEBUG_12())
//                        System.out.println(e.getMessage());
//                    
//                    throw new EmfException(e.getMessage());
//                }
//            }
//        }
//    }
    
//    private void purgeDeletedDatasets(EmfDataset dataset, EntityManager entityManager, DbServer dbServer) throws EmfException {
//        try {
//            DatasetDAO dao = new DatasetDAO();
//            dao.deleteDatasets(new EmfDataset[] {dataset}, dbServer, entityManager);
//        } catch (Exception e) {
//            throw new EmfException(e.getMessage());
//        } finally {
//            //
//        }
//    }

    public Integer[] getResultDatasetIds(int controlStrategyId, EntityManager entityManager) {
        List<ControlStrategyResult> results = getControlStrategyResults(controlStrategyId, entityManager);
        List<Integer> datasetLists = new ArrayList<Integer>();
        if(results != null){
            for (int i=0; i<results.size(); i++){
                if (results.get(i).getStrategyResultType().getName().equals(StrategyResultType.detailedStrategyResult)) {
                    if (results.get(i).getDetailedResultDataset() != null)
                        datasetLists.add(results.get(i).getDetailedResultDataset().getId());
                    if (results.get(i).getControlledInventoryDataset() != null)
                        datasetLists.add( results.get(i).getControlledInventoryDataset().getId());
                } else {
                    datasetLists.add( results.get(i).getDetailedResultDataset().getId());
                }
            }
        }
        if (datasetLists.size()>0)
            return datasetLists.toArray(new Integer[0]);
        return null; 
    }

    
    public EmfDataset[] getResultDatasets(int controlStrategyId, EntityManager entityManager) {
        List<ControlStrategyResult> results = getControlStrategyResults(controlStrategyId, entityManager);
        List<EmfDataset> datasets = new ArrayList<EmfDataset>();
        if(results != null){
            for (int i=0; i<results.size(); i++){
                if (results.get(i).getDetailedResultDataset() != null)
                    datasets.add((EmfDataset)results.get(i).getDetailedResultDataset());
                if (results.get(i).getControlledInventoryDataset() != null)
                    datasets.add((EmfDataset)results.get(i).getControlledInventoryDataset());
            }
        }
        if (datasets.size()>0)
            return datasets.toArray(new EmfDataset[0]);
        return null; 
    }

    public EmfDataset[] getResultDatasets(int controlStrategyId, int resultId, EntityManager entityManager) {
        ControlStrategyResult result = getControlStrategyResult(resultId, entityManager);
        List<EmfDataset> datasets = new ArrayList<EmfDataset>();
        if(result != null){
            if (result.getDetailedResultDataset() != null)
                datasets.add((EmfDataset)result.getDetailedResultDataset());
            if (result.getControlledInventoryDataset() != null)
                datasets.add((EmfDataset)result.getControlledInventoryDataset());
        }
        if (datasets.size()>0)
            return datasets.toArray(new EmfDataset[0]);
        return null; 
    }

    public void setControlStrategyRunStatus(int id, String runStatus, Date completionDate, EntityManager entityManager) {
        // NOTE Auto-generated method stub
        
    }

    public String getDefaultExportDirectory(EntityManager entityManager) {
        EmfProperty tmpDir = new EmfPropertiesDAO().getProperty("ImportExportTempDir", entityManager);
        String dir = "";
        if (tmpDir != null)
            dir = tmpDir.getValue();
        return dir;
    }

    public String getStrategyRunStatus(EntityManager entityManager, int id) {
        return (String)entityManager.createQuery("select cS.runStatus " +
                "from ControlStrategy cS where cS.id = " + id).getSingleResult();
    }

    public List<ControlStrategy> getControlStrategiesByControlMeasures(int[] cmIds, EntityManager entityManager) {
        List<ControlStrategy> list = new ArrayList<ControlStrategy>();
        String idList = "";
        for (int i = 0; i < cmIds.length; ++i) {
            idList += (i > 0 ? ","  : "") + cmIds[i];
        }
        try {
            TypedQuery<ControlStrategy> query = entityManager.createQuery("select distinct cs "
                    + "FROM ControlStrategy AS cs "
                    + (cmIds != null && cmIds.length > 0 
                            ? "inner join cs.controlMeasures AS csm inner join csm.controlMeasure AS cm "
                               + "WHERE cm.id in (" + idList + ") " 
                            : "")
                    + "order by cs.name", ControlStrategy.class);
//            Query query = entityManager.createQuery("select new ControlStrategy(cs.id, cs.name, cs.controlMeasures) "
//                    + "FROM ControlStrategy AS cs "
//                    + (cmIds != null && cmIds.length > 0 
//                            ? "inner join cs.controlMeasures AS csm inner join csm.controlMeasure AS cm "
//                               + "WHERE cm.id in (" + idList + ") " 
//                            : "")
//                    + "order by cs.name");
            query.setHint("org.hibernate.cacheable", Boolean.TRUE);
            list = query.getResultList();
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        return list;
    }
    
    public void finalizeControlStrategy(int controlStrategyId, String msg, EntityManager entityManager, int[] measureIdsToDelete) throws EmfException {
        try {
            hibernateFacade.executeInsideTransaction(em -> {
                em
                    .createQuery("update ControlStrategy set isFinal = :isFinal, description =  '' || "
                            + "description || '\n------\n' || :msg, lastModifiedDate = :date where id = :id")
                    .setParameter("isFinal", true)
                    .setParameter("msg", msg)
                    .setParameter("date", new Date())
                    .setParameter("id", Integer.valueOf(controlStrategyId))
                    .executeUpdate();
                }, entityManager);

            entityManager.clear();
            
            //also need to purge measures that are being deleted...this is needed to keep hibernate list_index in synch...
            ControlStrategy cs = getById(controlStrategyId, entityManager);
            List<ControlStrategyMeasure> measures = new ArrayList<ControlStrategyMeasure>();
            measures.addAll(Arrays.asList(cs.getControlMeasures()));
            for (ControlStrategyMeasure m : cs.getControlMeasures()) {
                for (int id : measureIdsToDelete) {
                    if (id == m.getControlMeasure().getId()) {
                        measures.remove(m);
                    }
                }
            }
            cs.setControlMeasures(measures.toArray(new ControlStrategyMeasure[0]));
            updateWithLock(cs, entityManager);
            
        } catch (HibernateException e) {
            throw e;
        } catch (EmfException e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
            throw e;
        }
    }

    public List<StrategyGroup> getAllStrategyGroups(EntityManager entityManager) {
        CriteriaBuilderQueryRoot<StrategyGroup> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(StrategyGroup.class, entityManager);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<StrategyGroup> root = criteriaBuilderQueryRoot.getRoot();

        return hibernateFacade.getAll(criteriaBuilderQueryRoot, builder.asc(root.get("name")), entityManager);
    }

    public StrategyGroup getGroupById(int id, EntityManager entityManager) {
        StrategyGroup group = hibernateFacade.load(StrategyGroup.class, "id", Integer.valueOf(id), entityManager);
        return group;
    }

    public StrategyGroup getGroupByName(String name, EntityManager entityManager) {
        StrategyGroup group = hibernateFacade.load(StrategyGroup.class, "name", new String(name), entityManager);
        return group;
    }

    public StrategyGroup obtainLockedGroup(User owner, int id, EntityManager entityManager) {
        return (StrategyGroup) lockingScheme.getLocked(owner, currentGroup(id, entityManager), entityManager);
    }

    public void releaseLockedGroup(User user, int id, EntityManager entityManager) {
        StrategyGroup current = getGroupById(id, entityManager);
        lockingScheme.releaseLock(user, current, entityManager);
    }

    public int addGroup(StrategyGroup group, EntityManager entityManager) {
        hibernateFacade.add(group, entityManager);
        return group.getId();
    }

    public boolean canUpdateGroup(StrategyGroup strategyGroup, EntityManager entityManager) {
        if (!exists(strategyGroup.getId(), entityManager)) {
            return false;
        }

        StrategyGroup current = currentGroup(strategyGroup.getId(), entityManager);

        entityManager.clear();// clear to flush current

        if (current.getName().equals(strategyGroup.getName()))
            return true;

        return !nameUsed(strategyGroup.getName(), entityManager);
    }

    private StrategyGroup currentGroup(int id, EntityManager entityManager) {
        return hibernateFacade.current(id, StrategyGroup.class, entityManager);
    }
    
    public StrategyGroup updateGroupWithLock(StrategyGroup locked, EntityManager entityManager) throws EmfException {
        return (StrategyGroup) lockingScheme.renewLockOnUpdate(locked, currentGroup(locked, entityManager), entityManager);
    }

    private StrategyGroup currentGroup(StrategyGroup strategyGroup, EntityManager entityManager) {
        return currentGroup(strategyGroup.getId(), entityManager);
    }
    
    public void removeGroup(StrategyGroup strategyGroup, EntityManager entityManager) {
        hibernateFacade.remove(strategyGroup, entityManager);
    }

}
