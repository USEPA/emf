package gov.epa.emissions.framework.client.console;

import gov.epa.emissions.framework.services.EmfException;

public class EmfConsolePresenter {

    private EmfConsoleView view;

    public void display(EmfConsoleView view) {
        this.view = view;
        view.observe(this);

        view.display();
    }

    public void notifyManageUsers() throws EmfException {
        view.displayUserManager();
    }

}
