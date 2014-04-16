package gov.epa.emissions.framework.client.cost.controlprogram.editor;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.cost.ControlProgramType;
import gov.epa.emissions.framework.services.data.EmfDataset;

public interface ControlProgramPresenter {

    void doDisplay() throws EmfException;

    void doDisplayNew();

    void doClose() throws EmfException;

    void doSave() throws EmfException;

    void doAdd() throws EmfException;

    void set(ControlProgramMeasuresTab view) throws EmfException;

    void set(ControlProgramSummaryTab view) throws EmfException;

    void set(ControlProgramTechnologiesTab view) throws EmfException;

//    void doLoad(String tabTitle) throws EmfException;
    
    void fireTracking();
    
    DatasetType getDatasetType(String name) throws EmfException;
    
    Version[] getVersions(EmfDataset dataset) throws EmfException; 

    EmfDataset[] getDatasets(DatasetType type) throws EmfException;

    EmfDataset getDataset(int id) throws EmfException;

    boolean hasResults();
    
    void doChangeControlProgramType(ControlProgramType controlProgramType);
    
}
