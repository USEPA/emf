package gov.epa.emissions.framework.client.login;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.MessageDialog;
import gov.epa.emissions.commons.gui.buttons.CancelButton;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.DefaultEmfSession;
import gov.epa.emissions.framework.client.EmfFrame;
import gov.epa.emissions.framework.client.Label;
import gov.epa.emissions.framework.client.admin.PostRegisterStrategy;
import gov.epa.emissions.framework.client.admin.RegisterUserPresenter;
import gov.epa.emissions.framework.client.admin.RegisterUserWindow;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.console.EmfConsolePresenter;
import gov.epa.emissions.framework.client.transport.ServiceLocator;
import gov.epa.emissions.framework.client.util.ComponentUtility;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.EmfProperty;
import gov.epa.emissions.framework.services.basic.FailedLoginAttemptException;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;
import gov.epa.mims.analysisengine.gui.ScreenUtils;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Date;
import java.util.concurrent.ExecutionException;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;

import org.apache.commons.lang3.StringUtils;

public class LoginWindow extends EmfFrame implements LoginView {

    private JTextField username;

    private JPasswordField password;

    private LoginPresenter presenter;

    private MessagePanel messagePanel;

    private ServiceLocator serviceLocator;

    public final static String EMF_VERSION = "v4.3 - 04/04/2024";

    private String ssoUrl;

    public LoginWindow(ServiceLocator serviceLocator) {
        super("Login", "Login to EMF [" + EMF_VERSION + "]");
        this.serviceLocator = serviceLocator;
        try {   //get SSO UJrl if there is one...
            this.ssoUrl = serviceLocator.userService().getPropertyValue(EmfProperty.SSO_URL);
        } catch (EmfException e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
        }

        JPanel layoutPanel = createLayout();

        this.setSize(new Dimension(500, 225));
        this.setLocation(ScreenUtils.getPointToCenter(this));

        this.getContentPane().add(layoutPanel);
    }

    private JPanel createLayout() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        messagePanel = new SingleLineMessagePanel();
        panel.add(messagePanel);
        JPanel loginPanel = createLoginPanel();
        panel.add(loginPanel);
        JPanel buttonsPanel = createButtonsPanel();
        panel.add(buttonsPanel);
        panel.add(new JSeparator(SwingConstants.HORIZONTAL));
        JPanel loginOptionsPanel = createLoginOptionsPanel();
        panel.add(loginOptionsPanel);

