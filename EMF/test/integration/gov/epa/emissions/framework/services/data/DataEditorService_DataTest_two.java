package gov.epa.emissions.framework.services.data;

import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.Page;
import gov.epa.emissions.commons.db.version.ChangeSet;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.db.version.VersionedRecord;
import gov.epa.emissions.commons.db.version.Versions;
import gov.epa.emissions.commons.io.DataFormatFactory;
import gov.epa.emissions.commons.io.importer.Importer;
import gov.epa.emissions.commons.io.importer.VersionedDataFormatFactory;
import gov.epa.emissions.commons.io.importer.VersionedImporter;
import gov.epa.emissions.commons.io.orl.ORLOnRoadImporter;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.ServiceTestCase;
import gov.epa.emissions.framework.services.basic.UserDAO;
import gov.epa.emissions.framework.services.basic.UserService;
import gov.epa.emissions.framework.services.basic.UserServiceImpl;
import gov.epa.emissions.framework.services.editor.DataAccessToken;
import gov.epa.emissions.framework.services.editor.DataEditorServiceImpl;

import java.io.File;
import java.util.Date;
import java.util.Random;

public class DataEditorService_DataTest_two extends ServiceTestCase {

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
        new VersionedImporter(importer, dataset, getDbServerInstance(), lastModifiedDate(file.getParentFile(),file.getName())).run();
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

    private void addDataset() {
        UserDAO userDAO = new UserDAO();
        User owner = userDAO.get("emf", session);
        dataset.setCreator(owner.getUsername());

        dataset.setModifiedDateTime(new Date());
        add(dataset);
    }

    private Version versionOne() throws EmfException {
        Version[] versions = service.getVersions(dataset.getId());
        return versions[1];
    }
                
    public void testShouldAddNewRecordsInChangeSetToPageOnRepeatFetchOfSamePage() throws Exception {
        Version v1 = versionOne();

        Page page = service.getPage(token, 1);

        ChangeSet changeset = new ChangeSet();
        changeset.setVersion(v1);

        VersionedRecord record6 = new VersionedRecord(10);
        record6.setDatasetId(dataset.getId());
        changeset.addNew(record6);

        service.submit(token, changeset, 1);

        // random page browsing
        assertEquals(3, service.getPageCount(token));
        service.getPage(token, 2);
        service.getPage(token, 1);
        service.getPage(token, 2);

        Page result = service.getPage(token, 1);
        VersionedRecord[] records = result.getRecords();
        assertEquals(page.count() + 1, records.length);

        VersionedRecord[] page1Records = page.getRecords();
        for (int i = 0; i < page1Records.length; i++)
            assertEquals(page1Records[i].getRecordId(), records[i].getRecordId());
        assertEquals(record6.getRecordId(), records[records.length - 1].getRecordId());
    }

    public void testTotalRecordsShouldIncludeUncommitedChanges() throws Exception {
        Version v1 = versionOne();

        ChangeSet changeset = new ChangeSet();
        changeset.setVersion(v1);

        VersionedRecord record6 = new VersionedRecord(10);
        record6.setDatasetId(dataset.getId());
        changeset.addNew(record6);

        int totalPriorToChanges = service.getTotalRecords(token);
        service.submit(token, changeset, 1);

        assertEquals(totalPriorToChanges + 1, service.getTotalRecords(token));
    }

    public void testShouldApplyChangeSetToPageOnRepeatFetchOfSamePage() throws Exception {
        Version v1 = versionOne();

        Page page = service.getPage(token, 1);

        ChangeSet changeset = new ChangeSet();
        changeset.setVersion(v1);

        VersionedRecord record6 = new VersionedRecord(10);
        record6.setDatasetId(dataset.getId());
        changeset.addNew(record6);

        VersionedRecord[] page1Records = page.getRecords();
        changeset.addDeleted(page1Records[2]);

        service.submit(token, changeset, 1);

        // random page browsing
        assertEquals(3, service.getPageCount(token));
        service.getPage(token, 2);
        service.getPage(token, 0);
        service.getPage(token, 1);

        Page result = service.getPage(token, 1);
        VersionedRecord[] records = result.getRecords();
        assertEquals(page.count(), records.length);

        assertEquals(page1Records[0].getRecordId(), records[0].getRecordId());
        assertEquals(page1Records[1].getRecordId(), records[1].getRecordId());
        // record 2 deleted from Page 1
        for (int i = 3; i < page1Records.length; i++)
            assertEquals(page1Records[i].getRecordId(), records[i - 1].getRecordId());
        assertEquals(record6.getRecordId(), records[records.length - 1].getRecordId());
    }

    public void testShouldApplyChangeSetToMultiplePages() throws Exception {
        Version v1 = versionOne();

        DataAccessToken token = token(v1, table);
        service.openSession(user, token);

        // page 1 changes
        ChangeSet page1ChangeSet = new ChangeSet();
        page1ChangeSet.setVersion(v1);
        VersionedRecord record6 = new VersionedRecord(10);
        record6.setDatasetId(dataset.getId());
        page1ChangeSet.addNew(record6);
        Page page1 = service.getPage(token, 1);
        service.submit(token, page1ChangeSet, 1);

        // random page browsing
        assertEquals(3, service.getPageCount(token));
        service.getPage(token, 0);
        service.getPage(token, 1);

        // page 0 changes
        Page page2 = service.getPage(token, 2);
        VersionedRecord[] page4Records = page2.getRecords();
        ChangeSet page2ChangeSet = new ChangeSet();
        page2ChangeSet.setVersion(v1);
        page2ChangeSet.addDeleted(page4Records[2]);
        service.submit(token, page2ChangeSet, 2);

        Page page1AfterChanges = service.getPage(token, 1);
        assertEquals(page1.count() + 1, page1AfterChanges.count());

        Page page2AfterChanges = service.getPage(token, 2);
        assertEquals(page2.count() - 1, page2AfterChanges.count());
    }
             
    public void testShouldSaveChangeSetAndRegeneratePagesAfterSave() throws Exception {
        int oldNumRecords = token.getVersion().getNumberRecords();
        Version v1 = versionOne();

        DataAccessToken token = token(v1, table);
        service.openSession(user, token);

        // page 1 changes
        ChangeSet page1ChangeSet = new ChangeSet();
        page1ChangeSet.setVersion(v1);
        VersionedRecord newRecord = new VersionedRecord(10);
        newRecord.setDatasetId(dataset.getId());
        page1ChangeSet.addNew(newRecord);
        service.submit(token, page1ChangeSet, 1);

        int recordsBeforeSave = service.getTotalRecords(token);
        service.save(token, dataset, v1);

        assertEquals(recordsBeforeSave, service.getTotalRecords(token));
    }
    
    private Date lastModifiedDate(File folder, String fileName) {
        return new Date(new File(folder,fileName).lastModified());
    }

}
