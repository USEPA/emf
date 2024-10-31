package gov.epa.emissions.framework.services.persistence;

import gov.epa.emissions.commons.data.Lockable;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.EmfProperty;

import java.util.Date;

import javax.persistence.EntityManager;

public class LockingScheme {

    private EmfPropertiesDAO propertiesDao;
    private HibernateFacade hibernateFacade;

    public LockingScheme() {
        propertiesDao = new EmfPropertiesDAO();
        this.hibernateFacade = new HibernateFacade();
    }

    // throw an exception if the object is already locked
    //
    public Lockable getLocked(User user, Lockable current, EntityManager entityManager) {
        if (!current.isLocked()) {
            grabLock(user, current, entityManager);
            return current;
        }

        long elapsed = new Date().getTime() - current.getLockDate().getTime();

        if ((user.getName().equals(current.getLockOwner())) || (elapsed > timeInterval(entityManager))) {
            grabLock(user, current, entityManager);
        }

        return current;
    }

    public long timeInterval(EntityManager entityManager) {
        EmfProperty timeInterval = propertiesDao.getProperty("lock.time-interval", entityManager);
        return Long.parseLong(timeInterval.getValue());
    }

    public void grabLock(User user, Lockable lockable, EntityManager entityManager) {
        lockable.setLockOwner(user.getUsername());
        lockable.setLockDate(new Date());

        hibernateFacade.executeInsideTransaction(em -> {
            em.merge(lockable);
            em.flush();
        }, entityManager);
    }

    public Lockable releaseLock(Lockable current, EntityManager entityManager) {
        current.setLockOwner(null);
        current.setLockDate(null);
        hibernateFacade.executeInsideTransaction(em -> {
            em.merge(current);
            em.flush();
        }, entityManager);

        return current;
    }

    public Lockable releaseLock(User owner, Lockable current, EntityManager entityManager) {
        if (current == null || !current.isLocked() || !current.isLocked(owner))
            return current;
        
        return releaseLock(current, entityManager);
    }

    public Lockable releaseLockOnUpdate(Lockable target, Lockable current, EntityManager entityManager) throws EmfException {
        doUpdate(target, current, entityManager);
        return releaseLock(target, entityManager);
    }

    private void doUpdate(Lockable target, Lockable current, EntityManager entityManager) throws EmfException {
        if (target.getLockOwner() == null || !current.isLocked(target.getLockOwner()))
            throw new EmfException("Cannot update without owning lock");

        entityManager.clear();// clear 'loaded' locked object - to make way for updated object
        doUpdate(entityManager, target);
    }

    public Lockable renewLockOnUpdate(Lockable target, Lockable current, EntityManager entityManager) throws EmfException {
        doUpdate(target, current, entityManager);
        return target;
    }

    private void doUpdate(EntityManager entityManager, Lockable target) {
        target.setLockDate(new Date());
        hibernateFacade.executeInsideTransaction(em -> {
            em.merge(target);
            em.flush();
        }, entityManager);
    }
}