        return panel;
    }

    public void display() {
        try {
            System.out.println("Verifying server version is "+EMF_VERSION);
            if (!presenter.checkEmfVersion(EMF_VERSION) && toUpdate() == JOptionPane.YES_OPTION)
                disposeView();
            else
            {
                System.out.println("Showing Login Window");
                if (StringUtils.isBlank(ssoUrl)) {
                    username.setText(presenter.userName());
                    password.setText(presenter.userPassword());
                }
                super.display();
            }
        } catch (Exception e) {
            // need to print this because message panel may not exist yet
            String message = "The EMF client was not able to contact the server due to this error: \n\n";
            if (e.getMessage().contains("UserService"))
                 message = message + "The EMF application is not properly deployed in Tomcat\n";
            else if (e.getMessage().contains("Services are unavailable"))
                 message = message +"The EMF services are unavailable through Tomcat on the specified server";
            else
                 message = message + e.getMessage();
            JOptionPane.showMessageDialog(this, message, "Error Starting the EMF Client",  
                    JOptionPane.ERROR_MESSAGE);
            System.out.println(e.getMessage());
            e.printStackTrace();
            if (messagePanel != null)
               messagePanel.setError(e.getMessage());
        }
    }

    @Override
    public void ssoLogin() {
        if (StringUtils.isNotBlank(ssoUrl)) {
            
            messagePanel.setMessage("Logging in via EPA Single Sign-On using PIV Card...");
        
            //long running methods.....
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            ComponentUtility.enableComponents(this, false);
//            messagePanel.setEnabled(true);
    
            //Instances of javax.swing.SwingWorker are not reusuable, so
            //we create new instances as needed.
            class SSOLoginTask extends SwingWorker<User, Void> {
                
                private Container parentContainer;
                private Exception ex;
    
                public SSOLoginTask(Container parentContainer) {
                    this.parentContainer = parentContainer;
                }
        
                /*
                 * Main task. Executed in background thread.
                 * don't update gui here
                 */
                @Override
                public User doInBackground() throws EmfException  {
                    return presenter.doSSOLogin();
                }
    
                /*
                 * Executed in event dispatching thread
                 */
                @Override
                public void done() {
                    try {
                        User user = get();
                        messagePanel.clear();
                        toExpire(user);
                        ((EmfFrame)parentContainer).refreshLayout();
                        launchConsole(user);
//                        disposeView();
                    } catch (InterruptedException | ExecutionException | EmfException e1) {
                        messagePanel.setError("");
                        messagePanel.setMessage("");
                        ex = e1;
                    } finally {
                        if (ex != null)
                            ComponentUtility.enableComponents(this.parentContainer, true);
                        this.parentContainer.setCursor(null); //turn off the wait cursor
                    }
                }
            };
            new SSOLoginTask(this).execute();
        }
    }
    
    private int toUpdate() {
        String message = "<html>An updated version of the EMF client exists (" + 
                  presenter.getUpdatedEmfVersion() + ").<br>"
                + "Would you like to stop logging in so that you can update<br>your client using the Installer?</html>";

        System.out.println("Showing confirm dialog");
        return JOptionPane.showConfirmDialog(this, new Label("", message), "Warning", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
    }

    private JPanel createButtonsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel container = new JPanel();
        FlowLayout layout = new FlowLayout();
        layout.setHgap(20);
        layout.setVgap(15);
        container.setLayout(layout);

        Button signIn = new Button("Log In", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doSignIn();
            }
        });
        signIn.setMnemonic(KeyEvent.VK_I);
        container.add(signIn);
        setDefaultButton(signIn);

        JButton cancel = new CancelButton(new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                disposeView();
            }
        });
        container.add(cancel);

        panel.add(container, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createSSOPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel container = new JPanel();
        FlowLayout layout = new FlowLayout();
        layout.setHgap(20);
        layout.setVgap(15);
        container.setLayout(layout);

        JLabel label = new JLabel("Logging in via EPA Single Sign-On using PIV Card");
        container.add(label);

        panel.add(container, BorderLayout.CENTER);

        return panel;
    }

    private void doSignIn() {
        if (presenter == null)
            return;
        try {
            User user = presenter.doLogin(username.getText(), new String(password.getPassword()));
            messagePanel.clear();
            toExpire(user);
            super.refreshLayout();
            launchConsole(user);
        } catch (FailedLoginAttemptException e) {
            messagePanel.setError(e.getMessage());
            MessageDialog messageDialog = new MessageDialog(e.getMessage(), "Warning", this);
            messageDialog.prompt();
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }
    }
    
    private int toExpire(User user) throws EmfException {
        int difInDays = (int) (((new Date()).getTime() - (user.getPasswordResetDate()).getTime())/(1000*60*60*24));
        Integer expireDays=0;
        try {
            expireDays = difInDays - presenter.getEffectiveDays();
        } catch (EmfException e) {
            throw new EmfException(e.getMessage());
        }
        if ( expireDays > 0) {
            throw new EmfException("Password has expired.  Reset Password.");
        }
//        else if ( Math.abs(expireDays)<5 ){   
//            String message = " Password is to expire in " + 
//            Math.abs(expireDays) + " days.\n"
//            + "Please reset your password using profile manager. ";
//            System.out.println("Showing confirm dialog");
//            return JOptionPane.showConfirmDialog(this, message, "Warning", JOptionPane.CLOSED_OPTION, JOptionPane.WARNING_MESSAGE);
//        }
        return 0; 
    }  

    private void launchConsole(User user) throws EmfException {
        messagePanel.setMessage("Launching Main EMF Console...");
        
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        ComponentUtility.enableComponents(this, false);

        //Instances of javax.swing.SwingWorker are not reusuable, so
        //we create new instances as needed.
        class LaunchConsoleTask extends SwingWorker<Void, Void> {
            
            private Container parentContainer;

            public LaunchConsoleTask(Container parentContainer) {
                this.parentContainer = parentContainer;
            }
    
            /*
             * Main task. Executed in background thread.
             * don't update gui here
             */
            @Override
            public Void doInBackground() throws EmfException {
                EmfConsole console = new EmfConsole(new DefaultEmfSession(user, serviceLocator));
                EmfConsolePresenter presenter = new EmfConsolePresenter();
                presenter.display(console);
                return null;
            }

            /*
             * Executed in event dispatching thread
             */
            @Override
            public void done() {
                try {
                    get();
                    disposeView();
                } catch (InterruptedException e1) {
                    // ignore
                } catch (ExecutionException e1) {
                    // ignore
                } finally {
                    ComponentUtility.enableComponents(parentContainer, true);
                    this.parentContainer.setCursor(null); //turn off the wait cursor
                }
            }
        };
        new LaunchConsoleTask(this).execute();
    }

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel();

        GridLayout labelsLayoutManager = new GridLayout(2, 1);
        labelsLayoutManager.setVgap(15);
        JPanel labelsPanel = new JPanel(labelsLayoutManager);
        final JLabel usernameLabel = new JLabel("EMF Username");
        labelsPanel.add(usernameLabel);
        final JLabel passwordLabel = new JLabel("EMF Password");
        labelsPanel.add(passwordLabel);

        panel.add(labelsPanel);

        GridLayout valuesLayoutManager = new GridLayout(2, 1);
        valuesLayoutManager.setVgap(10);
        JPanel valuesPanel = new JPanel(valuesLayoutManager);

        username = new JTextField(10);
        username.setName("username");
        username.setToolTipText("EMF Username");

        usernameLabel.setLabelFor(username);
        usernameLabel.setToolTipText(username.getToolTipText());

        valuesPanel.add(username);
        password = new JPasswordField(10);
        password.setName("password");
        password.setToolTipText("EMF Password");

        passwordLabel.setLabelFor(password);
        passwordLabel.setToolTipText(password.getToolTipText());

        valuesPanel.add(password);

        panel.add(valuesPanel);

        return panel;
    }

    private JPanel createLoginOptionsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        JButton forgotPassword = new Button("Reset Password", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doResetPassword();
            }
        });
        forgotPassword.setToolTipText("Reset password");
        JPanel forgotPasswordPanel = new JPanel(new BorderLayout());
        forgotPasswordPanel.add(forgotPassword);
        forgotPassword.setMnemonic(KeyEvent.VK_P);

        JButton register = new Button("Register New User", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doRegisterNewUser();
            }
        });
        register.setToolTipText("Register as a new user");
        register.setMnemonic(KeyEvent.VK_R);

        JPanel registerPanel = new JPanel(new BorderLayout());
        registerPanel.add(register);

        panel.add(registerPanel, BorderLayout.WEST);
        panel.add(forgotPasswordPanel, BorderLayout.EAST);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        return panel;
    }

    private void doRegisterNewUser() {
        try {
            launchRegisterUser();
        } catch (Exception ex) {
            messagePanel.setError(ex.getMessage());
            return;
        }
        disposeView();
    }
    
    private void doResetPassword() {
        try {
            User user = presenter.doLogin(username.getText(), new String(password.getPassword()));
            launchResetUser(user);
        } catch (Exception ex) {
            messagePanel.setError(ex.getMessage());
            return;
        }
        disposeView();
    }

    private void launchRegisterUser() throws Exception {
        PostRegisterStrategy strategy = new LaunchEmfConsolePostRegisterStrategy(serviceLocator);
        RegisterUserWindow view = new RegisterUserWindow(serviceLocator, strategy);

        RegisterUserPresenter presenter = new RegisterUserPresenter(serviceLocator.userService());
        presenter.display(view);
    }
    
    private void launchResetUser(User user) throws Exception {
        PostRegisterStrategy strategy = new LaunchEmfConsolePostRegisterStrategy(serviceLocator);
        RegisterUserWindow view = new RegisterUserWindow(serviceLocator, strategy, user);

        RegisterUserPresenter presenter = new RegisterUserPresenter(serviceLocator.userService());
        presenter.display(view);
    }

    public void observe(LoginPresenter presenter) {
        this.presenter = presenter;
    }

}
