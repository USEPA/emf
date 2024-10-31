package gov.epa.emissions.framework.services.fast;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.db.version.Versions;
import gov.epa.emissions.commons.io.VersionedQuery;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.EmfProperty;
import gov.epa.emissions.framework.services.cost.controlStrategy.FileFormatFactory;
import gov.epa.emissions.framework.services.data.DataCommonsDAO;
import gov.epa.emissions.framework.services.data.DataCommonsServiceImpl;
import gov.epa.emissions.framework.services.data.DataServiceImpl;
import gov.epa.emissions.framework.services.data.DataServiceImpl.DeleteType;
import gov.epa.emissions.framework.services.data.DatasetDAO;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.Keywords;
import gov.epa.emissions.framework.services.persistence.EmfPropertiesDAO;
import gov.epa.emissions.framework.services.persistence.HibernateFacade;
import gov.epa.emissions.framework.services.persistence.HibernateFacade.CriteriaBuilderQueryRoot;
import gov.epa.emissions.framework.services.persistence.LockingScheme;
import gov.epa.emissions.framework.tasks.DebugLevels;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.hibernate.HibernateException;

public class FastDAO {
    private LockingScheme lockingScheme;

    private HibernateFacade hibernateFacade;

    private EntityManagerFactory entityManagerFactory;

    private DbServerFactory dbServerFactory;
    
    private DatasetDAO datasetDao;

    private DataCommonsDAO dataCommonsDao;

    public FastDAO() {
        lockingScheme = new LockingScheme();
        hibernateFacade = new HibernateFacade();
        this.datasetDao = new DatasetDAO();
        this.dataCommonsDao = new DataCommonsDAO();
    }

    public FastDAO(DbServerFactory dbServerFactory, EntityManagerFactory entityManagerFactory) {
        this();
        this.dbServerFactory = dbServerFactory;
        this.entityManagerFactory = entityManagerFactory;
    }

    public int add(FastRun element, EntityManager entityManager) {
        hibernateFacade.add(element, entityManager);
        return element.getId();
    }

    public int add(FastRunOutput element, EntityManager entityManager) {
        hibernateFacade.add(element, entityManager);
        return element.getId();
    }

    public int add(FastAnalysis element, EntityManager entityManager) {
        hibernateFacade.add(element, entityManager);
        return element.getId();
    }

    public int add(FastAnalysisOutput element, EntityManager entityManager) {
        hibernateFacade.add(element, entityManager);
        return element.getId();
    }

    public String getFastRunRunStatus(int fastRunId, EntityManager entityManager) {
        return (String)entityManager.createQuery("select cS.runStatus from FastRun cS where cS.id = " + fastRunId).getSingleResult();
    }

    public Long getFastRunRunningCount(EntityManager entityManager) {
        Long count = (Long)entityManager.createQuery("select count(*) as total from FastRun cS where cS.runStatus = 'Running'").getSingleResult();
        return count != null ? count : 0L;
    }

    public List<FastRun> getFastRunsByRunStatus(String runStatus, EntityManager entityManager) {
//        Criterion critRunStatus = Restrictions.eq("runStatus", runStatus);
//        return hibernateFacade.get(FastRun.class, critRunStatus, Order.asc("lastModifiedDate"), entityManager);
//
        return entityManager
                .createQuery("select new FastRun(cS.id, cS.name) from FastRun cS where cS.runStatus = :runStatus order by cS.lastModifiedDate")
                .setParameter("runStatus", runStatus)
                .getResultList();
    }

    public void setFastRunRunStatusAndCompletionDate(int fastRunId, String runStatus, Date completionDate, EntityManager entityManager) {
        try {
            hibernateFacade.executeInsideTransaction(em -> {
                em
                    .createQuery("update FastRun set runStatus = :status, lastModifiedDate = :date, completionDate = :completionDate where id = :id")
                    .setParameter("status", runStatus)
                    .setParameter("date", new Date())
                    .setParameter("completionDate", completionDate)
                    .setParameter("id", Integer.valueOf(fastRunId))
                    .executeUpdate();
            }, entityManager);
        } catch (HibernateException e) {
            throw e;
        }
    }

