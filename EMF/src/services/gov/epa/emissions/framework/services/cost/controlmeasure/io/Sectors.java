package gov.epa.emissions.framework.services.cost.controlmeasure.io;

import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.framework.services.persistence.HibernateFacade;
import gov.epa.emissions.framework.services.persistence.HibernateFacade.CriteriaBuilderQueryRoot;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Root;

import org.hibernate.Session;

public class Sectors {

    private HibernateSessionFactory sessionFactory;

    private HibernateFacade facade;

    private List sectorsList;

    public Sectors(HibernateSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
        this.facade = new HibernateFacade();
        sectorsList = sectors(sessionFactory);
    }

    private List<Sector> sectors(HibernateSessionFactory sessionFactory) {
        Session session = sessionFactory.getSession();
        try {
            CriteriaBuilderQueryRoot<Sector> criteriaBuilderQueryRoot = facade.getCriteriaBuilderQueryRoot(Sector.class, session);
            CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
            Root<Sector> root = criteriaBuilderQueryRoot.getRoot();

            return facade.getAll(criteriaBuilderQueryRoot, builder.asc(root.get("name")), session);
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
            return facade.load(Sector.class, "name", name, session);
        } finally {
            session.close();
        }
    }

}
