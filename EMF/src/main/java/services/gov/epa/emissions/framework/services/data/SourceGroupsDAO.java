package gov.epa.emissions.framework.services.data;

import gov.epa.emissions.commons.data.SourceGroup;
import gov.epa.emissions.framework.services.persistence.HibernateFacade;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.criterion.Order;

public class SourceGroupsDAO {

    private HibernateFacade hibernateFacade;

    public SourceGroupsDAO() {
        hibernateFacade = new HibernateFacade();
    }

    public List getAll(Session session) {
        return session.createCriteria(SourceGroup.class).addOrder(Order.asc("name")).list();
    }

    public boolean canUpdate(SourceGroup sourcegrp, Session session) {
        if (!exists(sourcegrp.getId(), SourceGroup.class, session)) {
            return false;
        }

        SourceGroup current = current(sourcegrp.getId(), SourceGroup.class, session);
        // The current object is saved in the session. Hibernate cannot persist our
        // object with the same id.
        session.clear();
        if (current.getName().equals(sourcegrp.getName()))
            return true;

        return !nameUsed(sourcegrp.getName(), SourceGroup.class, session);
    }

    private boolean nameUsed(String name, Class clazz, Session session) {
        return hibernateFacade.nameUsed(name, clazz, session);
    }

    private SourceGroup current(int id, Class clazz, Session session) {
        return (SourceGroup) hibernateFacade.current(id, clazz, session);
    }

    private boolean exists(int id, Class clazz, Session session) {
        return hibernateFacade.exists(id, clazz, session);
    }

}
