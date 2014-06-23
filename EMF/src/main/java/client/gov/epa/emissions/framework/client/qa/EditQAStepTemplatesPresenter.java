package gov.epa.emissions.framework.client.qa;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.QAProgram;
import gov.epa.emissions.commons.data.QAStepTemplate;
import gov.epa.emissions.framework.services.EmfException;

public interface EditQAStepTemplatesPresenter {

    void display(DatasetType type, QAProgram[] programs, QAStepTemplate template);

    void doEdit() throws EmfException;

    void doCopyQAStepTemplates(QAStepTemplate[] templates, int[] datasetTypeIds, boolean replace) throws EmfException;
}
