package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.commons.db.postgres.PostgresDbUpdate;
import gov.epa.emissions.framework.client.UserAcceptanceTestCase;
import gov.epa.emissions.framework.client.console.ConsoleActions;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.data.DatasetsBrowserActions;
import gov.epa.emissions.framework.client.exim.ImportActions;

import java.util.Random;

public class PropertiesEditorTest extends UserAcceptanceTestCase {

    private EmfConsole console;

    private ConsoleActions consoleActions;

    private DatasetsBrowserActions browserActions;

    private String dataset;

    protected void setUp() throws Exception {
        consoleActions = new ConsoleActions(this);
        console = consoleActions.open();
        browserActions = new DatasetsBrowserActions(console, this);
        browserActions.open();

        dataset = "UAT-" + new Random().nextInt();
        doImport(dataset);
    }

    protected void tearDown() throws Exception {
        consoleActions.close();
        new PostgresDbUpdate().deleteAll("emf.datasets");
    }

    public void testShouldDisplayPropertiesOfSelectedDataset() throws Exception {
        browserActions.selectLast();

        DatasetPropertiesEditor props = browserActions.properties(dataset);
        assertNotNull("Properties Editor should be opened", props);

        PropertiesEditorActions editorActions = new PropertiesEditorActions(props, this);
        String[] tabs = editorActions.tabs();

        assertEquals(5, tabs.length);
        assertEquals("Summary", tabs[0]);
        assertEquals("Data", tabs[1]);
        assertEquals("Keywords", tabs[2]);
        assertEquals("Logs", tabs[3]);
        assertEquals("Info", tabs[4]);
    }

    public void testShouldDisplaySummaryPropertiesOnSummaryTab() throws Exception {
        browserActions.selectLast();

        DatasetPropertiesEditor props = browserActions.properties(dataset);
        assertNotNull("Properties Editor should be opened", props);

        PropertiesEditorActions editorActions = new PropertiesEditorActions(props, this);
        SummaryTabActions tabActions = editorActions.summary();

        // overview section
        assertEquals(dataset, tabActions.name());
        String description = tabActions.description();
        assertTrue(description.startsWith("Created from file EMSHAP99d.txt provided by M. Strum in September 2002."));
        assertNull(tabActions.project());
        assertEquals("EMF", tabActions.creator());
        assertEquals("ORL Nonroad Inventory", tabActions.datasetType());

        // time & space section
        // TODO: need to assert against useful date values
        assertNotNull(tabActions.startDateTime());
        assertNotNull(tabActions.endDateTime());
        assertEquals("Annual", tabActions.temporalResolution());
        assertNull(tabActions.sector());
        assertNotNull(tabActions.region());
        assertEquals("US", tabActions.country());

        // status & access dates section
        assertEquals("Imported", tabActions.status());
        // TODO: need to assert against useful date values
        assertNotNull(tabActions.lastModifiedDate());
        assertNotNull(tabActions.lastAccessedDate());
        assertNotNull(tabActions.creationDate());
    }

    private void doImport(String datasetName) throws Exception {
        ImportActions importActions = new ImportActions(console, this);
        importActions.open();
        importActions.importOrlNonRoad(datasetName);
        importActions.done();

        browserActions.refreshUntilDatasetIsListed(datasetName);
    }

}
