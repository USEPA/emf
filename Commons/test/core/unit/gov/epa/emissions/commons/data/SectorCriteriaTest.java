package gov.epa.emissions.commons.data;

import gov.epa.emissions.commons.data.SectorCriteria;
import junit.framework.TestCase;

public class SectorCriteriaTest extends TestCase {

    public void testShouldBeEqualIfIdsMatch() {
        SectorCriteria one = new SectorCriteria();
        one.setId(1);

        SectorCriteria anotherOne = new SectorCriteria();
        anotherOne.setId(1);
        
        assertEquals(one, anotherOne);
    }
}
