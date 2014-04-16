package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.meta.versions.VersionsSet;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.data.QAStepResult;
import gov.epa.emissions.framework.services.editor.DataEditorService;

public class ViewQATabPresenterImpl implements ViewQATabPresenter {

    private QATabView view;

    private EmfSession session;

    private EmfDataset dataset;

    public ViewQATabPresenterImpl(QATabView view, EmfDataset dataset, EmfSession session) {
        this.view = view;
        this.dataset = dataset;
        this.session = session;
        view.observe(this);
    }

    public void display() throws EmfException {
        //view.observe(this);
        QAStepResult[] results = session.qaService().getQAStepResults(dataset);
        view.display(session.qaService().getQASteps(dataset), results, session);
    }

    public void doView(QAStep step, QAStepView view) throws EmfException {
        ViewQAStepPresenter presenter = new ViewQAStepPresenter(view, dataset, session);
        view(step, view, presenter);
    }

    void view(QAStep step, QAStepView view, ViewQAStepPresenter presenter) throws EmfException {
        view.observe(presenter);
        VersionsSet versions = new VersionsSet(getVersions());
        presenter.display(step, versions.name(step.getVersion()));
    }

    public Version[] getVersions() throws EmfException {
        DataEditorService service = session.dataEditorService();
        return service.getVersions(dataset.getId());
    }

}
