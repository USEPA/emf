package gov.epa.emissions.framework.services.basic;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.persistence.GenericLockingDao;

/**
 * DAO of user.
 */
public interface UserDao extends GenericLockingDao<User, Integer> {

    /**
     * Gets user by username.
     * @param username username to lookup on
     * @return User user based on username.
     */
	User get(String username);
       
    /**
     * Gets user by id and email.
     * @param username username to lookup on
     * @return User user based on username.
     */
	User getUserByIdAndEmail(Integer id, String email);

//	List<User> all();
//
//	User get(Integer id);
//
	boolean contains(String username);

//	User releaseLock(User user, User locked);
//
//	User obtainLocked(User lockOwner, User user);
//
//	User releaseLocked(User user, User locked);
//
//	User obtainLocked(User lockOwner, User user);
//
//	boolean contains(String username);

}