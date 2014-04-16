package gov.epa.emissions.framework.services;

import gov.epa.emissions.commons.db.DataModifier;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.db.TableReader;
import gov.epa.emissions.commons.db.postgres.PostgresDbUpdate;
import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.framework.services.casemanagement.CaseInput;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.editor.Revision;
import gov.epa.emissions.framework.services.exim.ExImServiceImpl;
import gov.epa.emissions.framework.services.persistence.EmfDatabaseSetup;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;
import gov.epa.emissions.framework.services.persistence.LocalHibernateConfiguration;
import gov.epa.emissions.framework.services.persistence.VersionedTable;

import java.io.File;
import java.io.FileInputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;

import junit.framework.TestCase;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

public abstract class ServiceTestCase extends TestCase {

    protected EmfDatabaseSetup dbSetup;

    protected DbServerFactory dbServerFactory;

    protected HibernateSessionFactory sessionFactory;

    protected Session session;

    final protected void setUp() throws Exception {
        this.dbSetup = new EmfDatabaseSetup(config());
        this.dbServerFactory = new DbServerFactory(dbSetup);
        this.sessionFactory = sessionFactory();
        this.session = this.sessionFactory.getSession();
        doSetUp();
    }

    abstract protected void doSetUp() throws Exception;

    protected void setProperties() throws Exception {
        DbServer dbServer = dbServerFactory.getDbServer();
        //this will set the properties...
        new ExImServiceImpl(dbServerFactory, sessionFactory);
        dbServer.disconnect();
    }
    
    protected Properties config() throws Exception {
        File conf = configFile();

        if (!conf.exists() || !conf.isFile()) {
            String error = "File: " + conf + " does not exist. Please copy either of the two TEMPLATE files "
                    + "(from " + conf.getParent() + "), name it " + conf.getName() + ", configure "
                    + "it as needed, and rerun.";
            throw new RuntimeException(error);
        }

        Properties properties = new Properties();
        properties.load(new FileInputStream(conf));

        return properties;
    }

    protected File configFile() {
        String folder = "test";
        return new File(folder, "postgres.conf");
    }

    final protected void tearDown() throws Exception {
        doTearDown();
        session.close();
        dbSetup.tearDown();
    }

    abstract protected void doTearDown() throws Exception;

    protected Datasource emissions() {
        return dbServer().getEmissionsDatasource();
    }

    protected DataSource emf() {
        return dbSetup.emfDatasource();
    }

    protected DbServer dbServer() {
        return dbSetup.getDbServer();
    }
    
    protected DbServer getDbServerInstance() throws Exception {
        return dbSetup.getNewPostgresDbServerInstance();
    }

    protected DbServerFactory dbServerFactory() throws Exception {
        return dbServerFactory;
    }

    protected SqlDataTypes sqlDataTypes() {
        return dbServer().getSqlDataTypes();
    }

    protected void dropTable(String table, Datasource datasource) throws Exception {
        PostgresDbUpdate dbUpdate = new PostgresDbUpdate();
        dbUpdate.dropTable(datasource.getName(), table);
    }

    protected void dropData(String table, Datasource datasource) throws Exception {
        DataModifier modifier = datasource.dataModifier();
        modifier.dropAllData(table);
    }

    protected HibernateSessionFactory sessionFactory() throws Exception {
        if (this.sessionFactory == null) 
            sessionFactory = sessionFactory(configFile());
        return this.sessionFactory;
    }

    protected HibernateSessionFactory sessionFactory(File configFile) throws Exception {
        LocalHibernateConfiguration config = new LocalHibernateConfiguration(configFile);
        return new HibernateSessionFactory(config.factory());
    }

    public void deleteAllDatasets() throws Exception {
        dropAll(CaseInput.class);
        dropAll(Revision.class);
        dropAll(EmfDataset.class);
    }

    protected void dropAll(Class clazz) {
        session.clear();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            List all = session.createCriteria(clazz).list();
            for (Iterator iter = all.iterator(); iter.hasNext();)
                session.delete(iter.next());
            tx.commit();
        } catch (HibernateException e) {
            e.printStackTrace();
            tx.rollback();
            throw e;
        }catch(Throwable e){
            e.printStackTrace();
        } finally {
            session.clear();
        }
    }

    protected void dropAll(String clazz) {
        try {
          String hqlDelete = "delete " + clazz;
          session.createQuery( hqlDelete )
               .executeUpdate();
        } catch (HibernateException e) {
            e.printStackTrace();
            throw e;
        } catch(Throwable e){
            e.printStackTrace();
        }
    }

    protected void createVersionedTable(String table, Datasource datasource, Column[] cols) throws Exception {
        new VersionedTable(datasource, sqlDataTypes()).create(table, cols);
    }

    protected void remove(Object object) {
        session.clear();// flush cached objects
        Transaction tx = null;
        try{
            tx = session.beginTransaction();
            session.delete(object);
            tx.commit();
        }catch (Exception e) {
            tx.rollback();
            e.printStackTrace();
            //throw e;
        }
    }

    protected void add(Object object) {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.save(object);
            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }

    protected void save(Object obj) {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.save(obj);
            tx.commit();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }

    protected Object load(Class clazz, String name) {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            Criteria crit = session.createCriteria(clazz).add(Restrictions.eq("name", name));
            tx.commit();
            Object obj = crit.uniqueResult();
            return obj;
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }
    
    protected Object load(Class clazz, int id) {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            Criteria crit = session.createCriteria(clazz).add(Restrictions.eq("id", id));
            tx.commit();
            Object obj = crit.uniqueResult();
            return obj;
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }
    
    protected int countRecords(String tableName) {
        Datasource datasource = dbServer().getEmissionsDatasource();
        TableReader tableReader = dbSetup.tableReader(datasource);
        return tableReader.count(datasource.getName(), tableName.toLowerCase());
    }

}