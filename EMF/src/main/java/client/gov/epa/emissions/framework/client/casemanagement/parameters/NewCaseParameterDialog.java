package gov.epa.emissions.framework.client.casemanagement.parameters;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.Changeable;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.buttons.CancelButton;
import gov.epa.emissions.commons.gui.buttons.SaveButton;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.parameters.CaseParameter;
import gov.epa.emissions.framework.ui.Dialog;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

public class NewCaseParameterDialog extends Dialog implements NewCaseParameterView, ManageChangeables {

    private boolean shouldCreate;
    
    private EditParametersTabPresenterImpl presenter;

    private MessagePanel messagePanel;

    private ParameterFieldsPanel parameterFieldsPanel;
    
    public NewCaseParameterDialog(EmfConsole parent) {
        super("Add parameter to case ", parent);
        super.setSize(new Dimension(450, 550));
        super.setSize(new Dimension(450, 550));
        super.center();
    }

    public void display(int caseId, CaseParameter newParam) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.add(parameterPanel(newParam));
        panel.add(buttonsPanel());

        super.getContentPane().add(panel);
        super.display();
    }

    private JPanel parameterPanel(CaseParameter newParam) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        messagePanel = new SingleLineMessagePanel();
        panel.add(messagePanel);
        this.parameterFieldsPanel = new ParameterFieldsPanel(messagePanel, this);

        try {
            presenter.addParameterFields(newParam, panel, parameterFieldsPanel);
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }

        return panel;
    }

    private JPanel buttonsPanel() {
        JPanel panel = new JPanel();
        Button ok = new SaveButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                try {
                    addNewParameter();
                } catch (EmfException exc) {
                    messagePanel.setError(exc.getMessage());
                }
            }
        });
        getRootPane().setDefaultButton(ok);
        panel.add(ok);

        Button cancel = new CancelButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                shouldCreate = false;
                close();
            }
        });
        panel.add(cancel);

        return panel;
    }

    private void addNewParameter() throws EmfException {
        doValidateFields();
        shouldCreate = true;
        presenter.addNewParameter(parameter());
        close();
    }

    public boolean shouldCreate() {
        return shouldCreate;
    }

    public CaseParameter parameter() {
        try {
            parameterFieldsPanel.setFields();
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }

        return parameterFieldsPanel.getParameter();
    }

    public void register(Object presenter) {
        this.presenter = (EditParametersTabPresenterImpl) presenter;
    }
    
    private void doValidateFields() throws EmfException {
        parameterFieldsPanel.validateFields();
    }
    
    public void addChangeable(Changeable changeable) {
        // NOTE Auto-generated method stub
    }

    public void resetChanges() {
        // NOTE Auto-generated method stub
        
    }

}
