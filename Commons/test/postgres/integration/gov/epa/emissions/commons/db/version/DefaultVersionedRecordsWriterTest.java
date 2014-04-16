package gov.epa.emissions.commons.db.version;

import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.security.User;

import java.sql.SQLException;

public class DefaultVersionedRecordsWriterTest extends VersionedRecordsTestCase {

    private VersionedRecordsWriter writer;

    private Versions versions;

    protected void setUp() throws Exception {
        super.setUp();

        setupVersionZero(datasource, versionsTable);
        setupVersionZeroData(datasource, dataTable);

        writer = new DefaultVersionedRecordsWriter(datasource, dataTable);
        versions = new Versions();
    }

    protected void doTearDown() throws Exception {
        writer.close();
        super.doTearDown();
    }

    private void setupVersionZero(Datasource datasource, String table) throws SQLException {
        addRecord(datasource, table, new String[] { null, "1", "0", "ver 0", "", "true" });
    }

    private void setupVersionZeroData(Datasource datasource, String table) throws SQLException {
        addRecord(datasource, table, new String[] { null, "1", "0", null, "p1", "p2" });// 1
        addRecord(datasource, table, new String[] { null, "1", "0", null, "p21", "p22" });// 2
        addRecord(datasource, table, new String[] { null, "1", "0", null, "p31", "p32" });// 3
        addRecord(datasource, table, new String[] { null, "1", "0", null, "p41", "p42" });// 4
        addRecord(datasource, table, new String[] { null, "1", "0", null, "p51", "p52" });// 5
    }

    public void testChangeSetWithTwoUpdatesInGivenVersion() throws Exception {
        ChangeSet changeset = new ChangeSet();

        Version baseVersion = versions.get(1, 0, session);
        Version versionOne = versions.derive(baseVersion, "version one", user(), session);
        changeset.setVersion(versionOne);

        DefaultVersionedRecordsFactory reader = new DefaultVersionedRecordsFactory(datasource);
        VersionedRecord[] records = reader.fetchAll(baseVersion, dataTable, session);
        assertEquals(5, records.length);

        changeset.addUpdated(records[0]);
        changeset.addUpdated(records[1]);

        writer.update(changeset);

        Version version = versions.get(1, versionOne.getVersion(), session);
        assertEquals(1, version.getVersion());
    }
    

    public void testUpdate() throws Exception {
        ChangeSet changeset = new ChangeSet();

        Version versionZero = versions.get(1, 0, session);
        Version versionOne = versions.derive(versionZero, "version one", user(), session);
        changeset.setVersion(versionOne);

        DefaultVersionedRecordsFactory reader = new DefaultVersionedRecordsFactory(datasource);
        VersionedRecord[] records = reader.fetchAll(versionZero, dataTable, session);
        assertEquals(5, records.length);

        changeset.addUpdated(records[0]);
        changeset.addUpdated(records[1]);

        writer.update(changeset);

        Version version = versions.get(1, 1, session);
        assertEquals(1, version.getVersion());
        assertFalse("Should me marked as Final", version.isFinalVersion());

        VersionedRecord[] versionOneRecords = reader.fetchAll(version, dataTable, session);
        for (int i = 0; i < versionOneRecords.length; i++)
            assertEquals(3, versionOneRecords[i].getTokens().length);

        assertEquals("p1", versionOneRecords[3].token(0));
        assertEquals("p2", versionOneRecords[3].token(1));
        assertEquals("p21", versionOneRecords[4].token(0));
        assertEquals("p22", versionOneRecords[4].token(1));
    }

    public void testShouldDeleteExistingRecordAndAddNewRecordOnUpdate() throws Exception {
        // version one (based on version zero): 4 deleted, add new 6 & 7
        ChangeSet changeSetForVersionOne = new ChangeSet();

        Version versionZero = versions.get(1, 0, session);
        Version versionOne = versions.derive(versionZero, "version one", user(), session);
        changeSetForVersionOne.setVersion(versionOne);

        DefaultVersionedRecordsFactory reader = new DefaultVersionedRecordsFactory(datasource);
        VersionedRecord[] versionZeroRecords = reader.fetchAll(versionZero, dataTable, session);

        changeSetForVersionOne.addDeleted(versionZeroRecords[3]);// delete 4
        VersionedRecord record6 = new VersionedRecord();
        record6.setDatasetId(1);
        changeSetForVersionOne.addNew(record6);

        VersionedRecord record7 = new VersionedRecord();
        record7.setDatasetId(1);
        changeSetForVersionOne.addNew(record7);

        writer.update(changeSetForVersionOne);
        versions.markFinal(versionOne, session);

        // version two (based on version zero): update 3, add (new) 8
        ChangeSet changeSetForVersionTwo = new ChangeSet();
        Version versionTwo = versions.derive(versionZero, "version two", user(), session);
        changeSetForVersionTwo.setVersion(versionTwo);

        VersionedRecord record3 = versionZeroRecords[2];
        changeSetForVersionTwo.addUpdated(record3);// Xt 3

        VersionedRecord record8 = new VersionedRecord();
        record8.setDatasetId(1);
        changeSetForVersionTwo.addNew(record8);

        // Verify update of 2 -> delete 3, add (new)9. Verify 8 added.
        writer.update(changeSetForVersionTwo);

        VersionedRecord[] versionTwoRecords = reader.fetchAll(versionTwo, dataTable, session);
        assertEquals(6, versionTwoRecords.length);

        int start = versionTwoRecords[0].getRecordId();
        assertEquals(3 + start, versionTwoRecords[2].getRecordId());
        assertEquals("1", versionTwoRecords[2].getDeleteVersions());

        assertEquals(7 + start, versionTwoRecords[4].getRecordId());
        assertEquals("", versionTwoRecords[4].getDeleteVersions());

        assertEquals(8 + start, versionTwoRecords[5].getRecordId());
        assertEquals("", versionTwoRecords[5].getDeleteVersions());
    }

