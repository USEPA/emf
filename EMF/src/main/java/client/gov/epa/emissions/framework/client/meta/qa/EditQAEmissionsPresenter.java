package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;

public class EditQAEmissionsPresenter {
    
    private EditQAEmissionsView view;
    
    private EditQAStepView editQAStepView;
    
    private EmfSession session;
        
    public EditQAEmissionsPresenter(EditQAEmissionsView view, EditQAStepView view2, EmfSession session) {
        this.view = view;
        this.editQAStepView = view2;
        this.session = session;
    }

    public void display(EmfDataset dataset, QAStep qaStep) {
        view.observe(this);
        view.display(dataset, qaStep);
    }
    
    public void updateInventories(Object [] inventories, Object [] invTables, String summaryType) {
        editQAStepView.updateInventories(inventories, invTables, summaryType);
    }
    
    public void updateInventories(Object [] inventories, Object [] invTables, String summaryType, String emissionType) {
        editQAStepView.updateInventories(inventories, invTables, summaryType, emissionType);
    }
    
    public void updateInventories(Object [] invBase, Object [] invControl, Object [] invTables, String summaryType) {
        editQAStepView.updateInventories(invBase, invControl, invTables, summaryType);
    }

    public void updateDatasets(Object capInventory, 
            Object hapInventory, 
            Object speciationToolSpecieInfoDataset, 
            Object pollToPollConversionDataset, 
            Object[] speciationProfileWeightDatasets, 
            Object[] speciationCrossReferenceDatasets, 
            String filter,
            String summaryType) {
        editQAStepView.updateDatasets(capInventory, 
                hapInventory, 
                speciationToolSpecieInfoDataset, 
                pollToPollConversionDataset, 
                speciationProfileWeightDatasets, 
                speciationCrossReferenceDatasets, 
                filter,
                summaryType);
    }

    public DatasetType getDatasetType(String name) throws EmfException {
        return session.getLightDatasetType(name);
    }

    public void updateDatasets(Object temporalProfile, Object[] smkRpts, Object year) {
        editQAStepView.updateArguments(temporalProfile, 
                smkRpts,
                year);
    }

    public void updateCompareAnnualStateSummariesDatasets(Object[] inventories, Object[] smkRpts, Object invTable, Object tolerance, Object coStCy) {
        editQAStepView.updateCompareAnnualStateSummariesArguments(inventories, 
                smkRpts,
                invTable,
                tolerance,
                coStCy);
    }

    public void updateECControlScenarioDatasets(Object inventory, Object detailedResult, 
            Object[] gsrefDatasets, Object[] gsproDatasets) {
        editQAStepView.updateECControlScenarioArguments(inventory, detailedResult, 
                gsrefDatasets, gsproDatasets);
    }
    
    public Version getVersion(int datasetId, int version) throws EmfException {
        try {
            Version[] versions = session.dataEditorService().getVersions(datasetId);
            for (Version v : versions) {
                if (v.getVersion() == version)
                    return v;
            }
            return null;
        } finally {
            //
        }
    }

    public void updateProgramArguments(String programArguments) {
        editQAStepView.updateProgramArguments(programArguments);
    }

}
