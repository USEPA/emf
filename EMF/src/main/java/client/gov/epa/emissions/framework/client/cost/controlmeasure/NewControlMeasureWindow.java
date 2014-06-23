package gov.epa.emissions.framework.client.cost.controlmeasure;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.buttons.CloseButton;
import gov.epa.emissions.commons.gui.buttons.SaveButton;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlMeasure;
import gov.epa.emissions.framework.services.cost.controlStrategy.CostYearTable;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public class NewControlMeasureWindow extends DisposableInteralFrame implements ControlMeasureView {

    private ControlMeasurePresenter presenter;

    private MessagePanel messagePanel;

    private EmfSession session;

    private EmfConsole parent;
    
    private static int count = 0;

    private NewCMSummaryTab cMSummaryTabView;
    //private static final DateFormat dateFormat = new SimpleDateFormat(EmfDateFormat.format());
    private CostYearTable costYearTable;

    public NewControlMeasureWindow(EmfConsole parent, EmfSession session, DesktopManager desktopManager, CostYearTable costYearTable) {
        super("New Control Measure", new Dimension(770, 500), desktopManager); 
        this.desktopManager = desktopManager;
        this.session = session;
        this.parent = parent;
        this.costYearTable = costYearTable;
    }

    public void display(ControlMeasure measure) {
        setWindowTitle();
        Container contentPane = super.getContentPane();
        contentPane.removeAll();

        JPanel panel = new JPanel(new BorderLayout());
        messagePanel = new SingleLineMessagePanel();
        panel.add(messagePanel, BorderLayout.PAGE_START);
        panel.add(createTabbedPane(measure, messagePanel), BorderLayout.CENTER);
        panel.add(createBottomPanel(), BorderLayout.PAGE_END);

        contentPane.add(panel);
        super.display();
        super.resetChanges();
    }

    private JTabbedPane createTabbedPane(ControlMeasure measure, MessagePanel messagePanel) {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setName("tabbedPane");

        tabbedPane.addTab("Summary", createSummaryTab(measure, messagePanel));

        tabbedPane.addTab("Efficiencies", createEfficiencyTab(measure, messagePanel));

        tabbedPane.addTab("SCCs", createSCCTab(measure, messagePanel));
        
        try {
            tabbedPane.addTab("Equations", createEquationTab(measure, messagePanel));
        } catch (EmfException e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
        }
        
        tabbedPane.addTab("Properties", this.createPropertyTab(measure, messagePanel));
        tabbedPane.addTab("References", this.createReferencesTab(measure, messagePanel));

        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

        return tabbedPane;
    }

    private JPanel createPropertyTab(ControlMeasure measure, MessagePanel messagePanel) {
        ControlMeasurePropertyTab controlMeasurePropertyTabView = new ControlMeasurePropertyTab(measure, session, this, messagePanel, parent,
                presenter, desktopManager);
        presenter.set(controlMeasurePropertyTabView);
        return controlMeasurePropertyTabView;
    }

    private JPanel createReferencesTab(ControlMeasure measure, MessagePanel messagePanel) {

        ControlMeasureReferencesTab controlMeasureReferencesTabView = new ControlMeasureReferencesTab(measure, this, messagePanel, parent,
                presenter, desktopManager);
        presenter.set(controlMeasureReferencesTabView);
        return controlMeasureReferencesTabView;
    }

    private JPanel createEquationTab(ControlMeasure measure, MessagePanel messagePanel) throws EmfException{
        ControlMeasureEquationTab view=new ControlMeasureEquationTab(measure, session, this, messagePanel, parent, presenter); 
        presenter.set(view);
        return view;
    }
    
    private JPanel createSCCTab(ControlMeasure measure, MessagePanel messagePanel) {
        ControlMeasureSccTabView view = new EditableCMSCCTab(measure, session,
                this, messagePanel, 
                parent, presenter);
        presenter.set(view);
        return (JPanel) view;
    }

    private Component createEfficiencyTab(ControlMeasure measure, MessagePanel messagePanel) {
        ControlMeasureEfficiencyTab view = new ControlMeasureEfficiencyTab(measure, this, 
                parent, session, 
                desktopManager, messagePanel, 
                this, presenter, costYearTable);
        presenter.set(view);
        
        return view;
    }

    private JPanel createSummaryTab(ControlMeasure measure, MessagePanel messagePanel) {
        cMSummaryTabView = new NewCMSummaryTab(measure, session, messagePanel, this);
        cMSummaryTabView.populateValues();
        presenter.set(cMSummaryTabView);
        return cMSummaryTabView;
    }
    
    private void setWindowTitle() {
        int number = ++count;
        super.setTitle("New Control Measure: " + number);
        super.setName("newControlMeasure" + number);
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(createControlPanel(), BorderLayout.LINE_END);

        return panel;
    }

    private JPanel createControlPanel() {
        JPanel buttonsPanel = new JPanel();

        Button save = new SaveButton(new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                doSave();
            }
        });
        getRootPane().setDefaultButton(save);
        buttonsPanel.add(save);

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

    public void showError(String message) {
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
            presenter.doSave(false);
            disposeView();
            resetChanges();
        } catch (EmfException e) {
            showError(e.getMessage());
        }
    }

    public void notifyLockFailure(ControlMeasure measure) {
        // NOTE Auto-generated method stub
    }

    public void notifyModified() {
        presenter.doModify();
    }

    public void notifyEditFailure(ControlMeasure measure) {
        // NOTE Auto-generated method stub
        
    }
    
}
