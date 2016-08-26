package gov.epa.emissions.framework.client.module;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ComboBox;
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
import gov.epa.emissions.framework.client.Label;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.util.ComponentUtility;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.module.Module;
import gov.epa.emissions.framework.services.module.ModuleDataset;
import gov.epa.emissions.framework.services.module.ModuleParameter;
import gov.epa.emissions.framework.services.module.ModuleType;
import gov.epa.emissions.framework.services.module.ModuleTypeVersion;
import gov.epa.emissions.framework.services.module.ModuleTypeVersionDataset;
import gov.epa.emissions.framework.services.module.ModuleTypeVersionParameter;
import gov.epa.emissions.framework.ui.SelectableSortFilterWrapper;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;
import gov.epa.emissions.framework.client.data.dataset.InputDatasetSelectionDialog;
import gov.epa.emissions.framework.client.data.dataset.InputDatasetSelectionPresenter;
import gov.epa.emissions.framework.client.moduletype.ModuleTypeVersionDatasetsTableData;
import gov.epa.emissions.framework.client.moduletype.ModuleTypeVersionSelectionDialog;
import gov.epa.emissions.framework.client.moduletype.ModuleTypeVersionSelectionPresenter;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SpringLayout;

public class NewModuleWindow extends DisposableInteralFrame implements NewModuleView {
    private NewModulePresenter presenter;

    private EmfConsole parentConsole;
    private EmfSession session;
    private static int counter = 0;

    private Date date;
    private Module module;
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
    private JPanel historyPanel;

    // summary
    private Label moduleTypeName;
    private Label moduleTypeVersionNumber;
    private Button selectModuleTypeVersion;
    private TextField name;
    private TextArea  description;

    // datasets
    private JPanel datasetsTablePanel;
    private SelectableSortFilterWrapper datasetsTable;
    private ModuleDatasetsTableData datasetsTableData;

    // parameters
    private JPanel parametersTablePanel;
    private SelectableSortFilterWrapper parametersTable;
    private ModuleParametersTableData parametersTableData;

    // algorithm
    private TextArea algorithm;

    // history
    
    public NewModuleWindow(EmfConsole parentConsole, DesktopManager desktopManager, EmfSession session) {
        super("Create New Module", new Dimension(800, 600), desktopManager);

        layout = new JPanel();
        layout.setLayout(new BorderLayout());
        this.parentConsole = parentConsole;
        this.session = session;
        super.getContentPane().add(layout);
        
        date = new Date();
        module = new Module();
        module.setName("");
        module.setDescription("");
        module.setCreationDate(date);
        module.setLastModifiedDate(date);
        module.setCreator(session.user());
        module.setIsFinal(false);
    }

    private void selectModuleTypeVersion() {
        ModuleTypeVersionSelectionDialog selectionView = new ModuleTypeVersionSelectionDialog(parentConsole);
        ModuleTypeVersionSelectionPresenter selectionPresenter = new ModuleTypeVersionSelectionPresenter(selectionView, session);
        try {
            selectionPresenter.display();
        } catch (Exception e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
        }
        moduleTypeVersion = selectionView.getSelectedModuleTypeVersion();
        moduleType = moduleTypeVersion.getModuleType();
        
        module.setModuleTypeVersion(moduleTypeVersion);

        module.clearModuleDatasets();
        for(ModuleTypeVersionDataset moduleTypeVersionDataset : moduleTypeVersion.getModuleTypeVersionDatasets().values()) {
            ModuleDataset moduleDataset = new ModuleDataset();
            moduleDataset.setModule(module);
            moduleDataset.setPlaceholderName(moduleTypeVersionDataset.getPlaceholderName());
            if (moduleTypeVersionDataset.isModeOUT()) {
                moduleDataset.setOutputMethod(ModuleDataset.NEW);
                moduleDataset.setDatasetNamePattern(null);
                moduleDataset.setOverwriteExisting(null);
            } else {
                moduleDataset.setDatasetId(null);
                moduleDataset.setVersion(null);
            }
            module.addModuleDataset(moduleDataset);
        }
        
        module.clearModuleParameters();
        for(ModuleTypeVersionParameter moduleTypeVersionParameter : moduleTypeVersion.getModuleTypeVersionParameters().values()) {
            if (moduleTypeVersionParameter.isModeOUT()) continue;
            ModuleParameter moduleParameter = new ModuleParameter();
            moduleParameter.setModule(module);
            moduleParameter.setParameterName(moduleTypeVersionParameter.getParameterName());
            moduleParameter.setValue("");
            module.addModuleParameter(moduleParameter);
        }
    }

    private void refreshModuleTypeVersion() {
        moduleTypeName.setText(moduleType.getName());
        moduleTypeVersionNumber.setText(String.valueOf(moduleTypeVersion.getVersion()));
        datasetsTableData = new ModuleDatasetsTableData(module.getModuleDatasets(), session);
        datasetsTable.refresh(datasetsTableData);
        parametersTableData = new ModuleParametersTableData(module.getModuleParameters());
        parametersTable.refresh(parametersTableData);
        algorithm.setText(moduleTypeVersion.getAlgorithm());
    }
    
