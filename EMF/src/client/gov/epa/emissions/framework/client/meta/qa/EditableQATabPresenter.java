package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.meta.PropertiesEditorTabPresenter;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.data.QAStepResult;

public interface EditableQATabPresenter extends PropertiesEditorTabPresenter {
    void display() throws EmfException;

    void doAddUsingTemplate(NewQAStepView stepview);

    void doSetStatus(SetQAStatusView statusview, QAStep[] steps);
    
    void runStatus(QAStep step) throws EmfException;
    
    EmfSession getSession();

    void doAddCustomized(NewCustomQAStepView view) throws EmfException;

    void doEdit(QAStep step, EditQAStepView performView, String versionName) throws EmfException;

    QAStep[] addFromTemplates(QAStep[] newSteps) throws EmfException;

    public void doCopyQASteps(QAStep[] steps, int[] datasetIds, boolean replace) throws EmfException;

    QAStepResult getStepResult(QAStep step) throws EmfException;
    
    long getTableRecordCount(QAStepResult stepResult) throws EmfException;

    void viewResults(QAStep qaStep, long viewCount) throws EmfException;

    void doDelete(QAStep[] array) throws EmfException; //BUG3615
    
    boolean checkBizzareCharInColumn(QAStep step, String colName) throws EmfException; // BUG3588
}