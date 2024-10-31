package gov.epa.emissions.framework.services.editor;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.db.version.Versions;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.persistence.LockingScheme;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;

public class SessionLifecycle {
    private Log LOG = LogFactory.getLog(SessionLifecycle.class);

    private DataAccessCache cache;

    private EntityManagerFactory entityManagerFactory;

    private Versions versions;

    private LockableVersions lockableVersions;

    public SessionLifecycle(DataAccessCache cache, EntityManagerFactory entityManagerFactory) {
        this.cache = cache;
        this.entityManagerFactory = entityManagerFactory;
        versions = new Versions();
        lockableVersions = new LockableVersions(versions);
    }

    public Version[] getVersions(int datasetId) throws EmfException {
        try {
            EntityManager entityManager = entityManagerFactory.createEntityManager();
            Version[] results = versions.get(datasetId, entityManager);
            entityManager.close();

            return results;
        } catch (HibernateException e) {
            LOG.error("Could not get all versions of Dataset : " + datasetId, e);
            throw new EmfException("Could not get all versions of Dataset : " + datasetId);
        }
    }

    public Version getVersion(int datasetId, int version) throws EmfException {
        EntityManager entityManager = null;
        try {
            entityManager = entityManagerFactory.createEntityManager();
            return versions.get(datasetId, version, entityManager);
        } catch (HibernateException e) {
            LOG.error("Could not get all versions of Dataset : " + datasetId, e);
            throw new EmfException("Could not get all versions of Dataset : " + datasetId);
        } finally {
            entityManager.close();
        }
    }

    public DataAccessToken open(DataAccessToken token, int pageSize) throws Exception {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        cache.init(token, pageSize, entityManager);
        entityManager.close();

        return token;
    }

    public DataAccessToken open(DataAccessToken token) throws Exception {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        cache.init(token, entityManager);
        entityManager.close();

        return token;
    }

    public void close(DataAccessToken token) throws EmfException {
        try {
            EntityManager entityManager = entityManagerFactory.createEntityManager();
            cache.close(token, entityManager);
            entityManager.close();
        } catch (Exception e) {
            LOG.error("Could not close EntityManager for Dataset: " + token.datasetId() + ", Version: "
                    + token.getVersion().getVersion(), e);
            throw new EmfException("Could not close EntityManager for Dataset: " + token.datasetId() + ", Version: "
                    + token.getVersion().getVersion());
        }
    }

    public void obtainLock(User user, DataAccessToken token) throws EmfException {
        try {
            EntityManager entityManager = entityManagerFactory.createEntityManager();

            Version current = currentVersion(token.getVersion());
            Version locked = lockableVersions.obtainLocked(user, current, entityManager);
            token.setVersion(locked);
            token.setLockTimeInterval(lockTimeInterval(entityManager));

            entityManager.close();
        } catch (HibernateException e) {
            LOG.error("Could not obtain lock of Dataset " + token.datasetId(), e);
            throw new EmfException("Could not obtain lock of Dataset " + token.datasetId());
        }
    }

    private long lockTimeInterval(EntityManager entityManager) {
        return new LockingScheme().timeInterval(entityManager);
    }

    public Version currentVersion(Version reference) throws EmfException {
        try {
            EntityManager entityManager = entityManagerFactory.createEntityManager();
            Version current = versions.current(reference, entityManager);
            entityManager.close();

            return current;
        } catch (HibernateException e) {
            LOG.error("Could not load current version of Dataset : " + reference.getDatasetId(), e);
            throw new EmfException("Could not load current version of Dataset : " + reference.getDatasetId());
        }
    }

    public DataAccessToken openEdit(User user, DataAccessToken token, int pageSize) throws Exception {
        obtainLock(user, token);
        if (!token.isLocked(user))
            return token;// abort

        return open(token, pageSize);
    }

    void releaseLock(User user, DataAccessToken token) throws EmfException {
        try {
            EntityManager entityManager = entityManagerFactory.createEntityManager();
            Version unlocked = lockableVersions.releaseLocked(user, token.getVersion(), entityManager);
            token.setVersion(unlocked);
            token.setLockTimeInterval(lockTimeInterval(entityManager));

            entityManager.close();
        } catch (HibernateException e) {
            LOG.error("Could not release lock of Dataset " + token.datasetId(), e);
            throw new EmfException("Could not release lock of Dataset " + token.datasetId());
        }
    }

    public DataAccessToken closeEdit(User owner, DataAccessToken token) throws EmfException {
        if (!isLockOwned(token))
            return token;//abort

        releaseLock(owner, token);
        close(token);

        return token;
    }

    boolean isLockOwned(DataAccessToken token) throws EmfException {
        Version version = token.getVersion();
        Version current = currentVersion(version);
        return current.isLocked(version.getLockOwner());
    }

    public DataAccessToken renewLock(DataAccessToken token) throws EmfException {
        try {
            EntityManager entityManager = entityManagerFactory.createEntityManager();
            Version extended = lockableVersions.renewLockOnUpdate(token.getVersion(), entityManager);
            token.setVersion(extended);

            entityManager.close();
        } catch (HibernateException e) {
            LOG.error("Could not extend lock of Dataset " + token.datasetId(), e);
            throw new EmfException("Could not extend lock of Dataset " + token.datasetId());
        }

        return token;
    }
}
