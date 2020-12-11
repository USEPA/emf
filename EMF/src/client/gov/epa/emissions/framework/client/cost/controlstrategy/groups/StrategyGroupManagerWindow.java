package gov.epa.emissions.framework.client.cost.controlstrategy.groups;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JPanel;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ConfirmDialog;
import gov.epa.emissions.commons.gui.SelectAwareButton;
import gov.epa.emissions.commons.gui.buttons.NewButton;
import gov.epa.emissions.commons.gui.buttons.RemoveButton;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.ReusableInteralFrame;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.controlStrategy.StrategyGroup;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.RefreshButton;
import gov.epa.emissions.framework.ui.RefreshObserver;
import gov.epa.emissions.framework.ui.SelectableSortFilterWrapper;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;
import gov.epa.mims.analysisengine.table.sort.SortCriteria;

public class StrategyGroupManagerWindow extends ReusableInteralFrame implements StrategyGroupManagerView,
        RefreshObserver {

    private StrategyGroupManagerPresenter presenter;
    
    private StrategyGroupTableData tableData;

    private JPanel tablePanel;
    
    private SelectableSortFilterWrapper table;
    
    private JPanel layout;

    private MessagePanel messagePanel;

    private EmfConsole parentConsole;

    private EmfSession session;
    
    public StrategyGroupManagerWindow(EmfConsole parentConsole, EmfSession session, DesktopManager desktopManager) {
        super("Control Strategy Groups", new Dimension(600, 400), desktopManager);
        
        this.session = session;
        this.parentConsole = parentConsole;
        
        layout = new JPanel();
        this.getContentPane().add(layout);
    }
    
    public void observe(StrategyGroupManagerPresenter presenter) {
        this.presenter = presenter;
    }

    public void display(StrategyGroup[] strategyGroups) throws EmfException {
        doLayout(strategyGroups, this.session);
        super.display();
    }
    
    public void refresh(StrategyGroup[] strategyGroups) {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        setupTableModel(strategyGroups);
        table.refresh(tableData);
        panelRefresh();
        super.refreshLayout();
        setCursor(Cursor.getDefaultCursor());
    }
    
    private void panelRefresh() {
        tablePanel.removeAll();
        tablePanel.add(table);
        super.refreshLayout();
    }

    public void doRefresh() throws EmfException {
        presenter.doRefresh();
    }
    
    private void doLayout(StrategyGroup[] strategyGroups, EmfSession session) {
        layout.removeAll();
        layout.setLayout(new BorderLayout());

        layout.add(createTopPanel(), BorderLayout.NORTH);
        layout.add(tablePanel(strategyGroups, parentConsole, session), BorderLayout.CENTER);
        layout.add(createControlPanel(), BorderLayout.SOUTH);
    }
    
    private JPanel tablePanel(StrategyGroup[] strategyGroups, EmfConsole parentConsole, EmfSession session) {
        setupTableModel(strategyGroups);
        tablePanel = new JPanel(new BorderLayout());
        table = new SelectableSortFilterWrapper(parentConsole, tableData, sortCriteria());
        tablePanel.add(table, BorderLayout.CENTER);

        return tablePanel;
    }
    
    private void setupTableModel(StrategyGroup[] strategyGroups){
        tableData = new StrategyGroupTableData(strategyGroups);
    }
    
    private SortCriteria sortCriteria() {
        String[] columnNames = { "Name" };
        return new SortCriteria(columnNames, new boolean[] { true }, new boolean[] { false });
    }
    
    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        messagePanel = new SingleLineMessagePanel();
        panel.add(messagePanel, BorderLayout.CENTER);

        Button button = new RefreshButton(this, "Refresh Control Strategy Groups", messagePanel);
        panel.add(button, BorderLayout.EAST);

        return panel;
    }

    private JPanel createControlPanel() {
        JPanel crudPanel = createCrudPanel();

        JPanel closePanel = new JPanel();
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                presenter.doClose();
            }
        });
        closeButton.setMnemonic('l');
        closePanel.add(closeButton);
        getRootPane().setDefaultButton(closeButton);

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BorderLayout());

        controlPanel.add(crudPanel, BorderLayout.WEST);
        controlPanel.add(closePanel, BorderLayout.EAST);

        return controlPanel;
    }
    
    private JPanel createCrudPanel() {
        JPanel crudPanel = new JPanel();
        crudPanel.setLayout(new FlowLayout());

        String message = "You have asked to open a lot of windows. Do you want to proceed?";
        ConfirmDialog confirmDialog = new ConfirmDialog(message, "Warning", this);

        crudPanel.add(editButton(confirmDialog));

        Button newButton = new NewButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                createNewStrategyGroup();
            }
        });
        crudPanel.add(newButton);

        Button removeButton = new RemoveButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                try {
                    doRemove();
                } catch (EmfException exception) {
                    messagePanel.setError(exception.getMessage());
                }
            }
        });
        removeButton.setEnabled(false);
        crudPanel.add(removeButton);

        return crudPanel;
    }

    private SelectAwareButton editButton(ConfirmDialog confirmDialog) {
        Action editAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                editStrategyGroups();
            }
        };
        SelectAwareButton editButton = new SelectAwareButton("Edit", editAction, table, confirmDialog);
        return editButton;
    }
    
    private void editStrategyGroups() {
        List groups = selected();
        if (groups.isEmpty()) {
            messagePanel.setMessage("Please select one or more Control Strategy Groups to edit");
            return;
        }
        for (int i = 0; i < groups.size(); i++) {
            StrategyGroup group = (StrategyGroup) groups.get(i);
            StrategyGroupView view = new StrategyGroupWindow(desktopManager, session, parentConsole);
            try {
                presenter.doEdit(view, group);
            } catch (EmfException e) {
                messagePanel.setError(e.getMessage());
            }
        }
    }

    private List selected() {
        return table.selected();
    }
    
    private void createNewStrategyGroup() {
        StrategyGroupView view = new StrategyGroupWindow(desktopManager, session, parentConsole);
        presenter.doNew(view);
    }
    
    private void doRemove() throws EmfException {
        
    }
}
