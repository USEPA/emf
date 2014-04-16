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
import gov.epa.emissions.framework.services.data.DatasetDAO;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.DataServiceImpl.DeleteType;
import gov.epa.emissions.framework.services.persistence.EmfPropertiesDAO;
import gov.epa.emissions.framework.services.persistence.HibernateFacade;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;
import gov.epa.emissions.framework.services.persistence.LockingScheme;
import gov.epa.emissions.framework.tasks.DebugLevels;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;


public class SectorScenarioDAO {
    private LockingScheme lockingScheme;

    private HibernateFacade hibernateFacade;

    private HibernateSessionFactory sessionFactory;

    private DbServerFactory dbServerFactory;
    
    private DatasetDAO datasetDao;

    public SectorScenarioDAO() {
        lockingScheme = new LockingScheme();
        hibernateFacade = new HibernateFacade();
        this.datasetDao = new DatasetDAO();
    }

    public SectorScenarioDAO(DbServerFactory dbServerFactory, HibernateSessionFactory sessionFactory) {
        this();
        this.dbServerFactory = dbServerFactory;
        this.sessionFactory = sessionFactory;
    }

    public int add(SectorScenario element, Session session) {
        return addObject(element, session);
    }

    public int add(SectorScenarioOutput element, Session session) {
        return addObject(element, session);
    }

    private int addObject(Object obj, Session session) {
        return (Integer)hibernateFacade.add(obj, session);
    }

    public String getSectorScenarioRunStatus(int sectorScenarioId, Session session) {
        return (String)session.createQuery("select cS.runStatus from SectorScenario cS where cS.id = " + sectorScenarioId).uniqueResult();
    }

    public Long getSectorScenarioRunningCount(Session session) {
        return (Long)session.createQuery("select count(*) as total from SectorScenario cS where cS.runStatus = 'Running'").uniqueResult();
    }

    public List<SectorScenario> getSectorScenariosByRunStatus(String runStatus, Session session) {
//        Criterion critRunStatus = Restrictions.eq("runStatus", runStatus);
//        return hibernateFacade.get(SectorScenario.class, critRunStatus, Order.asc("lastModifiedDate"), session);
//
        return session.createQuery("select new SectorScenario(cS.id, cS.name) from SectorScenario cS where cS.runStatus = :runStatus order by cS.lastModifiedDate").setString("runStatus", runStatus).list();
    }

    public void setSectorScenarioRunStatusAndCompletionDate(int sectorScenarioId, String runStatus, Date completionDate, Session session) {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.createQuery("update SectorScenario set runStatus = :status, lastModifiedDate = :date, completionDate = :completionDate where id = :id")
            .setString("status", runStatus)
            .setTimestamp("date", new Date())
            .setTimestamp("completionDate", completionDate)
            .setInteger("id", sectorScenarioId)
            .executeUpdate();
            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }

    // return SectorScenarios orderby name
    public List all(Session session) {
        
        return session.createQuery("select new SectorScenario(sS.id, sS.name, " +
                "sS.abbreviation, sS.runStatus, sS.creator, " +
                "sS.lastModifiedDate, " +
                "sS.startDate, sS.completionDate) " +
                "from SectorScenario as sS " +
                "order by sS.name").list();
        //return hibernateFacade.getAll(SectorScenario.class, Order.asc("name"), session);
    }
//    // return SectorScenarios orderby name
//    public List test(Session session) {
//        //check if dataset is a input inventory for some strategy (via the StrategyInputDataset table)
//        List list = session.createQuery("select cS.name from SectorScenario as cS inner join cS.sectorScenarioInputDatasets as iDs inner join iDs.inputDataset as iD with iD.id = 1221").list();
//        //check if dataset is a input inventory for some strategy (via the StrategyResult table, could be here for historical reasons)
//        list = session.createQuery("select cS.name from SectorScenarioResult sR, SectorScenario cS where sR.sectorScenarioId = cS.id and sR.inputDataset.id = 1221").list();
//        //check if dataset is a detailed result dataset for some strategy
//        list = session.createQuery("select cS.name from SectorScenarioResult sR, SectorScenario cS where sR.sectorScenarioId = cS.id and sR.detailedResultDataset.id = 1221").list();
//        //check if dataset is a controlled inventory for some strategy
//        list = session.createQuery("select cS.name from SectorScenarioResult sR, SectorScenario cS where sR.sectorScenarioId = cS.id and sR.controlledInventoryDataset.id = 1221").list();
//        //check if dataset is used as a region/county dataset for specific strategy measures
//        list = session.createQuery("select cS.name from SectorScenario as cS inner join cS.controlMeasures as cM inner join cM.regionDataset as rD with rD.id = 1221").list();
//        //check if dataset is used as a region/county dataset for specific strategy
//        list = session.createQuery("select cS.name from SectorScenario cS where cS.countyDataset.id = 1221").list();
//
//        return list;
//    }

