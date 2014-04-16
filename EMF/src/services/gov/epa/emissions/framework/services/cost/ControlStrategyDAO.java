package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.EmfProperty;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyConstraint;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;
import gov.epa.emissions.framework.services.cost.controlStrategy.StrategyResultType;
import gov.epa.emissions.framework.services.data.DataServiceImpl;
import gov.epa.emissions.framework.services.data.DatasetDAO;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.DataServiceImpl.DeleteType;
import gov.epa.emissions.framework.services.persistence.EmfPropertiesDAO;
import gov.epa.emissions.framework.services.persistence.HibernateFacade;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;
import gov.epa.emissions.framework.services.persistence.LockingScheme;
import gov.epa.emissions.framework.tasks.DebugLevels;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

public class ControlStrategyDAO {
    private LockingScheme lockingScheme;

    private HibernateFacade hibernateFacade;

    private HibernateSessionFactory sessionFactory;

    private DbServerFactory dbServerFactory;
    
    private DatasetDAO datasetDao;

    public ControlStrategyDAO() {
        lockingScheme = new LockingScheme();
        hibernateFacade = new HibernateFacade();
        this.datasetDao = new DatasetDAO();
    }

    public ControlStrategyDAO(DbServerFactory dbServerFactory, HibernateSessionFactory sessionFactory) {
        this();
        this.dbServerFactory = dbServerFactory;
        this.sessionFactory = sessionFactory;
    }

    public int add(ControlStrategy element, Session session) {
        return addObject(element, session);
    }

    public void add(ControlStrategyConstraint element, Session session) {
        addObject(element, session);
    }

    public int add(ControlStrategyResult element, Session session) {
        return addObject(element, session);
    }

    private int addObject(Object obj, Session session) {
        return (Integer)hibernateFacade.add(obj, session);
    }

    public String getControlStrategyRunStatus(int controlStrategyId, Session session) {
        return (String)session.createQuery("select cS.runStatus from ControlStrategy cS where cS.id = " + controlStrategyId).uniqueResult();
    }

    public Long getControlStrategyRunningCount(Session session) {
        return (Long)session.createQuery("select count(*) as total from ControlStrategy cS where cS.runStatus = 'Running'").uniqueResult();
    }

    public List<ControlStrategy> getControlStrategiesByRunStatus(String runStatus, Session session) {
//        Criterion critRunStatus = Restrictions.eq("runStatus", runStatus);
//        return hibernateFacade.get(ControlStrategy.class, critRunStatus, Order.asc("lastModifiedDate"), session);
//
        return session.createQuery("select new ControlStrategy(cS.id, cS.name) from ControlStrategy cS where cS.runStatus = :runStatus order by cS.lastModifiedDate").setString("runStatus", runStatus).list();
    }

    public void setControlStrategyRunStatusAndCompletionDate(int controlStrategyId, String runStatus, Date completionDate, Session session) {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.createQuery("update ControlStrategy set runStatus = :status, lastModifiedDate = :date, completionDate = :completionDate where id = :id")
            .setString("status", runStatus)
            .setTimestamp("date", new Date())
            .setTimestamp("completionDate", completionDate)
            .setInteger("id", controlStrategyId)
            .executeUpdate();
            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }

