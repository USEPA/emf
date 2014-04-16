package gov.epa.emissions.framework.services.basic;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.persistence.HibernateFacade;
import gov.epa.emissions.framework.services.persistence.LockingScheme;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

public class UserDAO {

    private LockingScheme lockingScheme;

    private HibernateFacade facade;

    public UserDAO() {
        lockingScheme = new LockingScheme();
        facade = new HibernateFacade();
    }

    public List all(Session session) {
        return facade.getAll(User.class, session);
    }

    public void add(User user, Session session) {
        facade.add(user, session);
    }

    public void remove(User user, Session session) {
        User loaded = get(user.getUsername(), session);
        if (!loaded.isLocked(user.getLockOwner()))
            throw new RuntimeException("Cannot remove user unless locked");

        facade.remove(loaded, session);
    }

    public User get(String username, Session session) {
        Criterion criterion = Restrictions.eq("username", username);
        List list = facade.get(User.class, criterion, session);
        if (list.isEmpty())
            return null;
        return (User) list.get(0);
    }
    
    public User getUserByEmail(int id, String email, Session session) {
        Criterion crit1 = Restrictions.eq("email", email);
        Criterion crit2 = Restrictions.ne("id", new Integer(id));
        
        List list = facade.get(User.class, new Criterion[]{crit1, crit2}, session);
        if (list.isEmpty())
            return null;
        return (User) list.get(0);
    }

    public User get(int userId, Session session) {
        Criterion criterion = Restrictions.eq("id", userId);
        User user = (User) facade.load(User.class, criterion, session);
        return user;
    }

    public boolean contains(String username, Session session) {
        return get(username, session) != null;
    }

    public User obtainLocked(User lockOwner, User user, Session session) {
        return (User) lockingScheme.getLocked(lockOwner, current(user, session), session);
    }

    public User update(User user, Session session) throws EmfException {
        return (User) lockingScheme.releaseLockOnUpdate(user, current(user, session), session);
    }

    public User releaseLocked(User user, User locked, Session session) {
        return (User) lockingScheme.releaseLock(user, current(locked, session), session);
    }

    private User current(User user, Session session) {
        return (User) facade.current(user.getId(), User.class, session);
    }
}
