package gov.epa.emissions.commons.util;

import java.io.Serializable;

import gov.epa.emissions.commons.db.HibernateSessionFactory;
import gov.epa.emissions.commons.db.LocalHibernateConfiguration;
import gov.epa.emissions.commons.db.version.Version;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

public class utils {
    
    public static Serializable add(Object obj, Session session) {
        Transaction tx = null;
        Serializable id;
        try {
            tx = session.beginTransaction();
            id = session.save(obj);
            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();  
            throw e;
        }
        return id;
    }
    
    public static SessionFactory getHibernateSessionFactory() throws Exception {
        LocalHibernateConfiguration config = new LocalHibernateConfiguration();
        return config.factory();
    }
    
    public static Session getHibernateSession() throws Exception {
        HibernateSessionFactory sessionFactory = new HibernateSessionFactory(getHibernateSessionFactory());
        return sessionFactory.getSession();
    }
    
    public static void add(Version version, Session session) {
        add(version, session);
    }
}
