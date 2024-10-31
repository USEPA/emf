package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.framework.services.cost.data.ControlTechnology;
import gov.epa.emissions.framework.services.persistence.HibernateFacade;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import javax.persistence.EntityManager;

public class ControlTechnologiesDAO {

    private HibernateFacade hibernateFacade;

    public ControlTechnologiesDAO() {
        hibernateFacade = new HibernateFacade();
    }

    private void addObject(Object obj, EntityManager entityManager) {
        hibernateFacade.add(obj, entityManager);
    }

    public void addControlTechnology(ControlTechnology technology, EntityManager entityManager) {
        addObject(technology, entityManager);
    }

    public List<ControlTechnology> getAll(EntityManager entityManager) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<ControlTechnology> criteriaQuery = builder.createQuery(ControlTechnology.class);
        Root<ControlTechnology> root = criteriaQuery.from(ControlTechnology.class);

        criteriaQuery.select(root);
        criteriaQuery.orderBy(builder.asc(root.get("name")));

        return entityManager.createQuery(criteriaQuery).getResultList();
    }

    public boolean canUpdate(ControlTechnology technology, EntityManager entityManager) {
        if (!exists(technology.getId(), entityManager)) {
            return false;
        }

        ControlTechnology current = current(technology.getId(), entityManager);
        // The current object is saved in the entityManager. Hibernate cannot persist our
        // object with the same id.
        entityManager.clear();
        if (current.getName().equals(technology.getName()))
            return true;

        return !nameUsed(technology.getName(), entityManager);
    }

    private boolean nameUsed(String name, EntityManager entityManager) {
        return hibernateFacade.nameUsed(name, ControlTechnology.class, entityManager);
    }

    private ControlTechnology current(int id, EntityManager entityManager) {
        return hibernateFacade.current(id, ControlTechnology.class, entityManager);
    }

    private boolean exists(int id, EntityManager entityManager) {
        return hibernateFacade.exists(id, ControlTechnology.class, entityManager);
    }

}
