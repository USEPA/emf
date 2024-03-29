package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ConfirmDialog;
import gov.epa.emissions.commons.gui.SelectAwareButton;
import gov.epa.emissions.commons.gui.buttons.CloseButton;
import gov.epa.emissions.commons.gui.buttons.NewButton;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.util.ComponentUtility;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.RefreshButton;
import gov.epa.emissions.framework.ui.RefreshObserver;
import gov.epa.emissions.framework.ui.SelectableSortFilterWrapper;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

public class UsersManager extends DisposableInteralFrame implements UsersManagerView, RefreshObserver {

    private UsersManagerPresenter presenter;
    
    private SelectableSortFilterWrapper table;
    
    private JPanel tablePanel;

    private JPanel layout;

    private MessagePanel messagePanel;

    private EmfConsole parentConsole;

    private UsersTableData tableData;

    private EmfSession session;
  
    public UsersManager(EmfConsole parentConsole, DesktopManager desktopManager, EmfSession session) {
        super("User Manager", new Dimension(860, 400), desktopManager);
        super.setName("userManager");

        this.parentConsole = parentConsole;
        this.session = session;
        layout = new JPanel();
        this.getContentPane().add(layout);
    }

    public void display() {
        layout.setLayout(new BorderLayout());

        layout.add(topPanel(), BorderLayout.NORTH);
        layout.add(tablePanel(), BorderLayout.CENTER);
        layout.add(createControlPanel(), BorderLayout.SOUTH);
        
        super.display();
    }

    public void refresh() {
        messagePanel.clear();
        refresh(tableData.getValues());
    }

    public void refresh(User[] users) {
        messagePanel.clear();

        // TODO: A HACK, until we fix row-count issues w/ SortFilterSelectPanel
        tableData = new UsersTableData(users);

        //model.refresh(tableData);
        table.refresh(tableData);
        panelRefresh();
        super.refreshLayout();
    }

    public void doRefresh() throws EmfException {
        presenter.doRefresh();
    }

    private void panelRefresh() {
        tablePanel.removeAll();
        tablePanel.add(table);
        super.refreshLayout();
    }

    private JPanel topPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        messagePanel = new SingleLineMessagePanel();
        panel.add(messagePanel, BorderLayout.CENTER);

        Button button = new RefreshButton(this, "Refresh Users", messagePanel);
        panel.add(button, BorderLayout.EAST);

