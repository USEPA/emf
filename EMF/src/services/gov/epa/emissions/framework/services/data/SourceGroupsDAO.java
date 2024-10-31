package gov.epa.emissions.framework.services.data;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Root;

import javax.persistence.EntityManager;

import gov.epa.emissions.commons.data.SourceGroup;
import gov.epa.emissions.framework.services.persistence.HibernateFacade;
import gov.epa.emissions.framework.services.persistence.HibernateFacade.CriteriaBuilderQueryRoot;

public class SourceGroupsDAO {

    private HibernateFacade hibernateFacade;

    public SourceGroupsDAO() {
        hibernateFacade = new HibernateFacade();
    }

    public List<SourceGroup> getAll(EntityManager entityManager) {
        CriteriaBuilderQueryRoot<SourceGroup> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(SourceGroup.class, entityManager);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<SourceGroup> root = criteriaBuilderQueryRoot.getRoot();

        return hibernateFacade.getAll(criteriaBuilderQueryRoot, builder.asc(root.get("name")), entityManager);
    }

    public boolean canUpdate(SourceGroup sourcegrp, EntityManager entityManager) {
        if (!exists(sourcegrp.getId(), entityManager)) {
            return false;
        }

        SourceGroup current = current(sourcegrp.getId(), entityManager);
        // The current object is saved in the entityManager. Hibernate cannot persist our
        // object with the same id.
        entityManager.clear();
        if (current.getName().equals(sourcegrp.getName()))
            return true;

        return !nameUsed(sourcegrp.getName(), entityManager);
    }

    private boolean nameUsed(String name, EntityManager entityManager) {
        return hibernateFacade.nameUsed(name, SourceGroup.class, entityManager);
    }

    private SourceGroup current(int id, EntityManager entityManager) {
        return hibernateFacade.current(id, SourceGroup.class, entityManager);
    }

    private boolean exists(int id, EntityManager entityManager) {
        return hibernateFacade.exists(id, SourceGroup.class, entityManager);
    }

}
