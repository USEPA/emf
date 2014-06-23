package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.buttons.CloseButton;
import gov.epa.emissions.commons.gui.buttons.SaveButton;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.controlStrategy.CostYearTable;
import gov.epa.emissions.framework.ui.InfoDialog;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.StringTokenizer;

import javax.swing.AbstractAction;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

@SuppressWarnings("serial")
public class EditControlMeasureWindow extends DisposableInteralFrame implements ControlMeasureView {

    protected ControlMeasurePresenter presenter;

    private MessagePanel messagePanel;

    private EmfSession session;

    private EmfConsole parent;

    protected EditableCMSummaryTab editableCMSummaryTabView;

    private CostYearTable costYearTable;

    protected Button saveButton;

    protected ControlMeasureSccTabView controlMeasureSccTabView;

    protected ControlMeasureEfficiencyTab controlMeasureEfficiencyTabView;

    protected ControlMeasureEquationTab controlMeasureEquationTabView;

    protected ControlMeasurePropertyTab controlMeasurePropertyTabView;

    protected ControlMeasureReferencesTab controlMeasureReferencesTabView;

    public EditControlMeasureWindow(EmfConsole parent, EmfSession session, DesktopManager desktopManager,
            CostYearTable costYearTable) {
        super("Control Measure Editor", new Dimension(770, 500), desktopManager);
        this.desktopManager = desktopManager;
        this.session = session;
        this.parent = parent;
        this.costYearTable = costYearTable;
    }

    public void display(ControlMeasure measure) {
        setWindowTitle(measure);
        buildDisplay(measure);
        super.display();
        super.resetChanges();
    }

    protected void buildDisplay(ControlMeasure measure) {
        Container contentPane = super.getContentPane();
        contentPane.removeAll();

        JPanel panel = new JPanel(new BorderLayout());
        messagePanel = new SingleLineMessagePanel();
        panel.add(messagePanel, BorderLayout.PAGE_START);
        try {
            panel.add(createTabbedPane(measure, messagePanel), BorderLayout.CENTER);
        } catch (EmfException e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
        }
        panel.add(createBottomPanel(), BorderLayout.PAGE_END);

        contentPane.add(panel);
    }

