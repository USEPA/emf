package gov.epa.emissions.framework.services.editor;

import gov.epa.emissions.commons.CommonDebugLevel;
import gov.epa.emissions.commons.PerformanceMetrics;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.Page;
import gov.epa.emissions.commons.db.TableDefinition;
import gov.epa.emissions.commons.db.version.ChangeSet;
import gov.epa.emissions.commons.db.version.DefaultVersionedRecordsFactory;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.db.version.VersionedRecord;
import gov.epa.emissions.commons.db.version.VersionedRecordsFactory;
import gov.epa.emissions.commons.db.version.Versions;
import gov.epa.emissions.commons.io.ColumnMetaData;
import gov.epa.emissions.commons.io.TableMetadata;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.EmfServiceImpl;
import gov.epa.emissions.framework.services.InfrastructureException;
import gov.epa.emissions.framework.services.data.DatasetDAO;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Session;

public class DataEditorServiceImpl extends EmfServiceImpl implements DataEditorService {
    private static final Log LOG = LogFactory.getLog(DataEditorServiceImpl.class);

    private Versions versions;

    private VersionedRecordsFactory factory;

    private DataAccessCache cache;

    private HibernateSessionFactory sessionFactory;

    private DataAccessor accessor;

    public DataEditorServiceImpl() throws Exception {
        super("Data Editor Service");
        try {
            init(dbServer, dbServer.getEmissionsDatasource(), HibernateSessionFactory.get());
        } catch (Exception ex) {
            LOG.error("Could not initialize Data Editor Service", ex);
            throw new InfrastructureException("Server configuration error");
        }
    }

    public DataEditorServiceImpl(DataSource datasource, DbServer dbServer, HibernateSessionFactory sessionFactory)
            throws Exception {
        super(datasource, dbServer);
        init(dbServer, dbServer.getEmissionsDatasource(), sessionFactory);
    }

    private void init(DbServer dbServer, Datasource datasource, HibernateSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
        versions = new Versions();
        factory = new DefaultVersionedRecordsFactory(datasource);

        VersionedRecordsWriterFactory writerFactory = new DefaultVersionedRecordsWriterFactory();
        cache = new DataAccessCacheImpl(factory, writerFactory, datasource, dbServer.getSqlDataTypes());

        accessor = new DataAccessorImpl(cache, sessionFactory);
    }

    public Page applyConstraints(DataAccessToken token, String rowFilter, String sortOrder) throws EmfException {
        accessor.applyConstraints(token, null, rowFilter, sortOrder);
        return getPage(token, 1);
    }

    public Page getPage(DataAccessToken token, int pageNumber) throws EmfException {
        Page page = accessor.getPage(token, pageNumber);
        if ( CommonDebugLevel.DEBUG_PAGE_2){
            page.print();
        }
        return page;
    }

    public int getPageCount(DataAccessToken token) throws EmfException {
        return accessor.getPageCount(token);
    }

    public Page getPageWithRecord(DataAccessToken token, int record) throws EmfException {
        Page page = accessor.getPageWithRecord(token, record);
        if ( CommonDebugLevel.DEBUG_PAGE_2){
            page.print();
        }
        return page;
    }

    public int getTotalRecords(DataAccessToken token) throws EmfException {
        return accessor.getTotalRecords(token);
    }

