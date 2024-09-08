package gov.epa.emissions.framework.services.cost.controlmeasure.io;

import gov.epa.emissions.commons.data.SourceGroup;
import gov.epa.emissions.framework.services.persistence.HibernateFacade;
import gov.epa.emissions.framework.services.persistence.HibernateFacade.CriteriaBuilderQueryRoot;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Root;

import org.hibernate.Session;

public class SourceGroups {

    private HibernateSessionFactory sessionFactory;

    private HibernateFacade facade;

    private List sourceGroupList;

    public SourceGroups(HibernateSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
        this.facade = new HibernateFacade();
        sourceGroupList = sourceGroups(sessionFactory);
    }

    private List<SourceGroup> sourceGroups(HibernateSessionFactory sessionFactory) {
        Session session = sessionFactory.getSession();
        try {
            CriteriaBuilderQueryRoot<SourceGroup> criteriaBuilderQueryRoot = facade.getCriteriaBuilderQueryRoot(SourceGroup.class, session);
            CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
            Root<SourceGroup> root = criteriaBuilderQueryRoot.getRoot();

            return facade.getAll(criteriaBuilderQueryRoot, builder.asc(root.get("name")), session);
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
            return facade.load(SourceGroup.class, "name", name, session);

        } finally {
            session.close();
        }
    }

}
