package gov.epa.emissions.framework.services;

import gov.epa.emissions.commons.db.DatabaseSetup;
import gov.epa.emissions.commons.db.DbServer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DbServerFactory {

    private static Log log = LogFactory.getLog(DbServerFactory.class);

    private static DbServerFactory instance;

    private boolean tomcatEnvironment;

    private boolean junitEnvironment;
    
    private DatabaseSetup databaseSetup;

    //this is used when running in the tomcat enviroment
    private DbServerFactory() {
        this.tomcatEnvironment = true;
    }

    //this is used when running in the JUnit enviroment
    public DbServerFactory(DatabaseSetup databaseSetup) {
        this.junitEnvironment = true;
        this.databaseSetup = databaseSetup;
    }

    // TODO: stick a single instance in the Axis application-level cache. Only
    // one instance is needed for the entire application i.e. one per db
    public static DbServerFactory get() {
        if (instance == null)
            instance = new DbServerFactory();

        return instance;
    }

    public DbServer getDbServer() {
        DbServer dbServer = null;
        try {
            if (tomcatEnvironment) 
                dbServer = new EmfDbServer();
            if (junitEnvironment)
                dbServer = databaseSetup.getNewPostgresDbServerInstance();
        } catch (Exception e) {
            log.error("Initial DbServer creation failed", e);
            throw new ExceptionInInitializerError(e);
        }
        return dbServer;
    }
}
