package gov.epa.emissions.framework.client.exim;

import gov.epa.emissions.framework.client.UserAcceptanceTestCase;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.console.StatusActions;

import java.io.File;

import junit.framework.Assert;

public class ImportActions {

    private EmfConsole console;

    private UserAcceptanceTestCase testcase;

    private ImportWindow importWindow;

    private StatusActions statusActions;

    public ImportActions(EmfConsole console, UserAcceptanceTestCase testcase) {
        this.console = console;
        this.testcase = testcase;

        statusActions = new StatusActions(console, testcase);
    }

    public void importOrlNonRoad(String name) {
        doImport("ORL Nonroad Inventory", name, "arinv.nonroad.nti99d_NC.new.txt");
    }

    public void importOrlNonPoint(String name) {
        doImport("ORL Nonpoint Inventory", name, "arinv.nonpoint.nti99_NC.txt");
    }

    public void importOrlPoint(String name) {
        doImport("ORL Point Inventory", name, "ptinv.nti99_NC.txt");
    }

    public void importOrlOnRoadMobile(String name) {
        doImport("ORL Onroad Inventory", name, "nti99.NC.onroad.SMOKE.txt");
    }

    public ImportWindow open() throws Exception {
        testcase.click(console, "file");
        testcase.click(console, "import");

        importWindow = find();
        return importWindow;
    }

    public void doImport(String type, String name, String filename) {
        testcase.selectComboBoxItem(importWindow, "datasetTypes", type);
        testcase.setText(importWindow, "name", name);

        File userDir = new File(System.getProperty("user.dir"));
        String pathToFile = "test/data/orl/nc/";
        File repository = new File(userDir, pathToFile);
        testcase.setText(importWindow, "folder", repository.getAbsolutePath());

        testcase.setText(importWindow, "filename", filename);

        testcase.click(importWindow, "import");

        confirmImportCompletion(12000, type, filename);
    }

    private void confirmImportCompletion(long waitTime, String type, String filename) {
        for (int i = 0; i < waitTime; i += 500) {
            if (statusActions.hasStartedImport(type, filename) && statusActions.hasCompletedImport(type, filename))
                return;// success
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        Assert.fail("Did not find any completed status message after polling for " + waitTime + " msecs.");
    }

    public void done() throws Exception {
        testcase.click(importWindow, "done");
    }

    public ImportWindow find() throws Exception {
        return (ImportWindow) testcase.findInternalFrame(console, "importWindow");
    }

    public void selectDatasetType(String value) throws Exception {
        testcase.selectComboBoxItem(importWindow, "datasetTypes", value);
    }

    public void clickImport() throws Exception {
        testcase.click(importWindow, "import");
    }

    public void setName(String name) throws Exception {
        testcase.setText(importWindow, "name", name);
    }

    public void setFolder(String folder) throws Exception {
        testcase.setText(importWindow, "folder", folder);
    }

}
