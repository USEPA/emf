package gov.epa.emissions.framework.services.sms;

import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.db.version.Versions;
import gov.epa.emissions.commons.io.VersionedQuery;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.EmfProperty;
import gov.epa.emissions.framework.services.basic.Status;
import gov.epa.emissions.framework.services.basic.StatusDAO;
import gov.epa.emissions.framework.services.data.DataServiceImpl;
import gov.epa.emissions.framework.services.data.DataServiceImpl.DeleteType;
import gov.epa.emissions.framework.services.data.DatasetDAO;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.persistence.EmfPropertiesDAO;
import gov.epa.emissions.framework.services.persistence.HibernateFacade;
import gov.epa.emissions.framework.services.persistence.HibernateFacade.CriteriaBuilderQueryRoot;
import gov.epa.emissions.framework.services.persistence.LockingScheme;
import gov.epa.emissions.framework.tasks.DebugLevels;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

public class SectorScenarioDAO {
    private LockingScheme lockingScheme;

    private HibernateFacade hibernateFacade;

    private EntityManagerFactory entityManagerFactory;

    private DbServerFactory dbServerFactory;
    
    private DatasetDAO datasetDao;

    public SectorScenarioDAO() {
        lockingScheme = new LockingScheme();
        hibernateFacade = new HibernateFacade();
        this.datasetDao = new DatasetDAO();
    }

    public SectorScenarioDAO(DbServerFactory dbServerFactory, EntityManagerFactory entityManagerFactory) {
        this();
        this.dbServerFactory = dbServerFactory;
        this.entityManagerFactory = entityManagerFactory;
    }

    public int add(SectorScenario element, EntityManager entityManager) {
        return addObject(element, entityManager);
    }

    public int add(SectorScenarioOutput element, EntityManager entityManager) {
        return addObject(element, entityManager);
    }

    private int addObject(Object obj, EntityManager entityManager) {
        return (Integer)hibernateFacade.add(obj, entityManager);
    }

    public String getSectorScenarioRunStatus(int sectorScenarioId, EntityManager entityManager) {
        return (String)entityManager.createQuery("select cS.runStatus from SectorScenario cS where cS.id = " + sectorScenarioId).getSingleResult();
    }

    public Long getSectorScenarioRunningCount(EntityManager entityManager) {
        return (Long)entityManager.createQuery("select count(*) as total from SectorScenario cS where cS.runStatus = 'Running'").getSingleResult();
    }

    public List<SectorScenario> getSectorScenariosByRunStatus(String runStatus, EntityManager entityManager) {
        return entityManager.createQuery("select new SectorScenario(cS.id, cS.name) from SectorScenario cS where cS.runStatus = :runStatus order by cS.lastModifiedDate").setParameter("runStatus", runStatus).getResultList();
    }

    public void setSectorScenarioRunStatusAndCompletionDate(int sectorScenarioId, String runStatus, Date completionDate, EntityManager entityManager) {
        hibernateFacade.executeInsideTransaction(em -> {
            em.createQuery("update SectorScenario set runStatus = :status, lastModifiedDate = :date, completionDate = :completionDate where id = :id")
                .setParameter("status", runStatus)
                .setParameter("date", new Date())
                .setParameter("completionDate", completionDate)
                .setParameter("id", Integer.valueOf(sectorScenarioId))
                .executeUpdate();
        }, entityManager);
    }

    // return SectorScenarios orderby name
    public List all(EntityManager entityManager) {
        
        return entityManager.createQuery("select new SectorScenario(sS.id, sS.name, " +
                "sS.abbreviation, sS.runStatus, sS.creator, " +
                "sS.lastModifiedDate, " +
                "sS.startDate, sS.completionDate) " +
                "from SectorScenario as sS " +
                "order by sS.name").getResultList();
    }

    public List<SectorScenarioOutputType> getAllStrategyTypes(EntityManager entityManager) {
        CriteriaBuilderQueryRoot<SectorScenarioOutputType> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(SectorScenarioOutputType.class, entityManager);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<SectorScenarioOutputType> root = criteriaBuilderQueryRoot.getRoot();

        return hibernateFacade.getAll(criteriaBuilderQueryRoot, builder.asc(root.get("name")), entityManager);
    }

//    // TODO: gettig all the strategies to obtain the lock--- is it a good idea?
//    public SectorScenario obtainLocked(User owner, SectorScenario element, EntityManager entityManager) {
//        return (SectorScenario) lockingScheme.getLocked(owner, current(element, entityManager), entityManager);
//    }
//
    public SectorScenario obtainLocked(User owner, int id, EntityManager entityManager) {
        return (SectorScenario) lockingScheme.getLocked(owner, current(id, entityManager), entityManager);
    }

//    public void releaseLocked(SectorScenario locked, EntityManager entityManager) {
//        SectorScenario current = current(locked, entityManager);
//        String runStatus = current.getRunStatus();
//        if (runStatus == null || !runStatus.equalsIgnoreCase("Running"))
//            lockingScheme.releaseLock(current, entityManager);
//    }

