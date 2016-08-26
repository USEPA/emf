package gov.epa.emissions.framework.client.module;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.gui.buttons.CloseButton;
import gov.epa.emissions.commons.gui.buttons.SaveButton;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.Label;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.module.ModuleParameter;
import gov.epa.emissions.framework.services.module.ModuleTypeVersionParameter;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class EditModuleParameterWindow extends DisposableInteralFrame implements EditModuleParameterView {
    private EditModuleParameterPresenter presenter;

    private EmfConsole parentConsole;
    private EmfSession session;
    private static int counter = 0;

    private ModuleParameter moduleParameter;
    private ModuleTypeVersionParameter moduleTypeVersionParameter;

    // layout
    private JPanel layout;
    private SingleLineMessagePanel messagePanel;
    private JPanel detailsPanel;

    private Label mode;
    private Label parameterName;
    private Label sqlParameterType;
    private TextField parameterValue;

    public EditModuleParameterWindow(EmfConsole parentConsole, DesktopManager desktopManager, EmfSession session, ModuleParameter moduleParameter) {
        super("Edit Module Dataset", new Dimension(800, 400), desktopManager);

        this.moduleParameter = moduleParameter;
        this.moduleTypeVersionParameter = moduleParameter.getModule().getModuleTypeVersion().getModuleTypeVersionParameters().get(moduleParameter.getParameterName());

        layout = new JPanel();
        layout.setLayout(new BorderLayout());
        this.parentConsole = parentConsole;
        this.session = session;
        super.getContentPane().add(layout);
    }

    private void doLayout(JPanel layout) {
        messagePanel = new SingleLineMessagePanel();
        layout.add(messagePanel, BorderLayout.NORTH);
        layout.add(detailsPanel(), BorderLayout.NORTH);
        layout.add(buttonsPanel(), BorderLayout.SOUTH);
    }

    public void observe(EditModuleParameterPresenter presenter) {
        this.presenter = presenter;
    }

    public void display() {
        counter++;
        String name = "Edit Module Parameter " + counter;
        super.setTitle(name);
        super.setName("editModuleParameter:" + counter);
        layout.removeAll();
        doLayout(layout);

        super.display();
    }

    private JPanel detailsPanel() {
        detailsPanel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();
        int rows = 0;
        
        mode = new Label(moduleTypeVersionParameter.getMode());
        layoutGenerator.addLabelWidgetPair("Mode:", mode, detailsPanel);
        rows++;

        parameterName = new Label(moduleTypeVersionParameter.getParameterName());
        layoutGenerator.addLabelWidgetPair("Parameter Name:", parameterName, detailsPanel);
        rows++;

        sqlParameterType = new Label(moduleTypeVersionParameter.getSqlParameterType());
        layoutGenerator.addLabelWidgetPair("Parameter SQL Type:", sqlParameterType, detailsPanel);
        rows++;

        parameterValue = new TextField("parameterValue", 30);
        parameterValue.setText(moduleParameter.getValue());
        layoutGenerator.addLabelWidgetPair("Value:", parameterValue, detailsPanel);
        rows++;
            
        // Lay out the panel.
        layoutGenerator.makeCompactGrid(detailsPanel, rows, 2, // rows, cols
                10, 10, // initialX, initialY
                10, 10);// xPad, yPad

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

    private boolean checkInputFields() {
        // parameterValue can be anything, even empty string
        return true;
    }

    private Action saveAction() {
        Action action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                if (checkInputFields()) {
                    try {
                        resetChanges();
                        moduleParameter.setValue(parameterValue.getText());
                        presenter.doSave(moduleParameter);
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