    // return ControlStrategies orderby name
    public List all(Session session) {

//        "Name", "Last Modified", "Run Status", "Region", 
//        "Target Pollutant", "Total Cost", "Reduction", 
//        "Project", "Strategy Type", "Cost Year", 
//        "Inv. Year", "Creator"
//        element.getName(), format(element.getLastModifiedDate()), element.getRunStatus(), region(element),
//        element.getTargetPollutant(), getTotalCost(element.getId()), getReduction(element.getId()), 
//        project(element), analysisType(element), costYear(element), 
//        "" + (element.getInventoryYear() != 0 ? element.getInventoryYear() : ""), 
//        element.getCreator().getName()
        return session.createQuery("select new ControlStrategy(cS.id, cS.name, " +
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
                "order by cS.name").list();
        //return hibernateFacade.getAll(ControlStrategy.class, Order.asc("name"), session);
    }
//    // return ControlStrategies orderby name
//    public List test(Session session) {
//        //check if dataset is a input inventory for some strategy (via the StrategyInputDataset table)
//        List list = session.createQuery("select cS.name from ControlStrategy as cS inner join cS.controlStrategyInputDatasets as iDs inner join iDs.inputDataset as iD with iD.id = 1221").list();
//        //check if dataset is a input inventory for some strategy (via the StrategyResult table, could be here for historical reasons)
//        list = session.createQuery("select cS.name from ControlStrategyResult sR, ControlStrategy cS where sR.controlStrategyId = cS.id and sR.inputDataset.id = 1221").list();
//        //check if dataset is a detailed result dataset for some strategy
//        list = session.createQuery("select cS.name from ControlStrategyResult sR, ControlStrategy cS where sR.controlStrategyId = cS.id and sR.detailedResultDataset.id = 1221").list();
//        //check if dataset is a controlled inventory for some strategy
//        list = session.createQuery("select cS.name from ControlStrategyResult sR, ControlStrategy cS where sR.controlStrategyId = cS.id and sR.controlledInventoryDataset.id = 1221").list();
//        //check if dataset is used as a region/county dataset for specific strategy measures
//        list = session.createQuery("select cS.name from ControlStrategy as cS inner join cS.controlMeasures as cM inner join cM.regionDataset as rD with rD.id = 1221").list();
//        //check if dataset is used as a region/county dataset for specific strategy
//        list = session.createQuery("select cS.name from ControlStrategy cS where cS.countyDataset.id = 1221").list();
//
//        return list;
//    }

    public List getAllStrategyTypes(Session session) {
        return hibernateFacade.getAll(StrategyType.class, Order.asc("name"), session);
    }

//    // TODO: gettig all the strategies to obtain the lock--- is it a good idea?
//    public ControlStrategy obtainLocked(User owner, ControlStrategy element, Session session) {
//        return (ControlStrategy) lockingScheme.getLocked(owner, current(element, session), session);
//    }
//
    public ControlStrategy obtainLocked(User owner, int id, Session session) {
        return (ControlStrategy) lockingScheme.getLocked(owner, current(id, ControlStrategy.class, session), session);
    }

//    public void releaseLocked(ControlStrategy locked, Session session) {
//        ControlStrategy current = current(locked, session);
//        String runStatus = current.getRunStatus();
//        if (runStatus == null || !runStatus.equalsIgnoreCase("Running"))
//            lockingScheme.releaseLock(current, session);
//    }

    public void releaseLocked(User user, int id, Session session) {
        ControlStrategy current = getById(id, session);
        String runStatus = current.getRunStatus();
        if (runStatus == null || !runStatus.equalsIgnoreCase("Running"))
            lockingScheme.releaseLock(user, current, session);
    }

    public ControlStrategy update(ControlStrategy locked, Session session) throws EmfException {
        return (ControlStrategy) lockingScheme.releaseLockOnUpdate(locked, current(locked, session), session);
    }

    public void updateWithoutLock(ControlStrategy controlStrategy, Session session) throws EmfException {
        hibernateFacade.saveOrUpdate(controlStrategy, session);
    }
    
    public ControlStrategy updateWithLock(ControlStrategy locked, Session session) throws EmfException {
        return (ControlStrategy) lockingScheme.renewLockOnUpdate(locked, current(locked, session), session);
    }

    private ControlStrategy current(ControlStrategy strategy, Session session) {
        return current(strategy.getId(), ControlStrategy.class, session);
    }

    public boolean canUpdate(ControlStrategy controlStrategy, Session session) {
        if (!exists(controlStrategy.getId(), ControlStrategy.class, session)) {
            return false;
        }

        ControlStrategy current = current(controlStrategy.getId(), ControlStrategy.class, session);

        session.clear();// clear to flush current

        if (current.getName().equals(controlStrategy.getName()))
            return true;

        return !nameUsed(controlStrategy.getName(), ControlStrategy.class, session);
    }

    public boolean nameUsed(String name, Class clazz, Session session) {
        return hibernateFacade.nameUsed(name, clazz, session);
    }

    private ControlStrategy current(int id, Class clazz, Session session) {
        return (ControlStrategy) hibernateFacade.current(id, clazz, session);
    }

    public boolean exists(int id, Class clazz, Session session) {
        return hibernateFacade.exists(id, clazz, session);
    }

