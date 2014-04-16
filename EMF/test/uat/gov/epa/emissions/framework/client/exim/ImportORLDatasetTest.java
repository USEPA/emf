package gov.epa.emissions.framework.client.exim;

import gov.epa.emissions.commons.db.postgres.PostgresDbUpdate;
import gov.epa.emissions.framework.client.UserAcceptanceTestCase;
import gov.epa.emissions.framework.client.console.ConsoleActions;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.console.StatusActions;

import java.util.Random;

import javax.swing.JComboBox;
import javax.swing.JInternalFrame;
import javax.swing.JList;
import javax.swing.ListModel;

import abbot.tester.JComboBoxTester;

public class ImportORLDatasetTest extends UserAcceptanceTestCase {

    public interface OrlImportAction {
        void run(String name);
    }

    private ImportWindow importWindow;

    private EmfConsole console;

    private ImportActions importActions;

    private StatusActions statusActions;

    private ConsoleActions consoleActions;

    protected void setUp() throws Exception {
        consoleActions = new ConsoleActions(this);
        console = consoleActions.open();

        importActions = new ImportActions(console, this);
        importWindow = importActions.open();
        assertNotNull(importWindow);

        statusActions = new StatusActions(console, this);
    }

    public void tearDown() throws Exception {
        importActions.done();

        JInternalFrame importWindow = importActions.find();
        assertFalse("Import Window should be hidden from view", importWindow.isVisible());

        consoleActions.close();
    }

    public void testShouldShowAtleastFourORLDatasetTypesAsOptions() throws Exception {
        JComboBox comboBox = findComboBox(importWindow, "datasetTypes");

        assertNotNull(comboBox);

        ListModel model = findComboBoxList(comboBox);
        assertTrue("Should have atleast 4 ORL types", model.getSize() >= 4);
    }

    public void testShouldImportORLNonRoad() throws Exception {
        doImport(datasetName("ORLNonroadInventory"), new OrlImportAction() {
            public void run(String name) {
                importActions.importOrlNonRoad(name);
            }
        });
    }

    public void testShouldImportORLNonPoint() throws Exception {
        doImport(datasetName("ORL NonPoint Inventory"), new OrlImportAction() {
            public void run(String name) {
                importActions.importOrlNonPoint(name);
            }
        });
    }

    public void testShouldImportORLPoint() throws Exception {
        doImport(datasetName("ORL Point Inventory"), new OrlImportAction() {
            public void run(String name) {
                importActions.importOrlPoint(name);
            }
        });
    }

    public void testShouldImportORLOnRoadMobile() throws Exception {
        doImport(datasetName("ORL Onroad Inventory"), new OrlImportAction() {
            public void run(String name) {
                importActions.importOrlOnRoadMobile(name);
            }
        });
    }

    public void testShouldFailIfImportIsAttemptedWithDuplicateName() throws Exception {
        String name = "ORL Onroad Inventory" + " UAT - " + new Random().nextInt();
        importActions.doImport("ORL Onroad Inventory", name, "nti99.NC.onroad.SMOKE.txt");

        importActions.doImport("ORL Onroad Inventory", name, "nti99.NC.onroad.SMOKE.txt");
        // TODO: assert failure - check the status messages
    }

    private void doImport(String name, OrlImportAction action) throws Exception {
        try {
            action.run(name);
            assertEquals(2, statusActions.messageCount());
        } finally {
            new PostgresDbUpdate().delete("emf.datasets", "name", name);
        }
    }

    private String datasetName(String value) {
        return value + new Random().nextInt();
    }

    public void testShouldShowErrorMessageIfNameIsUnspecified() throws Exception {
        importActions.selectDatasetType("ORL Point Inventory");

        importActions.clickImport();

        assertErrorMessage(importWindow, "Dataset Name should be specified");
    }

    public void testShouldShowErrorMessageIfFilenameIsUnspecified() throws Exception {
        importActions.selectDatasetType("ORL Point Inventory");
        importActions.setName(" UAT - " + new Random().nextInt());
        importActions.setFolder("/folder/name");

        importActions.clickImport();

        assertErrorMessage(importWindow, "Filename should be specified");
    }

    protected ListModel findComboBoxList(JComboBox comboBox) {
        JComboBoxTester tester = new JComboBoxTester();
        tester.actionClick(comboBox);

        JList options = tester.findComboList(comboBox);
        return options.getModel();
    }

}
