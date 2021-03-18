package gov.epa.emissions.framework.client.module;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ConfirmDialog;
import gov.epa.emissions.commons.gui.SelectAwareButton;
import gov.epa.emissions.commons.gui.buttons.CloseButton;
import gov.epa.emissions.commons.gui.buttons.NewButton;
import gov.epa.emissions.commons.gui.buttons.RemoveButton;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.ViewMode;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.module.LiteModule;
import gov.epa.emissions.framework.services.module.Module;
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
import javax.swing.JPanel;

public class RelatedModulesWindow extends DisposableInteralFrame implements RelatedModulesView, RefreshObserver {

    private EmfSession session;

    private EmfConsole parentConsole;

    EmfDataset dataset;
    
    LiteModule[] liteModules;
    
    private RelatedModulesPresenter presenter;

    private SelectableSortFilterWrapper table;

    SelectAwareButton viewButton;
    SelectAwareButton editButton;
    Button lockButton;
    Button unlockButton;
    NewButton newButton;
    SelectAwareButton copyButton;
    RemoveButton removeButton;
    Button runButton;

    private JPanel layout;

    private JPanel tablePanel;

    private MessagePanel messagePanel;

    public RelatedModulesWindow(EmfSession session, EmfConsole parentConsole, DesktopManager desktopManager, EmfDataset dataset) {
        super("View Related Modules - " + dataset.getName(), new Dimension(700, 350), desktopManager);
        super.setName("View Related Modules - " + dataset.getName());

        this.session = session;
        this.parentConsole = parentConsole;

        this.dataset = dataset;
        this.liteModules = new LiteModule[]{};
        
        layout = new JPanel();
        this.getContentPane().add(layout);
    }

    @Override
    public void observe(RelatedModulesPresenter presenter) {
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

        Button button = new RefreshButton(this, "Refresh Related Modules", messagePanel);
        panel.add(button, BorderLayout.EAST);

        return panel;
    }

    private JPanel createTablePanel() {
        tablePanel = new JPanel(new BorderLayout());
        if (liteModules == null || liteModules.length == 0) {
            try {
                liteModules = presenter.getRelatedLiteModules(dataset.getId());
            } catch (EmfException e) {
                // NOTE Auto-generated catch block
                e.printStackTrace();
                messagePanel.setError("Failed to get the related modules: " + e.getMessage());
            }
        }
        table = new SelectableSortFilterWrapper(parentConsole, new RelatedModulesTableData(liteModules), null);
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

        JPanel crudPanel = new JPanel();
        crudPanel.setLayout(new FlowLayout());
        crudPanel.add(viewButton);

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
            LiteModule liteModule = (LiteModule) iter.next();
            try {
                Module module = presenter.getModule(liteModule.getId());
                ModulePropertiesWindow view = new ModulePropertiesWindow(parentConsole, desktopManager, session, ViewMode.VIEW, module, null);
                presenter.displayModuleProperties(view);
            } catch (EmfException e) {
                messagePanel.setError("Failed to get module: " + e.getMessage());
                continue;
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
            liteModules = presenter.getRelatedLiteModules(dataset.getId());
        } catch (EmfException e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
            messagePanel.setError("Failed to get the related modules: " + e.getMessage());
        }
        
        boolean hasData = (liteModules != null) && (liteModules.length > 0);

        // FIXME these settings are reverted somewhere else
        viewButton.setEnabled(hasData);

        table.refresh(new RelatedModulesTableData(liteModules));
        panelRefresh();
    }

    @Override
    public void populate() {
        doRefresh();
    }
}
