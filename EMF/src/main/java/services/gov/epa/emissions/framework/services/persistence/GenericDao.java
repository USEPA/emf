package gov.epa.emissions.framework.services.persistence;

import java.util.List;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;

public interface GenericDao<E, K> {

	K add(E entity);
    
	void update(E entity);

    void remove(E entity);

    void flush();

    void clear();

	E find(K key);

	E get(K key);

	List<E> list();

	List<E> all();

	E merge(E entity);

	List<E> list(Order order);

	E get(String name);

	boolean exists(K key);

	boolean exists(String propertyName, Object value);

	List<E> list(Criterion[] criterions);

	List<E> list(Criterion[] criterions, Order[] orders);

	List<E> list(Criterion criterion, Order order);

}