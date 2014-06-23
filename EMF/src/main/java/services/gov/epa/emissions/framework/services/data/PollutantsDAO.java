package gov.epa.emissions.framework.services.data;

import gov.epa.emissions.commons.data.Pollutant;
import gov.epa.emissions.framework.services.persistence.HibernateFacade;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.criterion.Order;

public class PollutantsDAO {

    private HibernateFacade hibernateFacade;

    public PollutantsDAO() {
        hibernateFacade = new HibernateFacade();
    }

    public List getAll(Session session) {
        return session.createCriteria(Pollutant.class).addOrder(Order.asc("name")).list();
    }

    public boolean canUpdate(Pollutant pollutant, Session session) {
        if (!exists(pollutant.getId(), Pollutant.class, session)) {
            return false;
        }

        Pollutant current = current(pollutant.getId(), Pollutant.class, session);
        // The current object is saved in the session. Hibernate cannot persist our
        // object with the same id.
        session.clear();
        if (current.getName().equals(pollutant.getName()))
            return true;

        return !nameUsed(pollutant.getName(), Pollutant.class, session);
    }

    private boolean nameUsed(String name, Class clazz, Session session) {
        return hibernateFacade.nameUsed(name, clazz, session);
    }

    private Pollutant current(int id, Class clazz, Session session) {
        return (Pollutant) hibernateFacade.current(id, clazz, session);
    }

    private boolean exists(int id, Class clazz, Session session) {
        return hibernateFacade.exists(id, clazz, session);
    }

}
