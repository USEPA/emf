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
import gov.epa.emissions.framework.services.data.DatasetDAO;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.Keywords;
import gov.epa.emissions.framework.services.data.DataServiceImpl.DeleteType;
import gov.epa.emissions.framework.services.persistence.EmfPropertiesDAO;
import gov.epa.emissions.framework.services.persistence.HibernateFacade;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;
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

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;


public class FastDAO {
    private LockingScheme lockingScheme;

    private HibernateFacade hibernateFacade;

    private HibernateSessionFactory sessionFactory;

    private DbServerFactory dbServerFactory;
    
    private DatasetDAO datasetDao;

    private DataCommonsDAO dataCommonsDao;

    public FastDAO() {
        lockingScheme = new LockingScheme();
        hibernateFacade = new HibernateFacade();
        this.datasetDao = new DatasetDAO();
        this.dataCommonsDao = new DataCommonsDAO();
    }

    public FastDAO(DbServerFactory dbServerFactory, HibernateSessionFactory sessionFactory) {
        this();
        this.dbServerFactory = dbServerFactory;
        this.sessionFactory = sessionFactory;
    }

    public int add(FastRun element, Session session) {
        return addObject(element, session);
    }

    public int add(FastRunOutput element, Session session) {
        return addObject(element, session);
    }

    public int add(FastAnalysis element, Session session) {
        return addObject(element, session);
    }

    public int add(FastAnalysisOutput element, Session session) {
        return addObject(element, session);
    }

    private int addObject(Object obj, Session session) {
        return (Integer)hibernateFacade.add(obj, session);
    }

    public String getFastRunRunStatus(int fastRunId, Session session) {
        return (String)session.createQuery("select cS.runStatus from FastRun cS where cS.id = " + fastRunId).uniqueResult();
    }

    public Long getFastRunRunningCount(Session session) {
        Long count = (Long)session.createQuery("select count(*) as total from FastRun cS where cS.runStatus = 'Running'").uniqueResult();
        return count != null ? count : 0L;
    }

    public List<FastRun> getFastRunsByRunStatus(String runStatus, Session session) {
//        Criterion critRunStatus = Restrictions.eq("runStatus", runStatus);
//        return hibernateFacade.get(FastRun.class, critRunStatus, Order.asc("lastModifiedDate"), session);
//
        return session.createQuery("select new FastRun(cS.id, cS.name) from FastRun cS where cS.runStatus = :runStatus order by cS.lastModifiedDate").setString("runStatus", runStatus).list();
    }

    public void setFastRunRunStatusAndCompletionDate(int fastRunId, String runStatus, Date completionDate, Session session) {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.createQuery("update FastRun set runStatus = :status, lastModifiedDate = :date, completionDate = :completionDate where id = :id")
            .setString("status", runStatus)
            .setTimestamp("date", new Date())
            .setTimestamp("completionDate", completionDate)
            .setInteger("id", fastRunId)
            .executeUpdate();
            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }

    // return FastRuns orderby name
    public List<FastRun> getFastRuns(Session session) {
        return hibernateFacade.getAll(FastRun.class, Order.asc("name"), session);
    }

    // return FastRuns by Grid and orderby name
    public List<FastRun> getFastRuns(int gridId, Session session) {
        return hibernateFacade.get(FastRun.class, Restrictions.eq("grid.id", new Integer(gridId)), Order.asc("name"), session);
    }

    public List<FastRunOutputType> getFastRunOutputTypes(Session session) {
        return hibernateFacade.getAll(FastRunOutputType.class, Order.asc("name"), session);
    }

//    // TODO: gettig all the strategies to obtain the lock--- is it a good idea?
//    public FastRun obtainLocked(User owner, FastRun element, Session session) {
//        return (FastRun) lockingScheme.getLocked(owner, current(element, session), session);
//    }
//
    public FastRun obtainLockedFastRun(User owner, int id, Session session) {
        return (FastRun) lockingScheme.getLocked(owner, getFastRun(id, session), session);
    }

//    public void releaseLocked(FastRun locked, Session session) {
//        FastRun current = current(locked, session);
//        String runStatus = current.getRunStatus();
//        if (runStatus == null || !runStatus.equalsIgnoreCase("Running"))
//            lockingScheme.releaseLock(current, session);
//    }

