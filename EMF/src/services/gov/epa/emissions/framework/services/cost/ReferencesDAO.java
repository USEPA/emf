package gov.epa.emissions.framework.services.cost;

import gov.epa.emissions.commons.data.Reference;
import gov.epa.emissions.commons.db.HibernateSessionFactory;
import gov.epa.emissions.framework.services.persistence.HibernateFacade;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

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
        return session.createCriteria(Reference.class).addOrder(Order.asc("description")).list();
    }

    public List<Reference> getReferences(Session session, String textContains) {
        return session.createCriteria(Reference.class).add(
                Restrictions.ilike("description", textContains.toLowerCase().trim(), MatchMode.ANYWHERE)).addOrder(
                Order.asc("description")).list();
        // return session.createQuery(
        // "select new Reference(ref.id, ref.description) from Reference as ref where lower(ref.description) "
        // + "like '%%" + textContains.toLowerCase().trim() + "%%' order by ref.description").list();
    }

    public boolean canUpdate(Reference reference, Session session) {
        if (!exists(reference.getId(), Reference.class, session)) {
            return false;
        }

        Reference current = current(reference.getId(), Reference.class, session);
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

    private Reference current(int id, Class clazz, Session session) {
        return (Reference) hibernateFacade.current(id, clazz, session);
    }

    private boolean exists(int id, Class clazz, Session session) {
        return hibernateFacade.exists(id, clazz, session);
    }

    public Session getSession() {

        if (this.session == null) {
            this.session = HibernateSessionFactory.get().getSession();
        }

        return this.session;
    }
}
