package gov.epa.emissions.framework.services;


import org.hibernate.Session;

import gov.epa.emissions.framework.services.basic.EmfProperty;

public interface EmfProperties {

    EmfProperty getProperty(String name);

    EmfProperty getProperty(String name, Session session);

}