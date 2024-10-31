package gov.epa.emissions.framework.services.cost.controlmeasure.io;

import gov.epa.emissions.framework.services.cost.ControlMeasureClass;
import gov.epa.emissions.framework.services.persistence.HibernateFacade;
import gov.epa.emissions.framework.services.persistence.HibernateFacade.CriteriaBuilderQueryRoot;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Root;

public class ControlMeasureClasses {

    private EntityManagerFactory entityManagerFactory;

    private HibernateFacade facade;

    private List classList;

    public ControlMeasureClasses(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
        this.facade = new HibernateFacade();
        classList = controlMeasureClasses();
    }

    private List<ControlMeasureClass> controlMeasureClasses() {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            CriteriaBuilderQueryRoot<ControlMeasureClass> criteriaBuilderQueryRoot = facade.getCriteriaBuilderQueryRoot(ControlMeasureClass.class, entityManager);
            CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
            Root<ControlMeasureClass> root = criteriaBuilderQueryRoot.getRoot();

            return facade.getAll(criteriaBuilderQueryRoot, builder.asc(root.get("name")), entityManager);
        } finally {
            entityManager.close();
        }
    }

    public ControlMeasureClass getControlMeasureClass(String name) throws CMImporterException {
        ControlMeasureClass controlMeasureClass;
        // having different versions of ControlMeasureClass in database with different cases causes problems
        controlMeasureClass = new ControlMeasureClass(name.toUpperCase());
        int index = classList.indexOf(controlMeasureClass);
        if (index != -1) {
            return (ControlMeasureClass) classList.get(index);
        }

        throw new CMImporterException("This is not a predefined control measure class - " + controlMeasureClass.getName());

//        controlMeasureClass = saveAndLoad(controlMeasureClass);
//        classList.add(controlMeasureClass);
//        return controlMeasureClass;
    }

/*

    private ControlMeasureClass saveAndLoad(ControlMeasureClass controlMeasureClass) throws CMImporterException {
        try {
            save(controlMeasureClass);
            return load(controlMeasureClass.getName());
        } catch (RuntimeException e) {
            throw new CMImporterException("Could not add a controlMeasureClass - " + controlMeasureClass.getName());
        }
    }

    private void save(ControlMeasureClass controlMeasureClass) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            facade.add(controlMeasureClass, entityManager);
        } finally {
            entityManager.close();
        }
    }

    private ControlMeasureClass load(String name) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            return (ControlMeasureClass) facade.load(ControlMeasureClass.class, Restrictions.eq("name", name), entityManager);
        } finally {
            entityManager.close();
        }
    }
 */
}
