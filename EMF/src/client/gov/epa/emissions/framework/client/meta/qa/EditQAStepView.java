package gov.epa.emissions.framework.client.meta.qa;

import gov.epa.emissions.commons.data.QAProgram;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.client.ManagedView;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;
import gov.epa.emissions.framework.services.data.QAStep;
import gov.epa.emissions.framework.services.data.QAStepResult;

public interface EditQAStepView extends ManagedView {

    void display(QAStep step, QAStepResult qaStepResult, QAProgram[] programs, EmfDataset dataset, 
            String versionName, boolean sameAstemplate, EmfSession session);

    void observe(EditQAStepPresenter presenter);
    
    QAStep save() throws EmfException;
    
    void setMostRecentUsedFolder(String mostRecentUsedFolder);

    void displayResultsTable(String qaStepName, String exportedFileName) throws EmfException;
    
    void updateArgumentsTextArea (String text);
    
    void updateInventories(Object [] inventories, Object [] invTables, String summaryType);
    
    void updateInventories(Object [] inventories, Object [] invTables, String summaryType, String emissionType);
    
    void updateInventories(Object [] invBase, Object [] invControl, Object [] invTables, String summaryType);
    
    void updateInventories(Object [] inventories);

    void updateDatasets(Object capInventory, 
            Object hapInventory, 
            Object speciationToolSpecieInfoDataset, 
            Object pollToPollConversionDataset, 
            Object[] speciationProfileWeightDatasets, 
            Object[] speciationCrossReferenceDatasets, 
            String filter,
            String summaryType);

    void updateArguments(Object temporalProfile, 
            Object[] smkRpts,
            Object year);

    void updateCompareAnnualStateSummariesArguments(Object[] inventories, 
            Object[] smkRpts,
            Object invTable,
            Object tolerance, Object coStCy);

    void updateCompareAnnualStateSummariesArguments(Object[] smkRpts, Object coStCy, Object[] polls, Object[] species,
            Object[] exclPollutants);

    void updateECControlScenarioArguments(Object inventory, Object detailedResult, Object[] gsrefs, Object[] gspros);

    void updateProgramArguments(String programArguments);
}
