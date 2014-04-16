package gov.epa.emissions.commons.db.version;

import junit.framework.TestCase;

public class ScrollableResultSetIndexTest extends TestCase {

    private ScrollableResultSetIndex index;

    protected void setUp() {
        index = new ScrollableResultSetIndex(10000,300);
    }

    public void testStartShouldBeZeroAndEndShouldBeFetchSizeOnInit() {
        assertEquals(0, index.start());
        assertEquals(10000, index.end());
    }

    public void testIndexShouldBeInRangeIfGreaterOrEqualToStartAndLessThanEqualToEnd() {
        assertTrue(index.inRange(5));
        assertFalse(index.inRange(-1));
        assertTrue(index.inRange(9999));
        assertFalse(index.inRange(10000));
    }

    public void testRelativeIndex() {
        assertEquals(5, index.relative(5));
        assertEquals(892, index.relative(892));
        assertEquals(0, index.relative(10000));
        assertEquals(7, index.relative(10007));
        assertEquals(1007, index.relative(21007));
        assertEquals(0, index.relative(0));
    }

    public void testNewStart() {
        assertEquals(0, index.newStart(5));
        assertEquals(0, index.newStart(6001));
        assertEquals(0, index.newStart(9999));
        assertEquals(0, index.newStart(0));
        assertEquals(10000, index.newStart(10000));
        assertEquals(10000, index.newStart(10001));
        assertEquals(10000, index.newStart(12001));
        assertEquals(20000, index.newStart(22001));
        assertEquals(0, index.newStart(2001));
        assertEquals(30000, index.newStart(35999));
        assertEquals(30000, index.newStart(39999));
        assertEquals(40000, index.newStart(40000));
        assertEquals(20000, index.newStart(20200));
    }

}
