package gov.epa.emissions.framework.services.persistence;

import gov.epa.emissions.commons.db.postgres.PostgresDbConfig;

import java.io.File;
import java.util.Map;
import java.util.Properties;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class LocalHibernateConfiguration {

    private SessionFactory sessionFactory;

    public LocalHibernateConfiguration(File configFile) throws Exception {
        Configuration config = new Configuration().configure();
        Properties props = config.getProperties();
        props.remove("hibernate.connection.datasource");

        props.putAll(testsConfig(configFile.getAbsolutePath()));

        config = config.setProperties(props);
        sessionFactory = config.buildSessionFactory();
    }

    private Map testsConfig(String configFileName) throws Exception {
        return new PostgresDbConfig(configFileName).properties();
    }

    public SessionFactory factory() {
        return sessionFactory;
    }
}
