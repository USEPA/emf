package gov.epa.emissions.commons.gui;

public interface Changeable {
    boolean hasChanges();
    
    void observe(Changeables list);
    
    void clear();
}
