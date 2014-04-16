package gov.epa.emissions.commons.security;

public class UserException extends RuntimeException {

    String details;

    public UserException(String description, String details, Throwable cause) {
        super(description, cause);
        this.details = details;
    }

    public UserException(String message) {
        super(message);
    }

}
