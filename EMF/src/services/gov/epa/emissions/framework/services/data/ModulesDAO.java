package gov.epa.emissions.framework.services.data;

import gov.epa.emissions.commons.data.Module;
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

public class ModulesDAO {

    private LockingScheme lockingScheme;

    private HibernateFacade hibernateFacade;

    public ModulesDAO() {
        lockingScheme = new LockingScheme();
        hibernateFacade = new HibernateFacade();
    }

    public List getAll(Session session) {
        return hibernateFacade.getAll(Module.class, Order.asc("name").ignoreCase(), session);
    }

    public Module obtainLocked(User user, Module module, Session session) {
        return (Module) lockingScheme.getLocked(user, current(module, session), session);
    }

    public Module releaseLocked(User user, Module module, Session session) {
        return (Module) lockingScheme.releaseLock(user, current(module, session), session);
    }

    public Module update(Module module, Session session) throws EmfException {
        return (Module) lockingScheme.releaseLockOnUpdate(module, current(module, session), session);
    }

    public Module get(String name, Session session) {
        Criterion criterion = Restrictions.eq("name", name);
        List list = hibernateFacade.get(Module.class, criterion, session);
        return (list == null || list.size() == 0) ? null : (Module) list.get(0);
    }

    public void add(Module module, Session session) {
        hibernateFacade.add(module, session);
    }

    public boolean canUpdate(Module module, Session session) {
        if (!exists(module.getId(), Module.class, session)) {
            return false;
        }

        Module current = current(module.getId(), Module.class, session);
        // The current object is saved in the session. Hibernate cannot persist our
        // object with the same id.
        session.clear();
        if (current.getName().equals(module.getName()))
            return true;

        return !nameUsed(module.getName(), Module.class, session);
    }

    private boolean exists(int id, Class clazz, Session session) {
        return hibernateFacade.exists(id, clazz, session);
    }

    private boolean nameUsed(String name, Class clazz, Session session) {
        return hibernateFacade.nameUsed(name, clazz, session);
    }

    public Module current(int id, Class clazz, Session session) {
        return (Module) hibernateFacade.current(id, clazz, session);
    }

    private Module current(Module module, Session session) {
        return current(module.getId(), Module.class, session);
    }

    private boolean hasColName(Column[] cols, String colName) {
        boolean hasIt = false;
        for (int i = 0; i < cols.length; i++)
            if (colName.equalsIgnoreCase(cols[i].name())) hasIt = true;

        return hasIt;
    }

}
