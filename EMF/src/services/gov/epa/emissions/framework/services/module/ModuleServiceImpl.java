package gov.epa.emissions.framework.services.module;

import gov.epa.emissions.commons.data.KeyVal;
import gov.epa.emissions.commons.data.Keyword;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.module.DatasetCreator;

import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.GCEnforcerTask;
import gov.epa.emissions.framework.services.basic.StatusDAO;
import gov.epa.emissions.framework.services.data.DataCommonsDAO;
import gov.epa.emissions.framework.services.data.DatasetDAO;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.Keywords;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;

import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;

public class ModuleServiceImpl implements ModuleService {

    private static Log LOG = LogFactory.getLog(ModuleServiceImpl.class);

    private HibernateSessionFactory sessionFactory;

    private DbServerFactory dbServerFactory;

    private ModulesDAO modulesDAO;
    
    private ModuleTypesDAO moduleTypesDAO;
    
    private StatusDAO statusDAO;
    
    private PooledExecutor threadPool;
    
    private DatasetDAO datasetDAO;
    
    private DataCommonsDAO dataCommonsDAO;
    
    private Keywords keywords;

    public ModuleServiceImpl() {
        this(HibernateSessionFactory.get(), DbServerFactory.get());
    }

    public ModuleServiceImpl(HibernateSessionFactory sessionFactory) {
        this(sessionFactory, DbServerFactory.get());
    }

    public ModuleServiceImpl(HibernateSessionFactory sessionFactory, DbServerFactory dbServerFactory) {
        this.sessionFactory = sessionFactory;
        this.dbServerFactory = dbServerFactory;
        this.threadPool = createThreadPool();
        modulesDAO = new ModulesDAO();
        moduleTypesDAO = new ModuleTypesDAO();
        statusDAO = new StatusDAO(sessionFactory);
        datasetDAO = new DatasetDAO(dbServerFactory);
        dataCommonsDAO = new DataCommonsDAO();
        keywords = DatasetCreator.getKeywords(sessionFactory);
    }

    private synchronized PooledExecutor createThreadPool() {
        PooledExecutor threadPool = new PooledExecutor(20);
        threadPool.setMinimumPoolSize(1);
        threadPool.setKeepAliveTime(1000 * 60 * 3);// terminate after 3 (unused) minutes

        return threadPool;
    }

    @Override
    public synchronized ModuleType[] getModuleTypes() throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            @SuppressWarnings("unchecked")
            List<ModuleType> list = moduleTypesDAO.getModuleTypes(session);

