package gov.epa.emissions.framework.client.meta.notes;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ScrollableComponent;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.buttons.OKButton;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.Label;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.services.data.DatasetNote;
import gov.epa.emissions.framework.services.data.Note;

import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.SpringLayout;

public class ViewNoteWindow extends DisposableInteralFrame implements NoteView {

    private JPanel layout;

    public ViewNoteWindow(DesktopManager desktopManager) {
        super("View Note", new Dimension(550, 250), desktopManager);

        layout = new JPanel();
        layout.setLayout(new BoxLayout(layout, BoxLayout.Y_AXIS));
        super.getContentPane().add(layout);
    }

    public void display(DatasetNote note) {
        super.setLabel(super.getTitle() + " : " + note.getNote().getName());

        doLayout(note);
        super.display();
    }

    private void doLayout(DatasetNote dsNote) {
        layout.add(inputPanel(dsNote));
        layout.add(buttonsPanel());
    }

    private JPanel inputPanel(DatasetNote dsNote) {
        JPanel panel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();
        Note note = dsNote.getNote();
        layoutGenerator.addLabelWidgetPair("Name:", new Label(note.getName()), panel);
        layoutGenerator.addLabelWidgetPair("Type:", new Label(note.getNoteType().getType()), panel);

        TextArea details = new TextArea("", note.getDetails(), 40, 3);
        details.setEditable(false);
        ScrollableComponent scrollableDetails = ScrollableComponent.createWithVerticalScrollBar(details);
        layoutGenerator.addLabelWidgetPair("Details:", scrollableDetails, panel);

        layoutGenerator.addLabelWidgetPair("References:", new Label(note.getReferences()), panel);
        layoutGenerator.addLabelWidgetPair("Version:", new Label(dsNote.getVersion() + ""), panel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(panel, 5, 2, // rows, cols
                5, 5, // initialX, initialY
                10, 10);// xPad, yPad

        return panel;
    }

    private JPanel buttonsPanel() {
        JPanel panel = new JPanel();
        Button ok = new OKButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                disposeView();
            }
        });
        getRootPane().setDefaultButton(ok);
        panel.add(ok);

        return panel;
    }

}
