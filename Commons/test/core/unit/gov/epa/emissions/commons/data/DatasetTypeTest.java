package gov.epa.emissions.commons.data;

import gov.epa.emissions.commons.security.User;

import java.util.Date;

import junit.framework.TestCase;

public class DatasetTypeTest extends TestCase {

    public void testAddKeyVal() {
        DatasetType type = new DatasetType();
        Keyword kw = new Keyword("key1");
        KeyVal val = new KeyVal(kw, "val");
        type.addKeyVal(val);

        KeyVal[] actual = type.getKeyVals();
        assertEquals(1, actual.length);
        assertEquals("key1", actual[0].getKeyword().getName());
        assertEquals("val", actual[0].getValue());
    }
    
    public void testAddQAStepTemplate() {
        DatasetType type = new DatasetType();
        QAStepTemplate val = new QAStepTemplate();
        type.addQaStepTemplate(val);
        
        QAStepTemplate[] actual = type.getQaStepTemplates();
        assertEquals(1, actual.length);
        assertSame(val, actual[0]);
    }

    public void testShouldBeLockedOnlyIfUsernameAndDateIsSet() {
        DatasetType locked = new DatasetType();
        locked.setLockOwner("user");
        locked.setLockDate(new Date());
        assertTrue("Should be locked", locked.isLocked());

        DatasetType unlockedAsOnlyUsernameIsSet = new DatasetType();
        unlockedAsOnlyUsernameIsSet.setLockOwner("user");
        assertFalse("Should be unlocked", unlockedAsOnlyUsernameIsSet.isLocked());

        DatasetType unlockedAsOnlyLockedDateIsSet = new DatasetType();
        unlockedAsOnlyLockedDateIsSet.setLockDate(new Date());
        assertFalse("Should be unlocked", unlockedAsOnlyLockedDateIsSet.isLocked());
    }

    public void testShouldBeLockedIfUsernameMatches() throws Exception {
        DatasetType locked = new DatasetType();
        locked.setLockOwner("user");
        locked.setLockDate(new Date());

        User lockedByUser = new User();
        lockedByUser.setUsername("user");
        assertTrue("Should be locked", locked.isLocked(lockedByUser.getUsername()));

        User notLockedByUser = new User();
        notLockedByUser.setUsername("user2");
        assertFalse("Should not be locked", locked.isLocked(notLockedByUser.getUsername()));
    }
}
