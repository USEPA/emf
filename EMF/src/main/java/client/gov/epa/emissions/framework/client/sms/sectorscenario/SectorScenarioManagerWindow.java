package gov.epa.emissions.framework.client.sms.sectorscenario;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ConfirmDialog;
import gov.epa.emissions.commons.gui.SelectAwareButton;
import gov.epa.emissions.commons.gui.buttons.CopyButton;
import gov.epa.emissions.commons.gui.buttons.NewButton;
import gov.epa.emissions.commons.gui.buttons.RemoveButton;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.ReusableInteralFrame;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.sms.sectorscenario.base.NewSectorScenarioDialog;
import gov.epa.emissions.framework.client.sms.sectorscenario.base.NewSectorScenarioView;
import gov.epa.emissions.framework.client.sms.sectorscenario.editor.EditSectorScenarioView;
import gov.epa.emissions.framework.client.sms.sectorscenario.editor.EditSectorScenarioWindow;
import gov.epa.emissions.framework.client.sms.sectorscenario.viewer.ViewSectorScenarioView;
import gov.epa.emissions.framework.client.sms.sectorscenario.viewer.ViewSectorScenarioWindow;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.sms.SectorScenario;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.RefreshButton;
import gov.epa.emissions.framework.ui.RefreshObserver;
import gov.epa.emissions.framework.ui.SelectableSortFilterWrapper;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;
import gov.epa.mims.analysisengine.table.sort.SortCriteria;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class SectorScenarioManagerWindow extends ReusableInteralFrame implements SectorScenarioManagerView,
        RefreshObserver, Runnable {

    private SectorScenarioManagerPresenter presenter;

    private SectorScenarioTableData tableData;

    private JPanel tablePanel;
    
    private SelectableSortFilterWrapper table;

    private JPanel layout;

    private MessagePanel messagePanel;

    private EmfConsole parentConsole;

    private EmfSession session;
    
    public SectorScenarioManagerWindow(EmfConsole parentConsole, EmfSession session, DesktopManager desktopManager) {
        super("Sector Scenarios Manager", new Dimension(850, 400), desktopManager);
        
        this.session = session;
        this.parentConsole = parentConsole;

        layout = new JPanel();
        this.getContentPane().add(layout);
    }

    public void observe(SectorScenarioManagerPresenterImpl presenter) {
        this.presenter = presenter;
    }

    public void display(SectorScenario[] sectorScenarios) {        
        doLayout(sectorScenarios, this.session);
        super.display();
    }

    public void refresh(SectorScenario[] sectorScenarios) {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        setupTableModel(sectorScenarios);
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

    private void doLayout(SectorScenario[] sectorScenarios, EmfSession session) {
        layout.removeAll();
        layout.setLayout(new BorderLayout());

        layout.add(createTopPanel(), BorderLayout.NORTH);
        layout.add(tablePanel(sectorScenarios, session), BorderLayout.CENTER);
        layout.add(createControlPanel(), BorderLayout.SOUTH);
    }
    
    private JPanel tablePanel(SectorScenario[] sectorScenarios, EmfSession session) {

        setupTableModel(sectorScenarios);
        tablePanel = new JPanel(new BorderLayout());
        table = new SelectableSortFilterWrapper(parentConsole, tableData, sortCriteria());

        tablePanel.add(table, BorderLayout.CENTER);

        return tablePanel;
    }
    
    private void setupTableModel(SectorScenario[] sectorScenarios){
        tableData = new SectorScenarioTableData(sectorScenarios);
    }
    
    private SortCriteria sortCriteria() {
        String[] columnNames = { "Last Modified" };
        return new SortCriteria(columnNames, new boolean[] { false }, new boolean[] { true });
    }
    
    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        messagePanel = new SingleLineMessagePanel();
        panel.add(messagePanel, BorderLayout.CENTER);

        Button button = new RefreshButton(this, "Refresh Sector Scenario", messagePanel);
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

        String message = "You have asked to open a lot of windows. Do you want proceed?";
        ConfirmDialog confirmDialog = new ConfirmDialog(message, "Warning", this);

        crudPanel.add(viewButton(confirmDialog));
        crudPanel.add(editButton(confirmDialog));

        Button newButton = new NewButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e)  {
                try {
                    messagePanel.clear();
                    createNewScenario();
                } catch (EmfException e1) {
                    messagePanel.setError(e1.getMessage());
                }
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
        crudPanel.add(removeButton);
        
        Button copyButton = new CopyButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                
                    try {
                        copySelectedSectorScenarios();
                    } catch (EmfException excp) {
                        messagePanel.setError("Error copying control strategies: " + excp.getMessage());
                    }
            }
        });
        crudPanel.add(copyButton);
