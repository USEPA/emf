package gov.epa.emissions.framework.services.data;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Root;

import org.hibernate.Session;

import gov.epa.emissions.commons.data.Pollutant;
import gov.epa.emissions.framework.services.persistence.HibernateFacade;
import gov.epa.emissions.framework.services.persistence.HibernateFacade.CriteriaBuilderQueryRoot;

public class PollutantsDAO {

    private HibernateFacade hibernateFacade;

    public PollutantsDAO() {
        hibernateFacade = new HibernateFacade();
    }

    public List<Pollutant> getAll(Session session) {
        CriteriaBuilderQueryRoot<Pollutant> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(Pollutant.class, session);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<Pollutant> root = criteriaBuilderQueryRoot.getRoot();

        return hibernateFacade.getAll(criteriaBuilderQueryRoot, builder.asc(root.get("name")), session);
    }

    public boolean canUpdate(Pollutant pollutant, Session session) {
        if (!exists(pollutant.getId(), session)) {
            return false;
        }

        Pollutant current = current(pollutant.getId(), session);
        // The current object is saved in the session. Hibernate cannot persist our
        // object with the same id.
        session.clear();
        if (current.getName().equals(pollutant.getName()))
            return true;

        return !nameUsed(pollutant.getName(), session);
    }

    private boolean nameUsed(String name, Session session) {
        return hibernateFacade.nameUsed(name, Pollutant.class, session);
    }

    private Pollutant current(int id, Session session) {
        return hibernateFacade.current(id, Pollutant.class, session);
    }

    private boolean exists(int id, Session session) {
        return hibernateFacade.exists(id, Pollutant.class, session);
    }

}
