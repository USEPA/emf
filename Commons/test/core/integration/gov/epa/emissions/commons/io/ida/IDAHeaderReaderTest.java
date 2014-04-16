package gov.epa.emissions.commons.io.ida;

import gov.epa.emissions.commons.io.importer.PersistenceTestCase;

import java.io.File;

public class IDAHeaderReaderTest extends PersistenceTestCase {

    public void testShouldIdentifyPollutants() throws Exception {
        File file = new File("test/data/ida/small-area.txt");
        IDAHeaderReader headerReader = new IDAHeaderReader(file);
        headerReader.read();

        // assert
        String[] polls = headerReader.polluntants();
        assertEquals(polls[0], "VOC");
        assertEquals(polls[1], "NOX");
        assertEquals(polls[2], "CO");
        assertEquals(polls[3], "SO2");
        assertEquals(polls[4], "PM10");
        assertEquals(polls[5], "PM2_5");
        assertEquals(polls[6], "NH3");
        assertEquals(10, headerReader.comments().size());
    }

    public void testShouldIdentifyNoPollutantsSpecified() throws Exception {
        File file = new File("test/data/ida/no-pollutants.txt");
        IDAHeaderReader headerReader = new IDAHeaderReader(file);
        try {
            headerReader.read();
        } catch (Exception e) {
            assertTrue(e.getMessage().endsWith("No pollutants specified"));
            return;
        }

        fail("Should have failed since pollutants were not specified");
    }

    protected void doTearDown() throws Exception {//no op
    }

}
