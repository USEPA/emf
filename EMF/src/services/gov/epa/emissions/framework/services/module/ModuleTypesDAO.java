package gov.epa.emissions.framework.services.module;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.persistence.HibernateFacade;
import gov.epa.emissions.framework.services.persistence.LockingScheme;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

public class ModuleTypesDAO {

    private LockingScheme lockingScheme;

    private HibernateFacade hibernateFacade;

    public ModuleTypesDAO() {
        lockingScheme = new LockingScheme();
        hibernateFacade = new HibernateFacade();
    }

    //---------------------------------------------
    
    @SuppressWarnings("rawtypes")
    public List getModuleTypes(Session session) {
        return hibernateFacade.getAll(ModuleType.class, Order.asc("name").ignoreCase(), session);
    }

    public void removeModuleType(ModuleType moduleType, Session session) {
        hibernateFacade.remove(moduleType, session);
    }

    public ModuleType obtainLockedModuleType(User user, int moduleTypeId, Session session) {
        return (ModuleType) lockingScheme.getLocked(user, currentModuleType(moduleTypeId, session), session);
    }

    public ModuleType releaseLockedModuleType(User user, int moduleTypeId, Session session) {
        return (ModuleType) lockingScheme.releaseLock(user, currentModuleType(moduleTypeId, session), session);
    }

    public ModuleType updateModuleType(ModuleType moduleType, Session session) throws EmfException {
        return (ModuleType) lockingScheme.renewLockOnUpdate(moduleType, currentModuleType(moduleType.getId(), session), session);
    }

    public ModuleType getModuleType(String name, Session session) {
        Criterion criterion = Restrictions.eq("name", name);
        @SuppressWarnings("rawtypes")
        List list = hibernateFacade.get(ModuleType.class, criterion, session);
        return (list == null || list.size() == 0) ? null : (ModuleType) list.get(0);
    }

    public ModuleType getModuleType(int id, Session session) {
        Criterion criterion = Restrictions.eq("id", id);
        @SuppressWarnings("rawtypes")
        List list = hibernateFacade.get(ModuleType.class, criterion, session);
        return (list == null || list.size() == 0) ? null : (ModuleType) list.get(0);
    }

    public void addModuleType(ModuleType moduleType, Session session) {
        hibernateFacade.add(moduleType, session);
    }

    public boolean canUpdateModuleType(ModuleType moduleType, Session session) {
        if (!moduleTypeExists(moduleType.getId(), session)) {
            return false;
        }

        ModuleType current = currentModuleType(moduleType.getId(), session);
        // The current object is saved in the session. Hibernate cannot persist our
        // object with the same id.
        session.clear();
        if (current.getName().equals(moduleType.getName()))
            return true;

        return !moduleTypeNameUsed(moduleType.getName(), session);
    }

    private boolean moduleTypeExists(int moduleTypeId, Session session) {
        return hibernateFacade.exists(moduleTypeId, ModuleType.class, session);
    }

    public boolean moduleTypeNameUsed(String name, Session session) {
        return hibernateFacade.nameUsed(name, ModuleType.class, session);
    }

    public ModuleType currentModuleType(int moduleTypeId, Session session) {
        return (ModuleType) hibernateFacade.current(moduleTypeId, ModuleType.class, session);
    }

    //---------------------------------------------
    
    public ModuleTypeVersion getModuleTypeVersion(int id, Session session) {
        Criterion criterion = Restrictions.eq("id", id);
        @SuppressWarnings("rawtypes")
        List list = hibernateFacade.get(ModuleTypeVersion.class, criterion, session);
        return (list == null || list.size() == 0) ? null : (ModuleTypeVersion) list.get(0);
    }

    public ModuleTypeVersion currentModuleTypeVersion(int moduleTypeVersionId, Session session) {
        return (ModuleTypeVersion) hibernateFacade.current(moduleTypeVersionId, ModuleTypeVersion.class, session);
    }

    public void removeModuleTypeVersion(ModuleTypeVersion moduleTypeVersion, Session session) {
        hibernateFacade.remove(moduleTypeVersion, session);
    }

    public ModuleTypeVersion addModuleTypeVersion(ModuleTypeVersion moduleTypeVersion, Session session) {
        hibernateFacade.add(moduleTypeVersion, session);
        return currentModuleTypeVersion(moduleTypeVersion.getId(), session);
    }

    public ModuleTypeVersion updateModuleTypeVersion(ModuleTypeVersion moduleTypeVersion, Session session) {
        hibernateFacade.updateOnly(moduleTypeVersion, session);
        return currentModuleTypeVersion(moduleTypeVersion.getId(), session);
    }

    //---------------------------------------------
    
    @SuppressWarnings("rawtypes")
    public List getParameterTypes(Session session) {
        return hibernateFacade.getAll(ParameterType.class, Order.asc("sqlType").ignoreCase(), session);
    }

}
