package gov.epa.emissions.framework.services.persistence;

import javax.persistence.EntityManager;

import gov.epa.emissions.framework.services.basic.EmfProperty;

public class EmfPropertiesDAO {

    private HibernateFacade hibernateFacade;

    public EmfPropertiesDAO() {
        hibernateFacade = new HibernateFacade();
    }

    public EmfProperty getProperty(String name, EntityManager entityManager) {
        return hibernateFacade.load(EmfProperty.class, "name", name, entityManager);
    }
}