//        copyButton.setEnabled(false);

        return crudPanel;
    }

    private SelectAwareButton editButton(ConfirmDialog confirmDialog) {
        Action editAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                editSectorScenarios();
            }

        };
        SelectAwareButton editButton = new SelectAwareButton("Edit", editAction, table, confirmDialog);
        return editButton;
    }

    private SelectAwareButton viewButton(ConfirmDialog confirmDialog) {
        Action viewAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                viewSectorScenarios();
            }
        };

        SelectAwareButton viewButton = new SelectAwareButton("View", viewAction, table, confirmDialog);
//        viewButton.setEnabled(false);
        return viewButton;
    }

    private void editSectorScenarios() {
        messagePanel.clear();
        List sectorScenarios = selected();
        if (sectorScenarios.isEmpty()) {
            messagePanel.setMessage("Please select one or more Sector Scenarios");
            return;
        }

        for (int i = 0; i < sectorScenarios.size(); i++) {
            SectorScenario sectorScenario = (SectorScenario) sectorScenarios.get(i);
            EditSectorScenarioView view = new EditSectorScenarioWindow("Edit Sector Scenario", desktopManager, session, parentConsole);

            try {
                presenter.doEdit(view, sectorScenario);
            } catch (EmfException e) {
                messagePanel.setError("Problem:" + e.getMessage());
            }
        }
    }

    private void viewSectorScenarios() {
        
        this.messagePanel.clear();
        
        List sectorScenarios = selected();
        if (sectorScenarios.isEmpty()) {
            this.messagePanel.setMessage("Please select one or more Sector Scenarios");
        }
        else {
            
            for (int i = 0; i < sectorScenarios.size(); i++) {
                
                SectorScenario sectorScenario = (SectorScenario) sectorScenarios.get(i);
                ViewSectorScenarioView view = new ViewSectorScenarioWindow(desktopManager, session, parentConsole);
                
                try {
                    presenter.doView(view, sectorScenario);
                } catch (EmfException e) {
                    messagePanel.setError(e.getMessage());
                }
            }
        }
        
    }
    
    protected void doRemove() throws EmfException {
        messagePanel.clear();
        SectorScenario[] records = (SectorScenario[])selected().toArray(new SectorScenario[0]);

        if (records.length == 0) {
            messagePanel.setError("Please select an item to remove.");
            return;
        }

        String title = "Warning";
        String message = (records.length == 1) ? 
                "Are you sure you want to remove the selected sector scenarios?" :
                "Are you sure you want to remove the "+records.length+" selected scenarios?";
       int selection = JOptionPane.showConfirmDialog(parentConsole, message, title, JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        if (selection == JOptionPane.YES_OPTION) {
            int[] ids = new int[records.length];
            for (int i = 0; i < records.length; i++) {
                ids[i] = records[i].getId(); 
            }
            try {
                presenter.doRemove(ids);
            } catch (EmfException ex) {
                throw ex;
            } finally {
                doRefresh();
                setCursor(Cursor.getDefaultCursor());
            }
        }
    }
    
    private void copySelectedSectorScenarios() throws EmfException {
        boolean error = false;
        messagePanel.clear();
        List scenarios = selected();
        if (scenarios.isEmpty()) {
            messagePanel.setMessage("Please select one or more sector scenarios.");
            return;
        }
        
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        for (Iterator iter = scenarios.iterator(); iter.hasNext();) {
            SectorScenario element = (SectorScenario) iter.next();
            
            try {
                presenter.doSaveCopiedSectorScenarios(element.getId(), session.user());
            } catch (Exception e) {
//                setCursor(Cursor.getDefaultCursor());
                messagePanel.setError(e.getMessage());
                error = true;
            }
        }
        if (!error) doRefresh();
        setCursor(Cursor.getDefaultCursor());
    }

    private List selected() {
        return table.selected();
    }

    private void createNewScenario() throws EmfException {
        NewSectorScenarioView view = new NewSectorScenarioDialog(parentConsole, session, desktopManager);
        presenter.doNew(view);   
    }

    public EmfConsole getParentConsole() {
        return parentConsole;
    }

    public void run() {
        // NOTE Auto-generated method stub
        
    }
    
    public void addNewSSToTableData(SectorScenario sectorScenario){
        List<SectorScenario> sectorScenarios = new ArrayList<SectorScenario>();
        sectorScenarios.addAll(Arrays.asList(tableData.sources()));
        sectorScenarios.add(sectorScenario);
        refresh(sectorScenarios.toArray(new SectorScenario[0]));
    }


}
