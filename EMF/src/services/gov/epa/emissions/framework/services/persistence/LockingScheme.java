package gov.epa.emissions.framework.services.persistence;

import gov.epa.emissions.commons.data.Lockable;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.EmfProperty;

import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

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
            return grabLock(user, current, entityManager);
        }

        long elapsed = new Date().getTime() - current.getLockDate().getTime();

        if ((user.getName().equals(current.getLockOwner())) || (elapsed > timeInterval(entityManager))) {
            return grabLock(user, current, entityManager);
        }

        return current;
    }

    public long timeInterval(EntityManager entityManager) {
        EmfProperty timeInterval = propertiesDao.getProperty("lock.time-interval", entityManager);
        return Long.parseLong(timeInterval.getValue());
    }

    public Lockable grabLock(User user, Lockable lockable, EntityManager entityManager) {
        lockable.setLockOwner(user.getUsername());
        lockable.setLockDate(new Date());
        return hibernateFacade.updateOnly(lockable, entityManager);
    }

    public Lockable releaseLock(Lockable current, EntityManager entityManager) {
        current.setLockOwner(null);
        current.setLockDate(null);
        return hibernateFacade.updateOnly(current, entityManager);
    }

    public Lockable releaseLock(User owner, Lockable current, EntityManager entityManager) {
        if (current == null || !current.isLocked() || !current.isLocked(owner))
            return current;
        
        return releaseLock(current, entityManager);
    }

    public Lockable releaseLockOnUpdate(Lockable target, Lockable current, EntityManager entityManager) throws EmfException {
        Lockable updated = doUpdate(target, current, entityManager);
        return releaseLock(updated, entityManager);
    }

    private Lockable doUpdate(Lockable target, Lockable current, EntityManager entityManager) throws EmfException {
        if (target.getLockOwner() == null || !current.isLocked(target.getLockOwner()))
            throw new EmfException("Cannot update without owning lock");

        entityManager.clear();// clear 'loaded' locked object - to make way for updated object
        return doUpdate(entityManager, target);
    }

    public Lockable renewLockOnUpdate(Lockable target, Lockable current, EntityManager entityManager) throws EmfException {
        return doUpdate(target, current, entityManager);
    }
    
    private Lockable doUpdate(EntityManager entityManager, Lockable target) {
        target.setLockDate(new Date());
        return hibernateFacade.updateOnly(target, entityManager);
    }
}
