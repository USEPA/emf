package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.commons.data.Reference;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.buttons.CancelButton;
import gov.epa.emissions.commons.gui.buttons.SaveButton;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.ui.ListWidget;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.AbstractListModel;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;

public class MeasureReferenceAddWindow extends DisposableInteralFrame implements MeasureReferenceView {

    private MessagePanel messagePanel;

    private JRadioButton existingReferenceRB;

    private JRadioButton newReferenceRB;

    private TextArea descriptionField;

    private SaveButton saveButton;

    private Button cancelButton;

    private MeasureReferencePresenter presenter;

    private Reference reference;

    private boolean newReference = false;

    private JTextField filterTextField;

    private Button searchButton;

    private ListWidget referenceList;

    private static int counter = 0;

    private EmfSession session;

    private JLabel referencesLabel;

    private JLabel descriptionLabel;

    private JLabel containsLabel;

    private static final Dimension DIMENSION = new Dimension(800, 400);

    public MeasureReferenceAddWindow(String title, DesktopManager desktopManager, EmfSession session) {

        super(title, DIMENSION, desktopManager);
        this.setMinimumSize(DIMENSION);

        this.session = session;
    }

    public void doRefresh() {

        try {

            int count = this.getReferenceCount();
            if (count >= 300) {

                String message = "There are " + count
                        + " references, which could take a while to transfer, would you like to continue? \n"
                        + "[Hint: if you choose not to continue, enter a filter in the 'Text contains' field\n"
                        + " before proceeding]";
                int selection = JOptionPane.showConfirmDialog(this, message, "Warning", JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);

                if (selection == JOptionPane.YES_OPTION) {
                    refresh(this.getReferences());
                }
            }

            refresh(this.getReferences());
        } catch (EmfException e) {
            this.messagePanel.setError(e.getMessage());
        }
    }

    private void refresh(final Reference[] references) {

        ListModel model = new AbstractListModel() {

            public int getSize() {
                return references.length;
            }

            public Object getElementAt(int i) {
                return references[i];
            }
        };

        this.referenceList.setModel(model);
    }

    public void save() {

        messagePanel.clear();

        String description = null;
        if (this.existingReferenceRB.isSelected()) {

            Reference selectedReference = (Reference) this.referenceList.getSelectedValue();
            if (selectedReference == null) {

                String message = "Please select an existing reference or create a new one.";
                this.messagePanel.setMessage(message);
            } else {
                description = selectedReference.getDescription();
            }
        } else {
            description = this.descriptionField.getText().trim();
        }

        if (description != null) {

            doSave(description);

            if (!newReference) {
                presenter.refresh();
            } else {
                presenter.add(reference);
            }

            disposeView();
        }

    }

    public void display(ControlMeasure measure, Reference reference) {

        String name = measure.getName();
        if (name == null) {
            name = "New Reference";
        }

        this.setLabel(this.getTitle() + " " + (counter++) + " for " + name);

        JPanel layout = createLayout();
        this.setContentPane(layout);
        this.display();
        this.reference = reference;

        populateFields();
        resetChanges();

    }

    // use this method when adding a new property
    public void display(ControlMeasure measure) {

        Reference newReference = new Reference();
        newReference.setUpdated(true);
        display(measure, newReference);
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
        panel.setLayout(new BorderLayout());

        messagePanel = new SingleLineMessagePanel();
        this.messagePanel.setOpaque(false);
        panel.add(messagePanel, BorderLayout.NORTH);
        panel.add(this.inputPanel(), BorderLayout.CENTER);

        // panel.add(detailPanel());
        panel.add(buttonsPanel(), BorderLayout.SOUTH);

        return panel;
    }

