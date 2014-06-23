package gov.epa.emissions.framework.client.sms.sectorscenario.editor;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.db.version.Version;
import gov.epa.emissions.framework.client.meta.PropertiesView;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.EmfDataset;



public interface EditSectorScenarioInputsTabPresenter extends EditSectorScenarioTabPresenter {
    
    void doDisplayPropertiesView(PropertiesView propertiesView, EmfDataset dataset) throws EmfException ;
  
    EmfDataset[] getDatasets(DatasetType datasetType) throws EmfException;
    Version[] getVersions(EmfDataset dataset) throws EmfException;
    
    DatasetType getDatasetType(String name) throws EmfException;
    
}
