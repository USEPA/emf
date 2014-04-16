package gov.epa.emissions.framework.services.basic;

import gov.epa.emissions.framework.services.data.DataCommonsDAO;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.StatelessSession;
import org.hibernate.Transaction;

public class StatusDAO {

    private HibernateSessionFactory sessionFactory;

    private DataCommonsDAO dao;

    public StatusDAO() {
        this(HibernateSessionFactory.get());
    }

    public StatusDAO(HibernateSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
        dao = new DataCommonsDAO();
    }

//    public void setSessionFactory(SessionFactory sessionFactory) {
//        this.sessionFactory2 = sessionFactory;
//    }
    
    public void add(Status status) {
        StatelessSession session = sessionFactory.getStatelessSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.insert(status);
            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        } finally {
            session.close();
        }
    }

    public void add2(Status status) {
        Session session = sessionFactory.getSession();
        try {
            dao.add(status, session);
            session.flush();
            session.clear();
        } finally {
            session.close();
        }
    }

}
