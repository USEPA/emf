package gov.epa.emissions.framework.services.cost.controlmeasure.io;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Root;

import org.hibernate.Session;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasurePropertyCategory;
import gov.epa.emissions.framework.services.persistence.HibernateFacade;
import gov.epa.emissions.framework.services.persistence.HibernateFacade.CriteriaBuilderQueryRoot;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

public class ControlMeasurePropertyCategories {

    private HibernateSessionFactory sessionFactory;

    private HibernateFacade facade;

    private List categoryList;

    public ControlMeasurePropertyCategories(HibernateSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
        this.facade = new HibernateFacade();
        categoryList = categories(sessionFactory);
    }

    private List<ControlMeasurePropertyCategory> categories(HibernateSessionFactory sessionFactory) {
        Session session = sessionFactory.getSession();
        try {
            CriteriaBuilderQueryRoot<ControlMeasurePropertyCategory> criteriaBuilderQueryRoot = facade.getCriteriaBuilderQueryRoot(ControlMeasurePropertyCategory.class, session);
            CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
            Root<ControlMeasurePropertyCategory> root = criteriaBuilderQueryRoot.getRoot();

            return facade.getAll(criteriaBuilderQueryRoot, builder.asc(root.get("name")), session);
        } finally {
            session.close();
        }
    }

    public ControlMeasurePropertyCategory getCategory(String name) throws EmfException {
        ControlMeasurePropertyCategory category;
        // having different versions of pollutant in database with different cases causes problems
        category = new ControlMeasurePropertyCategory(name);
        int index = categoryList.indexOf(category);
        if (index != -1) {
            return (ControlMeasurePropertyCategory) categoryList.get(index);
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
        Session session = sessionFactory.getSession();
        try {
            facade.add(category, session);
        } finally {
            session.close();
        }
    }

    private ControlMeasurePropertyCategory load(String name) {
        Session session = sessionFactory.getSession();
        try {
            return (ControlMeasurePropertyCategory) facade.load(ControlMeasurePropertyCategory.class, "name", name, session);
        } finally {
            session.close();
        }
    }
}