package gov.epa.emissions.framework.services.tempalloc;

import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.DataServiceImpl;
import gov.epa.emissions.framework.services.data.DatasetDAO;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.DataServiceImpl.DeleteType;
import gov.epa.emissions.framework.services.persistence.HibernateFacade;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;
import gov.epa.emissions.framework.services.persistence.LockingScheme;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

public class TemporalAllocationDAO {
    private LockingScheme lockingScheme;

    private HibernateFacade hibernateFacade;

    private HibernateSessionFactory sessionFactory;

    private DbServerFactory dbServerFactory;
    
    private DatasetDAO datasetDao;
    
    public TemporalAllocationDAO() {
        lockingScheme = new LockingScheme();
        hibernateFacade = new HibernateFacade();
        datasetDao = new DatasetDAO();
    }
    
    public TemporalAllocationDAO(DbServerFactory dbServerFactory, HibernateSessionFactory sessionFactory) {
        this();
        this.dbServerFactory = dbServerFactory;
        this.sessionFactory = sessionFactory;
    }

    public int add(TemporalAllocation element, Session session) {
        return addObject(element, session);
    }

    private int addObject(Object obj, Session session) {
        return (Integer)hibernateFacade.add(obj, session);
    }

    public List all(Session session) {
        return hibernateFacade.getAll(TemporalAllocation.class, Order.asc("name"), session);
    }

    public TemporalAllocation obtainLocked(User owner, int id, Session session) {
        return (TemporalAllocation) lockingScheme.getLocked(owner, current(id, TemporalAllocation.class, session), session);
    }

    public void releaseLocked(User user, int id, Session session) {
        TemporalAllocation current = getTemporalAllocation(id, session);
        lockingScheme.releaseLock(user, current, session);
    }

    public TemporalAllocation updateWithLock(TemporalAllocation locked, Session session) throws EmfException {
        return (TemporalAllocation) lockingScheme.renewLockOnUpdate(locked, current(locked, session), session);
    }

    private TemporalAllocation current(TemporalAllocation temporalAllocation, Session session) {
        return current(temporalAllocation.getId(), TemporalAllocation.class, session);
    }

    public boolean canUpdate(TemporalAllocation temporalAllocation, Session session) {
        if (!exists(temporalAllocation.getId(), TemporalAllocation.class, session)) {
            return false;
        }

        TemporalAllocation current = current(temporalAllocation.getId(), TemporalAllocation.class, session);

        session.clear(); // clear to flush current

        if (current.getName().equals(temporalAllocation.getName()))
            return true;

        return !nameUsed(temporalAllocation.getName(), TemporalAllocation.class, session);
    }

    public boolean nameUsed(String name, Class clazz, Session session) {
        return hibernateFacade.nameUsed(name, clazz, session);
    }

    private TemporalAllocation current(int id, Class clazz, Session session) {
        return (TemporalAllocation) hibernateFacade.current(id, clazz, session);
    }

    public boolean exists(int id, Class clazz, Session session) {
        return hibernateFacade.exists(id, clazz, session);
    }

    public TemporalAllocation getById(int id, Session session) {
        TemporalAllocation element = (TemporalAllocation)hibernateFacade.load(TemporalAllocation.class, Restrictions.eq("id", new Integer(id)), session);
        return element;
    }

    public TemporalAllocation getByName(String name, Session session) {
        TemporalAllocation element = (TemporalAllocation)hibernateFacade.load(TemporalAllocation.class, Restrictions.eq("name", new String(name)), session);
        return element;
    }

    public TemporalAllocation getTemporalAllocation(int id, Session session) {
        return (TemporalAllocation) hibernateFacade.load(TemporalAllocation.class, Restrictions.eq("id", new Integer(id)), session);
    }
    
    public List allResolutions(Session session) {
        return hibernateFacade.getAll(TemporalAllocationResolution.class, Order.asc("name"), session);
    }

    public Long getTemporalAllocationRunningCount(Session session) {
        return (Long)session.createQuery("SELECT COUNT(*) FROM TemporalAllocation WHERE runStatus = 'Running'").uniqueResult();
    }
    
    public List<TemporalAllocation> getTemporalAllocationsByRunStatus(String runStatus, Session session) {
      return session.createQuery("SELECT new TemporalAllocation(ta.id, ta.name) FROM TemporalAllocation ta WHERE ta.runStatus = :runStatus order by ta.lastModifiedDate").setString("runStatus", runStatus).list();
  }
    
    public void updateTemporalAllocationOutput(TemporalAllocationOutput output, Session session) {
        hibernateFacade.saveOrUpdate(output, session);
    }
    
    public TemporalAllocationOutputType getTemporalAllocationOutputType(String name, Session session) {
        Criterion critName = Restrictions.eq("name", name);
        return (TemporalAllocationOutputType)hibernateFacade.load(TemporalAllocationOutputType.class, critName, session);
    }
    
    public List<TemporalAllocationOutput> getTemporalAllocationOutputs(int temporalAllocationId, Session session) {
        return session.createCriteria(TemporalAllocationOutput.class).add(Restrictions.eq("temporalAllocationId", temporalAllocationId)).list();
    }
    
    public EmfDataset[] getTemporalAllocationOutputDatasets(int temporalAllocationId, Session session) {
        List<TemporalAllocationOutput> outputs = getTemporalAllocationOutputs(temporalAllocationId, session);
        List<EmfDataset> datasets = new ArrayList<EmfDataset>();
        if (outputs != null) {
            for (TemporalAllocationOutput output : outputs) {
                if (output.getOutputDataset() != null) {
                    datasets.add(output.getOutputDataset());
                }
            }
        }
        if (datasets.size() > 0) {
            return datasets.toArray(new EmfDataset[0]);
        }
        return null;
    }
    
    public void removeOutputs(int temporalAllocationId, Session session) {
        List<TemporalAllocationOutput> outputs = getTemporalAllocationOutputs(temporalAllocationId, session);
        hibernateFacade.remove(outputs.toArray(), session);
    }

    public void removeOutputDatasets(EmfDataset[] datasets, User user, Session session, DbServer dbServer) throws EmfException {
        if (datasets != null) {
            try {
                deleteDatasets(datasets, user, session);
                datasetDao.deleteDatasets(datasets, dbServer, session);
            } catch (EmfException e) {
                throw new EmfException(e.getMessage());
            }
        }
    }
    
    public void deleteDatasets(EmfDataset[] datasets, User user, Session session) throws EmfException {
        EmfDataset[] lockedDatasets = getLockedDatasets(datasets, user, session);
        
        if (lockedDatasets == null)
            return;
        
        try {
            new DataServiceImpl(dbServerFactory, sessionFactory).deleteDatasets(user, lockedDatasets, DeleteType.TEMPORAL_ALLOCATION);
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
    
    public void remove(TemporalAllocation element, Session session) {
        hibernateFacade.remove(element, session);
    }
    
    public void remove(TemporalAllocationOutput output, Session session) {
        hibernateFacade.remove(output, session);
    }

    public void setRunStatusAndCompletionDate(TemporalAllocation element, String runStatus, Date completionDate, Session session) {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.createQuery("update TemporalAllocation set runStatus = :status, lastModifiedDate = :date, completionDate = :completionDate where id = :id")
            .setString("status", runStatus)
            .setTimestamp("date", new Date())
            .setTimestamp("completionDate", completionDate)
            .setInteger("id", element.getId())
            .executeUpdate();
            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }
}
