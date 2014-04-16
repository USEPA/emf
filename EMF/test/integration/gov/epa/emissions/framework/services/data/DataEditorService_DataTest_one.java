package gov.epa.emissions.framework.services.data;

import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.Page;
import gov.epa.emissions.commons.db.version.ChangeSet;
import gov.epa.emissions.commons.db.version.DefaultVersionedRecordsFactory;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.db.version.VersionedRecord;
import gov.epa.emissions.commons.db.version.VersionedRecordsFactory;
import gov.epa.emissions.commons.db.version.Versions;
import gov.epa.emissions.commons.io.DataFormatFactory;
import gov.epa.emissions.commons.io.importer.Importer;
import gov.epa.emissions.commons.io.importer.VersionedDataFormatFactory;
import gov.epa.emissions.commons.io.importer.VersionedImporter;
import gov.epa.emissions.commons.io.orl.ORLOnRoadImporter;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.ServiceTestCase;
import gov.epa.emissions.framework.services.basic.EmfProperty;
import gov.epa.emissions.framework.services.basic.UserDAO;
import gov.epa.emissions.framework.services.basic.UserService;
import gov.epa.emissions.framework.services.basic.UserServiceImpl;
import gov.epa.emissions.framework.services.editor.DataAccessToken;
import gov.epa.emissions.framework.services.editor.DataEditorServiceImpl;
import gov.epa.emissions.framework.services.persistence.EmfPropertiesDAO;

import java.io.File;
import java.util.Date;
import java.util.Random;

public class DataEditorService_DataTest_one extends ServiceTestCase {

    private DataEditorServiceImpl service;

    private Datasource datasource;

    private EmfDataset dataset;

    private String table;

    private DataAccessToken token;

    private User user;

    protected void doSetUp() throws Exception {
        service = new DataEditorServiceImpl(emf(), super.dbServer(), sessionFactory(configFile()));
        UserServiceImpl userService = new UserServiceImpl(sessionFactory(configFile()));

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
        addDataset();

        doImport(dataset);

        Versions versions = new Versions();
        Version v1 = versions.derive(versionZero(), "v1", user, session);
        openSession(userService, v1);
    }

    private void openSession(UserService userService, Version v1) throws EmfException {
        token = token(v1);
        user = userService.getUser("emf");
        token = service.openSession(user, token, 5);
    }

    private void doImport(EmfDataset dataset) throws Exception {
        Version version = new Version();
        version.setVersion(0);

        File file = new File("test/data/orl/nc", "onroad-15records.txt");
        DataFormatFactory formatFactory = new VersionedDataFormatFactory(version, dataset);
        Importer importer = new ORLOnRoadImporter(file.getParentFile(), new String[] { file.getName() }, dataset,
                getDbServerInstance(), sqlDataTypes(), formatFactory);
        new VersionedImporter(importer, dataset, getDbServerInstance(), lastModifiedDate(file.getParentFile(), file.getName())).run();
    }

    private void setTestValues(EmfDataset dataset) {
        dataset.setId(Math.abs(new Random().nextInt()));
        dataset.setCreator("tester");
        dataset.setCreatedDateTime(new Date());
        dataset.setModifiedDateTime(new Date());
        dataset.setAccessedDateTime(new Date());
    }

    protected void doTearDown() throws Exception {
        service.closeSession(user, token);
        dropTable(dataset.getName(), datasource);
        dropData("versions", datasource);
    }

    public void testTokenContainLockStartAndEndInfoOnOpeningSession() {
        assertNotNull("Lock Start Date should be set on opening of session", token.lockStart());
        assertNotNull("Lock End Date should be set on opening of session", token.lockEnd());

        Date expectedStart = token.getVersion().getLockDate();
        assertEquals(expectedStart, token.lockStart());

        EmfProperty timeInterval = new EmfPropertiesDAO().getProperty("lock.time-interval", session);
        Date expectedEnd = new Date(expectedStart.getTime() + Long.parseLong(timeInterval.getValue()));
        assertEquals(expectedEnd, token.lockEnd());
    }

