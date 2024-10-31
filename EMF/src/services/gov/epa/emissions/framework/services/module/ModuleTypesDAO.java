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

import javax.persistence.EntityManager;

public class ModuleTypesDAO {

    private LockingScheme lockingScheme;

    private HibernateFacade hibernateFacade;

    public ModuleTypesDAO() {
        lockingScheme = new LockingScheme();
        hibernateFacade = new HibernateFacade();
    }

    //---------------------------------------------
    
    public List<ModuleType> getModuleTypes(EntityManager entityManager) {
        CriteriaBuilderQueryRoot<ModuleType> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(ModuleType.class, entityManager);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<ModuleType> root = criteriaBuilderQueryRoot.getRoot();

        return hibernateFacade.getAll(criteriaBuilderQueryRoot, builder.asc(root.get("name")), entityManager);
    }

    public List<ModuleType> getLockedModuleTypes(EntityManager entityManager) throws EmfException {
        try {
            CriteriaBuilderQueryRoot<ModuleType> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(ModuleType.class, entityManager);
            CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
            Root<ModuleType> root = criteriaBuilderQueryRoot.getRoot();

            return hibernateFacade.get(criteriaBuilderQueryRoot, new Predicate[] { builder.isNotNull(root.get("lockOwner")) }, entityManager);
        } catch (Exception ex) {
            throw new EmfException("Failed to get the list of locked module types: " + ex.getMessage());
        }
    }

    public void removeModuleType(ModuleType moduleType, EntityManager entityManager) {
        hibernateFacade.remove(moduleType, entityManager);
    }

    public ModuleType obtainLockedModuleType(User user, int moduleTypeId, EntityManager entityManager) {
        return (ModuleType) lockingScheme.getLocked(user, currentModuleType(moduleTypeId, entityManager), entityManager);
    }

    public ModuleType releaseLockedModuleType(User user, int moduleTypeId, EntityManager entityManager) {
        return (ModuleType) lockingScheme.releaseLock(user, currentModuleType(moduleTypeId, entityManager), entityManager);
    }

    public ModuleType updateModuleType(ModuleType moduleType, EntityManager entityManager) throws EmfException {
        return (ModuleType) lockingScheme.renewLockOnUpdate(moduleType, currentModuleType(moduleType.getId(), entityManager), entityManager);
    }

    public ModuleType getModuleType(String name, EntityManager entityManager) {
        List<ModuleType> list = hibernateFacade.get(ModuleType.class, "name", name, entityManager);
        return (list == null || list.size() == 0) ? null : list.get(0);
    }

    public ModuleType getModuleType(int id, EntityManager entityManager) {
        List<ModuleType> list = hibernateFacade.get(ModuleType.class, "id", Integer.valueOf(id), entityManager);
        return (list == null || list.size() == 0) ? null : list.get(0);
    }

    public void addModuleType(ModuleType moduleType, EntityManager entityManager) {
        hibernateFacade.add(moduleType, entityManager);
    }

    public boolean canUpdateModuleType(ModuleType moduleType, EntityManager entityManager) {
        if (!moduleTypeExists(moduleType.getId(), entityManager)) {
            return false;
        }

        ModuleType current = currentModuleType(moduleType.getId(), entityManager);
        // The current object is saved in the entityManager. Hibernate cannot persist our
        // object with the same id.
        entityManager.clear();
        if (current.getName().equals(moduleType.getName()))
            return true;

        return !moduleTypeNameUsed(moduleType.getName(), entityManager);
    }

    private boolean moduleTypeExists(int moduleTypeId, EntityManager entityManager) {
        return hibernateFacade.exists(moduleTypeId, ModuleType.class, entityManager);
    }

    public boolean moduleTypeNameUsed(String name, EntityManager entityManager) {
        return hibernateFacade.nameUsed(name, ModuleType.class, entityManager);
    }

    public ModuleType currentModuleType(int moduleTypeId, EntityManager entityManager) {
        return hibernateFacade.current(moduleTypeId, ModuleType.class, entityManager);
    }

    //---------------------------------------------
    
    public ModuleTypeVersion getModuleTypeVersion(int id, EntityManager entityManager) {
        List<ModuleTypeVersion> list = hibernateFacade.get(ModuleTypeVersion.class, "id", Integer.valueOf(id), entityManager);
        return (list == null || list.size() == 0) ? null : list.get(0);
    }

    public ModuleTypeVersion currentModuleTypeVersion(int moduleTypeVersionId, EntityManager entityManager) {
        return hibernateFacade.current(moduleTypeVersionId, ModuleTypeVersion.class, entityManager);
    }

    public void removeModuleTypeVersion(ModuleTypeVersion moduleTypeVersion, EntityManager entityManager) {
        hibernateFacade.remove(moduleTypeVersion, entityManager);
    }

    public ModuleTypeVersion addModuleTypeVersion(ModuleTypeVersion moduleTypeVersion, EntityManager entityManager) {
        hibernateFacade.add(moduleTypeVersion, entityManager);
        return currentModuleTypeVersion(moduleTypeVersion.getId(), entityManager);
    }

    public ModuleTypeVersion updateModuleTypeVersion(ModuleTypeVersion moduleTypeVersion, EntityManager entityManager) {
        hibernateFacade.updateOnly(moduleTypeVersion, entityManager);
        return currentModuleTypeVersion(moduleTypeVersion.getId(), entityManager);
    }

    //---------------------------------------------
    
    public List<ParameterType> getParameterTypes(EntityManager entityManager) {
        CriteriaBuilderQueryRoot<ParameterType> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(ParameterType.class, entityManager);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<ParameterType> root = criteriaBuilderQueryRoot.getRoot();

        return hibernateFacade.getAll(criteriaBuilderQueryRoot, builder.asc(builder.lower(root.get("sqlType"))), entityManager);
    }
    
    public List<Tag> getTags(EntityManager entityManager) {
        CriteriaBuilderQueryRoot<Tag> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(Tag.class, entityManager);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<Tag> root = criteriaBuilderQueryRoot.getRoot();

        return hibernateFacade.getAll(criteriaBuilderQueryRoot, builder.asc(root.get("name")), entityManager);
    }
    
    public void addTag(Tag tag, EntityManager entityManager) {
        hibernateFacade.add(tag, entityManager);
    }
}