    public void testChangeSetWithAllUpdatesInGivenVersion() throws Exception {
        ChangeSet changeset = new ChangeSet();

        Version versionZero = versions.get(1, 0, session);
        Version versionOne = versions.derive(versionZero, "version one", user(), session);
        changeset.setVersion(versionOne);

        DefaultVersionedRecordsFactory reader = new DefaultVersionedRecordsFactory(datasource);
        VersionedRecord[] records = reader.fetchAll(versionZero, dataTable, session);
        assertTrue(records.length == 5);

        // update all records in the base version
        for (int i = 0; i < records.length; i++)
            changeset.addUpdated(records[i]);

        writer.update(changeset);

        Version version = versions.get(1, versionOne.getVersion(), session);
        assertEquals(1, version.getVersion());
    }

    public void testChangeSetWithNewRecordsResultsInNewVersion() throws Exception {
        Version versionZero = versions.get(1, 0, session);
        Version versionOne = versions.derive(versionZero, "version one", user(), session);

        ChangeSet changeset = new ChangeSet();
        changeset.setVersion(versionOne);

        VersionedRecord record6 = new VersionedRecord();
        record6.setDatasetId(1);
        changeset.addNew(record6);

        VersionedRecord record7 = new VersionedRecord();
        record7.setDatasetId(1);
        changeset.addNew(record7);

        writer.update(changeset);
        Version version = versions.get(1, versionOne.getVersion(), session);
        assertNotNull("Should return version of changeset", version);
        assertEquals(1, version.getVersion());

        DefaultVersionedRecordsFactory reader = new DefaultVersionedRecordsFactory(datasource);
        VersionedRecord[] records = reader.fetchAll(version, dataTable, session);
        assertEquals(7, records.length);
        int init = records[0].getRecordId();
        for (int i = 1; i < records.length; i++) {
            assertEquals(++init, records[i].getRecordId());
        }
    }

    public void testChangeSetWithRecordsDeleteShouldResultInNewVersionWithoutThoseRecords() throws Exception {
        DefaultVersionedRecordsFactory reader = new DefaultVersionedRecordsFactory(datasource);

        Version versionZero = versions.get(1, 0, session);
        Version versionOne = versions.derive(versionZero, "version one", user(), session);

        ChangeSet changeset = new ChangeSet();
        changeset.setVersion(versionOne);

        VersionedRecord[] records = reader.fetchAll(versionZero, dataTable, session);
        changeset.addDeleted(records[1]);// delete record 2

        VersionedRecord record6 = new VersionedRecord();
        record6.setDatasetId(1);
        changeset.addNew(record6); // add record 6

        writer.update(changeset);

        Version version = versions.get(1, versionOne.getVersion(), session);
        assertNotNull("Should return version of changeset", version);
        assertEquals(1, version.getVersion());

        VersionedRecord[] versionOneRecords = reader.fetchAll(version, dataTable, session);
        assertEquals(5, versionOneRecords.length);
        // deleted record 2
        int init = versionOneRecords[0].getRecordId();
        assertEquals(init + 2, versionOneRecords[1].getRecordId());
        assertEquals(init + 3, versionOneRecords[2].getRecordId());
        assertEquals(init + 4, versionOneRecords[3].getRecordId());
        assertEquals(init + 5, versionOneRecords[4].getRecordId());
    }

    public void testChangeSetWithAddedAndDeletedRecords() throws Exception {
        DefaultVersionedRecordsFactory reader = new DefaultVersionedRecordsFactory(datasource);

        Version versionZero = versions.get(1, 0, session);
        Version versionOne = versions.derive(versionZero, "version one", user(), session);

        ChangeSet changeset = new ChangeSet();
        changeset.setVersion(versionOne);

        VersionedRecord[] records = reader.fetchAll(versionZero, dataTable, session);
        changeset.addDeleted(records[1]);// record 2

        writer.update(changeset);

        Version version = versions.get(1, versionOne.getVersion(), session);
        assertNotNull("Should return version of changeset", version);
        assertEquals(1, version.getVersion());

        VersionedRecord[] versionOneRecords = reader.fetchAll(version, dataTable, session);
        assertEquals(4, versionOneRecords.length);
        // deleted record 2
        int init = versionOneRecords[0].getRecordId();
        assertEquals(init + 2, versionOneRecords[1].getRecordId());
        assertEquals(init + 3, versionOneRecords[2].getRecordId());
        assertEquals(init + 4, versionOneRecords[3].getRecordId());
    }

    private User user() {
        User user = new User();
        user.setUsername("emf");
        user.setPassword("emf12345");
        user.setAffiliation("IE CEMPD");
        user.setAdmin(false);
        user.setEmail("abc@abc.com");
        user.setEncryptedPassword("7Dq/pgn1VOcABL644L59KlfI/eo=");
        user.setName("emf");
        user.setPhone("919-123-4567");
        user.setId(2); //To pretend to be the 'emf' user in emf.users table
        return user;
    }

}
