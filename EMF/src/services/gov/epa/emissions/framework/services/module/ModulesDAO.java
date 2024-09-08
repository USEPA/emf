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

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Property;

public class ModulesDAO {

    private LockingScheme lockingScheme;

    private HibernateFacade hibernateFacade;

    public ModulesDAO() {
        lockingScheme = new LockingScheme();
        hibernateFacade = new HibernateFacade();
    }

    //----------------------------------------------------------------
    
    public List<LiteModule> getLiteModules(Session session) {
        CriteriaBuilderQueryRoot<LiteModule> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(LiteModule.class, session);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<LiteModule> root = criteriaBuilderQueryRoot.getRoot();

        return hibernateFacade.getAll(criteriaBuilderQueryRoot, builder.asc(builder.lower(root.get("name"))), session);
    }

    @SuppressWarnings("rawtypes")
    public List getLiteModules(Session session, BasicSearchFilter searchFilter) {

        Criteria criteria = session.createCriteria(LiteModule.class, "lm")
//                .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
                ;
//                .setProjection(
//                        Projections.distinct(Projections.projectionList().add(Projections.property("lm.id"))))
//                .setResultTransformer(Transformers.aliasToBean(LiteModule.class));

//        criteria.createAlias("lm.project", "project", CriteriaSpecification.LEFT_JOIN);
//        criteria.createAlias("lm.creator", "creator", CriteriaSpecification.LEFT_JOIN);
//        criteria.createAlias("lm.tags", "tag", CriteriaSpecification.LEFT_JOIN);
//        criteria.createAlias("lm.liteModuleTypeVersion", "liteModuleTypeVersion", CriteriaSpecification.LEFT_JOIN);
//        criteria.createAlias("liteModuleTypeVersion.liteModuleType", "liteModuleType", CriteriaSpecification.LEFT_JOIN);

        if (StringUtils.isNotBlank(searchFilter.getFieldName())
                && StringUtils.isNotBlank(searchFilter.getFieldValue())) {
            Criteria inCriteria = session.createCriteria(LiteModule.class, "lm")
                    .setProjection(Property.forName("id"));
            //                .setProjection(Projections.projectionList().add(Projections.property("lm.id")))
            //                .setResultTransformer(Transformers.aliasToBean(Integer.class));
            //                .setResultTransformer(Transformers.aliasToBean(LiteModule.class));
            inCriteria.createAlias("lm.project", "project", CriteriaSpecification.LEFT_JOIN);
            inCriteria.createAlias("lm.creator", "creator", CriteriaSpecification.LEFT_JOIN);
            inCriteria.createAlias("lm.tags", "tag", CriteriaSpecification.LEFT_JOIN);
            inCriteria.createAlias("lm.liteModuleTypeVersion", "liteModuleTypeVersion", CriteriaSpecification.LEFT_JOIN);
            inCriteria.createAlias("liteModuleTypeVersion.liteModuleType", "liteModuleType", CriteriaSpecification.LEFT_JOIN);

            SearchDAOUtility.buildSearchCriterion(inCriteria, new ModuleFilter(), searchFilter);

            //get module ids to load from...
            List<LiteModule> moduleIds = inCriteria.list();

            if (moduleIds.size() > 0)
                criteria
                    .add(Property.forName("id").in(moduleIds));
            else
                criteria
                    .add(Property.forName("id").eq((Object)null));
        }

        List<LiteModule> modules = criteria.list();

        return modules;
    }

    public LiteModule getLiteModule(int id, Session session) {
        List<LiteModule> list = hibernateFacade.get(LiteModule.class, "id", Integer.valueOf(id), session);
        return (list == null || list.size() == 0) ? null : list.get(0);
    }

    public List<Module> getModules(Session session) {
        CriteriaBuilderQueryRoot<Module> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(Module.class, session);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<Module> root = criteriaBuilderQueryRoot.getRoot();

        return hibernateFacade.getAll(criteriaBuilderQueryRoot, builder.asc(builder.lower(root.get("name"))), session);
    }

    @SuppressWarnings("unchecked")
    public List<Module> getLockedModules(Session session) throws EmfException {
        try {
            CriteriaBuilderQueryRoot<Module> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(Module.class, session);
            CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
            Root<Module> root = criteriaBuilderQueryRoot.getRoot();

            return hibernateFacade.get(criteriaBuilderQueryRoot, new Predicate[] { builder.isNotNull(root.get("lockOwner")) }, session);
        } catch (Exception ex) {
            throw new EmfException("Failed to get the list of locked modules: " + ex.getMessage());
        }
    }

    public List<ModuleTypeVersionSubmodule> getSubmodulesUsingModuleTypeVersion(Session session, int moduleTypeVersionId) {
        CriteriaBuilderQueryRoot<ModuleTypeVersionSubmodule> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(ModuleTypeVersionSubmodule.class, session);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<ModuleTypeVersionSubmodule> root = criteriaBuilderQueryRoot.getRoot();

        return hibernateFacade.get(criteriaBuilderQueryRoot, new Predicate[] { builder.equal(root.get("id"), Integer.valueOf(moduleTypeVersionId)) }, session);
    }
    
