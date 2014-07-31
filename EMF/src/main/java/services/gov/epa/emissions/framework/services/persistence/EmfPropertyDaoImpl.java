package gov.epa.emissions.framework.services.persistence;

import gov.epa.emissions.framework.services.basic.EmfProperty;

import org.springframework.stereotype.Repository;

@Repository("emfPropertyDao")
public class EmfPropertyDaoImpl extends GenericHibernateDao<EmfProperty, Integer> implements EmfPropertyDao {

    public EmfPropertyDaoImpl() {
		super(EmfProperty.class);
	}

	public EmfProperty getProperty(String name) {
		return get(name);
//		return (EmfProperty) currentSession().createCriteria(EmfProperty.class).add(Restrictions.eq("name", name)).uniqueResult();
    }

}
