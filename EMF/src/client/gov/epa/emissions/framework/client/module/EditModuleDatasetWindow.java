package gov.epa.emissions.framework.client.module;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.gui.buttons.CloseButton;
import gov.epa.emissions.commons.gui.buttons.SaveButton;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.Label;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.ViewMode;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.data.dataset.InputDatasetSelectionDialog;
import gov.epa.emissions.framework.client.data.dataset.InputDatasetSelectionPresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.module.ModuleDataset;
import gov.epa.emissions.framework.services.module.ModuleTypeVersionDataset;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class EditModuleDatasetWindow extends DisposableInteralFrame implements EditModuleDatasetView {
    private EditModuleDatasetPresenter presenter;

    private EmfConsole parentConsole;
    private EmfSession session;
    private static int counter = 0;

    private ModuleDataset moduleDataset;
    private ModuleTypeVersionDataset moduleTypeVersionDataset;
    private boolean isOUT;
    
    // layout
    private JPanel layout;
    private SingleLineMessagePanel messagePanel;
    private JPanel detailsPanel;

    private Label mode;
    private Label placeholderName;
    private Label datasetType;
    private ComboBox outputMethod;
    private TextField datasetName;
//    private CheckBox overwriteExisting;
    private Button selectDataset;
    private Label existingDatasetName;
    private ComboBox existingVersion;

    EmfDataset dataset;
    String[] existingVersions;
    
    public EditModuleDatasetWindow(EmfConsole parentConsole, DesktopManager desktopManager, EmfSession session, ModuleDataset moduleDataset) {
        super(getWindowTitle(moduleDataset), new Dimension(800, 400), desktopManager);

        this.moduleDataset = moduleDataset;
        this.moduleTypeVersionDataset = moduleDataset.getModule().getModuleTypeVersion().getModuleTypeVersionDatasets().get(moduleDataset.getPlaceholderName());
        this.isOUT = moduleTypeVersionDataset.getMode().equals(ModuleTypeVersionDataset.OUT);

        layout = new JPanel();
        layout.setLayout(new BorderLayout());
        this.parentConsole = parentConsole;
        this.session = session;
        super.getContentPane().add(layout);
    }

    private static String getWindowTitle(ModuleDataset moduleDataset) {
        return "Edit Module Dataset (ID=" + moduleDataset.getId() + ")";
    }
    
    private void doLayout(JPanel layout) {
        messagePanel = new SingleLineMessagePanel();
        layout.add(messagePanel, BorderLayout.NORTH);
        layout.add(detailsPanel(), BorderLayout.NORTH);
        layout.add(buttonsPanel(), BorderLayout.SOUTH);
    }

    public void observe(EditModuleDatasetPresenter presenter) {
        this.presenter = presenter;
    }

    public void display() {
        layout.removeAll();
        doLayout(layout);
        super.display();
    }

    private JPanel detailsPanel() {
        detailsPanel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();
        int rows = 0;
        
        mode = new Label(moduleTypeVersionDataset.getMode());
        layoutGenerator.addLabelWidgetPair("Mode:", mode, detailsPanel);
        rows++;

        placeholderName = new Label(moduleTypeVersionDataset.getPlaceholderName());
        layoutGenerator.addLabelWidgetPair("Name/Placeholder:", placeholderName, detailsPanel);
        rows++;

        datasetType = new Label(moduleTypeVersionDataset.getDatasetType().getName());
        layoutGenerator.addLabelWidgetPair("Dataset Type:", datasetType, detailsPanel);
        rows++;

        if (moduleDataset.getDatasetId() != null) {
            try {
                dataset = session.dataService().getDataset(moduleDataset.getDatasetId());
            }
            catch (EmfException ex) {
                // TODO handle exception
            }
        }
        
        if (isOUT) {
            outputMethod = new ComboBox(new String[] {"New Dataset", "Replace Dataset"});
            outputMethod.addActionListener(selectOutputMethodActionListener());
            addChangeable(outputMethod);
            layoutGenerator.addLabelWidgetPair("Output Mathod:", outputMethod, detailsPanel);
            rows++;

            datasetName = new TextField("datasetName", 40);
            layoutGenerator.addLabelWidgetPair("Dataset Name:", datasetName, detailsPanel);
            rows++;
            
            selectDataset = new Button("Select Dataset", selectDatasetAction());
            layoutGenerator.addLabelWidgetPair("", selectDataset, detailsPanel);
            rows++;
    
            if (moduleDataset.getOutputMethod().equals(ModuleDataset.NEW)) {
                outputMethod.setSelectedIndex(0);
                datasetName.setText(moduleDataset.getDatasetNamePattern());
                datasetName.setEditable(true);
                selectDataset.setEnabled(false);
            } else { // REPLACE
                outputMethod.setSelectedIndex(1);
                if (dataset != null) {
                    datasetName.setText(dataset.getName());
                } else {
                    datasetName.setText("");
                }
                datasetName.setEditable(false);
                selectDataset.setEnabled(true);
            }
            
        } else { // IN or INOUT
            
            selectDataset = new Button("Select Dataset", selectDatasetAction());
            layoutGenerator.addLabelWidgetPair("", selectDataset, detailsPanel);
            rows++;
    
            existingDatasetName = new Label("");
            existingVersions = new String[]{};
            if (dataset != null) {
                try {
                    existingDatasetName.setText(dataset.getName());
                    Version[] versions = session.dataEditorService().getVersions(dataset.getId());
                    existingVersions = new String[versions.length];
                    int i = 0;
                    for(Version v : versions) {
                        existingVersions[i++] = v.getVersion() + " - " + v.getName() + (v.isFinalVersion() ? " - Final" : ""); 
                    }
                }
                catch (EmfException ex) {
                    existingDatasetName.setText(""); // TODO handle exception
                    existingVersions = new String[]{};
                }
            }
            layoutGenerator.addLabelWidgetPair("Existing Dataset:", existingDatasetName, detailsPanel);
            rows++;
    
            existingVersion = new ComboBox("Select Version", existingVersions);
            if (moduleDataset.getVersion() != null && existingVersions.length > moduleDataset.getVersion()) {
                existingVersion.setSelectedItem(existingVersions[moduleDataset.getVersion()]);
            }
            layoutGenerator.addLabelWidgetPair("Dataset Version:", existingVersion, detailsPanel);
            rows++;
        }
        
        // Lay out the panel.
        layoutGenerator.makeCompactGrid(detailsPanel, rows, 2, // rows, cols
                10, 10, // initialX, initialY
                10, 10);// xPad, yPad

        return detailsPanel;
    }

    private Action selectDatasetAction() {
        Action action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                try {
                    DatasetType[] datasetTypes = new DatasetType[] { moduleDataset.getModuleTypeVersionDataset().getDatasetType() };
                    InputDatasetSelectionDialog view = new InputDatasetSelectionDialog(parentConsole);
                    InputDatasetSelectionPresenter presenter = new InputDatasetSelectionPresenter(view, session, datasetTypes);
                    if (datasetTypes.length == 1)
                        presenter.display(datasetTypes[0], true);
                    else
                        presenter.display(null, true);
    
                    EmfDataset[] datasets = presenter.getDatasets();
                    if (datasets.length > 0) {
                        dataset = datasets[0];
                        if (isOUT) {
                            datasetName.setText(dataset.getName());
                        } else {
                            existingDatasetName.setText(dataset.getName());
                            Version[] versions = session.dataEditorService().getVersions(dataset.getId());
                            existingVersions = new String[versions.length];
                            int i = 0;
                            for(Version v : versions) {
                                existingVersions[i++] = v.getVersion() + " - " + v.getName() + (v.isFinalVersion() ? " - Final" : ""); 
                            }
                            existingVersion.resetModel(existingVersions);
                        }
                    } else {
                        dataset = null;
                    }
                } catch (Exception ex) {
                    messagePanel.setError(ex.getMessage());
                }
            }
        };

        return action;
    }

    private ActionListener selectOutputMethodActionListener() {
        ActionListener actionListener = new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                try {
                    if (outputMethod.getSelectedIndex() == 0) { // NEW DATASET
                        moduleDataset.setOutputMethod(ModuleDataset.NEW);
                    } else { // REPLACE DATASET
                        moduleDataset.setOutputMethod(ModuleDataset.REPLACE);
                    }
                    if (moduleDataset.getOutputMethod().equals(ModuleDataset.NEW)) {
                        datasetName.setText(moduleDataset.getDatasetNamePattern());
                        datasetName.setEditable(true);
                        selectDataset.setEnabled(false);
                    } else { // REPLACE
                        if (dataset != null) {
                            datasetName.setText(dataset.getName());
                        } else {
                            datasetName.setText("");
                        }
                        datasetName.setEditable(false);
                        selectDataset.setEnabled(true);
                    }
                } catch (Exception ex) {
                    messagePanel.setError(ex.getMessage());
                }
            }
        };

        return actionListener;
    }

    private JPanel buttonsPanel() {
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

    private void clear() {
        messagePanel.clear();
    }

    private boolean checkInputFields() {
        messagePanel.clear();
        if (isOUT) {
            if (moduleDataset.getOutputMethod().equals(ModuleDataset.NEW)) {
                if (datasetName.getText().equals("")) {
                    messagePanel.setError("You must enter a dataset name pattern.");
                } else {
                    return true;
                }
            } else { // REPLACE
                if (dataset == null) {
                    messagePanel.setError("You must select a dataset.");
                } else {
                    return true;
                }
            }
        } else { // IN or INOUT
            if (dataset == null) {
                messagePanel.setError("You must select a dataset.");
            } else if (existingVersion.getSelectedIndex() <= 0) {
                messagePanel.setError("You must select a dataset version.");
            } else {
                return true;
            }
        }
        return false;
    }

    private Action saveAction() {
        Action action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                if (checkInputFields()) {
                    try {
                        resetChanges();
                        if (isOUT) {
                            if (moduleDataset.getOutputMethod().equals(ModuleDataset.NEW)) {
                                moduleDataset.setDatasetId(null);
                                moduleDataset.setVersion(null);
                                moduleDataset.setDatasetNamePattern(datasetName.getText());
                                moduleDataset.setOverwriteExisting(false);
                            } else { // REPLACE
                                moduleDataset.setDatasetId(dataset.getId());
                                moduleDataset.setVersion(0);
                                moduleDataset.setDatasetNamePattern(null);
                                moduleDataset.setOverwriteExisting(null);
                            }
                        } else { // IN or INOUT
                            moduleDataset.setDatasetId(dataset.getId());
                            moduleDataset.setVersion(existingVersion.getSelectedIndex() - 1);
                            moduleDataset.setDatasetNamePattern(null);
                            moduleDataset.setOverwriteExisting(null);
                        }
                        presenter.doSave(moduleDataset);
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

}