    public List<ModuleTypeVersionSubmodule> getAllSubmodulesUsingModuleTypeVersion(Session session, int moduleTypeVersionId) {
        Map<Integer, ModuleTypeVersionSubmodule> allSubmodulesMap = new HashMap<Integer, ModuleTypeVersionSubmodule>();
        Set<Integer> todoMtvIds = new HashSet<Integer>();
        todoMtvIds.add(moduleTypeVersionId);
        while (todoMtvIds.size() > 0) {
            Set<Integer> newMtvIds = new HashSet<Integer>(todoMtvIds);
            todoMtvIds.clear();
            for(int newMtvId : newMtvIds) {
                List<ModuleTypeVersionSubmodule> newSubmodules = getSubmodulesUsingModuleTypeVersion(session, newMtvId);
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
    public List<Module> getModulesUsingModuleTypeVersion(Session session, int moduleTypeVersionId) {
        CriteriaBuilderQueryRoot<Module> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(Module.class, session);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<Module> root = criteriaBuilderQueryRoot.getRoot();

        return hibernateFacade.get(criteriaBuilderQueryRoot, new Predicate[] { builder.equal(root.get("id"), Integer.valueOf(moduleTypeVersionId)) }, session);
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
        List<ModuleDataset> list = hibernateFacade.get(ModuleDataset.class, "id", Integer.valueOf(moduleDatasetId), session);
        return (list == null || list.size() == 0) ? null : list.get(0);
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

    public HistorySubmodule updateHistorySubmodule(HistorySubmodule historySubmodule, Session session) {
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
    
    public void removeHistory(int historyId, Session session) {
        History history = currentHistory(historyId, session);
        hibernateFacade.remove(history, session);
    }

    public Module getModule(String name, Session session) {
        List<Module> list = hibernateFacade.get(Module.class, "name", name, session);
        Module result = null;
        if (list != null && list.size() > 0) {
            result = list.get(0);
            result.setLastHistory(getLastHistory(result.getId(), session));
        }
        return result;
    }

    public Module getModule(int id, Session session) {
        List<Module> list = hibernateFacade.get(Module.class, "id", Integer.valueOf(id), session);
        Module result = null;
        if (list != null && list.size() > 0) {
            result = list.get(0);
            result.setLastHistory(getLastHistory(id, session));
        }
        return result;
    }

    public Module add(Module module, Session session) {
        Serializable serializable = hibernateFacade.add(module, session);
        Integer id = (Integer)serializable;
        module.setId(id);
        return module;
    }
    
    public History add(History history, Session session) {
        Serializable serializable = hibernateFacade.add(history, session);
        Integer id = (Integer)serializable;
        history.setId(id);
        return history;
    }

    public boolean canUpdate(Module module, Session session) {
        if (!exists(module.getId(), session)) {
            return false;
        }

        Module current = currentModule(module.getId(), session);
        // The current object is saved in the session. Hibernate cannot persist our object with the same id.
        session.clear();
        if (current.getName().equals(module.getName()))
            return true;

        return !moduleNameUsed(module.getName(), session);
    }

    private boolean exists(int id, Session session) {
        return hibernateFacade.exists(id, Module.class, session);
    }

    public boolean moduleNameUsed(String name, Session session) {
        return hibernateFacade.nameUsed(name, Module.class, session);
    }

    public Module currentModule(int moduleId, Session session) {
        return hibernateFacade.current(moduleId, Module.class, session);
    }

    public History currentHistory(int historyId, Session session) {
        return hibernateFacade.current(historyId, History.class, session);
    }
    
    public List getHistoryForModule(int moduleId, Session session) {
        CriteriaBuilderQueryRoot<History> criteriaBuilderQueryRoot = hibernateFacade.getCriteriaBuilderQueryRoot(History.class, session);
        CriteriaBuilder builder = criteriaBuilderQueryRoot.getBuilder();
        Root<History> root = criteriaBuilderQueryRoot.getRoot();
        Join<Module, History> modJoin = root.join("module", JoinType.INNER);

        Predicate criterion = builder.equal(modJoin.get("id"), moduleId);
        return hibernateFacade.get(criteriaBuilderQueryRoot, criterion, builder.asc(root.get("runId")), session);
    }
    
    public History getLastHistory(int moduleId, Session session) {
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<History> criteriaQuery = builder.createQuery(History.class);
        Root<History> root = criteriaQuery.from(History.class);
        Join<Module, History> modJoin = root.join("module", JoinType.INNER);

        criteriaQuery.select(root);

        criteriaQuery.where(builder.equal(modJoin.get("id"), Integer.valueOf(moduleId)));
        criteriaQuery.orderBy(builder.desc(root.get("id")));

        return session
                .createQuery(criteriaQuery)
                .setMaxResults(1)
                .uniqueResult();
    }
}
