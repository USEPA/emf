package gov.epa.emissions.commons.db.version;

import java.sql.SQLException;

public interface ScrollableVersionedRecords {

    int total() throws SQLException;

    void close() throws SQLException;

    /**
     * @return returns a range of records inclusive of start and end. Range starts from zero.
     */
    VersionedRecord[] range(int start, int end) throws SQLException;

}