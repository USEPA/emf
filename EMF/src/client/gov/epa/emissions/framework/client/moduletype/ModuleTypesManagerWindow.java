package gov.epa.emissions.framework.client.moduletype;

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
import gov.epa.emissions.framework.services.module.ModuleType;
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

public class ModuleTypesManagerWindow extends ReusableInteralFrame implements ModuleTypesManagerView, RefreshObserver {

    private ModuleTypesManagerPresenter presenter;

    private SelectableSortFilterWrapper table;

    private JPanel layout;

    private JPanel tablePanel;

    private MessagePanel messagePanel;

    private EmfConsole parentConsole;

    private EmfSession session;

    public ModuleTypesManagerWindow(EmfSession session, EmfConsole parentConsole, DesktopManager desktopManager) {
        super("Module Type Manager", new Dimension(700, 350), desktopManager);
        super.setName("moduleTypeManager");

        this.session = session;
        this.parentConsole = parentConsole;

        layout = new JPanel();
        this.getContentPane().add(layout);
    }

    public void observe(ModuleTypesManagerPresenter presenter) {
        this.presenter = presenter;
    }

    public void refresh(ModuleType[] types) {
        table.refresh(new ModuleTypesTableData(types));
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
        table = new SelectableSortFilterWrapper(parentConsole, new ModuleTypesTableData(new ModuleType[] {}), null);
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
                viewModuleTypes();
            }
        };
        SelectAwareButton viewButton = new SelectAwareButton("View", viewAction, table, confirmDialog);

        Action editAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                editModuleTypes();
            }
        };
        SelectAwareButton editButton = new SelectAwareButton("Edit", editAction, table, confirmDialog);

        Action createAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                createModuleType();
            }
        };
        Button newButton = new NewButton(createAction);

        Action removeAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                removeModuleType();
            }
        };
        Button removeButton = new RemoveButton(removeAction);
        JPanel crudPanel = new JPanel();
        crudPanel.setLayout(new FlowLayout());
        crudPanel.add(viewButton);
        crudPanel.add(editButton);
        crudPanel.add(newButton);
        crudPanel.add(removeButton);
        return crudPanel;
    }

    private void viewModuleTypes() {
        List selected = selected();
        if (selected.isEmpty()) {
            messagePanel.setMessage("Please select one or more module types");
            return;
        }   

        for (Iterator iter = selected.iterator(); iter.hasNext();) {
            ModuleType moduleType = (ModuleType) iter.next();
            try {
                ModuleTypeVersionsManagerWindow view = new ModuleTypeVersionsManagerWindow(session, parentConsole, desktopManager, ViewMode.VIEW, moduleType);
                presenter.displayModuleTypeVersions(view);
            } catch (EmfException e) {
                messagePanel.setError("Could not diplay module type versions for: " + moduleType.getName() + "." + e.getMessage());
                break;
            }
        }
    }

    private void editModuleTypes() {
        List selected = selected();
        if (selected.isEmpty()) {
            messagePanel.setMessage("Please select one or more module types");
            return;
        }   

        for (Iterator iter = selected.iterator(); iter.hasNext();) {
            ModuleType moduleType = (ModuleType) iter.next();
            try {
                moduleType = session.moduleService().obtainLockedModuleType(session.user(), moduleType);
            } catch (EmfException e) {
                messagePanel.setError("Failed to lock " + moduleType.getName() + ": " + e.getMessage());
                break;
            }
            if (!moduleType.isLocked(session.user())) {
                messagePanel.setError("Failed to lock " + moduleType.getName() + ".");
                break;
            }
            try {
                ModuleTypeVersionsManagerWindow view = new ModuleTypeVersionsManagerWindow(session, parentConsole, desktopManager, ViewMode.EDIT, moduleType);
                presenter.displayModuleTypeVersions(view);
            } catch (EmfException e) {
                messagePanel.setError("Could not diplay module type versions for: " + moduleType.getName() + "." + e.getMessage());
                break;
            }
        }
    }

    private void createModuleType() {
        ModuleTypeVersionPropertiesWindow view = new ModuleTypeVersionPropertiesWindow(parentConsole, desktopManager, session, this);
        presenter.displayNewModuleTypeView(view);
    }

    private void removeModuleType() {
        messagePanel.clear();
        List<?> selected = selected();
        if (selected.isEmpty()) {
            messagePanel.setMessage("Please select one or more module types");
            return;
        }   

        ModuleType[] selectedMTs = selected.toArray(new ModuleType[0]);
        for(ModuleType selectedMT : selectedMTs) {
            if (selectedMT.isLocked()) {
                String error = String.format("Can't remove the %s module type: it's locked by %s", selectedMT.getName(), selectedMT.getLockOwner());
                messagePanel.setError(error);
                return;
            }
        }
        
        int count = 0;
        try {
            Module[] modules = presenter.getModules();
            for(Module module : modules) {
                for(ModuleType selectedMT : selectedMTs) {
                    if (module.getModuleTypeVersion().getModuleType().getId() == selectedMT.getId()) {
                        count++;
                        if (module.isLocked()) {
                            String error = String.format("Can't remove the %s module type: the %s module is locked by %s", selectedMT.getName(), module.getName(), module.getLockOwner());
                            messagePanel.setError(error);
                            return;
                        }
                    }
                }
            }
        } catch (EmfException e) {
            messagePanel.setError("Failed to get the list of modules.");
            return;
        }

        String message = "";
        if (count > 0) {
            message = String.format("Are you sure you want to remove the selected %d module type(s)? The %d module(s) that use this module type(s) will also be removed. There is no undo for this action.", selected.size(), count);
        } else {
            message = String.format("Are you sure you want to remove the selected %d module type(s)? There is no undo for this action.", selected.size());
        }
        
        int selection = JOptionPane.showConfirmDialog(parentConsole, message, "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (selection == JOptionPane.YES_OPTION) {
            try {
                presenter.doRemove(selected.toArray(new ModuleType[0]));
                messagePanel.setMessage(selected.size()
                        + " module types have been removed. Please Refresh to see the revised list of types.");
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
        class GetModuleTypesTask extends SwingWorker<ModuleType[], Void> {

            private Container parentContainer;

            public GetModuleTypesTask(Container parentContainer) {
                this.parentContainer = parentContainer;
            }

            /*
             * Main task. Executed in background thread.
             * don't update gui here
             */
            @Override
            public ModuleType[] doInBackground() throws EmfException  {
                return presenter.getModuleTypes();
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
        new GetModuleTypesTask(this).execute();
    }

    @Override
    public void closedChildWindow(ModuleTypeVersion moduleTypeVersion, ViewMode viewMode) {
        // nothing to do
    }
}
