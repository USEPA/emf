package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.io.DeepCopy;
import gov.epa.emissions.commons.io.ExporterException;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.GCEnforcerTask;
import gov.epa.emissions.framework.services.basic.EmfProperty;
import gov.epa.emissions.framework.services.cost.analysis.StrategySummaryFactory;
import gov.epa.emissions.framework.services.cost.analysis.SummarizeStrategy;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyConstraint;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;
import gov.epa.emissions.framework.services.cost.controlStrategy.SQLCompareControlStrategiesQuery;
import gov.epa.emissions.framework.services.cost.controlStrategy.StrategyResultType;
import gov.epa.emissions.framework.services.data.DatasetDAO;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.persistence.EmfPropertiesDAO;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;
import gov.epa.emissions.framework.services.qa.QueryToString;
import gov.epa.emissions.framework.tasks.DebugLevels;

import java.io.File;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Session;

import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;

public class ControlStrategyServiceImpl implements ControlStrategyService {

    private static Log LOG = LogFactory.getLog(ControlStrategyServiceImpl.class);

    private PooledExecutor threadPool;

    private HibernateSessionFactory sessionFactory;

    private DbServerFactory dbServerFactory;

    private ControlStrategyDAO dao;

    public ControlStrategyServiceImpl() throws Exception {
        init(HibernateSessionFactory.get(), DbServerFactory.get());
    }

    public ControlStrategyServiceImpl(HibernateSessionFactory sessionFactory, DbServerFactory dbServerFactory)
            throws Exception {
        init(sessionFactory, dbServerFactory);
    }

    private synchronized void init(HibernateSessionFactory sessionFactory, DbServerFactory dbServerFactory) {
        this.sessionFactory = sessionFactory;
        this.dbServerFactory = dbServerFactory;
        dao = new ControlStrategyDAO();
        threadPool = createThreadPool();

    }

    protected synchronized void finalize() throws Throwable {
        threadPool.shutdownAfterProcessingCurrentlyQueuedTasks();
        threadPool.awaitTerminationAfterShutdown();
        super.finalize();
    }

    private synchronized PooledExecutor createThreadPool() {
        PooledExecutor threadPool = new PooledExecutor(20);
        threadPool.setMinimumPoolSize(1);
        threadPool.setKeepAliveTime(1000 * 60 * 3);// terminate after 3 (unused) minutes

        return threadPool;
    }

