package gov.epa.emissions.framework.client.moduletype;

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
import gov.epa.emissions.commons.io.XFileFormat;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.ViewMode;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.EmfFileInfo;
import gov.epa.emissions.framework.services.basic.EmfFileSystemView;
import gov.epa.emissions.framework.services.module.ModuleType;
import gov.epa.emissions.framework.services.module.ModuleTypeVersion;
import gov.epa.emissions.framework.services.module.ModuleTypeVersionDataset;
import gov.epa.emissions.framework.services.module.ModuleTypeVersionParameter;
import gov.epa.emissions.framework.services.module.ModuleTypeVersionRevision;
import gov.epa.emissions.framework.ui.EmfFileChooser;
import gov.epa.emissions.framework.ui.SelectableSortFilterWrapper;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SpringLayout;
import javax.swing.border.TitledBorder;

public class ModuleTypeVersionParameterWindow extends DisposableInteralFrame implements ModuleTypeVersionParameterView {
    private ModuleTypeVersionParameterPresenter presenter;

    private EmfConsole parentConsole;
    private EmfSession session;
    private static int counter = 0;

    private ViewMode viewMode;
    private ModuleTypeVersion moduleTypeVersion;
    private ModuleTypeVersionParameter moduleTypeVersionParameter;

    // layout
    private JPanel layout;
    private SingleLineMessagePanel messagePanel;
    private JPanel detailsPanel;

    private ComboBox mode;
    private TextField name;
    private TextField sqlType;
    private TextArea description;

    public ModuleTypeVersionParameterWindow(EmfConsole parentConsole, DesktopManager desktopManager, EmfSession session,
            ModuleTypeVersion moduleTypeVersion, ViewMode viewMode, ModuleTypeVersionParameter moduleTypeVersionParameter) {
        super(getWindowTitle(viewMode), new Dimension(800, 600), desktopManager);

        this.moduleTypeVersion = moduleTypeVersion;
        
        this.viewMode = viewMode;
        if (viewMode == ViewMode.NEW) {
            this.moduleTypeVersionParameter = new ModuleTypeVersionParameter();
            this.moduleTypeVersionParameter.setModuleTypeVersion(moduleTypeVersion);
        } else {
            this.moduleTypeVersionParameter = moduleTypeVersionParameter;
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
            case NEW: return "Create New Module Type Version Parameter";
            case EDIT: return "Edit Module Type Version Parameter";
            case VIEW: return "View Module Type Version Parameter";
            default: return "";
        }
    }
    
    private void doLayout(JPanel layout) {
        messagePanel = new SingleLineMessagePanel();
        layout.add(messagePanel, BorderLayout.NORTH);
        layout.add(detailsPanel(), BorderLayout.CENTER);
        layout.add(buttonsPanel(), BorderLayout.SOUTH);
    }

    public void observe(ModuleTypeVersionParameterPresenter presenter) {
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

        mode = new ComboBox(new String[] {ModuleTypeVersionParameter.IN, ModuleTypeVersionParameter.INOUT, ModuleTypeVersionParameter.OUT});
        mode.setSelectedItem(moduleTypeVersionParameter.getMode());
        addChangeable(mode);
        layoutGenerator.addLabelWidgetPair("Mode:", mode, contentPanel);

        sqlType = new TextField("sqlType", 60);
        sqlType.setText(moduleTypeVersionParameter.getSqlParameterType());
        addChangeable(sqlType);
        sqlType.setMaximumSize(new Dimension(575, 20));
        layoutGenerator.addLabelWidgetPair("SQL Type:", sqlType, contentPanel);

        name = new TextField("name", 60);
        name.setText(moduleTypeVersionParameter.getParameterName());
        addChangeable(name);
        name.setMaximumSize(new Dimension(575, 20));
        layoutGenerator.addLabelWidgetPair("Name:", name, contentPanel);

        description = new TextArea("description", moduleTypeVersionParameter.getDescription(), 60, 8);
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
        if (!ModuleTypeVersionParameter.isValidParameterName(name.getText(), error)) {
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
//                        Map<String, ModuleTypeVersionParameter> moduleTypeVersionParameters = moduleTypeVersion.getModuleTypeVersionParameters();
//                        if (moduleTypeVersionParameters.containsKey(name.getText())) {
//                            throw new EmfException("Parameter " + name.getText() + " already exists!");
//                        }
                        moduleTypeVersionParameter.setMode(mode.getSelectedItem().toString());
                        moduleTypeVersionParameter.setParameterName(name.getText());
                        moduleTypeVersionParameter.setSqlParameterType(sqlType.getText());
                        moduleTypeVersionParameter.setDescription(description.getText());
                        presenter.doSave(moduleTypeVersion, moduleTypeVersionParameter);
                        messagePanel.setMessage("Parameter definition saved.");
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
