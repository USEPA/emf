package gov.epa.emissions.framework.client.moduletype;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.CheckBox;
import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.ScrollableComponent;
import gov.epa.emissions.commons.gui.TextArea;
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
import java.util.TreeMap;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class ModuleTypeVersionDatasetWindow extends DisposableInteralFrame implements ModuleTypeVersionDatasetView {
    private ModuleTypeVersionDatasetPresenter presenter;

    private EmfConsole parentConsole;
    private EmfSession session;

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
    private CheckBox isOptional;

    // data
    DatasetType[] datasetTypesCache;
    TreeMap<String, DatasetType> datasetTypeMap;
    String[] datasetTypeNames;

    public ModuleTypeVersionDatasetWindow(EmfConsole parentConsole, DesktopManager desktopManager, EmfSession session,
            ModuleTypeVersion moduleTypeVersion, DatasetType[] datasetTypesCache,
            ViewMode viewMode, ModuleTypeVersionDataset moduleTypeVersionDataset) {
        super(getWindowTitle(viewMode, moduleTypeVersionDataset), new Dimension(800, 600), desktopManager);

        this.moduleTypeVersion = moduleTypeVersion;
        
        this.datasetTypesCache = datasetTypesCache; 
        datasetTypeMap = new TreeMap<String, DatasetType>();
        for(DatasetType datasetType : datasetTypesCache) {
            if (!ModuleTypeVersionDataset.isValidDatasetType(datasetType))
                continue;
            datasetTypeMap.put(datasetType.getName(), datasetType);
        }
        datasetTypeNames = datasetTypeMap.keySet().toArray(new String[0]);

        this.viewMode = viewMode;
        if (viewMode == ViewMode.NEW) {
            this.moduleTypeVersionDataset = new ModuleTypeVersionDataset();
            this.moduleTypeVersionDataset.setModuleTypeVersion(moduleTypeVersion);
            this.moduleTypeVersionDataset.setPlaceholderName("");
            this.moduleTypeVersionDataset.setMode(ModuleTypeVersionDataset.IN);
            this.moduleTypeVersionDataset.setDatasetType(null);
            this.moduleTypeVersionDataset.setDescription("");
            this.moduleTypeVersionDataset.setIsOptional(false);
        } else {
            this.moduleTypeVersionDataset = moduleTypeVersionDataset;
        }
        layout = new JPanel();
        layout.setLayout(new BorderLayout());
        this.parentConsole = parentConsole;
        this.session = session;
        super.getContentPane().add(layout);
    }

    private static String getWindowTitle(ViewMode viewMode, ModuleTypeVersionDataset moduleTypeVersionDataset) {
        switch (viewMode)
        {
            case NEW: return "New Module Type Version Dataset";
            case EDIT: return "Edit Module Type Version Dataset (ID=" + moduleTypeVersionDataset.getId() + ")";
            case VIEW: return "View Module Type Version Dataset (ID=" + moduleTypeVersionDataset.getId() + ")";
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
        layout.removeAll();
        doLayout(layout);
        super.display();
    }

    private JPanel detailsPanel() {
        detailsPanel = new JPanel(new BorderLayout());
        
        JPanel contentPanel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        JPanel modePanel = new JPanel();
        FlowLayout modeLayout = new FlowLayout();
        modeLayout.setHgap(0);
        modeLayout.setVgap(0);
        modePanel.setLayout(modeLayout);
        
        // temporarily disable INOUT datasets (see UP-460)
        // mode = new ComboBox(new String[] {ModuleTypeVersionDataset.IN, ModuleTypeVersionDataset.INOUT, ModuleTypeVersionDataset.OUT});
        mode = new ComboBox(new String[] {ModuleTypeVersionDataset.IN, ModuleTypeVersionDataset.OUT});
        mode.setSelectedItem(moduleTypeVersionDataset.getMode());
        addChangeable(mode);
        modePanel.add(mode);
        modePanel.add(new Label("   "));
        
        isOptional = new CheckBox("Optional", moduleTypeVersionDataset.getIsOptional());
        addChangeable(isOptional);
        if (moduleTypeVersionDataset.isModeOUT()) {
            isOptional.setSelected(false);
            isOptional.setEnabled(false);
        }
        modePanel.add(isOptional);
        
        mode.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                String selectedItem = mode.getSelectedItem().toString();
                if (ModuleTypeVersionParameter.OUT.equals(selectedItem)) {
                    isOptional.setSelected(false);
                    isOptional.setEnabled(false);
                } else {
                    isOptional.setEnabled(true);
                }
            }
        });

        layoutGenerator.addLabelWidgetPair("Mode:", modePanel, contentPanel);

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
        String trimName = name.getText().trim();
        StringBuilder error = new StringBuilder();
        if (!ModuleTypeVersionDataset.isValidPlaceholderName(trimName, error)) {
            messagePanel.setError(error.toString());
            return false;
        }
        Map<String, ModuleTypeVersionDataset> moduleTypeVersionDatasets = moduleTypeVersion.getModuleTypeVersionDatasets();
        if (!trimName.equals(moduleTypeVersionDataset.getPlaceholderName()) && moduleTypeVersionDatasets.containsKey(trimName)) {
            messagePanel.setError("Placeholder " + trimName + " already exists!");
            return false;
        }
        messagePanel.clear();
        return true;
    }

    private void doSave() {
        if (checkTextFields()) {
            try {
                String trimName = name.getText().trim();
                if (!trimName.equals(moduleTypeVersionDataset.getPlaceholderName())) {
                    presenter.doRemove(moduleTypeVersion, moduleTypeVersionDataset);
                    moduleTypeVersionDataset = moduleTypeVersionDataset.deepCopy();
                }
                moduleTypeVersionDataset.setPlaceholderName(name.getText().trim());
                moduleTypeVersionDataset.setMode(mode.getSelectedItem().toString());
                moduleTypeVersionDataset.setDatasetType(datasetTypeMap.get(datasetTypeCB.getSelectedItem()));
                moduleTypeVersionDataset.setDescription(description.getText());
                moduleTypeVersionDataset.setIsOptional(isOptional.isSelected());
                moduleTypeVersionDataset = presenter.doSave(moduleTypeVersionDataset);
                moduleTypeVersion = moduleTypeVersionDataset.getModuleTypeVersion();
                messagePanel.setMessage("Dataset definition saved.");
                resetChanges();
            } catch (EmfException e) {
                messagePanel.setError(e.getMessage());
            }
        }
    }

    private Action saveAction() {
        Action action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                doSave();
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
