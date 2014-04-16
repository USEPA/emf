package gov.epa.emissions.framework.client.meta.revisions;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.Changeable;
import gov.epa.emissions.commons.gui.Changeables;
import gov.epa.emissions.commons.gui.DefaultChangeables;
import gov.epa.emissions.commons.gui.ScrollableComponent;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.buttons.CloseButton;
import gov.epa.emissions.commons.gui.buttons.SaveButton;
import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.client.DefaultChangeObserver;
import gov.epa.emissions.framework.client.EmfView;
import gov.epa.emissions.framework.client.Label;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.WidgetChangesMonitor;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.meta.SetReferencesDialog;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.DatasetNote;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.editor.Revision;
import gov.epa.emissions.framework.ui.Dialog;
import gov.epa.emissions.framework.ui.InfoDialog;
import gov.epa.emissions.framework.ui.Position;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class RevisionEditorViewImpl extends Dialog implements RevisionEditorView, EmfView {

    private EmfConsole parent;

    private JPanel mainPanel;

    private TextArea whyTextArea;

    private TextArea whatTextArea;

    private TextArea referenceTextArea;

    private RevisionEditorPresenter presenter;

    private Revision revision;

    private WidgetChangesMonitor monitor;

    private Changeables changeables;

    private DefaultChangeObserver changeObserver;

    public RevisionEditorViewImpl(EmfConsole parent) {

        super("Edit Revision", parent);

        this.setSize(new Dimension(700, 450));

        this.parent = parent;

        this.changeObserver = new DefaultChangeObserver(this);
        this.changeables = new DefaultChangeables(this.changeObserver);
        this.monitor = new WidgetChangesMonitor(this.changeables, this);

        this.mainPanel = new JPanel();
        this.mainPanel.setLayout(new BoxLayout(this.mainPanel, BoxLayout.Y_AXIS));
        this.getContentPane().add(this.mainPanel);
    }

    public void observe(RevisionEditorPresenter presenter) {
        this.presenter = presenter;
    }

    public void display(Revision revision, EmfDataset dataset) {

        this.revision = revision;

        String name = "Edit Revision: " + this.revision.getWhat();
        this.setTitle(name);
        this.setName(name);

        doLayout(this.revision, dataset);
        super.display();
    }

    private void doLayout(Revision revision, EmfDataset dataset) {

        this.mainPanel.add(inputPanel(revision, dataset));
        this.mainPanel.add(buttonsPanel());
    }

    private JPanel inputPanel(final Revision revision, EmfDataset dataset) {

        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        whatTextArea = new TextArea("What", revision.getWhat(), 48, 5);
        whatTextArea.setEditable(true);
        this.addChangeable(this.whatTextArea);
        
        ScrollableComponent scrollableWhat = ScrollableComponent.createWithVerticalScrollBar(whatTextArea);
        layoutGenerator.addLabelWidgetPair("What:", scrollableWhat, panel);

        this.whyTextArea = new TextArea("Why", revision.getWhy(), 48, 5);
        this.whyTextArea.setEditable(true);
        this.addChangeable(this.whyTextArea);
        
        ScrollableComponent scrollableWhy = ScrollableComponent.createWithVerticalScrollBar(whyTextArea);
        layoutGenerator.addLabelWidgetPair("Why: ", scrollableWhy, panel);

        layoutGenerator.addLabelWidgetPair("Dataset:", new Label(dataset.getName()), panel);
        layoutGenerator.addLabelWidgetPair("Version:", new Label("" + revision.getVersion()), panel);
        layoutGenerator.addLabelWidgetPair("Creator:", new Label(revision.getCreator().getName()), panel);
        layoutGenerator.addLabelWidgetPair("Date:", new Label(format(revision.getDate())), panel);

        JPanel refPanel = new JPanel();

        this.referenceTextArea = new TextArea("References", references(revision), 48, 5);
        this.referenceTextArea.setEditable(false);
        this.addChangeable(this.referenceTextArea);

        refPanel.add(ScrollableComponent.createWithVerticalScrollBar(this.referenceTextArea));

        DatasetNote[] datasetNotes = new DatasetNote[0];
        try {
            datasetNotes = this.parent.getSession().dataCommonsService().getDatasetNotes(dataset.getId());
        } catch (EmfException e1) {
            // NOTE Auto-generated catch block
            e1.printStackTrace();
        }

        final DatasetNote[] tempNotes = datasetNotes;

        JButton setButton = new JButton(new AbstractAction("Set") {
            public void actionPerformed(ActionEvent e) {
                doSetReference(tempNotes);
            }
        });

        refPanel.add(setButton);

        layoutGenerator.addLabelWidgetPair("References:", refPanel, panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 7, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad

        return panel;
    }

    protected void doSetReference(DatasetNote[] notes) {

        DatasetNote[] selectedReferences = new DatasetNote[0];

        SetReferencesDialog dialog = new SetReferencesDialog(parent);
        dialog.display(notes, selectedReferences);
        selectedReferences = dialog.selected();
        this.referenceTextArea.setText(dialog.referencesList());
    }

    private String references(Revision revision) {
        return revision.getReferences() != null ? revision.getReferences() : "";
    }

    public void notifyLockFailure(Revision revision) {
        String message = "Cannot edit revision: " + revision.getId() + System.getProperty("line.separator")
                + " as it was locked by User: " + revision.getLockOwner() + "(at " + format(revision.getLockDate())
                + ")";
        InfoDialog dialog = new InfoDialog(this, "Message", message);
        dialog.confirm();
    }

    private String format(Date date) {
        return CustomDateFormat.format_YYYY_MM_DD_HH_MM(date);
    }

    private JPanel buttonsPanel() {

        JPanel panel = new JPanel();

        Button saveButton = new SaveButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                RevisionEditorViewImpl.this.doSave();
            }
        });
        panel.add(saveButton);

        Button closeButton = new CloseButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                RevisionEditorViewImpl.this.doClose();
            }
        });
        panel.add(closeButton);

        getRootPane().setDefaultButton(saveButton);

        return panel;
    }

    public void doSave() {

        if (hasChanges()) {

            this.revision.setWhat(this.whatTextArea.getText());
            this.revision.setWhy(this.whyTextArea.getText());
            this.revision.setReferences(this.referenceTextArea.getText());

            try {
                this.presenter.doSave();
                presenter.doRefresh(); 
            } catch (EmfException e) {
                e.printStackTrace();
            }
        }

        this.disposeView();
        // try {
        // Revision[] revisions = this.view.getRevisions();
        // for (Revision revision : revisions) {
        // System.out.println(revision);
        // }
        //            
        // session.dataCommonsService().updateRevisions(revisions);
        // } catch (EmfException e) {
        // e.printStackTrace();
        // }
    }
    

    private void doClose() {

        if (this.shouldDiscardChanges()) {
            this.dispose();
        }
    }

    public void addChangeable(Changeable changeable) {
        this.changeables.add(changeable);
    }

    private boolean hasChanges() {
        return this.changeables.hasChanges();
    }

    private boolean shouldDiscardChanges() {
        return this.monitor.shouldDiscardChanges();
    }

    public void disposeView() {
        this.dispose();
    }

    public Position getPosition() {

        Point location = this.getLocation();
        return new Position(location.x, location.y);
    }

    public int height() {
        return this.getHeight();
    }

    public void setPosition(Position position) {
        this.setLocation(position.x(), position.y());
    }

    public int width() {
        return this.getWidth();
    }

    public void hideMe() {
        // NOTE Auto-generated method stub
        setVisible(false);
        //setState(JFrame.ICONIFIED);
    }

}