package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.data.Dataset;
import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.data.QAStepResult;

public interface EditableQATabView {

    void display(Dataset dataset, QAStep[] steps, QAStepResult[] qaStepResults, Version[] versions);
    
    void doRefresh(Dataset dataset, QAStep[] steps, QAStepResult[] qaStepResults, Version[] versions);

    void observe(EditableQATabPresenter presenter);

    void addFromTemplate(QAStep[] steps);

    void addCustomQAStep(QAStep step);

    QAStep[] steps();

    void informLackOfTemplatesForAddingNewSteps(DatasetType type);

    void refresh();
    
    void refresh(QAStep step, QAStepResult result);

    void displayResultsTable(String name, String absolutePath);

}
