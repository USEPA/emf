package gov.epa.emissions.framework.services.editor;

import gov.epa.emissions.commons.CommonDebugLevel;
import gov.epa.emissions.commons.db.Page;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.tasks.DebugLevels;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DataAccessorImpl implements DataAccessor {
    private Log LOG = LogFactory.getLog(DataAccessorImpl.class);

    private DataAccessCache cache;

    private PageFetch pageFetch;

    private SessionLifecycle sessionLifecycle;

    private EntityManagerFactory entityManagerFactory;

    public DataAccessorImpl(DataAccessCache cache, EntityManagerFactory entityManagerFactory) {
        this.cache = cache;
        this.entityManagerFactory = entityManagerFactory;
        pageFetch = new PageFetch(cache);
        sessionLifecycle = new SessionLifecycle(cache, entityManagerFactory);
    }

    public int defaultPageSize() {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        int result = pageFetch.defaultPageSize(entityManager);
        entityManager.close();

        return result;
    }

    public void applyConstraints(DataAccessToken token, String columnFilter, String rowFilter, String sortOrder)
            throws EmfException {
        if (DebugLevels.DEBUG_19()) {
            System.out.println("DataAccessorImpl:applyConstraints():token null ? " + (token == null));
            if (token != null)
                System.out.println("\tTable: " + token.getTable() + " column filter: " + columnFilter + " row filter: " + rowFilter);
        }
        
        try {
            EntityManager entityManager = entityManagerFactory.createEntityManager();
            cache.applyConstraints(token, columnFilter, rowFilter, sortOrder, entityManager);
            entityManager.close();
        } catch (Exception e) {
            // don't need to log this to file
            //LOG.error("Could not apply sort or filter constraints for Dataset: " + token.datasetId(), e);
            throw new EmfException("Could not apply sort or filter constraints to Dataset");
        }
    }

    public Page getPage(DataAccessToken token, int pageNumber) throws EmfException {
        if (DebugLevels.DEBUG_19()) {
            System.out.println("DataAccessorImpl:getPage():token null ? " + (token == null));
            if (token != null)
                System.out.println("\tTable: " + token.getTable() + " key: " + token.key() + " page number: " + pageNumber);
        }
        
        try {
            EntityManager entityManager = entityManagerFactory.createEntityManager();
            Page result = pageFetch.getPage(token, pageNumber, entityManager);
            entityManager.close();
            
            if ( CommonDebugLevel.DEBUG_PAGE_3){
                result.print();
            }

            return result;
        } catch (Exception e) {
            LOG.error("Could not get Page: " + pageNumber + " for Dataset: " + token.datasetId(), e);
            throw new EmfException("Could not get Page: " + pageNumber + " for Dataset: " + token.datasetId());
        }

    }

    public int getPageCount(DataAccessToken token) throws EmfException {
        try {
            return pageFetch.getPageCount(token);
        } catch (Exception e) {
            LOG.error("Failed to get page count", e);
            throw new EmfException("Failed to get page count");
        }
    }

    public Page getPageWithRecord(DataAccessToken token, int record) throws EmfException {
        try {
            EntityManager entityManager = entityManagerFactory.createEntityManager();
            Page page = pageFetch.getPageWithRecord(token, record, entityManager);
            entityManager.close();

            if ( CommonDebugLevel.DEBUG_PAGE_3){
                page.print();
            }
            
            return page;
        } catch (Exception ex) {
            LOG.error("Could not obtain the page with Record: " + record, ex);
            throw new EmfException("Could not obtain the page with Record: " + record);
        }
    }

    public int getTotalRecords(DataAccessToken token) throws EmfException {
        try {
            EntityManager entityManager = entityManagerFactory.createEntityManager();
            int result = pageFetch.getTotalRecords(token, entityManager);
            entityManager.close();

            return result;
        } catch (Exception e) {
            LOG.error("Failed to get a count of total number of records for dsID "+token.datasetId(), e);
            throw new EmfException("Failed to get a count of total number of records for dsID "+token.datasetId());
        }
    }

    public Version currentVersion(Version reference) throws EmfException {
        return sessionLifecycle.currentVersion(reference);
    }

    public Version[] getVersions(int datasetId) throws EmfException {
        return sessionLifecycle.getVersions(datasetId);
    }

    public Version getVersion(int datasetId, int version) throws EmfException {
        return sessionLifecycle.getVersion(datasetId, version);
    }

    public void shutdown() throws EmfException {
        try {
            cache.invalidate();
        } catch (Exception e) {
            LOG.error("Could not close DataView Service", e);
            throw new EmfException("Could not close DataView Service");
        }
    }

    public DataAccessToken openSession(DataAccessToken token, int pageSize) throws Exception {
        return sessionLifecycle.open(token, pageSize);
    }

    public DataAccessToken openSession(DataAccessToken token) throws Exception {
        return sessionLifecycle.open(token);
    }

    public void closeSession(DataAccessToken token) throws EmfException {
        sessionLifecycle.close(token);
    }

    public DataAccessToken openEditSession(User user, DataAccessToken token) throws Exception {
        return openEditSession(user, token, defaultPageSize());
    }

    public DataAccessToken openEditSession(User user, DataAccessToken token, int pageSize) throws Exception {
        return sessionLifecycle.openEdit(user, token, pageSize);
    }

    public DataAccessToken closeEditSession(User user, DataAccessToken token) throws EmfException {
        return sessionLifecycle.closeEdit(user, token);
    }

    public boolean isLockOwned(DataAccessToken token) throws EmfException {
        return sessionLifecycle.isLockOwned(token);
    }

    public DataAccessToken renewLock(DataAccessToken token) throws EmfException {
        return sessionLifecycle.renewLock(token);
    }

    public void lock(User user, DataAccessToken token) throws EmfException {
        sessionLifecycle.obtainLock(user, token);
    }

    public boolean isLocked(Version version) throws EmfException {
        return currentVersion(version).isLocked();
    }

}
