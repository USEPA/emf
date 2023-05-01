package gov.epa.emissions.framework.services.persistence;

import java.io.Serializable;
import java.util.List;

public interface IGenericDao<T extends Serializable> {
    void setClazz(Class< T > clazzToSet);

    T findOne(final int id);

    T findByName(final String name);

    List<T> findAll();

    T create(final T entity);

    T update(final T entity);

    void delete(final T entity);

    void deleteById(final int entityId);
}