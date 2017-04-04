package gov.epa.emissions.framework.services.module;

import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.persistence.HibernateFacade;
import gov.epa.emissions.framework.services.persistence.LockingScheme;

import java.io.Serializable;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

public class ModulesDAO {

    private LockingScheme lockingScheme;

    private HibernateFacade hibernateFacade;

    public ModulesDAO() {
        lockingScheme = new LockingScheme();
        hibernateFacade = new HibernateFacade();
    }

    //----------------------------------------------------------------
    
    @SuppressWarnings("rawtypes")
    public List getLiteModules(Session session) {
        return hibernateFacade.getAll(LiteModule.class, Order.asc("name").ignoreCase(), session);
    }

    public LiteModule getLiteModule(int id, Session session) {
        Criterion criterion = Restrictions.eq("id", id);
        @SuppressWarnings("rawtypes")
        List list = hibernateFacade.get(LiteModule.class, criterion, session);
        return (list == null || list.size() == 0) ? null : (LiteModule) list.get(0);
    }

    public List getModules(Session session) {
        return hibernateFacade.getAll(Module.class, Order.asc("name").ignoreCase(), session);
    }

    @SuppressWarnings({ "unchecked" })
    public List<Module> getModulesForModuleTypeVersion(Session session, int moduleTypeVersionId) {
        List<?> modules = session.createCriteria(Module.class)
                                 .createCriteria("moduleTypeVersion").add(Restrictions.eq("id", moduleTypeVersionId))
                                 .list();
        return (List<Module>) modules;
    }
    
    public void removeModule(Module module, Session session) {
        hibernateFacade.remove(module, session);
    }

    public void removeModule(int moduleId, Session session) {
        Module module = getModule(moduleId, session);
        removeModule(module, session);
    }

    public Module obtainLockedModule(User user, int moduleId, Session session) {
        return (Module) lockingScheme.getLocked(user, currentModule(moduleId, session), session);
    }

    public Module releaseLockedModule(User user, int moduleId, Session session) {
        return (Module) lockingScheme.releaseLock(user, currentModule(moduleId, session), session);
    }

    public ModuleDataset getModuleDataset(int moduleDatasetId, Session session) {
        Criterion criterion = Restrictions.eq("id", moduleDatasetId);
        @SuppressWarnings("rawtypes")
        List list = hibernateFacade.get(ModuleDataset.class, criterion, session);
        return (list == null || list.size() == 0) ? null : (ModuleDataset) list.get(0);
    }

    //----------------------------------------------------------------
    
    public Module updateModule(Module module, Session session) throws EmfException {
        return (Module) lockingScheme.renewLockOnUpdate(module, currentModule(module.getId(), session), session);
    }

    public History updateHistory(History history, Session session) {
        session.clear();
        Transaction tx = session.beginTransaction();
        try {
            session.update(history);
            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
        return history;
    }

    public HistorySubmodule updateSubmodule(HistorySubmodule historySubmodule, Session session) {
        session.clear();
        Transaction tx = session.beginTransaction();
        try {
            session.update(historySubmodule);
            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
        return historySubmodule;
    }

    public Module getModule(String name, Session session) {
        Criterion criterion = Restrictions.eq("name", name);
        @SuppressWarnings("rawtypes")
        List list = hibernateFacade.get(Module.class, criterion, session);
        return (list == null || list.size() == 0) ? null : (Module) list.get(0);
    }

    public Module getModule(int id, Session session) {
        Criterion criterion = Restrictions.eq("id", id);
        @SuppressWarnings("rawtypes")
        List list = hibernateFacade.get(Module.class, criterion, session);
        return (list == null || list.size() == 0) ? null : (Module) list.get(0);
    }

    public Module add(Module module, Session session) {
        Serializable serializable = hibernateFacade.add(module, session);
        Integer id = (Integer)serializable;
        module.setId(id);
        return module;
    }

    public boolean canUpdate(Module module, Session session) {
        if (!exists(module.getId(), Module.class, session)) {
            return false;
        }

        Module current = currentModule(module.getId(), session);
        // The current object is saved in the session. Hibernate cannot persist our object with the same id.
        session.clear();
        if (current.getName().equals(module.getName()))
            return true;

        return !moduleNameUsed(module.getName(), session);
    }

    private boolean exists(int id, Class clazz, Session session) {
        return hibernateFacade.exists(id, clazz, session);
    }

    public boolean moduleNameUsed(String name, Session session) {
        return hibernateFacade.nameUsed(name, Module.class, session);
    }

    public Module currentModule(int moduleId, Session session) {
        return (Module) hibernateFacade.current(moduleId, Module.class, session);
    }

    public History currentHistory(int historyId, Session session) {
        return (History) hibernateFacade.current(historyId, History.class, session);
    }
}
