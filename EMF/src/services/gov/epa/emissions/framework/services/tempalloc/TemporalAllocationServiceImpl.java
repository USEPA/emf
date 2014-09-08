package gov.epa.emissions.framework.services.tempalloc;

import java.util.Date;
import java.util.List;

import gov.epa.emissions.commons.io.DeepCopy;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Session;

import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;

public class TemporalAllocationServiceImpl implements TemporalAllocationService {

    private static Log LOG = LogFactory.getLog(TemporalAllocationServiceImpl.class);

    private PooledExecutor threadPool;

    private HibernateSessionFactory sessionFactory;

    protected DbServerFactory dbServerFactory;
    
    private TemporalAllocationDAO dao;
    
    public TemporalAllocationServiceImpl() throws Exception {
        init(HibernateSessionFactory.get(), DbServerFactory.get());
    }
    
    public TemporalAllocationServiceImpl(HibernateSessionFactory sessionFactory, DbServerFactory dbServerFactory) throws Exception {
        init(sessionFactory, dbServerFactory);
    }
    
    private synchronized void init(HibernateSessionFactory sessionFactory, DbServerFactory dbServerFactory) {
        this.sessionFactory = sessionFactory;
        this.dbServerFactory = dbServerFactory;
        dao = new TemporalAllocationDAO(dbServerFactory, sessionFactory);
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
        Session session = sessionFactory.getSession();
        try {
            return dao.getById(id, session);
        } catch (RuntimeException e) {
            LOG.error("Could not get Temporal Allocation", e);
            throw new EmfException("Could not get Temporal Allocation");
        } finally {
            session.close();
        }
    }

