package gov.epa.emissions.framework.services.cost.controlmeasure.io;

import gov.epa.emissions.commons.data.SourceGroup;
import gov.epa.emissions.framework.services.persistence.HibernateFacade;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

public class SourceGroups {

    private HibernateSessionFactory sessionFactory;

    private HibernateFacade facade;

    private List sourceGroupList;

    public SourceGroups(HibernateSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
        this.facade = new HibernateFacade();
        sourceGroupList = sourceGroups(sessionFactory);
    }

    private List sourceGroups(HibernateSessionFactory sessionFactory) {
        Session session = sessionFactory.getSession();
        try {
            return facade.getAll(SourceGroup.class, Order.asc("name"), session);
        } finally {
            session.close();
        }
    }

    public SourceGroup getSourceGroup(String name) throws CMImporterException {
        SourceGroup sourceGroup = new SourceGroup();
        sourceGroup.setName(name);
        int index = sourceGroupList.indexOf(sourceGroup);
        if (index != -1) {
            return (SourceGroup) sourceGroupList.get(index);
        }

        sourceGroup = saveAndLoad(sourceGroup);
        sourceGroupList.add(sourceGroup);
        return sourceGroup;
    }

    private SourceGroup saveAndLoad(SourceGroup sourceGroup) throws CMImporterException {
        try {
            save(sourceGroup);
            return load(sourceGroup.getName());
        } catch (RuntimeException e) {
            throw new CMImporterException("Could not add a source group - " + sourceGroup.getName());
        }
    }

    private void save(SourceGroup sourceGroup) {
        Session session = sessionFactory.getSession();
        try {
            facade.add(sourceGroup, session);
        } finally {
            session.close();
        }
    }

    private SourceGroup load(String name) {
        Session session = sessionFactory.getSession();
        try {
            return (SourceGroup) facade.load(SourceGroup.class, Restrictions.eq("name", name), session);

        } finally {
            session.close();
        }
    }

}
