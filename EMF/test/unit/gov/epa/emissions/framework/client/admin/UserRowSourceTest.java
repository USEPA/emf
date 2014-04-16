package gov.epa.emissions.framework.client.admin;

import gov.epa.emissions.commons.security.User;
import junit.framework.TestCase;

public class UserRowSourceTest extends TestCase {

    public void testShouldFillValuesInAppropriatePositions() {
        User user = user();

        UserRowSource source = new UserRowSource(user);

        Object[] values = source.values();
        assertEquals(4, values.length);
        assertEquals(user.getUsername(), values[0]);
        assertEquals(user.getName(), values[1]);
        assertEquals(user.getEmail(), values[2]);
        assertEquals(user.isAdmin(), ((Boolean)values[3]).booleanValue());
    }

    private User user() {
        User user = new User();
        user.setUsername("username");
        user.setName("name");
        user.setEmail("user@test.org");
        user.setAdmin(true);

        return user;
    }

    public void testShouldTrackOriginalSource() {
        User user = user();
        UserRowSource rowSource = new UserRowSource(user);

        assertEquals(user, rowSource.source());
    }
}
