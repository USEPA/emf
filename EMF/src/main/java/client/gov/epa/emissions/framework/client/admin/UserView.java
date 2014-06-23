package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.ManagedView;

public interface UserView extends ManagedView {

    void display(User user);

    void observe(ViewUserPresenter presenter);
}
