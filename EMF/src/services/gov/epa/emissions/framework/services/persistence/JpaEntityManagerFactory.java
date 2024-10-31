package gov.epa.emissions.framework.services.persistence;

import javax.persistence.Persistence;

public class JpaEntityManagerFactory {

    private static javax.persistence.EntityManagerFactory instance;

    private JpaEntityManagerFactory() {
    }

    // TODO: stick a single instance in the Axis application-level cache. Only
    // one instance is needed for the entire application i.e. one per db
    public static javax.persistence.EntityManagerFactory get() {
        if (instance == null)
            instance = Persistence.createEntityManagerFactory("gov.epa.emissions");

        return instance;
    }
}
