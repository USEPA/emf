package gov.epa.emissions.framework.client.fast.analyzer.create;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.fast.analyzer.AbstractFastAnalysisWindow;
import gov.epa.emissions.framework.client.fast.analyzer.FastAnalysisPresenter;
import gov.epa.emissions.framework.client.fast.analyzer.tabs.FastAnalysisConfigurationTab;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.fast.FastAnalysis;

import java.awt.BorderLayout;
import java.awt.Container;

import javax.swing.JComponent;
import javax.swing.JTabbedPane;

@SuppressWarnings("serial")
public class FastAnalysisCreatorWindow extends AbstractFastAnalysisWindow {

    public FastAnalysisCreatorWindow(DesktopManager desktopManager, EmfSession session, EmfConsole parentConsole) {
        super("New Fast Analysis", desktopManager, session, parentConsole);
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

        // tabbedPane.addTab("Inputs", this.createInputsTab(analysis));
        // tabbedPane.addTab("Control Strategies", this.createControlStrategiesTab(analysis));

        JComponent outputsTab = this.createOutputsTab(analysis);
        tabbedPane.addTab(outputsTab.getName(), outputsTab);

        return tabbedPane;
    }

    protected JComponent createConfigurationTab(FastAnalysis analysis) {

        try {
            FastAnalysisPresenter presenter = this.getPresenter();
            FastAnalysisConfigurationTab tab = new FastAnalysisConfigurationTab(analysis, this.getSession(), this
                    .getMessagePanel(), this, this.desktopManager, this.getParentConsole(), presenter) {
                protected void populateFields() {
                    // no-op: don't need to populate anything
                }
            };

            presenter.addTab(tab);
            return tab;
        } catch (EmfException e) {

            String message = "Could not load Configuration Tab." + e.getMessage();
            showError(message);
            return createErrorTab(message);
        }
    }

    public void refresh(FastAnalysis analysis) {
        /*
         * no-op
         */
    }
}