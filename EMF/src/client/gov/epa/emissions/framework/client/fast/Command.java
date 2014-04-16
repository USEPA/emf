package gov.epa.emissions.framework.client.fast;

import gov.epa.emissions.framework.services.EmfException;

public interface Command {

    void execute() throws EmfException;

    void postExecute() throws EmfException;
}