    // return FastRuns orderby name
    public List<FastRun> getFastRuns(EntityManager entityManager) {
        CriteriaBuilderQueryRoot<FastRun> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(FastRun.class, entityManager);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<FastRun> root = criteriaBuilderQueryRoot.getRoot();

        return hibernateFacade.getAll(criteriaBuilderQueryRoot, builder.asc(root.get("name")), entityManager);
    }

    // return FastRuns by Grid and orderby name
    public List<FastRun> getFastRuns(int gridId, EntityManager entityManager) {
        CriteriaBuilderQueryRoot<FastRun> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(FastRun.class, entityManager);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<FastRun> root = criteriaBuilderQueryRoot.getRoot();

        return hibernateFacade.get(criteriaBuilderQueryRoot, builder.equal(root.get("grid.id"), Integer.valueOf(gridId)), builder.asc(root.get("name")), entityManager);
    }

    public List<FastRunOutputType> getFastRunOutputTypes(EntityManager entityManager) {
        CriteriaBuilderQueryRoot<FastRunOutputType> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(FastRunOutputType.class, entityManager);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<FastRunOutputType> root = criteriaBuilderQueryRoot.getRoot();

        return hibernateFacade.getAll(criteriaBuilderQueryRoot, builder.asc(root.get("name")), entityManager);
    }

//    // TODO: gettig all the strategies to obtain the lock--- is it a good idea?
//    public FastRun obtainLocked(User owner, FastRun element, EntityManager entityManager) {
//        return (FastRun) lockingScheme.getLocked(owner, current(element, entityManager), entityManager);
//    }
//
    public FastRun obtainLockedFastRun(User owner, int id, EntityManager entityManager) {
        return (FastRun) lockingScheme.getLocked(owner, getFastRun(id, entityManager), entityManager);
    }

//    public void releaseLocked(FastRun locked, EntityManager entityManager) {
//        FastRun current = current(locked, entityManager);
//        String runStatus = current.getRunStatus();
//        if (runStatus == null || !runStatus.equalsIgnoreCase("Running"))
//            lockingScheme.releaseLock(current, entityManager);
//    }

    public void releaseLockedFastRun(User user, int id, EntityManager entityManager) {
        FastRun current = getFastRun(id, entityManager);
        String runStatus = current.getRunStatus();
        if (runStatus == null || !runStatus.equalsIgnoreCase("Running"))
            lockingScheme.releaseLock(user, current, entityManager);
    }

    public FastRun updateFastRun(FastRun locked, EntityManager entityManager) throws EmfException {
        return (FastRun) lockingScheme.releaseLockOnUpdate(locked, getFastRun(locked.getId(), entityManager), entityManager);
    }

    public FastRun updateFastRunWithLock(FastRun locked, EntityManager entityManager) throws EmfException {
        return (FastRun) lockingScheme.renewLockOnUpdate(locked, getFastRun(locked.getId(), entityManager), entityManager);
    }

    public boolean canUpdateFastRun(FastRun fastRun, EntityManager entityManager) {
        if (!exists(fastRun.getId(), FastRun.class, entityManager)) {
            return false;
        }

        FastRun current = getFastRun(fastRun.getId(), entityManager);

        entityManager.clear();// clear to flush current

        if (current.getName().equals(fastRun.getName()))
            return true;

        return !nameUsed(fastRun.getName(), FastRun.class, entityManager);
    }

    public <C> boolean nameUsed(String name, Class<C> clazz, EntityManager entityManager) {
        return hibernateFacade.nameUsed(name, clazz, entityManager);
    }

//    private FastRun current(int id, Class clazz, EntityManager entityManager) {
//        return (FastRun) hibernateFacade.current(id, clazz, entityManager);
//    }

    public boolean exists(int id, Class clazz, EntityManager entityManager) {
        return hibernateFacade.exists(id, clazz, entityManager);
    }

    public void remove(FastRun strategy, EntityManager entityManager) {
        hibernateFacade.remove(strategy, entityManager);
    }

    public void remove(FastRunOutput result, EntityManager entityManager) {
        hibernateFacade.remove(result, entityManager);
    }

