package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.io.DeepCopy;
import gov.epa.emissions.commons.io.ExporterException;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.GCEnforcerTask;
import gov.epa.emissions.framework.services.basic.BasicSearchFilter;
import gov.epa.emissions.framework.services.basic.EmfProperty;
import gov.epa.emissions.framework.services.cost.analysis.StrategySummaryFactory;
import gov.epa.emissions.framework.services.cost.analysis.SummarizeStrategy;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyConstraint;
import gov.epa.emissions.framework.services.cost.controlStrategy.ControlStrategyResult;
import gov.epa.emissions.framework.services.cost.controlStrategy.SQLCompareControlStrategiesQuery;
import gov.epa.emissions.framework.services.cost.controlStrategy.SQLSummarizeControlStrategiesQuery;
import gov.epa.emissions.framework.services.cost.controlStrategy.StrategyGroup;
import gov.epa.emissions.framework.services.cost.controlStrategy.StrategyResultType;
import gov.epa.emissions.framework.services.data.DatasetDAO;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.persistence.EmfPropertiesDAO;
import gov.epa.emissions.framework.services.persistence.JpaEntityManagerFactory;
import gov.epa.emissions.framework.services.qa.QueryToString;
import gov.epa.emissions.framework.tasks.DebugLevels;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;

import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;

public class ControlStrategyServiceImpl implements ControlStrategyService {

    private static Log LOG = LogFactory.getLog(ControlStrategyServiceImpl.class);

    private PooledExecutor threadPool;

    private EntityManagerFactory entityManagerFactory;

    private DbServerFactory dbServerFactory;

    private ControlStrategyDAO dao;

    public ControlStrategyServiceImpl() throws Exception {
        init(JpaEntityManagerFactory.get(), DbServerFactory.get());
    }

    public ControlStrategyServiceImpl(EntityManagerFactory entityManagerFactory, DbServerFactory dbServerFactory)
            throws Exception {
        init(entityManagerFactory, dbServerFactory);
    }