    public List getAllStrategyTypes(Session session) {
        return hibernateFacade.getAll(SectorScenarioOutputType.class, Order.asc("name"), session);
    }

//    // TODO: gettig all the strategies to obtain the lock--- is it a good idea?
//    public SectorScenario obtainLocked(User owner, SectorScenario element, Session session) {
//        return (SectorScenario) lockingScheme.getLocked(owner, current(element, session), session);
//    }
//
    public SectorScenario obtainLocked(User owner, int id, Session session) {
        return (SectorScenario) lockingScheme.getLocked(owner, current(id, SectorScenario.class, session), session);
    }

//    public void releaseLocked(SectorScenario locked, Session session) {
//        SectorScenario current = current(locked, session);
//        String runStatus = current.getRunStatus();
//        if (runStatus == null || !runStatus.equalsIgnoreCase("Running"))
//            lockingScheme.releaseLock(current, session);
//    }

    public void releaseLocked(User user, int id, Session session) {
        SectorScenario current = getById(id, session);
        String runStatus = current.getRunStatus();
        if (runStatus == null || !runStatus.equalsIgnoreCase("Running"))
            lockingScheme.releaseLock(user, current, session);
    }

    public SectorScenario update(SectorScenario locked, Session session) throws EmfException {
        return (SectorScenario) lockingScheme.releaseLockOnUpdate(locked, current(locked, session), session);
    }

    public SectorScenario updateWithLock(SectorScenario locked, Session session) throws EmfException {
        return (SectorScenario) lockingScheme.renewLockOnUpdate(locked, current(locked, session), session);
    }

    private SectorScenario current(SectorScenario strategy, Session session) {
        return current(strategy.getId(), SectorScenario.class, session);
    }

    public boolean canUpdate(SectorScenario sectorScenario, Session session) {
        if (!exists(sectorScenario.getId(), SectorScenario.class, session)) {
            return false;
        }

        SectorScenario current = current(sectorScenario.getId(), SectorScenario.class, session);

        session.clear();// clear to flush current

        if (current.getName().equals(sectorScenario.getName()))
            return true;

        return !nameUsed(sectorScenario.getName(), SectorScenario.class, session);
    }

    public boolean nameUsed(String name, Class clazz, Session session) {
        return hibernateFacade.nameUsed(name, clazz, session);
    }

    private SectorScenario current(int id, Class clazz, Session session) {
        return (SectorScenario) hibernateFacade.current(id, clazz, session);
    }

    public boolean exists(int id, Class clazz, Session session) {
        return hibernateFacade.exists(id, clazz, session);
    }

    public void remove(SectorScenario strategy, Session session) {
        hibernateFacade.remove(strategy, session);
    }

    public void remove(SectorScenarioOutput result, Session session) {
        hibernateFacade.remove(result, session);
    }

    public SectorScenarioOutputType getSectorScenarioOutputType(String name, Session session) {
        Criterion critName = Restrictions.eq("name", name);
        return (SectorScenarioOutputType)hibernateFacade.load(SectorScenarioOutputType.class, critName, session);
    }