    public FastRunOutputType getFastRunOutputType(String name, EntityManager entityManager) {
        return hibernateFacade.load(FastRunOutputType.class, "name", name, entityManager);
    }

    public FastRunOutput getFastRunOutput(int fastRunId, int inputDatasetId, 
            int detailedResultDatasetId, EntityManager entityManager) {
        CriteriaBuilderQueryRoot<FastRunOutput> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(FastRunOutput.class, entityManager);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<FastRunOutput> root = criteriaBuilderQueryRoot.getRoot();
        Join<EmfDataset, FastRunOutput> odJoin = root.join("outputDataset", javax.persistence.criteria.JoinType.INNER);

        Predicate critFastRunId = builder.equal(root.get("fastRunId"), fastRunId);
        Predicate critInputDatasetId = builder.equal(root.get("inputDatasetId"), inputDatasetId);
        Predicate critDetailedResultDatasetId = builder.equal(odJoin.get("id"), detailedResultDatasetId);
        return hibernateFacade.load(entityManager, criteriaBuilderQueryRoot, new Predicate[] {critFastRunId, critInputDatasetId, critDetailedResultDatasetId});
    }

    public FastRunOutput getFastRunOutput(int id, EntityManager entityManager) {
        return hibernateFacade.load(FastRunOutput.class, "id", Integer.valueOf(id), entityManager);
    }

//    private void updateFastRunIds(FastRun fastRun, EntityManager entityManager) {
//        Criterion c1 = Restrictions.eq("name", fastRun.getName());
//        List list = hibernateFacade.get(FastRun.class, c1, entityManager);
//        if (!list.isEmpty()) {
//            FastRun cs = (FastRun) list.get(0);
//            fastRun.setId(cs.getId());
//        }
//    }
//
    public void updateFastRunOutput(FastRunOutput result, EntityManager entityManager) {
        hibernateFacade.saveOrUpdate(result, entityManager);
    }

    public String fastRunRunStatus(int id, EntityManager entityManager) {
        FastRun fastRun = hibernateFacade.current(id, FastRun.class, entityManager);
        return fastRun.getRunStatus();
    }

//    public void removeFastRunResult(FastRun fastRun, EntityManager entityManager) {
//        Criterion c = Restrictions.eq("fastRunId", Integer.valueOf(fastRun.getId()));
//        List list = hibernateFacade.get(FastRunResult.class, c, entityManager);
//        for (int i = 0; i < list.size(); i++) {
//            FastRunResult result = (FastRunResult) list.get(i);
//            hibernateFacade.delete(result,entityManager);
//        }
//    }

    public void removeFastRunResults(int fastRunId, EntityManager entityManager) {
//        String hqlDelete = "delete FastRunOutput sr where sr.fastRunId = :fastRunId";
//        entityManager.createQuery( hqlDelete )
//             .setInteger("fastRunId", fastRunId)
//             .executeUpdate();
//        entityManager.flush();
        List<FastRunOutput> outputs = hibernateFacade.get(FastRunOutput.class, "fastRunId", Integer.valueOf(fastRunId), entityManager);
        hibernateFacade.remove(outputs.toArray(), entityManager);
    }

    public FastRun getFastRun(String name, EntityManager entityManager) {
        FastRun cs = hibernateFacade.load(FastRun.class, "name", new String(name), entityManager);
        return cs;
    }

    public FastRun getFastRun(int id, EntityManager entityManager) {
        FastRun cs = hibernateFacade.load(FastRun.class, "id", Integer.valueOf(id), entityManager);
        return cs;
    }

