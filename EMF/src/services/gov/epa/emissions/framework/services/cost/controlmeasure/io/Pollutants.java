package gov.epa.emissions.framework.services.cost.controlmeasure.io;

import gov.epa.emissions.commons.data.Pollutant;
import gov.epa.emissions.framework.services.persistence.HibernateFacade;
import gov.epa.emissions.framework.services.persistence.HibernateFacade.CriteriaBuilderQueryRoot;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Root;

public class Pollutants {

    private EntityManagerFactory entityManagerFactory;

    private HibernateFacade facade;

    private List pollutantList;

    public Pollutants(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
        this.facade = new HibernateFacade();
        pollutantList = pollutants(entityManagerFactory);
    }

    private List<Pollutant> pollutants(EntityManagerFactory entityManagerFactory) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            CriteriaBuilderQueryRoot<Pollutant> criteriaBuilderQueryRoot = facade.getCriteriaBuilderQueryRoot(Pollutant.class, entityManager);
            CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
            Root<Pollutant> root = criteriaBuilderQueryRoot.getRoot();

            return facade.getAll(criteriaBuilderQueryRoot, builder.asc(root.get("name")), entityManager);
        } finally {
            entityManager.close();
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
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            facade.add(pollutant, entityManager);
        } finally {
            entityManager.close();
        }
    }

    private Pollutant load(String name) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            return facade.load(Pollutant.class, "name", name, entityManager);
        } finally {
            entityManager.close();
        }
    }

}
