package gov.epa.emissions.commons.security;

import gov.epa.emissions.commons.CommonsException;
import gov.epa.emissions.commons.data.DatasetType;
import gov.epa.emissions.commons.data.Lockable;
import gov.epa.emissions.commons.data.Mutex;
import gov.epa.emissions.commons.data.UserFeature;

import java.io.Serializable;
import java.util.Date;
import java.util.regex.Pattern;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The User value object encapsulates all of the EMF user data The User object is serialized between the server and
 * client using Apache Axis Web Services (SOAP/HTTP and XML)
 * 
 */
public class User implements Serializable, Lockable {

    private int id;

    private String name;

    private String affiliation;

    private String phone;

    private String email;

    private String username;

    private String encryptedPassword;

    private boolean isAdmin;
    
    private boolean isLoggedIn;

    private boolean isAccountDisabled;
    
    private boolean wantEmails;
    
    private Date lastLoginDate;
    
    private Date passwordResetDate;

    private Date lastResetDate;

    private int failedLoginAttempts;

    private PasswordGenerator passwordGen;

    private Mutex lock;

    private DatasetType[] excludedDatasetTypes = new DatasetType[] {};
    private UserFeature[] excludedUserFeatures = new UserFeature[] {};

    public User() {// needed for serialization
        this.passwordGen = new PasswordGenerator();
        lock = new Mutex();
        this.wantEmails = true;
    }

    public User(String name, String affiliation, String phone, String email, String username, String password,
            boolean beAdmin, boolean disabled) throws UserException {
        this();

        setName(name);
        setAffiliation(affiliation);
        setPhone(phone);
        setEmail(email);
        setUsername(username);
        setPassword(password);
        setWantEmails(true);
        setLastLoginDate(new Date());
        setPasswordResetDate(new Date());
        this.isAdmin = beAdmin;
        this.isAccountDisabled = disabled;
    }

    public User(String name) throws UserException {
        this();
        this.name = name;
    }

    @JsonIgnore
    public DatasetType[] getExcludedDatasetTypes() {
        return excludedDatasetTypes;
    }

    public void setExcludedDatasetTypes(DatasetType[] datasetTypes) {
        this.excludedDatasetTypes = datasetTypes;
    }
    
    @JsonIgnore
    public UserFeature[] getExcludedUserFeatures() {
        return excludedUserFeatures;
    }

    public void setExcludedUserFeatures(UserFeature[] userFeatures) {
        this.excludedUserFeatures = userFeatures;
    }

    public boolean equals(Object other) {
        if (!(other instanceof User))
            return false;

        User otherUser = (User) other;
        return this.username.equals(otherUser.username);
    }

    public int hashCode() {
        return username != null ? username.hashCode() : 0;
    }

    public boolean isAccountDisabled() {
        return isAccountDisabled;
    }

    public void setAccountDisabled(boolean disable) {
        this.isAccountDisabled = disable;
    }

    public String getAffiliation() {
        return affiliation;
    }

