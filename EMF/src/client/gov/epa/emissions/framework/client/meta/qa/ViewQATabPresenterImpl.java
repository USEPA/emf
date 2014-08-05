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
    
    private QAStep[] steps;

    private QAStepResult[] qaStepResults;

    public ViewQATabPresenterImpl(QATabView view, EmfDataset dataset, EmfSession session) {
        this.view = view;
        this.dataset = dataset;
        this.session = session;
        view.observe(this);
    }

    public void display() throws EmfException {
        //view.observe(this);
        this.qaStepResults = session.qaService().getQAStepResults(dataset);
        this.steps = session.qaService().getQASteps(dataset);
        view.display(steps, qaStepResults, session);
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

    @Override
    public Object[] saveProcessData() throws EmfException {
        // NOTE Auto-generated method stub
        return null;
    }

    @Override
    public void saveDisplay(Object[] objs) throws EmfException {
        // NOTE Auto-generated method stub
        
    }

    @Override
    public Object[] swProcessData() throws EmfException {
        // NOTE Auto-generated method stub
        return null;
    }

    @Override
    public void swDisplay(Object[] objs) throws EmfException {
        // NOTE Auto-generated method stub
        
    }

    @Override
    public Object[] refreshProcessData() throws EmfException {
        this.qaStepResults = session.qaService().getQAStepResults(dataset);
        this.steps = session.qaService().getQASteps(dataset);
        return null;
    }

    @Override
    public void refreshDisplay(Object[] objs) throws EmfException {
        view.doRefresh(steps, qaStepResults);
    }

}