    public List<FastRunOutput> getFastRunOutputs(int fastRunId, EntityManager entityManager) {
        CriteriaBuilderQueryRoot<FastRunOutput> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(FastRunOutput.class, entityManager);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<FastRunOutput> root = criteriaBuilderQueryRoot.getRoot();

        return hibernateFacade.get(criteriaBuilderQueryRoot, builder.equal(root.get("fastRunId"), Integer.valueOf(fastRunId)), builder.asc(root.get("startDate")), entityManager);
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
            new DataServiceImpl(dbServerFactory, entityManagerFactory).deleteDatasets(user, datasets, DeleteType.FAST);
        } catch (EmfException e) {
            if (!e.getType().equals(EmfException.MSG_TYPE))
                throw new EmfException(e.getMessage());
        } finally {
            releaseLocked(lockedDatasets, user, entityManager);
        }
    }
    
    private EmfDataset[] getLockedDatasets(EmfDataset[] datasets, User user, EntityManager entityManager) {
        List lockedList = new ArrayList();
        
        for (int i = 0; i < datasets.length; i++) {
            EmfDataset locked = obtainLockedDataset(datasets[i], user, entityManager);
            if (locked == null) {
                releaseLocked((EmfDataset[])lockedList.toArray(new EmfDataset[0]), user, entityManager);
                return null;
            }
            
            lockedList.add(locked);
        }
        
        return (EmfDataset[])lockedList.toArray(new EmfDataset[0]);
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

    public Integer[] getResultDatasetIds(int fastRunId, EntityManager entityManager) {
        List<FastRunOutput> results = getFastRunOutputs(fastRunId, entityManager);
        List<Integer> datasetLists = new ArrayList<Integer>();
        if(results != null){
            System.out.println(results.size());
            for (int i=0; i<results.size(); i++){
                datasetLists.add( results.get(i).getOutputDataset().getId());
            }
        }
        if (datasetLists.size()>0)
            return datasetLists.toArray(new Integer[0]);
        return null; 
    }

    
    public EmfDataset[] getOutputDatasets(int fastRunId, EntityManager entityManager) {
        List<FastRunOutput> results = getFastRunOutputs(fastRunId, entityManager);
        List<EmfDataset> datasets = new ArrayList<EmfDataset>();
        if(results != null){
            for (int i=0; i<results.size(); i++){
                if (results.get(i).getOutputDataset() != null)
                    datasets.add(results.get(i).getOutputDataset());
            }
        }
        if (datasets.size()>0)
            return datasets.toArray(new EmfDataset[0]);
        return null; 
    }

    public void setFastRunRunStatus(int id, String runStatus, Date completionDate, EntityManager entityManager) {
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
                "from FastRun cS where cS.id = " + id).getSingleResult();
    }
    
    public List<FastDataset> getFastDatasets(EntityManager entityManager) {
        CriteriaBuilderQueryRoot<FastDataset> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(FastDataset.class, entityManager);
        return hibernateFacade.getAll(criteriaBuilderQueryRoot, entityManager);
    }

    public FastDataset getFastDataset(EntityManager entityManager, int fastDatasetId) {
        return hibernateFacade.load(FastDataset.class, "id", Integer.valueOf(fastDatasetId), entityManager);
    }

    public int addFastDataset(FastDataset fastDataset, EntityManager entityManager) {
        hibernateFacade.add(fastDataset, entityManager);
        return fastDataset.getId();
    }

    public void removeFastDataset(int fastDatasetId, EntityManager entityManager) {
        String hqlDelete = "delete FastDataset fd where fd.id = :fastDatasetId";
        try {
            hibernateFacade.executeInsideTransaction(em -> {
                em
                    .createQuery( hqlDelete )
                    .setParameter("fastDatasetId", fastDatasetId)
                    .executeUpdate();
            }, entityManager);
        } catch (RuntimeException e) {
            throw e;
        }
    }

    public List<FastNonPointDataset> getFastNonPointDatasets(EntityManager entityManager) {
        CriteriaBuilderQueryRoot<FastNonPointDataset> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(FastNonPointDataset.class, entityManager);
        return hibernateFacade.getAll(criteriaBuilderQueryRoot, entityManager);
    }

    public FastNonPointDataset getFastNonPointDataset(EntityManager entityManager, int fastNonPointDatasetId) {
        return hibernateFacade.load(FastNonPointDataset.class, "id", Integer.valueOf(fastNonPointDatasetId), entityManager);
    }

    public int addFastNonPointDataset(FastNonPointDataset fastNonPointDataset, EntityManager entityManager) {
        hibernateFacade.add(fastNonPointDataset, entityManager);
        return fastNonPointDataset.getId();
    }

    public void removeFastNonPointDataset(int fastNonPointDatasetId, EntityManager entityManager) {
        String hqlDelete = "delete FastNonPointDataset fd where fd.id = :fastNonPointDatasetId";
        try {
            hibernateFacade.executeInsideTransaction(em -> {
                em
                    .createQuery( hqlDelete )
                    .setParameter("fastNonPointDatasetId", fastNonPointDatasetId)
                    .executeUpdate();
            }, entityManager);
        } catch (RuntimeException e) {
            throw e;
        }
    }

    public List<Grid> getGrids(EntityManager entityManager) {
        CriteriaBuilderQueryRoot<Grid> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(Grid.class, entityManager);
        return hibernateFacade.getAll(criteriaBuilderQueryRoot, entityManager);
    }

    public Grid getGrid(EntityManager entityManager, String name) {
        return hibernateFacade.load(Grid.class, "name", name, entityManager);
    }

    public Grid getGrid(EntityManager entityManager, int id) {
        return hibernateFacade.load(Grid.class, "id", id, entityManager);
    }







    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    

    public String getFastAnalysisRunStatus(int fastAnalysisId, EntityManager entityManager) {
        return (String)entityManager.createQuery("select cS.runStatus from FastAnalysis cS where cS.id = " + fastAnalysisId).getSingleResult();
    }

    public Long getFastAnalysisRunningCount(EntityManager entityManager) {
        Long count = (Long)entityManager.createQuery("select count(*) as total from FastAnalysis cS where cS.runStatus = 'Running'").getSingleResult();
        return count != null ? count : 0L;
    }

    public List<FastAnalysis> getFastAnalysesByRunStatus(String runStatus, EntityManager entityManager) {
        return entityManager
                .createQuery("select new FastAnalysis(cS.id, cS.name) from FastAnalysis cS where cS.runStatus = :runStatus order by cS.lastModifiedDate", FastAnalysis.class)
                .setParameter("runStatus", runStatus)
                .getResultList();
    }

    public void setFastAnalysisRunStatusAndCompletionDate(int fastAnalysisId, String runStatus, Date completionDate, EntityManager entityManager) {
        try {
            hibernateFacade.executeInsideTransaction(em -> {
                em
                    .createQuery("update FastAnalysis set runStatus = :status, lastModifiedDate = :date, completionDate = :completionDate where id = :id")
                    .setParameter("status", runStatus)
                    .setParameter("date", new Date())
                    .setParameter("completionDate", completionDate)
                    .setParameter("id", Integer.valueOf(fastAnalysisId))
                    .executeUpdate();
            }, entityManager);
        } catch (HibernateException e) {
            throw e;
        }
    }

    // return FastAnalyses orderby name
    public List<FastAnalysis> getFastAnalyses(EntityManager entityManager) {
        CriteriaBuilderQueryRoot<FastAnalysis> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(FastAnalysis.class, entityManager);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<FastAnalysis> root = criteriaBuilderQueryRoot.getRoot();

        return hibernateFacade.getAll(criteriaBuilderQueryRoot, builder.asc(root.get("name")), entityManager);
    }

    public List<FastAnalysisOutputType> getAllFastAnalysisOuputTypes(EntityManager entityManager) {
        CriteriaBuilderQueryRoot<FastAnalysisOutputType> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(FastAnalysisOutputType.class, entityManager);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<FastAnalysisOutputType> root = criteriaBuilderQueryRoot.getRoot();

        return hibernateFacade.getAll(criteriaBuilderQueryRoot, builder.asc(root.get("name")), entityManager);
    }

    public FastAnalysis obtainLockedFastAnalysis(User owner, int id, EntityManager entityManager) {
        return (FastAnalysis) lockingScheme.getLocked(owner, getFastAnalysis(id, entityManager), entityManager);
    }

    public void releaseLockedFastAnalysis(User user, int id, EntityManager entityManager) {
        FastAnalysis current = getFastAnalysis(id, entityManager);
        String runStatus = current.getRunStatus();
        if (runStatus == null || !runStatus.equalsIgnoreCase("Running"))
            lockingScheme.releaseLock(user, current, entityManager);
    }

    public FastAnalysis updateFastAnalysis(FastAnalysis locked, EntityManager entityManager) throws EmfException {
        return (FastAnalysis) lockingScheme.releaseLockOnUpdate(locked, getFastAnalysis(locked.getId(), entityManager), entityManager);
    }

    public FastAnalysis updateWithLock(FastAnalysis locked, EntityManager entityManager) throws EmfException {
        return (FastAnalysis) lockingScheme.renewLockOnUpdate(locked, getFastAnalysis(locked.getId(), entityManager), entityManager);
    }

    public boolean canUpdate(FastAnalysis fastAnalysis, EntityManager entityManager) {
        if (!exists(fastAnalysis.getId(), FastAnalysis.class, entityManager)) {
            return false;
        }

        FastAnalysis current = getFastAnalysis(fastAnalysis.getId(), entityManager);

        entityManager.clear();// clear to flush current

        if (current.getName().equals(fastAnalysis.getName()))
            return true;

        return !nameUsed(fastAnalysis.getName(), FastAnalysis.class, entityManager);
    }

    public void remove(FastAnalysis strategy, EntityManager entityManager) {
        hibernateFacade.remove(strategy, entityManager);
    }

    public void remove(FastAnalysisOutput result, EntityManager entityManager) {
        hibernateFacade.remove(result, entityManager);
    }

    public FastAnalysisOutputType getFastAnalysisOutputType(String name, EntityManager entityManager) {
        return hibernateFacade.load(FastAnalysisOutputType.class, "name", name, entityManager);
    }

    public List<FastAnalysisOutputType> getFastAnalysisOutputTypes(EntityManager entityManager) {
        CriteriaBuilderQueryRoot<FastAnalysisOutputType> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(FastAnalysisOutputType.class, entityManager);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<FastAnalysisOutputType> root = criteriaBuilderQueryRoot.getRoot();

        return hibernateFacade.getAll(criteriaBuilderQueryRoot, builder.asc(root.get("name")), entityManager);
    }

    public FastAnalysisOutput getFastAnalysisOutput(int fastAnalysisId, int inputDatasetId, 
            int detailedResultDatasetId, EntityManager entityManager) {
        CriteriaBuilderQueryRoot<FastAnalysisOutput> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(FastAnalysisOutput.class, entityManager);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<FastAnalysisOutput> root = criteriaBuilderQueryRoot.getRoot();
        Join<EmfDataset, FastAnalysisOutput> odJoin = root.join("outputDataset", javax.persistence.criteria.JoinType.INNER);

        Predicate critFastAnalysisId = builder.equal(root.get("fastAnalysisId"), fastAnalysisId);
        Predicate critInputDatasetId = builder.equal(root.get("inputDatasetId"), inputDatasetId);
        Predicate critDetailedResultDatasetId = builder.equal(odJoin.get("id"), detailedResultDatasetId);
        return hibernateFacade.load(entityManager, criteriaBuilderQueryRoot, new Predicate[] {critFastAnalysisId, critInputDatasetId, critDetailedResultDatasetId});
    }

    public FastAnalysisOutput getFastAnalysisOutput(int id, EntityManager entityManager) {
        return hibernateFacade.load(FastAnalysisOutput.class, "id", Integer.valueOf(id), entityManager);
    }

