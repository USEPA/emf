package gov.epa.emissions.framework.client.meta;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.console.DesktopManager;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.exim.ExportPresenter;
import gov.epa.emissions.framework.client.exim.ExportWindow;
import gov.epa.emissions.framework.client.meta.info.InfoTabPresenter;
import gov.epa.emissions.framework.client.meta.info.InfoTabView;
import gov.epa.emissions.framework.client.meta.keywords.KeywordsTabPresenter;
import gov.epa.emissions.framework.client.meta.keywords.KeywordsTabView;
import gov.epa.emissions.framework.client.meta.logs.LogsTabPresenter;
import gov.epa.emissions.framework.client.meta.logs.LogsTabView;
import gov.epa.emissions.framework.client.meta.notes.NotesTabPresenter;
import gov.epa.emissions.framework.client.meta.notes.NotesTabView;
import gov.epa.emissions.framework.client.meta.qa.QATabView;
import gov.epa.emissions.framework.client.meta.qa.ViewQATabPresenter;
import gov.epa.emissions.framework.client.meta.qa.ViewQATabPresenterImpl;
import gov.epa.emissions.framework.client.meta.revisions.RevisionsTabPresenter;
import gov.epa.emissions.framework.client.meta.revisions.RevisionsTabView;
import gov.epa.emissions.framework.client.meta.summary.SummaryTabPresenter;
import gov.epa.emissions.framework.client.meta.summary.SummaryTabView;
import gov.epa.emissions.framework.client.meta.versions.VersionedDataPresenter;
import gov.epa.emissions.framework.client.meta.versions.VersionedDataWindow;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;

public class PropertiesViewPresenter {

    private EmfDataset dataset;

    private PropertiesView view;

    private EmfSession session;

    public PropertiesViewPresenter(EmfDataset dataset, EmfSession session) {
        this.dataset = dataset;
        this.session = session;
    }

    public void doDisplay(PropertiesView view) throws EmfException {
        this.view = view;
        view.observe(this);
        Version version = session.eximService().getVersion(dataset, dataset.getDefaultVersion());
        view.display(dataset, version);
    }

    public void doClose() {
        view.disposeView();
    }
    
//    private Version version(int datasetId, int version) {
//        Session session = sessionFactory.getSession();
//        try {
//            Versions versions = new Versions();
//            return versions.get(datasetId, version, session);
//        } finally {
//            session.close();
//        }
//    }
    public void set(SummaryTabView summary) {
        SummaryTabPresenter summaryPresenter = new SummaryTabPresenter(summary, dataset, session);
        summaryPresenter.display();
    }

    public void set(KeywordsTabView keywordsView) {
        KeywordsTabPresenter keywordsPresenter = new KeywordsTabPresenter(keywordsView, dataset, session);
        keywordsPresenter.display();
    }

    public void set(InfoTabView view) {
        new InfoTabPresenter(view, dataset, session);
    }

    public void set(DataTabView view) {
        DataTabPresenter presenter = new DataTabPresenter(view, dataset, session);
        presenter.doDisplay();
    }

    public void set(LogsTabView view) throws EmfException {
        LogsTabPresenter presenter = new LogsTabPresenter(view, dataset, session);
        presenter.display();
    }

    public void set(NotesTabView view) throws EmfException {
        NotesTabPresenter presenter = new NotesTabPresenter(dataset, session.dataCommonsService());
        presenter.display(view);
    }

    public void set(RevisionsTabView view) throws EmfException {
        RevisionsTabPresenter presenter = new RevisionsTabPresenter(dataset, session);
        presenter.display(view);
    }

    public void set(QATabView view) throws EmfException {
        set(new ViewQATabPresenterImpl(view, dataset, session));
    }

    void set(ViewQATabPresenter presenter) throws EmfException {
        presenter.display();
    }

    public void doDisplayPropertiesEditor(EmfConsole parentConsole, DesktopManager desktopManager)
            throws EmfException {
        DatasetPropertiesEditor propertiesEditorView = new DatasetPropertiesEditor(session, parentConsole, desktopManager);
        PropertiesEditorPresenter presenter = new PropertiesEditorPresenterImpl(dataset, propertiesEditorView, session);
        presenter.doDisplay();
    }
    
    public void doDisplayVersionedData(EmfConsole parentConsole, DesktopManager desktopManager) {
        VersionedDataWindow versionsView = new VersionedDataWindow(parentConsole, desktopManager);
        VersionedDataPresenter presenter = new VersionedDataPresenter(dataset, session);
        presenter.display(versionsView);
    }

    public void doExport(ExportWindow exportView, ExportPresenter presenter) {
        view.clearMessage();
        presenter.display(exportView);
        
    }

}
