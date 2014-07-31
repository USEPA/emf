package gov.epa.emissions.framework.services.persistence;

import java.io.Serializable;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * Basic DAO operations dependent with Hibernate's specific classes
 * @see SessionFactory
 */
//@Transactional(propagation= Propagation.REQUIRED, readOnly=false)
@Repository("genericHibernateDao")
public abstract class GenericHibernateDao<E, K extends Serializable> implements GenericDao<E, K> {

    private SessionFactory sessionFactory;
    protected Class<? extends E> daoType;

    public GenericHibernateDao(Class<E> daoType) {
//Class<E> daoType    	
    	this.daoType = daoType;
//        daoType = (Class<E>) ((ParameterizedType) getClass().getGenericSuperclass())
//                        .getActualTypeArguments()[0];
    }

//    public GenericHibernateDao() {
//		//
//	}

	public GenericHibernateDao() {
		// TODO Auto-generated constructor stub
	}

	@Autowired
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	protected Session currentSession() {
        return sessionFactory.getCurrentSession();
    }

    @SuppressWarnings("unchecked")
	@Override
    public K add(E entity) {
        return (K) currentSession().save(entity);
    }

    @Override
    public void update(E entity) {
        currentSession().update(entity);
//        currentSession().flush();
    }
 
    @SuppressWarnings("unchecked")
	@Override
    public E merge(E entity) {
        return (E) currentSession().merge(entity);
    }

    @Override
    public void remove(E entity) {
        currentSession().delete(entity);
    }

    @Override
    public void flush() {
        currentSession().flush();
    }

    @Override
    public void clear() {
        currentSession().clear();
    }


    @SuppressWarnings("unchecked")
	@Override
    public E find(K key) {
        return (E) currentSession().get(daoType, key);
    }

    @SuppressWarnings("unchecked")
	@Override
    public E get(K key) {
        return (E) currentSession().get(daoType, key);
    }

    @SuppressWarnings("unchecked")
	@Override
    public E get(String name) {
        return (E) currentSession().createCriteria(daoType).add(Restrictions.eq("name", name)).uniqueResult();
    }

    @SuppressWarnings("unchecked")
	@Override
    public List<E> list() {
        return currentSession().createCriteria(daoType).list();
    }

    @SuppressWarnings("unchecked")
	@Override
    public List<E> list(Order order) {
        return currentSession().createCriteria(daoType).addOrder(order).list();
    }

    @SuppressWarnings("unchecked")
	@Override
    public List<E> list(Criterion criterion, Order order) {
        return currentSession().createCriteria(daoType).add(criterion).addOrder(order).list();
    }

    @SuppressWarnings("unchecked")
	@Override
    public List<E> list(Criterion[] criterions) {
    	Criteria criteria = currentSession().createCriteria(daoType);
    	for (Criterion criterion : criterions)
    		criteria.add(criterion);
        return criteria.list();
    }
    
    @SuppressWarnings("unchecked")
	@Override
    public List<E> list(Criterion[] criterions, Order[] orders) {
    	Criteria criteria = currentSession().createCriteria(daoType);
    	for (Criterion criterion : criterions)
    		criteria.add(criterion);
    	for (Order order : orders)
    		criteria.addOrder(order);
        return criteria.list();
    }
    
    @SuppressWarnings("unchecked")
	@Override
    public List<E> all() {
        return currentSession().createCriteria(daoType).list();
    }
    
	@Override
    public boolean exists(K key) {
        Criteria crit = currentSession().createCriteria(daoType).add(Restrictions.eq("id", key));

        return crit.uniqueResult() != null;
    }
	
	@Override
    public boolean exists(String propertyName, Object value) {
	    Criteria crit = currentSession().createCriteria(daoType).add(Restrictions.eq(propertyName, value));
	    return crit.uniqueResult() != null;
    }
	
}