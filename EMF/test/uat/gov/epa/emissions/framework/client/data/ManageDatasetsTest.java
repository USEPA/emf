package gov.epa.emissions.framework.client.data;

import gov.epa.emissions.commons.db.postgres.PostgresDbUpdate;
import gov.epa.emissions.framework.client.UserAcceptanceTestCase;
import gov.epa.emissions.framework.client.console.ConsoleActions;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.data.dataset.DatasetsBrowserWindow;
import gov.epa.emissions.framework.client.exim.ImportActions;

import java.util.Random;

import javax.swing.JTable;

public class ManageDatasetsTest extends UserAcceptanceTestCase {

    private EmfConsole console;

    private DatasetsBrowserActions browserActions;

    private ConsoleActions consoleActions;

    protected void setUp() {
        consoleActions = new ConsoleActions(this);
        console = consoleActions.open();
        browserActions = new DatasetsBrowserActions(console, this);
    }

    protected void tearDown() throws Exception {
        consoleActions.close();
        new PostgresDbUpdate().deleteAll("emf.datasets");
    }

    public void testShouldDisplayImportedDatasets() throws Exception {
        String dataset = "UAT-" + new Random().nextInt();
        doImport(dataset);

        DatasetsBrowserWindow browser = browserActions.open();
        assertNotNull("browser should have been opened", browser);

        JTable table = browserActions.table();
        assertNotNull("datasets table should be displayed", table);

        assertEquals(dataset, browserActions.cell(browserActions.rowCount() - 1, 2));
    }

    public void testShouldCloseWindowOnClose() throws Exception {
        browserActions.open();
        browserActions.close();

        try {
            findInternalFrame(console, "datasetsBrowser");
        } catch (Exception e) {
            return;
        }

        fail("Datasets Browser should not be present and displayed on Close");
    }

    public void testShouldDisplayExportWindowOnClickOfExportButton() throws Exception {
        String datasetName = "UAT-" + new Random().nextInt();
        doImport(datasetName);

        browserActions.open();

        browserActions.select(0);
        browserActions.export();

        findByName(console, "exportWindow");
    }

    private void doImport(String datasetName) throws Exception {
        ImportActions importActions = new ImportActions(console, this);
        importActions.open();
        importActions.importOrlNonRoad(datasetName);
        importActions.done();
    }

}
