package gov.epa.emissions.framework.client.qa;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.QAProgram;
import gov.epa.emissions.commons.data.QAStepTemplate;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;

public class EditQAStepTemplatesPresenterImpl implements EditQAStepTemplatesPresenter {

    private EditQAStepTemplateView view;
    
    private QAStepTemplatesPanelView parentView;
    
    private EmfSession session;

    public EditQAStepTemplatesPresenterImpl(EditQAStepTemplateView view, 
            QAStepTemplatesPanelView parentView, EmfSession session) {
        this.session = session;
        this.view = view;
        this.parentView = parentView;
    }
    
    public void display(DatasetType type,QAProgram[] programs, QAStepTemplate template) {
        view.observe(this);
        view.display(type,programs, template, session);
    }
    
    public void doEdit() throws EmfException {
        view.loadTemplate();
        parentView.refresh();
    }

    public void doCopyQAStepTemplates(QAStepTemplate[] templates, int[] datasetTypeIds, boolean replace) throws EmfException {
        //view.loadTemplate();
        session.dataCommonsService().copyQAStepTemplates(session.user(), templates, datasetTypeIds, replace);
        
        parentView.refresh();
    }

}
