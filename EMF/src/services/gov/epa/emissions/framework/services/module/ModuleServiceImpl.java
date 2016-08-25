package gov.epa.emissions.framework.services.module;

import gov.epa.emissions.commons.data.Module;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;

import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.data.PivotConfiguration;
import gov.epa.emissions.commons.data.ProjectionShapeFile;
import gov.epa.emissions.commons.data.QAProgram;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.TableCreator;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.db.version.Versions;
import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.DbServerFactory;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.GCEnforcerTask;
import gov.epa.emissions.framework.services.basic.EmfProperty;
import gov.epa.emissions.framework.services.basic.RemoteCommand;
import gov.epa.emissions.framework.services.basic.Status;
import gov.epa.emissions.framework.services.basic.StatusDAO;
import gov.epa.emissions.framework.services.data.*;
import gov.epa.emissions.framework.services.persistence.EmfPropertiesDAO;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.io.*;
import java.sql.SQLException;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;

import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;

public class ModuleServiceImpl implements ModuleService {

    private static Log LOG = LogFactory.getLog(ModuleServiceImpl.class);

    private HibernateSessionFactory sessionFactory;

    private DbServerFactory dbServerFactory;

    private ModuleDAO dao;
    
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
        dao = new ModuleDAO();
        statusDAO = new StatusDAO(sessionFactory);
    }

    private synchronized PooledExecutor createThreadPool() {
        PooledExecutor threadPool = new PooledExecutor(20);
        threadPool.setMinimumPoolSize(1);
        threadPool.setKeepAliveTime(1000 * 60 * 3);// terminate after 3 (unused) minutes

        return threadPool;
    }

    public synchronized void runModules(Module[] modules, User user) throws EmfException {
        DbServer dbServer = null;
        
        try {
            dbServer = dbServerFactory.getDbServer();
        } catch (Exception e) {
            LOG.error("Error pre-processing modules for running.", e);
            throw new EmfException(e.getMessage());
        }

        ModuleRunner runner = new ModuleRunner(modules, user, dbServerFactory, sessionFactory);

        try {
            threadPool.execute(new GCEnforcerTask("Module Runner", runner));
        } catch (Exception e) {
            LOG.error("Error running modules", e);
            throw new EmfException("Error running modules:" + e.getMessage());
        } finally {
            try {
                dbServer.disconnect();
            } catch (Exception e) {
                LOG.error("Error closing DB server and logging DB connections.", e);
            }
        }
    }
}
