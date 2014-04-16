package gov.epa.emissions.commons.io.temporal;

import gov.epa.emissions.commons.Record;
import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.io.CustomCharSetInputStreamReader;
import gov.epa.emissions.commons.io.FileFormat;
import gov.epa.emissions.commons.io.importer.FixedWidthPacketReader;
import gov.epa.emissions.commons.io.importer.ImporterException;
import gov.epa.emissions.commons.io.importer.PacketReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

public class WeeklyPacketReaderTest extends MockObjectTestCase {

    private PacketReader reader;

    private BufferedReader fileReader;

    protected void setUp() throws Exception {
        File file = new File("test/data/temporal-profiles/weekly.txt");

        Mock typeMapper = mock(SqlDataTypes.class);
        typeMapper.stubs().method(ANYTHING).will(returnValue("ANY"));

        FileFormat cols = new WeeklyFileFormat((SqlDataTypes) typeMapper.proxy());
        fileReader = new BufferedReader(new CustomCharSetInputStreamReader(new FileInputStream(file)));
        int lineNumber = 0;
        reader = new FixedWidthPacketReader(fileReader, fileReader.readLine().trim(), cols, lineNumber);
    }

    protected void tearDown() throws IOException {
        fileReader.close();

    }

    public void testShouldIdentifyPacketHeaderAsWeekly() {
        assertEquals("WEEKLY", reader.identify());
    }

    public void testShouldReadThirteenRecordsOfTheWeeklyPacket() throws IOException, ImporterException {
        for (int i = 0; i < 13; i++) {
            Record record = reader.read();
            assertNotNull(record);
        }
    }

    public void testShouldReadFirstRecordCorrectly() throws IOException, ImporterException {
        Record record = reader.read();

        assertEquals(9, record.size());

        assertEquals("    1", record.token(0));
        assertEquals(" 200", record.token(1));
        assertEquals(" 200", record.token(2));
        assertEquals(" 200", record.token(3));
        assertEquals(" 200", record.token(4));
        assertEquals(" 200", record.token(5));
        assertEquals("   0", record.token(6));
        assertEquals("   0", record.token(7));
        assertEquals("  1000", record.token(8));
    }

    public void testShouldReadSecondRecordCorrectly() throws IOException, ImporterException {
        reader.read(); // ignore

        Record record = reader.read();

        assertEquals(9, record.size());

        assertEquals("    2", record.token(0));
        assertEquals(" 200", record.token(1));
        assertEquals(" 200", record.token(2));
        assertEquals(" 200", record.token(3));
        assertEquals(" 200", record.token(4));
        assertEquals(" 200", record.token(5));
        assertEquals("   0", record.token(6));
        assertEquals("   0", record.token(7));
        assertEquals("  1000", record.token(8));
    }

    public void testShouldIdentifyEndOfPacket() throws IOException, ImporterException {
        for (int i = 0; i < 13; i++) {
            assertNotNull(reader.read());
        }

        Record end = reader.read();
        assertEquals(0, end.size());
        assertTrue("Should be the Packet Terminator", end.isEnd());

    }

    public void testShouldReadCommentsAsItReadsRecords() throws IOException, ImporterException {
        for (int i = 0; i < 13; i++) {
            assertNotNull(reader.read());
        }

        assertTrue("Should be the Packet Terminator", reader.read().isEnd());

        assertEquals(3, reader.comments().size());
    }
}