    private synchronized void init(EntityManagerFactory entityManagerFactory, DbServerFactory dbServerFactory) {
        this.entityManagerFactory = entityManagerFactory;
        this.dbServerFactory = dbServerFactory;
        dao = new ControlStrategyDAO(dbServerFactory, entityManagerFactory);
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
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            List cs = dao.all(entityManager);
            return (ControlStrategy[]) cs.toArray(new ControlStrategy[0]);
        } catch (HibernateException e) {
            LOG.error("Could not retrieve all control strategies.");
            throw new EmfException("Could not retrieve all control strategies.");
        } finally {
            entityManager.close();
        }
    }

    public synchronized ControlStrategy[] getControlStrategies(BasicSearchFilter searchFilter) throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            List cs = dao.getControlStrategies(entityManager, searchFilter);
            return (ControlStrategy[]) cs.toArray(new ControlStrategy[0]);
        } catch (HibernateException e) {
            LOG.error("Could not retrieve all control strategies.");
            throw new EmfException("Could not retrieve all control strategies.");
        } finally {
            entityManager.close();
        }
    }

    public synchronized int addControlStrategy(ControlStrategy element) throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        int csId;
        try {
            csId = dao.add(element, entityManager);
        } catch (RuntimeException e) {
            LOG.error("Could not add Control Strategy: " + element, e);
            throw new EmfException("Could not add Control Strategy: " + element);
        } finally {
            entityManager.close();
        }
        return csId;
    }

    public synchronized void setControlStrategyRunStatusAndCompletionDate(int id, String runStatus, Date completionDate)
            throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            dao.setControlStrategyRunStatusAndCompletionDate(id, runStatus, completionDate, entityManager);
        } catch (RuntimeException e) {
            LOG.error("Could not set Control Strategy run status: " + id, e);
            throw new EmfException("Could not add Control Strategy run status: " + id);
        } finally {
            entityManager.close();
        }
    }

    public synchronized ControlStrategy obtainLocked(User owner, int id) throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            ControlStrategy locked = dao.obtainLocked(owner, id, entityManager);

            return locked;
        } catch (RuntimeException e) {
            LOG
                    .error("Could not obtain lock for Control Strategy: id = " + id + " by owner: "
                            + owner.getUsername(), e);
            throw new EmfException("Could not obtain lock for Control Strategy: id = " + id + " by owner: "
                    + owner.getUsername());
        } finally {
            entityManager.close();
        }
    }

    // FIXME
    // public void releaseLocked(ControlStrategy locked) throws EmfException {
    // EntityManager entityManager = entityManagerFactory.createEntityManager();
    // try {
    // dao.releaseLocked(locked, entityManager);
    // } catch (RuntimeException e) {
    // LOG.error(
    // "Could not release lock for Control Strategy : " + locked + " by owner: " + locked.getLockOwner(),
    // e);
    // throw new EmfException("Could not release lock for Control Strategy: " + locked + " by owner: "
    // + locked.getLockOwner());
    // } finally {
    // entityManager.close();
    // }
    // }

    public synchronized void releaseLocked(User user, int id) throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            dao.releaseLocked(user, id, entityManager);
        } catch (RuntimeException e) {
            LOG.error("Could not release lock for Control Strategy id: " + id, e);
            throw new EmfException("Could not release lock for Control Strategy id: " + id);
        } finally {
            entityManager.close();
        }
    }

    public synchronized ControlStrategy updateControlStrategy(ControlStrategy element) throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            if (!dao.canUpdate(element, entityManager))
                throw new EmfException("The Control Strategy name is already in use");

            ControlStrategy released = dao.update(element, entityManager);

            return released;
        } catch (RuntimeException e) {
            LOG.error("Could not update Control Strategy: " + element, e);
            throw new EmfException("Could not update ControlStrategy: " + element);
        } finally {
            entityManager.close();
        }
    }

    public synchronized ControlStrategy updateControlStrategyWithLock(ControlStrategy element) throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            if (!dao.canUpdate(element, entityManager))
                throw new EmfException("Control Strategy name already in use");

            ControlStrategy csWithLock = dao.updateWithLock(element, entityManager);

            return csWithLock;
            // return dao.getById(csWithLock.getId(), entityManager);
        } catch (RuntimeException e) {
            LOG.error("Could not update Control Strategy: " + element, e);
            throw new EmfException("Could not update ControlStrategy: " + element);
        } finally {
            entityManager.close();
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
        removeControlStrategies(ids, user, false, false);
    }

    public synchronized void removeControlStrategies(int[] ids, User user, boolean deleteResults, boolean deleteCntlInvs) throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        String exception = "";
        try {
            for (int i = 0; i < ids.length; i++) {
                ControlStrategy cs = dao.getById(ids[i], entityManager);
                entityManager.clear();

                // check if admin user, then allow it to be removed.
                if (user.equals(cs.getCreator()) || user.isAdmin()) {
                    if (cs.isLocked())
                        exception += "The control strategy, " + cs.getName()
                                + ", is in edit mode and can not be removed. ";
                    else
                        remove(cs, user, deleteResults, deleteCntlInvs);
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
            entityManager.close();
        }
    }

    private synchronized void remove(ControlStrategy element, User user, boolean deleteResults, boolean deleteCntlInvs) throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {

            if (!dao.canUpdate(element, entityManager))
                throw new EmfException("Control Strategy name already in use");

            ControlStrategyResult[] controlStrategyResults = getControlStrategyResults(element.getId());
            List<EmfDataset> dsList = new ArrayList<EmfDataset>();
            for (int i = 0; i < controlStrategyResults.length; i++) {
                EmfDataset controlledInv = (EmfDataset)controlStrategyResults[i].getControlledInventoryDataset();
                if (controlledInv != null && deleteCntlInvs) {
                    dsList.add(controlledInv);
                }
                EmfDataset dataset = (EmfDataset)controlStrategyResults[i].getDetailedResultDataset();
                if (dataset != null && deleteResults) {
                    dsList.add(dataset);
                }
            }
            
            dao.removeControlStrategyResults(element.getId(), entityManager);

            if (dsList.size() > 0) {
                dao.removeResultDatasets(dsList.toArray(new EmfDataset[0]), user, entityManager,
                        dbServerFactory.getDbServer());
            }

            dao.remove(element, entityManager);
        } catch (RuntimeException e) {
            LOG.error("Could not remove control strategy: " + element, e);
            throw new EmfException("Could not remove control strategy: " + element.getName());
        } finally {
            entityManager.close();
        }
    }

    public synchronized void removeResultDatasets(Integer[] ids, User user) throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        DatasetDAO dsDao = new DatasetDAO();
        try {
            for (Integer id : ids) {
                EmfDataset dataset = dsDao.getDataset(entityManager, id);

                if (dataset != null) {
                    try {
                        dsDao.remove(user, dataset, entityManager);
                    } catch (EmfException e) {
                        if (DebugLevels.DEBUG_12())
                            System.out.println(e.getMessage());

                        throw new EmfException(e.getMessage());
                    }
                }
            }
        } finally {
            entityManager.close();
        }
    }

    public synchronized void runStrategy(User user, int controlStrategyId) throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            ControlStrategy strategy = getById(controlStrategyId);
            LOG.info(strategy.getConstraint().getDomainWidePctReductionIncrement());
            LOG.info(strategy.getConstraint().getDomainWidePctReduction());
            //get rid of for now, since we don't auto export anything
            //make sure a valid server-side export path was specified
            //validateExportPath(strategy.getExportDirectory());
            
            //make the runner of the strategy is the owner of the strategy...
            //NEED TO TALK TO ALISON ABOUT ISSUES, LOCEKD owner might not be the creator of resulting datsets,
            //hence a exception when trying to purge/delete the resulting datasets
            //if (control);
            

            // queue up the strategy to be run, by setting runStatus to Waiting
            dao.setControlStrategyRunStatusAndCompletionDate(controlStrategyId, "Waiting", null, entityManager);

            StrategyFactory factory = new StrategyFactory();
