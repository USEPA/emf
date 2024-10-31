package gov.epa.emissions.framework.services.data;

import gov.epa.emissions.commons.db.intendeduse.IntendedUse;
import gov.epa.emissions.framework.services.persistence.HibernateFacade;
import gov.epa.emissions.framework.services.persistence.HibernateFacade.CriteriaBuilderQueryRoot;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Root;

import javax.persistence.EntityManager;

public class IntendedUsesDAO {

    private HibernateFacade hibernateFacade;

    public IntendedUsesDAO() {
        hibernateFacade = new HibernateFacade();
    }

    public List getAll(EntityManager entityManager) {
        CriteriaBuilderQueryRoot<IntendedUse> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(IntendedUse.class, entityManager);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<IntendedUse> root = criteriaBuilderQueryRoot.getRoot();

        return hibernateFacade.getAll(criteriaBuilderQueryRoot, builder.asc(root.get("name")), entityManager);
    }
    
    public IntendedUse getIntendedUse(String name, EntityManager entityManager) {
        return hibernateFacade.load(IntendedUse.class, "name", name, entityManager);
    }
    
    public void add(IntendedUse intendedUse, EntityManager entityManager) {
        hibernateFacade.add(intendedUse, entityManager);
    }
    
}