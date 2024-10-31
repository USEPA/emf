package gov.epa.emissions.framework.services.tempalloc;

import gov.epa.emissions.commons.io.DeepCopy;
import gov.epa.emissions.commons.security.User;
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

public class TemporalAllocationServiceImpl implements TemporalAllocationService {

    private static Log LOG = LogFactory.getLog(TemporalAllocationServiceImpl.class);

    private PooledExecutor threadPool;

    private EntityManagerFactory entityManagerFactory;

    protected DbServerFactory dbServerFactory;
    
    private TemporalAllocationDAO dao;
    
    public TemporalAllocationServiceImpl() throws Exception {
        init(JpaEntityManagerFactory.get(), DbServerFactory.get());
    }
    
    public TemporalAllocationServiceImpl(EntityManagerFactory entityManagerFactory, DbServerFactory dbServerFactory) throws Exception {
        init(entityManagerFactory, dbServerFactory);
    }
    
    private synchronized void init(EntityManagerFactory entityManagerFactory, DbServerFactory dbServerFactory) {
        this.entityManagerFactory = entityManagerFactory;
        this.dbServerFactory = dbServerFactory;
        dao = new TemporalAllocationDAO(dbServerFactory, entityManagerFactory);
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
    
    public synchronized TemporalAllocation getById(int id) throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            return dao.getById(id, entityManager);
        } catch (RuntimeException e) {
            LOG.error("Could not get Temporal Allocation", e);
            throw new EmfException("Could not get Temporal Allocation");
        } finally {
            entityManager.close();
        }
    }

    public synchronized TemporalAllocation[] getTemporalAllocations() throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            List items = dao.all(entityManager);
            System.err.println(items.size());
            return (TemporalAllocation[]) items.toArray(new TemporalAllocation[0]);
        } catch (HibernateException e) {
            LOG.error("Could not retrieve all temporal allocations.");
            throw new EmfException("Could not retrieve all temporal allocations.");
        } finally {
            entityManager.close();
        }
    }
    
    public synchronized TemporalAllocationOutput[] getTemporalAllocationOutputs(TemporalAllocation element) throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            List all = dao.getTemporalAllocationOutputs(element.getId(), entityManager);
            return (TemporalAllocationOutput[]) all.toArray(new TemporalAllocationOutput[0]);
        } catch (RuntimeException e) {
            LOG.error("Could not retrieve Temporal Allocation results.", e);
            throw new EmfException("Could not retrieve Temporal Allocation results.");
        } finally {
            entityManager.close();
        }
    }
    
    public synchronized int addTemporalAllocation(TemporalAllocation element) throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        int elementId;
        try {
            elementId = dao.add(element, entityManager);
        } catch (RuntimeException e) {
            LOG.error("Could not add Temporal Allocation: " + element, e);
            throw new EmfException("Could not add Temporal Allocation: " + element);
        } finally {
            entityManager.close();
        }
        return elementId;
    }
    
    public synchronized int copyTemporalAllocation(TemporalAllocation element, User creator) throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        int elementId;
        try {
            entityManager.clear();// clear to flush current

            String name = "Copy of " + element.getName();
            // make sure this won't cause duplicate issues...
            if (isDuplicate(name)) {
                throw new EmfException("A Temporal Allocation named '" + name + "' already exists.");
            }
            
            TemporalAllocation copy = (TemporalAllocation) DeepCopy.copy(element);
            copy.setName(name);
            copy.setCreator(creator);
            copy.setLastModifiedDate(new Date());
            copy.setRunStatus("Not started");
            copy.setStartDate(null);
            copy.setCompletionDate(null);

            if (copy.isLocked()) {
                copy.setLockDate(null);
                copy.setLockOwner(null);
            }

            elementId = dao.add(copy, entityManager);
        } catch (EmfException e) {
            LOG.error(e.getMessage());
            throw e;
        } catch (Exception e) {
            LOG.error("Could not copy temporal allocation", e);
            throw new EmfException("Could not copy temporal allocation");
        } finally {
            entityManager.close();
        }
        return elementId;
    }
    
    public synchronized void setRunStatusAndCompletionDate(TemporalAllocation element, String runStatus, Date completionDate) throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            dao.setRunStatusAndCompletionDate(element, runStatus, completionDate, entityManager);
        } catch (RuntimeException e) {
            LOG.error("Could not set Temporal Allocation run status: " + element, e);
            throw new EmfException("Could not add Temporal Allocation run status: " + element);
        } finally {
            entityManager.close();
        }
    }
    
    public synchronized TemporalAllocation obtainLocked(User owner, int id) throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            TemporalAllocation locked = dao.obtainLocked(owner, id, entityManager);
            
            return locked;
        } catch (RuntimeException e) {
            LOG.error("Could not obtain lock for Temporal Allocation: id = " + id + " by owner: " + owner.getUsername(), e);
            throw new EmfException("Could not obtain lock for Temporal Allocation: id = " + id + " by owner: " + owner.getUsername());
        } finally {
            entityManager.close();
        }
    }

    public synchronized void releaseLocked(User user, int id) throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            dao.releaseLocked(user, id, entityManager);
        } catch (RuntimeException e) {
            LOG.error("Could not release lock for Temporal Allocation id: " + id, e);
            throw new EmfException("Could not release lock for Temporal Allocation id: " + id);
        } finally {
            entityManager.close();
        }
    }
    
    public synchronized TemporalAllocation updateTemporalAllocationWithLock(TemporalAllocation element) throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            if (!dao.canUpdate(element, entityManager))
                throw new EmfException("Temporal Allocation name already in use");
            
            TemporalAllocation elementWithLock = dao.updateWithLock(element, entityManager);
            
            return elementWithLock;
        } catch (RuntimeException e) {
            LOG.error("Could not update Temporal Allocation: " + element, e);
            throw new EmfException("Could not update Temporal Allocation: " + element);
        } finally {
            entityManager.close();
        }
    }

    public synchronized TemporalAllocationResolution[] getResolutions() throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            List all = dao.allResolutions(entityManager);
            return (TemporalAllocationResolution[]) all.toArray(new TemporalAllocationResolution[0]);
        } catch (RuntimeException e) {
            LOG.error("Could not retrieve temporal allocation resolutions.", e);
            throw new EmfException("Could not retrieve temporal allocation resolutions.");
        } finally {
            entityManager.close();
        }
    }

    public synchronized void removeTemporalAllocations(int[] ids, User user) throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        String exception = "";
        try {
            for (int i = 0; i < ids.length; i++) {
                TemporalAllocation item = dao.getById(ids[i], entityManager);
                entityManager.clear();

                // check if admin user, then allow it to be removed.
                if (user.equals(item.getCreator()) || user.isAdmin()) {
                    if (item.isLocked())
                        exception += "The Temporal Allocation, " + item.getName()
                                + ", is in edit mode and can not be removed. ";
                    else
                        remove(item);
                } else {
                    exception += "You do not have permission to remove the Temporal Allocation: " + item.getName() + ". ";
                }
            }

            if (exception.length() > 0)
                throw new EmfException(exception);
        } catch (RuntimeException e) {
            LOG.error("Could not remove Temporal Allocation", e);
            throw new EmfException("Could not remove Temporal Allocation");
        } finally {
            entityManager.close();
        }
    }

    private synchronized void remove(TemporalAllocation element) throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {

            if (!dao.canUpdate(element, entityManager))
                throw new EmfException("Temporal Allocation doesn't exist.");

            TemporalAllocationOutput[] outputs = getTemporalAllocationOutputs(element);
            for (int i = 0; i < outputs.length; i++) {
                dao.remove(outputs[i], entityManager);
            }

            dao.remove(element, entityManager);
        } catch (RuntimeException e) {
            LOG.error("Could not remove temporal allocation: " + element, e);
            throw new EmfException("Could not remove temporal allocation: " + element.getName());
        } finally {
            entityManager.close();
        }
    }
    
    public void runTemporalAllocation(User user, TemporalAllocation element) throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            // check if temporal allocation is already running
            String runStatus = dao.getTemporalAllocationRunStatus(element.getId(), entityManager);
            if (runStatus.equals("Running")) {
                return;
            }
            
            dao.setRunStatusAndCompletionDate(element, "Waiting", null, entityManager);

            RunTemporalAllocation runTemporalAllocation = new RunTemporalAllocation(entityManagerFactory, dbServerFactory, threadPool);
            runTemporalAllocation.run(user, element, this);
        } catch (EmfException e) {
            dao.setRunStatusAndCompletionDate(element, "Failed", null, entityManager);
            
            throw new EmfException(e.getMessage());
        } finally {
            entityManager.close();
        }
    }
    
    public void stopTemporalAllocation(TemporalAllocation element) throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            String runStatus = dao.getTemporalAllocationRunStatus(element.getId(), entityManager);
            if (runStatus.equals("Waiting") || runStatus.equals("Running")) {
                dao.setRunStatusAndCompletionDate(element, "Pending Cancel", null, entityManager);
            }
        } catch (RuntimeException e) {
            LOG.error("Could not set Temporal Allocation run status: " + element.getId(), e);
            throw new EmfException("Could not set Temporal Allocation run status: " + element.getId());
        } finally {
            entityManager.close();
        }
    }
    
    public List<TemporalAllocation> getTemporalAllocationsByRunStatus(String runStatus) throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            return dao.getTemporalAllocationsByRunStatus(runStatus, entityManager);
        } catch (RuntimeException e) {
            throw new EmfException("Could not get Temporal Allocations by run status: " + runStatus);
        } finally {
            entityManager.close();
        }
    }

    public Long getTemporalAllocationRunningCount() throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            return dao.getTemporalAllocationRunningCount(entityManager);
        } catch (RuntimeException e) {
            throw new EmfException("Could not get Temporal Allocation running count");
        } finally {
            entityManager.close();
        }
    }
    
    public synchronized int isDuplicateName(String name) throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            TemporalAllocation ta = dao.getByName(name, entityManager);
            return ta == null ? 0 : ta.getId();
        } catch (RuntimeException e) {
            LOG.error("Could not determine if Temporal Allocation name is already used", e);
            throw new EmfException("Could not determine if Temporal Allocation name is already used");
        } finally {
            entityManager.close();
        }
    }

    private synchronized boolean isDuplicate(String name) throws EmfException {
        return (isDuplicateName(name) != 0);
    }
    
    public synchronized String getTemporalAllocationRunStatus(int id) throws EmfException {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            return dao.getTemporalAllocationRunStatus(id, entityManager);
        } catch (RuntimeException e) {
            LOG.error("Could not retrieve temporal allocation run status.", e);
            throw new EmfException("Could not retrieve temporal allcation run status.");
        } finally {
            entityManager.close();
        }
    }
}
