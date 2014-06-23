package gov.epa.emissions.framework.client.qa;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.QAProgram;
import gov.epa.emissions.commons.data.QAStepTemplate;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.buttons.CloseButton;
import gov.epa.emissions.commons.gui.buttons.SaveButton;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class EditQAStepTemplateWindow extends DisposableInteralFrame implements EditQAStepTemplateView {

    private boolean shouldCreate;

    private JPanel layout;

    private EditQAStepTemplatesPresenterImpl presenter;

    private QAStepTemplate template;

    private MessagePanel messagePanel;

    private Button ok;

    private QAStepTemplatePanel templatePanel;
    
    private EmfSession session;

    public EditQAStepTemplateWindow(String title, DesktopManager desktopManager) {
        super("Edit QA Step Template", new Dimension(550, 480), desktopManager);
        super.setLabel(super.getTitle() + ": " + title);
    }

    public void display(DatasetType type, QAProgram[] programs, QAStepTemplate template, EmfSession session) {
        this.template = template;
        this.session = session;
        layout = createLayout(type, programs);
        super.getContentPane().add(layout);
        super.display();
    }

    private JPanel createLayout(DatasetType type, QAProgram[] programs) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        messagePanel = new SingleLineMessagePanel();
        panel.add(messagePanel);
        this.templatePanel = new QAStepTemplatePanel(session, programs,messagePanel, this);
        panel.add(templatePanel);
        panel.add(buttonsPanel(type));

        populateFields(this.template);
        
        return panel;
    }

    private boolean verifyInput(DatasetType type) {
        String templatename = templatePanel.getTemplateName().trim();
        if (templatename.length() == 0) {
            JOptionPane.showMessageDialog(super.getParent(), "Please enter a Name.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (duplicate(templatename, type)) {
            JOptionPane.showMessageDialog(super.getParent(), "Name is duplicate. Please enter a different name.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
    }

    private boolean duplicate(String name, DatasetType type) {
        QAStepTemplate[] templates = type.getQaStepTemplates();
        for (int i = 0; i < templates.length; i++) {
            if (templates[i].getName().equals(name) && !template.getName().equalsIgnoreCase(name))
                return true;
        }

        return false;
    }

    private JPanel buttonsPanel(final DatasetType type) {
        JPanel panel = new JPanel();

        ok = new SaveButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doEdit(type);
            }
        });
        getRootPane().setDefaultButton(ok);
        panel.add(ok);

        Button cancel = new CloseButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doClose();
            }
        });
        panel.add(cancel);

        return panel;
    }

    private void doEdit(DatasetType type) {
        clearMessage();
        verifyInput(type);
        try {
            presenter.doEdit();
            disposeView();
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }
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

    public void observe(EditQAStepTemplatesPresenterImpl presenter) {
        this.presenter = presenter;
    }

    public void loadTemplate() throws EmfException {
        template.setName(templatePanel.getTemplateName().trim());
        template.setProgram(templatePanel.getProgram());
        template.setProgramArguments(templatePanel.getProgramArgs());
        template.setRequired(templatePanel.getRequired());
        template.setOrder(getOrder());
        template.setDescription(templatePanel.getDescription().trim());
    }

    private float getOrder() {
        return templatePanel.getOrder().length() == 0 ? 0 : Float.parseFloat(templatePanel.getOrder());
    }

    private void populateFields(QAStepTemplate template) {
        templatePanel.setFields(template);
        resetChanges();
    }

    private void doClose() {
        if (shouldDiscardChanges())
            super.disposeView();
    }

}
