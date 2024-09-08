package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.commons.data.Reference;
import gov.epa.emissions.framework.services.persistence.HibernateFacade;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.Session;

public class ReferencesDAO {

    private HibernateFacade hibernateFacade;

    private Session session;

    public ReferencesDAO() {
        hibernateFacade = new HibernateFacade();
    }

    private void addObject(Object obj, Session session) {
        hibernateFacade.add(obj, session);
    }

    public void addReference(Reference reference, Session session) {
        addObject(reference, session);
    }

    public List<Reference> getReferences(Session session) {
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Reference> criteriaQuery = builder.createQuery(Reference.class);
        Root<Reference> root = criteriaQuery.from(Reference.class);

        criteriaQuery.select(root);
        criteriaQuery.orderBy(builder.asc(root.get("description")));

        return session.createQuery(criteriaQuery).getResultList();
    }

    public List<Reference> getReferences(Session session, String textContains) {
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Reference> criteriaQuery = builder.createQuery(Reference.class);
        Root<Reference> root = criteriaQuery.from(Reference.class);

        criteriaQuery.select(root);
        criteriaQuery.where(builder.like(builder.lower(root.get("description")), textContains.toLowerCase()));
        criteriaQuery.orderBy(builder.asc(root.get("description")));

        return session.createQuery(criteriaQuery).getResultList();
    }

    public boolean canUpdate(Reference reference, Session session) {
        if (!exists(reference.getId(), session)) {
            return false;
        }

        Reference current = current(reference.getId(), session);
        // The current object is saved in the session. Hibernate cannot persist our
        // object with the same id.
        session.clear();
        if (current.getDescription().equals(reference.getDescription()))
            return true;

        return !descriptionUsed(reference.getDescription(), session);
    }

    public int getReferenceCount(Session session) {
        List<?> num = session.createQuery("SELECT COUNT(ref.id) from Reference as ref").list();
        return Integer.parseInt(num.get(0).toString());
    }

    public int getReferenceCount(Session session, String text) {
        List<?> num = session.createQuery(
                "SELECT COUNT(ref.id) from Reference as ref where lower(ref.description) like '%%"
                        + text.toLowerCase().trim() + "%%'").list();
        return Integer.parseInt(num.get(0).toString());
    }

    public boolean descriptionUsed(String description, Session session) {
        return hibernateFacade.isUsed("description", description, Reference.class, session);
    }

    private Reference current(int id, Session session) {
        return hibernateFacade.current(id, Reference.class, session);
    }

    private boolean exists(int id, Session session) {
        return hibernateFacade.exists(id, Reference.class, session);
    }

    public Session getSession() {

        if (this.session == null) {
            this.session = HibernateSessionFactory.get().getSession();
        }

        return this.session;
    }
}
