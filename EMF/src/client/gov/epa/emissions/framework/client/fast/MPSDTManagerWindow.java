package gov.epa.emissions.framework.client.fast;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.ReusableInteralFrame;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.fast.analyzer.FastAnalysisManagerPresenter;
import gov.epa.emissions.framework.client.fast.analyzer.FastAnalysisManagerPresenterImpl;
import gov.epa.emissions.framework.client.fast.analyzer.FastAnalysisManagerWindow;
import gov.epa.emissions.framework.client.fast.datasets.FastDatasetManagerPresenter;
import gov.epa.emissions.framework.client.fast.datasets.FastDatasetManagerPresenterImpl;
import gov.epa.emissions.framework.client.fast.datasets.FastDatasetManagerWindow;
import gov.epa.emissions.framework.client.fast.run.FastRunManagerPresenter;
import gov.epa.emissions.framework.client.fast.run.FastRunManagerPresenterImpl;
import gov.epa.emissions.framework.client.fast.run.FastRunManagerWindow;
import gov.epa.emissions.framework.services.EmfException;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class MPSDTManagerWindow extends ReusableInteralFrame implements MPSDTManagerView {

    private MPSDTManagerPresenter presenter;

    private FastDatasetManagerPresenter datasetManagerPresenter;

    private FastRunManagerPresenter runManagerPresenter;

    private FastAnalysisManagerPresenter analysisManagerPresenter;

    private MPSDTTabManager tabManager;

    private EmfConsole parentConsole;

    private EmfSession session;

//    private static final String ROOT_SELECT_PROMPT = "Please select one or more Fast entities to ";

    public MPSDTManagerWindow(EmfConsole parentConsole, EmfSession session, DesktopManager desktopManager) {

        super("MP-SDT Manager", new Dimension(900, 500), desktopManager);

        this.session = session;
        this.parentConsole = parentConsole;
    }

    public void observe(MPSDTManagerPresenter presenter) {
        this.presenter = presenter;
    }

    public void display() {

        doLayout(this.session);

        try {
            this.datasetManagerPresenter.display();
        } catch (EmfException e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
        }

        try {
            this.runManagerPresenter.display();
        } catch (EmfException e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
        }

        try {
            this.analysisManagerPresenter.display();
        } catch (EmfException e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
        }

        super.display();
    }

    private void doLayout(EmfSession session) {

        Container contentPane = this.getContentPane();
        contentPane.removeAll();
        contentPane.setLayout(new BorderLayout());

        contentPane.add(createTabPanel(parentConsole, session), BorderLayout.CENTER);
        contentPane.add(createControlPanel(), BorderLayout.SOUTH);
    }

    private JComponent createTabPanel(EmfConsole parentConsole, EmfSession session) {

        this.tabManager = new MPSDTTabManager();

        FastDatasetManagerWindow datasetTab = new FastDatasetManagerWindow(parentConsole, session, this
                .getDesktopManager());
        datasetTab.doLayout();
        this.datasetManagerPresenter = new FastDatasetManagerPresenterImpl(session, datasetTab);
        this.tabManager.addTab(datasetTab.getTitle(), datasetTab);

        FastRunManagerWindow runTab = new FastRunManagerWindow(parentConsole, session, this.getDesktopManager());
        runTab.doLayout();
        this.runManagerPresenter = new FastRunManagerPresenterImpl(session, runTab);
        this.tabManager.addTab(runTab.getTitle(), runTab);

        FastAnalysisManagerWindow entityTab = new FastAnalysisManagerWindow(parentConsole, session, this
                .getDesktopManager());
        entityTab.doLayout();
        this.analysisManagerPresenter = new FastAnalysisManagerPresenterImpl(session, entityTab);
        this.tabManager.addTab(entityTab.getTitle(), entityTab);

        return this.tabManager;
    }

    private JPanel createControlPanel() {

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {

                runManagerPresenter.doClose();
                presenter.doClose();
            }
        });
        getRootPane().setDefaultButton(closeButton);

        JPanel controlPanel = new JPanel(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.EAST;
        constraints.weightx = 1;
        constraints.insets = new Insets(5, 5, 5, 5);
        controlPanel.add(closeButton, constraints);

        return controlPanel;
    }

    public EmfConsole getParentConsole() {
        return this.parentConsole;
    }

    public DesktopManager getDesktopManager() {
        return this.desktopManager;
    }
}
