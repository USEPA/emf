package gov.epa.emissions.framework.services.basic;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;

import org.springframework.remoting.jaxrpc.ServletEndpointSupport;

@SuppressWarnings("deprecation")
public class UserWSEndPoint extends ServletEndpointSupport implements
		UserService {

	private UserService userService;

	protected void onInit() {
		this.userService = (UserService) getWebApplicationContext().getBean(
				"userService");
	}

	public void authenticate(String username, String password)
			throws EmfException {
		userService.authenticate(username, password);
	}

	public User getUser(String username) throws EmfException {
		return userService.getUser(username);
	}

//	public User getUserByEmail(int id, String email)
//			throws EmfException {
//		userService.getUserByEmail
//	}

	public User[] getUsers() throws EmfException {
		return userService.getUsers();
	}

	public User createUser(User user) throws EmfException {
		return userService.createUser(user);
	}

	public void updateUser(User user) throws EmfException {
		userService.updateUser(user);
	}

	public void checkDuplicatesByEmail(User user) throws EmfException {
		userService.checkDuplicatesByEmail(user);
	}

	public void deleteUser(User user) throws EmfException {
		userService.deleteUser(user);
	}

//	public User obtainLocked(User owner, User object)
//			throws EmfException {
//		return userService.obtainLocked(owner, object);
//	}

	public User obtainLocked(User owner, Integer userId)
			throws EmfException {
		return userService.obtainLocked(owner, userId);
	}

	public User releaseLocked(User user, User object)
			throws EmfException {
		return userService.releaseLocked(user, object);
	}

	public String getEmfVersion() throws EmfException {
		return userService.getEmfVersion();
	}

	public String getEmfPasswordEffDays() throws EmfException {
		return userService.getEmfPasswordEffDays();
	}

	public byte[] getEncodedPublickey() throws EmfException {
		return userService.getEncodedPublickey();
	}

	public void updateEncryptedPassword(String host, String username,
			byte[] encodedPassword) throws EmfException {
		userService.updateEncryptedPassword(host, username, encodedPassword);
	}

	public boolean passwordRegistered(String user, String host) throws EmfException {
		return userService.passwordRegistered(user, host);
	}

	public String getPropertyValue(String name) throws EmfException {
		return userService.getPropertyValue(name);
	}

	@Override
	public void logExitMessage(User user) throws EmfException {
		userService.logExitMessage(user);
	}

    @Override
    public User obtainLocked(User owner, User object) throws EmfException {
        return userService.obtainLocked(owner, object);
    }
}
