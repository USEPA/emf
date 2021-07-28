package gov.epa.emissions.framework.client.data.datasettype;

import gov.epa.emissions.commons.data.QAProgram;
import gov.epa.emissions.commons.data.QAStepTemplate;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.CheckBox;
import gov.epa.emissions.commons.gui.ScrollableComponent;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.gui.buttons.CloseButton;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.DesktopManager;

import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class ViewableQAStepTemplateWindow extends DisposableInteralFrame implements ViewableQAStepTemplateView {
    private TextField name;

    private TextField program;

    private TextArea programParameters;

    private CheckBox required;

    private TextField order;

    private JPanel layout;

    private String title;

    private TextArea description;

    public ViewableQAStepTemplateWindow(String title, DesktopManager desktopManager) {
        super("QA Step Template", new Dimension(550, 350), desktopManager);
        this.title = title;
    }

    public void display(QAStepTemplate template) {
        super.setTitle(super.getTitle() + ": " + " " + title);
        super.setName(super.getTitle() + ": " + " " + title);
        layout = createLayout();
        populateFields(template);
        super.getContentPane().add(layout);
        super.display();
    }

    private JPanel createLayout() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.add(inputPanel());
        panel.add(buttonsPanel());

        return panel;
    }

    private JPanel inputPanel() {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        name = new TextField("", 40, "QA step template name");
        name.setEditable(false);
        layoutGenerator.addLabelWidgetPair("Name:", name, panel);

        program = new TextField("", 40, "QA step template program");
        program.setEditable(false);
        layoutGenerator.addLabelWidgetPair("Program:", program, panel);

        programParameters = new TextArea("", "", 40, 3, "QA step template program parameters");
        programParameters.setEditable(false);
        ScrollableComponent scrollableDetails = ScrollableComponent.createWithVerticalScrollBar(programParameters);
        layoutGenerator.addLabelWidgetPair("Parameters:", scrollableDetails, panel);

        order = new TextField("", 40, "QA step template name");
        order.setEditable(false);
        layoutGenerator.addLabelWidgetPair("Order:", order, panel);

        required = new CheckBox("", "QA step template is required?");
        required.setEnabled(false);
        layoutGenerator.addLabelWidgetPair("Required?", required, panel);

        description = new TextArea("", "", 40, 10, "QA step template description");
        description.setLineWrap(true);
        description.setWrapStyleWord(true);
        description.setEditable(false);
        ScrollableComponent scrollableDesc = ScrollableComponent.createWithVerticalScrollBar(description);
        layoutGenerator.addLabelWidgetPair("Description:", scrollableDesc, panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 6, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad

        return panel;
    }

    private JPanel buttonsPanel() {
        JPanel panel = new JPanel();
        Button ok = new CloseButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                disposeView();
            }
        });
        ok.setToolTipText("Save QA step template");
        getRootPane().setDefaultButton(ok);
        panel.add(ok);

        return panel;
    }

    public void populateFields(QAStepTemplate template) {
        name.setText(template.getName());
        QAProgram qaProgram = template.getProgram();
        program.setText((qaProgram == null) ? "" : qaProgram.getName());
        programParameters.setText(template.getProgramArguments());
        required.setSelected(template.isRequired());
        order.setText(template.getOrder() + "");
        description.setText(template.getDescription());
    }

}
