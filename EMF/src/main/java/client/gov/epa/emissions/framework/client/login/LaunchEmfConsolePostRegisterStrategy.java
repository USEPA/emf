package gov.epa.emissions.framework.client.login;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.DefaultEmfSession;
import gov.epa.emissions.framework.client.admin.PostRegisterStrategy;
import gov.epa.emissions.framework.client.console.EmfConsole;
import gov.epa.emissions.framework.client.console.EmfConsolePresenter;
import gov.epa.emissions.framework.client.transport.ServiceLocator;
import gov.epa.emissions.framework.services.EmfException;

public class LaunchEmfConsolePostRegisterStrategy implements PostRegisterStrategy {

    private ServiceLocator serviceLocator;

    public LaunchEmfConsolePostRegisterStrategy(ServiceLocator serviceLocator) {
        this.serviceLocator = serviceLocator;
    }

    public void execute(User user) throws EmfException {
        EmfConsole console = new EmfConsole(new DefaultEmfSession(user, serviceLocator));

        EmfConsolePresenter presenter = new EmfConsolePresenter();
        presenter.display(console);
    }

}
