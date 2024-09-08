package gov.epa.emissions.framework.services.basic;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.persistence.HibernateFacade;
import gov.epa.emissions.framework.services.persistence.HibernateFacade.CriteriaBuilderQueryRoot;
import gov.epa.emissions.framework.services.persistence.LockingScheme;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.hibernate.Session;

public class UserDAO {

    private LockingScheme lockingScheme;

    private HibernateFacade facade;

    public UserDAO() {
        lockingScheme = new LockingScheme();
        facade = new HibernateFacade();
    }

    public List<User> all(Session session) {
        CriteriaBuilderQueryRoot<User> criteriaBuilderQueryRoot = facade.getCriteriaBuilderQueryRoot(User.class, session);
        return facade.getAll(criteriaBuilderQueryRoot, session);
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
        CriteriaBuilderQueryRoot<User> criteriaBuilderQueryRoot = facade.getCriteriaBuilderQueryRoot(User.class, session);
        
        List<User> list = facade.get(session, criteriaBuilderQueryRoot, criteriaBuilderQueryRoot.getBuilder().equal(criteriaBuilderQueryRoot.getRoot().get("username"), username));
        if (list.isEmpty())
            return null;
        return list.get(0);
    }
    
    public User getUserByEmail(int id, String email, Session session) {
        CriteriaBuilderQueryRoot<User> criteriaBuilderQueryRoot = facade.getCriteriaBuilderQueryRoot(User.class, session);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<User> root = criteriaBuilderQueryRoot.getRoot();

        Predicate crit1 = builder.equal(root.get("email"), email);
        Predicate crit2 = builder.notEqual(root.get("id"), Integer.valueOf(id));
        
        List list = facade.get(criteriaBuilderQueryRoot, new Predicate[]{crit1, crit2}, session);
        if (list.isEmpty())
            return null;
        return (User) list.get(0);
    }

    public User get(int userId, Session session) {
        return facade.load(User.class, "id", new Integer(userId), session);
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
        return facade.current(user.getId(), User.class, session);
    }
}
