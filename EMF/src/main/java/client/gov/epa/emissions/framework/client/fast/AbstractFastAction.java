package gov.epa.emissions.framework.client.fast;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.ui.MessagePanel;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

@SuppressWarnings("serial")
public abstract class AbstractFastAction extends AbstractAction {

    private MessagePanel messagePanel;

    private String errorMessage;

    public AbstractFastAction(MessagePanel messagePanel, String errorMessage) {

        this.messagePanel = messagePanel;
        this.errorMessage = errorMessage;
    }

    public void actionPerformed(ActionEvent e) {

        try {
            clearMessage();
            this.doActionPerformed(e);
        } catch (EmfException ex) {
            showError(this.errorMessage + ": " + ex.getMessage());
        }
    }

    protected abstract void doActionPerformed(ActionEvent e) throws EmfException;

    protected void showError(String message) {
        this.messagePanel.setError(message);
    }

    protected void showMessage(String message) {
        this.messagePanel.setMessage(message);
    }

    protected void clearMessage() {
        this.messagePanel.clear();
    }
}
