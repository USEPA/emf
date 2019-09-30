package gov.epa.emissions.framework.client.moduletype;

import gov.epa.emissions.commons.gui.Button;
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
import gov.epa.emissions.framework.services.module.ModuleTypeVersionSubmodule;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class ModuleTypeVersionSubmoduleWindow extends DisposableInteralFrame implements ModuleTypeVersionSubmoduleView {
    private ModuleTypeVersionSubmodulePresenter presenter;

    private EmfConsole parentConsole;
    private EmfSession session;

    private ViewMode viewMode;
    private ModuleTypeVersion compositeModuleTypeVersion;
    private ModuleTypeVersionSubmodule moduleTypeVersionSubmodule;

    // layout
    private JPanel layout;
    private SingleLineMessagePanel messagePanel;
    private JPanel detailsPanel;

    private TextField name;
    private Label     moduleTypeName;
    private Label     moduleTypeVersionNumber;
    private Button    selectModuleTypeVersion;
    private TextArea  description;

    public ModuleTypeVersionSubmoduleWindow(EmfConsole parentConsole, DesktopManager desktopManager, EmfSession session,
            ModuleTypeVersion compositeModuleTypeVersion, ViewMode viewMode, ModuleTypeVersionSubmodule moduleTypeVersionSubmodule) {
        super(getWindowTitle(viewMode, moduleTypeVersionSubmodule), new Dimension(800, 600), desktopManager);

        this.compositeModuleTypeVersion = compositeModuleTypeVersion;
        this.viewMode = viewMode;
        if (viewMode == ViewMode.NEW) {
            this.moduleTypeVersionSubmodule = new ModuleTypeVersionSubmodule();
            this.moduleTypeVersionSubmodule.setCompositeModuleTypeVersion(compositeModuleTypeVersion);
            this.moduleTypeVersionSubmodule.setName("");
            this.moduleTypeVersionSubmodule.setModuleTypeVersion(null);
            this.moduleTypeVersionSubmodule.setDescription("");
        } else {
            this.moduleTypeVersionSubmodule = moduleTypeVersionSubmodule;
        }
        layout = new JPanel();
        layout.setLayout(new BorderLayout());
        this.parentConsole = parentConsole;
        this.session = session;
        super.getContentPane().add(layout);
    }

    private static String getWindowTitle(ViewMode viewMode, ModuleTypeVersionSubmodule moduleTypeVersionSubmodule) {
        switch (viewMode)
        {
            case NEW: return "New Module Type Version Submodule";
            case EDIT: return "Edit Module Type Version Submodule (ID=" + moduleTypeVersionSubmodule.getId() + ")";
            case VIEW: return "View Module Type Version Submodule (ID=" + moduleTypeVersionSubmodule.getId() + ")";
            default: return "";
        }
    }

    public static ModuleTypeVersion selectModuleTypeVersion(EmfConsole parentConsole, EmfSession session, ModuleTypeVersion initialModuleTypeVersion) {
        ModuleTypeVersionSelectionDialog selectionView = new ModuleTypeVersionSelectionDialog(parentConsole, initialModuleTypeVersion);
        ModuleTypeVersionSelectionPresenter selectionPresenter = new ModuleTypeVersionSelectionPresenter(selectionView, session);
        try {
            selectionPresenter.display();
        } catch (Exception e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
        }
        return selectionView.getSelectedModuleTypeVersion();
    }
    
    public static ModuleTypeVersion selectModuleTypeVersion(EmfConsole parentConsole, EmfSession session) {
        return selectModuleTypeVersion(parentConsole, session, null);
    }
    
    private void setModuleTypeVersion(ModuleTypeVersion moduleTypeVersion) {
        if (this.moduleTypeVersionSubmodule.getModuleTypeVersion() != null && this.moduleTypeVersionSubmodule.getModuleTypeVersion().getId() == moduleTypeVersion.getId())
            return; // nothing to do
        
        this.moduleTypeVersionSubmodule.setModuleTypeVersion(moduleTypeVersion);
    }

    private void refreshModuleTypeVersion() {
        moduleTypeName.setText(this.moduleTypeVersionSubmodule.getModuleTypeVersion().getModuleType().getName());
        moduleTypeVersionNumber.setText(String.valueOf(this.moduleTypeVersionSubmodule.getModuleTypeVersion().getVersion()));
    }
    
    private void doSelectModuleTypeVersion() {
        try {
            ModuleTypeVersion newModuleTypeVersion = selectModuleTypeVersion(parentConsole, session, moduleTypeVersionSubmodule.getModuleTypeVersion());
            if (newModuleTypeVersion != null && !newModuleTypeVersion.equals(moduleTypeVersionSubmodule.getModuleTypeVersion())) {
                setModuleTypeVersion(newModuleTypeVersion);
                refreshModuleTypeVersion();
            }
        } catch (Exception ex) {
            messagePanel.setError(ex.getMessage());
        }
    }

    private Action selectModuleTypeVersionAction() {
        Action action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                doSelectModuleTypeVersion();
            }
        };

        return action;
    }

    private void doLayout(JPanel layout) {
        messagePanel = new SingleLineMessagePanel();
        layout.add(messagePanel, BorderLayout.NORTH);
        layout.add(detailsPanel(), BorderLayout.CENTER);
        layout.add(buttonsPanel(), BorderLayout.SOUTH);
    }

    public void observe(ModuleTypeVersionSubmodulePresenter presenter) {
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

        name = new TextField("name", 60);
        name.setText(moduleTypeVersionSubmodule.getName());
        addChangeable(name);
        name.setMaximumSize(new Dimension(575, 20));
        layoutGenerator.addLabelWidgetPair("Name:", name, contentPanel);

        if (moduleTypeVersionSubmodule.getModuleTypeVersion() == null) {
            moduleTypeName = new Label("");
            moduleTypeVersionNumber = new Label("");
        } else {
            moduleTypeName = new Label(moduleTypeVersionSubmodule.getModuleTypeVersion().getModuleType().getName());
            moduleTypeVersionNumber = new Label(String.valueOf(moduleTypeVersionSubmodule.getModuleTypeVersion().getVersion()));
        }
        layoutGenerator.addLabelWidgetPair("Module Type:", moduleTypeName, contentPanel);
        layoutGenerator.addLabelWidgetPair("Version:", moduleTypeVersionNumber, contentPanel);

        selectModuleTypeVersion = new Button("Select Module Type Version", selectModuleTypeVersionAction());
        selectModuleTypeVersion.setEnabled(viewMode != ViewMode.VIEW);
        layoutGenerator.addLabelWidgetPair("", selectModuleTypeVersion, contentPanel);

        description = new TextArea("description", moduleTypeVersionSubmodule.getDescription(), 60, 8);
        addChangeable(description);
        ScrollableComponent descScrollableTextArea = new ScrollableComponent(description);
        descScrollableTextArea.setMaximumSize(new Dimension(575, 200));
        layoutGenerator.addLabelWidgetPair("Description:", descScrollableTextArea, contentPanel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(contentPanel, 5, 2, // rows, cols
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
        if (!ModuleTypeVersionSubmodule.isValidName(trimName, error)) {
            messagePanel.setError(error.toString());
            return false;
        }
        Map<String, ModuleTypeVersionSubmodule> moduleTypeVersionSubmodules = compositeModuleTypeVersion.getModuleTypeVersionSubmodules();
        if (!trimName.equals(moduleTypeVersionSubmodule.getName()) && moduleTypeVersionSubmodules.containsKey(trimName)) {
            messagePanel.setError("Placeholder " + trimName + " already exists!");
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
                        String trimName = name.getText().trim();
                        if (!trimName.equals(moduleTypeVersionSubmodule.getName())) {
                            presenter.doRemove(compositeModuleTypeVersion, moduleTypeVersionSubmodule);
                            moduleTypeVersionSubmodule = moduleTypeVersionSubmodule.deepCopy();
                        }
                        moduleTypeVersionSubmodule.setName(name.getText().trim());
                        moduleTypeVersionSubmodule.setDescription(description.getText());
                        presenter.doSave(compositeModuleTypeVersion, moduleTypeVersionSubmodule);
                        messagePanel.setMessage("Submodule definition saved.");
                        resetChanges();
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
