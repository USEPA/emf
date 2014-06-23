package gov.epa.emissions.framework.services.cost.controlmeasure.io;

import gov.epa.emissions.framework.services.cost.ControlMeasureClass;
import gov.epa.emissions.framework.services.persistence.HibernateFacade;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.criterion.Order;
//import org.hibernate.criterion.Restrictions;

public class ControlMeasureClasses {

    private HibernateSessionFactory sessionFactory;

    private HibernateFacade facade;

    private List classList;

    public ControlMeasureClasses(HibernateSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
        this.facade = new HibernateFacade();
        classList = controlMeasureClasses();
    }

    private List controlMeasureClasses() {
        Session session = sessionFactory.getSession();
        try {
            return facade.getAll(ControlMeasureClass.class, Order.asc("name"), session);
        } finally {
            session.close();
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
        Session session = sessionFactory.getSession();
        try {
            facade.add(controlMeasureClass, session);
        } finally {
            session.close();
        }
    }

    private ControlMeasureClass load(String name) {
        Session session = sessionFactory.getSession();
        try {
            return (ControlMeasureClass) facade.load(ControlMeasureClass.class, Restrictions.eq("name", name), session);
        } finally {
            session.close();
        }
    }
 */
}
