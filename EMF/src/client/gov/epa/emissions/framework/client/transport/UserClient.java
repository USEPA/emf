package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.UserService;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

public class UserClient implements UserService {

    public User[] getAll() {
        Client client = ClientBuilder.newClient();
        WebTarget target = client
                .target("https://sage.hesc.epa.gov:8443/emf2")
                .path("rest")
                .path("user")
                .path("getAll");

        return target
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(User[].class);        
    }

    @Override
    public void authenticate(String username, String password) throws EmfException {
        // NOTE Auto-generated method stub
        
    }

    @Override
    public User getUser(String username) throws EmfException {
        Client client = ClientBuilder.newClient();
        
        WebTarget target = client
                .target("https://sage.hesc.epa.gov:8443/emf2")
                .path("rest")
                .path("user")
                .path("getUser")
                .queryParam("username", username);

        return target
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(User.class);        
    }

    @Override
    public User[] getUsers() throws EmfException {
        // NOTE Auto-generated method stub
        return null;
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
