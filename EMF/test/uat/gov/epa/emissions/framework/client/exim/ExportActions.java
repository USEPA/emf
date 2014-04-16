package gov.epa.emissions.framework.client.exim;

import gov.epa.emissions.framework.client.UserAcceptanceTestCase;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.console.StatusActions;
import gov.epa.emissions.framework.ui.MessagePanel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import junit.framework.Assert;

public class ExportActions {

    private UserAcceptanceTestCase testcase;

    private ExportWindow exportWindow;

    private StatusActions statusActions;

    public ExportActions(ExportWindow exportWindow, EmfConsole console, UserAcceptanceTestCase testcase) {
        this.exportWindow = exportWindow;
        this.testcase = testcase;
        statusActions = new StatusActions(console, testcase);
    }

    public void setFolder(String folder) {
        testcase.setText(exportWindow, "folder", folder);
    }

    public void clickExport() {
        testcase.click(exportWindow, "export");
    }

    public void setOverwriteFalse() throws Exception {
        testcase.click(exportWindow, "overwrite");// by default, it's true
    }

    public void assertErrorMessage(String error) throws Exception {
        MessagePanel panel = (MessagePanel) testcase.findByName(exportWindow, "messagePanel");
        Assert.assertEquals(error, panel.getMessage());
    }

    public void setPurpose(String purpose) {
        testcase.setText(exportWindow, "purpose", purpose);
    }

    public void export(String folder) {
        setFolder(folder);
        setPurpose("Testing export...");

        clickExport();

        String datasetsList = testcase.getText(exportWindow, "datasets");
        String[] datasets = toArray(datasetsList);
        for (int i = 0; i < datasets.length; i++) {
            statusActions.confirmExportCompletion(8000, datasets[i]);
            assertExportedFileExists(datasets[i], folder);
        }
    }

    private String[] toArray(String datasetsList) {
        List array = new ArrayList();
        StringTokenizer tokenizer = new StringTokenizer(datasetsList, ", ");
        while (tokenizer.hasMoreTokens()) {
            array.add(tokenizer.nextToken());
        }

        return (String[]) array.toArray(new String[0]);
    }

    public void assertExportedFileExists(String dataset, String folder) {
        File file = file(dataset, folder);
        Assert.assertTrue("Should have exported dataset '" + dataset + "' to file - " + file.getAbsolutePath(), file
                .exists());
        file.deleteOnExit();
    }

    public File file(String datasetName, String folder) {
        String filename = datasetName.replace(' ', '_') + ".txt";
        return new File(folder, filename);
    }
}