    public void setAffiliation(String affiliation) throws UserException {
        if (affiliation == null)
            throw new UserException("Affiliation should be specified");

        if (affiliation.length() < 3) {
            throw new UserException("Affiliation should have 2 or more characters");
        }

        this.affiliation = affiliation;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) throws UserException {
        if (email == null)
            throw new UserException("Email should be specified");

        if (!Pattern.matches("^(_?+\\w+(.\\w+)*)(\\w)*@(\\w)+.(\\w)+(.\\w+)*", email))
            throw new UserException("Email should have the format xx@yy.zz");

        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) throws UserException {
        if (name == null || name.length() == 0)
            throw new UserException("Name should be specified");
        this.name = name;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean inAdminGroup) {
        this.isAdmin = inAdminGroup;
    }
    
    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
        this.isLoggedIn = loggedIn;
    }
    
    public boolean getWantEmails() {
        return wantEmails;
    }

    public void setWantEmails(boolean wantEamils) {
        this.wantEmails = wantEamils;
    }
    
    public Date getLastLoginDate() {
        return lastLoginDate;
    }

    public void setLastLoginDate(Date lastLoginDate) {
        this.lastLoginDate = lastLoginDate;
    }
    
    public Date getPasswordResetDate() {
        return passwordResetDate;
    }

    public void setPasswordResetDate(Date passwordResetDate) {
        this.passwordResetDate = passwordResetDate;
    }

    public Date getLastResetDate() {
        return lastResetDate;
    }

    public void setLastResetDate(Date lastResetDate) {
        this.lastResetDate = lastResetDate;
    }

    public void setPassword(String password) throws UserException {
        if (password == null)
            throw new UserException("Password should be specified");

        if (password.length() < 12) {
            throw new UserException("Password length must be a minimum of 12 characters");
        }

        boolean hasDigits = false;
        boolean hasSymbol = false;
        boolean hasUpperLetter = false;
        boolean hasLowerLetter = false;
        int characterCriteriaMatches = 0;   //must match at least 3 of 4 criteria
//From requirements:
//        and contain characters from 3 of the following 4 categories:
//        At least 1 digit (0-9)
//        At least 1 symbol (~, !, @, #, $, %, =, +, <, >, /, ?)
//        At least 1 UPPERCASE English letter (A-Z)
//        At least 1 lowercase English letters (a-z)

        char[] validSymbols = new char[] {'~','!','@','#','$','%','=','+','<','>','/','?'};
        for (int i = 0; i < password.length(); i++) {
            if (Character.isDigit(password.charAt(i)))
                hasDigits = true;
            
            if (Character.isLetter(password.charAt(i))) {
                if (Character.isUpperCase(password.charAt(i)))
                    hasUpperLetter = true;
                if (Character.isLowerCase(password.charAt(i)))
                    hasLowerLetter = true;
            }


            for (char validSymbol : validSymbols) {
                if (Character.compare(password.charAt(i), validSymbol) == 0) {
                    hasSymbol = true;
                    break;
                }
            }
        }

        String trimmedName = name.replace(" ", "").toLowerCase();
        for (int i = 0; i < trimmedName.length() - 2; ++i) {
            if (password.toLowerCase().indexOf(trimmedName.substring(i, i + 3)) != -1) {
                throw new UserException("Must not contain any part of your full name that exceeds two characters (Example: cannot be 'SMI' if your last name is 'SMITH').");
            }
        }

        if (password.indexOf(username) != -1) {
            throw new UserException("Username should not be included in the Password");
        }

        if (hasDigits)
            ++characterCriteriaMatches;
        if (hasUpperLetter)
            ++characterCriteriaMatches;
        if (hasLowerLetter)
            ++characterCriteriaMatches;
        if (hasSymbol)
            ++characterCriteriaMatches;

        if (!hasDigits && characterCriteriaMatches < 3)
            throw new UserException("At least 1 digit (0-9)");

        if (!hasUpperLetter && characterCriteriaMatches < 3)
            throw new UserException("At least 1 UPPERCASE English letter (A-Z)");

        if (!hasLowerLetter && characterCriteriaMatches < 3)
            throw new UserException("At least 1 lowercase English letter (a-z)");

        if (!hasSymbol && characterCriteriaMatches < 3)
            throw new UserException("At least 1 symbol (~, !, @, #, $, %, =, +, <, >, /, ?)");

        try {
            this.encryptedPassword = passwordGen.encrypt(password);
        } catch (CommonsException e) {
            throw new UserException("Error encrypting password");
        }
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) throws UserException {
        if (username == null) {
            throw new UserException("Username should be specified");
        }
        if (username.length() < 3) {
            throw new UserException("Username should have at least 3 characters");
        }

        verifyUsernamePasswordDontMatch(username);

        this.username = username;
    }

    private void verifyUsernamePasswordDontMatch(String username) throws UserException {
        if (encryptedPassword == null)
            return;

        String encryptedUsername = null;
        try {
            encryptedUsername = passwordGen.encrypt(username);
        } catch (CommonsException e) {
            throw new UserException("failed on verification of username with password", e.getMessage(), e);
        }
        if (encryptedPassword.equals(encryptedUsername))
            throw new UserException("Username should be different from Password");
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) throws UserException {
        if (phone == null || phone.length() == 0)
            throw new UserException("Phone should be specified");

        this.phone = phone;
    }

    public void confirmPassword(String confirmPassword) throws UserException {
        String encryptConfirmPwd = null;

        try {
            encryptConfirmPwd = passwordGen.encrypt(confirmPassword);
        } catch (CommonsException e) {
            throw new UserException("Error encrypting password");
        }
        if (!encryptedPassword.equals(encryptConfirmPwd)) {
            throw new UserException("Confirm Password should match Password");
        }

    }

    @JsonIgnore
    public String getEncryptedPassword() {
        return encryptedPassword;
    }

    public void setEncryptedPassword(String encryptedPassword) {
        this.encryptedPassword = encryptedPassword;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getLockDate() {
        return lock.getLockDate();
    }

    public void setLockDate(Date lockDate) {
        lock.setLockDate(lockDate);
    }

    public boolean isLocked(String owner) {
        return lock.isLocked(owner);
    }

    public boolean isLocked(User owner) {
        return lock.isLocked(owner);
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public boolean isLocked() {
        return lock.isLocked();
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public boolean getLocked() {
        return lock.isLocked();
    }

    public String getLockOwner() {
        return lock.getLockOwner();
    }

    public void setLockOwner(String username) {
        lock.setLockOwner(username);
    }
    
    public String toString() {
        return getName()+" ("+getUsername()+")";
    }

    public int getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    public void setFailedLoginAttempts(int failedLoginAttempts) {
        this.failedLoginAttempts = failedLoginAttempts;
    }
}
