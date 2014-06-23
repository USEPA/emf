package gov.epa.emissions.framework.client.cost.controlmeasure.io;

import gov.epa.emissions.framework.client.ManagedView;

public interface CMImportView extends ManagedView {

    void register(CMImportPresenter presenter);

    void setDefaultBaseFolder(String folder);

    void setMessage(String message);
    
    boolean confirmToPurge( String msg); 
}
