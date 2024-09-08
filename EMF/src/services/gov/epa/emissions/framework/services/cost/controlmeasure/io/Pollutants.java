package gov.epa.emissions.framework.services.cost.controlmeasure.io;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Root;

import org.hibernate.Session;

import gov.epa.emissions.commons.data.Pollutant;
import gov.epa.emissions.framework.services.persistence.HibernateFacade;
import gov.epa.emissions.framework.services.persistence.HibernateFacade.CriteriaBuilderQueryRoot;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

public class Pollutants {

    private HibernateSessionFactory sessionFactory;

    private HibernateFacade facade;

    private List pollutantList;

    public Pollutants(HibernateSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
        this.facade = new HibernateFacade();
        pollutantList = pollutants(sessionFactory);
    }

    private List<Pollutant> pollutants(HibernateSessionFactory sessionFactory) {
        Session session = sessionFactory.getSession();
        try {
            CriteriaBuilderQueryRoot<Pollutant> criteriaBuilderQueryRoot = facade.getCriteriaBuilderQueryRoot(Pollutant.class, session);
            CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
            Root<Pollutant> root = criteriaBuilderQueryRoot.getRoot();

            return facade.getAll(criteriaBuilderQueryRoot, builder.asc(root.get("name")), session);
        } finally {
            session.close();
        }
    }

    public Pollutant getPollutant(String name) throws CMImporterException {
        Pollutant pollutant;
        // having different versions of pollutant in database with different cases causes problems
        pollutant = new Pollutant(name.toUpperCase());
        int index = pollutantList.indexOf(pollutant);
        if (index != -1) {
            return (Pollutant) pollutantList.get(index);
        }

        pollutant = saveAndLoad(pollutant);
        pollutantList.add(pollutant);
        return pollutant;
    }

    private Pollutant saveAndLoad(Pollutant pollutant) throws CMImporterException {
        try {
            save(pollutant);
            return load(pollutant.getName());
        } catch (RuntimeException e) {
            throw new CMImporterException("Could not add a pollutant - " + pollutant.getName());
        }
    }

    private void save(Pollutant pollutant) {
        Session session = sessionFactory.getSession();
        try {
            facade.add(pollutant, session);
        } finally {
            session.close();
        }
    }

    private Pollutant load(String name) {
        Session session = sessionFactory.getSession();
        try {
            return facade.load(Pollutant.class, "name", name, session);
        } finally {
            session.close();
        }
    }

}
