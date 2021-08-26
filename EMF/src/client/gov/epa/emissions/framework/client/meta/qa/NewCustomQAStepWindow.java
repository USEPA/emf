package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.data.QAProgram;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.CheckBox;
import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.EditableComboBox;
import gov.epa.emissions.commons.gui.ScrollableComponent;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.gui.buttons.CancelButton;
import gov.epa.emissions.commons.gui.buttons.OKButton;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.data.QAPrograms;
import gov.epa.emissions.framework.client.meta.versions.VersionsSet;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.ui.NumberFormattedTextField;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class NewCustomQAStepWindow extends DisposableInteralFrame implements NewCustomQAStepView {

    private ComboBox versionsSelection;

    private VersionsSet versionsSet;

    private EmfDataset dataset;

    private TextField name;

    private EditableComboBox program;

    private TextArea arguments;

    private NumberFormattedTextField order;

    private TextArea description;

    private SingleLineMessagePanel messagePanel;

    private CheckBox required;

    private QAPrograms qaPrograms;

    private NewCustomQAStepPresenter presenter;

    public NewCustomQAStepWindow(DesktopManager desktopManager) {
        super("Add Custom QA Step", new Dimension(550, 420), desktopManager);
    }

    public void display(EmfDataset dataset, QAProgram[] programs, Version[] versions, EditableQATabView tabView,EmfSession session) {
        super.setTitle(super.getTitle() + ": " + dataset.getName());

        this.dataset = dataset;
        this.versionsSet = new VersionsSet(versions);
        this.qaPrograms = new QAPrograms(session, programs);
        JPanel layout = createLayout();
        super.getContentPane().add(layout);
        super.display();
    }

    private JPanel createLayout() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        messagePanel = new SingleLineMessagePanel();
        panel.add(messagePanel);

        panel.add(inputPanel());
        panel.add(buttonsPanel());

        return panel;
    }

    private JPanel inputPanel() {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        name = new TextField("", 40);
        layoutGenerator.addLabelWidgetPair("Name:", name, panel);

        program = new EditableComboBox(qaPrograms.names());
        program.setPreferredSize(new Dimension(440,18));
        program.setSelectedItem("");
        program.setPrototypeDisplayValue("To make the combobox a bit wider");
        layoutGenerator.addLabelWidgetPair("Program:", program, panel);

        arguments = new TextArea("", "", 40, 2);
        ScrollableComponent scrollableDetails = ScrollableComponent.createWithVerticalScrollBar(arguments);
        layoutGenerator.addLabelWidgetPair("Arguments:", scrollableDetails, panel);

        order = new NumberFormattedTextField(5, orderAction());
        order.setText("0");
        order.addKeyListener(keyListener());
        layoutGenerator.addLabelWidgetPair("Order:", order, panel);

        required = new CheckBox("", false);
        layoutGenerator.addLabelWidgetPair("Required?", required, panel);

        description = new TextArea("", "", 40, 4);
        description.setLineWrap(true);
        description.setWrapStyleWord(true);
        ScrollableComponent scrollableDesc = ScrollableComponent.createWithVerticalScrollBar(description);
        layoutGenerator.addLabelWidgetPair("Description:", scrollableDesc, panel);

        versionsSelection = new ComboBox("Select one", versionsSet.nameAndNumbers());
        if (versionsSelection.getModel().getSize()==2)
        {
            versionsSelection.setSelectedIndex(1);
        }
        layoutGenerator.addLabelWidgetPair("Version:", versionsSelection, panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 7, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad

        return panel;
    }

    private KeyListener keyListener() {
        return new KeyListener() {
            public void keyTyped(KeyEvent e) {
                keyActions();
            }

            public void keyReleased(KeyEvent e) {
                keyActions();
            }

            public void keyPressed(KeyEvent e) {
                keyActions();
            }
        };
    }

    private void keyActions() {
        try {
            messagePanel.clear();
            Float.parseFloat(order.getText());
        } catch (NumberFormatException ex) {
            messagePanel.setError("Order should be a floating point number");
        }
    }

    private AbstractAction orderAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                try {
                    Float.parseFloat(order.getText());
                } catch (NumberFormatException ex) {
                    messagePanel.setError("Order should be a floating point number");
                }
            }
        };
    }

    private JPanel buttonsPanel() {
        JPanel panel = new JPanel();
        Button ok = new OKButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doOk();
            }
        });
        getRootPane().setDefaultButton(ok);
        panel.add(ok);

        Button cancel = new CancelButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                disposeView();
            }
        });
        panel.add(cancel);

        return panel;
    }

    private void doOk() {
        try {
            presenter.doSave();
            disposeView();
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }
    }

    private Version selectedVersion() {
        return versionsSet.getVersionFromNameAndNumber((String) versionsSelection.getSelectedItem());
    }

    public void observe(NewCustomQAStepPresenter presenter) {
        this.presenter = presenter;
    }

    public QAStep save() throws EmfException {
        if ( versionsSelection.getSelectedItem() == null ){
            throw new EmfException("Please select a version" );
        }
        
        String stepName = name.getText().trim();
        if(stepName.length()==0)
            throw new EmfException("Please enter a name for QA step");
        QAStep step = new QAStep();
        step.setDatasetId(dataset.getId());
        step.setVersion(selectedVersion().getVersion());
        step.setRequired(required.isSelected());

        step.setName(stepName);
        step.setProgram(qaPrograms.get(program.getSelectedItem()));
        step.setProgramArguments(arguments.getText());
        try {
            final float orderValue = Float.parseFloat(order.getText());
            step.setOrder(orderValue);
        } catch (NumberFormatException e) {
            throw new EmfException("Please enter an order that is a numeric value");
        }
        step.setDescription(description.getText().trim());
        step.setWho(dataset.getCreator());

        return step;
    }

}
