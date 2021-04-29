package gov.epa.emissions.framework.client.data.editor;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ScrollableComponent;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.meta.SetReferencesDialog;
import gov.epa.emissions.framework.services.data.DatasetNote;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.editor.Revision;
import gov.epa.emissions.framework.ui.Border;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class RevisionPanel extends JPanel {

    private TextArea what;

    private TextArea why;

    private User user;

    private EmfDataset dataset;

    private Version version;

    private DatasetNote[] selectedReferences;

    private DatasetNote[] notes;

    private SetReferencesDialog setReferencesDialog;

    public RevisionPanel(User user, EmfDataset dataset, Version version, DatasetNote[] notes, EmfConsole parent) {
        this.user = user;
        this.dataset = dataset;
        this.version = version;
        selectedReferences = new DatasetNote[0];
        this.notes = notes;
        setReferencesDialog = new SetReferencesDialog(parent);

        super.add(createLayout());
        super.setBorder(new Border("Revision Information (auto-saved when window is closed)"));
    }

    private JPanel createLayout() {
        JPanel panel = new JPanel(new BorderLayout(10, 0));

        panel.add(mainPanel(), BorderLayout.CENTER);
        panel.add(referencesPanel(), BorderLayout.LINE_END);

        return panel;
    }

    private JPanel referencesPanel() {
        JPanel panel = new JPanel();

        Button references = new Button("References", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                setReferences();
            }
        });
        references.setToolTipText("Select notes for the dataset that your changes are in reference to");
        references.setMnemonic(KeyEvent.VK_R);
        panel.add(references);

        return panel;
    }

    protected void setReferences() {
        setReferencesDialog.display(notes, selectedReferences);
        selectedReferences = setReferencesDialog.selected();
    }

    private JPanel mainPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 10, 0));

        what = new TextArea("", "", 30, 2);
        panel.add(labelValuePanel("What was changed", ScrollableComponent.createWithVerticalScrollBar(what)));

        why = new TextArea("", "", 30, 2);
        panel.add(labelValuePanel("Why it was changed", ScrollableComponent.createWithVerticalScrollBar(why)));

        return panel;
    }

    private JPanel labelValuePanel(String labelText, JComponent widget) {
        BorderLayout bl = new BorderLayout(3, 4);
        JPanel panel = new JPanel(bl);
        JLabel label = new JLabel(labelText, JLabel.CENTER);
        panel.add(label, BorderLayout.NORTH);
        panel.add(widget, BorderLayout.CENTER);

        return panel;
    }

    public Revision revision() {
        Revision revision = new Revision();
        revision.setCreator(user);
        revision.setDatasetId(dataset.getId());
        revision.setVersion(version.getVersion());
        revision.setWhat(what.getText());
        revision.setWhy(why.getText());
        revision.setReferences(setReferencesDialog.referencesList());
        revision.setDate(new Date());

        return revision;
    }

    public boolean verifyInput() {
        if (what.isEmpty() || why.isEmpty())
            return false;

        return true;
    }
    
    public void enableWhatNWhy() {
        what.setEnabled(true);
        why.setEnabled(true);
    }

    public void disableWhatNWhy() {
        what.setEnabled(false);
        why.setEnabled(false);
    }
    
    public void appendWhatField(String text) {
        String ls = System.getProperty("line.separator");
        String existing = what.getText();
        enableWhatNWhy();
        what.setText((existing == null ? text : existing + text) + ls);
    }
}
