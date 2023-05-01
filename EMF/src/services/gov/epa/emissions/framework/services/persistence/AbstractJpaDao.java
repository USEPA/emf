package gov.epa.emissions.framework.services.persistence;

import java.io.Serializable;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

public abstract class AbstractJpaDao<T extends Serializable> {
    private Class<T> clazz;

    @PersistenceContext(unitName = "entityManagerFactory")
    protected EntityManager entityManager;

    public final void setClazz(final Class<T> clazzToSet) {
        this.clazz = clazzToSet;
    }

    public T findOne(final int id) {
        return entityManager.find(clazz, id);
    }

    public T findByName(final String name) {
        List<T> listT = entityManager
                .createQuery("SELECT t FROM " + clazz.getName() + " t WHERE t.name = :name", clazz)
                .setParameter("name", name)
                .getResultList();
        return listT.size() > 0 ? listT.get(0) : null;
    }

    @SuppressWarnings("unchecked")
    public List<T> findAll() {
        return entityManager.createQuery("from " + clazz.getName()).getResultList();
    }

    public T create(final T entity) {
        entityManager.persist(entity);
        return entity;
    }

    public T update(final T entity) {
        return entityManager.merge(entity);
    }

    public void delete(final T entity) {
        entityManager.remove(entity);
    }

    public void deleteById(final int entityId) {
        final T entity = findOne(entityId);
        delete(entity);
    }
}