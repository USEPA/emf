package gov.epa.emissions.framework.client.data.region;

import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.data.GeoRegion;

public interface RegionEditorView {

    void display() throws EmfException;

    void observe(RegionEditorPresenter presenter);
    
    void setRegion(GeoRegion region, boolean isNew) throws EmfException;

}