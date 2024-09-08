package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.framework.services.cost.data.ControlTechnology;
import gov.epa.emissions.framework.services.persistence.HibernateFacade;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.Session;

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

    public List<ControlTechnology> getAll(Session session) {
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<ControlTechnology> criteriaQuery = builder.createQuery(ControlTechnology.class);
        Root<ControlTechnology> root = criteriaQuery.from(ControlTechnology.class);

        criteriaQuery.select(root);
        criteriaQuery.orderBy(builder.asc(root.get("name")));

        return session.createQuery(criteriaQuery).getResultList();
    }

    public boolean canUpdate(ControlTechnology technology, Session session) {
        if (!exists(technology.getId(), session)) {
            return false;
        }

        ControlTechnology current = current(technology.getId(), session);
        // The current object is saved in the session. Hibernate cannot persist our
        // object with the same id.
        session.clear();
        if (current.getName().equals(technology.getName()))
            return true;

        return !nameUsed(technology.getName(), session);
    }

    private boolean nameUsed(String name, Session session) {
        return hibernateFacade.nameUsed(name, ControlTechnology.class, session);
    }

    private ControlTechnology current(int id, Session session) {
        return hibernateFacade.current(id, ControlTechnology.class, session);
    }

    private boolean exists(int id, Session session) {
        return hibernateFacade.exists(id, ControlTechnology.class, session);
    }

}
