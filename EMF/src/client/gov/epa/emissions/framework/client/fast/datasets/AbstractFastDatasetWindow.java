package gov.epa.emissions.framework.client.fast.datasets;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.buttons.CloseButton;
import gov.epa.emissions.commons.gui.buttons.SaveButton;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.fast.AbstractFastAction;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.fast.FastDataset;
import gov.epa.emissions.framework.services.fast.FastDatasetWrapper;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.Box;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public abstract class AbstractFastDatasetWindow extends DisposableInteralFrame implements FastDatasetView {

    private FastDatasetPresenter presenter;

    private MessagePanel messagePanel;

    private EmfSession session;

    private EmfConsole parentConsole;

    public AbstractFastDatasetWindow(String title, DesktopManager desktopManager, EmfSession session,
            EmfConsole parentConsole) {

        super(title, new Dimension(760, 580), desktopManager);

        this.session = session;
        this.parentConsole = parentConsole;
        this.messagePanel = new SingleLineMessagePanel();
    }

    public void observe(FastDatasetPresenter presenter) {
        this.presenter = presenter;
    }

    protected MessagePanel getMessagePanel() {
        return this.messagePanel;
    }

    public void display(FastDatasetWrapper wrapper) {

        this.setLabel(this.getTitle());
        doLayout(wrapper);
        super.display();
    }

    abstract protected void doLayout(FastDatasetWrapper wrapper);

    abstract protected boolean showSave();

    protected JPanel createButtonsPanel() {

        JPanel panel = new JPanel(new BorderLayout());

        JPanel container = new JPanel();
        FlowLayout layout = new FlowLayout();
        layout.setHgap(20);
        layout.setVgap(15);
        container.setLayout(layout);

        if (this.showSave()) {

            Button saveButton = new SaveButton(saveAction());
            container.add(saveButton);
            getRootPane().setDefaultButton(saveButton);
        }

        Button closeButton = new CloseButton(closeAction());
        container.add(closeButton);

        if (!this.showSave()) {
            getRootPane().setDefaultButton(closeButton);
        }

        container.add(Box.createHorizontalStrut(20));

        panel.add(container, BorderLayout.CENTER);

        return panel;
    }

    protected Action closeAction() {
        Action action = new AbstractFastAction(this.getMessagePanel(), "Error closing window") {

            @Override
            protected void doActionPerformed(ActionEvent e){
                doClose();
            }
        };

        return action;
    }

    protected void doClose() {
        try {
            if (shouldDiscardChanges()) {
                presenter.doClose();
            }
        } catch (EmfException e) {
            messagePanel.setError("Could not close: " + e.getMessage());
        }
    }

    protected Action saveAction() {
        Action action = new AbstractFastAction(this.getMessagePanel(), "Error saving changes") {

            @Override
            protected void doActionPerformed(ActionEvent e) throws EmfException {
                presenter.doSave();
                showMessage("Dataset was saved successfully.");
                resetChanges();
            }
        };

        return action;
    }

    public void windowClosing() {
        doClose();
    }

    public void signalChanges() {

        clearMessage();
        super.signalChanges();
    }

    public void showError(String message) {
        this.messagePanel.setError(message);
    }

    public void showMessage(String message) {
        this.messagePanel.setMessage(message);
    }

    public void clearMessage() {
        this.messagePanel.clear();
    }

    public EmfSession getSession() {
        return session;
    }

    public EmfConsole getParentConsole() {
        return parentConsole;
    }

    public FastDatasetPresenter getPresenter() {
        return presenter;
    }

    public void refresh(FastDataset dataset) {
        /*
         * no-op
         */
    }
}