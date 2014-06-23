package gov.epa.emissions.framework.client.qa;

import gov.epa.emissions.commons.data.QAStepTemplate;

public interface QAStepTemplatesPanelView {
    void observe(QAStepTemplatesPanelPresenter presenter);
    
    void add(QAStepTemplate template);
    
    void refresh();
}
