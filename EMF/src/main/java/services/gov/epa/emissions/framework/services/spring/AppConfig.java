package gov.epa.emissions.framework.services.spring;

import java.beans.PropertyVetoException;
import java.util.Properties;

import org.hibernate.SessionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.hibernate4.HibernateExceptionTranslator;
import org.springframework.orm.hibernate4.HibernateTransactionManager;
import org.springframework.orm.hibernate4.LocalSessionFactoryBuilder;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.mchange.v2.c3p0.ComboPooledDataSource;

@Configuration
@ComponentScan(basePackages = "gov.epa.emissions.framework")
@EnableTransactionManagement//(proxyTargetClass=true, mode=AdviceMode.PROXY)
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
    public SessionFactory sessionFactory() {
//    	LocalSessionFactoryBean localSessionFactoryBuilder
//    		= new LocalSessionFactoryBean();
//    	localSessionFactoryBuilder.setDataSource(dataSource()); 
////    	localSessionFactoryBuilder.setPackagesToScan(new String[] {"gov.epa.emissions.framework.**.*"});
//    	Resource configLocation = new ClassPathResource("/hibernate.cfg.xml.old");
//		localSessionFactoryBuilder.setConfigLocation(configLocation);//configure("hibernate.cfg.xml");
////    	localSessionFactoryBuilder.setHibernateProperties(hibernateProperties());
////    	sacfsalocalSessionFactoryBuilder.setJtaTransactionManager(transactionManager());
//    	 return localSessionFactoryBuilder
////    	 .addAnnotatedClasses(Person.class, Account.class)
//    	 .getObject();
    	
    	
		LocalSessionFactoryBuilder localSessionFactoryBuilder = new LocalSessionFactoryBuilder(
				dataSource());
//		localSessionFactoryBuilder
//				.scanPackages("gov.epa.emissions.framework.services.basic");

		// gov/epa/emissions/framework/services/basic/User.hbm.xml
		//
//		localSessionFactoryBuilder.addProperties(hibernateProperties());
		localSessionFactoryBuilder.configure("hibernate.cfg.xml");
		// localSessionFactoryBuilder.setJtaTransactionManager(transactionManager());
		return localSessionFactoryBuilder
		// .addAnnotatedClasses(Person.class, Account.class)
				.buildSessionFactory();
		
		
		
//		    	LocalSessionFactoryBean sessionFactory = new LocalSessionFactoryBean();
//       sessionFactory.setDataSource(dataSource());
//       sessionFactory.setPackagesToScan(new String[] { "gov.epa.emissions.framework" });
//       sessionFactory.setHibernateProperties(hibernateProperties());
//
//       return sessionFactory.getObject();
    }

    @Bean
    public ComboPooledDataSource dataSource() {
//    	class="com.mchange.v2.c3p0.ComboPooledDataSource" destroy-method="close" 
		ComboPooledDataSource dataSource = new ComboPooledDataSource();
		try {
			dataSource.setDriverClass("org.postgresql.Driver");
		} catch (PropertyVetoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		dataSource.setJdbcUrl("jdbc:postgresql://localhost:5432/EMF?autoReconnect=true");
		// dataSource.
		dataSource.setUser("emf");
		dataSource.setPassword("emf");
		dataSource.setMaxPoolSize(92);
		dataSource.setMaxIdleTime(30);
		dataSource.setMaxAdministrativeTaskTime(30000);
		// dataSource.setRemoveAbandoned(true);
		// dataSource.setRemoveAbandonedTimeout(40);
		// dataSource.setLogAbandoned(true);
		dataSource.setPreferredTestQuery("select 1");
		return dataSource;
       
//       JndiObjectFactoryBean jndiObjectFactoryBean = new JndiObjectFactoryBean();
//       jndiObjectFactoryBean.setJndiName("jdbc/EMFDB");
//       jndiObjectFactoryBean.setResourceRef(true);
//       jndiObjectFactoryBean.getObject();
//       return (DataSource) jndiObjectFactoryBean.getObject();//dataSource;

       //    	<bean id="dataSource" class="org.springframework.jndi.JndiObjectFactoryBean" destroy-method="close">
//   		<property name="jndiName" value="jdbc/postgres"></property>
//   		<property name="resourceRef" value="true" />
//   	</bean>
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
    }

    @Bean
    public PlatformTransactionManager  transactionManager() {
       HibernateTransactionManager txManager = new HibernateTransactionManager();
       txManager.setSessionFactory(sessionFactory());

       return txManager;
    }

//   @Bean
//    public TransactionInterceptor transactionInterceptor() {
//    	TransactionInterceptor transactionInterceptor = new TransactionInterceptor();
//    	transactionInterceptor.setTransactionManager(transactionManager());
//    	transactionInterceptor.setTransactionAttributes(
//    			new Properties() {
//    		          {
//    		             setProperty("*", "PROPAGATION_REQUIRED");
//    		          }}    			);
//
//       return transactionInterceptor;
//    }

    
    @Bean 
    public HibernateExceptionTranslator hibernateExceptionTranslator(){ 
      return new HibernateExceptionTranslator(); 
    }
    
    Properties hibernateProperties() {
       return new Properties() {
          {
             setProperty("hibernate.hbm2ddl.auto", "create-drop");//env.getProperty("hibernate.hbm2ddl.auto"));
             setProperty("hibernate.dialect","org.hibernate.dialect.PostgreSQLDialect");// env.getProperty("hibernate.dialect"));
//             setProperty("hibernate.current_session_context_class","gov.epa.emissions.framework.services.spring.TransactionAwareSessionContext");
             setProperty("hibernate.current_session_context_class","org.springframework.orm.hibernate4.SpringSessionContext");
//             setProperty("hibernate.transaction.factory_class","org.hibernate.engine.transaction.internal.jta.CMTTransactionFactory");//org.hibernate.engine.transaction.internal.jta.JtaTransactionFactory org.hibernate.engine.transaction.internal.jta.CMTTransactionFactory

//             <property name="hibernate.current_session_context_class"> org.springframework.orm.hibernate4.SpringSessionContext </property>
             
//           setProperty("hibernate.transaction.manager_lookup_class","org.hibernate.transaction.JDBCTransactionFactory");// env.getProperty("hibernate.dialect"));
             //gov.epa.emissions.framework.services.spring.TransactionAwareSessionContext
             //org.hibernate.context.internal.ThreadLocalSessionContext
             //env.getProperty("hibernate.dialect"));
//             setProperty("hibernate.transaction.manager_lookup_class","org.hibernate.transaction.JDBCTransactionFactory");// env.getProperty("hibernate.dialect"));
//             <prop key="hibernate.transaction.manager_lookup_class">org.hibernate.transaction.JOTMTransactionManagerLookup</prop>              
//             <prop key="hibernate.cache.region.factory_class">org.hibernate.cache.infinispan.InfinispanRegionFactory</prop>
//        <property name="hibernate.transaction.factory_class">org.hibernate.engine.transaction.internal.jta.CMTTransactionFactory</property>
//             <prop key="hibernate.transaction.jta.platform">org.hibernate.service.jta.platform.internal.JBossStandAloneJtaPlatform</prop>
//
//             <prop key="hibernate.transaction.manager_lookup_class">org.hibernate.cache.infinispan.tm.HibernateTransactionManagerLookup</prop>
//<property name="hibernate.current_session_context_class">thread</property>
             
             setProperty("hibernate.transaction.auto_close_session","false");
             setProperty("hibernate.transaction.flush_before_completion","false");
             setProperty("hibernate.bytecode.use_reflection_optimizer","false");
//             <!-- Transactions/Cache -->
//             <property name="hibernate.transaction.auto_close_session">false</property>
//             <property name="hibernate.transaction.flush_before_completion">false</property>
//             <property name="hibernate.bytecode.use_reflection_optimizer">false</property>
//             
             setProperty("hibernate.c3p0.acquire_increment","2");
             setProperty("hibernate.c3p0.idle_test_period","300");
             setProperty("hibernate.c3p0.max_size","80");
             setProperty("hibernate.c3p0.max_statements","0");
             setProperty("hibernate.c3p0.min_size","5");
             setProperty("hibernate.c3p0.timeout","3000");
//             <!-- c3p0 -->
//             <property name="hibernate.c3p0.acquire_increment">2</property> 
//     		<property name="hibernate.c3p0.idle_test_period">300</property> <!-- seconds --> 
//     		<property name="hibernate.c3p0.max_size">80</property> 
//     		<property name="hibernate.c3p0.max_statements">0</property> 
//     		<property name="hibernate.c3p0.min_size">5</property> 
//     		<property name="hibernate.c3p0.timeout">3000</property> <!-- seconds -->
//     		
             setProperty("show_sql","true");
             setProperty("hibernate.use_sql_comments","false");
             setProperty("hibernate.generate_statistics","false");
             setProperty("hibernate.jdbc.batch_size","20");
             setProperty("hibernate.cache.use_second_level_cache","false");
//     		<!-- Misc -->        
//             <property name="show_sql">false</property>
//             <property name="hibernate.use_sql_comments">false</property>
//             <property name="hibernate.generate_statistics">false</property>
//             <property name="hibernate.jdbc.batch_size">20</property>
//             <property name="hibernate.cache.use_second_level_cache">false</property>
          }
       };
    }    
    
//    @Bean
//    public ExportTaskManager exportTaskManager() {
//        return new ExportTaskManager(); 
//    }
    
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
//        caseDAO.setSessionFactory(sessionFactory());
//        return caseDAO;
//    }
    
//    @Bean
//    public DebugLevels debugLevels() {
//        DebugLevels caseDAO = new DebugLevels();
//        caseDAO.setEmfPropertiesDAO(emfPropertiesDAO());
//        return caseDAO;
//    }
//    
//    @Bean
//    public UserServiceImpl userServiceImpl() {
//    	UserServiceImpl userServiceImpl = new UserServiceImpl();
//    	userServiceImpl.setEmfPropertiesDAO(emfPropertiesDAO());
//        return userServiceImpl;
//    }
    
   
}