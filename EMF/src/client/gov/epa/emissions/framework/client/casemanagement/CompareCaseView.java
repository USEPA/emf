package gov.epa.emissions.framework.client.casemanagement;

import gov.epa.emissions.commons.data.Sector;
import gov.epa.emissions.framework.client.ManagedView;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.casemanagement.Case;
import gov.epa.emissions.framework.services.data.GeoRegion;

public interface CompareCaseView extends ManagedView{
    
    void addSector(Sector sector); 
    
    void addRegion(GeoRegion region);  
    
    void observe(CompareCasePresenter presenter);

    void display();
}
