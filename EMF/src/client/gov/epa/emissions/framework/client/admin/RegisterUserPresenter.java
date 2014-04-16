package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.UserService;

public class RegisterUserPresenter {

    private UserService model;

    private RegisterUserView view;

    public RegisterUserPresenter(UserService model) {
        this.model = model;
    }

    // auto-login, upon registration
    public User doRegister(User user, Boolean isNewUser) throws EmfException {
        if ( isNewUser ){
            user = model.createUser(user);// create and update with saved object(so that id is updated on the client side)
            model.authenticate(user.getUsername(), user.getEncryptedPassword());
        }
        else {
            model.obtainLocked(user, user);
            model.checkDuplicatesByEmail(user);
            model.updateUser(user);
        }
        return user;
    }

    public void doCancel() {
        view.disposeView();
    }

    public void display(RegisterUserView view) {
        this.view = view;
        this.view.observe(this);

        view.display();
    }

}
