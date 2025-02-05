package gov.epa.emissions.framework.services.persistence;

import gov.epa.emissions.commons.data.Lockable;

import java.util.List;
import java.util.function.Consumer;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

public class HibernateFacade {

    public <T extends Lockable> Integer add(T t, EntityManager entityManager) {
        final EntityTransaction tx = entityManager.getTransaction();
        try {
            tx.begin();
            t = entityManager.contains(t) ? t : entityManager.merge(t);
            entityManager.persist(t);
            entityManager.flush();
            tx.commit(); 
        }
        catch (RuntimeException e) {
            tx.rollback();
            throw e;
        } finally {
            //
        }
        return t.getId();
    }

    public <T> T add(T t, EntityManager entityManager) {
        final EntityTransaction tx = entityManager.getTransaction();
        try {
            tx.begin();
            t = entityManager.contains(t) ? t : entityManager.merge(t);
            entityManager.persist(t);
            entityManager.flush();
            tx.commit(); 
        }
        catch (RuntimeException e) {
            tx.rollback();
            throw e;
        } finally {
            //
        }
        return t;
    }

    public <T extends Lockable> void add(T[] objects, EntityManager entityManager) {
        for (int i = 0; i < objects.length; i++)
            add(objects[i], entityManager);
    }

    public <T> void add(T[] objects, EntityManager entityManager) {
        for (int i = 0; i < objects.length; i++)
            add(objects[i], entityManager);
    }

    public <C> boolean exists(int id, Class<C> clazz, EntityManager entityManager) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<C> criteriaQuery = builder.createQuery(clazz);
        Root<C> root = criteriaQuery.from(clazz);

        criteriaQuery.select(root);

        criteriaQuery.where(builder.equal(root.get("id"), Integer.valueOf(id)));

        return entityManager.createQuery(criteriaQuery).getResultList().size() > 0;
    }

    public <C> boolean exists(CriteriaBuilderQueryRoot<C> criteriaBuilderQueryRoot, Predicate[] predicates, EntityManager entityManager) {
        criteriaBuilderQueryRoot.getCriteriaQuery().select(criteriaBuilderQueryRoot.getRoot());

        criteriaBuilderQueryRoot.getCriteriaQuery().where(predicates);

        return entityManager.createQuery(criteriaBuilderQueryRoot.getCriteriaQuery()).getResultList().size() > 0;
    }

    public <C> C current(int id, Class<C> clazz, EntityManager entityManager) {
        return load(clazz, "id", Integer.valueOf(id), entityManager);
    }

    public <C,K> boolean isUsed(String key, K value, Class<C> clazz, EntityManager entityManager) {
        return load(clazz, key, value, entityManager) != null;
    }
    
    public <C> boolean nameUsed(String name, Class<C> clazz, EntityManager entityManager) {
        return this.isUsed("name", name, clazz, entityManager);
    }

    // save- new object, updates if the objects already exist
    public void saveOrUpdate(Object object, EntityManager entityManager) {
        executeInsideTransaction(em -> {
            em.merge(object);
            em.flush();
        }, entityManager);
    }

    public void updateOnly(Object object, EntityManager entityManager) {
        executeInsideTransaction(em -> em.merge(object), entityManager);
    }

    public void update(Object[] objects, EntityManager entityManager) {
        for (int i = 0; i < objects.length; i++)
            saveOrUpdate(objects[i], entityManager);
    }

    public void removeObjects(Object[] objects, EntityManager entityManager) {
        final EntityTransaction tx = entityManager.getTransaction();
        try {
            tx.begin();
            for (Object obj : objects)
                entityManager.remove(entityManager.contains(obj) ? obj : entityManager.merge(obj));
            tx.commit(); 
        }
        catch (RuntimeException e) {
            tx.rollback();
            throw e;
        } finally {
            //
        }
    }

    public void remove(Object[] objects, EntityManager entityManager) {
        for (int i = 0; i < objects.length; i++)
            remove(objects[i], entityManager);
    }

    public void remove(Object obj, EntityManager entityManager) {
        executeInsideTransaction(em -> {
            em.remove(em.contains(obj) ? obj : em.merge(obj));
        }, entityManager);
    }

    public <C> List<C> getAll(CriteriaBuilderQueryRoot<C> criteriaBuilderQueryRoot, Order order, EntityManager entityManager) {
        criteriaBuilderQueryRoot.getCriteriaQuery().select(criteriaBuilderQueryRoot.getRoot());
        criteriaBuilderQueryRoot.getCriteriaQuery().orderBy(order);

        return entityManager.createQuery(criteriaBuilderQueryRoot.getCriteriaQuery()).getResultList();
    }

    public <C> List<C> getAll(CriteriaBuilderQueryRoot<C> criteriaBuilderQueryRoot, EntityManager entityManager) {
        criteriaBuilderQueryRoot.getCriteriaQuery().select(criteriaBuilderQueryRoot.getRoot());

        return entityManager.createQuery(criteriaBuilderQueryRoot.getCriteriaQuery()).getResultList();
    }

    public <C> boolean exists(String name, Class<C> clazz, EntityManager entityManager) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<C> criteriaQuery = builder.createQuery(clazz);
        Root<C> root = criteriaQuery.from(clazz);

        criteriaQuery.select(root);

        criteriaQuery.where(builder.equal(root.get("name"), name));

        return entityManager.createQuery(criteriaQuery).getSingleResult() != null;
    }

    public <C> List<C> get(CriteriaBuilderQueryRoot<C> criteriaBuilderQueryRoot, Predicate predicate, javax.persistence.criteria.Order order, EntityManager entityManager) {
        criteriaBuilderQueryRoot.getCriteriaQuery().select(criteriaBuilderQueryRoot.getRoot());
        criteriaBuilderQueryRoot.getCriteriaQuery().where(predicate);
        criteriaBuilderQueryRoot.getCriteriaQuery().orderBy(order);

        return entityManager.createQuery(criteriaBuilderQueryRoot.getCriteriaQuery()).getResultList();
    }

    public <C,K> List<C> get(Class<C> persistentClass, String keyName, K keyValue, EntityManager entityManager) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<C> criteriaQuery = builder.createQuery(persistentClass);
        Root<C> root = criteriaQuery.from(persistentClass);

        criteriaQuery.select(root);

        criteriaQuery.where(builder.equal(root.get(keyName), keyValue));

        return entityManager.createQuery(criteriaQuery).getResultList();
    }
    
    public <C> List<C> get(EntityManager entityManager, CriteriaBuilderQueryRoot<C> criteriaBuilderQueryRoot, Predicate predicate) {
        criteriaBuilderQueryRoot.getCriteriaQuery().select(criteriaBuilderQueryRoot.getRoot());

        criteriaBuilderQueryRoot.getCriteriaQuery().where(predicate);

        return entityManager.createQuery(criteriaBuilderQueryRoot.getCriteriaQuery()).getResultList();
    }

