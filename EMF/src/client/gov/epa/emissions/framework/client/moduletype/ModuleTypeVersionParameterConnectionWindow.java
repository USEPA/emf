package gov.epa.emissions.framework.client.moduletype;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.ScrollableComponent;
import gov.epa.emissions.commons.gui.TextArea;
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
import gov.epa.emissions.framework.services.module.ModuleTypeVersionParameterConnection;
import gov.epa.emissions.framework.services.module.ModuleTypeVersionParameterConnectionEndpoint;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.Map;
import java.util.Arrays;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class ModuleTypeVersionParameterConnectionWindow extends DisposableInteralFrame implements ModuleTypeVersionParameterConnectionView {

    private ModuleTypeVersionParameterConnectionPresenter presenter;

    private EmfConsole parentConsole;
    private EmfSession session;

    private ViewMode viewMode;
    private ModuleTypeVersionParameterConnection moduleTypeVersionParameterConnection;

    // layout
    private JPanel layout;
    private SingleLineMessagePanel messagePanel;
    private JPanel detailsPanel;

    private ComboBox sourcesCB;
    private TextArea description;

    // data
    private Map<String, ModuleTypeVersionParameterConnectionEndpoint> sourceEndpoints;
    private String[] sourceEndpointNames; 
    
    public ModuleTypeVersionParameterConnectionWindow(EmfConsole parentConsole, DesktopManager desktopManager, EmfSession session,
            ViewMode viewMode, ModuleTypeVersionParameterConnection moduleTypeVersionParameterConnection) {
        super(getWindowTitle(viewMode, moduleTypeVersionParameterConnection), new Dimension(800, 600), desktopManager);

        this.viewMode = (viewMode == ViewMode.NEW) ? ViewMode.EDIT : viewMode; // can't be NEW, use EDIT instead
        this.moduleTypeVersionParameterConnection = moduleTypeVersionParameterConnection;
        this.sourceEndpoints = moduleTypeVersionParameterConnection.getSourceParameterEndpoints();
        this.sourceEndpointNames = sourceEndpoints.keySet().toArray(new String[] {}); 
        Arrays.sort(this.sourceEndpointNames);
        
        layout = new JPanel();
        layout.setLayout(new BorderLayout());
        this.parentConsole = parentConsole;
        this.session = session;
        super.getContentPane().add(layout);
    }

    private static String getWindowTitle(ViewMode viewMode, ModuleTypeVersionParameterConnection moduleTypeVersionParameterConnection) {
        switch (viewMode)
        {
            case NEW:  // can't be NEW, use EDIT instead
            case EDIT: return "Edit Module Type Version Parameter Connection (ID=" + moduleTypeVersionParameterConnection.getId() + ")";
            case VIEW: return "View Module Type Version Parameter Connection (ID=" + moduleTypeVersionParameterConnection.getId() + ")";
            default: return "";
        }
    }
    
    private void doLayout(JPanel layout) {
        messagePanel = new SingleLineMessagePanel();
        layout.add(messagePanel, BorderLayout.NORTH);
        layout.add(detailsPanel(), BorderLayout.CENTER);
        layout.add(buttonsPanel(), BorderLayout.SOUTH);
    }

    @Override
    public void observe(ModuleTypeVersionParameterConnectionPresenter presenter) {
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

        String targetSqlType;
        try {
            targetSqlType = moduleTypeVersionParameterConnection.getTargetSqlType();
        } catch (EmfException e) {
            targetSqlType = e.getMessage();
            e.printStackTrace();
        }
        Label sqlType = new Label(targetSqlType);
        layoutGenerator.addLabelWidgetPair("Parameter Type:", sqlType, contentPanel);
        
        sourcesCB = new ComboBox(sourceEndpointNames);
        sourcesCB.setSelectedItem(moduleTypeVersionParameterConnection.getSourceName());
        addChangeable(sourcesCB);
        sourcesCB.setMaximumSize(new Dimension(575, 20));
        layoutGenerator.addLabelWidgetPair("Source:", sourcesCB, contentPanel);

        Label targetName = new Label(moduleTypeVersionParameterConnection.getTargetName());
        layoutGenerator.addLabelWidgetPair("Target:", targetName, contentPanel);
        
        description = new TextArea("description", moduleTypeVersionParameterConnection.getDescription(), 60, 8);
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
        return true;
    }

    private Action saveAction() {
        Action action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                if (checkTextFields()) {
                    ModuleTypeVersionParameterConnectionEndpoint endpoint = sourceEndpoints.get(sourcesCB.getSelectedItem());
                    moduleTypeVersionParameterConnection.setSourceSubmodule(endpoint.getSubmodule());
                    moduleTypeVersionParameterConnection.setSourceParameterName(endpoint.getParameterName());
                    moduleTypeVersionParameterConnection.setDescription(description.getText());
                    presenter.refreshConnections();
                    messagePanel.setMessage("Connection saved.");
                    resetChanges();
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
