package gov.epa.emissions.commons.db.version;

import java.sql.SQLException;

public interface VersionedRecordsWriter {

    /**
     * ChangeSet contains adds, deletes, and updates. An update is treated as a combination of 'delete' and 'add'. In
     * effect, the ChangeSet is written as a list of 'delete' and 'add' operations.
     */
    void update(ChangeSet changeset) throws Exception;

    void close() throws SQLException;

}