package gov.epa.emissions.framework.client.console;

import gov.epa.emissions.framework.client.UserAcceptanceTestCase;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.login.LoginWindow;
import gov.epa.emissions.framework.client.status.StatusWindow;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;

public class EmfConsoleTest extends UserAcceptanceTestCase {

    private boolean isWindowClosed = false;

    private EmfConsole window;

    protected void setUp() throws Exception {
        window = openConsole();

        window.addWindowListener(new WindowAdapter() {
            public void windowClosed(WindowEvent arg0) {
                isWindowClosed = true;
            }
        });
    }

    protected void tearDown() {
        window.disposeView();
    }

    public void testShouldShowLoginOnLogout() throws Exception {
        click(window, "file");
        click(window, "logout");

        assertTrue(isWindowClosed);

        LoginWindow login = (LoginWindow) findWindow("Login");
        assertNotNull(login);
        assertTrue(login.isVisible());
    }

    // TODO: idled, since the App exits and brings the test to a halt !
    public void TODO_testShouldTerminateEmfOnExit() throws Exception {
        click(window, "file");
        click(window, "exit");

        assertTrue(isWindowClosed);

        assertNull(findWindow("Login"));
        assertNull(findWindow("EMF Console"));
    }

    public void testShouldShowStatusOnLogin() throws Exception {
        StatusActions actions = new StatusActions(window, this);
        StatusWindow status = actions.window();
        assertNotNull(status);

        JButton refreshButton = (JButton) findByName(status, "clear");
        assertNotNull(refreshButton);
    }

}
