package gov.epa.emissions.commons.db.version;

import java.sql.SQLException;

import org.hibernate.Session;

public interface VersionedRecordsFactory {

    ScrollableVersionedRecords fetch(Version version, String table, Session session) throws SQLException;

    ScrollableVersionedRecords optimizedFetch(Version version, String table, int batchSize, int pageSize, Session session) throws SQLException;

    ScrollableVersionedRecords fetch(Version version, String table, String columnFilter, String rowFilter,
            String sortOrder, Session session) throws SQLException;

    ScrollableVersionedRecords optimizedFetch(Version version, String table, int batchSize,int pageSize, String columnFilter,
            String rowFilter, String sortOrder, Session session) throws SQLException;
}
