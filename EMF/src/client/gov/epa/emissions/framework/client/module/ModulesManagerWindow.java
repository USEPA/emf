package gov.epa.emissions.framework.client.module;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ConfirmDialog;
import gov.epa.emissions.commons.gui.SelectAwareButton;
import gov.epa.emissions.commons.gui.buttons.CloseButton;
import gov.epa.emissions.commons.gui.buttons.NewButton;
import gov.epa.emissions.commons.gui.buttons.RemoveButton;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.ReusableInteralFrame;
import gov.epa.emissions.framework.client.ViewMode;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.util.ComponentUtility;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.module.Module;
import gov.epa.emissions.framework.services.module.ModuleTypeVersion;
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
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

public class ModulesManagerWindow extends ReusableInteralFrame implements ModulesManagerView, RefreshObserver {

    private ModulesManagerPresenter presenter;

    private SelectableSortFilterWrapper table;

    SelectAwareButton viewButton;
    SelectAwareButton editButton;
    NewButton newButton;
    SelectAwareButton copyButton;
    RemoveButton removeButton;
    Button runButton;

    private JPanel layout;

    private JPanel tablePanel;

    private MessagePanel messagePanel;

    private EmfConsole parentConsole;

    private EmfSession session;

    public ModulesManagerWindow(EmfSession session, EmfConsole parentConsole, DesktopManager desktopManager) {
        super("Module Manager", new Dimension(700, 350), desktopManager);
        super.setName("moduleManager");

        this.session = session;
        this.parentConsole = parentConsole;

        layout = new JPanel();
        this.getContentPane().add(layout);
    }

    public void observe(ModulesManagerPresenter presenter) {
        this.presenter = presenter;
    }

    public void refresh(Module[] modules) {
        boolean hasData = (modules != null) && (modules.length > 0);
        boolean isAdmin = session.user().isAdmin();

        // FIXME these settings are reverted somewhere else
        viewButton.setEnabled(hasData);
        editButton.setEnabled(hasData && isAdmin);
        newButton.setEnabled(hasData && isAdmin);
        removeButton.setEnabled(hasData && isAdmin);
        runButton.setEnabled(hasData && isAdmin);

        table.refresh(new ModulesTableData(modules));
        panelRefresh();
    }

    private void panelRefresh() {
        tablePanel.removeAll();
        tablePanel.add(table);
        super.refreshLayout();
    }

    public void display() {
        createLayout();
        super.display();
        populate();
    }

    private void createLayout() {
        layout.removeAll();
        layout.setLayout(new BorderLayout());

        layout.add(createTopPanel(), BorderLayout.NORTH);
        layout.add(createTablePanel(), BorderLayout.CENTER);
        layout.add(createControlPanel(), BorderLayout.SOUTH);
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        messagePanel = new SingleLineMessagePanel();
        panel.add(messagePanel, BorderLayout.CENTER);

        Button button = new RefreshButton(this, "Refresh Module Types", messagePanel);
        panel.add(button, BorderLayout.EAST);

        return panel;
    }

    private JPanel createTablePanel() {
        tablePanel = new JPanel(new BorderLayout());
        table = new SelectableSortFilterWrapper(parentConsole, new ModulesTableData(new Module[] {}), null);
        tablePanel.add(table);
        return tablePanel;
    }

