package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;

public class EditQAAnnualStateSummariesCrosstabEmissionsPresenter {
    
    private QAAnnualStateSummariesCrosstabWindow view;
    
    private EditQAStepView editQAStepView;
    
    private EmfSession session;
        
    public EditQAAnnualStateSummariesCrosstabEmissionsPresenter(QAAnnualStateSummariesCrosstabWindow view, EditQAStepView view2, EmfSession session) {
        this.view = view;
        this.editQAStepView = view2;
        this.session = session;
    }

    public void display(EmfDataset dataset, QAStep qaStep) throws EmfException {
        view.observe(this);
        view.display(dataset, qaStep);
    }
    
    public DatasetType getDatasetType(String name) throws EmfException {
        return session.getLightDatasetType(name);
    }

    public void updateAnnualStateSummariesDatasets(Object[] smkRpts, Object coStCy, Object[] polls,
            Object[] species, Object[] exclPollutants) {
        editQAStepView.updateCompareAnnualStateSummariesArguments(smkRpts,
                coStCy,
                polls, 
                species, 
                exclPollutants);
    }
    
    public String[] getTableColumnDistinctValues(int datasetId, int datasetVersion, String columnName, String whereFilter, String sortOrder) throws EmfException {
        return session.dataService().getTableColumnDistinctValues(datasetId, datasetVersion, columnName, whereFilter, sortOrder);
    }
    
//    public void refreshPollList(String[] polls) {
//        view.refreshMasterPollList(polls);
//    }
}
