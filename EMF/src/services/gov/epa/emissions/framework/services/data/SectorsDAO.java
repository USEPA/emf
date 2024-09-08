package gov.epa.emissions.framework.services.data;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Root;

import org.hibernate.Session;

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

    public List getAll(Session session) {
        CriteriaBuilderQueryRoot<Sector> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(Sector.class, session);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<Sector> root = criteriaBuilderQueryRoot.getRoot();

        return hibernateFacade.getAll(criteriaBuilderQueryRoot, builder.asc(root.get("name")), session);
    }
    
    public Sector getSector(String name, Session session) {
        return hibernateFacade.load(Sector.class, "name", name, session);
    }
    
    public void addSector(Sector sector, Session session) {
        hibernateFacade.add(sector, session);
    }

    public Sector obtainLocked(User user, Sector sector, Session session) {
        return (Sector) lockingScheme.getLocked(user, current(sector, session), session);
    }

    public Sector update(Sector sector, Session session) throws EmfException {
        return (Sector) lockingScheme.releaseLockOnUpdate(sector, current(sector, session), session);
    }

    public Sector releaseLocked(User user, Sector locked, Session session) {
        return (Sector) lockingScheme.releaseLock(user, current(locked, session), session);
    }

    private Sector current(Sector sector, Session session) {
        return current(sector.getId(), session);
    }

    /*
     * True if sector exists in database
     * 
     * 1. Should Exist 2. Your id matches existing Id 3. Your name should not match another object's name
     * 
     */
    public boolean canUpdate(Sector sector, Session session) {
        if (!exists(sector.getId(), session)) {
            return false;
        }

        Sector current = current(sector.getId(), session);
        // The current object is saved in the session. Hibernate cannot persist our
        // object with the same id.
        session.clear();
        if (current.getName().equals(sector.getName()))
            return true;

        return !nameUsed(sector.getName(), session);
    }

    private boolean nameUsed(String name, Session session) {
        return hibernateFacade.nameUsed(name, Sector.class, session);
    }

    private Sector current(int id, Session session) {
        return hibernateFacade.current(id, Sector.class, session);
    }

    private boolean exists(int id, Session session) {
        return hibernateFacade.exists(id, Sector.class, session);
    }

}
