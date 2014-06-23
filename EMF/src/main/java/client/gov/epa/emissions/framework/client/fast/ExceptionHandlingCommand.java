package gov.epa.emissions.framework.client.fast;

import gov.epa.emissions.framework.services.EmfException;

public interface ExceptionHandlingCommand extends Command {

    void handleException(EmfException e) throws EmfException;
}
