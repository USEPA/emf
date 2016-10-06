package gov.epa.emissions.framework.client.moduletype;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.ScrollableComponent;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.gui.buttons.CloseButton;
import gov.epa.emissions.commons.gui.buttons.SaveButton;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.ViewMode;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.module.ModuleTypeVersion;
import gov.epa.emissions.framework.services.module.ModuleTypeVersionDataset;
import gov.epa.emissions.framework.services.module.ModuleTypeVersionParameter;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class ModuleTypeVersionDatasetWindow extends DisposableInteralFrame implements ModuleTypeVersionDatasetView {
    private ModuleTypeVersionDatasetPresenter presenter;

    private EmfConsole parentConsole;
    private EmfSession session;
    private static int counter = 0;

    private ViewMode viewMode;
    private ModuleTypeVersion moduleTypeVersion;
    private ModuleTypeVersionDataset moduleTypeVersionDataset;

    // layout
    private JPanel layout;
    private SingleLineMessagePanel messagePanel;
    private JPanel detailsPanel;

    private ComboBox mode;
    private TextField name;
    private ComboBox datasetTypeCB;
    private TextArea description;

    // data
    DatasetType[] datasetTypesCache;
    Map<String, DatasetType> datasetTypeMap;
    String[] datasetTypeNames;

    public ModuleTypeVersionDatasetWindow(EmfConsole parentConsole, DesktopManager desktopManager, EmfSession session,
            ModuleTypeVersion moduleTypeVersion, DatasetType[] datasetTypesCache,
            ViewMode viewMode, ModuleTypeVersionDataset moduleTypeVersionDataset) {
        super(getWindowTitle(viewMode), new Dimension(800, 600), desktopManager);

        this.moduleTypeVersion = moduleTypeVersion;
        
        this.datasetTypesCache = datasetTypesCache; 
        datasetTypeMap = new HashMap<String, DatasetType>();
        datasetTypeNames = new String[datasetTypesCache.length];
        int i = 0;
        for(DatasetType datasetType : datasetTypesCache) {
            datasetTypeMap.put(datasetType.getName(), datasetType);
            datasetTypeNames[i++] = datasetType.getName();
        }
        Arrays.sort(datasetTypeNames);

        this.viewMode = viewMode;
        if (viewMode == ViewMode.NEW) {
            this.moduleTypeVersionDataset = new ModuleTypeVersionDataset();
            this.moduleTypeVersionDataset.setModuleTypeVersion(moduleTypeVersion);
            this.moduleTypeVersionDataset.setMode(ModuleTypeVersionDataset.IN);
        } else {
            this.moduleTypeVersionDataset = moduleTypeVersionDataset;
        }
        layout = new JPanel();
        layout.setLayout(new BorderLayout());
        this.parentConsole = parentConsole;
        this.session = session;
        super.getContentPane().add(layout);
    }

    private static String getWindowTitle(ViewMode viewMode) {
        switch (viewMode)
        {
            case NEW: return "Create New Module Type Version Dataset";
            case EDIT: return "Edit Module Type Version Dataset";
            case VIEW: return "View Module Type Version Dataset";
            default: return "";
        }
    }
    
    private void doLayout(JPanel layout) {
        messagePanel = new SingleLineMessagePanel();
        layout.add(messagePanel, BorderLayout.NORTH);
        layout.add(detailsPanel(), BorderLayout.CENTER);
        layout.add(buttonsPanel(), BorderLayout.SOUTH);
    }

    public void observe(ModuleTypeVersionDatasetPresenter presenter) {
        this.presenter = presenter;
    }

    public void display() {
        counter++; // TODO use a different counter for each viewMode
        String name = getWindowTitle(viewMode) + " " + counter;
        super.setTitle(name);
        super.setName(name);
        layout.removeAll();
        doLayout(layout);

        super.display();
    }

    private JPanel detailsPanel() {
        detailsPanel = new JPanel(new BorderLayout());
        
        JPanel contentPanel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        mode = new ComboBox(new String[] {ModuleTypeVersionDataset.IN, ModuleTypeVersionDataset.INOUT, ModuleTypeVersionDataset.OUT});
        mode.setSelectedItem(moduleTypeVersionDataset.getMode());
        addChangeable(mode);
        layoutGenerator.addLabelWidgetPair("Mode:", mode, contentPanel);

        datasetTypeCB = new ComboBox(datasetTypeNames);
        if (moduleTypeVersionDataset.getDatasetType() == null) {
            datasetTypeCB.setSelectedIndex(0);
        } else {
            datasetTypeCB.setSelectedItem(moduleTypeVersionDataset.getDatasetType().getName());
        }
        addChangeable(datasetTypeCB);
        datasetTypeCB.setMaximumSize(new Dimension(575, 20));
        layoutGenerator.addLabelWidgetPair("Dataset Type:", datasetTypeCB, contentPanel);

        name = new TextField("name", 60);
        name.setText(moduleTypeVersionDataset.getPlaceholderName());
        addChangeable(name);
        name.setMaximumSize(new Dimension(575, 20));
        layoutGenerator.addLabelWidgetPair("Placeholder:", name, contentPanel);

        description = new TextArea("description", moduleTypeVersionDataset.getDescription(), 60, 8);
        addChangeable(description);
        ScrollableComponent descScrollableTextArea = new ScrollableComponent(description);
        descScrollableTextArea.setMaximumSize(new Dimension(575, 200));
        layoutGenerator.addLabelWidgetPair("Description:", descScrollableTextArea, contentPanel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(contentPanel, 4, 2, // rows, cols
                10, 10, // initialX, initialY
                10, 10);// xPad, yPad

        detailsPanel.add(contentPanel, BorderLayout.NORTH);
        return detailsPanel;
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

    private boolean checkTextFields() {
        StringBuilder error = new StringBuilder();
        if (!ModuleTypeVersionDataset.isValidPlaceholderName(name.getText(), error)) {
            messagePanel.setError(error.toString());
            return false;
        }
        messagePanel.clear();
        return true;
    }

    private Action saveAction() {
        Action action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                if (checkTextFields()) {
                    try {
                        resetChanges();
//                        Map<String, ModuleTypeVersionDataset> moduleTypeVersionDatasets = moduleTypeVersion.getModuleTypeVersionDatasets();
//                        if (moduleTypeVersionDatasets.containsKey(name.getText())) {
//                            throw new EmfException("Dataset " + name.getText() + " already exists!");
//                        }
                        moduleTypeVersionDataset.setMode(mode.getSelectedItem().toString());
                        moduleTypeVersionDataset.setPlaceholderName(name.getText());
                        moduleTypeVersionDataset.setDatasetType(datasetTypeMap.get(datasetTypeCB.getSelectedItem()));
                        moduleTypeVersionDataset.setDescription(description.getText());
                        presenter.doSave(moduleTypeVersion, moduleTypeVersionDataset);
                        messagePanel.setMessage("Dataset definition saved.");
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
