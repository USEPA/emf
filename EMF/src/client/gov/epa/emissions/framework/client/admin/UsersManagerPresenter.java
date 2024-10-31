package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.commons.util.CustomDateFormat;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.UserService;
import gov.epa.emissions.framework.ui.RefreshObserver;

public class UsersManagerPresenter implements RefreshObserver {

    private UsersManagerView view;

    private UserService service;

    private EmfSession session;

    public UsersManagerPresenter(EmfSession session, UserService service) {
        this.session = session;
        this.service = service;
    }

    public void doClose() {
        view.disposeView();
    }

    public void display(UsersManagerView view) throws EmfException {
        this.view = view;
        view.observe(this);

        view.display();
        view.populate();
    }

    public void doDelete(User[] users) throws EmfException {
        for (int i = 0; i < users.length; i++)
            doDelete(users[i]);

        view.refresh(service.getUsers());
    }

    private void doDelete(User userToDelete) throws EmfException {
        // NOTE: super user's name is fixed
        if (userToDelete.getUsername().equals("admin"))
            throw new EmfException("Cannot delete EMF super user - '" + userToDelete.getUsername() + "'");

        User loggedIn = session.user();
        if (loggedIn.getUsername().equals(userToDelete.getUsername()))
            throw new EmfException("Cannot delete yourself - '" + userToDelete.getUsername() + "'");

        delete(userToDelete);
    }

    private void delete(User userToDelete) throws EmfException {
        userToDelete = service.obtainLocked(session.user(), userToDelete);
        if (!userToDelete.isLocked(session.user())) {// locked by another user
            String message = "Locked by " + userToDelete.getLockOwner() + " at "
                    + CustomDateFormat.format_YYYY_MM_DD_HH_MM(userToDelete.getLockDate());
            throw new EmfException(message);
        }

        service.deleteUser(userToDelete);
    }

    public void doRegisterNewUser(RegisterUserDesktopView registerUserView) {
        RegisterUserPresenter registerPresenter = new RegisterUserPresenter(service);
        registerPresenter.display(registerUserView);

        view.refresh();
    }

    public void doUpdateUser(User updateUser, UpdatableUserView updatable, UserView viewable) throws EmfException {
        UpdateUserPresenter updatePresenter = new UpdateUserPresenterImpl(session, updateUser, service);
        updateUser(updatable, viewable, updatePresenter);
    }

    void updateUser(UpdatableUserView updatable, UserView viewable, UpdateUserPresenter updatePresenter)
            throws EmfException {
        showUpdateUser(updatable, viewable, updatePresenter);
    }

    private void showUpdateUser(UpdatableUserView updatable, UserView viewable, UpdateUserPresenter updatePresenter)
            throws EmfException {
        updatePresenter.display(updatable, viewable);
        view.refresh();
    }

    public void doRefresh() throws EmfException {
        view.refresh(service.getUsers());
    }

    public void doUpdateUsers(User[] users) throws EmfException {
        for (int i = 0; i < users.length; i++) {
            UpdatableUserView updatable = view.getUpdateUserView(users[i]);
            UserView viewable = view.getUserView();
            doUpdateUser(service.getUser(users[i].getUsername()), updatable, viewable);
        }
    }
    
    public User[] getUsers() throws EmfException {
        return service.getUsers();
    }

    public void doLogout(User[] users) throws EmfException {
        for (int i = 0; i < users.length; i++)
            doLogout(users[i]);

        view.refresh(service.getUsers());
    }

    private void doLogout(User user) throws EmfException {
        User loggedIn = session.user();
        if (loggedIn.getUsername().equals(user.getUsername()))
            throw new EmfException("Cannot logout yourself - '" + user.getUsername() + "'");

        logout(user);
    }

    private void logout(User user) throws EmfException {
        user = service.obtainLocked(session.user(), user);
        if (!user.isLocked(session.user())) {// locked by another user
            String message = "Locked by " + user.getLockOwner() + " at "
                    + CustomDateFormat.format_YYYY_MM_DD_HH_MM(user.getLockDate());
            throw new EmfException(message);
        }
        user.setLoggedIn(false);
        service.updateUser(user);
    }

    public void doEnable(User[] users) throws EmfException {
        for (int i = 0; i < users.length; i++)
            doEnableDisable(users[i], true);

        view.refresh(service.getUsers());
    }

    public void doDisable(User[] users) throws EmfException {
        for (int i = 0; i < users.length; i++)
            doEnableDisable(users[i], false);

        view.refresh(service.getUsers());
    }

    private void doEnableDisable(User user, boolean enable) throws EmfException {
        User loggedIn = session.user();
        if (loggedIn.getUsername().equals(user.getUsername()))
            throw new EmfException("Cannot logout yourself - '" + user.getUsername() + "'");

        enableDisable(user, enable);
    }

    private void doDisable(User user) throws EmfException {
        User loggedIn = session.user();
        if (loggedIn.getUsername().equals(user.getUsername()))
            throw new EmfException("Cannot logout yourself - '" + user.getUsername() + "'");

        enableDisable(user, false);
    }

    private void enableDisable(User user, boolean enable) throws EmfException {
        user = service.obtainLocked(session.user(), user);
        if (!user.isLocked(session.user())) {// locked by another user
            String message = "Locked by " + user.getLockOwner() + " at "
                    + CustomDateFormat.format_YYYY_MM_DD_HH_MM(user.getLockDate());
            throw new EmfException(message);
        }
        user.setAccountDisabled(!enable);
        service.updateUser(user);
    }
}
