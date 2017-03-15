package gov.epa.emissions.framework.services.module;

import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.persistence.HibernateFacade;
import gov.epa.emissions.framework.services.persistence.LockingScheme;

import java.io.Serializable;
import java.util.Date;
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
    
    public List getModules(Session session) {
        return getAll(session);
    }

    public void removeModule(Module module, Session session) {
        hibernateFacade.remove(module, session);
    }

    public Module obtainLockedModule(User user, Module module, Session session) {
        return obtainLocked(user, module, session);
    }

    public Module releaseLockedModule(User user, Module module, Session session) {
        return releaseLocked(user, module, session);
    }

    //----------------------------------------------------------------
    
    public List getAll(Session session) {
        return hibernateFacade.getAll(Module.class, Order.asc("name").ignoreCase(), session);
    }

    public Module obtainLocked(User user, Module module, Session session) {
        return (Module) lockingScheme.getLocked(user, currentModule(module, session), session);
    }

    public Module releaseLocked(User user, Module module, Session session) {
        return (Module) lockingScheme.releaseLock(user, currentModule(module, session), session);
    }

    public Module update(Module module, Session session) throws EmfException {
        return (Module) lockingScheme.renewLockOnUpdate(module, currentModule(module, session), session);
    }

    public History update(History history, Session session) {
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

    public HistorySubmodule update(HistorySubmodule historySubmodule, Session session) {
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

    public Module get(String name, Session session) {
        Criterion criterion = Restrictions.eq("name", name);
        List list = hibernateFacade.get(Module.class, criterion, session);
        return (list == null || list.size() == 0) ? null : (Module) list.get(0);
    }

    public Module get(int id, Session session) {
        Criterion criterion = Restrictions.eq("id", id);
        List list = hibernateFacade.get(Module.class, criterion, session);
        return (list == null || list.size() == 0) ? null : (Module) list.get(0);
    }

    public Module add(Module module, Session session) {
        Serializable serializable = hibernateFacade.add(module, session);
        String typeName = serializable.getClass().getName(); 
        Integer id = (Integer)serializable;
        module.setId(id);
        return module;
    }

    public boolean canUpdate(Module module, Session session) {
        if (!exists(module.getId(), Module.class, session)) {
            return false;
        }

        Module current = currentModule(module.getId(), Module.class, session);
        // The current object is saved in the session. Hibernate cannot persist our object with the same id.
        session.clear();
        if (current.getName().equals(module.getName()))
            return true;

        return !nameUsed(module.getName(), Module.class, session);
    }

    private boolean exists(int id, Class clazz, Session session) {
        return hibernateFacade.exists(id, clazz, session);
    }

    public boolean nameUsed(String name, Class clazz, Session session) {
        return hibernateFacade.nameUsed(name, clazz, session);
    }

    public Module currentModule(int id, Class clazz, Session session) {
        return (Module) hibernateFacade.current(id, clazz, session);
    }

    public Module currentModule(Module module, Session session) {
        return currentModule(module.getId(), Module.class, session);
    }

    public History currentHistory(int id, Class clazz, Session session) {
        return (History) hibernateFacade.current(id, clazz, session);
    }

    private boolean hasColName(Column[] cols, String colName) {
        boolean hasIt = false;
        for (int i = 0; i < cols.length; i++)
            if (colName.equalsIgnoreCase(cols[i].name())) hasIt = true;

        return hasIt;
    }

}
