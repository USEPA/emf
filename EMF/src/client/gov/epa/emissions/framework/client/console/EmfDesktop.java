package gov.epa.emissions.framework.client.console;

import javax.swing.JDesktopPane;

import gov.epa.emissions.framework.client.ManagedView;

public interface EmfDesktop {
    
    JDesktopPane getDesktopPane();
    
    void add(ManagedView view);

    void ensurePresence(ManagedView view);
}
