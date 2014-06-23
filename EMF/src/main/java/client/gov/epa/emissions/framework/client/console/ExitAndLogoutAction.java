package gov.epa.emissions.framework.client.console;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.client.EmfSession;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.UserService;
import gov.epa.emissions.framework.ui.YesNoDialog;

public class ExitAndLogoutAction {

    private EmfConsole emfConsole;

    private DesktopManager desktopManager;

    private EmfSession session;

    private UserService userService;

    public ExitAndLogoutAction(EmfConsole parent, DesktopManager desktopManager) {
        this.emfConsole = parent;
        this.desktopManager = desktopManager;
        this.session = emfConsole.getSession();
        this.userService = session.userService();
    }

    public boolean logout() {
        String message = "Do you want to log out of the Emission Modeling Framework?";
        if (confirm(message)) {
            if (desktopManager.closeAll()) {
                try {
                    logoutUser();
                } catch (EmfException e) {
                    // NOTE Auto-generated catch block
                    e.printStackTrace();
                }
                emfConsole.disposeView();
                emfConsole.logExitMessage();
                return true;
            }
        }
        return false;
    }

    private void logoutUser() throws EmfException {
        User user = session.user();
        if (user.isLoggedIn()) {
            user = userService.obtainLocked(session.user(), session.user());

            if (user != null) { //Let it be silent if lock cannot be obtained.
                user.setLoggedIn(false);
                userService.updateUser(user);
            }
        }
    }

    public boolean exit() throws EmfException {
        String message = "Do you want to exit the Emission Modeling Framework?";
        
        if (confirm(message)) {
            if (desktopManager.closeAll()) {
                logoutUser();
                emfConsole.disposeView();
                emfConsole.logExitMessage();
                System.exit(0);
            }
        }
        
        return false;
    }

    private boolean confirm(String message) {
        YesNoDialog emfDialog = new YesNoDialog(emfConsole, "Logout/Exit Confirmation", message);
        return emfDialog.confirm();
    }

}
