package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.ManagedView;
import gov.epa.emissions.framework.services.EmfException;

public interface UpdatableUserView extends ManagedView {

    void observe(UpdateUserPresenter presenter);

    /**
     * Close the window, if user confirms that he/she would'nt care of losing
     * any user edits (i.e. data changes)
     */
    void closeOnConfirmLosingChanges();

    void display(User user) throws EmfException;

}
