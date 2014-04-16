package gov.epa.emissions.framework.client.meta.info;

import gov.epa.emissions.commons.data.InternalSource;
import gov.epa.emissions.framework.services.EmfException;

public interface InfoTabView {
    void displayInternalSources(InternalSource[] sources) throws EmfException ;

    void displayExternalSources(int numOfSrcs) throws EmfException;
    
    void observe(InfoTabPresenter presenter);
    
    String getNameFilter();
    
    int getSourceSize();

}
