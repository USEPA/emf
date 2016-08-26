package gov.epa.emissions.framework.client.moduletype;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ConfirmDialog;
import gov.epa.emissions.commons.gui.ScrollableComponent;
import gov.epa.emissions.commons.gui.SelectAwareButton;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.gui.buttons.CloseButton;
import gov.epa.emissions.commons.gui.buttons.NewButton;
import gov.epa.emissions.commons.gui.buttons.RemoveButton;
import gov.epa.emissions.commons.gui.buttons.SaveButton;
import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.util.ComponentUtility;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.module.ModuleType;
import gov.epa.emissions.framework.services.module.ModuleTypeVersion;
import gov.epa.emissions.framework.services.module.ModuleTypeVersionDataset;
import gov.epa.emissions.framework.services.module.ModuleTypeVersionParameter;
import gov.epa.emissions.framework.ui.SelectableSortFilterWrapper;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SpringLayout;
import javax.swing.SwingWorker;

public class NewModuleTypeWindow extends DisposableInteralFrame implements NewModuleTypeView {
    private NewModuleTypePresenter presenter;

    private EmfConsole parentConsole;
    private EmfSession session;
    private static int counter = 0;

    private ModuleType moduleType;
    private ModuleTypeVersion moduleTypeVersion;

    // layout
    private JPanel layout;
    private SingleLineMessagePanel messagePanel;
    private JTabbedPane tabbedPane;
    private JPanel summaryPanel;
    private JPanel datasetsPanel;
    private JPanel parametersPanel;
    private JPanel algorithmPanel;

    // summary
    private TextField name;
    private TextArea description;

    // datasets
    private GetDatasetTypesTask getDatasetTypesTask; 
    private DatasetType[] datasetTypesCache;
    private JPanel datasetsTablePanel;
    private SelectableSortFilterWrapper datasetsTable;
    private ModuleTypeVersionDatasetsTableData datasetsTableData;

    // parameters
    private JPanel parametersTablePanel;
    private SelectableSortFilterWrapper parametersTable;
    private ModuleTypeVersionParametersTableData parametersTableData;

    // algorithm
    private TextArea algorithm;

    // Instances of javax.swing.SwingWorker are not reusable, so we create new instances as needed.
    class GetDatasetTypesTask extends SwingWorker<DatasetType[], Void> {

        private Container parentContainer;

        public GetDatasetTypesTask(Container parentContainer) {
            this.parentContainer = parentContainer;
        }

        /*
         * Main task. Executed in background thread.
         * don't update gui here
         */
        @Override
        public DatasetType[] doInBackground() throws EmfException  {
            return presenter.getDatasetTypes();
        }

        /*
         * Executed in event dispatching thread
         */
        @Override
        public void done() {
            try {
                //make sure something didn't happen
                datasetTypesCache = get();
            } catch (InterruptedException e1) {
//                messagePanel.setError(e1.getMessage());
//                setErrorMsg(e1.getMessage());
            } catch (ExecutionException e1) {
//                messagePanel.setError(e1.getCause().getMessage());
//                setErrorMsg(e1.getCause().getMessage());
            } finally {
//                this.parentContainer.setCursor(null); //turn off the wait cursor
//                this.parentContainer.
//                ComponentUtility.enableComponents(parentContainer, true);
//                this.parentContainer.setCursor(null); //turn off the wait cursor
            }
        }
    };

    public NewModuleTypeWindow(EmfConsole parentConsole, DesktopManager desktopManager, EmfSession session) {
        super("Create New Module Type", new Dimension(800, 600), desktopManager);

        moduleType = new ModuleType();
        moduleTypeVersion = new ModuleTypeVersion();
        moduleTypeVersion.setModuleType(moduleType);
        moduleTypeVersion.setVersion(0);
        moduleType.addModuleTypeVersion(moduleTypeVersion);

        layout = new JPanel();
        layout.setLayout(new BorderLayout());
        this.parentConsole = parentConsole;
        this.session = session;
        super.getContentPane().add(layout);
    }