    public void testShouldReturnExactlyThreePages() throws EmfException {
        assertEquals(3, service.getPageCount(token));

        Page page = service.getPage(token, 1);
        assertNotNull("Should be able to get Page 1", page);

        assertEquals(5, page.count());
        VersionedRecord[] records = page.getRecords();
        assertEquals(page.count(), records.length);
        for (int i = 0; i < records.length; i++) {
            assertEquals(token.datasetId(), records[i].getDatasetId());
            assertEquals(0, records[i].getVersion());
        }

        int recordId = records[0].getRecordId();
        for (int i = 1; i < records.length; i++) {
            assertEquals(++recordId, records[i].getRecordId());
        }
    }

    public void testShouldFailWhenAttemptingToEditAFinalVersion() throws Exception {
        Version v0 = versionZero();
        DataAccessToken token = token(v0);
        try {
            service.openSession(user, token);
        } catch (EmfException e) {
            return;
        }

        fail("Should have raised an error if attempting to edit a final version");
    }

    public void testShouldReturnAtleastOneRecord() throws EmfException {
        int numberOfRecords = service.getTotalRecords(token);
        assertTrue(numberOfRecords >= 1);
    }

    public void testShouldReturnAtLeastOnePage() throws EmfException {
        int numberOfPages = service.getPageCount(token);
        assertTrue(numberOfPages >= 1);
    }

    public void testShouldApplyConstraintsAndReturnFirstPage() throws Exception {
        String rowFilter = "POLL = '100414'";
        String sortOrder = "POLL, ANN_EMIS";
        Page page = service.applyConstraints(token, rowFilter, sortOrder);

        assertEquals(1, page.getNumber());
        assertEquals(2, page.count());
    }

    private DataAccessToken token(Version version) {
        return token(version, dataset.getName());
    }

    private DataAccessToken token(Version version, String table) {
        DataAccessToken result = new DataAccessToken(version, table);

        return result;
    }

    private Version versionZero() {
        Versions versions = new Versions();
        return versions.get(dataset.getId(), 0, session);
    }

    /**
     * This test gets a page using an integer record ID. The resulting collection that is acquired from the page should
     * contain the record with record id that was supplied.
     */
    public void testShouldReturnOnlyOnePage() throws EmfException {
        int numberOfRecords = service.getTotalRecords(token);

        Page page = service.getPageWithRecord(token, numberOfRecords - 1);
        VersionedRecord[] allRecs = page.getRecords();

        boolean found = false;
        for (int i = 0; i < allRecs.length; i++) {
            int recordId = allRecs[i].getRecordId();
            if (recordId == numberOfRecords - 1) {
                found = true;
            }
        }
        assertTrue("Could not look up Page by Record Number", found);
    }

    public void testShouldReturnNoPage() throws EmfException {
        int numberOfRecords = service.getTotalRecords(token);

        Page page = service.getPageWithRecord(token, numberOfRecords + 1);
        VersionedRecord[] allRecs = page.getRecords();
        boolean found = false;

        for (int i = 0; i < allRecs.length; i++) {
            if (allRecs[i].getRecordId() == numberOfRecords + 1) {
                found = true;
            }
        }
        assertTrue(!found);
    }

    public void testNewRecordsAreSavedOnSave() throws Exception {
        int oldNumRecords = token.getVersion().getNumberRecords();
        Version v1 = versionOne();

        ChangeSet changeset = new ChangeSet();
        changeset.setVersion(v1);

        VersionedRecord record6 = new VersionedRecord();
        record6.setDatasetId(dataset.getId());
        changeset.addNew(record6);

        VersionedRecord record7 = new VersionedRecord();
        record7.setDatasetId(dataset.getId());
        changeset.addNew(record7);

        VersionedRecord record8 = new VersionedRecord();
        record8.setDatasetId(dataset.getId());
        changeset.addNew(record8);

        service.submit(token, changeset, 1);
        service.save(token, dataset, v1);

        DefaultVersionedRecordsFactory reader = new DefaultVersionedRecordsFactory(datasource);
        int v0RecordsCount = reader.fetch(versionZero(), dataset.getName(), session).total();

        assertEquals(v0RecordsCount + 3, reader.fetch(v1, dataset.getName(), session).total());
    }