            ModuleType[] moduleTypes = list.toArray(new ModuleType[0]); 
            return moduleTypes;
        } catch (Exception e) {
            LOG.error("Could not get all ModuleTypes", e);
            throw new EmfException("Could not get all ModuleTypes: " + e.getMessage());
        } finally {
            session.close(); 
        }
    }

    @Override
    public synchronized ModuleType getModuleType(int id) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            ModuleType moduleType = moduleTypesDAO.getModuleType(id, session);
            return moduleType;
        } catch (Exception e) {
            LOG.error("Could not get ModuleType (ID=" + id + ")", e);
            throw new EmfException("Could not get ModuleType (ID=" + id + "): " + e.getMessage());
        } finally {
            session.close(); 
        }
    }

    @Override
    public synchronized ParameterType[] getParameterTypes() throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            @SuppressWarnings("unchecked")
            List<ParameterType> list = moduleTypesDAO.getParameterTypes(session);

            ParameterType[] moduleTypes = list.toArray(new ParameterType[0]); 
            return moduleTypes;
        } catch (Exception e) {
            LOG.error("Could not get all ParameterTypes", e);
            throw new EmfException("Could not get all ParameterTypes: " + e.getMessage());
        } finally {
            session.close(); 
        }
    }

    private String computeModuleTypeVersionCleanupScript(ModuleTypeVersion oldMTV, ModuleTypeVersion moduleTypeVersion, Session session) throws EmfException {
        StringBuilder statements = new StringBuilder();
        
        try {
            StringBuilder ids = new StringBuilder();
            for (ModuleTypeVersionDataset oldMTVD : oldMTV.getModuleTypeVersionDatasets().values()) {
                if (moduleTypeVersion.containsDatasetId(oldMTVD.getId()))
                    continue;
                if (ids.length() > 0)
                    ids.append(",");
                ids.append(oldMTVD.getId());
            }
            if (ids.length() > 0) {
                statements.append("DELETE FROM modules.module_types_versions_datasets WHERE id IN (" + ids.toString() + ");\n");
            }
    
            ids.setLength(0);
            for (ModuleTypeVersionParameter oldMTVP : oldMTV.getModuleTypeVersionParameters().values()) {
                if (moduleTypeVersion.containsParameterId(oldMTVP.getId()))
                    continue;
                if (ids.length() > 0)
                    ids.append(",");
                ids.append(oldMTVP.getId());
            }
            if (ids.length() > 0) {
                statements.append("DELETE FROM modules.module_types_versions_parameters WHERE id IN (" + ids.toString() + ");\n");
            }
    
            ids.setLength(0);
            for (ModuleTypeVersionSubmodule oldMTVS : oldMTV.getModuleTypeVersionSubmodules().values()) {
                if (moduleTypeVersion.containsSubmoduleId(oldMTVS.getId()))
                    continue;
                if (ids.length() > 0)
                    ids.append(",");
                ids.append(oldMTVS.getId());
            }
            if (ids.length() > 0) {
                statements.append("DELETE FROM modules.module_types_versions_submodules WHERE id IN (" + ids.toString() + ");\n");
            }
            
            ids.setLength(0);
            for (ModuleTypeVersionDatasetConnection oldMTVDC : oldMTV.getModuleTypeVersionDatasetConnections().values()) {
                if (moduleTypeVersion.containsDatasetConnectionId(oldMTVDC.getId()))
                    continue;
                if (ids.length() > 0)
                    ids.append(",");
                ids.append(oldMTVDC.getId());
            }
            if (ids.length() > 0) {
                statements.append("DELETE FROM modules.module_types_versions_connections_datasets WHERE id IN (" + ids.toString() + ");\n");
            }
            
            ids.setLength(0);
            for (ModuleTypeVersionParameterConnection oldMTVPC : oldMTV.getModuleTypeVersionParameterConnections().values()) {
                if (moduleTypeVersion.containsParameterConnectionId(oldMTVPC.getId()))
                    continue;
                if (ids.length() > 0)
                    ids.append(",");
                ids.append(oldMTVPC.getId());
            }
            if (ids.length() > 0) {
                statements.append("DELETE FROM modules.module_types_versions_connections_parameters WHERE id IN (" + ids.toString() + ");\n");
            }
        } catch (Exception e) {
            String errorMessage = "Failed to compute " + moduleTypeVersion.fullNameSS("\"%s\" version \"%s\"") + " cleanup script";
            LOG.error(errorMessage + ".", e);
            e.printStackTrace();
            throw new EmfException(errorMessage + ": " + e.getMessage());
        }
        
        return statements.toString();
    }

    @Override
    public synchronized ModuleType updateModuleTypeVersion(ModuleTypeVersion moduleTypeVersion, User user) throws EmfException {
        ModuleType moduleType = moduleTypeVersion.getModuleType();
        if (!moduleType.isLocked(user)) {
            throw new EmfException("Can't save module type version because the module type is not locked by " + user.getName() + ".");
        }
        
        Session session = sessionFactory.getSession();

        // lock all dependent modules
        
        Module[] modules = getModulesForModuleTypeVersion(moduleTypeVersion.getId());

        StringBuilder list = new StringBuilder();
        for (Module module : modules) {
            if (module.isLocked()) {
                list.append(module.getName() + " locked by " + module.getLockOwner() + " at " +
                            CustomDateFormat.format_YYYY_MM_DD_HH_MM(module.getLockDate()) + "\n");
            }
        }
        if (!list.toString().isEmpty()) {
            throw new EmfException("Can't save module type version because the following dependent modules are locked:\n" + list.toString());
        }

        int nextModule = 0;
        try {
            for (nextModule = 0; nextModule < modules.length; nextModule++) {
                modules[nextModule] = modulesDAO.obtainLockedModule(user, modules[nextModule].getId(), session);
            }
        } catch (Exception ex) {
            for (int i = 0; i < nextModule; i++) {
                if (modules[i].isLocked()) {
                    try {
                        modules[i] = modulesDAO.releaseLockedModule(user, modules[i].getId(), session);
                    } catch (Exception e) {
                        // NOTE Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
            throw new EmfException("Can't save module type version: failed to lock the \"" + modules[nextModule].getName() + "\" module. " + ex.getMessage());
        }

        Connection connection = null;
        Statement statement = null;
        try {
            connection = dbServerFactory.getDbServer().getConnection();
            statement = connection.createStatement();
            
            ModuleTypeVersion oldMTV = moduleTypesDAO.currentModuleTypeVersion(moduleTypeVersion.getId(), session);
            
            session.clear();
            moduleType = moduleTypesDAO.updateModuleType(moduleType, session);
            moduleTypeVersion = moduleType.getModuleTypeVersions().get(moduleTypeVersion.getVersion());
        
            if (oldMTV != null) {
                String clenupScript = computeModuleTypeVersionCleanupScript(oldMTV, moduleTypeVersion, session);
                
                // manually delete the missing datasets, parameters, submodules, and connections from the database
                // this is necessary to compensate for an old Hibernate bug
                
                if (clenupScript.length() > 0) {
                    try {
                        statement.execute(clenupScript);
                    } catch (Exception e) {
                        throw new EmfException("Failed to execute " + moduleTypeVersion.fullNameSS("\"%s\" version \"%s\"") + " cleanup script: " + e.getMessage());
                    }
                }
    
                // get fresh modules with updated moduleTypeVersion
                modules = getModulesForModuleTypeVersion(moduleTypeVersion.getId());
    
                StringBuilder errorMessage = new StringBuilder();
                for (nextModule = 0; nextModule < modules.length; nextModule++) {
                    try {
                        if (modules[nextModule].update(oldMTV)) {
                            modules[nextModule] = updateModule(modules[nextModule]);
                        }
                        modules[nextModule] = modulesDAO.releaseLockedModule(user, modules[nextModule].getId(), session);
                    } catch (Exception e) {
                        errorMessage.append("Failed to update or unlock the \"" + modules[nextModule].getName() + "\" module. " + e.getMessage());
                        e.printStackTrace();
                    }
                }
                if (errorMessage.length() > 0) {
                    throw new EmfException(errorMessage.toString());
                }
            }
        } catch (Exception e) {
            LOG.error("Failed to update module type \"" + moduleType.getName() + "\" version \"" + moduleTypeVersion.versionName() + "\"", e);
            e.printStackTrace();
            throw new EmfException("Failed to update module type \"" + moduleType.getName() + "\" version \"" + moduleTypeVersion.versionName() + "\": " + e.getMessage());
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    // ignore
                }
                statement = null;
            }
                
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    // NOTE Auto-generated catch block
                    e.printStackTrace();
                }
                connection = null;
            }

            StringBuilder errorMessage = new StringBuilder();
            for (int i = 0; i < modules.length; i++) {
                if (modules[i].isLocked()) {
                    try {
                        modules[i] = modulesDAO.releaseLockedModule(user, modules[i].getId(), session);
                    } catch (Exception e) {
                        errorMessage.append("Failed to unlock the \"" + modules[i].getName() + "\" module. " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
            if (errorMessage.length() > 0) {
                throw new EmfException(errorMessage.toString());
            }

            session.close(); 
        }

        return moduleTypeVersion.getModuleType();
    }

    private void finalizeSubmodules(ModuleTypeVersion moduleTypeVersion, User user, Session session, Date lastChangeDate) throws EmfException {
        TreeMap<Integer, ModuleTypeVersion> unfinalizedSubmodules = moduleTypeVersion.getUnfinalizedSubmodules();
        if (unfinalizedSubmodules.size() == 0)
            return;
        String fullName = moduleTypeVersion.fullNameSS("\"%s\" version \"%s\"");
        TreeMap<Integer, ModuleType> lockedModuleTypes = new TreeMap<Integer, ModuleType>();
        try {
            for (ModuleTypeVersion unfinalizedModuleTypeVersion : unfinalizedSubmodules.values()) {
                ModuleType moduleType = unfinalizedModuleTypeVersion.getModuleType();
                if (moduleType.equals(moduleTypeVersion.getModuleType())) // possible when a module type version uses one of its
                    continue;                                             // siblings as a submodule (directly or indirectly)
                if (lockedModuleTypes.containsKey(moduleType.getId()))
                    continue;
                if (moduleType.isLocked()) {
                    String errorMessage = String.format("Module type \"%s\" has been locked by %s since %s.\n",
                                                         moduleType.getName(), moduleType.getLockOwner(),
                                                         CustomDateFormat.format_YYYY_MM_DD_HH_MM(moduleType.getLockDate()));
                    throw new EmfException(errorMessage);
                }
                moduleType = moduleTypesDAO.obtainLockedModuleType(user, moduleType.getId(), session);
                lockedModuleTypes.put(moduleType.getId(), moduleType);
                unfinalizedModuleTypeVersion.setModuleType(moduleType);
            }
            
            for (ModuleTypeVersion unfinalizedModuleTypeVersion : unfinalizedSubmodules.values()) {
                unfinalizedModuleTypeVersion.setIsFinal(true);
                unfinalizedModuleTypeVersion.setLastModifiedDate(lastChangeDate);
                
                ModuleTypeVersionRevision moduleTypeVersionRevision = new ModuleTypeVersionRevision();
                moduleTypeVersionRevision.setCreationDate(lastChangeDate);
                moduleTypeVersionRevision.setCreator(user);
                moduleTypeVersionRevision.setDescription("Finalized while finalizing " + fullName);
                unfinalizedModuleTypeVersion.addModuleTypeVersionRevision(moduleTypeVersionRevision);
                
                ModuleType moduleType = unfinalizedModuleTypeVersion.getModuleType();
                moduleType.addModuleTypeVersion(unfinalizedModuleTypeVersion);
            }
            
            for (ModuleType lockedModuleType : lockedModuleTypes.values()) {
                lockedModuleType = moduleTypesDAO.updateModuleType(lockedModuleType, session);
                lockedModuleTypes.put(lockedModuleType.getId(), lockedModuleType);
            }
        } finally {
            for (ModuleType moduleType : lockedModuleTypes.values()) {
                moduleType = moduleTypesDAO.releaseLockedModuleType(user, moduleType.getId(), session);
            }
        }
    }
    
    @Override
    public synchronized ModuleType finalizeModuleTypeVersion(int moduleTypeVersionId, User user) throws EmfException {
        Session session = this.sessionFactory.getSession();
        try {
            ModuleTypeVersion moduleTypeVersion = moduleTypesDAO.getModuleTypeVersion(moduleTypeVersionId, session);
            if (moduleTypeVersion == null)
                throw new EmfException("Cant't finalize module type version (ID=" + moduleTypeVersionId +"): invalid ID");
            
            ModuleType moduleType = moduleTypeVersion.getModuleType();
            if (moduleTypeVersion.getIsFinal())
                return moduleType;
            
            String fullName = moduleTypeVersion.fullNameSS("\"%s\" version \"%s\"");
            if (!moduleType.isLocked(user))
                throw new EmfException("Cant't finalize module type " + fullName + ": the module type is not locked by " + user.getName());
            
            Date lastChangeDate = new Date();
            if (moduleTypeVersion.isComposite()) {
                finalizeSubmodules(moduleTypeVersion, user, session, lastChangeDate);
                moduleTypeVersion = moduleTypesDAO.getModuleTypeVersion(moduleTypeVersionId, session);
            }
            
            moduleTypeVersion.setIsFinal(true);
            moduleTypeVersion.setLastModifiedDate(lastChangeDate);
            
            ModuleTypeVersionRevision moduleTypeVersionRevision = new ModuleTypeVersionRevision();
            moduleTypeVersionRevision.setCreationDate(lastChangeDate);
            moduleTypeVersionRevision.setCreator(user);
            moduleTypeVersionRevision.setDescription("Finalized");
            moduleTypeVersion.addModuleTypeVersionRevision(moduleTypeVersionRevision);
            
            moduleTypeVersion = moduleTypesDAO.updateModuleTypeVersion(moduleTypeVersion, session);
            moduleType = moduleTypeVersion.getModuleType();
            
            return moduleType;
        } catch (Exception e) {
            LOG.error("Error finalizing module type version." , e);
            throw new EmfException("Error finalizing module type version.\n" + e.getMessage());
        } finally {
            session.close(); 
        }
    }

    @Override
    public synchronized void deleteModuleTypes(User owner, ModuleType[] types) throws EmfException {
        Session session = this.sessionFactory.getSession();
        try {
            for (int i=0; i<types.length; i++) {
                moduleTypesDAO.removeModuleType(types[i], session);
            }
        } catch (Exception e) {
            LOG.error("Error deleting module types. " , e);
            throw new EmfException("Error deleting module types. \n" + e.getMessage());
        } finally {
            session.close(); 
        }
    }

    @Override
    public synchronized ModuleType obtainLockedModuleType(User user, int moduleTypeId) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            ModuleType locked = moduleTypesDAO.obtainLockedModuleType(user, moduleTypeId, session);
            return locked;
        } catch (Exception e) {
            LOG.error("Could not obtain lock for module type (ID = " + moduleTypeId + "): ", e);
            throw new EmfException("Could not obtain lock for module type (ID = " + moduleTypeId + "): " + e.getMessage());
        } finally {
            session.close();
        }
    }

    @Override
    public synchronized ModuleType releaseLockedModuleType(User user, int moduleTypeId) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            ModuleType locked = moduleTypesDAO.releaseLockedModuleType(user, moduleTypeId, session);
            return locked;
        } catch (Exception e) {
            LOG.error("Could not release lock for module type (ID = " + moduleTypeId + "): ", e);
            throw new EmfException("Could not release lock for module type (ID = " + moduleTypeId + "): " + e.getMessage());
        } finally {
            session.close();
        }
    }

    @Override
    public synchronized ModuleType addModuleType(ModuleType moduleType) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            if (moduleTypesDAO.moduleTypeNameUsed(moduleType.getName(), session))
                throw new EmfException("The \"" + moduleType.getName() + "\" name is already in use");
            moduleTypesDAO.addModuleType(moduleType, session);
            return moduleType;
        } catch (Exception e) {
            LOG.error("Could not add new module type", e);
            throw new EmfException("Could not add module type " + moduleType.getName() + ": " + e.toString());
        } finally {
            session.close();
        }
    }

