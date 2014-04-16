package gov.epa.emissions.framework.services.data;

import gov.epa.emissions.commons.db.Datasource;
import gov.epa.emissions.commons.db.Page;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.db.version.VersionedRecord;
import gov.epa.emissions.commons.db.version.Versions;
import gov.epa.emissions.commons.io.DataFormatFactory;
import gov.epa.emissions.commons.io.importer.VersionedDataFormatFactory;
import gov.epa.emissions.commons.io.importer.VersionedImporter;
import gov.epa.emissions.commons.io.orl.ORLOnRoadImporter;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.ServiceTestCase;
import gov.epa.emissions.framework.services.basic.UserDAO;
import gov.epa.emissions.framework.services.editor.DataAccessToken;
import gov.epa.emissions.framework.services.editor.DataViewService;
import gov.epa.emissions.framework.services.editor.DataViewServiceImpl;

import java.io.File;
import java.util.Date;
import java.util.Random;

public class DataViewServiceTest extends ServiceTestCase {

    private DataViewService service;

    private Datasource datasource;

    private EmfDataset dataset;

    private String table;

    private DataAccessToken token;

    protected void doSetUp() throws Exception {
        this.service = new DataViewServiceImpl(emf(), super.dbServer(), sessionFactory(configFile()));

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

        token = token();
        token = service.openSession(token);
    }

    private void doImport() throws Exception {
        Version version = new Version();
        version.setVersion(0);

        File file = new File("test/data/orl/nc", "midsize-onroad.txt");
        DataFormatFactory formatFactory = new VersionedDataFormatFactory(version, dataset);
        ORLOnRoadImporter importer = new ORLOnRoadImporter(file.getParentFile(), new String[] { file.getName() },
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
        service.closeSession(token);
        dropTable(dataset.getName(), datasource);
        dropData("versions", datasource);
    }

    public void testShouldFailWhenAttemptingToViewANonFinalVersion() throws Exception {
        Version v0 = versionZero();
        Versions versions = new Versions();
        Version v1 = versions.derive(v0, "v1", user(), session);

        DataAccessToken token = token(v1);
        try {
            service.openSession(token);
        } catch (EmfException e) {
            return;
        }

        fail("Should have raised an error if attempting to view a non-final version");
    }

    private User user() {
        return new UserDAO().get("emf", session);
    }

    public void testShouldReturnExactlyTwoPages() throws EmfException {
        assertEquals(2, service.getPageCount(token));

        Page page = service.getPage(token, 1);
        assertNotNull("Should be able to get Page 1", page);

        assertEquals(100, page.count());
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

    public void testShouldReturnAtleastOneRecord() throws EmfException {
        int numberOfRecords = service.getTotalRecords(token());
        assertTrue(numberOfRecords >= 1);
    }

    public void testShouldReturnAtLeastOnePage() throws EmfException {
        int numberOfPages = service.getPageCount(token());
        assertTrue(numberOfPages >= 1);
    }

    public void testShouldApplyConstraintsAndReturnFirstPage() throws Exception {
        String rowFilter = "POLL = '100414'";
        String sortOrder = "POLL, ANN_EMIS";
        Page page = service.applyConstraints(token(), rowFilter, sortOrder);

        assertEquals(1, page.getNumber());
        assertEquals(20, page.count());
    }

    private DataAccessToken token() {
        Version version = versionZero();
        return token(version);
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
            if (allRecs[i].getRecordId() == numberOfRecords - 1) {
                found = true;
            }
        }
        assertTrue(found);
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

    private Date lastModifiedDate(File folder, String fileName) {
        return new Date(new File(folder, fileName).lastModified());
    }

}