    public SectorScenarioOutput getSectorScenarioOutput(int sectorScenarioId, int inputDatasetId, 
            int detailedResultDatasetId, Session session) {
        Criterion critSectorScenarioId = Restrictions.eq("sectorScenarioId", sectorScenarioId);
        Criterion critInputDatasetId = Restrictions.eq("inputDatasetId", inputDatasetId);
        Criterion critDetailedResultDatasetId = Restrictions.eq("detailedResultDataset.id", detailedResultDatasetId);
        return (SectorScenarioOutput)hibernateFacade.load(SectorScenarioOutput.class, new Criterion[] {critSectorScenarioId, critInputDatasetId, critDetailedResultDatasetId}, 
                session);
    }

    public SectorScenarioOutput getSectorScenarioOutput(int id, Session session) {
        Criterion critId = Restrictions.eq("id", id);
        return (SectorScenarioOutput)hibernateFacade.load(SectorScenarioOutput.class, new Criterion[] {critId}, 
                session);
    }

//    private void updateSectorScenarioIds(SectorScenario sectorScenario, Session session) {
//        Criterion c1 = Restrictions.eq("name", sectorScenario.getName());
//        List list = hibernateFacade.get(SectorScenario.class, c1, session);
//        if (!list.isEmpty()) {
//            SectorScenario cs = (SectorScenario) list.get(0);
//            sectorScenario.setId(cs.getId());
//        }
//    }
//
    public void updateSectorScenarioOutput(SectorScenarioOutput result, Session session) {
        hibernateFacade.saveOrUpdate(result, session);
    }

    public String sectorScenarioRunStatus(int id, Session session) {
        SectorScenario sectorScenario = (SectorScenario) hibernateFacade.current(id, SectorScenario.class, session);
        return sectorScenario.getRunStatus();
    }

//    public void removeSectorScenarioResult(SectorScenario sectorScenario, Session session) {
//        Criterion c = Restrictions.eq("sectorScenarioId", new Integer(sectorScenario.getId()));
//        List list = hibernateFacade.get(SectorScenarioResult.class, c, session);
//        for (int i = 0; i < list.size(); i++) {
//            SectorScenarioResult result = (SectorScenarioResult) list.get(i);
//            hibernateFacade.delete(result,session);
//        }
//    }

