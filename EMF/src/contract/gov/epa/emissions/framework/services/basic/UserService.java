package gov.epa.emissions.framework.services.basic;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EMFService;
import gov.epa.emissions.framework.services.EmfException;

/**
 * Provides services for Login and Administrative functions
 */
public interface UserService extends EMFService {

    void authenticate(String username, String password) throws EmfException;

    User getUser(String username) throws EmfException;

    User[] getUsers() throws EmfException;

    User createUser(User user) throws EmfException;

    /**
     * should obtain lock before updating. On completion of update, the lock is released implicitly.
     */
    void updateUser(User user) throws EmfException;
    
    void checkDuplicatesByEmail(User user) throws EmfException;

    void deleteUser(User user) throws EmfException;

    User obtainLocked(User owner, User object) throws EmfException;

    User releaseLocked(User user, User object) throws EmfException;
    
    String getEmfVersion() throws EmfException;
    
    String getEmfPasswordEffDays() throws EmfException;
    
    void logExitMessage(User user) throws EmfException;
    
    byte[] getEncodedPublickey() throws EmfException;
    
    void updateEncryptedPassword(String host, String username, byte[] encodedPassword) throws EmfException;

    boolean passwordRegistered(String smokeUser, String host) throws EmfException;

    String getPropertyValue(String name) throws EmfException;

}
