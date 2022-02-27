package gov.epa.emissions.framework.client.console;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.exim.ImportPresenter;
import gov.epa.emissions.framework.client.exim.ImportWindow;
import gov.epa.emissions.framework.client.login.LoginPresenter;
import gov.epa.emissions.framework.client.login.LoginWindow;
import gov.epa.emissions.framework.client.util.ComponentUtility;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.UserService;
import gov.epa.emissions.framework.ui.MessagePanel;

import java.awt.Container;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.concurrent.ExecutionException;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.SwingWorker;

public class FileMenu extends JMenu {

    private DesktopManager desktopManager;

//    private EmfConsole parent;

    // FIXME: where's the associated Presenter ?
    public FileMenu(EmfSession session, EmfConsole parent, MessagePanel messagePanel, DesktopManager desktopManager) {
        super("File");
        super.setName("file");
        super.setMnemonic(KeyEvent.VK_F);
//        this.parent = parent;

        super.add(createImport(session, messagePanel, parent));
//        super.add(createTransfer(session, parent));
        super.addSeparator();
        super.add(createLogout(session, parent));
        super.add(createExit(parent));

        this.desktopManager = desktopManager;
    }

    private JMenuItem createExit(final EmfConsole parent) {
        JMenuItem exit = new JMenuItem("Exit");
        exit.setName("exit");
        exit.setMnemonic(KeyEvent.VK_X);
        exit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                ExitAndLogoutAction exit = new ExitAndLogoutAction(parent, desktopManager);
                try {
                    exit.exit();
                } catch (EmfException e) {
                    // NOTE Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
        return exit;
    }

    private JMenuItem createLogout(final EmfSession session, final EmfConsole parent) {
        JMenuItem logout = new JMenuItem("Logout");
        logout.setName("logout");
        logout.setMnemonic(KeyEvent.VK_L);
        logout.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                logout(session, parent, desktopManager);
            }
        });
        return logout;
    }

    private void logout(EmfSession session, EmfConsole parent, DesktopManager desktopManager) {
        ExitAndLogoutAction logout = new ExitAndLogoutAction(parent, desktopManager);
        if (logout.logout()) {
            UserService userServices = session.userService();
            LoginWindow view = new LoginWindow(session.serviceLocator());

            LoginPresenter presenter = new LoginPresenter(userServices);
            presenter.display(view);
        }

    }

    protected JMenuItem createTransfer(final EmfSession session, final EmfConsole parent) {
        JMenuItem logout = new JMenuItem("Transfer");
        logout.setName("transfer");
        logout.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                transfer(session, parent, desktopManager);
            }
        });
        return logout;
    }

    private void transfer(EmfSession session, EmfConsole parent, DesktopManager desktopManager) {
//        try {
//            session.dataService().importRemoteEMFDataset(1, session.user());
//        } catch (EmfException e) {
//            // NOTE Auto-generated catch block
//            e.printStackTrace();
//        }
    }

    private JMenuItem createImport(final EmfSession session, final MessagePanel messagePanel, final EmfConsole parent) {
        JMenuItem importMenu = new JMenuItem("Import");
        importMenu.setName("import");
        importMenu.setMnemonic(KeyEvent.VK_I);
        importMenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                try {
                    //long running methods.....
                    displayImport(session, parent);
                } catch (Exception e) {
                    messagePanel.setError(e.getMessage());
                }
            }
        });
        return importMenu;
    }

    protected void displayImport(final EmfSession session, final EmfConsole parent) throws EmfException, Exception {

//        parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        ImportWindow importView = new ImportWindow(session, session.dataCommonsService(), desktopManager, parent, null);
        
        ImportPresenter importPresenter = new ImportPresenter(session, session.user(), session
                    .eximService());
        
        importPresenter.display(importView);
//        parent.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

//        //Instances of javax.swing.SwingWorker are not reusuable, so
//        //we create new instances as needed.
//        class SwingWorkerTask extends SwingWorker<Void, Void> {
//            
//            private Container parentContainer;
//
//            public SwingWorkerTask(Container parentContainer) {
//                this.parentContainer = parentContainer;
//            }
//
//            /*
//             * Main task. Executed in background thread.
//             * don't update gui here
//             */
//            @Override
//            public Void doInBackground() throws EmfException  {
//
//                ImportWindow importView = new ImportWindow(session, session.dataCommonsService(), desktopManager, parent, null);
//                
//                ImportPresenter importPresenter = new ImportPresenter(session, session.user(), session
//                            .eximService());
//                
//                importPresenter.display(importView);
//                return null;
//            }
//
//            /*
//             * Executed in event dispatching thread
//             */
//            @Override
//            public void done() {
//                //make sure something didn't happen
//                try {
//                    get();
//                } catch (InterruptedException e) {
//                    // NOTE Auto-generated catch block
//                    e.printStackTrace();
//                } catch (ExecutionException e) {
//                    // NOTE Auto-generated catch block
//                    e.printStackTrace();
//                } finally {
//                    System.out.println("normal cursor");
//                    this.parentContainer.setCursor(null); //turn off the wait cursor
//                }
//            }
//        };
//        SwingWorkerTask task = new SwingWorkerTask(parent);
//        task.execute();
//        task.get();
//        parent.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }
}
