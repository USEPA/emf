package gov.epa.emissions.framework.client.sms.sectorscenario.editor;

import gov.epa.emissions.commons.data.Project;
import gov.epa.emissions.framework.services.EmfException;



public interface EditSectorScenarioSummaryTabPresenter extends EditSectorScenarioTabPresenter {
  //void setResults(SectorScenario sectorScenario);
    Project[] getProjects() throws EmfException ;
}
