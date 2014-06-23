package gov.epa.emissions.framework.services.persistence;

import gov.epa.emissions.framework.services.InfrastructureException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

public class DataSourceFactory {

    public DataSource get() throws InfrastructureException {
        DataSource ds = null;
        try {
            Context ctx = new InitialContext();
            ds = (DataSource) ctx.lookup("java:/comp/env/jdbc/EMFDB");
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new InfrastructureException("Unable to lookup Datasource using JNDI");
        }

        return ds;
    }
    
    public DataSource getSectorSandboxDataSource() throws InfrastructureException {
        DataSource ds = null;
        try {
            Context ctx = new InitialContext();
            ds = (DataSource) ctx.lookup("java:/comp/env/jdbc/SECTORSANDBOX");
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new InfrastructureException("Unable to lookup Datasource using JNDI");
        }

        return ds;
    }    

    public DataSource getEISDataSource() throws InfrastructureException {
        DataSource ds = null;
        try {
            Context ctx = new InitialContext();
            ds = (DataSource) ctx.lookup("java:/comp/env/jdbc/EMFDB2");
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new InfrastructureException("Unable to lookup Datasource using JNDI");
        }

        return ds;
    }

//    private static Log log = LogFactory.getLog(DataSource.class);
//
//    private static DataSourceFactory instance;
//
//    private boolean tomcatEnvironment;
//
//    private boolean junitEnvironment;
//    
//    private EmfDatabaseSetup databaseSetup;
//
//    //this is used when running in the tomcat enviroment
//    private DataSourceFactory() {
//        this.tomcatEnvironment = true;
//    }
//
//    //this is used when running in the JUnit enviroment
//    public DataSourceFactory(EmfDatabaseSetup databaseSetup) {
//        this.junitEnvironment = true;
//        this.databaseSetup = databaseSetup;
//    }
//
//    // TODO: stick a single instance in the Axis application-level cache. Only
//    // one instance is needed for the entire application i.e. one per db
//    public static DataSourceFactory get() {
//        if (instance == null)
//            instance = new DataSourceFactory();
//
//        return instance;
//    }
//
//    public DataSource getDataSource() throws ExceptionInInitializerError {
//        DataSource ds = null;
//        try {
//            if (tomcatEnvironment) {
//                try {
//                    Context ctx = new InitialContext();
//                    ds = (DataSource) ctx.lookup("java:/comp/env/jdbc/EMFDB");
//                } catch (Exception ex) {
//                    ex.printStackTrace();
//                    throw new InfrastructureException("Unable to lookup Datasource using JNDI");
//                }
//            }
//            if (junitEnvironment)
//                ds = databaseSetup.emfDatasource();
//        } catch (Exception e) {
//            log.error("Initial DataSource creation failed", e);
//            throw new ExceptionInInitializerError(e);
//        }
//        return ds;
//    }
}