    private void doLayout(JPanel layout) {
        messagePanel = new SingleLineMessagePanel();
        layout.add(messagePanel, BorderLayout.NORTH);
        layout.add(tabbedPane(), BorderLayout.CENTER);
        layout.add(createButtonsPanel(), BorderLayout.SOUTH);
    }

    public void observe(NewModuleTypePresenter presenter) {
        this.presenter = presenter;
        populateDatasetTypesCache();
    }

    public void display() {
        counter++;
        String name = "Create New Module Type " + counter;
        super.setTitle(name);
        super.setName("createNewModuleType:" + counter);
        layout.removeAll();
        doLayout(layout);

        super.display();
    }

    private JTabbedPane tabbedPane() {
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Summary", summaryPanel());
        tabbedPane.addTab("Datasets", datasetsPanel());
        tabbedPane.addTab("Parameters", parametersPanel());
        tabbedPane.addTab("Algorithm", algorithmPanel());
        return tabbedPane;
    }

    private JPanel summaryPanel() {
        summaryPanel = new JPanel(new BorderLayout());

        JPanel formPanel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        name = new TextField("name", 60);
        name.setMaximumSize(new Dimension(575, 20));
        addChangeable(name);
        layoutGenerator.addLabelWidgetPair("Name:", name, formPanel);

        description = new TextArea("description", "", 60, 8);
        addChangeable(description);
        ScrollableComponent descScrollableTextArea = new ScrollableComponent(description);
        descScrollableTextArea.setMaximumSize(new Dimension(575, 200));
        layoutGenerator.addLabelWidgetPair("Description:", descScrollableTextArea, formPanel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(formPanel, 2, 2, // rows, cols
                10, 10, // initialX, initialY
                10, 10);// xPad, yPad

        summaryPanel.add(formPanel, BorderLayout.PAGE_START);
        return summaryPanel;
    }

    private JPanel datasetsPanel() {
        datasetsTablePanel = new JPanel(new BorderLayout());
        datasetsTableData = new ModuleTypeVersionDatasetsTableData(moduleTypeVersion.getModuleTypeVersionDatasets());
        datasetsTable = new SelectableSortFilterWrapper(parentConsole, datasetsTableData, null);
        datasetsTablePanel.add(datasetsTable);

        datasetsPanel = new JPanel(new BorderLayout());
        datasetsPanel.add(datasetsTablePanel, BorderLayout.CENTER);
        datasetsPanel.add(datasetsCrudPanel(), BorderLayout.SOUTH);

        return datasetsPanel;
    }

    private JPanel parametersPanel() {
        parametersTablePanel = new JPanel(new BorderLayout());
        parametersTableData = new ModuleTypeVersionParametersTableData(moduleTypeVersion.getModuleTypeVersionParameters());
        parametersTable = new SelectableSortFilterWrapper(parentConsole, parametersTableData, null);
        parametersTablePanel.add(parametersTable);

        parametersPanel = new JPanel(new BorderLayout());
        parametersPanel.add(parametersTablePanel, BorderLayout.CENTER);
        parametersPanel.add(parametersCrudPanel(), BorderLayout.SOUTH);

        return parametersPanel;
    }

    private JPanel algorithmPanel() {
        algorithmPanel = new JPanel(new BorderLayout());
        algorithm = new TextArea("algorithm", "", 60);
        algorithm.setText("-- Initial version created by " + session.user().getName() + "\n" +
                          "-- \n" +
                          "-- TODO: implement the algorithm\n\n");
        addChangeable(algorithm);
        algorithmPanel.add(algorithm);

        return algorithmPanel;
    }

    private JPanel createButtonsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel container = new JPanel();
        FlowLayout layout = new FlowLayout();
        layout.setHgap(20);
        layout.setVgap(25);
        container.setLayout(layout);

        Button saveButton = new SaveButton(saveAction());
        container.add(saveButton);
        container.add(new CloseButton("Close", closeAction()));
        getRootPane().setDefaultButton(saveButton);

        panel.add(container, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel datasetsCrudPanel() {
        String message = "You have asked to open a lot of windows. Do you wish to proceed?";
        ConfirmDialog confirmDialog = new ConfirmDialog(message, "Warning", this);

        Action editAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                editDatasets();
            }
        };
        SelectAwareButton editButton = new SelectAwareButton("Edit", editAction, datasetsTable, confirmDialog);

        Action createAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                createDataset();
            }
        };
        Button newButton = new NewButton(createAction);

        Action removeAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                removeDatasets();
            }
        };
        Button removeButton = new RemoveButton(removeAction);
        
        JPanel crudPanel = new JPanel();
        crudPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        crudPanel.add(newButton);
        crudPanel.add(editButton);
        crudPanel.add(removeButton);
        if (!session.user().isAdmin()){
            newButton.setEnabled(false);
            editButton.setEnabled(false);
            removeButton.setEnabled(false);
        }

        return crudPanel;
    }

    private void editDatasets() {
        List selected = selectedDatasets();
        if (selected.isEmpty()) {
            messagePanel.setMessage("Please select one or more datasets");
            return;
        }   

//        for (Iterator iter = selected.iterator(); iter.hasNext();) {
//            ModuleType type = (ModuleType) iter.next();
//            try {
//                presenter.doEdit(type, editableView(), viewableView());
//            } catch (EmfException e) {
//                messagePanel.setError("Could not display: " + type.getName() + "." + e.getMessage());
//                break;
//            }
//        }
    }

    private void createDataset() {
        getDatasetTypesTask.done(); // wait if necessary
        NewModuleTypeVersionDatasetWindow view = new NewModuleTypeVersionDatasetWindow(parentConsole, desktopManager, session, moduleTypeVersion, datasetTypesCache);
        presenter.displayNewModuleTypeVersionDatasetView(view);
    }

    public void refreshDatasets() {
        datasetsTableData = new ModuleTypeVersionDatasetsTableData(moduleTypeVersion.getModuleTypeVersionDatasets());
        datasetsTable.refresh(datasetsTableData);
    }

    private void removeDatasets() {
        messagePanel.clear();
        List<?> selected = selectedDatasets();
        if (selected.isEmpty()) {
            messagePanel.setMessage("Please select one or more datasets");
            return;
        }   

        String message = "Are you sure you want to remove the selected " + selected.size() + " dataset(s)?";
        int selection = JOptionPane.showConfirmDialog(parentConsole, message, "Warning", JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (selection == JOptionPane.YES_OPTION) {
            try {
                for(ModuleTypeVersionDataset moduleTypeVersionDataset : selected.toArray(new ModuleTypeVersionDataset[0])) {
                    moduleTypeVersion.removeModuleTypeVersionDataset(moduleTypeVersionDataset);
                }
                messagePanel.setMessage("Removed " + selected.size() + " dataset(s)");
                refreshDatasets();
            } catch (Exception e) {
                JOptionPane.showConfirmDialog(parentConsole, e.getMessage(), "Error", JOptionPane.CLOSED_OPTION);
            }
        }
    }

    private List selectedDatasets() {
        return datasetsTable.selected();
    }

    private JPanel parametersCrudPanel() {
        String message = "You have asked to open a lot of windows. Do you wish to proceed?";
        ConfirmDialog confirmDialog = new ConfirmDialog(message, "Warning", this);

        Action editAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                editParameters();
            }
        };
        SelectAwareButton editButton = new SelectAwareButton("Edit", editAction, parametersTable, confirmDialog);

        Action createAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                createParameter();
            }
        };
        Button newButton = new NewButton(createAction);

        Action removeAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                removeParameters();
            }
        };
        Button removeButton = new RemoveButton(removeAction);

        JPanel crudPanel = new JPanel();
        crudPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        crudPanel.add(newButton);
        crudPanel.add(editButton);
        crudPanel.add(removeButton);
        if (!session.user().isAdmin()){
            newButton.setEnabled(false);
            editButton.setEnabled(false);
            removeButton.setEnabled(false);
        }

        return crudPanel;
    }

    private void editParameters() {
        List selected = selectedParameters();
        if (selected.isEmpty()) {
            messagePanel.setMessage("Please select one or more parameters");
            return;
        }   

//        for (Iterator iter = selected.iterator(); iter.hasNext();) {
//            ModuleType type = (ModuleType) iter.next();
//            try {
//                presenter.doEdit(type, editableView(), viewableView());
//            } catch (EmfException e) {
//                messagePanel.setError("Could not display: " + type.getName() + "." + e.getMessage());
//                break;
//            }
//        }
    }

    private void createParameter() {
        NewModuleTypeVersionParameterWindow view = new NewModuleTypeVersionParameterWindow(parentConsole, desktopManager, session, moduleTypeVersion);
        presenter.displayNewModuleTypeVersionParameterView(view);
    }

    public void refreshParameters() {
        parametersTableData = new ModuleTypeVersionParametersTableData(moduleTypeVersion.getModuleTypeVersionParameters());
        parametersTable.refresh(parametersTableData);
    }

    private void removeParameters() {
        messagePanel.clear();
        List<?> selected = selectedParameters();
        if (selected.isEmpty()) {
            messagePanel.setMessage("Please select one or more parameters");
            return;
        }   

        String message = "Are you sure you want to remove the selected " + selected.size() + " parameter(s)?";
        int selection = JOptionPane.showConfirmDialog(parentConsole, message, "Warning", JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (selection == JOptionPane.YES_OPTION) {
            try {
                for(ModuleTypeVersionParameter moduleTypeVersionParameter : selected.toArray(new ModuleTypeVersionParameter[0])) {
                    moduleTypeVersion.removeModuleTypeVersionParameter(moduleTypeVersionParameter);
                }
                messagePanel.setMessage("Removed " + selected.size() + " parameter(s)");
                refreshParameters();
            } catch (Exception e) {
                JOptionPane.showConfirmDialog(parentConsole, e.getMessage(), "Error", JOptionPane.CLOSED_OPTION);
            }
        }
    }

    private List selectedParameters() {
        return parametersTable.selected();
    }

    private void clear() {
        messagePanel.clear();
    }

    private boolean checkTextFields() {
        if (name.getText().equals(""))
            messagePanel.setError("Name field should be a non-empty string.");
        else{
            messagePanel.clear();
            return true;
        }

        return false;
    }

    private Action saveAction() {
        Action action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                if (checkTextFields()) {
                    try {
                        resetChanges();
                        
                        Date date = new Date();
                        moduleType.setName(name.getText());
                        moduleType.setDescription(description.getText());
                        moduleType.setCreationDate(date);
                        moduleType.setLastModifiedDate(date);
                        moduleType.setCreator(session.user());
                        moduleType.setDefaultVersion(0);

                        moduleTypeVersion.setName("Initial Version");
                        String versionDescription = "Initial version created on " + CustomDateFormat.format_YYYY_MM_DD_HH_MM(date) + " by " + session.user().getName();
                        moduleTypeVersion.setDescription(versionDescription);
                        moduleTypeVersion.setCreationDate(date);
                        moduleTypeVersion.setLastModifiedDate(date);
                        moduleTypeVersion.setCreator(session.user());
                        moduleTypeVersion.setBaseVersion(0);
                        moduleTypeVersion.setAlgorithm(algorithm.getText());
                        moduleTypeVersion.setIsFinal(false);
                        
                        presenter.doSave(moduleType);
                    } catch (EmfException e) {
                        messagePanel.setError(e.getMessage());
                    }
                }
            }
        };

        return action;
    }

    public void windowClosing() {
        doClose();
    }

    private Action closeAction() {
        Action action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                doClose();
            }
        };

        return action;
    }

    private void doClose() {
        if (shouldDiscardChanges())
            presenter.doClose();
    }

    public void populateDatasetTypesCache() {
        // long running methods.....
        // this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        // ComponentUtility.enableComponents(this, false);
        getDatasetTypesTask = new GetDatasetTypesTask(this);
        getDatasetTypesTask.execute();
    }
}
