package gov.epa.emissions.framework.services.module;

import gov.epa.emissions.commons.io.Column;
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
    
    public List getModuleTypes(Session session) {
        return getAll(session);
    }

    public void removeModuleType(ModuleType type, Session session) {
        hibernateFacade.remove(type, session);
    }

    public ModuleType obtainLockedModuleType(User user, ModuleType type, Session session) {
        return obtainLocked(user, type, session);
    }

    public ModuleType releaseLockedModuleType(User user, ModuleType type, Session session) {
        return releaseLocked(user, type, session);
    }

    //---------------------------------------------
    
    public List getAll(Session session) {
        return hibernateFacade.getAll(ModuleType.class, Order.asc("name").ignoreCase(), session);
    }

    public ModuleType obtainLocked(User user, ModuleType type, Session session) {
        return (ModuleType) lockingScheme.getLocked(user, current(type, session), session);
    }

    public ModuleType releaseLocked(User user, ModuleType locked, Session session) {
        return (ModuleType) lockingScheme.releaseLock(user, current(locked, session), session);
    }

    public ModuleType update(ModuleType type, Session session) throws EmfException {
        return (ModuleType) lockingScheme.renewLockOnUpdate(type, current(type, session), session);
    }

    public ModuleType get(String name, Session session) {
        Criterion criterion = Restrictions.eq("name", name);
        List list = hibernateFacade.get(ModuleType.class, criterion, session);
        return (list == null || list.size() == 0) ? null : (ModuleType) list.get(0);
    }

    public ModuleType get(int id, Session session) {
        Criterion criterion = Restrictions.eq("id", id);
        List list = hibernateFacade.get(ModuleType.class, criterion, session);
        return (list == null || list.size() == 0) ? null : (ModuleType) list.get(0);
    }

    public void add(ModuleType moduleType, Session session) {
        hibernateFacade.add(moduleType, session);
    }

    public boolean canUpdate(ModuleType moduleType, Session session) {
        if (!exists(moduleType.getId(), ModuleType.class, session)) {
            return false;
        }

        ModuleType current = current(moduleType.getId(), ModuleType.class, session);
        // The current object is saved in the session. Hibernate cannot persist our
        // object with the same id.
        session.clear();
        if (current.getName().equals(moduleType.getName()))
            return true;

        return !nameUsed(moduleType.getName(), ModuleType.class, session);
    }

    private boolean exists(int id, Class clazz, Session session) {
        return hibernateFacade.exists(id, clazz, session);
    }

    public boolean nameUsed(String name, Class clazz, Session session) {
        return hibernateFacade.nameUsed(name, clazz, session);
    }

    private ModuleType current(int id, Class clazz, Session session) {
        return (ModuleType) hibernateFacade.current(id, clazz, session);
    }

    public ModuleType current(ModuleType moduleType, Session session) {
        return current(moduleType.getId(), ModuleType.class, session);
    }

    private boolean hasColName(Column[] cols, String colName) {
        boolean hasIt = false;
        for (int i = 0; i < cols.length; i++)
            if (colName.equalsIgnoreCase(cols[i].name())) hasIt = true;

        return hasIt;
    }

    //---------------------------------------------
    
    public List getParameterTypes(Session session) {
        return hibernateFacade.getAll(ParameterType.class, Order.asc("sqlType").ignoreCase(), session);
    }

}
