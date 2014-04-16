package gov.epa.emissions.commons.db.version;

import junit.framework.TestCase;

public class VersionedRecordTest extends TestCase {

    public void testShouldNotAppendCommentsIfItAlreadyExistsOnFlatteningData() {
        VersionedRecord record = new VersionedRecord();
        record.setDatasetId(2);
        record.setDeleteVersions("4, 5");

        record.add("val1");
        record.add("val2");
        record.add("!comment");

        Version version = new Version();
        version.setVersion(1);
        
        String[] results = record.dataForInsertion(version);

        assertEquals(4 + 3, results.length);

        assertEquals("", results[0]);
        assertEquals("2", results[1]);
        assertEquals("1", results[2]);
        assertEquals("", results[3]);
        assertEquals("val1", results[4]);
        assertEquals("val2", results[5]);
        assertEquals("!comment", results[6]);
    }

}
