package gov.epa.emissions.framework.client.casemanagement.editor;

import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.data.GeoRegion;

public interface EditableCaseSummaryTabView {
    // update with the view contents
    void save(Case caseObj) throws EmfException;
    
    void addSector(Sector sector); 
    
    void addRegion(GeoRegion region);
    
    void updateDescriptionTextArea(String descText); // BUG3621

}
