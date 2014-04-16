package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.data.QAProgram;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.ManagedView;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.data.QAStepResult;

public interface QAStepView extends ManagedView {

    void display(QAStep step, QAStepResult qaStepResult, QAProgram[] programs, EmfDataset dataset, 
            User user, String versionName, boolean sameAstemplate);

    void observe(ViewQAStepPresenter presenter);
    
    void setMostRecentUsedFolder(String mostRecentUsedFolder);

    void displayResultsTable(String name, String absolutePath);

}
