package gov.epa.emissions.framework.client.module;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.ConfirmDialog;
import gov.epa.emissions.commons.gui.ScrollableComponent;
import gov.epa.emissions.commons.gui.SelectAwareButton;
import gov.epa.emissions.commons.gui.TextArea;
import gov.epa.emissions.commons.gui.buttons.CloseButton;
import gov.epa.emissions.commons.gui.buttons.NewButton;
import gov.epa.emissions.commons.gui.buttons.RemoveButton;
import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.Label;
import gov.epa.emissions.framework.client.SpringLayoutGenerator;
import gov.epa.emissions.framework.client.ViewMode;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.meta.DatasetPropertiesViewer;
import gov.epa.emissions.framework.client.moduletype.ModuleTypeVersionConnectionsTableData;
import gov.epa.emissions.framework.client.moduletype.ModuleTypeVersionSubmoduleWindow;
import gov.epa.emissions.framework.client.moduletype.ModuleTypeVersionSubmodulesTableData;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.module.History;
import gov.epa.emissions.framework.services.module.HistoryDataset;
import gov.epa.emissions.framework.services.module.HistoryInternalDataset;
import gov.epa.emissions.framework.services.module.HistorySubmodule;
import gov.epa.emissions.framework.services.module.Module;
import gov.epa.emissions.framework.services.module.ModuleDataset;
import gov.epa.emissions.framework.services.module.ModuleTypeVersionSubmodule;
import gov.epa.emissions.framework.ui.RefreshButton;
import gov.epa.emissions.framework.ui.RefreshObserver;
import gov.epa.emissions.framework.ui.SelectableSortFilterWrapper;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;
import gov.epa.emissions.framework.client.util.ComponentUtility;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.Iterator;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SpringLayout;
import javax.swing.SwingWorker;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

public class HistorySubmoduleDetailsWindow extends DisposableInteralFrame implements HistorySubmoduleDetailsView, RefreshObserver {
    private HistorySubmoduleDetailsPresenter presenter;

    private EmfConsole parentConsole;
    private EmfSession session;

    private HistorySubmodule historySubmodule;
    private History history;
    private Module module;

    // layout
    private JPanel layout;
    private SingleLineMessagePanel messagePanel;
    private JTabbedPane tabbedPane;

    // summary
    private JPanel summaryPanel;
    private Label  submodulePathNames;
    private Label  runDate;
    private Label  status;
    private Label  result;
    private Label  comment;

    // scripts
    private JPanel          setupScriptPanel;
    private RSyntaxTextArea setupScript;
    private RTextScrollPane setupScriptScrollPane;

    private JPanel          userScriptPanel;
    private RSyntaxTextArea userScript;
    private RTextScrollPane userScriptScrollPane;

    private JPanel          teardownScriptPanel;
    private RSyntaxTextArea teardownScript;
    private RTextScrollPane teardownScriptScrollPane;

    // logs
    private JPanel   logsPanel;
    private TextArea logs;

    public HistorySubmoduleDetailsWindow(EmfConsole parentConsole, DesktopManager desktopManager, EmfSession session, HistorySubmodule historySubmodule) {
        super(getWindowTitle(historySubmodule), new Dimension(800, 600), desktopManager);

        layout = new JPanel();
        layout.setLayout(new BorderLayout());
        this.parentConsole = parentConsole;
        this.session = session;
        super.getContentPane().add(layout);
        
        this.historySubmodule = historySubmodule;
        this.history = historySubmodule.getHistory();
        this.module = history.getModule();
    }

    private static String getWindowTitle(HistorySubmodule historySubmodule) {
        return "Submodule History Details (ID=" + historySubmodule.getId() + ")";
    }
    
    public void observe(HistorySubmoduleDetailsPresenter presenter) {
        this.presenter = presenter;
    }

    public void display() {
        layout.removeAll();
        doLayout(layout);
        super.display();
    }

    private void doLayout(JPanel layout) {
        JPanel topPanel = new JPanel(new BorderLayout());
        messagePanel = new SingleLineMessagePanel();
        topPanel.add(messagePanel, BorderLayout.CENTER);
        Button button = new RefreshButton(this, "Refresh Submodule History Details", messagePanel);
        topPanel.add(button, BorderLayout.EAST);
        
        layout.add(topPanel, BorderLayout.NORTH);
        layout.add(tabbedPane(), BorderLayout.CENTER);
        layout.add(createButtonsPanel(), BorderLayout.SOUTH);
    }


    private JTabbedPane tabbedPane() {
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Summary", summaryPanel());
        tabbedPane.addTab("Setup Script", setupScriptPanel());
        tabbedPane.addTab("User Script", userScriptPanel());
        tabbedPane.addTab("Teardown Script", teardownScriptPanel());
        tabbedPane.addTab("Logs", logsPanel());
        return tabbedPane;
    }

