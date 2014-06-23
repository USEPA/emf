package gov.epa.emissions.framework.client.fast.run.edit;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.fast.run.AbstractFastRunWindow;
import gov.epa.emissions.framework.services.fast.FastRun;

import java.awt.BorderLayout;
import java.awt.Container;

import javax.swing.JComponent;
import javax.swing.JTabbedPane;

@SuppressWarnings("serial")
public class FastRunEditorWindow extends AbstractFastRunWindow {

    public FastRunEditorWindow(DesktopManager desktopManager, EmfSession session, EmfConsole parentConsole) {
        super("Edit Fast Run", desktopManager, session, parentConsole);
    }

    protected void doLayout(FastRun run) {

        Container contentPane = getContentPane();
        contentPane.removeAll();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(this.getMessagePanel(), BorderLayout.PAGE_START);
        contentPane.add(createTabbedPane(run));
        contentPane.add(createButtonsPanel(), BorderLayout.PAGE_END);
    }

    private JTabbedPane createTabbedPane(FastRun run) {

        final JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

        JComponent summaryTab = this.createSummaryTab(run);
        tabbedPane.addTab(summaryTab.getName(), summaryTab);

        JComponent configurationTab = this.createConfigurationTab(run);
        tabbedPane.addTab(configurationTab.getName(), configurationTab);

        JComponent inventoriesTab = this.createInventoriesTab(run);
        tabbedPane.addTab(inventoriesTab.getName(), inventoriesTab);

        JComponent outputsTab = this.createOutputsTab(run);
        tabbedPane.addTab(outputsTab.getName(), outputsTab);

        return tabbedPane;
    }

    public void refresh(FastRun run) {
        /*
         * no-op
         */
    }
}