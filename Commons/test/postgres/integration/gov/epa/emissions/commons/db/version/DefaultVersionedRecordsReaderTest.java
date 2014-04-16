package gov.epa.emissions.commons.db.version;

import gov.epa.emissions.commons.db.Datasource;

import java.sql.SQLException;

public class DefaultVersionedRecordsReaderTest extends VersionedRecordsTestCase {

    private DefaultVersionedRecordsFactory reader;

    protected void setUp() throws Exception {
        super.setUp();

        setupVersionZero(datasource, versionsTable);
        setupVersionZeroData(datasource, dataTable);

        reader = new DefaultVersionedRecordsFactory(datasource);
    }

    private void setupVersionZero(Datasource datasource, String table) throws SQLException {
        addRecord(datasource, table, new String[] { null, "1", "0", "", "", "true" });
    }

    private void setupVersionZeroData(Datasource datasource, String table) throws SQLException {
        addRecord(datasource, table, new String[] { "1", "1", "0", null, "p1", "p2" });
        addRecord(datasource, table, new String[] { "2", "1", "0", "6", "p21", "p22" });
        addRecord(datasource, table, new String[] { "3", "1", "0", "2", "p31", "p32" });
        addRecord(datasource, table, new String[] { "4", "1", "0", null, "p41", "p42" });
        addRecord(datasource, table, new String[] { "5", "1", "0", null, "p51", "p52" });
    }

    public void testFetchVersionZero() throws Exception {
        Version versionZero = new Version();
        versionZero.setDatasetId(1);
        versionZero.setVersion(0);

        ScrollableVersionedRecords records = reader.fetch(versionZero, dataTable, session);

        assertEquals(5, records.total());
    }

    public void testFetchVersionZeroUsingOptimizedFetch() throws Exception {
        Version versionZero = new Version();
        versionZero.setDatasetId(1);
        versionZero.setVersion(0);

        int batchSize = 10000;
        int pageSize =300;
        ScrollableVersionedRecords records = reader.optimizedFetch(versionZero, dataTable, batchSize, pageSize,session);

        assertEquals(5, records.total());
    }

    public void testFetchVersionTwoThatHasARecordDeleteFromVersionOne() throws Exception {
        // mark record 6 deleted from version 2
        addRecord(datasource, dataTable, new String[] { "6", "1", "1", "2", "p61", "p62" });
        addRecord(datasource, dataTable, new String[] { "7", "1", "1", null, "p71", "p72" });
        // setup version sequence
        addRecord(datasource, versionsTable, new String[] { null, "1", "1", "v1", "0" });
        addRecord(datasource, versionsTable, new String[] { null, "1", "2", "v2", "0,1" });

        Version versionTwo = new Version();
        versionTwo.setDatasetId(1);
        versionTwo.setVersion(2);

        VersionedRecord[] records = reader.fetchAll(versionTwo, dataTable, session);

        assertEquals(5, records.length);

        assertEquals(1, records[0].getRecordId());
        assertEquals(2, records[1].getRecordId());
        assertEquals(4, records[2].getRecordId());
        assertEquals(5, records[3].getRecordId());
        assertEquals(7, records[4].getRecordId());
    }

    public void testFetchWhenARecordIsRemovedFromMultipleVersionsInDifferentNonLinearSequences() throws Exception {
        // add records
        addRecord(datasource, dataTable, new String[] { "6", "1", "0", "3", "p61", "p62" });
        addRecord(datasource, dataTable, new String[] { "7", "1", "1", null, "p1", "p2" });
        addRecord(datasource, dataTable, new String[] { "8", "1", "4", null, "p1", "p2" });
        addRecord(datasource, dataTable, new String[] { "9", "1", "2", "4", "p1", "p2" });
        addRecord(datasource, dataTable, new String[] { "10", "1", "2", "5", "p1", "p2" });
        addRecord(datasource, dataTable, new String[] { "11", "1", "1", "3,4", "p1", "p2" });
        addRecord(datasource, dataTable, new String[] { "12", "1", "1", "2,3", "p1", "p2" });
        addRecord(datasource, dataTable, new String[] { "13", "1", "2", "4,5", "p1", "p2" });
        addRecord(datasource, dataTable, new String[] { "14", "1", "0", "2,3,7", "p1", "p2" });

        // setup version sequence
        addRecord(datasource, versionsTable, new String[] { null, "1", "1", "v1", "0" });
        addRecord(datasource, versionsTable, new String[] { null, "1", "2", "v2", "0,1" });
        addRecord(datasource, versionsTable, new String[] { null, "1", "3", "v3", "0,1" });
        addRecord(datasource, versionsTable, new String[] { null, "1", "4", "v4", "0,1,2" });
        addRecord(datasource, versionsTable, new String[] { null, "1", "5", "v5", "0,1,2" });
        addRecord(datasource, versionsTable, new String[] { null, "1", "6", "v6", "0,1,2,5" });
        addRecord(datasource, versionsTable, new String[] { null, "1", "7", "v7", "0" });

        verifyVersionThreeForRemoveMultipleVersionsInNonLinearSequence();
        verifyVersionTwoForRemoveMultipleVersionsInNonLinearSequence();
        verifyVersionFourForRemoveMultipleVersionsInNonLinearSequence();
    }

    private void verifyVersionThreeForRemoveMultipleVersionsInNonLinearSequence() throws SQLException {
        Version version = new Version();
        version.setDatasetId(1);
        version.setVersion(3);

        VersionedRecord[] records = reader.fetchAll(version, dataTable, session);

        assertEquals(6, records.length);

        assertEquals(1, records[0].getRecordId());
        assertEquals(2, records[1].getRecordId());
        assertEquals(3, records[2].getRecordId());
        assertEquals(4, records[3].getRecordId());
        assertEquals(5, records[4].getRecordId());
        assertEquals(7, records[5].getRecordId());
    }

