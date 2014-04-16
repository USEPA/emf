package gov.epa.emissions.framework.services.data;

import gov.epa.emissions.commons.db.DataModifier;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.version.DefaultVersionedRecordsFactory;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.db.version.Versions;
import gov.epa.emissions.commons.io.Column;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.ServiceTestCase;
import gov.epa.emissions.framework.services.basic.UserDAO;
import gov.epa.emissions.framework.services.editor.DataAccessCacheImpl;
import gov.epa.emissions.framework.services.editor.DataAccessToken;
import gov.epa.emissions.framework.services.editor.DataAccessor;
import gov.epa.emissions.framework.services.editor.DataAccessorImpl;
import gov.epa.emissions.framework.services.editor.DefaultVersionedRecordsWriterFactory;
import gov.epa.emissions.framework.services.editor.VersionedRecordsWriterFactory;

public class DataAccessorTest extends ServiceTestCase {

    private DataAccessor accessor;

    public Datasource datasource;

    private User owner;

    private DataAccessToken token;

    private String table;

    protected void doSetUp() throws Exception {
        datasource = emissions();
        accessor = createService();

        owner = new UserDAO().get("emf", session);
        token = new DataAccessToken();
        Version version = createVersionZero();
        token.setVersion(version);

        table = "dataccess_test";
        createDataTable(table);
        token.setTable(table);
    }

    private DataAccessor createService() throws Exception {
        DefaultVersionedRecordsFactory reader = new DefaultVersionedRecordsFactory(datasource);

        VersionedRecordsWriterFactory writerFactory = new DefaultVersionedRecordsWriterFactory();
        DataAccessCacheImpl cache = new DataAccessCacheImpl(reader, writerFactory, datasource, sqlDataTypes());

        return new DataAccessorImpl(cache, sessionFactory(configFile()));
    }

    protected void doTearDown() throws Exception {
        dropData("versions", datasource);
        dropTable(table, datasource);

        accessor.shutdown();
    }

    private void createDataTable(String table) throws Exception {
        Column col1 = new Column("p1", sqlDataTypes().text());
        createVersionedTable(table, emissions(), new Column[] { col1 });
    }

    private Version createVersionZero() throws Exception {
        DataModifier modifier = datasource.dataModifier();
        modifier.insertRow("versions", (new String[] { null, "1", "0", "version zero", "", "true" }));

        return loadVersionZero();
    }

    private Version loadVersionZero() {
        Versions versions = new Versions();
        return versions.get(1, session)[0];
    }

    public void testOpeningEditSessionShouldObtainLock() throws Exception {
        DataAccessToken result = accessor.openEditSession(owner, token);
        assertTrue("Should have obtained lock on opening edit session", result.isLocked(owner));
    }

    public void testShouldFailToOpenSessionIfVersionIsLockedByAnotherUser() throws Exception {
        accessor.openEditSession(owner, token);

        DataAccessToken anotherToken = new DataAccessToken();
        Version version = loadVersionZero();
        anotherToken.setVersion(version);
        anotherToken.setTable(table);

        User user = new UserDAO().get("admin", session);
        DataAccessToken result = accessor.openEditSession(user, anotherToken);
        assertTrue("Version should be locked by owner", result.isLocked(owner));
        assertFalse("Should have failed to obtain lock on opening edit session", result.isLocked(user));
    }

    public void testShouldConfirmLockOwnershipOnIsLocked() throws Exception {
        DataAccessToken result = accessor.openEditSession(owner, token);
        assertTrue("Should have obtained lock on opening edit session", accessor.isLockOwned(result));
    }
    
    public void testShouldExtendLockPeriodOnExtendLock() throws Exception {
        DataAccessToken locked = accessor.openEditSession(owner, token);
        
        DataAccessToken extended = accessor.renewLock(locked);
        assertTrue("Should continue to extend lock", accessor.isLockOwned(extended));
    }

    public void testClosingEditSessionShouldReleaseLock() throws Exception {
        DataAccessToken locked = accessor.openEditSession(owner, token);

        DataAccessToken result = accessor.closeEditSession(owner, locked);
        assertFalse("Should be unlocked on close", result.isLocked(owner));
    }
}
