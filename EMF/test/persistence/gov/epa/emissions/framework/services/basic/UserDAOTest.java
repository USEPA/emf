package gov.epa.emissions.framework.services.basic;

import gov.epa.emissions.commons.db.HibernateTestCase;
import gov.epa.emissions.commons.security.User;
import gov.epa.emissions.framework.services.basic.UserDAO;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

public class UserDAOTest extends HibernateTestCase {

    private UserDAO dao;

    private User user;

    protected void setUp() throws Exception {
        super.setUp();

        dao = new UserDAO();
        user = newUser(dao);
    }

    protected void doTearDown() throws Exception {
        remove(user);
    }

    public void testShouldFetchAllUsers() {
        List users = dao.all(session);
        assertTrue("Should contain at least 2 users", users.size() >= 2);
    }

    public void testShouldAddUser() throws Exception {
        List usersBeforeAdd = dao.all(session);
        User user = newUser("add-user", dao);

        // test
        dao.add(user, session);

        // assert
        try {
            List usersAfterAdd = dao.all(session);
            assertEquals(usersBeforeAdd.size() + 1, usersAfterAdd.size());
        } finally {
            remove(user);
        }
    }

    public void testShouldGetSpecificUser() throws Exception {
        // test
        User loaded = dao.get(user.getUsername(), session);

        // assert
        assertEquals(user.getUsername(), loaded.getUsername());
        assertEquals(user.getName(), loaded.getName());
    }

    public void testShouldVerifyIfUserAlreadyExists() throws Exception {
        assertTrue("Should contain the added user", dao.contains(user.getUsername(), session));
    }

    public void testShouldRemoveUser() throws Exception {
        List usersBeforeRemove = dao.all(session);
        User owner = dao.get("admin", session);
        User locked = dao.obtainLocked(owner, user, session);

        // test
        dao.remove(locked, session);

        // assert
        try {
            List usersAfterRemove = dao.all(session);
            assertEquals(usersBeforeRemove.size(), usersAfterRemove.size() + 1);
        } finally {
            user = newUser(dao);// restore
        }
    }

    public void testShouldFailToRemoveUserIfLockedByAnotherUser() throws Exception {
        User owner = dao.get("admin", session);
        dao.obtainLocked(owner, user(user.getUsername()), session);

        try {
            user.setLockOwner(null);// erasing existing owner
            dao.remove(user, session);
        } catch (Exception e) {
            return;
        }

        fail("Should have failed to remove if locked by another user");
    }

    public void testShouldObtainLockedUser() {
        User owner = dao.get("admin", session);

        User locked = dao.obtainLocked(owner, user, session);
        assertTrue("Should be locked by owner", locked.isLocked(owner));

        User userLoadedFromDb = user(locked.getUsername());
        assertEquals(userLoadedFromDb.getUsername(), user.getUsername());
        assertTrue("Should be locked by owner", userLoadedFromDb.isLocked(owner));
    }

    public void testShouldUpdateLockedUser() throws Exception {
        User emf = dao.get("emf", session);

        User modified1 = dao.obtainLocked(emf, user, session);
        assertEquals(modified1.getLockOwner(), emf.getUsername());
        modified1.setName("TEST");

        User modified2 = dao.update(modified1, session);
        assertEquals("TEST", modified1.getName());
        assertEquals(modified2.getLockOwner(), null);
    }

    public void testShouldReleaseLockOnReleaseLockedUser() throws Exception {
        User emf = dao.get("emf", session);

        User locked = dao.obtainLocked(emf, user, session);
        User released = dao.releaseLocked(emf, locked, session);
        assertFalse("Should have released lock", released.isLocked());

        User loadedFromDb = user(user.getUsername());
        assertFalse("Should have released lock", loadedFromDb.isLocked());
    }

    private User newUser(UserDAO dao) {
        return newUser("user-dao-test", dao);
    }

    private User newUser(String username, UserDAO dao) {
        User user = new User();
        user.setUsername(username);
        user.setPassword("abc12345");
        user.setName("user dao");
        user.setAffiliation("test");
        user.setPhone("123-123-1234");
        user.setEmail("email@user-test.test");

        dao.add(user, session);

        return user(user.getUsername());
    }

    private User user(String username) {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            Criteria crit = session.createCriteria(User.class).add(Restrictions.eq("username", username));
            tx.commit();

            return (User) crit.uniqueResult();
        } catch (HibernateException e) {
            tx.rollback();
            throw e;
        }
    }

    private void remove(User user) {
        Transaction tx = session.beginTransaction();
        session.delete(user);
        tx.commit();
    }
}