    public void removeSectorScenarioResults(int sectorScenarioId, User user, Session session, DbServer dbServer) throws EmfException {

        try {
            List<EmfDataset> dsList = new ArrayList<EmfDataset>();
            //first get the datasets to delete
            EmfDataset[] datasets = getOutputDatasets(sectorScenarioId, session);
            if (datasets != null) {
                for (EmfDataset dataset : datasets) {
                    if (!user.isAdmin() && !dataset.getCreator().equalsIgnoreCase(user.getUsername())) {
                        new StatusDAO(sessionFactory).add(new Status(user.getUsername(), "SectorScenario", "The sector scenario output dataset, " + dataset.getName() + ", will not be deleted since you are not the creator.", new Date()));
                    } else {
                        dsList.add(dataset);
                    }
                }
            }

//            String hqlDelete = "delete SectorScenarioOutput sr where sr.sectorScenarioId = :sectorScenarioId";
//            session.createQuery( hqlDelete )
//                 .setInteger("sectorScenarioId", sectorScenarioId)
//                 .executeUpdate();
//            session.clear();
//            session.flush();

            hibernateFacade.remove(getSectorScenarioOutputs(sectorScenarioId, session).toArray(new SectorScenarioOutput[0]), session);
            session.clear();
            session.flush();
//delete and purge datasets
            if (dsList != null && dsList.size() > 0){
                removeResultDatasets(dsList.toArray(new EmfDataset[0]), user, session, dbServer);
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw new EmfException("Could not remove sector scenario outputs.", e);
//        } catch (EmfException e) {
//            e.printStackTrace();
//            throw new EmfException("Could not remove sector scenario outputs.", e);
        } finally {
//            session.close();
        }
 
    }
    
    public void checkIfUsed( int [] datasetIDs, User user, DbServer dbServer, Session session) throws EmfException {
        String msgs = "";
        try {
            this.datasetDao.checkIfUsedByCases(datasetIDs, session);
        } catch ( Exception e) {
            msgs += e.getMessage() + "\n";
        }
        try {
            this.datasetDao.checkIfUsedByControlPrograms(datasetIDs, session);
        } catch ( Exception e) {
            msgs += e.getMessage() + "\n";
        }
        try {
            this.datasetDao.checkIfUsedByStrategies(datasetIDs, session);
        } catch ( Exception e) {
            msgs += e.getMessage() + "\n";
        }
        try {
            List<Integer> notUsed = this.datasetDao.notUsedByFast(datasetIDs, user, dbServer, session);
            if ( notUsed != null && notUsed.size() != datasetIDs.length) {
                msgs += "Some output datasets are used by Fast.\n";
            }
        } catch ( Exception e) {
            msgs += e.getMessage() + "\n";
        }
//        try {
//            List<Integer> notUsed = this.datasetDao.notUsedBySectorScnarios(datasetIDs, user, session);
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
    
    public void removeSectorScenarioResultsV2(int sectorScenarioId, User user, Session session, DbServer dbServer) throws EmfException {

        try {
            List<EmfDataset> dsList = new ArrayList<EmfDataset>();
            EmfDataset[] datasets = getOutputDatasets(sectorScenarioId, session);
            if (datasets != null) {
                List<String> msgList = new ArrayList<String>();
                for (EmfDataset dataset : datasets) {
                    if (!user.isAdmin() && !dataset.getCreator().equalsIgnoreCase(user.getUsername())) {
                        String msg = "The sector scenario output dataset, " + dataset.getName() + ", will not be deleted since you are not the creator.";
                        msgList.add( msg);
                        new StatusDAO(sessionFactory).add(new Status(user.getUsername(), "SectorScenario", msg, new Date()));
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
//            session.createQuery( hqlDelete )
//                 .setInteger("sectorScenarioId", sectorScenarioId)
//                 .executeUpdate();
//            session.clear();
//            session.flush();

            hibernateFacade.remove(getSectorScenarioOutputs(sectorScenarioId, session).toArray(new SectorScenarioOutput[0]), session);
            session.clear();
            session.flush();
//delete and purge datasets
            if (dsList != null && dsList.size() > 0){
                removeResultDatasets(dsList.toArray(new EmfDataset[0]), user, session, dbServer);
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw new EmfException("Could not remove sector scenario outputs - ", e);
//        } catch (EmfException e) {
//            e.printStackTrace();
//            throw new EmfException("Could not remove sector scenario outputs.", e);
        } finally {
//            session.close();
        }
 
    }    

    public SectorScenario getByName(String name, Session session) {
        SectorScenario cs = (SectorScenario) hibernateFacade.load(SectorScenario.class, Restrictions.eq("name", new String(name)), session);
        return cs;
    }
    
    public SectorScenario getByAbbre(String abbre, Session session) {
        SectorScenario cs = (SectorScenario) hibernateFacade.load(SectorScenario.class, Restrictions.eq("abbreviation", new String(abbre)), session);
        return cs;
    }

    public SectorScenario getById(int id, Session session) {
        SectorScenario cs = (SectorScenario) hibernateFacade.load(SectorScenario.class, Restrictions.eq("id", new Integer(id)), session);
        return cs;
    }

    public List<SectorScenarioOutput> getSectorScenarioOutputs(int sectorScenarioId, Session session) {
        return session.createCriteria(SectorScenarioOutput.class).add(Restrictions.eq("sectorScenarioId", sectorScenarioId)).addOrder(Order.desc("startDate")).list();
    }
    
    public void removeResultDatasets(EmfDataset[] datasets, User user, Session session, DbServer dbServer) throws EmfException {
        if (datasets != null) {
            try {
                deleteDatasets(datasets, user, session);
                datasetDao.deleteDatasets(datasets, dbServer, session);
            } catch (EmfException e) {
                if (DebugLevels.DEBUG_12())
                    System.out.println(e.getMessage());
                
                throw new EmfException(e.getMessage());
            }
        }
    }
    
    public void deleteDatasets(EmfDataset[] datasets, User user, Session session) throws EmfException {
        EmfDataset[] lockedDatasets = getLockedDatasets(datasets, user, session);
        
        if (lockedDatasets == null)
            return;
        
        try {
            new DataServiceImpl(dbServerFactory, sessionFactory).deleteDatasets(user, lockedDatasets, DeleteType.SECTOR_SCENARIO);
        } catch (EmfException e) {
            if (!e.getType().equals(EmfException.MSG_TYPE))
                throw new EmfException(e.getMessage());
        } finally {
            releaseLocked(lockedDatasets, user, session);
        }
    }
    
    private EmfDataset[] getLockedDatasets(EmfDataset[] datasets, User user, Session session) {
        List lockedList = new ArrayList();
        
        for (int i = 0; i < datasets.length; i++) {
            EmfDataset locked = obtainLockedDataset(datasets[i], user, session);
            if (locked == null) {
                releaseLocked((EmfDataset[])lockedList.toArray(new EmfDataset[0]), user, session);
                return null;
            }
            
            lockedList.add(locked);
        }
        
        return (EmfDataset[])lockedList.toArray(new EmfDataset[0]);
    }

    private EmfDataset obtainLockedDataset(EmfDataset dataset, User user, Session session) {
        EmfDataset locked = datasetDao.obtainLocked(user, dataset, session);
        return locked;
    }
    
    private void releaseLocked(EmfDataset[] lockedDatasets, User user, Session session) {
        if (lockedDatasets.length == 0)
            return;
        
        for(int i = 0; i < lockedDatasets.length; i++)
            datasetDao.releaseLocked(user, lockedDatasets[i], session);
    }
//    public void removeResultDatasets(Integer[] ids, User user, Session session, DbServer dbServer) throws EmfException {
//        DatasetDAO dsDao = new DatasetDAO();
//        for (Integer id : ids ) {
//            EmfDataset dataset = dsDao.getDataset(session, id);
//
//            if (dataset != null) {
//                try {
//                    dsDao.remove(user, dataset, session);
//                    purgeDeletedDatasets(dataset, session, dbServer);
//                    session.flush();
//                    session.clear();
//                } catch (EmfException e) {
//                    if (DebugLevels.DEBUG_12())
//                        System.out.println(e.getMessage());
//                    
//                    throw new EmfException(e.getMessage());
//                }
//            }
//        }
//    }
    
//    private void purgeDeletedDatasets(EmfDataset dataset, Session session, DbServer dbServer) throws EmfException {
//        try {
//            DatasetDAO dao = new DatasetDAO();
//            dao.deleteDatasets(new EmfDataset[] {dataset}, dbServer, session);
//        } catch (Exception e) {
//            throw new EmfException(e.getMessage());
//        } finally {
//            //
//        }
//    }

    public Integer[] getResultDatasetIds(int sectorScenarioId, Session session) {
        List<SectorScenarioOutput> results = getSectorScenarioOutputs(sectorScenarioId, session);
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

    
    public EmfDataset[] getOutputDatasets(int sectorScenarioId, Session session) {
        List<SectorScenarioOutput> results = getSectorScenarioOutputs(sectorScenarioId, session);
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

    public void setSectorScenarioRunStatus(int id, String runStatus, Date completionDate, Session session) {
        // NOTE Auto-generated method stub
        
    }

    public String getDefaultExportDirectory(Session session) {
        EmfProperty tmpDir = new EmfPropertiesDAO().getProperty("ImportExportTempDir", session);
        String dir = "";
        if (tmpDir != null)
            dir = tmpDir.getValue();
        return dir;
    }

    public String getStrategyRunStatus(Session session, int id) {
        return (String)session.createQuery("select cS.runStatus " +
                "from SectorScenario cS where cS.id = " + id).uniqueResult();
    }
    
    public String[] getDistinctSectorListFromDataset(Session session, DbServer dbServer, int datasetId, int versionNumber) throws EmfException {
        List<String> sectors = new ArrayList<String>();
        try {
            EmfDataset dataset = new DatasetDAO().getDataset(session, datasetId);
            Versions versions = new Versions();
            Version version = versions.get(datasetId, versionNumber, session);
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
