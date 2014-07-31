package gov.epa.emissions.framework.services.persistence;

import gov.epa.emissions.framework.services.basic.EmfProperty;
import gov.epa.emissions.framework.services.persistence.GenericDao;

/**
 * DAO of EmfProperty.
 */
public interface EmfPropertyDao extends GenericDao<EmfProperty, Integer> {

    /**
     * Get EmfProperty user by property name.
     * @param name name to lookup on
     * @return EmfProperty EmfProperty based on property name.
     */
    EmfProperty getProperty(String name);

}