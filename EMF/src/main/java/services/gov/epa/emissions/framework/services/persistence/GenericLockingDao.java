package gov.epa.emissions.framework.services.persistence;

import gov.epa.emissions.commons.data.Lockable;
import gov.epa.emissions.commons.security.User;

import java.io.Serializable;

public interface GenericLockingDao<E extends Lockable, K extends Serializable> extends GenericDao<E, K> {

	// throw an exception if the object is already locked
	//
	E getLocked(User user, E current);

	E getLocked(Integer userId, K entityId);

	long timeInterval();

	void grabLock(User user, E lockable);

	E releaseLock(E current);

	E releaseLock(User owner, E current);

	E releaseLockOnUpdate(E target, E current);

	E renewLockOnUpdate(E target, E current);

}