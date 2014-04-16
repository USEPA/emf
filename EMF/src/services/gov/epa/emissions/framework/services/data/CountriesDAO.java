package gov.epa.emissions.framework.services.data;

import gov.epa.emissions.commons.data.Country;
import gov.epa.emissions.framework.services.persistence.HibernateFacade;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

public class CountriesDAO {

    private HibernateFacade hibernateFacade;

    public CountriesDAO() {
        hibernateFacade = new HibernateFacade();
    }

    public List getAll(Session session) {
        return session.createCriteria(Country.class).addOrder(Order.asc("name")).list();
    }
    
    public Country getCountry(String name, Session session) {
        Criterion criterion = Restrictions.eq("name", name);
        return (Country)hibernateFacade.load(Country.class, criterion, session);
    }
    
    public Country addCountry(Country country, Session session) {
        hibernateFacade.add(country, session);
        return getCountry(country.getName(), session);
    }

}