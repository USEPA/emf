package gov.epa.emissions.framework.services.basic;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.persistence.EmfPropertyDao;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service("userService")
@Transactional
public class UserServiceImpl implements UserService {
    private static Log LOG = LogFactory.getLog(UserServiceImpl.class);

    private UserDao userDao;

//    private static int svcCount = 0;
//
//    private String svcLabel = null;
    
    private EmfPropertyDao emfPropertyDao;
    
//    public String myTag() {
//        if (svcLabel == null) {
//            svcCount++;
//            this.svcLabel = "#" + svcCount + "-" + getClass().getName() + "-" + new Date().getTime();
//        }
//
//        return "For label: " + svcLabel + " # of active objects of this type= " + svcCount;
//    }

//  public UserServiceImpl() {
//  }
    
//    public UserServiceImpl() {
//        this(HibernateSessionFactory.get());
//      	 System.out.println("UserServiceImpl()");
//        myTag();
//
//        if (DebugLevels.DEBUG_1())
//            System.out.println(">>>> " + myTag());
//    }
//
//    public UserServiceImpl(HibernateSessionFactory sessionFactory) {
//    	 System.out.println("UserServiceImpl(HibernateSessionFactory sessionFactory)");
//        this.sessionFactory = sessionFactory;
//   	 System.out.println("UserServiceImpl(HibernateSessionFactory sessionFactory)");
//        this.dao = new UserDAO();
//   	 System.out.println("UserServiceImpl(HibernateSessionFactory sessionFactory)");
//        myTag();
//   	 System.out.println("UserServiceImpl(HibernateSessionFactory sessionFactory)");
//
//     try {
//         if (DebugLevels.DEBUG_1())
//             System.out.println(">>>> " + myTag());
//     } catch (Exception e) {
//         LOG.error("UserServiceImpl(HibernateSessionFactory sessionFactory) ", e);
//     }
//   	 System.out.println("UserServiceImpl(HibernateSessionFactory sessionFactory) end");
//    }

    @Transactional(readOnly=true)
    public void authenticate(String username, String password) throws EmfException {
        try {
            User user = getUser(username);

            if (user == null)
                throw new AuthenticationException("User " + username + " does not exist");

            LOG.warn("User " + user.getUsername() + " (" + user.getName() + ") tried to login to the EMF service.");

            if (user.isAccountDisabled())
                throw new AuthenticationException("Account Disabled");

            if (!user.getEncryptedPassword().equals(password))
                throw new AuthenticationException("Incorrect Password");
        } catch (Exception e) {
            LOG.error("Unable to authenticate user: " + username + ". " + e.getMessage());
            throw new EmfException(e.getMessage());
        }
    }

    @Transactional(readOnly=true)
    public User getUser(String username) throws EmfException {
        try {
            if (username == null || username.trim().isEmpty())
                throw new EmfException("Please specify a valid username.");

            User user = userDao.get(username);

            return user;
        } catch (RuntimeException e) {
            LOG.error("Could not get User - " + username, e);
            throw new EmfException("Could not get User due to data access failure: \n" + e.getMessage());
        } catch (Exception e) {
            LOG.error("Could not get User - " + username, e);
            throw new EmfException("Could not get User due to data access failure: \n" + e.getMessage());
        }
    }

    @Transactional(readOnly=true)
    public User getUserByEmail(int id, String email) throws EmfException {

        try {
            User user = userDao.getUserByIdAndEmail(id, email);

            return user;
        } catch (RuntimeException e) {
            LOG.error("Could not get User by email - " + email, e);
            throw new EmfException("Could not get User due to data access failure");
        }
    }

    @Transactional(readOnly=true)
    public User[] getUsers() throws EmfException {
        try {
            List<User> all = userDao.all();

            return all.toArray(new User[0]);
        } catch (Exception e) {
            LOG.error("Could not get all Users", e);
            throw new EmfException("Unable to fetch all users due to data access failure");
        }
    }

    public User createUser(User user) throws EmfException {
        User existingUser = this.getUser(user.getUsername());

        if (existingUser != null) {
            throw new EmfException("Could not create new user. The username '" + user.getUsername()
                    + "' is already taken");
        }

        existingUser = this.getUserByEmail(user.getId(), user.getEmail());

        if (existingUser != null)
            throw new EmfException("The same email address has already been used by user '"
                    + existingUser.getUsername() + "'.");

        try {
            userDao.add(user);
            return userDao.get(user.getUsername());
        } catch (RuntimeException e) {
            LOG.error("Could not create new user - " + user.getUsername(), e);
            throw new EmfException("Unable to fetch user due to data access failure");
        }
    }
    
