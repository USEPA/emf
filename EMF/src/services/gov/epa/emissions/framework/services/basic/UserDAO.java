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

import javax.persistence.EntityManager;

public class UserDAO {

    private LockingScheme lockingScheme;

    private HibernateFacade facade;

    public UserDAO() {
        lockingScheme = new LockingScheme();
        facade = new HibernateFacade();
    }

    public List<User> all(EntityManager entityManager) {
        CriteriaBuilderQueryRoot<User> criteriaBuilderQueryRoot = facade.getCriteriaBuilderQueryRoot(User.class, entityManager);
        return facade.getAll(criteriaBuilderQueryRoot, entityManager);
    }

    public void add(User user, EntityManager entityManager) {
        facade.add(user, entityManager);
    }

    public void remove(User user, EntityManager entityManager) {
        User loaded = get(user.getUsername(), entityManager);
        if (!loaded.isLocked(user.getLockOwner()))
            throw new RuntimeException("Cannot remove user unless locked");

        facade.remove(loaded, entityManager);
    }

    public User get(String username, EntityManager entityManager) {
        CriteriaBuilderQueryRoot<User> criteriaBuilderQueryRoot = facade.getCriteriaBuilderQueryRoot(User.class, entityManager);
        
        List<User> list = facade.get(entityManager, criteriaBuilderQueryRoot, criteriaBuilderQueryRoot.getBuilder().equal(criteriaBuilderQueryRoot.getRoot().get("username"), username));
        if (list.isEmpty())
            return null;
        return list.get(0);
    }
    
    public User getUserByEmail(int id, String email, EntityManager entityManager) {
        CriteriaBuilderQueryRoot<User> criteriaBuilderQueryRoot = facade.getCriteriaBuilderQueryRoot(User.class, entityManager);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<User> root = criteriaBuilderQueryRoot.getRoot();

        Predicate crit1 = builder.equal(root.get("email"), email);
        Predicate crit2 = builder.notEqual(root.get("id"), Integer.valueOf(id));
        
        List list = facade.get(criteriaBuilderQueryRoot, new Predicate[]{crit1, crit2}, entityManager);
        if (list.isEmpty())
            return null;
        return (User) list.get(0);
    }

    public User get(int userId, EntityManager entityManager) {
        return facade.load(User.class, "id", new Integer(userId), entityManager);
    }

    public boolean contains(String username, EntityManager entityManager) {
        return get(username, entityManager) != null;
    }

    public User obtainLocked(User lockOwner, User user, EntityManager entityManager) {
        return (User) lockingScheme.getLocked(lockOwner, current(user, entityManager), entityManager);
    }

    public User update(User user, EntityManager entityManager) throws EmfException {
        return (User) lockingScheme.releaseLockOnUpdate(user, current(user, entityManager), entityManager);
    }

    public User releaseLocked(User user, User locked, EntityManager entityManager) {
        return (User) lockingScheme.releaseLock(user, current(locked, entityManager), entityManager);
    }

    private User current(User user, EntityManager entityManager) {
        return facade.current(user.getId(), User.class, entityManager);
    }
}
