package gov.epa.emissions.framework.services.cost.controlmeasure.io;

import gov.epa.emissions.framework.services.cost.data.ControlTechnology;
import gov.epa.emissions.framework.services.persistence.HibernateFacade;
import gov.epa.emissions.framework.services.persistence.HibernateFacade.CriteriaBuilderQueryRoot;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Root;

public class ControlTechnologies {

    private EntityManagerFactory entityManagerFactory;

    private HibernateFacade facade;

    private List<ControlTechnology> controlTechnologiesList;

    public ControlTechnologies(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
        this.facade = new HibernateFacade();
        controlTechnologiesList = controlTechnologies(entityManagerFactory);
    }

    private List<ControlTechnology> controlTechnologies(EntityManagerFactory entityManagerFactory) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            CriteriaBuilderQueryRoot<ControlTechnology> criteriaBuilderQueryRoot = facade.getCriteriaBuilderQueryRoot(ControlTechnology.class, entityManager);
            CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
            Root<ControlTechnology> root = criteriaBuilderQueryRoot.getRoot();

            return facade.getAll(criteriaBuilderQueryRoot, builder.asc(root.get("name")), entityManager);
        } finally {
            entityManager.close();
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
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            facade.add(controlTechnology, entityManager);
        } finally {
            entityManager.close();
        }
    }

    private ControlTechnology load(String name) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            return (ControlTechnology) facade.load(ControlTechnology.class, "name", name, entityManager);
        } finally {
            entityManager.close();
        }
    }

}
