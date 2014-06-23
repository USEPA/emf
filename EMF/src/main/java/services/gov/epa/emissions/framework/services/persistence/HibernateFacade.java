package gov.epa.emissions.framework.services.persistence;

import java.io.Serializable;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.StatelessSession;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

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

    public boolean exists(int id, Class clazz, Session session) {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            Criteria crit = session.createCriteria(clazz).add(Restrictions.eq("id", new Integer(id)));
            tx.commit();

            return crit.uniqueResult() != null;
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }

    public boolean exists(Class clazz, Criterion[] criterions, Session session) {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            Criteria criteria = session.createCriteria(clazz);
            for (int i = 0; i < criterions.length; i++)
                criteria.add(criterions[i]);

            tx.commit();

            return criteria.uniqueResult() != null;
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }

    public boolean exists(Class clazz, Criterion[] criterions, StatelessSession session) {
        try {
            Criteria criteria = session.createCriteria(clazz);
            for (int i = 0; i < criterions.length; i++)
                criteria.add(criterions[i]);

            return criteria.uniqueResult() != null;
        } catch (HibernateException e) {
            throw e;
        }
    }

    public Object current(int id, Class clazz, Session session) {
        try {
            Criteria crit = session.createCriteria(clazz).add(Restrictions.eq("id", new Integer(id)));
            return crit.uniqueResult();
        } catch (HibernateException e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
        }
        
        return null;
    }

    public boolean isUsed(String key, String value, Class clazz, Session session) {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            Criteria crit = session.createCriteria(clazz).add(Restrictions.eq(key, value));
            boolean result = crit.uniqueResult() != null;
            tx.commit();

            return result;
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }
    
    public boolean nameUsed(String name, Class clazz, Session session) {
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

    public List getAll(Class clazz, Order order, Session session) {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            List list = session.createCriteria(clazz).addOrder(order).list();
            tx.commit();
            return list;
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }

    public List getAll(Class clazz, Session session) {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            List list = session.createCriteria(clazz).list();
            tx.commit();

            return list;
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }

    public boolean exists(String name, Class clazz, Session session) {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            Criteria crit = session.createCriteria(clazz).add(Restrictions.eq("name", name));
            tx.commit();

            return crit.uniqueResult() != null;
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }

    public List get(Class clazz, Criterion criterion, Order order, Session session) {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            List list = session.createCriteria(clazz).add(criterion).addOrder(order).list();
            tx.commit();
            return list;
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }

    public List get(Class clazz, Criterion criterion, Session session) {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            List list = session.createCriteria(clazz).add(criterion).list();
            tx.commit();
            return list;
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }

    public Object get(Class clazz, Criterion criterion, StatelessSession session) {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            Object obj = session.get(clazz, criterion);
            tx.commit();
            return obj;
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }

    public List get(Class clazz, Criterion[] criterions, Session session) {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            Criteria criteria = session.createCriteria(clazz);
            for (int i = 0; i < criterions.length; i++)
                criteria.add(criterions[i]);

            tx.commit();
            return criteria.list();
        } catch (HibernateException e) {
            e.printStackTrace();
            tx.rollback();
            throw e;
        }
    }

    public Object load(Class clazz, Criterion criterion, Session session) {
        Transaction tx = null;

        try {
            tx = session.beginTransaction();
            Criteria crit = session.createCriteria(clazz).add(criterion);
            tx.commit();
            return crit.uniqueResult();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }

    public Object load(Class clazz, Criterion criterion, StatelessSession session) {
        Transaction tx = null;

        try {
            tx = session.beginTransaction();
            Criteria crit = session.createCriteria(clazz).add(criterion);
            tx.commit();
            return crit.uniqueResult();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }

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

    public Object load(Class clazz, Criterion[] criterions, Session session) {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            Criteria criteria = session.createCriteria(clazz);
            for (int i = 0; i < criterions.length; i++)
                criteria.add(criterions[i]);

            tx.commit();

            return criteria.uniqueResult();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }

    public Object load(Class clazz, Criterion[] criterions, StatelessSession session) {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            Criteria criteria = session.createCriteria(clazz);
            for (int i = 0; i < criterions.length; i++)
                criteria.add(criterions[i]);

            tx.commit();

            return criteria.uniqueResult();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }

    public List getDistinctForColumn(Class clazz, Criterion[] criterions, Session session) {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            Criteria criteria = session.createCriteria(clazz);
            for (int i = 0; i < criterions.length; i++)
                criteria.add(criterions[i]);

            tx.commit();
            return criteria.list();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }

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

}
