package gov.epa.emissions.framework.client.fast.analyzer.edit;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.fast.analyzer.AbstractFastAnalysisWindow;
import gov.epa.emissions.framework.services.fast.FastAnalysis;

import java.awt.BorderLayout;
import java.awt.Container;

import javax.swing.JComponent;
import javax.swing.JTabbedPane;

@SuppressWarnings("serial")
public class FastAnalysisEditorWindow extends AbstractFastAnalysisWindow {

    public FastAnalysisEditorWindow(DesktopManager desktopManager, EmfSession session, EmfConsole parentConsole) {
        super("Edit Fast Analysis", desktopManager, session, parentConsole);
    }

    protected void doLayout(FastAnalysis analysis) {

        Container contentPane = getContentPane();
        contentPane.removeAll();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(this.getMessagePanel(), BorderLayout.PAGE_START);
        contentPane.add(createTabbedPane(analysis));
        contentPane.add(createButtonsPanel(), BorderLayout.PAGE_END);
    }

    private JTabbedPane createTabbedPane(FastAnalysis analysis) {

        final JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

        JComponent summaryTab = this.createSummaryTab(analysis);
        tabbedPane.addTab(summaryTab.getName(), summaryTab);

        JComponent configurationTab = this.createConfigurationTab(analysis);
        tabbedPane.addTab(configurationTab.getName(), configurationTab);

        //        tabbedPane.addTab("Inputs", this.createInputsTab(analysis));
//        tabbedPane.addTab("Control Strategies", this.createControlStrategiesTab(analysis));

        JComponent outputsTab = this.createOutputsTab(analysis);
        tabbedPane.addTab(outputsTab.getName(), outputsTab);

        return tabbedPane;
    }

    public void refresh(FastAnalysis analysis) {
        /*
         * no-op
         */
    }
}