//    public List get(Class clazz, Criterion criterion, EntityManager entityManager) {
//        Transaction tx = null;
//        try {
//            tx = entityManager.beginTransaction();
//            List list = entityManager.createCriteria(clazz).add(criterion).list();
//            tx.commit();
//            return list;
//        } catch (HibernateException e) {
//            tx.rollback();
//            throw e;
//        }
//    }

//    public Object get(Class clazz, Criterion criterion, StatelessSession entityManager) {
//        Transaction tx = null;
//        try {
//            tx = entityManager.beginTransaction();
//            Object obj = entityManager.get(clazz, criterion);
//            tx.commit();
//            return obj;
//        } catch (HibernateException e) {
//            tx.rollback();
//            throw e;
//        }
//    }
//
    public <C> List<C> get(CriteriaBuilderQueryRoot<C> criteriaBuilderQueryRoot, Predicate[] predicate, EntityManager entityManager) {
        criteriaBuilderQueryRoot.getCriteriaQuery().select(criteriaBuilderQueryRoot.getRoot());

        criteriaBuilderQueryRoot.getCriteriaQuery().where(predicate);

        return entityManager.createQuery(criteriaBuilderQueryRoot.getCriteriaQuery()).getResultList();
    }

    public <C> List<C> get(CriteriaBuilderQueryRoot<C> criteriaBuilderQueryRoot, Predicate[] predicate, Order order, EntityManager entityManager) {
        criteriaBuilderQueryRoot.getCriteriaQuery().select(criteriaBuilderQueryRoot.getRoot());

        criteriaBuilderQueryRoot.getCriteriaQuery().where(predicate);
        criteriaBuilderQueryRoot.getCriteriaQuery().orderBy(order);

        return entityManager.createQuery(criteriaBuilderQueryRoot.getCriteriaQuery()).getResultList();
    }

//    public List get(Class clazz, Criterion[] criterions, EntityManager entityManager) {
//        Transaction tx = null;
//        try {
//            tx = entityManager.beginTransaction();
//            Criteria criteria = entityManager.createCriteria(clazz);
//            for (int i = 0; i < criterions.length; i++)
//                criteria.add(criterions[i]);
//
//            tx.commit();
//            return criteria.list();
//        } catch (HibernateException e) {
//            e.printStackTrace();
//            tx.rollback();
//            throw e;
//        }
//    }

    public <C,K> C load(Class<C> persistentClass, String keyName, K keyValue, EntityManager entityManager) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<C> criteriaQuery = builder.createQuery(persistentClass);
        Root<C> root = criteriaQuery.from(persistentClass);

        criteriaQuery.select(root);

        criteriaQuery.where(builder.equal(root.get(keyName), keyValue));
        
        try {
            return entityManager.createQuery(criteriaQuery).getSingleResult();
        } catch (NoResultException e) {
            return null; 
        }
    }
    
    public <C> C load(EntityManager entityManager, CriteriaBuilderQueryRoot<C> criteriaBuilderQueryRoot, Predicate predicate) {
        criteriaBuilderQueryRoot.getCriteriaQuery().select(criteriaBuilderQueryRoot.getRoot());

        criteriaBuilderQueryRoot.getCriteriaQuery().where(predicate);

        try {
            return entityManager.createQuery(criteriaBuilderQueryRoot.getCriteriaQuery()).getSingleResult();
        } catch (NoResultException e) {
            return null; 
        }
    }

