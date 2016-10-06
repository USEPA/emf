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
import gov.epa.emissions.framework.services.data.EmfDataset;
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
        super("Module Type Version Manager", new Dimension(700, 350), desktopManager);
        super.setName("moduleTypeVersionManager");

        this.session = session;
        this.parentConsole = parentConsole;
        this.viewMode = viewMode;
        this.moduleType = moduleType;
        
        layout = new JPanel();
        this.getContentPane().add(layout);
    }

    public void observe(ModuleTypeVersionsManagerPresenter presenter) {
        this.presenter = presenter;
        try {
            moduleType = session.moduleService().obtainLockedModuleType(session.user(), moduleType);
        } catch (EmfException e) {
            messagePanel.setError("Could not lock: " + moduleType.getName() + "." + e.getMessage());
        }
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
        if (!session.user().isAdmin() || (viewMode == ViewMode.VIEW)){
            editButton.setEnabled(false);
            newButton.setEnabled(false);
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
            ModuleTypeVersionPropertiesWindow view = new ModuleTypeVersionPropertiesWindow(parentConsole, desktopManager, session, ViewMode.EDIT, moduleTypeVersion);
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
            ModuleTypeVersionPropertiesWindow view = new ModuleTypeVersionPropertiesWindow(parentConsole, desktopManager, session, ViewMode.EDIT, newModuleTypeVersion);
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

        String message = "Are you sure you want to remove the selected " + selected.size() + " module type version(s)?";
        int selection = JOptionPane.showConfirmDialog(parentConsole, message, "Warning", JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (selection == JOptionPane.YES_OPTION) {
            try {
                presenter.doRemove(selected.toArray(new ModuleTypeVersion[0]));
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

    @Override
    public void doRefresh() throws EmfException {
        refresh();
    }
}
