package gov.epa.emissions.framework.client.data.datasettype;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ConfirmDialog;
import gov.epa.emissions.commons.gui.SelectAwareButton;
import gov.epa.emissions.commons.gui.buttons.CloseButton;
import gov.epa.emissions.commons.gui.buttons.NewButton;
import gov.epa.emissions.commons.gui.buttons.RemoveButton;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.ReusableInteralFrame;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
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

public class DatasetTypesManagerWindow extends ReusableInteralFrame implements DatasetTypesManagerView, RefreshObserver {

    private DatasetTypesManagerPresenter presenter;

    //private SortFilterSelectModel selectModel;

    private SelectableSortFilterWrapper table;

    private JPanel layout;
    
    private JPanel tablePanel;

    private MessagePanel messagePanel;

    private EmfConsole parentConsole;

    private EmfSession session;

    public DatasetTypesManagerWindow(EmfSession session, EmfConsole parentConsole, DesktopManager desktopManager) {
        super("Dataset Type Manager", new Dimension(700, 350), desktopManager);
        super.setName("datasetTypeManager");
        
        this.session = session;
        this.parentConsole = parentConsole;
        
        layout = new JPanel();
        this.getContentPane().add(layout);
    }

    public void observe(DatasetTypesManagerPresenter presenter) {
        this.presenter = presenter;
    }

    public void refresh(DatasetType[] types) {
        //doLayout(types);
        table.refresh(new DatasetTypesTableData(types));
        panelRefresh();
    }
    
    private void panelRefresh() {
        tablePanel.removeAll();
        tablePanel.add(table);
        super.refreshLayout();
    }

    public void display(DatasetType[] types) {
        doLayout(types);
        super.display();
    }

    private void doLayout(DatasetType[] types) {
        layout.removeAll();
        layout.setLayout(new BorderLayout());

        layout.add(createTopPanel(), BorderLayout.NORTH);
        layout.add(tablePanel(types), BorderLayout.CENTER);
        layout.add(createControlPanel(), BorderLayout.SOUTH);
    }

    private JPanel tablePanel(DatasetType[] types) {
        tablePanel = new JPanel(new BorderLayout());
        table = new SelectableSortFilterWrapper(parentConsole, new DatasetTypesTableData(types), null);
        tablePanel.add(table);
        return tablePanel;
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        messagePanel = new SingleLineMessagePanel();
        panel.add(messagePanel, BorderLayout.CENTER);

        Button button = new RefreshButton(this, "Refresh Dataset Types", messagePanel);
        panel.add(button, BorderLayout.EAST);

        return panel;
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
                viewDatasetTypes();
            }
        };
        SelectAwareButton viewButton = new SelectAwareButton("View", viewAction, table, confirmDialog);

        Action editAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                editDatasetTypes();
            }
        };
        SelectAwareButton editButton = new SelectAwareButton("Edit", editAction, table, confirmDialog);

        Action createAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                createDatasetType();
            }
        };
        Button newButton = new NewButton(createAction);
        
        Action removeAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                removeDatasetType();
            }
        };
        Button removeButton = new RemoveButton(removeAction);
        JPanel crudPanel = new JPanel();
        crudPanel.setLayout(new FlowLayout());
        crudPanel.add(viewButton);
        crudPanel.add(editButton);
        crudPanel.add(newButton);
        crudPanel.add(removeButton);
        if (!session.user().isAdmin()){
            editButton.setEnabled(false);
            newButton.setEnabled(false);
            removeButton.setEnabled(false);
        }

        return crudPanel;
    }

    private void viewDatasetTypes() {
        List selected = selected();
        for (Iterator iter = selected.iterator(); iter.hasNext();) {
            DatasetType type = (DatasetType) iter.next();
            try {
                presenter.doView(type, viewableView());
            } catch (EmfException e) {
                messagePanel.setError("Could not display: " + type.getName() + "." + e.getMessage());
                break;
            }
        }
    }

    private void editDatasetTypes() {
        List selected = selected();
        if (selected.isEmpty()) {
            messagePanel.setMessage("Please select one or more dataset types");
            return;
        }   
        
        for (Iterator iter = selected.iterator(); iter.hasNext();) {
            DatasetType type = (DatasetType) iter.next();
            try {
                presenter.doEdit(type, editableView(), viewableView());
            } catch (EmfException e) {
                messagePanel.setError("Could not display: " + type.getName() + "." + e.getMessage());
                break;
            }
        }
    }

    private void createDatasetType() {
        NewDatasetTypeWindow view = new NewDatasetTypeWindow(parentConsole, desktopManager, session);
        presenter.displayNewDatasetTypeView(view);
    }
    
    private void removeDatasetType() {
        messagePanel.clear();
        List<?> selected = selected();
        if (selected.isEmpty()) {
            messagePanel.setMessage("Please select one or more dataset types");
            return;
        }   

        String message = "Are you sure you want to remove the selected " + selected.size() + " dataset type(s)?";
        int selection = JOptionPane.showConfirmDialog(parentConsole, message, "Warning", JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (selection == JOptionPane.YES_OPTION) {
            try {
                presenter.doRemove(selected.toArray(new DatasetType[0]));
                messagePanel.setMessage(selected.size()
                        + " dataset types have been removed. Please Refresh to see the revised list of types.");
            } catch (EmfException e) {
              JOptionPane.showConfirmDialog(parentConsole, e.getMessage(), "Error", JOptionPane.CLOSED_OPTION);
            }
        }
    }


    private List selected() {
        return table.selected();
    }

    private ViewableDatasetTypeWindow viewableView() {
        ViewableDatasetTypeWindow view = new ViewableDatasetTypeWindow(desktopManager);
        return view;
    }

    private EditableDatasetTypeView editableView() {
        EditableDatasetTypeWindow view = new EditableDatasetTypeWindow(session,parentConsole, desktopManager);
        return view;
    }

    public EmfConsole getParentConsole() {
        return this.parentConsole;
    }

    public void doRefresh() throws EmfException {
        presenter.doRefresh();
    }
}
