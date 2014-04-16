package gov.epa.emissions.framework.services.editor;

import java.sql.SQLException;

import gov.epa.emissions.commons.CommonDebugLevel;
import gov.epa.emissions.commons.PerformanceMetrics;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbServer;
import gov.epa.emissions.commons.db.Page;
import gov.epa.emissions.commons.db.TableDefinition;
import gov.epa.emissions.commons.db.version.DefaultVersionedRecordsFactory;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.db.version.VersionedRecordsFactory;
import gov.epa.emissions.commons.io.TableMetadata;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.EmfServiceImpl;
import gov.epa.emissions.framework.services.InfrastructureException;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DataViewServiceImpl extends EmfServiceImpl implements DataViewService {
    private static Log LOG = LogFactory.getLog(DataViewServiceImpl.class);

    private DataAccessor accessor;

    public DataViewServiceImpl() throws Exception {
        super("Data View Service");
        try {
            init(dbServer, dbServer.getEmissionsDatasource(), HibernateSessionFactory.get());
        } catch (Exception ex) {
            LOG.error("Could not initialize DataView Service", ex);
            throw new InfrastructureException("Could not initialize DataView Service");
        }
    }

    public DataViewServiceImpl(DataSource datasource, DbServer dbServer, HibernateSessionFactory sessionFactory)
            throws Exception {
        super(datasource, dbServer);
        init(dbServer, dbServer.getEmissionsDatasource(), sessionFactory);
    }

    private void init(DbServer dbServer, Datasource datasource, HibernateSessionFactory sessionFactory) {
        VersionedRecordsFactory factory = new DefaultVersionedRecordsFactory(datasource);
        VersionedRecordsWriterFactory writerFactory = new DefaultVersionedRecordsWriterFactory();
        DataAccessCacheImpl cache = new DataAccessCacheImpl(factory, writerFactory, datasource, dbServer
                .getSqlDataTypes());

        accessor = new DataAccessorImpl(cache, sessionFactory);
    }

    public Page applyConstraints(DataAccessToken token, String rowFilter, String sortOrder) throws EmfException {
        accessor.applyConstraints(token, null, rowFilter, sortOrder);
        return getPage(token, 1);
    }

    public Page getPage(DataAccessToken token, int pageNumber) throws EmfException {
        Page page = accessor.getPage(token, pageNumber);
        if ( CommonDebugLevel.DEBUG_PAGE_2) {
            page.print();
        }
        return page;
    }

    public int getPageCount(DataAccessToken token) throws EmfException {
        return accessor.getPageCount(token);
    }

    public Page getPageWithRecord(DataAccessToken token, int record) throws EmfException {
        Page page = accessor.getPageWithRecord(token, record);
        if ( CommonDebugLevel.DEBUG_PAGE_2) {
            page.print();
        }
        return page;
    }

    public int getTotalRecords(DataAccessToken token) throws EmfException {
        return accessor.getTotalRecords(token);
    }

    public Version[] getVersions(int datasetId) throws EmfException {
        return accessor.getVersions(datasetId);
    }

    public Version getVersion(int datasetId, int version) throws EmfException {
        return accessor.getVersion(datasetId, version);
    }

    public DataAccessToken openSession(DataAccessToken token) throws EmfException {
//        Version current = accessor.currentVersion(token.getVersion());
//        if (!current.isFinalVersion())
//            throw new EmfException("Can only view a final Version.");

        try {
            return accessor.openSession(token);
        } catch (Exception e) {
            LOG.error("Could not open Session for Dataset: " + token.datasetId() + ", Version: "
                    + token.getVersion().getVersion(), e);
            throw new EmfException("Could not open Session for Dataset: " + token.datasetId() + ", Version: "
                    + token.getVersion().getVersion());
        }
    }

    public void closeSession(DataAccessToken token) throws EmfException {
        try {
            accessor.closeSession(token);
        } finally {
            new PerformanceMetrics().gc("Closing Data View session - (" + token+ ")");
        }
    }

    /**
     * This method is for cleaning up session specific objects within this service.
     */
    protected void finalize() throws Throwable {
        accessor.shutdown();
        super.finalize();
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
