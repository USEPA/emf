package gov.epa.emissions.commons.data;

import gov.epa.emissions.commons.data.InternalSource;
import junit.framework.TestCase;

public class InternalSourceTest extends TestCase {

    public void testShouldParseColsListCorrectlyOnSet() {
        InternalSource s = new InternalSource();
        s.setColsList("1, 2, 3");

        String[] cols = s.getCols();
        assertEquals(3, cols.length);
        assertEquals("1", cols[0]);
        assertEquals("2", cols[1]);
        assertEquals("3", cols[2]);
    }

    public void testShouldCreateColsListWithCommaDelimited() {
        InternalSource s = new InternalSource();
        s.setCols(new String[] { "1", "2", "3" });

        assertEquals("1, 2, 3", s.getColsList());
    }
}
