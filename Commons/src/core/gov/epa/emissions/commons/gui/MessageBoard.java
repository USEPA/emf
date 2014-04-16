package gov.epa.emissions.commons.gui;


public interface MessageBoard {
    
    void clear();
    
    void setError(String error);

    void setMessage(String message);
}
