package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.EmfView;

public interface UsersManagerView extends EmfView {

    void observe(UsersManagerPresenter presenter);

    void refresh();

    UpdatableUserView getUpdateUserView(User user);

    UserView getUserView();

    void refresh(User[] users);

    void display();
    
    void populate();
    
    
}
