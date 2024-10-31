package gov.epa.emissions.framework.services.editor;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.db.version.Versions;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.persistence.LockingScheme;

import javax.persistence.EntityManager;

public class LockableVersions {

    private Versions versions;

    private LockingScheme lockingScheme;

    public LockableVersions(Versions versions) {
        this.versions = versions;
        lockingScheme = new LockingScheme();
    }

    public Version obtainLocked(User owner, Version version, EntityManager entityManager) {
        return (Version) lockingScheme.getLocked(owner, versions.current(version, entityManager), entityManager);
    }

    public Version releaseLocked(User user, Version locked, EntityManager entityManager) {
        return (Version) lockingScheme.releaseLock(user, versions.current(locked, entityManager), entityManager);
    }

    public Version releaseLockOnUpdate(Version locked, EntityManager entityManager) throws EmfException {
        return (Version) lockingScheme.releaseLockOnUpdate(locked, versions.current(locked, entityManager), entityManager);
    }

    public Version renewLockOnUpdate(Version locked, EntityManager entityManager) throws EmfException {
        return (Version) lockingScheme.renewLockOnUpdate(locked, versions.current(locked, entityManager), entityManager);
    }

}