    public void releaseLockedFastRun(User user, int id, Session session) {
        FastRun current = getFastRun(id, session);
        String runStatus = current.getRunStatus();
        if (runStatus == null || !runStatus.equalsIgnoreCase("Running"))
            lockingScheme.releaseLock(user, current, session);
    }

    public FastRun updateFastRun(FastRun locked, Session session) throws EmfException {
        return (FastRun) lockingScheme.releaseLockOnUpdate(locked, getFastRun(locked.getId(), session), session);
    }

    public FastRun updateFastRunWithLock(FastRun locked, Session session) throws EmfException {
        return (FastRun) lockingScheme.renewLockOnUpdate(locked, getFastRun(locked.getId(), session), session);
    }

    public boolean canUpdateFastRun(FastRun fastRun, Session session) {
        if (!exists(fastRun.getId(), FastRun.class, session)) {
            return false;
        }

        FastRun current = getFastRun(fastRun.getId(), session);

        session.clear();// clear to flush current

        if (current.getName().equals(fastRun.getName()))
            return true;

        return !nameUsed(fastRun.getName(), FastRun.class, session);
    }

    public boolean nameUsed(String name, Class clazz, Session session) {
        return hibernateFacade.nameUsed(name, clazz, session);
    }

//    private FastRun current(int id, Class clazz, Session session) {
//        return (FastRun) hibernateFacade.current(id, clazz, session);
//    }

    public boolean exists(int id, Class clazz, Session session) {
        return hibernateFacade.exists(id, clazz, session);
    }

    public void remove(FastRun strategy, Session session) {
        hibernateFacade.remove(strategy, session);
    }

    public void remove(FastRunOutput result, Session session) {
        hibernateFacade.remove(result, session);
    }

    public FastRunOutputType getFastRunOutputType(String name, Session session) {
        Criterion critName = Restrictions.eq("name", name);
        return (FastRunOutputType)hibernateFacade.load(FastRunOutputType.class, critName, session);
    }

    public FastRunOutput getFastRunOutput(int fastRunId, int inputDatasetId, 
            int detailedResultDatasetId, Session session) {
        Criterion critFastRunId = Restrictions.eq("fastRunId", fastRunId);
        Criterion critInputDatasetId = Restrictions.eq("inputDatasetId", inputDatasetId);
        Criterion critDetailedResultDatasetId = Restrictions.eq("detailedResultDataset.id", detailedResultDatasetId);
        return (FastRunOutput)hibernateFacade.load(FastRunOutput.class, new Criterion[] {critFastRunId, critInputDatasetId, critDetailedResultDatasetId}, 
                session);
    }

    public FastRunOutput getFastRunOutput(int id, Session session) {
        Criterion critId = Restrictions.eq("id", id);
        return (FastRunOutput)hibernateFacade.load(FastRunOutput.class, new Criterion[] {critId}, 
                session);
    }

//    private void updateFastRunIds(FastRun fastRun, Session session) {
//        Criterion c1 = Restrictions.eq("name", fastRun.getName());
//        List list = hibernateFacade.get(FastRun.class, c1, session);
//        if (!list.isEmpty()) {
//            FastRun cs = (FastRun) list.get(0);
//            fastRun.setId(cs.getId());
//        }
//    }
//
    public void updateFastRunOutput(FastRunOutput result, Session session) {
        hibernateFacade.saveOrUpdate(result, session);
    }