    private JTabbedPane createTabbedPane(ControlMeasure measure, final MessagePanel messagePanel) throws EmfException {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setName("tabbedPane");
        tabbedPane.addTab("Summary", createSummaryTab(measure, messagePanel));
        tabbedPane.addTab("Efficiencies", createEfficiencyTab(measure, messagePanel));
        tabbedPane.addTab("SCCs", createSCCTab(measure, messagePanel));
        tabbedPane.addTab("Equations", createEquationTab(measure, messagePanel));
        tabbedPane.addTab("Properties", createPropertyTab(measure, messagePanel));
        tabbedPane.addTab("References", this.createReferencesTab(measure, messagePanel));
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tabbedPane.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                messagePanel.clear();
            }
        });

        return tabbedPane;
    }

    private JPanel createEquationTab(ControlMeasure measure, MessagePanel messagePanel) throws EmfException {
        controlMeasureEquationTabView = new ControlMeasureEquationTab(measure, session, this, messagePanel, parent,
                presenter);
        presenter.set(controlMeasureEquationTabView);
        return controlMeasureEquationTabView;
    }

    private JPanel createPropertyTab(ControlMeasure measure, MessagePanel messagePanel) {
        controlMeasurePropertyTabView = new ControlMeasurePropertyTab(measure, session, this, messagePanel, parent,
                presenter, desktopManager);
        presenter.set(controlMeasurePropertyTabView);
        return controlMeasurePropertyTabView;
    }

    private JPanel createReferencesTab(ControlMeasure measure, MessagePanel messagePanel) {

        this.controlMeasureReferencesTabView = new ControlMeasureReferencesTab(measure, this, messagePanel, parent,
                presenter, desktopManager);
        presenter.set(controlMeasureReferencesTabView);
        return controlMeasureReferencesTabView;
    }

    private JPanel createSCCTab(ControlMeasure measure, MessagePanel messagePanel) {
        controlMeasureSccTabView = new EditableCMSCCTab(measure, session, this, messagePanel, parent, presenter);
        presenter.set(controlMeasureSccTabView);
        return (JPanel) controlMeasureSccTabView;
    }

    private Component createEfficiencyTab(ControlMeasure measure, MessagePanel messagePanel) {
        controlMeasureEfficiencyTabView = new ControlMeasureEfficiencyTab(measure, this, parent, session,
                desktopManager, messagePanel, this, presenter, costYearTable);
        presenter.set(controlMeasureEfficiencyTabView);

        return controlMeasureEfficiencyTabView;
    }

    private JPanel createSummaryTab(ControlMeasure measure, MessagePanel messagePanel) {
        editableCMSummaryTabView = new EditableCMSummaryTab(measure, session, messagePanel, this, parent);
        editableCMSummaryTabView.populateValues();
        editableCMSummaryTabView.setTextFieldCaretPosition();
        presenter.set(editableCMSummaryTabView);
        return editableCMSummaryTabView;
    }

    public void notifyModified() {
        presenter.doModify();
    }

    private void setWindowTitle(ControlMeasure measure) {
        super.setTitle("Edit Control Measure: " + measure.getName());
        super.setName("editControlMeasure" + measure.getId());
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(createControlPanel(), BorderLayout.LINE_END);

        return panel;
    }

    private JPanel createControlPanel() {
        JPanel buttonsPanel = new JPanel();

        Button reportButton = new Button("Report", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                doReport();
            }
        });

        reportButton.setToolTipText("Generate \"At-a-glance\" report");

        buttonsPanel.add(reportButton);

        saveButton = new SaveButton(new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                doSave();
            }
        });

        buttonsPanel.add(saveButton);

        Button close = new CloseButton(new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                close();
            }
        });
        buttonsPanel.add(close);

        return buttonsPanel;
    }

    public void observe(ControlMeasurePresenter presenter) {
        this.presenter = presenter;
    }

    // FIXME: should this be mandatory for all EmfViews ?
    public void showError(String message) {
        // TODO: error should go away at some point. when ?
        messagePanel.setError(message);
    }

    public void windowClosing() {
        doClose();
    }

    public void close() {
        doClose();
    }

    private void doClose() {
        try {
            if (shouldDiscardChanges())
                presenter.doClose();
        } catch (EmfException e) {
            showError("Could not close: " + e.getMessage());
        }
    }

    private void doSave() {

        try {
            presenter.doSave(true);
            resetChanges();
        } catch (EmfException e) {
            showError(e.getMessage());
        }
    }

    private void doReport() {

        try {

            if (this.shouldProcessChanges("Save changes?", "Would you like to save current changes "
                    + System.getProperty("line.separator") + " prior to generating the report?")) {

                presenter.doSave(false);
                resetChanges();
            }
            else {
                //
            }
            
            presenter.doReport();
        } catch (EmfException e) {
            showError(e.getMessage());
        }
    }

    public void notifyLockFailure(ControlMeasure measure) {
        String message = "Cannot edit ControlMeasure: " + measure.getName() + System.getProperty("line.separator")
                + " as it was locked by User: " + measure.getLockOwner() + "(at "
                + CustomDateFormat.format_YYYY_MM_DD_HH_MM(measure.getLockDate()) + ")";
        InfoDialog dialog = new InfoDialog(this, "Message", message);
        dialog.confirm();
    }

    public void notifyEditFailure(ControlMeasure measure) {
        String message = "Cannot edit ControlMeasure: " + measure.getName()
                + " because you must be the creator of the measure or an Administrator";
        InfoDialog dialog = new InfoDialog(this, "Message", message);
        dialog.confirm();
    }
}
