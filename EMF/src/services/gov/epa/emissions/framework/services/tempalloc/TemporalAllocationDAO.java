package gov.epa.emissions.framework.services.tempalloc;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.persistence.HibernateFacade;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;
import gov.epa.emissions.framework.services.persistence.LockingScheme;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

public class TemporalAllocationDAO {
    private LockingScheme lockingScheme;

    private HibernateFacade hibernateFacade;

    private HibernateSessionFactory sessionFactory;

    private DbServerFactory dbServerFactory;
    
    public TemporalAllocationDAO() {
        lockingScheme = new LockingScheme();
        hibernateFacade = new HibernateFacade();
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

    public TemporalAllocation getTemporalAllocation(int id, Session session) {
        return (TemporalAllocation) hibernateFacade.load(TemporalAllocation.class, Restrictions.eq("id", new Integer(id)), session);
    }
}
