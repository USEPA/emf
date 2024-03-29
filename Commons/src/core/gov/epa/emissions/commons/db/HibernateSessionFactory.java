package gov.epa.emissions.commons.db;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.model.naming.ImplicitNamingStrategyJpaCompliantImpl;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;

public class HibernateSessionFactory {

    private static Log log = LogFactory.getLog(HibernateSessionFactory.class);

    private SessionFactory sessionFactory = null;

    private static HibernateSessionFactory instance;

    private HibernateSessionFactory() {
        try {
            StandardServiceRegistry standardRegistry = new StandardServiceRegistryBuilder()
                    .applySetting( AvailableSettings.TRANSACTION_COORDINATOR_STRATEGY, "jta" )
                    .configure( "hibernate.cfg.xml" )
                    .build();

            Metadata metadata = new MetadataSources( standardRegistry )
//                    .addAnnotatedClass( MyEntity.class )
//                    .addAnnotatedClassName( "org.hibernate.example.Customer" )
//                    .addResource( "org/hibernate/example/Order.hbm.xml" )
//                    .addResource( "org/hibernate/example/Product.orm.xml" )
                .getMetadataBuilder()
                .applyImplicitNamingStrategy( ImplicitNamingStrategyJpaCompliantImpl.INSTANCE )
                .build();

            sessionFactory = metadata.getSessionFactoryBuilder()
//                    .applyBeanManager( getBeanManager() )
                .build();
//            Configuration configure = new Configuration().configure("hibernate.cfg.xml");
//            sessionFactory = configure.buildSessionFactory();
        } catch (Throwable ex) {
            log.error("Initial SessionFactory creation failed.", ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public HibernateSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
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

}
