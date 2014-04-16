package gov.epa.emissions.commons.db.version;

import junit.framework.TestCase;

public class VersionTest extends TestCase {

    public void testPathForVersionDerivedFromRootVersion() {
        Version v = new Version();
        v.setVersion(0);
        v.setPath("");

        assertEquals("0", v.createCompletePath());
    }

    public void testPathForVersionDerivedFromNonRootVersion() {
        Version v = new Version();
        v.setVersion(2);
        v.setPath("0,1");

        assertEquals("0,1,2", v.createCompletePath());
    }

    public void testShouldObtainBaseFromPath() {
        Version v2 = new Version();
        v2.setVersion(2);
        v2.setPath("0,1");
        assertEquals(1, v2.getBase());

        Version v12 = new Version();
        v12.setVersion(12);
        v12.setPath("0,1,11");
        assertEquals(11, v12.getBase());
    }
    
    public void testShouldReturnEmptyAsBaseForVersionZero() {
        Version versionZero = new Version();
        versionZero.setVersion(0);
        versionZero.setPath("");
        assertEquals(0, versionZero.getBase());
    }
    
    public void testShouldBeEqualIfIdsMatch() {
        Version a = new Version();
        a.setId(0);
        
        Version b = new Version();
        b.setId(1);

        Version c = new Version();
        c.setId(1);
        
        assertFalse(a.equals(b));
        assertFalse(a.equals(c));
        assertEquals(b, c);
    }

}