    public synchronized ControlStrategy[] getControlStrategies() throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            List cs = dao.all(session);
            return (ControlStrategy[]) cs.toArray(new ControlStrategy[0]);
        } catch (HibernateException e) {
            LOG.error("Could not retrieve all control strategies.");
            throw new EmfException("Could not retrieve all control strategies.");
        } finally {
            session.close();
        }
    }

    public synchronized int addControlStrategy(ControlStrategy element) throws EmfException {
        Session session = sessionFactory.getSession();
        int csId;
        try {
            csId = dao.add(element, session);
        } catch (RuntimeException e) {
            LOG.error("Could not add Control Strategy: " + element, e);
            throw new EmfException("Could not add Control Strategy: " + element);
        } finally {
            session.close();
        }
        return csId;
    }

    public synchronized void setControlStrategyRunStatusAndCompletionDate(int id, String runStatus, Date completionDate)
            throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            dao.setControlStrategyRunStatusAndCompletionDate(id, runStatus, completionDate, session);
        } catch (RuntimeException e) {
            LOG.error("Could not set Control Strategy run status: " + id, e);
            throw new EmfException("Could not add Control Strategy run status: " + id);
        } finally {
            session.close();
        }
    }

    public synchronized ControlStrategy obtainLocked(User owner, int id) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            ControlStrategy locked = dao.obtainLocked(owner, id, session);

            return locked;
        } catch (RuntimeException e) {
            LOG
                    .error("Could not obtain lock for Control Strategy: id = " + id + " by owner: "
                            + owner.getUsername(), e);
            throw new EmfException("Could not obtain lock for Control Strategy: id = " + id + " by owner: "
                    + owner.getUsername());
        } finally {
            session.close();
        }
    }

    // FIXME
    // public void releaseLocked(ControlStrategy locked) throws EmfException {
    // Session session = sessionFactory.getSession();
    // try {
    // dao.releaseLocked(locked, session);
    // } catch (RuntimeException e) {
    // LOG.error(
    // "Could not release lock for Control Strategy : " + locked + " by owner: " + locked.getLockOwner(),
    // e);
    // throw new EmfException("Could not release lock for Control Strategy: " + locked + " by owner: "
    // + locked.getLockOwner());
    // } finally {
    // session.close();
    // }
    // }

    public synchronized void releaseLocked(User user, int id) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            dao.releaseLocked(user, id, session);
        } catch (RuntimeException e) {
            LOG.error("Could not release lock for Control Strategy id: " + id, e);
            throw new EmfException("Could not release lock for Control Strategy id: " + id);
        } finally {
            session.close();
        }
    }

    public synchronized ControlStrategy updateControlStrategy(ControlStrategy element) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            if (!dao.canUpdate(element, session))
                throw new EmfException("The Control Strategy name is already in use");

            ControlStrategy released = dao.update(element, session);

            return released;
        } catch (RuntimeException e) {
            LOG.error("Could not update Control Strategy: " + element, e);
            throw new EmfException("Could not update ControlStrategy: " + element);
        } finally {
            session.close();
        }
    }

    public synchronized ControlStrategy updateControlStrategyWithLock(ControlStrategy element) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            if (!dao.canUpdate(element, session))
                throw new EmfException("Control Strategy name already in use");

            ControlStrategy csWithLock = dao.updateWithLock(element, session);

            return csWithLock;
            // return dao.getById(csWithLock.getId(), session);
        } catch (RuntimeException e) {
            LOG.error("Could not update Control Strategy: " + element, e);
            throw new EmfException("Could not update ControlStrategy: " + element);
        } finally {
            session.close();
        }
    }

    // public void removeControlStrategies(ControlStrategy[] elements, User user) throws EmfException {
    // try {
    // for (int i = 0; i < elements.length; i++) {
    // if (!user.equals(elements[i].getCreator()))
    // throw new EmfException("Only the creator of " + elements[i].getName()
    // + " can remove it from the database.");
    // remove(elements[i]);
    // }
    //
    // } catch (RuntimeException e) {
    // LOG.error("Could not update Control Strategy: " + elements, e);
    // throw new EmfException("Could not update ControlStrategy: " + elements);
    // }
    // }

    public synchronized void removeControlStrategies(int[] ids, User user) throws EmfException {
        Session session = sessionFactory.getSession();
        String exception = "";
        try {
            for (int i = 0; i < ids.length; i++) {
                ControlStrategy cs = dao.getById(ids[i], session);
                session.clear();

                // check if admin user, then allow it to be removed.
                if (user.equals(cs.getCreator()) || user.isAdmin()) {
                    if (cs.isLocked())
                        exception += "The control strategy, " + cs.getName()
                                + ", is in edit mode and can not be removed. ";
                    else
                        remove(cs);
                } else {
                    exception += "You do not have permission to remove the strategy: " + cs.getName() + ". ";
                }
            }

            if (exception.length() > 0)
                throw new EmfException(exception);
        } catch (RuntimeException e) {
            LOG.error("Could not remove Control Strategy", e);
            throw new EmfException("Could not remove ControlStrategy");
        } finally {
            session.close();
        }
    }

    private synchronized void remove(ControlStrategy element) throws EmfException {
        Session session = sessionFactory.getSession();
        try {

            if (!dao.canUpdate(element, session))
                throw new EmfException("Control Strategy name already in use");

            ControlStrategyResult[] controlStrategyResults = getControlStrategyResults(element.getId());
            for (int i = 0; i < controlStrategyResults.length; i++) {
                dao.remove(controlStrategyResults[i], session);
            }

            dao.remove(element, session);
        } catch (RuntimeException e) {
            LOG.error("Could not remove control strategy: " + element, e);
            throw new EmfException("Could not remove control strategy: " + element.getName());
        } finally {
            session.close();
        }
    }

    public synchronized void removeResultDatasets(Integer[] ids, User user) throws EmfException {
        Session session = sessionFactory.getSession();
        DatasetDAO dsDao = new DatasetDAO();
        try {
            for (Integer id : ids) {
                EmfDataset dataset = dsDao.getDataset(session, id);

                if (dataset != null) {
                    try {
                        dsDao.remove(user, dataset, session);
                    } catch (EmfException e) {
                        if (DebugLevels.DEBUG_12())
                            System.out.println(e.getMessage());

                        throw new EmfException(e.getMessage());
                    }
                }
            }
        } finally {
            session.close();
        }
    }

    public synchronized void runStrategy(User user, int controlStrategyId) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            // first see if the strategy has been canceled, is so don't run it...
            String runStatus = dao.getControlStrategyRunStatus(controlStrategyId, session);
            if (runStatus.equals("Cancelled"))
                return;

            ControlStrategy strategy = getById(controlStrategyId);
            validateSectors(strategy);
            //get rid of for now, since we don't auto export anything
            //make sure a valid server-side export path was specified
            //validateExportPath(strategy.getExportDirectory());
            
            //make the runner of the strategy is the owner of the strategy...
            //NEED TO TALK TO ALISON ABOUT ISSUES, LOCEKD owner might not be the creator of resulting datsets,
            //hence a exception when trying to purge/delete the resulting datasets
            //if (control);
            

            // queue up the strategy to be run, by setting runStatus to Waiting
            dao.setControlStrategyRunStatusAndCompletionDate(controlStrategyId, "Waiting", null, session);

            StrategyFactory factory = new StrategyFactory();
