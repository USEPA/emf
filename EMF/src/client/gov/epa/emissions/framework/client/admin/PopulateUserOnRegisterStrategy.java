package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.UserFeature;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.commons.security.UserException;
import gov.epa.emissions.framework.services.EmfException;

import java.util.Date;

public class PopulateUserOnRegisterStrategy implements PopulateUserStrategy {

    private User user;
    private String oldPassword = null;

    PopulateUserOnRegisterStrategy(User user) {
        this.user = user;
    }

    public void populate(String name, String affiliation, String phone, String email, String username, char[] password,
            char[] confirmPassword, Boolean wantEmails) throws EmfException {
        try {
            user.setName(name);
            user.setAffiliation(affiliation);
            user.setPhone(phone);
            user.setEmail(email);

            user.setUsername(username);
            user.setPassword(new String(password));
            user.confirmPassword(new String(confirmPassword));
            user.setLoggedIn(true);
            user.setWantEmails(wantEmails);
            user.setLastLoginDate(new Date());
            user.setPasswordResetDate(new Date());
        } catch (UserException e) {
            throw new EmfException(e.getMessage());
        }
    }
    
    public void checkNewPwd(char[] password) throws EmfException {
        if ( oldPassword == null ){
            oldPassword = user.getEncryptedPassword();
        }
        try {
            user.setPassword(new String(password));
        } catch (UserException e) {
            throw new EmfException(e.getMessage());
        }
      
        if (oldPassword.equals(user.getEncryptedPassword())) {
            throw new EmfException("Please specify a new password. ");
        }
    }

    public void populate(String name, String affiliation, String phone, String email, String username, char[] password,
            char[] confirmPassword, Boolean wantEmails, DatasetType[] eDatasetTypes, UserFeature[] eUserFeatures)
            throws EmfException {
        // NOTE Auto-generated method stub      
    }

}
