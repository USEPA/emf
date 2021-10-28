package gov.epa.emissions.framework.client.qa;

import gov.epa.emissions.commons.data.QAProgram;
import gov.epa.emissions.commons.data.QAStepTemplate;
import gov.epa.emissions.commons.gui.CheckBox;
import gov.epa.emissions.commons.gui.EditableComboBox;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.ScrollableComponent;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.data.QAPrograms;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.NumberFormattedTextField;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.AbstractAction;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class QAStepTemplatePanel extends JPanel {

    private TextField name;

    private EditableComboBox program;

    private TextArea programParameters;

    private CheckBox required;

    private NumberFormattedTextField order;

    private JPanel layout;

    private TextArea description;

    private MessagePanel messagePanel;

    private ManageChangeables changeablesList;

    private QAPrograms qaPrograms;

    public QAStepTemplatePanel(EmfSession session, QAProgram[] programs, MessagePanel messagePanel, ManageChangeables changeablesList) {
        this.changeablesList = changeablesList;
        this.messagePanel = messagePanel;
        this.qaPrograms = new QAPrograms(session, programs);
        layout = inputPanel();
        add(layout);
    }

    private JPanel inputPanel() {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        name = new TextField("", 40);
        changeablesList.addChangeable(name);
        layoutGenerator.addLabelWidgetPair("Name:", name, panel);

        program = new EditableComboBox(qaPrograms.names());
        changeablesList.addChangeable(program);
        program.setPrototypeDisplayValue("To make the combobox a bit wider");
        layoutGenerator.addLabelWidgetPair("Program:", program, panel);

        programParameters = new TextArea("", "", 40, 3);
        changeablesList.addChangeable(programParameters);
        ScrollableComponent scrollableDetails = ScrollableComponent.createWithVerticalScrollBar(programParameters);
        layoutGenerator.addLabelWidgetPair("Arguments:", scrollableDetails, panel);

        order = new NumberFormattedTextField(5);
        changeablesList.addChangeable(order);
        layoutGenerator.addLabelWidgetPair("Order:", order, panel);

        required = new CheckBox("");
        changeablesList.addChangeable(required);
        layoutGenerator.addLabelWidgetPair("Required?", required, panel);

        description = new TextArea("", "", 40, 10);
        changeablesList.addChangeable(description);
        description.setLineWrap(true);
        description.setWrapStyleWord(true);
        ScrollableComponent scrollableDesc = ScrollableComponent.createWithVerticalScrollBar(description);
        layoutGenerator.addLabelWidgetPair("Description:", scrollableDesc, panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 6, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad

        return panel;
    }

    public String getTemplateName() {
        return name.getText();
    }

    public QAProgram getProgram() throws EmfException {
        return qaPrograms.get(program.getSelectedItem());
    }

    public String getProgramArgs() {
        return programParameters.getText();
    }

    public boolean getRequired() {
        return required.isSelected();
    }

    public String getDescription() {
        return description.getText();
    }

    public String getOrder() {
        return order.getText();
    }

    public void setFields(QAStepTemplate template) {
        name.setText(template.getName());
        program.setSelectedIndex(getItemIndex(template));
        programParameters.setText(template.getProgramArguments());
        required.setSelected(template.isRequired());
        order.setValue(template.getOrder());
        description.setText(template.getDescription());
    }

    private int getItemIndex(QAStepTemplate template) {
        int size = program.getItemCount();

        for (int i = 0; i < size; i++) {
            String name = program.getItemAt(i).toString();
            if (name.equalsIgnoreCase(template.getProgram().getName()))
                return i;
        }
        
        return 0;
    }
}
