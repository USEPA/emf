package gov.epa.emissions.framework.services.persistence;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import javax.persistence.EntityManager;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.hibernate.cfg.Configuration;

public class HibernateSessionFactory {

    private static Log log = LogFactory.getLog(HibernateSessionFactory.class);

    private SessionFactory entityManagerFactory = null;

    private static HibernateSessionFactory instance;

    private HibernateSessionFactory() {
        try {
            Configuration configure = new Configuration().configure("hibernate.cfg.xml");
            entityManagerFactory = configure.buildSessionFactory();
        } catch (Throwable ex) {
            log.error("Initial SessionFactory creation failed", ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public HibernateSessionFactory(SessionFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    public SessionFactory getSessionFactory() {
        return this.entityManagerFactory;
    }

    // TODO: stick a single instance in the Axis application-level cache. Only
    // one instance is needed for the entire application i.e. one per db
    public static HibernateSessionFactory get() {
        if (instance == null)
            instance = new HibernateSessionFactory();

        return instance;
    }

    public EntityManager getSession() throws HibernateException {
        return entityManagerFactory.openSession();
    }

    public StatelessSession getStatelessSession() throws HibernateException {
        return entityManagerFactory.openStatelessSession();
    }

}