    private JPanel inputPanel() {

        Insets labelInsets = new Insets(0, 24, 5, 5);
        Insets rbInsets = new Insets(0, 5, 5, 0);
        Insets textInsets = new Insets(0, 5, 5, 0);
        Insets inputInsets = new Insets(0, 5, 5, 10);
        Insets buttonInsets = new Insets(-2, 5, 5, 0);

        JPanel panel = new JPanel(new GridBagLayout());

        ButtonGroup bg = new ButtonGroup();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.gridheight = 1;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.weighty = 0;
        gbc.insets = rbInsets;

        this.existingReferenceRB = new JRadioButton("Use existing reference");
        this.existingReferenceRB.setSelected(true);
        this.existingReferenceRB.setToolTipText("Check to use existing reference");
        bg.add(this.existingReferenceRB);
        panel.add(this.existingReferenceRB, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridheight = 1;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.insets = labelInsets;

        containsLabel = new JLabel("Text contains");
        panel.add(containsLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.gridheight = 1;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 0;
        gbc.insets = textInsets;

        AbstractAction searchAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doRefresh();
            }
        };

        this.filterTextField = new JTextField();
        this.filterTextField.addActionListener(searchAction);
        this.filterTextField.setToolTipText("The text contains search filter for finding references");
        this.containsLabel.setToolTipText(this.filterTextField.getToolTipText());
        containsLabel.setLabelFor(this.filterTextField);
        panel.add(this.filterTextField, gbc);

        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.gridheight = 1;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 1;
        gbc.weighty = 0;
        gbc.insets = buttonInsets;

        this.searchButton = new Button("Search", searchAction);
        searchButton.setMnemonic(KeyEvent.VK_E);
        searchButton.setToolTipText("Search for reference by using a contains search filter");
        panel.add(this.searchButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridheight = 1;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.insets = labelInsets;

        referencesLabel = new JLabel("References");
        panel.add(referencesLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.gridheight = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.insets = inputInsets;

        this.referenceList = new ListWidget(null);
        this.referenceList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.referenceList.setVisibleRowCount(4);
        this.referenceList.setToolTipText("This is list of references, which can be filtered by a contains search");
        referencesLabel.setToolTipText(this.referenceList.getToolTipText());
        referencesLabel.setLabelFor(this.referenceList);

        JScrollPane scrollPane = new JScrollPane(this.referenceList, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setPreferredSize(new Dimension(645, 80));
        panel.add(scrollPane, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.gridheight = 1;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.weighty = 0;
        gbc.insets = rbInsets;

        this.newReferenceRB = new JRadioButton("Create new reference");
        this.newReferenceRB.setToolTipText("Check to create new reference");
        bg.add(this.newReferenceRB);
        panel.add(this.newReferenceRB, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridheight = 1;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.insets = labelInsets;

        descriptionLabel = new JLabel("Description");
        panel.add(descriptionLabel, gbc);

        this.descriptionField = new TextArea("", "");
        this.descriptionField.setToolTipText("Text area where a new reference can be entered");
        descriptionLabel.setToolTipText(this.descriptionField.getToolTipText());
        descriptionLabel.setLabelFor(this.descriptionField);

        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.gridheight = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.insets = inputInsets;

        scrollPane = new JScrollPane(this.descriptionField, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setPreferredSize(new Dimension(645, 80));
        panel.add(scrollPane, gbc);

        this.existingReferenceRB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                enabledExistingReferenceRB();
            }
        });

        this.newReferenceRB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                enableNewReferenceRB();
            }
        });

        this.enabledExistingReferenceRB();

        return panel;
    }

    private void enabledExistingReferenceRB() {

        this.containsLabel.setEnabled(true);
        this.filterTextField.setEnabled(true);
        this.searchButton.setEnabled(true);
        this.referencesLabel.setEnabled(true);
        this.referenceList.setEnabled(true);

        this.descriptionLabel.setEnabled(false);
        this.descriptionField.setEnabled(false);
    }

    private void enableNewReferenceRB() {

        this.containsLabel.setEnabled(false);
        this.filterTextField.setEnabled(false);
        this.searchButton.setEnabled(false);
        this.referencesLabel.setEnabled(false);
        this.referenceList.setEnabled(false);

        this.descriptionLabel.setEnabled(true);
        this.descriptionField.setEnabled(true);
    }

    private JPanel buttonsPanel() {

        JPanel panel = new JPanel();
        saveButton = new SaveButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                save();
            }
        });
        getRootPane().setDefaultButton(saveButton);
        saveButton.setToolTipText("Save control measure reference");
        panel.add(saveButton);

        cancelButton = new CancelButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                closeWindow();
            }
        });
        cancelButton.setToolTipText("Cancel saving control measure reference and close window");
        panel.add(cancelButton);

        return panel;
    }

    protected void doSave(String description) {
        this.reference.setDescription(description.trim());
    }

    private void closeWindow() {
        disposeView();
    }

    public void viewOnly() {

        saveButton.setVisible(false);
        cancelButton.setText("Close");
    }

    private int getReferenceCount() throws EmfException {
        return this.session.controlMeasureService().getReferenceCount(this.descriptionField.getText().trim());
    }

    private Reference[] getReferences() throws EmfException {
        return this.session.controlMeasureService().getReferences(this.filterTextField.getText().trim());
    }
}