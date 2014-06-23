package gov.epa.emissions.framework.services;

import java.rmi.RemoteException;

public class EmfException extends RemoteException {

    String details;
    
    String type;
    
    public final static String MSG_TYPE = "Message";
    
    public final static String ERR_TYPE = "Error";

    public EmfException() {//
    }

    public EmfException(String message, Exception e) {
        super(message, e);
    }
    
    public EmfException(String message) {
        super(message);
        this.details = message;
        
        if (message != null)
            message = message.trim();
        
        if (message.startsWith(MSG_TYPE))
            type = MSG_TYPE;
        else
            type = ERR_TYPE;
    }
    
    public EmfException(String message, String type) {
        super(message);
        this.details = message;
        this.type = type;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getDetails() {
        return details;
    }
    
    public String getType() {
        return type;
    }
    
    public boolean isMessage() {
        return type.equals(MSG_TYPE);
    }
    
    public boolean isError() {
        return type.equals(ERR_TYPE);
    }

}
