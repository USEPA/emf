package gov.epa.emissions.framework.client.moduletype;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ConfirmDialog;
import gov.epa.emissions.commons.gui.SelectAwareButton;
import gov.epa.emissions.commons.gui.buttons.CloseButton;
import gov.epa.emissions.commons.gui.buttons.RemoveButton;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.ReusableInteralFrame;
import gov.epa.emissions.framework.client.ViewMode;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.module.LiteModule;
import gov.epa.emissions.framework.services.module.ModuleType;
import gov.epa.emissions.framework.services.module.ModuleTypeVersion;
import gov.epa.emissions.framework.services.module.ModuleTypeVersionSubmodule;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.RefreshButton;
import gov.epa.emissions.framework.ui.RefreshObserver;
import gov.epa.emissions.framework.ui.SelectableSortFilterWrapper;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

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
    
    private Map<Integer, ViewMode> viewEditMTVs; // lists all View/Edit Module Type Version windows open (new MTVs don't have IDs yet)
    private int newEditMTVs;                     // counts all New/Edit Module Type Version windows open

    public ModuleTypeVersionsManagerWindow(EmfSession session, EmfConsole parentConsole, DesktopManager desktopManager, ViewMode viewMode, ModuleType moduleType) {
        super(getWindowTitle(viewMode, moduleType), new Dimension(800, 400), desktopManager);

        this.session = session;
        this.parentConsole = parentConsole;
        this.viewMode = viewMode;
        this.moduleType = moduleType;
        this.viewEditMTVs = new HashMap<Integer, ViewMode>();
        this.newEditMTVs = 0;
        
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
        int openMTVs = newEditMTVs + viewEditMTVs.size(); 
        if (openMTVs > 0) {
            String error = String.format("Can't close this window: %d View/Edit/New Module Type Version %s still open", openMTVs, (openMTVs == 1) ? "window is" : "windows are");
            messagePanel.setError(error);
            return;
        }

        if (viewMode == ViewMode.EDIT) {
            try {
                moduleType = presenter.releaseLockedModuleType(moduleType.getId());
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
        viewButton.setMnemonic('V');

        Action editAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                editModuleTypeVersions();
            }
        };
        SelectAwareButton editButton = new SelectAwareButton("Edit", editAction, table, confirmDialog);
        editButton.setMnemonic('E');

        Action newVersionAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                newVersion();
            }
        };
        Button newVersionButton = new Button("New Version", newVersionAction);
        newVersionButton.setMnemonic('N');

        Action newModuleTypeAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                newModuleType();
            }
        };
        Button newModuleTypeButton = new Button("New Module Type", newModuleTypeAction);
        newModuleTypeButton.setMnemonic('T');

        Action removeAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                removeModuleTypeVersion();
            }
        };
        Button removeButton = new RemoveButton(removeAction);
        removeButton.setMnemonic('m');

        JPanel crudPanel = new JPanel();
        crudPanel.setLayout(new FlowLayout());
        crudPanel.add(viewButton);
        crudPanel.add(editButton);
        crudPanel.add(newVersionButton);
        crudPanel.add(newModuleTypeButton);
        crudPanel.add(removeButton);
        if (viewMode == ViewMode.VIEW) {
            editButton.setEnabled(false);
            newVersionButton.setEnabled(false);
            removeButton.setEnabled(false);
        }

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
            if (viewEditMTVs.containsKey(moduleTypeVersion.getId())) {
                ViewMode lockedViewMode = viewEditMTVs.get(moduleTypeVersion.getId());
                String error = String.format("Module type version %d already open for %s", moduleTypeVersion.getVersion(), (lockedViewMode == ViewMode.VIEW) ? "viewing" : "editing");
                messagePanel.setMessage(error);
                continue;
            }
            viewEditMTVs.put(moduleTypeVersion.getId(), ViewMode.VIEW);
            ModuleTypeVersionPropertiesWindow view = new ModuleTypeVersionPropertiesWindow(parentConsole, desktopManager, session, this, ViewMode.VIEW, moduleTypeVersion);
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
            if (viewEditMTVs.containsKey(moduleTypeVersion.getId())) {
                ViewMode lockedViewMode = viewEditMTVs.get(moduleTypeVersion.getId());
                String error = String.format("Module type version %d already open for %s", moduleTypeVersion.getVersion(), (lockedViewMode == ViewMode.VIEW) ? "viewing" : "editing");
                messagePanel.setMessage(error);
                continue;
            }
            viewEditMTVs.put(moduleTypeVersion.getId(), viewMode);
            ModuleTypeVersionPropertiesWindow view = new ModuleTypeVersionPropertiesWindow(parentConsole, desktopManager, session, this, viewMode, moduleTypeVersion);
            presenter.displayNewModuleTypeView(view);
        }
    }

    private void newVersion() {
        List selected = selected();
        if (selected.isEmpty()) {
            messagePanel.setMessage("Please select one or more module type versions to use as base versions");
            return;
        }   

        for (Iterator iter = selected.iterator(); iter.hasNext();) {
            ModuleTypeVersion moduleTypeVersion = (ModuleTypeVersion) iter.next();
            ModuleTypeVersion newModuleTypeVersion = moduleTypeVersion.deepCopy(session.user());
            moduleTypeVersion.getModuleType().addModuleTypeVersion(newModuleTypeVersion);
            newEditMTVs++;
            ModuleTypeVersionPropertiesWindow view = new ModuleTypeVersionPropertiesWindow(parentConsole, desktopManager, session, this, ViewMode.NEW, newModuleTypeVersion);
            presenter.displayNewModuleTypeView(view);
        }
    }

    private void newModuleType() {
        List selected = selected();
        if (selected.isEmpty()) {
            messagePanel.setMessage("Please select a module type version to use as base version");
            return;
        }   
        if (selected.size() > 1) {
            messagePanel.setMessage("Please select only one module type version to use as base version");
            return;
        }   

        for (Iterator iter = selected.iterator(); iter.hasNext();) {
            ModuleTypeVersion moduleTypeVersion = (ModuleTypeVersion) iter.next();
            ModuleTypeVersionPropertiesWindow newView = new ModuleTypeVersionPropertiesWindow(parentConsole, desktopManager, session, null, moduleTypeVersion);
            ModuleTypeVersionPropertiesPresenter newPresenter = new ModuleTypeVersionPropertiesPresenter(session, newView);
            newPresenter.doDisplay();
        }
    }

    private void removeModuleTypeVersion() {
        messagePanel.clear();
        List<?> selected = selected();
        if (selected.isEmpty()) {
            messagePanel.setMessage("Please select a module type version");
            return;
        } else if (selected.size() > 1) {
            messagePanel.setMessage("Please select only one module type version");
            return;
        } else {
            messagePanel.setMessage("");
        }

        ModuleTypeVersion[] selectedMTVs = selected.toArray(new ModuleTypeVersion[0]);
        ModuleTypeVersion selectedMTV = selectedMTVs[0];
        
        // check if the selected MTV is finalized
        if (!session.user().isAdmin() && selectedMTV.getIsFinal()) {
            messagePanel.setError("Only administrators can remove finalized module type versions");
            return;
        }

        // check if the selected MTV is used by any module
        ConcurrentSkipListMap<Integer, LiteModule> liteModules = session.getLiteModules();
        StringBuilder list = new StringBuilder();
        int count = 0;
        for(LiteModule liteModule : liteModules.values()) {
            if (selectedMTV.getId() == liteModule.getLiteModuleTypeVersion().getId()) {
                list.append(liteModule.getName() + "\n");
                count++;
            }
        }
        if (count == 1) {
            messagePanel.setError("Can't remove module type version because it's being used by one module.");
            String message = "Can't remove module type version because the following module depends on it:\n" + list.toString();
            JOptionPane.showConfirmDialog(parentConsole, message, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
            return;
        } else if (count > 1) {
            messagePanel.setError("Can't remove module type version because it's being used by " + count + " module(s).");
            String message = "Can't remove module type version because the following module(s) depend on it:\n" + list.toString();
            JOptionPane.showConfirmDialog(parentConsole, message, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
            return;
        }

        // check if the selected MTV is used by any composite module
        ModuleType[] moduleTypes = null;
        try {
            moduleTypes = presenter.getModuleTypes();
        } catch (EmfException e) {
            e.printStackTrace();
            messagePanel.setError("Failed to get the list of module types: " + e.getMessage());
            return;
        }
        if (moduleTypes == null) {
            messagePanel.setError("Failed to get the list of module types.");
            return;
        }
        list.setLength(0);
        count = 0;
        for(ModuleType mt : moduleTypes) {
            if (!mt.isComposite())
                continue;
            for (ModuleTypeVersion mtv : mt.getModuleTypeVersions().values()) {
                for (ModuleTypeVersionSubmodule mtvs : mtv.getModuleTypeVersionSubmodules().values()) {
                    if (mtvs.getModuleTypeVersion().equals(selectedMTV)) {
                        list.append(mtv.fullNameSS("\"%s\" version \"%s\"") + " submodule " + mtvs.getName() + "\n");
                        count++;
                    }
                }
            }
        }
        if (count == 1) {
            messagePanel.setError("Can't remove module type version because it's being used by one submodule.");
            String message = "Can't remove module type version because the following submodule depends on it:\n" + list.toString();
            JOptionPane.showConfirmDialog(parentConsole, message, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
            return;
        } else if (count > 1) {
            messagePanel.setError("Can't remove module type version because it's being used by " + count + " submodule.");
            String message = "Can't remove module type version because the following submodule depend on it:\n" + list.toString();
            JOptionPane.showConfirmDialog(parentConsole, message, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // check if the selected MTV is currently being viewed on edited
        if (viewEditMTVs.containsKey(selectedMTV.getId())) {
            ViewMode lockedViewMode = viewEditMTVs.get(selectedMTV.getId());
            String lockedState = (lockedViewMode == ViewMode.VIEW) ? "View" : "Edit";
            String error = String.format("Can't remove module type version %d: the %s Module Type Version window is open", selectedMTV.getVersion(), lockedState);
            messagePanel.setError(error);
            return;
        }
        
        if (selectedMTV.getModuleType().isLocked() && !selectedMTV.getModuleType().isLocked(session.user())) {
            String error = String.format("Can't remove module type version %d: the Module Type is locked by %s", selectedMTV.getVersion(), selectedMTV.getModuleType().getLockOwner());
            messagePanel.setError(error);
            return;
        }
        
        String message = "Are you sure you want to remove the selected module type version? There is no undo for this action.";
        int selection = JOptionPane.showConfirmDialog(parentConsole, message, "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (selection == JOptionPane.YES_OPTION) {
            try {
                ModuleType moduleType = presenter.removeModuleTypeVersion(selectedMTV.getId());
                messagePanel.setMessage("The module type has been removed.");
                table.refresh(new ModuleTypeVersionsTableData(moduleType.getModuleTypeVersions()));
                panelRefresh();
            } catch (EmfException e) {
                messagePanel.setError("Failed to remove the selected module type version: " + e.getMessage());
            }
        }
    }

    private List selected() {
        return table.selected();
    }

    public EmfConsole getParentConsole() {
        return this.parentConsole;
    }

    @Override
    public void doRefresh() {
        try {
            moduleType = presenter.getModuleType(moduleType.getId());
        } catch (EmfException e) {
            messagePanel.setError("Refresh failed: " + e.getMessage());
            return;
        }
        table.refresh(new ModuleTypeVersionsTableData(moduleType.getModuleTypeVersions()));
        panelRefresh();
    }

    @Override
    public void closedChildWindow(ModuleTypeVersion moduleTypeVersion, ViewMode viewMode) {
        if (viewMode == ViewMode.NEW) {
            newEditMTVs--;
        } else {
            viewEditMTVs.remove(moduleTypeVersion.getId());
        }
        doRefresh();
    }
}
