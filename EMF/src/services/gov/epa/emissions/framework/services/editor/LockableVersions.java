package gov.epa.emissions.framework.services.editor;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.db.version.Versions;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.persistence.LockingScheme;

import org.hibernate.Session;

public class LockableVersions {

    private Versions versions;

    private LockingScheme lockingScheme;

    public LockableVersions(Versions versions) {
        this.versions = versions;
        lockingScheme = new LockingScheme();
    }

    public Version obtainLocked(User owner, Version version, Session session) {
        return (Version) lockingScheme.getLocked(owner, versions.current(version, session), session);
    }

    public Version releaseLocked(User user, Version locked, Session session) {
        return (Version) lockingScheme.releaseLock(user, versions.current(locked, session), session);
    }

    public Version releaseLockOnUpdate(Version locked, Session session) throws EmfException {
        return (Version) lockingScheme.releaseLockOnUpdate(locked, versions.current(locked, session), session);
    }

    public Version renewLockOnUpdate(Version locked, Session session) throws EmfException {
        return (Version) lockingScheme.renewLockOnUpdate(locked, versions.current(locked, session), session);
    }

}
