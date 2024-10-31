package gov.epa.emissions.framework.services.data;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Root;

import javax.persistence.EntityManager;

import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.persistence.HibernateFacade;
import gov.epa.emissions.framework.services.persistence.HibernateFacade.CriteriaBuilderQueryRoot;
import gov.epa.emissions.framework.services.persistence.LockingScheme;

public class SectorsDAO {

    private LockingScheme lockingScheme;

    private HibernateFacade hibernateFacade;

    public SectorsDAO() {
        lockingScheme = new LockingScheme();
        hibernateFacade = new HibernateFacade();
    }

    public List getAll(EntityManager entityManager) {
        CriteriaBuilderQueryRoot<Sector> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(Sector.class, entityManager);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<Sector> root = criteriaBuilderQueryRoot.getRoot();

        return hibernateFacade.getAll(criteriaBuilderQueryRoot, builder.asc(root.get("name")), entityManager);
    }
    
    public Sector getSector(String name, EntityManager entityManager) {
        return hibernateFacade.load(Sector.class, "name", name, entityManager);
    }
    
    public void addSector(Sector sector, EntityManager entityManager) {
        hibernateFacade.add(sector, entityManager);
    }

    public Sector obtainLocked(User user, Sector sector, EntityManager entityManager) {
        return (Sector) lockingScheme.getLocked(user, current(sector, entityManager), entityManager);
    }

    public Sector update(Sector sector, EntityManager entityManager) throws EmfException {
        return (Sector) lockingScheme.releaseLockOnUpdate(sector, current(sector, entityManager), entityManager);
    }

    public Sector releaseLocked(User user, Sector locked, EntityManager entityManager) {
        return (Sector) lockingScheme.releaseLock(user, current(locked, entityManager), entityManager);
    }

    private Sector current(Sector sector, EntityManager entityManager) {
        return current(sector.getId(), entityManager);
    }

    /*
     * True if sector exists in database
     * 
     * 1. Should Exist 2. Your id matches existing Id 3. Your name should not match another object's name
     * 
     */
    public boolean canUpdate(Sector sector, EntityManager entityManager) {
        if (!exists(sector.getId(), entityManager)) {
            return false;
        }

        Sector current = current(sector.getId(), entityManager);
        // The current object is saved in the entityManager. Hibernate cannot persist our
        // object with the same id.
        entityManager.clear();
        if (current.getName().equals(sector.getName()))
            return true;

        return !nameUsed(sector.getName(), entityManager);
    }

    private boolean nameUsed(String name, EntityManager entityManager) {
        return hibernateFacade.nameUsed(name, Sector.class, entityManager);
    }

    private Sector current(int id, EntityManager entityManager) {
        return hibernateFacade.current(id, Sector.class, entityManager);
    }

    private boolean exists(int id, EntityManager entityManager) {
        return hibernateFacade.exists(id, Sector.class, entityManager);
    }

}
