package gov.epa.emissions.framework.services.data;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Root;

import org.hibernate.Session;

import gov.epa.emissions.commons.data.Country;
import gov.epa.emissions.framework.services.persistence.HibernateFacade;
import gov.epa.emissions.framework.services.persistence.HibernateFacade.CriteriaBuilderQueryRoot;

public class CountriesDAO {

    private HibernateFacade hibernateFacade;

    public CountriesDAO() {
        hibernateFacade = new HibernateFacade();
    }

    public List<Country> getAll(Session session) {
        CriteriaBuilderQueryRoot<Country> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(Country.class, session);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<Country> root = criteriaBuilderQueryRoot.getRoot();

        return hibernateFacade.getAll(criteriaBuilderQueryRoot, builder.asc(root.get("name")), session);
    }
    
    public Country getCountry(String name, Session session) {
        return hibernateFacade.load(Country.class, "name", name, session);
    }
    
    public Country addCountry(Country country, Session session) {
        hibernateFacade.add(country, session);
        return getCountry(country.getName(), session);
    }

}