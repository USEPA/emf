package gov.epa.emissions.framework.services.data;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Root;

import javax.persistence.EntityManager;

import gov.epa.emissions.commons.data.Country;
import gov.epa.emissions.framework.services.persistence.HibernateFacade;
import gov.epa.emissions.framework.services.persistence.HibernateFacade.CriteriaBuilderQueryRoot;

public class CountriesDAO {

    private HibernateFacade hibernateFacade;

    public CountriesDAO() {
        hibernateFacade = new HibernateFacade();
    }

    public List<Country> getAll(EntityManager entityManager) {
        CriteriaBuilderQueryRoot<Country> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(Country.class, entityManager);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<Country> root = criteriaBuilderQueryRoot.getRoot();

        return hibernateFacade.getAll(criteriaBuilderQueryRoot, builder.asc(root.get("name")), entityManager);
    }
    
    public Country getCountry(String name, EntityManager entityManager) {
        return hibernateFacade.load(Country.class, "name", name, entityManager);
    }
    
    public Country addCountry(Country country, EntityManager entityManager) {
        hibernateFacade.add(country, entityManager);
        return getCountry(country.getName(), entityManager);
    }

}