    public void releaseLocked(User user, int id, EntityManager entityManager) {
        SectorScenario current = getById(id, entityManager);
        String runStatus = current.getRunStatus();
        if (runStatus == null || !runStatus.equalsIgnoreCase("Running"))
            lockingScheme.releaseLock(user, current, entityManager);
    }

    public SectorScenario update(SectorScenario locked, EntityManager entityManager) throws EmfException {
        return (SectorScenario) lockingScheme.releaseLockOnUpdate(locked, current(locked, entityManager), entityManager);
    }

    public SectorScenario updateWithLock(SectorScenario locked, EntityManager entityManager) throws EmfException {
        return (SectorScenario) lockingScheme.renewLockOnUpdate(locked, current(locked, entityManager), entityManager);
    }

    private SectorScenario current(SectorScenario strategy, EntityManager entityManager) {
        return current(strategy.getId(), entityManager);
    }

    public boolean canUpdate(SectorScenario sectorScenario, EntityManager entityManager) {
        if (!exists(sectorScenario.getId(), entityManager)) {
            return false;
        }

        SectorScenario current = current(sectorScenario.getId(), entityManager);

        entityManager.clear();// clear to flush current

        if (current.getName().equals(sectorScenario.getName()))
            return true;

        return !nameUsed(sectorScenario.getName(), entityManager);
    }

    public boolean nameUsed(String name, EntityManager entityManager) {
        return hibernateFacade.nameUsed(name, SectorScenario.class, entityManager);
    }

    private SectorScenario current(int id, EntityManager entityManager) {
        return hibernateFacade.current(id, SectorScenario.class, entityManager);
    }

    public boolean exists(int id, EntityManager entityManager) {
        return hibernateFacade.exists(id, SectorScenario.class, entityManager);
    }

    public void remove(SectorScenario strategy, EntityManager entityManager) {
        hibernateFacade.remove(strategy, entityManager);
    }

    public void remove(SectorScenarioOutput result, EntityManager entityManager) {
        hibernateFacade.remove(result, entityManager);
    }

    public SectorScenarioOutputType getSectorScenarioOutputType(String name, EntityManager entityManager) {
        return hibernateFacade.load(SectorScenarioOutputType.class, "name", name, entityManager);
    }

    public SectorScenarioOutput getSectorScenarioOutput(int sectorScenarioId, int inputDatasetId, 
            int detailedResultDatasetId, EntityManager entityManager) {
        CriteriaBuilderQueryRoot<SectorScenarioOutput> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(SectorScenarioOutput.class, entityManager);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<SectorScenarioOutput> root = criteriaBuilderQueryRoot.getRoot();
        Join<EmfDataset, SectorScenarioOutput> odJoin = root.join("outputDataset", javax.persistence.criteria.JoinType.INNER);

        Predicate critSectorScenarioId = builder.equal(root.get("sectorScenarioId"), sectorScenarioId);
        Predicate critInputDatasetId = builder.equal(root.get("inputDatasetId"), inputDatasetId);
        Predicate critDetailedResultDatasetId = builder.equal(odJoin.get("id"), detailedResultDatasetId);
        return hibernateFacade.load(entityManager, criteriaBuilderQueryRoot, new Predicate[] {critSectorScenarioId, critInputDatasetId, critDetailedResultDatasetId});
    }

    public SectorScenarioOutput getSectorScenarioOutput(int id, EntityManager entityManager) {
        return hibernateFacade.load(SectorScenarioOutput.class, "id", Integer.valueOf(id), entityManager);
    }

//    private void updateSectorScenarioIds(SectorScenario sectorScenario, EntityManager entityManager) {
//        Criterion c1 = Restrictions.eq("name", sectorScenario.getName());
//        List list = hibernateFacade.get(SectorScenario.class, c1, entityManager);
//        if (!list.isEmpty()) {
//            SectorScenario cs = (SectorScenario) list.get(0);
//            sectorScenario.setId(cs.getId());
//        }
//    }
//
    public void updateSectorScenarioOutput(SectorScenarioOutput result, EntityManager entityManager) {
        hibernateFacade.saveOrUpdate(result, entityManager);
    }

