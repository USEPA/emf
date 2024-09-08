package gov.epa.emissions.framework.services.data;

import gov.epa.emissions.commons.db.intendeduse.IntendedUse;
import gov.epa.emissions.framework.services.persistence.HibernateFacade;
import gov.epa.emissions.framework.services.persistence.HibernateFacade.CriteriaBuilderQueryRoot;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Root;

import org.hibernate.Session;

public class IntendedUsesDAO {

    private HibernateFacade hibernateFacade;

    public IntendedUsesDAO() {
        hibernateFacade = new HibernateFacade();
    }

    public List getAll(Session session) {
        CriteriaBuilderQueryRoot<IntendedUse> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(IntendedUse.class, session);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<IntendedUse> root = criteriaBuilderQueryRoot.getRoot();

        return hibernateFacade.getAll(criteriaBuilderQueryRoot, builder.asc(root.get("name")), session);
    }
    
    public IntendedUse getIntendedUse(String name, Session session) {
        return hibernateFacade.load(IntendedUse.class, "name", name, session);
    }
    
    public void add(IntendedUse intendedUse, Session session) {
        hibernateFacade.add(intendedUse, session);
    }
    
}