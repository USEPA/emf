package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;

public interface PostRegisterStrategy {
    void execute(User user) throws EmfException;
}
