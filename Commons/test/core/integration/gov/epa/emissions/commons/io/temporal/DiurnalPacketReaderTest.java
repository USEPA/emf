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

public class DiurnalPacketReaderTest extends MockObjectTestCase {

    private PacketReader reader;

    private BufferedReader fileReader;

    protected void setUp() throws Exception {
        File file = new File("test/data/temporal-profiles/diurnal-weekday.txt");

        Mock typeMapper = mock(SqlDataTypes.class);
        typeMapper.stubs().method(ANYTHING).will(returnValue("ANY"));

        FileFormat cols = new DiurnalFileFormat((SqlDataTypes) typeMapper.proxy());
        fileReader = new BufferedReader(new CustomCharSetInputStreamReader(new FileInputStream(file)));
        int lineNumber=0;
        reader = new FixedWidthPacketReader(fileReader, fileReader.readLine().trim(), cols, lineNumber);
    }

    protected void tearDown() throws Exception {
        fileReader.close();
    }

    public void testShouldIdentifyPacketHeaderAsDiurnalWeekday() {
        assertEquals("DIURNAL WEEKDAY", reader.identify());
    }

    public void testShouldReadTwentyRecordsOfTheDiurnalPacket() throws IOException, ImporterException {
        for (int i = 0; i < 20; i++) {
            Record record = reader.read();
            assertNotNull(record);
        }
    }

    public void testShouldReadFirstRecordCorrectly() throws IOException, ImporterException {
        Record record = reader.read();

        assertEquals(26, record.size());

        assertEquals("    1", record.token(0));
        assertEquals("   0", record.token(1));
        assertEquals("   0", record.token(2));
        assertEquals("   0", record.token(3));
        assertEquals("   0", record.token(4));
        assertEquals("   0", record.token(5));
        assertEquals("   0", record.token(6));
        assertEquals("   0", record.token(7));
        assertEquals("   0", record.token(8));
        assertEquals(" 125", record.token(9));
        assertEquals(" 125", record.token(10));
        assertEquals(" 125", record.token(11));
        assertEquals(" 125", record.token(12));
        assertEquals(" 125", record.token(13));
        assertEquals(" 125", record.token(14));
        assertEquals(" 125", record.token(15));
        assertEquals(" 125", record.token(16));
        assertEquals("   0", record.token(17));
        assertEquals("   0", record.token(18));
        assertEquals("   0", record.token(19));
        assertEquals("   0", record.token(20));
        assertEquals("   0", record.token(21));
        assertEquals("   0", record.token(22));
        assertEquals("   0", record.token(23));
        assertEquals("   0", record.token(24));
        assertEquals(" 1000", record.token(25));

    }

    public void testShouldReadSecondRecordCorrectly() throws IOException, ImporterException {
        reader.read(); // ignore

        Record record = reader.read();

        assertEquals(26, record.size());

        assertEquals("    2", record.token(0));
        assertEquals("   0", record.token(1));
        assertEquals("   0", record.token(2));
        assertEquals("   0", record.token(3));
        assertEquals("   0", record.token(4));
        assertEquals("   0", record.token(5));
        assertEquals("   0", record.token(6));
        assertEquals("   0", record.token(7));
        assertEquals("   0", record.token(8));
        assertEquals(" 125", record.token(9));
        assertEquals(" 125", record.token(10));
        assertEquals(" 125", record.token(11));
        assertEquals(" 125", record.token(12));
        assertEquals(" 125", record.token(13));
        assertEquals(" 125", record.token(14));
        assertEquals(" 125", record.token(15));
        assertEquals(" 125", record.token(16));
        assertEquals("   0", record.token(17));
        assertEquals("   0", record.token(18));
        assertEquals("   0", record.token(19));
        assertEquals("   0", record.token(20));
        assertEquals("   0", record.token(21));
        assertEquals("   0", record.token(22));
        assertEquals("   0", record.token(23));
        assertEquals("   0", record.token(24));
        assertEquals(" 1000", record.token(25));
    }

    public void testShouldIdentifyEndOfPacket() throws IOException, ImporterException {
        for (int i = 0; i < 20; i++) {
            assertNotNull(reader.read());
        }

        Record end = reader.read();
        assertEquals(0, end.size());
        assertTrue("Should be the Packet Terminator", end.isEnd());
    }
}
