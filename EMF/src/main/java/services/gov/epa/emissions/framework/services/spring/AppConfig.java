package gov.epa.emissions.framework.services.spring;

import gov.epa.emissions.framework.services.basic.FileDownloadDAO;
import gov.epa.emissions.framework.services.basic.RemoveDownloadFilesTask;

import java.util.Properties;

import javax.sql.DataSource;

import org.apache.tomcat.dbcp.dbcp.BasicDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.orm.hibernate3.annotation.AnnotationSessionFactoryBean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@ComponentScan(basePackages = "gov.epa.emissions.framework")
//@EnableTransactionManagement(proxyTargetClass=true, mode=AdviceMode.PROXY)gov.epa.emissions.framework.services.basic
public class AppConfig {

    @Bean
    public ThreadPoolTaskExecutor poolTaskExecutor() {
        ThreadPoolTaskExecutor pool = new ThreadPoolTaskExecutor();
        pool.setCorePoolSize(5);
        pool.setMaxPoolSize(10);
        pool.setWaitForTasksToCompleteOnShutdown(true);
        return pool;
    }

//    @Bean
//    public DataSource dataSource() {
//        try {
//            Context ctx = new InitialContext();
//            return (DataSource) ctx.lookup("java:/comp/env/jdbc/EMFDB");
//        } catch (NamingException e) {
//            // NOTE Auto-generated catch block
//            e.printStackTrace();
//        }
//        return null;
//    }

    @Bean
    public AnnotationSessionFactoryBean sessionFactory() {
       AnnotationSessionFactoryBean sessionFactory = new AnnotationSessionFactoryBean();
       sessionFactory.setDataSource(dataSource());
       sessionFactory.setAnnotatedPackages(/*PackagesToScan(*/new String[] { "gov.epa.emissions.framework" });
       sessionFactory.setAnnotatedClasses(new Class[] { FileDownloadDAO.class, RemoveDownloadFilesTask.class });
       sessionFactory.setHibernateProperties(hibernateProperties());

       return sessionFactory;
    }

    @Bean
    public DataSource dataSource() {
       BasicDataSource dataSource = new BasicDataSource();
       dataSource.setDriverClassName("org.postgresql.Driver");
       dataSource.setUrl("jdbc:postgresql://localhost:5432/EMF?autoReconnect=true");
       dataSource.setUsername("emf");
       dataSource.setPassword("emf");
       dataSource.setMaxActive(92);
       dataSource.setMaxIdle(30);
       dataSource.setMaxWait(30000);
       dataSource.setRemoveAbandoned(true);
       dataSource.setRemoveAbandonedTimeout(40);
       dataSource.setLogAbandoned(true);
       dataSource.setValidationQuery("select 1");
       
//       <Resource name="jdbc/EMFDB" auth="Container"
//               type="javax.sql.DataSource"
//               driverClassName="org.postgresql.Driver"
//               url="jdbc:postgresql://localhost:5432/EMF?autoReconnect=true"
//               username="emf"
//               password="emf"
//               maxActive="92"
//               maxIdle="30"
//               maxWait="30000"
//               removeAbandoned="true"
//               removeAbandonedTimeout="40"
//               logAbandoned="true"
//               validationQuery="select 1"
//               />

       return dataSource;
    }

//    @Bean
//    public HibernateTransactionManager transactionManager() {
//       HibernateTransactionManager txManager = new HibernateTransactionManager();
//       txManager.setSessionFactory((SessionFactory) sessionFactory().getObject());
//
//       return txManager;
//    }

    @Bean
    public PersistenceExceptionTranslationPostProcessor exceptionTranslation() {
       return new PersistenceExceptionTranslationPostProcessor();
    }

    Properties hibernateProperties() {
       return new Properties() {
          {
             setProperty("hibernate.hbm2ddl.auto", "create-drop");//env.getProperty("hibernate.hbm2ddl.auto"));
             setProperty("hibernate.dialect","org.hibernate.dialect.PostgreSQLDialect");// env.getProperty("hibernate.dialect"));
          }
       };
    }    
    
//    @Bean
//    public StatusDAO statusDAO() {
//        StatusDAO caseDAO = new StatusDAO();
//        caseDAO.setSessionFactory((SessionFactory) sessionFactory().getObject());
//        return caseDAO;
//    }
//    
//    @Bean
//    public EmfPropertiesDAO emfPropertiesDAO() {
//        EmfPropertiesDAO caseDAO = new EmfPropertiesDAO();
//        caseDAO.setSessionFactory((SessionFactory) sessionFactory().getObject());
//        return caseDAO;
//    }
//    
//    @Bean
//    public DebugLevels debugLevels() {
//        DebugLevels caseDAO = new DebugLevels();
////        caseDAO.setEmfPropertiesDAO(emfPropertiesDAO());
//        return caseDAO;
//    }
}