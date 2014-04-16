package gov.epa.emissions.framework.services.cost.controlmeasure.io;

import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.framework.services.persistence.HibernateFacade;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

public class Sectors {

    private HibernateSessionFactory sessionFactory;

    private HibernateFacade facade;

    private List sectorsList;

    public Sectors(HibernateSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
        this.facade = new HibernateFacade();
        sectorsList = sectors(sessionFactory);
    }

    private List sectors(HibernateSessionFactory sessionFactory) {
        Session session = sessionFactory.getSession();
        try {
            return facade.getAll(Sector.class, Order.asc("name"), session);
        } finally {
            session.close();
        }
    }

    // FIXME: handle arrays
    public Sector getSector(String name) throws CMImporterException {
        Sector sector = new Sector();
        sector.setName(name);
        int index = sectorsList.indexOf(sector);
        if (index != -1) {
            return (Sector) sectorsList.get(index);
        }

        sector = saveAndLoad(sector);
        sectorsList.add(sector);
        return sector;
    }

    private Sector saveAndLoad(Sector sector) throws CMImporterException {
        try {
            save(sector);
            return load(sector.getName());
        } catch (RuntimeException e) {
            throw new CMImporterException("Could not add a sector - " + sector.getName());
        }
    }

    private void save(Sector sector) {
        Session session = sessionFactory.getSession();
        try {
            facade.add(sector, session);
        } finally {
            session.close();
        }
    }

    private Sector load(String name) {
        Session session = sessionFactory.getSession();
        try {
            return (Sector) facade.load(Sector.class, Restrictions.eq("name", name), session);
        } finally {
            session.close();
        }
    }

}
