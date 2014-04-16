package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.commons.security.User;

public class ViewUserPresenterImpl implements ViewUserPresenter {

    private UserView view;

    private User user;

    public ViewUserPresenterImpl(User user) {
        this.user = user;
    }

    public void display(UserView view) {
        this.view = view;
        view.observe(this);
        view.display(user);
    }

    public void doClose() {
        view.disposeView();
    }

}
