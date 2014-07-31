
package gov.epa.emissions.framework.services.persistence;

import gov.epa.emissions.commons.data.Lockable;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.basic.EmfProperty;

import java.io.Serializable;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.stereotype.Repository;

@Repository("lockingScheme")
public class GenericLockingHibernateDao<E extends Lockable, K extends Serializable> extends GenericHibernateDao<E, K> implements GenericLockingDao<E, K> {

    public GenericLockingHibernateDao(Class<E> daoType) {
		super(daoType);
	}

    public GenericLockingHibernateDao() {
		super();
	}

	private EmfPropertyDao emfPropertyDao;

    @Autowired
	public void setEmfPropertyDao(EmfPropertyDao emfPropertyDao) {
		this.emfPropertyDao = emfPropertyDao;
	}

//    private SessionFactory sessionFactory;
//
//    @Autowired
//    public void setSessionFactory(SessionFactory sessionFactory) {
//        this.sessionFactory = sessionFactory;
//    }
//
//	public LockingScheme2() {
//        propertiesDao = new EmfPropertiesDAO();
//    }

	// throw an exception if the object is already locked
    //
    /* (non-Javadoc)
	 * @see gov.epa.emissions.framework.services.persistence.LockingSchemeInt#getLocked(gov.epa.emissions.commons.security.User, E)
	 */
    @Override
	public E getLocked(User user, E current) {
        if (!current.isLocked()) {
            grabLock(user, current);
            return current;
        }

        long elapsed = new Date().getTime() - current.getLockDate().getTime();

        if ((user.getName().equals(current.getLockOwner())) || (elapsed > timeInterval())) {
            grabLock(user, current);
        }

        return current;
    }

    /* (non-Javadoc)
	 * @see gov.epa.emissions.framework.services.persistence.LockingSchemeInt#getLocked(gov.epa.emissions.commons.security.User, E)
	 */
    @Override
	public E getLocked(Integer userId, K entityId) {
    	E entity = get(entityId);
    	String username = getUsername(userId);
    	
        if (!entity.isLocked()) {
      		entity.setLockOwner(username);
      		entity.setLockDate(new Date());
      		currentSession().update(entity);
//      		currentSession().clear();
            return entity;
        }

        long elapsed = new Date().getTime() - entity.getLockDate().getTime();

        if ((username.equals(entity.getLockOwner())) || (elapsed > timeInterval())) {
      		entity.setLockOwner(username);
      		entity.setLockDate(new Date());
      		currentSession().update(entity);
//      		currentSession().clear();
        }

        return entity;
    }

    private String getUsername(Integer userId) {
    	return (String)currentSession().createQuery("select username from User where id = " + userId).uniqueResult();
    }    
    
    /* (non-Javadoc)
	 * @see gov.epa.emissions.framework.services.persistence.LockingSchemeInt#timeInterval()
	 */
    @Override
	public long timeInterval() {
        EmfProperty timeInterval = emfPropertyDao.getProperty("lock.time-interval");
        return Long.parseLong(timeInterval.getValue());
    }

    /* (non-Javadoc)
	 * @see gov.epa.emissions.framework.services.persistence.LockingSchemeInt#grabLock(gov.epa.emissions.commons.security.User, E)
	 */
    @Override
	public void grabLock(User user, E lockable) {
        lockable.setLockOwner(user.getUsername());
        lockable.setLockDate(new Date());

        super.merge(lockable);
    }

    /* (non-Javadoc)
	 * @see gov.epa.emissions.framework.services.persistence.LockingSchemeInt#releaseLock(E)
	 */
    @Override
	public E releaseLock(E current) {
        current.setLockOwner(null);
        current.setLockDate(null);
        
        super.update(current);

        return current;
    }

    /* (non-Javadoc)
	 * @see gov.epa.emissions.framework.services.persistence.LockingSchemeInt#releaseLock(gov.epa.emissions.commons.security.User, E)
	 */
    @Override
	public E releaseLock(User owner, E current) {
        if (current == null || !current.isLocked() || !current.isLocked(owner))
            return current;
        
        return releaseLock(current);
    }

    /* (non-Javadoc)
	 * @see gov.epa.emissions.framework.services.persistence.LockingSchemeInt#releaseLockOnUpdate(E, E)
	 */
    @Override
	public E releaseLockOnUpdate(E target, E current) {
        doUpdate(target, current);
        return releaseLock(target);
    }

    private void doUpdate(E target, E current) {
        if (target.getLockOwner() == null || !current.isLocked(target.getLockOwner()))
            throw new DataRetrievalFailureException("Cannot update without owning lock");

        currentSession().clear();
        doUpdate(target);
    }

    /* (non-Javadoc)
	 * @see gov.epa.emissions.framework.services.persistence.LockingSchemeInt#renewLockOnUpdate(E, E)
	 */
    @Override
	public E renewLockOnUpdate(E target, E current) {
        doUpdate(target, current);
        return target;
    }

    private void doUpdate(E target) {
        target.setLockDate(new Date());
        super.update(target);
    }

}
