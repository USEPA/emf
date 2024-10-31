package gov.epa.emissions.framework.services.cost.controlmeasure.io;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Root;

import javax.persistence.EntityManager;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasurePropertyCategory;
import gov.epa.emissions.framework.services.persistence.HibernateFacade;
import gov.epa.emissions.framework.services.persistence.HibernateFacade.CriteriaBuilderQueryRoot;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

public class ControlMeasurePropertyCategories {

    private EntityManagerFactory entityManagerFactory;

    private HibernateFacade facade;

    private List<ControlMeasurePropertyCategory> categoryList;

    public ControlMeasurePropertyCategories(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
        this.facade = new HibernateFacade();
        categoryList = categories();
    }

    private List<ControlMeasurePropertyCategory> categories() {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            CriteriaBuilderQueryRoot<ControlMeasurePropertyCategory> criteriaBuilderQueryRoot = facade.getCriteriaBuilderQueryRoot(ControlMeasurePropertyCategory.class, entityManager);
            CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
            Root<ControlMeasurePropertyCategory> root = criteriaBuilderQueryRoot.getRoot();

            return facade.getAll(criteriaBuilderQueryRoot, builder.asc(root.get("name")), entityManager);
        } finally {
            entityManager.close();
        }
    }

    public ControlMeasurePropertyCategory getCategory(String name) throws EmfException {
        ControlMeasurePropertyCategory category;
        // having different versions of pollutant in database with different cases causes problems
        category = new ControlMeasurePropertyCategory(name);
        int index = categoryList.indexOf(category);
        if (index != -1) {
            return categoryList.get(index);
        }

        category = saveAndLoad(category);
        categoryList.add(category);
        return category;
    }

    private ControlMeasurePropertyCategory saveAndLoad(ControlMeasurePropertyCategory category) throws EmfException {
        try {
            save(category);
            return load(category.getName());
        } catch (RuntimeException e) {
            throw new EmfException("Could not add a control measure property category - " + category.getName());
        }
    }

    private void save(ControlMeasurePropertyCategory category) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            facade.add(category, entityManager);
        } finally {
            entityManager.close();
        }
    }

    private ControlMeasurePropertyCategory load(String name) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            return facade.load(ControlMeasurePropertyCategory.class, "name", name, entityManager);
        } finally {
            entityManager.close();
        }
    }
}