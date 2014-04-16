package gov.epa.emissions.framework.client.transport;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.basic.UserService;

public class UserServiceTransport implements UserService {
    private CallFactory callFactory;

    private DataMappings mappings;
    
    private EmfCall call;

    public UserServiceTransport(String endpoint) {
        callFactory = new CallFactory(endpoint);
        mappings = new DataMappings();
    }
    
    private EmfCall call() throws EmfException {
        if (call == null)
            call =  callFactory.createSessionEnabledCall("User Service");
        
        return call;
    }

    public synchronized void authenticate(String username, String password) throws EmfException {
        EmfCall call = call();

        call.setOperation("authenticate");
        call.addStringParam("username");
        call.addStringParam("password");
        call.setStringReturnType();

        call.request(new Object[] { username, password });
    }

    public synchronized User getUser(String username) throws EmfException {
        EmfCall call = call();

        call.setOperation("getUser");
        call.addStringParam("username");
        call.setReturnType(mappings.user());
        Object[] params = new Object[] { username };

        return (User) call.requestResponse(params);
    }

    public synchronized User createUser(User user) throws EmfException {
        EmfCall call = call();

        call.setOperation("createUser");
        call.addParam("user", mappings.user());
        call.setReturnType(mappings.user());

        return (User) call.requestResponse(new Object[] { user });
    }

    public void checkDuplicatesByEmail(User user) throws EmfException {
        EmfCall call = call();

        call.setOperation("checkDuplicatesByEmail");
        call.addParam("user", mappings.user());
        call.setVoidReturnType();

        call.request(new Object[] { user });
    }
    
    public synchronized void updateUser(User user) throws EmfException {
        EmfCall call = call();

        call.setOperation("updateUser");
        call.addParam("user", mappings.user());
        call.setVoidReturnType();

        call.request(new Object[] { user });
    }

    public synchronized void deleteUser(User user) throws EmfException {
        EmfCall call = call();

        call.setOperation("deleteUser");
        call.addParam("user", mappings.user());
        call.setVoidReturnType();

        call.request(new Object[] { user });
    }

    public synchronized User[] getUsers() throws EmfException {
        EmfCall call = call();

        call.setOperation("getUsers");
        call.setReturnType(mappings.users());

        return (User[]) call.requestResponse(new Object[0]);
    }

    public synchronized User obtainLocked(User owner, User object) throws EmfException {
        EmfCall call = call();

        call.setOperation("obtainLocked");
        call.addParam("owner", mappings.user());
        call.addParam("object", mappings.user());
        call.setReturnType(mappings.user());
        Object[] params = new Object[] { owner, object };

        return (User) call.requestResponse(params);
    }

    public synchronized User releaseLocked(User user, User object) throws EmfException {
        EmfCall call = call();

        call.setOperation("releaseLocked");
        call.addParam("user", mappings.user());
        call.addParam("object", mappings.user());
        call.setReturnType(mappings.user());

        return (User) call.requestResponse(new Object[] { user, object });
    }

    public synchronized String getEmfVersion() throws EmfException {
        EmfCall call = call();
        
        call.setOperation("getEmfVersion");
        call.setStringReturnType();
        
        return (String) call.requestResponse(new Object[] { });
    }
    
    public synchronized String getEmfPasswordEffDays() throws EmfException {
        EmfCall call = call();
        
        call.setOperation("getEmfPasswordEffDays");
        call.setStringReturnType();
        
        return (String) call.requestResponse(new Object[] { });
    }

    public void logExitMessage(User user) throws EmfException {
        EmfCall call = call();

        call.setOperation("logExitMessage");
        call.addParam("user", mappings.user());
        call.setVoidReturnType();

        call.request(new Object[] { user });
    }

    public byte[] getEncodedPublickey() throws EmfException {
        EmfCall call = call();

        call.setOperation("getEecodedPublickey");
        call.setByteArrayReturnType();

        return (byte[]) call.requestResponse(new Object[] { });
    }

    public void updateEncryptedPassword(String host, String username, byte[] encodedPassword) throws EmfException {
        EmfCall call = call();

        call.setOperation("updateEncryptedPassword");
        call.addStringParam("host");
        call.addStringParam("username");
        call.addByteArrayParam();
        call.setVoidReturnType();

        call.request(new Object[] { host, username, encodedPassword });
    }

    public boolean passwordRegistered(String user, String host) throws EmfException {
        EmfCall call = call();

        call.setOperation("passwordRegistered");
        call.addStringParam("user");
        call.addStringParam("host");
        call.setBooleanReturnType();

        return (Boolean) call.requestResponse(new Object[] { user, host });
    }

    public String getPropertyValue(String name) throws EmfException {
        EmfCall call = call();
        
        call.setOperation("getPropertyValue");
        call.addStringParam("name");
        call.setStringReturnType();
        
        return (String) call.requestResponse(new Object[] { name });
    }

}
