package gov.epa.emissions.framework.client.cost.controlstrategy;

import gov.epa.emissions.commons.gui.Button;
//import gov.epa.emissions.commons.gui.ScrollableComponent;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.gui.buttons.*;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.cost.controlstrategy.editor.EditControlStrategyView;
import gov.epa.emissions.framework.client.cost.controlstrategy.editor.EditControlStrategyWindow;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlStrategy;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class ControlStrategyWindow extends DisposableInteralFrame implements ControlStrategyView {

    private ControlStrategyPresenter presenter;

    private JPanel layout;

    private SingleLineMessagePanel messagePanel;

    private TextField name;

    private TextArea description;

    private static int count = 1;

    private EmfConsole parentConsole;

    private EmfSession session;
    
    private ControlStrategiesManagerPresenter managerPresenter;
    
    public ControlStrategyWindow(EmfConsole parentConsole, EmfSession session, DesktopManager desktopManager) {
        super("Create a Control Strategy", new Dimension(450, 150), desktopManager);
        this.session = session;
        this.parentConsole = parentConsole;
        layout = new JPanel();
        layout.setLayout(new BoxLayout(layout, BoxLayout.Y_AXIS));
        super.getContentPane().add(layout);
    }

    public void observe(ControlStrategyPresenter presenter, ControlStrategiesManagerPresenter managerPresenter) {
        this.presenter = presenter;
        this.managerPresenter = managerPresenter;
    }

    public void display() {
        super.setLabel("Create New Control Strategy " + count++);
        layout.removeAll();
        doLayout(layout);

        super.display();
    }

    private void doLayout(JPanel layout) {
        messagePanel = new SingleLineMessagePanel();
        layout.add(messagePanel);
        layout.add(createInputPanel());
        layout.add(createButtonsPanel());
    }

    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        name = new TextField("name", 30);
        addChangeable(name);
        layoutGenerator.addLabelWidgetPair("Name:", name, panel);

        description = new TextArea("Description", "", 30, 4);
        //addChangeable(description);
        //layoutGenerator.addLabelWidgetPair("Description:",
        //        ScrollableComponent.createWithVerticalScrollBar(description), panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 1, 2, // rows, cols
                5, 0, // initialX, initialY
                10, 10);// xPad, yPad

        return panel;
    }

    private JPanel createButtonsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel container = new JPanel();
        FlowLayout layout = new FlowLayout();
        layout.setHgap(20);
        layout.setVgap(25);
        container.setLayout(layout);

        Button saveButton = new OKButton(saveAction());
        container.add(saveButton);
        container.add(new CancelButton(closeAction()));
        getRootPane().setDefaultButton(saveButton);

        panel.add(container, BorderLayout.CENTER);

        return panel;
    }

    private Action closeAction() {
        Action action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                doClose();
            }
        };

        return action;
    }

    protected void doClose() {
        try {
            if (shouldDiscardChanges())
                presenter.doClose();
        } catch (EmfException e) {
            messagePanel.setError("Could not close: " + e.getMessage());
        }
    }

    private Action saveAction() {
        Action action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                resetChanges();
                // pass throud display()
                ControlStrategy newControlStrategy = new ControlStrategy(name.getText());
                newControlStrategy.setDescription(description.getText());
                newControlStrategy.setRunStatus("Not started");
                try {
                    int csId = presenter.doSave(newControlStrategy);
                    newControlStrategy.setId(csId);
                  
                    //open the edit window for the new strategy
                    EditControlStrategyView editControlStrategyView = new EditControlStrategyWindow(desktopManager, session, parentConsole);
                    managerPresenter.doEdit(editControlStrategyView, newControlStrategy);

                } catch (EmfException e) {
                    messagePanel.setError(e.getMessage());
                }
            }
        };

        return action;
    }

}
