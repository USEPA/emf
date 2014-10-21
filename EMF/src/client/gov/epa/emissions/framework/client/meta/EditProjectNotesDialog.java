package gov.epa.emissions.framework.client.meta;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;

import gov.epa.emissions.commons.data.Project;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.gui.ScrollableComponent;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.buttons.CloseButton;
import gov.epa.emissions.commons.gui.buttons.SaveButton;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;
import gov.epa.mims.analysisengine.gui.ScreenUtils;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class EditProjectNotesDialog extends JDialog {

    private EmfSession session;
    
    private Project project;
    
    private TextArea notes;
    
    public EditProjectNotesDialog(EmfConsole parent, EmfSession session, Project project) {
        super(parent);
        super.setIconImage(EmfImageTool.createImage("/logo.JPG"));
        this.session = session;
        this.project = project;
        setModal(true);
    }
    
    public void display() {
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout(5, 5));
        
        notes = new TextArea("notes", project.getNotes(), 40, 10);
        contentPane.add(new ScrollableComponent(notes), BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(new SaveButton(saveAction()));
        buttonPanel.add(new CloseButton(closeAction()));
        contentPane.add(buttonPanel, BorderLayout.SOUTH);
        
        setTitle("Edit Project Notes: " + project.getName());
        this.pack();
        this.setSize(500, 320);
        this.setLocation(ScreenUtils.getPointToCenter(this));
        this.setVisible(true);
    }

    private Action saveAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e){            
                project.setNotes(notes.getText());
                try {
                    session.dataCommonsService().updateProject(project);
                } catch (EmfException ex) {
                    return;
                }
                dispose();
            }
        };
    }

    private Action closeAction() {
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                dispose();
            }
        };
    }
}