    @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
    public void updateUser(User user) throws EmfException {
        try {
            userDao.releaseLockOnUpdate(user, user);
        } catch (RuntimeException e) {
            LOG.error("Could not update user - " + user.getName(), e);
            throw new EmfException("Unable to update user due to data access failure");
        }
    }

    public void checkDuplicatesByEmail(User user) throws EmfException {
        User existingUser = this.getUserByEmail(user.getId(), user.getEmail());

        if (existingUser != null)
            throw new EmfException("The same email address has already been used by user '"
                    + existingUser.getUsername() + "'.");
    }

    public void deleteUser(User user) throws EmfException {
        try {
            userDao.remove(user);
        } catch (RuntimeException e) {
            LOG.error("Could not delete user - " + user.getName(), e);
            throw new EmfException("Unable to delete user due to data access failure");
        }
    }

//    @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
//    public User obtainLocked(User owner, User object) throws EmfException {
//
//        try {
//            User locked = null;
//    		System.out.println("obtainLocked() ");
//
//            if (owner.isAdmin() || owner.equals(object))
//                locked = userDao.getLocked(owner, object);
//    		System.out.println("obtainLocked() ");
//
//            return locked;
//        } catch (RuntimeException e) {
//            LOG.error("Could not obtain lock for user: " + object.getUsername() + " by owner: " + owner.getUsername(),
//                    e);
//            throw new EmfException("Unable to fetch lock user due to data access failure");
//        }
//    }

    @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
    public User obtainLocked(User owner, Integer userId) throws EmfException {

            User locked = null;

            //only admin or actual user can do editing...
            if (owner.isAdmin() || owner.getId() == userId)
                locked = userDao.getLocked(owner.getId(), userId);

            return locked;
    }

    @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
    public User releaseLocked(User user, User object) throws EmfException {
        try {
            User released = userDao.releaseLock(user, object);

            return released;
        } catch (RuntimeException e) {
            LOG.error("Could not release lock for user: " + object.getUsername() + " by owner: "
                    + object.getLockOwner(), e);
            throw new EmfException("Unable to release lock on user due to data access failure");
        }
    }

    @Transactional(readOnly=true)
    public String getEmfVersion()  {    	
        try {
			EmfProperty property = emfPropertyDao.getProperty("EMF-version");
			return property == null ? null : property.getValue();
		} catch (Exception e) {
            LOG.warn("getEmfVersion " + e.getMessage());
			
			e.printStackTrace();
		}
		return null;
    }

    @Transactional(readOnly=true)
    public String getEmfPasswordEffDays() throws EmfException {
        EmfProperty property = emfPropertyDao.getProperty("PASSWORD_EFFECTIVE_DAYS");
        return property == null ? null : property.getValue();
    }

    public void logExitMessage(User user) {
        LOG.warn("User " + user.getUsername() + " (" + user.getName() + ") logged out of the EMF service.");

        try {
            User existed = getUser(user.getUsername());
            if (existed == null)
                LOG.warn("User " + user.getName() + " does not exist");
        } catch (Exception e) {
            LOG.warn("Problem retrieving user: " + user.getUsername() + ". " + e.getMessage());
        }
    }

    public byte[] getEncodedPublickey() throws EmfException {
        return SecurityManager.getInstance().getEncodedPublickey();
    }

    public void updateEncryptedPassword(String host, String username, byte[] encodedPassword) throws EmfException {
        SecurityManager.getInstance().updateEncryptedPassword(host, username, encodedPassword);
    }

   public boolean passwordRegistered(String user, String host) {
        return SecurityManager.getInstance().passwordRegistered(user, host);
    }

    @Transactional(readOnly=true)
    public String getPropertyValue(String name) throws EmfException {
        EmfProperty property = emfPropertyDao.getProperty(name);
        return property != null ? property.getValue() : null;
    }

    @Autowired
	public void setEmfPropertyDao(EmfPropertyDao emfPropertyDao) {
		this.emfPropertyDao = emfPropertyDao;
	}

	@Autowired
	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
    public User obtainLocked(User owner, User object) throws EmfException {
        User locked = null;

        if (owner.isAdmin() || owner.equals(object))
            locked = userDao.getLocked(owner.getId(), object.getId());

        return locked;
    }
}
