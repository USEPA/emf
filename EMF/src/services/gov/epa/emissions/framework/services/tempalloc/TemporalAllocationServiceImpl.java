package gov.epa.emissions.framework.services.tempalloc;

import java.util.List;

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
    
    private TemporalAllocationDAO dao;
    
    public TemporalAllocationServiceImpl() throws Exception {
        init(HibernateSessionFactory.get(), DbServerFactory.get());
    }
    
    public TemporalAllocationServiceImpl(HibernateSessionFactory sessionFactory, DbServerFactory dbServerFactory) throws Exception {
        init(sessionFactory, dbServerFactory);
    }
    
    private synchronized void init(HibernateSessionFactory sessionFactory, DbServerFactory dbServerFactory) {
        this.sessionFactory = sessionFactory;
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
}