    public synchronized TemporalAllocation[] getTemporalAllocations() throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            List items = dao.all(session);
            System.err.println(items.size());
            return (TemporalAllocation[]) items.toArray(new TemporalAllocation[0]);
        } catch (HibernateException e) {
            LOG.error("Could not retrieve all temporal allocations.");
            throw new EmfException("Could not retrieve all temporal allocations.");
        } finally {
            session.close();
        }
    }
    
    public synchronized TemporalAllocationOutput[] getTemporalAllocationOutputs(TemporalAllocation element) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            List all = dao.getTemporalAllocationOutputs(element.getId(), session);
            return (TemporalAllocationOutput[]) all.toArray(new TemporalAllocationOutput[0]);
        } catch (RuntimeException e) {
            LOG.error("Could not retrieve Temporal Allocation results.", e);
            throw new EmfException("Could not retrieve Temporal Allocation results.");
        } finally {
            session.close();
        }
    }
    
    public synchronized int addTemporalAllocation(TemporalAllocation element) throws EmfException {
        Session session = sessionFactory.getSession();
        int elementId;
        try {
            elementId = dao.add(element, session);
        } catch (RuntimeException e) {
            LOG.error("Could not add Temporal Allocation: " + element, e);
            throw new EmfException("Could not add Temporal Allocation: " + element);
        } finally {
            session.close();
        }
        return elementId;
    }
    
    public synchronized int copyTemporalAllocation(TemporalAllocation element, User creator) throws EmfException {
        Session session = sessionFactory.getSession();
        int elementId;
        try {
            session.clear();// clear to flush current

            String name = "Copy of " + element.getName();
            // make sure this won't cause duplicate issues...
            if (isDuplicateName(name)) {
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

            elementId = dao.add(copy, session);
        } catch (EmfException e) {
            LOG.error(e.getMessage());
            throw e;
        } catch (Exception e) {
            LOG.error("Could not copy temporal allocation", e);
            throw new EmfException("Could not copy temporal allocation");
        } finally {
            session.close();
        }
        return elementId;
    }
    
    public synchronized void setRunStatusAndCompletionDate(TemporalAllocation element, String runStatus, Date completionDate) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            dao.setRunStatusAndCompletionDate(element, runStatus, completionDate, session);
        } catch (RuntimeException e) {
            LOG.error("Could not set Temporal Allocation run status: " + element, e);
            throw new EmfException("Could not add Temporal Allocation run status: " + element);
        } finally {
            session.close();
        }
    }
    
    public synchronized TemporalAllocation obtainLocked(User owner, int id) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            TemporalAllocation locked = dao.obtainLocked(owner, id, session);
            
            return locked;
        } catch (RuntimeException e) {
            LOG.error("Could not obtain lock for Temporal Allocation: id = " + id + " by owner: " + owner.getUsername(), e);
            throw new EmfException("Could not obtain lock for Temporal Allocation: id = " + id + " by owner: " + owner.getUsername());
        } finally {
            session.close();
        }
    }

    public synchronized void releaseLocked(User user, int id) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            dao.releaseLocked(user, id, session);
        } catch (RuntimeException e) {
            LOG.error("Could not release lock for Temporal Allocation id: " + id, e);
            throw new EmfException("Could not release lock for Temporal Allocation id: " + id);
        } finally {
            session.close();
        }
    }
    
    public synchronized TemporalAllocation updateTemporalAllocationWithLock(TemporalAllocation element) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            if (!dao.canUpdate(element, session))
                throw new EmfException("Temporal Allocation name already in use");
            
            TemporalAllocation elementWithLock = dao.updateWithLock(element, session);
            
            return elementWithLock;
        } catch (RuntimeException e) {
            LOG.error("Could not update Temporal Allocation: " + element, e);
            throw new EmfException("Could not update Temporal Allocation: " + element);
        } finally {
            session.close();
        }
    }

    public synchronized TemporalAllocationResolution[] getResolutions() throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            List all = dao.allResolutions(session);
            return (TemporalAllocationResolution[]) all.toArray(new TemporalAllocationResolution[0]);
        } catch (RuntimeException e) {
            LOG.error("Could not retrieve temporal allocation resolutions.", e);
            throw new EmfException("Could not retrieve temporal allocation resolutions.");
        } finally {
            session.close();
        }
    }

    public synchronized void removeTemporalAllocations(int[] ids, User user) throws EmfException {
        Session session = sessionFactory.getSession();
        String exception = "";
        try {
            for (int i = 0; i < ids.length; i++) {
                TemporalAllocation item = dao.getById(ids[i], session);
                session.clear();

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
            session.close();
        }
    }

    private synchronized void remove(TemporalAllocation element) throws EmfException {
        Session session = sessionFactory.getSession();
        try {

            if (!dao.canUpdate(element, session))
                throw new EmfException("Temporal Allocation doesn't exist.");

            TemporalAllocationOutput[] outputs = getTemporalAllocationOutputs(element);
            for (int i = 0; i < outputs.length; i++) {
                dao.remove(outputs[i], session);
            }

            dao.remove(element, session);
        } catch (RuntimeException e) {
            LOG.error("Could not remove temporal allocation: " + element, e);
            throw new EmfException("Could not remove temporal allocation: " + element.getName());
        } finally {
            session.close();
        }
    }
    
    public void runTemporalAllocation(User user, TemporalAllocation element) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            // check if temporal allocation is already running
            String runStatus = element.getRunStatus();
            if (runStatus.equals("Running")) {
                return;
            }
            
            dao.setRunStatusAndCompletionDate(element, "Waiting", null, session);

            RunTemporalAllocation runTemporalAllocation = new RunTemporalAllocation(sessionFactory, dbServerFactory, threadPool);
            runTemporalAllocation.run(user, element, this);
        } catch (EmfException e) {
            dao.setRunStatusAndCompletionDate(element, "Failed", null, session);
            
            throw new EmfException(e.getMessage());
        } finally {
            session.close();
        }
    }
    
    public List<TemporalAllocation> getTemporalAllocationsByRunStatus(String runStatus) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            return dao.getTemporalAllocationsByRunStatus(runStatus, session);
        } catch (RuntimeException e) {
            throw new EmfException("Could not get Temporal Allocations by run status: " + runStatus);
        } finally {
            session.close();
        }
    }

    public Long getTemporalAllocationRunningCount() throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            return dao.getTemporalAllocationRunningCount(session);
        } catch (RuntimeException e) {
            throw new EmfException("Could not get Temporal Allocation running count");
        } finally {
            session.close();
        }
    }
    
    public synchronized boolean isDuplicateName(String name) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            TemporalAllocation ta = dao.getByName(name, session);
            return ta != null;
        } catch (RuntimeException e) {
            LOG.error("Could not determine if Temporal Allocation name is already used", e);
            throw new EmfException("Could not determine if Temporal Allocation name is already used");
        } finally {
            session.close();
        }
    }
}