    public String fastRunRunStatus(int id, Session session) {
        FastRun fastRun = (FastRun) hibernateFacade.current(id, FastRun.class, session);
        return fastRun.getRunStatus();
    }

//    public void removeFastRunResult(FastRun fastRun, Session session) {
//        Criterion c = Restrictions.eq("fastRunId", new Integer(fastRun.getId()));
//        List list = hibernateFacade.get(FastRunResult.class, c, session);
//        for (int i = 0; i < list.size(); i++) {
//            FastRunResult result = (FastRunResult) list.get(i);
//            hibernateFacade.delete(result,session);
//        }
//    }

    public void removeFastRunResults(int fastRunId, Session session) {
//        String hqlDelete = "delete FastRunOutput sr where sr.fastRunId = :fastRunId";
//        session.createQuery( hqlDelete )
//             .setInteger("fastRunId", fastRunId)
//             .executeUpdate();
//        session.flush();
        List<?> outputs = hibernateFacade.get(FastRunOutput.class, Restrictions.eq("fastRunId", new Integer(fastRunId)), session);
        hibernateFacade.remove(outputs.toArray(), session);
    }

    public FastRun getFastRun(String name, Session session) {
        FastRun cs = (FastRun) hibernateFacade.load(FastRun.class, Restrictions.eq("name", new String(name)), session);
        return cs;
    }

    public FastRun getFastRun(int id, Session session) {
        FastRun cs = (FastRun) hibernateFacade.load(FastRun.class, Restrictions.eq("id", new Integer(id)), session);
        return cs;
    }

