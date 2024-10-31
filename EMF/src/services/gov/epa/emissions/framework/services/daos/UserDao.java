package gov.epa.emissions.framework.services.daos;

import gov.epa.emissions.commons.security.User;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

public class UserDao extends AbstractDao<User> {

    public UserDao(EntityManagerFactory entityManagerFactory) {
        super(User.class, entityManagerFactory);
    }
    
    public User get(String username) {
        List<User> users = new ArrayList<User>();
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            CriteriaBuilder builder = entityManager.getCriteriaBuilder();
            CriteriaQuery<User> criteriaQuery = builder.createQuery(User.class);
            Root<User> root = criteriaQuery.from(User.class);

            criteriaQuery.select(root);
            criteriaQuery.where(builder.equal(root.get("username"), username));
            
            users = entityManager.createQuery(criteriaQuery).getResultList();
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        } finally {
            entityManager.close();
        }
        if (users.isEmpty())
            return null;
        return users.get(0);
    }
    
    public User getUserByEmail(int id, String email) {
        List<User> users = new ArrayList<User>();
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            CriteriaBuilder builder = entityManager.getCriteriaBuilder();
            CriteriaQuery<User> criteriaQuery = builder.createQuery(User.class);
            Root<User> root = criteriaQuery.from(User.class);

            Predicate crit1 = builder.equal(root.get("email"), email);
            Predicate crit2 = builder.notEqual(root.get("id"), Integer.valueOf(id));
            
            criteriaQuery.select(root);
            criteriaQuery.where(new Predicate[]{crit1, crit2});
            
            users = entityManager.createQuery(criteriaQuery).getResultList();
        } catch (RuntimeException e) {
            e.printStackTrace();
        } finally {
            entityManager.close();
        }
        if (users.isEmpty())
            return null;
        return users.get(0);
    }

    public boolean contains(String username) {
        return get(username) != null;
    }
}