    public void remove(ControlStrategy strategy, Session session) {
        if (strategy.getConstraint() != null) hibernateFacade.remove(strategy.getConstraint(), session);
        hibernateFacade.remove(strategy, session);
    }

    public void remove(ControlStrategyResult result, Session session) {
        hibernateFacade.remove(result, session);
    }

    public StrategyResultType getDetailedStrategyResultType(Session session) {
        return getStrategyResultType(StrategyResultType.detailedStrategyResult, session);
    }

    public StrategyResultType getStrategyResultType(String name, Session session) {
        Criterion critName = Restrictions.eq("name", name);
        return (StrategyResultType)hibernateFacade.load(StrategyResultType.class, critName, session);
    }

    public StrategyResultType[] getOptionalStrategyResultTypes(Session session) {
        return (StrategyResultType[]) session.createCriteria(StrategyResultType.class).add(Restrictions.eq("optional", true)).list().toArray(new StrategyResultType[0]);
    }

    public StrategyResultType getSummaryStrategyResultType(Session session) {
        return getStrategyResultType(StrategyResultType.strategyMeasureSummary, session);
    }

    public ControlStrategyResult getControlStrategyResult(int controlStrategyId, int inputDatasetId, 
            int detailedResultDatasetId, Session session) {
        Criterion critControlStrategyId = Restrictions.eq("controlStrategyId", controlStrategyId);
        Criterion critInputDatasetId = Restrictions.eq("inputDatasetId", inputDatasetId);
        Criterion critDetailedResultDatasetId = Restrictions.eq("detailedResultDataset.id", detailedResultDatasetId);
        return (ControlStrategyResult)hibernateFacade.load(ControlStrategyResult.class, new Criterion[] {critControlStrategyId, critInputDatasetId, critDetailedResultDatasetId}, 
                session);
    }

    public ControlStrategyResult getControlStrategyResult(int id, Session session) {
        Criterion critId = Restrictions.eq("id", id);
        return (ControlStrategyResult)hibernateFacade.load(ControlStrategyResult.class, new Criterion[] {critId}, 
                session);
    }

//    private void updateControlStrategyIds(ControlStrategy controlStrategy, Session session) {
//        Criterion c1 = Restrictions.eq("name", controlStrategy.getName());
//        List list = hibernateFacade.get(ControlStrategy.class, c1, session);
//        if (!list.isEmpty()) {
//            ControlStrategy cs = (ControlStrategy) list.get(0);
//            controlStrategy.setId(cs.getId());
//        }
//    }
//
    public void updateControlStrategyResult(ControlStrategyResult result, Session session) {
        hibernateFacade.saveOrUpdate(result, session);
    }

    public String controlStrategyRunStatus(int id, Session session) {
        ControlStrategy controlStrategy = (ControlStrategy) hibernateFacade.current(id, ControlStrategy.class, session);
        return controlStrategy.getRunStatus();
    }

//    public void removeControlStrategyResult(ControlStrategy controlStrategy, Session session) {
//        Criterion c = Restrictions.eq("controlStrategyId", new Integer(controlStrategy.getId()));
//        List list = hibernateFacade.get(ControlStrategyResult.class, c, session);
//        for (int i = 0; i < list.size(); i++) {
//            ControlStrategyResult result = (ControlStrategyResult) list.get(i);
//            hibernateFacade.delete(result,session);
//        }
//    }

    public void removeControlStrategyResults(int controlStrategyId, Session session) {
        String hqlDelete = "delete ControlStrategyResult sr where sr.controlStrategyId = :controlStrategyId";
        session.createQuery( hqlDelete )
             .setInteger("controlStrategyId", controlStrategyId)
             .executeUpdate();
        session.flush();
    }

    public void removeControlStrategyResult(int controlStrategyId, int resultId, Session session) {
        String hqlDelete = "delete ControlStrategyResult sr where sr.id = :resultId and sr.controlStrategyId = :controlStrategyId";
        session.createQuery( hqlDelete )
             .setInteger("resultId", resultId)
             .setInteger("controlStrategyId", controlStrategyId)
             .executeUpdate();
        session.flush();
    }

    public ControlStrategy getByName(String name, Session session) {
        ControlStrategy cs = (ControlStrategy) hibernateFacade.load(ControlStrategy.class, Restrictions.eq("name", new String(name)), session);
        return cs;
    }