    private Action selectModuleTypeVersionAction() {
        Action action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                try {
                    selectModuleTypeVersion();
                    refreshModuleTypeVersion();
                } catch (Exception ex) {
                    messagePanel.setError(ex.getMessage());
                }
            }
        };

        return action;
    }

    private void doLayout(JPanel layout) {
        selectModuleTypeVersion();
        messagePanel = new SingleLineMessagePanel();
        layout.add(messagePanel, BorderLayout.NORTH);
        layout.add(tabbedPane(), BorderLayout.CENTER);
        layout.add(createButtonsPanel(), BorderLayout.SOUTH);
    }

    public void observe(NewModulePresenter presenter) {
        this.presenter = presenter;
    }

    public void display() {
        counter++;
        String name = "Create New Module " + counter;
        super.setTitle(name);
        super.setName("createNewModule:" + counter);
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
        tabbedPane.addTab("History", historyPanel());
        return tabbedPane;
    }

    private JPanel summaryPanel() {
        summaryPanel = new JPanel(new BorderLayout());

        JPanel formPanel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        moduleTypeName = new Label(moduleType.getName());
        layoutGenerator.addLabelWidgetPair("Module Type:", moduleTypeName, formPanel);

        moduleTypeVersionNumber = new Label(String.valueOf(moduleTypeVersion.getVersion()));
        layoutGenerator.addLabelWidgetPair("Version:", moduleTypeVersionNumber, formPanel);

        selectModuleTypeVersion = new Button("Select Module Type Version", selectModuleTypeVersionAction());
        layoutGenerator.addLabelWidgetPair("", selectModuleTypeVersion, formPanel);

        name = new TextField("name", 60);
        name.setMaximumSize(new Dimension(575, 20));
        addChangeable(name);
        layoutGenerator.addLabelWidgetPair("Module Name:", name, formPanel);

        description = new TextArea("description", "", 60, 8);
        addChangeable(description);
        ScrollableComponent descScrollableTextArea = new ScrollableComponent(description);
        descScrollableTextArea.setMaximumSize(new Dimension(575, 200));
        layoutGenerator.addLabelWidgetPair("Description:", descScrollableTextArea, formPanel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(formPanel, 5, 2, // rows, cols
                10, 10, // initialX, initialY
                10, 10);// xPad, yPad

        summaryPanel.add(formPanel, BorderLayout.PAGE_START);
        return summaryPanel;
    }

    private JPanel datasetsPanel() {
        datasetsTablePanel = new JPanel(new BorderLayout());
        datasetsTableData = new ModuleDatasetsTableData(module.getModuleDatasets(), session);
        datasetsTable = new SelectableSortFilterWrapper(parentConsole, datasetsTableData, null);
        datasetsTablePanel.add(datasetsTable);

        datasetsPanel = new JPanel(new BorderLayout());
        datasetsPanel.add(datasetsTablePanel, BorderLayout.CENTER);
        datasetsPanel.add(datasetsCrudPanel(), BorderLayout.SOUTH);

        return datasetsPanel;
    }

    private JPanel parametersPanel() {
        parametersTablePanel = new JPanel(new BorderLayout());
        parametersTableData = new ModuleParametersTableData(module.getModuleParameters());
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
        algorithm.setText(moduleTypeVersion.getAlgorithm());
        algorithm.setEditable(false);
        algorithmPanel.add(algorithm);

        return algorithmPanel;
    }

    private JPanel historyPanel() {
        historyPanel = new JPanel(new BorderLayout());
        return historyPanel;
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

        JPanel crudPanel = new JPanel();
        crudPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        crudPanel.add(editButton);
        if (!session.user().isAdmin()){
            editButton.setEnabled(false);
        }

        return crudPanel;
    }

    private void editDatasets() {
        List selected = selectedDatasets();
        if (selected.isEmpty()) {
            messagePanel.setMessage("Please select one or more datasets");
            return;
        }   

        for (Iterator iter = selected.iterator(); iter.hasNext();) {
            ModuleDataset moduleDataset = (ModuleDataset) iter.next();
            try {
                EditModuleDatasetWindow view = new EditModuleDatasetWindow(parentConsole, desktopManager, session, moduleDataset);
                EditModuleDatasetPresenter presenter = new EditModuleDatasetPresenter(session, view, this);
                presenter.doDisplay();
            }
            catch (Exception e) {
                messagePanel.setError("Could not edit: " + moduleDataset.getPlaceholderName() + "." + e.getMessage());
                break;
            }
        }
    }

    private List selectedDatasets() {
        return datasetsTable.selected();
    }

    public void refreshDatasets() {
        datasetsTableData = new ModuleDatasetsTableData(module.getModuleDatasets(), session);
        datasetsTable.refresh(datasetsTableData);
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

        JPanel crudPanel = new JPanel();
        crudPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        crudPanel.add(editButton);
        if (!session.user().isAdmin()){
            editButton.setEnabled(false);
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
            ModuleParameter moduleParameter = (ModuleParameter) iter.next();
            try {
                EditModuleParameterWindow view = new EditModuleParameterWindow(parentConsole, desktopManager, session, moduleParameter);
                EditModuleParameterPresenter presenter = new EditModuleParameterPresenter(session, view, this);
                presenter.doDisplay();
            }
            catch (Exception e) {
                messagePanel.setError("Could not edit: " + moduleParameter.getParameterName() + "." + e.getMessage());
                break;
            }
        }
    }

    private List selectedParameters() {
        return parametersTable.selected();
    }

    public void refreshParameters() {
        parametersTableData = new ModuleParametersTableData(module.getModuleParameters());
        parametersTable.refresh(parametersTableData);
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
                        module.setName(name.getText());
                        module.setDescription(description.getText());
                        module.setCreationDate(date);
                        module.setLastModifiedDate(date);
                        module.setCreator(session.user());

                        presenter.doSave(module);
                    }
                    catch (EmfException e) {
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
}