//    private void updateFastAnalysisIds(FastAnalysis fastAnalysis, EntityManager entityManager) {
//        Criterion c1 = Restrictions.eq("name", fastAnalysis.getName());
//        List list = hibernateFacade.get(FastAnalysis.class, c1, entityManager);
//        if (!list.isEmpty()) {
//            FastAnalysis cs = (FastAnalysis) list.get(0);
//            fastAnalysis.setId(cs.getId());
//        }
//    }
//
    public void updateFastAnalysisOutput(FastAnalysisOutput result, EntityManager entityManager) {
        hibernateFacade.saveOrUpdate(result, entityManager);
    }

    public String fastAnalysisRunStatus(int id, EntityManager entityManager) {
        FastAnalysis fastAnalysis = hibernateFacade.current(id, FastAnalysis.class, entityManager);
        return fastAnalysis.getRunStatus();
    }

//    public void removeFastAnalysisResult(FastAnalysis fastAnalysis, EntityManager entityManager) {
//        Criterion c = Restrictions.eq("fastAnalysisId", Integer.valueOf(fastAnalysis.getId()));
//        List list = hibernateFacade.get(FastAnalysisResult.class, c, entityManager);
//        for (int i = 0; i < list.size(); i++) {
//            FastAnalysisResult result = (FastAnalysisResult) list.get(i);
//            hibernateFacade.delete(result,entityManager);
//        }
//    }

    public void removeFastAnalysisOutputs(int fastAnalysisId, EntityManager entityManager) {
//        String hqlDelete = "delete FastAnalysisOutput sr where sr.fastAnalysisId = :fastAnalysisId";
//        entityManager.createQuery( hqlDelete )
//             .setInteger("fastAnalysisId", fastAnalysisId)
//             .executeUpdate();
//        entityManager.flush();
        
        List<FastAnalysisOutput> outputs = hibernateFacade.get(FastAnalysisOutput.class, "fastAnalysisId", Integer.valueOf(fastAnalysisId), entityManager);
        hibernateFacade.remove(outputs.toArray(), entityManager);
    }

    public FastAnalysis getFastAnalysis(String name, EntityManager entityManager) {
        FastAnalysis cs = hibernateFacade.load(FastAnalysis.class, "name", new String(name), entityManager);
        return cs;
    }

    public FastAnalysis getFastAnalysis(int id, EntityManager entityManager) {
        FastAnalysis cs = hibernateFacade.load(FastAnalysis.class, "id", Integer.valueOf(id), entityManager);
        return cs;
    }

    public List<FastAnalysisOutput> getFastAnalysisOutputs(int fastAnalysisId, EntityManager entityManager) {
        CriteriaBuilderQueryRoot<FastAnalysisOutput> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(FastAnalysisOutput.class, entityManager);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<FastAnalysisOutput> root = criteriaBuilderQueryRoot.getRoot();

        return hibernateFacade.get(criteriaBuilderQueryRoot, builder.equal(root.get("fastAnalysisId"), Integer.valueOf(fastAnalysisId)), builder.asc(root.get("startDate")), entityManager);
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

    public Integer[] getFastAnalysisResultDatasetIds(int fastAnalysisId, EntityManager entityManager) {
        List<FastAnalysisOutput> results = getFastAnalysisOutputs(fastAnalysisId, entityManager);
        List<Integer> datasetLists = new ArrayList<Integer>();
        if(results != null){
            System.out.println(results.size());
            for (int i=0; i<results.size(); i++){
                datasetLists.add( results.get(i).getOutputDataset().getId());
            }
        }
        if (datasetLists.size()>0)
            return datasetLists.toArray(new Integer[0]);
        return null; 
    }

    
    public EmfDataset[] getFastAnalysisOutputDatasets(int fastAnalysisId, EntityManager entityManager) {
        List<FastAnalysisOutput> results = getFastAnalysisOutputs(fastAnalysisId, entityManager);
        List<EmfDataset> datasets = new ArrayList<EmfDataset>();
        if(results != null){
            for (int i=0; i<results.size(); i++){
                if (results.get(i).getOutputDataset() != null)
                    datasets.add(results.get(i).getOutputDataset());
            }
        }
        if (datasets.size()>0)
            return datasets.toArray(new EmfDataset[0]);
        return null; 
    }

    public void setFastAnalysisRunStatus(int id, String runStatus, Date completionDate, EntityManager entityManager) {
        // NOTE Auto-generated method stub
        
    }

    public String getFastAnalysisRunStatus(EntityManager entityManager, int id) {
        return (String)entityManager.createQuery("select cS.runStatus " +
                "from FastAnalysis cS where cS.id = " + id).getSingleResult();
    }

//    private EmfDataset getDataset(String name, EntityManager entityManager) {
//        return datasetDao.getDataset(entityManager, name);
//    }

    private EmfDataset getDataset(int id, EntityManager entityManager) {
        return datasetDao.getDataset(entityManager, id);
    }

    private DatasetType getDatasetType(String name, EntityManager entityManager) {
        return dataCommonsDao.getDatasetType(name, entityManager);
    }

//    private User getUser(String name, EntityManager entityManager) {
//        return new UserDAO().get(name, entityManager);
//    }

    public int addFastNonPointDataset(FastNonPointDataset fastNonPointDataset, User user, 
            EntityManager entityManager, DbServer dbServer) throws EmfException {
        
        EmfDataset dataset = createFastQuasiPointDataset(fastNonPointDataset.getBaseNonPointDataset(), fastNonPointDataset.getName(), user, entityManager, dbServer);
//            getDataset("ptnonipm_xportfrac_cap2005v2_20nov2008_revised_20jan2009_v0", entityManager);

        FastDataset fastDataset = new FastDataset();
        fastDataset.setDataset(dataset);
        fastDataset.setAddedDate(Calendar.getInstance().getTime());
//        fastService.addFastDataset(fastDataset);
        
        fastNonPointDataset.setFastDataset(fastDataset);
//        fastNonPointDataset.setId(fastService.addFastNonPointDataset(fastNonPointDataset));
        fastDataset.setFastNonPointDataset(fastNonPointDataset);
        addFastNonPointDataset(fastNonPointDataset, entityManager);
        return dataset.getId();
    }
    
    private EmfDataset createFastQuasiPointDataset(EmfDataset base, String newInventoryDatasetName, User user, EntityManager entityManager, DbServer dbServer) throws EmfException {
        DatasetType datasetType = getDatasetType(DatasetType.orlPointInventory, entityManager);
        Keywords keywords = new Keywords(new DataCommonsServiceImpl(entityManagerFactory).getKeywords());
        DatasetCreator creator = new DatasetCreator(null, user, 
                entityManagerFactory, dbServerFactory,
                dbServer.getEmissionsDatasource(), keywords);
        if (creator.isDatasetNameUsed(newInventoryDatasetName))
            throw new EmfException("Dataset name is already used, " + newInventoryDatasetName);
        try {
            return creator.addDataset(newInventoryDatasetName, "ds", 
                    base, datasetType, 
                    new FileFormatFactory(dbServer).tableFormat(datasetType), "");
        } catch (Exception e) {
            e.printStackTrace();
            throw new EmfException(e.getMessage());
            // NOTE Auto-generated catch block
        //    e.printStackTrace();
        }
    }

    private Version version(int datasetId, int version) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            Versions versions = new Versions();
            return versions.get(datasetId, version, entityManager);
        } finally {
            entityManager.close();
        }
    }

    public String[] getFastRunSpeciesMappingDatasetPollutants(int datasetId, int datasetVersion) throws EmfException {
        List<String> pollutantsList = new ArrayList<String>();
        ResultSet rs = null;
        Statement statement = null;
        DbServer dbServer = dbServerFactory.getDbServer();
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        Connection con = dbServer.getConnection();

        VersionedQuery versionedQuery = new VersionedQuery(version(datasetId,
                datasetVersion));
        String tableName = getDataset(datasetId, entityManager).getInternalSources()[0].getTable();

        String query = "select distinct cmaq_pollutant from emissions." + tableName + " where "
                + versionedQuery.query() + " order by cmaq_pollutant;";
        try {
            statement = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rs = statement.executeQuery(query);
            while (rs.next()) {
                pollutantsList.add(rs.getString(1));
            }
        } catch (SQLException e) {
            throw new EmfException("Could not execute query -" + query + "\n" + e.getMessage());
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
            if (dbServer != null) {
                try {
                    dbServer.disconnect();
                } catch (Exception e) {
                    // NOTE Auto-generated catch block
                    e.printStackTrace();
                }
                dbServer = null;
            }
            if (entityManager != null) {
                try {
                    entityManager.close();
                } catch (Exception e) {
                    // NOTE Auto-generated catch block
                    e.printStackTrace();
                }
                entityManager = null;
            }
            
        }
        return pollutantsList.toArray(new String[0]);
    }
}
