package gov.epa.emissions.framework.client.console;

import gov.epa.emissions.framework.client.UserAcceptanceTestCase;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.status.StatusWindow;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JTable;

import junit.framework.Assert;

public class StatusActions {

    private UserAcceptanceTestCase testcase;

    private EmfConsole console;

    public StatusActions(EmfConsole console, UserAcceptanceTestCase testcase) {
        this.console = console;
        this.testcase = testcase;
    }

    public StatusWindow window() {
        return (StatusWindow) testcase.findInternalFrame(console, "statusWindow");
    }

    public void clear() {
        testcase.click(window(), "clear");
    }

    private JTable table() {
        return (JTable) testcase.findByName(window(), "statusMessages");
    }

    public int messageCount() {
        return table().getRowCount();
    }

    public List filter(String suffix) {
        JTable table = table();
        List messages = new ArrayList();
        for (int i = 0; i < messageCount(); i++) {
            String message = (String) table.getValueAt(i, 1);
            if (contains(message, suffix))
                messages.add(message);
        }

        return messages;
    }

    private boolean contains(String message, String suffix) {
        return message.indexOf(suffix) >= 0;
    }

    public boolean hasStartedImport(String type, String filename) {
        return doesContain("Started import for " + type + ":" + filename);
    }

    public boolean hasCompletedImport(String type, String filename) {
        return doesContain("Completed import for " + type + ":" + filename);
    }

    private boolean doesContain(String message) {
        List messages = filter(message);
        return !messages.isEmpty();
    }

    public void confirmExportCompletion(long waitTime, String dataset) {
        for (int i = 0; i < waitTime; i += 500) {
            if (isExportComplete(dataset))
                return;// success
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        Assert.fail("Did not find completed status message after polling for " + waitTime + " msecs.");
    }

    private boolean isExportComplete(String dataset) {
        return doesContain("Started exporting " + dataset) && doesContain("Completed export of " + dataset);
    }
}
