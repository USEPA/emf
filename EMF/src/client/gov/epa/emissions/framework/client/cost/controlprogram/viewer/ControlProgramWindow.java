package gov.epa.emissions.framework.client.cost.controlprogram.viewer;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.DisabledButton;
import gov.epa.emissions.commons.gui.buttons.CloseButton;
import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.ControlProgram;
import gov.epa.emissions.framework.ui.InfoDialog;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public class ControlProgramWindow extends DisposableInteralFrame implements ControlProgramView {

    protected ControlProgramPresenter presenter;

    protected SingleLineMessagePanel messagePanel;

    protected EmfSession session;

    protected EmfConsole parentConsole;

    protected ControlProgramMeasuresTab measuresTabView;

    protected ControlProgramSummaryTab summaryTabView;

    protected ControlProgramTechnologiesTab technologiesTabView;

    protected ControlMeasure[] controlMeasures;

    public ControlProgramWindow(DesktopManager desktopManager, EmfSession session, EmfConsole parentConsole,
            ControlMeasure[] controlMeasures) {

        super("View Control Program", new Dimension(760, 580), desktopManager);

        this.session = session;
        this.desktopManager = desktopManager;
        this.parentConsole = parentConsole;
        this.controlMeasures = controlMeasures;
    }

    public void observe(ControlProgramPresenter presenter) {
        this.presenter = presenter;

    }

    public void display(ControlProgram controlProgram) {
        super.setLabel(super.getTitle() + ": " + controlProgram.getName());

        // this.controlProgram = controlProgram;

        doLayout(controlProgram);
        // pack();
        super.display();
        // super.resetChanges();
    }

    private void doLayout(ControlProgram controlProgram) {
        Container contentPane = getContentPane();
        contentPane.removeAll();

        messagePanel = new SingleLineMessagePanel();

        JPanel layout = new JPanel();
        layout.setLayout(new BorderLayout());
        layout.add(messagePanel, BorderLayout.PAGE_START);
        layout.add(createTabbedPane(controlProgram));
        layout.add(createButtonsPanel(), BorderLayout.PAGE_END);

        contentPane.add(layout);
    }

    private JTabbedPane createTabbedPane(ControlProgram controlProgram) {
        final JTabbedPane tabbedPane = new JTabbedPane();

        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tabbedPane.addTab("Summary", createSummaryTab(controlProgram));
        tabbedPane.addTab("Measures", createMeasuresTab());
        tabbedPane.addTab("Technologies", createTechnologiesTab());
        return tabbedPane;
    }

    private JPanel createSummaryTab(ControlProgram controlProgram) {
        try {
            summaryTabView = new ControlProgramSummaryTab(controlProgram, messagePanel, parentConsole, desktopManager);
            this.presenter.setSummaryTab(summaryTabView);
            return summaryTabView;
        } catch (EmfException e) {
            showError("Could not load Summary Tab." + e.getMessage());
            return createErrorTab("Could not create Summary Tab." + e.getMessage());
        }
    }

    private JPanel createMeasuresTab() {
        try {
            measuresTabView = new ControlProgramMeasuresTab(messagePanel, parentConsole, desktopManager);
            this.presenter.setMeasuresTab(measuresTabView);
        } catch (EmfException e) {
            showError("Could not create Measures tab.");
        }

        return measuresTabView;
    }

    private JPanel createTechnologiesTab() {

        try {
            technologiesTabView = new ControlProgramTechnologiesTab(messagePanel, parentConsole, desktopManager);
            this.presenter.setTechnologiesTab(technologiesTabView);
        } catch (EmfException e) {
            showError("Could not create Measures tab.");
        }

        return technologiesTabView;
    }

    private JPanel createErrorTab(String message) {
        JPanel panel = new JPanel(false);
        JLabel label = new JLabel(message);
        label.setForeground(Color.RED);
        panel.add(label);

        return panel;
    }

    private void showError(String message) {
        messagePanel.setError(message);
    }

    private JPanel createButtonsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel container = new JPanel();
        FlowLayout layout = new FlowLayout();
        layout.setHgap(20);
        layout.setVgap(15);
        container.setLayout(layout);

        JButton saveButton = new DisabledButton("Save");
        container.add(saveButton);

        Button closeButton = new CloseButton(closeAction());
        container.add(closeButton);
        getRootPane().setDefaultButton(saveButton);

        container.add(Box.createHorizontalStrut(20));

        panel.add(container, BorderLayout.CENTER);

        return panel;
    }

    private Action closeAction() {
        Action action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                clearMessage();
                doClose();
            }
        };

        return action;
    }

    protected void doClose() {
        try {
            presenter.doClose();
        } catch (EmfException e) {
            messagePanel.setError("Could not close: " + e.getMessage());
        }
    }

    public void notifyLockFailure(ControlProgram controlProgram) {
        String message = "Cannot edit Control Program: " + controlProgram + System.getProperty("line.separator")
                + " as it was locked by User: " + controlProgram.getLockOwner() + "(at "
                + format(controlProgram.getLockDate()) + ")";
        InfoDialog dialog = new InfoDialog(this, "Message", message);
        dialog.confirm();
    }

    private String format(Date lockDate) {
        return CustomDateFormat.format_YYYY_MM_DD_HH_MM(lockDate);
    }

    public void windowClosing() {
        doClose();
    }

    protected void clearMessage() {
        messagePanel.clear();
    }

    public void signalChanges() {
        clearMessage();
        super.signalChanges();
    }

    public void refresh(ControlProgram controlProgram) {
        // NOTE Auto-generated method stub

    }
}