    private JPanel summaryPanel() {
        summaryPanel = new JPanel(new BorderLayout());

        JPanel formPanel = new JPanel(new SpringLayout());
        SpringLayoutGenerator layoutGenerator = new SpringLayoutGenerator();

        submodulePathNames = new Label(historySubmodule.getSubmodulePathNames());
        layoutGenerator.addLabelWidgetPair("Submodule Path:", submodulePathNames, formPanel);

        runDate = new Label(CustomDateFormat.format_MM_DD_YYYY_HH_mm_ss(historySubmodule.getCreationDate()));
        layoutGenerator.addLabelWidgetPair("Run Date:", runDate, formPanel);

        status = new Label((historySubmodule.getStatus() == null) ? "" : historySubmodule.getStatus());
        layoutGenerator.addLabelWidgetPair("Status:", status, formPanel);

        result = new Label((historySubmodule.getResult() == null) ? "" : historySubmodule.getResult());
        layoutGenerator.addLabelWidgetPair("Result:", result, formPanel);

        comment = new Label((historySubmodule.getComment() == null) ? "" : historySubmodule.getComment());
        layoutGenerator.addLabelWidgetPair("Comment:", comment, formPanel);

        // Lay out the panel.
        layoutGenerator.makeCompactGrid(formPanel, 5, 2, // rows, cols
                10, 10, // initialX, initialY
                10, 10);// xPad, yPad

        summaryPanel.add(formPanel, BorderLayout.PAGE_START);
        return summaryPanel;
    }

    private JPanel setupScriptPanel() {
        setupScriptPanel = new JPanel(new BorderLayout());
        setupScript = new RSyntaxTextArea(historySubmodule.getSetupScript());
        setupScript.setEditable(false);
        setupScript.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        setupScript.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_SQL);
        setupScript.setCodeFoldingEnabled(true);
        setupScriptScrollPane = new RTextScrollPane(setupScript);
        setupScriptPanel.add(setupScriptScrollPane);
        return setupScriptPanel;
    }

    private JPanel userScriptPanel() {
        userScriptPanel = new JPanel(new BorderLayout());
        userScript = new RSyntaxTextArea(historySubmodule.getUserScript());
        userScript.setEditable(false);
        userScript.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        userScript.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_SQL);
        userScript.setCodeFoldingEnabled(true);
        userScriptScrollPane = new RTextScrollPane(userScript);
        userScriptPanel.add(userScriptScrollPane);
        return userScriptPanel;
    }

    private JPanel teardownScriptPanel() {
        teardownScriptPanel = new JPanel(new BorderLayout());
        teardownScript = new RSyntaxTextArea(historySubmodule.getTeardownScript());
        teardownScript.setEditable(false);
        teardownScript.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        teardownScript.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_SQL);
        teardownScript.setCodeFoldingEnabled(true);
        teardownScriptScrollPane = new RTextScrollPane(teardownScript);
        teardownScriptPanel.add(teardownScriptScrollPane);
        return teardownScriptPanel;
    }

    private JPanel logsPanel() {
        logsPanel = new JPanel(new BorderLayout());
        logs = new TextArea("logs", historySubmodule.getLogMessages(), 60);
        logs.setEditable(false);
        logs.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        ScrollableComponent scrollableLogs = new ScrollableComponent(logs);
        scrollableLogs.setMaximumSize(new Dimension(575, 200));
        logsPanel.add(scrollableLogs);

        return logsPanel;
    }

    private JPanel createButtonsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel container = new JPanel();
        FlowLayout layout = new FlowLayout();
        layout.setHgap(20);
        layout.setVgap(25);
        container.setLayout(layout);

        container.add(new CloseButton("Close", closeAction()));

        panel.add(container, BorderLayout.SOUTH);

        return panel;
    }

    @Override
    public void doRefresh() throws EmfException {
        module = presenter.getModule(module.getId());
        history = presenter.getHistory(history.getId());
        historySubmodule = history.getHistorySubmodules().get(historySubmodule.getSubmodulePath());
        refreshSummary();
        refreshScripts();
        refreshLogs();
    }

    private void refreshSummary() {
        submodulePathNames.setText(module.getName());
        runDate.setText(CustomDateFormat.format_MM_DD_YYYY_HH_mm_ss(historySubmodule.getCreationDate()));
        status.setText((historySubmodule.getStatus() == null) ? "" : historySubmodule.getStatus());
        result.setText((historySubmodule.getResult() == null) ? "" : historySubmodule.getResult());
        comment.setText((historySubmodule.getComment() == null) ? "" : historySubmodule.getComment());
    }
    
    public void refreshScripts() {
           setupScript.setText(historySubmodule.getSetupScript());
            userScript.setText(historySubmodule.getUserScript());
        teardownScript.setText(historySubmodule.getTeardownScript());
    }

    public void refreshLogs() {
        logs.setText(historySubmodule.getLogMessages());
    }

    //-------------------------------------------------------------------------
    
    private void clear() {
        messagePanel.clear();
    }

    public void windowClosing() {
        doClose();
    }

    private Action closeAction() {
        Action action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                doClose();
            }
        };

        return action;
    }

    private void doClose() {
        presenter.doClose();
    }
}
