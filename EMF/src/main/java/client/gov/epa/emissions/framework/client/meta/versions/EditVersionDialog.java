package gov.epa.emissions.framework.client.meta.versions;

import gov.epa.emissions.commons.db.intendeduse.IntendedUse;
import gov.epa.emissions.commons.db.intendeduse.IntendedUses;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ComboBox;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.TextField;
import gov.epa.emissions.commons.gui.buttons.CancelButton;
import gov.epa.emissions.commons.gui.buttons.OKButton;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.DataCommonsService;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.ui.Dialog;

import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SpringLayout;

public class EditVersionDialog extends Dialog {

    private TextField name;

    private TextArea descriptionTextArea;

    private ComboBox intendedUseCombo;

    protected boolean shouldChange = false;

    private Version version;

    private VersionsSet versionsSet;

    private IntendedUse[] allIntendedUses;

    private String originalName;

    private DataCommonsService service;

    public EditVersionDialog(EmfDataset dataset, Version selectedVersion, Version[] versions, EmfConsole parent)
            throws EmfException {

        super("Edit Version " + selectedVersion.getVersion() + " of " + dataset.getName(), parent);

        this.service = parent.getSession().dataCommonsService();

        this.setSize(new Dimension(420, 264));
        this.version = selectedVersion;
        versionsSet = new VersionsSet(versions);
        this.getContentPane().add(createLayout());
        this.center();
        this.setResizable(false);
        
        this.originalName = selectedVersion.getName();
    }

    private JPanel createLayout() throws EmfException {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.add(inputPanel());
        panel.add(buttonsPanel());

        return panel;
    }

    private JPanel inputPanel() throws EmfException {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        name = new TextField("", 25);
        name.setText(version.getName());
        layoutGenerator.addLabelWidgetPair("Name", name, panel);

        this.descriptionTextArea = new TextArea("", version.getDescription(), 25, 6);
        this.descriptionTextArea.setText(version.getDescription());
        
        JScrollPane scrollPane = new JScrollPane(this.descriptionTextArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        layoutGenerator.addLabelWidgetPair("Description", scrollPane, panel);

        setupIntendedUseCombo();
        layoutGenerator.addLabelWidgetPair("Intended Use: ", intendedUseCombo, panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 3, 2, // rows, cols
                5, 15, // initialX, initialY
                10, 10);// xPad, yPad

        return panel;
    }

    private JPanel buttonsPanel() {
        
        JPanel panel = new JPanel();

        Button ok = new OKButton(new AbstractAction() {
        
            public void actionPerformed(ActionEvent e) {

                if (verifyInput()) {

                    shouldChange = true;
                    version.setName(name.getText().trim());
                    version.setDescription(descriptionTextArea.getText());
                    
                    updateIntendedUse();
                    
                    close();
                }
            }
        });
        
        getRootPane().setDefaultButton(ok);
        panel.add(ok);

        Button cancel = new CancelButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                shouldChange = false;
                close();
            }
        });
        panel.add(cancel);

        return panel;
    }

    protected boolean verifyInput() {
        String newName = name().trim();
        if (newName.length() == 0) {
            JOptionPane.showMessageDialog(super.getParent(), "Please enter a name", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (newName.contains("(") || newName.contains(")")) {
            JOptionPane.showMessageDialog(super.getParent(), "Please enter a name that does not contain parentheses",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        /*
         * we don't need to check if the new name and the original name are equal. This was added when "description" was
         * added to allow editing of only the description.
         */
        if (!this.originalName.equals(newName)) {

            if (isDuplicate(newName)) {
                JOptionPane.showMessageDialog(super.getParent(), "Please enter a unique 'name'", "Error",
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }

        return true;
    }

    private boolean isDuplicate(String value) {
        return versionsSet.contains(value);
    }

    protected void close() {
        super.dispose();
    }

    public boolean shouldChange() {
        return shouldChange;
    }

    public void run() {
        super.display();
    }

    public Version getVersion() {
        return version;
    }

    public String name() {
        return name.getText();
    }

    public String getDescription() {
        return this.descriptionTextArea.getText();
    }

    private void setupIntendedUseCombo() throws EmfException {

        this.allIntendedUses = this.service.getIntendedUses();
        this.intendedUseCombo = new ComboBox(this.allIntendedUses);
        IntendedUse intendedUse = this.version.getIntendedUse();

        if (intendedUse == null) {
            intendedUse = getPublic(this.allIntendedUses);
        }

        this.intendedUseCombo.setSelectedItem(intendedUse);
    }

    private IntendedUse getPublic(IntendedUse[] allIntendedUses) {

        for (IntendedUse use : allIntendedUses) {
            if (use.getName().equalsIgnoreCase("public")) {
                return use;
            }
        }

        return null;
    }

    private void updateIntendedUse() {

        Object selected = this.intendedUseCombo.getSelectedItem();
        if (selected instanceof String) {

            String intendedUseName = (String) selected;
            if (intendedUseName.length() > 0) {

                IntendedUse intendedUse = intendedUse(intendedUseName);// checking for duplicates
                this.version.setIntendedUse(intendedUse);
            }
        } else if (selected instanceof IntendedUse) {
            this.version.setIntendedUse((IntendedUse) selected);
        }
    }

    private IntendedUse intendedUse(String name) {
        return new IntendedUses(this.allIntendedUses).get(name);
    }
}
