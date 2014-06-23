package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.framework.services.EmfException;

public interface ViewUserPresenter {

    void display(UserView view) throws EmfException;

    void doClose() throws EmfException;

}