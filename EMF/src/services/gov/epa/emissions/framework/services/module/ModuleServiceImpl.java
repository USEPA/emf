package gov.epa.emissions.framework.services.module;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;

import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.GCEnforcerTask;
import gov.epa.emissions.framework.services.basic.StatusDAO;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

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
    }

    private synchronized PooledExecutor createThreadPool() {
        PooledExecutor threadPool = new PooledExecutor(20);
        threadPool.setMinimumPoolSize(1);
        threadPool.setKeepAliveTime(1000 * 60 * 3);// terminate after 3 (unused) minutes

        return threadPool;
    }

    public synchronized ModuleType[] getModuleTypes() throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            List<ModuleType> list = moduleTypesDAO.getModuleTypes(session);

            ModuleType[] moduleTypes = list.toArray(new ModuleType[0]); 
            return moduleTypes;
        } catch (RuntimeException e) {
            LOG.error("Could not get all ModuleTypes", e);
            throw new EmfException("Could not get all ModuleTypes: " + e.getMessage());
        } finally {
            session.close(); 
        }
    }

    public synchronized ModuleType getModuleType(int id) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            ModuleType moduleType = moduleTypesDAO.get(id, session);
            return moduleType;
        } catch (RuntimeException e) {
            LOG.error("Could not get ModuleType (ID=" + id + ")", e);
            throw new EmfException("Could not get ModuleType (ID=" + id + "): " + e.getMessage());
        } finally {
            session.close(); 
        }
    }

    public synchronized ModuleType updateModuleType(ModuleType moduleType) throws EmfException {
        Session session = sessionFactory.getSession();
        Connection connection = null;
        try {
            if (!moduleTypesDAO.canUpdate(moduleType, session))
                throw new EmfException("The module type is already in use");

            // manually delete the missing module type versions, datasets, and parameters from the database 
            Map<Integer, ModuleTypeVersion> moduleTypeVersions = moduleType.getModuleTypeVersions();
            ModuleType currentMT = moduleTypesDAO.current(moduleType, session);
            for (ModuleTypeVersion currentMTV : currentMT.getModuleTypeVersions().values()) {
                if (!moduleTypeVersions.containsKey(currentMTV.getVersion())) {
                    // TODO manually delete the currentMTV from database
                    Statement statement = null;
                    try {
                        if (connection == null) {
                            connection = dbServerFactory.getDbServer().getConnection();
                        }
                        statement = connection.createStatement();
                        statement.execute("DELETE FROM modules.module_types_versions WHERE id=" + currentMTV.getId());
                    } catch (Exception e) {
                        throw new EmfException("Failed to delete module type version: " + e.getMessage());
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
                    continue;
                }
                ModuleTypeVersion moduleTypeVersion = moduleTypeVersions.get(currentMTV.getVersion());
                Map<String, ModuleTypeVersionDataset> moduleTypeVersionDatasets = moduleTypeVersion.getModuleTypeVersionDatasets(); 
                Map<String, ModuleTypeVersionParameter> moduleTypeVersionParameters = moduleTypeVersion.getModuleTypeVersionParameters(); 
                for (ModuleTypeVersionDataset currentMTVD : currentMTV.getModuleTypeVersionDatasets().values()) {
                    if (moduleTypeVersionDatasets.containsKey(currentMTVD.getPlaceholderName())) {
                        ModuleTypeVersionDataset moduleTypeVersionDataset = moduleTypeVersionDatasets.get(currentMTVD.getPlaceholderName());
                        if (moduleTypeVersionDataset.getId() == currentMTVD.getId())
                            continue;
                    }
                    // manually delete the currentMTVD
                    Statement statement = null;
                    try {
                        if (connection == null) {
                            connection = dbServerFactory.getDbServer().getConnection();
                        }
                        statement = connection.createStatement();
                        statement.execute("DELETE FROM modules.module_types_versions_datasets WHERE id=" + currentMTVD.getId());
                    } catch (Exception e) {
                        throw new EmfException("Failed to delete module type version dataset: " + e.getMessage());
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
                for (ModuleTypeVersionParameter currentMTVP : currentMTV.getModuleTypeVersionParameters().values()) {
                    if (moduleTypeVersionParameters.containsKey(currentMTVP.getParameterName())) {
                        ModuleTypeVersionParameter moduleTypeVersionParameter = moduleTypeVersionParameters.get(currentMTVP.getParameterName());
                        if (moduleTypeVersionParameter.getId() == currentMTVP.getId())
                            continue;
                    }
                    // manually delete the currentMTVP 
                    Statement statement = null;
                    try {
                        if (connection == null) {
                            connection = dbServerFactory.getDbServer().getConnection();
                        }
                        statement = connection.createStatement();
                        statement.execute("DELETE FROM modules.module_types_versions_parameters WHERE id=" + currentMTVP.getId());
                    } catch (Exception e) {
                        throw new EmfException("Failed to delete module type version parameter: " + e.getMessage());
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
            }

            ModuleType released = moduleTypesDAO.update(moduleType, session);
            return released;
        } catch (RuntimeException e) {
            LOG.error("Failed to update module type: " + moduleType.getName(), e);
            throw new EmfException("Failed to update module type " + moduleType.getName() + ": " + e.getMessage());
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

    public synchronized ModuleType obtainLockedModuleType(User user, ModuleType type) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            ModuleType locked = moduleTypesDAO.obtainLockedModuleType(user, type, session);
            return locked;
        } catch (RuntimeException e) {
            LOG.error("Could not obtain lock for ModuleType: " + type.getName(), e);
            throw new EmfException("Could not obtain lock for ModuleType: " + type.getName());
        } finally {
            session.close();
        }
    }

    public synchronized ModuleType releaseLockedModuleType(User user, ModuleType type) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            ModuleType locked = moduleTypesDAO.releaseLockedModuleType(user, type, session);
            return locked;
        } catch (RuntimeException e) {
            LOG.error("Could not release lock on ModuleType: " + type.getName(), e);
            throw new EmfException("Could not release lock on ModuleType: " + type.getName());
        } finally {
            session.close();
        }
    }

    public synchronized ModuleType addModuleType(ModuleType type) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            if (moduleTypesDAO.nameUsed(type.getName(), ModuleType.class, session))
                throw new EmfException("The \"" + type.getName() + "\" name is already in use");

            moduleTypesDAO.add(type, session);
            return type;
        } catch (RuntimeException e) {
            LOG.error("Could not add new ModuleType", e);
            throw new EmfException("Could not add module type " + type.getName() + ": " + e.toString());
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new EmfException(e.getMessage());
        } finally {
            session.close();
        }
    }

    public synchronized Module[] getModules() throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            List<Module> list = modulesDAO.getModules(session);

            Module[] modules = list.toArray(new Module[0]); 
            return modules;
        } catch (RuntimeException e) {
            LOG.error("Could not get all modules", e);
            throw new EmfException("Could not get all modules: " + e.getMessage());
        } finally {
            session.close();
        }
    }

    public synchronized Module getModule(int id) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            Module module = modulesDAO.get(id, session);
            return module;
        } catch (RuntimeException e) {
            LOG.error("Could not get module (ID=" + id + ")", e);
            throw new EmfException("Could not get module (ID=" + id + "): " + e.getMessage());
        } finally {
            session.close();
        }
    }

    public synchronized Module updateModule(Module module) throws EmfException {
        Session session = sessionFactory.getSession();
        Connection connection = null;
        Statement statement = null;
        try {
            if (!modulesDAO.canUpdate(module, session))
                throw new EmfException("The module is already in use");

            Map<String, ModuleDataset>   newModuleDatasets   = module.getModuleDatasets(); 
            Map<String, ModuleParameter> newModuleParameters = module.getModuleParameters();
            
            // manually delete the missing module datasets and parameters from the database
            Module currentModule = modulesDAO.currentModule(module, session);
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
            
            Module released = modulesDAO.update(module, session);
            return released;
        } catch (RuntimeException e) {
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

    public synchronized void deleteModules(User owner, Module[] modules) throws EmfException {
        Session session = this.sessionFactory.getSession();
        try {
            for (int i=0; i<modules.length; i++) {
                modulesDAO.removeModule(modules[i], session);
            }
        } catch (Exception e) {
            LOG.error("Error deleting modules. " , e);
            throw new EmfException("Error deleting modules. \n" + e.getMessage());
        } finally {
            session.close(); 
        }
    }

    public synchronized Module obtainLockedModule(User user, Module module) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            Module locked = modulesDAO.obtainLockedModule(user, module, session);
            return locked;
        } catch (RuntimeException e) {
            LOG.error("Could not obtain lock for Module: " + module.getName(), e);
            throw new EmfException("Could not obtain lock for Module: " + module.getName());
        } finally {
            session.close();
        }
    }

    public synchronized Module releaseLockedModule(User user, Module module) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            Module locked = modulesDAO.releaseLockedModule(user, module, session);
            return locked;
        } catch (RuntimeException e) {
            LOG.error("Could not release lock on Module: " + module.getName(), e);
            throw new EmfException("Could not release lock on Module: " + module.getName());
        } finally {
            session.close();
        }
    }

    public synchronized Module addModule(Module module) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            if (modulesDAO.nameUsed(module.getName(), Module.class, session))
                throw new EmfException("The \"" + module.getName() + "\" name is already in use");

            return modulesDAO.add(module, session);
        } catch (RuntimeException e) {
            LOG.error("Could not add new Module", e);
            throw new EmfException("Could not add module " + module.getName() + ": " + e.toString());
        } catch (Exception e) {
            LOG.error(e.getMessage());
            throw new EmfException(e.getMessage());
        } finally {
            session.close();
        }
    }

    public synchronized void runModules(Module[] modules, User user) throws EmfException {
        try {
            ModuleRunnerThread runner = new ModuleRunnerThread(modules, user, dbServerFactory, sessionFactory);
            threadPool.execute(new GCEnforcerTask("Module Runner", runner));
        } catch (Exception e) {
            LOG.error("Error running modules", e);
            throw new EmfException("Error running modules:" + e.getMessage());
        }
    }
}