    private void verifyVersionTwoForRemoveMultipleVersionsInNonLinearSequence() throws SQLException {
        Version version = new Version();
        version.setDatasetId(1);
        version.setVersion(2);

        VersionedRecord[] records = reader.fetchAll(version, dataTable, session);

        assertEquals(10, records.length);

        assertEquals(1, records[0].getRecordId());
        assertEquals(2, records[1].getRecordId());
        assertEquals(4, records[2].getRecordId());
        assertEquals(5, records[3].getRecordId());
        assertEquals(6, records[4].getRecordId());
        assertEquals(7, records[5].getRecordId());
        assertEquals(9, records[6].getRecordId());
        assertEquals(10, records[7].getRecordId());
        assertEquals(11, records[8].getRecordId());
        assertEquals(13, records[9].getRecordId());
    }

    private void verifyVersionFourForRemoveMultipleVersionsInNonLinearSequence() throws SQLException {
        Version version = new Version();
        version.setDatasetId(1);
        version.setVersion(4);

        VersionedRecord[] records = reader.fetchAll(version, dataTable, session);

        assertEquals(8, records.length);

        assertEquals(1, records[0].getRecordId());
        assertEquals(2, records[1].getRecordId());
        assertEquals(4, records[2].getRecordId());
        assertEquals(5, records[3].getRecordId());
        assertEquals(6, records[4].getRecordId());
        assertEquals(7, records[5].getRecordId());
        assertEquals(8, records[6].getRecordId());
        assertEquals(10, records[7].getRecordId());
    }

    public void testFetchWithDeletesAcrossMultipleVersions() throws Exception {
        // mark record 6 as deleted from version 2
        addRecord(datasource, dataTable, new String[] { "6", "1", "1", "2", "p61", "p62" });
        addRecord(datasource, dataTable, new String[] { "7", "1", "1", null, "p71", "p72" });
        addRecord(datasource, dataTable, new String[] { "8", "1", "2", "3", "p81", "p82" });
        addRecord(datasource, dataTable, new String[] { "9", "1", "3", null, "p1", "p2" });
        addRecord(datasource, dataTable, new String[] { "10", "1", "4", null, "p", "p2" });
        addRecord(datasource, dataTable, new String[] { "11", "1", "4", "5", "p", "p2" });
        addRecord(datasource, dataTable, new String[] { "12", "1", "5", null, "p", "p2" });
        addRecord(datasource, dataTable, new String[] { "13", "1", "5", null, "p", "p2" });
        addRecord(datasource, dataTable, new String[] { "14", "1", "6", null, "p", "p2" });

        // setup version sequence
        addRecord(datasource, versionsTable, new String[] { null, "1", "1", "v1", "0" });
        addRecord(datasource, versionsTable, new String[] { null, "1", "2", "v2", "0,1" });
        addRecord(datasource, versionsTable, new String[] { null, "1", "3", "v3", "0,1,2" });
        addRecord(datasource, versionsTable, new String[] { null, "1", "4", "v4", "0,1" });
        addRecord(datasource, versionsTable, new String[] { null, "1", "5", "v5", "0,1,4" });
        addRecord(datasource, versionsTable, new String[] { null, "1", "6", "v6", "0,1" });

        verifyVersionThreeForDeletesAcrossMultipleVersions();
        verifyVersionFiveForDeletesAcrossMultipleVersions();
        verifyVersionTwoForDeletesAcrossMultipleVersions();
    }

    private void verifyVersionThreeForDeletesAcrossMultipleVersions() throws SQLException {
        Version version = new Version();
        version.setDatasetId(1);
        version.setVersion(3);

        VersionedRecord[] records = reader.fetchAll(version, dataTable, session);

        assertEquals(6, records.length);

        assertEquals(1, records[0].getRecordId());
        assertEquals(2, records[1].getRecordId());
        assertEquals(4, records[2].getRecordId());
        assertEquals(5, records[3].getRecordId());
        assertEquals(7, records[4].getRecordId());
        assertEquals(9, records[5].getRecordId());
    }

    private void verifyVersionFiveForDeletesAcrossMultipleVersions() throws SQLException {
        Version version = new Version();
        version.setDatasetId(1);
        version.setVersion(5);

        VersionedRecord[] records = reader.fetchAll(version, dataTable, session);

        assertEquals(10, records.length);

        assertEquals(1, records[0].getRecordId());
        assertEquals(2, records[1].getRecordId());
        assertEquals(3, records[2].getRecordId());
        assertEquals(4, records[3].getRecordId());
        assertEquals(5, records[4].getRecordId());
        assertEquals(6, records[5].getRecordId());
        assertEquals(7, records[6].getRecordId());
        assertEquals(10, records[7].getRecordId());
        assertEquals(12, records[8].getRecordId());
        assertEquals(13, records[9].getRecordId());
    }

    private void verifyVersionTwoForDeletesAcrossMultipleVersions() throws SQLException {
        Version version = new Version();
        version.setDatasetId(1);
        version.setVersion(2);

        VersionedRecord[] records = reader.fetchAll(version, dataTable, session);

        assertEquals(6, records.length);

        assertEquals(1, records[0].getRecordId());
        assertEquals(2, records[1].getRecordId());
        assertEquals(4, records[2].getRecordId());
        assertEquals(5, records[3].getRecordId());
        assertEquals(7, records[4].getRecordId());
        assertEquals(8, records[5].getRecordId());
    }
}
