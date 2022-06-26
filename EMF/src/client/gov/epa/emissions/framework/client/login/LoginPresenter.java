package gov.epa.emissions.framework.client.login;

import gov.epa.emissions.commons.CommonsException;
import gov.epa.emissions.commons.security.PasswordGenerator;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.preference.DefaultUserPreferences;
import gov.epa.emissions.framework.client.preference.UserPreference;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.AuthenticationException;
import gov.epa.emissions.framework.services.basic.FailedLoginAttemptException;
import gov.epa.emissions.framework.services.basic.UserService;

import java.util.Date;

public class LoginPresenter {

    private UserService userAdmin;

    private LoginView view;

    private String update;

    private UserPreference preferences;

    public LoginPresenter(UserService model) {
        this.userAdmin = model;
    }

    public User doLogin(String username, String password) throws EmfException {
        try {
            userAdmin.authenticate(username, new PasswordGenerator().encrypt(password));
        } catch (EmfException e) {
            User user = userAdmin.getUser(username);
            if (user == null) {
                throw e;
            }

            user = userAdmin.obtainLocked(userAdmin.getUser(username), userAdmin.getUser(username));

            if (user == null)
                throw new EmfException("Unable to fetch lock on user: " + username + ".");

            user.setLoggedIn(false);
            user.setLastLoginDate(new Date());
            int failedLoginAttempts = user.getFailedLoginAttempts();
            final int maxFailedLoginAttempts = 5;
            ++failedLoginAttempts;
            if (failedLoginAttempts >= maxFailedLoginAttempts) {//allow only 5 login tries...
                user.setAccountDisabled(true);
            }
            user.setFailedLoginAttempts(failedLoginAttempts);
            userAdmin.updateUser(user);
            if (failedLoginAttempts >= maxFailedLoginAttempts) {//allow only maxFailedLoginAttempts login tries...
                throw new FailedLoginAttemptException("Your account is disabled. Contact an administrator to re-enable your account.");
            } else {
                throw new EmfException(e.getMessage() + "; Failed login attempt " + failedLoginAttempts + " of " + maxFailedLoginAttempts + ".");
            }
        } catch (CommonsException e) {
            throw new EmfException(e.getMessage());
        }

        User user = userAdmin.getUser(username);

        user = userAdmin.obtainLocked(userAdmin.getUser(username), userAdmin.getUser(username));

        if (user == null)
            throw new EmfException("Unable to fetch lock on user: " + username + ".");

        user.setLoggedIn(true);
        user.setLastLoginDate(new Date());
        if (user.getPasswordResetDate() == null)
            user.setPasswordResetDate(new Date());
        user.setFailedLoginAttempts(0);
        userAdmin.updateUser(user);

        return user;
    }

    public boolean checkEmfVersion(String current) throws EmfException {
        try {
            update = userAdmin.getEmfVersion();

            if (update == null)
                return true;

            return update.trim().equalsIgnoreCase(current);
        } catch (Exception e) {
            throw new EmfException(e.getMessage());
        }
    }

    public String getUpdatedEmfVersion() {
        return update;
    }

    public Integer getEffectiveDays() throws EmfException {
        try {
            return Integer.valueOf(userAdmin.getEmfPasswordEffDays());
        } catch (EmfException e) {
            e.printStackTrace();
            throw new EmfException(e.getMessage());
        }
    }

    public void doCancel() {
        view.disposeView();
    }

    public void display(LoginView view) {
        this.view = view;
        view.observe(this);

        view.display();
    }

    public String userName() throws EmfException {
        if (this.preferences == null)
            this.preferences = new DefaultUserPreferences();

        return preferences.userName();
    }

    public String userPassword() throws EmfException {
        if (this.preferences == null)
            this.preferences = new DefaultUserPreferences();

        return preferences.userPassword();
    }

}
