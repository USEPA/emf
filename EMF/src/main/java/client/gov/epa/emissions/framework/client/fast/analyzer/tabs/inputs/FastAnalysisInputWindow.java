package gov.epa.emissions.framework.client.fast.analyzer.tabs.inputs;

@SuppressWarnings("serial")
public abstract class FastAnalysisInputWindow implements FastAnalysisInputView {

    // public FastInputWindow(DesktopManager desktopManager, EmfSession session, EmfConsole parentConsole) {
    // super("Edit Fast Analysis", desktopManager, session, parentConsole);
    // }
    //
    // protected void doLayout(FastAnalysis analysis) {
    //
    // Container contentPane = getContentPane();
    // contentPane.removeAll();
    // contentPane.setLayout(new BorderLayout());
    // contentPane.add(this.getMessagePanel(), BorderLayout.PAGE_START);
    // contentPane.add(createTabbedPane(analysis));
    // contentPane.add(createButtonsPanel(), BorderLayout.PAGE_END);
    // }
    //
    // private JTabbedPane createTabbedPane(FastAnalysis analysis) {
    //
    // final JTabbedPane tabbedPane = new JTabbedPane();
    // tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
    // tabbedPane.addTab("Summary", this.createSummaryTab(analysis));
    // tabbedPane.addTab("Inputs", this.createInputsTab(analysis));
    // tabbedPane.addTab("Control Strategies", this.createControlStrategiesTab(analysis));
    // tabbedPane.addTab("Outputs", this.createOutputsTab(analysis));
    // return tabbedPane;
    // }
    //
    // public void refresh(FastAnalysis analysis) {
    // /*
    // * no-op
    // */
    // }
}