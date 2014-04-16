package gov.epa.emissions.commons.io.orl;

import gov.epa.emissions.commons.Record;
import gov.epa.emissions.commons.db.SqlDataTypes;
import gov.epa.emissions.commons.io.importer.DelimiterIdentifyingFileReader;
import gov.epa.emissions.commons.io.importer.PersistenceTestCase;
import gov.epa.emissions.commons.io.importer.Reader;

import java.io.File;
import java.io.IOException;

public class ORLReaderTest extends PersistenceTestCase {

    private Reader reader;

    private String dataFolder = "test/data/orl/nc";

    private SqlDataTypes sqlDataTypes;

    protected void setUp() throws Exception {
        super.setUp();

        sqlDataTypes = dbServer().getSqlDataTypes();
    }

    protected void doTearDown() throws IOException {
        reader.close();
    }

    public void testShouldIdentifyFirstSixLinesOfSmallPointFileAsComments() throws Exception {
        File file = new File(dataFolder, "small-point.txt");
        reader = new DelimiterIdentifyingFileReader(file, new ORLPointFileFormat(sqlDataTypes).minCols().length);

        assertNotNull(reader.read());
        assertEquals(6, reader.comments().size());
    }

    public void testShouldThrowExceptionOnSmallPointFileWithDiffrentDelimiter() throws Exception {
        File file = new File(dataFolder, "small-point-with-different-delimiters.txt");
        reader = new DelimiterIdentifyingFileReader(file, new ORLPointFileFormat(sqlDataTypes).minCols().length);
        
        reader.read();
        try {
            reader.read();
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("Could not find 27 of ' ' delimiters on the line."));
        }
    }

    public void testShouldThrowExceptionOnSmallPointFileWithDiffrentDelimiterOnFirstLine() throws Exception {
        File file = new File(dataFolder, "small-point-with-different-delimiters-on-first-line.txt");
        reader = new DelimiterIdentifyingFileReader(file, new ORLPointFileFormat(sqlDataTypes).minCols().length);
        
        reader.read();
        try {
            reader.read();
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("Could not find 27 of ',' delimiters on the line."));
        }
    }

    public void testShouldCollectAllTenCommentsOfSmallPointFile() throws Exception {
        File file = new File(dataFolder, "small-point.txt");
        reader = new DelimiterIdentifyingFileReader(file, new ORLPointFileFormat(sqlDataTypes).minCols().length);

        reader.read();
        assertEquals(6, reader.comments().size());

        for (int i = 0; i < 8; i++) {
            reader.read();
        }
        assertEquals(7, reader.comments().size());

        reader.read();
        assertTrue(reader.read().isEnd());
        assertEquals(8, reader.comments().size());
    }

    public void testShouldCreateRecordWithTwelveTokensForEachLineOfSmallNonPointFile() throws Exception {
        File file = new File(dataFolder, "small-nonpoint.txt");
        reader = new DelimiterIdentifyingFileReader(file, new ORLNonPointFileFormat(sqlDataTypes).minCols().length);

        Record record = reader.read();
        assertNotNull(record);
        assertEquals(12, record.size());
    }

    public void testVariationsOfDelimiterWidthsAndQuotesInAPointFile() throws Exception {
        File file = new File(dataFolder, "point-with-variations.txt");
        reader = new DelimiterIdentifyingFileReader(file, new ORLPointFileFormat(sqlDataTypes).minCols().length);

        for (int i = 0; i < 4; i++) {
            assertEquals(29, reader.read().size());
        }

        assertTrue(reader.read().isEnd());
    }

    public void testVariationsOfDelimiterWidthsAndQuotesInTheSmallPointFile() throws Exception {
        File file = new File(dataFolder, "small-point.txt");
        reader = new DelimiterIdentifyingFileReader(file, new ORLPointFileFormat(sqlDataTypes).minCols().length);

        for (int i = 0; i < 10; i++) {
            if (i == 0 || i == 1 || i == 4 || i == 9)
                assertEquals(28, reader.read().size());
            else
                assertEquals(28, reader.read().size());
        }

        assertTrue(reader.read().isEnd());
    }
}
