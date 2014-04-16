package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.commons.security.User;

public class NoOpPostRegisterStrategy implements PostRegisterStrategy {

    public void execute(User user) {// No Op
    }

}
