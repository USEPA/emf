package gov.epa.emissions.framework.services.persistence;

import java.io.Serializable;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.StatelessSession;
import org.hibernate.Transaction;

public class HibernateFacade {

    public Serializable add(Object obj, Session session) {
        Transaction tx = null;
        Serializable id;
        try {
            tx = session.beginTransaction();
            id = session.save(obj);
            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();  //TODO: JIZHEN BUG3316
            throw e;
        }
        return id;
    }

    public Serializable add(Object obj, StatelessSession session) {
        Transaction tx = null;
        Serializable id;
        try {
            tx = session.beginTransaction();
            id = session.insert(obj);
            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
        return id;
    }

    public void add(Object[] objects, Session session) {
        for (int i = 0; i < objects.length; i++)
            add(objects[i], session);
    }

    public <C> boolean exists(int id, Class<C> clazz, Session session) {
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<C> criteriaQuery = builder.createQuery(clazz);
        Root<C> root = criteriaQuery.from(clazz);

        criteriaQuery.select(root);

        criteriaQuery.where(builder.equal(root.get("id"), Integer.valueOf(id)));

        return session.createQuery(criteriaQuery).getSingleResult() != null;
    }

    public <C> boolean exists(CriteriaBuilderQueryRoot<C> criteriaBuilderQueryRoot, Predicate[] predicates, Session session) {
        criteriaBuilderQueryRoot.getCriteriaQuery().select(criteriaBuilderQueryRoot.getRoot());

        criteriaBuilderQueryRoot.getCriteriaQuery().where(predicates);

        return session.createQuery(criteriaBuilderQueryRoot.getCriteriaQuery()).getSingleResult() != null;
    }

    public <C> C current(int id, Class<C> clazz, Session session) {
        return load(clazz, "id", Integer.valueOf(id), session);
    }

    public <C,K> boolean isUsed(String key, K value, Class<C> clazz, Session session) {
        return load(clazz, key, value, session) != null;
    }
    
    public <C> boolean nameUsed(String name, Class<C> clazz, Session session) {
        return this.isUsed("name", name, clazz, session);
    }

    // save- new object, updates if the objects already exist
    public void saveOrUpdate(Object object, Session session) {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.saveOrUpdate(object);
            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }

    public void updateOnly(Object object, Session session) {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.update(object);
            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }

    public void updateOnly(Object object, StatelessSession session) {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.update(object);
            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }

    public void update(Object[] objects, Session session) {
        for (int i = 0; i < objects.length; i++)
            saveOrUpdate(objects[i], session);
    }

    public void removeObjects(Object[] objects, Session session) {
        Transaction tx = null;
        if ((session == null) || (!session.isConnected())){
            throw new IllegalArgumentException("Session null or not connected in removeObjects");
        }
        try {
            tx = session.beginTransaction();

            for (Object obj : objects)
                session.delete(obj);
            
            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();
            e.printStackTrace();
            throw e;
        }
    }

    public void remove(Object[] objects, Session session) {
        for (int i = 0; i < objects.length; i++)
            remove(objects[i], session);
    }

    public void remove(Object obj, Session session) {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.delete(obj);
            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }

    }

    public <C> List<C> getAll(CriteriaBuilderQueryRoot<C> criteriaBuilderQueryRoot, Order order, Session session) {
        criteriaBuilderQueryRoot.getCriteriaQuery().select(criteriaBuilderQueryRoot.getRoot());
        criteriaBuilderQueryRoot.getCriteriaQuery().orderBy(order);

        return session.createQuery(criteriaBuilderQueryRoot.getCriteriaQuery()).getResultList();
    }

    public <C> List<C> getAll(CriteriaBuilderQueryRoot<C> criteriaBuilderQueryRoot, Session session) {
        criteriaBuilderQueryRoot.getCriteriaQuery().select(criteriaBuilderQueryRoot.getRoot());

        return session.createQuery(criteriaBuilderQueryRoot.getCriteriaQuery()).getResultList();
    }

    public <C> boolean exists(String name, Class<C> clazz, Session session) {
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<C> criteriaQuery = builder.createQuery(clazz);
        Root<C> root = criteriaQuery.from(clazz);

        criteriaQuery.select(root);

        criteriaQuery.where(builder.equal(root.get("name"), name));

        return session.createQuery(criteriaQuery).getSingleResult() != null;
    }

    public <C> List<C> get(CriteriaBuilderQueryRoot<C> criteriaBuilderQueryRoot, Predicate predicate, javax.persistence.criteria.Order order, Session session) {
        criteriaBuilderQueryRoot.getCriteriaQuery().select(criteriaBuilderQueryRoot.getRoot());
        criteriaBuilderQueryRoot.getCriteriaQuery().where(predicate);
        criteriaBuilderQueryRoot.getCriteriaQuery().orderBy(order);

        return session.createQuery(criteriaBuilderQueryRoot.getCriteriaQuery()).getResultList();
    }

    public <C,K> List<C> get(Class<C> persistentClass, String keyName, K keyValue, Session session) {
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<C> criteriaQuery = builder.createQuery(persistentClass);
        Root<C> root = criteriaQuery.from(persistentClass);

        criteriaQuery.select(root);

        criteriaQuery.where(builder.equal(root.get(keyName), keyValue));

        return session.createQuery(criteriaQuery).getResultList();
    }
    
