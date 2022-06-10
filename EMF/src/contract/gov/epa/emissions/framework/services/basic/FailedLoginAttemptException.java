package gov.epa.emissions.framework.services.basic;

import gov.epa.emissions.framework.services.EmfException;

public class FailedLoginAttemptException extends EmfException {

    public FailedLoginAttemptException(String message) {
        super(message);
    }

}
