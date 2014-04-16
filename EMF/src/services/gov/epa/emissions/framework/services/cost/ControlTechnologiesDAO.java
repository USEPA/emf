package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.framework.services.cost.data.ControlTechnology;
import gov.epa.emissions.framework.services.persistence.HibernateFacade;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.criterion.Order;

public class ControlTechnologiesDAO {

    private HibernateFacade hibernateFacade;

    public ControlTechnologiesDAO() {
        hibernateFacade = new HibernateFacade();
    }

    private void addObject(Object obj, Session session) {
        hibernateFacade.add(obj, session);
    }

    public void addControlTechnology(ControlTechnology technology, Session session) {
        addObject(technology, session);
    }

    public List getAll(Session session) {
        return session.createCriteria(ControlTechnology.class).addOrder(Order.asc("name")).list();
    }

    public boolean canUpdate(ControlTechnology technology, Session session) {
        if (!exists(technology.getId(), ControlTechnology.class, session)) {
            return false;
        }

        ControlTechnology current = current(technology.getId(), ControlTechnology.class, session);
        // The current object is saved in the session. Hibernate cannot persist our
        // object with the same id.
        session.clear();
        if (current.getName().equals(technology.getName()))
            return true;

        return !nameUsed(technology.getName(), ControlTechnology.class, session);
    }

    private boolean nameUsed(String name, Class clazz, Session session) {
        return hibernateFacade.nameUsed(name, clazz, session);
    }

    private ControlTechnology current(int id, Class clazz, Session session) {
        return (ControlTechnology) hibernateFacade.current(id, clazz, session);
    }

    private boolean exists(int id, Class clazz, Session session) {
        return hibernateFacade.exists(id, clazz, session);
    }

}
