package gov.epa.emissions.framework.services.persistence;

import org.hibernate.Session;

import gov.epa.emissions.framework.services.basic.EmfProperty;

public class EmfPropertiesDAO {

    private HibernateFacade hibernateFacade;

    public EmfPropertiesDAO() {
        hibernateFacade = new HibernateFacade();
    }

    public EmfProperty getProperty(String name, Session session) {
        return hibernateFacade.load(EmfProperty.class, "name", name, session);
    }
}