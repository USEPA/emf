package gov.epa.emissions.framework.client.data.datasettype;

import gov.epa.emissions.commons.data.QAStepTemplate;

public class ViewableQAStepTemplatePresenter {

    private ViewableQAStepTemplateView view;
    
    private QAStepTemplate template;
    
    public ViewableQAStepTemplatePresenter(ViewableQAStepTemplateView view, 
            QAStepTemplate template) {
        this.view = view;
        this.template = template;
    }
    
    public void display() {
        view.display(template);
    }

}
