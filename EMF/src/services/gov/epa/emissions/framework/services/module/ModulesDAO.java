package gov.epa.emissions.framework.services.module;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.BasicSearchFilter;
import gov.epa.emissions.framework.services.basic.SearchDAOUtility;
import gov.epa.emissions.framework.services.persistence.HibernateFacade;
import gov.epa.emissions.framework.services.persistence.HibernateFacade.CriteriaBuilderQueryRoot;
import gov.epa.emissions.framework.services.persistence.LockingScheme;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;

public class ModulesDAO {

    private LockingScheme lockingScheme;

    private HibernateFacade hibernateFacade;

    public ModulesDAO() {
        lockingScheme = new LockingScheme();
        hibernateFacade = new HibernateFacade();
    }

    //----------------------------------------------------------------
    
    public List<LiteModule> getLiteModules(EntityManager entityManager) {
        CriteriaBuilderQueryRoot<LiteModule> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(LiteModule.class, entityManager);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<LiteModule> root = criteriaBuilderQueryRoot.getRoot();

        return hibernateFacade.getAll(criteriaBuilderQueryRoot, builder.asc(builder.lower(root.get("name"))), entityManager);
    }

    public List<LiteModule> getLiteModules(EntityManager entityManager, BasicSearchFilter searchFilter) {
        String hql = "select distinct lm.id " +
                "from LiteModule as lm " +
                "left join lm.project as project " +
                "left join lm.creator as creator " +
                "left join lm.tags as tag " +
                "left join lm.liteModuleTypeVersion as liteModuleTypeVersion " +
                "left join liteModuleTypeVersion.liteModuleType as liteModuleType ";
        //
        if (StringUtils.isNotBlank(searchFilter.getFieldName())
                && StringUtils.isNotBlank(searchFilter.getFieldValue())) {
            String whereClause = SearchDAOUtility.buildSearchCriterion(new ModuleFilter(), searchFilter);
            if (StringUtils.isNotBlank(whereClause))
                hql += " where " + whereClause;
            
            
            List<Integer> moduleIds = entityManager.createQuery(hql, Integer.class).getResultList();

            CriteriaBuilderQueryRoot<LiteModule> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(LiteModule.class, entityManager);
            CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
            Root<LiteModule> root = criteriaBuilderQueryRoot.getRoot();
            CriteriaQuery<LiteModule> criteriaQuery = criteriaBuilderQueryRoot.getCriteriaQuery();
            
            criteriaQuery.select(root);
            criteriaQuery.where(builder.equal(root.get("id"), moduleIds));

            return hibernateFacade.getAll(criteriaBuilderQueryRoot, entityManager);
        }
        return new ArrayList<LiteModule>();
    }

    public LiteModule getLiteModule(int id, EntityManager entityManager) {
        List<LiteModule> list = hibernateFacade.get(LiteModule.class, "id", Integer.valueOf(id), entityManager);
        return (list == null || list.size() == 0) ? null : list.get(0);
    }

    public List<Module> getModules(EntityManager entityManager) {
        CriteriaBuilderQueryRoot<Module> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(Module.class, entityManager);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<Module> root = criteriaBuilderQueryRoot.getRoot();

        return hibernateFacade.getAll(criteriaBuilderQueryRoot, builder.asc(builder.lower(root.get("name"))), entityManager);
    }

    @SuppressWarnings("unchecked")
    public List<Module> getLockedModules(EntityManager entityManager) throws EmfException {
        try {
            CriteriaBuilderQueryRoot<Module> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(Module.class, entityManager);
            CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
            Root<Module> root = criteriaBuilderQueryRoot.getRoot();

            return hibernateFacade.get(criteriaBuilderQueryRoot, new Predicate[] { builder.isNotNull(root.get("lockOwner")) }, entityManager);
        } catch (Exception ex) {
            throw new EmfException("Failed to get the list of locked modules: " + ex.getMessage());
        }
    }

