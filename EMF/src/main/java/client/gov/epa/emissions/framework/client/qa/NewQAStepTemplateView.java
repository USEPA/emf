package gov.epa.emissions.framework.client.qa;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.QAProgram;
import gov.epa.emissions.commons.data.QAStepTemplate;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;

public interface NewQAStepTemplateView {
    
    void display(EmfSession session, DatasetType type, QAProgram[] programs);

    QAStepTemplate template() throws EmfException;

    void observe(NewQAStepTemplatePresenter presenter);

}