    private void addDataset() {
        UserDAO userDAO = new UserDAO();
        User owner = userDAO.get("emf", session);
        dataset.setCreator(owner.getUsername());

        dataset.setModifiedDateTime(new Date());
        add(dataset);
    }

    public void testLockRenewedOnSave() throws Exception {
        int oldNumRecords = token.getVersion().getNumberRecords();
        Version v1 = versionOne();

        ChangeSet changeset = new ChangeSet();
        changeset.setVersion(v1);

        VersionedRecord record6 = new VersionedRecord();
        record6.setDatasetId(dataset.getId());
        changeset.addNew(record6);

        service.submit(token, changeset, 1);
        DataAccessToken saved = service.save(token, dataset, v1);
        assertTrue("Should renew lock on save", saved.isLocked(user));
    }

    private Version versionOne() throws EmfException {
        Version[] versions = service.getVersions(dataset.getId());
        return versions[1];
    }

    public void testShouldBeAbleToSubmitMultipleChangeSetsForSameVersion() throws Exception {
        int oldNumRecords = token.getVersion().getNumberRecords();
        Version v1 = versionOne();

        ChangeSet changeset1 = new ChangeSet();
        changeset1.setVersion(v1);
        VersionedRecord record6 = new VersionedRecord();
        record6.setDatasetId(dataset.getId());
        changeset1.addNew(record6);
        service.submit(token, changeset1, 1);

        ChangeSet changeset2 = new ChangeSet();
        changeset2.setVersion(v1);
        VersionedRecord record7 = new VersionedRecord();
        record7.setDatasetId(dataset.getId());
        changeset2.addNew(record7);
        service.submit(token, changeset2, 1);

        service.save(token, dataset, v1);

        VersionedRecordsFactory reader = new DefaultVersionedRecordsFactory(datasource);
        int v0RecordsCount = reader.fetch(versionZero(), dataset.getName(), session).total();

        int v1Count = reader.fetch(v1, dataset.getName(), session).total();
        assertEquals(v0RecordsCount + 2, v1Count);
    }

    public void testShouldDiscardChangesOnDiscard() throws Exception {
        Version v1 = versionOne();

        VersionedRecordsFactory reader = new DefaultVersionedRecordsFactory(datasource);
        int v1Count = reader.fetch(v1, dataset.getName(), session).total();

        DataAccessToken token = token(v1, table);

        ChangeSet changeset1 = new ChangeSet();
        changeset1.setVersion(v1);
        VersionedRecord record6 = new VersionedRecord();
        record6.setDatasetId(dataset.getId());
        changeset1.addNew(record6);
        service.submit(token, changeset1, 1);

        service.discard(token);

        int postDiscardCount = reader.fetch(v1, dataset.getName(), session).total();
        assertEquals(v1Count, postDiscardCount);
    }

    public void testShouldConfirmWithYesIfChangesExist() throws Exception {
        Version v1 = versionOne();
        DataAccessToken token = token(v1, table);

        ChangeSet changeset = new ChangeSet();
        changeset.setVersion(v1);
        VersionedRecord record6 = new VersionedRecord();
        record6.setDatasetId(dataset.getId());
        changeset.addNew(record6);
        service.submit(token, changeset, 1);

        assertTrue("Should confirm with Yes if session contains changes", service.hasChanges(token));
    }

    public void testShouldConfirmWithNoIfChangesDontExist() throws Exception {
        Version v1 = versionOne();
        DataAccessToken token = token(v1, table);

        assertFalse("Should confirm with No if session does not contains changes", service.hasChanges(token));
    }

    private Date lastModifiedDate(File folder, String fileName) {
        return new Date(new File(folder, fileName).lastModified());
    }

}
