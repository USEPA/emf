package gov.epa.emissions.framework.services.data;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Root;

import javax.persistence.EntityManager;

import gov.epa.emissions.commons.data.Pollutant;
import gov.epa.emissions.framework.services.persistence.HibernateFacade;
import gov.epa.emissions.framework.services.persistence.HibernateFacade.CriteriaBuilderQueryRoot;

public class PollutantsDAO {

    private HibernateFacade hibernateFacade;

    public PollutantsDAO() {
        hibernateFacade = new HibernateFacade();
    }

    public List<Pollutant> getAll(EntityManager entityManager) {
        CriteriaBuilderQueryRoot<Pollutant> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(Pollutant.class, entityManager);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<Pollutant> root = criteriaBuilderQueryRoot.getRoot();

        return hibernateFacade.getAll(criteriaBuilderQueryRoot, builder.asc(root.get("name")), entityManager);
    }

    public boolean canUpdate(Pollutant pollutant, EntityManager entityManager) {
        if (!exists(pollutant.getId(), entityManager)) {
            return false;
        }

        Pollutant current = current(pollutant.getId(), entityManager);
        // The current object is saved in the entityManager. Hibernate cannot persist our
        // object with the same id.
        entityManager.clear();
        if (current.getName().equals(pollutant.getName()))
            return true;

        return !nameUsed(pollutant.getName(), entityManager);
    }

    private boolean nameUsed(String name, EntityManager entityManager) {
        return hibernateFacade.nameUsed(name, Pollutant.class, entityManager);
    }

    private Pollutant current(int id, EntityManager entityManager) {
        return hibernateFacade.current(id, Pollutant.class, entityManager);
    }

    private boolean exists(int id, EntityManager entityManager) {
        return hibernateFacade.exists(id, Pollutant.class, entityManager);
    }

}
