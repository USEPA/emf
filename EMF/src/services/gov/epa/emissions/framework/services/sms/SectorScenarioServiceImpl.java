package gov.epa.emissions.framework.services.sms;

import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.io.DeepCopy;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.persistence.JpaEntityManagerFactory;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;

import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;

public class SectorScenarioServiceImpl implements SectorScenarioService {

    private static Log LOG = LogFactory.getLog(SectorScenarioServiceImpl.class);

    private PooledExecutor threadPool;

    private EntityManagerFactory entityManagerFactory;

    protected DbServerFactory dbServerFactory;

    private SectorScenarioDAO dao;

    public SectorScenarioServiceImpl() throws Exception {
        init(JpaEntityManagerFactory.get(), DbServerFactory.get());
    }

    public SectorScenarioServiceImpl(EntityManagerFactory entityManagerFactory, DbServerFactory dbServerFactory)
            throws Exception {
        init(entityManagerFactory, dbServerFactory);
    }

    private synchronized void init(EntityManagerFactory entityManagerFactory, DbServerFactory dbServerFactory) {
        this.entityManagerFactory = entityManagerFactory;
        this.dbServerFactory = dbServerFactory;
        dao = new SectorScenarioDAO(dbServerFactory, entityManagerFactory);
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

    public synchronized SectorScenario[] getSectorScenarios() throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            List cs = dao.all(entityManager);
            return (SectorScenario[]) cs.toArray(new SectorScenario[0]);
        } catch (HibernateException e) {
            LOG.error("Could not retrieve all sector scenarios." + e.getMessage());
            throw new EmfException("Could not retrieve all sector scenarios.");
        } finally {
            entityManager.close();
        }
    }

    public synchronized int addSectorScenario(SectorScenario element) throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        int csId;
        try {
            csId = dao.add(element, entityManager);
        } catch (RuntimeException e) {
            LOG.error("Could not add Sector Scenario: " + element, e);
            throw new EmfException("Could not add Sector Scenario: " + element);
        } finally {
            entityManager.close();
        }
        return csId;
    }

    public synchronized void setSectorScenarioRunStatusAndCompletionDate(int id, String runStatus, Date completionDate)
            throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            dao.setSectorScenarioRunStatusAndCompletionDate(id, runStatus, completionDate, entityManager);
        } catch (RuntimeException e) {
            LOG.error("Could not set Control Strategy run status: " + id, e);
            throw new EmfException("Could not add Control Strategy run status: " + id);
        } finally {
            entityManager.close();
        }
    }

    public synchronized SectorScenario obtainLocked(User owner, int id) throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            SectorScenario locked = dao.obtainLocked(owner, id, entityManager);

            return locked;
        } catch (Exception e) {
            LOG.error("Could not obtain lock for Sector Scenario: id = " + id + " by owner: " + owner.getUsername(), e);
            throw new EmfException("Could not obtain lock for Sector Scenario: id = " + id + " by owner: "
                    + owner.getUsername());
        } finally {
            entityManager.close();
        }
    }

    public synchronized void releaseLocked(User user, int id) throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            dao.releaseLocked(user, id, entityManager);
        } catch (RuntimeException e) {
            LOG.error("Could not release lock for Sector Scenario id: " + id, e);
            throw new EmfException("Could not release lock for Sector Scenario id: " + id);
        } finally {
            entityManager.close();
        }
    }

    public synchronized SectorScenario updateSectorScenario(SectorScenario element) throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            if (!dao.canUpdate(element, entityManager))
                throw new EmfException("The Sector Scenario name is already in use");

            SectorScenario released = dao.update(element, entityManager);

            return released;
        } catch (RuntimeException e) {
            LOG.error("Could not update Sector Scenario: " + element, e);
            throw new EmfException("Could not update SectorScenario: " + element);
        } finally {
            entityManager.close();
        }
    }

    public synchronized SectorScenario updateSectorScenarioWithLock(SectorScenario element) throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            if (!dao.canUpdate(element, entityManager))
                throw new EmfException("Sector Scenario name already in use");

            SectorScenario csWithLock = dao.updateWithLock(element, entityManager);

            return csWithLock;
        } catch (RuntimeException e) {
            LOG.error("Could not update Sector Scenario: " + element, e);
            throw new EmfException("Could not update Sector Scenario: " + element);
        } finally {
            entityManager.close();
        }
    }

    public synchronized void removeSectorScenarios(int[] ids, User user) throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        String exception = "";
        try {
            for (int i = 0; i < ids.length; i++) {
                SectorScenario cs = dao.getById(ids[i], entityManager);
                entityManager.clear();

                // check if admin user, then allow it to be removed.
                if (user.equals(cs.getCreator()) || user.isAdmin()) {
                    if (cs.isLocked())
                        exception += "The Sector Scenario, " + cs.getName()
                                + ", is in edit mode and can not be removed. ";
                    else
                        remove(cs, user);
                } else {
                    exception += "You do not have permission to remove the Sector Scenario: " + cs.getName() + ". ";
                }
            }

            if (exception.length() > 0)
                throw new EmfException(exception);
        } catch (RuntimeException e) {
            LOG.error("Could not remove Sector Scenario", e);
            throw new EmfException("Could not remove Sector Scenario");
        } finally {
            entityManager.close();
        }
    }

    private synchronized void remove(SectorScenario element, User user) throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        DbServer dbServer = dbServerFactory.getDbServer();
        try {

            if (!dao.canUpdate(element, entityManager))
                throw new EmfException("Sector Scenario doesn't exist.");

            dao.removeSectorScenarioResults(element.getId(), user, entityManager, dbServer);
            // SectorScenarioOutput[] sectorScenarioOutputs = getSectorScenarioOutputs(element.getId());
            // for (int i = 0; i < sectorScenarioOutputs.length; i++) {
            // dao.remove(sectorScenarioOutputs[i], entityManager);
            // }

            dao.remove(element, entityManager);
        } catch (RuntimeException e) {
            LOG.error("Could not remove Sector Scenario: " + element, e);
            throw new EmfException("Could not remove Sector Scenario: " + element.getName());
        } finally {
            entityManager.close();
            try {
                dbServer.disconnect();
            } catch (Exception e) {
                throw new EmfException("Could not disconnect DbServer - " + e.getMessage());
            }
        }
    }

    public synchronized void runSectorScenario(User user, int sectorScenarioId) throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            // first see if the strategy has been canceled cor is running, is so don't run it...
            String runStatus = dao.getSectorScenarioRunStatus(sectorScenarioId, entityManager);
            if (//runStatus.equals("Cancelled") || 
                    runStatus.equals("Running"))
                return;

            SectorScenario sectorScenario = getById(sectorScenarioId);
            // validateSectors(strategy);
            // get rid of for now, since we don't auto export anything
            // make sure a valid server-side export path was specified
            // validateExportPath(strategy.getExportDirectory());

            // make the runner of the strategy is the owner of the strategy...
            // NEED TO TALK TO ALISON ABOUT ISSUES, LOCEKD owner might not be the creator of resulting datsets,
            // hence a exception when trying to purge/delete the resulting datasets
            // if (control);

            String preRunStatus = dao.getSectorScenarioRunStatus(sectorScenarioId, entityManager);
            // queue up the strategy to be run, by setting runStatus to Waiting
            dao.setSectorScenarioRunStatusAndCompletionDate(sectorScenarioId, "Waiting", null, entityManager);

            // validatePath(strategy.getExportDirectory());
            RunSectorScenario runSectorScenario = new RunSectorScenario(entityManagerFactory, dbServerFactory, threadPool);
            runSectorScenario.run(user, sectorScenario, this, preRunStatus);
        } catch (EmfException e) {
            // queue up the strategy to be run, by setting runStatus to Waiting
            dao.setSectorScenarioRunStatusAndCompletionDate(sectorScenarioId, "Failed", null, entityManager);

            throw new EmfException(e.getMessage());
        } finally {
            entityManager.close();
        }
    }

    public List<SectorScenario> getSectorScenariosByRunStatus(String runStatus) throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            return dao.getSectorScenariosByRunStatus(runStatus, entityManager);
        } catch (RuntimeException e) {
            throw new EmfException("Could not get Sector Scenarioes by run status: " + runStatus);
        } finally {
            entityManager.close();
        }
    }

    public Long getSectorScenarioRunningCount() throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            return dao.getSectorScenarioRunningCount(entityManager);
        } catch (RuntimeException e) {
            throw new EmfException("Could not get Sector Scenarioes running count");
        } finally {
            entityManager.close();
        }
    }

    public synchronized void stopRunSectorScenario(int sectorScenarioId) throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            // look at the current status, if waiting or running, then update to Cancelled.
            String status = dao.getSectorScenarioRunStatus(sectorScenarioId, entityManager);
            if (status.toLowerCase().startsWith("waiting") || status.toLowerCase().startsWith("running"))
                dao.setSectorScenarioRunStatusAndCompletionDate(sectorScenarioId, "Cancelled", null, entityManager);
        } catch (RuntimeException e) {
            LOG.error("Could not set Sector Scenario run status: " + sectorScenarioId, e);
            throw new EmfException("Could not add Sector Scenario run status: " + sectorScenarioId);
        } finally {
            entityManager.close();
        }
    }

    public synchronized String sectorScenarioRunStatus(int id) throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            return dao.sectorScenarioRunStatus(id, entityManager);
        } catch (RuntimeException e) {
            LOG.error("Could not retrieve Sector Scenario Status", e);
            throw new EmfException("Could not retrieve Sector Scenario Status");
        } finally {
            entityManager.close();
        }
    }

    // returns Sector Scenario Id for the given name
    public synchronized int isDuplicateName(String name) throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            SectorScenario cs = dao.getByName(name, entityManager);
            return cs == null ? 0 : cs.getId();
        } catch (RuntimeException e) {
            LOG.error("Could not retrieve if Sector Scenario name is already used", e);
            throw new EmfException("Could not retrieve if Sector Scenario name is already used");
        } finally {
            entityManager.close();
        }
    }

    public synchronized int isDuplicateAbbre(String abbre) throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            SectorScenario cs = dao.getByAbbre(abbre, entityManager);
            return cs == null ? 0 : cs.getId();
        } catch (RuntimeException e) {
            LOG.error("Could not retrieve if Sector Scenario abbreviation is already used", e);
            throw new EmfException("Could not retrieve if Sector Scenario abbreviation is already used");
        } finally {
            entityManager.close();
        }
    }

    public synchronized int copySectorScenario(int id, User creator) throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            // get cs to copy
            SectorScenario cs = dao.getById(id, entityManager);

            entityManager.clear();// clear to flush current

            String name = "Copy of " + cs.getName();
            // make sure this won't cause duplicate issues...
            if (isDuplicate(name))
                throw new EmfException("A Sector Scenario named '" + name + "' already exists.");

            // do a deep copy
            SectorScenario copied = (SectorScenario) DeepCopy.copy(cs);
            // change to applicable values
            copied.setName(name);
            // make up the abbreviation for now...
            copied.setAbbreviation(CustomDateFormat.format_YYYYMMDDHHMMSSSS(new Date()));
            copied.setCreator(creator);
            copied.setLastModifiedDate(new Date());
            copied.setRunStatus("Not started");
            copied.setCopiedFrom(cs.getName());
            if (copied.isLocked()) {
                copied.setLockDate(null);
                copied.setLockOwner(null);
            }

            dao.add(copied, entityManager);
            int csId = copied.getId();
            return csId;
        } catch (EmfException e) {
            LOG.error(e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            LOG.error("Could not copy Sector Scenario", e);
            throw new EmfException("Could not copy Sector Scenario");
        } catch (Exception e) {
            LOG.error("Could not copy Sector Scenario", e);
            throw new EmfException("Could not copy Sector Scenario");
        } finally {
            entityManager.close();
        }
    }

    private synchronized boolean isDuplicate(String name) throws EmfException {
        return (isDuplicateName(name) != 0);
    }

    public synchronized SectorScenario getById(int id) throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            return dao.getById(id, entityManager);
        } catch (RuntimeException e) {
            LOG.error("Could not get Sector Scenario", e);
            throw new EmfException("Could not get Sector Scenario");
        } finally {
            entityManager.close();
        }
    }

    public synchronized SectorScenarioOutput[] getSectorScenarioOutputs(int sectorScenarioId) throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            List all = dao.getSectorScenarioOutputs(sectorScenarioId, entityManager);
            return (SectorScenarioOutput[]) all.toArray(new SectorScenarioOutput[0]);
        } catch (RuntimeException e) {
            LOG.error("Could not retrieve Sector Scenario results.", e);
            throw new EmfException("Could not retrieve Sector Scenario results.");
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

    public synchronized String[] getDistinctSectorListFromDataset(int datasetId, int versionNumber) throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        DbServer dbServer = dbServerFactory.getDbServer();
        try {
            return dao.getDistinctSectorListFromDataset(entityManager, dbServer, datasetId, versionNumber);
        } catch (RuntimeException e) {
            LOG.error("Could not retrieve distinct sector list from a dataset.", e);
            throw new EmfException("Could not retrieve distinct sector list from a dataset.");
        } finally {
            try {
                entityManager.close();
            } catch (Exception e) {
                throw new EmfException("Could not close hibernate entityManager - " + e.getMessage());
            }
            try {
                dbServer.disconnect();
            } catch (Exception e) {
                throw new EmfException("Could not disconnect DbServer - " + e.getMessage());
            }
        }
    }
}
