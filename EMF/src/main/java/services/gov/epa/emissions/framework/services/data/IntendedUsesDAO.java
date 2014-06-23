package gov.epa.emissions.framework.services.data;

import gov.epa.emissions.commons.db.intendeduse.IntendedUse;
import gov.epa.emissions.framework.services.persistence.HibernateFacade;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

public class IntendedUsesDAO {

    private HibernateFacade hibernateFacade;

    public IntendedUsesDAO() {
        hibernateFacade = new HibernateFacade();
    }

    public List getAll(Session session) {
        return session.createCriteria(IntendedUse.class).addOrder(Order.asc("name")).list();
    }
    
    public IntendedUse getIntendedUse(String name, Session session) {
        Criterion criterion = Restrictions.eq("name", name);
        return (IntendedUse)hibernateFacade.load(IntendedUse.class, criterion, session);
    }
    
    public void add(IntendedUse intendedUse, Session session) {
        hibernateFacade.add(intendedUse, session);
    }
    
}