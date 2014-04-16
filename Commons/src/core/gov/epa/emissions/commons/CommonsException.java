package gov.epa.emissions.commons;

@SuppressWarnings("serial")
public class CommonsException extends Exception {

    String details;

    public CommonsException(String message, String details, Throwable cause) {
        super(message, cause);
        this.details = details;
    }

    public CommonsException(String message, String details) {
        super(message);
        this.details = details;
    }

    public CommonsException(String message) {
        super(message);
    }
}