//            validatePath(strategy.getExportDirectory());
            RunControlStrategy runStrategy = new RunControlStrategy(factory, sessionFactory, dbServerFactory,
                    threadPool);
            runStrategy.run(user, strategy, this);
        } catch (EmfException e) {
            // queue up the strategy to be run, by setting runStatus to Waiting
            dao.setControlStrategyRunStatusAndCompletionDate(controlStrategyId, "Failed", null, session);

            throw new EmfException(e.getMessage());
        } finally {
            session.close();
        }
    }

    public synchronized void summarizeStrategy(User user, int controlStrategyId, 
            String exportDirectory, StrategyResultType strategyResultType) throws EmfException {
        Session session = sessionFactory.getSession();
        try {

            ControlStrategy strategy = getById(controlStrategyId);

            // make sure a valid server-side export path was specified
            validateExportPath(exportDirectory);
            
            StrategySummaryFactory factory = new StrategySummaryFactory();

            SummarizeStrategy runStrategyResult = new SummarizeStrategy(factory, sessionFactory, 
                    dbServerFactory, threadPool);

            runStrategyResult.run(user, strategy, 
                    strategyResultType);

        } catch (EmfException e) {
            throw new EmfException(e.getMessage());
        } finally {
            session.close();
        }
    }

    private void validateSectors(ControlStrategy strategy) throws EmfException {
        ControlStrategyInputDataset[] inputDatasets = strategy.getControlStrategyInputDatasets();
        if (inputDatasets == null || inputDatasets.length == 0)
            throw new EmfException("Input Dataset does not exist. ");
        for (ControlStrategyInputDataset dataset : inputDatasets) {
            Sector[] sectors = dataset.getInputDataset().getSectors();
            if (sectors == null || sectors.length == 0)
                throw new EmfException("Inventory, " + dataset.getInputDataset().getName() + ", is missing a sector.  Edit dataset to add sector.");
        }
    }

    private void validateExportPath(String folderPath) throws EmfException {
        
        
        if (folderPath == null || folderPath.trim().length() == 0) 
            throw new EmfException("Missing export folder");
        
        File file = new File(folderPath);

        if (!file.exists() || !file.isDirectory()) 
            throw new EmfException("Export folder does not exist: " + folderPath);
    }

    public List<ControlStrategy> getControlStrategiesByRunStatus(String runStatus) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            return dao.getControlStrategiesByRunStatus(runStatus, session);
        } catch (RuntimeException e) {
            throw new EmfException("Could not get Control Strategies by run status: " + runStatus);
        } finally {
            session.close();
        }
    }

    public Long getControlStrategyRunningCount() throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            return dao.getControlStrategyRunningCount(session);
        } catch (RuntimeException e) {
            throw new EmfException("Could not get Control Strategies running count");
        } finally {
            session.close();
        }
    }

