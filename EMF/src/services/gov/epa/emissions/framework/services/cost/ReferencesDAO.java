package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.commons.data.Reference;
import gov.epa.emissions.framework.services.persistence.HibernateFacade;
import gov.epa.emissions.framework.services.persistence.JpaEntityManagerFactory;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

public class ReferencesDAO {

    private HibernateFacade hibernateFacade;

    private EntityManager entityManager;

    public ReferencesDAO() {
        hibernateFacade = new HibernateFacade();
    }

    private void addObject(Object obj, EntityManager entityManager) {
        hibernateFacade.add(obj, entityManager);
    }

    public void addReference(Reference reference, EntityManager entityManager) {
        addObject(reference, entityManager);
    }

    public List<Reference> getReferences(EntityManager entityManager) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Reference> criteriaQuery = builder.createQuery(Reference.class);
        Root<Reference> root = criteriaQuery.from(Reference.class);

        criteriaQuery.select(root);
        criteriaQuery.orderBy(builder.asc(root.get("description")));

        return entityManager.createQuery(criteriaQuery).getResultList();
    }

    public List<Reference> getReferences(EntityManager entityManager, String textContains) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Reference> criteriaQuery = builder.createQuery(Reference.class);
        Root<Reference> root = criteriaQuery.from(Reference.class);

        criteriaQuery.select(root);
        criteriaQuery.where(builder.like(builder.lower(root.get("description")), textContains.toLowerCase()));
        criteriaQuery.orderBy(builder.asc(root.get("description")));

        return entityManager.createQuery(criteriaQuery).getResultList();
    }

    public boolean canUpdate(Reference reference, EntityManager entityManager) {
        if (!exists(reference.getId(), entityManager)) {
            return false;
        }

        Reference current = current(reference.getId(), entityManager);
        // The current object is saved in the entityManager. Hibernate cannot persist our
        // object with the same id.
        entityManager.clear();
        if (current.getDescription().equals(reference.getDescription()))
            return true;

        return !descriptionUsed(reference.getDescription(), entityManager);
    }

    public int getReferenceCount(EntityManager entityManager) {
        List<?> num = entityManager.createQuery("SELECT COUNT(ref.id) from Reference as ref").getResultList();
        return Integer.parseInt(num.get(0).toString());
    }

    public int getReferenceCount(EntityManager entityManager, String text) {
        List<?> num = entityManager.createQuery(
                "SELECT COUNT(ref.id) from Reference as ref where lower(ref.description) like '%%"
                        + text.toLowerCase().trim() + "%%'").getResultList();
        return Integer.parseInt(num.get(0).toString());
    }

    public boolean descriptionUsed(String description, EntityManager entityManager) {
        return hibernateFacade.isUsed("description", description, Reference.class, entityManager);
    }

    private Reference current(int id, EntityManager entityManager) {
        return hibernateFacade.current(id, Reference.class, entityManager);
    }

    private boolean exists(int id, EntityManager entityManager) {
        return hibernateFacade.exists(id, Reference.class, entityManager);
    }

    public EntityManager getSession() {

        if (this.entityManager == null) {
            this.entityManager = JpaEntityManagerFactory.get().createEntityManager();
        }

        return this.entityManager;
    }
}