    public <C> List<C> get(Session session, CriteriaBuilderQueryRoot<C> criteriaBuilderQueryRoot, Predicate predicate) {
        criteriaBuilderQueryRoot.getCriteriaQuery().select(criteriaBuilderQueryRoot.getRoot());

        criteriaBuilderQueryRoot.getCriteriaQuery().where(predicate);

        return session.createQuery(criteriaBuilderQueryRoot.getCriteriaQuery()).getResultList();
    }

//    public List get(Class clazz, Criterion criterion, Session session) {
//        Transaction tx = null;
//        try {
//            tx = session.beginTransaction();
//            List list = session.createCriteria(clazz).add(criterion).list();
//            tx.commit();
//            return list;
//        } catch (HibernateException e) {
//            tx.rollback();
//            throw e;
//        }
//    }

//    public Object get(Class clazz, Criterion criterion, StatelessSession session) {
//        Transaction tx = null;
//        try {
//            tx = session.beginTransaction();
//            Object obj = session.get(clazz, criterion);
//            tx.commit();
//            return obj;
//        } catch (HibernateException e) {
//            tx.rollback();
//            throw e;
//        }
//    }
//
    public <C> List<C> get(CriteriaBuilderQueryRoot<C> criteriaBuilderQueryRoot, Predicate[] predicate, Session session) {
        criteriaBuilderQueryRoot.getCriteriaQuery().select(criteriaBuilderQueryRoot.getRoot());

        criteriaBuilderQueryRoot.getCriteriaQuery().where(predicate);

        return session.createQuery(criteriaBuilderQueryRoot.getCriteriaQuery()).getResultList();
    }

    public <C> List<C> get(CriteriaBuilderQueryRoot<C> criteriaBuilderQueryRoot, Predicate[] predicate, Order order, Session session) {
        criteriaBuilderQueryRoot.getCriteriaQuery().select(criteriaBuilderQueryRoot.getRoot());

        criteriaBuilderQueryRoot.getCriteriaQuery().where(predicate);
        criteriaBuilderQueryRoot.getCriteriaQuery().orderBy(order);

        return session.createQuery(criteriaBuilderQueryRoot.getCriteriaQuery()).getResultList();
    }

//    public List get(Class clazz, Criterion[] criterions, Session session) {
//        Transaction tx = null;
//        try {
//            tx = session.beginTransaction();
//            Criteria criteria = session.createCriteria(clazz);
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

    public <C,K> C load(Class<C> persistentClass, String keyName, K keyValue, Session session) {
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<C> criteriaQuery = builder.createQuery(persistentClass);
        Root<C> root = criteriaQuery.from(persistentClass);

        criteriaQuery.select(root);

        criteriaQuery.where(builder.equal(root.get(keyName), keyValue));

        return session.createQuery(criteriaQuery).getSingleResult();
    }
    
    public <C> C load(Session session, CriteriaBuilderQueryRoot<C> criteriaBuilderQueryRoot, Predicate predicate) {
        criteriaBuilderQueryRoot.getCriteriaQuery().select(criteriaBuilderQueryRoot.getRoot());

        criteriaBuilderQueryRoot.getCriteriaQuery().where(predicate);

        return session.createQuery(criteriaBuilderQueryRoot.getCriteriaQuery()).getSingleResult();
    }

//    
//    public Object load(Class clazz, Criterion criterion, Session session) {
//        Transaction tx = null;
//
//        try {
//            tx = session.beginTransaction();
//            Criteria crit = session.createCriteria(clazz).add(criterion);
//            tx.commit();
//            return crit.uniqueResult();
//        } catch (HibernateException e) {
//            tx.rollback();
//            throw e;
//        }
//    }
//
//    public Object load(Class clazz, Criterion criterion, StatelessSession session) {
//        Transaction tx = null;
//
//        try {
//            tx = session.beginTransaction();
//            Criteria crit = session.createCriteria(clazz).add(criterion);
//            tx.commit();
//            return crit.uniqueResult();
//        } catch (HibernateException e) {
//            tx.rollback();
//            throw e;
//        }
//    }

    public void delete(Object object, Session session) {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.delete(object);
            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }

    public <C> C load(Session session, CriteriaBuilderQueryRoot<C> criteriaBuilderQueryRoot, Predicate[] predicate) {
        criteriaBuilderQueryRoot.getCriteriaQuery().select(criteriaBuilderQueryRoot.getRoot());

        criteriaBuilderQueryRoot.getCriteriaQuery().where(predicate);

        return session.createQuery(criteriaBuilderQueryRoot.getCriteriaQuery()).getSingleResult();
    }

//    public Object load(Class clazz, Criterion[] criterions, Session session) {
//        Transaction tx = null;
//        try {
//            tx = session.beginTransaction();
//            Criteria criteria = session.createCriteria(clazz);
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

//    public Object load(Class clazz, Criterion[] criterions, StatelessSession session) {
//        Transaction tx = null;
//        try {
//            tx = session.beginTransaction();
//            Criteria criteria = session.createCriteria(clazz);
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

//    public List getDistinctForColumn(Class clazz, Criterion[] criterions, Session session) {
//        Transaction tx = null;
//        try {
//            tx = session.beginTransaction();
//            Criteria criteria = session.createCriteria(clazz);
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

    public void deleteTask(Object object, Session session) {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.delete(object);
            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }

    }

    public <C> CriteriaBuilderQueryRoot<C> getCriteriaBuilderQueryRoot(Class<C> persistentClass, Session session) {
      return new CriteriaBuilderQueryRoot<C>(persistentClass, session);
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
    
        public CriteriaBuilderQueryRoot(Class<C> persistentClass, Session session) {
            builder = session.getCriteriaBuilder();
            criteriaQuery = builder.createQuery(persistentClass);
            root = criteriaQuery.from(persistentClass);
        }    
    }
}
