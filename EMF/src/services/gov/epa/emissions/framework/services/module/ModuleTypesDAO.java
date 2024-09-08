package gov.epa.emissions.framework.services.module;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.persistence.HibernateFacade;
import gov.epa.emissions.framework.services.persistence.HibernateFacade.CriteriaBuilderQueryRoot;
import gov.epa.emissions.framework.services.persistence.LockingScheme;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.hibernate.Session;

public class ModuleTypesDAO {

    private LockingScheme lockingScheme;

    private HibernateFacade hibernateFacade;

    public ModuleTypesDAO() {
        lockingScheme = new LockingScheme();
        hibernateFacade = new HibernateFacade();
    }

    //---------------------------------------------
    
    public List<ModuleType> getModuleTypes(Session session) {
        CriteriaBuilderQueryRoot<ModuleType> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(ModuleType.class, session);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<ModuleType> root = criteriaBuilderQueryRoot.getRoot();

        return hibernateFacade.getAll(criteriaBuilderQueryRoot, builder.asc(root.get("name")), session);
    }

    public List<ModuleType> getLockedModuleTypes(Session session) throws EmfException {
        try {
            CriteriaBuilderQueryRoot<ModuleType> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(ModuleType.class, session);
            CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
            Root<ModuleType> root = criteriaBuilderQueryRoot.getRoot();

            return hibernateFacade.get(criteriaBuilderQueryRoot, new Predicate[] { builder.isNotNull(root.get("lockOwner")) }, session);
        } catch (Exception ex) {
            throw new EmfException("Failed to get the list of locked module types: " + ex.getMessage());
        }
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
        List<ModuleType> list = hibernateFacade.get(ModuleType.class, "name", name, session);
        return (list == null || list.size() == 0) ? null : list.get(0);
    }

    public ModuleType getModuleType(int id, Session session) {
        List<ModuleType> list = hibernateFacade.get(ModuleType.class, "id", Integer.valueOf(id), session);
        return (list == null || list.size() == 0) ? null : list.get(0);
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
        return hibernateFacade.current(moduleTypeId, ModuleType.class, session);
    }

    //---------------------------------------------
    
    public ModuleTypeVersion getModuleTypeVersion(int id, Session session) {
        List<ModuleTypeVersion> list = hibernateFacade.get(ModuleTypeVersion.class, "id", Integer.valueOf(id), session);
        return (list == null || list.size() == 0) ? null : list.get(0);
    }

    public ModuleTypeVersion currentModuleTypeVersion(int moduleTypeVersionId, Session session) {
        return hibernateFacade.current(moduleTypeVersionId, ModuleTypeVersion.class, session);
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
    
    public List<ParameterType> getParameterTypes(Session session) {
        CriteriaBuilderQueryRoot<ParameterType> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(ParameterType.class, session);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<ParameterType> root = criteriaBuilderQueryRoot.getRoot();

        return hibernateFacade.getAll(criteriaBuilderQueryRoot, builder.asc(builder.lower(root.get("sqlType"))), session);
    }
    
    public List<Tag> getTags(Session session) {
        CriteriaBuilderQueryRoot<Tag> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(Tag.class, session);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<Tag> root = criteriaBuilderQueryRoot.getRoot();

        return hibernateFacade.getAll(criteriaBuilderQueryRoot, builder.asc(root.get("name")), session);
    }
    
    public void addTag(Tag tag, Session session) {
        hibernateFacade.add(tag, session);
    }
}
