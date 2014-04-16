package gov.epa.emissions.framework.client.casemanagement.editor;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.buttons.CloseButton;
import gov.epa.emissions.commons.gui.buttons.SaveButton;
import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.casemanagement.CaseDescriptionPresenter;
import gov.epa.emissions.framework.client.casemanagement.CaseDescriptionWindow;
import gov.epa.emissions.framework.client.casemanagement.history.ShowHistoryTab;
import gov.epa.emissions.framework.client.casemanagement.inputs.EditInputsTab;
import gov.epa.emissions.framework.client.casemanagement.jobs.EditJobsTab;
import gov.epa.emissions.framework.client.casemanagement.outputs.EditOutputsTab;
import gov.epa.emissions.framework.client.casemanagement.parameters.EditParametersTab;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.meta.qa.EditQAArgumentsPresenter;
import gov.epa.emissions.framework.client.meta.qa.EditQAArgumentsWindow;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.ui.ErrorPanel;
import gov.epa.emissions.framework.ui.InfoDialog;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.RefreshObserver;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class CaseEditor extends DisposableInteralFrame implements CaseEditorView {

    private CaseEditorPresenter presenter;

    private MessagePanel messagePanel;

    private EmfSession session;

    private EmfConsole parentConsole;

    private String tabTitle;

    private JTabbedPane tabbedPane;

    private Case caseObj;

    public CaseEditor(EmfConsole parentConsole, EmfSession session, DesktopManager desktopManager) {
        super("Case Editor", new Dimension(820, 580), desktopManager);
        this.session = session;
        this.desktopManager = desktopManager;
        this.parentConsole = parentConsole;
    }

    private JTabbedPane createTabbedPane() {
        tabbedPane = new JTabbedPane();

        tabbedPane.addTab("Summary", createSummaryTab());
        tabbedPane.addTab("Jobs", createJobsTab());
        tabbedPane.addTab("Inputs", createInputTab());
        tabbedPane.addTab("Parameters", createParameterTab());
        tabbedPane.addTab("Outputs", createOutputTab());
        tabbedPane.addTab("History", createHistoryTab());
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        final MessagePanel localMsgPanel = this.messagePanel;

        tabbedPane.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                try {
                    localMsgPanel.clear();
                    loadComponents();
                } catch (EmfException exc) {
                    showError("Could not load component: " + tabbedPane.getSelectedComponent().getName());
                }
            }
        });

        return tabbedPane;
    }

    protected void loadComponents() throws EmfException {
        int tabIndex = tabbedPane.getSelectedIndex();
        tabTitle = tabbedPane.getTitleAt(tabIndex);
        presenter.doLoad(tabTitle);
    }

    private JPanel createSummaryTab() {
        EditableCaseSummaryTab view = new EditableCaseSummaryTab(caseObj, session, this, messagePanel, parentConsole);
        view.setDesktopManager(desktopManager);
        EditCaseSummaryTabPresenter summaryPresenter = new EditCaseSummaryTabPresenter(caseObj.getId(), session);
        view.observe(summaryPresenter);
        try {
            view.display();
        } catch (EmfException e) {
            messagePanel.setError(e.getMessage());
        }
        presenter.set(view);
        return view;
    }

    private Component createParameterTab() {
        EditParametersTab view = new EditParametersTab(parentConsole, messagePanel, desktopManager);
        presenter.set(view);
        return view;
    }

    private JPanel createInputTab() {
        try {
            EditInputsTab view = new EditInputsTab(parentConsole, this, messagePanel, desktopManager);
            presenter.set(view);
            return view;
        } catch (EmfException e) {
            showError("Could not load Parameter Tab." + e.getMessage());
            return createErrorTab("Could not load Parameter Tab." + e.getMessage());
        }
    }

    private Component createJobsTab() {
        try {
            EditJobsTab view = new EditJobsTab(parentConsole, this, messagePanel, desktopManager, session);
            presenter.set(view);
            return view;
        } catch (EmfException e) {
            showError("Could not load CaseJob Tab." + e.getMessage());
            return createErrorTab("Could not load CaseJob Tab." + e.getMessage());
        }
    }

    private JPanel createOutputTab() {
        try {
            EditOutputsTab view = new EditOutputsTab(parentConsole, this, messagePanel, desktopManager, session);
            presenter.set(view);
            return view;
        } catch (EmfException e) {
            showError("Could not load Output Tab. " + e.getMessage());
            return createErrorTab("Could not load Output Tab. " + e.getMessage());
        }
    }

    private JPanel createHistoryTab() {
        ShowHistoryTab view = new ShowHistoryTab(parentConsole, messagePanel, session);
        presenter.set(view);
        return view;
    }

    protected JPanel createErrorTab(String message) {
        return new ErrorPanel(message);
    }

    public void display(Case caseObj, String msg) {
        super.setLabel("Case Editor: " + caseObj);
        Container contentPane = super.getContentPane();
        contentPane.removeAll();

        this.caseObj = caseObj;

        JPanel panel = new JPanel(new BorderLayout());
        messagePanel = new SingleLineMessagePanel();
        panel.add(messagePanel, BorderLayout.PAGE_START);
        panel.add(createTabbedPane(), BorderLayout.CENTER);
        panel.add(createBottomPanel(), BorderLayout.PAGE_END);

        if (msg != null && !msg.isEmpty())
            messagePanel.setMessage(msg);

        contentPane.add(panel);
        super.display();
        resetChanges();
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(createControlPanel(), BorderLayout.LINE_END);

        return panel;
    }

    private JPanel createControlPanel() {
        JPanel buttonsPanel = new JPanel();

        Button describe = new Button("Describe", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                try {
                    describe();
                } catch (EmfException e) {
                    showError(e.getMessage());
                }
            }
        });
        buttonsPanel.add(describe);
        describe.setToolTipText("Show the description in a non-modal window.");
        
        Button refresh = new Button("Refresh", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                try {
                    refreshCurrentTab();
                } catch (EmfException e) {
                    showError(e.getMessage());
                }
            }
        });
        buttonsPanel.add(refresh);
        refresh.setToolTipText("Refresh only the current tab with focus.");

        Button loadCaseButton = new Button("Load", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                loadCase();
            }
        });
        buttonsPanel.add(loadCaseButton);
        loadCaseButton.setToolTipText("Load case inputs, parameters, and outputs from case log file.");
        
        Button printCaseButton = new Button("Export", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                printCase();
            }
        });
        buttonsPanel.add(printCaseButton);
        printCaseButton.setToolTipText("Export the settings for current case.");

        Button save = new SaveButton(new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                doSave();
            }
        });
        buttonsPanel.add(save);
        save.setToolTipText("Saves only the information on the Summary tab and the input and output folders.");

        Button viewParent = new Button("View Parent", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                viewParentCase();
            }
        });
        buttonsPanel.add(viewParent);
        
        Button viewRelated = new Button("View Related", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                viewRelatedCase();
            }
        });
        buttonsPanel.add(viewRelated);

        Button close = new CloseButton(new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                doClose();
            }
        });
        getRootPane().setDefaultButton(close);
        buttonsPanel.add(close);

        return buttonsPanel;
    }

    protected void describe() throws EmfException{ // BUG3621
        EditableCaseSummaryTab summaryTab = (EditableCaseSummaryTab) tabbedPane.getComponentAt(0);
        String descText = summaryTab.getDescription();
        CaseDescriptionWindow view = new CaseDescriptionWindow(desktopManager, descText, true);
        CaseDescriptionPresenter presenter = new CaseDescriptionPresenter(view,summaryTab);
        presenter.display();
    }

    private void refreshCurrentTab() throws EmfException {
        RefreshObserver tab = (RefreshObserver) tabbedPane.getSelectedComponent();
        RefreshObserver summaryTab = (RefreshObserver) tabbedPane.getComponentAt(0);
        try {
            messagePanel.clear();
            tab.doRefresh();
            if (tabbedPane.getSelectedIndex() != 0)
                summaryTab.doRefresh();
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        }
    }

    public void observe(CaseEditorPresenter presenter) {
        this.presenter = presenter;
    }

    public void showError(String message) {
        messagePanel.setError(message);
    }

    public void notifyLockFailure(Case caseObj) {
        String message = "Cannot edit Case: " + caseObj + System.getProperty("line.separator")
                + " as it was locked by User: " + caseObj.getLockOwner() + "(at " + format(caseObj.getLockDate()) + ")";
        InfoDialog dialog = new InfoDialog(parentConsole, "Message", message);
        dialog.confirm();
    }

    public void showRemindingMessage(String msg) {
        messagePanel.setMessage(msg);
    }

    private String format(Date lockDate) {
        return CustomDateFormat.format_YYYY_MM_DD_HH_MM(lockDate);
    }

    public void windowClosing() {
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
            presenter.doSave();
            messagePanel.setMessage("Case was saved successfully.");
            resetChanges();
        } catch (EmfException e) {
            showError(e.getMessage());
        }
    }

    private void printCase() {
        PrintCaseDialog printCase = new PrintCaseDialog("Export Case " + caseObj.getName(), this, parentConsole, session);
        PrintCasePresenter printPresenter = new PrintCasePresenter(session, caseObj);
        printPresenter.display(printCase);
    }

    private void loadCase() {
        LoadCaseDialog loadCaseView = new LoadCaseDialog("Load Data into Case \"" + caseObj.getName() + "\"", this, parentConsole, session);
        LoadCasePresenter printPresenter = new LoadCasePresenter(session, caseObj);
        printPresenter.display(loadCaseView);
    }

    public void showLockingMsg(String msg) {
        InfoDialog dialog = new InfoDialog(parentConsole, "Message", msg);
        dialog.confirm();
    }

    private void viewParentCase() {
        try {
            Case parentCase = presenter.getCaseFromName(caseObj.getTemplateUsed());
            if (parentCase == null) {
                showRemindingMessage("No parent case is available. ");
                return;
            }
            CaseViewer view = new CaseViewer(parentConsole, session, desktopManager);
            presenter.doView(view, parentCase);
        } catch (EmfException e) {
            showError(e.getMessage());
        }

    }
    
    private void viewRelatedCase() {
        try {
            Case[] produceInputCases = presenter.getCasesThatInputToOtherCases();
            Case[] useAsOutputCases  = presenter.getCasesThatOutputToOtherCases();
                
            String title = "View Related Cases: " + caseObj.getName();
            RelatedCaseView view = new RelatedCaseWindow(title, session, parentConsole, desktopManager);
            presenter.doViewRelated(view, produceInputCases, useAsOutputCases);
        } catch (EmfException e) {
            showError(e.getMessage());
        }

    } 

}
