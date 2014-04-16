package gov.epa.emissions.framework.services.data;

import gov.epa.emissions.commons.db.DataModifier;
import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.DbUpdate;
import gov.epa.emissions.commons.db.postgres.PostgresDbUpdate;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.db.version.Versions;
import gov.epa.emissions.commons.io.DataFormatFactory;
import gov.epa.emissions.commons.io.importer.VersionedDataFormatFactory;
import gov.epa.emissions.commons.io.importer.VersionedImporter;
import gov.epa.emissions.commons.io.orl.ORLNonPointImporter;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.ServiceTestCase;
import gov.epa.emissions.framework.services.basic.UserDAO;
import gov.epa.emissions.framework.services.basic.UserServiceImpl;
import gov.epa.emissions.framework.services.editor.DataAccessToken;
import gov.epa.emissions.framework.services.editor.DataEditorService;
import gov.epa.emissions.framework.services.editor.DataEditorServiceImpl;

import java.io.File;
import java.util.Date;
import java.util.Random;

public class DataEditorService_VersionsTest extends ServiceTestCase {

    private DataEditorService service;

    private Datasource datasource;

    private EmfDataset dataset;

    private String table;

    private DataAccessToken token;

    private UserServiceImpl userService;

    protected void doSetUp() throws Exception {
        service = new DataEditorServiceImpl(emf(), super.dbServer(), sessionFactory(configFile()));
        userService = new UserServiceImpl(sessionFactory(configFile()));

        datasource = emissions();

        dataset = new EmfDataset();
        table = "test" + new Date().getTime();
        
        String newName = table;
        if ( newName != null) {
            newName = newName.trim();
        } else {
            throw new EmfException("Dataset name is null");
        }
        dataset.setName(newName);
        
        setTestValues(dataset);

        doImport();

        Version v1 = new Versions().derive(versionZero(), "v1", user(), session);
        token = token(v1);
        service.openSession(userService.getUser("emf"), token);
    }

    private User user() {
        return new UserDAO().get("emf", session);
    }

    private void doImport() throws Exception {
        Version version = new Version();
        version.setVersion(0);

        File file = new File("test/data/orl/nc", "very-small-nonpoint.txt");
        DataFormatFactory formatFactory = new VersionedDataFormatFactory(version, dataset);
        ORLNonPointImporter importer = new ORLNonPointImporter(file.getParentFile(), new String[] { file.getName() },
                dataset, getDbServerInstance(), sqlDataTypes(), formatFactory);
        new VersionedImporter(importer, dataset, getDbServerInstance(), lastModifiedDate(file.getParentFile(), file.getName()))
                .run();
    }

    private void setTestValues(EmfDataset dataset) {
        dataset.setId(Math.abs(new Random().nextInt()));
        dataset.setCreator("tester");
        dataset.setCreatedDateTime(new Date());
        dataset.setModifiedDateTime(new Date());
        dataset.setAccessedDateTime(new Date());
    }

    protected void doTearDown() throws Exception {
        service.closeSession(new UserDAO().get("emf", session), token);

        DbUpdate dbUpdate = new PostgresDbUpdate(datasource.getConnection());
        dbUpdate.dropTable(datasource.getName(), dataset.getName());

        DataModifier modifier = datasource.dataModifier();
        modifier.dropAllData("versions");
    }

    private DataAccessToken token(Version version) {
        return token(version, dataset.getName());
    }

    private DataAccessToken token(Version version, String table) {
        return new DataAccessToken(version, table);
    }

    private Version versionZero() {
        Versions versions = new Versions();
        return versions.get(dataset.getId(), 0, session);
    }

    private Version derived() {
        Versions versions = new Versions();
        return versions.get(dataset.getId(), 1, session);
    }

    public void testShouldHaveVersionZeroAfterDatasetImport() throws Exception {
        Version[] versions = service.getVersions(dataset.getId());

        assertNotNull("Should return versions of imported dataset", versions);
        assertEquals(2, versions.length);

        Version versionZero = versions[0];
        assertEquals(0, versionZero.getVersion());
        assertEquals(dataset.getId(), versionZero.getDatasetId());
    }

    public void testShouldDeriveVersionFromAFinalVersion() throws Exception {
        Version[] versions = service.getVersions(dataset.getId());

        Version versionZero = versions[0];
        Version derived = service.derive(versionZero, user(), "v 1");

        assertNotNull("Should be able to derive from a Final version", derived);
        assertEquals(versionZero.getDatasetId(), derived.getDatasetId());
        assertEquals(2, derived.getVersion());
        assertEquals("0", derived.getPath());
        assertFalse("Derived version should be non-final", derived.isFinalVersion());
    }

    public void testShouldBeAbleToMarkADerivedVersionAsFinalAfterObtainingLockOnVersion() throws Exception {
        Version versionZero = versionZero();
        Version derived = service.derive(versionZero, user(), "v2");
        assertEquals(versionZero.getDatasetId(), derived.getDatasetId());
        assertEquals("v2", derived.getName());

        DataAccessToken tokenDerived = token(derived);
        Version finalVersion = service.markFinal(tokenDerived);

        assertNotNull("Should be able to mark a 'derived' as a Final version", derived);
        assertEquals(derived.getDatasetId(), finalVersion.getDatasetId());
        assertEquals(derived.getVersion(), finalVersion.getVersion());
        assertEquals("0", finalVersion.getPath());
        assertTrue("Derived version should be final on being marked 'final'", finalVersion.isFinalVersion());

        Version[] updated = service.getVersions(dataset.getId());
        assertEquals(3, updated.length);
        assertEquals("v2", updated[2].getName());
        assertTrue("Derived version (loaded from db) should be final on being marked 'final'", updated[2]
                .isFinalVersion());
    }

    public void testShouldRaiseErrorOnMarkFinalIfItIsLockedByAnotherUser() throws Exception {
        DataAccessToken tokenDerived = token(derived());
        try {
            service.markFinal(tokenDerived);
        } catch (EmfException e) {
            return;
        }

        fail("Should have raised an error as the version is locked by another user");
    }

    private Date lastModifiedDate(File folder, String fileName) {
        return new Date(new File(folder, fileName).lastModified());
    }

}
