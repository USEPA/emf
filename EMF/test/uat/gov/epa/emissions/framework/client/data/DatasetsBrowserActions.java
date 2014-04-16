package gov.epa.emissions.framework.client.data;

import javax.swing.JTable;

import abbot.tester.JTableTester;

import gov.epa.emissions.framework.client.UserAcceptanceTestCase;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.data.dataset.DatasetsBrowserWindow;
import gov.epa.emissions.framework.client.exim.ExportWindow;
import gov.epa.emissions.framework.client.meta.DatasetPropertiesEditor;

public class DatasetsBrowserActions {

    private UserAcceptanceTestCase testcase;

    private EmfConsole console;

    private DatasetsBrowserWindow browser;

    public DatasetsBrowserActions(EmfConsole console, UserAcceptanceTestCase testcase) {
        this.console = console;
        this.testcase = testcase;
    }

    public Object cell(int row, int col) {
        return table().getValueAt(row, col);
    }

    public DatasetsBrowserWindow open() throws Exception {
        testcase.click(console, "manage");
        testcase.click(console, "datasets");

        browser = (DatasetsBrowserWindow) testcase.findInternalFrame(console, "datasetsBrowser");
        return browser;
    }

    public JTable table() {
        return (JTable) testcase.findByName(browser, "datasetsTable");
    }

    public void close() throws Exception {
        testcase.click(browser, "close");
    }

    public void select(int row) throws Exception {
        select(new int[] { row });
    }

    public void select(int[] rows) throws Exception {
        JTable table = refresh();

        JTableTester tester = new JTableTester();
        for (int i = 0; i < rows.length; i++) {
            // 'Select' is 2nd col
            tester.actionSelectCell(table, rows[i], 1);
        }
    }

    public JTable refresh() {
        testcase.click(browser, "refresh");
        return table();
    }

    public ExportWindow export() throws Exception {
        testcase.click(browser, "export");
        return (ExportWindow) testcase.findInternalFrame(console, "exportWindow");
    }

    public void export(int row) throws Exception {
        select(row);
        export();
    }

    public void selectLast() throws Exception {
        select(table().getRowCount() - 1);
    }

    public void selectDataset(String dataset) throws Exception {
        int rows = rowCount();
        for (int i = 0; i < rows; i++) {
            String actualDataset = (String) cell(i, 2);
            if (dataset.equals(actualDataset)) {
                select(i);
                return;
            }
        }
    }

    public int rowCount() {
        return refresh().getRowCount();
    }

    public ExportWindow export(String datasetName) throws Exception {
        selectDataset(datasetName);
        return export();
    }

    public DatasetPropertiesEditor properties(String datasetName) {
        testcase.click(browser, "properties");
        return (DatasetPropertiesEditor) testcase.findInternalFrame(console, "Properties Editor: " + datasetName);
    }

    public void refreshUntilDatasetIsListed(String dataset) {
        for (int i = 0; i < 3000; i += 500) {// loop for 3 secs
            if (isDatasetListed(dataset))
                return;

            refresh();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException("could not wait for browser refresh");
            }// wait for browser refresh
        }
    }

    private boolean isDatasetListed(String dataset) {
        int rowCount = rowCount();
        for (int i = 0; i < rowCount; i++) {
            if (datasetName(i).equals(dataset))
                return true;
        }
        return false;
    }

    private String datasetName(int row) {
        return (String) cell(row, 2);
    }

}
