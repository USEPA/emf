package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.framework.client.UserAcceptanceTestCase;
import gov.epa.emissions.framework.client.login.LoginWindow;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Random;

import abbot.tester.ComponentTester;

public class RegisterNewUserOnStartupTest extends UserAcceptanceTestCase {

    public void testShouldShowRegisterUserOnSelectionOfRegisterNewUserOption() throws Exception {
        RegisterUserWindow window = gotoRegisterNewUserScreen();

        assertNotNull(window);
        assertTrue(window.isVisible());
        assertEquals("Register New User", window.getTitle());
    }

    private boolean isWindowClosed = false;

    public void testShouldShowLoginOnCancel() throws Exception {
        RegisterUserWindow window = gotoRegisterNewUserScreen();

        window.addWindowListener(new WindowAdapter() {
            public void windowClosed(WindowEvent arg0) {
                isWindowClosed = true;
            }
        });

        click(window, "cancel");

        assertTrue("Register window should close on cancel", isWindowClosed);

        assertLoginIsShown();
    }

    protected void assertLoginIsShown() throws Exception {
        LoginWindow login = (LoginWindow) findWindow("Login");
        
        assertNotNull(login);
        assertTrue(login.isVisible());
    }

    public void testShouldShowErrorsOnMissingName() throws Exception {
        RegisterUserWindow window = gotoRegisterNewUserScreen();

        click(window, "ok");

        assertErrorMessage(window, "Name should be specified");
    }

    public void testShouldShowErrorsOnMissingAffiliation() throws Exception {
        RegisterUserWindow window = gotoRegisterNewUserScreen();

        setText(window, "name", "A User");

        click(window, "ok");

        assertErrorMessage(window, "Affiliation should have 2 or more characters");
    }

    public void testShouldShowErrorsOnMissingPhone() throws Exception {
        RegisterUserWindow window = gotoRegisterNewUserScreen();

        setText(window, "name", "A User");
        setText(window, "affiliation", "User's affiliation");

        click(window, "ok");

        assertErrorMessage(window, "Phone should be specified");
    }

    public void testShouldShowErrorsOnMissingEmail() throws Exception {
        RegisterUserWindow window = gotoRegisterNewUserScreen();

        setText(window, "name", "A User");
        setText(window, "affiliation", "User's affiliation");
        setText(window, "phone", "123-123-1234-123");

        click(window, "ok");

        assertErrorMessage(window, "Email should have the format xx@yy.zz");
    }

    public void testShouldShowErrorsOnMissingUsername() throws Exception {
        RegisterUserWindow window = gotoRegisterNewUserScreen();

        setText(window, "name", "A User");
        setText(window, "affiliation", "User's affiliation");
        setText(window, "phone", "123-123-1234-123");
        setText(window, "email", "uat-user@uat.emf.edu");

        click(window, "ok");

        assertErrorMessage(window, "Username should have at least 3 characters");
    }

    public void testShouldShowErrorsOnInvalidUsername() throws Exception {
        RegisterUserWindow window = gotoRegisterNewUserScreen();

        setText(window, "name", "A User");
        setText(window, "affiliation", "User's affiliation");
        setText(window, "phone", "123-123-1234-123");
        setText(window, "email", "uat-user@uat.emf.edu");
        setText(window, "username", "ua");

        click(window, "ok");

        assertErrorMessage(window, "Username should have at least 3 characters");
    }

    public void testShouldShowErrorsOnMissingPassword() throws Exception {
        RegisterUserWindow window = gotoRegisterNewUserScreen();

        setText(window, "name", "A User");
        setText(window, "affiliation", "User's affiliation");
        setText(window, "phone", "123-123-1234-123");
        setText(window, "email", "uat-user@uat.emf.edu");
        setText(window, "username", "uat-emf");

        click(window, "ok");

        assertErrorMessage(window, "Password should have at least 8 characters");
    }

    public void testShouldShowErrorsOnPasswordNotMatchingConfirmPassword() throws Exception {
        RegisterUserWindow window = gotoRegisterNewUserScreen();

        setText(window, "name", "A User");
        setText(window, "affiliation", "User's affiliation");
        setText(window, "phone", "123-123-1234-123");
        setText(window, "email", "uat-user@uat.emf.edu");
        setText(window, "username", "uat-emf");
        setText(window, "password", "uatemf12");

        click(window, "ok");

        assertErrorMessage(window, "Confirm Password should match Password");
    }

    public void testShouldShowEmfConsoleOnSuccessfulRegistration() throws Exception {
        RegisterUserWindow window = gotoRegisterNewUserScreen();

        setText(window, "name", "A User");
        setText(window, "affiliation", "User's affiliation");
        setText(window, "phone", "123-123-1234");
        setText(window, "email", "uat-user@uat.emf.edu");
        setText(window, "username", "uat" + new Random().nextInt());
        setText(window, "password", "uatemf12");
        setText(window, "confirmPassword", "uatemf12");

        window.addWindowListener(new WindowAdapter() {
            public void windowClosed(WindowEvent arg0) {
                isWindowClosed = true;
            }
        });

        click(window, "ok");

        assertTrue(isWindowClosed);
        assertEmfConsoleShown();
    }

    public void TODO_testShouldShowLoginOnWindowClose() throws Exception {
        RegisterUserWindow window = gotoRegisterNewUserScreen();

        window.addWindowListener(new WindowAdapter() {
            public void windowClosed(WindowEvent arg0) {
                isWindowClosed = true;
            }
        });

        close(window);

        assertTrue("Register window should close on closing of window", isWindowClosed);
        assertLoginIsShown();
    }

    private void close(RegisterUserWindow window) {
        ComponentTester tester = new ComponentTester();
        tester.close(window);
    }
}
