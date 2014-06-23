package gov.epa.emissions.framework.client.casemanagement.jobs;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.buttons.CloseButton;
import gov.epa.emissions.commons.gui.buttons.SaveButton;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.jobs.CaseJob;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

public class EditCaseJobWindow extends DisposableInteralFrame implements EditCaseJobView {

    private boolean shouldCreate;

    private boolean edit = true;

    private JPanel layout;

    private EditCaseJobPresenterImpl presenter;

    private MessagePanel messagePanel;

    private Button ok;

    private EmfConsole parent;

    private EmfSession session;

    private JobFieldsPanel jobFieldsPanel;

    public EditCaseJobWindow(String title, DesktopManager desktopManager, EmfConsole parent, EmfSession session) {
        super(title, new Dimension(600, 640), desktopManager);
        this.parent = parent;
        this.session = session;
    }

    public EditCaseJobWindow(boolean forView, String title, DesktopManager desktopManager, EmfConsole parent,
            EmfSession session) {
        super(title, new Dimension(600, 640), desktopManager);
        this.parent = parent;
        this.session = session;
        this.edit = !forView;
    }

    public void display(CaseJob job) throws EmfException {
        layout = createLayout();

        super.getContentPane().add(layout);
        super.display();
        super.resetChanges();
    }

    private JPanel createLayout() throws EmfException {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        messagePanel = new SingleLineMessagePanel();
        panel.add(messagePanel);
        this.jobFieldsPanel = new JobFieldsPanel(edit, messagePanel, this, parent, session);
        presenter.doAddJobFields(panel, jobFieldsPanel);
        panel.add(buttonsPanel());

        return panel;
    }

    private JPanel buttonsPanel() {
        JPanel panel = new JPanel();

        ok = new SaveButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                try {
                    if (hasChanges()) {
                        validateFields();
                        presenter.saveJob();
                    }
                    
                    disposeView();
                } catch (EmfException e1) {
                    messagePanel.setError(e1.getMessage());
                }
            }
        });

        if (edit) {
            getRootPane().setDefaultButton(ok);
            panel.add(ok);
        }

        Button cancel = new CloseButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doClose();
            }
        });
        panel.add(cancel);

        panel.setBorder(BorderFactory.createEmptyBorder(5, 0, 10, 0));

        return panel;
    }

    private void validateFields() throws EmfException {
        jobFieldsPanel.validateFields();
    }

    private void clearMessage() {
        messagePanel.clear();
    }

    public void windowClosing() {
        doClose();
    }

    public boolean shouldCreate() {
        return shouldCreate;
    }

    public void observe(EditCaseJobPresenterImpl presenter) {
        this.presenter = presenter;
    }

    private void doClose() {
        if (!edit || shouldDiscardChanges())
            super.disposeView();
    }

    public void loadCaseJob() throws EmfException {
        // NOTE Auto-generated method stub
        throw new EmfException("Under construction...");
    }

    public void populateFields() {
        // NOTE Auto-generated method stub

    }

    public void signalChanges() {
        clearMessage();
        super.signalChanges();
    }

}