    public List<FastRunOutput> getFastRunOutputs(int fastRunId, Session session) {
        return session.createCriteria(FastRunOutput.class).add(Restrictions.eq("fastRunId", fastRunId)).addOrder(Order.desc("startDate")).list();
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
            new DataServiceImpl(dbServerFactory, sessionFactory).deleteDatasets(user, datasets, DeleteType.FAST);
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

    public Integer[] getResultDatasetIds(int fastRunId, Session session) {
        List<FastRunOutput> results = getFastRunOutputs(fastRunId, session);
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

    
    public EmfDataset[] getOutputDatasets(int fastRunId, Session session) {
        List<FastRunOutput> results = getFastRunOutputs(fastRunId, session);
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

    public void setFastRunRunStatus(int id, String runStatus, Date completionDate, Session session) {
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
                "from FastRun cS where cS.id = " + id).uniqueResult();
    }
    
    public List<FastDataset> getFastDatasets(Session session) {
        return hibernateFacade.getAll(FastDataset.class, session);
    }

    public FastDataset getFastDataset(Session session, int fastDatasetId) {
        return (FastDataset) hibernateFacade.load(FastDataset.class, Restrictions.eq("id", new Integer(fastDatasetId)), session);
    }

    public int addFastDataset(FastDataset fastDataset, Session session) {
        return addObject(fastDataset, session);
    }

    public void removeFastDataset(int fastDatasetId, Session session) {
        String hqlDelete = "delete FastDataset fd where fd.id = :fastDatasetId";
        session.createQuery( hqlDelete )
             .setInteger("fastDatasetId", fastDatasetId)
             .executeUpdate();
        session.flush();
    }

    public List<FastNonPointDataset> getFastNonPointDatasets(Session session) {
        return hibernateFacade.getAll(FastNonPointDataset.class, session);
    }

    public FastNonPointDataset getFastNonPointDataset(Session session, int fastNonPointDatasetId) {
        return (FastNonPointDataset) hibernateFacade.load(FastNonPointDataset.class, Restrictions.eq("id", new Integer(fastNonPointDatasetId)), session);
    }

    public int addFastNonPointDataset(FastNonPointDataset fastNonPointDataset, Session session) {
        return addObject(fastNonPointDataset, session);
    }

    public void removeFastNonPointDataset(int fastNonPointDatasetId, Session session) {
        String hqlDelete = "delete FastNonPointDataset fd where fd.id = :fastNonPointDatasetId";
        session.createQuery( hqlDelete )
             .setInteger("fastNonPointDatasetId", fastNonPointDatasetId)
             .executeUpdate();
        session.flush();
    }

    public List<Grid> getGrids(Session session) {
        return hibernateFacade.getAll(Grid.class, session);
    }

    public Grid getGrid(Session session, String name) {
        return (Grid) hibernateFacade.load(Grid.class, Restrictions.eq("name", name), session);
    }

    public Grid getGrid(Session session, int id) {
        return (Grid) hibernateFacade.load(Grid.class, Restrictions.eq("id", id), session);
    }







    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    

    public String getFastAnalysisRunStatus(int fastAnalysisId, Session session) {
        return (String)session.createQuery("select cS.runStatus from FastAnalysis cS where cS.id = " + fastAnalysisId).uniqueResult();
    }

    public Long getFastAnalysisRunningCount(Session session) {
        Long count = (Long)session.createQuery("select count(*) as total from FastAnalysis cS where cS.runStatus = 'Running'").uniqueResult();
        return count != null ? count : 0L;
    }

    public List<FastAnalysis> getFastAnalysesByRunStatus(String runStatus, Session session) {
//        Criterion critRunStatus = Restrictions.eq("runStatus", runStatus);
//        return hibernateFacade.get(FastAnalysis.class, critRunStatus, Order.asc("lastModifiedDate"), session);
//
        return session.createQuery("select new FastAnalysis(cS.id, cS.name) from FastAnalysis cS where cS.runStatus = :runStatus order by cS.lastModifiedDate").setString("runStatus", runStatus).list();
    }

    public void setFastAnalysisRunStatusAndCompletionDate(int fastAnalysisId, String runStatus, Date completionDate, Session session) {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.createQuery("update FastAnalysis set runStatus = :status, lastModifiedDate = :date, completionDate = :completionDate where id = :id")
            .setString("status", runStatus)
            .setTimestamp("date", new Date())
            .setTimestamp("completionDate", completionDate)
            .setInteger("id", fastAnalysisId)
            .executeUpdate();
            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }

    // return FastAnalyses orderby name
    public List<FastAnalysis> getFastAnalyses(Session session) {
        return hibernateFacade.getAll(FastAnalysis.class, Order.asc("name"), session);
    }

    public List getAllFastAnalysisOuputTypes(Session session) {
        return hibernateFacade.getAll(FastAnalysisOutputType.class, Order.asc("name"), session);
    }

    public FastAnalysis obtainLockedFastAnalysis(User owner, int id, Session session) {
        return (FastAnalysis) lockingScheme.getLocked(owner, getFastAnalysis(id, session), session);
    }

    public void releaseLockedFastAnalysis(User user, int id, Session session) {
        FastAnalysis current = getFastAnalysis(id, session);
        String runStatus = current.getRunStatus();
        if (runStatus == null || !runStatus.equalsIgnoreCase("Running"))
            lockingScheme.releaseLock(user, current, session);
    }

    public FastAnalysis updateFastAnalysis(FastAnalysis locked, Session session) throws EmfException {
        return (FastAnalysis) lockingScheme.releaseLockOnUpdate(locked, getFastAnalysis(locked.getId(), session), session);
    }

    public FastAnalysis updateWithLock(FastAnalysis locked, Session session) throws EmfException {
        return (FastAnalysis) lockingScheme.renewLockOnUpdate(locked, getFastAnalysis(locked.getId(), session), session);
    }

    public boolean canUpdate(FastAnalysis fastAnalysis, Session session) {
        if (!exists(fastAnalysis.getId(), FastAnalysis.class, session)) {
            return false;
        }

        FastAnalysis current = getFastAnalysis(fastAnalysis.getId(), session);

        session.clear();// clear to flush current

        if (current.getName().equals(fastAnalysis.getName()))
            return true;

        return !nameUsed(fastAnalysis.getName(), FastAnalysis.class, session);
    }

    public void remove(FastAnalysis strategy, Session session) {
        hibernateFacade.remove(strategy, session);
    }

    public void remove(FastAnalysisOutput result, Session session) {
        hibernateFacade.remove(result, session);
    }

    public FastAnalysisOutputType getFastAnalysisOutputType(String name, Session session) {
        Criterion critName = Restrictions.eq("name", name);
        return (FastAnalysisOutputType)hibernateFacade.load(FastAnalysisOutputType.class, critName, session);
    }

    public List<FastAnalysisOutputType> getFastAnalysisOutputTypes(Session session) {
        return hibernateFacade.getAll(FastAnalysisOutputType.class, Order.asc("name"), session);
    }

    public FastAnalysisOutput getFastAnalysisOutput(int fastAnalysisId, int inputDatasetId, 
            int detailedResultDatasetId, Session session) {
        Criterion critFastAnalysisId = Restrictions.eq("fastAnalysisId", fastAnalysisId);
        Criterion critInputDatasetId = Restrictions.eq("inputDatasetId", inputDatasetId);
        Criterion critDetailedResultDatasetId = Restrictions.eq("detailedResultDataset.id", detailedResultDatasetId);
        return (FastAnalysisOutput)hibernateFacade.load(FastAnalysisOutput.class, new Criterion[] {critFastAnalysisId, critInputDatasetId, critDetailedResultDatasetId}, 
                session);
    }

    public FastAnalysisOutput getFastAnalysisOutput(int id, Session session) {
        Criterion critId = Restrictions.eq("id", id);
        return (FastAnalysisOutput)hibernateFacade.load(FastAnalysisOutput.class, new Criterion[] {critId}, 
                session);
    }

//    private void updateFastAnalysisIds(FastAnalysis fastAnalysis, Session session) {
//        Criterion c1 = Restrictions.eq("name", fastAnalysis.getName());
//        List list = hibernateFacade.get(FastAnalysis.class, c1, session);
//        if (!list.isEmpty()) {
//            FastAnalysis cs = (FastAnalysis) list.get(0);
//            fastAnalysis.setId(cs.getId());
//        }
//    }
//
    public void updateFastAnalysisOutput(FastAnalysisOutput result, Session session) {
        hibernateFacade.saveOrUpdate(result, session);
    }

    public String fastAnalysisRunStatus(int id, Session session) {
        FastAnalysis fastAnalysis = (FastAnalysis) hibernateFacade.current(id, FastAnalysis.class, session);
        return fastAnalysis.getRunStatus();
    }

//    public void removeFastAnalysisResult(FastAnalysis fastAnalysis, Session session) {
//        Criterion c = Restrictions.eq("fastAnalysisId", new Integer(fastAnalysis.getId()));
//        List list = hibernateFacade.get(FastAnalysisResult.class, c, session);
//        for (int i = 0; i < list.size(); i++) {
//            FastAnalysisResult result = (FastAnalysisResult) list.get(i);
//            hibernateFacade.delete(result,session);
//        }
//    }

    public void removeFastAnalysisOutputs(int fastAnalysisId, Session session) {
//        String hqlDelete = "delete FastAnalysisOutput sr where sr.fastAnalysisId = :fastAnalysisId";
//        session.createQuery( hqlDelete )
//             .setInteger("fastAnalysisId", fastAnalysisId)
//             .executeUpdate();
//        session.flush();
        
        List<?> outputs = hibernateFacade.get(FastAnalysisOutput.class, Restrictions.eq("fastAnalysisId", new Integer(fastAnalysisId)), session);
        hibernateFacade.remove(outputs.toArray(), session);
    }

    public FastAnalysis getFastAnalysis(String name, Session session) {
        FastAnalysis cs = (FastAnalysis) hibernateFacade.load(FastAnalysis.class, Restrictions.eq("name", new String(name)), session);
        return cs;
    }

    public FastAnalysis getFastAnalysis(int id, Session session) {
        FastAnalysis cs = (FastAnalysis) hibernateFacade.load(FastAnalysis.class, Restrictions.eq("id", new Integer(id)), session);
        return cs;
    }

    public List<FastAnalysisOutput> getFastAnalysisOutputs(int fastAnalysisId, Session session) {
        return session.createCriteria(FastAnalysisOutput.class).add(Restrictions.eq("fastAnalysisId", fastAnalysisId)).addOrder(Order.desc("startDate")).list();
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

    public Integer[] getFastAnalysisResultDatasetIds(int fastAnalysisId, Session session) {
        List<FastAnalysisOutput> results = getFastAnalysisOutputs(fastAnalysisId, session);
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

    
    public EmfDataset[] getFastAnalysisOutputDatasets(int fastAnalysisId, Session session) {
        List<FastAnalysisOutput> results = getFastAnalysisOutputs(fastAnalysisId, session);
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

    public void setFastAnalysisRunStatus(int id, String runStatus, Date completionDate, Session session) {
        // NOTE Auto-generated method stub
        
    }

    public String getFastAnalysisRunStatus(Session session, int id) {
        return (String)session.createQuery("select cS.runStatus " +
                "from FastAnalysis cS where cS.id = " + id).uniqueResult();
    }

//    private EmfDataset getDataset(String name, Session session) {
//        return datasetDao.getDataset(session, name);
//    }

    private EmfDataset getDataset(int id, Session session) {
        return datasetDao.getDataset(session, id);
    }

    private DatasetType getDatasetType(String name, Session session) {
        return dataCommonsDao.getDatasetType(name, session);
    }

//    private User getUser(String name, Session session) {
//        return new UserDAO().get(name, session);
//    }

    public int addFastNonPointDataset(FastNonPointDataset fastNonPointDataset, User user, 
            Session session, DbServer dbServer) throws EmfException {
        
        EmfDataset dataset = createFastQuasiPointDataset(fastNonPointDataset.getBaseNonPointDataset(), fastNonPointDataset.getName(), user, session, dbServer);
//            getDataset("ptnonipm_xportfrac_cap2005v2_20nov2008_revised_20jan2009_v0", session);

        FastDataset fastDataset = new FastDataset();
        fastDataset.setDataset(dataset);
        fastDataset.setAddedDate(Calendar.getInstance().getTime());
//        fastService.addFastDataset(fastDataset);
        
        fastNonPointDataset.setFastDataset(fastDataset);
//        fastNonPointDataset.setId(fastService.addFastNonPointDataset(fastNonPointDataset));
        fastDataset.setFastNonPointDataset(fastNonPointDataset);
        addFastNonPointDataset(fastNonPointDataset, session);
        return dataset.getId();
    }
    
    private EmfDataset createFastQuasiPointDataset(EmfDataset base, String newInventoryDatasetName, User user, Session session, DbServer dbServer) throws EmfException {
        DatasetType datasetType = getDatasetType(DatasetType.orlPointInventory, session);
        Keywords keywords = new Keywords(new DataCommonsServiceImpl(sessionFactory).getKeywords());
        DatasetCreator creator = new DatasetCreator(null, user, 
                sessionFactory, dbServerFactory,
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
        Session session = sessionFactory.getSession();
        try {
            Versions versions = new Versions();
            return versions.get(datasetId, version, session);
        } finally {
            session.close();
        }
    }

    public String[] getFastRunSpeciesMappingDatasetPollutants(int datasetId, int datasetVersion) throws EmfException {
        List<String> pollutantsList = new ArrayList<String>();
        ResultSet rs = null;
        Statement statement = null;
        DbServer dbServer = dbServerFactory.getDbServer();
        Session session = sessionFactory.getSession();
        Connection con = dbServer.getConnection();

        VersionedQuery versionedQuery = new VersionedQuery(version(datasetId,
                datasetVersion));
        String tableName = getDataset(datasetId, session).getInternalSources()[0].getTable();

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
            if (session != null) {
                try {
                    session.close();
                } catch (Exception e) {
                    // NOTE Auto-generated catch block
                    e.printStackTrace();
                }
                session = null;
            }
            
        }
        return pollutantsList.toArray(new String[0]);
    }
}