    public List<ModuleTypeVersionSubmodule> getSubmodulesUsingModuleTypeVersion(EntityManager entityManager, int moduleTypeVersionId) {
        CriteriaBuilderQueryRoot<ModuleTypeVersionSubmodule> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(ModuleTypeVersionSubmodule.class, entityManager);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<ModuleTypeVersionSubmodule> root = criteriaBuilderQueryRoot.getRoot();

        return hibernateFacade.get(criteriaBuilderQueryRoot, new Predicate[] { builder.equal(root.get("id"), Integer.valueOf(moduleTypeVersionId)) }, entityManager);
    }
    
    public List<ModuleTypeVersionSubmodule> getAllSubmodulesUsingModuleTypeVersion(EntityManager entityManager, int moduleTypeVersionId) {
        Map<Integer, ModuleTypeVersionSubmodule> allSubmodulesMap = new HashMap<Integer, ModuleTypeVersionSubmodule>();
        Set<Integer> todoMtvIds = new HashSet<Integer>();
        todoMtvIds.add(moduleTypeVersionId);
        while (todoMtvIds.size() > 0) {
            Set<Integer> newMtvIds = new HashSet<Integer>(todoMtvIds);
            todoMtvIds.clear();
            for(int newMtvId : newMtvIds) {
                List<ModuleTypeVersionSubmodule> newSubmodules = getSubmodulesUsingModuleTypeVersion(entityManager, newMtvId);
                for (ModuleTypeVersionSubmodule newSubmodule : newSubmodules) {
                    // making sure it's not an orphan submodule (that is, it's actually a submodule in its composite module type version)
                    if (newSubmodule.getCompositeModuleTypeVersion().containsSubmoduleId(newSubmodule.getId())) {
                        allSubmodulesMap.put(newSubmodule.getId(), newSubmodule);
                        todoMtvIds.add(newSubmodule.getCompositeModuleTypeVersion().getId());
                    }
                }
            }
        }
        List<ModuleTypeVersionSubmodule> allSubmodules = new ArrayList<ModuleTypeVersionSubmodule>(allSubmodulesMap.values());
        return allSubmodules;
    }
    
    @SuppressWarnings({ "unchecked" })
    public List<Module> getModulesUsingModuleTypeVersion(EntityManager entityManager, int moduleTypeVersionId) {
        CriteriaBuilderQueryRoot<Module> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(Module.class, entityManager);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<Module> root = criteriaBuilderQueryRoot.getRoot();

        return hibernateFacade.get(criteriaBuilderQueryRoot, new Predicate[] { builder.equal(root.get("id"), Integer.valueOf(moduleTypeVersionId)) }, entityManager);
    }
    
    public void removeModule(Module module, EntityManager entityManager) {
        hibernateFacade.remove(module, entityManager);
    }

    public void removeModule(int moduleId, EntityManager entityManager) {
        Module module = getModule(moduleId, entityManager);
        removeModule(module, entityManager);
    }

    public Module obtainLockedModule(User user, int moduleId, EntityManager entityManager) {
        return (Module) lockingScheme.getLocked(user, currentModule(moduleId, entityManager), entityManager);
    }

    public Module releaseLockedModule(User user, int moduleId, EntityManager entityManager) {
        return (Module) lockingScheme.releaseLock(user, currentModule(moduleId, entityManager), entityManager);
    }

    public ModuleDataset getModuleDataset(int moduleDatasetId, EntityManager entityManager) {
        List<ModuleDataset> list = hibernateFacade.get(ModuleDataset.class, "id", Integer.valueOf(moduleDatasetId), entityManager);
        return (list == null || list.size() == 0) ? null : list.get(0);
    }

    //----------------------------------------------------------------
    
    public Module updateModule(Module module, EntityManager entityManager) throws EmfException {
        return (Module) lockingScheme.renewLockOnUpdate(module, currentModule(module.getId(), entityManager), entityManager);
    }

