package gov.epa.emissions.framework.client.casemanagement.parameters;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.buttons.CloseButton;
import gov.epa.emissions.commons.gui.buttons.SaveButton;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.parameters.CaseParameter;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

public class EditCaseParameterWindow extends DisposableInteralFrame implements EditCaseParameterView {

    private boolean shouldCreate;

    private JPanel layout;

    private EditCaseParameterPresenterImpl presenter;

    private MessagePanel messagePanel;

    private Button ok;

    private boolean viewOnly = false;

    private ParameterFieldsPanel parameterFieldsPanel;

    public EditCaseParameterWindow(String title, DesktopManager desktopManager) {
        super(title, new Dimension(450, 550), desktopManager);
        // super.setLabel(super.getTitle() + ": " + title);
    }

    public void display(CaseParameter parameter) throws EmfException {
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
        this.parameterFieldsPanel = new ParameterFieldsPanel(messagePanel, this);
        presenter.doAddInputFields(panel, parameterFieldsPanel);
        panel.add(buttonsPanel());

        return panel;
    }

    private JPanel buttonsPanel() {
        JPanel panel = new JPanel();

        ok = new SaveButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                doSave();
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

    private void doSave() {
        clearMessage();

        try {
            if (hasChanges()) {
                doValidateFields();
                presenter.doSave();
            }
            
            disposeView();
        } catch (EmfException e) {
            e.printStackTrace();
            messagePanel.setError(e.getMessage());
        }
    }

    private void doValidateFields() throws EmfException {
        parameterFieldsPanel.validateFields();
    }

    // private void doCheckDuplicate() throws EmfException {
    // presenter.doCheckDuplicate(input);
    // }

    private void clearMessage() {
        messagePanel.clear();
    }

    public void windowClosing() {
        doClose();
    }

    public boolean shouldCreate() {
        return shouldCreate;
    }

    public void observe(EditCaseParameterPresenterImpl presenter) {
        this.presenter = presenter;
    }

    private void doClose() {
        if (viewOnly || shouldDiscardChanges())
            super.disposeView();
    }

    public void loadInput() throws EmfException {
        // NOTE Auto-generated method stub
        throw new EmfException("");
    }

    public void populateFields() {
        // NOTE Auto-generated method stub

    }

    public void signalChanges() {
        clearMessage();
        super.signalChanges();
    }

    public void viewOnly(String title) {
        viewOnly = true;
        ok.setVisible(false);
        parameterFieldsPanel.viewOnly();
    }

}
