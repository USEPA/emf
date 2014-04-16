package gov.epa.emissions.commons.db;

import gov.epa.emissions.commons.io.importer.PersistenceTestCase;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

public abstract class HibernateTestCase extends PersistenceTestCase {

    protected Session session;

    public HibernateTestCase(String name) {
        super(name);
    }

    public HibernateTestCase() {
    }

    protected void setUp() throws Exception {
        super.setUp();
        HibernateSessionFactory sessionFactory = new HibernateSessionFactory(sessionFactory());
        session = sessionFactory.getSession();
    }

    protected void doTearDown() throws Exception {
        session.close();
    }

    protected SessionFactory sessionFactory() throws Exception {
        LocalHibernateConfiguration config = new LocalHibernateConfiguration();
        return config.factory();
    }

    protected void remove(Object object) {
        Transaction tx = session.beginTransaction();
        session.delete(object);
        tx.commit();
    }

    protected void save(Object element) {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.save(element);
            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }
}
