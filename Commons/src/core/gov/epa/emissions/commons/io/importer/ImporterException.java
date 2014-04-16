package gov.epa.emissions.commons.io.importer;

public class ImporterException extends Exception {

    public ImporterException(String message, Throwable cause) {
        super(message, cause);
    }

    public ImporterException(String message) {
        super(message);
    }

}