    public Version derive(Version base, User user, String name) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            Version derived = versions.derive(base, name, user, session);
            return derived;
        } catch (HibernateException e) {
            LOG.error("Could not derive a new Version from the base Version: " + base.getVersion() + " of Dataset: "
                    + base.getDatasetId(), e);
            throw new EmfException("Could not create a new Version using " + base.getVersion() + " as the base");
        }finally{
            session.close();
        }
    }
    
    private VersionedRecord[] validateVersionedRecord( ColumnMetaData[] colMetaDate, VersionedRecord[] records) throws EmfException {
        if ( colMetaDate != null && records != null){
            for ( VersionedRecord vr : records) {
                
                if ( CommonDebugLevel.DEBUG_PAGE_2) {
                    System.out.println("******");
                    System.out.println("******");
                    System.out.println("******");

                    for ( int j=0; j<vr.size(); j++) {
                        if ( vr.token(j) != null) {
                            System.out.println( j + "> class: " + vr.token(j).getClass());
                        } else {
                            System.out.println( j + "> null");
                        }
                    }
                }
                
                if ( vr != null && colMetaDate.length != vr.size() + vr.numVersionCols()) {
                    throw new EmfException("VersionedRecord length + " + vr.numVersionCols() + " <> ColumnMetaData length");
                }
                
                for ( int j=0; j<vr.size(); j++) {    
                    String type = colMetaDate[j+vr.numVersionCols()].getType();
                    if ( vr.token(j) != null && !vr.token(j).getClass().getName().equals( type)) {
                        if ( vr.token(j) instanceof java.util.Calendar) {
                            if ( type.equals( java.sql.Timestamp.class.getName())){
                                java.sql.Timestamp ts = new java.sql.Timestamp(((java.util.Calendar)vr.token(j)).getTime().getTime());
                                vr.replace(j, ts);
                            } else if ( type.equals( java.sql.Time.class.getName())){
                                java.sql.Time ts = new java.sql.Time(((java.util.Calendar)vr.token(j)).getTime().getTime());
                                vr.replace(j, ts);
                            } else if ( type.equals( java.sql.Date.class.getName())){
                                java.sql.Date ts = new java.sql.Date(((java.util.Calendar)vr.token(j)).getTime().getTime());
                                vr.replace(j, ts);
                            } else if ( type.equals( java.util.Date.class.getName())){
                                java.util.Date ts = ((java.util.Calendar)vr.token(j)).getTime();
                                vr.replace(j, ts);
                            } else {
                                throw new EmfException("Do not support the conversion from " + vr.token(j).getClass() + " to " + type);
                            }
                        }
                    }
                }
            }
        }
        return records;
    }
    
    private ChangeSet validateChangeSet( DataAccessToken token, ChangeSet changeset) throws EmfException {
        TableMetadata tableMetaData = getTableMetadata( token.getTable());
        ColumnMetaData[] colMetaDate = tableMetaData.getCols();
        
        if ( CommonDebugLevel.DEBUG_PAGE_2) {
            if ( colMetaDate != null) {
                for ( int i=0; i<colMetaDate.length; i++) {
                    if ( colMetaDate[i] != null) {
                        System.out.println( i + "> class: " + colMetaDate[i].getType() );
                    } else {
                        System.out.println( i + "> null" );
                    }
                }
            }
        }
        
        changeset.setNewRecords( validateVersionedRecord( colMetaDate, changeset.getNewRecords()));
        changeset.setUpdatedRecords( validateVersionedRecord( colMetaDate, changeset.getUpdatedRecords()));
        changeset.setDeletedRecords( validateVersionedRecord( colMetaDate, changeset.getDeletedRecords()));
        
        return changeset;
    }

    public void submit(DataAccessToken token, ChangeSet changeset, int pageNumber) throws EmfException {
        try {
            
            if ( CommonDebugLevel.DEBUG_PAGE_2) {
                System.out.println("======");
                System.out.println("======");
                System.out.println("======\n>>> Before Validation");
                changeset.print();
                System.out.println("======");
                System.out.println("======");
                System.out.println("======");
            } 
            
            changeset = validateChangeSet ( token, changeset);
            
            if ( CommonDebugLevel.DEBUG_PAGE_2) {
                System.out.println("\n======");
                System.out.println("\n======");
                System.out.println("\n======\n>>> After Validation");
                changeset.print();
                System.out.println("\n======");
                System.out.println("\n======");
                System.out.println("\n======");
            } 
            
            Session session = sessionFactory.getSession();
            cache.submitChangeSet(token, changeset, pageNumber, session);
            session.close();
        } catch (Exception e) {
            LOG.error("Could not submit changes for Dataset: " + token.datasetId() + ". Version: " + token.getVersion()
                    + "." + e.getMessage(), e);
            throw new EmfException("Could not submit changes for Dataset: " + token.datasetId() + ". Version: "
                    + token.getVersion() + " - " + e.getMessage());
        }
    }

    public void discard(DataAccessToken token) throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            cache.discardChangeSets(token, session);
            session.close();
        } catch (Exception e) {
            LOG.error("Could not discard changes for Dataset: " + token.datasetId() + ". Version: "
                    + token.getVersion() + "\t" + e.getMessage(), e);
            throw new EmfException("Could not discard changes for Dataset: " + token.datasetId() + ". Version: "
                    + token.getVersion() + "\t" + e.getMessage());
        }
    }

    public DataAccessToken save(DataAccessToken token, EmfDataset dataset, Version version) throws EmfException {
        try {
            if (!accessor.isLockOwned(token))
                return token;// abort

            DataAccessToken extended = accessor.renewLock(token);
            return doSave(extended, cache, sessionFactory, dataset);
        } catch (Exception e) {
            LOG.error("Could not save changes for Dataset: " + token.datasetId() + ". Version: " + token.getVersion()
                    + "\t" + e.getMessage(), e);
            throw new EmfException("Could not save changes for Dataset: " + token.datasetId() + ". Version: "
                    + token.getVersion() + "\t" + e.getMessage());
        }
    }

    private DataAccessToken doSave(DataAccessToken token, DataAccessCache cache,
            HibernateSessionFactory hibernateSessionFactory, EmfDataset dataset) throws EmfException {
        try {
            updateDataset(hibernateSessionFactory, dataset);
            saveDataEditChanges(token, cache, hibernateSessionFactory); //JIZHEN-JIZHEN

        } catch (Exception e) {
            LOG.error("Could not update Dataset: " + token.datasetId() + " with changes for Version: "
                    + token.getVersion() + "\t" + e.getMessage(), e);
            // unwind token
            
            throw new EmfException("Could not update Dataset: " + token.datasetId() + " with changes for Version: "
                    + token.getVersion());
        }

        return token;
    }

    private void saveDataEditChanges(DataAccessToken token, DataAccessCache cache,
            HibernateSessionFactory hibernateSessionFactory) throws Exception {
        Session session = hibernateSessionFactory.getSession();
        try {
            cache.save(token, session);
        } finally {
            session.close();
        }
    }

    private void updateDataset(HibernateSessionFactory hibernateSessionFactory, EmfDataset dataset) throws Exception {
        Session session = hibernateSessionFactory.getSession();
        try {
            DatasetDAO dao = new DatasetDAO();
            dao.updateWithoutLocking(dataset, session);
        } finally {
            session.close();
        }
    }

    void updateVersion(Version version) {
        Session session = sessionFactory.getSession();
        try {
            versions.save(version, session);
        } finally {
            session.close();
        }
    }

    Version doMarkFinal(Version derived) throws EmfException {
        Session session = sessionFactory.getSession();
        try {
            Version version = versions.markFinal(derived, session);
            return version;
        } catch (HibernateException e) {
            LOG.error("Could not mark a derived Version: " + derived.getDatasetId() + " as Final" + "." + e);
            throw new EmfException("Could not mark a derived Version: " + derived.getDatasetId() + " as Final");
        }finally{
            session.close();
        }
    }

    public Version markFinal(DataAccessToken token) throws EmfException {
        Version derived = token.getVersion();
        Version current = accessor.currentVersion(derived);
        if (current.isLocked() && !derived.isLocked(current.getLockOwner()))
            throw new EmfException("Cannot mark Version " + derived.getName() + " Final as it is locked by "
                    + current.getLockOwner());

        return doMarkFinal(derived);
    }

    public Version[] getVersions(int datasetId) throws EmfException {
        return accessor.getVersions(datasetId);
    }

    public Version getVersion(int datasetId, int version) throws EmfException {
        return accessor.getVersion(datasetId, version);
    }

    public DataAccessToken openSession(User user, DataAccessToken token) throws EmfException {
        return openSession(user, token, accessor.defaultPageSize());
    }

    public DataAccessToken openSession(User user, DataAccessToken token, int pageSize) throws EmfException {
        Version current = accessor.currentVersion(token.getVersion());
        if (current.isFinalVersion())
            throw new EmfException("Can only edit non-final Version.");

        try {
            return accessor.openEditSession(user, token, pageSize);
        } catch (Exception e) {
            LOG.error("Could not open Session for Dataset: " + token.datasetId() + ", Version: "
                    + token.getVersion().getVersion() + "." + e.getMessage(), e);
            throw new EmfException("Could not open Session for Dataset: " + token.datasetId() + ", Version: "
                    + token.getVersion().getVersion());
        }
    }

    public void closeSession(User user, DataAccessToken token) throws EmfException {
        try {
            accessor.closeEditSession(user, token);
        } finally {
            new PerformanceMetrics().gc("Closing Data Editor session - (" + token + ")");
        }
    }

    /**
     * This method is for cleaning up session specific objects within this service.
     */
    protected void finalize() throws Throwable {
        accessor.shutdown();
        super.finalize();
    }

    public boolean hasChanges(DataAccessToken token) throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            boolean result = cache.hasChanges(token, session);
            session.close();

            return result;
        } catch (Exception e) {
            Version version = token.getVersion();
            LOG.error("Could not confirm changes for Version: " + version.getDatasetId() + "." + e);
            throw new EmfException("Could not confirm changes for Version: " + version.getDatasetId());
        }
    }

    public TableMetadata getTableMetadata(String table) throws EmfException {
        try {
            TableDefinition definition = dbServer.getEmissionsDatasource().tableDefinition();
            return definition.getTableMetaData(table);
        } catch (SQLException e) {
            LOG.error("Database error. Failed to get table metadata for table: ", e);
            throw new EmfException("Database error");
        }

    }

}
