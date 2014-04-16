package gov.epa.emissions.framework.client;

import gov.epa.emissions.framework.client.admin.RegisterUserWindow;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.exim.ImportWindow;
import gov.epa.emissions.framework.client.login.LoginPresenter;
import gov.epa.emissions.framework.client.login.LoginWindow;
import gov.epa.emissions.framework.client.transport.RemoteServiceLocator;
import gov.epa.emissions.framework.client.transport.ServiceLocator;
import gov.epa.emissions.framework.ui.MessagePanel;

import java.awt.Component;
import java.awt.Container;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.text.JTextComponent;

import junit.extensions.abbot.ComponentTestFixture;
import abbot.finder.ComponentFinder;
import abbot.finder.Matcher;
import abbot.finder.matchers.NameMatcher;
import abbot.finder.matchers.WindowMatcher;
import abbot.tester.ComponentTester;
import abbot.tester.JComboBoxTester;

//TODO: reorganize EMF-specific vs Abbot-specific methods
public abstract class UserAcceptanceTestCase extends ComponentTestFixture {

    protected RegisterUserWindow gotoRegisterNewUserScreen() {
        LoginWindow window = createLoginWindow();
        showWindow(window);

        click(window, "registerUser");

        return (RegisterUserWindow) findWindow("RegisterUser");
    }

    public void click(Container window, String componentName) {
        Component component = findByName(window, componentName);
        ComponentTester tester = new ComponentTester();

        tester.actionClick(component);
    }

    public LoginWindow createLoginWindow() {
        ServiceLocator serviceLocator = null;
        try {
            serviceLocator = new RemoteServiceLocator("http://localhost:8080/emf/services");
        } catch (Exception e) {
            throw new RuntimeException("could not lookup EMF Services");
        }

        LoginWindow view = new LoginWindow(serviceLocator);
        view.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        LoginPresenter presenter = new LoginPresenter(serviceLocator.userService());
        presenter.display(view);

//        assertEquals("Login to EMF", view.getTitle());
        assertTrue("Login to EMF", view.getTitle().startsWith("Login to"));

        return view;
    }

    public void setText(Container window, String name, String value) {
        JTextComponent field = (JTextComponent) findByName(window, name);
        field.setText(value);
    }

    public String getText(Container window, String name) {
        JTextComponent field = (JTextComponent) findByName(window, name);
        return field.getText();
    }

    public void assertErrorMessage(Container window, String errorMessage) {
        MessagePanel messagePanel = (MessagePanel) findByName(window, "messagePanel");
        assertEquals(errorMessage, messagePanel.getMessage());
    }

    public Component findByName(Container window, String componentName) {
        try {
            return getFinder().find(window, new NameMatcher(componentName));
        } catch (Exception e) {
            throw new RuntimeException("could not find component - " + componentName + " in window - "
                    + window.getName());
        }
    }

    public void assertEmfConsoleShown() {
        EmfConsole console = (EmfConsole) findWindow("EMF Console");
        assertNotNull(console);
        assertTrue(console.isVisible());
    }

    public EmfConsole openConsole() {
        LoginWindow window = createLoginWindow();
        showWindow(window);

        setUsername(window, "emf");
        setPassword(window, "emf12345");

        click(window, "signIn");

        return (EmfConsole) findWindow("EMF Console");
    }

    private void setPassword(LoginWindow window, String password) {
        setText(window, "password", password);
    }

    private void setUsername(LoginWindow window, String username) {
        setText(window, "username", username);
    }

    public Component findWindow(String title) {
        try {
            return getFinder().find(new WindowMatcher(title));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public JInternalFrame findInternalFrame(JFrame frame, final String name) {
        try {
            return (JInternalFrame) getFinder().find(frame, new Matcher() {
                public boolean matches(Component component) {
                    if (!(component instanceof JInternalFrame))
                        return false;

                    JInternalFrame internalFrame = (JInternalFrame) component;
                    return name.equals(internalFrame.getName());
                }
            });
        } catch (Exception e) {
            throw new RuntimeException("could not find internal frame - " + name + " in frame - " + frame.getName());
        }
    }

    public JComboBox findComboBox(Container container, final String name) {
        ComponentFinder finder = getFinder();
        try {
            return (JComboBox) finder.find(container, new Matcher() {
                public boolean matches(Component component) {
                    if (!(component instanceof JComboBox))
                        return false;

                    JComboBox comboBox = (JComboBox) component;
                    return name.equals(comboBox.getName());
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void selectComboBoxItem(ImportWindow window, String comboBoxName, String value) {
        try {
            JComboBox comboBox = findComboBox(window, comboBoxName);
            JComboBoxTester tester = new JComboBoxTester();
            tester.actionSelectItem(comboBox, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
