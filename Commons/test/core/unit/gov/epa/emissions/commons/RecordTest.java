package gov.epa.emissions.commons;

import junit.framework.TestCase;

public class RecordTest extends TestCase {

    public void testShouldGetAllTokens() {
        Record record = new Record();
        record.add("1");
        record.add("2");

        String[] tokens = record.getTokens();
        assertEquals(2, tokens.length);
        assertEquals("1", tokens[0]);
        assertEquals("2", tokens[1]);
    }

    public void testShouldSetAllTokens() {
        Record record = new Record();
        record.setTokens(new String[] { "1", "2" });

        String[] tokens = record.getTokens();
        assertEquals(2, tokens.length);
        assertEquals("1", tokens[0]);
        assertEquals("2", tokens[1]);
    }
    
    public void testReplaceTokenAtSpecificPosition() {
        Record record = new Record();
        record.setTokens(new String[] { "1", "2" });

        record.replace(1, "modified");
        
        String[] tokens = record.getTokens();
        assertEquals(2, tokens.length);
        assertEquals("1", tokens[0]);
        assertEquals("modified", tokens[1]);
        
    }
}