    public String sectorScenarioRunStatus(int id, EntityManager entityManager) {
        SectorScenario sectorScenario = hibernateFacade.current(id, SectorScenario.class, entityManager);
        return sectorScenario.getRunStatus();
    }

//    public void removeSectorScenarioResult(SectorScenario sectorScenario, EntityManager entityManager) {
//        Criterion c = Restrictions.eq("sectorScenarioId", Integer.valueOf(sectorScenario.getId()));
//        List list = hibernateFacade.get(SectorScenarioResult.class, c, entityManager);
//        for (int i = 0; i < list.size(); i++) {
//            SectorScenarioResult result = (SectorScenarioResult) list.get(i);
//            hibernateFacade.delete(result,entityManager);
//        }
//    }

    public void removeSectorScenarioResults(int sectorScenarioId, User user, EntityManager entityManager, DbServer dbServer) throws EmfException {

        try {
            List<EmfDataset> dsList = new ArrayList<EmfDataset>();
            //first get the datasets to delete
            EmfDataset[] datasets = getOutputDatasets(sectorScenarioId, entityManager);
            if (datasets != null) {
                for (EmfDataset dataset : datasets) {
                    if (!user.isAdmin() && !dataset.getCreator().equalsIgnoreCase(user.getUsername())) {
                        new StatusDAO(entityManagerFactory).add(new Status(user.getUsername(), "SectorScenario", "The sector scenario output dataset, " + dataset.getName() + ", will not be deleted since you are not the creator.", new Date()));
                    } else {
                        dsList.add(dataset);
                    }
                }
            }

//            String hqlDelete = "delete SectorScenarioOutput sr where sr.sectorScenarioId = :sectorScenarioId";
//            entityManager.createQuery( hqlDelete )
//                 .setInteger("sectorScenarioId", sectorScenarioId)
//                 .executeUpdate();
//            entityManager.clear();
//            entityManager.flush();

            hibernateFacade.remove(getSectorScenarioOutputs(sectorScenarioId, entityManager).toArray(new SectorScenarioOutput[0]), entityManager);
            entityManager.clear();
//            entityManager.flush();
//delete and purge datasets
            if (dsList != null && dsList.size() > 0){
                removeResultDatasets(dsList.toArray(new EmfDataset[0]), user, entityManager, dbServer);
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw new EmfException("Could not remove sector scenario outputs.", e);
//        } catch (EmfException e) {
//            e.printStackTrace();
//            throw new EmfException("Could not remove sector scenario outputs.", e);
        } finally {
//            entityManager.close();
        }
 
    }
    
    public void checkIfUsed( int [] datasetIDs, User user, DbServer dbServer, EntityManager entityManager) throws EmfException {
        String msgs = "";
        try {
            this.datasetDao.checkIfUsedByCases(datasetIDs, entityManager);
        } catch ( Exception e) {
            msgs += e.getMessage() + "\n";
        }
        try {
            this.datasetDao.checkIfUsedByControlPrograms(datasetIDs, entityManager);
        } catch ( Exception e) {
            msgs += e.getMessage() + "\n";
        }
        try {
            this.datasetDao.checkIfUsedByStrategies(datasetIDs, entityManager);
        } catch ( Exception e) {
            msgs += e.getMessage() + "\n";
        }
        try {
            List<Integer> notUsed = this.datasetDao.notUsedByFast(datasetIDs, user, dbServer, entityManager);
            if ( notUsed != null && notUsed.size() != datasetIDs.length) {
                msgs += "Some output datasets are used by Fast.\n";
            }
        } catch ( Exception e) {
            msgs += e.getMessage() + "\n";
        }
//        try {
//            List<Integer> notUsed = this.datasetDao.notUsedBySectorScnarios(datasetIDs, user, entityManager);
//            if ( notUsed != null && notUsed.size() != datasetIDs.length) {
//                msgs += "Some output datasets are used by SectorScenarios.\n";
//            }
//        } catch ( Exception e) {
//            msgs += e.getMessage() + "\n";
//        }
        if ( msgs != "") {
            throw new EmfException( msgs);
        }
    }
    
    public void removeSectorScenarioResultsV2(int sectorScenarioId, User user, EntityManager entityManager, DbServer dbServer) throws EmfException {

        try {
            List<EmfDataset> dsList = new ArrayList<EmfDataset>();
            EmfDataset[] datasets = getOutputDatasets(sectorScenarioId, entityManager);
            if (datasets != null) {
                List<String> msgList = new ArrayList<String>();
                for (EmfDataset dataset : datasets) {
                    if (!user.isAdmin() && !dataset.getCreator().equalsIgnoreCase(user.getUsername())) {
                        String msg = "The sector scenario output dataset, " + dataset.getName() + ", will not be deleted since you are not the creator.";
                        msgList.add( msg);
                        new StatusDAO(entityManagerFactory).add(new Status(user.getUsername(), "SectorScenario", msg, new Date()));
                    } else {
                        dsList.add(dataset);
                    }
                }
                if ( msgList.size()>0) {
                    String msgs = "";                
                    for ( int i=0; i<msgList.size(); i++) {
                        msgs += msgList.get(i) + "\n";
                    }
                    throw new EmfException( msgs);
                }
            }

//            String hqlDelete = "delete SectorScenarioOutput sr where sr.sectorScenarioId = :sectorScenarioId";
//            entityManager.createQuery( hqlDelete )
//                 .setInteger("sectorScenarioId", sectorScenarioId)
//                 .executeUpdate();
//            entityManager.clear();
//            entityManager.flush();

            hibernateFacade.remove(getSectorScenarioOutputs(sectorScenarioId, entityManager).toArray(new SectorScenarioOutput[0]), entityManager);
            entityManager.clear();
            entityManager.flush();
//delete and purge datasets
            if (dsList != null && dsList.size() > 0){
                removeResultDatasets(dsList.toArray(new EmfDataset[0]), user, entityManager, dbServer);
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw new EmfException("Could not remove sector scenario outputs - ", e);
//        } catch (EmfException e) {
//            e.printStackTrace();
//            throw new EmfException("Could not remove sector scenario outputs.", e);
        } finally {
//            entityManager.close();
        }
 
    }    

    public SectorScenario getByName(String name, EntityManager entityManager) {
        SectorScenario cs = hibernateFacade.load(SectorScenario.class, "name", new String(name), entityManager);
        return cs;
    }
    
    public SectorScenario getByAbbre(String abbre, EntityManager entityManager) {
        SectorScenario cs = hibernateFacade.load(SectorScenario.class, "abbreviation", new String(abbre), entityManager);
        return cs;
    }

    public SectorScenario getById(int id, EntityManager entityManager) {
        SectorScenario cs = hibernateFacade.load(SectorScenario.class, "id", Integer.valueOf(id), entityManager);
        return cs;
    }

    public List<SectorScenarioOutput> getSectorScenarioOutputs(int sectorScenarioId, EntityManager entityManager) {
        CriteriaBuilderQueryRoot<SectorScenarioOutput> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(SectorScenarioOutput.class, entityManager);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<SectorScenarioOutput> root = criteriaBuilderQueryRoot.getRoot();

        return hibernateFacade
                .get(criteriaBuilderQueryRoot, new Predicate[] { builder.equal(root.get("sectorScenarioId"), Integer.valueOf(sectorScenarioId)) }, builder.desc(root.get("startDate")), entityManager);
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
            new DataServiceImpl(dbServerFactory, entityManagerFactory).deleteDatasets(user, lockedDatasets, DeleteType.SECTOR_SCENARIO);
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

    public Integer[] getResultDatasetIds(int sectorScenarioId, EntityManager entityManager) {
        List<SectorScenarioOutput> results = getSectorScenarioOutputs(sectorScenarioId, entityManager);
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

    
    public EmfDataset[] getOutputDatasets(int sectorScenarioId, EntityManager entityManager) {
        List<SectorScenarioOutput> results = getSectorScenarioOutputs(sectorScenarioId, entityManager);
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

    public void setSectorScenarioRunStatus(int id, String runStatus, Date completionDate, EntityManager entityManager) {
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
                "from SectorScenario cS where cS.id = " + id).getSingleResult();
    }
    
    public String[] getDistinctSectorListFromDataset(EntityManager entityManager, DbServer dbServer, int datasetId, int versionNumber) throws EmfException {
        List<String> sectors = new ArrayList<String>();
        try {
            EmfDataset dataset = new DatasetDAO().getDataset(entityManager, datasetId);
            Versions versions = new Versions();
            Version version = versions.get(datasetId, versionNumber, entityManager);
            VersionedQuery versionedQuery = new VersionedQuery(version);
            String inventoryTableName = dataset.getInternalSources()[0].getTable();
            ResultSet rs = dbServer.getEmfDatasource().query().executeQuery("select distinct sector from emissions." + inventoryTableName + " where " + versionedQuery.query() + " and sector is not null order by sector");
            while (rs.next()) {
                sectors.add(rs.getString(1));
            }

        } catch (RuntimeException e) {
            throw new EmfException("Could not retrieve distinct sector list. Ex=" + e.getMessage());
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        } finally {
            //
        }
        return sectors.toArray(new String[0]);
    }

}