//    
//    public Object load(Class clazz, Criterion criterion, EntityManager entityManager) {
//        Transaction tx = null;
//
//        try {
//            tx = entityManager.beginTransaction();
//            Criteria crit = entityManager.createCriteria(clazz).add(criterion);
//            tx.commit();
//            return crit.uniqueResult();
//        } catch (HibernateException e) {
//            tx.rollback();
//            throw e;
//        }
//    }
//
//    public Object load(Class clazz, Criterion criterion, StatelessSession entityManager) {
//        Transaction tx = null;
//
//        try {
//            tx = entityManager.beginTransaction();
//            Criteria crit = entityManager.createCriteria(clazz).add(criterion);
//            tx.commit();
//            return crit.uniqueResult();
//        } catch (HibernateException e) {
//            tx.rollback();
//            throw e;
//        }
//    }

    public void delete(Object object, EntityManager entityManager) {
        executeInsideTransaction(em -> em.remove(em.contains(object) ? object : em.merge(object)), entityManager);
    }

    public <C> C load(EntityManager entityManager, CriteriaBuilderQueryRoot<C> criteriaBuilderQueryRoot, Predicate[] predicate) {
        criteriaBuilderQueryRoot.getCriteriaQuery().select(criteriaBuilderQueryRoot.getRoot());

        criteriaBuilderQueryRoot.getCriteriaQuery().where(predicate);

        return entityManager.createQuery(criteriaBuilderQueryRoot.getCriteriaQuery()).getSingleResult();
    }

//    public Object load(Class clazz, Criterion[] criterions, EntityManager entityManager) {
//        Transaction tx = null;
//        try {
//            tx = entityManager.beginTransaction();
//            Criteria criteria = entityManager.createCriteria(clazz);
//            for (int i = 0; i < criterions.length; i++)
//                criteria.add(criterions[i]);
//
//            tx.commit();
//
//            return criteria.uniqueResult();
//        } catch (HibernateException e) {
//            tx.rollback();
//            throw e;
//        }
//    }

//    public Object load(Class clazz, Criterion[] criterions, StatelessSession entityManager) {
//        Transaction tx = null;
//        try {
//            tx = entityManager.beginTransaction();
//            Criteria criteria = entityManager.createCriteria(clazz);
//            for (int i = 0; i < criterions.length; i++)
//                criteria.add(criterions[i]);
//
//            tx.commit();
//
//            return criteria.uniqueResult();
//        } catch (HibernateException e) {
//            tx.rollback();
//            throw e;
//        }
//    }

//    public List getDistinctForColumn(Class clazz, Criterion[] criterions, EntityManager entityManager) {
//        Transaction tx = null;
//        try {
//            tx = entityManager.beginTransaction();
//            Criteria criteria = entityManager.createCriteria(clazz);
//            for (int i = 0; i < criterions.length; i++)
//                criteria.add(criterions[i]);
//
//            tx.commit();
//            return criteria.list();
//        } catch (HibernateException e) {
//            tx.rollback();
//            throw e;
//        }
//    }

    public void deleteTask(Object object, EntityManager entityManager) {
        executeInsideTransaction(em -> em.remove(em.contains(object) ? object : em.merge(object)), entityManager);
    }

    public <C> CriteriaBuilderQueryRoot<C> getCriteriaBuilderQueryRoot(Class<C> persistentClass, EntityManager entityManager) {
      return new CriteriaBuilderQueryRoot<C>(persistentClass, entityManager);
  }

    public class CriteriaBuilderQueryRoot<C> {
        public CriteriaBuilder getBuilder() {
            return builder;
        }

        public CriteriaQuery<C> getCriteriaQuery() {
            return criteriaQuery;
        }

        public Root<C> getRoot() {
            return root;
        }

        private CriteriaBuilder builder;
        private CriteriaQuery<C> criteriaQuery;
        private Root<C> root;
    
        public CriteriaBuilderQueryRoot(Class<C> persistentClass, EntityManager entityManager) {
            builder = entityManager.getCriteriaBuilder();
            criteriaQuery = builder.createQuery(persistentClass);
            root = criteriaQuery.from(persistentClass);
        }    
    }

    public void executeInsideTransaction(Consumer<EntityManager> action, EntityManager entityManager) {
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
            //
        }
    }
}
