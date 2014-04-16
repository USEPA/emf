package gov.epa.emissions.framework.client.fast.analyzer.tabs;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.commons.gui.Button;
import gov.epa.emissions.framework.client.EmfInternalFrame;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.data.viewer.DataViewPresenter;
import gov.epa.emissions.framework.client.data.viewer.DataViewer;
import gov.epa.emissions.framework.client.fast.ExportPresenter;
import gov.epa.emissions.framework.client.fast.ExportPresenterImpl;
import gov.epa.emissions.framework.client.fast.ExportToNetCDFPresenterImpl;
import gov.epa.emissions.framework.client.fast.ExportToNetCDFWindow;
import gov.epa.emissions.framework.client.fast.ExportWindow;
import gov.epa.emissions.framework.client.fast.analyzer.FastAnalysisPresenter;
import gov.epa.emissions.framework.client.meta.DatasetPropertiesViewer;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.fast.FastAnalysis;
import gov.epa.emissions.framework.services.fast.FastAnalysisOutput;
import gov.epa.emissions.framework.services.fast.FastOutputExportWrapper;
import gov.epa.emissions.framework.ui.MessagePanel;
import gov.epa.emissions.framework.ui.SelectableSortFilterWrapper;
import gov.epa.mims.analysisengine.table.sort.SortCriteria;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class FastAnalysisOutputsTab extends AbstractFastAnalysisTab {

    private SelectableSortFilterWrapper table;

    private static final String ROOT_SELECT_PROMPT = "Please select one or more Fast analysis outputs to ";

    public FastAnalysisOutputsTab(FastAnalysis analysis, EmfSession session, MessagePanel messagePanel,
            EmfInternalFrame parentInternalFrame, DesktopManager desktopManager, EmfConsole parentConsole,
            FastAnalysisPresenter presenter) {

        super(analysis, session, messagePanel, parentInternalFrame, desktopManager, parentConsole, presenter);
        this.setName("Outputs");
    }

    public void display() {

        this.setLayout(new BorderLayout());
        this.add(createTablePanel(this.getAnalysis(), this.getParentConsole(), this.getSession()), BorderLayout.CENTER);
        this.add(createButtonsPanel(), BorderLayout.PAGE_END);
        super.display();
    }

    protected void addChangables() {
        /*
         * no-op
         */
    }

    protected void populateFields() {
        /*
         * no-op
         */
    }

    public void save(FastAnalysis analysis) {
        this.clearMessage();
    }

    @Override
    void refreshData() {

        FastAnalysisOutput[] analysisOutputs = getFastAnalysisOutputs();
        this.table.refresh(new FastAnalysisOutputTableData(analysisOutputs));
    }

    private JPanel createTablePanel(FastAnalysis analysis, EmfConsole parentConsole, EmfSession session) {

        FastAnalysisOutput[] analysisOutputs = getFastAnalysisOutputs();

        JPanel tablePanel = new JPanel(new BorderLayout());
        this.table = new SelectableSortFilterWrapper(parentConsole, new FastAnalysisOutputTableData(analysisOutputs),
                sortCriteria());
        tablePanel.add(this.table, BorderLayout.CENTER);

        return tablePanel;
    }

    protected JPanel createButtonsPanel() {

        JPanel panel = new JPanel(new BorderLayout());

        JPanel container = new JPanel();
        FlowLayout layout = new FlowLayout();
        layout.setHgap(10);
        layout.setVgap(5);
        container.setLayout(layout);

        Button viewDataButton = new Button("View Output", getViewDataAction());
        container.add(viewDataButton);

        Button exportButton = new Button("Export to Shapefile", getExportAction());
        container.add(exportButton);

        Button exportToNetCDFButton = new Button("Export to NetCDF", getExportToNetCDFAction());
        container.add(exportToNetCDFButton);

        panel.add(container, BorderLayout.LINE_START);

        return panel;
    }

    private Action getViewDataAction() {

        return new AbstractAction() {

            public void actionPerformed(ActionEvent e) {
                doViewData();
            }
        };
    }

    private void doViewData() {

        List<FastAnalysisOutput> analysisOutputs = getSelected();
        if (analysisOutputs.isEmpty()) {
            this.showMessage(ROOT_SELECT_PROMPT + "edit.");
        } else {
            for (FastAnalysisOutput analysisOutput : analysisOutputs) {

                try {
                    showDatasetDataViewer(analysisOutput.getOutputDataset());
                } catch (EmfException e) {
                    showError("Error viewing Fast analysis output: " + e.getMessage());
                }
            }
        }
    }

    private void showDatasetDataViewer(EmfDataset dataset) throws EmfException {

        EmfConsole parentConsole = this.getParentConsole();
        DesktopManager desktopManager = this.getDesktopManager();
        EmfSession session = this.getSession();
        FastAnalysisPresenter presenter = this.getPresenter();

        Version[] versions = presenter.getVersions(dataset);
        if (versions.length == 1) {

            DataViewer dataViewerView = new DataViewer(dataset, parentConsole, desktopManager);
            DataViewPresenter dataViewPresenter = new DataViewPresenter(dataset, versions[0], getTableName(dataset),
                    dataViewerView, session);
            dataViewPresenter.display();
        } else {

            DatasetPropertiesViewer datasetPropertiesViewerView = new DatasetPropertiesViewer(session, parentConsole,
                    desktopManager);
            presenter.doDisplayPropertiesView(datasetPropertiesViewerView, dataset);
            datasetPropertiesViewerView.setDefaultTab(1);
        }
    }

    protected String getTableName(Dataset dataset) {

        InternalSource[] internalSources = dataset.getInternalSources();
        String tableName = "";
        if (internalSources.length > 0) {
            tableName = internalSources[0].getTable();
        }

        return tableName;
    }

    private Action getExportAction() {

        return new AbstractAction() {

            public void actionPerformed(ActionEvent e) {
                doExport();
            }
        };
    }

    private Action getExportToNetCDFAction() {

        return new AbstractAction() {

            public void actionPerformed(ActionEvent e) {
                doExportToNetCDF();
            }
        };
    }

    private void doExport() {

        List<FastAnalysisOutput> analysisOutputs = getSelected();
        if (analysisOutputs.isEmpty()) {
            this.showMessage(ROOT_SELECT_PROMPT + "export.");
        } else {

            List<FastOutputExportWrapper> wrappers = new ArrayList<FastOutputExportWrapper>();
            FastAnalysis analysis = this.getAnalysis();
            for (FastAnalysisOutput analysisOutput : analysisOutputs) {
                wrappers.add(new FastOutputExportWrapper(analysis, analysisOutput));
            }

            ExportWindow exportView = new ExportWindow(wrappers, this.getDesktopManager(), this.getParentConsole(),
                    this.getSession());
            getDesktopPane().add(exportView);

            ExportPresenter exportPresenter = new ExportPresenterImpl(this.getSession());

            try {
                this.getPresenter().doExport(exportView, exportPresenter, wrappers);
            } catch (EmfException e) {
                // NOTE Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private void doExportToNetCDF() {

        List<FastAnalysisOutput> analysisOutputs = getSelected();
        if (analysisOutputs.isEmpty()) {
            this.showMessage(ROOT_SELECT_PROMPT + "export.");
        } else {

            List<FastOutputExportWrapper> wrappers = new ArrayList<FastOutputExportWrapper>();
            FastAnalysis analysis = this.getAnalysis();
            for (FastAnalysisOutput analysisOutput : analysisOutputs) {
                wrappers.add(new FastOutputExportWrapper(analysis, analysisOutput));
            }

            ExportToNetCDFWindow exportView = new ExportToNetCDFWindow(wrappers, this.getDesktopManager(), this.getParentConsole(),
                    this.getSession());
            getDesktopPane().add(exportView);

            ExportPresenter exportPresenter = new ExportToNetCDFPresenterImpl(this.getSession());

            try {
                this.getPresenter().doExport(exportView, exportPresenter, wrappers);
            } catch (EmfException e) {
                // NOTE Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private FastAnalysisOutput[] getFastAnalysisOutputs() {

        FastAnalysisOutput[] analysisOutputs = new FastAnalysisOutput[0];
        try {
            analysisOutputs = this.getFastAnalysisOutputs(this.getAnalysis().getId());
        } catch (EmfException e) {
            // NOTE Auto-generated catch block
            e.printStackTrace();
        }

        return analysisOutputs;
    }

    protected FastAnalysisOutput[] getFastAnalysisOutputs(int id) throws EmfException {
        return this.getSession().fastService().getFastAnalysisOutputs(id);
    }

    private SortCriteria sortCriteria() {
        return new SortCriteria(new String[] { "Type" }, new boolean[] { false }, new boolean[] { true });
    }

    public void viewOnly() {
        //
    }

    private List<FastAnalysisOutput> getSelected() {
        return (List<FastAnalysisOutput>) table.selected();
    }
}
