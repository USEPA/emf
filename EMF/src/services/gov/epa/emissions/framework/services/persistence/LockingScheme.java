package gov.epa.emissions.framework.services.persistence;

import gov.epa.emissions.commons.data.Lockable;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.EmfProperties;
import gov.epa.emissions.framework.services.basic.EmfProperty;

import java.util.Date;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class LockingScheme {

    private EmfProperties propertiesDao;

    public LockingScheme() {
        propertiesDao = new EmfPropertiesDAO();
    }

    // throw an exception if the object is already locked
    //
    public Lockable getLocked(User user, Lockable current, Session session) {
        if (!current.isLocked()) {
            grabLock(user, current, session);
            return current;
        }

        long elapsed = new Date().getTime() - current.getLockDate().getTime();

        if ((user.getName().equals(current.getLockOwner())) || (elapsed > timeInterval(session))) {
            grabLock(user, current, session);
        }

        return current;
    }

    public long timeInterval(Session session) {
        EmfProperty timeInterval = propertiesDao.getProperty("lock.time-interval", session);
        return Long.parseLong(timeInterval.getValue());
    }

    public void grabLock(User user, Lockable lockable, Session session) {
        lockable.setLockOwner(user.getUsername());
        lockable.setLockDate(new Date());

        Transaction tx = session.beginTransaction();
        try {
            session.update(lockable);
            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }

    public Lockable releaseLock(Lockable current, Session session) {
        Transaction tx = session.beginTransaction();
        
        try {
            current.setLockOwner(null);
            current.setLockDate(null);
            session.update(current);

            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }

        return current;
    }

    public Lockable releaseLock(User owner, Lockable current, Session session) {
        if (current == null || !current.isLocked() || !current.isLocked(owner))
            return current;
        
        return releaseLock(current, session);
    }

    public Lockable releaseLockOnUpdate(Lockable target, Lockable current, Session session) throws EmfException {
        doUpdate(target, current, session);
        return releaseLock(target, session);
    }

    private void doUpdate(Lockable target, Lockable current, Session session) throws EmfException {
        if (target.getLockOwner() == null || !current.isLocked(target.getLockOwner()))
            throw new EmfException("Cannot update without owning lock");

        session.clear();// clear 'loaded' locked object - to make way for updated object
        doUpdate(session, target);
    }

    public Lockable renewLockOnUpdate(Lockable target, Lockable current, Session session) throws EmfException {
        doUpdate(target, current, session);
        return target;
    }

    private void doUpdate(Session session, Lockable target) {
        Transaction tx = session.beginTransaction();
        try {
            target.setLockDate(new Date());
            session.update(target);
            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        } 
    }

}
