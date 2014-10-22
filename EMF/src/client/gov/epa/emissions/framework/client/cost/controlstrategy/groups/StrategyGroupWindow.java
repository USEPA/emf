package gov.epa.emissions.framework.client.cost.controlstrategy.groups;

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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.commons.gui.buttons.CloseButton;
import gov.epa.emissions.commons.gui.buttons.SaveButton;
import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.client.DisposableInteralFrame;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.controlStrategy.StrategyGroup;
import gov.epa.emissions.framework.ui.InfoDialog;
import gov.epa.emissions.framework.ui.SingleLineMessagePanel;

public class StrategyGroupWindow extends DisposableInteralFrame implements StrategyGroupView {
    
    protected StrategyGroupPresenter presenter;

    protected SingleLineMessagePanel messagePanel;

    protected EmfSession session;

    protected EmfConsole parentConsole;

    public StrategyGroupWindow(DesktopManager desktopManager, EmfSession session, EmfConsole parentConsole) {
        super("Edit Control Strategy Group", new Dimension(760, 580), desktopManager);

        this.session = session;
        this.parentConsole = parentConsole;
    }

    public void observe(StrategyGroupPresenter presenter) {
        this.presenter = presenter;
    }

    public void display(StrategyGroup strategyGroup) {
        super.setLabel(super.getTitle() + ": " + strategyGroup.getName());
        doLayout(strategyGroup);
        super.display();
    }

    private void doLayout(StrategyGroup strategyGroup) {
        Container contentPane = getContentPane();
        contentPane.removeAll();

        messagePanel = new SingleLineMessagePanel();

        JPanel layout = new JPanel();
        layout.setLayout(new BorderLayout());
        layout.add(messagePanel, BorderLayout.PAGE_START);
        layout.add(createTabbedPane(strategyGroup));
        layout.add(createButtonsPanel(strategyGroup), BorderLayout.PAGE_END);

        contentPane.add(layout);
    }

    private JTabbedPane createTabbedPane(StrategyGroup strategyGroup) {
        final JTabbedPane tabbedPane = new JTabbedPane();

        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tabbedPane.addTab(presenter.NOTES_TAB, createTab(presenter.NOTES_TAB, strategyGroup));
        tabbedPane.addTab(presenter.STRATEGIES_TAB, createTab(presenter.STRATEGIES_TAB, strategyGroup));
        return tabbedPane;
    }

    private JPanel createTab(String tabName, StrategyGroup strategyGroup) {
        StrategyGroupTabView tabView = null;
        if (presenter.STRATEGIES_TAB.equals(tabName)) {
            tabView = new StrategyGroupStrategiesTab(strategyGroup, session, this, messagePanel, 
                    parentConsole);
        } else if (presenter.NOTES_TAB.equals(tabName)) {
            tabView = new StrategyGroupNotesTab(strategyGroup, session, this, messagePanel);
        }
        if (tabView != null) {
            try {
                presenter.set(tabName, tabView);
            } catch (EmfException e) {
                showError("Could not load " + tabName + ".");
                if (presenter.STRATEGIES_TAB.equals(tabName))
                    return createErrorTab("Could not create " + tabName + "." + e.getMessage());
            }
            return (JPanel) tabView;
        }
        
        return createErrorTab("Unknown tab");
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
    
    private JPanel createButtonsPanel(StrategyGroup strategyGroup) {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel container = new JPanel();
        FlowLayout layout = new FlowLayout();
        layout.setHgap(20);
        layout.setVgap(15);
        container.setLayout(layout);

        Button saveButton = new SaveButton(saveAction());
        container.add(saveButton);

        Button closeButton = new CloseButton(closeAction());
        container.add(closeButton);
        getRootPane().setDefaultButton(saveButton);

        container.add(Box.createHorizontalStrut(20));

        panel.add(container, BorderLayout.CENTER);

        return panel;
    }

    protected void save() throws EmfException {
        clearMessage();
        presenter.doSave();
        messagePanel.setMessage("Control Strategy Group was saved successfully.");
        resetChanges();
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
            if (shouldDiscardChanges()) presenter.doClose();
        } catch (EmfException e) {
            messagePanel.setError("Could not close: " + e.getMessage());
        }
    }

    private Action saveAction() {
        Action action = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                try {
                    save();
                } catch (EmfException e) {
                    messagePanel.setError(e.getMessage());
                }
            }
        };

        return action;
    }

    public void notifyLockFailure(StrategyGroup strategyGroup) {
        String message = "Cannot edit Control Strategy Group: " + strategyGroup
                + System.getProperty("line.separator") + " as it was locked by User: " + strategyGroup.getLockOwner()
                + "(at " + format(strategyGroup.getLockDate()) + ")";
        InfoDialog dialog = new InfoDialog(this, "Message", message);
        dialog.confirm();
    }

    private String format(Date lockDate) {
        return CustomDateFormat.format_YYYY_MM_DD_HH_MM(lockDate);
    }

    protected void clearMessage() {
        messagePanel.clear();
    }
}
