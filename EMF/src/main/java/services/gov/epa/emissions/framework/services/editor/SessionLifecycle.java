package gov.epa.emissions.framework.services.editor;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.db.version.Versions;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;
import gov.epa.emissions.framework.services.persistence.LockingScheme;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Session;

public class SessionLifecycle {
    private Log LOG = LogFactory.getLog(SessionLifecycle.class);

    private DataAccessCache cache;

    private HibernateSessionFactory sessionFactory;

    private Versions versions;

    private LockableVersions lockableVersions;

    public SessionLifecycle(DataAccessCache cache, HibernateSessionFactory sessionFactory) {
        this.cache = cache;
        this.sessionFactory = sessionFactory;
        versions = new Versions();
        lockableVersions = new LockableVersions(versions);
    }

    public Version[] getVersions(int datasetId) throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            Version[] results = versions.get(datasetId, session);
            session.close();

            return results;
        } catch (HibernateException e) {
            LOG.error("Could not get all versions of Dataset : " + datasetId, e);
            throw new EmfException("Could not get all versions of Dataset : " + datasetId);
        }
    }

    public Version getVersion(int datasetId, int version) throws EmfException {
        Session session = null;
        try {
            session = sessionFactory.getSession();
            return versions.get(datasetId, version, session);
        } catch (HibernateException e) {
            LOG.error("Could not get all versions of Dataset : " + datasetId, e);
            throw new EmfException("Could not get all versions of Dataset : " + datasetId);
        } finally {
            session.close();
        }
    }

    public DataAccessToken open(DataAccessToken token, int pageSize) throws Exception {
        Session session = sessionFactory.getSession();
        cache.init(token, pageSize, session);
        session.close();

        return token;
    }

    public DataAccessToken open(DataAccessToken token) throws Exception {
        Session session = sessionFactory.getSession();
        cache.init(token, session);
        session.close();

        return token;
    }

    public void close(DataAccessToken token) throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            cache.close(token, session);
            session.close();
        } catch (Exception e) {
            LOG.error("Could not close Session for Dataset: " + token.datasetId() + ", Version: "
                    + token.getVersion().getVersion(), e);
            throw new EmfException("Could not close Session for Dataset: " + token.datasetId() + ", Version: "
                    + token.getVersion().getVersion());
        }
    }

    public void obtainLock(User user, DataAccessToken token) throws EmfException {
        try {
            Session session = sessionFactory.getSession();

            Version current = currentVersion(token.getVersion());
            Version locked = lockableVersions.obtainLocked(user, current, session);
            token.setVersion(locked);
            token.setLockTimeInterval(lockTimeInterval(session));

            session.close();
        } catch (HibernateException e) {
            LOG.error("Could not obtain lock of Dataset " + token.datasetId(), e);
            throw new EmfException("Could not obtain lock of Dataset " + token.datasetId());
        }
    }

    private long lockTimeInterval(Session session) {
        return new LockingScheme().timeInterval(session);
    }

    public Version currentVersion(Version reference) throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            Version current = versions.current(reference, session);
            session.close();

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
            Session session = sessionFactory.getSession();
            Version unlocked = lockableVersions.releaseLocked(user, token.getVersion(), session);
            token.setVersion(unlocked);
            token.setLockTimeInterval(lockTimeInterval(session));

            session.close();
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
            Session session = sessionFactory.getSession();
            Version extended = lockableVersions.renewLockOnUpdate(token.getVersion(), session);
            token.setVersion(extended);

            session.close();
        } catch (HibernateException e) {
            LOG.error("Could not extend lock of Dataset " + token.datasetId(), e);
            throw new EmfException("Could not extend lock of Dataset " + token.datasetId());
        }

        return token;
    }
}
