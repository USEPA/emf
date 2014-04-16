package gov.epa.emissions.commons.io;

import gov.epa.emissions.commons.io.importer.FillDefaultValues;
import gov.epa.emissions.commons.io.importer.FillDefaultValuesOfVersionedRecord;

import java.util.ArrayList;
import java.util.List;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

public class FillDefaultValuesOfVersionedRecordTest extends MockObjectTestCase {

    private FillDefaultValues filler;
    private FileFormatWithOptionalCols fileFormatProxy;

    protected void setUp() throws Exception {
        super.setUp();

        Mock fileFormat = mock(FileFormatWithOptionalCols.class);

        Column[] minCols = new Column[] { new ColumnStub(), new ColumnStub(), new ColumnStub() };
        fileFormat.stubs().method("minCols").will(returnValue(minCols));

        Column[] optionalCols = new Column[] { new ColumnStub(), new ColumnStub() };
        fileFormat.stubs().method("optionalCols").will(returnValue(optionalCols));

        Column[] cols = new Column[] { new ColumnStub(), new ColumnStub(), new ColumnStub(), new ColumnStub(),
                new ColumnStub() };
        fileFormat.stubs().method("cols").will(returnValue(cols));

        fileFormatProxy = (FileFormatWithOptionalCols) fileFormat.proxy();
        filler = new FillDefaultValuesOfVersionedRecord();
    }

    public void testShouldAddDatasetIdAndFillersForMissingOptionalColsOnFillDefaultValues() {
        // setup
        List data = new ArrayList();
        data.add("fixed1");
        data.add("fixed2");
        data.add("fixed3");
        data.add("optional1");
        data.add("!Comments");// comments
        long datasetId = 129;

        // run
        filler.fill(fileFormatProxy, data, datasetId);

        // verify
        // 4 version cols, 3 - fixed, 2 optionals, 1 Comments
        assertEquals((4 + 3 + 2 + 1), data.size());
        assertEquals("", data.get(0));// record id - filler
        assertEquals(datasetId + "", data.get(1));// dataset id
        assertEquals("0", data.get(2)); // version
        assertEquals("", data.get(3));// delete versions
        assertEquals("fixed1", data.get(4));// fixed 1
        assertEquals("fixed2", data.get(5));// fixed 2
        assertEquals("fixed3", data.get(6));// fixed 3
        assertEquals("optional1", data.get(7));// optional 1
        assertEquals("", data.get(8));// optional 2 - filler
        assertEquals("!Comments", data.get(9));// comments
    }

    public void testShouldAddDatasetIdAndFillersForOptionalColsAndCommentsOnFillDefaultValues() {
        // setup
        List data = new ArrayList();
        data.add("fixed1");
        data.add("fixed2");
        data.add("fixed3");
        data.add("op1");
        long datasetId = 129;

        // run
        filler.fill(fileFormatProxy, data, datasetId);

        // verify
        // 1 dataset id, 3 - fixed, 2 optionals, 1 Comments
        assertEquals((4 + 3 + 2 + 1), data.size());
        assertEquals("", data.get(0));// record id - filler
        assertEquals(datasetId + "", data.get(1));// dataset id
        assertEquals("0", data.get(2)); // version
        assertEquals("", data.get(3));// delete versions
        assertEquals("fixed1", data.get(4));// fixed 1
        assertEquals("fixed2", data.get(5));// fixed 2
        assertEquals("fixed3", data.get(6));// fixed 3
        assertEquals("op1", data.get(7));// optional 1 - filler
        assertEquals("", data.get(8));// optional 2 - filler
        assertEquals("", data.get(9));// comments - filler
    }

    public void testShouldAddDatasetIdAndFillersForAllOptionalColsOnFillDefaultValues() {
        // setup
        List data = new ArrayList();
        data.add("fixed1");
        data.add("fixed2");
        data.add("fixed3");
        data.add("!Comments");
        long datasetId = 129;

        // run
        filler.fill(fileFormatProxy, data, datasetId);

        // verify
        // 1 dataset id, 3 - fixed, 2 optionals, 1 Comments
        assertEquals((4 + 3 + 2 + 1), data.size());
        assertEquals("", data.get(0));// record id - filler
        assertEquals(datasetId + "", data.get(1));// dataset id
        assertEquals("0", data.get(2)); // version
        assertEquals("", data.get(3));// delete versions
        assertEquals("fixed1", data.get(4));// fixed 1
        assertEquals("fixed2", data.get(5));// fixed 2
        assertEquals("fixed3", data.get(6));// fixed 3
        assertEquals("", data.get(7));// optional 1 - filler
        assertEquals("", data.get(8));// optional 2 - filler
        assertEquals("!Comments", data.get(9));// comments
    }
    
    public void testShouldAddDatasetIdAndAddComentAndFillersForAllOptionalColsOnFillDefaultValues() {
        // setup
        List data = new ArrayList();
        data.add("fixed1");
        data.add("fixed2");
        data.add("fixed3");
        long datasetId = 129;

        // run
        filler.fill(fileFormatProxy, data, datasetId);

        // verify
        // 1 dataset id, 3 - fixed, 2 optionals, 1 Comments
        assertEquals((4 + 3 + 2 + 1), data.size());
        assertEquals("", data.get(0));// record id - filler
        assertEquals(datasetId + "", data.get(1));// dataset id
        assertEquals("0", data.get(2)); // version
        assertEquals("", data.get(3));// delete versions
        assertEquals("fixed1", data.get(4));// fixed 1
        assertEquals("fixed2", data.get(5));// fixed 2
        assertEquals("fixed3", data.get(6));// fixed 3
        assertEquals("", data.get(7));// optional 1 - filler
        assertEquals("", data.get(8));// optional 2 - filler
        assertEquals("", data.get(9));// comments
    }

    public class ColumnStub extends Column {
        public ColumnStub() {
            super(null, null);
        }
    }
}
