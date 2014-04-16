package gov.epa.emissions.commons.gui;

public interface ChangeObserver {
    void signalChanges();
    
    void signalSaved();
}
