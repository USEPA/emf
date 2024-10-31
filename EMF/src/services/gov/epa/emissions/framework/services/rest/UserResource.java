package gov.epa.emissions.framework.services.rest;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.AuthenticationException;
import gov.epa.emissions.framework.services.basic.UserService;
import gov.epa.emissions.framework.services.daos.UserDao;
import gov.epa.emissions.framework.services.persistence.JpaEntityManagerFactory;
import gov.epa.emissions.framework.services.rest.dtos.UserDto;

import javax.inject.Singleton;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

@Singleton
@Path("user")
public class UserResource implements UserService {

    private UserDao userDao = new UserDao(JpaEntityManagerFactory.get());
    
    @GET @Path("/getAll")
    @Produces({"application/json"})
    public User[] getAll() {
        return userDao
                .getAll()
                .toArray(new User[0]);
    }

    @POST @Path("/authenticate")
    @Override
    public void authenticate(@FormParam("username") String username, @FormParam("password") String password) throws EmfException {
        try {
            User user = getUser(username);

            if (user == null)
                throw new AuthenticationException("User " + username + " does not exist");

//            LOG.warn("User " + user.getUsername() + " (" + user.getName() + ") tried to login to the EMF service.");

            if (user.isAccountDisabled())
                throw new AuthenticationException("Account Disabled");

            if (!user.getEncryptedPassword().equals(password))
                throw new AuthenticationException("Incorrect Password");
        } catch (Exception e) {
//            LOG.error("Unable to authenticate user: " + username + ". " + e.getMessage());
            throw new EmfException(e.getMessage());
        }
    }

    @GET @Path("/getUser")
    @Produces({"application/json"})
    @Override
    public User getUser(@QueryParam("username") String username) throws EmfException {
        if (username == null || username.trim().isEmpty())
            throw new EmfException("Please specify a valid username.");

        User user = userDao.get(username);

        return user;
    }

    @Override
    public UserDto[] getUsers() throws EmfException {
        return userDao
                .getAll()
                .toArray(new UserDto[0]);
    }

    @Override
    public User createUser(User user) throws EmfException {
        // NOTE Auto-generated method stub
        return null;
    }

    @Override
    public void updateUser(User user) throws EmfException {
        // NOTE Auto-generated method stub
        
    }

    @Override
    public void checkDuplicatesByEmail(User user) throws EmfException {
        // NOTE Auto-generated method stub
        
    }

    @Override
    public void deleteUser(User user) throws EmfException {
        // NOTE Auto-generated method stub
        
    }

    @Override
    public User obtainLocked(User owner, User object) throws EmfException {
        // NOTE Auto-generated method stub
        return null;
    }

    @Override
    public User releaseLocked(User user, User object) throws EmfException {
        // NOTE Auto-generated method stub
        return null;
    }

    @Override
    public String getEmfVersion() throws EmfException {
        // NOTE Auto-generated method stub
        return null;
    }

    @Override
    public String getEmfPasswordEffDays() throws EmfException {
        // NOTE Auto-generated method stub
        return null;
    }

    @Override
    public void logExitMessage(User user) throws EmfException {
        // NOTE Auto-generated method stub
        
    }

    @Override
    public byte[] getEncodedPublickey() throws EmfException {
        // NOTE Auto-generated method stub
        return null;
    }

    @Override
    public void updateEncryptedPassword(String host, String username, byte[] encodedPassword) throws EmfException {
        // NOTE Auto-generated method stub
        
    }

    @Override
    public boolean passwordRegistered(String smokeUser, String host) throws EmfException {
        // NOTE Auto-generated method stub
        return false;
    }

    @Override
    public String getPropertyValue(String name) throws EmfException {
        // NOTE Auto-generated method stub
        return null;
    }
}