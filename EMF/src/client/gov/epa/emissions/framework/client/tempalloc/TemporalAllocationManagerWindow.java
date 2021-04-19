package gov.epa.emissions.framework.client.tempalloc;

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
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ConfirmDialog;
import gov.epa.emissions.commons.gui.SelectAwareButton;
import gov.epa.emissions.commons.gui.buttons.CloseButton;
import gov.epa.emissions.commons.gui.buttons.CopyButton;
import gov.epa.emissions.commons.gui.buttons.NewButton;
import gov.epa.emissions.commons.gui.buttons.RemoveButton;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.tempalloc.editor.TemporalAllocationView;
import gov.epa.emissions.framework.client.tempalloc.editor.TemporalAllocationWindow;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.tempalloc.TemporalAllocation;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.RefreshButton;
import gov.epa.emissions.framework.ui.RefreshObserver;
import gov.epa.emissions.framework.ui.SelectableSortFilterWrapper;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;
import gov.epa.mims.analysisengine.table.sort.SortCriteria;

public class TemporalAllocationManagerWindow extends DisposableInteralFrame implements TemporalAllocationManagerView,
        RefreshObserver, Runnable {

    private TemporalAllocationManagerPresenter presenter;
    
    private TemporalAllocationTableData tableData;
    
    private JPanel tablePanel;
    
    private SelectableSortFilterWrapper table;

    private JPanel layout;

    private MessagePanel messagePanel;

    private EmfConsole parentConsole;

    private EmfSession session;

    private volatile Thread populateThread;
    
    public TemporalAllocationManagerWindow(EmfConsole parentConsole, EmfSession session, DesktopManager desktopManager) {
        super("Temporal Allocation Manager", new Dimension(850, 400), desktopManager);
        
        this.session = session;
        this.parentConsole = parentConsole;

        layout = new JPanel();
        this.getContentPane().add(layout);
    }
    
    public void observe(TemporalAllocationManagerPresenter presenter) {
        this.presenter = presenter;
    }
    
    public void display(TemporalAllocation[] temporalAllocations) {
        doLayout(temporalAllocations, this.session);
        super.display();
        this.populateThread = new Thread(this);
        populateThread.start();
    }
    
    public void run() {
        try {
            //presenter.loadTemporalAllocations();
        } catch (Exception e) {
            messagePanel.setError("Cannot retrieve all temporal allocations.");
        }
        this.populateThread = null;
    }
    
    public void refresh(TemporalAllocation[] temporalAllocations) {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        setupTableModel(temporalAllocations);
        table.refresh(tableData);
        panelRefresh();
        super.refreshLayout();
        setCursor(Cursor.getDefaultCursor());
        this.populateThread = new Thread(this);
        populateThread.start();
    }
    
    private void panelRefresh() {
        tablePanel.removeAll();
        tablePanel.add(table);
        super.refreshLayout();
    }

    public void doRefresh() throws EmfException {
        presenter.doRefresh();
    }
    
    private void doLayout(TemporalAllocation[] temporalAllocations, EmfSession session) {
        layout.removeAll();
        layout.setLayout(new BorderLayout());

        layout.add(createTopPanel(), BorderLayout.NORTH);
        layout.add(tablePanel(temporalAllocations, parentConsole, session), BorderLayout.CENTER);
        layout.add(createControlPanel(), BorderLayout.SOUTH);
    }
    
    private JPanel tablePanel(TemporalAllocation[] temporalAllocations, EmfConsole parentConsole, EmfSession session) {
        setupTableModel(temporalAllocations);
        tablePanel = new JPanel(new BorderLayout());
        table = new SelectableSortFilterWrapper(parentConsole, tableData, sortCriteria());
        tablePanel.add(table, BorderLayout.CENTER);

        return tablePanel;
    }
    
    private void setupTableModel(TemporalAllocation[] temporalAllocations) {
        tableData = new TemporalAllocationTableData(temporalAllocations);
    }
    
    private SortCriteria sortCriteria() {
        String[] columnNames = { "Last Modified" };
        return new SortCriteria(columnNames, new boolean[] { false }, new boolean[] { true });
    }
    
    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        messagePanel = new SingleLineMessagePanel();
        panel.add(messagePanel, BorderLayout.CENTER);

        Button button = new RefreshButton(this, "Refresh Temporal Allocations", messagePanel);
        panel.add(button, BorderLayout.EAST);

        return panel;
    }

    private JPanel createControlPanel() {
        JPanel crudPanel = createCrudPanel();

        JPanel closePanel = new JPanel();
        JButton closeButton = new CloseButton(new AbstractAction() {
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

        String message = "You have asked to open a lot of windows. Do you want to proceed?";
        ConfirmDialog confirmDialog = new ConfirmDialog(message, "Warning", this);

        crudPanel.add(viewButton(confirmDialog));
        crudPanel.add(editButton(confirmDialog));

        Button newButton = new NewButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                createNewTemporalAllocation();
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
                    copySelectedItem();
                } catch (EmfException excp) {
                    messagePanel.setError("Error copying temporal allocations: " + excp.getMessage());
                }
            }
        });
        crudPanel.add(copyButton);

        return crudPanel;
    }
    
    private SelectAwareButton viewButton(ConfirmDialog confirmDialog) {
        Action viewAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                viewTemporalAllocations();
            }
        };
        SelectAwareButton viewButton = new SelectAwareButton("View", viewAction, table, confirmDialog);
        return viewButton;
    }

    private SelectAwareButton editButton(ConfirmDialog confirmDialog) {
        Action editAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                editTemporalAllocations();
            }
        };
        SelectAwareButton editButton = new SelectAwareButton("Edit", editAction, table, confirmDialog);
        return editButton;
    }
    
    private void viewTemporalAllocations() {
        List temporalAllocations = selected();
        if (temporalAllocations.isEmpty()) {
            messagePanel.setMessage("Please select one or more Temporal Allocations to view.");
            return;
        }
        for (int i = 0; i < temporalAllocations.size(); i++) {
            TemporalAllocation temporalAllocation = (TemporalAllocation) temporalAllocations.get(i);
            TemporalAllocationView view = new TemporalAllocationWindow(desktopManager, session, parentConsole);
            try {
                presenter.doView(view, temporalAllocation);
            } catch (EmfException e) {
                messagePanel.setError(e.getMessage());
            }
        }
    }
    
    private void editTemporalAllocations() {
        List temporalAllocations = selected();
        if (temporalAllocations.isEmpty()) {
            messagePanel.setMessage("Please select one or more Temporal Allocations to edit.");
            return;
        }
        for (int i = 0; i < temporalAllocations.size(); i++) {
            TemporalAllocation temporalAllocation = (TemporalAllocation) temporalAllocations.get(i);
            TemporalAllocationView view = new TemporalAllocationWindow(desktopManager, session, parentConsole);
            try {
                presenter.doEdit(view, temporalAllocation);
            } catch (EmfException e) {
                messagePanel.setError(e.getMessage());
            }
        }
    }

    private List selected() {
        return table.selected();
    }
    
    private void createNewTemporalAllocation() {
        TemporalAllocationView view = new TemporalAllocationWindow(desktopManager, session, parentConsole);
        presenter.doNew(view);
    }
    
    private void copySelectedItem() throws EmfException {
        boolean error = false;
        messagePanel.clear();
        List temporalAllocations = selected();
        if (temporalAllocations.isEmpty()) {
            messagePanel.setMessage("Please select one or more temporal allocations.");
            return;
        }
        
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        for (int i = 0; i < temporalAllocations.size(); i++) {
            TemporalAllocation temporalAllocation = (TemporalAllocation) temporalAllocations.get(i);
            try {
                presenter.doCopy(temporalAllocation, session.user());
            } catch (EmfException e) {
                messagePanel.setError(e.getMessage());
                error = true;
            }
        }
        if (!error) doRefresh();
        setCursor(Cursor.getDefaultCursor());
    }

    protected void doRemove() throws EmfException {
        messagePanel.clear();
        List temporalAllocations = selected();
        int numSelected = temporalAllocations.size();
        if (numSelected == 0) {
            messagePanel.setMessage("Please select an item to remove.");
            return;
        }

        String title = "Warning";
        String message = (numSelected == 1) ? 
                "Are you sure you want to remove the selected temporal allocation?" :
                "Are you sure you want to remove the "+numSelected+" selected temporal allocations?";
       int selection = JOptionPane.showConfirmDialog(parentConsole, message, title, JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        if (selection == JOptionPane.YES_OPTION) {
            int[] ids = new int[numSelected];
            for (int i = 0; i < numSelected; i++) {
                ids[i] = ((TemporalAllocation)temporalAllocations.get(i)).getId(); 
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
}