//    private File validatePath(String folderPath) throws EmfException {
//        File file = new File(folderPath);
//
//        if (!file.exists() || !file.isDirectory()) {
//            LOG.error("Folder " + folderPath + " does not exist");
//            throw new EmfException("Export folder does not exist: " + folderPath);
//        }
//        return file;
//    }

    public synchronized void stopRunStrategy(int controlStrategyId) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            // look at the current status, if waiting or running, then update to Cancelled.
            String status = dao.getControlStrategyRunStatus(controlStrategyId, session);
            if (status.toLowerCase().startsWith("waiting") || status.toLowerCase().startsWith("running"))
                dao.setControlStrategyRunStatusAndCompletionDate(controlStrategyId, "Cancelled", null, session);
        } catch (RuntimeException e) {
            LOG.error("Could not set Control Strategy run status: " + controlStrategyId, e);
            throw new EmfException("Could not add Control Strategy run status: " + controlStrategyId);
        } finally {
            session.close();
        }
    }

    public synchronized StrategyType[] getStrategyTypes() throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            List st = dao.getAllStrategyTypes(session);
            return (StrategyType[]) st.toArray(new StrategyType[0]);
        } catch (HibernateException e) {
            LOG.error("could not retrieve all control strategy types. " + e.getMessage());
            throw new EmfException("could not retrieve all control strategy types. " + e.getMessage());
        } finally {
            session.close();
        }
    }

    public synchronized void createInventories(User user, ControlStrategy controlStrategy,
            ControlStrategyResult[] controlStrategyResults, String namePrefix) throws EmfException {
        try {
            ControlStrategyInventoryOutputTask task = new ControlStrategyInventoryOutputTask(user, controlStrategy,
                    controlStrategyResults, namePrefix, 
                    sessionFactory, dbServerFactory);
            if (task.shouldProceed())
                threadPool.execute(new GCEnforcerTask("Create Inventories: " + controlStrategy.getName(), task));
        } catch (Exception e) {
            LOG.error("Error running control strategy: " + controlStrategy.getName(), e);
            throw new EmfException(e.getMessage());
        }
    }

    public synchronized String controlStrategyRunStatus(int id) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            return dao.controlStrategyRunStatus(id, session);
        } catch (RuntimeException e) {
            LOG.error("Could not retrieve ControlStrategy Status", e);
            throw new EmfException("Could not retrieve ControlStrategy Status");
        } finally {
            session.close();
        }
    }

    // returns control strategy Id for the given name
    public synchronized int isDuplicateName(String name) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            ControlStrategy cs = dao.getByName(name, session);
            return cs == null ? 0 : cs.getId();
        } catch (RuntimeException e) {
            LOG.error("Could not retrieve if ControlStrategy name is already used", e);
            throw new EmfException("Could not retrieve if ControlStrategy name is already used");
        } finally {
            session.close();
        }
    }

    public synchronized int copyControlStrategy(int id, User creator) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            // get cs to copy
            ControlStrategy cs = dao.getById(id, session);

            ControlStrategyConstraint constraint = cs.getConstraint();

            session.clear();// clear to flush current

            String name = "Copy of " + cs.getName();
            // make sure this won't cause duplicate issues...
            if (isDuplicate(name))
                throw new EmfException("A control strategy named '" + name + "' already exists.");

            // do a deep copy
            ControlStrategy copied = (ControlStrategy) DeepCopy.copy(cs);
            // change to applicable values
            copied.setName(name);
            copied.setCreator(creator);
            copied.setLastModifiedDate(new Date());
            copied.setIsFinal(false);
            copied.setRunStatus("Not started");
            copied.setCopiedFrom(cs.getName());
            if (copied.isLocked()) {
                copied.setLockDate(null);
                copied.setLockOwner(null);
            }

            dao.add(copied, session);
            int csId = copied.getId();
            // FIXME: something is not right with the hibernate mapping, constraint should be added automatically.
            if (constraint != null) {
                constraint.setControlStrategyId(csId);
                dao.add(constraint, session);
            }
            return csId;
        } catch (EmfException e) {
            LOG.error(e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            LOG.error("Could not copy control strategy", e);
            throw new EmfException("Could not copy control strategy");
        } catch (Exception e) {
            LOG.error("Could not copy control strategy", e);
            throw new EmfException("Could not copy control strategy");
        } finally {
            session.close();
        }
    }

    private synchronized boolean isDuplicate(String name) throws EmfException {
        return (isDuplicateName(name) != 0);
    }

    public synchronized ControlStrategy getById(int id) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            return dao.getById(id, session);
        } catch (RuntimeException e) {
            LOG.error("Could not get control strategy", e);
            throw new EmfException("Could not get control strategy");
        } finally {
            session.close();
        }
    }

    public synchronized ControlStrategyResult[] getControlStrategyResults(int controlStrategyId) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            List all = dao.getControlStrategyResults(controlStrategyId, session);
            return (ControlStrategyResult[]) all.toArray(new ControlStrategyResult[0]);
        } catch (RuntimeException e) {
            LOG.error("Could not retrieve control strategy results.", e);
            throw new EmfException("Could not retrieve control strategy results.");
        } finally {
            session.close();
        }
    }

    public synchronized String getDefaultExportDirectory() throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            String dir = dao.getDefaultExportDirectory(session);
            return dir;
        } catch (RuntimeException e) {
            LOG.error("Could not retrieve default export directory.", e);
            throw new EmfException("Could not retrieve default export directory.");
        } finally {
            session.close();
        }
    }

    public synchronized StrategyResultType[] getOptionalStrategyResultTypes() throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            return dao.getOptionalStrategyResultTypes(session);
        } catch (RuntimeException e) {
            LOG.error("Could not retrieve strategy result types.", e);
            throw new EmfException("Could not retrieve strategy result types.");
        } finally {
            session.close();
        }
    }

    public synchronized String getStrategyRunStatus(int id) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            return dao.getStrategyRunStatus(session, id);
        } catch (RuntimeException e) {
            LOG.error("Could not retrieve strategy run status.", e);
            throw new EmfException("Could not retrieve strategy run status.");
        } finally {
            session.close();
        }
    }

    public synchronized String getCoSTSUs() throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            EmfProperty property = new EmfPropertiesDAO().getProperty("COST_SU", session);
            
            return (property != null ? property.getValue() : "");
        } finally {
            session.close();
        }
    }
    
    public synchronized boolean isCoSTSU(String username) throws EmfException {
        return true;
    }
    
    public String getControlStrategyComparisonResult(int[] controlStrategyIds) throws EmfException {
        DbServer dbServer = dbServerFactory.getDbServer();
        try {
            return new QueryToString(dbServer, new SQLCompareControlStrategiesQuery().createCompareQuery(controlStrategyIds), ",").toString();
        } catch (RuntimeException e) {
            throw new EmfException("Could not retrieve control strategy comparison result: " + e.getMessage(), e);
        } catch (ExporterException e) {
            throw new EmfException("Could not retrieve control strategy comparison result: " + e.getMessage(), e);
        } finally {
            try {
                if (dbServer != null && dbServer.isConnected())
                    dbServer.disconnect();
            } catch (Exception e) {
                throw new EmfException("ControlStrategyService: error closing db server. " + e.getMessage());
            }
        }
    }
}
