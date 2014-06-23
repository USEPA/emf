package gov.epa.emissions.framework.client.qa;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.QAStepTemplate;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;

public class QAStepTemplatesPanelPresenter {

    private QAStepTemplatesPanelView view;

    private DatasetType type;

    private EmfSession session;

    public QAStepTemplatesPanelPresenter(EmfSession session, DatasetType type, QAStepTemplatesPanelView view) {
        this.session = session;
        this.type = type;
        this.view = view;
    }

    public void display() {
        view.observe(this);
    }

    public void doEdit(EditQAStepTemplateView view, QAStepTemplate template) throws EmfException {
        EditQAStepTemplatesPresenter presenter = new EditQAStepTemplatesPresenterImpl(view, this.view, session);
        presenter.display(type, session.qaService().getQAPrograms(), template);
    }

    public void doCopyQAStepTemplates(QAStepTemplate[] templates, int[] datasetTypeIds, boolean replace) throws EmfException {
        session.dataCommonsService().copyQAStepTemplates(session.user(), templates, datasetTypeIds, replace);
    }

    public DatasetType obtainLockedDatasetType(User user, DatasetType datasetType) throws EmfException {
        return session.dataCommonsService().obtainLockedDatasetType(user, datasetType);
    }

}
