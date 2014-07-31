package gov.epa.emissions.framework.services.basic;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.persistence.GenericLockingHibernateDao;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.stereotype.Repository;

@Repository("userDao")
public class UserDaoImpl extends GenericLockingHibernateDao<User, Integer> implements UserDao {

	public UserDaoImpl() {
		super(User.class);
	}

	/* (non-Javadoc)
	 * @see gov.epa.emissions.framework.services.basic.UserDAOInt#all()
	 */
    @Override
	public List<User> all() {
        return list(Order.asc("name"));
    }

    /* (non-Javadoc)
	 * @see gov.epa.emissions.framework.services.basic.UserDAOInt#remove(gov.epa.emissions.commons.security.User)
	 */
    @Override
	public void remove(User user) {
        User loaded = get(user.getUsername());
        if (!loaded.isLocked(user.getLockOwner()))
            throw new DataAccessResourceFailureException("Cannot remove user unless locked");

        remove(loaded);
    }

    /* (non-Javadoc)
	 * @see gov.epa.emissions.framework.services.basic.UserDAOInt#get(java.lang.String)
	 */
    @Override
	public User get(String username) {
        Criterion criterion = Restrictions.eq("username", username);
        Session session = currentSession();
        
        Criteria criteria = session.createCriteria(User.class);
        criteria.add(criterion);

		@SuppressWarnings("unchecked")
		List<User> list = criteria.list();
		if (list.isEmpty())
		    return null;
		return list.get(0);
    }
    
    /* (non-Javadoc)
	 * @see gov.epa.emissions.framework.services.basic.UserDAOInt#getUserByEmail(int, java.lang.String)
	 */
    @Override
	public User getUserByIdAndEmail(Integer id, String email) {
        Criterion crit1 = Restrictions.eq("email", email);
        Criterion crit2 = Restrictions.ne("id", id);
        
        Criteria criteria = currentSession().createCriteria(User.class);
        criteria.add(crit1);
        criteria.add(crit2);
        @SuppressWarnings("unchecked")
		List<User> list = criteria.list();
        if (list.isEmpty())
            return null;
        return list.get(0);
    }

    /* (non-Javadoc)
	 * @see gov.epa.emissions.framework.services.basic.UserDAOInt#contains(java.lang.String)
	 */
    @Override
	public boolean contains(String username) {
        return get(username) != null;
    }

    /* (non-Javadoc)
	 * @see gov.epa.emissions.framework.services.basic.UserDAOInt#update(gov.epa.emissions.commons.security.User)
	 */
    @Override
	public void update(User user) {
			releaseLockOnUpdate(user, current(user));
    }

//    /* (non-Javadoc)
//	 * @see gov.epa.emissions.framework.services.basic.UserDAOInt#releaseLocked(gov.epa.emissions.commons.security.User, gov.epa.emissions.commons.security.User)
//	 */
//    @Override
//	public User releaseLock(User user, User locked) {
//        return lockingScheme.releaseLock(user, current(locked));
//    }

    private User current(User user) {
        return find(user.getId());
    }

}
