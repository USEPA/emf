package gov.epa.emissions.framework.client.qa;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.QAProgram;
import gov.epa.emissions.commons.data.QAStepTemplate;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;

public interface EditQAStepTemplateView {

    void display(DatasetType type, QAProgram[] programs, QAStepTemplate template, EmfSession session);

    void observe(EditQAStepTemplatesPresenterImpl presenter);

    void loadTemplate() throws EmfException;
}