//            validatePath(strategy.getExportDirectory());
            RunControlStrategy runStrategy = new RunControlStrategy(factory, entityManagerFactory, dbServerFactory,
                    threadPool);
            runStrategy.run(user, strategy, this);
        } catch (EmfException e) {
            // queue up the strategy to be run, by setting runStatus to Waiting
            dao.setControlStrategyRunStatusAndCompletionDate(controlStrategyId, "Failed", null, entityManager);

            throw new EmfException(e.getMessage());
        } finally {
            entityManager.close();
        }
    }

    public synchronized void summarizeStrategy(User user, int controlStrategyId, 
            String exportDirectory, StrategyResultType strategyResultType) throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {

            ControlStrategy strategy = getById(controlStrategyId);
            
            StrategySummaryFactory factory = new StrategySummaryFactory();

            SummarizeStrategy runStrategyResult = new SummarizeStrategy(factory, entityManagerFactory, 
                    dbServerFactory, threadPool);

            runStrategyResult.run(user, strategy, 
                    strategyResultType);

        } catch (EmfException e) {
            throw new EmfException(e.getMessage());
        } finally {
            entityManager.close();
        }
    }

    public List<ControlStrategy> getControlStrategiesByRunStatus(String runStatus) throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            return dao.getControlStrategiesByRunStatus(runStatus, entityManager);
        } catch (RuntimeException e) {
            throw new EmfException("Could not get Control Strategies by run status: " + runStatus);
        } finally {
            entityManager.close();
        }
    }

    public Long getControlStrategyRunningCount() throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            return dao.getControlStrategyRunningCount(entityManager);
        } catch (RuntimeException e) {
            throw new EmfException("Could not get Control Strategies running count");
        } finally {
            entityManager.close();
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
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            // look at the current status, if waiting or running, then update to Cancelled.
            String status = dao.getControlStrategyRunStatus(controlStrategyId, entityManager);
            if (status.toLowerCase().startsWith("waiting") || status.toLowerCase().startsWith("running"))
                dao.setControlStrategyRunStatusAndCompletionDate(controlStrategyId, "Cancelled", null, entityManager);
        } catch (RuntimeException e) {
            LOG.error("Could not set Control Strategy run status: " + controlStrategyId, e);
            throw new EmfException("Could not add Control Strategy run status: " + controlStrategyId);
        } finally {
            entityManager.close();
        }
    }

    public synchronized StrategyType[] getStrategyTypes() throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            List st = dao.getAllStrategyTypes(entityManager);
            return (StrategyType[]) st.toArray(new StrategyType[0]);
        } catch (HibernateException e) {
            LOG.error("could not retrieve all control strategy types. " + e.getMessage());
            throw new EmfException("could not retrieve all control strategy types. " + e.getMessage());
        } finally {
            entityManager.close();
        }
    }

    public synchronized void createInventories(User user, ControlStrategy controlStrategy,
            ControlStrategyResult[] controlStrategyResults, String namePrefix) throws EmfException {
        try {
            ControlStrategyInventoryOutputTask task = new ControlStrategyInventoryOutputTask(user, controlStrategy,
                    controlStrategyResults, namePrefix, 
                    entityManagerFactory, dbServerFactory);
            if (task.shouldProceed())
                threadPool.execute(new GCEnforcerTask("Create Inventories: " + controlStrategy.getName(), task));
        } catch (Exception e) {
            LOG.error("Error running control strategy: " + controlStrategy.getName(), e);
            throw new EmfException(e.getMessage());
        }
    }

    public synchronized String controlStrategyRunStatus(int id) throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            return dao.controlStrategyRunStatus(id, entityManager);
        } catch (RuntimeException e) {
            LOG.error("Could not retrieve ControlStrategy Status", e);
            throw new EmfException("Could not retrieve ControlStrategy Status");
        } finally {
            entityManager.close();
        }
    }

    // returns control strategy Id for the given name
    public synchronized int isDuplicateName(String name) throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            ControlStrategy cs = dao.getByName(name, entityManager);
            return cs == null ? 0 : cs.getId();
        } catch (RuntimeException e) {
            LOG.error("Could not retrieve if ControlStrategy name is already used", e);
            throw new EmfException("Could not retrieve if ControlStrategy name is already used");
        } finally {
            entityManager.close();
        }
    }

    public synchronized int copyControlStrategy(int id, User creator) throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            // get cs to copy
            ControlStrategy cs = dao.getById(id, entityManager);

            ControlStrategyConstraint constraint = cs.getConstraint();

            entityManager.clear();// clear to flush current

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

            dao.add(copied, entityManager);
            int csId = copied.getId();
            // FIXME: something is not right with the hibernate mapping, constraint should be added automatically.
            if (constraint != null) {
                constraint.setControlStrategyId(csId);
                dao.add(constraint, entityManager);
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
            entityManager.close();
        }
    }

    private synchronized boolean isDuplicate(String name) throws EmfException {
        return (isDuplicateName(name) != 0);
    }

    public synchronized ControlStrategy getById(int id) throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            return dao.getById(id, entityManager);
        } catch (RuntimeException e) {
            LOG.error("Could not get control strategy", e);
            throw new EmfException("Could not get control strategy");
        } finally {
            entityManager.close();
        }
    }

    public synchronized ControlStrategyResult[] getControlStrategyResults(int controlStrategyId) throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            List all = dao.getControlStrategyResults(controlStrategyId, entityManager);
            return (ControlStrategyResult[]) all.toArray(new ControlStrategyResult[0]);
        } catch (RuntimeException e) {
            LOG.error("Could not retrieve control strategy results.", e);
            throw new EmfException("Could not retrieve control strategy results.");
        } finally {
            entityManager.close();
        }
    }

    public synchronized String getDefaultExportDirectory() throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            String dir = dao.getDefaultExportDirectory(entityManager);
            return dir;
        } catch (RuntimeException e) {
            LOG.error("Could not retrieve default export directory.", e);
            throw new EmfException("Could not retrieve default export directory.");
        } finally {
            entityManager.close();
        }
    }

    public synchronized StrategyResultType[] getOptionalStrategyResultTypes() throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            return dao.getOptionalStrategyResultTypes(entityManager);
        } catch (RuntimeException e) {
            LOG.error("Could not retrieve strategy result types.", e);
            throw new EmfException("Could not retrieve strategy result types.");
        } finally {
            entityManager.close();
        }
    }

    public synchronized String getStrategyRunStatus(int id) throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            return dao.getStrategyRunStatus(entityManager, id);
        } catch (RuntimeException e) {
            LOG.error("Could not retrieve strategy run status.", e);
            throw new EmfException("Could not retrieve strategy run status.");
        } finally {
            entityManager.close();
        }
    }

    public synchronized String getCoSTSUs() throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            EmfProperty property = new EmfPropertiesDAO().getProperty("COST_SU", entityManager);
            
            return (property != null ? property.getValue() : "");
        } finally {
            entityManager.close();
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
    
    public String getControlStrategySummary(int[] controlStrategyIds) throws EmfException {
        DbServer dbServer = dbServerFactory.getDbServer();
        try {
            return new QueryToString(dbServer, new SQLSummarizeControlStrategiesQuery().createSummarizeQuery(controlStrategyIds), ",").toString();
        } catch (RuntimeException e) {
            throw new EmfException("Could not retrieve control strategy summary: " + e.getMessage(), e);
        } catch (ExporterException e) {
            throw new EmfException("Could not retrieve control strategy summary: " + e.getMessage(), e);
        } finally {
            try {
                if (dbServer != null && dbServer.isConnected())
                    dbServer.disconnect();
            } catch (Exception e) {
                throw new EmfException("ControlStrategyService: error closing db server. " + e.getMessage());
            }
        }
    }

    public synchronized StrategyGroup[] getStrategyGroups() throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            List groups = dao.getAllStrategyGroups(entityManager);
            return (StrategyGroup[]) groups.toArray(new StrategyGroup[0]);
        } catch (HibernateException e) {
            LOG.error("Could not retrieve all control strategy groups. " + e.getMessage());
            throw new EmfException("Could not retrieve all control strategy groups. " + e.getMessage());
        } finally {
            entityManager.close();
        }
    }

    public synchronized StrategyGroup obtainLockedGroup(User owner, int id) throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            StrategyGroup locked = dao.obtainLockedGroup(owner, id, entityManager);
            return locked;
        } catch (RuntimeException e) {
            LOG.error("Could not obtain lock for Strategy Group: id = " + id + " by owner: "
                            + owner.getUsername(), e);
            throw new EmfException("Could not obtain lock for Strategy Group: id = " + id + " by owner: "
                    + owner.getUsername());
        } finally {
            entityManager.close();
        }
    }

    public synchronized void releaseLockedGroup(User user, int id) throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            dao.releaseLockedGroup(user, id, entityManager);
        } catch (RuntimeException e) {
            LOG.error("Could not release lock for Strategy Group id: " + id, e);
            throw new EmfException("Could not release lock for Strategy Group id: " + id);
        } finally {
            entityManager.close();
        }
    }

    public synchronized int addStrategyGroup(StrategyGroup group) throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        int groupId;
        try {
            groupId = dao.addGroup(group, entityManager);
        } catch (RuntimeException e) {
            LOG.error("Could not add Control Strategy Group: " + group, e);
            throw new EmfException("Could not add Control Strategy Group: " + group);
        } finally {
            entityManager.close();
        }
        return groupId;
    }

    public synchronized StrategyGroup updateStrategyGroupWithLock(StrategyGroup group) throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            if (!dao.canUpdateGroup(group, entityManager))
                throw new EmfException("Control Strategy Group name already in use");

            StrategyGroup groupWithLock = dao.updateGroupWithLock(group, entityManager);

            return groupWithLock;
        } catch (RuntimeException e) {
            LOG.error("Could not update Control Strategy Group: " + group, e);
            throw new EmfException("Could not update Control Strategy Group: " + group);
        } finally {
            entityManager.close();
        }
    }

    public synchronized int isDuplicateGroupName(String name) throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            StrategyGroup group = dao.getGroupByName(name, entityManager);
            return group == null ? 0 : group.getId();
        } catch (RuntimeException e) {
            LOG.error("Could not determine if Control Strategy Group name is already used", e);
            throw new EmfException("Could not determine if Control Strategy Group name is already used");
        } finally {
            entityManager.close();
        }
    }

    public synchronized void removeStrategyGroups(int[] ids, User user) throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        String exception = "";
        try {
            for (int i = 0; i < ids.length; i++) {
                StrategyGroup group = dao.getGroupById(ids[i], entityManager);
                entityManager.clear();

                // check if admin user, then allow it to be removed.
//                if (user.equals(cs.getCreator()) || user.isAdmin()) {
//                    if (cs.isLocked())
//                        exception += "The control strategy, " + cs.getName()
//                                + ", is in edit mode and can not be removed. ";
//                    else
                        removeGroup(group);
//                } else {
//                    exception += "You do not have permission to remove the strategy: " + cs.getName() + ". ";
//                }
            }

            if (exception.length() > 0)
                throw new EmfException(exception);
        } catch (RuntimeException e) {
            LOG.error("Could not remove Control Strategy Group", e);
            throw new EmfException("Could not remove Control Strategy Group");
        } finally {
            entityManager.close();
        }
    }

    private synchronized void removeGroup(StrategyGroup group) throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            if (!dao.canUpdateGroup(group, entityManager))
                throw new EmfException("Control Strategy Group doesn't exist.");

            dao.removeGroup(group, entityManager);
        } catch (RuntimeException e) {
            LOG.error("Could not remove Control Strategy Group: " + group, e);
            throw new EmfException("Could not remove Control Strategy Group: " + group.getName());
        } finally {
            entityManager.close();
        }
    }
}
