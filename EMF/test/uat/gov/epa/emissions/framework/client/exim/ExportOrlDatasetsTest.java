package gov.epa.emissions.framework.client.exim;

import gov.epa.emissions.framework.client.UserAcceptanceTestCase;
import gov.epa.emissions.framework.client.console.ConsoleActions;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.data.DatasetsBrowserActions;
import gov.epa.emissions.framework.services.persistence.ExImDbUpdate;

import java.util.Random;

public class ExportOrlDatasetsTest extends UserAcceptanceTestCase {

    private EmfConsole console;

    private DatasetsBrowserActions browserActions;

    private ConsoleActions consoleActions;

    public void setUp() throws Exception {
        consoleActions = new ConsoleActions(this);
        console = consoleActions.open();

        browserActions = new DatasetsBrowserActions(console, this);
        browserActions.open();
    }

    protected void tearDown() throws Exception {
        consoleActions.close();
        new ExImDbUpdate().deleteAllDatasets();
    }

    public void testShouldExportDatasetToFileOnClickOfExportButton() throws Exception {
        String datasetName = "Test" + new Random().nextInt();
        importOrlNonRoad(datasetName);
        exportOrlNonRoad(datasetName);
    }

    public void testShouldFailToExportIfOverwriteIsUncheckedAndFileAlreadyExists() throws Exception {
        String datasetName = "Test" + new Random().nextInt();
        importOrlNonRoad(datasetName);
        exportOrlNonRoad(datasetName);

        failOnExportDueToOverwrite(datasetName);
    }

    private void failOnExportDueToOverwrite(String datasetName) throws Exception {
        ExportWindow exportWindow = browserActions.export(datasetName);
        ExportActions exportActions = new ExportActions(exportWindow, console, this);

        exportActions.setFolder(System.getProperty("java.io.tmpdir"));
        exportActions.setOverwriteFalse();
        exportActions.clickExport();

        exportActions.assertErrorMessage("Cannot export to existing file.  Choose overwrite option");
    }

    public void testShouldExportMultipleSelectedDatasetsToFilesOnClickOfExportButton() throws Exception {
        String dataset1 = "Test" + new Random().nextInt();
        String dataset2 = "Test" + new Random().nextInt();
        String folder = System.getProperty("java.io.tmpdir");

        int preImportTotal = browserActions.rowCount();
        importOrlNonRoad(dataset1);
        importOrlNonRoad(dataset2);

        int postImportTotal = browserActions.rowCount();
        assertEquals(preImportTotal, postImportTotal - 2);

        browserActions.select(new int[] { postImportTotal - 2, postImportTotal - 1 });
        ExportActions exportActions = new ExportActions(browserActions.export(), console, this);
        exportActions.export(folder);
    }

    private void importOrlNonRoad(String datasetName) throws Exception {
        ImportActions importActions = new ImportActions(console, this);
        importActions.open();
        importActions.importOrlNonRoad(datasetName);
        importActions.done();
    }

    private void exportOrlNonRoad(String datasetName) throws Exception {
        ExportWindow exportWindow = browserActions.export(datasetName);
        ExportActions exportActions = new ExportActions(exportWindow, console, this);

        exportActions.export(System.getProperty("java.io.tmpdir"));
    }

}
