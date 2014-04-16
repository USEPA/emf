package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.UserFeature;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.commons.security.UserException;
import gov.epa.emissions.framework.services.EmfException;

import java.util.Date;

public class PopulateUserOnUpdateStrategy implements PopulateUserStrategy {

    private User user;
    
    private String oldPassword =null;

    public PopulateUserOnUpdateStrategy(User user) {
        this.user = user;
    }

    public void populate(String name, String affiliation, String phone, String email, String username, char[] password,
            char[] confirmPassword, Boolean wantEmails, DatasetType[] eDatasetTypes, 
            UserFeature[] eUserFeatures) throws EmfException {
        try {
            user.setName(name);
            user.setAffiliation(affiliation);
            user.setPhone(phone);
            user.setEmail(email);
            if (password.length > 0) {
                checkNewPwd(password);
                user.confirmPassword(new String(confirmPassword));
                user.setPasswordResetDate(new Date());
            }
            user.setWantEmails(wantEmails);
            user.setExcludedDatasetTypes(eDatasetTypes);
            user.setExcludedUserFeatures(eUserFeatures);
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
            char[] confirmPassword, Boolean wantEmails) throws EmfException {
        // NOTE Auto-generated method stub
        
    }

}
