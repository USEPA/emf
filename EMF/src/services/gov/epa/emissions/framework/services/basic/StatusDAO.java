package gov.epa.emissions.framework.services.basic;

import gov.epa.emissions.framework.services.data.DataCommonsDAO;
import gov.epa.emissions.framework.services.persistence.JpaEntityManagerFactory;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

public class StatusDAO {

    private EntityManagerFactory entityManagerFactory;

    private DataCommonsDAO dao;

    public StatusDAO() {
        this(JpaEntityManagerFactory.get());
    }

    public StatusDAO(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
        dao = new DataCommonsDAO();
    }

    public void add(Status status) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            dao.add(status, entityManager);
//            entityManager.flush();
            entityManager.clear();
        } finally {
            entityManager.close();
        }
    }

}
