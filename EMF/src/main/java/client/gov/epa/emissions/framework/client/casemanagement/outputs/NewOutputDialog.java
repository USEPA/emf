package gov.epa.emissions.framework.client.casemanagement.outputs;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.Changeable;
import gov.epa.emissions.commons.gui.ManageChangeables;
import gov.epa.emissions.commons.gui.buttons.CancelButton;
import gov.epa.emissions.commons.gui.buttons.SaveButton;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.outputs.CaseOutput;
import gov.epa.emissions.framework.ui.Dialog;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

public class NewOutputDialog extends Dialog implements NewOutputView, ManageChangeables {

    protected boolean shouldCreate;

    protected EditOutputsTabPresenterImpl presenter;

    private MessagePanel messagePanel;
    
    private EmfConsole parentConsole; 

    private OutputFieldsPanel OutputFieldsPanel;
    
    public NewOutputDialog(EmfConsole parent) {
        super("Add Output to case", parent);
        super.setSize(new Dimension(630, 500));
        super.center();
        this.parentConsole = parent;
    }

    public void display(int caseId, CaseOutput newOutput) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.add(OutputPanel(newOutput));
        panel.add(buttonsPanel());

        super.getContentPane().add(panel);
        super.display();
    }

    private JPanel OutputPanel(CaseOutput newOutput) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        messagePanel = new SingleLineMessagePanel();
        panel.add(messagePanel);
        this.OutputFieldsPanel = new OutputFieldsPanel(messagePanel, this, parentConsole);

        try {
            presenter.doAddOutputFields(panel, OutputFieldsPanel, newOutput);
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
                    addNewOutput();
                } catch (EmfException exc) {
                    messagePanel.setError(exc.getMessage());
                }
            }
        });
        getRootPane().setDefaultButton(ok);
        panel.add(ok);

        Button cancel = new CancelButton(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
//                shouldCreate = false;
                close();
            }
        });
        panel.add(cancel);

        return panel;
    }

    private void addNewOutput() throws EmfException {
        doValidateFields();
//        shouldCreate = true;
        presenter.addNewOutput(output());
        close();
    }

//    public boolean shouldCreate() {
//        return shouldCreate;
//    }

    private CaseOutput output() {
       return  OutputFieldsPanel.setFields();
//        return OutputFieldsPanel.getOutput();
    }

    public void observe(Object presenter) {
        this.presenter = (EditOutputsTabPresenterImpl) presenter;
    }
    
    private void doValidateFields() throws EmfException {
        OutputFieldsPanel.validateFields();
    }
    
    public void addChangeable(Changeable changeable) {
        // NOTE Auto-generated method stub
    }

    public void resetChanges() {
        // NOTE Auto-generated method stub
        
    }

}
