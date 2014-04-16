package gov.epa.emissions.framework.services.basic;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.persistence.EmfPropertiesDAO;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;
import gov.epa.emissions.framework.tasks.DebugLevels;

import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;

public class UserServiceImpl implements UserService {
    private static Log LOG = LogFactory.getLog(UserServiceImpl.class);

    private HibernateSessionFactory sessionFactory;

    private UserDAO dao;

    private static int svcCount = 0;

    private String svcLabel = null;

    public synchronized String myTag() {
        if (svcLabel == null) {
            svcCount++;
            this.svcLabel = "#" + svcCount + "-" + getClass().getName() + "-" + new Date().getTime();
        }

        return "For label: " + svcLabel + " # of active objects of this type= " + svcCount;
    }

    public UserServiceImpl() {
        this(HibernateSessionFactory.get());
        myTag();

        if (DebugLevels.DEBUG_1())
            System.out.println(">>>> " + myTag());
    }

    public UserServiceImpl(HibernateSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
        this.dao = new UserDAO();
        myTag();

        if (DebugLevels.DEBUG_1())
            System.out.println(">>>> " + myTag());
    }

    public synchronized void authenticate(String username, String password) throws EmfException {
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

    public synchronized User getUser(String username) throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            if (username == null || username.trim().isEmpty())
                throw new EmfException("Please specify a valid username.");

            User user = dao.get(username, session);

            return user;
        } catch (RuntimeException e) {
            LOG.error("Could not get User - " + username, e);
            throw new EmfException("Could not get User due to data access failure: \n" + e.getMessage());
        } finally {
            session.close();
        }
    }

    public synchronized User getUserByEmail(int id, String email) throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            User user = dao.getUserByEmail(id, email, session);

            return user;
        } catch (RuntimeException e) {
            LOG.error("Could not get User by email - " + email, e);
            throw new EmfException("Could not get User due to data access failure");
        } finally {
            session.close();
        }
    }

    public synchronized User[] getUsers() throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            List<?> all = dao.all(session);
            session.close();

            return all.toArray(new User[0]);
        } catch (Exception e) {
            LOG.error("Could not get all Users", e);
            throw new EmfException("Unable to fetch all users due to data access failure");
        }
    }

    public synchronized User createUser(User user) throws EmfException {
        User existingUser = this.getUser(user.getUsername());

        if (existingUser != null) {
            throw new EmfException("Could not create new user. The username '" + user.getUsername()
                    + "' is already taken");
        }

        existingUser = this.getUserByEmail(user.getId(), user.getEmail());

        if (existingUser != null)
            throw new EmfException("The same email address has already been used by user '"
                    + existingUser.getUsername() + "'.");

        Session session = sessionFactory.getSession();
        try {
            dao.add(user, session);
            return dao.get(user.getUsername(), session);
        } catch (RuntimeException e) {
            LOG.error("Could not create new user - " + user.getUsername(), e);
            throw new EmfException("Unable to fetch user due to data access failure");
        } finally {
            session.close();
        }
    }

    public synchronized void updateUser(User user) throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            dao.update(user, session);
        } catch (RuntimeException e) {
            LOG.error("Could not update user - " + user.getName(), e);
            throw new EmfException("Unable to update user due to data access failure");
        } finally {
            if (session != null && session.isConnected())
                session.close();
        }
    }

    public void checkDuplicatesByEmail(User user) throws EmfException {
        User existingUser = this.getUserByEmail(user.getId(), user.getEmail());

        if (existingUser != null)
            throw new EmfException("The same email address has already been used by user '"
                    + existingUser.getUsername() + "'.");
    }

    public synchronized void deleteUser(User user) throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            dao.remove(user, session);
            session.close();
        } catch (RuntimeException e) {
            LOG.error("Could not delete user - " + user.getName(), e);
            throw new EmfException("Unable to delete user due to data access failure");
        }
    }

    public synchronized User obtainLocked(User owner, User object) throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            User locked = null;

            if (owner.isAdmin() || owner.equals(object))
                locked = dao.obtainLocked(owner, object, session);

            return locked;
        } catch (RuntimeException e) {
            LOG.error("Could not obtain lock for user: " + object.getUsername() + " by owner: " + owner.getUsername(),
                    e);
            throw new EmfException("Unable to fetch lock user due to data access failure");
        } finally {
            if (session != null && session.isConnected())
                session.close();
        }
    }

    public synchronized User releaseLocked(User user, User object) throws EmfException {
        try {
            Session session = sessionFactory.getSession();
            User released = dao.releaseLocked(user, object, session);
            session.close();

            return released;
        } catch (RuntimeException e) {
            LOG.error("Could not release lock for user: " + object.getUsername() + " by owner: "
                    + object.getLockOwner(), e);
            throw new EmfException("Unable to release lock on user due to data access failure");
        }
    }

    public synchronized String getEmfVersion() throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            EmfProperty property = new EmfPropertiesDAO().getProperty("EMF-version", session);
            return property == null ? null : property.getValue();
        } catch (Exception e) {
            LOG.error("Could not get EMF version info.", e);
            throw new EmfException("Could not get EMF version info.");
        } finally {
            session.close();
        }
    }

    public synchronized String getEmfPasswordEffDays() throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            EmfProperty property = new EmfPropertiesDAO().getProperty("PASSWORD_EFFECTIVE_DAYS", session);
            return property == null ? null : property.getValue();
        } catch (Exception e) {
            LOG.error("Could not get EMF password effective days.", e);
            throw new EmfException("Could not get EMF password effective days.");
        } finally {
            session.close();
        }
    }

    @Override
    protected synchronized void finalize() throws Throwable {
        this.sessionFactory = null;
        super.finalize();
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

    public String getPropertyValue(String name) throws EmfException {
        Session session = sessionFactory.getSession();

        try {
            EmfProperty property = new EmfPropertiesDAO().getProperty(name, session);
            return property != null ? property.getValue() : null;
        } catch (Exception e) {
            LOG.error("Could not get EMF property.", e);
            throw new EmfException("Could not get EMF property.");
        } finally {
            session.close();
        }
    }

}
