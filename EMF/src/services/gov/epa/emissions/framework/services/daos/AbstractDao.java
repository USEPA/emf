
package gov.epa.emissions.framework.services.daos;

import gov.epa.emissions.commons.data.Lockable;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.EmfProperty;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

public abstract class AbstractDao<T extends Lockable> implements Dao<T> {

    protected final Class<T> persistentClass;
    protected final EntityManagerFactory entityManagerFactory;
    
    public AbstractDao(Class<T> persistentClass, EntityManagerFactory entityManagerFactory) {
        this.persistentClass = persistentClass;
        this.entityManagerFactory = entityManagerFactory;
    }
    
    @Override
    public Optional<T> get(int id) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            return Optional.ofNullable(entityManager.find(this.persistentClass, id));
        } catch (RuntimeException e) {
            e.printStackTrace();
        } finally {
            entityManager.close();
        }
        return null;
    }

    @Override
    public List<T> getAll() {
        List<T> all = new ArrayList<T>();
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            CriteriaBuilder builder = entityManager.getCriteriaBuilder();
            CriteriaQuery<T> criteriaQuery = builder.createQuery(persistentClass);
            Root<T> root = criteriaQuery.from(persistentClass);

            criteriaQuery.select(root);
            
            all = entityManager.createQuery(criteriaQuery).getResultList();
        } catch (RuntimeException e) {
            e.printStackTrace();
        } finally {
            entityManager.close();
        }
        return all;
    }

    @Override
    public T add(T t) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            executeInsideTransaction(em -> entityManager.persist(t), entityManager);
        } catch (RuntimeException e) {
            e.printStackTrace();
        } finally {
            entityManager.close();
        }
        return t;
    }

    @Override
    public T update(T t) throws EmfException {
        return releaseLockOnUpdate(t, get(t.getId()).get());
    }

    @Override
    public void delete(T t) throws EmfException {
        T loaded = get(t.getId()).get();
        if (!loaded.isLocked(t.getLockOwner()))
            throw new EmfException("Cannot remove " + t.getClass().getName() + " unless locked");

        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            executeInsideTransaction(em -> entityManager.remove(t), entityManager);
        } catch (RuntimeException e) {
            e.printStackTrace();
        } finally {
            entityManager.close();
        }
    }
    
    // throw an exception if the object is already locked
    //
    public T getLocked(User user, T current) {
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

    public long timeInterval() {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            CriteriaBuilder builder = entityManager.getCriteriaBuilder();
            CriteriaQuery<EmfProperty> criteriaQuery = builder.createQuery(EmfProperty.class);
            Root<EmfProperty> root = criteriaQuery.from(EmfProperty.class);

            criteriaQuery.select(root);
            criteriaQuery.where(builder.equal(root.get("name"), "lock.time-interval"));
            
            EmfProperty timeInterval = entityManager.createQuery(criteriaQuery).getSingleResult();
            return Long.parseLong(timeInterval.getValue());
        } catch (RuntimeException e) {
            e.printStackTrace();
        } finally {
            entityManager.close();
        }
        return 0;
    }

    public void grabLock(User user, T lockable) {
        lockable.setLockOwner(user.getUsername());
        lockable.setLockDate(new Date());
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            executeInsideTransaction(em -> entityManager.merge(lockable), entityManager);
        } catch (RuntimeException e) {
            e.printStackTrace();
        } finally {
            entityManager.close();
        }
    }

    public T releaseLock(T current) {
        current.setLockOwner(null);
        current.setLockDate(null);
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            executeInsideTransaction(em -> entityManager.merge(current), entityManager);
        } catch (RuntimeException e) {
            e.printStackTrace();
        } finally {
            entityManager.close();
        }
        return current;
    }

    public T releaseLock(User owner, T current) {
        if (current == null || !current.isLocked() || !current.isLocked(owner))
            return current;
        
        return releaseLock(current);
    }

    public T releaseLockOnUpdate(T target, T current) throws EmfException {
        doUpdate(target, current);
        return releaseLock(target);
    }

    private void doUpdate(T target, T current) throws EmfException {
        if (target.getLockOwner() == null || !current.isLocked(target.getLockOwner()))
            throw new EmfException("Cannot update without owning lock");

//        entityManager.clear();// clear 'loaded' locked object - to make way for updated object
        doUpdate(target);
    }

    public T renewLockOnUpdate(T target, T current) throws EmfException {
        doUpdate(target, current);
        return target;
    }

    private void doUpdate(T target) {
        target.setLockDate(new Date());
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            executeInsideTransaction(em -> entityManager.merge(target), entityManager);
        } catch (RuntimeException e) {
            e.printStackTrace();
        } finally {
            entityManager.close();
        }
    }

    protected void executeInsideTransaction(Consumer<EntityManager> action, EntityManager entityManager) {
        final EntityTransaction tx = entityManager.getTransaction();
        try {
            tx.begin();
            action.accept(entityManager);
            tx.commit(); 
        }
        catch (RuntimeException e) {
            tx.rollback();
            throw e;
        } finally {
            entityManager.close();
        }
    }
}
