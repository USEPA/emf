package gov.epa.emissions.commons.db.version;

import java.sql.SQLException;

import javax.persistence.EntityManager;

public interface VersionedRecordsFactory {

    ScrollableVersionedRecords fetch(Version version, String table, EntityManager entityManager) throws SQLException;

    ScrollableVersionedRecords optimizedFetch(Version version, String table, int batchSize, int pageSize, EntityManager entityManager) throws SQLException;

    ScrollableVersionedRecords fetch(Version version, String table, String columnFilter, String rowFilter,
            String sortOrder, EntityManager entityManager) throws SQLException;

    ScrollableVersionedRecords optimizedFetch(Version version, String table, int batchSize,int pageSize, String columnFilter,
            String rowFilter, String sortOrder, EntityManager entityManager) throws SQLException;
}
