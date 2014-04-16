package gov.epa.emissions.framework.client.meta.versions;

import gov.epa.emissions.commons.db.version.Version;
import junit.framework.TestCase;

public class VersionsSetTest extends TestCase {

    public void testVersionNumsMatchVersionNames() {
        Version[] versions = new Version[11];
        for (int i = 0; i < versions.length; i++) {
            versions[i] = new Version();
            versions[i].setVersion(i);
            versions[i].setName("" + i);
        }

        VersionsSet vset = new VersionsSet(versions);
        Integer[] vnums = vset.versions();
        String[] vnames = vset.names();
        for (int j = 0; j < vnums.length; j++)
            assertTrue(vnums[j].intValue() == Integer.parseInt(vnames[j]));

        assertTrue(vset.name(3).equals("" + 3));
        assertTrue(vset.getVersionName(10).equals("" + 10));
    }

    public void testShouldContainListedVersion() {
        Version v1 = new Version();
        v1.setName("final2");

        Version v2 = new Version();
        v2.setName("final2");

        Version[] versions = { v1, v2 };

        VersionsSet set = new VersionsSet(versions);

        assertTrue(set.contains(v1.getName()));
        assertTrue(set.contains(v2.getName()));
        assertFalse(set.contains("non-existent"));
    }

    public void testShouldFetchNamesOfFinalVersions() {
        Version nonFinal = new Version();
        Version final1 = new Version();
        final1.setName("final2");
        final1.setFinalVersion(true);

        Version final2 = new Version();
        final2.setName("final2");
        final2.setFinalVersion(true);

        Version[] versions = { nonFinal, final1, final2 };

        VersionsSet set = new VersionsSet(versions);

        String[] results = set.namesOfFinalVersions();
        assertEquals(2, results.length);
        assertEquals(final1.getName(), results[0]);
        assertEquals(final2.getName(), results[1]);
    }
}
