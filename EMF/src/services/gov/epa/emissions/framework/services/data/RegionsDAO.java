package gov.epa.emissions.framework.services.data;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Root;

import org.hibernate.Session;

import gov.epa.emissions.commons.data.Region;
import gov.epa.emissions.framework.services.persistence.HibernateFacade;
import gov.epa.emissions.framework.services.persistence.HibernateFacade.CriteriaBuilderQueryRoot;

public class RegionsDAO {

    private HibernateFacade hibernateFacade;

    public RegionsDAO() {
        hibernateFacade = new HibernateFacade();
    }

    public List getAll(Session session) {
        CriteriaBuilderQueryRoot<Region> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(Region.class, session);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<Region> root = criteriaBuilderQueryRoot.getRoot();

        return hibernateFacade.getAll(criteriaBuilderQueryRoot, builder.asc(root.get("name")), session);
    }
    
    public Region getRegion(String name, Session session) {
        return hibernateFacade.load(Region.class, "name", name, session);
    }
    
    public Region addRegion(Region region, Session session) {
        hibernateFacade.add(region, session);
        return getRegion(region.getName(), session);
    }

}