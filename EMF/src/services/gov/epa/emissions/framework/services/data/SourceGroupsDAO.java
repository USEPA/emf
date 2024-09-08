package gov.epa.emissions.framework.services.data;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Root;

import org.hibernate.Session;

import gov.epa.emissions.commons.data.SourceGroup;
import gov.epa.emissions.framework.services.persistence.HibernateFacade;
import gov.epa.emissions.framework.services.persistence.HibernateFacade.CriteriaBuilderQueryRoot;

public class SourceGroupsDAO {

    private HibernateFacade hibernateFacade;

    public SourceGroupsDAO() {
        hibernateFacade = new HibernateFacade();
    }

    public List<SourceGroup> getAll(Session session) {
        CriteriaBuilderQueryRoot<SourceGroup> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(SourceGroup.class, session);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<SourceGroup> root = criteriaBuilderQueryRoot.getRoot();

        return hibernateFacade.getAll(criteriaBuilderQueryRoot, builder.asc(root.get("name")), session);
    }

    public boolean canUpdate(SourceGroup sourcegrp, Session session) {
        if (!exists(sourcegrp.getId(), session)) {
            return false;
        }

        SourceGroup current = current(sourcegrp.getId(), session);
        // The current object is saved in the session. Hibernate cannot persist our
        // object with the same id.
        session.clear();
        if (current.getName().equals(sourcegrp.getName()))
            return true;

        return !nameUsed(sourcegrp.getName(), session);
    }

    private boolean nameUsed(String name, Session session) {
        return hibernateFacade.nameUsed(name, SourceGroup.class, session);
    }

    private SourceGroup current(int id, Session session) {
        return hibernateFacade.current(id, SourceGroup.class, session);
    }

    private boolean exists(int id, Session session) {
        return hibernateFacade.exists(id, SourceGroup.class, session);
    }

}
