package gov.epa.emissions.framework.services.cost.controlmeasure.io;

import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.framework.services.persistence.HibernateFacade;
import gov.epa.emissions.framework.services.persistence.HibernateFacade.CriteriaBuilderQueryRoot;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Root;

public class Sectors {

    private EntityManagerFactory entityManagerFactory;

    private HibernateFacade facade;

    private List<Sector> sectorsList;

    public Sectors(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
        this.facade = new HibernateFacade();
        sectorsList = sectors(entityManagerFactory);
    }

    private List<Sector> sectors(EntityManagerFactory entityManagerFactory) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            CriteriaBuilderQueryRoot<Sector> criteriaBuilderQueryRoot = facade.getCriteriaBuilderQueryRoot(Sector.class, entityManager);
            CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
            Root<Sector> root = criteriaBuilderQueryRoot.getRoot();

            return facade.getAll(criteriaBuilderQueryRoot, builder.asc(root.get("name")), entityManager);
        } finally {
            entityManager.close();
        }
    }

    // FIXME: handle arrays
    public Sector getSector(String name) throws CMImporterException {
        Sector sector = new Sector();
        sector.setName(name);
        int index = sectorsList.indexOf(sector);
        if (index != -1) {
            return sectorsList.get(index);
        }

        sector = saveAndLoad(sector);
        sectorsList.add(sector);
        return sector;
    }

    private Sector saveAndLoad(Sector sector) throws CMImporterException {
        try {
            save(sector);
            return load(sector.getName());
        } catch (RuntimeException e) {
            throw new CMImporterException("Could not add a sector - " + sector.getName());
        }
    }

    private void save(Sector sector) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            facade.add(sector, entityManager);
        } finally {
            entityManager.close();
        }
    }

    private Sector load(String name) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            return facade.load(Sector.class, "name", name, entityManager);
        } finally {
            entityManager.close();
        }
    }

}