        return panel;
    }

    private JPanel tablePanel() {
        tablePanel = new JPanel(new BorderLayout());
        tableData = new UsersTableData(new User[] {});//User[] users
        table = new SelectableSortFilterWrapper(parentConsole, tableData, null);
        table.getTable().getAccessibleContext().setAccessibleName("List of users");
        tablePanel.add(table);
        return tablePanel;
        
    }

    private JPanel createControlPanel() {
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BorderLayout());
        
        if (parentConsole.getSession().user().isAdmin())
             controlPanel.add(createCrudPanel(), BorderLayout.WEST);
        controlPanel.add(closePanel(), BorderLayout.EAST);

        return controlPanel;
    }

    private JPanel closePanel() {
        JPanel closePanel = new JPanel();
        Button closeButton = new CloseButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                messagePanel.clear();
                presenter.doClose();
            }
        });
        closePanel.add(closeButton);

        return closePanel;
    }

    private JPanel createCrudPanel() {
        Action newAction = new AbstractAction() {
            public void actionPerformed(ActionEvent arg0) {
                messagePanel.clear();
                displayRegisterUser();
            }
        };
        Button newButton = new NewButton(newAction);

        Action deleteAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                messagePanel.clear();
                deleteUsers();
            }
        };
        Button deleteButton = new Button("Delete", deleteAction);
        deleteButton.setMnemonic(KeyEvent.VK_D);
        deleteButton.setEnabled(false);

        String messageTooManyWindows = "You have asked to open a lot of windows. Do you wish to proceed?";
        ConfirmDialog confirmUpdateDialog = new ConfirmDialog(messageTooManyWindows, "Warning", this);
        Action updateAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                messagePanel.clear();
                updateUsers();
            }
        };
        SelectAwareButton updateButton = new SelectAwareButton("Edit", updateAction, table, confirmUpdateDialog);

        Action logoutAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                messagePanel.clear();
                logoutUsers();
            }
        };
        Button logoutButton = new Button("Logout", logoutAction);
        logoutButton.setMnemonic(KeyEvent.VK_L);
        logoutButton.setEnabled(false);

        Action enableAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                messagePanel.clear();
                enableUsers();
            }
        };
        Button enableButton = new Button("Enable", enableAction);
        enableButton.setMnemonic(KeyEvent.VK_N);
        enableButton.setEnabled(false);

        Action disableAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                messagePanel.clear();
                disableUsers();
            }
        };
        Button disableButton = new Button("Disable", disableAction);
        disableButton.setMnemonic(KeyEvent.VK_I);
        disableButton.setEnabled(false);

        JPanel crudPanel = new JPanel();
        crudPanel.setLayout(new FlowLayout());
        crudPanel.add(newButton);
        crudPanel.add(updateButton);
        crudPanel.add(deleteButton);
        if (session.user().isAdmin()) {
            crudPanel.add(logoutButton);
            crudPanel.add(enableButton);
            crudPanel.add(disableButton);
        }

        return crudPanel;
    }

    public UserView getUserView() {
        ViewUserWindow view = new ViewUserWindow(desktopManager);

        // view.addInternalFrameListener(new InternalFrameAdapter() {
        // public void internalFrameClosed(InternalFrameEvent event) {
        // refresh();
        // }
        // });

        return view;
    }

    private void updateUsers() {
        User[] selected = getSelectedUsers();
        if (selected.length == 0) {
            showMessage("To update, please select at least one User.");
            return;
        }

        try {
            presenter.doUpdateUsers(selected);
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }
    }

    public UpdatableUserView getUpdateUserView(User updateUser) {
        UpdateUserWindow updateUserWindow = new UpdateUserWindow(new AddAdminOption(), desktopManager, parentConsole);

        return updateUserWindow;
    }

    private User[] getSelectedUsers() {
        List selected = table.selected();
        return (User[]) selected.toArray(new User[0]);
    }

    private void deleteUsers() {
        User[] selected = getSelectedUsers();
        if (selected.length == 0) {
            showMessage("To delete, please select at least one User.");
            return;
        }

        if (!promptDelete(selected))
            return;

        try {
            presenter.doDelete(selected);
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }
    }

    private void logoutUsers() {
        User[] selected = getSelectedUsers();
        if (selected.length == 0) {
            showMessage("To logout, please select at least one User.");
            return;
        }

        if (!promptLogout(selected))
            return;

        try {
            presenter.doLogout(selected);
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }
    }

    private void enableUsers() {
        User[] selected = getSelectedUsers();
        if (selected.length == 0) {
            showMessage("To enable user, please select at least one User.");
            return;
        }

        if (!promptEnableDisable(selected, true))
            return;

        try {
            presenter.doEnable(selected);
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }
    }

    private void disableUsers() {
        User[] selected = getSelectedUsers();
        if (selected.length == 0) {
            showMessage("To disable user, please select at least one User.");
            return;
        }

        if (!promptEnableDisable(selected, false))
            return;

        try {
            presenter.doDisable(selected);
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }
    }

    private void showMessage(String message) {
        messagePanel.setMessage(message);
    }

    public boolean promptDelete(User[] users) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < users.length; i++) {
            buffer.append("'" + users[i].getUsername() + "'");
            if (i + 1 < users.length)
                buffer.append(", ");
        }

        int option = JOptionPane.showConfirmDialog(this, "Are you sure about deleting user(s) - " + buffer.toString(),
                "Delete User", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        return (option == 0);
    }

    public boolean promptLogout(User[] users) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < users.length; i++) {
            buffer.append("'" + users[i].getUsername() + "'");
            if (i + 1 < users.length)
                buffer.append(", ");
        }

        int option = JOptionPane.showConfirmDialog(this, "Are you sure to logout user(s) - " + buffer.toString(),
                "Logout User", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        return (option == 0);
    }

    public boolean promptEnableDisable(User[] users, boolean enable) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < users.length; i++) {
            buffer.append("'" + users[i].getUsername() + "'");
            if (i + 1 < users.length)
                buffer.append(", ");
        }

        int option = JOptionPane.showConfirmDialog(this, "Are you sure to " + (enable ? "enable" : "disable") + " user(s) - " + buffer.toString(),
                "Logout User", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        return (option == 0);
    }

    private void displayRegisterUser() {
        RegisterUserInternalFrame registerUserView = new RegisterUserInternalFrame(new NoOpPostRegisterStrategy(),
                desktopManager);
        presenter.doRegisterNewUser(registerUserView);
    }

    public void observe(UsersManagerPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void populate() {
        //long running methods.....
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
//        ComponentUtility.enableComponents(this, false);
        ComponentUtility.enableComponents(this, false);

        //Instances of javax.swing.SwingWorker are not reusuable, so
        //we create new instances as needed.
        class GetUsersTask extends SwingWorker<User[], Void> {
            
            private Container parentContainer;

            public GetUsersTask(Container parentContainer) {
                this.parentContainer = parentContainer;
            }

            /*
             * Main task. Executed in background thread.
             * don't update gui here
             */
            @Override
            public User[] doInBackground() throws EmfException  {
                return presenter.getUsers();
            }

            /*
             * Executed in event dispatching thread
             */
            @Override
            public void done() {
                try {
                    //make sure something didn't happen
                    refresh(get());//User[] users

                
                } catch (InterruptedException e1) {
//                    messagePanel.setError(e1.getMessage());
//                    setErrorMsg(e1.getMessage());
                } catch (ExecutionException e1) {
//                    messagePanel.setError(e1.getCause().getMessage());
//                    setErrorMsg(e1.getCause().getMessage());
                } finally {
//                    this.parentContainer.setCursor(null); //turn off the wait cursor
//                    this.parentContainer.
                    ComponentUtility.enableComponents(this.parentContainer, true);
                    this.parentContainer.setCursor(null); //turn off the wait cursor
                }
            }
        };
        new GetUsersTask(this).execute();
        
    }

}
