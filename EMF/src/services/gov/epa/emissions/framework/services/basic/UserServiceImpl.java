package gov.epa.emissions.framework.services.basic;

import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.EmfException;
import gov.epa.emissions.framework.services.daos.UserDao;
import gov.epa.emissions.framework.services.persistence.EmfPropertiesDAO;
import gov.epa.emissions.framework.services.persistence.HibernateSessionFactory;
import gov.epa.emissions.framework.tasks.DebugLevels;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;

public class UserServiceImpl implements UserService {
    private static Log LOG = LogFactory.getLog(UserServiceImpl.class);

    private HibernateSessionFactory sessionFactory;
    
    private EntityManagerFactory entityManagerFactory;

    private UserDao dao;

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
        this.entityManagerFactory = Persistence.createEntityManagerFactory("com.baeldung.movie_catalog");
        this.dao = new UserDao(this.entityManagerFactory);
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
        if (username == null || username.trim().isEmpty())
            throw new EmfException("Please specify a valid username.");

        User user = dao.get(username);

        return user;
    }

    public synchronized User getUserByEmail(int id, String email) throws EmfException {
        User user = dao.getUserByEmail(id, email);

        return user;
    }

    public synchronized User[] getUsers() throws EmfException {
        List<?> all = dao.getAll();

        return all.toArray(new User[0]);
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

        dao.add(user);
        return dao.get(user.getUsername());
    }

    public synchronized void updateUser(User user) throws EmfException {
        dao.update(user);
    }

    public void checkDuplicatesByEmail(User user) throws EmfException {
        User existingUser = this.getUserByEmail(user.getId(), user.getEmail());

        if (existingUser != null)
            throw new EmfException("The same email address has already been used by user '"
                    + existingUser.getUsername() + "'.");
    }

    public synchronized void deleteUser(User user) throws EmfException {
        dao.delete(user);
    }

    public synchronized User obtainLocked(User owner, User object) throws EmfException {
        User locked = null;

        if (owner.isAdmin() || owner.equals(object))
            locked = dao.getLocked(owner, object);

        return locked;
    }

    public synchronized User releaseLocked(User user, User object) throws EmfException {
        User released = dao.releaseLock(user, object);

        return released;
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