    private JPanel createControlPanel() {
        JPanel crudPanel = createCrudPanel();

        JPanel closePanel = new JPanel();
        Button closeButton = new CloseButton(new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                presenter.doClose();
            }
        });
        closePanel.add(closeButton);
        getRootPane().setDefaultButton(closeButton);

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BorderLayout());

        controlPanel.add(crudPanel, BorderLayout.WEST);
        controlPanel.add(closePanel, BorderLayout.EAST);

        return controlPanel;
    }

    private JPanel createCrudPanel() {
        String message = "You have asked to open a lot of windows. Do you wish to proceed?";
        ConfirmDialog confirmDialog = new ConfirmDialog(message, "Warning", this);

        Action viewAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                viewModules();
            }
        };
        viewButton = new SelectAwareButton("View", viewAction, table, confirmDialog);

        Action editAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                editModules();
            }
        };
        editButton = new SelectAwareButton("Edit", editAction, table, confirmDialog);

        Action createAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                createModule();
            }
        };
        newButton = new NewButton(createAction);

        Action copyAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                copyModules();
            }
        };
        copyButton = new SelectAwareButton("Copy", copyAction, table, confirmDialog);

        Action removeAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                removeModules();
            }
        };
        removeButton = new RemoveButton(removeAction);

        Action runAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                runModules();
            }
        };
        runButton = new Button("Run", runAction);

        JPanel crudPanel = new JPanel();
        crudPanel.setLayout(new FlowLayout());
        crudPanel.add(viewButton);
        crudPanel.add(editButton);
        crudPanel.add(newButton);
        crudPanel.add(copyButton);
        crudPanel.add(removeButton);
        crudPanel.add(runButton);
        if (!session.user().isAdmin()){
            editButton.setEnabled(false);
            newButton.setEnabled(false);
            copyButton.setEnabled(false);
            removeButton.setEnabled(false);
            runButton.setEnabled(false);
        }

        return crudPanel;
    }

    private void viewModules() {
        List selected = selected();
        if (selected.isEmpty()) {
            if (table.getTable().getRowCount() > 0) {
                messagePanel.setMessage("Please select one or more modules");
            }
            return;
        }   

        for (Iterator iter = selected.iterator(); iter.hasNext();) {
            Module module = (Module) iter.next();
            try {
                module = session.moduleService().obtainLockedModule(session.user(), module);
                ModulePropertiesWindow view = new ModulePropertiesWindow(parentConsole, desktopManager, session, ViewMode.VIEW, module, null);
                presenter.displayNewModuleView(view);
            } catch (EmfException e) {
                messagePanel.setMessage("Failed to lock the \"" + module.getName() + "\" module: " + e.getMessage());
            }
        }
    }

    private void editModules() {
        List selected = selected();
        if (selected.isEmpty()) {
            if (table.getTable().getRowCount() > 0) {
                messagePanel.setMessage("Please select one or more modules");
            }
            return;
        }   

        for (Iterator iter = selected.iterator(); iter.hasNext();) {
            Module module = (Module) iter.next();
            try {
                module = session.moduleService().obtainLockedModule(session.user(), module);
                ModulePropertiesWindow view = new ModulePropertiesWindow(parentConsole, desktopManager, session, ViewMode.EDIT, module, null);
                presenter.displayNewModuleView(view);
            } catch (EmfException e) {
                messagePanel.setMessage("Failed to lock the \"" + module.getName() + "\" module: " + e.getMessage());
            }
        }
    }

    private void createModule() {
        ModuleTypeVersion moduleTypeVersion = ModulePropertiesWindow.selectModuleTypeVersion(parentConsole, session);
        if (moduleTypeVersion != null) {
            ModulePropertiesWindow view = new ModulePropertiesWindow(parentConsole, desktopManager, session, ViewMode.NEW, null, moduleTypeVersion);
            presenter.displayNewModuleView(view);
        }
    }

    private void copyModules() {
        List selected = selected();
        if (selected.isEmpty()) {
            if (table.getTable().getRowCount() > 0) {
                messagePanel.setMessage("Please select one or more modules");
            }
            return;
        }   

        for (Iterator iter = selected.iterator(); iter.hasNext();) {
            Module module = (Module) iter.next();
            Module copy = module.deepCopy(session.user());
            ModulePropertiesWindow view = new ModulePropertiesWindow(parentConsole, desktopManager, session, ViewMode.NEW, copy, null);
            presenter.displayNewModuleView(view);
        }
    }

    private void removeModules() {
        messagePanel.clear();
        List<?> selected = selected();
        if (selected.isEmpty()) {
            if (table.getTable().getRowCount() > 0) {
                messagePanel.setMessage("Please select one or more modules");
            }
            return;
        }   

        String message = "Are you sure you want to remove the selected " + selected.size() + " module(s)?";
        int selection = JOptionPane.showConfirmDialog(parentConsole, message, "Warning", JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (selection == JOptionPane.YES_OPTION) {
            try {
                presenter.doRemove(selected.toArray(new Module[0]));
                messagePanel.setMessage(selected.size()
                        + " module modules have been removed. Please Refresh to see the revised list of modules.");
            } catch (EmfException e) {
              JOptionPane.showConfirmDialog(parentConsole, e.getMessage(), "Error", JOptionPane.CLOSED_OPTION);
            }
        }
    }

    private void runModules() {
        messagePanel.clear();
        List<?> selected = selected();
        if (selected.isEmpty()) {
            if (table.getTable().getRowCount() > 0) {
                messagePanel.setMessage("Please select one or more modules");
            }
            return;
        }   

        Module[] modules = selected.toArray(new Module[0]);

        String message;
        if (modules.length == 1) {
            message = "Are you sure you want to run the '" + modules[0].getName() + "' module?";
        } else {
            message = "Are you sure you want to run the selected " + selected.size() + " modules?";
        }
        int selection = JOptionPane.showConfirmDialog(parentConsole, message, "Warning", JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (selection == JOptionPane.YES_OPTION) {
            try {
                presenter.runModules(modules);
                if (modules.length == 1) {
                    messagePanel.setMessage("Module " + modules[0].getName() + " has been executed.");
                } else {
                    messagePanel.setMessage(modules.length + " modules have been executed.");
                }
            } catch (EmfException e) {
              JOptionPane.showConfirmDialog(parentConsole, e.getMessage(), "Error", JOptionPane.CLOSED_OPTION);
            }
        }
    }

    private List selected() {
        return table.selected();
    }

    public EmfConsole getParentConsole() {
        return this.parentConsole;
    }

    public void doRefresh() throws EmfException {
        populate();
    }

    @Override
    public void populate() {
        //long running methods.....
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        ComponentUtility.enableComponents(this, false);

        //Instances of javax.swing.SwingWorker are not reusable, so
        //we create new instances as needed.
        class GetModulesTask extends SwingWorker<Module[], Void> {

            private Container parentContainer;

            public GetModulesTask(Container parentContainer) {
                this.parentContainer = parentContainer;
            }

            /*
             * Main task. Executed in background thread.
             * don't update gui here
             */
            @Override
            public Module[] doInBackground() throws EmfException  {
                return presenter.getModules();
            }

            /*
             * Executed in event dispatching thread
             */
            @Override
            public void done() {
                try {
                    //make sure something didn't happen
                    refresh(get());
                } catch (InterruptedException e1) {
//                    messagePanel.setError(e1.getMessage());
//                    setErrorMsg(e1.getMessage());
                } catch (ExecutionException e1) {
//                    messagePanel.setError(e1.getCause().getMessage());
//                    setErrorMsg(e1.getCause().getMessage());
                } finally {
//                    this.parentContainer.setCursor(null); //turn off the wait cursor
//                    this.parentContainer.
                    ComponentUtility.enableComponents(parentContainer, true);
                    this.parentContainer.setCursor(null); //turn off the wait cursor
                }
            }
        };
        new GetModulesTask(this).execute();
    }
}
