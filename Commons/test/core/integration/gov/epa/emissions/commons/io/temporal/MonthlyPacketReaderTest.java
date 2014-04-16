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

public class MonthlyPacketReaderTest extends MockObjectTestCase {

    private PacketReader reader;
    private BufferedReader fileReader;

    protected void setUp() throws Exception {
        File file = new File("test/data/temporal-profiles/monthly.txt");

        Mock typeMapper = mock(SqlDataTypes.class);
        typeMapper.stubs().method(ANYTHING).will(returnValue("ANY"));

        FileFormat cols = new MonthlyFileFormat((SqlDataTypes) typeMapper.proxy());
        
        fileReader = new BufferedReader(new CustomCharSetInputStreamReader(new FileInputStream(file)));
        int lineNumber=0;
        reader = new FixedWidthPacketReader(fileReader, fileReader.readLine().trim(), cols, lineNumber);
    }

    protected void tearDown() throws Exception {
        fileReader.close();
    }

    public void testShouldIdentifyPacketHeaderAsMonthly() {
        assertEquals("MONTHLY", reader.identify());
    }

    public void testShouldReadTenRecordsOfTheMonthlyPacket() throws IOException, ImporterException {
        for (int i = 0; i < 10; i++) {
            Record record = reader.read();
            assertNotNull(record);
        }
    }

    public void testShouldReadFirstRecordCorrectly() throws IOException, ImporterException {
        Record record = reader.read();

        assertEquals(14, record.size());

        assertEquals("    1", record.token(0));
        assertEquals("   0", record.token(1));
        assertEquals("   0", record.token(2));
        assertEquals("   0", record.token(3));
        assertEquals("   0", record.token(4));
        assertEquals("   0", record.token(5));
        assertEquals(" 110", record.token(6));
        assertEquals(" 110", record.token(7));
        assertEquals(" 110", record.token(8));
        assertEquals(" 223", record.token(9));
        assertEquals(" 223", record.token(10));
        assertEquals(" 223", record.token(11));
        assertEquals("   0", record.token(12));
        assertEquals("  999", record.token(13));
    }

    public void testShouldReadSecondRecordCorrectly() throws IOException, ImporterException {
        reader.read(); // ignore

        Record record = reader.read();

        assertEquals(14, record.size());

        assertEquals("    2", record.token(0));
        assertEquals("   0", record.token(1));
        assertEquals("   0", record.token(2));
        assertEquals("   0", record.token(3));
        assertEquals("   0", record.token(4));
        assertEquals("   0", record.token(5));
        assertEquals(" 290", record.token(6));
        assertEquals(" 290", record.token(7));
        assertEquals(" 290", record.token(8));
        assertEquals("  43", record.token(9));
        assertEquals("  43", record.token(10));
        assertEquals("  43", record.token(11));
        assertEquals("   0", record.token(12));
        assertEquals("  999", record.token(13));
    }

    public void testShouldIdentifyEndOfPacket() throws IOException, ImporterException {
        for (int i = 0; i < 10; i++) {
            assertNotNull(reader.read());
        }

        Record end = reader.read();
        assertEquals(0, end.size());
        assertTrue("Should be the Packet Terminator", end.isEnd());

    }
}
