package gov.epa.emissions.framework.services.editor;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.security.User;

import java.util.Date;

public class DataAccessToken {

    private String table;

    private Version version;

    private long lockTimeInterval;

    public DataAccessToken() {// needed by Axis
    }

    public DataAccessToken(Version version, String table) {
        this.version = version;
        this.table = table;
    }

    public String getTable() {
        return table;
    }

    public Version getVersion() {
        return version;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public void setVersion(Version version) {
        this.version = version;
    }

    public String key() {
        return "DatasetId:" + version.getDatasetId() + ", Version:" + version.getVersion() + ", Table:" + getTable();
    }

    public int datasetId() {
        return version.getDatasetId();
    }

    public boolean isLocked(User user) {
        return version.isLocked(user);
    }

    public Date lockStart() {
        return version.getLockDate();
    }

    public Date lockEnd() {
        Date lockStart = lockStart();
        return lockStart != null ? new Date(lockStart.getTime() + lockTimeInterval) : null;
    }

    public void setLockTimeInterval(long lockTimeInterval) {
        this.lockTimeInterval = lockTimeInterval;
    }

    public long getLockTimeInterval() {
        return lockTimeInterval;
    }

    public String toString() {
        return key();
    }

}
