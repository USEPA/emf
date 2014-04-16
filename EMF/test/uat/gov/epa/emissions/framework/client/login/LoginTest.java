package gov.epa.emissions.framework.client.login;

import gov.epa.emissions.framework.client.UserAcceptanceTestCase;
import gov.epa.emissions.framework.client.admin.RegisterUserWindow;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class LoginTest extends UserAcceptanceTestCase {

    private boolean isWindowClosed = false;

    public void testShouldCloseOnClickCancel() throws Exception {
        LoginWindow window = createLoginWindow();
        showWindow(window);

        window.addWindowListener(new WindowAdapter() {
            public void windowClosed(WindowEvent arg0) {
                isWindowClosed = true;
            }
        });

        click(window, "cancel");

        assertTrue(isWindowClosed);
    }

    public void testShouldShowEmfConsoleOnLogin() throws Exception {
        LoginWindow window = createLoginWindow();
        showWindow(window);

        window.addWindowListener(new WindowAdapter() {
            public void windowClosed(WindowEvent arg0) {
                isWindowClosed = true;
            }
        });

        setUsername(window, "emf");
        setPassword(window, "emf12345");

        click(window, "signIn");

        assertTrue(isWindowClosed);
        assertEmfConsoleShown();
    }

    public void testShouldShowRegisterUserOnSelectionOfRegisterNewUserOption() throws Exception {
        LoginWindow window = createLoginWindow();
        showWindow(window);

        window.addWindowListener(new WindowAdapter() {
            public void windowClosed(WindowEvent arg0) {
                isWindowClosed = true;
            }
        });

        click(window, "registerUser");

        assertTrue(isWindowClosed);

        RegisterUserWindow registerUser = (RegisterUserWindow) findWindow("RegisterUser");
        assertNotNull(registerUser);
        assertTrue(registerUser.isVisible());
    }

    private void setPassword(LoginWindow window, String password) throws Exception {
        setText(window, "password", password);
    }

    private void setUsername(LoginWindow window, String username) throws Exception {
        setText(window, "username", username);
    }

    public void testShouldShowErrorMessageOnInvalidUsername() throws Exception {
        LoginWindow window = createLoginWindow();
        showWindow(window);

        setUsername(window, "invalid username");

        click(window, "signIn");

        assertErrorMessage(window, "Invalid username");
    }

    public void testShouldShowErrorMessageOnInvalidPassword() throws Exception {
        LoginWindow window = createLoginWindow();
        showWindow(window);

        setUsername(window, "emf");
        setPassword(window, "invalid password");

        click(window, "signIn");

        assertErrorMessage(window, "Incorrect Password");
    }

}