    public ControlStrategy getById(int id, Session session) {
        ControlStrategy cs = (ControlStrategy) hibernateFacade.load(ControlStrategy.class, Restrictions.eq("id", new Integer(id)), session);
        return cs;
    }

    public List<ControlStrategyResult> getControlStrategyResults(int controlStrategyId, Session session) {
        return session.createCriteria(ControlStrategyResult.class).add(Restrictions.eq("controlStrategyId", controlStrategyId)).addOrder(Order.desc("startTime")).list();
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
            new DataServiceImpl(dbServerFactory, sessionFactory).deleteDatasets(user, datasets, DeleteType.CONTROL_STRATEGY);
        } catch (EmfException e) {
//            releaseLocked(lockedDatasets, user, session);
//            throw new EmfException(e.getMessage());
            if (!e.getType().equals(EmfException.MSG_TYPE))
                throw new EmfException(e.getMessage());
        } finally {
            releaseLocked(lockedDatasets, user, session);
        }
    }
    
    private EmfDataset[] getLockedDatasets(EmfDataset[] datasets, User user, Session session) {
        List<EmfDataset> lockedList = new ArrayList<EmfDataset>();
        
        for (int i = 0; i < datasets.length; i++) {
            EmfDataset locked = obtainLockedDataset(datasets[i], user, session);
            if (locked == null) {
                releaseLocked(lockedList.toArray(new EmfDataset[0]), user, session);
                return null;
            }
            
            lockedList.add(locked);
        }
        
        return lockedList.toArray(new EmfDataset[0]);
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

    public Integer[] getResultDatasetIds(int controlStrategyId, Session session) {
        List<ControlStrategyResult> results = getControlStrategyResults(controlStrategyId, session);
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

    
    public EmfDataset[] getResultDatasets(int controlStrategyId, Session session) {
        List<ControlStrategyResult> results = getControlStrategyResults(controlStrategyId, session);
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

    public EmfDataset[] getResultDatasets(int controlStrategyId, int resultId, Session session) {
        ControlStrategyResult result = getControlStrategyResult(resultId, session);
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

    public void setControlStrategyRunStatus(int id, String runStatus, Date completionDate, Session session) {
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
                "from ControlStrategy cS where cS.id = " + id).uniqueResult();
    }

    public List<ControlStrategy> getControlStrategiesByControlMeasures(int[] cmIds, Session session) {
        List<ControlStrategy> list = new ArrayList<ControlStrategy>();
        String idList = "";
        for (int i = 0; i < cmIds.length; ++i) {
            idList += (i > 0 ? ","  : "") + cmIds[i];
        }
        try {
            Query query = session.createQuery("select distinct cs "
                    + "FROM ControlStrategy AS cs "
                    + (cmIds != null && cmIds.length > 0 
                            ? "inner join cs.controlMeasures AS csm inner join csm.controlMeasure AS cm "
                               + "WHERE cm.id in (" + idList + ") " 
                            : "")
                    + "order by cs.name");
//            Query query = session.createQuery("select new ControlStrategy(cs.id, cs.name, cs.controlMeasures) "
//                    + "FROM ControlStrategy AS cs "
//                    + (cmIds != null && cmIds.length > 0 
//                            ? "inner join cs.controlMeasures AS csm inner join csm.controlMeasure AS cm "
//                               + "WHERE cm.id in (" + idList + ") " 
//                            : "")
//                    + "order by cs.name");
            query.setCacheable(true);
            list = query.list();
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        return list;
    }
    
    public void finalizeControlStrategy(int controlStrategyId, String msg, Session session, int[] measureIdsToDelete) throws EmfException {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.createQuery("update ControlStrategy set isFinal = :isFinal, description =  '' || "
                        + "description || '\n------\n' || :msg, lastModifiedDate = :date where id = :id")
            .setBoolean("isFinal", true)
            .setText("msg", msg)
            .setTimestamp("date", new Date())
            .setInteger("id", controlStrategyId)
            .executeUpdate();
            tx.commit();
            session.clear();
            
            //also need to purge measures that are being deleted...this is needed to keep hibernate list_index in synch...
            ControlStrategy cs = getById(controlStrategyId, session);
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
            updateWithLock(cs, session);
            
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        } catch (EmfException e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
            throw e;
        }
    }

}