    public History updateHistory(History history, EntityManager entityManager) {
        entityManager.clear();
        hibernateFacade.executeInsideTransaction(em -> {
            entityManager.merge(history);
        }, entityManager);
        return history;
    }

    public HistorySubmodule updateHistorySubmodule(HistorySubmodule historySubmodule, EntityManager entityManager) {
        entityManager.clear();
        hibernateFacade.executeInsideTransaction(em -> {
            entityManager.merge(historySubmodule);
        }, entityManager);
        return historySubmodule;
    }
    
    public void removeHistory(int historyId, EntityManager entityManager) {
        History history = currentHistory(historyId, entityManager);
        hibernateFacade.remove(history, entityManager);
    }

    public Module getModule(String name, EntityManager entityManager) {
        List<Module> list = hibernateFacade.get(Module.class, "name", name, entityManager);
        Module result = null;
        if (list != null && list.size() > 0) {
            result = list.get(0);
            result.setLastHistory(getLastHistory(result.getId(), entityManager));
        }
        return result;
    }

    public Module getModule(int id, EntityManager entityManager) {
        List<Module> list = hibernateFacade.get(Module.class, "id", Integer.valueOf(id), entityManager);
        Module result = null;
        if (list != null && list.size() > 0) {
            result = list.get(0);
            result.setLastHistory(getLastHistory(id, entityManager));
        }
        return result;
    }

    public Module add(Module module, EntityManager entityManager) {
        Serializable serializable = hibernateFacade.add(module, entityManager);
        Integer id = (Integer)serializable;
        module.setId(id);
        return module;
    }
    
    public History add(History history, EntityManager entityManager) {
        Serializable serializable = hibernateFacade.add(history, entityManager);
        Integer id = (Integer)serializable;
        history.setId(id);
        return history;
    }

    public boolean canUpdate(Module module, EntityManager entityManager) {
        if (!exists(module.getId(), entityManager)) {
            return false;
        }

        Module current = currentModule(module.getId(), entityManager);
        // The current object is saved in the entityManager. Hibernate cannot persist our object with the same id.
        entityManager.clear();
        if (current.getName().equals(module.getName()))
            return true;

        return !moduleNameUsed(module.getName(), entityManager);
    }

    private boolean exists(int id, EntityManager entityManager) {
        return hibernateFacade.exists(id, Module.class, entityManager);
    }

    public boolean moduleNameUsed(String name, EntityManager entityManager) {
        return hibernateFacade.nameUsed(name, Module.class, entityManager);
    }

    public Module currentModule(int moduleId, EntityManager entityManager) {
        return hibernateFacade.current(moduleId, Module.class, entityManager);
    }

    public History currentHistory(int historyId, EntityManager entityManager) {
        return hibernateFacade.current(historyId, History.class, entityManager);
    }
    
    public List getHistoryForModule(int moduleId, EntityManager entityManager) {
        CriteriaBuilderQueryRoot<History> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(History.class, entityManager);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<History> root = criteriaBuilderQueryRoot.getRoot();
        Join<Module, History> modJoin = root.join("module", JoinType.INNER);

        Predicate criterion = builder.equal(modJoin.get("id"), moduleId);
        return hibernateFacade.get(criteriaBuilderQueryRoot, criterion, builder.asc(root.get("runId")), entityManager);
    }
    
    public History getLastHistory(int moduleId, EntityManager entityManager) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<History> criteriaQuery = builder.createQuery(History.class);
        Root<History> root = criteriaQuery.from(History.class);
        Join<Module, History> modJoin = root.join("module", JoinType.INNER);

        criteriaQuery.select(root);

        criteriaQuery.where(builder.equal(modJoin.get("id"), Integer.valueOf(moduleId)));
        criteriaQuery.orderBy(builder.desc(root.get("id")));

        return entityManager
                .createQuery(criteriaQuery)
                .setMaxResults(1)
                .getSingleResult();
    }
}
