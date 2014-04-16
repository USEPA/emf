package gov.epa.emissions.commons.db;

import gov.epa.emissions.commons.db.version.VersionedRecord;
import junit.framework.TestCase;

public class PageTest extends TestCase {

    public void testShouldAddRecord() {
        Page page = new Page();

        page.add(new VersionedRecord());
        page.add(new VersionedRecord());

        assertEquals(2, page.count());
    }

    public void testShouldReturnRangeRepresentingMinAndMaxRecordIds() {
        Page page = new Page();
        VersionedRecord record1 = new VersionedRecord();
        page.add(record1);
        VersionedRecord record2 = new VersionedRecord();
        page.add(record2);
        
        page.setMin(82);
        assertEquals(82, page.getMin());

        assertEquals(83, page.getMax());
    }

    public void testShouldGetRecords() {
        Page page = new Page();

        VersionedRecord record1 = new VersionedRecord();
        page.add(record1);
        VersionedRecord record2 = new VersionedRecord();
        page.add(record2);

        VersionedRecord[] records = page.getRecords();

        assertEquals(2, records.length);
        assertEquals(record1, records[0]);
        assertEquals(record2, records[1]);
    }

    public void testShouldSetRecords() {
        Page page = new Page();

        VersionedRecord record1 = new VersionedRecord();
        VersionedRecord record2 = new VersionedRecord();
        page.setRecords(new VersionedRecord[] { record1, record2 });

        VersionedRecord[] records = page.getRecords();

        assertEquals(2, records.length);
        assertEquals(record1, records[0]);
        assertEquals(record2, records[1]);
    }

    public void testShouldRemoveRecord() {
        Page page = new Page();

        page.add(new VersionedRecord());
        page.add(new VersionedRecord());

        assertTrue("Should be able to remove record 1", page.remove(1));
        assertFalse("Should be unable to remove record 12", page.remove(7));

        assertEquals(1, page.count());
    }
    
    public void testShouldConfirmYesIfRecordIsPresentOnPage() {
        Page page = new Page();

        page.setMin(1);
        page.add(new VersionedRecord());
        page.add(new VersionedRecord());

        assertTrue("record 1 should be on page", page.contains(1));
        assertTrue("record 2 should be on page", page.contains(2));
        assertFalse("record 3 should not be on page", page.contains(3));
    }
}
