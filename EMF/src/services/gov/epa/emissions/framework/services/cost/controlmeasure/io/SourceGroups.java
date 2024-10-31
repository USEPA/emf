package gov.epa.emissions.framework.services.cost.controlmeasure.io;

import gov.epa.emissions.commons.data.SourceGroup;
import gov.epa.emissions.framework.services.persistence.HibernateFacade;
import gov.epa.emissions.framework.services.persistence.HibernateFacade.CriteriaBuilderQueryRoot;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Root;

public class SourceGroups {

    private EntityManagerFactory entityManagerFactory;

    private HibernateFacade facade;

    private List<SourceGroup> sourceGroupList;

    public SourceGroups(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
        this.facade = new HibernateFacade();
        sourceGroupList = sourceGroups(entityManagerFactory);
    }

    private List<SourceGroup> sourceGroups(EntityManagerFactory entityManagerFactory) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            CriteriaBuilderQueryRoot<SourceGroup> criteriaBuilderQueryRoot = facade.getCriteriaBuilderQueryRoot(SourceGroup.class, entityManager);
            CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
            Root<SourceGroup> root = criteriaBuilderQueryRoot.getRoot();

            return facade.getAll(criteriaBuilderQueryRoot, builder.asc(root.get("name")), entityManager);
        } finally {
            entityManager.close();
        }
    }

    public SourceGroup getSourceGroup(String name) throws CMImporterException {
        SourceGroup sourceGroup = new SourceGroup();
        sourceGroup.setName(name);
        int index = sourceGroupList.indexOf(sourceGroup);
        if (index != -1) {
            return sourceGroupList.get(index);
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
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            facade.add(sourceGroup, entityManager);
        } finally {
            entityManager.close();
        }
    }

    private SourceGroup load(String name) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            return facade.load(SourceGroup.class, "name", name, entityManager);

        } finally {
            entityManager.close();
        }
    }

}
