package gov.epa.emissions.commons.io.importer;

import gov.epa.emissions.commons.Record;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

public class DelimitedFileReaderTest extends TestCase {

    private DelimitedFileReader reader;

    protected void setUp() throws Exception {
        File file = new File("test/data/orl/SimpleDelimited.txt");
        reader = new DelimitedFileReader(file, new WhitespaceDelimitedTokenizer());
    }

    protected void tearDown() throws IOException {
        reader.close();
    }

    public void testShouldAddTrailingInlineCommentsAsASingleTokenToRecord() throws IOException, ImporterException {
        Record record = reader.read();

        assertNotNull(record);
        assertEquals("! EPA-derived", record.token(7));
    }

    public void testShouldReadTenRecordsOfTheMonthlyPacket() throws IOException, ImporterException {
        for (int i = 0; i < 10; i++) {
            Record record = reader.read();
            assertNotNull(record);
        }
    }

    public void testShouldReadFirstRecordCorrectly() throws IOException, ImporterException {
        Record record = reader.read();
        assertEquals(8, record.size());
        assertEquals("37119", record.token(0));
        assertEquals("0001", record.token(1));
        assertEquals("0001", record.token(2));
        assertEquals("1", record.token(3));
        assertEquals("1", record.token(4));
        assertEquals("40201301", record.token(5));
        assertEquals("02", record.token(6));
        assertEquals("! EPA-derived", record.token(7));
    }

    public void testShouldReadSecondRecordCorrectly() throws IOException, ImporterException {
        reader.read(); // ignore

        Record record = reader.read();
        assertEquals(7, record.size());
        assertEquals("37119", record.token(0));
        assertEquals("0001", record.token(1));
        assertEquals("0001", record.token(2));
        assertEquals("1", record.token(3));
        assertEquals("1", record.token(4));
        assertEquals("40201301", record.token(5));
        assertEquals("02", record.token(6));
    }

    public void testShouldIdentifyEndOfFile() throws IOException, ImporterException {
        for (int i = 0; i < 10; i++) {
            assertNotNull(reader.read());
        }

        Record end = reader.read();
        assertEquals(0, end.size());
        assertTrue("Should be the Packet Terminator", end.isEnd());

    }

    public void testShouldCorrectlyTokenizeFileWithQuotesAfterInlineCommentChar() throws Exception {
        File file = new File("test/data/orl/SimpleDelimitedWithDiffrentDelimiters.txt");
        reader = new DelimitedFileReader(file, new SemiColonDelimitedTokenizer());

        for (int i = 0; i < 10; i++) {
            assertNotNull(reader.read());
        }

        Record semiColDelmtd = reader.read();
        assertEquals(11, semiColDelmtd.size());
        assertEquals(
                semiColDelmtd.token(10),
                "! Profile name: Surface Coating Operations - Adhesive Application; Assignment basis: Legacy (note, this profile is a \"controlle");
    }

    public void testShouldCorrectlyTokenizeFileWithExportInfo() throws Exception {
        File file = new File("test/data/orl/SimpleDelimited-with-export-info.txt");
        reader = new DelimitedFileReader(file, new WhitespaceDelimitedTokenizer());
        
        Record semiColDelmtd = reader.read();
        assertEquals(8, semiColDelmtd.size());
        assertEquals("! EPA-derived", semiColDelmtd.token(7));
        semiColDelmtd = reader.read();
        assertEquals(7, semiColDelmtd.size());
        assertEquals("02", semiColDelmtd.token(6));
    }
}
