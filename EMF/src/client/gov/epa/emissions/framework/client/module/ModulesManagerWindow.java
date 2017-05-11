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
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.module.Module;
import gov.epa.emissions.framework.services.module.LiteModule;
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
import java.awt.event.KeyEvent;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListMap;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class ModulesManagerWindow extends ReusableInteralFrame implements ModulesManagerView, RefreshObserver {

    private ModulesManagerPresenter presenter;

    private SelectableSortFilterWrapper table;

    SelectAwareButton viewButton;
    SelectAwareButton editButton;
    Button lockButton;
    Button unlockButton;
    NewButton newButton;
    SelectAwareButton copyButton;
    RemoveButton removeButton;
    Button runButton;
    Button compareButton;

    private JPanel layout;

    private JPanel tablePanel;

    private MessagePanel messagePanel;

    private EmfConsole parentConsole;

    private EmfSession session;

    public ModulesManagerWindow(EmfSession session, EmfConsole parentConsole, DesktopManager desktopManager) {
        super("Module Manager", new Dimension(800, 400), desktopManager);
        super.setName("moduleManager");

        this.session = session;
        this.parentConsole = parentConsole;

        layout = new JPanel();
        this.getContentPane().add(layout);
    }

    public void observe(ModulesManagerPresenter presenter) {
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
        table = new SelectableSortFilterWrapper(parentConsole, new ModulesTableData(new ConcurrentSkipListMap<Integer, LiteModule>()), null);
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
        viewButton.setMnemonic(KeyEvent.VK_V);

        Action editAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                editModules();
            }
        };
        editButton = new SelectAwareButton("Edit", editAction, table, confirmDialog);
        editButton.setMnemonic(KeyEvent.VK_E);

        Action lockAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                lockModules();
            }
        };
        lockButton = new Button("Lock", lockAction);
        lockButton.setMnemonic(KeyEvent.VK_L);

        Action unlockAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                unlockModules();
            }
        };
        unlockButton = new Button("Unlock", unlockAction);
        unlockButton.setMnemonic(KeyEvent.VK_O);

        Action createAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                createModule();
            }
        };
        newButton = new NewButton(createAction);
        newButton.setMnemonic(KeyEvent.VK_N);

        Action copyAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                copyModules();
            }
        };
        copyButton = new SelectAwareButton("Copy", copyAction, table, confirmDialog);
        copyButton.setMnemonic(KeyEvent.VK_Y);

        Action removeAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                removeModules();
            }
        };
        removeButton = new RemoveButton(removeAction);
        removeButton.setMnemonic(KeyEvent.VK_M);

        Action runAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                runModules();
            }
        };
        runButton = new Button("Run", runAction);
        runButton.setMnemonic(KeyEvent.VK_U);

        Action compareAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                compareModules();
            }
        };
        compareButton = new Button("Compare", compareAction);
        compareButton.setMnemonic(KeyEvent.VK_C);

        JPanel crudPanel = new JPanel();
        crudPanel.setLayout(new FlowLayout());
        crudPanel.add(viewButton);
        crudPanel.add(editButton);
        crudPanel.add(newButton);
        crudPanel.add(copyButton);
        crudPanel.add(removeButton);
        crudPanel.add(Box.createRigidArea(new Dimension(5,0)));
        crudPanel.add(compareButton);
        crudPanel.add(Box.createRigidArea(new Dimension(5,0)));
        crudPanel.add(lockButton);
        crudPanel.add(unlockButton);
        crudPanel.add(Box.createRigidArea(new Dimension(5,0)));
        crudPanel.add(runButton);

        return crudPanel;
    }

    private void viewModules() {
        int[] selectedModuleIds = selectedModuleIds();
        if (selectedModuleIds.length == 0) {
            if (table.getTable().getRowCount() > 0) {
                messagePanel.setMessage("Please select one or more modules");
            }
            return;
        }   

        for (int moduleId : selectedModuleIds) {
            Module module = null;
            try {
                module = presenter.getModule(moduleId);
            } catch (EmfException e) {
                messagePanel.setError("Failed to get module (ID = " + moduleId + "): " + e.getMessage());
                continue;
            }
            if (module == null)
                continue;
            ModulePropertiesWindow view = new ModulePropertiesWindow(parentConsole, desktopManager, session, ViewMode.VIEW, module, null);
            presenter.displayNewModuleView(view);
        }
    }

    private void editModules() {
        int[] selectedModuleIds = selectedModuleIds();
        if (selectedModuleIds.length == 0) {
            if (table.getTable().getRowCount() > 0) {
                messagePanel.setMessage("Please select one or more modules");
            }
            return;
        }   

        for (int moduleId : selectedModuleIds) {
            Module module = null;
            try {
                module = presenter.getModule(moduleId);
            } catch (EmfException e) {
                messagePanel.setError("Failed to get module (ID = " + moduleId + "): " + e.getMessage());
                continue;
            }
            if (module == null)
                continue;
            ViewMode viewMode = module.getIsFinal() ? ViewMode.VIEW : ViewMode.EDIT;
            if (viewMode == ViewMode.EDIT) {
                try {
                    module = session.moduleService().obtainLockedModule(session.user(), module.getId());
                } catch (EmfException e) {
                    messagePanel.setError("Failed to lock " + module.getName() + ": " + e.getMessage());
                    continue;
                }
                if (!module.isLocked(session.user())) {
                    messagePanel.setError("Failed to lock " + module.getName() + ".");
                    continue;
                }
            } else {
                try {
                    module = presenter.getModule(module.getId());
                } catch (EmfException e) {
                    messagePanel.setError("Failed to get module: " + e.getMessage());
                    continue;
                }
            }
            ModulePropertiesWindow view = new ModulePropertiesWindow(parentConsole, desktopManager, session, viewMode, module, null);
            presenter.displayNewModuleView(view);
        }
    }

    private void lockModules() {
        int[] selectedModuleIds = selectedModuleIds();
        if (selectedModuleIds.length == 0) {
            if (table.getTable().getRowCount() > 0) {
                messagePanel.setMessage("Please select one or more modules");
            }
            return;
        }   

        boolean mustRefresh = false;
        
        try {
            session.moduleService().lockModules(session.user(), selectedModuleIds);
            mustRefresh = true;
        } catch (EmfException e) {
            messagePanel.setError("Failed to lock module(s): " + e.getMessage());
        }

        if (mustRefresh)
            doRefresh();
    }

    private void unlockModules() {
        int[] selectedModuleIds = selectedModuleIds();
        if (selectedModuleIds.length == 0) {
            if (table.getTable().getRowCount() > 0) {
                messagePanel.setMessage("Please select one or more modules");
            }
            return;
        }
        
        boolean mustRefresh = false;
        
        try {
            session.moduleService().unlockModules(session.user(), selectedModuleIds);
            mustRefresh = true;
        } catch (EmfException e) {
            messagePanel.setError("Failed to lock module(s): " + e.getMessage());
        }

        if (mustRefresh)
            doRefresh();
    }

    private void createModule() {
        ModuleTypeVersion moduleTypeVersion = ModulePropertiesWindow.selectModuleTypeVersion(parentConsole, session);
        if (moduleTypeVersion != null) {
            ModulePropertiesWindow view = new ModulePropertiesWindow(parentConsole, desktopManager, session, ViewMode.NEW, null, moduleTypeVersion);
            presenter.displayNewModuleView(view);
        }
    }

    private void copyModules() {
        int[] selectedModuleIds = selectedModuleIds();
        if (selectedModuleIds.length == 0) {
            if (table.getTable().getRowCount() > 0) {
                messagePanel.setMessage("Please select one or more modules");
            }
            return;
        }
        
        for (int moduleId : selectedModuleIds) {
            Module module = null;
            try {
                module = presenter.getModule(moduleId);
            } catch (EmfException e) {
                messagePanel.setError("Failed to get module (ID = " + moduleId + "): " + e.getMessage());
                continue;
            }
            if (module == null)
                continue;
            Module copy = module.deepCopy(session.user());
            ModulePropertiesWindow view = new ModulePropertiesWindow(parentConsole, desktopManager, session, ViewMode.NEW, copy, null);
            presenter.displayNewModuleView(view);
        }
    }

    private void removeModules() {
        messagePanel.clear();
        
        int[] selectedModuleIds = selectedModuleIds();
        if (selectedModuleIds.length == 0) {
            if (table.getTable().getRowCount() > 0) {
                messagePanel.setMessage("Please select one or more modules");
            }
            return;
        }
        
        String message = "Are you sure you want to remove the selected " + selectedModuleIds.length + " module(s)?";
        int selection = JOptionPane.showConfirmDialog(parentConsole, message, "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (selection != JOptionPane.YES_OPTION)
            return;
        
        try {
            int[] removedModuleIds = presenter.doRemove(selectedModuleIds);
            if (removedModuleIds.length == 0)
                messagePanel.setError("No module has been removed.");
            else if (removedModuleIds.length == 1)
                messagePanel.setMessage("One module has been removed. Please Refresh to see the revised list of modules.");
            else
                messagePanel.setMessage(removedModuleIds.length + " modules have been removed. Please Refresh to see the revised list of modules.");
        } catch (EmfException e) {
          JOptionPane.showConfirmDialog(parentConsole, e.getMessage(), "Error", JOptionPane.CLOSED_OPTION);
        }
    }

    private void runModules() {
        messagePanel.clear();
        
        int[] selectedModuleIds = selectedModuleIds();
        if (selectedModuleIds.length == 0) {
            if (table.getTable().getRowCount() > 0) {
                messagePanel.setMessage("Please select one or more modules");
            }
            return;
        }

        String message;
        if (selectedModuleIds.length == 1) {
            message = "Are you sure you want to run this module?";
        } else {
            message = "Are you sure you want to run the selected " + selectedModuleIds.length + " modules?";
        }
        int selection = JOptionPane.showConfirmDialog(parentConsole, message, "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (selection != JOptionPane.YES_OPTION)
            return;
            
        try {
            presenter.runModules(selectedModuleIds);
            if (selectedModuleIds.length == 1) {
                messagePanel.setMessage("The module has been executed.");
            } else {
                messagePanel.setMessage("The " + selectedModuleIds.length + " modules have been executed.");
            }
        } catch (EmfException e) {
          JOptionPane.showConfirmDialog(parentConsole, e.getMessage(), "Error", JOptionPane.CLOSED_OPTION);
        }
    }

    private void compareModules() {
        messagePanel.clear();
        
        int[] selectedModuleIds = selectedModuleIds();
        if (selectedModuleIds.length < 2) {
            messagePanel.setMessage("Please select two modules");
            return;
        }
        if (selectedModuleIds.length > 2) {
            messagePanel.setMessage("Please select only two modules");
            return;
        }

        Module[] modules = new Module[selectedModuleIds.length];
        for (int i = 0; i < selectedModuleIds.length; i++) {
            int moduleId = selectedModuleIds[i];
            modules[i] = null;
            try {
                modules[i] = presenter.getModule(moduleId);
            } catch (EmfException e) {
                messagePanel.setError("Failed to get module (ID = " + moduleId + "): " + e.getMessage());
                return;
            }
            if (modules[i] == null) {
                messagePanel.setError("Failed to get module (ID = " + moduleId + ")");
                return;
            }
        }

        ModuleComparisonWindow view = new ModuleComparisonWindow(session, parentConsole, desktopManager, modules[0], modules[1]);
        presenter.displayModuleComparisonView(view);
    }

    private int[] selectedModuleIds() {
        List selected = table.selected();
        int[] moduleIds = new int[selected.size()];
        int i = 0;
        for (Iterator iter = selected.iterator(); iter.hasNext();) {
            LiteModule liteModule = (LiteModule) iter.next();
            moduleIds[i++] = liteModule.getId();
        }
        return moduleIds;
    }

    public EmfConsole getParentConsole() {
        return this.parentConsole;
    }

    @Override
    public void doRefresh() {
        ConcurrentSkipListMap<Integer, LiteModule> liteModules = presenter.getLiteModules();
        boolean hasData = (liteModules != null) && !liteModules.isEmpty();

        // FIXME these settings are reverted somewhere else
        viewButton.setEnabled(hasData);
        editButton.setEnabled(hasData);
        lockButton.setEnabled(hasData);
        unlockButton.setEnabled(hasData);
        newButton.setEnabled(hasData);
        removeButton.setEnabled(hasData);
        runButton.setEnabled(hasData);

        table.refresh(new ModulesTableData(liteModules));
        panelRefresh();
    }

    @Override
    public void populate() {
        doRefresh();
    }
}
