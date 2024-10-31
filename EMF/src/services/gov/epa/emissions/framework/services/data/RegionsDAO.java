package gov.epa.emissions.framework.services.data;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Root;

import javax.persistence.EntityManager;

import gov.epa.emissions.commons.data.Region;
import gov.epa.emissions.framework.services.persistence.HibernateFacade;
import gov.epa.emissions.framework.services.persistence.HibernateFacade.CriteriaBuilderQueryRoot;

public class RegionsDAO {

    private HibernateFacade hibernateFacade;

    public RegionsDAO() {
        hibernateFacade = new HibernateFacade();
    }

    public List getAll(EntityManager entityManager) {
        CriteriaBuilderQueryRoot<Region> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(Region.class, entityManager);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<Region> root = criteriaBuilderQueryRoot.getRoot();

        return hibernateFacade.getAll(criteriaBuilderQueryRoot, builder.asc(root.get("name")), entityManager);
    }
    
    public Region getRegion(String name, EntityManager entityManager) {
        return hibernateFacade.load(Region.class, "name", name, entityManager);
    }
    
    public Region addRegion(Region region, EntityManager entityManager) {
        hibernateFacade.add(region, entityManager);
        return getRegion(region.getName(), entityManager);
    }

}