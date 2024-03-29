package gov.epa.emissions.framework.services.spring;

import gov.epa.emissions.framework.services.basic.FileDownloadDAO;
import gov.epa.emissions.framework.services.basic.RemoveDownloadFilesTask;
import gov.epa.emissions.framework.services.basic.RemoveUploadFilesTask;
import gov.epa.emissions.framework.services.cost.ControlStrategyDAO;

import org.apache.tomcat.dbcp.dbcp2.BasicDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@ComponentScan(basePackages = {
        "gov.epa.emissions.framework.services.cost",
        "gov.epa.emissions.framework.services.basic",
        "gov.epa.emissions.framework.services.cost.analysis.common"
    })
@EnableScheduling
@EnableTransactionManagement
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
    public LocalSessionFactoryBean sessionFactory() {
        LocalSessionFactoryBean sessionFactory = new LocalSessionFactoryBean();
       sessionFactory.setDataSource(dataSource());
//       sessionFactory.setAnnotatedPackages(new String[]{"gov.epa.emissions.framework", "gov.epa.emissions.framework.services.cost"});
//       sessionFactory.setPackagesToScan(new String[]{"gov.epa.emissions.framework.services.cost", "gov.epa.emissions.framework.services.cost.analysis.common"});
//       sessionFactory.setAnnotatedClasses(new Class[] { ControlStrategyDAO.class, FileDownloadDAO.class, RemoveDownloadFilesTask.class, RemoveUploadFilesTask.class });
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
       dataSource.setMaxTotal(92);
       dataSource.setMaxIdle(30);
       dataSource.setMaxWaitMillis(30000);
       dataSource.setRemoveAbandonedOnBorrow(true);
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

//    @Bean
//    public PlatformTransactionManager transactionManager(LocalSessionFactoryBean sessionFactory) {
//        JpaTransactionManager transactionManager = new JpaTransactionManager();
//        transactionManager.set(sessionFactory);
//        return transactionManager;
//    }
    
    @Bean
    public PlatformTransactionManager hibernateTransactionManager() {
        HibernateTransactionManager transactionManager
          = new HibernateTransactionManager();
        transactionManager.setSessionFactory(sessionFactory().getObject());
        return transactionManager;
    }

    @Bean
    public PersistenceExceptionTranslationPostProcessor exceptionTranslation() {
       return new PersistenceExceptionTranslationPostProcessor();
    }

    Properties hibernateProperties() {
       return new Properties() {
          {
             setProperty("hibernate.hbm2ddl.auto", "create-drop");//env.getProperty("hibernate.hbm2ddl.auto"));
             setProperty("hibernate.dialect","org.hibernate.dialect.PostgreSQLDialect");// env.getProperty("hibernate.dialect"));
//             setProperty("hibernate.transaction.coordinator_class","jta");
//             setProperty("hibernate.transaction.jta.platform","JBossAS");
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