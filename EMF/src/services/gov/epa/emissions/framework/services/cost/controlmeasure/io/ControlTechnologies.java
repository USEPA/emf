package gov.epa.emissions.framework.services.cost.controlmeasure.io;

import gov.epa.emissions.framework.services.cost.data.ControlTechnology;
import gov.epa.emissions.framework.services.persistence.HibernateFacade;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

public class ControlTechnologies {

    private HibernateSessionFactory sessionFactory;

    private HibernateFacade facade;

    private List controlTechnologiesList;

    public ControlTechnologies(HibernateSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
        this.facade = new HibernateFacade();
        controlTechnologiesList = controlTechnologies(sessionFactory);
    }

    private List controlTechnologies(HibernateSessionFactory sessionFactory) {
        Session session = sessionFactory.getSession();
        try {
            return facade.getAll(ControlTechnology.class, Order.asc("name"), session);
        } finally {
            session.close();
        }
    }

    public ControlTechnology getControlTechnology(String name) throws CMImporterException {
        ControlTechnology controlTechnology = new ControlTechnology();
        controlTechnology.setName(name);
        int index = controlTechnologiesList.indexOf(controlTechnology);
        if (index != -1) {
            return (ControlTechnology) controlTechnologiesList.get(index);
        }
        
        controlTechnology=saveAndLoad(controlTechnology);
        controlTechnologiesList.add(controlTechnology);
        return controlTechnology;
    }

    private ControlTechnology saveAndLoad(ControlTechnology controlTechnology) throws CMImporterException {
        try {
            save(controlTechnology);
            return load(controlTechnology.getName());
        } catch (RuntimeException e) {
            throw new CMImporterException("Could not add a control technology - " + controlTechnology.getName());
        }
    }

    private void save(ControlTechnology controlTechnology) {
        Session session = sessionFactory.getSession();
        try {
            facade.add(controlTechnology, session);
        } finally {
            session.close();
        }
    }

    private ControlTechnology load(String name) {
        Session session = sessionFactory.getSession();
        try {
            return (ControlTechnology) facade.load(ControlTechnology.class, Restrictions.eq("name", name), session);
        } finally {
            session.close();
        }
    }

}
