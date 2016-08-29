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
import gov.epa.emissions.framework.client.ViewMode;
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
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SpringLayout;
import javax.swing.SwingWorker;

public class ModuleTypeVersionPropertiesWindow extends DisposableInteralFrame implements ModuleTypeVersionPropertiesView {
    private ModuleTypeVersionPropertiesPresenter presenter;

    private EmfConsole parentConsole;
    private EmfSession session;
    private static int counter = 0;

    private ViewMode viewMode;
    
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

    public ModuleTypeVersionPropertiesWindow(EmfConsole parentConsole, DesktopManager desktopManager, EmfSession session, ViewMode viewMode, ModuleType moduleType, ModuleTypeVersion moduleTypeVersion) {
        super(getWindowTitle(viewMode, moduleType), new Dimension(800, 600), desktopManager);

        this.parentConsole = parentConsole;
        this.session = session;
        this.viewMode = viewMode;
        
        if (viewMode == ViewMode.NEW) {
            this.moduleType = new ModuleType();
            this.moduleTypeVersion = new ModuleTypeVersion();
            this.moduleTypeVersion.setModuleType(moduleType);
            this.moduleTypeVersion.setVersion(0);
            this.moduleTypeVersion.setAlgorithm("-- Initial version created by " + session.user().getName() + "\n" +
                                                "-- \n" +
                                                "-- TODO: implement the algorithm\n\n");
            this.moduleType.addModuleTypeVersion(this.moduleTypeVersion);
        } else {
            this.moduleTypeVersion = moduleTypeVersion;
            this.moduleType = moduleTypeVersion.getModuleType();
        }

        layout = new JPanel();
        layout.setLayout(new BorderLayout());
        super.getContentPane().add(layout);
    }

    private static String getWindowTitle(ViewMode viewMode, ModuleType moduleType) {
        switch (viewMode)
        {
            case NEW:
                if (moduleType == null)
                    return "Create New Module Type";
                else
                    return "Create New Module Type Version";

            case EDIT: return "Edit Module Type Version";
            
            case VIEW: return "View Module Type Version";
            
            default: return "";
        }
    }
    
    private void doLayout(JPanel layout) {
        messagePanel = new SingleLineMessagePanel();
        layout.add(messagePanel, BorderLayout.NORTH);
        layout.add(tabbedPane(), BorderLayout.CENTER);
        layout.add(createButtonsPanel(), BorderLayout.SOUTH);
    }

    public void observe(ModuleTypeVersionPropertiesPresenter presenter) {
        this.presenter = presenter;
        populateDatasetTypesCache();
    }

    public void display() {
        counter++; // TODO use a different counter for each viewMode
        String name = getWindowTitle(viewMode, moduleType) + " " + counter;
        super.setTitle(name);
        super.setName(name);
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
        name.setText(moduleType.getName());
        name.setEditable(viewMode != ViewMode.VIEW);
        addChangeable(name);
        layoutGenerator.addLabelWidgetPair("Name:", name, formPanel);

        description = new TextArea("description", moduleType.getDescription(), 60, 8);
        description.setEditable(viewMode != ViewMode.VIEW);
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
        algorithm = new TextArea("algorithm", moduleTypeVersion.getAlgorithm(), 60);
        algorithm.setEditable(viewMode != ViewMode.VIEW);
        addChangeable(algorithm);
        ScrollableComponent scrollableAlgorithm = new ScrollableComponent(algorithm);
        scrollableAlgorithm.setMaximumSize(new Dimension(575, 200));
        algorithmPanel.add(scrollableAlgorithm);

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
        saveButton.setEnabled(viewMode != ViewMode.VIEW);
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
        if (!session.user().isAdmin() || (viewMode == ViewMode.VIEW)){
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

        getDatasetTypesTask.done(); // wait if necessary

        for (Iterator iter = selected.iterator(); iter.hasNext();) {
            ModuleTypeVersionDataset moduleTypeVersionDataset = (ModuleTypeVersionDataset) iter.next();
            ModuleTypeVersionDatasetWindow view = new ModuleTypeVersionDatasetWindow(parentConsole, desktopManager, session, moduleTypeVersion, datasetTypesCache, ViewMode.EDIT, moduleTypeVersionDataset);
            presenter.displayNewModuleTypeVersionDatasetView(view);
        }
    }

    private void createDataset() {
        getDatasetTypesTask.done(); // wait if necessary
        ModuleTypeVersionDatasetWindow view = new ModuleTypeVersionDatasetWindow(parentConsole, desktopManager, session, moduleTypeVersion, datasetTypesCache, ViewMode.NEW, null);
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
        if (!session.user().isAdmin() || (viewMode == ViewMode.VIEW)){
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

        for (Iterator iter = selected.iterator(); iter.hasNext();) {
            ModuleTypeVersionParameter moduleTypeVersionParameter = (ModuleTypeVersionParameter) iter.next();
            ModuleTypeVersionParameterWindow view = new ModuleTypeVersionParameterWindow(parentConsole, desktopManager, session, moduleTypeVersion, ViewMode.EDIT, moduleTypeVersionParameter);
            presenter.displayNewModuleTypeVersionParameterView(view);
        }
    }

    private void createParameter() {
        ModuleTypeVersionParameterWindow view = new ModuleTypeVersionParameterWindow(parentConsole, desktopManager, session, moduleTypeVersion, ViewMode.NEW, null);
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
                        
                        if (viewMode == ViewMode.NEW) {
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
                            
                            presenter.addModule(moduleType);
                            viewMode = ViewMode.EDIT;
                        } else {
                            Date date = new Date();
                            moduleType.setName(name.getText());
                            moduleType.setDescription(description.getText());
                            moduleType.setLastModifiedDate(date);
    
                            moduleTypeVersion.setLastModifiedDate(date);
                            moduleTypeVersion.setAlgorithm(algorithm.getText());
                            
                            moduleType = presenter.updateModuleType(moduleType);
                        }
                        messagePanel.setMessage("Saved module type.");
                        ModuleType lockedModuleType = presenter.obtainLockedModuleType(moduleType);
                        if (lockedModuleType == null) {
                            throw new EmfException("Failed to lock module type.");
                        }
                        moduleType = lockedModuleType;
                        
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
