package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.commons.data.Reference;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.buttons.CancelButton;
import gov.epa.emissions.commons.gui.buttons.SaveButton;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class MeasureReferenceWindow extends DisposableInteralFrame implements MeasureReferenceView {

    private MessagePanel messagePanel;

    private TextArea descriptionField;

    private SaveButton saveButton;

    private Button cancelButton;

    private MeasureReferencePresenter presenter;

    private Reference reference;

    private boolean newReference = false;

    private ManageChangeables changeablesList;

    private ControlMeasure controlMeasure;

    private static int counter = 0;

    private static final Dimension DIMENSION = new Dimension(500, 200);

    public MeasureReferenceWindow(String title, ManageChangeables changeablesList, DesktopManager desktopManager,
            EmfSession session) {

        super(title, DIMENSION, desktopManager);
        this.setMinimumSize(DIMENSION);
        this.changeablesList = changeablesList;
    }

    public void save() {

        messagePanel.clear();

        if (!reference.getDescription().equalsIgnoreCase(this.descriptionField.getText().trim())
                && this.presenter.checkIfExists(this.descriptionField.getText().trim(), this.controlMeasure)) {
            this.messagePanel.setMessage("Control Measure already contains reference with the same description.");
        } else {

            doSave();

            if (!newReference) {
                presenter.refresh();
            } else {
                presenter.add(reference);
            }

            disposeView();
        }
    }

    public void display(ControlMeasure measure, Reference reference) {

        this.controlMeasure = measure;

        String name = measure.getName();
        if (name == null) {
            name = "New Control Measure";
        }

        this.setLabel(this.getTitle() + " " + (counter++) + " for " + name);

        JPanel layout = createLayout();
        this.getContentPane().add(layout);
        this.display();
        this.reference = reference;

        populateFields();
        resetChanges();

    }

    // use this method when adding a new property
    public void display(ControlMeasure measure) {

        display(measure, new Reference());
        this.newReference = true;
    }

    private void populateFields() {

        String description = this.reference.getDescription();
        if (description == null) {
            description = "";
        }
        this.descriptionField.setText(description);
    }

    public void observe(MeasureReferencePresenter presenter) {
        this.presenter = presenter;

    }

    private JPanel createLayout() {

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        messagePanel = new SingleLineMessagePanel();
        this.messagePanel.setOpaque(false);
        panel.add(messagePanel);
        panel.add(this.inputPanel());

        // panel.add(detailPanel());
        panel.add(buttonsPanel());

        return panel;
    }

    private JPanel inputPanel() {

        Insets labelInsets = new Insets(0, 24, 5, 5);
        Insets inputInsets = new Insets(0, 5, 5, 10);

        JPanel panel = new JPanel(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridheight = 1;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.insets = labelInsets;

        JLabel descriptionLabel = new JLabel("Description");
        panel.add(descriptionLabel, gbc);

        this.descriptionField = new TextArea("", "");
        this.changeablesList.addChangeable(this.descriptionField);

        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.gridheight = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.insets = inputInsets;

        JScrollPane scrollPane = new JScrollPane(this.descriptionField, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setPreferredSize(new Dimension(645, 80));
        panel.add(scrollPane, gbc);

        return panel;
    }

    private JPanel buttonsPanel() {
        JPanel panel = new JPanel();
        saveButton = new SaveButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                save();
            }
        });
        getRootPane().setDefaultButton(saveButton);
        panel.add(saveButton);

        cancelButton = new CancelButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                closeWindow();
            }
        });
        panel.add(cancelButton);

        return panel;
    }

    protected void doSave() {
        this.reference.setDescription(this.descriptionField.getText().trim());
        this.reference.setUpdated(true);
    }

    private void closeWindow() {
        if (shouldDiscardChanges())
            disposeView();
    }

    public void viewOnly() {

        saveButton.setVisible(false);
        cancelButton.setText("Close");
        this.descriptionField.setEditable(false);
    }
}