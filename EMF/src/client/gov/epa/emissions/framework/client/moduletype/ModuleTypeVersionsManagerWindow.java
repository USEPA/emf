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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class ModuleTypeVersionsManagerWindow extends ReusableInteralFrame implements ModuleTypeVersionsManagerView, RefreshObserver {

    private ModuleTypeVersionsManagerPresenter presenter;

    private ViewMode viewMode;
    
    private ModuleType moduleType;
    
    private SelectableSortFilterWrapper table;

    private JPanel layout;

    private JPanel tablePanel;

    private MessagePanel messagePanel;

    private EmfConsole parentConsole;

    private EmfSession session;

    public ModuleTypeVersionsManagerWindow(EmfSession session, EmfConsole parentConsole, DesktopManager desktopManager, ViewMode viewMode, ModuleType moduleType) {
        super(getWindowTitle(viewMode, moduleType), new Dimension(700, 350), desktopManager);

        this.session = session;
        this.parentConsole = parentConsole;
        this.viewMode = viewMode;
        this.moduleType = moduleType;
        
        layout = new JPanel();
        this.getContentPane().add(layout);
    }

    private static String getWindowTitle(ViewMode viewMode, ModuleType moduleType) {
        switch(viewMode) {
            case VIEW: return "View Module Type - " + moduleType.getName();
            case EDIT: return "Edit Module Type - " + moduleType.getName();
            default:   return "Module Type - " + moduleType.getName(); // should never happen
        }
    }

    public void observe(ModuleTypeVersionsManagerPresenter presenter) {
        this.presenter = presenter;
    }

    public void refresh() {
        table.refresh(new ModuleTypeVersionsTableData(moduleType.getModuleTypeVersions()));
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

        Button button = new RefreshButton(this, "Refresh Module Type Versions", messagePanel);
        panel.add(button, BorderLayout.EAST);

        return panel;
    }

    private JPanel createTablePanel() {
        tablePanel = new JPanel(new BorderLayout());
        table = new SelectableSortFilterWrapper(parentConsole, new ModuleTypeVersionsTableData(moduleType.getModuleTypeVersions()), null);
        tablePanel.add(table);
        return tablePanel;
    }

    private JPanel createControlPanel() {
        JPanel crudPanel = createCrudPanel();

        JPanel closePanel = new JPanel();
        Button closeButton = new CloseButton(new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                doClose();
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

    private void doClose() {
        if (viewMode == ViewMode.EDIT) {
            try {
                moduleType = session.moduleService().releaseLockedModuleType(session.user(), moduleType);
            } catch (EmfException e) {
                messagePanel.setError("Could not unlock lock: " + moduleType.getName() + "." + e.getMessage());
            }
        }
        presenter.doClose();
    }
    
    public void windowClosing() {
        doClose();
    }

    private JPanel createCrudPanel() {
        String message = "You have asked to open a lot of windows. Do you wish to proceed?";
        ConfirmDialog confirmDialog = new ConfirmDialog(message, "Warning", this);

        Action viewAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                viewModuleTypeVersions();
            }
        };
        SelectAwareButton viewButton = new SelectAwareButton("View", viewAction, table, confirmDialog);

        Action editAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                editModuleTypeVersions();
            }
        };
        SelectAwareButton editButton = new SelectAwareButton("Edit", editAction, table, confirmDialog);

        Action createAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                createModuleTypeVersion();
            }
        };
        Button newButton = new NewButton(createAction);

        Action removeAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                removeModuleTypeVersion();
            }
        };
        Button removeButton = new RemoveButton(removeAction);

        JPanel crudPanel = new JPanel();
        crudPanel.setLayout(new FlowLayout());
        crudPanel.add(viewButton);
        crudPanel.add(editButton);
        crudPanel.add(newButton);
        crudPanel.add(removeButton);
        if (viewMode == ViewMode.VIEW) {
            editButton.setEnabled(false);
            newButton.setEnabled(false);
            removeButton.setEnabled(false);
        }

        // temporarily disabling the remove button
        // TODO update presenter.doRemove() to remove all modules that are based on the about-to-be-deleted module type versions
        removeButton.setEnabled(false);

        return crudPanel;
    }

    private void viewModuleTypeVersions() {
        List selected = selected();
        if (selected.isEmpty()) {
            messagePanel.setMessage("Please select one or more module type versions");
            return;
        }   

        for (Iterator iter = selected.iterator(); iter.hasNext();) {
            ModuleTypeVersion moduleTypeVersion = (ModuleTypeVersion) iter.next();
            ModuleTypeVersionPropertiesWindow view = new ModuleTypeVersionPropertiesWindow(parentConsole, desktopManager, session, ViewMode.VIEW, moduleTypeVersion);
            presenter.displayNewModuleTypeView(view);
        }
    }

    private void editModuleTypeVersions() {
        List selected = selected();
        if (selected.isEmpty()) {
            messagePanel.setMessage("Please select one or more module type versions");
            return;
        }   

        for (Iterator iter = selected.iterator(); iter.hasNext();) {
            ModuleTypeVersion moduleTypeVersion = (ModuleTypeVersion) iter.next();
            ViewMode viewMode = moduleTypeVersion.getIsFinal() ? ViewMode.VIEW : ViewMode.EDIT;
            ModuleTypeVersionPropertiesWindow view = new ModuleTypeVersionPropertiesWindow(parentConsole, desktopManager, session, viewMode, moduleTypeVersion);
            presenter.displayNewModuleTypeView(view);
        }
    }

    private void createModuleTypeVersion() {
        List selected = selected();
        if (selected.isEmpty()) {
            messagePanel.setMessage("Please select one or more module type versions to use as base versions");
            return;
        }   

        for (Iterator iter = selected.iterator(); iter.hasNext();) {
            ModuleTypeVersion moduleTypeVersion = (ModuleTypeVersion) iter.next();
            ModuleTypeVersion newModuleTypeVersion = moduleTypeVersion.deepCopy(session.user());
            ModuleTypeVersionPropertiesWindow view = new ModuleTypeVersionPropertiesWindow(parentConsole, desktopManager, session, ViewMode.NEW, newModuleTypeVersion);
            presenter.displayNewModuleTypeView(view);
        }
    }

    private void removeModuleTypeVersion() {
        messagePanel.clear();
        List<?> selected = selected();
        if (selected.isEmpty()) {
            messagePanel.setMessage("Please select one or more module type versions");
            return;
        }   

        ModuleTypeVersion[] selectedMTVs = selected.toArray(new ModuleTypeVersion[0]);
        
        int count = 0;
        try {
            Module[] modules = presenter.getModules();
            for(Module module : modules) {
                for(ModuleTypeVersion selectedMTV : selectedMTVs) {
                    if (module.getModuleTypeVersion().getId() == selectedMTV.getId()) {
                        count++;
                        if (module.isLocked()) {
                            String error = String.format("Can't remove module type version %d: the %s module is locked by %s", selectedMTV.getVersion(), module.getName(), module.getLockOwner());
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
            message = String.format("Are you sure you want to remove the selected %d module type version(s)? The %d module(s) that use this module type version(s) will also be removed. There is no undo for this action.", selected.size(), count);
        } else {
            message = String.format("Are you sure you want to remove the selected %d module type version(s)? There is no undo for this action.", selected.size());
        }
        
        int selection = JOptionPane.showConfirmDialog(parentConsole, message, "Warning", JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (selection == JOptionPane.YES_OPTION) {
            messagePanel.setError("Removing module type versions has not been implemented yet.");
//            try {
//                presenter.doRemove(selectedMTVS);
//                messagePanel.setMessage(selected.size() + " module types have been removed. Please Refresh to see the revised list of types.");
//            } catch (EmfException e) {
//                messagePanel.setError("Failed to remove the selected module type(s): " + e.getMessage());
//            }
        }
    }

    private List selected() {
        return table.selected();
    }

    public EmfConsole getParentConsole() {
        return this.parentConsole;
    }

    @Override
    public void doRefresh() throws EmfException {
        refresh();
    }
}
