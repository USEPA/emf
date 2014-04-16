package gov.epa.emissions.framework.services.editor;

import gov.epa.emissions.commons.db.version.ChangeSet;
import gov.epa.emissions.commons.db.version.ScrollableVersionedRecords;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.db.version.VersionedRecordsFactory;
import gov.epa.emissions.commons.db.version.VersionedRecordsWriter;
import gov.epa.emissions.framework.services.EmfProperties;
import gov.epa.emissions.framework.services.basic.EmfProperty;

import org.hibernate.Session;
import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;

public class DataAccessCacheTest extends MockObjectTestCase {

    private DataAccessCacheImpl cache;

    private Session session;

    protected void setUp() throws Exception {
        super.setUp();

        Mock records = mock(ScrollableVersionedRecords.class);
        records.expects(atLeastOnce()).method("close");

        Mock reader = mock(VersionedRecordsFactory.class);
        reader.stubs().method("optimizedFetch").withAnyArguments().will(returnValue(records.proxy()));

        Mock writer = mock(VersionedRecordsWriter.class);
        writer.expects(once()).method("close");

        Mock writerFactory = mock(VersionedRecordsWriterFactory.class);
        writerFactory.stubs().method("create").withAnyArguments().will(returnValue(writer.proxy()));

        session = null;
        Mock properties = properties();

        cache = new DataAccessCacheImpl((VersionedRecordsFactory) reader.proxy(),
                (VersionedRecordsWriterFactory) writerFactory.proxy(), null, null, (EmfProperties) properties.proxy());
    }

    private Mock properties() {
        Mock properties = mock(EmfProperties.class);

        EmfProperty pageSize = new EmfProperty();
        pageSize.setName("page-size");
        pageSize.setValue("100");
        
        EmfProperty batchSize = new EmfProperty();
        batchSize.setName("batch-size");
        batchSize.setValue("10000");
        
        properties.stubs().method("getProperty").with(eq("page-size"), eq(null)).will(returnValue(pageSize));

        properties.stubs().method("getProperty").with(eq("batch-size"), eq(null)).will(returnValue(batchSize));
        return properties;
    }

    public void testShouldMaintainListOfChangeSetsPerPage() throws Exception {
        DataAccessToken token = new DataAccessToken();
        Version version = new Version();
        version.setDatasetId(2);
        version.setVersion(3);
        token.setVersion(version);

        cache.init(token, session);

        ChangeSet changeset1 = new ChangeSet();
        cache.submitChangeSet(token, changeset1, 1, session);

        ChangeSet changeset2 = new ChangeSet();
        cache.submitChangeSet(token, changeset2, 1, session);

        ChangeSets results = cache.changesets(token, 1, session);
        assertEquals(2, results.size());
        assertEquals(changeset1, results.get(0));
        assertEquals(changeset2, results.get(1));

        cache.close(token, session);
    }

    public void testShouldMaintainSeparateChangeSetListsForEachPage() throws Exception {
        DataAccessToken token = new DataAccessToken();
        Version version = new Version();
        version.setDatasetId(2);
        version.setVersion(3);
        token.setVersion(version);

        cache.init(token, session);

        ChangeSet changeset1 = new ChangeSet();
        cache.submitChangeSet(token, changeset1, 1, session);

        ChangeSet changeset2 = new ChangeSet();
        cache.submitChangeSet(token, changeset2, 2, session);

        ChangeSets resultsPage1 = cache.changesets(token, 1, session);
        assertEquals(1, resultsPage1.size());
        assertEquals(changeset1, resultsPage1.get(0));

        ChangeSets resultsPage2 = cache.changesets(token, 2, session);
        assertEquals(1, resultsPage2.size());
        assertEquals(changeset2, resultsPage2.get(0));

        cache.close(token, session);
    }

    public void testShouldGetChangesetsForAllPagesByPage() throws Exception {
        DataAccessToken token = new DataAccessToken();
        Version version = new Version();
        version.setDatasetId(2);
        version.setVersion(3);
        token.setVersion(version);

        cache.init(token, session);

        ChangeSet changeset1Page1 = new ChangeSet();
        cache.submitChangeSet(token, changeset1Page1, 1, session);

        ChangeSet changesetPage2 = new ChangeSet();
        cache.submitChangeSet(token, changesetPage2, 2, session);

        ChangeSet changeset2Page1 = new ChangeSet();
        cache.submitChangeSet(token, changeset2Page1, 1, session);

        ChangeSets all = cache.changesets(token, session);
        assertEquals(3, all.size());
        assertEquals(changeset1Page1, all.get(0));
        assertEquals(changeset2Page1, all.get(1));
        assertEquals(changesetPage2, all.get(2));

        cache.close(token, session);
    }

    public void testCloseShouldDiscardChangeSetsRelatedToEditToken() throws Exception {
        DataAccessToken token = new DataAccessToken();
        Version version = new Version();
        version.setDatasetId(2);
        version.setVersion(3);
        token.setVersion(version);

        cache.init(token, session);

        ChangeSet changeset1Page1 = new ChangeSet();
        cache.submitChangeSet(token, changeset1Page1, 1, session);

        ChangeSets all = cache.changesets(token, session);
        assertEquals(1, all.size());

        cache.close(token, session);

        ChangeSets empty = cache.changesets(token, session);
        assertEquals(0, empty.size());
    }

    public void testConfirmChangesIfChangeSetContainsUpdates() throws Exception {
        DataAccessToken token = new DataAccessToken();
        Version version = new Version();
        version.setDatasetId(2);
        version.setVersion(3);
        token.setVersion(version);

        cache.init(token, session);

        Mock changeset = mock(ChangeSet.class);
        changeset.stubs().method("hasChanges").will(returnValue(Boolean.TRUE));
        cache.submitChangeSet(token, (ChangeSet) changeset.proxy(), 1, session);
        assertTrue("Should confirm updates if ChangeSet has changes", cache.hasChanges(token, session));

        cache.close(token, session);
        assertFalse("Should not have any changes once Cache is closed", cache.hasChanges(token, session));
    }

    public void testShouldDiscardChangeSetsOfAllPagesOnDiscard() throws Exception {
        DataAccessToken token = new DataAccessToken();
        Version version = new Version();
        version.setDatasetId(2);
        version.setVersion(3);
        token.setVersion(version);

        cache.init(token, session);

        ChangeSet changeset1 = new ChangeSet();
        cache.submitChangeSet(token, changeset1, 1, session);

        ChangeSet changeset2 = new ChangeSet();
        cache.submitChangeSet(token, changeset2, 2, session);

        cache.discardChangeSets(token, session);

        // empty
        ChangeSets resultsPage1 = cache.changesets(token, 1, session);
        assertEquals(0, resultsPage1.size());
        ChangeSets resultsPage2 = cache.changesets(token, 2, session);
        assertEquals(0, resultsPage2.size());

        cache.close(token, session);
    }

}
