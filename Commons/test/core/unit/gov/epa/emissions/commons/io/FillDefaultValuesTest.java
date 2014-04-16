package gov.epa.emissions.commons.io;

import gov.epa.emissions.commons.io.importer.FillRecordWithBlankValues;

import java.util.ArrayList;
import java.util.List;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

public class FillDefaultValuesTest extends MockObjectTestCase {

    private FileFormatWithOptionalCols fileFormatProxy;

    private FillRecordWithBlankValues filler;

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
        filler = new FillRecordWithBlankValues();
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
        // 1 dataset id, 3 - fixed, 2 optionals, 1 Comments
        assertEquals((1 + 3 + 2 + 1), data.size());
        assertEquals(datasetId + "", data.get(0));// dataset id
        assertEquals("fixed1", data.get(1));// fixed 1
        assertEquals("fixed2", data.get(2));// fixed 2
        assertEquals("fixed3", data.get(3));// fixed 3
        assertEquals("optional1", data.get(4));// optional 1
        assertEquals("", data.get(5));// optional 2 - filler
        assertEquals("!Comments", data.get(6));// comments
    }

    public void testShouldAddDatasetIdAndFillersForOptionalColsAndCommentsOnFillDefaultValues() {
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
        assertEquals((1 + 3 + 2 + 1), data.size());
        assertEquals(datasetId + "", data.get(0));// dataset id
        assertEquals("fixed1", data.get(1));// fixed 1
        assertEquals("fixed2", data.get(2));// fixed 2
        assertEquals("fixed3", data.get(3));// fixed 3
        assertEquals("", data.get(4));// optional 1 - filler
        assertEquals("", data.get(5));// optional 2 - filler
        assertEquals("", data.get(6));// comments - filler
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
        assertEquals((1 + 3 + 2 + 1), data.size());
        assertEquals(datasetId + "", data.get(0));// dataset id
        assertEquals("fixed1", data.get(1));// fixed 1
        assertEquals("fixed2", data.get(2));// fixed 2
        assertEquals("fixed3", data.get(3));// fixed 3
        assertEquals("", data.get(4));// optional 1 - filler
        assertEquals("", data.get(5));// optional 2 - filler
        assertEquals("!Comments", data.get(6));// comments
    }

    public class ColumnStub extends Column {
        public ColumnStub() {
            super(null, null);
        }
    }
}
