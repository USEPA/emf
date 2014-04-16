package gov.epa.emissions.framework.ui;

import gov.epa.emissions.framework.client.ManagedView;

public interface Layout {
    
    void add(ManagedView managedView);

    void remove(ManagedView managedView);

}