//    public synchronized Module[] getModules() throws EmfException {
//        Session session = sessionFactory.getSession();
//        try {
//            @SuppressWarnings("unchecked")
//            List<Module> list = modulesDAO.getModules(session);
//
//            Module[] modules = list.toArray(new Module[0]); 
//            return modules;
//        } catch (Exception e) {
//            LOG.error("Could not get all modules", e);
//            throw new EmfException("Could not get all modules: " + e.getMessage());
//        } finally {
//            session.close();
//        }
//    }

    @Override
    public synchronized Module getModule(int id) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            Module module = modulesDAO.getModule(id, session);
            return module;
        } catch (Exception e) {
            LOG.error("Could not get module (ID=" + id + ")", e);
            throw new EmfException("Could not get module (ID=" + id + "): " + e.getMessage());
        } finally {
            session.close();
        }
    }

    @Override
    public synchronized Module updateModule(Module module) throws EmfException {
        Session session = sessionFactory.getSession();
        Connection connection = null;
        Statement statement = null;
        try {
            if (!modulesDAO.canUpdate(module, session))
                throw new EmfException("The module is already in use");

            // manually delete the missing module datasets, parameters, internal datasets, and internal parameters from the database
            
            Module currentModule = modulesDAO.currentModule(module.getId(), session);
            Map<String, ModuleDataset> newModuleDatasets = module.getModuleDatasets(); 
            for(ModuleDataset currentModuleDataset : currentModule.getModuleDatasets().values()) {
                if (newModuleDatasets.containsKey(currentModuleDataset.getPlaceholderName())) {
                    ModuleDataset newModuleDataset = newModuleDatasets.get(currentModuleDataset.getPlaceholderName());
                    if (newModuleDataset.getId() == currentModuleDataset.getId())
                        continue;
                }
                // manually delete the currentModuleDataset
                try {
                    if (connection == null) {
                        connection = dbServerFactory.getDbServer().getConnection();
                    }
                    statement = connection.createStatement();
                    statement.execute("DELETE FROM modules.modules_datasets WHERE id=" + currentModuleDataset.getId());
                } catch (Exception e) {
                    throw new EmfException("Failed to delete module dataset: " + e.getMessage());
                } finally {
                    if (statement != null) {
                        try {
                            statement.close();
                        } catch (SQLException e) {
                            // ignore
                        }
                        statement = null;
                    }
                }
            }
            Map<String, ModuleParameter> newModuleParameters = module.getModuleParameters();
            for(ModuleParameter currentModuleParameter : currentModule.getModuleParameters().values()) {
                if (newModuleParameters.containsKey(currentModuleParameter.getParameterName())) {
                    ModuleParameter newModuleParameter = newModuleParameters.get(currentModuleParameter.getParameterName());
                    if (newModuleParameter.getId() == currentModuleParameter.getId())
                        continue;
                }
                // manually delete the currentModuleParameter
                try {
                    if (connection == null) {
                        connection = dbServerFactory.getDbServer().getConnection();
                    }
                    statement = connection.createStatement();
                    statement.execute("DELETE FROM modules.modules_parameters WHERE id=" + currentModuleParameter.getId());
                } catch (Exception e) {
                    throw new EmfException("Failed to delete module parameter: " + e.getMessage());
                } finally {
                    if (statement != null) {
                        try {
                            statement.close();
                        } catch (SQLException e) {
                            // ignore
                        }
                        statement = null;
                    }
                }
            }
            Map<String, ModuleInternalDataset> newInternalDatasets = module.getModuleInternalDatasets();
            for(ModuleInternalDataset currentModuleInternalDataset : currentModule.getModuleInternalDatasets().values()) {
                if (newInternalDatasets.containsKey(currentModuleInternalDataset.getPlaceholderPath())) {
                    ModuleInternalDataset newInternalDataset = newInternalDatasets.get(currentModuleInternalDataset.getPlaceholderPath());
                    if (newInternalDataset.getId() == currentModuleInternalDataset.getId())
                        continue;
                }
                // manually delete the currentModuleInternalDataset
                try {
                    if (connection == null) {
                        connection = dbServerFactory.getDbServer().getConnection();
                    }
                    statement = connection.createStatement();
                    statement.execute("DELETE FROM modules.modules_internal_datasets WHERE id=" + currentModuleInternalDataset.getId());
                } catch (Exception e) {
                    throw new EmfException("Failed to delete module internal dataset: " + e.getMessage());
                } finally {
                    if (statement != null) {
                        try {
                            statement.close();
                        } catch (SQLException e) {
                            // ignore
                        }
                        statement = null;
                    }
                }
            }
            Map<String, ModuleInternalParameter> newInternalParameters = module.getModuleInternalParameters();
            for(ModuleInternalParameter currentModuleInternalParameter : currentModule.getModuleInternalParameters().values()) {
                if (newInternalParameters.containsKey(currentModuleInternalParameter.getParameterPath())) {
                    ModuleInternalParameter newInternalParameter = newInternalParameters.get(currentModuleInternalParameter.getParameterPath());
                    if (newInternalParameter.getId() == currentModuleInternalParameter.getId())
                        continue;
                }
                // manually delete the currentModuleInternalParameter
                try {
                    if (connection == null) {
                        connection = dbServerFactory.getDbServer().getConnection();
                    }
                    statement = connection.createStatement();
                    statement.execute("DELETE FROM modules.modules_internal_parameters WHERE id=" + currentModuleInternalParameter.getId());
                } catch (Exception e) {
                    throw new EmfException("Failed to delete module internal parameter: " + e.getMessage());
                } finally {
                    if (statement != null) {
                        try {
                            statement.close();
                        } catch (SQLException e) {
                            // ignore
                        }
                        statement = null;
                    }
                }
            }
            
            Module released = modulesDAO.updateModule(module, session);
            return released;
        } catch (Exception e) {
            LOG.error("Could not update module: " + module.getName(), e);
            throw new EmfException("The module is already in use");
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    // NOTE Auto-generated catch block
                    e.printStackTrace();
                }
                connection = null;
            }
            session.close();
        }
    }

    @Override
    public synchronized int[] deleteModules(User owner, int[] moduleIds) throws EmfException {
        List<Integer> deletedModuleIdsList = new ArrayList<Integer>();

        Session session = this.sessionFactory.getSession();
        for (int moduleId : moduleIds) {
            try {
                modulesDAO.removeModule(moduleId, session);
                deletedModuleIdsList.add(moduleId);
            } finally {
                // ignore
            }
        }
        session.close();
        
        int[] deletedModuleIds = new int[deletedModuleIdsList.size()];
        for (int i = 0; i < deletedModuleIdsList.size(); i++) {
            deletedModuleIds[i] = deletedModuleIdsList.get(i);
        }
        return deletedModuleIds;
    }

    @Override
    public synchronized Module obtainLockedModule(User user, int moduleId) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            Module locked = modulesDAO.obtainLockedModule(user, moduleId, session);
            return locked;
        } catch (Exception e) {
            LOG.error("Could not lock module. ", e);
            throw new EmfException("Could not lock module: " + e.getMessage());
        } finally {
            session.close();
        }
    }

    @Override
    public synchronized Module releaseLockedModule(User user, int moduleId) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            Module locked = modulesDAO.releaseLockedModule(user, moduleId, session);
            return locked;
        } catch (Exception e) {
            LOG.error("Could not unlock module. ", e);
            throw new EmfException("Could not unlock module: " + e.getMessage());
        } finally {
            session.close();
        }
    }

    @Override
    public synchronized int[] lockModules(User user, int[] moduleIds) throws EmfException {
        Session session = sessionFactory.getSession();
        List<Integer> lockedModulesList = new ArrayList<Integer>();
        try {
            for(int moduleId : moduleIds) {
                try {
                    Module lockedModule = modulesDAO.obtainLockedModule(user, moduleId, session);
                    if (lockedModule != null)
                        lockedModulesList.add(lockedModule.getId());
                } finally {
                    // ignore
                }
            }
        } catch (Exception e) {
            LOG.error("Could not lock module. ", e);
            throw new EmfException("Could not lock module: " + e.getMessage());
        } finally {
            session.close();
        }
        
        int[] lockedModules = new int[lockedModulesList.size()];
        for (int i = 0; i < lockedModulesList.size(); i++) {
            lockedModules[i] = lockedModulesList.get(i);
        }
        
        return lockedModules;
    }

    @Override
    public synchronized int[] unlockModules(User user, int[] moduleIds) throws EmfException {
        Session session = sessionFactory.getSession();
        List<Integer> unlockedModulesList = new ArrayList<Integer>();
        try {
            for(int moduleId : moduleIds) {
                try {
                    Module unlockedModule = modulesDAO.releaseLockedModule(user, moduleId, session);
                    if (unlockedModule != null)
                        unlockedModulesList.add(unlockedModule.getId());
                } finally {
                    // ignore
                }
            }
        } catch (Exception e) {
            LOG.error("Could not lock module. ", e);
            throw new EmfException("Could not lock module: " + e.getMessage());
        } finally {
            session.close();
        }
        
        int[] unlockedModules = new int[unlockedModulesList.size()];
        for (int i = 0; i < unlockedModulesList.size(); i++) {
            unlockedModules[i] = unlockedModulesList.get(i);
        }
        
        return unlockedModules;
    }

    @Override
    public synchronized Module addModule(Module module) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            if (modulesDAO.moduleNameUsed(module.getName(), session))
                throw new EmfException("The \"" + module.getName() + "\" name is already in use");

            return modulesDAO.add(module, session);
        } catch (Exception e) {
            LOG.error("Could not add new Module", e);
            throw new EmfException("Could not add module " + module.getName() + ": " + e.toString());
        } finally {
            session.close();
        }
    }

    @Override
    public synchronized void runModules(int[] moduleIds, User user) throws EmfException {
        try {
            ModuleRunnerThread runner = new ModuleRunnerThread(moduleIds, user, dbServerFactory, sessionFactory);
            threadPool.execute(new GCEnforcerTask("Module Runner", runner));
        } catch (Exception e) {
            LOG.error("Error running modules", e);
            throw new EmfException("Error running modules:" + e.getMessage());
        }
    }

    @Override
    public synchronized EmfDataset getEmfDatasetForModuleDataset(int moduleDatasetId) {
        Session session = sessionFactory.getSession();
        EmfDataset emfDataset = null;
        try {
            ModuleDataset moduleDataset = modulesDAO.getModuleDataset(moduleDatasetId, session);
            if (moduleDataset == null)
                throw new EmfException("Failed to get module dataset (ID = " + moduleDatasetId + ")");
            if (moduleDataset.getDatasetId() != null) {
                emfDataset = datasetDAO.getDataset(session, moduleDataset.getDatasetId());
            } else if (moduleDataset.isSimpleDatasetName()) {
                emfDataset = datasetDAO.getDataset(session, moduleDataset.getDatasetNamePattern());
            } else {
                Module module = moduleDataset.getModule();
                HistoryDataset historyDataset = null;
                History lastHistory = module.lastHistory();
                if (lastHistory != null) {
                    String result = lastHistory.getResult();
                    if (result != null && result.equals(History.SUCCESS)) {
                        historyDataset = lastHistory.getHistoryDatasets().get(moduleDataset.getPlaceholderName());
                    }
                }
                if ((historyDataset != null) && (historyDataset.getDatasetId() != null)) {
                    emfDataset = datasetDAO.getDataset(session, historyDataset.getDatasetId());
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            session.close();
        }
        
        return emfDataset;
    }
    
    @Override
    public synchronized EmfDataset getEmfDatasetForModuleDataset(int moduleDatasetId, Integer newDatasetId, String newDatasetNamePattern) {
        Session session = sessionFactory.getSession();
        EmfDataset emfDataset = null;
        try {
            ModuleDataset moduleDataset = modulesDAO.getModuleDataset(moduleDatasetId, session);
            if (moduleDataset == null)
                throw new EmfException("Failed to get module dataset (ID = " + moduleDatasetId + ")");
            if (newDatasetId != null) {
                emfDataset = datasetDAO.getDataset(session, newDatasetId);
            } else if (ModuleDataset.isSimpleDatasetName(newDatasetNamePattern)) {
                emfDataset = datasetDAO.getDataset(session, newDatasetNamePattern);
            } else {
                Module module = moduleDataset.getModule();
                List<History> history = module.getModuleHistory();
                HistoryDataset historyDataset = null;
                if (history.size() > 0) {
                    History lastHistory = history.get(history.size() - 1);
                    String result = lastHistory.getResult();
                    if (result != null && result.equals(History.SUCCESS)) {
                        if (lastHistory.getHistoryDatasets().containsKey(moduleDataset.getPlaceholderName())) {
                            historyDataset = lastHistory.getHistoryDatasets().get(moduleDataset.getPlaceholderName());
                        }
                    }
                }
                if ((historyDataset != null) && (historyDataset.getDatasetId() != null)) {
                    emfDataset = datasetDAO.getDataset(session, historyDataset.getDatasetId());
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            session.close();
        }
        
        return emfDataset;
    }
    
    @Override
    public synchronized Module[] getModulesForModuleTypeVersion(int moduleTypeVersionId) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            List<Module> list = modulesDAO.getModulesForModuleTypeVersion(session, moduleTypeVersionId);
            Module[] modules = list.toArray(new Module[0]); 
            return modules;
        } catch (Exception e) {
            LOG.error("Could not get all modules for module type version (ID = " + moduleTypeVersionId + ")", e);
            throw new EmfException("Could not get all modules for module type version (ID = " + moduleTypeVersionId + "): " + e.getMessage());
        } finally {
            session.close();
        }
    }
    
    @Override
    public synchronized LiteModule[] getLiteModules() throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            @SuppressWarnings("unchecked")
            List<LiteModule> liteModules = modulesDAO.getLiteModules(session);
            return liteModules.toArray(new LiteModule[]{});
        } catch (Exception e) {
            LOG.error("Could not get all lite modules", e);
            throw new EmfException("Could not get all lite module: " + e.getMessage());
        } finally {
            session.close();
        }
    }

    // TODO enhance this method by returning the type of relation also (created, using, replacing, used/created/replaced in old runs, etc.)
    @Override
    public synchronized LiteModule[] getRelatedLiteModules(int datasetId) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            EmfDataset dataset = datasetDAO.getDataset(session, datasetId);
            KeyVal[] keyVals = dataset.getKeyVals();
            Keyword moduleIdKeyword = keywords.get("MODULE_ID");
            int creatorModuleId = -1; 
            for (KeyVal keyVal : keyVals) {
                if (keyVal.getKeyword().equals(moduleIdKeyword)) {
                    creatorModuleId = Integer.parseInt(keyVal.getValue());
                }
            }
            @SuppressWarnings("unchecked")
            List<Module> modules = modulesDAO.getModules(session);
            List<LiteModule> liteModules = new ArrayList<LiteModule>();
            
            nextModule:
            for (Module module : modules) {
                if (module.getId() == creatorModuleId) {
                    liteModules.add(modulesDAO.getLiteModule(module.getId(), session));
                    continue;
                }
                for (ModuleDataset moduleDataset : module.getModuleDatasets().values()) {
                    if (moduleDataset.getId() == datasetId) {
                        liteModules.add(modulesDAO.getLiteModule(module.getId(), session));
                        continue nextModule;
                    }
                    if (moduleDataset.getDatasetNamePattern() != null && moduleDataset.isSimpleDatasetName() && moduleDataset.getDatasetNamePattern().equals(dataset.getName())) {
                        liteModules.add(modulesDAO.getLiteModule(module.getId(), session));
                        continue nextModule;
                    }
                }
                for (ModuleInternalDataset moduleInternalDataset : module.getModuleInternalDatasets().values()) {
                    if (moduleInternalDataset.getKeep() && moduleInternalDataset.getDatasetNamePattern() != null && moduleInternalDataset.isSimpleDatasetName() && moduleInternalDataset.getDatasetNamePattern().equals(dataset.getName())) {
                        liteModules.add(modulesDAO.getLiteModule(module.getId(), session));
                        continue nextModule;
                    }
                }
                for(History history : module.getModuleHistory()) {
                    for (HistoryDataset historyDataset : history.getHistoryDatasets().values()) {
                        if (historyDataset.getDatasetId() != null && historyDataset.getDatasetId() == datasetId) {
                            liteModules.add(modulesDAO.getLiteModule(module.getId(), session));
                            continue nextModule;
                        }
                    }
                    for (HistoryInternalDataset historyInternalDataset : history.getHistoryInternalDatasets().values()) {
                        if (historyInternalDataset.getDatasetId() != null && historyInternalDataset.getDatasetId() == datasetId) {
                            liteModules.add(modulesDAO.getLiteModule(module.getId(), session));
                            continue nextModule;
                        }
                    }
                }
            }
            return liteModules.toArray(new LiteModule[0]);
        } catch (Exception e) {
            LOG.error("Could not get all related modules", e);
            e.printStackTrace();
            throw new EmfException("Could not get all related modules: " + e.getMessage());
        } finally {
            session.close();
        }
    }

    @Override
    public ModuleType removeModuleTypeVersion(int moduleTypeVersionId) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            ModuleTypeVersion moduleTypeVersion = null;
            try {
                moduleTypeVersion = moduleTypesDAO.getModuleTypeVersion(moduleTypeVersionId, session);
            } catch (Exception e) {
                LOG.error("Could not get module type version (ID=" + moduleTypeVersionId +")", e);
                throw new EmfException("Could not get module type version (ID=" + moduleTypeVersionId +"): " + e.getMessage());
            }
            if (moduleTypeVersion == null) {
                LOG.error("Could not get module type version (ID=" + moduleTypeVersionId +")");
                throw new EmfException("Could not get module type version (ID=" + moduleTypeVersionId +")");
            }
            
            try {
                ModuleType moduleType = moduleTypeVersion.getModuleType();
                moduleType.removeModuleTypeVersion(moduleTypeVersion);
                moduleTypesDAO.removeModuleTypeVersion(moduleTypeVersion, session);
                return moduleTypesDAO.getModuleType(moduleType.getId(), session); 
            } catch (Exception e) {
                String fullName = moduleTypeVersion.fullNameSS("\"%s\" version \"%s\"");
                LOG.error("Could not remove module type " + fullName, e);
                throw new EmfException("Could not remove module type " + fullName + ": " + e.getMessage());
            }
        } finally {
            session.close();
        }
    }
}
