package gov.epa.emissions.framework.services.data;

import gov.epa.emissions.commons.data.Region;
import gov.epa.emissions.framework.services.persistence.HibernateFacade;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

public class RegionsDAO {

    private HibernateFacade hibernateFacade;

    public RegionsDAO() {
        hibernateFacade = new HibernateFacade();
    }

    public List getAll(Session session) {
        return session.createCriteria(Region.class).addOrder(Order.asc("name")).list();
    }
    
    public Region getRegion(String name, Session session) {
        Criterion criterion = Restrictions.eq("name", name);
        return (Region)hibernateFacade.load(Region.class, criterion, session);
    }
    
    public Region addRegion(Region region, Session session) {
        hibernateFacade.add(region, session);
        return getRegion(region.getName(), session);
    }

}