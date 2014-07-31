package gov.epa.emissions.framework.services.persistence;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.hibernate.cfg.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

@Repository
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class HibernateSessionFactory {

    private static Log log = LogFactory.getLog(HibernateSessionFactory.class);

    private SessionFactory sessionFactory = null;

    private static HibernateSessionFactory instance;

    public HibernateSessionFactory() {
        try {
            Configuration configure = new Configuration().configure("hibernate.cfg.xml");
            sessionFactory = configure.buildSessionFactory();
        } catch (Throwable ex) {
            log.error("Initial SessionFactory creation failed", ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public HibernateSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Autowired
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public SessionFactory getSessionFactory() {
        return this.sessionFactory;
    }

    // TODO: stick a single instance in the Axis application-level cache. Only
    // one instance is needed for the entire application i.e. one per db
    public static HibernateSessionFactory get() {
        if (instance == null)
            instance = new HibernateSessionFactory();

        return instance;
    }

    public Session getSession() throws HibernateException {
        return sessionFactory.openSession();
    }

    public StatelessSession getStatelessSession() throws HibernateException {
        return sessionFactory.openStatelessSession();
    }

}
